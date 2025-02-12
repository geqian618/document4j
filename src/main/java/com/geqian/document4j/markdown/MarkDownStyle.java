package com.geqian.document4j.markdown;

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
    enum Title implements MarkDownStyle {

        FIRST("# %s\n"),
        SECOND("## %s\n"),
        THIRD("### %s\n"),
        FOUR("#### %s\n"),
        FIVE("##### %s\n"),
        SIX("###### %s\n");

        private final String template;

        Title(String template) {
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

        public static Font fontColor(String color) {
            return new Font("<span style=\"color:" + color + ";\">%s</span>");
        }

        public static Font fontSize(int size) {
            return new Font("<span style=\"font-size:" + size + "px;\">%s</span>");
        }

        private final String template;

        Font(String template) {
            this.template = template;
        }


        @Override
        public String buildStyle(String text) {
            return String.format(template, text);
        }
    }
}
