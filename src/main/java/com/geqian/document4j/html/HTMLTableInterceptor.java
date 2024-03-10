package com.geqian.document4j.html;

import com.geqian.document4j.common.extend.WriteTableable;

import java.lang.reflect.Field;
import java.util.List;

/**
 * @author geqian
 * @date 21:29 2023/11/5
 */
public interface HTMLTableInterceptor extends WriteTableable {


    /**
     * 拦截写入表格的属性
     *
     * @param type
     * @param fields
     * @return
     */
    default List<Field> interceptHtmlFields(List<Field> fields, Class<?> type) {
        return fields;
    }


    /**
     * 拦截写入表格的表头
     *
     * @param type    类型
     * @param headers 表头名称
     * @return
     */
    default void interceptHtmlHeaders(List<String> headers, Class<?> type) {

    }


    /**
     * 拦截表头单元格，用于设置如单元格样式、修改表头名称等
     *
     * @param cellStyleBuilder 单元格样式
     * @param type             类型
     */
    default void interceptHtmlHeaderCell(StringBuilder cellStyleBuilder, String cellValue, Class<?> type) {

    }


    /**
     * 拦截表格，用于设置如表格样式等
     *
     * @param tableStyleBuilder 表格样式
     */
    default void interceptHtmlTable(StringBuilder tableStyleBuilder) {

    }


    /**
     * 拦截表格单元格
     *
     * @param cellValue 单元格值
     * @param obj       当前写入单元格对象
     * @param field     当前写入单元格属性
     */
    default String interceptHtmlCell(StringBuilder cellStyleBuilder, String cellValue, Object obj, Field field) {
        return cellValue;
    }
}
