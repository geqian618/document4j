package com.geqian.document4j.word;

import org.apache.poi.xwpf.usermodel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

/**
 * @author geqian
 * @date 15:18 2024/3/9
 */
public class WordExtendUtils {

    private static final Logger log = LoggerFactory.getLogger(WordExtendUtils.class);


    /**
     * 添加水印
     *
     * @param inputPath
     * @param targetPath
     * @param waterMark
     */
    public static void addWaterMark(String inputPath, String targetPath, String waterMark) {
        WordWaterMarkUtil.addWaterMark(inputPath, targetPath, waterMark, 10, 4);
    }


    /**
     * 添加水印
     *
     * @param inputPath
     * @param targetPath
     * @param waterMark
     * @param rows       水印行数
     * @param cols       水印列数
     */
    public static void addWaterMark(String inputPath, String targetPath, String waterMark, int rows, int cols) {
        WordWaterMarkUtil.addWaterMark(inputPath, targetPath, waterMark, rows, cols);
    }


    /**
     * 模板填充
     *
     * @param placeholderMap 占位符映射
     * @param templatePath   输入路径
     * @param outPutPath     输出路径
     */
    public static void fillTemplate(Map<String, String> placeholderMap, String templatePath, String outPutPath) {
        try (FileOutputStream outputStream = new FileOutputStream(outPutPath);
             XWPFDocument document = new XWPFDocument(Files.newInputStream(Paths.get(templatePath)))
        ) {
            // 遍历word模板中的所有段落
            for (XWPFParagraph paragraph : document.getParagraphs()) {
                // 替换段落中的占位符
                for (Map.Entry<String, String> entry : placeholderMap.entrySet()) {
                    String placeholder = entry.getKey();
                    String value = entry.getValue();
                    if (paragraph.getText().contains(placeholder)) {
                        replacePlaceholder(paragraph, placeholder, value);
                    }
                }
            }
            // 遍历word模板中的所有表格
            for (XWPFTable table : document.getTables()) {
                // 替换表格中的占位符
                for (XWPFTableRow row : table.getRows()) {
                    for (XWPFTableCell cell : row.getTableCells()) {
                        for (Map.Entry<String, String> entry : placeholderMap.entrySet()) {
                            String placeholder = entry.getKey();
                            String value = entry.getValue();
                            if (cell.getText().contains(placeholder)) {
                                replacePlaceholder(cell, placeholder, value);
                            }
                        }
                    }
                }
            }
            document.write(outputStream);
            log.info("模板填充完成!!!");
        } catch (IOException e) {
            log.error("填充word模板发生异常", e);
        }
    }

    /**
     * 段落占位符替换
     *
     * @param paragraph   段落
     * @param placeholder 占位符
     * @param value       值
     */
    private static void replacePlaceholder(XWPFParagraph paragraph, String placeholder, String value) {
        for (XWPFRun run : paragraph.getRuns()) {
            String text = run.getText(0);
            if (text != null && text.contains(placeholder)) {
                text = text.replace(placeholder, value);
                run.setText(text, 0);
                log.info("填充占位符 " + placeholder + " ==> " + value);
            }
        }
    }

    /**
     * 表格占位符替换
     *
     * @param cell        单元格
     * @param placeholder 占位符
     * @param value       值
     */
    private static void replacePlaceholder(XWPFTableCell cell, String placeholder, String value) {
        for (XWPFParagraph paragraph : cell.getParagraphs()) {
            replacePlaceholder(paragraph, placeholder, value);
        }
    }
}