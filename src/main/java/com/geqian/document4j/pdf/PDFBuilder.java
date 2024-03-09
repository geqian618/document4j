package com.geqian.document4j.pdf;

import com.geqian.document4j.common.annotation.NoTableHeader;
import com.geqian.document4j.common.annotation.TableField;
import com.geqian.document4j.common.extend.Converter;
import com.geqian.document4j.common.extend.NoConverter;
import com.geqian.document4j.common.extend.WriteTableable;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * <dependency>
 * <groupId>com.itextpdf</groupId>
 * <artifactId>itextpdf</artifactId>
 * <version>5.2.0</version>
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
public class PDFBuilder {

    private final Logger log = LoggerFactory.getLogger(PDFBuilder.class);

    private final Document document;

    private final PdfWriter pdfWriter;

    private final ByteArrayOutputStream out;

    private Map<String, String> enumMappings;

    private Map<String, Converter> converters;

    private PDFBuilder(String watermark) {
        try {
            this.document = new Document(PageSize.A4);
            float padding = PageSize.A4.getWidth() * 0.1f;
            // 设置左右填充为页面宽度的 10%
            document.setMargins(padding, padding, 36, 36);
            this.out = new ByteArrayOutputStream();
            pdfWriter = PdfWriter.getInstance(this.document, this.out);
            if (watermark != null && !watermark.trim().isEmpty()) {
                pdfWriter.setPageEvent(new WaterMarkEvent(createFont(), watermark));
            }
            this.document.open();
        } catch (Exception e) {
            throw new RuntimeException("创建PdfBuilder异常", e);
        }
    }


    private PDFBuilder(Rectangle pageSize, float margin, String watermark) {
        try {
            this.document = new Document(pageSize);
            // 设置左右填充为页面宽度的 10%
            document.setMargins(margin, margin, margin, margin);
            this.out = new ByteArrayOutputStream();
            pdfWriter = PdfWriter.getInstance(this.document, this.out);
            if (watermark != null && !watermark.trim().isEmpty()) {
                pdfWriter.setPageEvent(new WaterMarkEvent(createFont(), watermark));
            }
            this.document.open();
        } catch (Exception e) {
            throw new RuntimeException("创建PdfBuilder异常", e);
        }
    }


    private PDFBuilder(Rectangle pageSize, float marginLeft, float marginRight, float marginTop, float marginBottom, String watermarkText) {
        try {
            this.document = new Document(pageSize);
            // 设置左右填充为页面宽度的 10%
            document.setMargins(marginLeft, marginRight, marginTop, marginBottom);
            this.out = new ByteArrayOutputStream();
            pdfWriter = PdfWriter.getInstance(this.document, this.out);
            if (watermarkText != null && !watermarkText.trim().isEmpty()) {
                pdfWriter.setPageEvent(new WaterMarkEvent(createFont(), watermarkText));
            }
            this.document.open();
        } catch (Exception e) {
            throw new RuntimeException("创建PdfBuilder异常", e);
        }
    }

    public static PDFBuilder create() {
        return new PDFBuilder(null);
    }

    public static PDFBuilder create(String watermark) {
        return new PDFBuilder(watermark);
    }


    public static PDFBuilder create(Rectangle pageSize, float margin) {
        return new PDFBuilder(pageSize, margin, null);
    }

    public static PDFBuilder create(Rectangle pageSize, float margin, String watermark) {
        return new PDFBuilder(pageSize, margin, watermark);
    }


    public static PDFBuilder create(Rectangle pageSize, float marginLeft, float marginRight, float marginTop, float marginBottom) {
        return new PDFBuilder(pageSize, marginLeft, marginRight, marginTop, marginBottom, null);
    }

    public static PDFBuilder create(Rectangle pageSize, float marginLeft, float marginRight, float marginTop, float marginBottom, String watermark) {
        return new PDFBuilder(pageSize, marginLeft, marginRight, marginTop, marginBottom, watermark);
    }


    /**
     * 设置文档说明（描述）信息
     *
     * @param des
     * @return
     */
    public PDFBuilder setDescription(Description des) {
        this.document.addTitle(des.getTitle());
        this.document.addAuthor(des.getAuthor());
        this.document.addSubject(des.getSubject());
        this.document.addKeywords(des.getKeywords());
        this.document.addCreator(des.getCreator());
        if (des.isCreateTime()) {
            this.document.addCreationDate();
        }
        if (des.isProducer()) {
            this.document.addProducer();
        }
        return this;
    }


    /**
     * 换行
     *
     * @return
     */
    public PDFBuilder addCarriageReturn() {
        try {
            Paragraph paragraph = new Paragraph();
            paragraph.add(Chunk.NEWLINE);
            this.document.add(paragraph);
        } catch (DocumentException e) {
            log.error("添加换行异常", e);
        }
        return this;
    }


    /**
     * 添加一个新页面
     *
     * @return
     */
    public PDFBuilder addNewPage() {
        this.document.newPage();
        return this;
    }


    /**
     * 添加段落文字
     *
     * @return
     */
    public PDFBuilder addTitle(String text) {
        addTitle(text, null);
        return this;
    }


    /**
     * 添加段落文本
     *
     * @return
     */
    public PDFBuilder addTitle(String text, PdfStyle... pdfStyles) {
        if (pdfStyles == null) {
            PdfStyle.Paragraph alignCenter = PdfStyle.Paragraph.ALIGN_CENTER;
            PdfStyle.Font fontSize = PdfStyle.Font.fontSize(25);
            pdfStyles = new PdfStyle[]{alignCenter, fontSize};
        }
        addParagraph(text, pdfStyles);
        return this;
    }


    /**
     * 添加列表
     *
     * @param elements
     * @param pdfStyles
     * @return
     */
    public PDFBuilder addList(List<String> elements, PdfStyle... pdfStyles) {
        try {
            com.itextpdf.text.List list = new com.itextpdf.text.List();
            for (String element : elements) {
                list.add(element);
            }
            this.document.add(list);
        } catch (DocumentException e) {
            log.error("添加列表发生异常", e);
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
    public PDFBuilder addParagraph(String text, PdfStyle... pdfStyles) {
        try {
            Paragraph paragraph = createParagraph(text, pdfStyles);
            this.document.add(paragraph);
        } catch (DocumentException e) {
            log.error("添加段落异常", e);
        }
        return this;
    }


    /**
     * 添加图片
     *
     * @param bytes
     * @return
     */
    public PDFBuilder addPicture(byte[] bytes, int width, int height) {
        try {
            // 读取图片文件
            Image image = Image.getInstance(bytes);
            // 调整图片大小（可选）
            image.scaleToFit(width, height);
            this.document.add(image);
        } catch (Exception e) {
            log.error("添加图片发生异常", e);
            return this;
        }
        return this;
    }


    /**
     * 添加表格
     *
     * @param dataList 数据列表
     */
    public PDFBuilder addTable(List<? extends WriteTableable> dataList, PdfStyle... pdfStyles) {

        if (dataList != null && !(dataList = dataList.stream().filter(Objects::nonNull).collect(Collectors.toList())).isEmpty()) {

            WriteTableable writeTableable = dataList.get(0);

            NoTableHeader noTableHeader = writeTableable.getClass().getAnnotation(NoTableHeader.class);

            //获取需要输出字段
            List<Field> fields = getFieldList(writeTableable);

            int colums = fields.size();

            PdfPTable table = createTable(colums);


            if (writeTableable instanceof PDFTableInterceptor) {
                ((PDFTableInterceptor) writeTableable).interceptPdfTable(table);
            }

            //加载枚举映射关系
            loadEnumMappings(fields);

            //加载全部属性值转换器
            loadConverters(fields);

            if (Objects.isNull(noTableHeader)) {
                //获取表头
                List<String> tableHeaders = getTableHeaders(fields);

                if (writeTableable instanceof PDFTableInterceptor) {
                    ((PDFTableInterceptor) writeTableable).interceptPdfHeaders(writeTableable.getClass(), tableHeaders);
                }
                //写入表头
                writeTableHeaders(table, tableHeaders, writeTableable, pdfStyles);
            }
            //写入表格主体数据
            writeTableRows(table, dataList, fields, pdfStyles);
            try {
                this.document.add(table);
            } catch (DocumentException e) {
                log.error("添加表格发生异常", e);
                return this;
            }
        }
        return this;
    }


    /**
     * 数据写入表格
     *
     * @param rows 数据列表
     */
    public PDFBuilder writeTableRows(Object[][] rows, PdfStyle... pdfStyles) throws Exception {
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
                        String cellValue = String.valueOf(rows[i][j]);
                        PdfPCell cell = createCell(cellValue, pdfStyles);
                        table.addCell(cell);
                    }
                }
                this.document.add(table);
            }
        }
        return this;
    }


    /**
     * 行内模式
     *
     * @return
     */
    public NowrapBuilder startRow() {
        return new NowrapBuilder();
    }


    /**
     * word文档转换为字节数组
     *
     * @return
     * @throws Exception
     */
    public byte[] asBytes() {
        try {
            //关闭pdf文档对象
            closeDocument(this.document);
            return this.out.toByteArray();
        } catch (Exception e) {
            log.error("生成pdf文件发生异常", e);
            return new byte[0];
        } finally {
            closePdfWriter(pdfWriter);
            //关闭输出流
            closeResource(this.out);
        }
    }

    /**
     * word文档写入文件
     *
     * @return
     * @throws Exception
     */
    public void asFile(String filePath) {
        try (OutputStream outputStream = Files.newOutputStream(Paths.get(filePath))) {
            outputStream.write(asBytes());
        } catch (IOException e) {
            log.error("生成pdf文件发生异常", e);
        }
    }


    /**
     * word文档写入输出流
     *
     * @return
     * @throws Exception
     */
    public void asStream(OutputStream outputStream) {
        try {
            outputStream.write(asBytes());
        } catch (IOException e) {
            log.error("pdf字节数组写入输出流发生异常", e);
        }
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
                        log.error("创建 Converter 转换器发生异常", e);
                    }
                }
            }
        }
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
    private void writeTableHeaders(PdfPTable table, List<String> tableHeaders, WriteTableable obj, PdfStyle... pdfStyles) {

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
    private void writeTableRows(PdfPTable table, List<? extends WriteTableable> dataList, List<Field> fields, PdfStyle... pdfStyles) {
        for (WriteTableable writeTableable : dataList) {
            writeTableRow(table, writeTableable, fields, pdfStyles);
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
    private void writeTableRow(PdfPTable table, WriteTableable obj, List<Field> fieldList, PdfStyle... pdfStyles) {
        for (Field field : fieldList) {
            String cellValue = getCellValue(obj, field);
            PdfPCell cell = createCell(cellValue, pdfStyles);
            //拦截单元格
            if (obj instanceof PDFTableInterceptor) {
                ((PDFTableInterceptor) obj).interceptPdfContentCell(cell, obj, field);
            }
            table.addCell(cell);
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
    private <T> List<Field> getFieldList(T obj) {

        List<Class<?>> classes = new ArrayList<>();

        Class<?> classType = obj.getClass();

        //遍历获取父类class
        while (!Objects.equals(classType, Object.class)) {
            classes.add(classType);
            classType = classType.getSuperclass();
        }

        List<Field> fields = classes.stream()
                .map(pojoClass -> Stream.of(pojoClass.getDeclaredFields()).collect(Collectors.toList()))
                .flatMap(Collection::stream)
                .filter(field -> field.isAnnotationPresent(TableField.class) && !field.getAnnotation(TableField.class).exclude())
                .collect(Collectors.toList());

        if (obj instanceof PDFTableInterceptor) {
            fields = ((PDFTableInterceptor) obj).interceptPdfFields(obj.getClass(), fields);
        }

        return fields.stream().sorted((a, b) -> {
            TableField before = a.getAnnotation(TableField.class);
            TableField next = b.getAnnotation(TableField.class);
            return (before == null ? Integer.MAX_VALUE : before.order()) - (next == null ? Integer.MAX_VALUE : next.order());
        }).collect(Collectors.toList());
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
     * 获取类名+属性名
     *
     * @param field
     * @return
     */
    private String getFieldKey(Field field) {
        return field.getDeclaringClass().getName() + "." + field.getName();
    }


    /**
     * 关闭资源
     */
    private void closeDocument(Document document) {
        if (document != null) {
            try {
                document.close();
            } catch (Exception e) {
                log.error("资源释放出出现异常", e);
            }
        }
    }


    /**
     * 关闭资源
     */
    private void closeResource(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Exception e) {
                log.error("资源释放出出现异常", e);
            }
        }
    }

    /**
     * 关闭资源
     *
     * @param pdfWriter
     */
    private void closePdfWriter(PdfWriter pdfWriter) {
        if (pdfWriter != null) {
            pdfWriter.close();
        }
    }


    /**
     * 创建段落
     *
     * @param text
     * @return
     */
    private Paragraph createParagraph(String text, PdfStyle... pdfStyles) {
        Paragraph paragraph = new Paragraph(text, createFont(pdfStyles));
        for (PdfStyle pdfStyle : pdfStyles) {
            if (PdfStyle.Paragraph.class.isAssignableFrom(pdfStyle.getClass())) {
                pdfStyle.wrapperStyle(paragraph);
            }
        }
        return paragraph;
    }


    /**
     * 创建字体
     *
     * @return
     */
    private Font createFont(PdfStyle... pdfStyles) {
        String fontFamily = null;
        Font font;
        try {
            for (PdfStyle pdfStyle : pdfStyles) {
                if (pdfStyle instanceof PdfStyle.Font && (fontFamily = ((PdfStyle.Font) pdfStyle).getFamilyStyle()) != null) {
                    break;
                }
            }
            font = fontFamily != null ?
                    FontFactory.getFont(fontFamily, BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED)
                    : FontFactory.getFont("fonts/simsun.ttc,0", BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);
        } catch (Exception ignored) {
            font = FontFactory.getFont("STSongStd-Light", "UniGB-UCS2-H", BaseFont.NOT_EMBEDDED);
        }
        font.setStyle(Font.NORMAL);
        font.setSize(Font.DEFAULTSIZE);
        for (PdfStyle pdfStyle : pdfStyles) {
            if (PdfStyle.Font.class.isAssignableFrom(pdfStyle.getClass())) {
                pdfStyle.wrapperStyle(font);
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
    private PdfPCell createCell(String text, PdfStyle... pdfStyles) {
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


    public class NowrapBuilder {

        private final Paragraph paragraph;

        public NowrapBuilder() {
            paragraph = new Paragraph();
        }

        public NowrapBuilder addText(String text, PdfStyle... pdfStyles) {
            Chunk chunk = new Chunk(text, createFont(pdfStyles));
            paragraph.add(chunk);
            for (PdfStyle pdfStyle : pdfStyles) {
                if (PdfStyle.Paragraph.class.isAssignableFrom(pdfStyle.getClass())) {
                    pdfStyle.wrapperStyle(paragraph);
                }
            }
            return this;
        }


        public PDFBuilder endRow() {
            try {
                PDFBuilder.this.document.add(paragraph);
                return PDFBuilder.this;
            } catch (DocumentException e) {
                throw new RuntimeException("写入文本发生异常", e);
            }
        }
    }

}
