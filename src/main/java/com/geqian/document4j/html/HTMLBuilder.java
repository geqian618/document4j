package com.geqian.document4j.html;

import com.geqian.document4j.common.core.AbstractDocumentBuilder;
import com.geqian.document4j.common.core.TableSupport;
import com.geqian.document4j.common.core.WriteTableable;
import com.geqian.document4j.common.extend.Converter;

import java.io.*;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author geqian
 * @date 18:03 2024/3/9
 */
public class HTMLBuilder extends AbstractDocumentBuilder<HTMLBuilder, HTMLStyle> {

    private final StringBuilder labelBuilder = new StringBuilder();

    private final StringBuilder styleBuilder = new StringBuilder();

    private final StringBuilder javaScriptBuilder = new StringBuilder();


    private HTMLBuilder() {
        setStyleSelector(defaultStyle());
        setJavaScript(defaultJavaScript());
    }


    public HTMLBuilder setStyleSelector(String style) {
        this.styleBuilder.append(style);
        return this;
    }

    public HTMLBuilder setJavaScript(String style) {
        this.javaScriptBuilder.append(style);
        return this;
    }

    public static HTMLBuilder create() {
        return new HTMLBuilder();
    }


    /**
     * 是否需要左侧目录
     *
     * @param required
     * @return
     */
    public HTMLBuilder setCatalogue(boolean required) {
        setJavaScript("showCatalogue(" + required + ")");
        return this;
    }


    public HTMLBuilder heading(String text, HTMLStyle heading, List<HTMLStyle> styles) {
        if (!(heading instanceof HTMLStyle.Heading)) {
            throw new IllegalArgumentException("HTMLStyle.heading cannot be null");
        }
        HTMLStyle.Heading headingStyle = (HTMLStyle.Heading) heading;
        this.labelBuilder.append(String.format("<%s style=\"%s\">%s</%s>", headingStyle.getTagName(), heading.buildStyle() + styleOf(styles), escapeHtml(text), headingStyle.getTagName()));
        return this;
    }


    public HTMLBuilder paragraph(String text, List<HTMLStyle> htmlStyles) {
        this.labelBuilder.append(String.format("<p style=\"%s\">%s</p>", styleOf(htmlStyles), escapeHtml(text)));
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


    public HTMLBuilder link(String text, String link, List<HTMLStyle> htmlStyles) {
        this.labelBuilder.append(String.format("<a style=\"%s\" href=\"%s\">%s</a>", styleOf(htmlStyles), safeHref(link), escapeHtml(text)));
        return this;
    }

    public HTMLBuilder blankRow() {
        this.labelBuilder.append("<br>");
        return this;
    }

    public HTMLBuilder newPage() {
        this.labelBuilder.append("<div style=\"page-break-after:always;\"></div>");
        return this;
    }

    /**
     * 添加无序列表
     */
    public HTMLBuilder list(List<String> elements, List<HTMLStyle> htmlStyles) {
        if (elements != null && !elements.isEmpty()) {
            String style = styleOf(htmlStyles);
            String styleAttr = style.isEmpty() ? "" : " style=\"" + style + "\"";
            this.labelBuilder.append("<ul>");
            for (String element : elements) {
                this.labelBuilder.append("<li").append(styleAttr).append(">").append(escapeHtml(element)).append("</li>");
            }
            this.labelBuilder.append("</ul>");
        }
        return this;
    }

    /**
     * 添加图片，以 base64 内联，避免依赖外部图片地址
     */
    public HTMLBuilder picture(byte[] bytes, String filename, int width, int height) {
        String src = "data:" + imageMimeType(filename) + ";base64," + Base64.getEncoder().encodeToString(bytes);
        this.labelBuilder.append(String.format("<img src=\"%s\" alt=\"%s\" width=\"%d\" height=\"%d\">", src, escapeHtml(filename), width, height));
        return this;
    }

    /**
     * 拼接样式
     */
    private static String styleOf(List<HTMLStyle> styles) {
        StringBuilder styleBuilder = new StringBuilder();
        if (styles != null) {
            for (HTMLStyle htmlStyle : styles) {
                if (htmlStyle != null) {
                    styleBuilder.append(htmlStyle.buildStyle());
                }
            }
        }
        return styleBuilder.toString();
    }

    public HTMLBuilder divisionLine() {
        this.labelBuilder.append("<hr>");
        return this;
    }


    public HTMLBuilder html(String html) {
        this.labelBuilder.append(html);
        return this;
    }

    /**
     * 写入表格
     *
     * @param dataList
     * @return
     */
    public HTMLBuilder table(List<? extends WriteTableable> dataList, List<HTMLStyle> htmlStyles) {

        if (dataList != null && !(dataList = dataList.stream().filter(Objects::nonNull).collect(Collectors.toList())).isEmpty()) {

            WriteTableable writeTableable = dataList.get(0);

            TableData tableData = resolveTable(writeTableable);

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
            writeTableHeaders(writeTableable, tableBuilder, tableData.headers, htmlStyles);
            //写入表格主体数据
            writeTableRows(tableBuilder, dataList, tableData.fields, tableData.enumMappings, tableData.converters, htmlStyles);
            tableBuilder.append("</table>");

            labelBuilder.append(tableBuilder);
        }
        return this;
    }


    /**
     * 写入表格
     *
     * @param data
     * @return
     */
    public HTMLBuilder table(Object[][] data, List<HTMLStyle> htmlStyles) {
        if (data != null && data.length > 0) {
            String style = styleOf(htmlStyles);
            String styleAttr = style.isEmpty() ? "" : " style=\"" + style + "\"";
            labelBuilder.append("<table>");
            for (int i = 0; i < data.length; i++) {
                Object[] rowData = data[i];
                if (i == 0) {
                    labelBuilder.append("<thead><tr>");
                    for (Object head : rowData) {
                        labelBuilder.append("<th").append(styleAttr).append(">").append(escapeHtml(cellText(head))).append("</th>");
                    }
                    labelBuilder.append("</tr></thead>");
                } else {
                    labelBuilder.append("<tbody><tr>");
                    for (Object cell : rowData) {
                        labelBuilder.append("<td").append(styleAttr).append(">").append(escapeHtml(cellText(cell))).append("</td>");
                    }
                    labelBuilder.append("</tr></tbody>");
                }
            }
            labelBuilder.append("</table>");
        }
        return this;
    }

    private void writeTableRows(StringBuilder tableBuilder, List<? extends WriteTableable> dataList, List<Field> fields,
                                Map<String, String> enumMappings, Map<String, Converter> converters, List<HTMLStyle> htmlStyles) {
        tableBuilder.append("<tbody>");
        for (WriteTableable writeTableable : dataList) {
            writeTableRow(tableBuilder, writeTableable, fields, enumMappings, converters, htmlStyles);
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
    private void writeTableRow(StringBuilder tableBuilder, WriteTableable obj, List<Field> fieldList,
                               Map<String, String> enumMappings, Map<String, Converter> converters, List<HTMLStyle> htmlStyles) {
        StringBuilder cellStyleBuilder = new StringBuilder();
        tableBuilder.append("<tr>");
        for (Field field : fieldList) {
            String cellValue = TableSupport.getCellValue(obj, field, enumMappings, converters);
            //拦截单元格
            if (obj instanceof HTMLTableInterceptor) {
                cellValue = ((HTMLTableInterceptor) obj).interceptHtmlCell(cellStyleBuilder, cellValue, obj, field);
            }
            cellStyleBuilder.append(styleOf(htmlStyles));
            if (cellStyleBuilder.length() > 0) {
                cellStyleBuilder.insert(0, " style=\"");
                cellStyleBuilder.append("\"");
            }
            tableBuilder.append("<td").append(cellStyleBuilder).append(">").append(escapeHtml(cellValue)).append("</td>");
            cellStyleBuilder.setLength(0);
        }
        tableBuilder.append("</tr>");
    }

    /**
     * 写入表头
     *
     * @param writeTableable
     * @param tableBuilder
     * @param tableHeaders
     */
    private void writeTableHeaders(WriteTableable writeTableable, StringBuilder tableBuilder, List<String> tableHeaders, List<HTMLStyle> htmlStyles) {
        if (tableHeaders != null && !tableHeaders.isEmpty()) {
            StringBuilder cellStyleBuilder = new StringBuilder();
            tableBuilder.append("<thead>").append("<tr>");
            for (String header : tableHeaders) {
                if (writeTableable instanceof HTMLTableInterceptor) {
                    ((HTMLTableInterceptor) writeTableable).interceptHtmlHeaderCell(cellStyleBuilder, header, writeTableable.getClass());
                }
                cellStyleBuilder.append(styleOf(htmlStyles));
                if (cellStyleBuilder.length() > 0) {
                    cellStyleBuilder.insert(0, " style=\"");
                    cellStyleBuilder.append("\"");
                }
                tableBuilder.append("<th").append(cellStyleBuilder).append(">").append(escapeHtml(header)).append("</th>");
                cellStyleBuilder.setLength(0);
            }
            tableBuilder.append("</tr>").append("</thead>");
        }
    }


    /**
     * 转义 HTML 特殊字符。文本内容默认转义后输出，防止使用方把来自用户输入的
     * 数据写入文档时被当作标签执行（存储型 XSS）；需要输出富文本请使用 html(String)。
     */
    private static String escapeHtml(String text) {
        if (text == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder(text.length() + 16);
        for (char c : text.toCharArray()) {
            switch (c) {
                case '<':
                    sb.append("&lt;");
                    break;
                case '>':
                    sb.append("&gt;");
                    break;
                case '&':
                    sb.append("&amp;");
                    break;
                case '"':
                    sb.append("&quot;");
                    break;
                default:
                    sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * 过滤链接协议，仅拦截 javascript:/vbscript:/data: 等可执行协议，放行 http/https 与相对地址
     */
    private static String safeHref(String link) {
        if (link == null) {
            return "";
        }
        String lower = link.trim().toLowerCase().replace(" ", "").replace("\t", "");
        if (lower.startsWith("javascript:") || lower.startsWith("vbscript:") || lower.startsWith("data:")) {
            return "#";
        }
        return link;
    }

    /**
     * 二维数组单元格文本：null 写作空字符串，避免字面量 "null" 出现在文档中
     */
    private static String cellText(Object value) {
        return value == null ? "" : String.valueOf(value);
    }


    /**
     * HTMl文档转换为字节数组
     *
     * @return
     */
    public byte[] asBytes() {
        return buildHtml().getBytes(StandardCharsets.UTF_8);
    }

    private String buildHtml() {
        return "<!DOCTYPE html>\n" +
                "<html lang=\"zh-CN\">\n" +
                "<meta charset=\"UTF-8\">\n" +
                "<head>\n" +
                "<title>HTML文档</title>\n" +
                "<style>\n" +
                    styleBuilder +
                "\n</style>\n" +
                "</head>\n" +
                "<body>\n" +
                "<div class=\"page-layout\">\n" +
                "    <div class=\"container\">\n" +
                "        <!--侧边栏区域-->\n" +
                "        <section class=\"aside\">\n" +
                "        </section>\n" +
                "        <!--拖动按钮-->\n" +
                "        <div class=\"drag-bar\"></div>\n" +
                "        <!--内容区域-->\n" +
                "        <div class=\"content\">\n" +
                            labelBuilder +
                "        \n</div>\n" +
                "    </div>\n" +
                "</div>\n" +
                "<script type=\"text/javascript\">\n" +
                javaScriptBuilder +
                "\n</script>\n" +
                "</body>\n" +
                "</html>\n";
    }


    /**
     * 设置 body 样式
     *
     * @param style
     * @return
     */
    public HTMLBuilder setBodyStyle(String style) {
        setStyleSelector(".page-layout {" + style + "}");
        return this;
    }


    /**
     * 加载默认样式
     *
     * @return
     */
    private String defaultStyle() {
        InputStream inputStream = HTMLBuilder.class.getClassLoader().getResourceAsStream("css/default.css");
        return readToString(inputStream);
    }

    /**
     * 加载默认样式
     *
     * @return
     */
    private String defaultJavaScript() {
        InputStream inputStream = HTMLBuilder.class.getClassLoader().getResourceAsStream("js/default.js");
        return readToString(inputStream);
    }


    /**
     * 读取输入流为字符串
     *
     * @param inputStream
     * @return
     * @throws IOException
     */
    public static String readToString(InputStream inputStream) {
        try {
            StringBuilder stringBuilder = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line).append("\n");
                }
            }
            return stringBuilder.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public class ParagraphBuilder {

        private final StringBuilder paragraphBuilder;

        public ParagraphBuilder() {
            paragraphBuilder = new StringBuilder();
        }

        public HTMLBuilder.ParagraphBuilder text(String text, HTMLStyle... htmlStyles) {
            StringBuilder styleBuilder = new StringBuilder();
            if (htmlStyles != null) {
                for (HTMLStyle htmlStyle : htmlStyles) {
                    if (htmlStyle != null) {
                        styleBuilder.append(htmlStyle.buildStyle());
                    }
                }
            }
            paragraphBuilder.append(String.format("<span style=\"%s\">%s</span>", styleBuilder, escapeHtml(text)));
            return this;
        }

        public HTMLBuilder endParagraph() {
            HTMLBuilder.this.labelBuilder.append(paragraphBuilder);
            return HTMLBuilder.this;
        }

    }

}
