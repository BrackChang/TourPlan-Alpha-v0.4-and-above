package com.brack.mapmobile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import com.brack.mapmobile.R;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.Overlay;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Debug;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.SimpleAdapter;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

@SuppressLint("HandlerLeak")
public class Map2Activity extends MapActivity 
{    
    private MapView mapView; 
    private MapController mapControl;
    private MyLocationOverlay myLayer;
    private GeoPoint gp;
    private MapOverlay Marker;
    private SearchOverlay locMarker;
    private ListView PlanList;
    private PopupWindow popUp;
    private View view;
    private AutoCompleteTextView typingText;
    private long exitTime = 0;
    private String tourURL = "http://140.128.198.44:406/plandata/";
    protected static final int REFRESH_DATA = 0x00000001;
    
    private String MyName;
    private String[] planArr;
    private String[] planDaysArr;
    private String[] planStartArr;
    private String[] planEndArr;
    private String[] latArr;
    private String[] lngArr;
    private String[] spotArr;
    private String[] spotInfoArr;
    private ArrayList<HashMap<String, String>> planListArr;
    private ArrayList<HashMap<String, Object>> spotListArr;
    @SuppressWarnings("deprecation")
	int version = Integer.parseInt(VERSION.SDK);
    private static boolean startNeed = false;
    int count = 0;
    
    /** Called when the activity is first created. **/
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        Debug.startMethodTracing("report");
        setContentView(R.layout.map);
        this.setTitle(R.string.VersionName);
        Debug.stopMethodTracing();
        
        findMapControl();
        
        typingText = (AutoCompleteTextView)findViewById(R.id.typingText);
        TextView UserName = (TextView)findViewById(R.id.UserName);
        
        Bundle userName = this.getIntent().getExtras();		//Obtain Bundle
        String Name = userName.getString("name").toString();
        MyName = new String(Name);
        UserName.setText(MyName);								//Output the contents of Bundle
        
    	String xmlURL = tourURL + Name;
    	
    	Thread th = new Thread(new sendItToRun(xmlURL));
    	th.start();
    	
    	typingText.setOnKeyListener(searchKey);
    }
    
    private void findMapControl()
    {
    	mapView = (MapView) findViewById(R.id.MapView);
        mapView.setBuiltInZoomControls(true);
        mapView.setSatellite(false);
        
        mapControl = mapView.getController();
        String coordinates[] = {"23.94", "121.00"};
        double lat = Double.parseDouble(coordinates[0]);
        double lng = Double.parseDouble(coordinates[1]);
        
        gp = new GeoPoint(
        		(int)(lat * 1E6),(int)(lng * 1E6));
        
        mapControl.animateTo(gp);
        mapControl.setZoom(8);
        
        /* Find My Location */
        findMyLocation();
        /* Find My Location */
    }
    
    private void findMyLocation()
    {
    	List<Overlay> locOverlays = mapView.getOverlays();
        myLayer = new MyLocationOverlay(this, mapView);
        
        myLayer.enableCompass();
        myLayer.enableMyLocation();
        myLayer.runOnFirstFix(new Runnable()
        {
        	public void run()
        	{
        		mapControl.animateTo(myLayer.getMyLocation());
        	}
        });
        locOverlays.add(myLayer);
        
        mapView.invalidate();
    }

    Handler planMove = new Handler()
   	{
   		@Override
   		public void handleMessage(Message msg)
   		{
   			switch (msg.what)
   			{
   			//Display the catch data from Internet.
   			case REFRESH_DATA:
   				
   				String xmlString = null;
   				
   				if (msg.obj instanceof String)
   					xmlString = (String) msg.obj;
   				
   		        typingText = (AutoCompleteTextView)findViewById(R.id.typingText);
				typingText.setFocusable(true);
		        typingText.setFocusableInTouchMode(true);
   				
   				PlanVO planVO = XmlParser.parse(xmlString);
   				
   		        String res = "";
   		        StringTokenizer stPlan = new StringTokenizer(planVO.getPlan(),",");
   		        StringTokenizer stPid = new StringTokenizer(planVO.getPid(),",");
   				stPlan.nextToken();
   				stPid.nextToken();
   					while (stPlan.hasMoreTokens() & stPid.hasMoreTokens()) {
   							res = res + stPid.nextToken()+ " " + stPlan.nextToken();
   					}
   				StringBuffer planBuf = new StringBuffer(res);
   				planBuf.deleteCharAt(0);
   				String res2 = new String(planBuf);
   				
   				final String[] plans = res2.split(":");
   				
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
   				
   				planArr = plans;
   				planDaysArr = daysList;
   				planStartArr = startList;
   				planEndArr = endList;
   				
   				startCountDown();
   				
   				Toast.makeText(Map2Activity.this, "Now, Please choose your plan!", Toast.LENGTH_LONG).show();
   				
   				break;
   			}
   		}
   	};
   	
   	Handler planChosen = new Handler()
   	{
   		@Override
   		public void handleMessage(Message url)
   		{
   			switch (url.what)
   			{
   			case REFRESH_DATA:
   				
   				String xmlPidString = null;
   				
   				if (url.obj instanceof String)
   					xmlPidString = (String) url.obj;
   					
   					PlanVO pidVO = XmlParser.parse(xmlPidString);
						
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
					String[] spotInfoList = SpotInfo2.split(",");
					String[] dayList = Day2.split(",");
					String[] queList = Que2.split(",");
					String[] flagFoodList = flagFood2.split(",");
					String[] flagHotelList = flagHotel2.split(",");
					String[] flagShopList = flagShop2.split(",");
					String[] flagSceneList = flagScene2.split(",");
					String[] flagTransList = flagTrans2.split(",");
					
					showSpot(latList, lngList, spotList, spotInfoList, dayList, queList,
							flagFoodList, flagHotelList, flagShopList, flagSceneList, flagTransList);
					
					latArr = latList;
					lngArr = lngList;
					spotArr = spotList;
					spotInfoArr = spotInfoList;
					
					putSpotArr(latList, lngList, spotList, spotInfoList, dayList, queList,
							flagFoodList, flagHotelList, flagShopList, flagSceneList, flagTransList);
					startNeed = true;
   			}
   		}
   	};
   	
   	
   	public void showSpot (String[] latArr, String[] lngArr, String[] spotArr, 
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

		typingText = (AutoCompleteTextView)findViewById(R.id.typingText);
	        
	    TableLayout addInfo = (TableLayout)findViewById(R.id.AddInfo);
	    addInfo.removeAllViews();
		addInfo.setStretchAllColumns(true);
		
		TableLayout.LayoutParams row_layout = new TableLayout.LayoutParams
												(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		TableRow.LayoutParams view_layout = new TableRow.LayoutParams
												(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		
		mapView.getOverlays().remove(Marker);
		Drawable marker1 = getResources().getDrawable(R.drawable.map_marker_icon);
		Bitmap markerScale = ((BitmapDrawable) marker1).getBitmap();
		Drawable marker = new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(markerScale, 40, 45, true));
		
		Marker = new MapOverlay(marker, this);
		
		ArrayAdapter<String> autoText = new ArrayAdapter<String>
										(Map2Activity.this, android.R.layout.simple_spinner_item, spotList);
		typingText.setAdapter(autoText);
		
		TableRow titleRow = new TableRow(Map2Activity.this);
		titleRow.setLayoutParams(row_layout);
		titleRow.setGravity(Gravity.CENTER);
		titleRow.setBackgroundResource(R.drawable.SteelBlue);
		titleRow.setPadding(5, 3, 0, 3); //(left, top, right, bottom)
		
		TextView dayTitle = new TextView(Map2Activity.this);
		dayTitle.setText("Spots:");
		dayTitle.setLayoutParams(view_layout);
		dayTitle.setTextColor(getResources().getColor(R.drawable.White));
		dayTitle.setGravity(Gravity.CENTER);
		
		TextView spotTitle = new TextView(Map2Activity.this);
		spotTitle.setText("(Long press for information)");
		spotTitle.setLayoutParams(view_layout);
		spotTitle.setTextColor(getResources().getColor(R.drawable.White));
		spotTitle.setGravity(Gravity.CENTER);
		
		titleRow.addView(dayTitle);
		titleRow.addView(spotTitle);
		addInfo.addView(titleRow);
		
		ArrayList<HashMap<String, Object>> itemList = new ArrayList<HashMap<String, Object>>();
		
		for (int i = 0; i < latList.length; i++)
		{
			String coordinates[] = {latList[i], lngList[i]};
			double latitude = Double.parseDouble(coordinates[0]);
			double longitude = Double.parseDouble(coordinates[1]);
			
			GeoPoint points = new GeoPoint(
								(int)(latitude * 1E6),
								(int)(longitude * 1E6));
			
			Marker.setPoint(points, spotList[i], spotInfoList[i]);
			
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
			
			/*
			TableRow contentRow = new TableRow(Map2Activity.this);
			contentRow.setLayoutParams(row_layout);
			contentRow.setGravity(Gravity.CENTER_HORIZONTAL);
			contentRow.setBackgroundResource(drawable.menuitem_background);
			contentRow.setClickable(true);
			contentRow.setPadding(3, 5, 0, 5);
			
			contentRow.setTag("Row" + i);
			
			TextView dayCount = new TextView(Map2Activity.this);
			dayCount.setText(queList[i] + "-Day" + dayList[i]);
			dayCount.setLayoutParams(view_layout);
			
			TextView spot = new TextView(Map2Activity.this);
			spot.setText(spotList[i]);
			spot.setLayoutParams(view_layout);
			
			contentRow.addView(dayCount);
			contentRow.addView(spot);
			addInfo.addView(contentRow);
			*/
		}
		Marker.finish();
		mapView.getOverlays().add(Marker);
		mapView.invalidate();
		
		final SimpleAdapter spotAdapter = new SimpleAdapter
								(Map2Activity.this, itemList, R.layout.my_list_layout02,
								new String[] {"spot","day"},
								new int[] {R.id.textView_2_1, R.id.textView_2_2});
		
		PlanList = (ListView)findViewById(R.id.PlanList);
		PlanList.setAdapter(spotAdapter);
		PlanList.setTextFilterEnabled(true);
		
		//typingText.setAdapter(spotAdapter);
		
		PlanList.setOnItemClickListener(new AdapterView.OnItemClickListener(){
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				
				String coordinates[] = {latList[arg2], lngList[arg2]};
				double latitude = Double.parseDouble(coordinates[0]);
				double longitude = Double.parseDouble(coordinates[1]);
				
				GeoPoint position = new GeoPoint(
									(int)(latitude * 1E6),
									(int)(longitude * 1E6));
				
				mapControl.animateTo(position);
				mapControl.setZoom(16);
				
				Toast.makeText(Map2Activity.this,spotList[arg2], Toast.LENGTH_SHORT).show();
			}
		});
		PlanList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener(){
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				
				AlertDialog.Builder infoDialog = new AlertDialog.Builder(Map2Activity.this);
	   			infoDialog.setIcon(R.drawable.info_icon);
	   			infoDialog.setTitle(spotList[arg2]);
	   			infoDialog.setMessage(spotInfoList[arg2]);
	   			infoDialog.setPositiveButton("OK!", 
	   					new DialogInterface.OnClickListener()
	   					{
	   						public void onClick(DialogInterface dialog, int which)
	   						{
	   						//Actions after you press OK!	
	   						}
	   					});
	   			infoDialog.show();
				return true;
			}
		});
		
		typingText.addTextChangedListener(new TextWatcher() {
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				spotAdapter.getFilter().filter(s);
			}
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            public void afterTextChanged(Editable s) {
            }
		});
   	}
   	
   	public void searchClick(View searchClick) {
   		typingText = (AutoCompleteTextView)findViewById(R.id.typingText);
   		String input = typingText.getText().toString().trim();
   		
   		Drawable marker1 = getResources().getDrawable(R.drawable.map_green_marker_icon);
   		Bitmap markerScale = ((BitmapDrawable) marker1).getBitmap();
   		Drawable marker = new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(markerScale, 40, 45, true));
   		
   		mapView.getOverlays().remove(locMarker);
   		locMarker = new SearchOverlay(marker, this);
   		
   		if (input.equals("planlist"))
   		{
   			showPlanWindow();
   		} else if (input.equals("spotlist")){
   			showSpotWindow();
   		} else if (input.length() > 0) 
   		{
   			Geocoder geocoder = new Geocoder(Map2Activity.this);
   			List<Address> addrs = null;
   			Address addr = null;

   			try {
   				addrs = geocoder.getFromLocationName(input, 1);
   			} catch (IOException e) {
   				Log.e("findLocation:", e.toString());
   			}

   			if (addrs == null || addrs.isEmpty()) {
   				Toast.makeText(Map2Activity.this, "Location not find!", Toast.LENGTH_SHORT).show();
   			} else {
   				addr = addrs.get(0);
   				double geoLat = addr.getLatitude() * 1E6;
   				double geoLng = addr.getLongitude() * 1E6;

   				gp = new GeoPoint((int) geoLat, (int) geoLng);

   				locMarker.setPoint(gp, "Your searching result", input);
   				locMarker.finish();
   				mapView.getOverlays().add(locMarker);
   				mapView.invalidate();

   				mapControl.animateTo(gp);
   				mapControl.setZoom(10);
   			}
   		} else {
   			Toast.makeText(Map2Activity.this, "Your Input is Empty!!", Toast.LENGTH_SHORT).show();
   		}
   	}

   	OnKeyListener searchKey = new OnKeyListener() {
   		
   		public boolean onKey(View v, int keyCode, KeyEvent event) {
   			// TODO Auto-generated method stub
   			if (keyCode == KeyEvent.KEYCODE_ENTER) 
   			{	
   				InputMethodManager imm = (InputMethodManager)v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
   				if (imm.isActive() && count == 0)
   				{
   					searchClick(v);
   	   				imm.hideSoftInputFromWindow(v.getApplicationWindowToken(), 0);
   	   				count++;
   	   				countDown();
   				}
   			}
   			return false;
   		}
   	};
   	
   	public void countDown()
   	{
   		new CountDownTimer(1000,1000){
   			@Override
   			public void onFinish() {
   				// TODO Auto-generated method stub
   				count--;
   			}
   			@Override
   			public void onTick(long millisUntilFinished) {
   				// TODO Auto-generated method stub
   			}
   		}.start();
   	}
	
   	public void startCountDown()
   	{
   		new CountDownTimer(1500,1000){
   			@Override
   			public void onFinish() {
   				// TODO Auto-generated method stub
   				showPlanWindow();
   			}
   			@Override
   			public void onTick(long millisUntilFinished) {
   				// TODO Auto-generated method stub
   			}
   		}.start();
   	}
   	
	public void putArr()
	{
		ArrayList<HashMap<String, String>> planList = new ArrayList<HashMap<String, String>>();
		for (int i = 0; i < planArr.length; i++)
		{
			HashMap<String, String> planItems = new HashMap<String, String>();
			
			planItems.put("planName", planArr[i]);
			planItems.put("planInfos","Total " + planDaysArr[i] +" Days, "+"\n" + "From " + planStartArr[i]
							+ " To " + planEndArr[i]);
			planList.add(planItems);
		}
		planListArr = planList;
	}
	
	
	public void putSpotArr (String[] latArr, String[] lngArr, String[] spotArr, 
				String[] spotinfoArr,String[] dayArr, String[] queArr,
				String[] flag1Arr, String[] flag2Arr, String[] flag3Arr, String[] flag4Arr, String[] flag5Arr)
	{
		String[] latList = latArr;
		String[] lngList = lngArr;
		String[] spotList = spotArr;
		String[] spotInfoList = spotinfoArr;
		String[] dayList = dayArr;
		String[] queList = queArr;
		String[] flagFoodList = flag1Arr;
		String[] flagHotelList = flag2Arr;
		String[] flagShopList = flag3Arr;
		String[] flagSceneList = flag4Arr;
		String[] flagTransList = flag5Arr;
		
		ArrayList<HashMap<String, Object>> itemList = new ArrayList<HashMap<String, Object>>();
		
		for (int i = 0; i < spotList.length; i++)
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
		spotListArr = itemList;
	}
	
	@SuppressWarnings("deprecation")
	public void showPlanWindow()
	{
		putArr();
		
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		view = inflater.inflate(R.layout.expanded_list, null);
		ListView extanded_list = (ListView) view.findViewById(R.id.exList1); 
		LinearLayout SayHiLayout = (LinearLayout) findViewById(R.id.SayHi);

		SimpleAdapter planAdapter = new SimpleAdapter
				(Map2Activity.this, planListArr, R.layout.my_list_layout01,
						new String[] {"planName", "planInfos"}, new int[] {R.id.textView_1_1, R.id.textView_1_2});

		extanded_list.setAdapter(planAdapter);

		popUp = new PopupWindow (view, 400, 450);	//(view, width, height)
			
		popUp.setFocusable(true);
		popUp.setOutsideTouchable(true);
		popUp.setBackgroundDrawable(new BitmapDrawable());
		
		WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
		
		int xpos = wm.getDefaultDisplay().getWidth() / 2 - popUp.getWidth() / 2;
		int ypos = (wm.getDefaultDisplay().getHeight() / 2) - (popUp.getHeight() - 100);
		
		Log.i("Coder", "xPos:" + xpos);
		Log.i("Coder", "yPos:" + ypos);
		
		popUp.showAsDropDown(SayHiLayout, xpos, ypos);
		
		extanded_list.setOnItemClickListener(new AdapterView.OnItemClickListener() { 
            public void onItemClick(AdapterView<?> ar0, View arg1, int arg2, long arg3)
            {  
            	String selected = planArr[arg2];

            	if (selected.contains("---")) 
            	{
            		Toast.makeText(Map2Activity.this, "Now, Please choose your plan!", Toast.LENGTH_SHORT).show();
            	} else {
            		Toast.makeText(Map2Activity.this, "Plan: " + planArr[arg2].toString(), Toast.LENGTH_SHORT).show();
            		//String pidBuffer = pid.trim();
            		String pid = selected.substring(0,selected.indexOf(" "));
            		final String xmlPidUrl = tourURL + MyName +"/" +pid;

            		new Thread()
            		{
            			public void run()
            			{
            				String xmlPidString = getStringByUrl(xmlPidUrl);
            				planChosen.obtainMessage(REFRESH_DATA, xmlPidString).sendToTarget();
            			}
            		}.start();
            	}
            	if (popUp != null)
            	{
            		popUp.dismiss();
            	}
            }
		});
	}
	
	@SuppressWarnings("deprecation")
	public void showSpotWindow()
	{
		if (startNeed == false)
		{
			Toast.makeText(Map2Activity.this, "You haven't select a plan yet~", Toast.LENGTH_SHORT).show();
		} else {
			final String[] latList = latArr;
			final String[] lngList = lngArr;
			final String[] spotList = spotArr;
			final String[] spotInfoList = spotInfoArr;

			LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			view = inflater.inflate(R.layout.expanded_list, null);
			ListView extanded_list = (ListView) view.findViewById(R.id.exList2); 
			LinearLayout SayHiLayout = (LinearLayout) findViewById(R.id.SayHi);

			SimpleAdapter spotAdapter = new SimpleAdapter
					(Map2Activity.this, spotListArr, R.layout.my_list_layout03,
							new String[] {"spot", "day"}, new int[] {R.id.textView_1_3, R.id.textView_1_4});

			extanded_list.setAdapter(spotAdapter);

			popUp = new PopupWindow (view, 450, 600);	//(view, width, height)

			popUp.setFocusable(true);
			popUp.setOutsideTouchable(true);
			popUp.setBackgroundDrawable(new BitmapDrawable());

			WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);

			int xpos = wm.getDefaultDisplay().getWidth() / 2 - popUp.getWidth() / 2;
			int ypos = (wm.getDefaultDisplay().getHeight() / 2) - (popUp.getHeight() - 200);

			Log.i("Coder", "xPos:" + xpos);
			Log.i("Coder", "yPos:" + ypos);

			popUp.showAsDropDown(SayHiLayout, xpos, ypos);

			extanded_list.setOnItemClickListener(new AdapterView.OnItemClickListener(){
				public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) 
				{
					String coordinates[] = {latList[arg2], lngList[arg2]};
					double latitude = Double.parseDouble(coordinates[0]);
					double longitude = Double.parseDouble(coordinates[1]);

					GeoPoint position = new GeoPoint(
							(int)(latitude * 1E6),
							(int)(longitude * 1E6));

					mapControl.animateTo(position);
					mapControl.setZoom(16);

					Toast.makeText(Map2Activity.this,spotList[arg2], Toast.LENGTH_SHORT).show();
					if (popUp != null)
					{
						popUp.dismiss();
					}
				}
			});
			extanded_list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener(){
				public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {

					AlertDialog.Builder infoDialog = new AlertDialog.Builder(Map2Activity.this);
					infoDialog.setIcon(R.drawable.info_icon);
					infoDialog.setTitle(spotList[arg2]);
					infoDialog.setMessage(spotInfoList[arg2]);
					infoDialog.setPositiveButton("OK!", 
							new DialogInterface.OnClickListener()
					{
						public void onClick(DialogInterface dialog, int which)
						{
							//Actions after you press OK!	
						}
					});
					infoDialog.show();
					return true;
				}
			});
		}
	}
	
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		if (keyCode == KeyEvent.KEYCODE_BACK || event.getAction() == KeyEvent.KEYCODE_BACK)
		{
			if (System.currentTimeMillis() - exitTime > 3500)
			{
				Toast.makeText(Map2Activity.this, "Press Back again to quit this program", Toast.LENGTH_LONG).show();
				exitTime = System.currentTimeMillis();
			} else {
				finish();
			}
			return true;
		} 
		return super.onKeyDown(keyCode, event);
	}
    
    class sendItToRun implements Runnable 
    {
    	String strTxt = null;
    	
    	public sendItToRun(String strTxt)
    	{
    		this.strTxt = strTxt;
    	}
    	
    	@Override
    	public void run()
    	{
    		String result = getStringByUrl(strTxt);
    		planMove.obtainMessage(REFRESH_DATA, result).sendToTarget();
    	}
    }
    
    
  //Create HTTP Connection!!
    
    private String getStringByUrl(String url)
    {
    	String uriXML = url;
    	HttpGet httpRequest = new HttpGet(uriXML);

    	try
    	{
    		HttpResponse httpResponse = new DefaultHttpClient().execute(httpRequest);
    		if (httpResponse.getStatusLine().getStatusCode() == 200)
    		{
    			String strResult = EntityUtils.toString(httpResponse.getEntity());
    			strResult = eregi_replace("(.*)<\\?xml","<?xml",strResult);
    			return strResult;
    		}
    	}
    	
    	catch (ClientProtocolException e)
    	{
    		Toast.makeText(this,e.getMessage().toString(),Toast.LENGTH_SHORT).show();
    		e.printStackTrace();
    	}
    	
    	catch (IOException e)
    	{
    		Toast.makeText(this,e.getMessage().toString(),Toast.LENGTH_SHORT).show();
    		e.printStackTrace();
    	}
    	
    	catch (Exception e)
    	{
    		Toast.makeText(this,e.getMessage().toString(),Toast.LENGTH_SHORT).show();
    		e.printStackTrace();
    	}
    	
    	return null;
    }
    
    public String eregi_replace(String strFrom, String strTo, String strTarget)  
    {  
    	String strPattern = "(?i)"+strFrom;  
    	Pattern p = Pattern.compile(strPattern);  
    	Matcher m = p.matcher(strTarget);  
    	if(m.find())  
    	{  
    		return strTarget.replaceAll(strFrom, strTo);  
    	}
    	else  
    	{
    		return strTarget;  
    	}
    }
      
 
    @Override
    protected boolean isRouteDisplayed() {
        // TODO Auto-generated method stub
        return false;
    }
    
    
    @SuppressLint("NewApi")
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	//(groupId, itemId, order, title)
    	menu.add(0,0,4,"Logout").setIcon(android.R.drawable.ic_menu_close_clear_cancel);
    	menu.add(0,1,3,"Read Me").setIcon(android.R.drawable.ic_menu_info_details);
    	menu.add(0,2,2,"Find My Position").setIcon(android.R.drawable.ic_menu_mylocation);
    	menu.add(0,3,1,"Select Plan").setIcon(android.R.drawable.ic_menu_view);
    	menu.add(0,4,0,"Show Spot List");
    	
    	if (version > 10)
    	{
    		getMenuInflater().inflate(R.menu.options, menu);
    		Button menuBtn1 = (Button) menu.findItem(R.id.selectPlan).getActionView();
    		menuBtn1.setText("Select Plan");

    		menuBtn1.setOnClickListener(new OnClickListener() {
    			public void onClick(View v) {

    				showPlanWindow();
    			}
    		});
    	}

    	return super.onCreateOptionsMenu(menu);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch(item.getItemId()){
    		case 0:
    			new AlertDialog.Builder(this).setTitle("Are you sure?!")
    			.setPositiveButton("NO~", new DialogInterface.OnClickListener()
                {  
    				public void onClick(DialogInterface arg0, int arg1)
    				{  
    				} 
               }).setNegativeButton("YES!", new DialogInterface.OnClickListener()
               	{  
            	   public void onClick(DialogInterface arg0, int arg1)
            	   {
            		   Intent goBack = new Intent();
            		   Bundle clear = new Bundle();
            		   goBack.setClass(Map2Activity.this, Login.class);
            		   clear.putString("Clear", "");
            		   goBack.putExtras(clear);
            		   Map2Activity.this.startActivity(goBack);
            		   finish();
            	   } 
               	}).show();
    		break;
    		
    		case 1:
    			Toast.makeText(Map2Activity.this, "Still working on it...", Toast.LENGTH_LONG).show();
    			break;
    			
    		case 2:
    			findMyLocation();
    			break;
    		
    		case 3:
    		    showPlanWindow(); 
    			break;
    			
    		case 4:
    			showSpotWindow();
    			break;
    	}
    	return super.onOptionsItemSelected(item);
    }
    
}