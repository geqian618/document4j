package com.geqian.document4j.word;

import com.geqian.document4j.excel.WaterMarkUtil;
import com.microsoft.schemas.office.office.CTLock;
import com.microsoft.schemas.vml.*;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.wp.usermodel.HeaderFooterType;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFHeader;
import org.openxmlformats.schemas.officeDocument.x2006.sharedTypes.STTrueFalse;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * @description: word文档添加水印，支持一行多个，一页多行的效果，而非一页只有一个水印的效果
 * @author: slhe
 * @version: 1.0
 * @date: 2024/8/23 下午1:43
 * @Copyright:
 */
public class WordWaterMarkUtil {

    private static final Logger log = LoggerFactory.getLogger(WaterMarkUtil.class);

    /**
     * word字体
     */
    private static final String FONT_NAME = "宋体";
    /**
     * 字体大小
     */
    private static final String FONT_SIZE = "0.3pt";
    /**
     * 字体颜色
     */
    private static final String FONT_COLOR = "#C0C0C0";

    /**
     * 一个字平均长度，单位pt，用于：计算文本占用的长度（文本总个数*单字长度）
     */
    private static final Integer WIDTH_PER_WORD = 10;
    /**
     * 文本旋转角度
     */
    private static final String STYLE_ROTATION = "30";


    public static void addWaterMark(String inputPath, String targetPath, String waterMark) {
        addWaterMark(inputPath, targetPath, waterMark, 10, 4);
    }

    public static void addWaterMark(String inputPath, String targetPath, String waterMark, int rows, int cols) {
        long beginTime = System.currentTimeMillis();
        try (
                OutputStream out = Files.newOutputStream(Paths.get(targetPath));
                InputStream in = Files.newInputStream(Paths.get(inputPath));
                OPCPackage srcPackage = OPCPackage.open(in);
                XWPFDocument doc = new XWPFDocument(srcPackage)
        ) {
            addWaterMark(doc, waterMark, rows, cols);
            // 输出新文档
            doc.write(out);
            log.info("添加水印成功！一共耗时" + (System.currentTimeMillis() - beginTime) + "毫秒");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * 为文档添加平铺水印
     *
     * @param doc       需要被处理的docx文档对象
     * @param waterMark 水印内容
     */
    private static void addWaterMark(XWPFDocument doc, String waterMark, int rows, int cols) {
        // 获取文档的所有页眉，如果文档没有页眉，则新建一个默认页眉
        List<XWPFHeader> headerList = new ArrayList<>(doc.getHeaderList());
        if (headerList.isEmpty()) {
            headerList.add(doc.createHeader(HeaderFooterType.DEFAULT));
        }
        CTDocument1 document = doc.getDocument();
        CTBody body = document.getBody();
        CTSectPr section = body.getSectPr();
        // 设置页面大小  当前A4大小
        CTPageSz pageSize = section.getPgSz();
        // 页面宽度，单位 pt
        int pageWidth = ((BigInteger) pageSize.getW()).intValue() / 20;
        // 页面高度，单位 pt
        int pageHeight = ((BigInteger) pageSize.getH()).intValue() / 20;
        // 水印宽度
        int waterMarkWidth = pageWidth / cols / WIDTH_PER_WORD;
        // 水印上下间隔高度
        int spacing = 3 * (pageHeight / rows);
        // 遍历所有的header，这里包含所有的分页
        for (XWPFHeader header : headerList) {
            int size = header.getParagraphs().size();
            if (size == 0) {
                header.createParagraph();
            }
            for (int row = 0; row < rows; row++) {
                int halfLength = (waterMarkWidth - waterMark.length()) / 2;
                // 水印文字之间使用8个空格分隔
                String content = repeatString(" ", halfLength) + waterMark + repeatString(" ", halfLength);
                // 一行水印重复水印文字次数
                content = repeatString(content, cols);
                // 添加水印图形
                CTP ctp = header.getParagraphArray(0).getCTP();
                byte[] rsidr = doc.getDocument().getBody().getPArray(0).getRsidR();
                byte[] rsidrDefault = doc.getDocument().getBody().getPArray(0).getRsidRDefault();
                ctp.setRsidP(rsidr);
                ctp.setRsidRDefault(rsidrDefault);
                CTPPr ppr = ctp.addNewPPr();
                ppr.addNewPStyle().setVal("Header");
                // 开始加水印
                CTR ctr = ctp.addNewR();
                CTGroup group = CTGroup.Factory.newInstance();
                CTShapetype shapeType = group.addNewShapetype();
                CTTextPath shapeTypeTextPath = shapeType.addNewTextpath();
                shapeTypeTextPath.setOn(STTrueFalse.T);
                shapeTypeTextPath.setFitshape(STTrueFalse.T);
                CTLock lock = shapeType.addNewLock();
                lock.setExt(STExt.EDIT);
                CTShape shape = group.addNewShape();
                shape.setId("PowerPlusWaterMarkObject");
                shape.setSpid("_x0000_s206");
                shape.setSpt(136f);
                shape.setType("#_x0000_t136");
                // 设置形状样式（旋转，位置，相对路径等参数）
                shape.setStyle(getShapeStyle(content.length(), (row - 1) * spacing));
                shape.setFillcolor(FONT_COLOR);
                shape.setFilled(STTrueFalse.T);
                shape.setCoordsize("21600,21600");
                shape.setAdj("10800");
                // 字体设置为实心
                shape.setStroked(STTrueFalse.FALSE);

                // 绘制文本的路径
                // 设置文本字体与大小
                CTTextPath shapeTextPath = shape.addNewTextpath();
                shapeTextPath.setStyle("font-family:" + FONT_NAME + ";font-size:" + FONT_SIZE);
                shapeTextPath.setString(content);

                CTPicture pict = ctr.addNewPict();
                pict.set(group);
            }
        }
    }

    private static String getShapeStyle(int waterMarkSize, int marginTop) {
        StringBuilder sb = new StringBuilder();
        // 文本path绘制的定位方式
        sb.append("position: ").append("absolute");
        // 计算文本占用的长度（文本总个数*单字长度）
        sb.append(";width: ").append(waterMarkSize * WIDTH_PER_WORD).append("pt");
        // 字体高度
        sb.append(";height: ").append("20pt");
        sb.append(";z-index: ").append(-251645952);
        sb.append(";mso-wrap-edited: ").append("f");
        // 设置水印的间隔，这是一个大坑，不能用top,必须要margin-top。
        sb.append(";margin-top: ").append(marginTop);
        sb.append(";mso-position-horizontal-relative: ").append("page");
        sb.append(";mso-position-vertical-relative: ").append("page");
        sb.append(";mso-position-vertical: ").append("left");
        sb.append(";mso-position-horizontal: ").append("center");
        sb.append(";rotation: ").append(STYLE_ROTATION);
        return sb.toString();
    }

    private static String repeatString(String pattern, int repeats) {
        StringBuilder buffer = new StringBuilder(pattern.length() * repeats);
        Stream.generate(() -> pattern).limit(repeats).forEach(buffer::append);
        return new String(buffer);
    }


    public static void main(String[] args) {
        String srcWord = "D:\\Desktop\\个人简历.docx";
        String tarWord = "D:\\Desktop\\个人简历-副本.docx";
        String waterMark = "严禁复制";
        WordWaterMarkUtil.addWaterMark(srcWord, tarWord, waterMark);
    }

}