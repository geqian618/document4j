package com.geqian.document4j.html;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URL;

public class HTMLWatermarkUtil {

    private static final Logger log = LoggerFactory.getLogger(HTMLWatermarkUtil.class);

    private static final String charset = "UTF-8";

    private static final String watermarkScript;

    static {
        URL url = HTMLWatermarkUtil.class.getClassLoader().getResource("js/WaterMark.js");
        String file = url.getFile();
        watermarkScript = readToString(file);
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
            log.error("字节数组转字节流发生异常", e);
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
        } catch (Exception e) {
            log.error("添加水印发生异常", e);
            return new byte[0];
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
            StringBuilder scriptBuilder = new StringBuilder();
            scriptBuilder.append("\n")
                    .append(watermarkScript)
                    .append(String.format("let waterMark = new WaterMark(document.documentElement, '%s');", watermark))
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
            log.error("添加水印发生异常", e);
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
            log.error("添加水印发生异常", e);
            return new byte[0];
        }
    }


    /**
     * 读取文件为字符串
     *
     * @param filePath
     * @return
     * @throws IOException
     */
    public static String readToString(String filePath) {
        return readFile(new File(filePath));
    }

    /**
     * 读取文件为字符串
     *
     * @param file
     * @return
     * @throws IOException
     */
    public static String readFile(File file) {
        try {
            StringBuilder stringBuilder = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line).append("\n");
                }
            }
            return stringBuilder.toString();
        } catch (IOException e) {
            throw new RuntimeException("读取文件发生异常", e);
        }
    }


    /**
     * 读取输入流为字符串
     *
     * @param inputStream
     * @return
     * @throws IOException
     */
    public static String readToString(InputStream inputStream) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line).append("\n");
            }
        }
        return stringBuilder.toString();
    }

    public static void main(String[] args) throws Exception {

        String text = readFile(new File("d://desktop/index.html"));

        byte[] htmlBytes = text.getBytes(charset);

        FileOutputStream outputStream = new FileOutputStream("d://desktop/output.html");

        addFullTextWatermark(htmlBytes, outputStream, "水印文字");
    }
}
