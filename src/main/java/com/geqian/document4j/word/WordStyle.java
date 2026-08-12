package com.geqian.document4j.word;

import org.apache.poi.xwpf.usermodel.*;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTDecimalNumber;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTInd;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTOnOff;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTPPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSpacing;

import java.math.BigInteger;
import java.util.function.Consumer;

/**
 * @author geqian
 * @date 21:08 2024/2/1
 */
public interface WordStyle {

    void buildStyle(Object obj);

    class Heading implements WordStyle {
        public static final Heading LEVEL_1 = new Heading(paragraph -> {
            setHeading(paragraph, 1);
        });
        public static final Heading LEVEL_2 = new Heading(paragraph -> {
            setHeading(paragraph, 2);
        });
        public static final Heading LEVEL_3 = new Heading(paragraph -> {
            setHeading(paragraph, 3);
        });
        public static final Heading LEVEL_4 = new Heading(paragraph -> {
            setHeading(paragraph, 4);
        });
        public static final Heading LEVEL_5 = new Heading(paragraph -> {
            setHeading(paragraph, 5);
        });
        public static final Heading LEVEL_6 = new Heading(paragraph -> {
            setHeading(paragraph, 6);
        });
        public static final Heading LEVEL_7 = new Heading(paragraph -> {
            setHeading(paragraph, 7);
        });
        public static final Heading LEVEL_8 = new Heading(paragraph -> {
            setHeading(paragraph, 8);
        });

        private final Consumer<XWPFParagraph> wrapper;

        Heading(Consumer<XWPFParagraph> wrapper) {
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
         * @param paragraph 段落
         * @param level     标题级别
         */
        private static void setHeading(XWPFParagraph paragraph, int level) {
            if (level < 1 || level > 8) {
                throw new IllegalArgumentException("标题级别需为 1~8 的整数");
            }
            // 设置段落样式为对应级别的标题（例如"Heading1"）
            paragraph.setStyle("Heading" + level);

            // 操作底层 XML 对象以设置大纲级别（必需步骤）
            // 获取或创建段落属性对象（CTPPr）
            CTPPr ppr = paragraph.getCTP().getPPr();
            if (ppr == null) {
                ppr = paragraph.getCTP().addNewPPr();
            }

            XWPFRun run = paragraph.getRuns().get(0);
            if (run == null) {
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

            // 关闭 "如果定义了文档网格，则对齐到网格"（等价于 Word 中取消勾选该项），
            // 否则 Word/WPS 会把标题的段前段后间距、行距按文档网格取整渲染，
            // 导致 XML 中设置的间距与行距与实际显示不一致
            // 复用已存在的元素，避免与 applyDefaultSpacing 重复写入多个 w:snapToGrid
            CTOnOff snapToGrid = ppr.isSetSnapToGrid() ? ppr.getSnapToGrid() : ppr.addNewSnapToGrid();
            snapToGrid.setVal(false);
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

        /**
         * 取消加粗
         */
        public static final Font NOT_BOLD = new Font(run -> run.setBold(false));

        /**
         * 取消斜体
         */
        public static final Font NOT_ITALIC = new Font(run -> run.setItalic(false));

        /**
         * 取消下划线（同时取消单、双下划线）
         */
        public static final Font NOT_UNDER_LINE = new Font(run -> run.setUnderline(UnderlinePatterns.NONE));

        /**
         * 取消双下划线（同取消下划线）
         */
        public static final Font NOT_DOUBLE_UNDER_LINE = new Font(run -> run.setUnderline(UnderlinePatterns.NONE));

        /**
         * 取消删除线
         */
        public static final Font NOT_DELETE_LINE = new Font(run -> run.setStrikeThrough(false));

        /**
         * 取消双删除线
         */
        public static final Font NOT_DOUBLE_DELETE_LINE = new Font(run -> run.setDoubleStrikethrough(false));

        /**
         * 取消文字阴影
         */
        public static final Font NOT_SHADOW = new Font(run -> run.setShadow(false));

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
         * 行间距（单位基数为 fontSize）
         *
         * @param fontSizeMultiple 字体大小的倍数
         * @return
         */
        public static Paragraph spacingBetween(double fontSizeMultiple) {
            return new Paragraph(paragraph -> paragraph.setSpacingBetween(fontSizeMultiple, LineSpacingRule.AUTO));
        }


        /**
         * 设置段前间距：参数单位是磅的 1/20
         *
         * @param spacing
         * @return
         */
        public static Paragraph spacingBefore(int spacing) {
            return new Paragraph(paragraph -> {
                paragraph.setSpacingBefore(spacing * 20);
            });
        }


        /**
         * 设置段后间距：参数单位是磅的 1/20
         *
         * @param spacing
         * @return
         */
        public static Paragraph spacingAfter(int spacing) {
            return new Paragraph(paragraph -> paragraph.setSpacingAfter(spacing * 20));
        }


        /**
         * 设置段后与段后间距：参数单位是磅的 1/20
         *
         * @param before 段前间距
         * @param after  断后间距
         * @return
         */
        public static Paragraph spacing(int before, int after) {
            return new Paragraph(paragraph -> {
                paragraph.setSpacingBefore(before * 20);
                paragraph.setSpacingAfter(after * 20);
            });
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
