package com.example.mapmobile;

public class PlanVO {

	String plan;
	String spot;
	String loc;
	String pid;
	String spotPid;
	String lat;
	String lng;
	    
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
		//this.spot = this.spot + "," + spot;
	}
	
	public String getLoc() {
		return loc;
	}
	public void setLoc(String loc) {
		this.loc = this.loc + "," + loc;
	}
	
	public String getPid() {
		return pid;
	}
	
	public void setPid(String pid) {
		this.pid = this.pid + ",:" + pid;
	}
	
	public String getSpotPid() {
		return spotPid;
	}
	
	public void setSpotPid(String spotPid) {
		this.spotPid = this.spotPid + ",:" + spotPid;
	}
	
	public String getLat() {
		return lat;
	}
	public void setLat(String lat) {
		this.lat = this.lat + "," + lat;
	}
	
	public String getLng() {
		return lng;
	}
	public void setLng(String lng) {
		this.lng = this.lng + "," + lng;
	}
	
	
	public PlanVO(String plan, String spot, String loc, String pid, String spotPid, String lat, String lng) {
		super();
		this.plan = plan;
		this.spot = spot;
		this.loc = loc;
		this.pid = pid;
		this.spotPid = spotPid;
		this.lat = lat;
		this.lng = lng;
	}

	public PlanVO() {
		
	}
}