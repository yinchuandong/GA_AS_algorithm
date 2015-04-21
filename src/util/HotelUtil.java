package util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Iterator;

import model.Hotel;

public class HotelUtil {
	
	/**
	 * load hotel from local file
	 * @param filename
	 * @return
	 */
	public static HashMap<String, Hotel> loadHotel(String filename){
		HashMap<String, Hotel> hotelMap = new HashMap<String, Hotel>();
		BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader(new File(filename)));
			String buff = null;
			while((buff = reader.readLine()) != null){
				String[] arr = buff.split("\t");
				String sid = arr[0];
				String uid = arr[1];
				
				double price = Double.parseDouble(arr[2]);
				double commentScore = Double.parseDouble(arr[3]);
				String hotelName = arr[4];
				
				Hotel hotel = new Hotel();
				hotel.setSid(sid);
				hotel.setUid(uid);
				hotel.setHotelName(hotelName);
				hotel.setCommentScore(commentScore);
				hotel.setPrice(price);
				hotelMap.put(sid, hotel);
			}
			reader.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return hotelMap;
	}
	
	/**
	 * export the hotel from database to local file
	 * @param filename
	 */
	public static void exportHotel(String filename){
		try {
			PrintWriter writer = new PrintWriter(new File(filename));
			HashMap<String, Hotel> hotelMap = getAllHotel();
			Iterator<String> iter = hotelMap.keySet().iterator();
			while (iter.hasNext()) {
				String key = (String) iter.next();
				Hotel hotel = hotelMap.get(key);
				String buff = hotel.getSid() + "\t";
				buff += hotel.getUid() + "\t";
				buff += hotel.getPrice() + "\t";
				buff += hotel.getCommentScore() +"\t";
				buff += hotel.getHotelName();
				writer.println(buff);
			}
			writer.flush();
			writer.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * select all local file from database in minimum price strategy
	 * @return
	 */
	public static HashMap<String, Hotel> getAllHotel(){
		//the minimum hotel price
		double minPrice = 80.0;
		HashMap<String, Hotel> result = new HashMap<String, Hotel>();
		String sql = "select a.* from t_baiduhotel as a, (select b.sid, min(b.price) as min_price from t_baiduhotel as b GROUP BY b.sid) as b where a.sid=b.sid and a.price=b.min_price";
		ResultSet set = DbUtil.executeQuery(sql, null);
		try {
			while(set.next()){
				String sid = set.getString("sid");
				String uid = set.getString("uid");
				String hotelName = set.getString("hotel_name");
				String hotelAddress = set.getString("hotel_address");
				String phone = set.getString("phone");
				String pic = set.getString("pic");
				double price = set.getDouble("price");
				double commentScore = set.getDouble("comment_score");
				price = (price < minPrice) ? minPrice : price;
				double lng = set.getDouble("lng");
				double lat = set.getDouble("lat");
				
				if(!result.containsKey(sid)){
					Hotel hotel = new Hotel();
					hotel.setSid(sid);
					hotel.setUid(uid);
					hotel.setHotelName(hotelName);
					hotel.setHotelAddress(hotelAddress);
					hotel.setPhone(phone);
					hotel.setPic(pic);
					hotel.setPrice(price);
					hotel.setCommentScore(commentScore);
					hotel.setLat(lat);
					hotel.setLng(lng);
					result.put(sid, hotel);
				}else{
					Hotel hotel = result.get(sid);
					//if a scenery has more than 1 hotels, choose the high comment_score one
					if(commentScore > hotel.getCommentScore()){
						hotel.setSid(sid);
						hotel.setUid(uid);
						hotel.setHotelName(hotelName);
						hotel.setHotelAddress(hotelAddress);
						hotel.setPhone(phone);
						hotel.setPic(pic);
						hotel.setPrice(price);
						hotel.setCommentScore(commentScore);
						hotel.setLat(lat);
						hotel.setLng(lng);
					}
				}
				
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally{
//			DbUtil.close();
		}
		return result;
	}
	
	private static void convertToLngLat(){
		String sql = "select * from t_baiduhotel as b";
		ResultSet set = DbUtil.executeQuery(sql, null);
		try {
			while(set.next()){
				String sid = set.getString("sid");
				String uid = set.getString("uid");
				double pointX = set.getDouble("point_x");
				double pointY = set.getDouble("point_y");
				double[] geo = MapUtil.mercator2lonLat(pointX, pointY);
				double lng = geo[0];
				double lat = geo[1];
				String sql2 = "update t_baiduhotel set lng=?, lat=? where uid=?";
				String[] params = {lng+"", lat+"", uid};
				DbUtil.executeUpdate(sql2, params);
				
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally{
			DbUtil.close();
		}
	}
	
	public static void main(String[] args){
		long begin = System.currentTimeMillis();
		
//		exportHotel("./hotel.txt");
//		loadHotel("./hotel.txt");
		convertToLngLat();
		
		long end = System.currentTimeMillis();
		long delay = end - begin;
		System.out.println("耗时：" + delay +"ms");
	}
	
}
