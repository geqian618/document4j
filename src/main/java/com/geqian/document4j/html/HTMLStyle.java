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

        public static Font fontsize(String fontsize) {
            return new Font(String.format("font-size:%s;", fontsize));
        }

        public static Font fontFamily(String fontFamily) {
            return new Font(String.format("font-size:%s,serif;", fontFamily));
        }

        public static Font fontColor(String fontColor) {
            return new Font(String.format("color:%s;", fontColor));
        }
    }


    class Paragraph implements HTMLStyle {

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


        /**
         * 行高
         *
         * @param lineHeight
         * @return
         */
        public static Paragraph lineHeight(String lineHeight) {
            return new Paragraph(String.format("line-height:%s;", lineHeight));
        }

    }


    enum Display implements HTMLStyle {
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


    class Common implements HTMLStyle {

        private final String style;

        public Common(String style) {
            this.style = style;
        }

        @Override
        public String getStyle() {
            return style;
        }


        /**
         * 上填充
         *
         * @param paddingTop
         * @return
         */
        public static Common paddingTop(String paddingTop) {
            return new Common(String.format("padding-top:%s;", paddingTop));
        }


        /**
         * 下填充
         *
         * @param paddingBottom
         * @return
         */
        public static Common paddingBottom(String paddingBottom) {
            return new Common(String.format("padding-bottom:%s;", paddingBottom));
        }

        /**
         * 左填充
         *
         * @param paddingBottom
         * @return
         */
        public static Common paddingLeft(String paddingBottom) {
            return new Common(String.format("padding-left:%s;", paddingBottom));
        }


        /**
         * 右填充
         *
         * @param paddingBottom
         * @return
         */
        public static Common paddingRight(String paddingBottom) {
            return new Common(String.format("padding-right:%s;", paddingBottom));
        }

        /**
         * 填充
         *
         * @param padding
         * @return
         */
        public static Common padding(String padding) {
            return new Common(String.format("padding:%s;", padding));
        }

        /**
         * 上边距
         *
         * @param marginTop
         * @return
         */
        public static Common marginTop(String marginTop) {
            return new Common(String.format("margin-top:%s;", marginTop));
        }

        /**
         * 下边距
         *
         * @param marginBottom
         * @return
         */
        public static Common marginBottom(String marginBottom) {
            return new Common(String.format("margin-bottom:%s;", marginBottom));
        }

        /**
         * 左边距
         *
         * @param marginLeft
         * @return
         */
        public static Common marginLeft(String marginLeft) {
            return new Common(String.format("margin-left:%s;", marginLeft));
        }


        /**
         * 右边距
         *
         * @param marginRight
         * @return
         */
        public static Common marginRight(String marginRight) {
            return new Common(String.format("margin-right:%s;", marginRight));
        }

        /**
         * 边距
         *
         * @param margin
         * @return
         */
        public static Common margin(String margin) {
            return new Common(String.format("margin:%s;", margin));
        }
    }

}
