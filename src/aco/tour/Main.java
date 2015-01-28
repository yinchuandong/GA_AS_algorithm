package aco.tour;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;

import util.HotelUtil;
import util.SceneryUtil;
import model.Hotel;
import model.Scenery;

public class Main {
	
	public static void main(String[] args) {
		System.out.println("begin");
		long begin = System.currentTimeMillis();
//		ArrayList<Scenery> list = loadData("./guangzhou.txt");
		HashMap<String, Hotel> hotelMap = HotelUtil.getAllHotel();
		Scenery city = SceneryUtil.getCity("da666bc57594baeb76b3bcf0");
		ArrayList<Scenery> sceneryList = SceneryUtil.getSceneryList("da666bc57594baeb76b3bcf0");
		
		ACO aco = new ACO();
		aco.init(city, sceneryList, hotelMap, 100, 2.0, 3.0);
		aco.run(1000);
		aco.reportResult();
		
		long end = System.currentTimeMillis();
		long delay = end - begin;
		System.out.println("耗时：" + delay + "ms");
	}
	
	/**
	 * 从文件中读取景点数据
	 * @param filepath
	 * @return
	 */
	public static ArrayList<Scenery> loadData(String filepath){
		ArrayList<Scenery> list = new ArrayList<Scenery>();
		int count = 0;
		try {
			BufferedReader reader = new BufferedReader(new FileReader(new File(filepath)));
			String buff = null;
			while((buff = reader.readLine()) != null){
				String[] arr = buff.split(",");
				String cityName = arr[0];
				int viewCount = Integer.parseInt(arr[1]);
				double visitDay = Double.parseDouble(arr[2]);
				Scenery scene = new Scenery();
				scene.setSname(cityName);
				scene.setViewCount(viewCount);
				scene.setVisitDay(visitDay);
				list.add(scene);
				if(count == 20){
//					break;
				}
				count ++;
			}
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}
	
}
