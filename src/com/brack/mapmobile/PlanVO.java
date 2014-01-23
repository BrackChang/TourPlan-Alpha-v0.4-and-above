package com.brack.mapmobile;

public class PlanVO {

	String plan;
	String spot;
	String spotInfo;
	String pid;
	String day;
	String lat;
	String lng;
	String que;
	String planDays;
	String planStart;
	String planEnd;
	String flagFood;
	String flagHotel;
	String flagShop;
	String flagScene;
	String flagTrans;
	
	    
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
		StringBuffer sb = new StringBuffer(this.spotInfo + "¢H" + spotInfo);
		sb.delete(0, sb.indexOf("¢H"));
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
	
	public String getPlanDays() {
		return planDays;
	}
	public void setPlanDays(String planDays) {
		this.planDays = this.planDays + "," + planDays;
	}
	
	public String getPlanStart() {
		return planStart;
	}
	public void setPlanStart(String planStart) {
		this.planStart = this.planStart + "," + planStart;
	}
	
	public String getPlanEnd() {
		return planEnd;
	}
	public void setPlanEnd(String planEnd) {
		this.planEnd = this.planEnd + "," + planEnd;
	}
	
	public String getFlagFood() {
		return flagFood;
	}
	public void setFlagFood(String flagFood) {
		StringBuffer sb = new StringBuffer(this.flagFood = this.flagFood + "," + flagFood);
		sb.delete(0, sb.indexOf(","));
		this.flagFood = sb.toString();
	}
	
	public String getFlagHotel() {
		return flagHotel;
	}
	public void setFlagHotel(String flagHotel) {
		StringBuffer sb = new StringBuffer(this.flagHotel = this.flagHotel + "," + flagHotel);
		sb.delete(0, sb.indexOf(","));
		this.flagHotel = sb.toString();
	}
	
	public String getFlagShop() {
		return flagShop;
	}
	public void setFlagShop(String flagShop) {
		StringBuffer sb = new StringBuffer(this.flagShop = this.flagShop + "," + flagShop);
		sb.delete(0, sb.indexOf(","));
		this.flagShop = sb.toString();
	}
	
	public String getFlagScene() {
		return flagScene;
	}
	public void setFlagScene(String flagScene) {
		StringBuffer sb = new StringBuffer(this.flagScene = this.flagScene + "," + flagScene);
		sb.delete(0, sb.indexOf(","));
		this.flagScene = sb.toString();
	}
	
	public String getFlagTrans() {
		return flagTrans;
	}
	public void setFlagTrans(String flagTrans) {
		StringBuffer sb = new StringBuffer(this.flagTrans = this.flagTrans + "," + flagTrans);
		sb.delete(0, sb.indexOf(","));
		this.flagTrans = sb.toString();
	}
	
	public PlanVO(String plan, String spot, String spotInfo, String pid, String day, String lat, String lng,
					String que, String planStart, String planEnd, String planDays,
					String flagFood, String flagHotel, String flagShop, String flagScene, String flagTrans) {
		super();
		this.plan = plan;
		this.spot = spot;
		this.spotInfo = spotInfo;
		this.pid = pid;
		this.day = day;
		this.lat = lat;
		this.lng = lng;
		this.que = que;
		this.planDays = planDays;
		this.planStart = planStart;
		this.planEnd = planEnd;
		this.flagFood = flagFood;
		this.flagHotel = flagHotel;
		this.flagShop = flagShop;
		this.flagScene = flagScene;
		this.flagTrans = flagTrans;
	}

	public PlanVO() {}
}