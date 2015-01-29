package aco.tour;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import aco.tour.ACO;
import util.HotelUtil;
import util.RouteUtil;
import util.SceneryUtil;
import model.Hotel;
import model.Route;
import model.Scenery;

public class Main {
	
	public static void main(String[] args) {
		testOne();
//		testMore();
	}
	
	public static void testOne(){
		System.out.println("begin");
		long beginT = System.currentTimeMillis();
		HashMap<String, Hotel> hotelMap = HotelUtil.getAllHotel();
		Scenery city = SceneryUtil.getCity("da666bc57594baeb76b3bcf0");
		ArrayList<Scenery> sceneryList = SceneryUtil.getSceneryList("da666bc57594baeb76b3bcf0");
		
		long beginM = Runtime.getRuntime().freeMemory();

		ACO aco = new ACO();
		aco.init(city, sceneryList, hotelMap, 100, 2.0, 3.0);
		ArrayList<Route> routeList = aco.run(1000);
//		aco.reportResult();
		
		System.out.println("-----split-------");
		int subLen = routeList.size();
		subLen = subLen > 20 ? 20 : subLen - 1;
		
		List<Route> topNList = routeList.subList(0, subLen);
		double hotness = RouteUtil.caclAvgHotness(topNList);
		
		long tmpDelay = System.currentTimeMillis() - beginT;
		long tmpMem = (beginM - Runtime.getRuntime().freeMemory()) / (1024 * 1024);
		System.out.println("热度：" + hotness + "  耗时：" + tmpDelay + "ms  内存：" + tmpMem + "M");
	}
	
	public static void testMore(){
		System.out.println("begin");
		
		HashMap<String, Hotel> hotelMap = HotelUtil.getAllHotel();
		Scenery city = SceneryUtil.getCity("da666bc57594baeb76b3bcf0");
		ArrayList<Scenery> sceneryList = SceneryUtil.getSceneryList("da666bc57594baeb76b3bcf0");
		
		int runGens = 10;
		double avgTime = 0.0;
		double avgMem = 0.0;
		double avgHotness = 0.0;
		
		for (int i = 0; i < runGens; i++) {
			long beginT = System.currentTimeMillis();
			Runtime.getRuntime().gc();
			long beginM = Runtime.getRuntime().freeMemory();
			ACO aco = new ACO();
			aco.init(city, sceneryList, hotelMap, 100, 2.0, 3.0);
			ArrayList<Route> routeList = aco.run(1000);
			
			int subLen = routeList.size();
			subLen = subLen > 20 ? 20 : subLen - 1;
			
			List<Route> topNList = routeList.subList(0, subLen);
			
			double hotness = RouteUtil.caclAvgHotness(topNList);
			avgHotness += hotness;
			
			long tmpDelay = System.currentTimeMillis() - beginT;
			long tmpMem = (beginM - Runtime.getRuntime().freeMemory()) / (1024 * 1024);
			
			avgTime += tmpDelay;
			avgMem += tmpMem ;
			System.out.println("热度：" + hotness + "  耗时：" + tmpDelay + "ms  内存：" + tmpMem + "M");
		}
		avgHotness = avgHotness / runGens;
		avgTime = avgTime / runGens;
		avgMem = avgMem / runGens;
		
		System.out.println("平均热度：" + avgHotness + "  平均耗时：" + avgTime + "ms  平均内存：" + avgMem + "M");
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
