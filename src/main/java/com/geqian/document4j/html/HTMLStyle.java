package com.geqian.document4j.html;

/**
 * @author geqian
 * @date 18:12 2024/3/9
 */
public interface HTMLStyle {

    String buildStyle();

    enum Heading implements HTMLStyle {
        LEVEL_1("h1", "font-size:32px;font-weight:bold;color:#333;"),
        LEVEL_2("h2", "font-size:28px;font-weight:bold;color:#555;"),
        LEVEL_3("h3", "font-size:24px;font-weight:bold;color:#777;"),
        LEVEL_4("h4", "font-size:20px;font-weight:bold;color:#999;"),
        LEVEL_5("h5", "font-size:16px;font-weight:bold;color:#aaa;"),
        LEVEL_6("h6", "font-size:14px; font-weight:bold;color:#bbb;");

        private final String style;

        private final String tagName;

        Heading(String tagName, String style) {
            this.tagName = tagName;
            this.style = style;
        }

        @Override
        public String buildStyle() {
            return this.style;
        }

        public String getTagName() {
            return tagName;
        }
    }


    class Font implements HTMLStyle {
        // 加粗字体
        public static final Font BOLD = new Font("font-weight:bold;");
        // 细字体
        public static final Font LIGHTER = new Font("font-weight:lighter;");
        // 斜体
        public static final Font ITALIC = new Font("font-style:italic;");
        // 无修饰线条
        public static final Font NONE_LINE = new Font("text-decoration-line:none;");
        // 上划线
        public static final Font OVER_LINE = new Font("text-decoration-line:overline;");
        // 下划线
        public static final Font UNDER_LINE = new Font("text-decoration-line:underline;");
        // 删除线
        public static final Font DELETE_LINE = new Font("text-decoration-line:line-through;");
        // 双删除线
        public static final Font DOUBLE_DELETE_LINE = new Font("text-decoration-line:line-through;text-decoration-style:double;");
        // 双下划线
        public static final Font DOUBLE_UNDER_LINE = new Font("text-decoration-line:underline;text-decoration-style:double;");
        // 虚下划线
        public static final Font DASHED_UNDER_LINE = new Font("text-decoration-line:underline;text-decoration-style:dashed;");
        // 点下划线
        public static final Font DOTTED_UNDER_LINE = new Font("text-decoration-line:underline;text-decoration-style:dotted;");
        // 取消加粗（标题默认加粗时按需传入，追加在标题样式之后可覆盖 font-weight:bold）
        public static final Font NOT_BOLD = new Font("font-weight:normal;");
        // 取消斜体
        public static final Font NOT_ITALIC = new Font("font-style:normal;");
        // 取消上划线
        public static final Font NOT_OVER_LINE = new Font("text-decoration-line:none;");
        // 取消下划线
        public static final Font NOT_UNDER_LINE = new Font("text-decoration-line:none;");
        // 取消双下划线
        public static final Font NOT_DOUBLE_UNDER_LINE = new Font("text-decoration-line:none;");
        // 取消虚下划线
        public static final Font NOT_DASHED_UNDER_LINE = new Font("text-decoration-line:none;");
        // 取消点下划线
        public static final Font NOT_DOTTED_UNDER_LINE = new Font("text-decoration-line:none;");
        // 取消删除线
        public static final Font NOT_DELETE_LINE = new Font("text-decoration-line:none;");
        // 取消双删除线
        public static final Font NOT_DOUBLE_DELETE_LINE = new Font("text-decoration-line:none;");

        private final String style;

        public Font(String style) {
            this.style = style;
        }

        @Override
        public String buildStyle() {
            return this.style;
        }

        public static Font fontsize(String fontsize) {
            return new Font(String.format("font-size:%s;", fontsize));
        }

        public static Font fontFamily(String fontFamily) {
            return new Font(String.format("font-family:%s,serif;", fontFamily));
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
        public String buildStyle() {
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
         * @param number 字符个数
         * @return
         */
        public static Paragraph firstLineIndent(int number) {
            return new Paragraph(String.format("text-indent:%dem;", number));
        }


        /**
         * 行高
         *
         * @param lineHeight 行高
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
        public String buildStyle() {
            return style;
        }
    }


    class Padding implements HTMLStyle {

        private final String style;

        public Padding(String style) {
            this.style = style;
        }

        @Override
        public String buildStyle() {
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
        public static Padding padding(String vertical, String horizontal) {
            return new Padding(String.format("padding:%s %s;", vertical, horizontal));
        }

        /**
         * 填充
         *
         * @param padding
         * @return
         */
        public static Padding padding(String padding) {
            return new Padding(String.format("padding:%s;", padding));
        }

    }

    class Margin implements HTMLStyle {

        private final String style;

        public Margin(String style) {
            this.style = style;
        }

        @Override
        public String buildStyle() {
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

    class Size implements HTMLStyle {

        private final String style;

        public Size(String style) {
            this.style = style;
        }

        @Override
        public String buildStyle() {
            return style;
        }

        /**
         * 设置宽度
         *
         * @param width
         * @return
         */
        public static Size width(String width) {
            return new Size(String.format("width:%s;", width));
        }

        /**
         * 设置高度
         *
         * @param height
         * @return
         */
        public static Size height(String height) {
            return new Size(String.format("height:%s;", height));
        }
    }
}
