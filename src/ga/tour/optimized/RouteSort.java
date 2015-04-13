package ga.tour.optimized;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import model.Scenery;

public class RouteSort {
	
	/**
	 * 种群规模
	 */
	private int scale;
	
	/**
	 * 城市数量
	 */
	private int cityNum;
	
	/**
	 * 城市列表
	 */
	private ArrayList<Scenery> cityList;
	
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
	 * 距离矩阵，每行代表一条染色体
	 */
	private double[][] distance;
	
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
	
	/**
	 * 随机数
	 */
	private Random random;

	/**
	 * 
	 * @param scale 种群规模
	 * @param maxGen 运行代数
	 * @param pc 交叉概率
	 * @param pm 变异概率
	 */
	public RouteSort(int scale, int maxGen, double pc, double pm){
		this.scale = scale;
		this.maxGen = maxGen;
		this.pc = pc;
		this.pm = pm;
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
	 * @param filename
	 * @throws Exception 
	 */
	public void init(ArrayList<Scenery> cityList){
		this.cityList = cityList;
		ArrayList<Double> x = new ArrayList<Double>();
		ArrayList<Double> y = new ArrayList<Double>();
		for (Scenery scene : cityList) {
			x.add(scene.getMapX());
			y.add(scene.getMapY());
		}
		
		this.cityNum = this.cityList.size();
		if (this.cityNum < 2) {
			throw new IllegalArgumentException("gasort cityNum 不能为小于2");
		}
		this.distance = new double[cityNum][cityNum];
		
		//通过欧式距离计算距离矩阵
		for (int i = 0; i < cityNum - 1; i++) {
			distance[i][i] = 0; //对角线为0
			for (int j = i+1; j < cityNum; j++) {
				double xi = x.get(i);
				double yi = y.get(i);
				double xj = x.get(j);
				double yj = y.get(j);
				double rij = Math.sqrt(((xi - xj)*(xi - xj) + (yi - yj)*(yi - yj)));
				distance[i][j] = rij;
				distance[j][i] = rij;
			}
		}
		distance[cityNum - 1][cityNum - 1] = 0;//最后一个城市的距离为0，for循环里没有初始化
		
		this.bestLen = Integer.MAX_VALUE;
		this.bestGen = 0;
		this.bestRoute = new int[cityNum];
		this.curGen = 0;
		
		this.newPopulation = new int[scale][cityNum];
		this.oldPopulation = new int[scale][cityNum];
		this.fitness = new double[scale];
		this.pi = new double[scale];
		
		this.random = new Random(System.currentTimeMillis());
	}
	
	/**
	 * 初始化种群
	 */
	private void initGroup(){
		int i, j, k;
		for (k = 0; k < scale; k++) {
			for (i = 0; i < cityNum; ) {
				oldPopulation[k][i] = getRandomNum() % cityNum;
				//确保随机产生的染色体中没有重复的基因
				for (j = 0; j < i; j++) {
					if (oldPopulation[k][i] == oldPopulation[k][j]) {
						break;
					}
				}
				if (i == j) {
					i++;
				}
			}
		}
	}
	
	/**
	 * 计算染色体的距离
	 * @param chromosome 染色体，包含：起始城市,城市1,城市2...城市n
	 * @return the total distance of all chromosome's cities;
	 */
	private double evaluate(int[] chromosome){
		double len = 0.0;
		for(int i=1; i<cityNum; i++){
			int preCity = chromosome[i - 1];
			int curCity = chromosome[i];
			len += distance[preCity][curCity];
		}
		// 城市n,起始城市
//		len += distance[chromosome[cityNum - 1]][chromosome[0]];
//		System.out.println(" len:" + len);
		return len;
	}
	
	/**
	 * 计算种群中各个个体的累积概率，
	 * 前提是已经计算出各个个体的适应度fitness[max]，
	 * 作为赌轮选择策略一部分，Pi[max]
	 */
	private void countRate(){
		double sumFitness = 0; 
		double[] tmpF = new double[scale];
		for (int i = 0; i < scale; i++) {
			//求倒数是因为距离越大，概率应该越小
			tmpF[i]  = 10.0 / fitness[i];
			sumFitness += tmpF[i];
		}
		
		//计算累计概率
		this.pi[0] = tmpF[0] / sumFitness;
		for (int i = 1; i < scale; i++) {
			pi[i] = (tmpF[i] / sumFitness) + pi[i - 1]; 
		}
	}
	
	/**
	 *  挑选某代种群中适应度最高的个体，直接复制到子代中，
	 *  前提是已经计算出各个个体的适应度Fitness[max]
	 */
	private void selectBestGh(){
		int minId = 0;
		double minEvaluation = fitness[0];
		//记录距离最小的cityId和适度
		for (int i = 1; i < scale; i++) {
			if (minEvaluation > fitness[i]) {
				minEvaluation = fitness[i];
				minId = i;
			}
		}
		
		//记录最好的染色体出现代数
		if (bestLen > minEvaluation) {
			bestLen = minEvaluation;
			bestGen = curGen;
			for (int i = 0; i < cityNum; i++) {
				bestRoute[i] = oldPopulation[minId][i];
			}
		}
		
		// 将当代种群中适应度最高的染色体maxId复制到新种群中，排在第一位0
		this.copyGh(0, minId);
	}
	
	/**
	 * 复制染色体，将oldPopulation复制到newPopulation
	 * @param curP 新染色体在种群中的位置
	 * @param oldP 旧的染色体在种群中的位置
	 */
	private void copyGh(int curP, int oldP){
		for (int i = 0; i < cityNum; i++) {
			newPopulation[curP][i] = oldPopulation[oldP][i];
		}
	}
	
	/**
	 * 赌轮选择策略挑选
	 */
	private void select(){
		int selectId = 0;
		double tmpRan;
//		System.out.print("selectId:");
		for (int i = 1; i < scale; i++) {
			tmpRan = (double)((getRandomNum() % 1000) / 1000.0);
			for (int j = 0; j < scale; j++) {
				selectId = j;
				if (tmpRan <= pi[j]) {
					break;
				}
			}
//			System.out.print(selectId+" ");
			copyGh(i, selectId);
		}
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
					onVariation(i);
				}
				
				ran = random.nextDouble();
				if (ran < this.pm) {
					//变异染色体i+1
					onVariation(i + 1);
				}
			}
		}
	}
	
	/**
	 * 两点交叉,相同染色体交叉产生不同子代染色体
	 * @param k1 染色体编号 1|234|56
	 * @param k2 染色体编号 7|890|34
	 */
	private void crossover(int k1, int k2){
		//临时存放正在交叉的染色体
		int[] gh1 = new int[cityNum];//染色体1
		int[] gh2 = new int[cityNum];//染色体2
		
		//随机发生交叉的位置
		int pos1 = getRandomNum() % cityNum;
		int pos2 = getRandomNum() % cityNum;
		//确保pos1和pos2两个位置不同
		while(pos1 == pos2){
			pos2 = getRandomNum() % cityNum;
		}
		
		//确保pos1小于pos2
		if (pos1 > pos2) {
			int tmpPos = pos1;
			pos1 = pos2;
			pos2 = tmpPos;
		}
		
		int i, j, k;
		//记录当前复制交换位置
		int flag; 
		
		// 将染色体1中的第三部分移到染色体2的首部
		for(i = 0, j = pos2; j < cityNum; i++, j++){
			gh2[i] = newPopulation[k1][j];
		}
		//染色体2原基因开始位置
		flag = i;
		
		//复制源染色体2到gh2后面
		for(k = 0, j = flag; j < cityNum; k++){
			gh2[j] = newPopulation[k1][k];
			//避免交换后，同一条染色体中存在重复的基因
			for (i = 0; i < flag; i++) {
				if (gh2[j] == gh2[i]) {
					break;
				}
			}
			//当染色体重不存在重复基因时，才复制下一个基因
			if (i == flag) {
				j++;
			}
		}
		
		//交换第一条染色体
		flag = pos1;
		for(k = 0, j = 0; k < cityNum; k++){
			gh1[j] = newPopulation[k1][k];
			//判断k2染色体的0-pos1的位置是否和k1的相同
			for (i = 0; i < flag; i++) {
				if (newPopulation[k2][i] == gh1[j]) {
					break;
				}
			}
			if (i == flag) {
				j++;
			}
		}
		
		//交换k1的第三部分
		flag = cityNum - pos1;
		for (i = 0, j = flag; j < cityNum; i++, j++) {
			gh1[j] = newPopulation[k2][i];
		}
		
		// 交叉完毕放回种群
		for (i = 0; i < cityNum; i++) {
			newPopulation[k1][i] = gh1[i];
			newPopulation[k2][i] = gh2[i];
		}
	}
	
	/**
	 * 多次对换变异算子
	 * 如：123456变成153426，基因2和5对换了
	 * @param k 染色体标号
	 */
	private void onVariation(int k){
		int ran1, ran2, tmp;
		//对换变异次数
		int count;
		
		count = getRandomNum() % cityNum;
		for (int i = 0; i < count; i++) {
			ran1 = getRandomNum() % cityNum;
			ran2 = getRandomNum() % cityNum;
			while(ran1 == ran2){
				ran2 = getRandomNum() % cityNum;
			}
			tmp = newPopulation[k][ran1];
			newPopulation[k][ran1] = newPopulation[k][ran2];
			newPopulation[k][ran2] = tmp;
		}
	}
	
	/**
	 * 解决问题，返回排好序的景点列表
	 * @return
	 */
	public ArrayList<Scenery> run(){
		//初始化种群
		initGroup();
		//计算初始适度
		for (int i = 0; i < scale; i++) {
			fitness[i] = this.evaluate(oldPopulation[i]);
		}
		// 计算初始化种群中各个个体的累积概率，pi[max]
		countRate();
		
//		System.out.println("gasort初始种群...");
		
		//开始进化
		for (curGen = 0; curGen < maxGen; curGen++) {
			evolution();
			// 将新种群newGroup复制到旧种群oldGroup中，准备下一代进化
			for (int i = 0; i < scale; i++) {
				for (int j = 0; j < cityNum; j++) {
					oldPopulation[i][j] = newPopulation[i][j];
				}
			}
			
			//计算当前代的适度
			for (int i = 0; i < scale; i++) {
				fitness[i] = this.evaluate(oldPopulation[i]);
			}
			
			// 计算当前种群中各个个体的累积概率，pi[max]
			countRate();
		}
		
		selectBestGh();
		
//		System.out.println("gasort 进化完毕");
//		System.out.println("最佳长度出现代数：");
//		System.out.println(bestGen);
//		System.out.println("最佳长度");
//		System.out.println(bestLen);
//		System.out.println("最佳路径：");
		
		ArrayList<Scenery> sortedList = new ArrayList<Scenery>();
		
		for (int i = 0; i < cityNum; i++) {
//			System.out.print(bestRoute[i] + ",");
			int index = bestRoute[i];
			sortedList.add(cityList.get(index));
		}
//		System.out.println("bestlen:"+bestLen);
		return sortedList;
		
	}
	
	
	/**
	 * 获得最短长度
	 * @return
	 */
	public double getBestLen() {
		return bestLen;
	}

	/**
	 * 默认进行排序的函数
	 * @param sceneList
	 * @return
	 * @throws Exception 
	 */
	public static ArrayList<Scenery> runDefault(ArrayList<Scenery> sceneList) throws Exception{
		RouteSort ga = new RouteSort(30, 100, 0.8, 0.9);
		ga.init(sceneList);
		return ga.run();
	}
	
	public static void main(String[] args) throws IOException{
		RouteSort ga = new RouteSort(6, 10, 0.8, 0.9);
//		ga.init("./gadata/data2.txt");
		ga.run();
	}
	
	
	
	
	
	
	
	
	

}
