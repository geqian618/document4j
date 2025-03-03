package com.geqian.document4j.pdf;

import com.geqian.document4j.common.core.BaseTableInterceptor;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;

import java.lang.reflect.Field;

/**
 * @author geqian
 * @date 21:29 2023/11/5
 */
public interface PDFTableInterceptor extends BaseTableInterceptor {


    /**
     * 拦截表格，用于设置如表格样式等
     *
     * @param table 表格
     */
    default void interceptPdfTable(PdfPTable table) {

    }


    /**
     * 拦截表头单元格，用于设置如单元格样式、修改表头名称等
     *
     * @param cell 单元格
     * @param type 类型
     */
    default void interceptPdfHeaderCell(PdfPCell cell, Class<?> type) {
        //cell.getPhrase().getContent() 获取原表头文本
    }


    /**
     * 拦截表格主体单元格
     *
     * @param cell  单元格
     * @param obj   当前写入单元格对象
     * @param field 当前写入单元格属性
     */
    default void interceptPdfContentCell(PdfPCell cell, Object obj, Field field) {
        // cell.getPhrase().getContent() 获取文本内容
    }
}
