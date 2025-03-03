package com.geqian.document4j.markdown;

import com.geqian.document4j.common.core.BaseTableInterceptor;

import java.lang.reflect.Field;

/**
 * @author geqian
 * @date 21:29 2023/11/5
 */
public interface MarkdownTableInterceptor extends BaseTableInterceptor {

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
