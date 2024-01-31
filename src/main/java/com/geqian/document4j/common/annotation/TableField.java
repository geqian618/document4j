package com.geqian.document4j.common.annotation;

import com.geqian.document4j.common.extend.Converter;
import com.geqian.document4j.common.extend.NoConverter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author geqian
 * @date 20:48 2023/5/31
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface TableField {

    //标题名称
    String value() default "";

    //排序（升序）
    int order() default Integer.MAX_VALUE;

    //是否排除该字段
    boolean exclude() default false;

    //格式：旧值->新值
    String[] enums() default {};

    //属性值转换器
    Class<? extends Converter> converter() default NoConverter.class;

}
