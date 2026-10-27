package com.geqian.document4j.pdf;

import com.itextpdf.text.*;
import com.itextpdf.text.Font;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfGState;
import com.itextpdf.text.pdf.PdfPageEvent;
import com.itextpdf.text.pdf.PdfWriter;

import javax.swing.*;
import java.awt.*;

/**
 * @author geqian
 * @date 23:03 2024/3/8
 */
public class WaterMarkEvent implements PdfPageEvent {

    private final String watermark;

    private final Font font;

    public WaterMarkEvent(Font font, String watermark) {
        if (watermark == null || watermark.trim().isEmpty()) {
            // 空水印会使文本宽度为 0，平铺循环步长为 0 将死循环
            throw new IllegalArgumentException("水印内容不能为空");
        }
        this.watermark = watermark;
        this.font = font;
    }

    @Override
    public void onOpenDocument(PdfWriter writer, Document document) {

    }

    @Override
    public void onStartPage(PdfWriter pdfWriter, Document document) {
        // 新的一页被创建后添加水印
        PdfGState gs = new PdfGState();
        gs.setFillOpacity(0.3f);
        gs.setStrokeOpacity(0.4f);
        JLabel label = new JLabel();
        label.setText(watermark);
        FontMetrics metrics = label.getFontMetrics(label.getFont());
        int textH = metrics.getHeight();
        int textW = metrics.stringWidth(label.getText());

        Rectangle pageRect = pdfWriter.getPageSize();
        PdfContentByte under = pdfWriter.getDirectContentUnder();
        under.saveState();
        under.setGState(gs);
        under.beginText();
        under.setFontAndSize(font.getBaseFont(), 20);
        under.setRGBColorFill(145, 145, 145);

        // 页面填充水印
        for (int height = textH; height < pageRect.getHeight(); height = height + textH * 10) {
            for (int width = textW; width < pageRect.getWidth() + textW; width = width + textW * 5) {
                // rotation是水印倾斜角度
                under.showTextAligned(Element.ALIGN_LEFT, watermark, width - textW, height - textH, 30);
            }
        }
        under.endText();
        under.restoreState();
    }

    @Override
    public void onEndPage(PdfWriter writer, Document document) {

    }

    @Override
    public void onCloseDocument(PdfWriter pdfWriter, Document document) {

    }

    @Override
    public void onParagraph(PdfWriter writer, Document document, float paragraphPosition) {

    }

    @Override
    public void onParagraphEnd(PdfWriter writer, Document document, float paragraphPosition) {

    }

    @Override
    public void onChapter(PdfWriter writer, Document document, float paragraphPosition, Paragraph title) {

    }

    @Override
    public void onChapterEnd(PdfWriter writer, Document document, float paragraphPosition) {

    }

    @Override
    public void onSection(PdfWriter writer, Document document, float paragraphPosition, int depth, Paragraph title) {

    }

    @Override
    public void onSectionEnd(PdfWriter writer, Document document, float paragraphPosition) {

    }

    @Override
    public void onGenericTag(PdfWriter writer, Document document, Rectangle rect, String text) {

    }
}
