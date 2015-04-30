package model;

public class Scenery implements Comparable<Scenery>{

	private String sid;
	private String surl;
	private String sname;
	private String ambiguitySname;
	private String parentSid;
	private String moreDesc;
	private String fullUrl;
	private int viewCount;
	private int goingCount;
	private int goneCount;
	private double rating;
	private double lng;
	private double lat;
	private double mapX;
	private double mapY;
	private double price;
	private double visitDay;
	
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
	public String getParentSid() {
		return parentSid;
	}
	public void setParentSid(String parentSid) {
		this.parentSid = parentSid;
	}
	public String getMoreDesc() {
		return moreDesc;
	}
	public void setMoreDesc(String moreDesc) {
		this.moreDesc = moreDesc;
	}
	public String getFullUrl() {
		return fullUrl;
	}
	public void setFullUrl(String fullUrl) {
		this.fullUrl = fullUrl;
	}
	public int getViewCount() {
		return viewCount;
	}
	public void setViewCount(int viewCount) {
		this.viewCount = viewCount;
	}
	public int getGoingCount() {
		return goingCount;
	}
	public void setGoingCount(int goingCount) {
		this.goingCount = goingCount;
	}
	public int getGoneCount() {
		return goneCount;
	}
	public void setGoneCount(int goneCount) {
		this.goneCount = goneCount;
	}
	public double getRating() {
		return rating;
	}
	public void setRating(double rating) {
		this.rating = rating;
	}
	public double getLng() {
		return lng;
	}
	public void setLng(double lng) {
		this.lng = lng;
	}
	public double getLat() {
		return lat;
	}
	public void setLat(double lat) {
		this.lat = lat;
	}
	public double getMapX() {
		return mapX;
	}
	public void setMapX(double mapX) {
		this.mapX = mapX;
	}
	public double getMapY() {
		return mapY;
	}
	public void setMapY(double mapY) {
		this.mapY = mapY;
	}
	public double getPrice() {
		return price;
	}
	public void setPrice(double price) {
		this.price = price;
	}
	public double getVisitDay() {
		return visitDay;
	}
	public void setVisitDay(double visitDay) {
		this.visitDay = visitDay;
	}
	
	@Override
	public int compareTo(Scenery s1) {
		if (this.getViewCount() >= s1.getViewCount()) {
			return 1;
		}else{
			return -1;
		}
	}
	
	
}
