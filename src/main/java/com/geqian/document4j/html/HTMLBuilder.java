package com.geqian.document4j.html;

import com.geqian.document4j.common.annotation.TableField;
import com.geqian.document4j.common.extend.Converter;
import com.geqian.document4j.common.extend.NoConverter;
import com.geqian.document4j.common.extend.WriteTableable;
import com.geqian.document4j.markdown.MarkDownBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author geqian
 * @date 18:03 2024/3/9
 */
public class HTMLBuilder {

    private final Logger log = LoggerFactory.getLogger(MarkDownBuilder.class);

    private final StringBuffer bodyBuffer = new StringBuffer();

    private final StringBuffer styleBuffer = new StringBuffer();

    private final OutputStream outputStream;

    private Map<String, String> enumMappings;

    private Map<String, Converter> converters;

    private HTMLBuilder(OutputStream outputStream) {
        this.outputStream = outputStream;
        setStyleSelector("*{padding:0;margin:0;}p{overflow:auto;word-wrap:break-word;}");
        setStyleSelector(tableDefaultStyle());
    }


    public HTMLBuilder setStyleSelector(String style) {
        this.styleBuffer.append(style);
        return this;
    }

    public static HTMLBuilder create(OutputStream outputStream) {
        return new HTMLBuilder(outputStream);
    }

    public HTMLBuilder title(String text, HTMLStyle.Title title) {
        this.bodyBuffer.append(String.format("<p style=\"%s\">%s</p>", title != null ? title.buildStyle() : "", text));
        return this;
    }


    public HTMLBuilder paragraph(String text, HTMLStyle... htmlStyles) {
        StringBuilder styleBuilder = new StringBuilder();
        for (HTMLStyle htmlStyle : htmlStyles) {
            if (htmlStyle != null) {
                styleBuilder.append(htmlStyle.buildStyle());
            }
        }
        this.bodyBuffer.append(String.format("<p style=\"%s\">%s</p>", styleBuilder, text));
        return this;
    }

    /**
     * 段落模式
     *
     * @return
     */
    public HTMLBuilder.ParagraphBuilder startParagraph() {
        return new HTMLBuilder.ParagraphBuilder();
    }


    public HTMLBuilder link(String link, String desc, HTMLStyle... htmlStyles) {
        StringBuilder styleBuilder = new StringBuilder();
        for (HTMLStyle htmlStyle : htmlStyles) {
            if (htmlStyle != null) {
                styleBuilder.append(htmlStyle.buildStyle());
            }
        }
        this.bodyBuffer.append(String.format("<a style=\"%s\" href=\"%s\">%s</a>", styleBuilder, link, desc));
        return this;
    }

    public HTMLBuilder blankRow() {
        this.bodyBuffer.append("<br>");
        return this;
    }

    public HTMLBuilder divisionLine() {
        this.bodyBuffer.append("<hr>");
        return this;
    }


    public HTMLBuilder html(String html) {
        this.bodyBuffer.append(html);
        return this;
    }

    /**
     * 写入表格
     *
     * @param dataList
     * @return
     */
    public <T extends WriteTableable> HTMLBuilder table(List<T> dataList, HTMLStyle... htmlStyles) {

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

            if (writeTableable instanceof HTMLTableInterceptor) {
                ((HTMLTableInterceptor) writeTableable).interceptHtmlHeaders(tableHeaders, writeTableable.getClass());
            }

            StringBuilder tableBuilder = new StringBuilder();
            StringBuilder tableStyleBuilder = new StringBuilder();
            if (writeTableable instanceof HTMLTableInterceptor) {
                ((HTMLTableInterceptor) writeTableable).interceptHtmlTable(tableStyleBuilder);
            }
            if (tableStyleBuilder.length() > 0) {
                tableBuilder.append(String.format("<table style=\"%s\">", tableStyleBuilder));
            } else {
                tableBuilder.append("<table>");
            }
            //写入表头
            writeTableHeaders(writeTableable, tableBuilder, tableHeaders, htmlStyles);
            //写入表格主体数据
            writeTableRows(tableBuilder, dataList, fields, htmlStyles);
            tableBuilder.append("</table>");

            bodyBuffer.append(tableBuilder);
        }
        return this;
    }


    /**
     * 写入表格
     *
     * @param data
     * @return
     */
    public HTMLBuilder table(Object[][] data, StringBuilder... styleSelectors) {
        if (data != null && data.length > 0) {
            for (StringBuilder selector : styleSelectors) {
                setStyleSelector(selector.toString());
            }
            bodyBuffer.append("<table>");
            for (int i = 0; i < data.length; i++) {
                if (i == 0) {
                    bodyBuffer.append("<thead><tr>");
                    Object[] heads = data[i];
                    for (Object head : heads) {
                        bodyBuffer.append("<th>").append(head).append("</th>");
                    }
                    bodyBuffer.append("</tr></thead>");
                } else {
                    bodyBuffer.append("<tbody><tr>");
                    Object[] heads = data[i];
                    for (Object head : heads) {
                        bodyBuffer.append("<td>").append(head).append("</td>");
                    }
                    bodyBuffer.append("</tr></tbody>");
                }
            }
        }
        bodyBuffer.append("</table>");
        return this;
    }

    private <T extends WriteTableable> void writeTableRows(StringBuilder tableBuilder, List<T> dataList, List<Field> fields, HTMLStyle... htmlStyles) {
        tableBuilder.append("<tbody>");
        for (WriteTableable writeTableable : dataList) {
            writeTableRow(tableBuilder, writeTableable, fields, htmlStyles);
        }
        tableBuilder.append("</tbody>");
    }

    /**
     * 写入表格一行数据
     *
     * @param tableBuilder
     * @param obj
     * @param fieldList
     */
    private void writeTableRow(StringBuilder tableBuilder, WriteTableable obj, List<Field> fieldList, HTMLStyle... htmlStyles) {
        StringBuilder cellStyleBuilder = new StringBuilder();
        tableBuilder.append("<tr>");
        for (Field field : fieldList) {
            String cellValue = getCellValue(obj, field);
            //拦截单元格
            if (obj instanceof HTMLTableInterceptor) {
                cellValue = ((HTMLTableInterceptor) obj).interceptHtmlCell(cellStyleBuilder, cellValue, obj, field);
            }
            Arrays.stream(htmlStyles).forEach(style -> cellStyleBuilder.append(style != null ? style.buildStyle() : ""));
            if (cellStyleBuilder.length() > 0) {
                cellStyleBuilder.insert(0, " style=\"");
                cellStyleBuilder.append("\"");
            }
            tableBuilder.append("<td").append(cellStyleBuilder).append(">").append(cellValue).append("</td>");
            cellStyleBuilder.setLength(0);
        }
        tableBuilder.append("</tr>");
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

        if (obj instanceof HTMLTableInterceptor) {
            fields = ((HTMLTableInterceptor) obj).interceptHtmlFields(fields, obj.getClass());
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
     * @param writeTableable
     * @param tableBuilder
     * @param tableHeaders
     */
    private void writeTableHeaders(WriteTableable writeTableable, StringBuilder tableBuilder, List<String> tableHeaders, HTMLStyle... htmlStyles) {
        if (tableHeaders != null && !tableHeaders.isEmpty()) {
            StringBuilder cellStyleBuilder = new StringBuilder();
            tableBuilder.append("<thead>").append("<tr>");
            for (String header : tableHeaders) {
                if (writeTableable instanceof HTMLTableInterceptor) {
                    ((HTMLTableInterceptor) writeTableable).interceptHtmlHeaderCell(cellStyleBuilder, header, writeTableable.getClass());
                }
                Arrays.stream(htmlStyles).forEach(style -> cellStyleBuilder.append(style != null ? style.buildStyle() : ""));
                if (cellStyleBuilder.length() > 0) {
                    cellStyleBuilder.insert(0, " style=\"");
                    cellStyleBuilder.append("\"");
                }
                tableBuilder.append("<th").append(cellStyleBuilder).append(">").append(header).append("</th>");
                cellStyleBuilder.setLength(0);
            }
            tableBuilder.append("</tr>").append("</thead>");
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


    public void build() {
        String html = "<!DOCTYPE html>" +
                "<html lang=\"zh-CN\">" +
                "<meta charset=\"UTF-8\">" +
                "<head>" +
                "<title>HTML文档</title>" +
                "<style>" + styleBuffer + "</style>" +
                "</head>" +
                "<body>" +
                bodyBuffer +
                "</body>" +
                "</html>";
        try {
            byte[] bytes = html.getBytes(StandardCharsets.UTF_8);
            this.outputStream.write(bytes);
        } catch (IOException e) {
            throw new RuntimeException("构建HTML文档发生异常", e);
        }
    }


    private String tableDefaultStyle() {
        return "table{text-align:left;border-collapse:collapse;width:100%;margin-left:auto;margin-right:auto;} table thead {text-align:left;} table th {height:40px;} table td {height:30px;}\n" +
                "table,th,td{border:1px solid #b4b3b3;padding-left:10px;padding-right:10px;}";
    }


    public class ParagraphBuilder {

        private final StringBuilder paragraphBuilder;

        public ParagraphBuilder() {
            paragraphBuilder = new StringBuilder();
        }

        public HTMLBuilder.ParagraphBuilder text(String text, HTMLStyle... htmlStyles) {
            StringBuilder styleBuilder = new StringBuilder();
            for (HTMLStyle htmlStyle : htmlStyles) {
                if (htmlStyle != null) {
                    styleBuilder.append(htmlStyle.buildStyle());
                }
            }
            paragraphBuilder.append(String.format("<span style=\"%s\">%s</span>", styleBuilder, text));
            return this;
        }

        public HTMLBuilder endParagraph() {
            HTMLBuilder.this.bodyBuffer.append(paragraphBuilder);
            return HTMLBuilder.this;
        }

    }
}
