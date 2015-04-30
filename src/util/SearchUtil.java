package util;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Term;
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
import org.wltea.analyzer.core.IKSegmenter;
import org.wltea.analyzer.core.Lexeme;


import util.AppUtil;

public class SearchUtil {

	private static SearchUtil context = null;
	private Directory directory = null;
	private IndexSearcher iSeacher = null;
    private IKSegmenter ik = null;

	
	private SearchUtil(){
		
	}
	
	public static SearchUtil getInstance(File indexFile){
		if(context == null){
			context = new SearchUtil();
			context.init(indexFile);
		}
		return context;
	}
	
	private void init(File indexFile){
		try {
			directory = new SimpleFSDirectory(indexFile);
			iSeacher = new IndexSearcher(DirectoryReader.open(directory));
			ik = new IKSegmenter(null, true);//当为true时，分词器进行最大词长切分
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 切分词语，结果返回到arraylist中
	 * @param word
	 * @return
	 */
	private ArrayList<String> cut(String word){
		ArrayList<String> list = new ArrayList<String>();
		if (word == null || word.equals("")) {
			return list;
		}
	    StringReader reader = new StringReader(word); 
	    ik.reset(reader);
	    
	    Lexeme lexeme = null; 
	    try {
			while((lexeme = ik.next())!=null) {
				list.add(lexeme.getLexemeText());
				System.out.println(lexeme.getLexemeText());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	    
	    return list;
	}
	
	/**
	 * 搜索接口，搜索路线中含有keyword的路线
	 * @param keyword
	 * @param minPrice
	 * @param maxPrice
	 * @param day
	 * @param orderBy
	 * @param reverse
	 * @return
	 */
	public String search(String keyword, double minPrice, double maxPrice, double day, String orderBy, boolean reverse){
		BooleanQuery booleanQuery = new BooleanQuery();
		ArrayList<String> list = this.cut(keyword);
		for (String word : list) {
			TermQuery query = new TermQuery(new Term("keyword", word));
			booleanQuery.add(query, BooleanClause.Occur.MUST);
		}
		
		NumericRangeQuery<Double> priceQuery = NumericRangeQuery.newDoubleRange("sumPrice", minPrice, maxPrice, true, true);
		booleanQuery.add(priceQuery, BooleanClause.Occur.MUST);
		
		double qMinDay = ((int)day == -1) ? 0 : day - 1.0;
		double qMaxDay = ((int)day == -1) ? 100.0 : day;
		NumericRangeQuery<Double> dayQuery = NumericRangeQuery.newDoubleRange("visitDay", qMinDay, qMaxDay, false, true);
		booleanQuery.add(dayQuery, BooleanClause.Occur.MUST);
		
		Sort sort = null;
		if(orderBy != null && !orderBy.equals("") && !orderBy.equals("viewCount")){
			sort = new Sort(new SortField(orderBy, SortField.Type.DOUBLE, reverse));
		}else{
			sort = new Sort(new SortField("viewCount", SortField.Type.INT, reverse));
		}
		
		JSONObject resultObj = JSONObject.fromObject("{}");
		try {
			JSONArray dataArr = JSONArray.fromObject("[]");
			TopDocs topDocs = iSeacher.search(booleanQuery, 30, sort);
			ScoreDoc[] scoreDocs = topDocs.scoreDocs;
			for (ScoreDoc scoreDoc : scoreDocs) {
				Document doc = iSeacher.doc(scoreDoc.doc);
				String uid = doc.get("uid");
				String sid = doc.get("sid");
				String surl = doc.get("surl");
				String sname = doc.get("sname");
				String ambiguitySname = doc.get("ambiguitySname");
				double hotness = Double.parseDouble(doc.get("hotness"));
				int viewCount = Integer.parseInt(doc.get("viewCount"));
				double sumPrice = Double.parseDouble(doc.get("sumPrice"));
				double hotelPrice = Double.parseDouble(doc.get("hotelPrice"));
				double sceneTicket = Double.parseDouble(doc.get("sceneTicket"));
				double minDay = Double.parseDouble(doc.get("minDay"));
				double maxDay = Double.parseDouble(doc.get("maxDay"));
				double visitDay = Double.parseDouble(doc.get("visitDay"));
				double distance = Double.parseDouble(doc.get("distance"));
				String jsonName = doc.get("jsonName");
				JSONArray arrange = JSONArray.fromObject(doc.get("arrange"));
				
				JSONObject rowObj = JSONObject.fromObject("{}");
				rowObj.put("uid", uid);
				rowObj.put("sid", sid);
				rowObj.put("surl", surl);
				rowObj.put("sname", sname);
				rowObj.put("ambiguitySname", ambiguitySname);
				rowObj.put("hotness", hotness);
				rowObj.put("viewCount", viewCount);
				rowObj.put("sumPrice", sumPrice);
				rowObj.put("hotelPrice", hotelPrice);
				rowObj.put("sceneTicket", sceneTicket);
				rowObj.put("minDay", minDay);
				rowObj.put("maxDay", maxDay);
				rowObj.put("visitDay", visitDay);
				rowObj.put("distance", distance);
				rowObj.put("jsonName", jsonName);
				rowObj.put("arrange", arrange);
				
				dataArr.add(rowObj);
//				System.out.println(doc);
			}
			
			if(dataArr.size()  > 0){
				resultObj.put("data", dataArr);
				resultObj.put("info", "返回成功");
				resultObj.put("status", 1);	
			}else{
				resultObj.put("data", "[]");
				resultObj.put("info", "服务器无结果");
				resultObj.put("status", 0);
			}
		} catch (IOException e) {
//			e.printStackTrace();
			resultObj.put("data", "[]");
			resultObj.put("info", "服务器错误");
			resultObj.put("status", 0);
		}
//		System.out.println(resultObj.toString());
		return AppUtil.toUnicode(resultObj.toString());
	}
	
	
	
	
	
	public static void main(String[] args){
		SearchUtil util = SearchUtil.getInstance(new File("./index/"));
//		util.search("白云山", 0.0, 2000.0, 3.0, "viewCount", false);
		System.out.println("-------------");
		util.search("长隆欢乐世界", 0.0, 1200.0, -1.0, "hotness", false);
	}
	
	
	
	
	
	
	
	
	
	
	
	
}
