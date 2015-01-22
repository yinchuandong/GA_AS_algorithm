package aco.tourold;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;

public class Main {
	
	/**
	 * 从文件中读取景点数据
	 * @param filepath
	 * @return
	 */
	public static ArrayList<Scene> loadData(String filepath){
		ArrayList<Scene> list = new ArrayList<Scene>();
		int count = 0;
		try {
			BufferedReader reader = new BufferedReader(new FileReader(new File(filepath)));
			String buff = null;
			while((buff = reader.readLine()) != null){
				String[] arr = buff.split(",");
				String cityName = arr[0];
				int viewCount = Integer.parseInt(arr[1]);
				double visitDays = Double.parseDouble(arr[2]);
				Scene citys = new Scene(cityName, viewCount, visitDays);
				list.add(citys);
				if(count == 20){
//					break;
				}
				count ++;
			}
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}
	
	public static void main(String[] args) {
		System.out.println("begin");
		long begin = System.currentTimeMillis();
		ArrayList<Scene> list = loadData("./guangzhou.txt");
		ACO aco = new ACO();
		aco.init(list, 1000, 3.0);
		aco.run(1000);
		aco.reportResult();
		
		long end = System.currentTimeMillis();
		long delay = end - begin;
		System.out.println("耗时：" + delay + "ms");
	}
}
