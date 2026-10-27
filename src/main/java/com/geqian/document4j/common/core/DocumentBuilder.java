package com.geqian.document4j.common.core;

import java.io.File;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Word、PDF、HTML、Markdown 等文档 Builder 的公共 API。
 * <p>
 * {@code B} 为 Builder 自身类型，用于链式调用；{@code S} 为该格式的样式类型，
 * 例如 Word 为 {@code WordStyle}、PDF 为 {@code PDFStyle}。
 * 各文档格式独有的能力（目录、模板、文档描述等）由对应实现类提供。
 * <p>
 * 样式参数以 {@code List<S>} 作为抽象方法、{@code S...} 作为默认方法：
 * 泛型可变参数在接口调用处会创建 {@code Object[]}，若直接作为抽象方法，
 * 实现类桥接方法将其转换成具体样式数组时会抛 {@link ClassCastException}。
 *
 * @author geqian
 */
@SuppressWarnings("unchecked")
public interface DocumentBuilder<B extends DocumentBuilder<B, S>, S> {

    /**
     * 添加标题
     *
     * @param text    标题文本
     * @param heading 标题级别样式
     * @param styles  其他样式
     */
    B heading(String text, S heading, List<S> styles);

    /**
     * 添加段落
     */
    B paragraph(String text, List<S> styles);

    /**
     * 添加空白行
     */
    B blankRow();

    /**
     * 换页
     */
    B newPage();

    /**
     * 分割线
     */
    B divisionLine();

    /**
     * 添加链接
     *
     * @param text 链接文本
     * @param link 链接地址
     */
    B link(String text, String link, List<S> styles);

    /**
     * 添加无序列表
     */
    B list(List<String> elements, List<S> styles);

    /**
     * 添加图片（HTML、Markdown 以 base64 内联）
     *
     * @param bytes    图片内容
     * @param filename 文件名，用于图片替代文本及类型推断
     * @param width    宽度
     * @param height   高度
     */
    B picture(byte[] bytes, String filename, int width, int height);

    /**
     * 写入对象列表表格
     */
    B table(List<? extends WriteTableable> dataList, List<S> styles);

    /**
     * 写入二维数组表格
     */
    B table(Object[][] data, List<S> styles);

    /**
     * 文档转换为字节数组
     */
    byte[] asBytes();

    /**
     * 文档写入文件
     */
    void asFile(File file);

    /**
     * 文档写入文件
     */
    void asFile(String filePath);

    /**
     * 文档写入输出流
     */
    void asOutputStream(OutputStream outputStream);

    default B heading(String text, S heading, S... styles) {
        return heading(text, heading, styles == null ? Collections.<S>emptyList() : Arrays.asList(styles));
    }

    default B paragraph(String text, S... styles) {
        return paragraph(text, styles == null ? Collections.<S>emptyList() : Arrays.asList(styles));
    }

    default B link(String text, String link, S... styles) {
        return link(text, link, styles == null ? Collections.<S>emptyList() : Arrays.asList(styles));
    }

    default B list(List<String> elements, S... styles) {
        return list(elements, styles == null ? Collections.<S>emptyList() : Arrays.asList(styles));
    }

    default B table(List<? extends WriteTableable> dataList, S... styles) {
        return table(dataList, styles == null ? Collections.<S>emptyList() : Arrays.asList(styles));
    }

    default B table(Object[][] data, S... styles) {
        return table(data, styles == null ? Collections.<S>emptyList() : Arrays.asList(styles));
    }
}
