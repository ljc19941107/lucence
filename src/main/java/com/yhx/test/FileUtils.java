package com.yhx.test;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FileUtils {

    public static void main(String... args) {
        try {
            // readFile("F:\\AIMS\\Sources\\TestLucene\\src\\main\\resources\\docment\\2.txt");
            readFileContentToList("F:\\AIMS\\Sources\\TestLucene\\src\\main\\resources\\docment\\1.txt");
        } catch (Exception e) {
            System.out.println("读取文件出错!");
        }
    }

    public static void readFile(String filePath) throws Exception {
        StringBuffer content = new StringBuffer();
        RandomAccessFile raf = new RandomAccessFile(new File(filePath),"r");
        String str ;
        while ((str = raf.readLine()) != null) {
            content.append(str);
        }
        raf.close();
        System.out.println(content);
    }

    public static List<Map<String,String>> readFileContentToList(String filePath) throws Exception {
        List<Map<String,String>> list = new ArrayList<>();
        RandomAccessFile raf = new RandomAccessFile(new File(filePath),"r");
        String str ;
        int i = 0;
        while ((str = raf.readLine()) != null && i < 10000000) {
            str = new String(str.getBytes("ISO-8859-1"), "GBK");
            Map<String,String> map = new HashMap<>();
            if (str.contains(":")) {
                String[] strArr = str.split(":");
                map.put("code", strArr[0]);
                map.put("name", strArr[1]);
                if (!str.contains("分行") && !str.contains("支行") && !str.contains("分部")) {
                    list.add(map);
                }
                i++;
                System.out.println("第" + i + "条数据:" + map.get("code") + ":" + map.get("name"));
            } else {
                System.out.println("数据异常退出循环");
                break;
            }
        }
        raf.close();
        return list;
    }

    public static long sizeOf(File file) {
        return 0;
    }

    public static String readFileToString(File file) {
        String content = "";

        return "";
    }
}
