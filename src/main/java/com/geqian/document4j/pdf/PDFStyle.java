package com.geqian.document4j.pdf;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Element;

import java.util.function.Consumer;

/**
 * @author geqian
 * @date 23:33 2024/2/1
 */
public interface PDFStyle {

    void buildStyle(Object obj);


    class Title implements PDFStyle {

        public static final Title LEVEL_1 = new Title(1);
        public static final Title LEVEL_2 = new Title(2);
        public static final Title LEVEL_3 = new Title(3);
        public static final Title LEVEL_4 = new Title(4);
        public static final Title LEVEL_5 = new Title(5);
        public static final Title LEVEL_6 = new Title(6);

        private final int level;

        public Title(int level) {
            this.level = level;
        }

        public int getLevel() {
            return level;
        }

        @Override
        public void buildStyle(Object obj) {
            if (obj instanceof com.itextpdf.text.Font) {
                com.itextpdf.text.Font font = (com.itextpdf.text.Font) obj;
                font.setSize(22 - 2 * level);
                font.setStyle(com.itextpdf.text.Font.BOLD);
            }
        }
    }


    class Font implements PDFStyle {

        private String familyStyle;

        /**
         * 加粗
         */
        public static final PDFStyle.Font BOLD = new PDFStyle.Font(font -> font.setStyle(font.getStyle() | com.itextpdf.text.Font.BOLD));

        /**
         * 斜体
         */
        public static final PDFStyle.Font ITALIC = new PDFStyle.Font(font -> font.setStyle(font.getStyle() | com.itextpdf.text.Font.ITALIC));
        /**
         * 斜体加粗
         */
        public static final PDFStyle.Font BOLD_ITALIC = new PDFStyle.Font(font -> font.setStyle(font.getStyle() | com.itextpdf.text.Font.BOLDITALIC));

        /**
         * 下划线
         */
        public static final PDFStyle.Font UNDER_LINE = new PDFStyle.Font(font -> font.setStyle(font.getStyle() | com.itextpdf.text.Font.UNDERLINE));


        /**
         * 删除线
         */
        public static final PDFStyle.Font DELETE_LINE = new PDFStyle.Font(font -> font.setStyle(font.getStyle() | com.itextpdf.text.Font.DEFAULTSIZE));

        /**
         * 正常
         */
        public static final PDFStyle.Font NORMAL = new PDFStyle.Font(font -> font.setStyle(font.getStyle() | com.itextpdf.text.Font.NORMAL));


        private final Consumer<com.itextpdf.text.Font> wrapper;


        public Font(Consumer<com.itextpdf.text.Font> wrapper) {
            this.wrapper = wrapper;
        }

        public Font(String familyStyle, Consumer<com.itextpdf.text.Font> wrapper) {
            this.familyStyle = familyStyle;
            this.wrapper = wrapper;
        }

        public static PDFStyle.Font fontSize(float fontSize) {
            return new PDFStyle.Font(font -> font.setSize(fontSize));
        }


        public static PDFStyle.Font fontFamily(String fontFamilyPath) {
            return new PDFStyle.Font(fontFamilyPath, font -> font.setFamily(fontFamilyPath));
        }


        public static PDFStyle.Font fontColor(BaseColor baseColor) {
            return new PDFStyle.Font(font -> font.setColor(baseColor));
        }

        @Override
        public void buildStyle(Object obj) {
            this.wrapper.accept((com.itextpdf.text.Font) obj);
        }


        public String getFamilyStyle() {
            return familyStyle;
        }
    }


    class Paragraph implements PDFStyle {


        private final Consumer<com.itextpdf.text.Paragraph> wrapper;

        /**
         * 左对齐
         */
        public static final PDFStyle.Paragraph ALIGN_LEFT = new PDFStyle.Paragraph(paragraph -> paragraph.setAlignment(Element.ALIGN_LEFT));

        /**
         * 右对齐
         */
        public static final PDFStyle.Paragraph ALIGN_RIGHT = new PDFStyle.Paragraph(paragraph -> paragraph.setAlignment(Element.ALIGN_RIGHT));

        /**
         * 居中对齐
         */
        public static final PDFStyle.Paragraph ALIGN_CENTER = new PDFStyle.Paragraph(paragraph -> paragraph.setAlignment(Element.ALIGN_CENTER));

        public Paragraph(Consumer<com.itextpdf.text.Paragraph> wrapper) {
            this.wrapper = wrapper;
        }

        @Override
        public void buildStyle(Object obj) {
            this.wrapper.accept((com.itextpdf.text.Paragraph) obj);
        }


        /**
         * 段落首行缩进（文本个数）
         *
         * @param firstLineIndent
         * @return
         */
        public static Paragraph firstLineIndent(float firstLineIndent) {
            return new Paragraph(paragraph -> {
                float fontSize = paragraph.getFont().getSize();
                paragraph.setFirstLineIndent(firstLineIndent * fontSize);
            });
        }

        /**
         * 行间距
         *
         * @param spacing 行间距
         * @return
         */
        public static Paragraph spacingBetween(int spacing) {
            return new Paragraph(paragraph -> paragraph.setLeading(spacing));
        }


        /**
         * 行间距
         *
         * @param fontSizeMultiple 字体大小倍数
         * @return
         */
        public static Paragraph spacingBetween(float fontSizeMultiple) {
            return new Paragraph(paragraph -> paragraph.setLeading(0, fontSizeMultiple));
        }


        /**
         * 段落前与段后间距
         *
         * @param before 段前间距
         * @param after  段后间距
         * @return
         */
        public static Paragraph spacing(float before, float after) {
            return new Paragraph(paragraph -> {
                paragraph.setSpacingBefore(before);
                paragraph.setSpacingAfter(after);
            });
        }


        /**
         * 段落间距
         *
         * @param spacing 段前间距
         * @return
         */
        public static Paragraph spacingBefore(float spacing) {
            return new Paragraph(paragraph -> paragraph.setSpacingBefore(spacing));
        }


        /**
         * 段落间距
         *
         * @param spacing 段后间距
         * @return
         */
        public static Paragraph spacingAfter(float spacing) {
            return new Paragraph(paragraph -> paragraph.setSpacingAfter(spacing));
        }


        /**
         * 段落前间距倍数
         *
         * @param spacing
         * @return
         */
        public static Paragraph paragraphBeforeSpacing(float spacing) {
            return new Paragraph(paragraph -> {
                float fontSize = paragraph.getFont().getSize();
                paragraph.setSpacingBefore(fontSize * (spacing - 1));
            });
        }


        /**
         * 段落后间距倍数
         *
         * @param spacing
         * @return
         */
        public static Paragraph paragraphAfterSpacing(float spacing) {
            return new Paragraph(paragraph -> {
                float fontSize = paragraph.getFont().getSize();
                paragraph.setSpacingAfter(fontSize * (spacing - 1));
            });
        }
    }

}
