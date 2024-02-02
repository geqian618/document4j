package com.geqian.document4j.md;

/**
 * @author geqian
 * @date 1:36 2024/1/31
 */
public class MarkDownStyle {

    /**
     * 对齐方式
     */
    public enum Alignment {
        LEFT(":-"),
        RIGHT("-:"),
        CENTER(":-:");

        private final String alignment;

        Alignment(String alignment) {
            this.alignment = alignment;
        }

        public String getMark() {
            return alignment;
        }
    }

    /**
     * 标题级别
     */
    public enum Level {

        FIRST("# %s\n"),
        SECOND("## %s\n"),
        THIRD("### %s\n"),
        FOUR("#### %s\n"),
        FIVE("##### %s\n"),
        SIX("###### %s\n");

        private final String template;

        Level(String template) {
            this.template = template;
        }

        public String fillStyle(String text) {
            return String.format(template, text);
        }
    }


    /**
     * 字体样式
     */
    public enum FontStyle {

        BOLD("**%s**"),
        ITALIC("*%s*"),
        DELETE_LINE("~~%s~~"),
        CODE("`%s`"),
        UNDER_LINE("<u>%s</u>");

        private final String template;

        FontStyle(String template) {
            this.template = template;
        }

        public String fillStyle(String text) {
            return String.format(template, text);
        }
    }
}
