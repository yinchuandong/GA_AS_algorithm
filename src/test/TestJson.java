package test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;


import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import util.AppUtil;
import util.HotelUtil;
import util.SceneryUtil;
import model.Hotel;
import model.Scenery;

public class TestJson {
	
	
	public static void main(String[] args){
//		exportScenery("guangzhou_scenery.json");
		exportHotel("hotel.json");
		System.out.println("end");
	}
	
	public static void exportScenery(String filepath){
		ArrayList<Scenery> sceneryList = SceneryUtil.getSceneryListById("da666bc57594baeb76b3bcf0");
		JSONArray jsonArr = JSONArray.fromObject("[]");
		for (Scenery scene : sceneryList) {
			JSONObject obj = JSONObject.fromObject("{}");
			obj.put("sid", scene.getSid());
			obj.put("surl", scene.getSurl());
			obj.put("sname", scene.getSname());
			obj.put("viewCount", scene.getViewCount());
			obj.put("price", scene.getPrice());
			obj.put("visitDay", scene.getVisitDay());
			obj.put("mapX", scene.getMapX());
			obj.put("mapY", scene.getMapY());
			jsonArr.add(obj);
		}
		try {
			PrintWriter writer = new PrintWriter(new File(filepath));
			writer.write(AppUtil.jsonFormatter(jsonArr.toString()));
			writer.flush();
			writer.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	
	public static void exportHotel(String filepath){
		HashMap<String, Hotel> hotelMap = HotelUtil.getAllHotel();
		Iterator<String> iter = hotelMap.keySet().iterator();
		int count = 0;
		JSONArray jsonArr = JSONArray.fromObject("[]");
		while(iter.hasNext()){
			String key = iter.next();
			Hotel hotel = hotelMap.get(key);
			JSONObject obj = JSONObject.fromObject("{}");
			obj.put("uid", hotel.getUid());
			obj.put("sid", hotel.getSid());
			obj.put("hotelName", hotel.getHotelName());
			obj.put("hotelAddress", hotel.getHotelAddress());
			obj.put("hotelPhone", hotel.getPhone());
			obj.put("commentScore", hotel.getCommentScore());
			obj.put("price", hotel.getPrice());
			jsonArr.add(obj);
			count ++;
			if(count > 100){
				break;
			}
		}
		
		try {
			PrintWriter writer = new PrintWriter(new File(filepath));
			writer.write(AppUtil.jsonFormatter(jsonArr.toString()));
			writer.flush();
			writer.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	

}
