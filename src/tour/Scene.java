package tour;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

public class Scene {
	
	String cityName;
	int viewCount;
	double visitDays;
	
	public Scene(String cityName, int viewCount, double visitDays) {
		super();
		this.cityName = cityName;
		this.viewCount = viewCount;
		this.visitDays = visitDays;
	}

	public void setCityName(String cityName) {
		this.cityName = cityName;
	}

	public void setViewCount(int viewCount) {
		this.viewCount = viewCount;
	}
	
	public void setVisitDays(double visitDays) {
		this.visitDays = visitDays;
	}

	public String getCityName() {
		return cityName;
	}

	public int getViewCount() {
		return viewCount;
	}

	public double getVisitDays() {
		return visitDays;
	}

	
}
