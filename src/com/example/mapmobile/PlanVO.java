package com.example.mapmobile;

public class PlanVO {

	String plan;
	String spot;
	String spotInfo;
	String pid;
	String day;
	String lat;
	String lng;
	String que;
	    
	public String getPlan() {
		return plan;
	}
	public void setPlan(String plan) {
		this.plan = this.plan + "," + plan;
	}
	
	public String getSpot() {
		return spot;
	}
	public void setSpot(String spot) {
		StringBuffer sb = new StringBuffer(this.spot + "," + spot);
		sb.delete(0,sb.indexOf(","));
		this.spot = sb.toString();
	}
	
	public String getSpotInfo() {
		return spotInfo;
	}
	public void setSpotInfo(String spotInfo) {
		StringBuffer sb = new StringBuffer(this.spotInfo + "," + spotInfo);
		sb.delete(0, sb.indexOf(","));
		this.spotInfo = sb.toString();
	}
	
	public String getPid() {
		return pid;
	}
	
	public void setPid(String pid) {
		this.pid = this.pid + ",:" + pid;
	}
	
	public String getDay() {
		return day;
	}
	
	public void setDay(String day) {
		StringBuffer sb = new StringBuffer(this.day = this.day + "," + day);
		sb.delete(0, sb.indexOf(","));
		this.day = sb.toString();
	}
	
	public String getLat() {
		return lat;
	}
	public void setLat(String lat) {
		StringBuffer sb = new StringBuffer(this.lat = this.lat + "," + lat);
		sb.delete(0, sb.indexOf(","));
		this.lat = sb.toString();
	}
	
	public String getLng() {
		return lng;
	}
	public void setLng(String lng) {
		StringBuffer sb = new StringBuffer(this.lng = this.lng + "," + lng);
		sb.delete(0, sb.indexOf(","));
		this.lng = sb.toString();
	}
	
	public String getQue() {
		return que;
	}
	public void setQue(String que) {
		StringBuffer sb = new StringBuffer(this.que = this.que + "," + que);
		sb.delete(0, sb.indexOf(","));
		this.que = sb.toString();
	}
	
	
	public PlanVO(String plan, String spot, String spotInfo, String pid, String day, String lat, String lng, String que) {
		super();
		this.plan = plan;
		this.spot = spot;
		this.spotInfo = spotInfo;
		this.pid = pid;
		this.day = day;
		this.lat = lat;
		this.lng = lng;
		this.que = que;
	}

	public PlanVO() {
		
	}
}