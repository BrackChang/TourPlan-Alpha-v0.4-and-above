package com.example.mapmobile;

import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;

import android.util.Log;

public class HttpClientConnector {
	
	static String getStringByUrl(String url) {
			String outputString = "";
		
		//Default Http Client
		DefaultHttpClient httpClient = new DefaultHttpClient();
		//Http Get
		HttpGet httpGet = new HttpGet(url);
		//Response Handler
		ResponseHandler<String> resHandler = new BasicResponseHandler();
		
		try {
			outputString = httpClient.execute(httpGet, resHandler);
			Log.i("Brack", "Connect successful");
		} catch (Exception e) {
			Log.i("Brack", "Connect failed");
			e.printStackTrace();
		}
		httpClient.getConnectionManager().shutdown();
		return outputString;
	}
	
}
