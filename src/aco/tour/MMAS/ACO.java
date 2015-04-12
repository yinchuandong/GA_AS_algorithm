package aco.tour.MMAS;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import util.AppUtil;
import model.Hotel;
import model.Route;
import model.Scenery;

public class ACO {

	/**
	 * 城市对象
	 */
	Scenery city;
	/**
	 * 酒店的map
	 */
	HashMap<String, Hotel> hotelMap;
	/**
	 * 景点的列表
	 */
	ArrayList<Scenery> sceneList;
	
	double minDay;
	/**
	 * 游玩的天数 默认为3
	 */
	double maxDay;
	/**
	 * 蚂蚁对象数组
	 */
	Ant[] ants;
	/**
	 * 蚂蚁的数量
	 */
	int antCount;
	/**
	 * ant-cycle模型中的Q,信息素的总量
	 */
	private double Q = 100000000.0;
	/**
	 * 景点的信息素 公式中的tao
	 */
	double[] pheromone;
	/**
	 * 景点的热度
	 */
	double[] hotness;
	/**
	 * 景点的数量
	 */
	int sceneCount;
	/**
	 * 最好的蚂蚁id
	 */
	int bestAntId = 0;
	/**
	 * 最优蚂蚁走过的路线
	 */
	int[] bestTour;
	/**
	 * 最优蚂蚁走过的长度
	 */
	double bestLength;

	public ACO() {
		this.maxDay = 3.0;
	}

	/**
	 * 初始化蚁群
	 * 
	 * @param sceneList
	 *            景点的列表
	 * @param antCount
	 *            蚂蚁的数量
	 * @param maxDay
	 *            游玩的天数
	 */
	public void init(Scenery city, ArrayList<Scenery> sceneList, HashMap<String, Hotel> hotelMap, int antCount, double minDay, double maxDay) {
		this.city = city;
		this.sceneList = sceneList;
		this.hotelMap = hotelMap;
		this.antCount = antCount;
		this.minDay = minDay;
		this.maxDay = maxDay;
		
		ants = new Ant[antCount];
		sceneCount = sceneList.size();
		// 初始化信息素 默认为1
		pheromone = new double[sceneCount];
		hotness = new double[sceneCount];
		
		//select max viewCount of all scenery
		this.Q = sceneList.get(0).getViewCount();
		for (int i = 0; i < sceneCount; i++) {
			double tmpViewCount = sceneList.get(0).getViewCount();
			if(tmpViewCount > this.Q){
				this.Q = tmpViewCount;
			}
		}
		this.Q *= 20;
		
		//initialize the pheromone and hotness
		for (int i = 0; i < sceneCount; i++) {
			pheromone[i] = 0.8;
			hotness[i] = (double) sceneList.get(i).getViewCount() / this.Q;
		}
		
		bestLength = Integer.MIN_VALUE;
		bestTour = new int[sceneCount];
		for (int i = 0; i < antCount; i++) {
			ants[i] = new Ant();
			ants[i].init(city, sceneList, hotelMap, this.Q, minDay, maxDay);
		}
	}

	/**
	 * 蚁群算法的运行入口
	 * 
	 * @param maxgen
	 *            运行最大的代数
	 */
	public ArrayList<Route> run(int maxgen) {
		//save the local ant tour route
		ArrayList<int[]> antTourList  = new ArrayList<int[]>();
		//save the local scene hotness
		ArrayList<Double> hotnessList = new ArrayList<Double>();
		for (int gen = 0; gen < maxgen; gen++) {
			// 每一只蚂蚁的移动过程
			for (int i = 0; i < antCount; i++) {
				// 对该蚂蚁进行城市路线选择
				ants[i].calcProb(pheromone, hotness);
				for (int j = 1; j < sceneCount; j++) {
					// select需要增加一个返回值
					if (!ants[i].selectNextCity(j)) {
						break;
					}
				}
				// 计算该蚂蚁爬过的路线总长度
				ants[i].calcTourLength(sceneList);
				// 判断是否为最优路线
				if (ants[i].getLength() > bestLength) {
					// 保存最优代
					bestAntId = i;
					bestLength = ants[i].getLength();
					int[] tmpTour = new int[sceneList.size()];
					
//					System.out.println("第" + gen + "代, 蚂蚁" + i + "，发现新的解为："
//							+ bestLength);
					for (int j = 0; j < sceneCount; j++) {
						bestTour[j] = ants[i].getTour()[j];
						tmpTour[j] = bestTour[j];
						if (bestTour[j] != -1) {
							System.out.print(sceneList.get(bestTour[j])
									.getSname() + " ");
						}
					}
					antTourList.add(bestTour.clone());
					hotnessList.add(bestLength);
					
					System.out.println();
				}
			}
			// 更新信息素
			updatePheromone();
			// 蚂蚁重新初始化
			for (int i = 0; i < antCount; i++) {
				ants[i].init(city, sceneList, hotelMap, this.Q, minDay, maxDay);
			}
		}
		Arrays.sort(this.pheromone);
		System.out.println("end");
		
		return this.decodeRoute(antTourList, hotnessList);
	}

	/**
	 * 更新信息素,使用ant-cycle模型 <br/>
	 * 公式1: T_ij(t+1) = (1-r)*T_ij(t) + delta_T_ij(t) <br/>
	 * 公式2: delta_T_ij(t) = Q/L_k Q为常数，L_k为蚂蚁走过的总长度
	 */
	private void updatePheromone() {
		double rho = 0.01;
		// 信息素的衰减
		for (int i = 0; i < sceneCount; i++) {
			pheromone[i] *= (1 - rho);
		}
		// 普通蚁群算法，所有蚂蚁都留信息素，被访问过的城市信息素增加
//		for (int i = 0; i < antCount; i++) {
//			for (int j = 0; j < cityCount; j++) {
//				int curId = ants[i].getTour()[j];
//				if (curId != -1) {
//					// 如果改城市被访问过
//					pheromone[curId] += ants[i].getLength() / Q;
//				} else {
//					return;
//				}
//			}
//		}

		// 最大最小蚂蚁, 只有最优化蚂蚁才留信息素
		for (int i = 0; i < sceneCount; i++) {
			int curId = bestTour[i];
			if (curId != -1) {
				// 如果改城市被访问过
				pheromone[curId] += ants[bestAntId].getLength() / Q;
				if(pheromone[curId] <= 0.0001){
					pheromone[curId] = 0.0001;
				}
			} else {
				return;
			}
		}
	}
	
	
	public ArrayList<Route> decodeRoute(ArrayList<int[]> antTourList, ArrayList<Double> hotnessList){
		int len = antTourList.size();
		HashMap<String, Integer> routeMap = new HashMap<String, Integer>();
		ArrayList<Route> routeList = new ArrayList<Route>();

		for(int i = 0; i < len; i++){
			int[] tmpTour = antTourList.get(i);
			double ticketPrice = 0.0;
			double hotness = hotnessList.get(i); //need to save ant object
			double days = 0.0;
			int viewCount = 0;
			String tmpR = "";
			Route route = new Route();
			ArrayList<Scenery> sList = new ArrayList<Scenery>();
			for (int j = 0; j < tmpTour.length; j++) {
				if(tmpTour[j] == -1){
					break;
				}
				Scenery tmpScene = this.sceneList.get(tmpTour[j]);
				ticketPrice += tmpScene.getPrice();
				days += tmpScene.getVisitDay();
				viewCount += tmpScene.getViewCount();
				tmpR += tmpScene.getSid();
				sList.add(tmpScene);
			}
			
			String uid = AppUtil.md5(tmpR + this.maxDay);
			String sid = city.getSid();
			String ambiguitySname = city.getAmbiguitySname();
			String sname = city.getSname();
			String surl = city.getSurl();
			
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
			route.setSceneTicket(ticketPrice);
			route.setSceneryList(sList);
			if (!routeMap.containsKey(uid) && sList.size() >= 2) {
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
		
//		for (Route route : routeList) {
//			ArrayList<Scenery> sceneList = route.getSceneryList();
//			for (Scenery scenery : sceneList) {
//				System.out.print(scenery.getSname() + " " + scenery.getVisitDay() + ", ");
//			}
//			System.out.print("----:" + route.getVisitDay());
//			System.out.println();
//		}
		
		System.out.println("decode end");
		return routeList;
	}
	
	

	/**
	 * 打印路径长度
	 */
	public void reportResult() {
		System.out.println("最优路径长度是" + bestLength);
		for (int j = 0; j < sceneCount; j++) {
			if (bestTour[j] != -1) {
				System.out
						.print(sceneList.get(bestTour[j]).getSname() + " ");
			} else {
				return;
			}
		}
	}
}
