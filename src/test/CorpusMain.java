package test;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import util.DbUtil;

public class CorpusMain {

	HashMap<String, KeyLine> keyWordMap = new HashMap<String, KeyLine>();
	
	public static void main(String[] args){
		
	}
	
	/**
	 * 将所有的景点surl放入map中，不需要转拼音
	 * @throws SQLException
	 */
	public void selectWithoutTransfer() throws SQLException{
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
	public void selectWithTransfer() throws SQLException{
		String sql = "SELECT s.surl,s.sname,s.view_count from t_scenery as s where s.ambiguity_sname != s.sname";
		ResultSet set = DbUtil.executeQuery(sql, null);
		while(set.next()){
			String surl = set.getString("surl");
			String sname = set.getString("sname");
			int viewCount = set.getInt("view_count");
			if(!keyWordMap.containsKey(surl)){
				
				KeyNode node = new KeyNode();
				node.viewCount = viewCount;
				node.name = sname;
				
				KeyLine line = new KeyLine();
				line.maxCount = viewCount;
				line.nodeList.add(node);
				keyWordMap.put(surl, line);
			}else{
				KeyNode node = new KeyNode();
				node.viewCount = viewCount;
				node.name = sname;
				
				KeyLine line = keyWordMap.get(surl);
				line.maxCount = viewCount > line.maxCount ? viewCount : line.maxCount;
				line.nodeList.add(node);
			}
		}
	}
	
	private class KeyNode{
		String name;
		int viewCount = 0;
	}
	
	private class KeyLine{
		int maxCount = 0;
		ArrayList<KeyNode> nodeList = new ArrayList<KeyNode>();
	}
	
}
