package model;

import java.util.Comparator;

/**
 * @author yinchuandong
 *
 */
public class Hotel implements Comparable<Hotel>{

	private String sid;
	private String uid;
	private String hotelName;
	private String hotelAddress;
	private String phone;
	private String pic;
	private double commentCount;
	private double commentScore;
	private double lng;
	private double lat;
	private double price;
	
	
	public String getSid() {
		return sid;
	}
	public void setSid(String sid) {
		this.sid = sid;
	}
	public String getUid() {
		return uid;
	}
	public void setUid(String uid) {
		this.uid = uid;
	}
	
	public String getHotelName() {
		return hotelName;
	}
	public void setHotelName(String hotelName) {
		this.hotelName = hotelName;
	}
	
	public String getHotelAddress() {
		return hotelAddress;
	}
	public void setHotelAddress(String hotelAddress) {
		this.hotelAddress = hotelAddress;
	}
	public String getPhone() {
		return phone;
	}
	public void setPhone(String phone) {
		this.phone = phone;
	}
	public String getPic() {
		return pic;
	}
	public void setPic(String pic) {
		this.pic = pic;
	}
	public double getCommentCount() {
		return commentCount;
	}
	public void setCommentCount(double commentCount) {
		this.commentCount = commentCount;
	}
	public double getCommentScore() {
		return commentScore;
	}
	public void setCommentScore(double commentScore) {
		this.commentScore = commentScore;
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
	public double getPrice() {
		return price;
	}
	public void setPrice(double price) {
		this.price = price;
	}
	
	@Override
	public int compareTo(Hotel o) {
		if (this.getPrice() > o.getPrice()) {
			return 1;
		}
		if (this.getPrice() == o.getPrice()) {
			return 0;
		}
		return -1;
	}
	
	
}
