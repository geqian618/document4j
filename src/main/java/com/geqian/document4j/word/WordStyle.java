package com.geqian.document4j.word;

import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.UnderlinePatterns;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTInd;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTPPr;
import java.math.BigInteger;
import java.util.function.Consumer;

/**
 * @author geqian
 * @date 21:08 2024/2/1
 */
public interface WordStyle {

    void wrapperStyle(Object obj);

    class Font implements WordStyle {
        /**
         * 加粗
         */
        public static final Font BOLD = new Font(run -> run.setBold(true));

        /**
         * 斜体
         */
        public static final Font ITALIC = new Font(run -> run.setItalic(true));

        /**
         * 下划线
         */
        public static final Font UNDER_LINE = new Font(run -> run.setUnderline(UnderlinePatterns.SINGLE));

        /**
         * 双下划线
         */
        public static final Font DOUBLE_UNDER_LINE = new Font(run -> run.setUnderline(UnderlinePatterns.DOUBLE));

        /**
         * 删除线
         */
        public static final Font DELETE_LINE = new Font(run -> run.setStrikeThrough(true));

        /**
         * 双删除线
         */
        public static final Font DOUBLE_DELETE_LINE = new Font(run -> run.setDoubleStrikethrough(true));

        /**
         * 文字阴影
         */
        public static final Font SHADOW = new Font(run -> run.setShadow(true));

        private final Consumer<XWPFRun> wrapper;

        Font(Consumer<XWPFRun> wrapper) {
            this.wrapper = wrapper;
        }

        @Override
        public void wrapperStyle(Object run) {
            this.wrapper.accept((XWPFRun) run);
        }


        public static Font fontcolor(String fontColor) {
            return new Font(run -> run.setColor(fontColor));
        }

        public static Font fontSize(int fontSize) {
            return new Font(run -> run.setFontSize(fontSize));
        }

        public static Font fontFamily(String fontFamily) {
            return new Font(run -> run.setFontFamily(fontFamily));
        }

    }


    class Paragraph implements WordStyle {

        /**
         * 左对齐
         */
        public static final Paragraph ALIGN_LEFT = new Paragraph(paragraph -> paragraph.setAlignment(ParagraphAlignment.LEFT));

        /**
         * 右对齐
         */
        public static final Paragraph ALIGN_RIGHT = new Paragraph(paragraph -> paragraph.setAlignment(ParagraphAlignment.RIGHT));

        /**
         * 居中对齐
         */
        public static final Paragraph ALIGN_CENTER = new Paragraph(paragraph -> paragraph.setAlignment(ParagraphAlignment.CENTER));


        private final Consumer<XWPFParagraph> wrapper;


        public Paragraph(Consumer<XWPFParagraph> wrapper) {
            this.wrapper = wrapper;
        }


        /**
         * 行间距倍数（fontSize为基数）
         *
         * @param lineSpacing
         * @return
         */
        public static Paragraph lineSpacing(int lineSpacing) {
            return new Paragraph(paragraph -> paragraph.setSpacingBetween(lineSpacing));
        }

        /**
         * 段落首行缩进
         *
         * @param indent
         * @return
         */
        public static Paragraph firstLineIndent(int indent) {
            return new Paragraph(paragraph -> {
                // 设置段落的首行缩进
                CTPPr ppr = paragraph.getCTP().getPPr();
                if (ppr == null) {
                    ppr = paragraph.getCTP().addNewPPr();
                }
                CTInd ind = ppr.getInd();
                if (ind == null) {
                    ind = ppr.addNewInd();
                }
                ind.setFirstLineChars(BigInteger.valueOf(100L * indent));
            });
        }

        @Override
        public void wrapperStyle(Object paragraph) {
            this.wrapper.accept((XWPFParagraph) paragraph);
        }
    }

}
