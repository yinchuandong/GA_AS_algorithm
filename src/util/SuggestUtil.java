package util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import util.TrieTree.Sentence;

public class SuggestUtil {
	private static SuggestUtil context = null;
	
	private TrieTree trieTree = null;
	private HashMap<String, KeyLine> keyWordMap = null;

	private SuggestUtil(){
		this.trieTree = new TrieTree();
		this.keyWordMap = new HashMap<String, KeyLine>();
	}
	
	/**
	 * 单例模式
	 * @param keyFile 存储keyword的文件,该文件由SuggestPreprocessUtil.java生成
	 * @return
	 */
	public static SuggestUtil getInstance(File keyFile){
		if(context == null){
			context = new SuggestUtil();
			context.init(keyFile);
		}
		return context;
	}
	
	/**
	 * 销毁实例
	 */
	public static void destoryInstance(){
		context = null;
		System.gc();
	}
	
	public static void main(String[] args){
		SuggestUtil util = SuggestUtil.getInstance(new File("keyword.txt"));
		System.out.println(util.suggest("bai云shan"));

	}
	
	/**
	 * 初始化文件
	 * @param keyFile
	 */
	private void init(File keyFile){
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(keyFile));
			String buff = null;
			while((buff = reader.readLine()) != null){
				String[] lineArr = buff.split(" ");
				trieTree.add(lineArr[0], Integer.parseInt(lineArr[1]));
				
				KeyLine line = new KeyLine();
				for (int i = 2; i < lineArr.length; i += 2) {
					KeyNode node = new KeyNode();
					node.name = lineArr[i];
					node.viewCount = Integer.parseInt(lineArr[i+1]);
					line.nodeList.add(node);
				}
				keyWordMap.put(lineArr[0], line);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally{
			try {
				reader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * 进行搜索的接口，最好不要在jsp调用该方法
	 * @param key
	 * @return
	 */
	public ArrayList<KeyNode> search(String key){
		ArrayList<Sentence> result = trieTree.find(key);
		ArrayList<KeyNode> nodeList = new ArrayList<KeyNode>();
		for (Sentence sentence : result) {
//			System.out.println(sentence.getWord() + "-" + sentence.getViewCount());
			nodeList.addAll(keyWordMap.get(sentence.getWord()).nodeList);
		}
		
		Collections.sort(nodeList, new Comparator<KeyNode>() {

			@Override
			public int compare(KeyNode o1, KeyNode o2) {
				return (o1.viewCount < o2.viewCount) ? 1 : -1;
			}
		});
		
		return nodeList;
	}
	
	/**
	 * 外部接口，返回json格式<br/>
	 * eg:{"data":[{"name":"白云山","viewCount": 128}], "info": "返回成功", "status":1}
	 * @param keyWord 输入的查询词，拼音或者中文
	 * @return 返回热度最大的前10条记录
	 */
	public String suggest(String keyWord){
		JSONObject resultObj = JSONObject.fromObject("{}");
		if (keyWord == null || keyWord.length() <1) {
			resultObj.put("info", "关键字不合法");
			resultObj.put("status", "0");
			resultObj.put("data", "[]");
		}else{
			keyWord = PinYinUtil.converterToSpell(keyWord);
			ArrayList<KeyNode> nodeList = search(keyWord);
			List<KeyNode> subList = nodeList.subList(0, nodeList.size() > 10 ? 10 : nodeList.size());
//			for (KeyNode node : subList) {
//				System.out.println(node.name + "-" + node.viewCount);
//			}
			if(subList.size() != 0){
				resultObj.put("info", "返回成功");
				resultObj.put("status", "1");
				resultObj.put("data", JSONArray.fromObject(subList));
			}else{
				resultObj.put("info", "暂无记录");
				resultObj.put("status", "0");
				resultObj.put("data", "[]");
			}
		}
		return AppUtil.toUnicode(resultObj.toString());
	}
	
	public class KeyNode{
		public String name;
		public int viewCount = 0;
		
		@Override
		public boolean equals(Object obj) {
			KeyNode node = (KeyNode)obj;
			return name.equals(node.name);
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public int getViewCount() {
			return viewCount;
		}

		public void setViewCount(int viewCount) {
			this.viewCount = viewCount;
		}
		
		
		
	}
	
	private class KeyLine{
		int maxCount = 0;
		ArrayList<KeyNode> nodeList = new ArrayList<KeyNode>();
	}
}
