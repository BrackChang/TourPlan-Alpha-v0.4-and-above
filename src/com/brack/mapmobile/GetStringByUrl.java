package com.brack.mapmobile;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.content.Context;
import android.widget.Toast;

public class GetStringByUrl {
	
	private String Url;
	private Context context;
	
	public GetStringByUrl (String url)
	{
		super();
		this.Url = url;
	}

	public String getString ()
	{
    	HttpGet httpRequest = new HttpGet(Url);
    	String strResult = "";
    	
    	try
    	{
    		HttpResponse httpResponse = new DefaultHttpClient().execute(httpRequest);
    		if (httpResponse.getStatusLine().getStatusCode() == 200)
    		{
    			strResult = EntityUtils.toString(httpResponse.getEntity());
    			return strResult;
    		}
    		else
    			return "Connection Failed!";
    	}
    	
    	catch (ClientProtocolException e)
    	{
    		Toast.makeText(context,e.getMessage().toString(),Toast.LENGTH_LONG).show();
    		e.printStackTrace();
    		return e.toString();
    	}
    	catch (IOException e)
    	{
    		Toast.makeText(context,e.getMessage().toString(),Toast.LENGTH_LONG).show();
    		e.printStackTrace();
    		return e.toString();
    	}
    	catch (Exception e)
    	{
    		Toast.makeText(context,e.getMessage().toString(),Toast.LENGTH_LONG).show();
    		e.printStackTrace();
    		return e.toString();
    	}
	}
}
