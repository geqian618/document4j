package com.geqian.document4j.word;

import com.geqian.document4j.common.annotation.TableField;
import com.geqian.document4j.common.core.BaseTableInterceptor;
import com.geqian.document4j.common.extend.Converter;
import com.geqian.document4j.common.extend.NoConverter;
import com.geqian.document4j.common.core.WriteTableable;
import org.apache.poi.util.Units;
import org.apache.poi.xwpf.usermodel.*;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.reflect.Field;
import java.math.BigInteger;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author geqian
 * @date 15:18 2024/3/9
 */
public class WordBuilder {

    private final Logger log = LoggerFactory.getLogger(WordBuilder.class);

    private final XWPFDocument document;

    private Map<String, String> enumMappings;

    private Map<String, Converter> converters;

    private WordBuilder(XWPFDocument document) {
        this.document = document;
        // 确保标题样式已定义，使 Word/WPS 导航窗格能按层次识别标题
        ensureHeadingStyles();
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
     * 添加一个新页面
     *
     * @return
     */
    public WordBuilder newPage() {
        //创建段落
        XWPFParagraph paragraph = document.createParagraph();
        XWPFRun run = paragraph.createRun();
        run.addBreak(BreakType.PAGE);
        return this;
    }


    /**
     * 添加段落文本
     *
     * @return
     */
    public WordBuilder heading(String text, WordStyle.Heading heading, WordStyle... wordStyles) {
        //创建段落
        XWPFParagraph paragraph = document.createParagraph();
        // 标题样式最先应用，用户自定义样式（如 spacing/spacingBefore/Font 等）随后应用，
        // 以覆盖标题的默认值（如段前段后间距、字号等），否则 Heading 会覆盖用户传入的样式
        WordStyle[] styles = new WordStyle[(wordStyles == null ? 0 : wordStyles.length) + 1];
        styles[0] = heading;
        if (wordStyles != null) {
            System.arraycopy(wordStyles, 0, styles, 1, wordStyles.length);
        }
        this.paragraph(paragraph, text, styles);
        return this;
    }


    /**
     * 添加段落文本
     *
     * @return
     */
    public WordBuilder paragraph(String text, WordStyle... wordStyles) {
        //创建段落
        XWPFParagraph paragraph = document.createParagraph();
        this.paragraph(paragraph, text, wordStyles);
        return this;
    }

    /**
     * 添加段落文本
     *
     * @return
     */
    private void paragraph(XWPFParagraph paragraph, String text, WordStyle... wordStyles) {
        // 显式设置段落行距与段后间距，避免 Word 与 WPS 因各自内置 Normal 样式默认值
        // （Word：段后 8 磅、1.08 倍行距；WPS：段后 0、单倍行距）不同而导致显示不一致
        applyDefaultSpacing(paragraph);
        //创建段落文本
        XWPFRun run = paragraph.createRun();
        run.setText(text);
        if (wordStyles != null) {
            for (WordStyle wordStyle : wordStyles) {
                if (wordStyle != null) {
                    boolean isParagraph = WordStyle.Paragraph.class.isAssignableFrom(wordStyle.getClass()) || WordStyle.Heading.class.isAssignableFrom(wordStyle.getClass());
                    wordStyle.buildStyle(isParagraph ? paragraph : run);
                }
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
        paragraph.setSpacingBefore(1);
        paragraph.setSpacingBetween(1, LineSpacingRule.AUTO);
        paragraph.setSpacingAfter(0);
    }

    /**
     * 确保文档样式中已定义 Word 内置标题样式 Heading1~Heading9（含大纲级别）。
     * <p>
     * Word/WPS 的导航窗格（文档结构图）依据"标题样式或大纲级别"展示标题层级。
     * POI 新建的文档默认没有 styles.xml，此时段落 pStyle="HeadingN" 引用的样式未定义，
     * Word/WPS 无法将该段落识别为标题样式（样式面板显示为"正文"），标题无法按层次稳定显示。
     * 此方法在样式缺失时补充标准标题样式定义；模板或其他来源已自带 Heading1 时跳过，避免覆盖。
     */
    private void ensureHeadingStyles() {
        XWPFStyles styles = document.getStyles();
        if (styles == null) {
            styles = document.createStyles();
        }
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
    public WordBuilder table(List<? extends WriteTableable> dataList, WordStyle... wordStyles) {

        if (dataList != null && !(dataList = dataList.stream().filter(Objects::nonNull).collect(Collectors.toList())).isEmpty()) {

            WriteTableable writeTableable = dataList.get(0);

            //获取需要输出字段
            List<Field> fields = getFieldList(writeTableable);

            if (writeTableable instanceof BaseTableInterceptor) {
                fields = ((BaseTableInterceptor) writeTableable).interceptFields(writeTableable.getClass(), fields);
            }

            int rows = dataList.size() + 1;

            int columns = fields.size();

            XWPFTable table = createTable(rows, columns);

            if (writeTableable instanceof WordTableInterceptor) {
                ((WordTableInterceptor) writeTableable).interceptWordTable(table);
            }

            //加载枚举映射关系
            loadEnumMappings(fields);

            //加载全部属性值转换器
            loadConverters(fields);

            //获取表头
            List<String> tableHeaders = getTableHeaders(fields);

            if (writeTableable instanceof BaseTableInterceptor) {
                tableHeaders = ((BaseTableInterceptor) writeTableable).interceptTableHeaders(tableHeaders, writeTableable.getClass());
            }

            //写入表头
            writeTableHeaders(table, tableHeaders, writeTableable, fields, wordStyles);

            //表格内容开始行数
            int startRow = 1;

            //写入表格主体数据
            writeTableRows(table, startRow, dataList, fields, wordStyles);
        }
        return this;
    }


    /**
     * 数据写入表格
     *
     * @param rows 数据列表
     */
    public WordBuilder table(Object[][] rows, WordStyle... wordStyles) {
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
                        paragraph(paragraph, String.valueOf(rows[i][j]), wordStyles);
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
     * word文档写入文件
     *
     * @return
     */
    public void asFile(File file) {
        try (OutputStream outputStream = new BufferedOutputStream(Files.newOutputStream(file.toPath()))) {
            asOutputStream(outputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * word文档写入文件
     *
     * @return
     */
    public void asFile(String filePath) {
        asFile(new File(filePath));
    }


    /**
     * word文档写入输出流
     */
    public void asOutputStream(OutputStream outputStream) {
        try {
            document.write(outputStream);
        } catch (IOException e) {
            throw new RuntimeException("构建PDF文件时发生异常", e);
        } finally {
            //关闭word文档对象
            closeResources(document);
        }
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
     * 加载全部属性值转换器
     *
     * @param fields
     */
    private void loadConverters(List<Field> fields) {
        this.converters = new HashMap<>();
        for (Field field : fields) {
            if (field.isAnnotationPresent(TableField.class)) {
                TableField annotation = field.getAnnotation(TableField.class);
                if (annotation.converter() != NoConverter.class) {
                    try {
                        Converter converter = annotation.converter().getConstructor().newInstance();
                        this.converters.put(getFieldKey(field), converter);
                    } catch (Exception e) {
                        log.error("加载转换器发生异常", e);
                    }
                }
            }
        }
    }


    /**
     * 获取类名+属性名
     *
     * @param field
     * @return
     */
    private String getFieldKey(Field field) {
        return field.getDeclaringClass().getName() + "." + field.getName();
    }


    /**
     * 加载全部枚举映射关系
     *
     * @param fields
     * @return
     */
    private void loadEnumMappings(List<Field> fields) {
        this.enumMappings = new HashMap<>();
        for (Field field : fields) {
            if (field.isAnnotationPresent(TableField.class)) {
                TableField annotation = field.getAnnotation(TableField.class);
                String[] enums = annotation.enums();
                if (enums.length > 0) {
                    appendEnumMapping(getFieldKey(field), enums);
                }
            }
        }
    }


    /**
     * 追加一组枚举映射关系
     *
     * @param fieldKey
     * @param enums
     */
    private void appendEnumMapping(String fieldKey, String[] enums) {
        for (String enumStr : enums) {
            String[] splitArray = enumStr.split("->");
            if (splitArray.length != 2) {
                throw new IllegalArgumentException("The convert property format of TableField is：oldValue -> newValue");
            }
            this.enumMappings.put(fieldKey + ":" + splitArray[0].trim(), splitArray[1].trim());
        }
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
    private void writeTableHeaders(XWPFTable table, List<String> tableHeaders, WriteTableable obj, List<Field> fields, WordStyle... wordStyles) {

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
    private void writeTableRows(XWPFTable table, int startRow, List<? extends WriteTableable> dataList, List<Field> fields, WordStyle... wordStyles) {
        for (int i = 0; i < dataList.size(); i++) {
            writeTableRow(table, i + startRow, dataList.get(i), fields, wordStyles);
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
    private void writeTableRow(XWPFTable table, int rowIndex, WriteTableable obj, List<Field> fieldList, WordStyle... wordStyles) {
        for (int i = 0; i < fieldList.size(); i++) {
            XWPFTableCell cell = table.getRow(rowIndex).getCell(i);
            XWPFParagraph paragraph = cell.getParagraphs().get(0);
            String cellValue = getCellValue(obj, fieldList.get(i));
            paragraph(paragraph, cellValue, wordStyles);
            //拦截单元格
            if (obj instanceof WordTableInterceptor) {
                ((WordTableInterceptor) obj).interceptWordContentCell(cell, paragraph, obj, fieldList.get(i));
            }
        }
    }

    /**
     * 获取单元格值
     *
     * @param obj
     * @param field
     * @return
     */
    private String getCellValue(WriteTableable obj, Field field) {
        Object fieldValue;
        try {
            field.setAccessible(true);
            fieldValue = field.get(obj);
            //执行属性值枚举映射
            Object enumValue = this.enumMappings.get(getFieldKey(field) + ":" + fieldValue);
            if (enumValue != null) {
                fieldValue = enumValue;
            }
            //执行属性值转换
            Converter converter = this.converters.get(getFieldKey(field));
            if (converter != null) {
                fieldValue = converter.convert(fieldValue);
            }
        } catch (IllegalAccessException e) {
            fieldValue = "";
        }
        return String.valueOf(fieldValue);
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
        CTBorder topBorder = borders.isSetTop() ? borders.getTop() : borders.addNewTop();
        topBorder.setVal(STBorder.SINGLE);
        CTBorder bottomBorder = borders.isSetBottom() ? borders.getBottom() : borders.addNewBottom();
        bottomBorder.setVal(STBorder.SINGLE);
        CTBorder leftBorder = borders.isSetLeft() ? borders.getLeft() : borders.addNewLeft();
        leftBorder.setVal(STBorder.SINGLE);
        CTBorder rightBorder = borders.isSetRight() ? borders.getRight() : borders.addNewRight();
        rightBorder.setVal(STBorder.SINGLE);
        CTBorder insideHBorder = borders.isSetInsideH() ? borders.getInsideH() : borders.addNewInsideH();
        insideHBorder.setVal(STBorder.SINGLE);
        CTBorder insideVBorder = borders.isSetInsideV() ? borders.getInsideV() : borders.addNewInsideV();
        insideVBorder.setVal(STBorder.SINGLE);

        // 设置单元格边距：上下 70 缇，左右 108 缇（0.19cm，OOXML 标准表格默认边距）
        // 左右边距必须显式指定：Word 与 WPS 对缺失 left/right 的默认值处理不同，
        // 不显式写入会导致单元格文本距边框的位置在两软件中不一致
        CTTblCellMar cellMar = tblPr.isSetTblCellMar() ? tblPr.getTblCellMar() : tblPr.addNewTblCellMar();
        CTTblWidth top = cellMar.isSetTop() ? cellMar.getTop() : cellMar.addNewTop();
        top.setType(STTblWidth.DXA);
        top.setW(BigInteger.valueOf(70));
        CTTblWidth start = cellMar.isSetStart() ? cellMar.getStart() : cellMar.addNewStart();
        start.setType(STTblWidth.DXA);
        start.setW(BigInteger.valueOf(108));
        CTTblWidth bottom = cellMar.isSetBottom() ? cellMar.getBottom() : cellMar.addNewBottom();
        bottom.setType(STTblWidth.DXA);
        bottom.setW(BigInteger.valueOf(70));
        CTTblWidth end = cellMar.isSetEnd() ? cellMar.getEnd() : cellMar.addNewEnd();
        end.setType(STTblWidth.DXA);
        end.setW(BigInteger.valueOf(108));
    }

    /**
     * 获取表头
     *
     * @param fieldList
     * @return
     */
    private List<String> getTableHeaders(List<Field> fieldList) {
        //通过注解获取需要输出的属性别名
        return fieldList.stream()
                .map(field -> {
                    TableField annotation = field.getAnnotation(TableField.class);
                    return annotation != null && !Objects.equals("", annotation.value())
                            ? annotation.value()
                            : field.getName();
                }).collect(Collectors.toList());
    }

    /**
     * 获取需要写入表格的属性
     *
     * @param obj
     * @return
     */
    private <T extends WriteTableable> List<Field> getFieldList(T obj) {

        List<Class<?>> classes = new ArrayList<>();

        Class<?> classType = obj.getClass();

        //遍历获取父类class
        while (!Objects.equals(classType, Object.class)) {
            classes.add(classType);
            classType = classType.getSuperclass();
        }

        // 获取子类及父类全部属性
        List<Field> fields = classes.stream()
                .map(pojoClass -> Stream.of(pojoClass.getDeclaredFields()).collect(Collectors.toList()))
                .flatMap(Collection::stream)
                .collect(Collectors.toList());

        // 属性中是否匹配到任意一个包含 TableField 注解
        boolean anyMatch = fields.stream()
                .anyMatch(field -> field.isAnnotationPresent(TableField.class));

        // 如果使用了 TableField 注解，只获取包含 TableField 注解的属性，反之获取全部属性
        if (anyMatch) {
            fields = fields.stream()
                    .filter(field -> field.isAnnotationPresent(TableField.class) && !field.getAnnotation(TableField.class).exclude())
                    .collect(Collectors.toList());
        }

        return fields.stream().sorted((a, b) -> {
            TableField before = a.getAnnotation(TableField.class);
            TableField next = b.getAnnotation(TableField.class);
            return (before == null ? Integer.MAX_VALUE : before.order()) - (next == null ? Integer.MAX_VALUE : next.order());
        }).collect(Collectors.toList());

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
            WordBuilder.this.paragraph(paragraph, text, wordStyles);
            return this;
        }

        public WordBuilder endParagraph() {
            return WordBuilder.this;
        }

    }
}