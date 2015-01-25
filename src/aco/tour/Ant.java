package aco.tour;

import java.util.ArrayList;
import java.util.Random;

class Ant {
	/**
	 * 景点对象列表
	 */
	ArrayList<Scene> sceneList;
	/**
	 * 蚂蚁的路径
	 */
	private int[] tour;

	/**
	 * 存储是否访问过某一城市，1代表访问过
	 */
	private int[] city;

	/**
	 * 城市被访问的概率
	 */
	private double[] p;

	/**
	 * 概率的总和
	 */
	private double pSum = 0.0;

	/**
	 * 蚂蚁当前走过的距离
	 */
	private int length;

	/**
	 * 游玩的天数上边界 默认为3
	 */
	double maxDay;

	/**
	 * 记录当前路线的天数总和
	 */
	double visitedDays;

	/**
	 * 城市个数
	 */
	private int count;

	/**
	 * 公式中的参数alpha
	 */
	private double alpha = 1.0;
	/**
	 * 公式中的参数beta
	 */
	private double beta = 2.0;

	/**
	 * 获得蚂蚁当前的路线
	 * 
	 * @return
	 */
	public int[] getTour() {
		return tour;
	}

	/**
	 * 获得蚂蚁当前的长度
	 * 
	 * @return
	 */
	public int getLength() {
		return length;
	}

	public Ant() {

	}

	/**
	 * 初始化蚂蚁的起始路径
	 * 
	 * @param count
	 *            城市的个数
	 * @param maxDay
	 *            天数的上限
	 */
	public void init(ArrayList<Scene> sceneList, double maxDay) {
		this.sceneList = sceneList;
		this.count = sceneList.size();
		this.maxDay = maxDay;
		this.pSum = 0.0;
		this.p = new double[count];
		this.city = new int[count];
		this.tour = new int[count];
		for (int i = 0; i < count; i++) {
			city[i] = 0;
			tour[i] = -1;
			p[i] = 0.0;
		}
		int random = new Random(System.currentTimeMillis()).nextInt(count);
		p[random] = 0.0;
		city[random] = 1;
		tour[0] = random;
		visitedDays = sceneList.get(random).getVisitDays();
	}

	/**
	 * 计算蚂蚁选择景点的概率
	 * 
	 * @param pheromone
	 *            信息素
	 * @param hotness
	 *            热度
	 */
	public void calcProb(double[] pheromone, double[] hotness) {
		this.pSum = 0.0;
		double sum = 0.0;// 信息素概率总和
		// 公式中得分母部分
		for (int i = 0; i < count; i++) {
			if (city[i] == 0) {
				sum += Math.pow(pheromone[i], this.alpha)
						* (Math.pow(hotness[i], this.beta));
			}
		}
		// 公式中的分子部分
		for (int i = 0; i < count; i++) {
			if (city[i] == 1) {
				p[i] = 0.0;
			} else {
				p[i] = Math.pow(pheromone[i], this.alpha)
						* (Math.pow(hotness[i], this.beta)) / sum;
				pSum += p[i];
			}
		}
	}

	/**
	 * 通过信息素和距离计算轮盘赌注概率，选择下一个城市
	 * 
	 * @param index
	 *            下一个城市在tour数组中的id
	 * @return 如果满足一切约束条件，则返回true；否则返回false
	 */
	public boolean selectNextCity(int index) {
		int select = getRandomCity(p);
		double day = this.sceneList.get(select).getVisitDays();
		// 检查当前路线的游玩时间是否合法
		if (this.visitedDays + day > maxDay) {
			return false;
		}
		this.visitedDays += day;
		tour[index] = select;
		city[select] = 1;
		pSum -= p[select];
		p[select] = 0.0; // 选择过的城市概率设为0，以后就不会被选择到
		return true;
	}

	/**
	 * 使用轮盘赌注选择城市
	 * 
	 * @param p
	 * @return
	 */
	private int getRandomCity(double[] p) {
		double selectP = new Random(System.currentTimeMillis()).nextDouble()
				* pSum;
		double sumSel = 0.0;
		for (int i = 0; i < count; i++) {
			sumSel += p[i];
			if (sumSel > selectP)
				return i;
		}
		return -1;
	}

	/**
	 * 计算蚂蚁当前走过的距离总和
	 * 
	 * @param distance
	 */
	public void calcTourLength(ArrayList<Scene> sceneList) {
		length = 0;
		for (int i = 0; i < count; i++) {
			if (tour[i] == -1) {
				break;
			}
			length += sceneList.get(tour[i]).getViewCount();
		}
	}
}
