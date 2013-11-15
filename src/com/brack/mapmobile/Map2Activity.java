package com.brack.mapmobile;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Debug;
import android.os.Environment;
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
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.ImageView.ScaleType;
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
import com.google.android.maps.Projection;

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
    private String providerType = "";
    private Location myLocation;
    private Criteria criteria;
    
    private String MyName;
    private String planTitle;
    private String planXml;
    private String Pid;
    private String spotXml;
    private int planCount;
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
    private int currentChose;
    private ArrayList<HashMap<String, String>> planListArr;
    private ArrayList<HashMap<String, Object>> spotListArr;
    private ArrayList<HashMap<String, String>> publicListArr;
    private List<Map<String, Object>> directionList;
    private List<String> pathInfoList;
    private int dayCount;
    private int screenWidth;
    private int screenHeight;
    private double screenSize;
    
    @SuppressWarnings("deprecation")
	int SDKversion = Integer.parseInt(VERSION.SDK);
    private boolean startNeed;
    private boolean mapAutoNone = true;
    private boolean requestGPS;
    private boolean stopAsk;
    private boolean enableTool;
    private boolean alreadyPop;
    private boolean listExpanded;
	private String mapStatus;
    private float posX;
	private float posY;
	private float currentPosX;
	private float currentPosY;
    
    /** Called when the activity is first created. **/
    @SuppressWarnings("deprecation")
	@Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        Debug.startMethodTracing("report");
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);	//It must setting up before setContentView!!
        setContentView(R.layout.map);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        
        String version = getResources().getString(R.string.Version);
        String versionName = getResources().getString(R.string.VersionName);
        this.setTitle(versionName + version);
        Debug.stopMethodTracing();
        
        ImageButton pathBtn = (ImageButton) findViewById(R.id.pathBtn);
    	pathBtn.setVisibility(View.GONE);
        typingText = (AutoCompleteTextView)findViewById(R.id.typingText);
        TextView UserName = (TextView)findViewById(R.id.UserName);
       
        Bundle userName = this.getIntent().getExtras();		//Obtain Bundle
        String Name = userName.getString("name").toString();
        MyName = Name;
        UserName.setText(MyName);								//Output the contents of Bundle
        
        loadingBarRun();
    	String xmlURL = tourURL + Name;

    	Thread th = new Thread(new sendItToRun(xmlURL));
    	th.start();
    	
    	typingText.setOnKeyListener(searchKey);
    	
    	WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
    	screenWidth = wm.getDefaultDisplay().getWidth();
		screenHeight = wm.getDefaultDisplay().getHeight();
		
		findMapControl();
		mapHalf();
		
		DisplayMetrics DM = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(DM);
		double diagonalPixels = Math.sqrt((Math.pow(DM.widthPixels, 2) + Math.pow(DM.heightPixels, 2)));
        double screen = diagonalPixels / (160 * DM.density);
        screenSize = screen;
        Log.i("ScreenSize", ""+screenSize);
        Log.i("ScreenDisplay", ""+screenWidth +" x "+ screenHeight);
        
        slideMove();
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
        getLocationProvider();
        
        //findMyLocation();
    }
    
    private void GPSinit()
    {	
    	if (requestGPS == true)
    	{
    		if (!locManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
    		{
    			AlertDialog.Builder infoDialog = new AlertDialog.Builder(Map2Activity.this);
    			
    			TextView title = new TextView(this);
    			title.setText("Increase Accuracy");
    			title.setTextColor(getResources().getColor(R.color.DeepSkyBlue));
				title.setGravity(Gravity.CENTER);
				title.setPadding(0, 20, 0, 20);
    			
    			if (screenSize >= 6.5)
    			{
    				title.setTextSize(30);
    				infoDialog.setCustomTitle(title);
    			} else {
    				title.setTextSize(22);
    				infoDialog.setCustomTitle(title);
    			}
    			infoDialog.setMessage("Turn on the GPS in order to improve location accuracy!")
    			.setCancelable(false).setPositiveButton("Setting", new DialogInterface.OnClickListener() {
    				public void onClick(DialogInterface dialog, int which) {
    					startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
    					findMyLocation();
    				}
    			}).setNegativeButton("NO!", new DialogInterface.OnClickListener() {
    				public void onClick(DialogInterface dialog, int which) {
    					requestGPS = false;
    					stopAsk = true;
    					findMyLocation();
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
    		Log.i("onState", "First Enable!");
    	}
    }
    
    @Override
    protected void onResume()
    {
    	super.onResume();
    	providerType = locManager.getBestProvider(criteria, true);
    	locManager.requestLocationUpdates(providerType, 2000, 1, Map2Activity.this);
    	if (enableTool)
    	{
    		//locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, Map2Activity.this);
    		//locManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 0, Map2Activity.this);
    		myLayer.enableMyLocation();
    		myLayer.enableCompass();
    		longMessage("Location Provider¡G" + providerType);
    		Log.i("ProviderType", providerType);
    		Log.i("onState", "Resume");
    	}
    	else
    	{
    		GPSinit();
    		Log.i("ProviderType", providerType);
    		Log.i("onState", "Resume else");
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
    		Log.i("onState", "Pause");
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
        		int lat = myLayer.getMyLocation().getLatitudeE6();
        		int lng = myLayer.getMyLocation().getLongitudeE6();
        		
        		GeoPoint point = new GeoPoint(lat,lng);
        		
        		if (alreadyPop == true)
        		{
        			if (popUp.isShowing() && mapStatus.equals("full"))
        			{
        				point = new GeoPoint((int)(lat*0.9995),(int)(lng*0.99988));
        			}
        		}
        		mapControl.animateTo(point);
        	}
        });
        locOverlays.add(myLayer);

		if (mapStatus.equals("none"))
			mapHalf();
		
        mapView.invalidate();
    }
    
    public void getLocationProvider()
    {
    	try
    	{
    		criteria = new Criteria();
    		criteria.setAccuracy(Criteria.ACCURACY_FINE);
    		criteria.setAltitudeRequired(false);
    		criteria.setBearingRequired(false);
    		criteria.setCostAllowed(true);
    		criteria.setPowerRequirement(Criteria.POWER_LOW);
    		
    		providerType = locManager.getBestProvider(criteria, true);
    		myLocation = locManager.getLastKnownLocation(providerType);
    		
    		Log.i("LocProviderType", providerType);
    		shortMessage("ProviderType = " + providerType);
    	}
    	catch (Exception e)
    	{
    		longMessage(e.toString());
    		Log.e("LocationProvider", e.getMessage().toString());
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
    	loadingBarRun();
    }
    
    public void routeToSearch(String searchLat, String searchLng)
    {
    	double lat = myLocation.getLatitude();
    	double lng = myLocation.getLongitude();
    	Log.i("MyPosition", ""+lat +" "+ lng);

    	exListMapMove(""+lat, ""+lng);
    	
    	new GoogleDirection().execute(lat + "," + lng, searchLat + "," + searchLng);
    	loadingBarRun();
    }
    
    public void routeToNextSpot(String fromLat, String fromLng, String toLat, String toLng)
    { 	
    	double fromGPLat = Double.parseDouble(fromLat);
    	double fromGPLng = Double.parseDouble(fromLng);
    	
    	double toGPLat = Double.parseDouble(toLat);
    	double toGPLng = Double.parseDouble(toLng);
    	
    	exListMapMove(""+fromGPLat, ""+fromGPLng);
    	
    	new GoogleDirection().execute(fromGPLat + "," + fromGPLng, toGPLat + "," + toGPLng);
    	loadingBarRun();
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
    						directions(strResult);
        				}
    				} else {
    					POLY = "";
    				}
    			}
    		} 
    		catch(Exception e)
    		{
				Log.e("mapUrlFailed", e.toString());
    		}
    		return geoPoints;
    	}

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
    			directionPop();
    			loadingBarStop();
    		} else {
    			loadingBarStop();
    			longMessage("Oops~Routed failed!!\nThe path is NOT drawable, it may caused by the sea crossing path.");
    		}
    	}
    }
    
    public void directions(String routeInJSON)
    {
    	List<Map<String, Object>> dirList = new ArrayList<Map<String, Object>>();
    	List<String> infoList = new ArrayList<String>();
    	try
    	{
    		JSONObject jb = new JSONObject(routeInJSON);
    		JSONArray routesArr = jb.getJSONArray("routes");
    		JSONObject route = routesArr.getJSONObject(0);
    		JSONArray legsArr = route.getJSONArray("legs");
    		JSONObject leg = legsArr.getJSONObject(0);
    		JSONArray stepsArr = leg.getJSONArray("steps");
    		
    		for (int i = 0; i < stepsArr.length(); i++)
    		{
    			JSONObject singleStep = stepsArr.getJSONObject(i);
    			JSONObject distance = singleStep.getJSONObject("distance");
    			JSONObject duration = singleStep.getJSONObject("duration");
    			JSONObject startLocation = singleStep.getJSONObject("start_location");
    			String path2 = singleStep.getString("html_instructions");
    			
    			Map<String, Object> listItem = new HashMap<String, Object>();
    			
    			String path = path2.replace("<b>", "");
    			path = path.replace("</b>", "");
    			if (path.contains("<div"))
    			{
    				String pathGoOn = path.substring(path.indexOf(">")+1, path.lastIndexOf("<"));
    				if (pathGoOn.contains("div"))
    				{
    					StringBuffer clearGoOn = new StringBuffer(pathGoOn);
    					clearGoOn.delete(pathGoOn.indexOf("<"), pathGoOn.lastIndexOf(">"));
    					pathGoOn = clearGoOn.toString().replace(">", "-->");
    				}
    				Log.i("pathGoOn", pathGoOn.toString());
    				
    				listItem.put("pathGoOn", pathGoOn);
    				
    				StringBuffer pathClear = new StringBuffer(path);
    				pathClear.delete(path.indexOf("<"), path.lastIndexOf(">")+1);
    				path = pathClear.toString();
    			}
    			
    			Log.i("Path", path);
    			
    			listItem.put("disText", distance.getString("text"));
                listItem.put("durText", duration.getString("text"));
                listItem.put("startLat", startLocation.getString("lat"));
                listItem.put("startLng", startLocation.getString("lng"));
                
                listItem.put("path", path);
                
                dirList.add(listItem);
    		}
    		JSONObject Dis = leg.getJSONObject("distance");
    		JSONObject Dur = leg.getJSONObject("duration");
    		String Destination = leg.getString("end_address");
    		String Duration = Dur.getString("text");
    		String Distance = Dis.getString("text");
    		String Summary = route.getString("summary");
    		
    		infoList.add(Destination);	//0
    		infoList.add(Duration);		//1
    		infoList.add(Distance);		//2
    		infoList.add(Summary);		//3
    		
    		directionList = dirList;
    		pathInfoList = infoList;
    	}
    	catch (JSONException e)
    	{
    		Log.e("Failed", e.getMessage().toString());
    	}
    }
    
    public void directionPop()
    {
    	ImageButton pathBtn = (ImageButton) findViewById(R.id.pathBtn);
    	
    	if (alreadyPop == true)
    	{
    		if (popUp.isShowing()) {
    			popUp.dismiss();
    		}
    	}
    	if (!pathBtn.isShown())
    		pathBtn.setVisibility(View.INVISIBLE);
    	
    	pathBtn.setVisibility(View.VISIBLE);
    	pathBtn.setImageResource(R.drawable.menu_close_window);
    	pathBtn.setScaleType(ScaleType.CENTER_CROP);

    	LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    	view = inflater.inflate(R.layout.direction_listview, null);
    	LinearLayout functionsLayout = (LinearLayout) findViewById(R.id.Functions);

    	ListView directList = (ListView) view.findViewById(R.id.directionList);
    	TextView destinationText = (TextView) view.findViewById(R.id.Destination);
    	TextView timeNeedText = (TextView) view.findViewById(R.id.TimeNeed);
    	TextView disText = (TextView) view.findViewById(R.id.disText);
    	TextView pathInfoText = (TextView) view.findViewById(R.id.PathInfo);
    	ImageButton findMyPos = (ImageButton) view.findViewById(R.id.FindMyPos);
    	
    	destinationText.setText(pathInfoList.get(0));
    	timeNeedText.setText(pathInfoList.get(1));
    	disText.setText(pathInfoList.get(2));
    	pathInfoText.setText(pathInfoList.get(3));
    	
    	findMyPos.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (stopAsk == false)
    				requestGPS = true;
    			GPSinit();
			}
    	});
    	
    	DirectionAdapter dirAdapter = new DirectionAdapter(this, directionList);
    	directList.setAdapter(dirAdapter);

    	double width = screenWidth / 1.6;
    	double height = screenHeight / 2.16;
    	
    	if (screenSize >= 6.5) {
    		height = screenHeight / 2.06;
    	}

    	popUp = new PopupWindow (view, (int)width, (int)height);
    	
    	alreadyPop = true;

    	popUp.setTouchable(true);
    	popUp.setFocusable(false);
    	popUp.setOutsideTouchable(false);

    	int xpos = screenWidth / 2 - popUp.getWidth();
    	double ypos = (screenHeight / 2) - (popUp.getHeight() * 1.08);

    	if (screenSize >= 6.5) {
    		ypos = (screenHeight / 2) - (popUp.getHeight() * 1.03);
    	}
    	
    	popUp.showAsDropDown(functionsLayout, xpos, (int)ypos);

    	//ListView OnClick event implement in DirectionAdapter!!
    }
    
    public void showPathWindow(View pathView)
    {
    	ImageButton pathBtn = (ImageButton) findViewById(R.id.pathBtn);
    	if (popUp.isShowing()) {
    		popUp.dismiss();
    		pathBtn.setImageResource(R.drawable.path_icon);
        	pathBtn.setScaleType(ScaleType.CENTER_CROP);
    	}
    	else {
    		directionPop();
    		pathBtn.setImageResource(R.drawable.menu_close_window);
        	pathBtn.setScaleType(ScaleType.CENTER_CROP);
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
   				
		        planXml = xmlString;
		        
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
   				loadingBarStop();
   				
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
   					
   					spotXml = xmlPidString;
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
					
					if (mapAutoNone == true) 
						mapNoneCountDown();
					
					mapControl.setZoom(8);
					mapView.getOverlays().remove(drawOverlay);
					
					if (alreadyPop == true)
					{
						popUp.dismiss();
						ImageButton pathBtn = (ImageButton) findViewById(R.id.pathBtn);
						pathBtn.setVisibility(View.GONE);
			    	}
   			}
   		}
   	};
   	
	public void showSpotList()
   	{
   		exSpotList = (ExpandableListView) findViewById(R.id.exPlanList);
   		typingText = (AutoCompleteTextView)findViewById(R.id.typingText);
   		
   		ImageButton listControlBtn = (ImageButton) findViewById(R.id.listControlBtn);
   		ImageButton downloadAllBtn = (ImageButton) findViewById(R.id.downloadAllBtn);
   		listControlBtn.setVisibility(View.VISIBLE);
   		//downloadAllBtn.setVisibility(View.VISIBLE);
	    TextView planName = (TextView) findViewById(R.id.planName);
	    TableRow titleRow = (TableRow) findViewById(R.id.titleRow);
	    TableRow dayRow = (TableRow) findViewById(R.id.dayRow);
	    
	    titleRow.removeAllViews();
		dayRow.removeAllViews();
		
		double btnHeight = screenHeight / 16;
		int textSize = screenHeight / 55;
		int markerWidth = 40;
		int markerHeight = 45;
		
		Log.i("ButtonHeight", ""+btnHeight);
		Log.i("TextSize1", ""+textSize);
		
		if (screenSize < 6.5)
		{
			if (screenWidth >= 480 && screenWidth < 720)
			{
				textSize = 13;
				markerWidth = 40;
				markerHeight = 45;
				btnHeight = 48;
			} 
			else if (screenWidth >= 720 && screenWidth < 800)
			{
				textSize = 16;
				markerWidth = 50;
				markerHeight = 55;
			}
			else
			{
				textSize = 16;
				markerWidth = 70;
				markerHeight = 75;
				btnHeight = 108;
			}
		} else {
			textSize = 22;
			markerWidth = 55;
			markerHeight = 60;
			btnHeight = 68;
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
		
		LayoutParams listBtnParams = listControlBtn.getLayoutParams();
		listBtnParams.height = (int) (btnHeight);
		listControlBtn.setLayoutParams(listBtnParams);
		
		LayoutParams downloadAllBtnParams = downloadAllBtn.getLayoutParams();
		downloadAllBtnParams.height = (int) (btnHeight);
		downloadAllBtn.setLayoutParams(downloadAllBtnParams);
		
		mapView.getOverlays().remove(Marker);
		Drawable marker1 = getResources().getDrawable(R.drawable.map_marker_icon);
		Bitmap markerScale = ((BitmapDrawable) marker1).getBitmap();
		Drawable marker = new BitmapDrawable(getResources(), Bitmap.createScaledBitmap
														(markerScale, markerWidth, markerHeight, true));
		
		Marker = new MapOverlay(marker, this);
   		
   		final List<Map<String, Object>> spotGroup = new ArrayList<Map<String, Object>>();
		final List<List<Map<String, String>>> spotChild = new ArrayList<List<Map<String, String>>>();
		
		boolean day0Tag = true;
		
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
				spotItem.put("pic1", "1");
			
			if (flagHotelArr[i].equals("1"))
				spotItem.put("pic2", "1");
			
			if (flagShopArr[i].equals("1"))
				spotItem.put("pic3", "1");
			
			if (flagSceneArr[i].equals("1"))
				spotItem.put("pic4", "1");
			
			if (flagTransArr[i].equals("1"))
				spotItem.put("pic5", "1");
			
			spotGroup.add(spotItem);
			
			List<Map<String, String>> spotInfos = new ArrayList<Map<String, String>>();
			Map<String, String> spotInfo = new HashMap<String, String>();
			
			spotInfo.put("info", "Info:\n" + spotInfoArr[i]);
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
				params.height = (int) btnHeight;
				dayBtn.setLayoutParams(params);
				
				dayBtn.setOnClickListener(new OnClickListener() {
					public void onClick(View v) {
						String dayString = dayBtn.getText().toString();
						typingText.setText(dayString);
					}
				});
			} else if (day0Tag == true)
			{
				if (dayArr[i].equals("0"))
				{
					final Button dayBtn = new Button(Map2Activity.this);
					dayBtn.setText("Day " + dayArr[i]);
					dayBtn.setTextColor(getResources().getColor(R.drawable.Ivory));
					dayBtn.setGravity(Gravity.CENTER);
					dayBtn.setTextSize(textSize);

					dayRow.addView(dayBtn);

					LayoutParams params = dayBtn.getLayoutParams();
					params.width = LayoutParams.WRAP_CONTENT;
					params.height = (int) btnHeight;
					dayBtn.setLayoutParams(params);

					day0Tag = false;

					dayBtn.setOnClickListener(new OnClickListener() {
						public void onClick(View v) {
							String dayString = dayBtn.getText().toString();
							typingText.setText(dayString);
						}
					});
				}
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
		
   		exAdapter = new ExAdapter(this, spotGroup, spotChild, screenSize);
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
		
		loadingBarStop();
		
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
					exAdapter = new ExAdapter(Map2Activity.this, spot, spotContent, screenSize);
					exSpotList.setAdapter(exAdapter);
				} else {
					exAdapter = new ExAdapter(Map2Activity.this, spotGroup, spotChild, screenSize);
					exSpotList.setAdapter(exAdapter);
				}
			}
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            public void afterTextChanged(Editable s) {
            }
		});
   	}
	
	public void slideMove()
	{
		TableRow titleRow = (TableRow) findViewById(R.id.titleRow);
		
		OnTouchListener touch = new OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction())
				{
				case MotionEvent.ACTION_DOWN:
					posX = event.getX();
					posY = event.getY();
					break;
					
				case MotionEvent.ACTION_MOVE:
					currentPosX = event.getX();
					currentPosY = event.getY();
					Log.i("mapStatus", mapStatus);
					
					if (currentPosY - posY > 0 && Math.abs(currentPosX - posX) < 10)	//Move down
					{
						if (mapStatus.equals("half"))
							mapFull();
						else if (mapStatus.equals("none"))
							mapHalf();
					} 
					else if (currentPosY - posY < 0 && Math.abs(currentPosX - posX) <10)	//Move up
					{
						if (mapStatus.equals("half"))
							mapNone();
						else if (mapStatus.equals("full"))
							mapHalf();
						break;
					}
				}
				return true;
			}
		};
		titleRow.setOnTouchListener(touch);
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
		
		if (alreadyPop == true)
		{
			if (popUp.isShowing() && mapStatus.equals("full"))
			{
				GP = new GeoPoint((int) ((int)(latitude*1E6)*0.9995),(int) ((int)(longitude*1E6)*0.99988));
				mapControl.animateTo(GP);
			}
		}
		
		if (mapStatus.equals("none"))
			mapHalf();
   	}
   	
   	public void clearClick(View clearClick)
   	{
   		typingText = (AutoCompleteTextView) findViewById(R.id.typingText);
   		typingText.setText("");
   	}
   	
	public void searchClick(View searchClick) {
   		typingText = (AutoCompleteTextView)findViewById(R.id.typingText);
   		final String input = typingText.getText().toString().trim().replace(" ", "");
   		
   		InputMethodManager imm = (InputMethodManager)searchClick.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
   		
   		if (input.equals("planlist"))
   		{
   			imm.hideSoftInputFromWindow(searchClick.getApplicationWindowToken(), 0);
   			typingText.setText("");
   			showPlanWindow();
   		} 
   		else if (input.equals("spotlist")){
   			imm.hideSoftInputFromWindow(searchClick.getApplicationWindowToken(), 0);
   			typingText.setText("");
   			showSpotWindow();
   		} 
   		else if (input.length() > 0) 
   		{
   			Geocoder geocoder = new Geocoder(Map2Activity.this);
   			List<Address> addrs = null;
   			Address addr = null;

   			try {
   				addrs = geocoder.getFromLocationName(input, 1);
   				Log.i("findLocation", addrs.toString());
   			}
   			catch (IOException e) {
   				shortMessage("Geocoder¡G" + e.getMessage().toString());
   				Log.e("findLocationError:", e.toString());
   			}

   			if (addrs == null || addrs.isEmpty())
   			{
   				//Log.e("addrsIsEmpty!", addrs.toString());
   				typingText.setText("");
   				imm.hideSoftInputFromWindow(searchClick.getApplicationWindowToken(), 0);
   				
   				new Thread()
            	{
            		public void run()
            		{
            			String JSONString = getLocationInfo(input);
            			LocationJSON.obtainMessage(REFRESH_DATA, JSONString).sendToTarget();
            		}
            	}.start();
            	
   			} else
   			{
   				addr = addrs.get(0);
   				double geoLat = addr.getLatitude() * 1E6;
   				double geoLng = addr.getLongitude() * 1E6;

   				String locationName = addr.getFeatureName() + " - " + addr.getAdminArea() + " (" + addr.getCountryName() + ")";
   				
   				Log.i("LocationFound", locationName);
   				Log.i("LocationFound", addr.getLatitude()+","+addr.getLongitude());

   				putSearchMarker(geoLat, geoLng, locationName);
   				
   				typingText.setText("");
   				imm.hideSoftInputFromWindow(searchClick.getApplicationWindowToken(), 0);
   			}
   		} else {
   			shortMessage("Your Input is Empty!!");
   		}
   	}
	
	public String getLocaleLanguage()
    {
    	Locale locale = Locale.getDefault();
    	return String.format("%s-%s", locale.getLanguage(), locale.getCountry());
    }
	
	private String getLocationInfo(String input)
	{
    	String language = getLocaleLanguage();
    	Log.i("Language", language);
		
		HttpGet httpGet = new HttpGet
				("http://maps.google.com/maps/api/geocode/json?address=" + input + "&sensor=true&language=" + language);
		try
    	{
    		HttpResponse httpResponse = new DefaultHttpClient().execute(httpGet);
    		if (httpResponse.getStatusLine().getStatusCode() == 200)
    		{
    			String strResult = EntityUtils.toString(httpResponse.getEntity());
    			return strResult;
    		}
    	}
    	catch (ClientProtocolException e)
    	{
    		Log.e("getLocationFiled", e.getMessage().toString());
    		e.printStackTrace();
    	}
    	catch (IOException e)
    	{
    		Log.e("getLocationFiled", e.getMessage().toString());
    		e.printStackTrace();
    	}
    	catch (Exception e)
    	{
    		Log.e("getLocationFiled", e.getMessage().toString());
    		e.printStackTrace();
    	}
    	return null;
	}
	
	Handler LocationJSON = new Handler()
   	{
   		@Override
   		public void handleMessage(Message url)
   		{
   			switch (url.what)
   			{
   			case REFRESH_DATA:
   				
   				String JASONString = null;
   				
   				if (url.obj instanceof String)
   					JASONString = (String) url.obj;
   				try
   				{
   					JSONObject jb = new JSONObject(JASONString);
   					String status = jb.getString("status");
   					Log.i("LocationStatus", status);
   					
   					JSONArray ja = jb.getJSONArray("results");
   					JSONObject result = ja.getJSONObject(0);
   					JSONObject location = result.getJSONObject("geometry").getJSONObject("location");

   					String addressName = result.getString("formatted_address");
   					Log.i("LocationFound", addressName);

   					String lat = location.getString("lat");
   					String lng = location.getString("lng");

   					double locLat = Double.parseDouble(lat) * 1E6;
   					double locLng = Double.parseDouble(lng) * 1E6;

   					putSearchMarker(locLat, locLng, addressName);

   					Log.i("LocationFound", lat+","+lng);
   				}
   				catch (JSONException e) {
   					shortMessage("Location not find!");
   					Log.e("LocationError", e.getMessage().toString());
   				}
   			}
   		}
   	};
   	
	public void putSearchMarker(double lat, double lng, String input)
	{
		if (mapStatus.equals("none"))
			mapHalf();
		
		int markerWidth = 40;
		int markerHeight = 45;
		
		if (screenSize < 6.5)
		{
			if (screenWidth >= 480 && screenWidth < 720)
			{
				markerWidth = 40;
				markerHeight = 45;
			} 
			else if (screenWidth >= 720 && screenWidth < 800)
			{
				markerWidth = 50;
				markerHeight = 55;
			}
			else
			{
				markerWidth = 70;
				markerHeight = 75;
			}
		} else {
			markerWidth = 55;
			markerHeight = 60;
		}
   		
   		Drawable marker1 = getResources().getDrawable(R.drawable.map_green_marker_icon);
   		Bitmap markerScale = ((BitmapDrawable) marker1).getBitmap();
   		Drawable marker = new BitmapDrawable(getResources(), Bitmap.createScaledBitmap
   																(markerScale, markerWidth, markerHeight, true));
   		
   		mapView.getOverlays().remove(locMarker);
   		locMarker = new SearchOverlay(marker, this);
   		
   		GP = new GeoPoint((int) lat, (int) lng);

   		locMarker.setPoint(GP, "Your searching result", input);
   		locMarker.finish();
   		mapView.getOverlays().add(locMarker);

   		mapControl.animateTo(GP);
   		mapControl.setZoom(13);
   		mapView.invalidate();
	}

   	OnKeyListener searchKey = new OnKeyListener() {
   		
   		public boolean onKey(View v, int keyCode, KeyEvent event) {
   			// TODO Auto-generated method stub
   			if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_UP) 
   			{	
   				InputMethodManager imm = (InputMethodManager)v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
   				if (imm.isActive())
   				{
   					imm.hideSoftInputFromWindow(v.getApplicationWindowToken(), 0);
   					searchClick(v);
   				}
   			}
   			return false;
   		}
   	};
	
   	public void startCountDown()
   	{
   		new CountDownTimer(800,800){
   			@Override
   			public void onFinish() {
   				// TODO Auto-generated method stub
   				new getSharedPlan().execute();
   				//showPlanWindow();
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
			if (planDaysArr[i].equals("1"))
				planItems.put("planInfos","Total " + planDaysArr[i] +" Day," + " On " + planStartArr[i]);
			else
				planItems.put("planInfos","Total " + planDaysArr[i] +" Days,"+ "\n" + "From " + planStartArr[i]
						+ " to " + planEndArr[i]);
			
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
		view = inflater.inflate(R.layout.popup_list, null);
		final ListView extanded_list = (ListView) view.findViewById(R.id.exList1); 
		RelativeLayout SayHiLayout = (RelativeLayout) findViewById(R.id.SayHi);

		final SimpleAdapter planAdapter = new SimpleAdapter
				(Map2Activity.this, planListArr, R.layout.simple_plan_layout,
						new String[] {"planName", "planInfos"}, new int[] {R.id.textView_1_1, R.id.textView_1_2});
		
		extanded_list.setAdapter(planAdapter);
		
		new CountDownTimer(800, 800)
		{
			public void onFinish()
			{
				HashMap<String, String> publicOption1 = new HashMap<String, String>();
				publicOption1.put("planName", "");
				publicOption1.put("planInfos", "     ¡õ   ¡õ   ¡õ   ¡õ   ¡õ   ¡õ    ");
				planListArr.add(publicOption1);
				
				HashMap<String, String> publicOption2 = new HashMap<String, String>();
				publicOption2.put("planName", "   Check Public Plans");
				if (publicListArr.size() == 0)
					publicOption2.put("planInfos", "Currently no plans shared.");
				else if (publicListArr.size() == 1)
					publicOption2.put("planInfos", "Currently only have " + publicListArr.size() + " plan shared.");
				else
					publicOption2.put("planInfos", "There are " + publicListArr.size() + " Plans have been shared.");
				
				planListArr.add(publicOption2);
				
				extanded_list.setAdapter(planAdapter);
			}
			public void onTick(long millisUntilFinished) {}
		}.start();
		
		double width = screenWidth / 1.35;
		double height = screenHeight / 1.5;

		final PopupWindow planPop = new PopupWindow (view, (int)width, (int)height);	//(view, width, height)
		
		//popUp.setAnimationStyle(R.style.PopupAnimation);
		planPop.setFocusable(true);
		planPop.setOutsideTouchable(true);
		planPop.setBackgroundDrawable(new BitmapDrawable());
		
		int xpos = screenWidth / 2 - planPop.getWidth() / 2;
		double ypos = (screenHeight / 2) - (planPop.getHeight() / 1.55);
		
		Log.i("Coder", "xPos:" + xpos);
		Log.i("Coder", "yPos:" + ypos);
		
		planPop.showAsDropDown(SayHiLayout, xpos, (int)ypos);
		
		extanded_list.setOnItemClickListener(new AdapterView.OnItemClickListener() { 
            public void onItemClick(AdapterView<?> ar0, View arg1, int arg2, long arg3)
            {  
            	String viewText = ((TextView) arg1.findViewById(R.id.textView_1_1)).getText().toString();
            	String shareText = ((TextView) arg1.findViewById(R.id.textView_1_2)).getText().toString();
            	
            	if (viewText.length() == 0)
            	{
            		shortMessage("Ker Ker...");
            	}
            	else if (viewText.contains("Public"))
            	{
            		if (shareText.contains("no plans"))
            			shortMessage("(£½_>£¿)");
            		else
            		{
            			showPublicWindow();
            			planPop.dismiss();
            		}
            	}
            	else
            	{
            		String selected = planArr[arg2];
            		String onlyPlanString = planArr[arg2]
            				.substring(planArr[arg2].indexOf(" "), planArr[arg2].lastIndexOf(" ")).trim();
            		planTitle = onlyPlanString;
            		currentChose = arg2;

            		shortMessage("Plan: " + planArr[arg2]);

            		String pid = selected.substring(0,selected.indexOf(" "));
            		Pid = pid;
            		final String xmlPidUrl = tourURL + MyName +"/" + pid;

            		new Thread()
            		{
            			public void run()
            			{
            				String xmlPidString = getStringByUrl(xmlPidUrl);
            				planChosen.obtainMessage(REFRESH_DATA, xmlPidString).sendToTarget();
            			}
            		}.start();

            		if (planPop != null)
            			planPop.dismiss();
            		
            		loadingBarRun();
            	}
            }
		});
	}
	
	@SuppressWarnings("deprecation")
	public void showPublicWindow()
	{
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		view = inflater.inflate(R.layout.popup_list, null);
		final ListView extanded_list = (ListView) view.findViewById(R.id.publicList); 
		RelativeLayout SayHiLayout = (RelativeLayout) findViewById(R.id.SayHi);

		final SimpleAdapter publicPlanAdapter = new SimpleAdapter
				(Map2Activity.this, publicListArr, R.layout.simple_public_layout,
						new String[] {"plan","days","user"}, new int[] {R.id.textView_plan,R.id.textView_info,R.id.textView_name});
		
		extanded_list.setAdapter(publicPlanAdapter);
		
		double width = screenWidth / 1.35;
		double height = screenHeight / 1.25;

		final PopupWindow publicPop = new PopupWindow (view, (int)width, (int)height);	//(view, width, height)
		
		//popUp.setAnimationStyle(R.style.PopupAnimation);
		publicPop.setFocusable(true);
		publicPop.setOutsideTouchable(true);
		publicPop.setBackgroundDrawable(new BitmapDrawable());
		
		int xpos = screenWidth / 2 - publicPop.getWidth() / 2;
		
		publicPop.showAsDropDown(SayHiLayout, xpos, 10);
		
		extanded_list.setOnItemClickListener(new AdapterView.OnItemClickListener() { 
            public void onItemClick(AdapterView<?> ar0, View arg1, int arg2, long arg3)
            {  
            	String name = publicListArr.get(arg2).get("user").toString().replace("Shared by ", "");
            	String plan = publicListArr.get(arg2).get("plan").toString();
            	
            	Log.i("publicName", name);
            	Log.i("publicPlan", plan);
            	
            	String onlyPlanString = plan.substring(plan.indexOf(" "), plan.lastIndexOf(" ")).trim();
            	planTitle = onlyPlanString;

            	shortMessage("Plan: " + plan);

            	String pid = plan.substring(0,plan.indexOf(" "));
            	Pid = pid;
            	final String xmlPidUrl = tourURL + name +"/" + pid;

            	new Thread()
            	{
            		public void run()
            		{
            			String xmlPidString = getStringByUrl(xmlPidUrl);
            			planChosen.obtainMessage(REFRESH_DATA, xmlPidString).sendToTarget();
            		}
            	}.start();

            	if (publicPop != null)
            		publicPop.dismiss();

            	loadingBarRun();
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
			view = inflater.inflate(R.layout.popup_list, null);
			ListView extanded_list = (ListView) view.findViewById(R.id.exList2); 
			RelativeLayout SayHiLayout = (RelativeLayout) findViewById(R.id.SayHi);

			SimpleAdapter spotAdapter = new SimpleAdapter
					(Map2Activity.this, spotListArr, R.layout.simple_spot_layout,
							new String[] {"spot","day","pic1","pic2","pic3","pic4","pic5"}, 
							new int[] {R.id.textView_2_1, R.id.textView_2_2, 
									R.id.typePic1, R.id.typePic2, R.id.typePic3, R.id.typePic4, R.id.typePic5});

			extanded_list.setAdapter(spotAdapter);
			
			double width = screenWidth / 1.15;
			double height = screenHeight / 1.25;

			final PopupWindow spotPop = new PopupWindow (view, (int)width, (int)height);	//(view, width, height)
			
			//popUp.setAnimationStyle(R.style.PopupAnimation);
			spotPop.setFocusable(true);
			spotPop.setOutsideTouchable(true);
			spotPop.setBackgroundDrawable(new BitmapDrawable());

			int xpos = screenWidth / 2 - spotPop.getWidth() / 2;
			//double ypos = (screenHeight / 2) - (popUp.getHeight() / 1.54);

			spotPop.showAsDropDown(SayHiLayout, xpos, 1);

			extanded_list.setOnItemClickListener(new AdapterView.OnItemClickListener(){
				public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) 
				{
					AlertDialog.Builder infoDialog = new AlertDialog.Builder(Map2Activity.this);
					
					TextView title = new TextView(Map2Activity.this);
					title.setText(spotList[arg2]);
					title.setTextColor(getResources().getColor(R.color.DeepSkyBlue));
					title.setGravity(Gravity.CENTER);
					title.setPadding(0, 15, 0, 15);
					
					if (screenSize >= 6.5)
					{
						title.setTextSize(30);
						infoDialog.setCustomTitle(title);
					} else {
						title.setTextSize(22);
						infoDialog.setCustomTitle(title);
					}
					
					infoDialog.setMessage(spotInfoList[arg2]);
					infoDialog.setPositiveButton("OK!", 
							new DialogInterface.OnClickListener()
					{
						public void onClick(DialogInterface dialog, int which)
						{
							//Actions after you press OK!	
						}
					});
					AlertDialog dialog = infoDialog.create();
					dialog.show();
					
					dialog.getWindow().getAttributes();
					TextView msgText = (TextView) dialog.findViewById(android.R.id.message);
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
					if (spotPop != null)
					{
						spotPop.dismiss();
					}
					mapFull();
					return true;
				}
			});
		}
	}

	public void saveData()
	{
		File dir = getDir("offLine", Context.MODE_PRIVATE);
		File userFolder = new File(dir, MyName);
		if(!userFolder.exists())
			userFolder.mkdir();
		File planList = new File (userFolder, "planList.xml");
		File planWithPid = new File (userFolder, "plan"+Pid+".xml");
		
		try
		{
			FileOutputStream FOS_planList = new FileOutputStream(planList);
			FileOutputStream FOS_spots = new FileOutputStream(planWithPid);
			//FOS = openFileOutput("plans", Context.MODE_PRIVATE);	//Create file in the folder "files".
			FOS_planList.write(planXml.getBytes());
			FOS_spots.write(spotXml.getBytes());
			FOS_planList.close();
			FOS_spots.close();
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
			Log.e("FileNotFound",e.getMessage().toString());
		}
		catch (IOException e)
		{
			e.printStackTrace();
			Log.e("IOException", e.getMessage().toString());
		}
		longMessage(planTitle + "\n" + "Save completed!");
		
		//External storage!!
		/*
		File SDCard = null;
		try
		{
			if (Environment.getExternalStorageState().equals(Environment.MEDIA_REMOVED))
				return;
			else
				SDCard = Environment.getExternalStorageDirectory();
			
			File path = new File(SDCard.getParent() + "/" + SDCard.getName() + "/tourPlanSaved/" + MyName + "/");
			if (!path.exists())
				path.mkdirs();
			
			FileWriter planFiles = new FileWriter
					(SDCard.getParent() + "/" + SDCard.getName() + "/tourPlanSaved/" + MyName + "/" + "plans.xml");
			planFiles.write(planXml);
			planFiles.close();
		}
		catch (Exception e)
		{
			longMessage(e.getMessage().toString());
			Log.e("SaveFailed", e.getMessage().toString());
		}
		shortMessage("Done!");
		*/
		/*
		DBHelper dbHelper = new DBHelper(this, MyName);
		SQLiteDatabase DB = null;
		
		DB = dbHelper.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put("planName", planTitle);
		values.put("spots", "Test~~");
		values.put("spotInfo", "One more Test~~");
		
		DB.insertOrThrow(MyName, null, values);
		DB.close();
		*/
	}
	
	public void saveAllPlans()
	{
		loadingBarRun();
		
		planCount = 0;
		
		for (int i = 0; i < planArr.length; i++)
		{
			final String pid = planArr[i].substring(0, planArr[i].indexOf(" "));
			final String xmlUrl = tourURL + MyName + "/" + pid;
			
			new Thread()
			{
				public void run()
				{
					String xmlString = pid + " " + getStringByUrl(xmlUrl);
					saveAll.obtainMessage(REFRESH_DATA, xmlString).sendToTarget();
				}
			}.start();
		}
	}
	
	Handler saveAll = new Handler()
   	{
   		@Override
   		public void handleMessage(Message url)
   		{
   			switch (url.what)
   			{
   			case REFRESH_DATA:
   				
   				String xmlString = null;
   				
   				if (url.obj instanceof String)
   					xmlString = (String) url.obj;
   				
   				String pid = xmlString.substring(0, xmlString.indexOf(" "));
   				StringBuffer xmlBuf = new StringBuffer(xmlString);
   				xmlBuf.delete(0, xmlBuf.indexOf(" ")+1);
   				String xml = xmlBuf.toString();
   					
   				File dir = getDir("offLine", Context.MODE_PRIVATE);
   				File userFolder = new File(dir, MyName);
   				if (!userFolder.exists())
   					userFolder.mkdir();
   				File planList = new File(userFolder, "planList.xml");
   				File planWithPid = new File(userFolder,"plan" + pid + ".xml");
   				
   				try
   				{
   					FileOutputStream FOS_planList = new FileOutputStream(planList);
   					FileOutputStream FOS_spots = new FileOutputStream(planWithPid);
   					
   					FOS_planList.write(planXml.getBytes());
   					FOS_spots.write(xml.getBytes());
   					FOS_planList.close();
   					FOS_spots.close();
   				}
   				catch (FileNotFoundException e)
   				{
   					e.printStackTrace();
   					Log.e("FileNotFound",e.getMessage().toString());
   				}
   				catch (IOException e)
   				{
   					e.printStackTrace();
   					Log.e("IOException", e.getMessage().toString());
   				}
   				
   				planCount = planCount + 1;
   				if (planCount == planArr.length) {
   					loadingBarStop();
   					longMessage("All plans save completed!");
   				}
   			}
   		}
   	};
	
	public void laterSave(final String queue)
   	{
   		new CountDownTimer(2000,1000){
   			@Override
   			public void onFinish() {
   				// TODO Auto-generated method stub
   				saveMapImage(queue);
   			}
   			@Override
   			public void onTick(long millisUntilFinished) {
   				// TODO Auto-generated method stub
   				mapControl.setZoom(17);
   			}
   		}.start();
   	}
	
	public void saveMapImage(String queue)
	{
		boolean drawingEnable = mapView.isDrawingCacheEnabled();
		mapView.setDrawingCacheEnabled(true);
		Bitmap bm = mapView.getDrawingCache();
		
		File SDCard = null;
		try
		{
			if (Environment.getExternalStorageState().equals(Environment.MEDIA_REMOVED))
				return;
			else
				SDCard = Environment.getExternalStorageDirectory();
			
			File path = new File
						(SDCard.getParent() + "/" + SDCard.getName() + "/tourPlanSaved/" + MyName + "/" + Pid + "/");
			if (!path.exists())
				path.mkdirs();
			
			final File imageFile = new File(path, queue + ".png");
			
			BufferedOutputStream outStream = new BufferedOutputStream(new FileOutputStream(imageFile));
			boolean success = bm.compress(CompressFormat.PNG, 100, outStream);
			outStream.flush();
			outStream.close();
			
			mapView.setDrawingCacheEnabled(drawingEnable);
			
			longMessage("Map Image Saved!");
			
			if (success)
			{
				MediaScannerConnection.scanFile
					(this, new String[] { imageFile.toString() }, new String[] {"image/png"},
							new MediaScannerConnection.OnScanCompletedListener() {
						public void onScanCompleted(String path, Uri uri) {
							Log.i("FileScanner", "Scanned: " + path);
							Log.i("FileScanner", "Uri = " + uri);
						}
					});
			}
		}
		catch (Exception e)
		{
			longMessage(e.toString());
			Log.e("ImageSaveError", e.toString());
		}
	}
	
	private class getSharedPlan extends AsyncTask <Void, Void, Void>
	{
		private List<String> userName = new ArrayList<String>();
		private List<String> pid = new ArrayList<String>();
		private List<String> planName = new ArrayList<String>();
		private List<String> planDays = new ArrayList<String>();
		private List<String> planStart = new ArrayList<String>();
		private List<String> planEnd = new ArrayList<String>();

		@Override
		protected void onPreExecute()
		{
			loadingBarRun();
		}
		
		@Override
		protected Void doInBackground(Void... params)
		{
			String url = "http://140.128.198.44/mobileApp/shared_json.php";
			try
			{
				GetStringByUrl urlString = new GetStringByUrl(url);
				String urlResult = urlString.getString();

				JSONObject jo = new JSONObject(urlResult);
				JSONArray ja = jo.getJSONArray("result");
				Log.i("JSONArrayLength",""+ja.length());
				for (int i = 0; i < ja.length(); i++)
				{
					String name = ja.getJSONObject(i).getString("name");
					String planid = ja.getJSONObject(i).getString("planid");
					String plan_name = ja.getJSONObject(i).getString("planname");
					String total_days = ja.getJSONObject(i).getString("total_days");
					String plan_startDate = ja.getJSONObject(i).getString("plan_start_date");
					String plan_endDate = ja.getJSONObject(i).getString("plan_end_date");

					userName.add(name);
					pid.add(planid);
					planName.add(plan_name);
					planDays.add(total_days);
					planStart.add(plan_startDate);
					planEnd.add(plan_endDate);

				}
			}
			catch (Exception e)
			{
				Log.e("shareException", e.getMessage().toString());
			}
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result)
		{
			String[] nameArr = userName.toArray(new String[userName.size()]);
			String[] pidArr = pid.toArray(new String[pid.size()]);
			String[] plannameArr = planName.toArray(new String[planName.size()]);
			String[] totalDaysArr = planDays.toArray(new String[planDays.size()]);
			String[] startDateArr = planStart.toArray(new String[planStart.size()]);
			String[] endDateArr = planEnd.toArray(new String[planEnd.size()]);
			
			ArrayList<HashMap<String, String>> publicList = new ArrayList<HashMap<String, String>>();

			for (int i = 0; i < pidArr.length; i++)
			{
				HashMap<String, String> listItem = new HashMap<String, String>();
				
				listItem.put("user", "Shared by " + nameArr[i]);
				listItem.put("plan", pidArr[i] + " " + plannameArr[i] + " ");
				if (totalDaysArr[i].equals("1"))
					listItem.put("days", "Total " + totalDaysArr[i] + " Day, On " + startDateArr[i]);
				else
					listItem.put("days", "Total " + totalDaysArr[i]+" Days," + "\n" + 
									"From "+startDateArr[i]+" to "+endDateArr[i]);
				
				publicList.add(listItem);
			}
			publicListArr = publicList;
			Log.i("getPublicList", "Done!");
			showPlanWindow();
			loadingBarStop();
		}
	}
	
	private class sharePlan extends AsyncTask <Void, Void, Void>
	{
		private String postResult;
		
		@Override
		protected void onPreExecute()
		{
			loadingBarRun();
		}
		
		@Override
		protected Void doInBackground(Void... params)
		{
			postResult = sendToPHP();
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result)
		{
			Log.i("PostResult", postResult);
			if (postResult.contains("already"))
				shortMessage("Plan Already Exists!");
			else if (postResult.contains("Done"))
				shortMessage("Done!");
			else {
				shortMessage("Oops! Something Wrong~");
				Log.e("WrongWrong", postResult);
			}
			
			loadingBarStop();
		}
	}
	
	public String sendToPHP()
	{
		String url = "http://140.128.198.44/mobileApp/share.php";
		HttpPost post = new HttpPost(url);
		
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("username", MyName));
		params.add(new BasicNameValuePair("pid", Pid));
		params.add(new BasicNameValuePair("plan", planTitle));
		params.add(new BasicNameValuePair("days", planDaysArr[currentChose]));
		params.add(new BasicNameValuePair("startDate", planStartArr[currentChose]));
		params.add(new BasicNameValuePair("endDate", planEndArr[currentChose]));
		
		try
		{
			post.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
			HttpResponse HR = new DefaultHttpClient().execute(post);
			
			if (HR.getStatusLine().getStatusCode() == 200)
			{
				String result = EntityUtils.toString(HR.getEntity());
				return result;
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			Log.e("PostError", e.getMessage().toString());
		}
		return null;
	}
	
	public void savedDataTest()
	{
		//Get the folder list behind offLine!
		File dirs = getDir("offLine",Context.MODE_PRIVATE);
		File folders[] = dirs.listFiles();
		
		List<String> folderList = new ArrayList<String>();
		for (int i = 0; i < folders.length; i++)
		{
			folderList.add(folders[i].getName());
		}
		Log.i("Folders", ""+folderList);
		//Get the folder list behind offLine!
		
		//Get the files content!
		StringBuilder fileContent_plans = new StringBuilder();
		StringBuilder fileContent_spots = new StringBuilder();
		File dir = new File(dirs, MyName);
		File planList = new File(dir, "planList.xml");
		File planWithPid = new File(dir, "plan"+Pid+".xml");
		try
		{
			FileInputStream FIS_plans = new FileInputStream(planList);
			FileInputStream FIS_spots = new FileInputStream(planWithPid);
			BufferedReader planReader = new BufferedReader(new InputStreamReader(FIS_plans,"UTF-8"));
			BufferedReader spotReader = new BufferedReader(new InputStreamReader(FIS_spots,"UTF-8"));
			
			String planLine = null;
			String spotLine = null;
			
			while ((planLine = planReader.readLine()) != null )
			{
				fileContent_plans.append(planLine).append("\n");
			}
			while ((spotLine = spotReader.readLine()) != null)
			{
				fileContent_spots.append(spotLine).append("\n");
			}
			FIS_plans.close();
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
		String plans = fileContent_plans.toString();
		Log.i("Plans", plans);
		String spots = fileContent_spots.toString();
		Log.i("Spots", spots);

		//External Storage!!
		/*
		String sdPath = Environment.getExternalStorageDirectory().toString();
		File folderPath = null;
		
		try
		{
			if (Environment.getExternalStorageState().equals(Environment.MEDIA_REMOVED))
				return;
			else
			{
				folderPath = new File(sdPath + "/tourPlanSaved/");
				
				if (!folderPath.exists())
					folderPath.mkdirs();
					
				File folders[] = folderPath.listFiles();
				
				List<String> folderList = new ArrayList<String>();
				for (int i = 0; i < folders.length; i++)
				{
					folderList.add(folders[i].getName());
				}
				Log.i("Folders", ""+folderList);
				longMessage(""+folderList);
			}
		}
		catch (Exception e)
		{
			Log.e("Data test failed",e.toString());
			longMessage(e.toString());
		}

		File planXml = new File
				(Environment.getExternalStorageDirectory() + "/tourPlanSaved/" + MyName + "/" + "plans.xml");

		Log.i("planXml", XML.toString());
		*/
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
	
	public void mapNone()
	{
		exSpotList = (ExpandableListView) findViewById(R.id.exPlanList);
		RelativeLayout infoArea = (RelativeLayout) findViewById(R.id.InfoArea);
		View line3 = (View) findViewById(R.id.Line3);
		exSpotList.setVisibility(View.VISIBLE);
		infoArea.setVisibility(View.VISIBLE);
		line3.setVisibility(View.VISIBLE);
		
		LinearLayout mapArea = (LinearLayout) findViewById(R.id.MapArea);
		mapArea.setVisibility(View.GONE);
		mapStatus = "none";
		
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
	}
	
	public void mapHalf()
	{
		double height = screenHeight / 2.8;
		
		exSpotList = (ExpandableListView) findViewById(R.id.exPlanList);
		RelativeLayout infoArea = (RelativeLayout) findViewById(R.id.InfoArea);
		View line3 = (View) findViewById(R.id.Line3);
		exSpotList.setVisibility(View.VISIBLE);
		infoArea.setVisibility(View.VISIBLE);
		line3.setVisibility(View.VISIBLE);
		
		LinearLayout mapArea = (LinearLayout) findViewById(R.id.MapArea);
		LinearLayout.LayoutParams half = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, (int)height);
		
		mapArea.setVisibility(View.VISIBLE);
		mapArea.setLayoutParams(half);
		mapStatus = "half";
		
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
	}
	
	public void mapFull()
	{
		RelativeLayout infoArea = (RelativeLayout) findViewById(R.id.InfoArea);
		exSpotList = (ExpandableListView) findViewById(R.id.exPlanList);
		View line3 = (View) findViewById(R.id.Line3);
		
		infoArea.setVisibility(View.GONE);
		exSpotList.setVisibility(View.GONE);
		line3.setVisibility(View.GONE);

		LinearLayout mapArea = (LinearLayout) findViewById(R.id.MapArea);
		LinearLayout.LayoutParams full = new LinearLayout.LayoutParams
											(LayoutParams.MATCH_PARENT, 0, 200);
		mapArea.setVisibility(View.VISIBLE);
		mapArea.setLayoutParams(full);
		mapStatus = "full";
		
		if (alreadyPop == true)
		{
			if (popUp.isShowing())
			{
				Projection p = mapView.getProjection();
				GeoPoint point = p.fromPixels(200, 400);
				mapControl.animateTo(point);
			}
		}
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
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
		openOptionMenu();
	}
	
	public void userNameClick(View userName)
	{
		longMessage("Welcome " + MyName + " (£½_>£¿)");
	}
	
	public void listControl(View listCtrl)
	{
		exSpotList = (ExpandableListView) findViewById(R.id.exPlanList);
		int spotCount = exSpotList.getExpandableListAdapter().getGroupCount();
		Log.i("SpotCount", ""+spotCount);
		ImageButton listControl = (ImageButton) findViewById(R.id.listControlBtn);
		
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
	
	public void shortMessage (String message)
    {
		View toastRoot = getLayoutInflater().inflate(R.layout.toast, null);
    	TextView toastText = (TextView) toastRoot.findViewById(R.id.myToast);
    	toastText.setText(message);
    	
    	Toast toastStart = new Toast(Map2Activity.this);
    	toastStart.setGravity(Gravity.BOTTOM, 0, 60);
    	toastStart.setDuration(Toast.LENGTH_SHORT);
    	toastStart.setView(toastRoot);
    	toastStart.show();
    }
    public void longMessage (String message)
    {
    	View toastRoot = getLayoutInflater().inflate(R.layout.toast, null);
    	TextView toastText = (TextView) toastRoot.findViewById(R.id.myToast);
    	toastText.setText(message);
    	
    	Toast toastStart = new Toast(Map2Activity.this);
    	toastStart.setGravity(Gravity.BOTTOM, 0, 60);
    	toastStart.setDuration(Toast.LENGTH_LONG);
    	toastStart.setView(toastRoot);
    	toastStart.show();
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
    		return strTarget.replaceAll(strFrom, strTo);
    	else  
    		return strTarget;  
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
    
    public void planBtnClick(View v)
    {
    	new getSharedPlan().execute();
    }
    
    public void loadingBarRun()
    {
    	ProgressBar loadingPlan = (ProgressBar) findViewById(R.id.loadingPlan);
		loadingPlan.setVisibility(View.VISIBLE);
    }
    public void loadingBarStop()
    {
    	ProgressBar loadingPlan = (ProgressBar) findViewById(R.id.loadingPlan);
		loadingPlan.setVisibility(View.GONE);
    }
    
    public void openOptionMenu()
    {
    	super.openOptionsMenu();
    }
    
    @SuppressWarnings("deprecation")
	@Override
    public boolean onMenuOpened(int featureId, Menu menu)
    {
    	String[] menuItems = new String[]
    	{
    		"Save All Plans for Offline",
    		"Save Selected Plan for Offline",
    		"Find My Position",
    		"Show Spot List",
    		"Read Me",
    		"Back to Login Screen",
    		"Logout",
    		"imageTest"
    	};
    	int[] menuIcons = new int[]
    	{
    		R.drawable.save_all_icon,
    		R.drawable.save_icon,
    		android.R.drawable.ic_menu_mylocation,
    		android.R.drawable.ic_menu_view,
    		R.drawable.info_icon,
    		R.drawable.go_back_icon,
    		R.drawable.logout_icon,
    		android.R.drawable.ic_menu_manage
    	};
    		
    	List<Map<String, Object>> menuList = new ArrayList<Map<String, Object>>();
    	
    	for (int i = 0; i < menuItems.length; i++)
    	{
    		HashMap<String, Object> items = new HashMap<String, Object>();
    		items.put("menuItems", menuItems[i]);
    		items.put("menuIcons", menuIcons[i]);
    		menuList.add(items);
    	}
    	MenuListAdapter menuAdapter = new MenuListAdapter(this, menuList);
    	
    	LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		view = inflater.inflate(R.layout.menu_listview, null);
		ListView menu_list = (ListView) view.findViewById(R.id.menuList); 
		RelativeLayout SayHiLayout = (RelativeLayout) findViewById(R.id.SayHi);
		
		menu_list.setAdapter(menuAdapter);
		
		double width = screenWidth / 1.25;
		double height = screenHeight / 1.4;

		final PopupWindow popMenu = new PopupWindow (view, (int)width, (int)height);
		
		popMenu.setFocusable(true);
		popMenu.setOutsideTouchable(true);
		popMenu.setBackgroundDrawable(new BitmapDrawable());

		int xpos = screenWidth / 2;

		popMenu.showAsDropDown(SayHiLayout, xpos, 1);
		
		view.setFocusableInTouchMode(true);
		view.setOnKeyListener(new OnKeyListener() {
		@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				// TODO Auto-generated method stub
				if ((keyCode == KeyEvent.KEYCODE_MENU) && (popMenu.isShowing()))
				{
					popMenu.dismiss();
					return true;
				}
				return false;
			}
		});
		
		menu_list.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				switch (arg2)
				{
					case 0:
						//savedDataTest();
						saveAllPlans();
		    			break;
					case 1:
						if (startNeed == false)
							longMessage("Please select a plan first!");
						else
							saveData();
		    			break;
					case 2:
						if (stopAsk == false)
		    			{
		    				requestGPS = true;
		    			}
		    			GPSinit();
		    			break;
					case 3:
						showSpotWindow();
						break;
					case 4:
						AlertDialog.Builder infos = new AlertDialog.Builder(Map2Activity.this);
						
						TextView infoTitle = new TextView(Map2Activity.this);
						infoTitle.setText("Screen Info");
						infoTitle.setTextColor(getResources().getColor(R.color.DeepSkyBlue));
						infoTitle.setGravity(Gravity.CENTER);
						infoTitle.setPadding(0, 20, 0, 20);
						
		    			if (screenSize >= 6.5)
		    			{
		    				infoTitle.setTextSize(30);
		    				infos.setCustomTitle(infoTitle);
		    			} else {
		    				infoTitle.setTextSize(22);
		    				infos.setCustomTitle(infoTitle);
		    			}
		    			infos.setMessage("Screen Size = "+screenSize +
		    										"\nScreen Width = "+screenWidth + "\nScreen Height = "+screenHeight);
		    			infos.setPositiveButton("OK!", new DialogInterface.OnClickListener()
		                {  
		    				public void onClick(DialogInterface arg0, int arg1)
		    				{  
		    				} 
		               });
		               	AlertDialog dialogStyle = infos.create();
		    			dialogStyle.show();
		    			
		    			dialogStyle.getWindow().getAttributes();
		    			TextView infoText = (TextView) dialogStyle.findViewById(android.R.id.message);
						Button okBtn = (Button) dialogStyle.getButton(DialogInterface.BUTTON_POSITIVE);
						okBtn.setTextColor(getResources().getColor(R.drawable.DarkOrange));
		    			
		    			if (screenSize >= 6.5)
						{
		    				infoText.setTextSize(28);
		    				infoText.setPadding(10, 15, 10, 15);
							okBtn.setTextSize(28);
							okBtn.setPadding(0, 15, 0, 15);
						} else {
							infoText.setTextSize(18);
							infoText.setPadding(10, 15, 10, 15);
							okBtn.setTextSize(18);
							okBtn.setPadding(0, 15, 0, 15);
						}
						
						break;
					case 5:
						Intent goBack = new Intent();
		    			goBack.setClass(Map2Activity.this, Login.class);
		    			Map2Activity.this.startActivity(goBack);
		    			finish();
						break;
					case 6:
						AlertDialog.Builder infoDialog = new AlertDialog.Builder(Map2Activity.this);
						
						TextView title = new TextView(Map2Activity.this);
	        			title.setText("Are you sure?!");
	        			title.setTextColor(getResources().getColor(R.color.DeepSkyBlue));
	    				title.setGravity(Gravity.CENTER);
	    				title.setPadding(0, 20, 0, 20);
						
		    			if (screenSize >= 6.5)
		    			{
		    				title.setTextSize(30);
		    				infoDialog.setCustomTitle(title);
		    			} else {
		    				title.setTextSize(22);
		    				infoDialog.setCustomTitle(title);
		    			}
		    			infoDialog.setPositiveButton("No~", new DialogInterface.OnClickListener()
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
		               	});
		               	AlertDialog dialog = infoDialog.create();
		    			dialog.show();
		    			
		    			dialog.getWindow().getAttributes();
						Button positive = (Button) dialog.getButton(DialogInterface.BUTTON_POSITIVE);
						Button negative = (Button) dialog.getButton(DialogInterface.BUTTON_NEGATIVE);
						positive.setTextColor(getResources().getColor(R.drawable.DarkOrange));
						negative.setTextColor(getResources().getColor(R.drawable.Brown));
		    			
		    			if (screenSize >= 6.5)
						{
							positive.setTextSize(28);
							positive.setPadding(0, 15, 0, 15);
							negative.setTextSize(28);
							negative.setPadding(0, 15, 0, 15);
						} else {
							positive.setTextSize(18);
							positive.setPadding(0, 15, 0, 15);
							negative.setTextSize(18);
							negative.setPadding(0, 15, 0, 15);
						}
						break;
						
					case 7:
						if (startNeed == false)
							longMessage("You haven't select a plan yet~");
						else {
							new sharePlan().execute();
						}
							
						break;
						
					default:
						shortMessage("Oops!! Something wrong~");
				}
				popMenu.dismiss();
			}
		});
    	
    	return false;
    }
    
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	//(groupId, itemId, order, title)
    	MenuInflater menuInflater = getMenuInflater();
    	menuInflater.inflate(R.menu.options, menu);
    	return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	return super.onOptionsItemSelected(item);
    }
}