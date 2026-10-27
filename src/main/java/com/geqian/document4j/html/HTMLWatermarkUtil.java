package com.geqian.document4j.html;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.*;

public class HTMLWatermarkUtil {

    private static final String charset = "UTF-8";

    private static final String watermarkScript;

    static {
        InputStream inputStream = HTMLWatermarkUtil.class.getClassLoader().getResourceAsStream("js/WaterMark.js");
        watermarkScript = readToString(inputStream);
    }

    /**
     * 添加文字平铺水印
     *
     * @param watermark
     * @param htmlBytes
     * @param outputStream
     */
    public static void addFullTextWatermark(byte[] htmlBytes, OutputStream outputStream, String watermark) {
        try (InputStream inputStream = new ByteArrayInputStream(htmlBytes)) {
            addFullTextWatermark(inputStream, outputStream, watermark);
        } catch (IOException e) {
            throw new RuntimeException("HTML添加水印时发生异常", e);
        }
    }


    /**
     * 添加文字水印
     *
     * @param watermark
     * @param htmlBytes
     */
    public static byte[] addFullTextWatermark(byte[] htmlBytes, String watermark) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            addFullTextWatermark(htmlBytes, out, watermark);
            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("HTML添加水印时发生异常", e);
        }
    }


    /**
     * 添加文字水印
     *
     * @param source
     * @param watermark
     */
    public static void addFullTextWatermark(InputStream source, OutputStream outputStream, String watermark) {
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, charset))) {
            String html = readToString(source);
            Document doc = Jsoup.parse(html, "UTF-8");
            // 水印文本拼入 JS 字符串字面量前必须转义，否则内容中的引号会使整段脚本语法错误，
            // "</script>" 则会提前闭合脚本标签
            String safeWatermark = watermark == null ? "" :
                    watermark.replace("\\", "\\\\")
                            .replace("'", "\\'")
                            .replace("</script>", "<\\/script>");
            StringBuilder scriptBuilder = new StringBuilder();
            scriptBuilder.append("\n")
                    .append(watermarkScript)
                    .append(String.format("let waterMark = new WaterMark(document.documentElement, '%s');", safeWatermark))
                    .append("\n")
                    .append("waterMark.create();")
                    .append("\n");
            // 创建 <script> 标签并添加 JavaScript 代码
            Element scriptElement = doc.createElement("script");
            scriptElement.attr("type", "text/javascript");
            scriptElement.append(scriptBuilder.toString());
            // 将<script>标签添加到<head>部分
            doc.head().appendChild(scriptElement);
            // 或者可以选择将其添加到<body>部分
            // doc.body().appendChild(scriptElement);
            // 使用 Jsoup 的 .html() 方法获取 HTML 字符串并写入到输出流
            writer.write(doc.html());
            // 确保数据写入
            writer.flush();
        } catch (IOException e) {
            throw new RuntimeException("HTML添加水印时发生异常", e);
        }
    }


    /**
     * 添加文字水印
     *
     * @param watermark
     * @param inputStream
     */
    public static byte[] addFullTextWatermark(InputStream inputStream, String watermark) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            addFullTextWatermark(inputStream, out, watermark);
            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("HTML添加水印时发生异常", e);
        }
    }


    /**
     * 读取输入流为字符串
     *
     * @param inputStream
     * @return
     * @throws IOException
     */
    private static String readToString(InputStream inputStream) {
        try {
            StringBuilder stringBuilder = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line).append("\n");
                }
            }
            return stringBuilder.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
