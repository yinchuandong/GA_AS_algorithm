package util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import util.TrieTree.Sentence;

/**
 * 预处理拼音智能提示
 * @author yinchuandong
 *
 */
public class SuggestPreprocessUtil {

	HashMap<String, KeyLine> keyWordMap = new HashMap<String, KeyLine>();
	
	public static void main(String[] args) throws SQLException{
		runKeyWord();
	}
	
	
	
	private static void runKeyWord(){
		try {
			SuggestPreprocessUtil main = new SuggestPreprocessUtil();
			//step1
			main.selectWithoutTransfer();
			//step2
			main.selectWithTransfer();
			//step3
			main.genTmpKeyword(new File("./tmp/keyword.txt"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 将所有的景点surl放入map中，不需要转拼音
	 * @throws SQLException
	 */
	private void selectWithoutTransfer() throws SQLException{
		String sql = "SELECT s.surl,s.ambiguity_sname,s.view_count from t_scenery as s";
		ResultSet set = DbUtil.executeQuery(sql, null);
		while(set.next()){
			String surl = set.getString("surl");
			String ambiguitySname = set.getString("ambiguity_sname");
			int viewCount = set.getInt("view_count");
			if(!keyWordMap.containsKey(surl)){
				
				KeyNode node = new KeyNode();
				node.viewCount = viewCount;
				node.name = ambiguitySname;
				
				KeyLine line = new KeyLine();
				line.maxCount = viewCount;
				line.nodeList.add(node);
				keyWordMap.put(surl, line);
			}else{
				KeyNode node = new KeyNode();
				node.viewCount = viewCount;
				node.name = ambiguitySname;
				
				KeyLine line = keyWordMap.get(surl);
				line.maxCount = viewCount > line.maxCount ? viewCount : line.maxCount;
				line.nodeList.add(node);
			}
		}
	}
	
	/**
	 * 将sname != ambiguity_sname 的景点sname选出并转为拼音放入map中
	 * @throws SQLException 
	 */
	private void selectWithTransfer() throws SQLException{
		String sql = "SELECT s.surl,s.sname, s.ambiguity_sname, s.view_count from t_scenery as s where s.ambiguity_sname != s.sname";
		ResultSet set = DbUtil.executeQuery(sql, null);
		while(set.next()){
			String sname = set.getString("sname");
			String ambiguitySname = set.getString("ambiguity_sname");
			int viewCount = set.getInt("view_count");
			String surl = PinYinUtil.converterToSpell(sname);

			if(!keyWordMap.containsKey(surl)){
				
				KeyNode node = new KeyNode();
				node.viewCount = viewCount;
				node.name = ambiguitySname;
				
				KeyLine line = new KeyLine();
				line.maxCount = viewCount;
				line.nodeList.add(node);
				keyWordMap.put(surl, line);
			}else{
				KeyNode node = new KeyNode();
				node.viewCount = viewCount;
				node.name = ambiguitySname;
				
				KeyLine line = keyWordMap.get(surl);
				line.maxCount = viewCount > line.maxCount ? viewCount : line.maxCount;
				if(!line.nodeList.contains(node)){
					line.nodeList.add(node);
				}
			}
		}
		System.out.println(keyWordMap.size());
	}
	
	private void genTmpKeyword(File file) throws FileNotFoundException{
		PrintWriter writer = new PrintWriter(file);
		Iterator<String> iter = keyWordMap.keySet().iterator();
		while(iter.hasNext()){
			String surl = iter.next();
			KeyLine line = keyWordMap.get(surl);
			String lineStr = "";
			lineStr += surl + " " + line.maxCount;
			for (KeyNode node : line.nodeList) {
				lineStr += " " + node.name + " " + node.viewCount;
			}
			writer.println(lineStr);
		}
		writer.close();
		
	}
	
	private class KeyNode{
		String name;
		int viewCount = 0;
		
		@Override
		public boolean equals(Object obj) {
			KeyNode node = (KeyNode)obj;
			return name.equals(node.name);
		}
		
	}
	
	private class KeyLine{
		int maxCount = 0;
		ArrayList<KeyNode> nodeList = new ArrayList<KeyNode>();
	}
	
}
