package com.brack.mapmobile;

public class Poi {

	private String name;
	private double lat;
	private double lng;
	private double distance;
	
	public Poi (String name, double latitude, double longitude)
	{
		this.name = name;
		this.lat = latitude;
		this.lng = longitude;
	}
	
	public String getName()
	{
		return name;
	}
	
	public double getLatitude()
	{
		return lat;
	}
	
	public double getLongitude()
	{
		return lng;
	}
	
	public void setDistance(double distance)
	{
		this.distance = distance;
	}
	
	public double getDistance()
	{
		return distance;
	}
	
}
