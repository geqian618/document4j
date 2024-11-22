package com.geqian.document4j.pdf;

import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.PdfWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * @author geqian
 * @date 0:06 2024/3/9
 */
public class PDPExtendUtils {

    private static final Logger log = LoggerFactory.getLogger(PDPExtendUtils.class);


    /**
     * 设置密码
     *
     * @param is
     * @param os
     * @param userPassword  用户密码
     * @param ownerPassword 拥有者密码
     */
    public static void setPassword(InputStream is, OutputStream os, String userPassword, String ownerPassword) {
        PdfReader reader = null;
        PdfStamper stamper = null;
        try {
            // is为pdf文件输入流，os为要输出的文件流
            reader = new PdfReader(is);
            stamper = new PdfStamper(reader, os);
            // 设密码，禁止编辑
            stamper.setEncryption(userPassword.getBytes(StandardCharsets.UTF_8), ownerPassword.getBytes(StandardCharsets.UTF_8), PdfWriter.ALLOW_PRINTING | PdfWriter.ALLOW_COPY, PdfWriter.ENCRYPTION_AES_128);

        } catch (Exception e) {
            log.error("PDF文档设置密码时发生异常", e);
        } finally {
            closeResource(stamper, reader);
        }
    }


    /**
     * 设置密码
     *
     * @param inputPath
     * @param outputPath
     * @param userPassword  用户密码
     * @param ownerPassword 拥有者密码
     */
    public static void setPassword(String inputPath, String outputPath, String userPassword, String ownerPassword) {
        try (FileInputStream is = new FileInputStream(inputPath);
             FileOutputStream os = new FileOutputStream(outputPath)) {
            setPassword(is, os, userPassword, ownerPassword);
        } catch (Exception e) {
            log.error("PDF文档添加水印时发生异常", e);
        }
    }


    /**
     * 添加水印
     *
     * @param inputBytes
     * @param userPassword
     * @param ownerPassword
     */
    public static byte[] setPassword(byte[] inputBytes, String userPassword, String ownerPassword) {
        try (ByteArrayInputStream is = new ByteArrayInputStream(inputBytes);
             ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            setPassword(is, os, userPassword, ownerPassword);
            return os.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("PDF文档添加水印时发生异常", e);
        }
    }


    /**
     * 添加水印
     *
     * @param is
     * @param os
     * @param watermark
     */
    public static void addWaterMark(InputStream is, OutputStream os, String watermark) {
        PDFWaterMarkUtil.addWaterMark(is, os, watermark);
    }


    /**
     * 添加水印
     *
     * @param inputPath
     * @param outputPath
     * @param waterMark
     */
    public static void addWaterMark(String inputPath, String outputPath, String waterMark) {
        PDFWaterMarkUtil.addWaterMark(inputPath, outputPath, waterMark);
    }

    /**
     * 添加水印
     *
     * @param inputBytes
     * @param waterMark
     */
    public static byte[] addWaterMark(byte[] inputBytes, String waterMark) {
        return PDFWaterMarkUtil.addWaterMark(inputBytes, waterMark);
    }


    /**
     * 关闭资源
     *
     * @param stamper
     * @param reader
     */
    private static void closeResource(PdfStamper stamper, PdfReader reader) {
        if (stamper != null) {
            try {
                stamper.close();
            } catch (Exception e) {
                log.error("pdf文档添加水印,关闭PdfStamper时发生异常", e);
            }
        }
        if (reader != null) {
            try {
                reader.close();
            } catch (Exception e) {
                log.error("pdf文档添加水印,关闭PdfReader时发生异常", e);
            }
        }
    }


}
