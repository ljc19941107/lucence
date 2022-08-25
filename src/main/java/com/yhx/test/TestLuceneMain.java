package com.yhx.test;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.wltea.analyzer.lucene.IKAnalyzer;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class TestLuceneMain {

    private static final String INDEX_FILEPATH = "F:\\AIMS\\Sources\\TestLucene\\src\\main\\resources\\index";

    public static void main(String... args) {
        try {
            long s = System.currentTimeMillis();
            // testCreateIndex();
            long e = System.currentTimeMillis();
            System.out.println("耗时：" + (e - s));
            testSearcher();
        } catch (Exception e) {
            System.out.println("读取文件出错!"+e.getMessage());
        }
    }

    //创建索引
    public static void testCreateIndex() throws Exception {
        //指定索引库的存放位置Directory对象
        Directory directory = FSDirectory.open(Paths.get(INDEX_FILEPATH));
        //索引库还可以存放到内存中
        //Directory directory = new RAMDirectory();

        //指定一个标准分析器，对文档内容进行分析
        Analyzer analyzer = new IKAnalyzer();

        //创建indexwriterCofig对象
        IndexWriterConfig config = new IndexWriterConfig(analyzer);

        //创建一个indexwriter对象
        IndexWriter indexWriter = new IndexWriter(directory, config);

        //原始文档的路径
        File file = new File("F:\\AIMS\\Sources\\TestLucene\\src\\main\\resources\\docment");
        File[] fileList = file.listFiles();
        for (File file2 : fileList) {
            List<Map<String,String>> list = FileUtils.readFileContentToList(file2.getAbsolutePath());
            for (Map<String, String> map : list) {
                //创建document对象
                Document document = new Document();

                //创建field对象，将field添加到document对象中
                //第一个参数：域的名称
                //第二个参数：域的内容
                //第三个参数：是否存储
                Field nameField = new TextField("name", map.get("name"), Field.Store.YES);
                nameField.setBoost(100f);
                //文件路径域（不分析、不索引、只存储）
                Field codeField = new StoredField("code", map.get("code"));
                Field sizeField = new StoredField("size", map.get("name").length());
                Field sizeSortField = new NumericDocValuesField("size", map.get("name").length());

                document.add(codeField);
                document.add(nameField);
                document.add(sizeField);
                document.add(sizeSortField);

                //使用indexwriter对象将document对象写入索引库，此过程进行索引创建。并将索引和document对象写入索引库。
                indexWriter.addDocument(document);
            }
        }
        //关闭IndexWriter对象。
        indexWriter.close();
    }

    public static void testSearcher() throws Exception {
        // 创建IndexSearcher
        Directory directory = FSDirectory.open(Paths.get(INDEX_FILEPATH));
        IndexReader r = DirectoryReader.open(directory);
        IndexSearcher indexSearcher = new IndexSearcher(r);

        // 创建搜索解析器
        String defaultField = "name";
        Analyzer analyzer = new IKAnalyzer();
        QueryParser queryParser = new QueryParser(defaultField, analyzer);

        //创建一个布尔查询对象
        BooleanQuery query = new BooleanQuery();
        //创建第一个查询条件
        Query query1 = queryParser.parse("小");
        Query query2 = queryParser.parse("米");
        //组合查询条件
        query.add(query1, BooleanClause.Occur.MUST);
        query.add(query2, BooleanClause.Occur.MUST);

        // 模糊查询对中文效果不正确
        FuzzyQuery query3 = new FuzzyQuery(new Term("name","小米"));

        // 通配符搜索
        Query query4 = new WildcardQuery(new Term("name","*小米科技*"));

        // 解析搜索
        Query query5 = queryParser.parse("name:小米科技");

        String keyWord = "中国建设银行股份有限公司";
        // keyWord = "人保资产保险";
        List<String> keyList = analyzerKeyWord(analyzer, keyWord);
        String[] queryStrArr = keyList.toArray(new String[0]);
        String[] queryFieldArr = new String[keyList.size()];
        BooleanClause.Occur[] queryOccurArr = new BooleanClause.Occur[keyList.size()];
        for (int i = 0; i < queryStrArr.length; i++) {
            queryFieldArr[i] = "name";
            queryOccurArr[i] = BooleanClause.Occur.MUST;
        }
        Query query6 = MultiFieldQueryParser.parse(queryStrArr, queryFieldArr, queryOccurArr, analyzer);

        Sort sort = new Sort(new SortField("size", SortField.Type.LONG, false), new SortField("name", SortField.Type.SCORE, false));
        TopDocs topDocs = indexSearcher.search(query6,  10000, sort, true, false);


        //读取搜索到的内容
        ScoreDoc[] scoreDocs = topDocs.scoreDocs;
        for (ScoreDoc scoreDoc : scoreDocs) {
            Document document = indexSearcher.doc(scoreDoc.doc);
            System.out.print("code:" + document.get("code") + ";name:" + document.get("name"));
            System.out.println(";size:" + document.get("size") + ">>>>得分:" + scoreDoc.score);
        }

        System.out.println("总命中数: " + topDocs.totalHits);
    }

    public static List<String> analyzerKeyWord(Analyzer analyzer, String keyword) {
        List<String> keyList = new ArrayList<>();
        try {
            System.out.println("解析搜索关键字开始!");
            TokenStream tokenStream = analyzer.tokenStream("content", new StringReader(keyword));
            tokenStream.addAttribute(CharTermAttribute.class);
            tokenStream.reset();
            while (tokenStream.incrementToken()) {
                CharTermAttribute charTermAttribute = tokenStream.getAttribute(CharTermAttribute.class);
                keyList.add(charTermAttribute.toString());
                System.out.println(charTermAttribute);
            }
            tokenStream.close();
        } catch (Exception e) {
            System.out.println("解析搜索关键字出错!");
            System.out.println(e.getMessage());
        }
        return keyList;
    }
}
