package com.geqian.document4j.word;

import com.geqian.document4j.common.extend.WriteTableable;
import org.apache.poi.wp.usermodel.Paragraph;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;

import java.lang.reflect.Field;
import java.util.List;

/**
 * @author geqian
 * @date 21:29 2023/11/5
 */
public interface WordTableIntercepter extends WriteTableable {

    /**
     * 拦截写入表格的属性集合
     *
     * @param type
     * @param fields
     * @return
     */
    default List<Field> interceptWordFields(Class<?> type, List<Field> fields) {
        return fields;
    }

    /**
     * 拦截写入表格的表头
     *
     * @param type
     * @param fields
     * @param headers
     * @return
     */
    default void interceptWordHeaders(Class<?> type, List<Field> fields, List<String> headers) {
    }


    /**
     * 拦截表格，用于设置如表格样式等
     *
     * @param table 表格
     */
    default void interceptWordTable(XWPFTable table) {

    }


    /**
     * 拦截表头单元格，用于设置如单元格样式、修改表头名称等
     *
     * @param cell 单元格
     * @param type 类型
     */
    default void interceptWordHeaderCell(XWPFTableCell cell, XWPFParagraph paragraph, Class<?> type) {
    }


    /**
     * 拦截表格单元格
     *
     * @param cell  单元格
     * @param obj   当前写入单元格对象
     * @param field 当前写入单元格属性
     */
    default void interceptWordContentCell(XWPFTableCell cell, Paragraph paragraph, Object obj, Field field) {
        //设置单元格颜色 cell.setColor("000000");
        //获取单元格文本 paragraph.getText();
        //修改单元格文本 paragraph.getRuns().get(0).setText("");
    }
}
