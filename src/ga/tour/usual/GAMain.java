package ga.tour.usual;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import model.Hotel;
import model.Route;
import model.Scenery;
import util.HotelUtil;
import util.RouteUtil;
import util.SceneryUtil;

public class GAMain {
	

	public static void main(String[] args) throws Exception{
//		testOne();
		testMore();
	}
	
	public static void testOne() throws Exception{
		System.out.println("begin");
		long beginT = System.currentTimeMillis();
		long beginM = Runtime.getRuntime().freeMemory();
		
		HashMap<String, Hotel> hotelMap = HotelUtil.getAllHotel();
		Scenery city = SceneryUtil.getCity("da666bc57594baeb76b3bcf0");
		ArrayList<Scenery> sceneryList = SceneryUtil.getSceneryList("da666bc57594baeb76b3bcf0");
		//sort all the scenery according to viewCount
//		Collections.sort(sceneryList);
//		Collections.reverse(sceneryList);

		GA ga = new GA(300, 1000, 0.9, 0.9);
		ga.init(city, sceneryList, hotelMap, 2.0, 3.0);
		ArrayList<Route> routeList = ga.run();
		
		System.out.println("-----split-------");
		int subLen = routeList.size();
		subLen = subLen > 20 ? 20 : subLen - 1;
		
		List<Route> topNList = routeList.subList(0, subLen);
		double hotness = RouteUtil.caclAvgHotness(topNList);
		
		long tmpDelay = System.currentTimeMillis() - beginT;
		long tmpMem = (beginM - Runtime.getRuntime().freeMemory()) / (1024 * 1024);
		System.out.println("热度：" + hotness + "  耗时：" + tmpDelay + "ms  内存：" + tmpMem + "M");
	}
	
	public static void testMore() throws Exception{
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
			long beginM = Runtime.getRuntime().freeMemory();
			
			GA ga = new GA(500, 1000, 0.9, 0.9);
			ga.init(city, sceneryList, hotelMap, 2.0, 3.0);
			ArrayList<Route> routeList = ga.run();
			
			int subLen = routeList.size();
			if(subLen == 0){
				continue;
			}
			subLen = subLen > 10 ? 10 : subLen - 1;
			
			List<Route> topNList = routeList.subList(0, subLen);
			
			double hotness = RouteUtil.caclAvgHotness(topNList);
			avgHotness += hotness;
			if(hotness < 100){
				ga.reportResult();
			}
			
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

}
