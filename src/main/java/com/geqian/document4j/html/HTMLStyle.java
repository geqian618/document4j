package com.geqian.document4j.html;

/**
 * @author geqian
 * @date 18:12 2024/3/9
 */
public interface HTMLStyle {

    String getStyle();

    enum Title implements HTMLStyle {
        FIRST("font-size:32px;font-weight:bold;color:#333;"),
        SECOND("font-size:28px;font-weight:bold;color:#555;"),
        THIRD("font-size:24px;font-weight:bold;color:#777;"),
        FOUR("font-size:20px;font-weight:bold;color:#999;"),
        FIVE("font-size:16px;font-weight:bold;color:#aaa;"),
        SIX("font-size:14px; font-weight:bold;color:#bbb;");

        private final String level;

        Title(String level) {
            this.level = level;
        }

        @Override
        public String getStyle() {
            return this.level;
        }

    }


    class Font implements HTMLStyle {

        // 加粗字体
        public static Font BOLD = new Font("font-weight:bold;");
        // 细字体
        public static Font LIGHTER = new Font("font-weight:lighter;");
        // 斜体
        public static Font ITALIC = new Font("font-styleitalic;");
        // 无修饰线条
        public static Font NONE_LINE = new Font("text-decoration-line:none;");
        // 上划线
        public static Font OVER_LINE = new Font("text-decoration-line:overline;");
        // 下划线
        public static Font UNDER_LINE = new Font("text-decoration-line:underline;");
        // 删除线
        public static Font DELETE_LINE = new Font("text-decoration-line:line-through;");
        // 双删除线
        public static Font DOUBLE_DELETE_LINE = new Font("text-decoration-line:line-through;text-decoration-style:double;");
        // 双下划线
        public static Font DOUBLE_UNDER_LINE = new Font("text-decoration-line:underline;text-decoration-style:double;");
        // 虚下划线
        public static Font DASHED_UNDER_LINE = new Font("text-decoration-line:underline;text-decoration-style:dashed;");
        // 点下划线
        public static Font DOTTED_UNDER_LINE = new Font("text-decoration-line:underline;text-decoration-style:dotted;");

        private final String style;

        public Font(String style) {
            this.style = style;
        }

        @Override
        public String getStyle() {
            return this.style;
        }

        public static Font fontsize(int fontsize) {
            return new Font(String.format("font-size:%dpx;", fontsize));
        }

        public static Font fontFamily(String fontFamily) {
            return new Font(String.format("font-size:%s,serif;", fontFamily));
        }

        public static Font fontColor(String fontColor) {
            return new Font(String.format("color:%s;", fontColor));
        }
    }


    public class Paragraph implements HTMLStyle {

        private final String style;

        public Paragraph(String style) {
            this.style = style;
        }

        @Override
        public String getStyle() {
            return this.style;
        }

        // 左对齐
        public static Paragraph AlignLeft = new Paragraph("text-align:left;");
        // 居中对齐
        public static Paragraph AlignCenter = new Paragraph("text-align:center;");
        // 右对齐
        public static Paragraph AlignRight = new Paragraph("text-align:right;");

        /**
         * 首行缩进字符个数
         *
         * @param index
         * @return
         */
        public static Paragraph firstLineIndent(int index) {
            return new Paragraph(String.format("text-indent:%dem;", index));
        }

    }


    public enum Display implements HTMLStyle {
        BLOCK("display:block;"),
        INLINE_BLOCK("display:inline-block;"),
        INLINE("display:inline;");

        private final String style;

        Display(String style) {
            this.style = style;
        }

        @Override
        public String getStyle() {
            return style;
        }
    }

}
