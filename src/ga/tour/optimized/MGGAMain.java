package ga.tour.optimized;

import ga.tour.optimized.MGGA;
import ga.tour.optimized.MGGA.EncodedRoute;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import model.Hotel;
import model.Route;
import model.Scenery;
import util.HotelUtil;
import util.RouteUtil;
import util.SceneryUtil;
import util.AppUtil;

public class MGGAMain {
	

	public static void main(String[] args) throws Exception{
		HashMap<String, Hotel> hotelMap = HotelUtil.getAllHotel();
		double minDay = 2.0;
		double maxDay = 3.0;
		calcCity("da666bc57594baeb76b3bcf0", hotelMap, minDay, maxDay);
	}
	
	/**
	 * 计算一个城市
	 * @param cityId
	 * @param hotelMap
	 * @param minDay
	 * @param maxDay
	 * @throws Exception
	 */
	public static void calcCity(String cityId, HashMap<String, Hotel> hotelMap, double minDay, double maxDay) throws Exception{
		System.out.println("begin");
		long beginT = System.currentTimeMillis();
		
		
		Scenery city = SceneryUtil.getCityById(cityId);
		ArrayList<Scenery> sceneryList = SceneryUtil.getSceneryListById(cityId);
		Runtime.getRuntime().gc();
		long beginM = Runtime.getRuntime().totalMemory();

		ArrayList<EncodedRoute> encodedRoutes = new ArrayList<EncodedRoute>();
		
		//step1:运行混合遗传算法
		MGGA ga = new MGGA(300, 1000, 0.9, 0.9);
		for (int i = 0; i < 3; i++) {
			ga.init(city, sceneryList, hotelMap, minDay, maxDay);
			ga.run();
			encodedRoutes.addAll(ga.getEndecodedRoute());
		}
		
		//step2:对混合遗传算法的结果进行解码，并且过滤掉相似路线
		RouteDecoder decoder = new RouteDecoder();
		decoder.init(city, sceneryList, encodedRoutes, minDay, maxDay);
		decoder.filterRoute(0.7);
		ArrayList<Route> routeList = decoder.decodeChromosome();
//		decoder.filterRoute(0.7);
//		routeList = decoder.decodeChromosome();
		
		decoder.report(routeList);
		
		//step3:对游玩景点进行排序
		RouteSort sort = new RouteSort(30, 100, 0.8, 0.9);
		for (Route route : routeList) {
			ArrayList<Scenery> tmpSceneList = route.getSceneryList();
			sort.init(tmpSceneList);
			tmpSceneList = sort.run();
			route.setSceneryList(tmpSceneList);
		}
		
		decoder.report(routeList);
		
		saveRoutes(routeList, "./routes");
		
		System.out.println("-----split-------");
		int subLen = routeList.size();
		subLen = subLen > 20 ? 20 : subLen - 1;
		
		List<Route> topNList = routeList.subList(0, subLen);
		double hotness = RouteUtil.caclAvgHotness(topNList);
		
		long tmpDelay = System.currentTimeMillis() - beginT;
		long tmpMem = (beginM - Runtime.getRuntime().freeMemory()) / (1024 * 1024);
		System.out.println("热度：" + hotness + "  耗时：" + tmpDelay + "ms  内存：" + tmpMem + "M");
	}

	
	/**
	 * 将计算结果保存到文件夹中
	 * @param routeList
	 * @param dirPath
	 */
	public static void saveRoutes(ArrayList<Route> routeList, String dirPath){
		for (int i = 0; i < routeList.size(); i++) {
			Route route  = routeList.get(i);
			JSONObject rootObj = JSONObject.fromObject(route);
			//
			rootObj.put("arrange", arrangeRoute(route.getSceneryList(), route.getMaxDay()));
			String filename = (int)route.getMaxDay() + "_" + i +"_" + route.getUid() + ".json";
			File file = new File(dirPath + "/" + filename);
			if(!file.getParentFile().exists()){
				file.getParentFile().mkdirs();
			}
			AppUtil.exportFile(file, rootObj.toString());
					
		}
	}
	/**
	 * 获得整个路程的安排，将整个路程切分为按天计算
	 * @param sceneList
	 * @param maxDay 几天游
	 */
	private static JSONArray arrangeRoute(ArrayList<Scenery> sceneList, double maxDay){
		JSONArray allDaysArr = JSONArray.fromObject("[]");
		double tmpDays = 0.0;
		int curDay = 1;
		ArrayList<Scenery> tmpList = new ArrayList<Scenery>();
		for (Scenery scenery : sceneList) {
			tmpDays += scenery.getVisitDay();
			tmpList.add(scenery);
			if (tmpDays >= 1.0 || maxDay <= 1.0) {
				tmpDays -= 1.0;
				JSONObject daysObj = JSONObject.fromObject("{}");
				JSONArray daysArr = JSONArray.fromObject(tmpList);
				daysObj.put("list", daysArr);
				daysObj.put("curDay", "第" + curDay + "天");
				allDaysArr.add(daysObj);
				
				tmpList.clear();
				curDay ++;
				//一天玩不完，第二天继续玩
				if (tmpDays >= 0.3) {
					tmpList.add(scenery);
				}
			}
		}
		return allDaysArr;
	}

}
