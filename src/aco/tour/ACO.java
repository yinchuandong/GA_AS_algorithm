package aco.tour;

import java.util.ArrayList;

public class ACO {

	/**
	 * 景点的列表
	 */
	ArrayList<Scene> sceneList;
	/**
	 * 游玩的天数 默认为3
	 */
	double upDays;
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
	 * 城市的数量
	 */
	int cityCount;
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
	int bestLength;

	public ACO() {
		this.upDays = 3.0;
	}

	/**
	 * 初始化蚁群
	 * 
	 * @param sceneList
	 *            景点的列表
	 * @param antCount
	 *            蚂蚁的数量
	 * @param upDays
	 *            游玩的天数
	 */
	public void init(ArrayList<Scene> sceneList, int antCount, double upDays) {
		this.sceneList = sceneList;
		this.antCount = antCount;
		this.upDays = upDays;
		ants = new Ant[antCount];
		cityCount = sceneList.size();
		// 初始化信息素 默认为1
		pheromone = new double[cityCount];
		hotness = new double[cityCount];
		for (int i = 0; i < cityCount; i++) {
			pheromone[i] = 0.8;
			hotness[i] = (double) sceneList.get(i).getViewCount() / this.Q;
		}
		bestLength = Integer.MIN_VALUE;
		bestTour = new int[cityCount];
		for (int i = 0; i < antCount; i++) {
			ants[i] = new Ant();
			ants[i].init(sceneList, upDays);
		}
	}

	/**
	 * 蚁群算法的运行入口
	 * 
	 * @param maxgen
	 *            运行最大的代数
	 */
	public void run(int maxgen) {
		for (int gen = 0; gen < maxgen; gen++) {
			// System.out.println("gen:" + gen);
			// 每一只蚂蚁的移动过程
			for (int i = 0; i < antCount; i++) {
				// System.out.println("gen: " + gen + " -- antId:" + i);
				// 对该蚂蚁进行城市路线选择
				ants[i].calcProb(pheromone, hotness);
				for (int j = 1; j < cityCount; j++) {
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
					System.out.println("第" + gen + "代, 蚂蚁" + i + "，发现新的解为："
							+ bestLength);
					for (int j = 0; j < cityCount; j++) {
						bestTour[j] = ants[i].getTour()[j];
						if (bestTour[j] != -1) {
							System.out.print(sceneList.get(bestTour[j])
									.getCityName() + " ");
						}
					}
					System.out.println();
				}
			}
			// 更新信息素
			updatePheromone();
			// 蚂蚁重新初始化
			for (int i = 0; i < antCount; i++) {
				ants[i].init(sceneList, upDays);
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
		double rho = 0.01;
		// 信息素的衰减
		for (int i = 0; i < cityCount; i++) {
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
		for (int i = 0; i < cityCount; i++) {
			int curId = bestTour[i];
			if (curId != -1) {
				// 如果改城市被访问过
				pheromone[curId] += ants[bestAntId].getLength() / Q;
			} else {
				return;
			}
		}
	}

	/**
	 * 打印路径长度
	 */
	public void reportResult() {
		System.out.println("最优路径长度是" + bestLength);
		for (int j = 0; j < cityCount; j++) {
			if (bestTour[j] != -1) {
				System.out
						.print(sceneList.get(bestTour[j]).getCityName() + " ");
			} else {
				return;
			}
		}
	}
}
