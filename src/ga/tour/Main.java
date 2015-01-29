package ga.tour;

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

public class Main {
	

	public static void main(String[] args) throws Exception{
		
		HashMap<String, Hotel> hotelMap = HotelUtil.getAllHotel();
		Scenery city = SceneryUtil.getCity("da666bc57594baeb76b3bcf0");
		ArrayList<Scenery> sceneryList = SceneryUtil.getSceneryList("da666bc57594baeb76b3bcf0");
		
		//sort all the scenery according to viewCount
//		Collections.sort(sceneryList);
//		Collections.reverse(sceneryList);
		
		long begin = System.currentTimeMillis();
		GA ga = new GA(300, 1000, 0.9, 0.9);
		ga.init(city, sceneryList, hotelMap, 2.0, 3.0);
		
		ArrayList<Route> routeList = ga.run();
		
		System.out.println("-----split-------");
		List<Route> top30List = routeList.subList(0, 20);
		System.out.println("avg=" + RouteUtil.caclAvgHotness(top30List));
		
		System.out.println("总共：" + routeList.size() +"条路径");
		long end = System.currentTimeMillis();
		long time = (end - begin);
		System.out.println();
		System.out.println("耗时："+ time +" ms");
	}

}
