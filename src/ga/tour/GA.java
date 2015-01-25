package ga.tour;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.rmi.server.UID;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;

import javax.rmi.CORBA.Tie;
import javax.xml.crypto.Data;

import model.Hotel;
import model.Route;
import model.Scenery;

import org.omg.PortableInterceptor.HOLDING;

import util.AppUtil;


public class GA {
	
	/**
	 * 种群规模
	 */
	private int scale;
	
	/**
	 * 城市数量
	 */
	private int sceneryNum;
	
	/**
	 * 城市列表
	 */
	private ArrayList<Scenery> sceneryList;
	
	/**
	 * 最大运行代数
	 */
	private int maxGen;
	
	/**
	 * 当前运行代数
	 */
	private int curGen;
	
	/**
	 * 交叉概率
	 */
	private double pc;
	
	/**
	 * 变异概率
	 */
	private double pm;
	
	/**
	 * 种群中个体的累计概率
	 */
	private double[] pi;
	
	/**
	 *  初始种群，父代种群，行数表示种群规模，一行代表一个个体，即染色体，列表示染色体基因片段
	 */
	private int[][] oldPopulation;
	
	/**
	 * 新的种群，子代种群
	 */
	private int[][] newPopulation;
	
	/**
	 * 种群适应度，表示种群中各个个体的适应度
	 */
	private double[] fitness;
	
	/**
	 * 每一条路径对应的酒店的sid
	 */
	private String[] recommendHotel;
	
	/**
	 * 最佳出现代数
	 */
	private int bestGen;
	
	/**
	 * 最佳长度
	 */
	private double bestLen;
	
	/**
	 * 最佳路径
	 */
	private int[] bestRoute;
	
	private String bestHotelIds;
	
	/**
	 * 随机数
	 */
	private Random random;
	
	/**
	 * 酒店列表
	 */
	private HashMap<String, Hotel> hotelMap;
	
	/**
	 * 游玩天数的上限
	 */
	private double maxDay = 3;
	/**
	 * 游玩天数的下限
	 */
	private double minDay = 2.0;
	
	/**
	 * 景点属于的城市的id
	 */
	private String cityId;
	
	/**
	 * 
	 * @param scale 种群规模
	 * @param maxGen 运行代数
	 * @param pc 交叉概率
	 * @param pm 变异概率
	 */
	public GA(int scale, int maxGen, double pc, double pm){
		this.scale = scale;
		this.maxGen = maxGen;
		this.pc = pc;
		this.pm = pm;
		this.sceneryList = new ArrayList<Scenery>();
	}
	

	/**
	 * 生成一个0-65535之间的随机数
	 * @return
	 */
	private int getRandomNum(){
		return this.random.nextInt(65535);
	}
	
	/**
	 * 初始化算法，从file中加载数据文件
	 * @param cityId 城市的sid
	 * @param downDay 天数下限，开区间
	 * @param upDay 天数上限，闭区间
	 * @param hotelMap 酒店的信息
	 * @throws Exception 
	 */
	public void init(String cityId, ArrayList<Scenery> sceneryList, double downDay, double upDay, HashMap<String, Hotel> hotelMap) throws Exception{
		this.cityId = cityId;
		this.sceneryList = sceneryList;
		this.minDay = downDay;
		this.maxDay = upDay;
		this.hotelMap = hotelMap;
		
		this.sceneryNum = this.sceneryList.size();
		if (sceneryNum < 2) {
			throw new Exception("景点的个数为" + sceneryNum +"，不符合，其id为：" + cityId);
		}
		
		this.bestLen = Integer.MIN_VALUE;
		this.bestGen = 0;
		this.bestRoute = new int[sceneryNum];
		this.bestHotelIds = "";
		this.curGen = 0;
		
		this.newPopulation = new int[scale][sceneryNum];
		this.oldPopulation = new int[scale][sceneryNum];
		this.fitness = new double[scale];
		this.pi = new double[scale];
		
		this.recommendHotel = new String[scale];
		
		this.random = new Random(System.currentTimeMillis());
	}
	
	/**
	 * 初始化种群
	 * 以01001的形式编码染色体
	 */
	private void initGroup(){
		GreedyAlgorithm util = new GreedyAlgorithm(minDay, maxDay, scale, sceneryList);
		oldPopulation = util.getInitPopulation();
	}
	
	/**
	 * 评价函数，用于计算适度
	 * @param index 当前染色体的下标
	 * @param chromosome 染色体，包含：城市1,城市2...城市n
	 * @return the total distance of all chromosome's cities;
	 */
	private double evaluate(int index, int[] chromosome){
		double ticketPrice = 0.0;//门票
		double hotness = 0.0;//热度
		double days = 0.0;
		//酒店当前染色体对应的酒店信息
		ArrayList<Hotel> hotels = new ArrayList<Hotel>();
		for (int i = 0; i < chromosome.length; i++) {
			if (chromosome[i] == 1) {
				Scenery scene = sceneryList.get(i);
				ticketPrice +=  scene.getPrice();
				hotness += (double)scene.getViewCount();
				days += scene.getVisitDay();
				//获得该景点的酒店信息
				Hotel hotel = hotelMap.get(scene.getSid());
				if (hotel != null) {
					hotels.add(hotel);
				}
			}
		}
		
		if (days <= minDay || days > maxDay) {
			recommendHotel[index] = "";
			return 0.00000000000001;
		}
		
		Collections.sort(hotels);
		double hotelPrice = 0.0;
		/* 判断酒店的个数是否大于需要入住的天数
		 * 如果大于则按照入住的天数计算价格
		 * 如果小于则计算所有酒店的价格，剩余天数就按照最低价格计算
		 */
		String hotelIds = "";//保存推荐的hotelId
		int len = Math.min(hotels.size(), (int)minDay);
		if (len != 0) {
			for (int i = 0; i < len; i++) {
				hotelPrice += hotels.get(i).getPrice();
				hotelIds += hotels.get(i).getSid() + ",";
			}
			int span = (int)(minDay - hotels.size());
			for (int i = 0; i < span; i++) {
				hotelPrice += hotels.get(0).getPrice();
				hotelIds += hotels.get(0).getSid() + ",";
			}
		}else{
			//当该景点没有酒店的时候，默认80块
			for (int i = 0; i < (int)minDay; i++) {
				hotelPrice += 80.0;
			}
		}
		
		if (!hotelIds.equals("")) {
			hotelIds = hotelIds.substring(0, hotelIds.length() - 1);
		}
		recommendHotel[index] = hotelIds;
		
		double price = hotelPrice + ticketPrice;
		double fitness = (10000.0 / (price + 10.0)) * 0.1 + Math.pow(hotness, 1.0/3.0) * 0.9;
//		System.out.println("fiteness:" + fitness);
		return fitness;
	}
	
	/**
	 * 计算种群中各个个体的累积概率，
	 * 前提是已经计算出各个个体的适应度fitness[max]，
	 * 作为赌轮选择策略一部分，Pi[max]
	 */
	private void countRate(){
		double sumFitness = 0; 
		for (int i = 0; i < scale; i++) {
			sumFitness += fitness[i];
		}
		
		//计算概率
		for (int i = 0; i < scale; i++) {
			this.pi[i] = fitness[i] / sumFitness ; 
		}
	}
	
	/**
	 *  挑选某代种群中适应度最高的个体，直接复制到子代中，
	 *  前提是已经计算出各个个体的适应度Fitness[max]
	 */
	private void selectBestGh(){
		int maxId = 0;
		double maxEvaluation = fitness[0];
		//记录适度最大的cityId和适度
		for (int i = 1; i < scale; i++) {
			if (maxEvaluation < fitness[i]) {
				maxEvaluation = fitness[i];
				maxId = i;
			}
		}
		
		//记录最好的染色体出现代数
		if (bestLen < maxEvaluation) {
			bestLen = maxEvaluation;
			bestGen = curGen;
			for (int i = 0; i < sceneryNum; i++) {
				bestRoute[i] = oldPopulation[maxId][i];
			}
		}
		
		//记录最好景点对应的酒店
		bestHotelIds = recommendHotel[maxId];
		
		// 将当代种群中适应度最高的染色体maxId复制到新种群中，排在第一位0
		this.copyGh(0, maxId);
	}
	
	/**
	 * 复制染色体，将oldPopulation复制到newPopulation
	 * @param curP 新染色体在种群中的位置
	 * @param oldP 旧的染色体在种群中的位置
	 */
	private void copyGh(int curP, int oldP){
		for (int i = 0; i < sceneryNum; i++) {
			newPopulation[curP][i] = oldPopulation[oldP][i];
		}
	}
	
	/**
	 * 选择算子，赌轮选择策略挑选scale-1个下一代个体
	 */
	private void select(){
		int selectId = 0;
		double tmpRan;
		double tmpSum;
		for (int i = 1; i < scale; i++) {
			tmpRan = (double)((getRandomNum() % 1000) / 1000.0);
			tmpSum = 0.0;
			for (int j = 0; j < scale; j++) {
				selectId = j;
				tmpSum += this.pi[j];
				if (tmpSum > tmpRan) {
					break;
				}
			}
			copyGh(i, selectId);
		}
	}
	
	/**
	 * 交叉算子，两点交叉,相同染色体交叉产生不同子代染色体
	 * @param k1 染色体编号 1|234|56
	 * @param k2 染色体编号 7|890|34
	 */
	private void crossover(int k1, int k2){
		//随机发生交叉的位置
		int pos1 = getRandomNum() % sceneryNum;
		int pos2 = getRandomNum() % sceneryNum;
		//确保pos1和pos2两个位置不同
		while(pos1 == pos2){
			pos2 = getRandomNum() % sceneryNum;
		}
		
		//确保pos1小于pos2
		if (pos1 > pos2) {
			int tmpPos = pos1;
			pos1 = pos2;
			pos2 = tmpPos;
		}
		
		//交换两条染色体中间部分
		for (int i = pos1; i < pos2; i++) {
			int t = newPopulation[k1][i];
			newPopulation[k1][i] = newPopulation[k2][i];
			newPopulation[k2][i] = t;
		}
	}
	
	/**
	 * 变异算子
	 * @param k 染色体标号
	 */
	private void mutation(int k){
		//对换变异次数
		int index;
		index = getRandomNum() % sceneryNum;
		newPopulation[k][index] = getRandomNum() % 2;
	}
	
	/**
	 * 进化函数，正常交叉变异
	 */
	private void evolution(){
		// 挑选某代种群中适应度最高的个体
		selectBestGh();
		// 赌轮选择策略挑选scale-1个下一代个体
		select();
		
		double ran;
		for (int i = 0; i < scale; i = i+2) {
			ran = random.nextDouble();
			if (ran < this.pc) {
				//如果小于pc，则进行交叉
				crossover(i, i+1);
			}else{
				//否者，进行变异
				ran = random.nextDouble();
				if (ran < this.pm) {
					//变异染色体i
					mutation(i);
				}
				
				ran = random.nextDouble();
				if (ran < this.pm) {
					//变异染色体i+1
					mutation(i + 1);
				}
			}
		}
	}
	
	double lastFitness = 0.0;
	
	/**
	 * 解决问题
	 */
	public ArrayList<Route> solve(){
		//初始化种群
		initGroup();
		//计算初始适度
		for (int i = 0; i < scale; i++) {
			fitness[i] = this.evaluate(i, oldPopulation[i]);
		}
		// 计算初始化种群中各个个体的累积概率，pi[max]
		countRate();
		
		System.out.println("gascenery 初始种群...");
		
		//开始进化
		for (curGen = 0; curGen < maxGen; curGen++) {
			evolution();
			// 将新种群newGroup复制到旧种群oldGroup中，准备下一代进化
			for (int i = 0; i < scale; i++) {
				for (int j = 0; j < sceneryNum; j++) {
					oldPopulation[i][j] = newPopulation[i][j];
				}
			}
			
			//计算当前代的适度
			double curFitness = 0.0;
			for (int i = 0; i < scale; i++) {
				fitness[i] = this.evaluate(i, oldPopulation[i]);
				curFitness += fitness[i];
			}
			
			// 计算当前种群中各个个体的累积概率，pi[max]
			countRate();
			if(this.lastFitness > curFitness){
				System.out.println("curGen of curfintess: " + this.curGen + "-" + this.maxGen);
			}
			this.lastFitness = curFitness;
		}
		
		selectBestGh();
		
		System.out.println("gasecnery 最后种群");
		HashMap<String, Route> routeMap = new HashMap<String, Route>();
		//获得城市对象
		for (int i = 0; i < scale; i++) {
			double sceneTicket = 0.0;
			double hotelPrice = 0.0;
			double hotness = fitness[i];
			double days = 0.0;
			int viewCount = 0;
			String tmpR = "";
			Route route = new Route();
			//获得景点列表
			ArrayList<Scenery> sList = new ArrayList<Scenery>();
			for (int j = 0; j < sceneryNum; j++) {
				if (oldPopulation[i][j] == 1) {
					Scenery scene = sceneryList.get(j);
					sceneTicket += scene.getPrice();
					viewCount += scene.getViewCount();
					days += scene.getVisitDay();
					tmpR += scene.getSid();
					sList.add(scene);
//					System.out.print(scene.getSname() + ",");
				}
			}
			//获得推荐的酒店列表
			ArrayList<Hotel> hotelList = new ArrayList<Hotel>();
			String hotelStr = recommendHotel[i];
			if (hotelStr != null && !hotelStr.equals("")) {
				String[] arr = hotelStr.split(",");
				for (String hSid : arr) {
					Hotel hotel = hotelMap.get(hSid);
					hotelPrice += hotel.getPrice();
					hotelList.add(hotel);
				}
			}
			
			String uid = AppUtil.md5(tmpR + this.maxDay);
			String sid = "da666bc57594baeb76b3bcf0";
			String ambiguitySname = "广州";
			String sname = "广州";
			String surl = "guangzhou";
			double sumPrice = hotelPrice + sceneTicket;
			
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
			route.setHotelPrice(hotelPrice);
			route.setSceneTicket(sceneTicket);
			route.setSumPrice(sumPrice);
			route.setSceneryList(sList);
			route.setHotelList(hotelList);
			if (!routeMap.containsKey(uid)) {
				routeMap.put(uid, route);
			}
//			System.out.print("  天数：" + days + " --价格：" + sceneTicket + " --热度:" + hotness);
//			System.out.print(" 适度：" + fitness[i] + " 酒店：" + recommendHotel[i]);
//			System.out.println();
		}
		
		ArrayList<Route> routeList = new ArrayList<Route>();
		Iterator<String> iter = routeMap.keySet().iterator();
		while(iter.hasNext()){
			String key = iter.next();
			Route route = routeMap.get(key);
			//至少两个景点才能算一个路径
			if(route.getSceneryList().size() >= 2){
				routeList.add(route);
			}
//			System.out.println(route.getSname() + "--" + route.getHotness());
		}
		
		Collections.sort(routeList);
		
		
		//------------------------------------
		System.out.println("最佳长度出现代数：" + bestGen);
		System.out.println("最佳长度" + bestLen);
		System.out.println("最佳酒店：" + bestHotelIds);
		System.out.println("最佳路径：");
		for (int i = 0; i < sceneryNum; i++) {
			System.out.print(bestRoute[i] + ",");
		}
		System.out.println();
		double price = 0.0;
		double hotness = 0.0;
		double days = 0.0;
		for (int i = 0; i < sceneryNum; i++) {
			if (bestRoute[i] == 1) {
				Scenery scene = sceneryList.get(i);
				price += scene.getPrice();
				hotness += scene.getViewCount();
				days += scene.getVisitDay();
				System.out.print(scene.getSname() + ",");
			}
		}
		if (!bestHotelIds.equals("")) {
			System.out.println();
			System.out.println("景点花费：" + price +" 元");
			String[] hotelArr = bestHotelIds.split(",");
			for (String sid : hotelArr) {
				Hotel hotel = hotelMap.get(sid);
				price += hotel.getPrice();
				System.out.println("酒店：" + sid + "-" + hotel.getPrice() + "元");
			}
		}
//		System.out.print("  天数：" + days + " --价格：" + price + " --热度:" + hotness);
//		System.out.print(" 酒店:" + bestHotelIds);
//		System.out.println();
		
		return routeList;
	}
	
	
	
	
	
	
	
	
	
	

}
