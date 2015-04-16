package util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;


public class AppUtil {

	/**
	 * md5加密算法
	 * @param str
	 * @return
	 */
	static public String md5(String str) {
		MessageDigest algorithm = null;
		try {
			algorithm = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		if (algorithm != null) {
			algorithm.reset();
			algorithm.update(str.getBytes());
			byte[] bytes = algorithm.digest();
			StringBuilder hexString = new StringBuilder();
			for (byte b : bytes) {
				hexString.append(Integer.toHexString(0xFF & b));
			}
			return hexString.toString();
		}
		return "";

	}
	
	/**
	 * beautify messy json string
	 * @param uglyJSONString
	 * @return
	 */
	public static String jsonFormatter(String uglyJSONString){
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		JsonParser jp = new JsonParser();
		JsonElement je = jp.parse(uglyJSONString);
		String prettyJsonString = gson.toJson(je);
		return prettyJsonString;
	}
	
	/**
	 * 读取文件
	 * @param file
	 * @return
	 */
	public static String readFile(File file){
		String result = "";
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(file));
			String tmp = "";
			while((tmp = reader.readLine()) != null){
				result += tmp;
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally{
			try {
				if (reader != null) {
					reader.close();
				}
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}
		return result;
	}
	
	/**
	 * 将中文转为unicode，防止传输乱码
	 * @param strText
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public static String toUnicode(String strText) { 
		char c; 
		String strRet = ""; 
		int intAsc; 
		String strHex; 
		for (int i = 0; i < strText.length(); i++) { 
			c = strText.charAt(i); 
			intAsc = (int) c; 
			if (intAsc > 128) { 
				strHex = Integer.toHexString(intAsc); 
				strRet += "\\u" + strHex; 
			} else { 
				strRet = strRet + c; 
			} 
		} 
		return strRet; 
	}
	
	/**
	 * 将\\\\u转为\\u
	 * @param jsonStr
	 * @return
	 */
	public static String formatUnicodeJson(String jsonStr){
		return jsonStr.replaceAll("\\\\u", "\\u");
	}
	
	public static void exportFile(File file, String pageContent){
		try {
			PrintWriter writer = new PrintWriter(file);
			writer.write(pageContent);
			writer.flush();
			writer.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} 
	}
	
	public static void main(String[] args) throws UnsupportedEncodingException{
		File dir = new File("E:\\traveldata\\routes\\anhuifuyang\\4_0_d9299d6e773e52ac46169c39517aaa.json");
//		File[] files = dir.listFiles();
//		for (int i = 0; i < 1; i++) {
//			File file = files[i];
//			String content = readFile(file);
//			System.out.println(content);
//		}
		String content = readFile(dir);
		System.out.println(toUnicode(content));
		
//		System.out.println(toUnicode("白云山"));
		
	}
}
