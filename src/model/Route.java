package model;

import java.util.ArrayList;

public class Route{
	
	private String uid;
	private String sid;
	private String surl;
	private String sname;
	private String ambiguitySname;
	private double visitDay;
	private double maxDay;
	private double minDay;
	private double hotness;
	private int viewCount;
	private ArrayList<Scenery> sceneryList;
	private ArrayList<Hotel> hotelList;
	private double sumPrice;
	private double sceneTicket;
	private double hotelPrice;
	private double distance;
	
	public Route(){
		sceneryList = new ArrayList<Scenery>();
		hotelList = new ArrayList<Hotel>();
	}

	
	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public String getSid() {
		return sid;
	}

	public void setSid(String sid) {
		this.sid = sid;
	}

	public String getSurl() {
		return surl;
	}

	public void setSurl(String surl) {
		this.surl = surl;
	}

	public String getSname() {
		return sname;
	}

	public void setSname(String sname) {
		this.sname = sname;
	}

	public String getAmbiguitySname() {
		return ambiguitySname;
	}

	public void setAmbiguitySname(String ambiguitySname) {
		this.ambiguitySname = ambiguitySname;
	}

	public double getVisitDay() {
		return visitDay;
	}

	public void setVisitDay(double visitDay) {
		this.visitDay = visitDay;
	}

	public double getMaxDay() {
		return maxDay;
	}

	public void setMaxDay(double maxDay) {
		this.maxDay = maxDay;
	}

	public double getMinDay() {
		return minDay;
	}

	public void setMinDay(double minDay) {
		this.minDay = minDay;
	}

	public double getHotness() {
		return hotness;
	}

	public void setHotness(double hotness) {
		this.hotness = hotness;
	}

	public int getViewCount() {
		return viewCount;
	}


	public void setViewCount(int viewCount) {
		this.viewCount = viewCount;
	}


	public ArrayList<Scenery> getSceneryList() {
		return sceneryList;
	}

	public void setSceneryList(ArrayList<Scenery> sceneryList) {
		this.sceneryList = sceneryList;
	}
	
	public ArrayList<Hotel> getHotelList() {
		return hotelList;
	}


	public void setHotelList(ArrayList<Hotel> hotelList) {
		this.hotelList = hotelList;
	}


	public double getSumPrice() {
		return sumPrice;
	}

	public void setSumPrice(double sumPrice) {
		this.sumPrice = sumPrice;
	}

	public double getSceneTicket() {
		return sceneTicket;
	}

	public void setSceneTicket(double sceneTicket) {
		this.sceneTicket = sceneTicket;
	}

	public double getHotelPrice() {
		return hotelPrice;
	}

	public void setHotelPrice(double hotelPrice) {
		this.hotelPrice = hotelPrice;
	}

	public double getDistance() {
		return distance;
	}


	public void setDistance(double distance) {
		this.distance = distance;
	}


	@Override
	public boolean equals(Object obj) {
		Route routes = (Route)obj;
		return routes.getUid().equals(this.uid);
	}


	
	

}
