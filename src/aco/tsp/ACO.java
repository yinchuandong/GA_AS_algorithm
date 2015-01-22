package aco.tsp;


public class ACO {
	/**
	 * 蚂蚁对象数组
	 */
	Ant[] ants;
	/**
	 * 蚂蚁的数量
	 */
	int antCount;
	/**
	 * 城市两两之间的距离
	 */
	int[][] distance;
	/**
	 * 城市两两之间的信息素 公式中的tao
	 */
	double[][] pheromone;
	/**
	 * 城市的数量
	 */
	int cityCount;
	/**
	 * 最优的路线
	 */
	int[] bestTour;
	/**
	 * 城市名称
	 */
	String[] city;
	/**
	 * 当前最优长度
	 */
	int bestLength;

	/**
	 * 初始化蚁群
	 * @param antCount 蚂蚁的数量
	 */
	public void init(int antCount) {
		this.antCount = antCount;
		ants = new Ant[antCount];
		Citys citys = new Citys();
		citys.initDis();
		distance = citys.getDistance();
		city = citys.getCityName();
		cityCount = city.length;
		pheromone = new double[cityCount][cityCount];
		for (int i = 0; i < cityCount; i++) {
			for (int j = 0; j < cityCount; j++) {
				pheromone[i][j] = 1;
			}
		}
		bestLength = Integer.MAX_VALUE;
		bestTour = new int[cityCount + 1];
		for (int i = 0; i < antCount; i++) {
			ants[i] = new Ant();
			ants[i].init(cityCount);
		}
	}

	/**
	 * 蚁群算法的运行入口
	 * @param maxgen 运行最大的代数
	 */
	public void run(int maxgen) {
		for (int gen = 0; gen < maxgen; gen++) {
			//每一只蚂蚁的移动过程
			for (int i = 0; i < antCount; i++) {
				//对该蚂蚁进行城市路线选择
				for (int j = 1; j < cityCount; j++) {
					ants[i].selectNextCity(j, pheromone, distance);
				}
				//计算该蚂蚁爬过的路线总长度
				ants[i].calcTourLength(distance);
				//判断是否为最优路线
				if (ants[i].getLength() < bestLength) {
					//保存最优代
					bestLength = ants[i].getLength();
					System.out.println("第" + gen + "代，发现新的解为：" + bestLength);
					for (int j = 0; j < cityCount + 1; j++) {
						bestTour[j] = ants[i].getTour()[j];
						System.out.print(city[bestTour[j]] + " ");
					}
					System.out.println();
				}
			}
			//更新信息素
			updatePheromone();
			//蚂蚁重新初始化
			for (int i = 0; i < antCount; i++) {
				ants[i].init(cityCount);
			}
		}
		System.out.println("end");
	}

	/**
	 * 更新信息素,使用ant-cycle模型 <br/>
	 * 公式1: T_ij(t+1) = (1-r)*T_ij(t) + delta_T_ij(t) <br/>
	 * 公式2: delta_T_ij(t) = Q/L_k Q为常数，L_k为蚂蚁走过的总长度
	 */
	private void updatePheromone() {
		double rou = 0.5;
		for (int i = 0; i < cityCount; i++) {
			for (int j = 0; j < cityCount; j++) {
				pheromone[i][j] *= (1 - rou);
			}
		}
		for (int i = 0; i < antCount; i++) {
			for (int j = 0; j < cityCount; j++) {
				int curId = ants[i].getTour()[j];
				int nextId = ants[i].getTour()[j + 1];
				pheromone[curId][nextId] += 1.0 / ants[i].getLength();
			}
		}
	}

	/**
	 * 打印路径长度
	 */
	public void reportResult() {
		System.out.println("最优路径长度是" + bestLength);
		for (int j = 0; j < cityCount + 1; j++) {
			System.out.print(city[bestTour[j]] + " ");
		}
	}
}
