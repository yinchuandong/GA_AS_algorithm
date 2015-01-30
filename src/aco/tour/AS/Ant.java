package aco.tour.AS;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Random;

import model.Hotel;
import model.Scenery;

public class Ant {
	/**
	 * 城市对象
	 */
	Scenery city;
	/**
	 * 酒店的map
	 */
	HashMap<String, Hotel> hotelMap;
	/**
	 * 景点对象列表
	 */
	ArrayList<Scenery> sceneryList;
	/**
	 * 蚂蚁的路径
	 */
	private int[] tour;

	private double Q;

	/**
	 * 存储是否访问过某一城市，1代表访问过
	 */
	private int[] tabu;

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
	private double length;

	/**
	 * 游玩的天数上边界 默认为3
	 */
	double maxDay;
	double minDay;

	/**
	 * 记录当前路线的天数总和
	 */
	double curVisitDay;

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
	public double getLength() {
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
	public void init(Scenery city, ArrayList<Scenery> sceneList,
			HashMap<String, Hotel> hotelMap, double Q, double minDay, double maxDay) {
		this.city = city;
		this.sceneryList = sceneList;
		this.hotelMap = hotelMap;
		this.count = sceneList.size();
		this.Q = Q;
		this.minDay = minDay;
		this.maxDay = maxDay;
		this.pSum = 0.0;
		this.p = new double[count];
		this.tabu = new int[count];
		this.tour = new int[count];
		for (int i = 0; i < count; i++) {
			tabu[i] = 0;
			tour[i] = -1;
			p[i] = 0.0;
		}
		int random = new Random(System.currentTimeMillis()).nextInt(count);
		p[random] = 0.0;
		tabu[random] = 1;
		tour[0] = random;
		curVisitDay = sceneList.get(random).getVisitDay();
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
			if (tabu[i] == 0) {
				sum += Math.pow(pheromone[i], this.alpha)
						* (Math.pow(hotness[i], this.beta));
			}
		}
		// 公式中的分子部分
		for (int i = 0; i < count; i++) {
			if (tabu[i] == 1) {
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
		double day = this.sceneryList.get(select).getVisitDay();
		// 检查当前路线的游玩时间是否合法
		if (this.curVisitDay + day > maxDay) {
			return false;
		}
		this.curVisitDay += day;
		tour[index] = select;
		tabu[select] = 1;
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
	public void calcTourLength(ArrayList<Scenery> sceneList) {
		length = 0;
		double ticketPrice = 0.0;
		double viewCount = 0.0;
		double days = 0.0;
		for (int i = 0; i < count; i++) {
			int tourId = tour[i];
			if (tourId == -1) {
				break;
			}
			Scenery scene = sceneList.get(tourId);
			viewCount += scene.getViewCount();
			ticketPrice += scene.getPrice();
			days += scene.getVisitDay();
		}
		// 酒店当前染色体对应的酒店信息
		ArrayList<Hotel> curHotels = new ArrayList<Hotel>();

		if (days <= minDay || days > maxDay) {
			return;
		}

		Collections.sort(curHotels);
		double hotelPrice = 0.0;
		/*
		 * 判断酒店的个数是否大于需要入住的天数 如果大于则按照入住的天数计算价格 如果小于则计算所有酒店的价格，剩余天数就按照最低价格计算
		 */
		String hotelIds = "";// 保存推荐的hotelId
		int len = Math.min(curHotels.size(), (int) minDay);
		if (len != 0) {
			for (int i = 0; i < len; i++) {
				hotelPrice += curHotels.get(i).getPrice();
				hotelIds += curHotels.get(i).getSid() + ",";
			}
			int span = (int) (minDay - curHotels.size());
			for (int i = 0; i < span; i++) {
				hotelPrice += curHotels.get(0).getPrice();
				hotelIds += curHotels.get(0).getSid() + ",";
			}
		} else {
			// 当该景点没有酒店的时候，默认80块
			for (int i = 0; i < (int) minDay; i++) {
				hotelPrice += 80.0;
			}
		}

		if (!hotelIds.equals("")) {
			hotelIds = hotelIds.substring(0, hotelIds.length() - 1);
		}

		double price = hotelPrice + ticketPrice;
		double rho = 0.9;
//		double fx = (1.0 - rho) * Math.pow(1.0 / (10.0 + price), 1.0);
//		double gx = rho * Math.pow(1.0 / (10.0 + this.Q - viewCount), 1.0 / 3.0);
		double fx = (1 - rho)*(10000.0 / (price + 10.0));
		double gx =  rho * Math.pow(viewCount, 1.0/3.0);
		this.length = fx + gx;
//		this.length = viewCount;

	}

}
