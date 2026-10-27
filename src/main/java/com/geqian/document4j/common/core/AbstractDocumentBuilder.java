package com.geqian.document4j.common.core;

import com.geqian.document4j.common.extend.Converter;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 各文档 Builder 的公共实现：文件输出与表格元数据解析。
 * 文档格式相关的输出由子类实现 {@link #asBytes()}。
 *
 * @author geqian
 */
public abstract class AbstractDocumentBuilder<B extends AbstractDocumentBuilder<B, S>, S> implements DocumentBuilder<B, S> {

    @Override
    public void asFile(String filePath) {
        asFile(new File(filePath));
    }

    @Override
    public void asFile(File file) {
        try (OutputStream outputStream = new BufferedOutputStream(Files.newOutputStream(file.toPath()))) {
            asOutputStream(outputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void asOutputStream(OutputStream outputStream) {
        try {
            outputStream.write(asBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 解析表格需要输出的字段、表头、枚举映射与值转换器，并应用拦截器
     */
    protected static TableData resolveTable(WriteTableable writeTableable) {
        List<Field> fields = TableSupport.getFieldList(writeTableable.getClass());
        if (writeTableable instanceof BaseTableInterceptor) {
            fields = ((BaseTableInterceptor) writeTableable).interceptFields(writeTableable.getClass(), fields);
        }
        List<String> headers = TableSupport.getTableHeaders(fields);
        if (writeTableable instanceof BaseTableInterceptor) {
            headers = ((BaseTableInterceptor) writeTableable).interceptTableHeaders(headers, writeTableable.getClass());
        }
        return new TableData(fields, headers, TableSupport.loadEnumMappings(fields), TableSupport.loadConverters(fields));
    }

    /**
     * 根据扩展名推断图片类型，默认 png
     */
    protected static String imageMimeType(String filename) {
        String name = filename == null ? "" : filename.toLowerCase(Locale.ROOT);
        if (name.endsWith(".jpg") || name.endsWith(".jpeg")) {
            return "image/jpeg";
        }
        if (name.endsWith(".gif")) {
            return "image/gif";
        }
        if (name.endsWith(".bmp")) {
            return "image/bmp";
        }
        if (name.endsWith(".webp")) {
            return "image/webp";
        }
        if (name.endsWith(".svg")) {
            return "image/svg+xml";
        }
        return "image/png";
    }

    /**
     * 表格元数据
     */
    protected static final class TableData {

        public final List<Field> fields;

        public final List<String> headers;

        public final Map<String, String> enumMappings;

        public final Map<String, Converter> converters;

        TableData(List<Field> fields, List<String> headers, Map<String, String> enumMappings, Map<String, Converter> converters) {
            this.fields = fields;
            this.headers = headers;
            this.enumMappings = enumMappings;
            this.converters = converters;
        }
    }
}
