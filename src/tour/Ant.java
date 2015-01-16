package tour;

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
	 * 蚂蚁当前走过的距离
	 */
	private int length;
	
	/**
	 * 游玩的天数上边界 默认为3
	 */
	double upDays;
	
	/**
	 * 记录当前路线的天数总和
	 */
	double visitedDays;
	
	/**
	 * 城市个数
	 */
	private int count;
	
	/**
	 * 公式中得参数alpha
	 */
	private double alpha = 1.0;
	/**
	 * 公式中得参数beta
	 */
	private double beta = 2.0;
	/**
	 * 获得蚂蚁当前的路线
	 * @return
	 */
	public int[] getTour() {
		return tour;
	}

	/**
	 * 获得蚂蚁当前的长度
	 * @return
	 */
	public int getLength() {
		return length;
	}
	
	public Ant(){
		
	}

	/**
	 * 初始化蚂蚁的起始路径
	 * @param count 城市的个数
	 * @param upDays 天数的上限
	 */
	public void init(ArrayList<Scene> sceneList, double upDays) {
		this.sceneList = sceneList;
		this.count = sceneList.size();
		this.upDays = upDays;
		this.city = new int[count];
		this.tour = new int[count];
		for (int i = 0; i < count; i++) {
			city[i] = 0;
			tour[i] = -1;
		}
		int random = new Random(System.currentTimeMillis()).nextInt(count);
		city[random] = 1;
		tour[0] = random;
		visitedDays = sceneList.get(random).getVisitDays(); 
	}

	/**
	 * 通过信息素和距离计算轮盘赌注概率，选择下一个城市
	 * @param index 下一个城市在tour数组中的id
	 * @param pheromone
	 * @param distance
	 * @return 如果满足一切约束条件，则返回true；否则返回false
	 */
	public boolean selectNextCity(int index, double[] pheromone, double[] hotness) {
		double[] p = new double[count];
		double sum = 0.0;//信息素概率总和
		//公式中得分母部分
		for (int i = 0; i < count; i++) {
			if (city[i] == 0) {
				sum += Math.pow(pheromone[i], this.alpha) * (Math.pow(hotness[i], this.beta));
			}
		}
		//公式中的分子部分
		for (int i = 0; i < count; i++) {
			if (city[i] == 1) {
				p[i] = 0.0;
			} else {
				p[i] = Math.pow(pheromone[i], this.alpha) * (Math.pow(hotness[i], this.beta)) / sum;
			}
		}
		
		int select = getRandomCity(p);
		double day = this.sceneList.get(select).getVisitDays();
		//检查当前路线的游玩时间是否合法		
		if(this.visitedDays + day > upDays){
			return false;
		}
		this.visitedDays += day;
		tour[index] = select;
		city[select] = 1;
		return true;
	}
	
	/**
	 * 使用轮盘赌注选择城市
	 * @param p
	 * @return
	 */
	private int getRandomCity(double[] p) {
		double selectP = new Random(System.currentTimeMillis()).nextDouble();
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
	 * @param distance
	 */
	public void calcTourLength(ArrayList<Scene> sceneList) {
		length = 0;
		for (int i = 0; i < count; i++) {
			if(tour[i] == -1){
				break;
			}
			length += sceneList.get(tour[i]).getViewCount();
		}
	}
}
