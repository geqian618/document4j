# document4j

## 简介

一个简易的符合文档编写习惯的 Java 文档库，用于操作 Word、PDF、Markdown、HTML 文档
<br>
<br>

## 使用教程

### Word 文档

#### WordStyle

word 样式接口，包含两个内部实现类字体样式 Font、段落样式 Paragraph

- Font 字体样式 （部分）

```java
// 加粗
WordStyle.Font bold = WordStyle.Font.BOLD;

// 字体颜色
WordStyle.Font fontColor = WordStyle.Font.fontColor("C40315");

// 字体大小
WordStyle.Font fontSize = WordStyle.Font.fontSize(20);

// 下划线
WordStyle.Font underLine = WordStyle.Font.UNDER_LINE;
```

- Paragraph  段落样式

```java
// 文本左对齐
WordStyle.Paragraph alignLeft = WordStyle.Paragraph.ALIGN_LEFT;

// 文本居中对齐
WordStyle.Paragraph alignCenter = WordStyle.Paragraph.ALIGN_CENTER;

// 文本右对齐
WordStyle.Paragraph alignRight = WordStyle.Paragraph.ALIGN_RIGHT;

// 首行缩进字符个数
WordStyle.Paragraph firstLineIndent = WordStyle.Paragraph.firstLineIndent(2);

// 一个段落中文本行间距倍数（字体大小为基数）
WordStyle.Paragraph linedSpacing = WordStyle.Paragraph.lineSpacing(1);
```



#### WordBuilder

##### 创建 WordBuilder

```java
// 创建一个 WordBuilder
WordBuilder wordBuilder = WordBuilder.create();

// 通过已有文件创建 WordBuilder
WordBuilder wordBuilder = WordBuilder.create("D://desktop/demo.docx");
```

> WordBuilder 支持流式写法



##### 构建 word 文档

```java
// 构建并写入文件
wordBuilder.asFile("D://desktop/demo.docx");

// 构建并获取子节数组
byte[] bytes = wordBuilder.asBytes();

// 构建并写入输出流
wordBuilder.asStream(OutputStream out);
```



##### 写入段落（简单段落）

```java
// 加粗
WordStyle.Font bold = WordStyle.Font.BOLD;

// 字体颜色
WordStyle.Font fontColor = WordStyle.Font.fontColor("C40315");

// 字体大小
WordStyle.Font fontSize = WordStyle.Font.fontSize(20);

// 文本居中
WordStyle.Paragraph alignCenter = WordStyle.Paragraph.ALIGN_CENTER;

// 添加标题
wordBuilder.paragraph("标题", fontSize, bold, alignCenter);

// 一个段落默认样式文本
wordBuilder.paragraph("默认样式的段落文本...");

// 字体加粗的段落文本
wordBuilder.paragraph("字体加粗的段落文本...", bold);

// 设置字体颜色的段落文本
wordBuilder.paragraph("设置字体颜色的段落文本...", fontColor);
```
> wordBuilder.paragraph 方法每调用一次就是一个段落

##### 写入段落（复杂段落）
```java
WordBuilder.create()
        .startParagraph()
            .text("hello")
            .text("word", WordStyle.Font.BOLD)
        .endParagraph();
```
> startParagraph 和 endParagraph 方法之间的整体构成一个段落

##### 换页

```java
wordBuilder.newPage();
```



##### 空白行

```java
wordBuilder.blankRow();
```



##### 写入图片

```java
public WordBuilder picture(byte[] bytes, String filename, int width, int height);

public WordBuilder picture(InputStream is, String filename, int width, int height)
```



##### 写入表格

**二维数组类型数据**

```java
public static void main(String[] args) {

    // 二维数组类型数据
    Object[][] data = {
        {"表头一", "表头二", "表头三"},
        {"一行一列", "一行二列", "一行三列"},
        {"二行一列", "二行二列", "二行三列"}
    };

    // 创建一个 WordBuilder
    WordBuilder.create()
        //写入表格，并设置单元格内文本字体样式
        .table(data, WordStyle.Font.fontSize(16))
        // 构建 word文档，并写入文件
        .asFile("D://desktop/demo.docx");
}
```



**对象类型数据**

用户对象

```java
@Data
public class User implements WriteTableable {

    @TableField("用户ID")
    private Long userId;

    @TableField("用户名")
    private String username;

    @TableField("性别")
    private Integer gender;

}
```



写入对象

```java
public static void main(String[] args) {

    List<User> users = new ArrayList<>();

    for (int i = 0; i < 10; i++) {
        User user = new User();
        user.setUserId((long) i);
        user.setUsername(UUID.randomUUID().toString());
        user.setGender(i % 2);
        users.add(user);
    }


    // 创建一个 WordBuilder
    WordBuilder.create()
         //写入表格，并设置单元格内文本字体样式
        .table(users, WordStyle.Font.fontSize(16))
        // 构建 word文档，并写入文件
        .asFile("d://desktop/demo.docx");
}
```



#### @TableField

用于设置表格表头名称，枚举值映射、值转换、是否排除属性写入表格、属性值写入表格顺序

```java
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface TableField {

    // 标题名称
    String value() default "";

    // 排序（升序）
    int order() default Integer.MAX_VALUE;

    // 是否排除该字段
    boolean exclude() default false;

    // 格式：旧值->新值
    String[] enums() default {};

    // 属性值转换器
    Class<? extends Converter> converter() default NoConverter.class;

}
```



#### WordTableInterceptor

继承了 WriteTableable 接口，用于拦截表格并修改写入的属性、表头、表格样式、表格单元格样式、表格单元格值

```java
public interface WordTableInterceptor extends WriteTableable {

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
```


<br>
<br>



### PDF 文档
请参考上述 word 文档
<br>
<br>



### MarkDown 文档
请参考上述 word 文档
<br>
<br>



### HTML 文档
请参考上述 word 文档

<br>
<br>

## 开源协议

本项目基于 [MIT License](LICENSE) 开源。

GitHub 仓库：https://github.com/geqian618/document4j

> **依赖协议说明**：项目的 PDF 能力基于 iText（`com.itextpdf:itextpdf` 5.5.13.3）实现，iText 为 AGPL-3.0 协议。将本项目作为依赖集成或二次分发时，请一并遵守 iText 的 AGPL 协议要求（https://www.gnu.org/licenses/agpl-3.0.html）；如需在闭源商业产品中使用，请更换为商业授权版本或其他 PDF 组件。