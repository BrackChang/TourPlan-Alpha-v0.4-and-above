package com.brack.mapmobile;

import java.util.ArrayList;
import java.util.HashMap;

public class popList {
	
	ArrayList<HashMap<String, Object>> itemList;
	
	public ArrayList<HashMap<String, Object>> getList() {
		
		return itemList;
	}

	public void setList (String[] latArr, String[] lngArr, String[] spotArr, 
						String[] spotinfoArr,String[] dayArr, String[] queArr,
						String[] flag1Arr, String[] flag2Arr, String[] flag3Arr, String[] flag4Arr, String[] flag5Arr)
	{
		final String[] latList = latArr;
		final String[] lngList = lngArr;
		final String[] spotList = spotArr;
		final String[] spotInfoList = spotinfoArr;
		final String[] dayList = dayArr;
		final String[] queList = queArr;
		final String[] flagFoodList = flag1Arr;
		final String[] flagHotelList = flag2Arr;
		final String[] flagShopList = flag3Arr;
		final String[] flagSceneList = flag4Arr;
		final String[] flagTransList = flag5Arr;
		
		ArrayList<HashMap<String, Object>> itemList = new ArrayList<HashMap<String, Object>>();
		
		for (int i = 0; i < latList.length; i++)
		{
			HashMap<String, Object> planItems = new HashMap<String, Object>();
			
			planItems.put("spot", spotList[i]);
			planItems.put("day", queList[i]+ "-" + "Day:" + dayList[i]);
			/*
			if (flagFoodList[i].equals("1"))
			{
				planItems.put("pic1", R.drawable.food_icon);
			} 
			if (flagHotelList[i].equals("1"))
			{
				planItems.put("pic2", R.drawable.hotel_icon);
			}
			if (flagShopList[i].equals("1"))
			{
				planItems.put("pic3", R.drawable.shopping_icon);
			}
			if (flagSceneList[i].equals("1"))
			{
				planItems.put("pic4", R.drawable.scene_icon);
			}
			if (flagTransList[i].equals("1"))
			{
				planItems.put("pic5", R.drawable.transport_icon);
			}
			*/
			itemList.add(planItems);
		}
		
		this.itemList = itemList;
	}
	
	public popList (ArrayList<HashMap<String, Object>> itemList) {
		super();
		this.itemList = itemList;
	}
	public popList(){}
}
