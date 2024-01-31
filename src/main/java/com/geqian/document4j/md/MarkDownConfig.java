package com.geqian.commontools.markdown;

/**
 * @author geqian
 * @date 1:36 2024/1/31
 */
public class MarkDownConfig {

    /**
     * 对齐方式
     */
    public enum Alignment {
        LEFT(":-"),
        RIGHT("-:"),
        CENTER(":-:");

        private final String mark;

        Alignment(String mark) {
            this.mark = mark;
        }

        public String getMark() {
            return mark;
        }
    }

    /**
     * 标题级别
     */
    public enum Level {

        First("# %s\n"),
        Second("## %s\n"),
        Third("### %s\n"),
        Four("#### %s\n"),
        Five("##### %s\n"),
        Six("###### %s\n");

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
