package com.geqian.document4j.pdf;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;

/**
 * @author geqian
 * @date 16:14 2024/1/16
 */

public class PdfParagraphConfig {

    /**
     * 首航缩进
     */
    private float firstLineIndent = 0;
    /**
     * 对齐方式
     */
    private int alignment = Element.ALIGN_LEFT;

    /**
     * 字体大小
     */
    private int fontSize = 16;

    /**
     * 行高
     */
    private float lineHeight = (float) (fontSize * 1.5);


    /**
     * 字体颜色
     */
    private BaseColor fontColor = BaseColor.BLACK;

    /**
     * 字体样式
     */
    private int fontStyle = Font.NORMAL;

    /**
     * 字体文件路径
     */
    private String fontFamily = "fonts/simsun.ttc,0";

    private PdfParagraphConfig() {
    }

    public static PdfParagraphConfig create() {
        return new PdfParagraphConfig();
    }

    public float getFirstLineIndent() {
        return firstLineIndent;
    }

    public PdfParagraphConfig setFirstLineIndent(float firstLineIndent) {
        this.firstLineIndent = firstLineIndent;
        return this;
    }

    public int getAlignment() {
        return alignment;
    }

    public PdfParagraphConfig setAlignment(int alignment) {
        this.alignment = alignment;
        return this;
    }


    public float getLineHeight() {
        return lineHeight;
    }

    public PdfParagraphConfig setLineHeight(int lineHeight) {
        this.lineHeight = lineHeight;
        return this;
    }

    public PdfParagraphConfig setLineHeight(float lineHeight) {
        this.lineHeight = lineHeight;
        return this;
    }

    public int getFontSize() {
        return fontSize;
    }

    public PdfParagraphConfig setFontSize(int fontSize) {
        this.fontSize = fontSize;
        return this;
    }

    public BaseColor getFontColor() {
        return fontColor;
    }

    public PdfParagraphConfig setFontColor(BaseColor fontColor) {
        this.fontColor = fontColor;
        return this;
    }

    public int getFontStyle() {
        return fontStyle;
    }

    public PdfParagraphConfig setFontStyle(int... fontStyles) {
        for (int fontStyle : fontStyles) {
            this.fontStyle |= fontStyle;
        }
        return this;
    }

    public String getFontFamily() {
        return fontFamily;
    }

    public PdfParagraphConfig setFontFamily(String fontFamily) {
        this.fontFamily = fontFamily;
        return this;
    }
}
