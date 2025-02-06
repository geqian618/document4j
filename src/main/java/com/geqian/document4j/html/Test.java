package com.geqian.document4j.html;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

/**
 * @author geqian
 * @date 21:26 2025/2/6
 */
public class Test {

    public static void main(String[] args) throws FileNotFoundException {

        //FileOutputStream outputStream = new FileOutputStream("d://desktop/test.html");
        //
        //HTMLBuilder.create(outputStream)
        //        .setStyleSelector("body{padding:50px}")
        //        .paragraph("111111111111", HTMLStyle.Font.BOLD)
        //        .paragraph("111111111111", HTMLStyle.Font.fontColor("red"))
        //        .paragraph("111111111111")
        //        .paragraph("111111111111")
        //        .paragraph("111111111111")
        //        .paragraph("111111111111")
        //        .build();

        FileInputStream inputStream = new FileInputStream("d://desktop/test.html");

        FileOutputStream outputStream = new FileOutputStream("d://desktop/test_back.html");
        //
        HTMLWatermarkUtil.addFullTextWatermark(inputStream,outputStream,"禁止复制");

    }

}
