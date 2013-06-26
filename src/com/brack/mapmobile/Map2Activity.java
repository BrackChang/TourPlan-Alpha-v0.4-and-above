package com.brack.mapmobile;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Debug;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.Overlay;

@SuppressLint("HandlerLeak")
public class Map2Activity extends MapActivity implements LocationListener
{    
    private MapView mapView; 
    private MapController mapControl;
    private MyLocationOverlay myLayer;
    private LocationManager locManager;
    private DrawOverlay drawOverlay;
    private GeoPoint GP;
    private MapOverlay Marker;
    private SearchOverlay locMarker;
    private ExpandableListView exSpotList;
    private ExAdapter exAdapter;
    private PopupWindow popUp;
    private View view;
    private AutoCompleteTextView typingText;
    private long exitTime = 0;
    private String tourURL = "http://140.128.198.44:406/plandata/";
    protected static final int REFRESH_DATA = 0x00000001;
    private String strLocation = "";
    private Location myLocation;
    
    private String MyName;
    private String planTitle;
    private String[] planArr;
    private String[] planDaysArr;
    private String[] planStartArr;
    private String[] planEndArr;
    private String[] latArr;
    private String[] lngArr;
    private String[] dayArr;
    private String[] queArr;
    private String[] spotArr;
    private String[] spotInfoArr;
    private String[] flagFoodArr;
    private String[] flagHotelArr;
    private String[] flagShopArr;
    private String[] flagSceneArr;
    private String[] flagTransArr;
    private ArrayList<HashMap<String, String>> planListArr;
    private ArrayList<HashMap<String, Object>> spotListArr;
    private int dayCount;
    
    @SuppressWarnings("deprecation")
	int SDKversion = Integer.parseInt(VERSION.SDK);
    private boolean startNeed;
    private boolean mapAutoNone = true;
    private boolean requestGPS;
    private boolean stopAsk;
    private boolean enableTool;
    
    /** Called when the activity is first created. **/
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        Debug.startMethodTracing("report");
        setContentView(R.layout.map);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        
        String version = getResources().getString(R.string.Version);
        String versionName = getResources().getString(R.string.VersionName);
        this.setTitle(versionName + version);
        Debug.stopMethodTracing();
        
        findMapControl();
        mapHalf();
        
        typingText = (AutoCompleteTextView)findViewById(R.id.typingText);
        TextView UserName = (TextView)findViewById(R.id.UserName);
        
        Bundle userName = this.getIntent().getExtras();		//Obtain Bundle
        String Name = userName.getString("name").toString();
        MyName = Name;
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
        
        GP = new GeoPoint(
        		(int)(lat * 1E6),(int)(lng * 1E6));
        
        mapControl.animateTo(GP);
        mapControl.setZoom(8);
        
        locManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, Map2Activity.this);
        //locManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 0, Map2Activity.this);
        
        getLocationProvider();
        
        //findMyLocation();
    }
    
    private void GPSinit()
    {	
    	if (requestGPS == true)
    	{
    		if (!locManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
    		{
    			new AlertDialog.Builder(Map2Activity.this).setTitle("GPS Setting")
    			.setMessage("GPS is not enabled, do you want to switch to the setting page?")
    			.setCancelable(false).setPositiveButton("OKAY~", new DialogInterface.OnClickListener() {
    				public void onClick(DialogInterface dialog, int which) {
    					startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
    				}
    			}).setNegativeButton("NO!", new DialogInterface.OnClickListener() {
    				public void onClick(DialogInterface dialog, int which) {
    					requestGPS = false;
    					stopAsk = true;
    					findMyLocation();
    				}
    			}).show();
    		}
    		else
        	{
        		findMyLocation();
        		enableTool = true;
        	}
    	} 
    	else 
    	{
    		findMyLocation();
    		enableTool = true;
    	}
    }
    
    @Override
    protected void onResume()
    {
    	super.onResume();
    	if (enableTool)
    	{
    		locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, Map2Activity.this);
    		//locManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 0, Map2Activity.this);
    		myLayer.enableMyLocation();
    		myLayer.enableCompass();
    	}
    	else
    	{
    		GPSinit();
    	}
    }
    @Override
    protected void onPause()
    {
    	super.onPause();
    	if (enableTool)
    	{
    		locManager.removeUpdates(Map2Activity.this);
    		myLayer.disableMyLocation();
    		myLayer.disableCompass();
    	}
    }
    
    private void findMyLocation()
    {
    	List<Overlay> locOverlays = mapView.getOverlays();
    	locOverlays.remove(myLayer);
    	
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
    
    public void getLocationProvider()
    {
    	try
    	{
    		Criteria criteria = new Criteria();
    		criteria.setAccuracy(Criteria.ACCURACY_FINE);
    		criteria.setAltitudeRequired(false);
    		criteria.setBearingRequired(false);
    		criteria.setCostAllowed(true);
    		criteria.setPowerRequirement(Criteria.POWER_LOW);
    		
    		strLocation = locManager.getBestProvider(criteria, true);
    		myLocation = locManager.getLastKnownLocation(strLocation);
    	}
    	catch (Exception e)
    	{
    		longMessage(e.toString());
    		e.printStackTrace();
    	}
    }
    
    public void routeFromMyPosition(String toLat, String toLng)
    { 	
    	double lat = myLocation.getLatitude();
    	double lng = myLocation.getLongitude();
    	Log.i("MyPosition", ""+lat +" "+ lng);
    	
    	double toGPLat = Double.parseDouble(toLat);
    	double toGPLng = Double.parseDouble(toLng);
    	Log.i("Destination", ""+ toGPLat +" & " + toGPLng );
    	
    	exListMapMove(""+lat, ""+lng);
    	
    	new GoogleDirection().execute(lat + "," + lng, toGPLat + "," + toGPLng);
    	/*
    	Geocoder geocoder = new Geocoder(Map2Activity.this);
		List<Address> addrs;
		
		try
		{
			addrs = geocoder.getFromLocation(lat, lng, 1);
			if (addrs != null && addrs.size() > 0)
			{
				String address = new String();
				Log.i("Address", addrs.get(0).toString());
				address = addrs.get(0).getAddressLine(0) + "," + System.getProperty("line.separator")
						+ addrs.get(0).getAddressLine(1) + "," + addrs.get(0).getAddressLine(2);
				
				longMessage(""+address);
				
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		*/
    }
    
    public void routeToSearch(String searchLat, String searchLng)
    {
    	double lat = myLocation.getLatitude();
    	double lng = myLocation.getLongitude();
    	Log.i("MyPosition", ""+lat +" "+ lng);

    	exListMapMove(""+lat, ""+lng);
    	
    	new GoogleDirection().execute(lat + "," + lng, searchLat + "," + searchLng);
    }
    
    public void routeToNextSpot(String fromLat, String fromLng, String toLat, String toLng)
    { 	
    	double fromGPLat = Double.parseDouble(fromLat);
    	double fromGPLng = Double.parseDouble(fromLng);
    	
    	double toGPLat = Double.parseDouble(toLat);
    	double toGPLng = Double.parseDouble(toLng);
    	
    	exListMapMove(""+fromGPLat, ""+fromGPLng);
    	
    	new GoogleDirection().execute(fromGPLat + "," + fromGPLng, toGPLat + "," + toGPLng);
    }

    private class GoogleDirection extends AsyncTask<String, Integer, List<GeoPoint>>
    {
    	private final String mapAPI = 
    	"http://maps.google.com/maps/api/directions/json?" + "origin={0}&destination={1}&language=zh-TW&sensor=true";
    	private String from;
    	private String to;
    	private List<GeoPoint> geoPoints = new ArrayList<GeoPoint>();
    	private String POLY;
    	
    	@Override
    	protected List<GeoPoint> doInBackground(String...params)
    	{
    		if (params.length < 0)
    			return null;
    		
    		from = params[0];
    		to = params[1];
    		
    		String Url = MessageFormat.format(mapAPI, from, to);
    		Log.i("mapUrl", Url);
    		
    		HttpGet get = new HttpGet(Url);
    		String strResult = "";
    		
    		try
    		{
    			HttpParams httpParams = new BasicHttpParams();
    			HttpConnectionParams.setConnectionTimeout(httpParams, 3000);
    			
    			HttpClient HC = new DefaultHttpClient(httpParams);
    			HttpResponse HR = null;
    			HR = HC.execute(get);
    			
    			if (HR.getStatusLine().getStatusCode() == 200)
    			{
    				strResult = EntityUtils.toString(HR.getEntity());
    				
    				JSONObject jb = new JSONObject(strResult);
    				JSONArray ja = jb.getJSONArray("routes");
    				String status = jb.getString("status");
    				
    				Log.i("status", status);
    				
    				if (status.contains("OK"))
    				{
    					String polyLine = ja.getJSONObject(0).getJSONObject("overview_polyline").getString("points");
    					if (polyLine.length() > 0)
        				{
    						POLY = polyLine;
        				}
    				} else {
    					POLY = "";
    				}
    			}
    		} 
    		catch(Exception e)
    		{
				Log.e("map", e.toString());
    		}
    		return geoPoints;
    	}
    	/*
    	private void decodePolylines(String poly)
    	{
    		int len = poly.length();
    		int index = 0;
    		int lat = 0;
    		int lng = 0;
    		
    		while (index < len)
    		{
    			int b, shift = 0, result = 0;
    			do
    			{
    				b = poly.charAt(index++) -63;
    				result |= (b & 0x1f) << shift;
    				shift += 5;
    			}
    			while (b >= 0x20);
    			
    			int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
				lng += dlng;

				GeoPoint p = new GeoPoint((int) (((double) lat / 1E5) * 1E6), (int) (((double) lng / 1E5) * 1E6));
				geoPoints.add(p);
    		}
    	}
    	*/
    	@SuppressWarnings({ "rawtypes", "unchecked" })
		protected void onPostExecute(List<GeoPoint> geoPoints)
    	{
    		if (POLY.length() > 0)
    		{
    			ArrayList myPoints = (ArrayList) PolyHelper.decodePolyline(POLY);
    			mapView.getOverlays().remove(drawOverlay);
    			drawOverlay = new DrawOverlay(myPoints);
    			mapView.getOverlays().add(drawOverlay);
    			mapView.invalidate();
    		} else {
    			longMessage("Oops~Routed failed!!\nThe path is NOT drawable, it may cause by the path of cross sea.");
    		}
    	}
    }
    
    
    Handler planMove = new Handler()
   	{
   		@SuppressWarnings("deprecation")
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
   							res = res + stPid.nextToken()+ " " + stPlan.nextToken() + " ";
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
   				
   				for (int i = 0; i < endList.length; i++)
   				{
   					endList[i] = endList[i].replace(Integer.toString(new Date().getYear()+1900)+"-", "");
   				}
   				
   				planArr = plans;
   				planDaysArr = daysList;
   				planStartArr = startList;
   				planEndArr = endList;
   				
   				startCountDown();
   				
   				longMessage("Now, Please choose your plan!");
   				
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
					
					dayCount = 1;
					
					showSpotList();
					putSpotArr();
					
					startNeed = true;
					if (mapAutoNone == true) {
						mapNoneCountDown();
					}
					findMyLocation();
					mapControl.setZoom(8);
   			}
   		}
   	};
   	
   	@SuppressWarnings("deprecation")
	public void showSpotList()
   	{
   		exSpotList = (ExpandableListView) findViewById(R.id.exPlanList);
   		typingText = (AutoCompleteTextView)findViewById(R.id.typingText);
	    
	    TextView planName = (TextView) findViewById(R.id.planName);
	    TableRow titleRow = (TableRow) findViewById(R.id.titleRow);
	    TableRow dayRow = (TableRow) findViewById(R.id.dayRow);
	    
	    titleRow.removeAllViews();
		dayRow.removeAllViews();
		
		DisplayMetrics DM = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(DM);
		
		WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
		double height = wm.getDefaultDisplay().getHeight() / 16;
		int textSize = wm.getDefaultDisplay().getHeight() / 55;
		int screenWidth = wm.getDefaultDisplay().getWidth();
		int screenHeight = wm.getDefaultDisplay().getHeight();
		
		Log.i("Metrics", DM.widthPixels + " x " + DM.heightPixels);
		Log.i("ScreenDisplay", ""+screenWidth +" x "+ screenHeight);
		Log.i("ButtonHeight", ""+height);
		Log.i("TextSize1", ""+textSize);
		
		if (screenWidth >= 480 && screenWidth < 720)
		{
			textSize = 13;
		} 
		else if (screenWidth >= 720 && screenWidth < 800)
		{
			textSize = 16;
		}
		Log.i("TextSize2", ""+textSize);
		/*
		TableLayout.LayoutParams row_layout = new TableLayout.LayoutParams
												(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		TableRow.LayoutParams view_layout = new TableRow.LayoutParams
												(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		*/
		planName.setText(planTitle);
		planName.setTextSize(textSize);
		titleRow.addView(planName);
		
		mapView.getOverlays().remove(Marker);
		Drawable marker1 = getResources().getDrawable(R.drawable.map_marker_icon);
		Bitmap markerScale = ((BitmapDrawable) marker1).getBitmap();
		Drawable marker = new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(markerScale, 40, 45, true));
		
		Marker = new MapOverlay(marker, this);
   		
   		final List<Map<String, Object>> spotGroup = new ArrayList<Map<String, Object>>();
		final List<List<Map<String, String>>> spotChild = new ArrayList<List<Map<String, String>>>();
		
		for (int i = 0; i < spotArr.length; i++)
		{
			String coordinates[] = {latArr[i], lngArr[i]};
			double latitude = Double.parseDouble(coordinates[0]);
			double longitude = Double.parseDouble(coordinates[1]);
			
			GeoPoint points = new GeoPoint(
								(int)(latitude * 1E6),
								(int)(longitude * 1E6));
			
			Marker.setPoint(points, "Day:"+dayArr[i]+" "+spotArr[i], spotInfoArr[i]);
			
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
			
			spotInfo.put("info", spotInfoArr[i]);
			spotInfos.add(spotInfo);
			spotChild.add(spotInfos);
			
			String d = Integer.toString(dayCount);
			if (dayArr[i].equals(d))
			{
				final Button dayBtn = new Button(Map2Activity.this);
				dayBtn.setText("Day " + dayArr[i]);
				dayBtn.setTextColor(getResources().getColor(R.drawable.Ivory));
				dayBtn.setGravity(Gravity.CENTER);
				dayBtn.setTextSize(textSize);
				
				dayRow.addView(dayBtn);
				dayCount++;
				
				LayoutParams params = dayBtn.getLayoutParams();
				params.width = LayoutParams.WRAP_CONTENT;
				params.height = (int) height;
				dayBtn.setLayoutParams(params);
				
				dayBtn.setOnClickListener(new OnClickListener() {
					public void onClick(View v) {
						String dayString = dayBtn.getText().toString();
						typingText.setText(dayString);
					}
				});
			} else if (dayArr[i].equals("0"))
			{
				final Button dayBtn = new Button(Map2Activity.this);
				dayBtn.setText("Day " + dayArr[i]);
				dayBtn.setTextColor(getResources().getColor(R.drawable.Ivory));
				dayBtn.setGravity(Gravity.CENTER);
				dayBtn.setTextSize(textSize);
				
				dayRow.addView(dayBtn);
				
				LayoutParams params = dayBtn.getLayoutParams();
				params.width = LayoutParams.WRAP_CONTENT;
				params.height = (int) height;
				dayBtn.setLayoutParams(params);
				
				dayBtn.setOnClickListener(new OnClickListener() {
					public void onClick(View v) {
						String dayString = dayBtn.getText().toString();
						typingText.setText(dayString);
					}
				});
			}
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
		  		
   		exAdapter = new ExAdapter(this, spotGroup, spotChild);
   		exSpotList.setIndicatorBounds(0, 20);
   		exSpotList.setAdapter(exAdapter);

   		/*
   		SimpleAdapter spotAdapter = new SimpleAdapter
				(Map2Activity.this, spotGroup, R.layout.autotext_list_layout02,
						new String[] {"title","titleDescribe"}, 
						new int[] {R.id.autoText1, R.id.autoText2});
   		typingText.setAdapter(spotAdapter);
   		*/
   		
		ArrayAdapter<String> autoText = new ArrayAdapter<String>
										(Map2Activity.this, R.layout.autotext_list_layout01, spotArr);
		typingText.setAdapter(autoText);
		
		ProgressBar loadingPlan = (ProgressBar) findViewById(R.id.loadingPlan);
		loadingPlan.setVisibility(View.GONE);
		
   		typingText.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3)
			{
				String selected = arg0.getItemAtPosition(arg2).toString();
				if (selected.contains("=") && selected.contains(","))
				{
					String selected2 = selected.substring(0, selected.lastIndexOf(","));
					StringBuffer buffer = new StringBuffer(selected2);
					buffer.delete(0, selected2.lastIndexOf("=")+1);
					typingText.setText(buffer.toString());
				}
			}
   		});
   		
   		typingText.addTextChangedListener(new TextWatcher() {
			@SuppressLint("DefaultLocale")
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if (!s.toString().equals(""))
				{
					List<Map<String, Object>> spot = new ArrayList<Map<String, Object>>();
					List<List<Map<String, String>>> spotContent = new ArrayList<List<Map<String, String>>>();
					
					for (int i = 0; i < spotGroup.size(); i++)
					{
						if (spotGroup.get(i).get("title").toString().contains(s) || 
								spotGroup.get(i).get("titleDescribe").toString().contains(s.toString().toUpperCase()))
						{
							spot.add(spotGroup.get(i));
							spotContent.add(spotChild.get(i));
							//Log.i("Value",spot.toString());
						}
					}
					exAdapter = new ExAdapter(Map2Activity.this, spot, spotContent);
					exSpotList.setAdapter(exAdapter);
				} else {
					exAdapter = new ExAdapter(Map2Activity.this, spotGroup, spotChild);
					exSpotList.setAdapter(exAdapter);
				}
			}
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            public void afterTextChanged(Editable s) {
            }
		});
   	}
   	
   	public void exListMapMove (String lat, String lng)
   	{
   		String coordinates[] = {lat, lng};
		double latitude = Double.parseDouble(coordinates[0]);
		double longitude = Double.parseDouble(coordinates[1]);
		
		GP = new GeoPoint(
					(int)(latitude * 1E6),
					(int)(longitude * 1E6));
		
		mapControl.animateTo(GP);
		mapControl.setZoom(15);
		
		mapHalf();
   	}
   	
   	public void clearClick(View clearClick)
   	{
   		typingText = (AutoCompleteTextView) findViewById(R.id.typingText);
   		typingText.setText("");
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
   			typingText.setText("");
   		} else if (input.equals("spotlist")){
   			showSpotWindow();
   			typingText.setText("");
   		} else if (input.length() > 0) 
   		{
   			mapHalf();
   			
   			Geocoder geocoder = new Geocoder(Map2Activity.this);
   			List<Address> addrs = null;
   			Address addr = null;

   			try {
   				addrs = geocoder.getFromLocationName(input, 1);
   			} catch (IOException e) {
   				Log.e("findLocation:", e.toString());
   			}

   			if (addrs == null || addrs.isEmpty()) {
   				shortMessage("Location not find!");
   			} else {
   				addr = addrs.get(0);
   				double geoLat = addr.getLatitude() * 1E6;
   				double geoLng = addr.getLongitude() * 1E6;

   				GP = new GeoPoint((int) geoLat, (int) geoLng);

   				locMarker.setPoint(GP, "Your searching result", input);
   				locMarker.finish();
   				mapView.getOverlays().add(locMarker);

   				mapControl.animateTo(GP);
   				mapControl.setZoom(13);
   				mapView.invalidate();
   				
   				typingText.setText("");
   			}
   		} else {
   			shortMessage("Your Input is Empty!!");
   		}
   	}

   	OnKeyListener searchKey = new OnKeyListener() {
   		
   		public boolean onKey(View v, int keyCode, KeyEvent event) {
   			// TODO Auto-generated method stub
   			if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_UP) 
   			{	
   				InputMethodManager imm = (InputMethodManager)v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
   				if (imm.isActive())
   				{
   					searchClick(v);
   	   				imm.hideSoftInputFromWindow(v.getApplicationWindowToken(), 0);
   				}
   			}
   			return false;
   		}
   	};
	
   	public void startCountDown()
   	{
   		new CountDownTimer(1000,1000){
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
   	
   	public void mapNoneCountDown()
   	{
   		new CountDownTimer(1000,1000) {
   			public void onFinish() {
   				mapNone();
   				mapAutoNone = false;
   			}
   			public void onTick(long millisUntilFinished) {
   			}
   		}.start();
   	}
   	
	public void putPlanArr()
	{
		ArrayList<HashMap<String, String>> planList = new ArrayList<HashMap<String, String>>();
		for (int i = 0; i < planArr.length; i++)
		{
			HashMap<String, String> planItems = new HashMap<String, String>();
			
			planItems.put("planName", planArr[i]);
			planItems.put("planInfos","Total " + planDaysArr[i] +" Days,"+ "\n" + "From " + planStartArr[i]
							+ " To " + planEndArr[i]);
			planList.add(planItems);
		}
		planListArr = planList;
	}
	
	
	public void putSpotArr ()
	{
		ArrayList<HashMap<String, Object>> itemList = new ArrayList<HashMap<String, Object>>();
		
		for (int i = 0; i < spotArr.length; i++)
		{
			HashMap<String, Object> planItems = new HashMap<String, Object>();
			
			planItems.put("spot", spotArr[i]);
			planItems.put("day", queArr[i]+ "-" + "Day:" + dayArr[i]);
			
			if (flagFoodArr[i].equals("1"))
			{
				planItems.put("pic1", R.drawable.food_icon);
			} 
			if (flagHotelArr[i].equals("1"))
			{
				planItems.put("pic2", R.drawable.hotel_icon);
			}
			if (flagShopArr[i].equals("1"))
			{
				planItems.put("pic3", R.drawable.shopping_icon);
			}
			if (flagSceneArr[i].equals("1"))
			{
				planItems.put("pic4", R.drawable.scene_icon);
			}
			if (flagTransArr[i].equals("1"))
			{
				planItems.put("pic5", R.drawable.transport_icon);
			}
			
			itemList.add(planItems);
		}
		spotListArr = itemList;
	}
	
	@SuppressWarnings("deprecation")
	public void showPlanWindow()
	{
		putPlanArr();
		
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		view = inflater.inflate(R.layout.expanded_list, null);
		ListView extanded_list = (ListView) view.findViewById(R.id.exList1); 
		RelativeLayout SayHiLayout = (RelativeLayout) findViewById(R.id.SayHi);

		SimpleAdapter planAdapter = new SimpleAdapter
				(Map2Activity.this, planListArr, R.layout.my_list_layout01,
						new String[] {"planName", "planInfos"}, new int[] {R.id.textView_1_1, R.id.textView_1_2});
		
		WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
		
		extanded_list.setAdapter(planAdapter);
		
		double width = wm.getDefaultDisplay().getWidth() / 1.35;
		double height =wm.getDefaultDisplay().getHeight() / 1.5;

		popUp = new PopupWindow (view, (int)width, (int)height);	//(view, width, height)
		
		//popUp.setAnimationStyle(R.style.PopupAnimation);
		popUp.setFocusable(true);
		popUp.setOutsideTouchable(true);
		popUp.setBackgroundDrawable(new BitmapDrawable());
		
		int xpos = wm.getDefaultDisplay().getWidth() / 2 - popUp.getWidth() / 2;
		double ypos = (wm.getDefaultDisplay().getHeight() / 2) - (popUp.getHeight() / 1.55);
		
		Log.i("Coder", "xPos:" + xpos);
		Log.i("Coder", "yPos:" + ypos);
		
		popUp.showAsDropDown(SayHiLayout, xpos, (int)ypos);
		
		extanded_list.setOnItemClickListener(new AdapterView.OnItemClickListener() { 
            public void onItemClick(AdapterView<?> ar0, View arg1, int arg2, long arg3)
            {  
            	String selected = planArr[arg2];
            	String onlyPlanString = planArr[arg2]
            							.substring(planArr[arg2].indexOf(" "), planArr[arg2].lastIndexOf(" ")).trim();
            	planTitle = onlyPlanString;

            	shortMessage("Plan: " + planArr[arg2]);

            	String pid = selected.substring(0,selected.indexOf(" "));
            	final String xmlPidUrl = tourURL + MyName +"/" + pid;
            	
            	new Thread()
            	{
            		public void run()
            		{
            			String xmlPidString = getStringByUrl(xmlPidUrl);
            			planChosen.obtainMessage(REFRESH_DATA, xmlPidString).sendToTarget();
            		}
            	}.start();

            	if (popUp != null)
            	{
            		popUp.dismiss();
            	}
            	ProgressBar loadingPlan = (ProgressBar) findViewById(R.id.loadingPlan);
        		loadingPlan.setVisibility(View.VISIBLE);
            }
		});
	}

	@SuppressWarnings("deprecation")
	public void showSpotWindow()
	{
		if (startNeed == false)
		{
			shortMessage("You haven't select a plan yet~");
		} else {
			final String[] latList = latArr;
			final String[] lngList = lngArr;
			final String[] spotList = spotArr;
			final String[] spotInfoList = spotInfoArr;

			LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			view = inflater.inflate(R.layout.expanded_list, null);
			ListView extanded_list = (ListView) view.findViewById(R.id.exList2); 
			RelativeLayout SayHiLayout = (RelativeLayout) findViewById(R.id.SayHi);

			SimpleAdapter spotAdapter = new SimpleAdapter
					(Map2Activity.this, spotListArr, R.layout.my_list_layout02,
							new String[] {"spot","day","pic1","pic2","pic3","pic4","pic5"}, 
							new int[] {R.id.textView_2_1, R.id.textView_2_2, 
									R.id.imageView1, R.id.imageView2, R.id.imageView3, R.id.imageView4, R.id.imageView5});

			extanded_list.setAdapter(spotAdapter);
			
			WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
			
			double width = wm.getDefaultDisplay().getWidth() / 1.15;
			double height =wm.getDefaultDisplay().getHeight() / 1.3;

			popUp = new PopupWindow (view, (int)width, (int)height);	//(view, width, height)
			
			//popUp.setAnimationStyle(R.style.PopupAnimation);
			popUp.setFocusable(true);
			popUp.setOutsideTouchable(true);
			popUp.setBackgroundDrawable(new BitmapDrawable());

			int xpos = wm.getDefaultDisplay().getWidth() / 2 - popUp.getWidth() / 2;
			double ypos = (wm.getDefaultDisplay().getHeight() / 2) - (popUp.getHeight() / 1.55);

			Log.i("Coder", "xPos:" + xpos);
			Log.i("Coder", "yPos:" + ypos);

			popUp.showAsDropDown(SayHiLayout, xpos, (int)ypos);

			extanded_list.setOnItemClickListener(new AdapterView.OnItemClickListener(){
				public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) 
				{
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
				}
			});
			extanded_list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener(){
				public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2, long arg3)
				{
					String coordinates[] = {latList[arg2], lngList[arg2]};
					double latitude = Double.parseDouble(coordinates[0]);
					double longitude = Double.parseDouble(coordinates[1]);

					GP = new GeoPoint(
							(int)(latitude * 1E6),
							(int)(longitude * 1E6));

					mapControl.animateTo(GP);
					mapControl.setZoom(16);

					shortMessage(spotList[arg2]);
					if (popUp != null)
					{
						popUp.dismiss();
					}
					mapFull();
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
				longMessage("Press BACK again to exit.");
				exitTime = System.currentTimeMillis();
			} else {
				finish();
			}
			return true;
		} 
		return super.onKeyDown(keyCode, event);
	}
	
	@SuppressWarnings("deprecation")
	public void mapNone()
	{
		WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
		
		int width = wm.getDefaultDisplay().getWidth() - wm.getDefaultDisplay().getWidth();
		int height = wm.getDefaultDisplay().getHeight() - wm.getDefaultDisplay().getHeight();
		
		LinearLayout mapArea = (LinearLayout) findViewById(R.id.MapArea);
		LinearLayout.LayoutParams none = new LinearLayout.LayoutParams(width, height);
		
		mapArea.setLayoutParams(none);
	}
	
	@SuppressWarnings("deprecation")
	public void mapHalf()
	{
		WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
		
		int width = wm.getDefaultDisplay().getWidth();
		double height = wm.getDefaultDisplay().getHeight() / 2.8;
		
		LinearLayout mapArea = (LinearLayout) findViewById(R.id.MapArea);
		LinearLayout.LayoutParams half = new LinearLayout.LayoutParams(width, (int)height);
		
		mapArea.setLayoutParams(half);
	}
	
	@SuppressWarnings("deprecation")
	public void mapFull()
	{
		LinearLayout mapArea = (LinearLayout) findViewById(R.id.MapArea);
		LinearLayout.LayoutParams full = new LinearLayout.LayoutParams
											(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
		mapArea.setLayoutParams(full);
	}
	
	public void mapNoneClick(View mapNoneClick)
	{
		mapNone();
	}
	
	public void mapHalfClick(View mapHalfClick)
	{
		mapHalf();
	}
	
	public void mapFullClick(View mapFullClick)
	{
		mapFull();
	}
	
	public void popUpClick(View popWindow)
	{
		showSpotWindow();
	}
	
	public void userNameClick(View userName)
	{
		longMessage("Welcome " + MyName + " (£½_>£¿)");
	}
	
	private void shortMessage (String message)
    {
    	Toast.makeText(Map2Activity.this, message, Toast.LENGTH_SHORT).show();
    }
    private void longMessage (String message)
    {
    	Toast.makeText(Map2Activity.this, message, Toast.LENGTH_LONG).show();
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
    
    @Override
    public void onLocationChanged(Location location) {
    	// TODO Auto-generated method stub
    	Log.v("mapLocation", location.toString());
    	myLocation = location;
    }

    @Override
    public void onProviderDisabled(String provider) {
    	// TODO Auto-generated method stub
    }

    @Override
    public void onProviderEnabled(String provider) {
    	// TODO Auto-generated method stub
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    	// TODO Auto-generated method stub
    }
    
    
    @SuppressLint("NewApi")
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	//(groupId, itemId, order, title)
    	menu.add(0,0,4,"Logout").setIcon(android.R.drawable.ic_menu_close_clear_cancel);
    	menu.add(0,1,3,"Read Me").setIcon(android.R.drawable.ic_menu_info_details);
    	menu.add(0,2,2,"Find My Position").setIcon(android.R.drawable.ic_menu_mylocation);
    	menu.add(0,3,1,"Select Plan").setIcon(android.R.drawable.ic_menu_view);
    	menu.add(0,4,0,"Show Spot List").setIcon(android.R.drawable.ic_menu_view);
    	
    	if (SDKversion > 10)
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
    			.setPositiveButton("No~", new DialogInterface.OnClickListener()
                {  
    				public void onClick(DialogInterface arg0, int arg1)
    				{  
    				} 
               }).setNegativeButton("Yes!", new DialogInterface.OnClickListener()
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
    			shortMessage("Still working on it...");
    			break;
    			
    		case 2:
    			if (stopAsk == false)
    			{
    				requestGPS = true;
    			}
    			GPSinit();
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