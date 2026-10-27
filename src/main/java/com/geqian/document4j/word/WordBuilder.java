package com.geqian.document4j.word;

import com.geqian.document4j.common.core.AbstractDocumentBuilder;
import com.geqian.document4j.common.core.TableSupport;
import com.geqian.document4j.common.core.WriteTableable;
import com.geqian.document4j.common.extend.Converter;
import org.apache.poi.util.Units;
import org.apache.poi.xwpf.usermodel.*;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.reflect.Field;
import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author geqian
 * @date 15:18 2024/3/9
 */
public class WordBuilder extends AbstractDocumentBuilder<WordBuilder, WordStyle> {

    private final Logger log = LoggerFactory.getLogger(WordBuilder.class);

    private final XWPFDocument document;

    /**
     * Word 中文版默认正文字体（主题字体 minorEastAsia）
     */
    private static final String DEFAULT_FONT = "等线";

    private WordBuilder(XWPFDocument document) {
        this.document = document;
        XWPFStyles styles = document.getStyles();
        // 仅对新建文档写入默认样式；模板自带样式时保持模板定义
        if (styles == null) {
            styles = document.createStyles();
            ensureDocumentDefaults(styles);
            ensureNormalStyle(styles);
        }
        // 确保标题样式已定义，使 Word/WPS 导航窗格能按层次识别标题
        ensureHeadingStyles(styles);
    }

    /**
     * 创建一个WordBuilder
     *
     * @return
     */
    public static WordBuilder create() {
        return new WordBuilder(new XWPFDocument());
    }

    /**
     * 基于模板创建一个WordBuilder
     *
     * @return
     */
    public static WordBuilder create(InputStream template) {
        try {
            return new WordBuilder(new XWPFDocument(template));
        } catch (IOException e) {
            throw new RuntimeException("获取word模板时发生异常", e);
        }
    }


    /**
     * 空白行
     *
     * @return
     */
    public WordBuilder blankRow() {
        //创建段落
        XWPFParagraph paragraph = document.createParagraph();
        // 与正文段落保持一致的显式间距（单倍行距、段后 0），避免 Word/WPS 对空白行渲染高度不一致
        applyDefaultSpacing(paragraph);
        // 对齐方式 paragraph.setAlignment(ParagraphAlignment.CENTER);
        return this;
    }

    /**
     * 取消段落"如果定义了文档网格，则对齐到网格"。
     * <p>
     * 文档定义了网格（sectPr 中的 docGrid，Word 新建文档默认携带）时，段落默认按网格对齐，
     * Word/WPS 会把段落的段前/段后间距、行距按文档网格取整渲染，
     * 导致 XML 中已是段前段后 0、单倍行距的段落，实际显示仍有额外间距或非单倍行距。
     * 写入 {@code <w:snapToGrid w:val="0"/>} 等价于在 Word 中取消勾选
     * "如果定义了文档网格，则对齐到网格(W)"，使间距与行距严格按显式设置渲染。
     *
     * @param paragraph 段落
     */
    private void setSnapToGridOff(XWPFParagraph paragraph) {
        CTPPr ppr = paragraph.getCTP().getPPr();
        if (ppr == null) {
            ppr = paragraph.getCTP().addNewPPr();
        }
        // 复用已存在的元素，避免重复写入多个 w:snapToGrid
        CTOnOff snapToGrid = ppr.isSetSnapToGrid() ? ppr.getSnapToGrid() : ppr.addNewSnapToGrid();
        snapToGrid.setVal(false);
    }


    /**
     * 添加一个新页面
     *
     * @return
     */
    public WordBuilder newPage() {
        //创建段落
        XWPFParagraph paragraph = document.createParagraph();
        // 分页符段落同样关闭网格对齐，避免其行高被文档网格取整
        setSnapToGridOff(paragraph);
        XWPFRun run = paragraph.createRun();
        run.addBreak(BreakType.PAGE);
        return this;
    }


    /**
     * 添加段落文本
     *
     * @return
     */
    public WordBuilder heading(String text, WordStyle heading, List<WordStyle> styles) {
        if (!(heading instanceof WordStyle.Heading)) {
            throw new IllegalArgumentException("WordStyle.Heading cannot be null");
        }
        // 标题样式最先应用，用户自定义样式（如 spacing/spacingBefore/Font 等）随后应用，
        // 以覆盖标题的默认值（如段前段后间距、字号等），否则 Heading 会覆盖用户传入的样式
        List<WordStyle> allStyles = new ArrayList<>();
        allStyles.add(heading);
        if (styles != null) {
            allStyles.addAll(styles);
        }
        this.paragraph(document.createParagraph(), text, allStyles);
        return this;
    }


    /**
     * 添加段落文本
     *
     * @return
     */
    public WordBuilder paragraph(String text, List<WordStyle> wordStyles) {
        //创建段落
        XWPFParagraph paragraph = document.createParagraph();
        this.paragraph(paragraph, text, wordStyles);
        return this;
    }

    /**
     * 添加无序列表（每项一个段落）
     */
    public WordBuilder list(List<String> elements, List<WordStyle> wordStyles) {
        if (elements != null) {
            for (String element : elements) {
                paragraph("• " + (element == null ? "" : element), wordStyles);
            }
        }
        return this;
    }

    /**
     * 添加链接
     */
    public WordBuilder link(String text, String link, List<WordStyle> wordStyles) {
        XWPFParagraph paragraph = document.createParagraph();
        applyDefaultSpacing(paragraph);
        XWPFRun run = paragraph.createHyperlinkRun(link);
        run.setText(text);
        // Word 默认超链接样式：蓝色下划线
        run.setColor("0563C1");
        run.setUnderline(UnderlinePatterns.SINGLE);
        applyStyles(paragraph, run, wordStyles);
        return this;
    }

    /**
     * 添加分割线（段落底边框）
     */
    public WordBuilder divisionLine() {
        XWPFParagraph paragraph = document.createParagraph();
        applyDefaultSpacing(paragraph);
        CTPPr ppr = paragraph.getCTP().isSetPPr() ? paragraph.getCTP().getPPr() : paragraph.getCTP().addNewPPr();
        CTPBdr borders = ppr.isSetPBdr() ? ppr.getPBdr() : ppr.addNewPBdr();
        CTBorder bottom = borders.isSetBottom() ? borders.getBottom() : borders.addNewBottom();
        bottom.setVal(STBorder.SINGLE);
        // 0.75 磅（1/8 磅为单位）
        bottom.setSz(BigInteger.valueOf(6));
        bottom.setSpace(BigInteger.valueOf(1));
        bottom.setColor("000000");
        return this;
    }

    /**
     * 添加段落文本
     *
     * @return
     */
    private void paragraph(XWPFParagraph paragraph, String text, List<WordStyle> wordStyles) {
        // 显式设置段落行距与段后间距，避免 Word 与 WPS 因各自内置 Normal 样式默认值
        // （Word：段后 8 磅、1.08 倍行距；WPS：段后 0、单倍行距）不同而导致显示不一致
        applyDefaultSpacing(paragraph);
        //创建段落文本
        XWPFRun run = paragraph.createRun();
        run.setText(text);
        applyStyles(paragraph, run, wordStyles);
    }

    /**
     * 应用样式：段落级样式作用于段落，其余（字体样式）作用于文本
     */
    private void applyStyles(XWPFParagraph paragraph, XWPFRun run, List<WordStyle> wordStyles) {
        if (wordStyles == null) {
            return;
        }
        for (WordStyle wordStyle : wordStyles) {
            if (wordStyle != null) {
                boolean isParagraph = WordStyle.Paragraph.class.isAssignableFrom(wordStyle.getClass()) || WordStyle.Heading.class.isAssignableFrom(wordStyle.getClass());
                wordStyle.buildStyle(isParagraph ? paragraph : run);
            }
        }
    }

    /**
     * 为段落设置统一的显式间距：单倍行距、段后 0。
     * 段落显式属性优先于样式继承，可保证 Word 与 WPS 渲染一致；
     * 后续通过 WordStyle 设置的间距（如 spacingBefore/spacingAfter/Heading）会覆盖其中的对应值。
     *
     * @param paragraph 段落
     */
    private void applyDefaultSpacing(XWPFParagraph paragraph) {
        paragraph.setSpacingBefore(0);
        paragraph.setSpacingBetween(1, LineSpacingRule.AUTO);
        paragraph.setSpacingAfter(0);
        // 关闭"如果定义了文档网格，则对齐到网格"，否则 Word/WPS 会把上述显式间距
        // 与行距按文档网格取整渲染（表现为段前段后 0、单倍行距但实际显示不一致）
        setSnapToGridOff(paragraph);
    }

    /**
     * 写入与 Office Word 一致的文档默认格式（docDefaults）：等线 11pt，段后 8 磅、1.08 倍行距。
     * <p>
     * POI 新建的文档没有 docDefaults，Word 会按内置默认值渲染，
     * 而网页预览（docx-preview 等）按浏览器默认值渲染（Times New Roman 16px、无段后间距），
     * 未显式设置样式的文本在两端的显示因此不一致；显式写入 Word 的默认值后两端渲染一致。
     */
    private void ensureDocumentDefaults(XWPFStyles styles) {
        CTStyles ctStyles = styles.getCtStyles();
        if (ctStyles.isSetDocDefaults()) {
            return;
        }
        CTDocDefaults docDefaults = ctStyles.addNewDocDefaults();

        CTRPr rPr = docDefaults.addNewRPrDefault().addNewRPr();
        CTFonts fonts = rPr.addNewRFonts();
        fonts.setAscii(DEFAULT_FONT);
        fonts.setHAnsi(DEFAULT_FONT);
        fonts.setEastAsia(DEFAULT_FONT);
        fonts.setCs(DEFAULT_FONT);
        // 11 磅（半磅为单位）
        rPr.addNewSz().setVal(BigInteger.valueOf(22));
        rPr.addNewSzCs().setVal(BigInteger.valueOf(22));

        CTSpacing spacing = docDefaults.addNewPPrDefault().addNewPPr().addNewSpacing();
        // 段后 8 磅（1/20 磅为单位）、1.08 倍行距（240 * 1.08 ≈ 259）
        spacing.setAfter(BigInteger.valueOf(160));
        spacing.setLine(BigInteger.valueOf(259));
        spacing.setLineRule(STLineSpacingRule.AUTO);
    }

    /**
     * 定义默认段落样式 Normal（正文），使 Word 样式面板与预览均以该样式作为基准。
     * 具体格式继承 docDefaults。
     */
    private void ensureNormalStyle(XWPFStyles styles) {
        if (styles.getStyle("Normal") != null) {
            return;
        }
        CTStyle ctStyle = CTStyle.Factory.newInstance();
        ctStyle.setType(STStyleType.PARAGRAPH);
        ctStyle.setStyleId("Normal");
        ctStyle.setDefault(true);
        ctStyle.addNewName().setVal("Normal");
        ctStyle.addNewQFormat();
        styles.addStyle(new XWPFStyle(ctStyle));
    }

    /**
     * 确保文档样式中已定义 Word 内置标题样式 Heading1~Heading9（含大纲级别）。
     * <p>
     * Word/WPS 的导航窗格（文档结构图）依据"标题样式或大纲级别"展示标题层级。
     * POI 新建的文档默认没有 styles.xml，此时段落 pStyle="HeadingN" 引用的样式未定义，
     * Word/WPS 无法将该段落识别为标题样式（样式面板显示为"正文"），标题无法按层次稳定显示。
     * 此方法在样式缺失时补充标准标题样式定义；模板或其他来源已自带 Heading1 时跳过，避免覆盖。
     */
    private void ensureHeadingStyles(XWPFStyles styles) {
        // 模板或其他来源已定义标题样式时，直接复用
        if (styles.getStyle("Heading1") != null) {
            return;
        }
        for (int i = 1; i <= 9; i++) {
            CTStyle ctStyle = CTStyle.Factory.newInstance();
            ctStyle.setType(STStyleType.PARAGRAPH);
            ctStyle.setStyleId("Heading" + i);
            ctStyle.addNewName().setVal("heading " + i);
            // 样式优先级：决定样式面板中的推荐顺序
            ctStyle.addNewUiPriority().setVal(BigInteger.valueOf(9 + (i - 1)));
            ctStyle.addNewQFormat();
            // 大纲级别：决定导航窗格层级（0 = 一级标题）
            CTPPrGeneral ppr = ctStyle.addNewPPr();
            ppr.addNewKeepNext();
            ppr.addNewKeepLines();
            ppr.addNewOutlineLvl().setVal(BigInteger.valueOf(i - 1));
            // 与 Heading 中 run 的显式格式保持一致：字号 22pt 起逐级递减、加粗
            CTRPr rpr = ctStyle.addNewRPr();
            BigInteger sz = BigInteger.valueOf(44 - (i - 1) * 4);
            rpr.addNewSz().setVal(sz);
            rpr.addNewSzCs().setVal(sz);
            rpr.addNewB().setVal(true);
            styles.addStyle(new XWPFStyle(ctStyle));
        }
    }


    /**
     * 添加图片
     *
     * @param bytes
     * @return
     * @throws Exception
     */
    public WordBuilder picture(byte[] bytes, String filename, int width, int height) {
        try (InputStream is = new ByteArrayInputStream(bytes)) {
            return picture(is, filename, width, height);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * 添加图片
     *
     * @param is
     * @return
     * @throws Exception
     */
    public WordBuilder picture(InputStream is, String filename, int width, int height) {
        try {
            XWPFParagraph paragraph = document.createParagraph();
            // 关闭网格对齐，避免图片所在段落的行高被文档网格取整
            setSnapToGridOff(paragraph);
            XWPFRun run = paragraph.createRun();
            run.addPicture(is, XWPFDocument.PICTURE_TYPE_PNG, filename, Units.toEMU(width), Units.toEMU(height));
            return this;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * 添加表格
     *
     * @param dataList 数据列表
     */
    public WordBuilder table(List<? extends WriteTableable> dataList, List<WordStyle> wordStyles) {

        if (dataList != null && !(dataList = dataList.stream().filter(Objects::nonNull).collect(Collectors.toList())).isEmpty()) {

            WriteTableable writeTableable = dataList.get(0);

            TableData tableData = resolveTable(writeTableable);

            int rows = dataList.size() + 1;

            int columns = tableData.fields.size();

            XWPFTable table = createTable(rows, columns);

            if (writeTableable instanceof WordTableInterceptor) {
                ((WordTableInterceptor) writeTableable).interceptWordTable(table);
            }

            //写入表头
            writeTableHeaders(table, tableData.headers, writeTableable, tableData.fields, wordStyles);

            //表格内容开始行数
            int startRow = 1;

            //写入表格主体数据
            writeTableRows(table, startRow, dataList, tableData.fields, tableData.enumMappings, tableData.converters, wordStyles);
        }
        return this;
    }


    /**
     * 数据写入表格
     *
     * @param rows 数据列表
     */
    public WordBuilder table(Object[][] rows, List<WordStyle> wordStyles) {
        int row;
        int column;
        if (rows != null && (rows = Stream.of(rows).filter(Objects::nonNull).toArray(Object[][]::new)).length > 0) {
            //有效行数
            row = rows.length;
            Optional<Object[]> optional = Stream.of(rows).max(Comparator.comparingInt(array -> array == null ? 0 : array.length));
            if (optional.isPresent()) {
                //子数组长度最大的长度作为列数
                column = optional.get().length;
                //创建表格
                XWPFTable table = createTable(row, column);
                for (int i = 0; i < row; i++) {
                    for (int j = 0; j < rows[i].length; j++) {
                        XWPFParagraph paragraph = table.getRow(i).getCell(j).getParagraphs().get(0);
                        Object cellValue = rows[i][j];
                        //null 写作空字符串，避免字面量 "null" 出现在文档中
                        paragraph(paragraph, cellValue == null ? "" : String.valueOf(cellValue), wordStyles);
                    }
                }
            }
        }
        return this;
    }


    /**
     * 段落模式
     *
     * @return
     */
    public ParagraphBuilder startParagraph() {
        return new ParagraphBuilder();
    }


    /**
     * word文档转换为字节数组
     *
     * @return
     */
    public byte[] asBytes() {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            asOutputStream(outputStream);
            return outputStream.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * word文档写入输出流
     */
    @Override
    public void asOutputStream(OutputStream outputStream) {
        try {
            //写出前确保文档带有页面设置，保证各端渲染一致
            ensureDefaultSectionProperties();
            document.write(outputStream);
        } catch (IOException e) {
            throw new RuntimeException("构建Word文件时发生异常", e);
        } finally {
            //关闭word文档对象
            closeResources(document);
        }
    }


    /**
     * 确保文档包含 sectPr 页面设置（A4 纵向、上下页边距 1440 缇/2.54cm、左右 1800 缇/3.17cm）。
     * <p>
     * POI 新建的文档默认没有 sectPr，MS Word/WPS 会按自身内置默认页边距渲染，
     * 而部分预览组件（如 docx-preview）不会补充默认值，内容会贴着页面边缘渲染；
     * 显式写入与 Word 默认值一致的页面设置，保证各端渲染一致。
     * 模板等来源已自带页面设置时跳过，避免覆盖。
     */
    private void ensureDefaultSectionProperties() {
        CTBody body = document.getDocument().getBody();
        if (body == null || body.isSetSectPr()) {
            return;
        }
        CTSectPr sectPr = body.addNewSectPr();
        CTPageSz pageSize = sectPr.addNewPgSz();
        pageSize.setW(BigInteger.valueOf(11906));
        pageSize.setH(BigInteger.valueOf(16838));
        CTPageMar pageMargin = sectPr.addNewPgMar();
        pageMargin.setTop(BigInteger.valueOf(1440));
        pageMargin.setBottom(BigInteger.valueOf(1440));
        pageMargin.setLeft(BigInteger.valueOf(1800));
        pageMargin.setRight(BigInteger.valueOf(1800));
        pageMargin.setHeader(BigInteger.valueOf(851));
        pageMargin.setFooter(BigInteger.valueOf(992));
        pageMargin.setGutter(BigInteger.ZERO);
    }


    /**
     * 设置段落首行缩进字符个数
     *
     * @param paragraph
     * @param indent
     */
    private void setFirstLineIndent(XWPFParagraph paragraph, int indent) {
        // 设置段落的首行缩进
        CTPPr ppr = paragraph.getCTP().getPPr();
        if (ppr == null) {
            ppr = paragraph.getCTP().addNewPPr();
        }
        CTInd ind = ppr.getInd();
        if (ind == null) {
            ind = ppr.addNewInd();
        }
        ind.setFirstLineChars(BigInteger.valueOf(100L * indent));
    }


    /**
     * 创建表格
     *
     * @param rows
     * @param columns
     * @return
     */
    private XWPFTable createTable(int rows, int columns) {
        //创建表格
        XWPFTable table = document.createTable(rows, columns);
        //设置表格样式
        setTableStyle(table);

        return table;
    }


    /**
     * 写入表头
     *
     * @param table
     * @param tableHeaders
     * @param wordStyles
     */
    private void writeTableHeaders(XWPFTable table, List<String> tableHeaders, WriteTableable obj, List<Field> fields, List<WordStyle> wordStyles) {

        for (int i = 0; i < tableHeaders.size(); i++) {

            XWPFTableCell cell = table.getRow(0).getCell(i);

            XWPFParagraph paragraph = cell.getParagraphs().get(0);

            paragraph(paragraph, tableHeaders.get(i), wordStyles);

            if (obj instanceof WordTableInterceptor) {
                ((WordTableInterceptor) obj).interceptWordHeaderCell(cell, paragraph, obj.getClass());
            }
        }
    }

    /**
     * 写入表格多行数据
     *
     * @param table
     * @param dataList
     * @param fields
     * @param wordStyles
     */
    private void writeTableRows(XWPFTable table, int startRow, List<? extends WriteTableable> dataList, List<Field> fields,
                                Map<String, String> enumMappings, Map<String, Converter> converters, List<WordStyle> wordStyles) {
        for (int i = 0; i < dataList.size(); i++) {
            writeTableRow(table, i + startRow, dataList.get(i), fields, enumMappings, converters, wordStyles);
        }
    }


    /**
     * 写入表格一行数据
     *
     * @param table
     * @param rowIndex
     * @param obj
     * @param fieldList
     * @param wordStyles
     */
    private void writeTableRow(XWPFTable table, int rowIndex, WriteTableable obj, List<Field> fieldList,
                               Map<String, String> enumMappings, Map<String, Converter> converters, List<WordStyle> wordStyles) {
        for (int i = 0; i < fieldList.size(); i++) {
            XWPFTableCell cell = table.getRow(rowIndex).getCell(i);
            XWPFParagraph paragraph = cell.getParagraphs().get(0);
            String cellValue = TableSupport.getCellValue(obj, fieldList.get(i), enumMappings, converters);
            paragraph(paragraph, cellValue, wordStyles);
            //拦截单元格
            if (obj instanceof WordTableInterceptor) {
                ((WordTableInterceptor) obj).interceptWordContentCell(cell, paragraph, obj, fieldList.get(i));
            }
        }
    }

    /**
     * 应用单线边框并显式写入边框宽度、间距与颜色
     *
     * @param border 边框元素
     */
    private void applySingleBorder(CTBorder border) {
        border.setVal(STBorder.SINGLE);
        // OOXML 规范中缺失 w:sz/w:space/w:color 时由阅读端按默认值渲染（0.5 磅黑色实线），
        // 但部分预览组件（如 docx-preview）不会补默认值，缺失这些属性时边框不渲染，需显式写入。
        // w:sz 单位为 1/8 磅，4 表示 0.5 磅
        border.setSz(BigInteger.valueOf(4));
        border.setSpace(BigInteger.valueOf(0));
        border.setColor("000000");
    }

    /**
     * 设置表格样式
     *
     * @param table
     */
    private void setTableStyle(XWPFTable table) {
        // 注意：OOXML 规范中 tblPr 元素最多只能出现一次，必须复用同一个 CTTblPr，
        // 不能重复调用 addNewTblPr()，否则生成的文档中会出现多个 tblPr，
        // 导致 Word 与 WPS 对表格宽度/边框/单元格边距等属性解析不一致
        CTTblPr tblPr = table.getCTTbl().getTblPr();
        if (tblPr == null) {
            tblPr = table.getCTTbl().addNewTblPr();
        }

        // 设置表格宽度：固定 8270 缇（约 14.6cm），显式指定 DXA 类型，
        // 确保 Word/WPS 均按该宽度渲染，而不是各自按内容自适应
        CTTblWidth tblW = tblPr.isSetTblW() ? tblPr.getTblW() : tblPr.addNewTblW();
        tblW.setType(STTblWidth.DXA);
        tblW.setW(BigInteger.valueOf(8270));

        // 设置表格居中对齐
        CTJcTable jc = tblPr.isSetJc() ? tblPr.getJc() : tblPr.addNewJc();
        jc.setVal(STJcTable.CENTER);

        // 设置表格边框线条类型（createTable 默认已生成边框，需复用已有元素，避免重复添加）
        CTTblBorders borders = tblPr.isSetTblBorders() ? tblPr.getTblBorders() : tblPr.addNewTblBorders();
        applySingleBorder(borders.isSetTop() ? borders.getTop() : borders.addNewTop());
        applySingleBorder(borders.isSetBottom() ? borders.getBottom() : borders.addNewBottom());
        applySingleBorder(borders.isSetLeft() ? borders.getLeft() : borders.addNewLeft());
        applySingleBorder(borders.isSetRight() ? borders.getRight() : borders.addNewRight());
        applySingleBorder(borders.isSetInsideH() ? borders.getInsideH() : borders.addNewInsideH());
        applySingleBorder(borders.isSetInsideV() ? borders.getInsideV() : borders.addNewInsideV());

        // 设置单元格边距：上下 70 缇，左右 108 缇（0.19cm，OOXML 标准表格默认边距）
        // 必须写成 left/right：start/end 是 Strict 命名，Word 能识别，
        // 但 docx-preview 等网页预览组件只解析 left/right，缺失时预览中文字紧贴边框
        CTTblCellMar cellMar = tblPr.isSetTblCellMar() ? tblPr.getTblCellMar() : tblPr.addNewTblCellMar();
        CTTblWidth top = cellMar.isSetTop() ? cellMar.getTop() : cellMar.addNewTop();
        top.setType(STTblWidth.DXA);
        top.setW(BigInteger.valueOf(70));
        CTTblWidth left = cellMar.isSetLeft() ? cellMar.getLeft() : cellMar.addNewLeft();
        left.setType(STTblWidth.DXA);
        left.setW(BigInteger.valueOf(108));
        CTTblWidth bottom = cellMar.isSetBottom() ? cellMar.getBottom() : cellMar.addNewBottom();
        bottom.setType(STTblWidth.DXA);
        bottom.setW(BigInteger.valueOf(70));
        CTTblWidth right = cellMar.isSetRight() ? cellMar.getRight() : cellMar.addNewRight();
        right.setType(STTblWidth.DXA);
        right.setW(BigInteger.valueOf(108));
    }

    /**
     * 关闭资源
     */
    private void closeResources(Closeable... closeables) {
        for (Closeable closeable : closeables) {
            if (closeable != null) {
                try {
                    closeable.close();
                } catch (IOException e) {
                    log.error("释放资源发生异常", e);
                }
            }
        }

    }


    public class ParagraphBuilder {

        private final XWPFParagraph paragraph;

        public ParagraphBuilder() {
            paragraph = WordBuilder.this.document.createParagraph();
        }

        public ParagraphBuilder text(String text, WordStyle... wordStyles) {
            WordBuilder.this.paragraph(paragraph, text, wordStyles == null ? Collections.<WordStyle>emptyList() : Arrays.asList(wordStyles));
            return this;
        }

        public WordBuilder endParagraph() {
            return WordBuilder.this;
        }

    }
}