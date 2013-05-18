package com.example.mapmobile;

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

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

import android.R.drawable;
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
import android.os.Bundle;
import android.os.Debug;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.ViewGroup.LayoutParams;
import android.view.inputmethod.InputMethodManager;

import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
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
    private Spinner PlanSelector;
    private AutoCompleteTextView typingText;
    private String tourURL = "http://140.128.198.44:406/plandata/";
    protected static final int REFRESH_DATA = 0x00000001;
    
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
        UserName.setText(Name);								//Output the contents of Bundle
        
    	String xmlURL = tourURL + Name;
    	
    	Thread th = new Thread(new sendItToRun(xmlURL));
    	th.start();
    	
    	typingText.setOnKeyListener(searchKey);
    	
    	//String xmlString = getStringByUrl(xmlURL);
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
        
        //mapControl.animateTo(gp);
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
   				
   		        PlanSelector = (Spinner) findViewById(R.id.PlanSelector);
   		        typingText = (AutoCompleteTextView)findViewById(R.id.typingText);
				typingText.setFocusable(true);
		        typingText.setFocusableInTouchMode(true);
		        
   		        Bundle userName = Map2Activity.this.getIntent().getExtras();
   		        final String Name = userName.getString("name").toString();
   				
   				PlanVO planVO = XmlParser.parse(xmlString);
   		       
   		        String res = "-----";
   		        StringTokenizer stPlan = new StringTokenizer(planVO.getPlan(),",");
   		        StringTokenizer stPid = new StringTokenizer(planVO.getPid(),",");
   				stPlan.nextToken();
   				stPid.nextToken();
   					while (stPlan.hasMoreTokens() & stPid.hasMoreTokens()) {
   							res = res + stPid.nextToken()+ "  " + stPlan.nextToken();
   		        
   				        	final String[] plans = res.split(":");
   				        	       
   				        	ArrayAdapter<String> adapter = new ArrayAdapter<String>(Map2Activity.this, android.R.layout.simple_spinner_item, plans);
   				        	adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
   				        	        
   				        	PlanSelector.setAdapter(adapter);
   				        	PlanSelector.setSelection(0,true);
   				        	
   					PlanSelector.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
   						public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
   							//TODO Auto-generated method stub
   							
   							String selected = plans[arg2];

   							if (selected.contains("---")) 
   							{
   								Toast.makeText(Map2Activity.this, "Now, Please choose your plan!", Toast.LENGTH_SHORT).show();
   							} else {
   								Toast.makeText(Map2Activity.this, "Plan: " + arg0.getSelectedItem().toString(), Toast.LENGTH_SHORT).show();
   								//String pidBuffer = pid.trim();
   								String pid = selected.substring(0,selected.indexOf(" "));
	   							final String xmlPidUrl = tourURL + Name +"/" +pid;
   								
   								new Thread()
   								{
   									public void run()
   									{
   										String xmlPidString = getStringByUrl(xmlPidUrl);
   		   								planChosen.obtainMessage(REFRESH_DATA, xmlPidString).sendToTarget();
   									}
   								}.start();
   							}
   						}
   						public void onNothingSelected(AdapterView<?> arg0){
   							Toast.makeText(Map2Activity.this, "Please select a plan", Toast.LENGTH_LONG).show();
   						}
   					});
   					}
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
   					
   					PlanList = (ListView)findViewById(R.id.PlanList);
   					typingText = (AutoCompleteTextView)findViewById(R.id.typingText);
   					typingText.setFocusable(true);
   			        typingText.setFocusableInTouchMode(true);
   			        
   			        TableLayout addInfo = (TableLayout)findViewById(R.id.AddInfo);
   					addInfo.setStretchAllColumns(true);
   					TableLayout.LayoutParams row_layout = new TableLayout.LayoutParams
   														(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
   					TableRow.LayoutParams view_layout = new TableRow.LayoutParams
   														(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
   					addInfo.removeAllViews();
   					
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
					
					final String[] latList = Lat2.split(",");
					final String[] lngList = Lng2.split(",");
					final String[] spotList = Spot2.split(",");
					final String[] spotInfoList = SpotInfo2.split(",");
					final String[] dayList = Day2.split(",");
					final String[] queList = Que2.split(",");
					
					mapView.getOverlays().remove(0);
					Drawable marker1 = getResources().getDrawable(R.drawable.here_icon);
					Bitmap markerScale = ((BitmapDrawable) marker1).getBitmap();
					Drawable marker = new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(markerScale, 35, 45, true));
					
					Marker = new MapOverlay(marker);
					
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
					
					ArrayList<HashMap<String, String>> itemList = new ArrayList<HashMap<String, String>>();
					
					
					for (int i = 0; i < latList.length; i++)
					{
						String coordinates[] = {latList[i], lngList[i]};
						double latitude = Double.parseDouble(coordinates[0]);
						double longitude = Double.parseDouble(coordinates[1]);
						
						GeoPoint points = new GeoPoint(
											(int)(latitude * 1E6),
											(int)(longitude * 1E6));
						
						Marker.setPoint(points, spotList[i], spotInfoList[i]);
						
						HashMap<String, String> planItems = new HashMap<String, String>();
						
						planItems.put("spot", spotList[i]);
						planItems.put("day", queList[i]+ "-" + "Day:" + dayList[i]);
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
					mapView.getOverlays().add(0, Marker);
					mapView.invalidate();
					
					final SimpleAdapter planAdapter = new SimpleAdapter
											(Map2Activity.this, itemList, android.R.layout.simple_list_item_2,
											new String[] {"spot","day"},new int[] {android.R.id.text1, android.R.id.text2});
					PlanList.setAdapter(planAdapter);
					PlanList.setTextFilterEnabled(true);
					
					//typingText.setAdapter(planAdapter);
					
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
							planAdapter.getFilter().filter(s);
						}
                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                        }
                        public void afterTextChanged(Editable s) {
                        }
					});
   			}
   		}
   	};
   	
   	
   	public class MapOverlay extends ItemizedOverlay<OverlayItem> {
   		
   		private List<OverlayItem> Items = new ArrayList<OverlayItem>();
   		
   		public MapOverlay (Drawable defaultMarker) {
   			super (boundCenterBottom(defaultMarker));
   		}
   		
   		public void setPoint (GeoPoint points, String title, String snippet)
   		{
   			Items.add (new OverlayItem(points, title, snippet));
   		}
   		
   		public void finish()
   		{
   			populate();
   		}
   		
   		@Override
   		protected OverlayItem createItem(int i) {
   			return Items.get(i);
   		}
   		
   		@Override
   		public int size() {
   			return Items.size();
   		}
   		
   		@Override
   		protected boolean onTap(int index) {
   			AlertDialog.Builder infoDialog = new AlertDialog.Builder(Map2Activity.this);
   			infoDialog.setIcon(R.drawable.info_icon);
   			infoDialog.setTitle(Items.get(index).getTitle());
   			infoDialog.setMessage(Items.get(index).getSnippet());
   			infoDialog.setPositiveButton("OK!", 
   					new DialogInterface.OnClickListener()
   					{
   						public void onClick(DialogInterface dialog, int which)
   						{
   						//Actions after you press OK!	
   						}
   					});
   			infoDialog.show();
   			//Toast.makeText(Map2Activity.this, Items.get(index).getSnippet(), Toast.LENGTH_SHORT).show();
   			return true;
   		}
   	}
   	
   	
   	public void searchClick(View searchClick) {
   		typingText = (AutoCompleteTextView)findViewById(R.id.typingText);
   		String input = typingText.getText().toString().trim();
   		
   		Drawable marker1 = getResources().getDrawable(R.drawable.search_icon);
   		Bitmap markerScale = ((BitmapDrawable) marker1).getBitmap();
   		Drawable marker = new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(markerScale, 35, 40, true));
   		
   		mapView.getOverlays().remove(locMarker);
   		locMarker = new SearchOverlay(marker);
   		
   		if (input.length() > 0) {
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
   	
   	public class SearchOverlay extends ItemizedOverlay<OverlayItem> {
   		
   		private List<OverlayItem> Items = new ArrayList<OverlayItem>();
   		
   		public SearchOverlay (Drawable defaultMarker) {
   			super (boundCenterBottom(defaultMarker));
   		}
   		
   		public void setPoint (GeoPoint points, String title, String snippet)
   		{
   			Items.add (new OverlayItem(points, title, snippet));
   		}
   		
   		public void finish()
   		{
   			populate();
   		}
   		
   		@Override
   		protected OverlayItem createItem(int i) {
   			return Items.get(i);
   		}
   		
   		@Override
   		public int size() {
   			return Items.size();
   		}
   		
   		@Override
   		protected boolean onTap(int index) {
   			AlertDialog.Builder infoDialog = new AlertDialog.Builder(Map2Activity.this);
   			infoDialog.setIcon(R.drawable.info_icon);
   			infoDialog.setTitle(Items.get(index).getTitle());
   			infoDialog.setMessage(Items.get(index).getSnippet());
   			infoDialog.setPositiveButton("OK!", 
   					new DialogInterface.OnClickListener()
   					{
   						public void onClick(DialogInterface dialog, int which)
   						{
   						//Actions after you press OK!	
   						}
   					});
   			infoDialog.show();
   			//Toast.makeText(Map2Activity.this, Items.get(index).getSnippet(), Toast.LENGTH_SHORT).show();
   			return true;
   		}
   	}

		OnKeyListener searchKey = new OnKeyListener() {
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
			// TODO Auto-generated method stub
				if (keyCode == KeyEvent.KEYCODE_ENTER) 
				{
					InputMethodManager imm = (InputMethodManager)v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
					
					if (imm.isActive()) 
						{
							searchClick(v);
							imm.hideSoftInputFromWindow(v.getApplicationWindowToken(), 0);
						}
					return true;
				}
				return false;
			}
		};

 
/*
    public void testBtnClick(View testClick) {
    	
        new Thread()
        {
        	public void run()
        	{
        		String xmlURL = "http://labm406.serveftp.com/mobileApp/xml/plan_spot.php?uid=22";
        		String xmlString = getStringByUrl(xmlURL);
        		testBtn.obtainMessage(REFRESH_DATA, xmlString).sendToTarget();
        	}
        }.start();
    }
    
    Handler testBtn = new Handler()
    {
    	@Override
    	public void handleMessage(Message msg)
    	{
    		switch (msg.what)
    		{
    		case REFRESH_DATA:
    			String xmlString = null;
    			if (msg.obj instanceof String)
    				xmlString = (String) msg.obj;
    			
    	    	displayText = (TextView)findViewById(R.id.displayText);
    	        PlanSelector = (Spinner) findViewById(R.id.PlanSelector);
    	        
    		    PlanVO planVO = XmlParser.parse(xmlString);

    	        String res = "-----";
    	        StringTokenizer stPlan = new StringTokenizer(planVO.getPlan(),",");
    			stPlan.nextToken();
    				while (stPlan.hasMoreTokens()) {
    						res = res + "\n" + stPlan.nextToken();
    				}
    			displayText.setText(res);
    		}
    	}
    };
*/
    
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
    public boolean onCreateOptionsMenu(Menu menu) {
    	//(groupId, itemId, order, title)
    	menu.add(0,0,0,"Logout").setIcon(android.R.drawable.ic_menu_close_clear_cancel);
    	menu.add(0,1,1,"Read Me").setIcon(android.R.drawable.ic_menu_info_details);
    	menu.add(0,2,2,"Find My Position").setIcon(android.R.drawable.ic_menu_mylocation);
    	return super.onCreateOptionsMenu(menu);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch(item.getItemId()){
    		case 0:
    			Intent goBack = new Intent();
    			Bundle clear = new Bundle();
    			goBack.setClass(Map2Activity.this, Login.class);
    			clear.putString("Clear", "");
    			goBack.putExtras(clear);
    			Map2Activity.this.startActivity(goBack);
    			finish();
    			break;
    		default:
    		
    		case 1:
    			Toast.makeText(Map2Activity.this, "Still working on it...", Toast.LENGTH_LONG).show();
    			break;
    			
    		case 2:
    			findMyLocation();
    			break;
    	}
    	return super.onOptionsItemSelected(item);
    }
}