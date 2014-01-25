package com.brack.mapmobile;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

public class OffLineMode extends MapActivity
{
	private double screenSize;
	private int screenWidth;
	private int screenHeight;
	private int cantStart;
	private MapView mapView;
	private MapController mapControl;
	private GeoPoint GP;
	
	private PopupWindow popUp;
	private View view;
	private Spinner userNameSelector;
	private Spinner planSelector;
	private ExpandableListView exSpotList;
	private int currentUser;
	private int currentPlan;
	private ArrayList<HashMap<String, String>> userInfoList, planList;
	private ArrayList<Integer> IDArr;
	private String[] plansArr, planDaysArr, planStartArr, planEndArr;
	private String[] latArr, lngArr, dayArr, queArr, spotArr, spotInfoArr,
					flagFoodArr, flagHotelArr, flagShopArr, flagSceneArr, flagTransArr;
	private String userName;
	private String Pid;
	
	private ExAdapter exAdapter;
	private int dayCount;
	private boolean listExpanded;
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE); //It must setting up before setContentView!!
		setContentView(R.layout.offline);
		
		DisplayMetrics DM = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(DM);
		double diagonalPixels = Math.sqrt((Math.pow(DM.widthPixels, 2) + Math.pow(DM.heightPixels, 2)));
        double screen = diagonalPixels / (160 * DM.density);
        screenSize = screen;
		screenWidth = DM.widthPixels;
        screenHeight = DM.heightPixels;
        Log.i("screenSize", ""+screenSize);
        Log.i("screenWidth", ""+screenWidth);
        Log.i("screenHeight", ""+screenHeight);
        
		File dirs = getDir("offLine",Context.MODE_PRIVATE);
		File[] folders = dirs.listFiles();
		
		Log.i("FolderNumbers", ""+folders.length);
		
		if (folders.length == 0)
			nothingSaved();
		else if (folders.length == 1)
		{
			ArrayList<HashMap<String, String>> Lists = new ArrayList<HashMap<String, String>>();
			
			HashMap<String, String> items = new HashMap<String, String>();

			items.put("userName", folders[0].getName());

			File dirInside = new File(dirs, folders[0].getName());
			File[] numbers = dirInside.listFiles();

			int number = (numbers.length)-1;
			String amount = Integer.toString(number);
			Log.i("planAmount", amount);

			if (number <= 0)
				items.put("amount", "This guy got no plan!");
			else if (number == 1)
				items.put("amount", "Got only " + amount + " plan");
			else
				items.put("amount", "Got " + amount + " plans");

			Lists.add(items);
			
			userInfoList = Lists;
			
			if (userInfoList.get(0).get("amount").contains("no plan"))
			{
				userSpinner(0, false);
				goBackCountDown();
			} else {
				userSpinner(0, true);
			}
		}
		else 
		{
			ArrayList<HashMap<String, String>> Lists = new ArrayList<HashMap<String, String>>();
			for (int i = 0; i < folders.length; i++)
			{
				HashMap<String, String> items = new HashMap<String, String>();
				
				items.put("userName", folders[i].getName());
				
				File dirInside = new File(dirs, folders[i].getName());
				File[] numbers = dirInside.listFiles();
				
				int number = (numbers.length)-1;
				String amount = Integer.toString(number);
				Log.i("planAmount", amount);
				
				if (number <= 0) {
					items.put("amount", "This guy got no plan!");
					cantStart = cantStart +1;
				}
				else if (number == 1)
					items.put("amount", "Got only " + amount + " plan");
				else
					items.put("amount", "Got " + amount + " plans");

				Lists.add(items);
			}
			userInfoList = Lists;
			
			if (cantStart < 2) {
				longMessage("Now, select your user name first!");
				startCountDown();
			} else {
				startCountDown();
				goBackCountDown();
			}
		}
		//findMapControl();
		//mapHalf();
		
	}
	
	public void findMapControl()
	{
		mapView = (MapView) findViewById(R.id.MapView);
		mapView.setBuiltInZoomControls(true);
		mapControl = mapView.getController();
		
		String coordinates[] = {"23.94", "121.00"};
        double lat = Double.parseDouble(coordinates[0]);
        double lng = Double.parseDouble(coordinates[1]);
        
        GP = new GeoPoint(
        		(int)(lat * 1E6),(int)(lng * 1E6));
        
        mapControl.animateTo(GP);
        mapControl.setZoom(8);
	}
	
	public void popUserList()
	{
		TextView userName = (TextView) findViewById(R.id.UserName2);
		userName.setText("Select User");
		
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		view = inflater.inflate(R.layout.popup_list, null);
		ListView user_list = (ListView) view.findViewById(R.id.exList1); 
		RelativeLayout header = (RelativeLayout) findViewById(R.id.OffLineHeader);

		SimpleAdapter userAdapter = new SimpleAdapter
				(OffLineMode.this, userInfoList, R.layout.offline_userlist,
						new String[] {"userName", "amount"}, new int[] {R.id.offlineUserText, R.id.offlineUserInfoText});
		
		user_list.setAdapter(userAdapter);
		
		double width = screenWidth / 1.4;
		double height = screenHeight / 1.3;

		popUp = new PopupWindow (view, (int)width, (int)height);
		
		//popUp.setAnimationStyle(R.style.PopupAnimation);
		popUp.setFocusable(true);
		popUp.setOutsideTouchable(true);
		//popUp.setBackgroundDrawable(new BitmapDrawable());
		
		view.setOnKeyListener(new OnKeyListener() {
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				// TODO Auto-generated method stub
				if ((keyCode == KeyEvent.KEYCODE_BACK) && (popUp.isShowing()))
				{
					popUp.dismiss();
					finish();
					return true;
				}
				return false;
			}
		});
		
		int xpos = screenWidth / 2 - popUp.getWidth() / 2;
		double ypos = (screenHeight / 2) - (popUp.getHeight() / 1.8);
		
		Log.i("Coder", "xPos:" + xpos);
		Log.i("Coder", "yPos:" + ypos);
		
		popUp.showAsDropDown(header, xpos, (int)ypos);
		
		user_list.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				if (userInfoList.get(arg2).get("amount").toString().contains("no plan"))
					longMessage("Nothing inside!\nAt least save a plan for getting start.");
				else {
					userSpinner(arg2, true);
					popUp.dismiss();
				}
			}
		});
	}
	
	public void userSpinner(int i, final boolean hasPlan)
	{
		final TextView nameText = (TextView) findViewById(R.id.UserName2);
		userNameSelector = (Spinner) findViewById(R.id.userNameSelector);
		LinearLayout userLayout = (LinearLayout) findViewById(R.id.userNameLayout);
		
		userLayout.setVisibility(View.VISIBLE);
		
		SimpleAdapter userAdapter = new SimpleAdapter
				(OffLineMode.this, userInfoList, R.layout.offline_userlist_spinner,
							new String[] {"userName", "amount"},
							new int[] {R.id.offlineUserText_spinner, R.id.offlineUserInfoText_spinner});
		userAdapter.setDropDownViewResource(R.layout.offline_userlist_dropdown);
		
		userAdapter.notifyDataSetChanged();
		userNameSelector.setAdapter(userAdapter);
		userNameSelector.setSelection(i);
		nameText.setText(userInfoList.get(i).get("userName"));
		userName = userInfoList.get(i).get("userName");
		currentUser = i;
		if (hasPlan)
		{
			userNameSelector.setOnItemSelectedListener(new OnItemSelectedListener() {
				public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {

					if (userInfoList.get(arg2).get("amount").toString().contains("no plan")) {
						longMessage("No saved data!");
						if (arg2 != currentUser)
							userSpinner(currentUser, true);
						else{
							Log.i("Spinner NO PLAN", "YOOOOO~~");
						}
					} else {
						nameText.setText(userInfoList.get(arg2).get("userName"));
						putPlan(userInfoList.get(arg2).get("userName"));
						currentUser = arg2;
					}
				}
				public void onNothingSelected(AdapterView<?> arg0) {
				}
			});
		}  else {
			ArrayList<HashMap<String, String>> nullList = new ArrayList<HashMap<String, String>>();
			HashMap<String, String> item = new HashMap<String, String>();
			item.put("planName", "Nothing here~");
			item.put("planInfos", "Null");
			nullList.add(item);

			planSelector = (Spinner) findViewById(R.id.planSelector);
			SimpleAdapter noPlanAdapter = new SimpleAdapter(OffLineMode.this, nullList, R.layout.offline_planlist,
					new String[] {"planName", "planInfos"}, 
					new int[] {R.id.offlinePlanText, R.id.offlinePlanInfoText});
			noPlanAdapter.setDropDownViewResource(R.layout.offline_planlist_dropdown);
			planSelector.setAdapter(noPlanAdapter);
		}
	}
	
	@SuppressWarnings("deprecation")
	public void putPlan(String user)
	{
		StringBuilder fileContent_plans = new StringBuilder();
		File dirs = getDir("offLine", Context.MODE_PRIVATE);
		File dir = new File(dirs, user);
		File planListXml = new File(dir, "planList.xml");
		
		try
		{
			FileInputStream FIS_plans = new FileInputStream(planListXml);
			BufferedReader planReader = new BufferedReader(new InputStreamReader(FIS_plans,"UTF-8"));
			
			String planLine = null;
			
			while ((planLine = planReader.readLine()) != null )
			{
				fileContent_plans.append(planLine).append("\n");
			}
			FIS_plans.close();
		}
		catch (OutOfMemoryError e)
		{
			e.printStackTrace();
			Log.e("FileNotFound", e.getMessage().toString());
		}
		catch (Exception e)
		{
			e.printStackTrace();
			Log.e("IOException", e.getMessage().toString());
		}
		String planXML = fileContent_plans.toString();

		File[] planIDs = dir.listFiles();
		List<String> IDList = new ArrayList<String>();
		
		for (int i = 0; i < planIDs.length; i++)
		{
			if (!planIDs[i].getName().contains("planList"))
			{
				String Pid = planIDs[i].getName().toString();
				Pid = Pid.replace("plan", "");
				Pid = Pid.replace(".xml", "");
				IDList.add(Pid);
			}
		}
		String[] originalIDs = IDList.toArray(new String[IDList.size()]);
		
		PlanVO planVO = XmlParser.parse(planXML);
		
		String res = "";
	        StringTokenizer stPlan = new StringTokenizer(planVO.getPlan(),",");
	        StringTokenizer stPid = new StringTokenizer(planVO.getPid(),",");
			stPlan.nextToken();
			stPid.nextToken();
				while (stPlan.hasMoreTokens() & stPid.hasMoreTokens()) {
						res = res + stPid.nextToken()+ " " + stPlan.nextToken() + " ";
				}
			StringBuffer planBuf = new StringBuffer(res);
			planBuf.deleteCharAt(0);
			String res2 = new String(planBuf);
			
			final String[] planArr2 = res2.split(":");
			
			ArrayList<Integer> planInt = new ArrayList<Integer>();
			List<String> plans = new ArrayList<String>();
			
			for (int i = 0; i < originalIDs.length; i++)
			{
				for (int j = 0; j < planArr2.length; j++)
				{
					if (planArr2[j].contains(originalIDs[i]))
					{
						plans.add(planArr2[j]);
						planInt.add(j);
					}
				}
			}
			Log.i("You got", ""+plans);
			Log.i("And ID", ""+planInt);
			String[] planArr = plans.toArray(new String[plans.size()]);
			
			String planDays = planVO.getPlanDays();
			StringBuffer planDaysBuf = new StringBuffer(planDays);
			planDaysBuf.delete(0, 5);
			String planDays2 = new String(planDaysBuf);
			String[] daysList = planDays2.split(",");
			
			String planStart = planVO.getPlanStart();
			StringBuffer planStartBuf = new StringBuffer(planStart);
			planStartBuf.delete(0, 5);
			String planStart2 = new String(planStartBuf);
			String[] startList = planStart2.split(",");
			
			String planEnd = planVO.getPlanEnd();
			StringBuffer planEndBuf = new StringBuffer(planEnd);
			planEndBuf.delete(0, 5);
			String planEnd2 = new String(planEndBuf);
			String[] endList = planEnd2.split(",");
			
			for (int i = 0; i < endList.length; i++)
			{
				endList[i] = endList[i].replace(Integer.toString(new Date().getYear()+1900)+"-", "");
			}
			
			plansArr = planArr;
			planDaysArr = daysList;
			planStartArr = startList;
			planEndArr = endList;
			IDArr = planInt;
			
			ArrayList<HashMap<String, String>> planListArr = new ArrayList<HashMap<String, String>>();
			
			for (int i = 0; i < plansArr.length; i++)
			{
				HashMap<String, String> planItems = new HashMap<String, String>();
				
				planItems.put("planName", plansArr[i]);
				
				for (int j = 0; j < planDaysArr.length; j++)
				{
					if (IDArr.get(i) == j)
					{
						if (planDaysArr[j].equals("1")) {
							planItems.put("planInfos","Total " + planDaysArr[j] +" Day," + " On " + planStartArr[j]);
						} else {
							planItems.put("planInfos","Total " + planDaysArr[j] +" Days,"+ "\n" + "From " + planStartArr[j]
									+ " to " + planEndArr[j]);
						}
					}
				}
				planListArr.add(planItems);
			}
			planList = planListArr;
			
			getPlan();
	}
	
	public void getPlan()
	{
		planSelector = (Spinner) findViewById(R.id.planSelector);
		SimpleAdapter planAdapter = new SimpleAdapter(OffLineMode.this, planList, R.layout.offline_planlist,
									new String[] {"planName", "planInfos"}, 
									new int[] {R.id.offlinePlanText, R.id.offlinePlanInfoText});
		planAdapter.setDropDownViewResource(R.layout.offline_planlist_dropdown);
		planSelector.setAdapter(planAdapter);
		
		planSelector.setOnItemSelectedListener(new OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3)
			{
				if (planSelector.getSelectedItem().toString().contains("Null") ||
						userInfoList.get(currentUser).get("amount").contains("no plan"))
				{
					Log.i("No More Plan","YOOOOOOO~");
					LinearLayout planLayout = (LinearLayout) findViewById(R.id.PlanLayout);
					planLayout.setVisibility(View.GONE);
				} else
				{
					currentPlan = arg2;
					String pid = planList.get(arg2).get("planName").toString();
					pid = pid.substring(0, pid.indexOf(" "));
					Pid = pid;

					File dir = getDir("offLine", Context.MODE_PRIVATE);
					File userFolder = new File(dir, userInfoList.get(currentUser).get("userName"));
					File spotListXml = new File(userFolder, "plan" + pid + ".xml");
					StringBuilder fileContent_spots = new StringBuilder();

					try
					{
						FileInputStream FIS_spots = new FileInputStream(spotListXml);
						BufferedReader planReader = new BufferedReader(new InputStreamReader(FIS_spots,"UTF-8"));

						String planLine = null;

						while ((planLine = planReader.readLine()) != null )
						{
							fileContent_spots.append(planLine).append("\n");
						}
						FIS_spots.close();
					}
					catch (OutOfMemoryError e)
					{
						e.printStackTrace();
						Log.e("FileNotFound", e.getMessage().toString());
					}
					catch (Exception e)
					{
						e.printStackTrace();
						Log.e("IOException", e.getMessage().toString());
					}
					String spotXML = fileContent_spots.toString();

					PlanVO pidVO = XmlParser.parse(spotXML);

					String Lat = pidVO.getLat();
					StringBuffer LatBuf = new StringBuffer(Lat);
					LatBuf.deleteCharAt(0);
					String Lat2 = new String(LatBuf);

					String Lng = pidVO.getLng();
					StringBuffer LngBuf = new StringBuffer(Lng);
					LngBuf.deleteCharAt(0);
					String Lng2 = new String(LngBuf);

					String Spot = pidVO.getSpot();
					StringBuffer SpotBuf = new StringBuffer(Spot);
					SpotBuf.deleteCharAt(0);
					String Spot2 = new String(SpotBuf);

					String SpotInfo = pidVO.getSpotInfo();
					StringBuffer SpotInfoBuf = new StringBuffer(SpotInfo);
					SpotInfoBuf.deleteCharAt(0);
					String SpotInfo2 = new String(SpotInfoBuf);

					String Day = pidVO.getDay();
					StringBuffer DayBuf = new StringBuffer(Day);
					DayBuf.deleteCharAt(0);
					String Day2 = new String(DayBuf);

					String Que = pidVO.getQue();
					StringBuffer QueBuf = new StringBuffer(Que);
					QueBuf.deleteCharAt(0);
					String Que2 = new String(QueBuf);

					String flagFood = pidVO.getFlagFood();
					StringBuffer flagFoodBuf = new StringBuffer(flagFood);
					flagFoodBuf.deleteCharAt(0);
					String flagFood2 = new String(flagFoodBuf);

					String flagHotel = pidVO.getFlagHotel();
					StringBuffer flagHotelBuf = new StringBuffer(flagHotel);
					flagHotelBuf.deleteCharAt(0);
					String flagHotel2 = new String(flagHotelBuf);

					String flagShop = pidVO.getFlagShop();
					StringBuffer flagShopBuf = new StringBuffer(flagShop);
					flagShopBuf.deleteCharAt(0);
					String flagShop2 = new String(flagShopBuf);

					String flagScene = pidVO.getFlagScene();
					StringBuffer flagSceneBuf = new StringBuffer(flagScene);
					flagSceneBuf.deleteCharAt(0);
					String flagScene2 = new String(flagSceneBuf);

					String flagTrans = pidVO.getFlagTrans();
					StringBuffer flagTransBuf = new StringBuffer(flagTrans);
					flagTransBuf.deleteCharAt(0);
					String flagTrans2 = new String(flagTransBuf);

					String[] latList = Lat2.split(",");
					String[] lngList = Lng2.split(",");
					String[] spotList = Spot2.split(",");
					String[] spotInfoList = SpotInfo2.split("¢H");
					String[] dayList = Day2.split(",");
					String[] queList = Que2.split(",");
					String[] flagFoodList = flagFood2.split(",");
					String[] flagHotelList = flagHotel2.split(",");
					String[] flagShopList = flagShop2.split(",");
					String[] flagSceneList = flagScene2.split(",");
					String[] flagTransList = flagTrans2.split(",");

					latArr = latList;
					lngArr = lngList;
					dayArr = dayList;
					queArr = queList;
					spotArr = spotList;
					spotInfoArr = spotInfoList;
					flagFoodArr = flagFoodList;
					flagHotelArr = flagHotelList;
					flagShopArr = flagShopList;
					flagSceneArr = flagSceneList;
					flagTransArr = flagTransList;

					showSpotList();
				}
			}
			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});
	}
	
	public void showSpotList()
	{
		LinearLayout planLayout = (LinearLayout) findViewById(R.id.PlanLayout);
		ImageButton listControlBtn = (ImageButton) findViewById(R.id.offlineListControlBtn);
		TableRow dayRow = (TableRow) findViewById(R.id.offlineDayRow);
		exSpotList = (ExpandableListView) findViewById(R.id.exSpotList);
		
		planLayout.setVisibility(View.VISIBLE);
   		listControlBtn.setVisibility(View.VISIBLE);
		
		double btnHeight = screenHeight / 16;
		int textSize = screenHeight / 55;
		
		Log.i("ButtonHeight1", ""+btnHeight);
		Log.i("TextSize1", ""+textSize);
		
		if (screenSize < 6.5)
		{
			if (screenWidth >= 480 && screenWidth < 720)
			{
				textSize = 13;
				btnHeight = 52;
			} 
			else if (screenWidth >= 720 && screenWidth < 800)
			{
				textSize = 16;
			}
			else
			{
				textSize = 16;
				btnHeight = 108;
			}
		}
		else {
			if (screenWidth <= 800)
			{
				textSize = 24;
				btnHeight = 68;
			} else
			{
				textSize = 25;
				btnHeight = 68;
			}
		}
		Log.i("TextSize2", ""+textSize);
		Log.i("ButtonHeight2", ""+btnHeight);
		
		LayoutParams listBtnParams = listControlBtn.getLayoutParams();
		listBtnParams.height = (int) (btnHeight);
		listControlBtn.setLayoutParams(listBtnParams);
		
		dayRow.removeAllViews();
		
		final List<Map<String, Object>> spotGroup = new ArrayList<Map<String, Object>>();
		final List<List<Map<String, String>>> spotChild = new ArrayList<List<Map<String, String>>>();
		
		Button allDayBtn = new Button(OffLineMode.this);
		allDayBtn.setText("ALL");
		allDayBtn.setTextColor(getResources().getColor(R.drawable.Ivory));
		allDayBtn.setGravity(Gravity.CENTER);
		allDayBtn.setTextSize(textSize);
		
		dayRow.addView(allDayBtn);
		
		LayoutParams params = allDayBtn.getLayoutParams();
		params.width = LayoutParams.WRAP_CONTENT;
		params.height = (int) btnHeight;
		allDayBtn.setLayoutParams(params);
		
		allDayBtn.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				exAdapter.filterData("");
			}
		});
		
		boolean day0Tag = true;
		dayCount = 1;
		
		for (int i = 0; i < spotArr.length; i++)
		{
			Map<String, Object> spotItem = new HashMap<String, Object>();
			
			spotItem.put("title", spotArr[i]);
			spotItem.put("titleDescribe", queArr[i]+ "-" + "DAY " + dayArr[i]);
			spotItem.put("lat", latArr[i]);
			spotItem.put("lng", lngArr[i]);
			
			if (flagFoodArr[i].equals("1"))
			{
				spotItem.put("pic1", "1");
			} 
			if (flagHotelArr[i].equals("1"))
			{
				spotItem.put("pic2", "1");
			}
			if (flagShopArr[i].equals("1"))
			{
				spotItem.put("pic3", "1");
			}
			if (flagSceneArr[i].equals("1"))
			{
				spotItem.put("pic4", "1");
			}
			if (flagTransArr[i].equals("1"))
			{
				spotItem.put("pic5", "1");
			}
			
			spotGroup.add(spotItem);
			
			List<Map<String, String>> spotInfos = new ArrayList<Map<String, String>>();
			Map<String, String> spotInfo = new HashMap<String, String>();
			
			spotInfo.put("info", "Info: \n" + spotInfoArr[i]);
			spotInfos.add(spotInfo);
			spotChild.add(spotInfos);
			
			String d = Integer.toString(dayCount);
			if (dayArr[i].equals(d))
			{
				final Button dayBtn = new Button(OffLineMode.this);
				dayBtn.setText("Day " + dayArr[i]);
				dayBtn.setTextColor(getResources().getColor(R.drawable.Ivory));
				dayBtn.setGravity(Gravity.CENTER);
				dayBtn.setTextSize(textSize);
				
				dayRow.addView(dayBtn);
				dayCount++;
				dayBtn.setLayoutParams(params);
				
				dayBtn.setOnClickListener(new OnClickListener() {
					public void onClick(View v) {
						exAdapter.filterData(dayBtn.getText().toString());
					}
				});
			} else if (day0Tag)
			{
				if (dayArr[i].equals("0"))
				{
					final Button dayBtn = new Button(OffLineMode.this);
					dayBtn.setText("Day " + dayArr[i]);
					dayBtn.setTextColor(getResources().getColor(R.drawable.Ivory));
					dayBtn.setGravity(Gravity.CENTER);
					dayBtn.setTextSize(textSize);

					dayRow.addView(dayBtn);
					dayBtn.setLayoutParams(params);

					day0Tag = false;

					dayBtn.setOnClickListener(new OnClickListener() {
						public void onClick(View v) {
							exAdapter.filterData(dayBtn.getText().toString());
						}
					});
				}
			}
		}
		exAdapter = new ExAdapter(this, spotGroup, spotChild, false, userName, Pid);
   		exSpotList.setIndicatorBounds(0, 20);
   		exSpotList.setAdapter(exAdapter);
	}
	
	public void userDeleteClick(View v)
	{
		AlertDialog.Builder infoDialog = new AlertDialog.Builder(this);
		
		TextView title = new TextView(this);
		title.setText("Delete User (" + userInfoList.get(currentUser).get("userName") + ")");
		title.setTextColor(getResources().getColor(R.color.DeepSkyBlue));
		title.setGravity(Gravity.CENTER);
		
		if (screenSize >= 6.5)
		{
			title.setPadding(0, 15, 0, 15);
			title.setTextSize(30);
			//title.setTypeface(null,Typeface.BOLD);
			infoDialog.setCustomTitle(title);
		} else {
			title.setPadding(0, 15, 0, 15);
			title.setTextSize(22);
			infoDialog.setCustomTitle(title);
		}

		infoDialog.setMessage("It will delete this user and all of the saved data of him, are you sure about this?");
		infoDialog.setPositiveButton("YES", 
				new DialogInterface.OnClickListener()
				{
					public void onClick(DialogInterface dialog, int which)
					{
						File dir = getDir("offLine", Context.MODE_PRIVATE);
						File userDir = new File (dir, userInfoList.get(currentUser).get("userName"));
						File[] allData = userDir.listFiles();
						for (int i = 0; i < allData.length; i++)
						{
							allData[i].delete();
						}
						userDir.delete();
						
						userInfoList.remove(currentUser);
						
						if (userInfoList.isEmpty()) {
							nothingSaved();
						} else {
							int afterRemove;
							if (currentUser == 0)
								afterRemove = 0;
							else
								afterRemove = currentUser -1;

							if (userInfoList.get(afterRemove).get("amount").contains("no plan"))
								userSpinner(afterRemove, false);
							else
								userSpinner(afterRemove, true);
						}
					}
				});
		infoDialog.setNegativeButton("Please Don't¡I", new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int which) {
			}
		});
		
		AlertDialog dialog = infoDialog.create();
		dialog.show();
		dialog.getWindow().getAttributes();
		
		TextView msgText = (TextView) dialog.findViewById(android.R.id.message);
		Button positive = (Button) dialog.getButton(DialogInterface.BUTTON_POSITIVE);
		Button negative = (Button) dialog.getButton(DialogInterface.BUTTON_NEGATIVE);
		positive.setTextColor(getResources().getColor(R.drawable.DarkOrange));
		negative.setTextColor(getResources().getColor(R.drawable.Brown));
		
		if (screenSize >= 6.5)
		{
			msgText.setTextSize(28);
			msgText.setPadding(10, 15, 10, 15);
			positive.setTextSize(28);
			negative.setTextSize(28);
		} else {
			msgText.setTextSize(18);
			msgText.setPadding(10, 15, 10, 15);
			positive.setTextSize(18);
			negative.setTextSize(18);
		}
	}
	
	public void planDeleteClick(View v)
	{
		planSelector = (Spinner) findViewById(R.id.planSelector);
		if (planSelector.getSelectedItem().toString().contains("Null"))
		{
			shortMessage("Ker Ker Ker....");
		}
		else
		{
			AlertDialog.Builder infoDialog = new AlertDialog.Builder(this);

			TextView title = new TextView(this);
			title.setText("Delete Plan");
			title.setTextColor(getResources().getColor(R.color.DeepSkyBlue));
			title.setGravity(Gravity.CENTER);

			if (screenSize >= 6.5)
			{
				title.setPadding(0, 15, 0, 15);
				title.setTextSize(30);
				//title.setTypeface(null,Typeface.BOLD);
				infoDialog.setCustomTitle(title);
			} else {
				title.setPadding(0, 15, 0, 15);
				title.setTextSize(22);
				infoDialog.setCustomTitle(title);
			}

			String planTitle = planList.get(currentPlan).get("planName");
			planTitle = planTitle.substring(planTitle.indexOf(" "), planTitle.lastIndexOf(" ")).trim();

			infoDialog.setMessage("This will delete¡u"
					+ planTitle + "¡v, are you sure?");
			infoDialog.setPositiveButton("YES", 
					new DialogInterface.OnClickListener()
			{
				public void onClick(DialogInterface dialog, int which)
				{
					String pid = planList.get(currentPlan).get("planName");
					pid = pid.substring(0, pid.indexOf(" "));

					File dir = getDir("offLine", Context.MODE_PRIVATE);
					File userDir = new File (dir, userInfoList.get(currentUser).get("userName"));
					File planFile = new File (userDir, "plan" + pid + ".xml");
					planFile.delete();
					planList.remove(currentPlan);

					File[] folders = dir.listFiles();
					ArrayList<HashMap<String, String>> Lists = new ArrayList<HashMap<String, String>>();
					for (int i = 0; i < folders.length; i++)
					{
						HashMap<String, String> items = new HashMap<String, String>();

						items.put("userName", folders[i].getName());

						File dirInside = new File(dir, folders[i].getName());
						File[] numbers = dirInside.listFiles();
						int number = (numbers.length)-1;
						String amount = Integer.toString(number);
						Log.i("planAmount", amount);

						if (number <= 0)
							items.put("amount", "This guy got no plan!");
						else if (number == 1)
							items.put("amount", "Got only " + amount + " plan");
						else
							items.put("amount", "Got " + amount + " plans");

						Lists.add(items);
					}
					userInfoList = Lists;


					if (planList.isEmpty())
					{
						longMessage("It's all GONE!!!");

						ArrayList<HashMap<String, String>> nullList = new ArrayList<HashMap<String, String>>();
						HashMap<String, String> item = new HashMap<String, String>();
						item.put("planName", "Nothing here~");
						item.put("planInfos", "Null");
						nullList.add(item);

						planSelector = (Spinner) findViewById(R.id.planSelector);
						SimpleAdapter noPlanAdapter = new SimpleAdapter(OffLineMode.this, nullList, R.layout.offline_planlist,
								new String[] {"planName", "planInfos"}, 
								new int[] {R.id.offlinePlanText, R.id.offlinePlanInfoText});
						noPlanAdapter.setDropDownViewResource(R.layout.offline_planlist_dropdown);
						planSelector.setAdapter(noPlanAdapter);

						userSpinner(currentUser, true);
					} else {
						userSpinner(currentUser, true);
					}
				}
			});
			infoDialog.setNegativeButton("NO", new DialogInterface.OnClickListener()
			{
				public void onClick(DialogInterface dialog, int which) {
				}
			});

			AlertDialog dialog = infoDialog.create();
			dialog.show();
			dialog.getWindow().getAttributes();

			TextView msgText = (TextView) dialog.findViewById(android.R.id.message);
			Button positive = (Button) dialog.getButton(DialogInterface.BUTTON_POSITIVE);
			Button negative = (Button) dialog.getButton(DialogInterface.BUTTON_NEGATIVE);
			positive.setTextColor(getResources().getColor(R.drawable.DarkOrange));
			negative.setTextColor(getResources().getColor(R.drawable.Brown));

			if (screenSize >= 6.5)
			{
				msgText.setTextSize(28);
				msgText.setPadding(10, 15, 10, 15);
				positive.setTextSize(28);
				negative.setTextSize(28);
			} else {
				msgText.setTextSize(18);
				msgText.setPadding(10, 15, 10, 15);
				positive.setTextSize(18);
				negative.setTextSize(18);
			}
		}
	}
	
	public void popUpClick(View v)
	{
		shortMessage("It's Not ready yet!");
	}
	
 	public void startCountDown()
   	{
   		new CountDownTimer(1000,1000){
   			@Override
   			public void onFinish() {
   				// TODO Auto-generated method stub
   				popUserList();
   			}
   			@Override
   			public void onTick(long millisUntilFinished) {
   				// TODO Auto-generated method stub
   			}
   		}.start();
   	}
 	
 	public void goBackCountDown()
   	{
   		new CountDownTimer(5000,1000){
   			@Override
   			public void onFinish() {
   				// TODO Auto-generated method stub
   				popUp = new PopupWindow();
   				if (popUp.isShowing())
   					popUp.dismiss();
   				nothingSaved();
   			}
   			@Override
   			public void onTick(long millisUntilFinished) {
   				// TODO Auto-generated method stub
   			}
   		}.start();
   	}
 	
 	public void nothingSaved()
 	{
 		AlertDialog.Builder warning = new AlertDialog.Builder(OffLineMode.this);
		
		TextView title = new TextView(this);
		title.setText("Oops!");
		title.setTextColor(getResources().getColor(R.color.DeepSkyBlue));
		title.setGravity(Gravity.CENTER);
		title.setPadding(0, 20, 0, 20);
		
		if (screenSize >= 6.5)
		{
			title.setTextSize(30);
			warning.setCustomTitle(title);
		} else {
			title.setTextSize(22);
			warning.setCustomTitle(title);
		}
		warning.setMessage("You haven't saved any user's plan yet!")
		.setPositiveButton("GO BACK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				finish();
			}
		});
		AlertDialog dialog = warning.create();
		dialog.show();
		dialog.setCancelable(false);
		dialog.getWindow().getAttributes();
		
		TextView msgText = (TextView) dialog.findViewById(android.R.id.message);
		msgText.setGravity(Gravity.CENTER);
		Button positive = (Button) dialog.getButton(DialogInterface.BUTTON_POSITIVE);
		positive.setTextColor(getResources().getColor(R.drawable.DarkOrange));
		
		if (screenSize >= 6.5)
		{
			msgText.setTextSize(28);
			msgText.setPadding(10, 15, 10, 15);
			positive.setTextSize(28);
			
		} else {
			msgText.setTextSize(18);
			msgText.setPadding(10, 15, 10, 15);
			positive.setTextSize(18);
		}
 	}
 	
 	public void listControl(View listCtrl)
	{
		int spotCount = exSpotList.getExpandableListAdapter().getGroupCount();
		Log.i("SpotCount", ""+spotCount);
		ImageButton listControl = (ImageButton) findViewById(R.id.offlineListControlBtn);
		
		if (!listExpanded)
		{
			for (int i = 0; i < spotCount; i++)
			{
				exSpotList.expandGroup(i);
			}
			listExpanded = true;
			listControl.setImageResource(R.drawable.list_collapse);
		} else
		{
			for (int i = 0; i < spotArr.length; i++)
			{
				exSpotList.collapseGroup(i);
			}
			listExpanded = false;
			listControl.setImageResource(R.drawable.list_expand);
		}
	}
 	
 	public void mapHalf()
	{
		double height = screenHeight / 3;
		
		LinearLayout mapArea = (LinearLayout) findViewById(R.id.offLineMapLayout);
		LinearLayout.LayoutParams half = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, (int)height);
		
		mapArea.setVisibility(View.VISIBLE);
		mapArea.setLayoutParams(half);
		
	}
	
	public void goBackClick(View v)
	{
		finish();
	}
	
	public void userNameClick(View v)
	{
		TextView userName = (TextView) findViewById(R.id.UserName2);
		longMessage("Welcome " + userName.getText().toString() + " (£½_>£¿)");
	}
	
	private void shortMessage (String message)
    {
		View toastRoot = getLayoutInflater().inflate(R.layout.toast, null);
    	TextView toastText = (TextView) toastRoot.findViewById(R.id.myToast);
    	toastText.setText(message);
    	
    	Toast toastStart = new Toast(OffLineMode.this);
    	toastStart.setGravity(Gravity.BOTTOM, 0, 60);
    	toastStart.setDuration(Toast.LENGTH_SHORT);
    	toastStart.setView(toastRoot);
    	toastStart.show();
    }
    private void longMessage (String message)
    {
    	View toastRoot = getLayoutInflater().inflate(R.layout.toast, null);
    	TextView toastText = (TextView) toastRoot.findViewById(R.id.myToast);
    	toastText.setText(message);
    	
    	Toast toastStart = new Toast(OffLineMode.this);
    	toastStart.setGravity(Gravity.BOTTOM, 0, 60);
    	toastStart.setDuration(Toast.LENGTH_LONG);
    	toastStart.setView(toastRoot);
    	toastStart.show();
    }

	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}
}
