package com.yhx.test;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.wltea.analyzer.lucene.IKAnalyzer;

import java.io.IOException;
import java.io.StringReader;

public class IKAnalyzerTest {

    public static String beginAnalyzer(String line){
        IKAnalyzer analyzer = new IKAnalyzer();

        //使用智能分词
        //ik2012和ik3.0,3.0没有这个方法
        //analyzer.setUseSmart(true);


        try {
            return printAnalyzerResult(analyzer, line);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static String printAnalyzerResult(Analyzer analyzer, String keyword) throws IOException {
        String resultData = "";
        String infoData = "";

        TokenStream tokenStream = analyzer.tokenStream("content",new StringReader(keyword));
        tokenStream.addAttribute(CharTermAttribute.class);
        tokenStream.reset();
        while(tokenStream.incrementToken()){
            CharTermAttribute charTermAttribute = tokenStream.getAttribute(CharTermAttribute.class);
            infoData = infoData+ "    "+charTermAttribute.toString();

        }
        if(!"".equals(infoData)){
            resultData = resultData + infoData.trim()+"\r\n";
        }else{
            resultData = "";
        }
        return resultData;
    }

    public static void main(String[] args) {
        String line = "国寿金";
        String s = IKAnalyzerTest.beginAnalyzer(line);
        System.out.println(s);
    }

}


