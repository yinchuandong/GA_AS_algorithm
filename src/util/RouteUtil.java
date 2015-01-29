package util;

import java.util.ArrayList;
import java.util.List;

import model.Route;

public class RouteUtil {

	/**
	 * calculate the average value of hotness
	 * @param top30List
	 * @return
	 */
	public static double caclAvgHotness(List<Route> top30List){
		double avg = 0.0;
		for (Route route : top30List) {
			avg += route.getHotness();
		}
		avg = avg / top30List.size();
		return avg;
	}
}
