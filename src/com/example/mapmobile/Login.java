package com.example.mapmobile;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class Login extends Activity {

	protected static final int REFRESH_DATA = 0x00000001;
	
	 /** Called when the activity is first created. */ 
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.main);
    	this.setTitle("TourPlan-Alpha_v0.4");
    }
    
    
    public void btn1Click(View SignInClick) {  	
    	EditText email_input = (EditText) findViewById(R.id.EmailInput);
    	EditText pass_input = (EditText) findViewById(R.id.PassInput);
    	 		
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
	    			

	    			//Create a Thread!!
	    			//Thread th = new Thread(new sendPostRunnable(msg));
	    			//th.start();
	    			
	    			//String echoResult = sendPostDataToInternet(msg);
    			}
	    	}
    
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
    				Toast.makeText(Login.this,echoResult,Toast.LENGTH_LONG).show();
		    	    		
    			} else {
    				EditText pass_input = (EditText) findViewById(R.id.PassInput);
    				Toast.makeText(Login.this,"You have logged!",Toast.LENGTH_LONG).show();
    				pass_input.setText("");
    			
					Intent goMap = new Intent();
	    	    	goMap.setClass(Login.this,Map2Activity.class);
	    	    				    	    	
	    	    	//Set the parameter to send
	    	    	Bundle userName = new Bundle();
	    	    	userName.putString("name",echoResult);
	    	    	goMap.putExtras(userName);				//Put parameter into bundle
	    	    	
	    	    	Login.this.startActivity(goMap);
					}
				break;
			}
		}
	};
    
    
    
    public void textClick(View goWebClick) {
    		Uri uri = Uri.parse(getString(R.string.Labm406));
    		Intent intent = new Intent(Intent.ACTION_VIEW,uri);
    		startActivity(intent);
    }

    /*
    class sendPostRunnable implements Runnable 
    {
    	String[] strArr = null;
    	
    	public sendPostRunnable(String[] strArr)
    	{
    		this.strArr = strArr;
    	}
    	
    	@Override
    	public void run()
    	{
    		String result = sendPostDataToInternet(strArr);
    		mHandler.obtainMessage(REFRESH_DATA, result).sendToTarget();
    	}
    }
    */
    
    //Create HTTP Connection!!
    
    private String sendPostDataToInternet(String[] strArr)
    {
    	String uriAPI = "http://labm406.serveftp.com/mobileApp/connect.php";
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