package com.geqian.document4j.word;

import org.apache.poi.xwpf.usermodel.ParagraphAlignment;

public class ParagraphConfig {
    //首行缩进字符个数
    private Integer firstLineIndent = 0;
    //对齐方式
    private ParagraphAlignment alignment = ParagraphAlignment.LEFT;
    // 行距倍数
    private double lineSpacing = 1;

    //文字大小
    private Integer fontSize = 16;
    //文字颜色 16进制格式
    private String fontColor = "000000";
    //文字类型
    private String fontFamily = "Arial";
    //是否斜体
    private boolean fontItalic = false;
    //是否加粗
    private boolean fontBold = false;


    private ParagraphConfig() {
    }

    public static ParagraphConfig create() {
        return new ParagraphConfig();
    }

    public Integer getFirstLineIndent() {
        return firstLineIndent;
    }

    public ParagraphConfig setFirstLineIndent(Integer firstLineIndent) {
        this.firstLineIndent = firstLineIndent;
        return this;
    }

    public ParagraphAlignment getAlignment() {
        return alignment;
    }

    public ParagraphConfig setAlignment(ParagraphAlignment alignment) {
        this.alignment = alignment;
        return this;
    }

    public double getLineSpacing() {
        return lineSpacing;
    }

    public ParagraphConfig setLineSpacing(double lineSpacing) {
        this.lineSpacing = lineSpacing;
        return this;
    }

    public Integer getFontSize() {
        return fontSize;
    }

    public ParagraphConfig setFontSize(Integer fontSize) {
        this.fontSize = fontSize;
        return this;
    }

    public String getFontColor() {
        return fontColor;
    }

    public ParagraphConfig setFontColor(String fontColor) {
        this.fontColor = fontColor;
        return this;
    }

    public String getFontFamily() {
        return fontFamily;
    }

    public ParagraphConfig setFontFamily(String fontFamily) {
        this.fontFamily = fontFamily;
        return this;
    }

    public boolean isFontItalic() {
        return fontItalic;
    }

    public ParagraphConfig setFontItalic(boolean fontItalic) {
        this.fontItalic = fontItalic;
        return this;
    }

    public boolean isFontBold() {
        return fontBold;
    }

    public ParagraphConfig setFontBold(boolean fontBold) {
        this.fontBold = fontBold;
        return this;
    }
}