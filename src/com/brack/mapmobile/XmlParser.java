package com.brack.mapmobile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InputStream;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.app.Activity;
import android.util.Log;

public class XmlParser extends Activity {
	
	public static PlanVO parse(String xmlString) {

		Log.i("XmlParser", "StartParse");
		
		PlanVO planVO = new PlanVO();
		
		try {
			InputStream bin = new ByteArrayInputStream(xmlString.toString().getBytes("UTF-8"));
			InputStreamReader ISR = new InputStreamReader(bin,"UTF-8");
			
			XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
			XmlPullParser parser = factory.newPullParser();
			
			parser.setInput(ISR);
			parser.next();
			

			int eventType = parser.getEventType();

			while (eventType != XmlPullParser.END_DOCUMENT) {
				switch (eventType) {
				
				case XmlPullParser.START_DOCUMENT:
					break;

				case XmlPullParser.START_TAG:
					String tagName = parser.getName();
					
					if (tagName.equals("name")) {
							Log.i("Brack", "plan= " + parser.getAttributeValue(0));
							//Log.i("Brack", "planText= " + parser.getText());
							planVO.setPlan(parser.getAttributeValue(0));
					}
					if (tagName.equals("planid")) {
						Log.i("Brack", "pid= " + parser.getAttributeValue(0));
						planVO.setPid(parser.getAttributeValue(0));
					}
					
					if (tagName.equals("itemname")) {
						Log.i("Brack", "Spot= " + parser.getAttributeValue(0));
						planVO.setSpot(parser.getAttributeValue(0));
					}
					
					if (tagName.equals("infocontent")) {
						
						if (parser.getAttributeValue(0).length() == 0)
						{
							Log.i("Brack", "Info= " + "Empty!!!!!!!!!!!!!!!!!");
							planVO.setSpotInfo("Ker Ker...");
						}
						else
						{
							Log.i("Brack", "Info= " + parser.getAttributeValue(0));
							planVO.setSpotInfo(parser.getAttributeValue(0));
						}
					}
					
					if (tagName.equals("day")) {
						Log.i("Brack", "Day " + parser.getAttributeValue(0));
						planVO.setDay(parser.getAttributeValue(0));
					}
					
					if (tagName.equals("lat")) {
						Log.i("Brack", "Latitude= " + parser.getAttributeValue(0));
						planVO.setLat(parser.getAttributeValue(0));
					}
					if (tagName.equals("lng")) {
						Log.i("Brack", "Longitude= " + parser.getAttributeValue(0));
						planVO.setLng(parser.getAttributeValue(0));
					}
					
					if (tagName.equals("queue")) {
						Log.i("Brack", "Queue= " + parser.getAttributeValue(0));
						planVO.setQue(parser.getAttributeValue(0));
					}
					
					if (tagName.equals("days")) {
						Log.i("Brack", "planDays= " + parser.getAttributeValue(0));
						planVO.setPlanDays(parser.getAttributeValue(0));
					}
					
					if (tagName.equals("start")) {
						Log.i("Brack", "planStart= " + parser.getAttributeValue(0));
						planVO.setPlanStart(parser.getAttributeValue(0));
					}
					
					if (tagName.equals("end")) {
						Log.i("Brack", "planEnd= " + parser.getAttributeValue(0));
						planVO.setPlanEnd(parser.getAttributeValue(0));
					}
					
					if (tagName.equals("flag_food")) {
						Log.i("Brack", "flagFood= " + parser.getAttributeValue(0));
						planVO.setFlagFood(parser.getAttributeValue(0));
					}
					
					if (tagName.equals("flag_hotel")) {
						Log.i("Brack", "flagHotel= " + parser.getAttributeValue(0));
						planVO.setFlagHotel(parser.getAttributeValue(0));
					}
					
					if (tagName.equals("flag_shopping")) {
						Log.i("Brack", "flagShop= " + parser.getAttributeValue(0));
						planVO.setFlagShop(parser.getAttributeValue(0));
					}
					
					if (tagName.equals("flag_scene")) {
						Log.i("Brack", "flagScene= " + parser.getAttributeValue(0));
						planVO.setFlagScene(parser.getAttributeValue(0));
					}
					
					if (tagName.equals("flag_transport")) {
						Log.i("Brack", "flagTrans= " + parser.getAttributeValue(0));
						planVO.setFlagTrans(parser.getAttributeValue(0));
					}
					break;
				case XmlPullParser.END_TAG:
					break;
				case XmlPullParser.END_DOCUMENT:
					break;
				}

				eventType = parser.next();
			}
			
			ISR.close();
			bin.close();
			
		} catch (XmlPullParserException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		Log.i("XmlParser", "ParseSuccessful");

		return planVO;
	}
	
}