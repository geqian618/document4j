package com.geqian.document4j.excel;

import org.apache.poi.ooxml.POIXMLDocumentPart;
import org.apache.poi.openxml4j.opc.PackagePartName;
import org.apache.poi.openxml4j.opc.PackageRelationship;
import org.apache.poi.openxml4j.opc.TargetMode;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFRelation;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;

/**
 * @author geqian
 * @date 20:39 2024/11/21
 */
public class ExcelWaterMarkUtil {


    public static byte[] addWaterMark(InputStream in, String waterMark) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            addWaterMark(in, out, waterMark);
            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("EXCEL文档添加水印时发生异常", e);
        }
    }


    /**
     * 添加 excel 水印
     *
     * @param in        原文件输入流
     * @param out       输出文件输出流
     * @param waterMark 水印文字
     */
    public static void addWaterMark(InputStream in, OutputStream out, String waterMark) {
        // 读取水印文字
        String[] textArray = waterMark.split("\n");
        // 设置字体
        Font font = new Font("microsoft-yahei", Font.PLAIN, 30);
        // 单元水印图片宽高
        int width = 500;
        int height = 300;

        // 缓冲区图片对象
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        // 基于特定宽高，创建图形对象
        Graphics2D g = image.createGraphics();
        // 基于图形对象生成半透明图片
        image = g.getDeviceConfiguration().createCompatibleImage(width, height, Transparency.TRANSLUCENT);
        // 释放画笔
        g.dispose();

        // 基于半透明图片，创建图形对象
        g = image.createGraphics();
        // 设置图形颜色
        g.setColor(new Color(Integer.parseInt("#C5CBCF".substring(1), 16)));
        // 设置图形字体
        g.setFont(font);
        // 设置图形倾斜度
        g.shear(0.1, -0.26);
        // 设置字体平滑
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        // 画出字符串
        int y = 150;
        for (String s : textArray) {
            g.drawString(s, 0, y);
            y = y + font.getSize();
        }
        // 释放画笔
        g.dispose();

        try (// 读取原文档
             XSSFWorkbook workbook = new XSSFWorkbook(in);
             ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            // 将内存中的图片写入至输出流
            ImageIO.write(image, "png", os);

            // 将图片输出流写入Excel
            int pictureIdx = workbook.addPicture(os.toByteArray(), Workbook.PICTURE_TYPE_PNG);
            // 获取工作簿中的图片对象
            POIXMLDocumentPart poixmlDocumentPart = workbook.getAllPictures().get(pictureIdx);

            //获取每个Sheet表
            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                XSSFSheet sheet = workbook.getSheetAt(i);
                PackagePartName ppn = poixmlDocumentPart.getPackagePart().getPartName();
                String relType = XSSFRelation.IMAGES.getRelation();
                // 为当前Sheet添加图片关系
                PackageRelationship pr = sheet.getPackagePart().addRelationship(ppn, TargetMode.INTERNAL, relType, null);
                // 将图片关系添加到Sheet底层结构中
                sheet.getCTWorksheet().addNewPicture().setId(pr.getId());
            }
            workbook.write(out);
        } catch (Exception e) {
            throw new RuntimeException("EXCEL文档添加水印时发生异常", e);
        }
    }

}
