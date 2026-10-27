package com.geqian.document4j.markdown;

import com.geqian.document4j.common.core.AbstractDocumentBuilder;
import com.geqian.document4j.common.core.TableSupport;
import com.geqian.document4j.common.core.WriteTableable;
import com.geqian.document4j.common.extend.Converter;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author geqian
 * @date 21:33 2024/1/30
 */
public class MarkDownBuilder extends AbstractDocumentBuilder<MarkDownBuilder, MarkDownStyle> {

    private final StringBuilder stringBuilder;

    private MarkDownBuilder() {
        this.stringBuilder = new StringBuilder();
    }

    public static MarkDownBuilder create() {
        return new MarkDownBuilder();
    }


    /**
     * 添加目录
     *
     * @param hasCatalogue 是否添加目录
     * @return
     */
    public MarkDownBuilder addCatalogue(boolean hasCatalogue) {
        if (hasCatalogue) {
            this.stringBuilder.insert(0, "[TOC]\n");
        }
        return this;
    }


    /**
     * 标题
     *
     * @param text
     * @param heading
     * @return
     */
    public MarkDownBuilder heading(String text, MarkDownStyle heading, List<MarkDownStyle> styles) {
        if (!(heading instanceof MarkDownStyle.Heading)) {
            throw new IllegalArgumentException("MarkDownStyle.Heading cannot be null");
        }
        stringBuilder.append(heading.buildStyle(applyStyles(text, styles)));
        return this;
    }


    /**
     * 添加空白行
     *
     * @return
     */
    public MarkDownBuilder blankRow() {
        stringBuilder.append("\n\n");
        return this;
    }


    /**
     * 文本
     *
     * @param text
     * @param styles
     * @return
     */
    public MarkDownBuilder paragraph(String text, List<MarkDownStyle> styles) {
        stringBuilder.append(applyStyles(text, styles)).append("\n");
        return this;
    }

    /**
     * 分页（Markdown 无原生分页，输出 HTML 分页块）
     */
    public MarkDownBuilder newPage() {
        stringBuilder.append("<div style=\"page-break-after:always;\"></div>\n");
        return this;
    }

    /**
     * 添加无序列表
     */
    public MarkDownBuilder list(List<String> elements, List<MarkDownStyle> styles) {
        if (elements != null) {
            for (String element : elements) {
                stringBuilder.append("- ").append(applyStyles(element, styles)).append("\n");
            }
        }
        return this;
    }

    /**
     * 添加图片，以 base64 内联；Markdown 图片语法不支持尺寸，尺寸参数忽略
     */
    public MarkDownBuilder picture(byte[] bytes, String filename, int width, int height) {
        String src = "data:" + imageMimeType(filename) + ";base64," + Base64.getEncoder().encodeToString(bytes);
        image(filename, src);
        stringBuilder.append("\n");
        return this;
    }

    /**
     * 仅应用字体样式（对齐方式等样式不作用于文本）
     */
    private static String applyStyles(String text, List<MarkDownStyle> styles) {
        if (styles != null) {
            for (MarkDownStyle style : styles) {
                if (style instanceof MarkDownStyle.Font) {
                    text = style.buildStyle(text);
                }
            }
        }
        return text;
    }


    /**
     * 添加图片
     *
     * @param alt
     * @param url
     * @return
     */
    public MarkDownBuilder image(String alt, String url) {
        stringBuilder.append(String.format("![%s](%s)", alt, url));
        return this;
    }


    /**
     * 分割线
     *
     * @return
     */
    public MarkDownBuilder divisionLine() {
        stringBuilder.append("---\n");
        return this;
    }


    /**
     * 区块
     *
     * @param text
     * @return
     */
    public MarkDownBuilder block(String text) {
        stringBuilder.append(String.format("> %s\n", text));
        return this;
    }

    /**
     * 无序列表
     *
     * @param list
     * @return
     */
    public MarkDownBuilder unorderedList(String... list) {
        Arrays.stream(list).forEach(element -> stringBuilder.append(String.format("- %s\n", element)));
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
            stringBuilder.append(String.format("%d. %s\n", i + 1, list[i]));
        }
        return this;
    }


    /**
     * 链接
     *
     * @param text 链接文本
     * @param link 链接地址
     */
    public MarkDownBuilder link(String text, String link) {
        return link(text, link, Collections.<MarkDownStyle>emptyList());
    }

    /**
     * 链接
     *
     * @param text 链接文本
     * @param link 链接地址
     */
    public MarkDownBuilder link(String text, String link, List<MarkDownStyle> styles) {
        stringBuilder.append(String.format("[%s](%s)\n", applyStyles(text, styles), link));
        return this;
    }

    /**
     * 链接
     *
     * @param link
     * @return
     */
    public MarkDownBuilder link(String link) {
        stringBuilder.append(String.format("<%s>\n", link));
        return this;
    }


    /**
     * 原始 md 表达式
     *
     * @param raw
     * @return
     */
    public MarkDownBuilder raw(String raw) {
        stringBuilder.append(raw).append("\n");
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
     * 代码块
     *
     * @param type
     * @param code
     * @return
     */
    public MarkDownBuilder codeBlock(String type, String code) {
        stringBuilder.append(String.format("```%s\n%s\n```\n", type, code));
        return this;
    }


    /**
     * 写入表格
     *
     * @param dataList
     * @param styles   对齐方式（MarkDownStyle.CellAlignment）
     * @return
     */
    public MarkDownBuilder table(List<? extends WriteTableable> dataList, List<MarkDownStyle> styles) {

        if (dataList != null && !(dataList = dataList.stream().filter(Objects::nonNull).collect(Collectors.toList())).isEmpty()) {

            WriteTableable writeTableable = dataList.get(0);

            TableData tableData = resolveTable(writeTableable);

            StringBuilder tableBuilder = new StringBuilder();

            //写入表头
            writeTableHeaders(tableBuilder, tableData.headers, alignmentOf(styles));

            //写入表格主体数据
            writeTableRows(tableBuilder, dataList, tableData.fields, tableData.enumMappings, tableData.converters);

            stringBuilder.append(tableBuilder);
        }

        return this;
    }


    /**
     * 写入表格
     *
     * @param data
     * @param styles 对齐方式（MarkDownStyle.CellAlignment）
     * @return
     */
    public MarkDownBuilder table(Object[][] data, List<MarkDownStyle> styles) {
        MarkDownStyle.CellAlignment alignment = alignmentOf(styles);
        StringBuilder tableBuilder = new StringBuilder();
        if (data != null && data.length > 0) {
            for (int i = 0; i < data.length; i++) {
                Object[] rows = data[i];
                for (Object row : rows) {
                    //null 写作空字符串，避免字面量 "null" 出现在文档中
                    tableBuilder.append("|").append(escapeMdCell(row == null ? "" : String.valueOf(row)));
                }
                tableBuilder.append("|\n");
                if (i == 0) {
                    for (int j = 0; j < rows.length; j++) {
                        tableBuilder.append("|").append(alignment != null ? alignment.buildStyle(null) : "");
                    }
                    tableBuilder.append("|\n");
                }
            }
        }
        stringBuilder.append(tableBuilder);
        return this;
    }

    /**
     * 取出样式中的表格对齐方式
     */
    private static MarkDownStyle.CellAlignment alignmentOf(List<MarkDownStyle> styles) {
        if (styles != null) {
            for (MarkDownStyle style : styles) {
                if (style instanceof MarkDownStyle.CellAlignment) {
                    return (MarkDownStyle.CellAlignment) style;
                }
            }
        }
        return null;
    }

    private void writeTableRows(StringBuilder tableBuilder, List<? extends WriteTableable> dataList, List<Field> fields,
                                Map<String, String> enumMappings, Map<String, Converter> converters) {
        for (WriteTableable writeTableable : dataList) {
            writeTableRow(tableBuilder, writeTableable, fields, enumMappings, converters);
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
    private void writeTableRow(StringBuilder tableBuilder, WriteTableable obj, List<Field> fieldList,
                               Map<String, String> enumMappings, Map<String, Converter> converters) {
        for (Field field : fieldList) {
            String cellValue = TableSupport.getCellValue(obj, field, enumMappings, converters);
            //拦截单元格
            if (obj instanceof MarkdownTableInterceptor) {
                cellValue = ((MarkdownTableInterceptor) obj).interceptMdCellValue(cellValue, obj, field);
            }
            //竖线与换行会破坏 Markdown 表格结构，需转义
            tableBuilder.append("|").append(escapeMdCell(cellValue));
        }
        tableBuilder.append("|\n");
    }

    /**
     * 转义会破坏 Markdown 表格结构的字符：竖线改用转义形式，换行替换为 <br>
     */
    private static String escapeMdCell(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("|", "\\|").replace("\r\n", "\n").replace("\n", "<br>");
    }


    /**
     * 构建 Markdown 文档
     */
    public String buildMarkdown() {
        return stringBuilder.toString();
    }


    /**
     * Markdown 文档转换为字节数组
     *
     * @return
     */
    public byte[] asBytes() {
        return buildMarkdown().getBytes(StandardCharsets.UTF_8);
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
                tableBuilder.append("|").append(escapeMdCell(header));
            }
            tableBuilder.append("|\n");
            for (int i = 0; i < tableHeaders.size(); i++) {
                tableBuilder.append("|").append(alignment != null ? alignment.buildStyle(null) : "");
            }
            tableBuilder.append("|\n");
        }
    }


    public class ParagraphBuilder {

        private final StringBuilder subStringBuilder;

        public ParagraphBuilder() {
            this.subStringBuilder = new StringBuilder();
        }


        public MarkDownBuilder endParagraph() {
            MarkDownBuilder.this.stringBuilder.append(subStringBuilder).append("\n");
            return MarkDownBuilder.this;
        }


        /**
         * 原始 md 表达式
         *
         * @param raw
         * @return
         */
        public ParagraphBuilder raw(String raw) {
            subStringBuilder.append(raw);
            return this;
        }


        /**
         * 文本
         *
         * @param text
         * @param styles
         * @return
         */
        public ParagraphBuilder text(String text, MarkDownStyle.Font... styles) {
            if (styles != null) {
                for (MarkDownStyle.Font style : styles) {
                    if (style != null) {
                        text = style.buildStyle(text);
                    }
                }
            }
            subStringBuilder.append(text);
            return this;
        }


        /**
         * 链接
         *
         * @param linkName
         * @param link
         * @return
         */
        public ParagraphBuilder link(String linkName, String link) {
            subStringBuilder.append(String.format("[%s](%s)", linkName, link));
            return this;
        }


        /**
         * 链接
         *
         * @param link
         * @return
         */
        public ParagraphBuilder link(String link) {
            subStringBuilder.append(String.format("<%s>", link));
            return this;
        }
    }

}
