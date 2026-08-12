package com.geqian.document4j.markdown;

import java.util.function.Function;

/**
 * @author geqian
 * @date 1:36 2024/1/31
 */
public interface MarkDownStyle {

    String buildStyle(String text);

    /**
     * 对齐方式
     */
    enum CellAlignment implements MarkDownStyle {
        LEFT(":-"),
        RIGHT("-:"),
        CENTER(":-:");

        private final String style;

        CellAlignment(String style) {
            this.style = style;
        }

        @Override
        public String buildStyle(String text) {
            return this.style;
        }
    }

    /**
     * 标题级别
     */
    enum Heading implements MarkDownStyle {

        LEVEL_1("# %s\n"),
        LEVEL_2("## %s\n"),
        LEVEL_3("### %s\n"),
        LEVEL_4("#### %s\n"),
        LEVEL_5("##### %s\n"),
        LEVEL_6("###### %s\n");

        private final String template;

        Heading(String template) {
            this.template = template;
        }


        @Override
        public String buildStyle(String text) {
            return String.format(template, text);
        }
    }


    /**
     * 字体样式
     */
    public class Font implements MarkDownStyle {

        public static final Font BOLD = new Font("**%s**");

        public static final Font ITALIC = new Font("*%s*");

        public static final Font DELETE_LINE = new Font("~~%s~~");

        public static final Font CODE = new Font("`%s`");

        public static final Font UNDER_LINE = new Font("<u>%s</u>");

        // 取消加粗
        public static final Font NOT_BOLD = new Font(text -> text.replaceAll("\\*\\*(.+?)\\*\\*", "$1"));

        // 取消斜体（负向断言避免误删 ** 加粗标记）
        public static final Font NOT_ITALIC = new Font(text -> text.replaceAll("(?<!\\*)\\*(?!\\*)(.+?)(?<!\\*)\\*(?!\\*)", "$1"));

        // 取消下划线
        public static final Font NOT_UNDER_LINE = new Font(text -> text.replaceAll("<u>(.+?)</u>", "$1"));

        // 取消删除线
        public static final Font NOT_DELETE_LINE = new Font(text -> text.replaceAll("~~(.+?)~~", "$1"));

        // 取消代码样式
        public static final Font NOT_CODE = new Font(text -> text.replaceAll("`(.+?)`", "$1"));


        public static Font fontColor(String color) {
            return new Font("<span style=\"color:" + color + ";\">%s</span>");
        }

        public static Font fontSize(int size) {
            return new Font("<span style=\"font-size:" + size + "px;\">%s</span>");
        }

        private String template;

        private Function<String, String> transformer;

        Font(String template) {
            this.template = template;
        }

        Font(Function<String, String> transformer) {
            this.transformer = transformer;
        }

        @Override
        public String buildStyle(String text) {
            return transformer != null ? transformer.apply(text) : String.format(template, text);
        }
    }
}
