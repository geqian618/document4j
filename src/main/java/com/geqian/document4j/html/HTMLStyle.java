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
        public static Font ITALIC = new Font("font-style:italic;");
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
        public static Paragraph ALIGN_LEFT = new Paragraph("text-align:left;");
        // 居中对齐
        public static Paragraph ALIGN_CENTER = new Paragraph("text-align:center;");
        // 右对齐
        public static Paragraph ALIGN_RIGHT = new Paragraph("text-align:right;");

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


    class Padding implements HTMLStyle {

        private final String style;

        public Padding(String style) {
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
        public static Padding top(String paddingTop) {
            return new Padding(String.format("padding-top:%s;", paddingTop));
        }


        /**
         * 下填充
         *
         * @param paddingBottom
         * @return
         */
        public static Padding bottom(String paddingBottom) {
            return new Padding(String.format("padding-bottom:%s;", paddingBottom));
        }

        /**
         * 左填充
         *
         * @param paddingLeft
         * @return
         */
        public static Padding left(String paddingLeft) {
            return new Padding(String.format("padding-left:%s;", paddingLeft));
        }


        /**
         * 右填充
         *
         * @param paddingRight
         * @return
         */
        public static Padding right(String paddingRight) {
            return new Padding(String.format("padding-right:%s;", paddingRight));
        }

        /**
         * 填充
         *
         * @param vertical
         * @param horizontal
         * @return
         */
        public static Margin padding(String vertical, String horizontal) {
            return new Margin(String.format("padding:%s %s;", vertical, horizontal));
        }

        /**
         * 填充
         *
         * @param padding
         * @return
         */
        public static Margin padding(String padding) {
            return new Margin(String.format("padding:%s;", padding));
        }

    }

    class Margin implements HTMLStyle {

        private final String style;

        public Margin(String style) {
            this.style = style;
        }

        @Override
        public String getStyle() {
            return style;
        }

        /**
         * 上边距
         *
         * @param marginTop
         * @return
         */
        public static Margin top(String marginTop) {
            return new Margin(String.format("margin-top:%s;", marginTop));
        }

        /**
         * 下边距
         *
         * @param marginBottom
         * @return
         */
        public static Margin bottom(String marginBottom) {
            return new Margin(String.format("margin-bottom:%s;", marginBottom));
        }

        /**
         * 左边距
         *
         * @param marginLeft
         * @return
         */
        public static Margin left(String marginLeft) {
            return new Margin(String.format("margin-left:%s;", marginLeft));
        }


        /**
         * 右边距
         *
         * @param marginRight
         * @return
         */
        public static Margin right(String marginRight) {
            return new Margin(String.format("margin-right:%s;", marginRight));
        }

        /**
         * 边距
         *
         * @param vertical
         * @param horizontal
         * @return
         */
        public static Margin margin(String vertical, String horizontal) {
            return new Margin(String.format("margin:%s %s;", vertical, horizontal));
        }

        /**
         * 边距
         *
         * @param margin
         * @return
         */
        public static Margin margin(String margin) {
            return new Margin(String.format("margin:%s;", margin));
        }

    }

    class Dimension implements HTMLStyle {

        private final String style;

        public Dimension(String style) {
            this.style = style;
        }

        @Override
        public String getStyle() {
            return style;
        }

        /**
         * 设置宽度
         *
         * @param width
         * @return
         */
        public static Dimension width(String width) {
            return new Dimension(String.format("width:%s;", width));
        }

        /**
         * 设置高度
         *
         * @param height
         * @return
         */
        public static Dimension height(String height) {
            return new Dimension(String.format("height:%s;", height));
        }
    }
}
