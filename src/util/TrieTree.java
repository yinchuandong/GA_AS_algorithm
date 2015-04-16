package util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Stack;


import net.sf.json.JSONArray;
import net.sf.json.JSONObject;



import util.AppUtil;

/**
 * 测试trie树，用于搜索的提示模块
 * @author yinchuandong
 *
					   _ooOoo_
					  o8888888o
					  88" . "88
					  (| -_- |)
					  O\  =  /O
				   ____/`---'\____
				 .'  \\|     |//  `.
				/  \\|||  :  |||//  \
			   /  _||||| -:- |||||-  \
			   |   | \\\  -  /// |   |
			   | \_|  ''\---/''  |   |
			   \  .-\__  `-`  ___/-. /
			  ___`. .'  /--.--\  `. . __
		   ."" '<  `.___\_<|>_/___.'  >'"".
		  | | :  `- \`.;`\ _ /`;.`/ - ` : | |
		  \  \ `-.   \_ __\ /__ _/   .-` /  /
	 ======`-.____`-.___\_____/___.-`____.-'======
					    `=---='
	 ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				  佛祖保佑       永无BUG
*/
public class TrieTree {

	private TrieNode root = null;
	private HashMap<String, Integer> sentenceMap = null;
	
	public TrieTree(){
		root = new TrieNode();
		sentenceMap = new HashMap<String, Integer>();
	}
	
	/**
	 * 加入一个词到树中
	 * @param word 如：长隆欢乐世界
	 * @param viewCount 如：36555
	 */
	public void add(String word, int viewCount){
		//根节点为空
		TrieNode node = root;
		word = word.trim();
		sentenceMap.put(word, viewCount);
		
		for(int i=0; i<word.length(); i++){
			String key = word.substring(i, i+1);
			if (!node.getChildren().containsKey(key)) {
				TrieNode sub = new TrieNode();
				sub.setWord(key);
				sub.setCount(viewCount);
				node.getChildren().put(key, sub);
			}
			node.setTerminal(false);
			node = node.getChildren().get(key);
		}
		node.setTerminal(true);
		node.setSentence(true);
		
	}
	
	/**
	 * 查找指定的前缀
	 * @param word
	 * @return
	 */
	public ArrayList<Sentence> find(String word){
		ArrayList<Sentence> result = new ArrayList<Sentence>();
		 
		TrieNode node = root;
		word = word.trim();
		String prefix = "";
		for(int i=0; i<word.length(); i++){
			String key = word.substring(i, i+1);
			if (node.getChildren().containsKey(key)) {
				node = node.getChildren().get(key);
				prefix += node.getWord();
			}else{
				return result;
			}
		}
		
		//如果keyword已经构成一个词,如:baiyunshan
		if(node.isSentence){
			result.add(new Sentence(prefix, node.getCount()));
		}
		
		//节点栈，用来保存访问过的节点
		Stack<TrieNode> nodeStack = new Stack<TrieNode>();
		//字符栈，用来保存访问过的路径的字符
		Stack<String> strStack = new Stack<String>();
		
		//初始化堆栈
		Iterator<String> iterator = node.getChildren().keySet().iterator();
		while (iterator.hasNext()) {
			String key = iterator.next();
			nodeStack.push(node.getChildren().get(key));
			strStack.push(prefix);
		}
		
		String tmpStr = "";
		while(!nodeStack.empty()){
			TrieNode tmpNode = nodeStack.pop();
			tmpStr = strStack.pop() + tmpNode.word;
			
			if (tmpNode.isTerminal()) {//如果是终端词，则构成一个句子，加入到结果列表中
				Sentence sentence = new Sentence(tmpStr, tmpNode.getCount());
				result.add(sentence);
				tmpStr = "";
			}else{
				//如果该字符已经构成一个keyword词，则加入到result中
				if(tmpNode.isSentence){
					Sentence sentence = new Sentence(tmpStr, tmpNode.getCount());
					result.add(sentence);
				}
				//如果不是终端词，则将该词的children压栈，等待访问
				Iterator<String> iterChild = tmpNode.getChildren().keySet().iterator();
				while (iterChild.hasNext()) {
					String key = iterChild.next();
					nodeStack.push(tmpNode.getChildren().get(key));
					strStack.push(tmpStr);
				}
			}
		}
		
		Collections.sort(result);
		
		return result;
	}
	
	
	public static void main(String[] args) throws IOException{
		System.out.println("-----------------");
		long begin = System.currentTimeMillis();
		
		
		long end = System.currentTimeMillis();
		System.out.println("耗时：" + (end - begin));
	}
	
	
	
	/**
	 * 搜索的结果对象
	 * @author yinchuandong
	 *
	 */
	public class Sentence implements Comparable<Sentence>{
		/**
		 * 句子，如:广州白云山
		 */
		private String word = null;
		/**
		 * 访问量，如：36555
		 */
		private int viewCount = 0;
		
		public Sentence(String word, int viewCount){
			this.word = word;
			this.viewCount = viewCount;
		}

		public String getWord() {
			return word;
		}

		public void setWord(String word) {
			this.word = word;
		}

		public int getViewCount() {
			return viewCount;
		}

		public void setViewCount(int viewCount) {
			this.viewCount = viewCount;
		}

		@Override
		public int compareTo(Sentence o) {
			if (this.getViewCount() > o.getViewCount()) {
				return -1;
			}else{
				return 1;
			}
		}
		
		
	}
	
	/**
	 * 字典树对象
	 * @author yinchuandong
	 *
	 */
	public class TrieNode{
		
		private String word = null;
		private HashMap<String, TrieNode> children = null;
		private int count = 0;
		private boolean isSentence = false;
		private boolean isTerminal = false;
		
		public TrieNode(){
			children = new HashMap<String, TrieNode>();
		}

		public String getWord() {
			return word;
		}

		public void setWord(String word) {
			this.word = word;
		}
		
		public HashMap<String, TrieNode> getChildren() {
			return children;
		}

		public void setChildren(HashMap<String, TrieNode> children) {
			this.children = children;
		}

		public int getCount() {
			return count;
		}

		public void setCount(int count) {
			this.count = count;
		}

		public boolean isSentence() {
			return isSentence;
		}

		public void setSentence(boolean isSentence) {
			this.isSentence = isSentence;
		}

		public boolean isTerminal() {
			return isTerminal;
		}

		public void setTerminal(boolean isTerminal) {
			this.isTerminal = isTerminal;
		}

		
		
	}
}
