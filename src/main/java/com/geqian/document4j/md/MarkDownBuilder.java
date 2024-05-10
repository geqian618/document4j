package com.geqian.document4j.md;

import com.geqian.document4j.common.extend.Converter;
import com.geqian.document4j.common.extend.NoConverter;
import com.geqian.document4j.common.annotation.TableField;
import com.geqian.document4j.common.extend.WriteTableable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author geqian
 * @date 21:33 2024/1/30
 */
public class MarkDownBuilder {

    private final Logger log = LoggerFactory.getLogger(MarkDownBuilder.class);

    private final StringBuffer stringBuffer;

    private Map<String, String> enumMappings;

    private Map<String, Converter> converters;

    private MarkDownBuilder() {
        this.stringBuffer = new StringBuffer();
    }

    private MarkDownBuilder(boolean hasCatalogue) {
        this.stringBuffer = new StringBuffer();
        if (hasCatalogue) {
            this.stringBuffer.append("[TOC]\n");
        }
    }


    public static MarkDownBuilder create() {
        return new MarkDownBuilder();
    }

    public static MarkDownBuilder create(boolean hasCatalogue) {
        return new MarkDownBuilder(hasCatalogue);
    }


    /**
     * 标题
     *
     * @param text
     * @param title
     * @return
     */
    public MarkDownBuilder title(String text, MarkDownStyle.Title title) {
        stringBuffer.append(title.fillStyle(text));
        return this;
    }


    /**
     * 添加空白行
     *
     * @return
     */
    public MarkDownBuilder blankRow() {
        stringBuffer.append("\n\n");
        return this;
    }


    /**
     * 文本
     *
     * @param text
     * @param styles
     * @return
     */
    public MarkDownBuilder paragraph(String text, MarkDownStyle.Font... styles) {
        for (MarkDownStyle.Font style : styles) {
            text = style.fillStyle(text);
        }
        stringBuffer.append(text).append("\n");
        return this;
    }


    /**
     * 添加图片
     *
     * @param alt
     * @param url
     * @return
     */
    public MarkDownBuilder image(String alt, String url) {
        stringBuffer.append(String.format("![%s](%s)", alt, url));
        return this;
    }


    /**
     * 分割线
     *
     * @return
     */
    public MarkDownBuilder divisionLine() {
        stringBuffer.append("---\n");
        return this;
    }


    /**
     * 区块
     *
     * @param text
     * @return
     */
    public MarkDownBuilder block(String text) {
        stringBuffer.append(String.format("> %s\n", text));
        return this;
    }

    /**
     * 无序列表
     *
     * @param list
     * @return
     */
    public MarkDownBuilder unorderedList(String... list) {
        Arrays.stream(list).forEach(element -> stringBuffer.append(String.format("- %s\n", element)));
        return this;
    }

    /**
     * 有序列表
     *
     * @param list
     * @return
     */
    public MarkDownBuilder orderedList(String... list) {
        for (int i = 0; i < list.length; i++) {
            stringBuffer.append(String.format("%d. %s\n", i + 1, list[i]));
        }
        return this;
    }


    /**
     * 链接
     *
     * @param linkName
     * @param link
     * @return
     */
    public MarkDownBuilder link(String linkName, String link) {
        stringBuffer.append(String.format("[%s](%s)\n", linkName, link));
        return this;
    }

    /**
     * 链接
     *
     * @param link
     * @return
     */
    public MarkDownBuilder link(String link) {
        stringBuffer.append(String.format("<%s>\n", link));
        return this;
    }


    /**
     * 原始 md 表达式
     *
     * @param raw
     * @return
     */
    public MarkDownBuilder raw(String raw) {
        stringBuffer.append(raw).append("\n");
        return this;
    }


    /**
     * 行内模式
     *
     * @return
     */
    public NowrapBuilder startParagraph() {
        return new NowrapBuilder().startRow();
    }


    /**
     * 代码块
     *
     * @param type
     * @param code
     * @return
     */
    public MarkDownBuilder codeBlock(String type, String code) {
        stringBuffer.append(String.format("```%s\n%s\n```\n", type, code));
        return this;
    }


    /**
     * 写入表格
     *
     * @param dataList
     * @param alignment
     * @return
     */
    public <T extends WriteTableable> MarkDownBuilder table(List<T> dataList, MarkDownStyle.CellAlignment alignment) {

        if (dataList != null && !(dataList = dataList.stream().filter(Objects::nonNull).collect(Collectors.toList())).isEmpty()) {

            WriteTableable writeTableable = dataList.get(0);

            //获取需要输出字段
            List<Field> fields = getFieldList(writeTableable);

            //加载枚举映射关系
            loadEnumMappings(fields);

            //加载全部属性值转换器
            loadConverters(fields);

            //获取表头
            List<String> tableHeaders = getTableHeaders(fields);

            if (writeTableable instanceof MarkdownTableInterceptor) {
                ((MarkdownTableInterceptor) writeTableable).interceptMdHeaders(tableHeaders, writeTableable.getClass());
            }

            StringBuilder tableBuilder = new StringBuilder();

            //写入表头
            writeTableHeaders(tableBuilder, tableHeaders, alignment);

            //写入表格主体数据
            writeTableRows(tableBuilder, dataList, fields);

            stringBuffer.append(tableBuilder);
        }

        return this;
    }


    /**
     * 写入表格
     *
     * @param data
     * @param alignment
     * @return
     */
    public MarkDownBuilder table(Object[][] data, MarkDownStyle.CellAlignment alignment) {
        StringBuilder tableBuilder = new StringBuilder();
        if (data != null && data.length > 0) {
            for (int i = 0; i < data.length; i++) {
                Object[] rows = data[i];
                for (Object row : rows) {
                    tableBuilder.append("|").append(row);
                }
                tableBuilder.append("|\n");
                if (i == 0) {
                    for (int j = 0; j < rows.length; j++) {
                        tableBuilder.append("|").append(alignment.getMark());
                    }
                    tableBuilder.append("|\n");
                }
            }
        }
        stringBuffer.append(tableBuilder);
        return this;
    }

    private <T extends WriteTableable> void writeTableRows(StringBuilder tableBuilder, List<T> dataList, List<Field> fields) {
        for (WriteTableable writeTableable : dataList) {
            writeTableRow(tableBuilder, writeTableable, fields);
        }
        tableBuilder.append("\n");
    }

    /**
     * 写入表格一行数据
     *
     * @param tableBuilder
     * @param obj
     * @param fieldList
     */
    private void writeTableRow(StringBuilder tableBuilder, WriteTableable obj, List<Field> fieldList) {
        for (Field field : fieldList) {
            String cellValue = getCellValue(obj, field);
            //拦截单元格
            if (obj instanceof MarkdownTableInterceptor) {
                cellValue = ((MarkdownTableInterceptor) obj).interceptMdCellValue(cellValue, obj, field);
            }
            tableBuilder.append("|").append(cellValue);
        }
        tableBuilder.append("|\n");
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


    public byte[] asBytes() {
        return stringBuffer.toString().getBytes(StandardCharsets.UTF_8);
    }


    public void asStream(OutputStream out) {
        if (out != null) {
            try {
                out.write(asBytes());
            } catch (IOException e) {
                log.error("写入输出流发生异常", e);
            }
        }

    }


    public void asFile(String filePath) {
        try (FileOutputStream out = new FileOutputStream(filePath)) {
            out.write(asBytes());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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

        if (obj instanceof MarkdownTableInterceptor) {
            fields = ((MarkdownTableInterceptor) obj).interceptMdFields(obj.getClass(), fields);
        }

        return fields.stream().sorted((a, b) -> {
            TableField before = a.getAnnotation(TableField.class);
            TableField next = b.getAnnotation(TableField.class);
            return (before == null ? Integer.MAX_VALUE : before.order()) - (next == null ? Integer.MAX_VALUE : next.order());
        }).collect(Collectors.toList());
    }


    /**
     * 写入表头
     *
     * @param tableBuilder
     * @param tableHeaders
     */
    private void writeTableHeaders(StringBuilder tableBuilder, List<String> tableHeaders, MarkDownStyle.CellAlignment alignment) {
        if (tableHeaders != null && !tableHeaders.isEmpty()) {
            for (String header : tableHeaders) {
                tableBuilder.append("|").append(header);
            }
            tableBuilder.append("|\n");
            for (int i = 0; i < tableHeaders.size(); i++) {
                tableBuilder.append("|").append(alignment.getMark());
            }
            tableBuilder.append("|\n");
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
     * 获取类名+属性名
     *
     * @param field
     * @return
     */
    private String getFieldKey(Field field) {
        return field.getDeclaringClass().getName() + "." + field.getName();
    }


    public class NowrapBuilder {

        private final StringBuffer subStringBuffer;

        public NowrapBuilder() {
            this.subStringBuffer = new StringBuffer();
        }

        public NowrapBuilder startRow() {
            return this;
        }

        public MarkDownBuilder endParagraph() {
            MarkDownBuilder.this.stringBuffer.append(subStringBuffer).append("\n");
            return MarkDownBuilder.this;
        }


        /**
         * 原始 md 表达式
         *
         * @param raw
         * @return
         */
        public NowrapBuilder raw(String raw) {
            stringBuffer.append(raw);
            return this;
        }


        /**
         * 文本
         *
         * @param text
         * @param styles
         * @return
         */
        public NowrapBuilder paragraph(String text, MarkDownStyle.Font... styles) {
            for (MarkDownStyle.Font style : styles) {
                text = style.fillStyle(text);
            }
            subStringBuffer.append(text);
            return this;
        }


        /**
         * 链接
         *
         * @param linkName
         * @param link
         * @return
         */
        public NowrapBuilder link(String linkName, String link) {
            subStringBuffer.append(String.format("[%s](%s)", linkName, link));
            return this;
        }


        /**
         * 链接
         *
         * @param link
         * @return
         */
        public NowrapBuilder link(String link) {
            subStringBuffer.append(String.format("<%s>", link));
            return this;
        }

    }

}
