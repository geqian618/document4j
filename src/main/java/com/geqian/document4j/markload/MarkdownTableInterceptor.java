package com.geqian.document4j.markload;

import java.lang.reflect.Field;
import java.util.List;

/**
 * @author geqian
 * @date 21:29 2023/11/5
 */
public interface MarkdownTableInterceptor {


    /**
     * 拦截写入表格的属性
     *
     * @param type
     * @param fields
     * @return
     */
    default List<Field> interceptMdFields(Class<?> type, List<Field> fields) {
        return fields;
    }


    /**
     * 拦截写入表格的表头
     *
     * @param type    类型
     * @param headers 表头名称
     * @return
     */
    default void interceptMdHeaders(List<String> headers, Class<?> type) {

    }


    /**
     * 拦截表格单元格
     *
     * @param cellValue 单元格值
     * @param obj       当前写入单元格对象
     * @param field     当前写入单元格属性
     */
    default String interceptMdCellValue(String cellValue, Object obj, Field field) {
        return cellValue;
    }
}
