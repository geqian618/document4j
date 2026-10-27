package com.geqian.document4j.image;

/**
 * @author geqian
 * @date 21:09 2024/11/23
 */
public class ImageWatermarkConfig {

    private Integer offsetX;

    private Integer offsetY;

    private Float opacity = 0.1f;

    private Integer scale = 1;

    private String formatName = "jpg";

    private Integer spacing = 20;


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

    public Integer getScale() {
        return scale;
    }

    public void setScale(Integer scale) {
        // null 会在缩放时自动拆箱 NPE，回退默认值
        if (scale != null) {
            this.scale = scale;
        }
    }

    public Float getOpacity() {
        return opacity;
    }

    public void setOpacity(Float opacity) {
        if (opacity != null) {
            this.opacity = opacity;
        }
    }

    public String getFormatName() {
        return formatName;
    }

    public void setFormatName(String formatName) {
        if (formatName != null && !formatName.trim().isEmpty()) {
            this.formatName = formatName;
        }
    }

    public Integer getSpacing() {
        return spacing;
    }

    public void setSpacing(Integer spacing) {
        if (spacing != null) {
            this.spacing = spacing;
        }
    }
}
