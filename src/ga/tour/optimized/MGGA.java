package ga.tour.optimized;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.rmi.server.UID;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
import util.SceneryUtil;


public class MGGA {
	
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
	 * 每条路线对应的酒店价格均价
	 */
	private double[] recommendHotelPrice;
	
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
	
	private ArrayList<EncodedRoute> globalBestRoute;
	
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
	 * 景点属于的城市的对象
	 */
	private Scenery city;
	
	/**
	 * greedy algorithm util
	 */
	private GreedyAlgorithm greedyAgm;
	
	/**
	 * 
	 * @param scale 种群规模
	 * @param maxGen 运行代数
	 * @param pc 交叉概率
	 * @param pm 变异概率
	 */
	public MGGA(int scale, int maxGen, double pc, double pm){
		this.scale = scale;
		this.maxGen = maxGen;
		this.pc = pc;
		this.pm = pm;
		this.sceneryList = new ArrayList<Scenery>();
	}
	
	public class EncodedRoute{
		public int[] chromosome;
		public double hotelPrice;
		public double fitness;
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
	 * @param city 城市
	 * @param minDay 天数下限，开区间
	 * @param maxDay 天数上限，闭区间
	 * @param hotelMap 酒店的信息
	 * @throws Exception 
	 */
	public void init(Scenery city, ArrayList<Scenery> sceneryList, HashMap<String, Hotel> hotelMap, double minDay, double maxDay){
		this.city = city;
		this.sceneryList = sceneryList;
		this.minDay = minDay;
		this.maxDay = maxDay;
		this.hotelMap = hotelMap;
		
		this.sceneryNum = this.sceneryList.size();
		if (sceneryNum < 2) {
			throw new IllegalArgumentException("景点的个数为" + sceneryNum +"，不符合，其url为：" + city.getSurl());
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
		
		this.recommendHotelPrice = new double[scale];
		this.globalBestRoute = new ArrayList<EncodedRoute>();
		
		this.random = new Random(System.currentTimeMillis());
	}
	
	/**
	 * initialize the population by greedy algorithm <br/>
	 * encode the chromosome in the pattern like 01001
	 */
	private void initGroup(){
		this.greedyAgm = new GreedyAlgorithm(minDay, maxDay, scale, sceneryList);
		oldPopulation = greedyAgm.getInitPopulation();
	}
	
	/**
	 * check if the distance of 2 stochastic scene is valid   
	 * @param index
	 * @param chromosome
	 */
	private void checkDistance(int index, int[] chromosome){
		Scenery lastScene = null;
		for (int i = 0; i < chromosome.length; i++) {
			if (chromosome[i] != 1) {
				continue;
			}
			Scenery scene = sceneryList.get(i);
			if(lastScene != null){
				double distance = SceneryUtil.calcDistance(scene, lastScene);
				if(distance > 200000.0){
					chromosome[i] = 0;
				}
			}
			lastScene = scene;
		}
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
		
		//检测景点间的距离
		this.checkDistance(index, chromosome);
		
		//酒店当前染色体对应的酒店信息
		ArrayList<Hotel> curHotels = new ArrayList<Hotel>();
		for (int i = 0; i < chromosome.length; i++) {
			if (chromosome[i] != 1) {
				continue;
			}
			Scenery scene = sceneryList.get(i);
			if (days + scene.getVisitDay() > maxDay) {
				//if current visitDays larger than maxDay, remove this scenery
				chromosome[i] = 0;
				continue;
			}
			ticketPrice += scene.getPrice();
			hotness += (double) scene.getViewCount();
			days += scene.getVisitDay();
			// 获得该景点的酒店信息
			Hotel hotel = hotelMap.get(scene.getSid());
			if (hotel != null) {
				curHotels.add(hotel);
			}
		}
		
		if (days <= minDay || days > maxDay) {
			return 0;
		}
		
		//计算该路线酒店的花费, cost = avg_cost * maxDay
		double hotelPrice = 0.0;
		if(curHotels.size() != 0){
			for (Hotel hotel : curHotels) {
				hotelPrice += hotel.getPrice();
			}
			hotelPrice = (hotelPrice / curHotels.size()) * (maxDay);
		}else{
			//如果没有酒店，则按照80元的花费计算
			hotelPrice = 80.0 * maxDay;
		}
		recommendHotelPrice[index] = hotelPrice;

		//计算适度
		double price = hotelPrice + ticketPrice;
		double fitness =  0.0;
		double rho = 0.9;
		
		double fx = (1 - rho)*(10000.0 / (price + 10.0));
		double gx =  Math.pow(hotness, 1.0/3.0);
		fitness = fx + gx;
//		System.out.println("fiteness: price=" + fx + "  hotness=" + gx );
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
	private void selectBestAndWorst(){
		int maxId = 0;
		double maxFitness = fitness[0];
		
		int minId = 0;
		double minFitness = fitness[0];
		ArrayList<Integer> minIdList = new ArrayList<Integer>();
		
		//save the best and worst city's id and fitness
		for (int i = 1; i < scale; i++) {
			//save the best chromosome
			if (maxFitness < fitness[i]) {
				maxFitness = fitness[i];
				maxId = i;
			}
			
			//save the worst chromosome
			if (minFitness > fitness[i]){
				minFitness = fitness[i];
				minId = i;
				minIdList.add(i);
			}
		}
		
		//save the globally best chromosome
		if (bestLen < maxFitness) {
			bestLen = maxFitness;
			bestGen = curGen;
			
			//recode the local best route;
//			recodeLocalBest(maxId);
			int[] tmpChromo = new int[sceneryNum];
			for (int i = 0; i < sceneryNum; i++) {
				bestRoute[i] = oldPopulation[maxId][i];
				tmpChromo[i] = bestRoute[i];
			}
			EncodedRoute tmpRoute = new EncodedRoute();
			tmpRoute.chromosome = tmpChromo;
			tmpRoute.fitness = fitness[maxId];
			tmpRoute.hotelPrice = recommendHotelPrice[maxId];
			this.globalBestRoute.add(tmpRoute);
		}
		
		// copy the best chromosome into new population and put on the first of population
		this.copyChromosome(0, maxId);
		
//		this.greedyAgm.optimize(oldPopulation[minId]);
		for (Integer id : minIdList) {
			this.greedyAgm.optimize(oldPopulation[id]);
		}
	}
	
	/**
	 * 复制染色体，将oldPopulation复制到newPopulation
	 * @param curP 新染色体在种群中的位置
	 * @param oldP 旧的染色体在种群中的位置
	 */
	private void copyChromosome(int curP, int oldP){
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
			copyChromosome(i, selectId);
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
		selectBestAndWorst();
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
	
	/**
	 * 解决问题
	 */
	public void run(){
		//初始化种群
		initGroup();
		//计算初始适度
		for (int i = 0; i < scale; i++) {
			fitness[i] = this.evaluate(i, oldPopulation[i]);
		}
		// 计算初始化种群中各个个体的累积概率，pi[max]
		countRate();
		
//		System.out.println("gascenery 初始种群...");
		
		//开始进化
		for (curGen = 0; curGen < maxGen; curGen++) {
			//do select, crossover and mutation operator
			evolution();
			
			//计算当前代的适度
			for (int i = 0; i < scale; i++) {
				fitness[i] = this.evaluate(i, newPopulation[i]);
			}
			
			//calculate the probability of each chromosome in population
			countRate();
			
			// 将新种群newGroup复制到旧种群oldGroup中，准备下一代进化
			for (int i = 0; i < scale; i++) {
				for (int j = 0; j < sceneryNum; j++) {
					oldPopulation[i][j] = newPopulation[i][j];
				}
			}
		}
		
		//select the best and worst generation
		selectBestAndWorst();
		
	}
	
	/**
	 * 返回未解码的路线
	 * @return
	 */
	public ArrayList<EncodedRoute> getEndecodedRoute(){
		return this.globalBestRoute;
	}
	

}
