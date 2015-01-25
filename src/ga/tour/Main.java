package ga.tour;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import model.Hotel;
import model.Route;
import model.Scenery;
import util.HotelUtil;
import util.SceneryUtil;

public class Main {
	

	public static void main(String[] args) throws Exception{
		
		HashMap<String, Hotel> hotelMap = HotelUtil.getAllHotel();
		ArrayList<Scenery> sceneryList = SceneryUtil.getSceneryList("da666bc57594baeb76b3bcf0");
		
		Collections.sort(sceneryList);
		Collections.reverse(sceneryList);
		
		long begin = System.currentTimeMillis();
		GA ga = new GA(300, 1000, 0.8, 0.9);
		ga.init("da666bc57594baeb76b3bcf0", sceneryList, 2.0, 3.0, hotelMap);
//		ga.init("622bc401f1153f0fd41f74dd", sceneryList, 2.0, 3.0, hotelMap);
//		ga.init("1c41ec5be32fd14cfbe36df6", sceneryList, 2.0, 3.0, hotelMap);
		
		ArrayList<Route> routeList = ga.solve();
		
//		System.out.println("总共：" + routeList.size() +"条路径");
		long end = System.currentTimeMillis();
		long time = (end - begin);
		System.out.println();
		System.out.println("耗时："+ time +" ms");
	}

}
