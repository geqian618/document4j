package com.geqian.document4j.pdf;

import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.PdfWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * PDF 扩展工具：加密、水印
 *
 * @author geqian
 * @date 0:06 2024/3/9
 */
public class PDFExtendUtils {

    private static final Logger log = LoggerFactory.getLogger(PDFExtendUtils.class);


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
            // 加密属于安全功能，失败必须向上抛出；静默失败会导致未加密的 PDF 被当作已加密交付
        } catch (Exception e) {
            throw new RuntimeException("PDF文档设置密码时发生异常", e);
        } finally {
            // stamper.close() 负责把加密后的内容写出，必须在关闭 reader 之前执行
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
        } catch (IOException e) {
            throw new RuntimeException("PDF文档设置密码时发生异常", e);
        }
    }


    /**
     * 设置密码
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
        } catch (IOException e) {
            throw new RuntimeException("PDF文档设置密码时发生异常", e);
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
                // 主流程异常时已向上抛出，这里只记录二次关闭的失败
                log.error("PDF设置密码，关闭PdfStamper时发生异常", e);
            }
        }
        if (reader != null) {
            try {
                reader.close();
            } catch (Exception e) {
                log.error("PDF设置密码，关闭PdfReader时发生异常", e);
            }
        }
    }


}
