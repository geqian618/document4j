package com.geqian.document4j.image;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;

public class ImageWatermarkUtil {


    private static final Logger log = LoggerFactory.getLogger(ImageWatermarkUtil.class);

    /**
     * 添加文字水印
     *
     * @param text
     * @param config
     * @param source
     * @param target
     */
    public static void addTextWatermark(String text, InputStream source, TextWatermarkConfig config, OutputStream target) {
        config = config == null ? new TextWatermarkConfig() : config;
        Float opacity = config.getOpacity();
        Color color = config.getColor();
        Font font = config.getFont();
        Integer offsetX = config.getOffsetX();
        Integer offsetY = config.getOffsetY();
        String formatName = config.getFormatName();
        try {
            int imageType = "png".equalsIgnoreCase(formatName) ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB;
            BufferedImage sourceImage = ImageIO.read(source);
            // 不支持的图片格式 ImageIO.read 返回 null 而非抛异常，需显式判空
            checkImage(sourceImage);
            BufferedImage rgbImage = new BufferedImage(sourceImage.getWidth(), sourceImage.getHeight(), imageType);
            // 创建 Graphics2D 对象，并绘制原始图像
            Graphics2D g2d = rgbImage.createGraphics();
            g2d.drawImage(sourceImage, 0, 0, null);
            // 设置透明度 (在底层绘制水印时保持透明度)
            AlphaComposite alphaChannel = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity);
            g2d.setComposite(alphaChannel);
            // 设置文字颜色和字体
            g2d.setColor(color);
            g2d.setFont(font);
            // 如果没有设置偏移量，则居中显示水印
            if (offsetX == null || offsetY == null) {
                FontMetrics fontMetrics = g2d.getFontMetrics();
                int textWidth = fontMetrics.stringWidth(text);
                int textHeight = fontMetrics.getHeight();
                offsetX = (sourceImage.getWidth() - textWidth) / 2;
                offsetY = (sourceImage.getHeight() - textHeight) / 2;
            }
            // 绘制水印文本
            g2d.drawString(text, offsetX, offsetY);
            // 释放资源
            g2d.dispose();
            // 将修改后的图像保存为指定格式
            writeImage(rgbImage, formatName, target);
            log.info("Text watermark added successfully.");
        } catch (Exception e) {
            throw new RuntimeException("添加文字水印发生异常", e);
        }
    }


    /**
     * 添加文字水印
     *
     * @param text
     * @param config
     * @param source
     */
    public static byte[] addTextWatermark(String text, InputStream source, TextWatermarkConfig config) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            addTextWatermark(text, source, config, out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("添加文字水印发生异常", e);
        }
    }


    /**
     * 添加图片水印
     *
     * @param watermark
     * @param config
     * @param source
     * @param target
     */
    public static void addImageWatermark(InputStream watermark, InputStream source, ImageWatermarkConfig config, OutputStream target) {
        config = config == null ? new ImageWatermarkConfig() : config;
        Float opacity = config.getOpacity();
        Integer offsetX = config.getOffsetX();
        Integer offsetY = config.getOffsetY();
        Integer scale = config.getScale();
        String formatName = config.getFormatName();
        try {
            int imageType = "png".equalsIgnoreCase(formatName) ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB;
            // 读取水印图片
            BufferedImage watermarkImage = ImageIO.read(watermark);
            checkImage(watermarkImage);
            // 缩放水印图片
            int width = watermarkImage.getWidth() * scale;
            int height = watermarkImage.getHeight() * scale;
            // 使用高质量的图像缩放
            Image scaledWatermarkImg = watermarkImage.getScaledInstance(width, height, Image.SCALE_SMOOTH);
            BufferedImage scaledWatermarkBufferedImage = new BufferedImage(width, height, imageType);
            Graphics2D g2dScale = scaledWatermarkBufferedImage.createGraphics();
            g2dScale.drawImage(scaledWatermarkImg, 0, 0, null);
            g2dScale.dispose();
            // 读取原始图片
            BufferedImage sourceImage = ImageIO.read(source);
            checkImage(sourceImage);
            // 获取原始图片的宽高
            int sourceWidth = sourceImage.getWidth();
            int sourceHeight = sourceImage.getHeight();
            // 创建一个新的图像（带透明度）
            BufferedImage rgbImage = new BufferedImage(sourceWidth, sourceHeight, imageType);
            // 创建 Graphics2D 对象，并绘制原始图像
            Graphics2D g2d = rgbImage.createGraphics();
            g2d.drawImage(sourceImage, 0, 0, null);
            // 设置透明度
            AlphaComposite alphaChannel = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity);
            g2d.setComposite(alphaChannel);
            // 如果没有设置偏移量，则默认将水印居中
            if (offsetX == null || offsetY == null) {
                offsetX = (sourceWidth - width) / 2;
                offsetY = (sourceHeight - height) / 2;
            }
            // 绘制水印图片
            g2d.drawImage(scaledWatermarkImg, offsetX, offsetY, null);
            // 释放资源
            g2d.dispose();
            // 将图像保存为指定格式（如JPEG、PNG等）
            writeImage(rgbImage, formatName, target);
            log.info("Image watermark added successfully.");
        } catch (Exception e) {
            throw new RuntimeException("添加图片水印发生异常", e);
        }
    }


    /**
     * 添加图片水印
     *
     * @param watermark
     * @param config
     * @param source
     */
    public static byte[] addImageWatermark(InputStream watermark, InputStream source, ImageWatermarkConfig config) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            addImageWatermark(watermark, source, config, out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("添加图片水印发生异常", e);
        }
    }


    /**
     * 添加文字平铺水印
     *
     * @param text
     * @param config
     * @param source
     * @param target
     */
    public static void addFullTextWatermark(String text, InputStream source, TextWatermarkConfig config, OutputStream target) {
        config = config == null ? new TextWatermarkConfig() : config;
        Float opacity = config.getOpacity();
        Color color = config.getColor();
        Font font = config.getFont();
        Integer spacing = config.getSpacing();
        String formatName = config.getFormatName();
        try {
            int imageType = "png".equalsIgnoreCase(formatName) ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB;
            BufferedImage sourceImage = ImageIO.read(source);
            checkImage(sourceImage);
            BufferedImage rgbImage = new BufferedImage(sourceImage.getWidth(), sourceImage.getHeight(), imageType);
            // 创建 Graphics2D 对象，并绘制原始图像
            Graphics2D g2d = rgbImage.createGraphics();
            g2d.drawImage(sourceImage, 0, 0, null);
            // 设置透明度 (在底层绘制水印时保持透明度)
            AlphaComposite alphaChannel = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity);
            g2d.setComposite(alphaChannel);
            // 设置文字颜色和字体
            g2d.setColor(color);
            g2d.setFont(font);

            FontMetrics fontMetrics = g2d.getFontMetrics();
            int textWidth = fontMetrics.stringWidth(text);
            int textHeight = fontMetrics.getHeight();

            // 循环绘制水印文本，覆盖整个图像
            for (int x = 0; x < sourceImage.getWidth(); x += textWidth + spacing) {
                for (int y = 0; y < sourceImage.getHeight(); y += textHeight + spacing) {
                    g2d.drawString(text, x, y);
                }
            }

            // 释放资源
            g2d.dispose();
            // 将修改后的图像保存为指定格式
            writeImage(rgbImage, formatName, target);
            log.info("Text watermark added successfully.");
        } catch (Exception e) {
            throw new RuntimeException("添加文字水印发生异常", e);
        }
    }


    /**
     * 添加文字水印
     *
     * @param text
     * @param config
     * @param source
     */
    public static byte[] addFullTextWatermark(String text, InputStream source, TextWatermarkConfig config) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            addFullTextWatermark(text, source, config, out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("添加文字水印发生异常", e);
        }
    }


    /**
     * 添加图片平铺水印
     *
     * @param watermark
     * @param config
     * @param source
     * @param target
     */
    public static void addFullImageWatermark(InputStream watermark, InputStream source, ImageWatermarkConfig config, OutputStream target) {
        config = config == null ? new ImageWatermarkConfig() : config;
        Float opacity = config.getOpacity();
        Integer scale = config.getScale();
        String formatName = config.getFormatName();
        Integer spacing = config.getSpacing();
        try {
            int imageType = "png".equalsIgnoreCase(formatName) ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB;
            // 读取水印图片
            BufferedImage watermarkImage = ImageIO.read(watermark);
            checkImage(watermarkImage);
            // 缩放水印图片
            int width = watermarkImage.getWidth() * scale;
            int height = watermarkImage.getHeight() * scale;
            // 使用高质量的图像缩放
            Image scaledWatermarkImg = watermarkImage.getScaledInstance(width, height, Image.SCALE_SMOOTH);
            BufferedImage scaledWatermarkBufferedImage = new BufferedImage(width, height, imageType);
            Graphics2D g2dScale = scaledWatermarkBufferedImage.createGraphics();
            g2dScale.drawImage(scaledWatermarkImg, 0, 0, null);
            g2dScale.dispose();
            // 读取原始图片
            BufferedImage sourceImage = ImageIO.read(source);
            checkImage(sourceImage);
            // 获取原始图片的宽高
            int sourceWidth = sourceImage.getWidth();
            int sourceHeight = sourceImage.getHeight();
            // 创建一个新的图像（带透明度）
            BufferedImage rgbImage = new BufferedImage(sourceWidth, sourceHeight, imageType);
            // 创建 Graphics2D 对象，并绘制原始图像
            Graphics2D g2d = rgbImage.createGraphics();
            g2d.drawImage(sourceImage, 0, 0, null);
            // 设置透明度
            AlphaComposite alphaChannel = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity);
            g2d.setComposite(alphaChannel);

            // 循环绘制水印图片，覆盖整个图像
            for (int x = 0; x < sourceImage.getWidth(); x += width + spacing) {
                for (int y = 0; y < sourceImage.getHeight(); y += height + spacing) {
                    g2d.drawImage(scaledWatermarkImg, x, y, null);
                }
            }
            // 释放资源
            g2d.dispose();
            // 将图像保存为指定格式（如JPEG、PNG等）
            writeImage(rgbImage, formatName, target);
            log.info("Image watermark added successfully.");
        } catch (Exception e) {
            throw new RuntimeException("添加图片水印发生异常", e);
        }
    }


    /**
     * 添加图片水印
     *
     * @param watermark
     * @param config
     * @param source
     */
    public static byte[] addFullImageWatermark(InputStream watermark, InputStream source, ImageWatermarkConfig config) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            addFullImageWatermark(watermark, source, config, out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("添加图片水印发生异常", e);
        }
    }

    /**
     * 不支持的图片格式 ImageIO.read 会返回 null 而非抛异常，需显式判空给出明确错误
     */
    private static void checkImage(BufferedImage image) {
        if (image == null) {
            throw new IllegalArgumentException("无法读取图片，可能是不支持的图片格式");
        }
    }

    /**
     * ImageIO.write 对不支持的格式返回 false 且不写出任何字节，需检查返回值
     */
    private static void writeImage(BufferedImage image, String formatName, OutputStream target) throws IOException {
        if (!ImageIO.write(image, formatName, target)) {
            throw new IllegalArgumentException("不支持的输出图片格式: " + formatName);
        }
    }
}
