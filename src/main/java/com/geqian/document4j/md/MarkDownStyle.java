package com.geqian.document4j.md;

/**
 * @author geqian
 * @date 1:36 2024/1/31
 */
public interface MarkDownStyle {

    /**
     * 对齐方式
     */
    public enum CellAlignment {
        LEFT(":-"),
        RIGHT("-:"),
        CENTER(":-:");

        private final String alignment;

        CellAlignment(String alignment) {
            this.alignment = alignment;
        }

        public String getMark() {
            return alignment;
        }
    }

    /**
     * 标题级别
     */
    public enum Title {

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

        public String fillStyle(String text) {
            return String.format(template, text);
        }
    }


    /**
     * 字体样式
     */
    public enum Font {

        BOLD("**%s**"),
        ITALIC("*%s*"),
        DELETE_LINE("~~%s~~"),
        CODE("`%s`"),
        UNDER_LINE("<u>%s</u>");

        private final String template;

        Font(String template) {
            this.template = template;
        }

        public String fillStyle(String text) {
            return String.format(template, text);
        }
    }
}
