package util;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.NumericRangeQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.SimpleFSDirectory;
import org.wltea.analyzer.core.IKSegmenter;
import org.wltea.analyzer.core.Lexeme;
import org.wltea.analyzer.lucene.IKAnalyzer;

public class SearchUtil {

	private static SearchUtil context = null;
	private Analyzer analyzer = null;
	private Directory directory = null;
	private IndexSearcher iSeacher = null;
	
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
			analyzer = new IKAnalyzer(false);
			directory = new SimpleFSDirectory(indexFile);
			iSeacher = new IndexSearcher(DirectoryReader.open(directory));
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
	    IKSegmenter ik = new IKSegmenter(reader,false);//当为true时，分词器进行最大词长切分 
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
	
	public String search(String keyword, double minPrice, double maxPrice, double day, String orderBy, boolean reverse){
		BooleanQuery booleanQuery = new BooleanQuery();
		ArrayList<String> list = this.cut(keyword);
		for (String word : list) {
			TermQuery query = new TermQuery(new Term("ambiguitySname", word));
			booleanQuery.add(query, BooleanClause.Occur.MUST);
		}
		
		NumericRangeQuery<Double> priceQuery = NumericRangeQuery.newDoubleRange("sumPrice", minPrice, maxPrice, true, true);
		booleanQuery.add(priceQuery, BooleanClause.Occur.MUST);
		
		double minDay = day == -1 ? 0 : day - 1.0;
		double maxDay = day == -1 ? 100.0 : day;
		NumericRangeQuery<Double> dayQuery = NumericRangeQuery.newDoubleRange("visitDay", minDay, maxDay, false, true);
		booleanQuery.add(dayQuery, BooleanClause.Occur.MUST);
		
		Sort sort = null;
		if(orderBy != null && !orderBy.equals("")){
			sort = new Sort(new SortField(orderBy, SortField.Type.DOUBLE, reverse));
		}else{
			sort = new Sort(new SortField("viewCount", SortField.Type.DOUBLE, reverse));
		}
		
		try {
			TopDocs topDocs = iSeacher.search(booleanQuery, 10, sort);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
