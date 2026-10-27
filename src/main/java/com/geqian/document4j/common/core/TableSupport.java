package com.geqian.document4j.common.core;

import com.geqian.document4j.common.annotation.TableField;
import com.geqian.document4j.common.extend.Converter;
import com.geqian.document4j.common.extend.NoConverter;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Word/PDF/HTML/Markdown 四种 Builder 共用的表格写入支撑：
 * 属性筛选排序、表头生成、枚举映射与转换器加载、单元格取值。
 *
 * @author geqian
 */
public final class TableSupport {

    private TableSupport() {
    }

    /**
     * 获取需要写入表格的属性：子类与父类全部属性；
     * 任一属性标注了 TableField 时，只保留未 exclude 的标注属性，按 order 升序排序。
     */
    public static List<Field> getFieldList(Class<?> type) {

        List<Class<?>> classes = new ArrayList<>();

        Class<?> classType = type;

        //遍历获取父类class
        while (!Objects.equals(classType, Object.class)) {
            classes.add(classType);
            classType = classType.getSuperclass();
        }

        // 获取子类及父类全部属性
        List<Field> fields = classes.stream()
                .map(pojoClass -> Stream.of(pojoClass.getDeclaredFields()).collect(Collectors.toList()))
                .flatMap(Collection::stream)
                .collect(Collectors.toList());

        // 属性中是否匹配到任意一个包含 TableField 注解
        boolean anyMatch = fields.stream()
                .anyMatch(field -> field.isAnnotationPresent(TableField.class));

        // 如果使用了 TableField 注解，只获取包含 TableField 注解的属性，反之获取全部属性
        if (anyMatch) {
            fields = fields.stream()
                    .filter(field -> field.isAnnotationPresent(TableField.class) && !field.getAnnotation(TableField.class).exclude())
                    .collect(Collectors.toList());
        }

        // Integer.compare 而非相减，避免 order 差值过大时整型溢出
        return fields.stream().sorted((a, b) -> {
            TableField before = a.getAnnotation(TableField.class);
            TableField next = b.getAnnotation(TableField.class);
            return Integer.compare(before == null ? Integer.MAX_VALUE : before.order(),
                    next == null ? Integer.MAX_VALUE : next.order());
        }).collect(Collectors.toList());
    }

    /**
     * 获取表头：通过注解获取需要输出的属性别名
     */
    public static List<String> getTableHeaders(List<Field> fieldList) {
        return fieldList.stream()
                .map(field -> {
                    TableField annotation = field.getAnnotation(TableField.class);
                    return annotation != null && !Objects.equals("", annotation.value())
                            ? annotation.value()
                            : field.getName();
                }).collect(Collectors.toList());
    }

    /**
     * 加载全部属性值转换器
     */
    public static Map<String, Converter> loadConverters(List<Field> fields) {
        Map<String, Converter> converters = new HashMap<>();
        for (Field field : fields) {
            if (field.isAnnotationPresent(TableField.class)) {
                TableField annotation = field.getAnnotation(TableField.class);
                if (annotation.converter() != NoConverter.class) {
                    try {
                        converters.put(getFieldKey(field), annotation.converter().getConstructor().newInstance());
                    } catch (ReflectiveOperationException e) {
                        throw new RuntimeException("创建 Converter 转换器发生异常: " + annotation.converter().getName(), e);
                    }
                }
            }
        }
        return converters;
    }

    /**
     * 加载全部枚举映射关系
     */
    public static Map<String, String> loadEnumMappings(List<Field> fields) {
        Map<String, String> enumMappings = new HashMap<>();
        for (Field field : fields) {
            if (field.isAnnotationPresent(TableField.class)) {
                TableField annotation = field.getAnnotation(TableField.class);
                String[] enums = annotation.enums();
                for (String enumStr : enums) {
                    String[] splitArray = enumStr.split("->");
                    if (splitArray.length != 2) {
                        throw new IllegalArgumentException("The convert property format of TableField is：oldValue -> newValue");
                    }
                    enumMappings.put(getFieldKey(field) + ":" + splitArray[0].trim(), splitArray[1].trim());
                }
            }
        }
        return enumMappings;
    }

    /**
     * 获取单元格值：枚举映射 → 转换器 → 字符串；null 写作空字符串而非字面量 "null"
     */
    public static String getCellValue(Object obj, Field field, Map<String, String> enumMappings, Map<String, Converter> converters) {
        Object fieldValue;
        try {
            field.setAccessible(true);
            fieldValue = field.get(obj);
            //执行属性值枚举映射
            Object enumValue = enumMappings.get(getFieldKey(field) + ":" + fieldValue);
            if (enumValue != null) {
                fieldValue = enumValue;
            }
            //执行属性值转换
            Converter converter = converters.get(getFieldKey(field));
            if (converter != null) {
                fieldValue = converter.convert(fieldValue);
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException("读取表格属性值发生异常: " + field.getName(), e);
        }
        return fieldValue == null ? "" : String.valueOf(fieldValue);
    }

    /**
     * 获取类名+属性名
     */
    private static String getFieldKey(Field field) {
        return field.getDeclaringClass().getName() + "." + field.getName();
    }
}
