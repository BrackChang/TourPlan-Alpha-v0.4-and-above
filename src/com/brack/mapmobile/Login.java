package com.brack.mapmobile;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.BitmapDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

@SuppressLint("HandlerLeak")
public class Login extends Activity {
	
	private double screenSize;
	private SharedPreferences infoSave;
	private SharedPreferences.Editor editor;
	private ConnectivityManager CM;
	private NetworkInfo NI;
//	private WifiManager wifi;
//	private WifiInfo wifiInfo;
	protected static final int REFRESH_DATA = 0x00000001;
	private PopupWindow popUp;
	/*
	protected static final int STOP = 0x10000;  
    protected static final int NEXT = 0x10001;  
    private int iCount = 0;
    */
    private String loading;
    private String ipMsg;
	
	 /** Called when the activity is first created. */ 
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.main);
    	String version = getResources().getString(R.string.Version);
        String versionName = getResources().getString(R.string.VersionName);
        this.setTitle(versionName + version);
    	
        DisplayMetrics DM = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(DM);
		double diagonalPixels = Math.sqrt((Math.pow(DM.widthPixels, 2) + Math.pow(DM.heightPixels, 2)));
        double screen = diagonalPixels / (160 * DM.density);
        screenSize = screen;
        
    	final CheckBox rememberMe = (CheckBox)findViewById(R.id.remember);
    	final EditText emailInput = (EditText)findViewById(R.id.EmailInput);
    	final EditText passInput = (EditText)findViewById(R.id.PassInput);
    	emailInput.setFocusable(true);
    	emailInput.setFocusableInTouchMode(true);
    	passInput.setFocusable(true);
    	passInput.setFocusableInTouchMode(true);
    	
    	passInput.setOnKeyListener(goKey);
    	
    	infoSave = getPreferences(Activity.MODE_PRIVATE);
    	String email_save = infoSave.getString("email", "");
    	String pass_save = infoSave.getString("pass", "");
    	
    	emailInput.setText(email_save);
    	passInput.setText(pass_save);
    	
    	Bundle clear = this.getIntent().getExtras();
    	
    	if (clear != null)
    	{
    		String makeClear = clear.getString("Clear").toString();
    		if (makeClear != null) 
    		{
        		emailInput.setText(makeClear);
    			passInput.setText(makeClear);
    			
    			editor = infoSave.edit();
				editor.putString("email", "");
				editor.putString("pass", "");
				editor.commit();
        	}
    	}
    	if (emailInput.getText().toString().contains("@"))
    		rememberMe.setChecked(true);
    	
    	rememberMe.setOnCheckedChangeListener(new OnCheckedChangeListener() {
    		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
    			if (isChecked)
    			{
    				editor = infoSave.edit();
    				editor.putString("email", emailInput.getText().toString());
    				editor.putString("pass", passInput.getText().toString());
    				editor.commit();
    			}
    			else
    			{
    				editor = infoSave.edit();
    				editor.putString("email", "");
    				editor.putString("pass", "");
    				editor.commit();
    			}
    		}
    	});
    	
    	new Thread()
    	{
    		public void run()
    		{
    			getCurrentIP();
    			showIP.obtainMessage(REFRESH_DATA, ipMsg).sendToTarget();
    		}
    	}.start();
    }

    /*
    public boolean isWiFiActive() {
		ConnectivityManager CM = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
		if (CM != null)
		{
			NetworkInfo[] infos = CM.getAllNetworkInfo();
			if (infos != null)
			{
				for (int i = 0; i < infos.length; i++)
				{
					if (infos[i].getTypeName().contains("WIFI") && infos[i].isConnected())
					{
						return true;
					}
				}
			}
		}
		return false;
	}
	*/
    /*
    public String pingHost()
    {
    	String result = "";
    	try
    	{
    		Process p = Runtime.getRuntime().exec("/system/bin/ping -c 8 " + "google.com.tw");
    		
    		int count = p.waitFor();
    		String echo = Integer.toString(count);
    		
    		BufferedReader reader = new BufferedReader( new InputStreamReader(p.getInputStream()));
    		
    		int i;
    		char[] buffer = new char[4096];
    		
    		StringBuffer output = new StringBuffer();
    		
    		while ((i = reader.read(buffer)) > 0)
    			output.append(buffer, 0, i);
    		
    		reader.close();
    		
    		result = echo;
    	}
    	catch (IOException e)
    	{
    		longMessage(e.getMessage().toString());
    		Log.e("PingFailed", e.getMessage().toString());
    	}
    	catch (InterruptedException e)
    	{
    		longMessage(e.getMessage().toString());
    		Log.e("Interruption", e.getMessage().toString());
    	}
    	return result;
    }
    */
    
    public void getCurrentIP ()
    {
    	try
    	{
    		GetStringByUrl urlString = new GetStringByUrl("http://api.externalip.net/ip");
    		String response = urlString.getString();
    		ipMsg = response;
    		/*
    		HttpClient httpClient = new DefaultHttpClient();
    		HttpGet httpGet = new HttpGet("http://api.externalip.net/ip");
    		HttpResponse response;

    		response = httpClient.execute(httpGet);

    		Log.i("Connect Status",response.getStatusLine().toString());

    		HttpEntity entity = response.getEntity();
    		
    		if (entity != null)
    		{
    			long len = entity.getContentLength();
    			
    			if (len != -1 && len < 1024)
    			{
    				ipMsg = EntityUtils.toString(entity);
    				Log.i("External Ip", ipMsg);
    			}
    			else
    			{
    				ipMsg = "Response too long or error! " + EntityUtils.toString(entity);
    				Log.e("IP Error!", "Response too long or error: " + EntityUtils.toString(entity));
    			}            
    		} else {
    			ipMsg = response.getStatusLine().toString();
    			Log.i("Connect Status", response.getStatusLine().toString());
    		}
    		*/
    	}
    	catch (Exception e)
    	{
    		ipMsg = e.toString();
    		Log.e("Connect Error", e.toString());
    	}
    }
    
    Handler showIP = new Handler()
	{
		@Override
		public void handleMessage(Message msg)
		{
			switch (msg.what)
			{
			case REFRESH_DATA:
				
				String IP = null;
				
				if (msg.obj instanceof String)
					IP = (String) msg.obj;
				
				if (IP.contains("ProtocolException") || IP.contains("IllegalStateException"))
				{
					Button SignInBtn = (Button) findViewById(R.id.SignInBtn);
					TextView SignUp = (TextView) findViewById(R.id.Register);
					TextView goWeb = (TextView) findViewById(R.id.goWeb);
					
					SignInBtn.setClickable(false);
					SignUp.setClickable(false);
					goWeb.setClickable(false);
					
					noConnection();
					longMessage(IP);
				}
				else if (IP.contains("Exception"))
					longMessage(IP);
				else
					longMessage("External IP¡G" + IP);
				
				break;
			}
		}
	};
    
    public void btn1Click(View SignInClick) {
    	EditText email_input = (EditText) findViewById(R.id.EmailInput);
    	EditText pass_input = (EditText) findViewById(R.id.PassInput);
    	CheckBox rememberMe = (CheckBox)findViewById(R.id.remember);
    	infoSave = getPreferences(Activity.MODE_PRIVATE);
    	
    	if (rememberMe.isChecked())
    	{
    		editor = infoSave.edit();
    		editor.putString("email", email_input.getText().toString());
    		editor.putString("pass", pass_input.getText().toString());
    		editor.commit();
    	} else {
    		editor = infoSave.edit();
    		editor.putString("email", "");
    		editor.putString("pass", "");
    		editor.commit();
    	}
    	/*
    	wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
    	wifiInfo = wifi.getConnectionInfo();
    	int ip = wifiInfo == null ? 0 : wifiInfo.getIpAddress();
    	
    	if (wifi.isWifiEnabled() && ip == 0)
    		longMessage("Your WiFi is NOT connected yet!");
    	*/
    	CM = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
    	NI = CM.getActiveNetworkInfo();

    	if (NI == null || NI.isAvailable() == false)
    		longMessage("Your Network is NOT Available!");
    	else 
    	{
    		final String[] msg = new String[2];

    		if (email_input != null && pass_input != null)
    		{
    			msg[0] = email_input.getEditableText().toString();
    			msg[1] = pass_input.getEditableText().toString();

    			new Thread()
    			{
    				public void run()
    				{
    					String result = sendPostDataToInternet(msg);
    					mHandler.obtainMessage(REFRESH_DATA, result).sendToTarget();
    				}
    			}.start();
    		}
    	}
    }
    
    OnKeyListener goKey = new OnKeyListener() {
		@Override
		public boolean onKey(View v, int keyCode, KeyEvent event) {
			// TODO Auto-generated method stub
			if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_UP) {
				
    			InputMethodManager input = (InputMethodManager)v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
    			
    			if (input.isActive()) {
    				btn1Click(v);
    				input.hideSoftInputFromWindow(v.getApplicationWindowToken(), 0);
    			}
    			return true;
    		}
			return false;
		}
    };
    
    Handler mHandler = new Handler()
	{
		@Override
		public void handleMessage(Message msg)
		{
			switch (msg.what)
			{
			//Display the catch data from Internet.
			case REFRESH_DATA:
				
				String echoResult = null;
				
				if (msg.obj instanceof String)
					echoResult = (String) msg.obj;
				
				if (echoResult.contains("Dude! Who the hell are you?!"))
    			{
    				longMessage(echoResult);
    			} else if (echoResult.contains("You SUCK"))
    				{
    					longMessage("CANNOT Connect to Database!");
    				} else if (echoResult.contains("The database is SUCKS"))
    				{
    					longMessage("The Database is Not Found!");
    				} else {
    					popLoadingBar(echoResult);
    					/*
    					longMessage("You are logged!");

    					Intent goMap = new Intent();
    					goMap.setClass(Login.this, Map2Activity.class);

    					//Set the parameter to send
    					Bundle userName = new Bundle();
    					userName.putString("name",echoResult);
    					goMap.putExtras(userName);				//Put parameter into bundle

    					Login.this.startActivity(goMap);

    					finish();
    					*/
    				}
				break;
			}
		}
	};
	
	public void reTypeClick(View reType)
	{
		EditText emailInput = (EditText)findViewById(R.id.EmailInput);
    	EditText passInput = (EditText)findViewById(R.id.PassInput);
    	emailInput.setText("");
    	passInput.setText("");
	}
    
    public void textClick(View goWebClick)
    {
    	CM = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
    	NI = CM.getActiveNetworkInfo();
    	
    	if (NI == null || NI.isAvailable() == false)
    	{
    		longMessage("Your Network is NOT Available!");
    	} else {
    		Uri uri = Uri.parse(getString(R.string.Labm406));
    		Intent intent = new Intent(Intent.ACTION_VIEW,uri);
    		startActivity(intent);
    	}
    }
    
    public void signUpClick (View register) {
    	CM = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
    	NI = CM.getActiveNetworkInfo();
    	
    	if (NI == null || NI.isAvailable() == false)
    	{
    		longMessage("Your Network is NOT Available!");
    	} else {
    		longMessage("Not open for register yet~");
    	}
    }
    
    public void offLineClick (View offlineClick) {
    	Intent goOffLine = new Intent();
    	goOffLine.setClass(Login.this, OffLineMode.class);
    	Login.this.startActivity(goOffLine);
    }
    
    
    @SuppressWarnings("deprecation")
	public void popLoadingBar(final String echo)
    {
    	LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    	View view = inflater.inflate(R.layout.pop_progress, null);
    	LinearLayout Header = (LinearLayout) findViewById(R.id.Header);
    	
    	WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
    	
    	final ProgressBar loadingBar = (ProgressBar) view.findViewById(R.id.loadingBar);
    	final TextView loadingText = (TextView) view.findViewById(R.id.loadingText);
    	final TextView loadingDone = (TextView) view.findViewById(R.id.loadingDone);
    	loading = "Fetching Data";
    	loadingText.setText(loading);
    	
    	double width = wm.getDefaultDisplay().getWidth() / 1.2;
		double height =wm.getDefaultDisplay().getHeight() / 7;
		
		popUp = new PopupWindow (view, (int)width, (int)height);
		
		popUp.setFocusable(true);
		popUp.setOutsideTouchable(true);
		popUp.setBackgroundDrawable(new BitmapDrawable());
		
		int xpos = wm.getDefaultDisplay().getWidth() / 2 - popUp.getWidth() / 2;
		double ypos = (wm.getDefaultDisplay().getHeight() / 4) - (popUp.getHeight());
		
		popUp.showAsDropDown(Header, xpos, (int)ypos);
		
    	loadingBar.setVisibility(View.VISIBLE);
    	loadingText.setVisibility(View.VISIBLE);
    	loadingBar.setProgress(0);
    	
    	new CountDownTimer(2000,400){
   			public void onFinish() {
   				loadingDone.setText("Successful Logged!!");
   				
   				Intent goMap = new Intent();
				goMap.setClass(Login.this, Map2Activity.class);

				//Set the parameter to send
				Bundle userName = new Bundle();
				userName.putString("name",echo);
				goMap.putExtras(userName);				//Put parameter into bundle

				Login.this.startActivity(goMap);
				
				finishCountDown();
   			}
   			public void onTick(long millisUntilFinished) {
   				String loads = loading + ".";
   				loadingText.setText(loads);
   				loading = loads;
   			}
   		}.start();
    }
    
    public void noConnection()
    {
    	AlertDialog.Builder warning = new AlertDialog.Builder(Login.this);
		
		TextView title = new TextView(this);
		title.setText("Network Error!!");
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
		warning.setMessage("Can't connect to Internet!\n" +
				"The WiFi you currently connected may need to log on,\n" +
				"And restart this App for using online mode.")
		.setPositiveButton("Alright~", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				
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
    
    
    private void longMessage(String message)
    {
    	View toastRoot = getLayoutInflater().inflate(R.layout.toast, null);
    	TextView toastText = (TextView) toastRoot.findViewById(R.id.myToast);
    	toastText.setText(message);
    	
    	Toast toastStart = new Toast(Login.this);
    	toastStart.setGravity(Gravity.BOTTOM, 0, 60);
    	toastStart.setDuration(Toast.LENGTH_LONG);
    	toastStart.setView(toastRoot);
    	toastStart.show();
    }
    
    public void finishCountDown()
   	{
   		new CountDownTimer(4000, 1000)
   		{
   			public void onFinish()
   			{
   				popUp.dismiss();
   				finish();
   			}
   			public void onTick(long millisUntilFinished) 
   			{ }
   		}.start();
   	}
    
    /*
    private Handler loadingHandler = new Handler()
    {
    	public void handleMessage(Message msg)
    	{
    		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	    	View view = inflater.inflate(R.layout.pop_progress, null);
	    	ProgressBar loadingBar = (ProgressBar) view.findViewById(R.id.loadingBar);
	    	
    		switch (msg.what)
    		{
    		case STOP:
    	    	loadingBar.setVisibility(View.GONE);
    	    	Thread.currentThread().interrupt();
    	    	break;
    		
    		case NEXT:
    			if (!Thread.currentThread().isInterrupted())
    			{
    				loadingBar.setProgress(iCount);
    			}
    			break;
    		}
    	}
    };
    */
    
    //Create HTTP Connection!!
    
    private String sendPostDataToInternet(String[] strArr)
    {
    	String uriAPI = "http://140.128.198.44/mobileApp/connect.php";
    	HttpPost httpRequest = new HttpPost(uriAPI);
    	
    	List<NameValuePair> params = new ArrayList<NameValuePair>();
    	params.add(new BasicNameValuePair("user",strArr[0]));
    	params.add(new BasicNameValuePair("pass",strArr[1]));
    	
    	try
    	{
    		httpRequest.setEntity(new UrlEncodedFormEntity(params,HTTP.UTF_8));
    		HttpResponse httpResponse = new DefaultHttpClient().execute(httpRequest);
    		
    		if (httpResponse.getStatusLine().getStatusCode() == 200)
    		{
    			String strResult = EntityUtils.toString(httpResponse.getEntity());
    			return strResult;
    		}
    	}
    	catch (Exception e)
        {
            e.printStackTrace();
        }
    	
    	return null;
    }
}