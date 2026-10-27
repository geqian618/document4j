package com.geqian.document4j.pdf;

import com.geqian.document4j.common.core.AbstractDocumentBuilder;
import com.geqian.document4j.common.core.TableSupport;
import com.geqian.document4j.common.core.WriteTableable;
import com.geqian.document4j.common.extend.Converter;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.itextpdf.text.pdf.draw.LineSeparator;

import java.io.*;
import java.lang.reflect.Field;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * <dependency>
 * <groupId>com.itextpdf</groupId>
 * <artifactId>itextpdf</artifactId>
 * <version>5.5.13.4</version>
 * </dependency>
 *
 * <dependency>
 * <groupId>com.itextpdf</groupId>
 * <artifactId>itext-asian</artifactId>
 * <version>5.2.0</version>
 * </dependency>
 *
 * @author geqian
 * @date 15:46 2024/1/16
 */
public class PDFBuilder extends AbstractDocumentBuilder<PDFBuilder, PDFStyle> {

    private final Document document;

    private final PdfWriter pdfWriter;

    private final OutputStream outputStream;

    private final Stack<Map.Entry<Integer, PdfOutline>> stack;

    private PDFBuilder(OutputStream outputStream) {
        try {
            this.document = new Document(PageSize.A4);
            // 与 Word 中文版默认页面设置一致：A4，上下页边距 2.54cm（72pt），左右 3.17cm（90pt）
            document.setMargins(90f, 90f, 72f, 72f);
            this.outputStream = outputStream;
            pdfWriter = PdfWriter.getInstance(this.document, this.outputStream);
            this.stack = new Stack<>();
            //if (watermark != null && !watermark.trim().isEmpty()) {
            //    pdfWriter.setPageEvent(new WaterMarkEvent(createFont(), watermark));
            //}
            this.document.open();
            // 获取根大纲
            PdfOutline root = pdfWriter.getRootOutline();
            this.stack.push(new AbstractMap.SimpleEntry<>(0, root));
        } catch (Exception e) {
            throw new RuntimeException("创建PdfBuilder异常", e);
        }
    }

    private PDFBuilder() {
        this(new ByteArrayOutputStream());
    }


    public static PDFBuilder create() {
        return new PDFBuilder();
    }


    public static PDFBuilder create(OutputStream target) {
        return new PDFBuilder(target);
    }


    /**
     * 设置文档说明（描述）信息
     *
     * @param des
     * @return
     */
    public PDFBuilder description(Description des) {
        String title = des.getTitle();
        String author = des.getAuthor();
        String subject = des.getSubject();
        String keywords = des.getKeywords();
        String creator = des.getCreator();
        if (isNotBlank(title)) {
            this.document.addTitle(title);
        }
        if (isNotBlank(author)) {
            this.document.addAuthor(author);
        }
        if (isNotBlank(subject)) {
            this.document.addSubject(subject);
        }
        if (isNotBlank(keywords)) {
            this.document.addKeywords(keywords);
        }
        if (isNotBlank(creator)) {
            this.document.addCreator(creator);
        }
        if (des.isCreateTime()) {
            this.document.addCreationDate();
        }
        if (des.isProducer()) {
            this.document.addProducer();
        }
        return this;
    }


    /**
     * 添加标题（书签）
     *
     * @param text
     * @param heading
     * @param styles
     * @return
     * @throws DocumentException
     */
    public PDFBuilder heading(String text, PDFStyle heading, List<PDFStyle> styles) {
        if (!(heading instanceof PDFStyle.Heading)) {
            throw new IllegalArgumentException("Title level cannot be null");
        }
        int level = ((PDFStyle.Heading) heading).getLevel();
        List<PDFStyle> allStyles = new ArrayList<>();
        allStyles.add(heading);
        if (styles != null) {
            allStyles.addAll(styles);
        }
        // 锚点
        String anchor = UUID.randomUUID().toString();
        Chunk anchorChunk = new Chunk(text, createFont(allStyles));
        anchorChunk.setLocalDestination(anchor);
        // 应用段落级样式（如 spacing/对齐等），与 paragraph() 保持一致；否则传入的 PDFStyle.Paragraph 样式在标题上不生效
        Paragraph paragraph = new Paragraph(anchorChunk);
        for (PDFStyle pdfStyle : allStyles) {
            if (pdfStyle != null && PDFStyle.Paragraph.class.isAssignableFrom(pdfStyle.getClass())) {
                pdfStyle.buildStyle(paragraph);
            }
        }
        try {
            document.add(paragraph);
            // 使用锚点名
            PdfAction action = PdfAction.gotoLocalPage(anchor, true);
            while (!stack.isEmpty() && stack.peek().getKey() >= level) {
                stack.pop();
            }
            PdfOutline parent = stack.peek().getValue();
            PdfOutline pdfOutline = new PdfOutline(parent, action, text);
            stack.push(new AbstractMap.SimpleEntry<>(level, pdfOutline));
        } catch (DocumentException e) {
            throw new RuntimeException("添加标题时发生异常", e);
        }
        return this;
    }

    /**
     * 空白行
     *
     * @return
     */
    public PDFBuilder blankRow() {
        try {
            Paragraph paragraph = new Paragraph();
            // 与正文段落保持一致的显式默认间距（段前段后 0、单倍行距）
            applyDefaultSpacing(paragraph);
            paragraph.add(Chunk.NEWLINE);
            this.document.add(paragraph);
        } catch (DocumentException e) {
            throw new RuntimeException("添加空白行发生异常", e);
        }
        return this;
    }


    /**
     * 添加一个新页面
     *
     * @return
     */
    public PDFBuilder newPage() {
        this.document.newPage();
        return this;
    }


    /**
     * 添加列表
     *
     * @param elements
     * @param pdfStyles
     * @return
     */
    public PDFBuilder list(List<String> elements, List<PDFStyle> pdfStyles) {
        try {
            com.itextpdf.text.List list = new com.itextpdf.text.List();
            for (String element : elements) {
                list.add(createParagraph(element, pdfStyles));
            }
            this.document.add(list);
        } catch (DocumentException e) {
            throw new RuntimeException("添加列表发生异常", e);
        }
        return this;
    }


    /**
     * 添加段落
     *
     * @param text
     * @param pdfStyles
     * @return
     */
    public PDFBuilder paragraph(String text, List<PDFStyle> pdfStyles) {
        try {
            Paragraph paragraph = createParagraph(text, pdfStyles);
            this.document.add(paragraph);
        } catch (DocumentException e) {
            throw new RuntimeException("添加段落异常", e);
        }
        return this;
    }

    /**
     * 添加链接
     */
    public PDFBuilder link(String text, String link, List<PDFStyle> pdfStyles) {
        try {
            Paragraph paragraph = createParagraph(text, pdfStyles);
            ((Chunk) paragraph.get(0)).setAnchor(link);
            this.document.add(paragraph);
        } catch (DocumentException e) {
            throw new RuntimeException("添加链接发生异常", e);
        }
        return this;
    }

    /**
     * 添加分割线
     */
    public PDFBuilder divisionLine() {
        try {
            this.document.add(new LineSeparator(1f, 100f, BaseColor.BLACK, Element.ALIGN_CENTER, -2f));
        } catch (DocumentException e) {
            throw new RuntimeException("添加分割线发生异常", e);
        }
        return this;
    }


    /**
     * 添加图片
     *
     * @param bytes
     * @return
     */
    public PDFBuilder picture(byte[] bytes, int width, int height) {
        try {
            // 读取图片文件
            Image image = Image.getInstance(bytes);
            // 调整图片大小（可选）
            image.scaleToFit(width, height);
            this.document.add(image);
        } catch (Exception e) {
            throw new RuntimeException("添加图片发生异常", e);
        }
        return this;
    }

    /**
     * 添加图片（PDF 不使用文件名，等价于 {@link #picture(byte[], int, int)}）
     */
    @Override
    public PDFBuilder picture(byte[] bytes, String filename, int width, int height) {
        return picture(bytes, width, height);
    }


    /**
     * 添加表格
     *
     * @param dataList 数据列表
     */
    public PDFBuilder table(List<? extends WriteTableable> dataList, List<PDFStyle> pdfStyles) {

        if (dataList != null && !(dataList = dataList.stream().filter(Objects::nonNull).collect(Collectors.toList())).isEmpty()) {

            WriteTableable writeTableable = dataList.get(0);

            TableData tableData = resolveTable(writeTableable);

            PdfPTable table = createTable(tableData.fields.size());

            if (writeTableable instanceof PDFTableInterceptor) {
                ((PDFTableInterceptor) writeTableable).interceptPdfTable(table);
            }

            //写入表头
            writeTableHeaders(table, tableData.headers, writeTableable, pdfStyles);

            //写入表格主体数据
            writeTableRows(table, dataList, tableData.fields, tableData.enumMappings, tableData.converters, pdfStyles);

            try {
                this.document.add(table);
            } catch (DocumentException e) {
                throw new RuntimeException("添加表格发生异常", e);
            }
        }
        return this;
    }


    /**
     * 数据写入表格
     *
     * @param rows 数据列表
     */
    public PDFBuilder table(Object[][] rows, List<PDFStyle> pdfStyles) {
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
                PdfPTable table = createTable(column);
                for (int i = 0; i < row; i++) {
                    for (int j = 0; j < rows[i].length; j++) {
                        Object cellValue = rows[i][j];
                        //null 写作空字符串，避免字面量 "null" 出现在文档中
                        PdfPCell cell = createCell(cellValue == null ? "" : String.valueOf(cellValue), pdfStyles);
                        table.addCell(cell);
                    }
                }
                try {
                    this.document.add(table);
                } catch (DocumentException e) {
                    throw new RuntimeException("添加表格发生异常", e);
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
     * 构建PDF文档
     */
    public void build() {
        closeResources(this.document, this.pdfWriter);
    }


    /**
     * PDF文档转换为字节数组
     *
     * @return
     */
    public byte[] asBytes() {
        closeResources(this.document, this.pdfWriter);
        if (outputStream instanceof ByteArrayOutputStream) {
            return ((ByteArrayOutputStream) outputStream).toByteArray();
        }
        throw new IllegalStateException(
                "create(OutputStream) 创建的实例 PDF 已直接写入该流，请使用 build() 收尾；asBytes()/asFile()/asOutputStream() 仅支持 create() 创建的实例");
    }


    /**
     * 创建表格
     *
     * @param columns
     * @return
     */
    private PdfPTable createTable(int columns) {
        //创建表格
        PdfPTable table = new PdfPTable(columns);
        //设置表格样式
        setTableStyle(table);

        return table;
    }


    /**
     * 写入表头
     *
     * @param table
     * @param tableHeaders
     * @param pdfStyles
     */
    private void writeTableHeaders(PdfPTable table, List<String> tableHeaders, WriteTableable obj, List<PDFStyle> pdfStyles) {

        for (String header : tableHeaders) {
            PdfPCell cell = createCell(header, pdfStyles);
            if (obj instanceof PDFTableInterceptor) {
                ((PDFTableInterceptor) obj).interceptPdfHeaderCell(cell, obj.getClass());
            }
            table.addCell(cell);
        }
    }

    /**
     * 写入表格多行数据
     *
     * @param table
     * @param dataList
     * @param fields
     * @param pdfStyles
     */
    private void writeTableRows(PdfPTable table, List<? extends WriteTableable> dataList, List<Field> fields,
                                Map<String, String> enumMappings, Map<String, Converter> converters, List<PDFStyle> pdfStyles) {
        for (WriteTableable writeTableable : dataList) {
            writeTableRow(table, writeTableable, fields, enumMappings, converters, pdfStyles);
        }
    }


    /**
     * 写入表格一行数据
     *
     * @param table
     * @param obj
     * @param fieldList
     * @param pdfStyles
     */
    private void writeTableRow(PdfPTable table, WriteTableable obj, List<Field> fieldList,
                               Map<String, String> enumMappings, Map<String, Converter> converters, List<PDFStyle> pdfStyles) {
        for (Field field : fieldList) {
            String cellValue = TableSupport.getCellValue(obj, field, enumMappings, converters);
            PdfPCell cell = createCell(cellValue, pdfStyles);
            //拦截单元格
            if (obj instanceof PDFTableInterceptor) {
                ((PDFTableInterceptor) obj).interceptPdfContentCell(cell, obj, field);
            }
            table.addCell(cell);
        }
    }

    /**
     * 设置表格样式
     *
     * @param table
     */
    private void setTableStyle(PdfPTable table) {
        // 设置表格上面空白宽度
        table.setSpacingBefore(10f);
        // 设置表格下面空白宽度
        table.setSpacingAfter(10f);
        // 设置表格的宽度占页面宽度的百分比
        table.setWidthPercentage(100);

    }

    /**
     * 关闭资源
     */
    private void closeResources(Document document, PdfWriter pdfWriter) {
        if (document != null) {
            document.close();
        }
        if (pdfWriter != null) {
            pdfWriter.close();
        }
    }


    /**
     * 为段落设置统一的显式默认间距：段前 0、段后 0、单倍行距（1 倍字号）。
     * <p>
     * iText 的 Paragraph 默认行距为固定 16pt（约 1.33 倍 12pt 字号），并非单倍行距，
     * 这里显式改为倍数行距 1.0（{@code setLeading(0, 1.0f)}），字号变化时行距自动跟随，
     * 与 WordBuilder 中"段前段后 0、单倍行距"的默认保持一致；
     * 后续通过 PDFStyle 设置的间距/行距（如 spacing/spacingBefore/spacingAfter/spacingBetween）会覆盖其中对应值。
     *
     * @param paragraph 段落
     */
    private void applyDefaultSpacing(Paragraph paragraph) {
        paragraph.setSpacingBefore(0);
        paragraph.setSpacingAfter(0);
        paragraph.setLeading(0, 1.0f);
    }

    /**
     * 创建段落
     *
     * @param text
     * @return
     */
    private Paragraph createParagraph(String text, List<PDFStyle> pdfStyles) {
        Paragraph paragraph = new Paragraph(text, createFont(pdfStyles));
        // 显式设置默认间距（段前段后 0、单倍行距），用户样式随后覆盖
        applyDefaultSpacing(paragraph);
        if (pdfStyles != null) {
            for (PDFStyle pdfStyle : pdfStyles) {
                if (pdfStyle != null && PDFStyle.Paragraph.class.isAssignableFrom(pdfStyle.getClass())) {
                    pdfStyle.buildStyle(paragraph);
                }
            }
        }
        return paragraph;
    }


    /**
     * 创建字体
     *
     * @return
     */
    private Font createFont(List<PDFStyle> pdfStyles) {
        String fontFamily = null;
        Font font;
        try {
            if (pdfStyles != null) {
                for (PDFStyle pdfStyle : pdfStyles) {
                    if (pdfStyle instanceof PDFStyle.Font && (fontFamily = ((PDFStyle.Font) pdfStyle).getFamilyStyle()) != null) {
                        break;
                    }
                }
            }
            font = fontFamily != null ?
                    FontFactory.getFont(fontFamily, BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED)
                    // 默认使用 iText 内置中文字体 STSong（itext-asian 提供），不再依赖随包分发的字体文件
                    : FontFactory.getFont("STSongStd-Light", "UniGB-UCS2-H", BaseFont.NOT_EMBEDDED);
        } catch (Exception e) {
            font = new Font();
        }
        font.setStyle(Font.NORMAL);
        // 与 Word 默认正文字号一致：11 磅（iText 默认 12 磅）
        font.setSize(11);
        if (pdfStyles != null) {
            for (PDFStyle pdfStyle : pdfStyles) {
                if (pdfStyle != null && (PDFStyle.Font.class.isAssignableFrom(pdfStyle.getClass()) || PDFStyle.Heading.class.isAssignableFrom(pdfStyle.getClass()))) {
                    pdfStyle.buildStyle(font);
                }
            }
        }
        return font;
    }

    /**
     * 创建表格单元格
     *
     * @param text
     * @param pdfStyles
     * @return
     */
    private PdfPCell createCell(String text, List<PDFStyle> pdfStyles) {
        PdfPCell cell = new PdfPCell(new Phrase(text, createFont(pdfStyles)));
        // 设置单元格的水平和垂直对齐方式
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPaddingLeft(8);
        cell.setPaddingTop(5);
        cell.setPaddingBottom(5);
        cell.setBorderColor(BaseColor.GRAY);
        cell.setBorderWidth(0.5f);
        return cell;
    }

    private boolean isNotBlank(String text) {
        return text != null && !text.trim().isEmpty();
    }

    public class ParagraphBuilder {

        private final Paragraph paragraph;

        public ParagraphBuilder() {
            paragraph = new Paragraph();
            // 与正文段落保持一致的显式默认间距（段前段后 0、单倍行距）
            applyDefaultSpacing(paragraph);
        }

        public ParagraphBuilder text(String text, PDFStyle... pdfStyles) {
            Chunk chunk = new Chunk(text, createFont(pdfStyles == null ? Collections.<PDFStyle>emptyList() : Arrays.asList(pdfStyles)));
            paragraph.add(chunk);
            if (pdfStyles != null) {
                for (PDFStyle pdfStyle : pdfStyles) {
                    if (pdfStyle != null && PDFStyle.Paragraph.class.isAssignableFrom(pdfStyle.getClass())) {
                        pdfStyle.buildStyle(paragraph);
                    }
                }
            }
            return this;
        }


        public PDFBuilder endParagraph() {
            try {
                PDFBuilder.this.document.add(paragraph);
                return PDFBuilder.this;
            } catch (DocumentException e) {
                throw new RuntimeException("写入文本发生异常", e);
            }
        }

    }

}
