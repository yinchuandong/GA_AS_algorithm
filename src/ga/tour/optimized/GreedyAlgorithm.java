package ga.tour.optimized;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import util.AppUtil;
import util.SceneryUtil;
import model.Scenery;


/**
 * 贪心算法求出第一代解空间
 * @author yinchuandong
 *
 */
public class GreedyAlgorithm {

	/**
	 * 城市的列表
	 */
	private ArrayList<Scenery> sceneryList;
	
	/**
	 * 最终的路线列表
	 */
	private HashMap<String, Routes> routesMap;
	/**
	 * 天数上限
	 */
	private double maxDay = 3.0;
	/**
	 * 天数下限
	 */
	private double downDay = 2.0;
	
	private int scale = 0;
	
	private int[][] initPopulation;
	
	
	public GreedyAlgorithm(double downDay, double maxDay, int scale, ArrayList<Scenery> sceneryList){
		this.sceneryList = sceneryList;
		this.scale = scale;
		routesMap = new HashMap<String, Routes>();
		initPopulation = new int[scale][sceneryList.size()];
		
		this.init();
	}
	
	private void init(){
		this.calculate();
		this.code();
	}
	
	/**
	 * 计算出符合天数的第一代
	 */
	private void calculate(){
		int len = sceneryList.size();
		int spanLen = len > 30 ? 30 : len;
		//控制跨度
		for (int span = 1; span < spanLen; span++) {
			for(int i=0; i < len; i++){
				double tmpDays = 0.0;
				int tmpViewCount = 0;
				String tmpRoute = "";
				//一条路径中前一个景点
				Scenery lastScene = sceneryList.get(0);
				double distance = 0.0;
				for (int j = i; j < len; j += span) {
					Scenery curScene = sceneryList.get(j);
					tmpDays += curScene.getVisitDay();
					distance = SceneryUtil.calcDistance(lastScene, curScene);
					if (tmpDays > maxDay || distance > 200000.0) {
						tmpDays -= curScene.getVisitDay();
						continue;
					}
					tmpRoute += j + ",";
					tmpViewCount += curScene.getViewCount();
					if (tmpDays > downDay && tmpDays <= maxDay) {
						tmpRoute = tmpRoute.substring(0, tmpRoute.length() - 1);
						Routes routes = new Routes();
						routes.setRoute(tmpRoute);
						routes.setDays(tmpDays);
						routes.setViewCount(tmpViewCount);
						String key = AppUtil.md5(tmpRoute);
						if (!routesMap.containsKey(key)) {
							routesMap.put(key, routes);
						}
						tmpRoute = "";
						tmpDays = 0;
						tmpViewCount = 0;
					}
				}
			}
		}
	}
	
	
	
	/**
	 * 保存运算的结果到文件中
	 * 编码为 0,1,0,1,1这种类型，1代表改下标对应的城市被选中,0代表不被选中
	 */
	private void code(){
		Iterator<String> iter = routesMap.keySet().iterator();
		int k = 0;
		ArrayList<Routes> routeList = new ArrayList<Routes>();
		while(iter.hasNext()){
			String key = (String) iter.next();
			Routes model = routesMap.get(key);
			routeList.add(model);
		}
		Collections.sort(routeList);
		ArrayList<Routes> sortedList = new ArrayList<Routes>();
		if (routeList.size() > scale) {
			for (int i = 0; i < scale; i++) {
				sortedList.add(routeList.get(i));
			}
		}else{
			int len = routeList.size();
			if (len != 0) {
				for (int i = 0; i < scale; i++) {
					sortedList.add(routeList.get(i % len));
				}
			}
		}
		
		for (Routes model : sortedList) {
			String routes = model.getRoute();
			String[] arr = routes.split(",");
			
			String codeStr = "";
			for (int i = 0; i < sceneryList.size(); i++) {
				boolean isFound = false;
				for (int j = 0; j < arr.length; j++) {
					if (i == Integer.parseInt(arr[j])) {
						isFound = true;
						break;
					}else{
						isFound = false;
					}
				}
				if (isFound) {
					codeStr += "1,";
					initPopulation[k][i] = 1;
				}else{
					codeStr += "0,";
					initPopulation[k][i] = 0;
				}
			}
			k++;
//			codeStr = codeStr.substring(0, codeStr.length() - 1) + "\r\n";
//			System.out.print(codeStr);
		}
		
		
	}
	
	
	/**
	 * 获得初始化好的种群
	 * @return
	 */
	public int[][] getInitPopulation() {
		return initPopulation;
	}
	
	/**
	 * 使用贪心策略优化适度较差的染色体
	 * @param chromosome
	 * @return
	 */
	public int[] optimize(int[] chromosome){
		int len = chromosome.length;
		int[] optChromo = new int[len];
		//排序之前的景点对应的id
		HashMap<String, Integer> orderMap = new HashMap<String, Integer>();
		ArrayList<Scenery> unAddedList = new ArrayList<Scenery>();
		double curAddedDays = 0.0;
		for (int i = 0; i < len; i++) {
			if(chromosome[i] == 1){
				curAddedDays += sceneryList.get(i).getVisitDay();
				optChromo[i] = 1;
			}else{
				optChromo[i] = 0;
				unAddedList.add(sceneryList.get(i));
				orderMap.put(sceneryList.get(i).getSid(), i);
			}
		}
		//sorted the scene not added to the chromosome
		Collections.sort(unAddedList);
		//select the hotter scenery to add in chromosome
		for (int i = unAddedList.size() - 1; i >= 0; i--) {
			if(curAddedDays > maxDay){
				break;
			}
			Scenery s = unAddedList.get(i);
			int originId = orderMap.get(s.getSid());
			optChromo[originId] = 1;
			curAddedDays += s.getVisitDay();
		}
		return optChromo;
	}
	
	public class Routes implements Comparable<Routes>{
		private String route;
		private int viewCount;
		private double days;
		public String getRoute() {
			return route;
		}
		public void setRoute(String route) {
			this.route = route;
		}
		public int getViewCount() {
			return viewCount;
		}
		public void setViewCount(int viewCount) {
			this.viewCount = viewCount;
		}
		public double getDays() {
			return days;
		}
		public void setDays(double days) {
			this.days = days;
		}
		@Override
		public int compareTo(Routes o) {
			if (this.viewCount > o.getViewCount()) {
				return -1;
			}else{
				return 1;
			}
		}
		
	}
	
	public static void test(){
		double x1 = 12616984.063376;
		double y1 = 2616239.6646102;
		double x2 = 12622859.013427;
		double y2 = 2608180.9771793;
		double distance = Math.sqrt((x1 - x2)*(x1 - x2) + (y1 - y2)*(y1 - y2));
		System.out.println(distance);
	}

	public static void main(String[] args) throws IOException{
		ArrayList<Scenery> sceneryList = SceneryUtil.getSceneryListById("622bc401f1153f0fd41f74dd");
//		ArrayList<Scenery> sceneryList = SceneryUtil.getSceneryList("da666bc57594baeb76b3bcf0");
		GreedyAlgorithm greedy = new GreedyAlgorithm(2.0, 3.0, 300, sceneryList);
		int[][] population =  greedy.getInitPopulation();
		int[] optChromo = greedy.optimize(population[0]);
//		test();
		System.out.println();
	}
}
