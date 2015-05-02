package test.sort;

import ga.tour.optimized.RouteSort;

import java.awt.List;
import java.io.File;
import java.util.ArrayList;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import util.AppUtil;
import model.Scenery;

public class DPSort {
	private ArrayList<Scenery> sceneList = null;
	private double[][] distance = null;
	private int sceneNum = 0;
	
	public DPSort() {
		
	}
	
	public void init(ArrayList<Scenery> sceneList){
		this.sceneList = sceneList;
		this.sceneNum = sceneList.size();
		this.distance = new double[sceneNum][sceneNum];
		ArrayList<Double> x = new ArrayList<Double>();
		ArrayList<Double> y = new ArrayList<Double>();
		for (Scenery scene : sceneList) {
			x.add(scene.getMapX());
			y.add(scene.getMapY());
		}
		//通过欧式距离计算距离矩阵
		for (int i = 0; i < sceneNum - 1; i++) {
			distance[i][i] = 0; // 对角线为0
			for (int j = i + 1; j < sceneNum; j++) {
				double xi = x.get(i);
				double yi = y.get(i);
				double xj = x.get(j);
				double yj = y.get(j);
				double rij = Math.sqrt(((xi - xj) * (xi - xj) + (yi - yj)
						* (yi - yj)));
				distance[i][j] = rij;
				distance[j][i] = rij;
			}
		}
		distance[sceneNum - 1][sceneNum - 1] = 0;//最后一个城市的距离为0，for循环里没有初始化
	}
	
	
	public void run(){
		int i, j, k, min, tmp;
		int n = sceneNum;
		int b = (int) Math.pow(2, n - 1);
		
		int[][] F = new int[n][b];
		int[][] M = new int[n][b];
		
	}
	
	public static void main(String[] args){
		
		String content = AppUtil.readFile(new File("./routes/guangzhou/6_5_a48c1376fd29b8d2983b981d8b3d5f.json"));
		JSONObject rootObj = JSONObject.fromObject(content);
		JSONArray sceneArr = rootObj.getJSONArray("sceneryList");
		
		ArrayList<Scenery> sceneList = new ArrayList<Scenery>();
		for (int i = 0; i < sceneArr.size(); i++) {
			JSONObject obj = sceneArr.getJSONObject(i);
			Scenery scene = (Scenery)JSONObject.toBean(obj, Scenery.class);
			sceneList.add(scene);
		}
		
//		DPSort sort = new DPSort();
//		sort.init(sceneList);
		
		RouteSort sort = new RouteSort(300, 1000, 0.9, 0.9);
		sort.init(sceneList);
		sort.run();
		System.out.println(sort.getBestLen());
		sort = new RouteSort(500, 1500, 0.99, 0.99);
		sort.init(sceneList);
		sort.run();
		System.out.println(sort.getBestLen());
		
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
