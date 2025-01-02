package com.geqian.document4j.html;

/**
 * @author geqian
 * @date 23:13 2025/1/2
 */
public class TextWatermarkConfig {
    private String fontFamily = "宋体";
    private Float opacity = 0.3f;
    private String fontColor = "grey";
    private String fontSize = "22px";
    private int rotateAngle = 20;

    public TextWatermarkConfig() {
    }

    public TextWatermarkConfig(String fontFamily, Float opacity, String fontColor, String fontSize, int rotateAngle) {
        this.fontFamily = fontFamily;
        this.opacity = opacity;
        this.fontColor = fontColor;
        this.fontSize = fontSize;
        this.rotateAngle = rotateAngle;
    }

    public String getFontFamily() {
        return fontFamily;
    }

    public void setFontFamily(String fontFamily) {
        this.fontFamily = fontFamily;
    }

    public Float getOpacity() {
        return opacity;
    }

    public void setOpacity(Float opacity) {
        this.opacity = opacity;
    }

    public String getFontColor() {
        return fontColor;
    }

    public void setFontColor(String fontColor) {
        this.fontColor = fontColor;
    }

    public String getFontSize() {
        return fontSize;
    }

    public void setFontSize(String fontSize) {
        this.fontSize = fontSize;
    }

    public int getRotateAngle() {
        return rotateAngle;
    }

    public void setRotateAngle(int rotateAngle) {
        this.rotateAngle = rotateAngle;
    }
}
