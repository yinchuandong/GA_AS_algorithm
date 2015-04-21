package test;

import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.management.Query;

import model.Route;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.DoubleField;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.IntField;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.NumericRangeQuery;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.SimpleFSDirectory;
import org.apache.lucene.util.Version;
import org.wltea.analyzer.lucene.IKAnalyzer;

import util.DbUtil;
import util.RouteUtil;

public class TestLucene {

	// Lucene Document的域名
	public static final String COL_NAME = "sname";
	public static final String COL_CONT = "viewCount";
	
	// 检索内容
	String text = "IK Analyzer是一个结合词典分词和文法分词的中文分词开源工具包。它使用了全新的正向迭代最细粒度切分算法。";
	Analyzer analyzer = null;
	Directory directory = null;
	IndexWriter iWriter = null;
	IndexReader iReader = null;
	IndexSearcher iSeacher = null;

	
	public TestLucene(){
		try {
			analyzer = new IKAnalyzer(false);
			directory = new SimpleFSDirectory(new File("./index/"));
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
	
	private IndexWriter getWriter() throws IOException{
		IndexWriterConfig iWConfig = new IndexWriterConfig(Version.LUCENE_48, analyzer);
		//每次都重新创建索引，否则可选create_or_append
		iWConfig.setOpenMode(OpenMode.CREATE);
		IndexWriter iWriter = new IndexWriter(directory, iWConfig);
		return iWriter;
	}
	
	private void genRouteIndex() throws IOException, SQLException{
		iWriter = getWriter();
		String sql = "SELECT * FROM t_route as r";
		ResultSet set = DbUtil.executeQuery(sql, null);
		while(set.next()){
			String sname = set.getString("sname");
			int viewCount = set.getInt("view_count");
			double sumPrice = set.getDouble("sum_price");
			double visitDay = set.getDouble("visit_day");
			String routeDesc = set.getString("route_desc");
			
			Field fdSname = new TextField("sname", sname, Field.Store.YES);
			Field fdViewCount = new IntField("view_count", viewCount, Field.Store.YES);
			Field fdSumPrice = new DoubleField("sum_price", sumPrice, Field.Store.YES);
			Field fdVisitDay = new DoubleField("visit_day", visitDay, Field.Store.YES);
			Field fdRouteDesc = new TextField("route_desc", routeDesc, Field.Store.YES);
			
			Document doc = new Document();
			doc.add(fdSname);
			doc.add(fdViewCount);
			doc.add(fdSumPrice);
			doc.add(fdVisitDay);
			doc.add(fdRouteDesc);
			
			iWriter.addDocument(doc);
//			System.out.println(sname);
		}
		iWriter.close();
		directory.close();
	}
	
	private void writeIndex() throws IOException{
		iWriter = getWriter();
		
		Document doc1 = new Document();
		Document doc2 = new Document();
		
		Field field11 = new TextField(COL_NAME, "广州白云山", Field.Store.YES);
		Field field12 = new IntField(COL_CONT, 100, Field.Store.YES);
		Field field13 = new StoredField("stored", "guangzhou");
		
		Field field21 = new TextField(COL_NAME, "内江白云山", Field.Store.YES);
		Field field22 = new IntField(COL_CONT, 10, Field.Store.YES);
		Field field23 = new StoredField("stored", "neijiang");

		
		doc1.add(field11);
		doc1.add(field12);
		doc1.add(field13);
		doc2.add(field21);
		doc2.add(field22);
		doc2.add(field23);
		
		iWriter.addDocument(doc1);
		iWriter.addDocument(doc2);
		
		iWriter.close();
		directory.close();
	}
	
	private void seach() throws IOException{
		iSeacher = new IndexSearcher(DirectoryReader.open(directory));
		
		Sort sort = new Sort(new SortField(COL_CONT, SortField.Type.INT, true));
		BooleanQuery bQuery = new BooleanQuery();
		
//		Term nameTerm = new Term(COL_NAME, "广州");
//		TermQuery nameQuery = new TermQuery(nameTerm);
//		bQuery.add(nameQuery, BooleanClause.Occur.MUST);

		Term descTerm = new Term("keyword", "长隆欢乐世界");
		TermQuery descQuery = new TermQuery(descTerm);
		bQuery.add(descQuery, BooleanClause.Occur.MUST);
		
		NumericRangeQuery<Double> priceQuery = NumericRangeQuery.newDoubleRange("sumPrice", 400.0, 500.0, true, true);
		bQuery.add(priceQuery, BooleanClause.Occur.MUST);
		
		NumericRangeQuery<Double> dayQuery = NumericRangeQuery.newDoubleRange("visitDay", 2.0, 3.0, false, true);
		bQuery.add(dayQuery, BooleanClause.Occur.MUST);
		
		TopDocs topDocs = iSeacher.search(bQuery, 10, sort);
		ScoreDoc[] scoreDocs = topDocs.scoreDocs;
		System.out.println(topDocs.totalHits);
		
		System.out.println("===================================");
		for (ScoreDoc scoreDoc : scoreDocs) {
			Document targetDoc = iSeacher.doc(scoreDoc.doc);
			String routeDesc = targetDoc.get("arrange");
			System.out.println(targetDoc.toString());
			System.out.println(routeDesc);
		}
	}
	
	public static void main(String[] args) throws IOException, SQLException {
		TestLucene lucene = new TestLucene();
//		lucene.writeIndex();
//		lucene.genRouteIndex();
		lucene.seach();
		System.out.println("end");
	}
	
	
	
	
	
	
	
	
	
	
}
