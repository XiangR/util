package com.joker.normal;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;

/**
 * 按照行读取文件 插入到另一个文件中 按逗号分隔符进行分割
 *
 * @author xiangR
 */
public class ReadFile {

    public static void main(String[] args) {
        readFileByLines("f:wenjian.txt");
    }

    public static void readFileByLines(String fileName) {
        // BufferedReader 是可以按行读取文件
        try {

            FileInputStream inputStream = new FileInputStream("f://wenjian.txt");
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

            String str = null;
            while ((str = bufferedReader.readLine()) != null) {
                // System.out.println(str);
                String[] strArr = str.split(",|，");
                for (String src : strArr) {
                    appendFileText("f://wenjianFanyi.txt", src);
                }
            }
            // close
            inputStream.close();
            bufferedReader.close();
        } catch (Exception e) {

        }
    }

    public static void appendFileText(String fileName, String content) {
        if (content == null || content.length() == 0) {
            return;
        }
        BufferedWriter out = null;
        try {
            out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName, true), "UTF-8"));
            out.newLine();
            out.write(content);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
