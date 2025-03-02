package com.geqian.document4j.word;

import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.UnderlinePatterns;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.*;

import java.math.BigInteger;
import java.util.function.Consumer;

/**
 * @author geqian
 * @date 21:08 2024/2/1
 */
public interface WordStyle {

    void buildStyle(Object obj);

    class Title implements WordStyle {

        public static final Title LEVEL_1 = new Title(paragraph -> {
            setTitleLevel(paragraph, 0);

        });
        public static final Title LEVEL_2 = new Title(paragraph -> {
            setTitleLevel(paragraph, 1);
        });
        public static final Title LEVEL_3 = new Title(paragraph -> {
            setTitleLevel(paragraph, 2);
        });
        public static final Title LEVEL_4 = new Title(paragraph -> {
            setTitleLevel(paragraph, 3);
        });
        public static final Title LEVEL_5 = new Title(paragraph -> {
            setTitleLevel(paragraph, 4);
        });
        public static final Title LEVEL_6 = new Title(paragraph -> {
            setTitleLevel(paragraph, 5);
        });
        public static final Title LEVEL_7 = new Title(paragraph -> {
            setTitleLevel(paragraph, 6);
        });
        public static final Title LEVEL_8 = new Title(paragraph -> {
            setTitleLevel(paragraph, 7);
        });
        public static final Title level9 = new Title(paragraph -> {
            setTitleLevel(paragraph, 8);
        });

        private final Consumer<XWPFParagraph> wrapper;

        Title(Consumer<XWPFParagraph> wrapper) {
            this.wrapper = wrapper;
        }

        @Override
        public void buildStyle(Object paragraph) {
            if (paragraph instanceof XWPFParagraph) {
                this.wrapper.accept((XWPFParagraph) paragraph);
            }
        }

        /**
         * 设置标题级别
         *
         * @param paragraph
         * @param level
         */
        private static void setTitleLevel(XWPFParagraph paragraph, int level) {
            if (level < 0 || level > 8) {
                throw new IllegalArgumentException("标题级别需为 1~8 的整数");
            }
            // 设置段落样式为对应级别的标题（例如"Heading1"）
            // 注意：Word 内置样式名称需与实际模板匹配，否则可能不生效
            paragraph.setStyle("Heading" + level);

            // 操作底层 XML 对象以设置大纲级别（必需步骤）
            // 获取或创建段落属性对象（CTPPr）
            CTPPr ppr = paragraph.getCTP().getPPr();
            if (ppr == null) {
                ppr = paragraph.getCTP().addNewPPr();
            }

            XWPFRun run = paragraph.getRuns().get(0);
            if (run == null){
                run = paragraph.createRun();
            }
            // 字号递减
            run.setFontSize(22 - (level * 2));
            run.setBold(true);

            // 设置段后间距（单位：缇，1磅=20缇，此处200缇=10磅）
            // 获取或创建间距属性对象（CTSpacing）
            CTSpacing spacing = ppr.isSetSpacing() ? ppr.getSpacing() : ppr.addNewSpacing();
            // 设置段后间距
            spacing.setBefore(BigInteger.valueOf(200));
            spacing.setAfter(BigInteger.valueOf(200));

            // 设置大纲级别（对应导航窗格中的层级）
            // 获取或创建大纲级别对象（CTDecimalNumber）
            CTDecimalNumber ctDecimalNumber = ppr.isSetOutlineLvl() ? ppr.getOutlineLvl() : ppr.addNewOutlineLvl();
            // 0=正文，1=1级标题，依此类推
            ctDecimalNumber.setVal(BigInteger.valueOf(level));
        }
    }

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
        public void buildStyle(Object run) {
            if (run instanceof XWPFRun) {
                this.wrapper.accept((XWPFRun) run);
            }
        }

        public static Font fontColor(String fontColor) {
            return new Font(run -> run.setColor(fontColor.replace("#", "")));
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
        public void buildStyle(Object paragraph) {
            if (paragraph instanceof XWPFParagraph) {
                this.wrapper.accept((XWPFParagraph) paragraph);
            }
        }
    }

}
