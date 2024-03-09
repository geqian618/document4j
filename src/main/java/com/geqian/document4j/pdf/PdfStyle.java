package com.geqian.document4j.pdf;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Element;

import java.util.function.Consumer;

/**
 * @author geqian
 * @date 23:33 2024/2/1
 */
public interface PdfStyle {

    void fillStyle(Object obj);


    class Font implements PdfStyle {

        private String familyStyle;

        /**
         * 加粗
         */
        public static final PdfStyle.Font BOLD = new PdfStyle.Font(font -> font.setStyle(font.getStyle() | com.itextpdf.text.Font.BOLD));

        /**
         * 斜体
         */
        public static final PdfStyle.Font ITALIC = new PdfStyle.Font(font -> font.setStyle(font.getStyle() | com.itextpdf.text.Font.ITALIC));
        /**
         * 斜体加粗
         */
        public static final PdfStyle.Font BOLD_ITALIC = new PdfStyle.Font(font -> font.setStyle(font.getStyle() | com.itextpdf.text.Font.BOLDITALIC));

        /**
         * 下划线
         */
        public static final PdfStyle.Font UNDER_LINE = new PdfStyle.Font(font -> font.setStyle(font.getStyle() | com.itextpdf.text.Font.UNDERLINE));


        /**
         * 删除线
         */
        public static final PdfStyle.Font DELETE_LINE = new PdfStyle.Font(font -> font.setStyle(font.getStyle() | com.itextpdf.text.Font.DEFAULTSIZE));

        /**
         * 正常
         */
        public static final PdfStyle.Font NORMAL = new PdfStyle.Font(font -> font.setStyle(font.getStyle() | com.itextpdf.text.Font.NORMAL));


        private final Consumer<com.itextpdf.text.Font> wrapper;


        public Font(Consumer<com.itextpdf.text.Font> wrapper) {
            this.wrapper = wrapper;
        }

        public Font(String familyStyle, Consumer<com.itextpdf.text.Font> wrapper) {
            this.familyStyle = familyStyle;
            this.wrapper = wrapper;
        }

        public static PdfStyle.Font fontSize(float fontSize) {
            return new PdfStyle.Font(font -> font.setSize(fontSize));
        }


        public static PdfStyle.Font fontFamily(String fontFamilyPath) {
            return new PdfStyle.Font(fontFamilyPath, font -> font.setFamily(fontFamilyPath));
        }


        public static PdfStyle.Font fontColor(BaseColor baseColor) {
            return new PdfStyle.Font(font -> font.setColor(baseColor));
        }

        @Override
        public void fillStyle(Object obj) {
            this.wrapper.accept((com.itextpdf.text.Font) obj);
        }


        public String getFamilyStyle() {
            return familyStyle;
        }
    }


    class Paragraph implements PdfStyle {


        private final Consumer<com.itextpdf.text.Paragraph> wrapper;

        /**
         * 左对齐
         */
        public static final PdfStyle.Paragraph ALIGN_LEFT = new PdfStyle.Paragraph(paragraph -> paragraph.setAlignment(Element.ALIGN_LEFT));

        /**
         * 右对齐
         */
        public static final PdfStyle.Paragraph ALIGN_RIGHT = new PdfStyle.Paragraph(paragraph -> paragraph.setAlignment(Element.ALIGN_RIGHT));

        /**
         * 居中对齐
         */
        public static final PdfStyle.Paragraph ALIGN_CENTER = new PdfStyle.Paragraph(paragraph -> paragraph.setAlignment(Element.ALIGN_CENTER));

        public Paragraph(Consumer<com.itextpdf.text.Paragraph> wrapper) {
            this.wrapper = wrapper;
        }

        @Override
        public void fillStyle(Object obj) {
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
         * 行间距倍数（fontSize为基数）
         *
         * @param lineSpacing
         * @return
         */
        public static Paragraph lineSpacing(float lineSpacing) {
            return new Paragraph(paragraph -> {
                float fontSize = paragraph.getFont().getSize();
                paragraph.setLeading(fontSize * lineSpacing);
            });
        }


        /**
         * 段落间距倍数
         *
         * @param paragraphSpacing
         * @return
         */
        public static Paragraph paragraphSpacing(float paragraphSpacing) {
            return new Paragraph(paragraph -> {
                float fontSize = paragraph.getFont().getSize();
                paragraph.setSpacingBefore(fontSize * (paragraphSpacing - 1));
                paragraph.setSpacingAfter(fontSize * (paragraphSpacing - 1));
            });
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
