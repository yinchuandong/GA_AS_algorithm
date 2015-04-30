package ga.tour.optimized;

import ga.tour.optimized.MGGA.EncodedRoute;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import util.AppUtil;
import model.Route;
import model.Scenery;

public class RouteDecoder {

	/**
	 * 游玩天数的上限
	 */
	private double maxDay = 3;
	/**
	 * 游玩天数的下限
	 */
	private double minDay = 2.0;
	
	/**
	 * 景点属于的城市的对象
	 */
	private Scenery city;
	
	private ArrayList<Scenery> sceneryList;
	
	private ArrayList<EncodedRoute> globalBestRoute;
	
	public RouteDecoder(){
		
	}
	
	public void init(Scenery city, ArrayList<Scenery> sceneryList, ArrayList<EncodedRoute> globalBestRoute, double minDay, double maxDay){
		this.city = city;
		this.sceneryList = sceneryList;
		this.globalBestRoute = globalBestRoute;
		this.maxDay = maxDay;
		this.minDay = minDay;
	}
	
	
	/**
	 * 过滤掉相似的路线
	 * @param threshold 余弦相似性阀值，越大过滤的强度越低，建议0.6-0.8
	 */
	public void filterRoute(double threshold){
		ArrayList<EncodedRoute> routes1 = globalBestRoute;
		ArrayList<EncodedRoute> routes2 = (ArrayList<EncodedRoute>) globalBestRoute.clone();
		
		double sim = 0.0;
		for(int i = 0; i < routes1.size() - 1; i++){
			EncodedRoute r1 = routes1.get(i);
			for (int j = i+1; j < routes2.size(); j++) {
				EncodedRoute r2 = routes2.get(j);
				sim = calcSim(r1.chromosome, r2.chromosome);
				if(sim >= threshold){
					if(r2.fitness > r1.fitness){
						routes1.set(i, r2);
					}
					routes1.remove(j);
					routes2.remove(j);
					j--;
				}
			}
		}
	}
	
	private static double calcSim(int[] r1, int[] r2){
		if(r1.length != r2.length){
			throw new IllegalArgumentException("r1.length != r2.length, invalid input vector");
		}
		double sim = 0.0, denomR1 = 0.0, denomR2 = 0.0;
	
		for (int i = 0; i < r1.length; i++) {
			sim += r1[i]*r2[i];
			denomR1 += r1[i] * r1[i];
			denomR2 += r2[i] * r2[i];
		}
		sim = sim / (Math.sqrt(denomR1) * Math.sqrt(denomR2));
		return sim;
	}
	
	
	/**
	 * decode the chromosome and transform into route model 
	 * @return
	 */
	public  ArrayList<Route> decodeChromosome(){
		System.out.println("gasecnery 最后种群");
		HashMap<String, Integer> routeMap = new HashMap<String, Integer>();
		ArrayList<Route> routeList = new ArrayList<Route>();
		
		//获得城市对象
		for (EncodedRoute encodedRoute: this.globalBestRoute) {
			double ticketPrice = 0.0;
			double hotness = encodedRoute.fitness;
			double days = 0.0;
			int viewCount = 0;
			String tmpR = "";
			//获得景点列表
			ArrayList<Scenery> sceneList = new ArrayList<Scenery>();
			for (int j = 0; j < encodedRoute.chromosome.length; j++) {
				if (encodedRoute.chromosome[j] == 1) {
					Scenery scene = sceneryList.get(j);
					ticketPrice += scene.getPrice();
					viewCount += scene.getViewCount();
					days += scene.getVisitDay();
					tmpR += scene.getSid();
					sceneList.add(scene);
				}
			}
			//viewcount为所有景点的平均值
			viewCount = viewCount / sceneList.size(); 
			
			//ensure the visitDays is between minDay and maxDay
			if (days <= minDay || days > maxDay) {
				continue;
			}
			
			String uid = AppUtil.md5(tmpR + this.maxDay);
			String sid = city.getSid();
			String ambiguitySname = city.getAmbiguitySname();
			String sname = city.getSname();
			String surl = city.getSurl();
			
			if (!routeMap.containsKey(uid) && sceneList.size() >= 2) {
				Route route = new Route();
				route.setUid(uid);
				route.setSid(sid);
				route.setAmbiguitySname(ambiguitySname);
				route.setSname(sname);
				route.setSurl(surl);
				route.setMaxDay(maxDay);
				route.setMinDay(minDay);
				route.setVisitDay(days);
				route.setHotness(hotness);
				route.setViewCount(viewCount);
				route.setHotelPrice(encodedRoute.hotelPrice);
				route.setSceneTicket(ticketPrice);
				route.setSumPrice(encodedRoute.hotelPrice + ticketPrice);
				route.setSceneryList(sceneList);
				routeMap.put(uid, 1);
				routeList.add(route);
			}
		}
		
		//sort the routeList
		Collections.sort(routeList, new Comparator<Route>() {

			@Override
			public int compare(Route o1, Route o2) {
				if(o1.getHotness() < o2.getHotness()){
					return 1;
				}else{
					return -1;
				}
			}
		});
		
		return routeList;
	}
	
	public void report(ArrayList<Route> routeList){
		System.out.println("last generation------------------------");
		
		for (int i = 0; i < routeList.size(); i++) {
			Route route = routeList.get(i);
			ArrayList<Scenery> sceneList = route.getSceneryList();
			System.out.print((i+1) + "/" + routeList.size()+" ");
			System.out.print(sceneList.size() + "个景点");
			System.out.print(" 天数：" + route.getVisitDay());
			System.out.print(" 门票：" + route.getSceneTicket() + "/酒店：" + route.getHotelPrice());
			System.out.println(" 适度：" + route.getHotness());
			for (Scenery scenery : sceneList) {
				System.out.print(scenery.getSname() + "-" + scenery.getVisitDay() + ",");
			}
			System.out.println();
			System.out.println();
		}
	}
}
