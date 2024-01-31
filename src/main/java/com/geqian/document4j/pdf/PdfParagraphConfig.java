package com.geqian.document4j.pdf;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;

/**
 * @author geqian
 * @date 16:14 2024/1/16
 */

public class ParagraphConfig {

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

    private ParagraphConfig() {
    }

    public static ParagraphConfig create() {
        return new ParagraphConfig();
    }

    public float getFirstLineIndent() {
        return firstLineIndent;
    }

    public ParagraphConfig setFirstLineIndent(float firstLineIndent) {
        this.firstLineIndent = firstLineIndent;
        return this;
    }

    public int getAlignment() {
        return alignment;
    }

    public ParagraphConfig setAlignment(int alignment) {
        this.alignment = alignment;
        return this;
    }


    public float getLineHeight() {
        return lineHeight;
    }

    public ParagraphConfig setLineHeight(int lineHeight) {
        this.lineHeight = lineHeight;
        return this;
    }

    public ParagraphConfig setLineHeight(float lineHeight) {
        this.lineHeight = lineHeight;
        return this;
    }

    public int getFontSize() {
        return fontSize;
    }

    public ParagraphConfig setFontSize(int fontSize) {
        this.fontSize = fontSize;
        return this;
    }

    public BaseColor getFontColor() {
        return fontColor;
    }

    public ParagraphConfig setFontColor(BaseColor fontColor) {
        this.fontColor = fontColor;
        return this;
    }

    public int getFontStyle() {
        return fontStyle;
    }

    public ParagraphConfig setFontStyle(int... fontStyles) {
        for (int fontStyle : fontStyles) {
            this.fontStyle |= fontStyle;
        }
        return this;
    }

    public String getFontFamily() {
        return fontFamily;
    }

    public ParagraphConfig setFontFamily(String fontFamily) {
        this.fontFamily = fontFamily;
        return this;
    }
}
