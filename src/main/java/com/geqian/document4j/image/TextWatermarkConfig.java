package com.geqian.document4j.image;

import java.awt.*;

/**
 * @author geqian
 * @date 19:11 2024/11/23
 */
public class TextWatermarkConfig {

    private Float opacity = 0.1f;

    private Color color = Color.GRAY;

    private Font font = new Font("宋体", Font.BOLD, 64);

    private Integer margin = 20;

    private String formatName = "jpg";

    private Integer offsetX;

    private Integer offsetY;


    public Float getOpacity() {
        return opacity;
    }

    public void setOpacity(Float opacity) {
        this.opacity = opacity;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public Font getFont() {
        return font;
    }

    public void setFont(Font font) {
        this.font = font;
    }

    public String getFormatName() {
        return formatName;
    }

    public void setFormatName(String formatName) {
        this.formatName = formatName;
    }

    public Integer getOffsetX() {
        return offsetX;
    }

    public void setOffsetX(Integer offsetX) {
        this.offsetX = offsetX;
    }

    public Integer getOffsetY() {
        return offsetY;
    }

    public void setOffsetY(Integer offsetY) {
        this.offsetY = offsetY;
    }

    public Integer getMargin() {
        return margin;
    }

    public void setMargin(Integer margin) {
        this.margin = margin;
    }

}
