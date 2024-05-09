## document4j

### 简介

一个简易的用于通过 Java API 实现像真实编写文档一样，编写 Word、PDF、Markdown、HTML 文档
<br>
<br>

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



##### 写入段落

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

###### 写入数据类型

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

    private Long userId;

    private String username;

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



###### @TableField

用于设置表格表头名称，枚举值映射、值转换、是否排除属性写入表格



###### WordTableInterceptor

拦截表格写入的属性、表格创建、表格单元格样式、表格单元格值



<br>
<br>



### PDF 文档

<br>
<br>



### MarkDown 文档

<br>
<br>



### HTML 文档