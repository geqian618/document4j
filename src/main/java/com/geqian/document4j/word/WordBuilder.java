package com.geqian.document4j.word;

import com.geqian.document4j.common.annotation.TableField;
import com.geqian.document4j.common.extend.Converter;
import com.geqian.document4j.common.extend.NoConverter;
import com.geqian.document4j.common.extend.WriteTableable;
import org.apache.poi.util.Units;
import org.apache.poi.xwpf.usermodel.*;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTInd;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTPPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTblBorders;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STBorder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.reflect.Field;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
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
    public static WordBuilder create(String templatePath) {
        try {
            FileInputStream inputStream = new FileInputStream(templatePath);
            return new WordBuilder(new XWPFDocument(inputStream));
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
        document.createParagraph();
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
    public WordBuilder title(String text, WordStyle... wordStyles) {
        //创建段落
        XWPFParagraph paragraph = document.createParagraph();
        if (wordStyles == null) {
            WordStyle.Font bold = WordStyle.Font.BOLD;
            WordStyle.Font fontSize = WordStyle.Font.fontSize(25);
            WordStyle.Paragraph center = WordStyle.Paragraph.ALIGN_CENTER;
            wordStyles = new WordStyle[]{bold, fontSize, center};
        }
        paragraph(paragraph, text, wordStyles);
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
        paragraph(paragraph, text, wordStyles);
        return this;
    }

    /**
     * 添加段落文本
     *
     * @return
     */
    private void paragraph(XWPFParagraph paragraph, String text, WordStyle... wordStyles) {

        //创建段落文本
        XWPFRun run = paragraph.createRun();

        run.setText(text);

        for (WordStyle wordStyle : wordStyles) {
            if (WordStyle.Paragraph.class.isAssignableFrom(wordStyle.getClass())) {
                wordStyle.fillStyle(paragraph);
            } else {
                wordStyle.fillStyle(run);
            }
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

            int rows = dataList.size() + 1;

            int colums = fields.size();

            XWPFTable table = createTable(rows, colums);

            if (writeTableable instanceof WordTableInterceptor) {
                ((WordTableInterceptor) writeTableable).interceptWordTable(table);
            }

            //加载枚举映射关系
            loadEnumMappings(fields);

            //加载全部属性值转换器
            loadConverters(fields);

            //获取表头
            List<String> tableHeaders = getTableHeaders(fields);

            if (writeTableable instanceof WordTableInterceptor) {
                ((WordTableInterceptor) writeTableable).interceptWordHeaders(writeTableable.getClass(), fields, tableHeaders);
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
     * @throws Exception
     */
    public byte[] asBytes() {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            document.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            //关闭word文档对象
            close(document);
        }
    }


    /**
     * word文档写入文件
     *
     * @return
     * @throws Exception
     */
    public void asFile(String filePath) {
        try (OutputStream out = Files.newOutputStream(Paths.get(filePath))) {
            out.write(asBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * word文档写入输出流
     *
     * @return
     * @throws Exception
     */
    public void asStream(OutputStream out) {
        try {
            out.write(asBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
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
        // 设置表格对齐方式
        table.setCellMargins(70, 0, 70, 0);
        table.setTableAlignment(TableRowAlign.LEFT);

        // 设置表格样式，表格宽度
        table.getCTTbl().addNewTblPr().addNewTblW().setW(BigInteger.valueOf(8250));
        table.getCTTbl().getTblPr().unsetTblBorders();

        // 设置表格 x 轴移动大小
        table.getCTTbl().getTblPr().addNewTblpPr().setTblpX(BigInteger.valueOf(125));
        // 设置表格 y 轴移动大小
        //table.getCTTbl().getTblPr().getTblpPr().setTblpY(BigInteger.valueOf(500));

        // 设置表格边框线条类型
        CTTblBorders borders = table.getCTTbl().addNewTblPr().addNewTblBorders();
        borders.addNewTop().setVal(STBorder.SINGLE);
        borders.addNewBottom().setVal(STBorder.SINGLE);
        borders.addNewLeft().setVal(STBorder.SINGLE);
        borders.addNewRight().setVal(STBorder.SINGLE);
        borders.addNewInsideH().setVal(STBorder.SINGLE);
        borders.addNewInsideV().setVal(STBorder.SINGLE);
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

        if (obj instanceof WordTableInterceptor) {
            fields = ((WordTableInterceptor) obj).interceptWordFields(obj.getClass(), fields);
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
    private void close(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                log.error("释放资源发生异常", e);
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