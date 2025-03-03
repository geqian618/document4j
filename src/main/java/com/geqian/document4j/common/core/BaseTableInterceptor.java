package com.geqian.document4j.common.core;

import java.lang.reflect.Field;
import java.util.List;

/**
 * @author geqian
 * @date 20:06 2025/3/3
 */
public interface BaseTableInterceptor extends WriteTableable {


    /**
     * 拦截写入表格的属性
     *
     * @param type
     * @param fields
     * @return
     */
    default List<Field> interceptFields(Class<?> type, List<Field> fields) {
        return fields;
    }


    /**
     * 拦截写入表格的表头
     *
     * @param type    类型
     * @param headers 表头名称
     * @return
     */
    default List<String> interceptTableHeaders(List<String> headers, Class<?> type) {
        return headers;
    }
}
