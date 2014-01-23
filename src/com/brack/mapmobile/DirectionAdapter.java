package com.brack.mapmobile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

public class DirectionAdapter extends BaseAdapter {
	
	private Context context;
	List<Map<String, Object>> items;
	ArrayList<Poi> pois;
	
	private LocationManager locManager;
	private boolean countable;
	private boolean viewable;
	
	public DirectionAdapter (Context context,  List<Map<String, Object>> items)
	{
		this.context = context;
		this.items = items;
		
		locManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
		locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, LocationChange);
    	locManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 1, LocationChange);
		
		putPoi();
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return items.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.direction_list, null);
		
		TextView pathText = (TextView) layout.findViewById(R.id.pathText);
		TextView pathInfoText = (TextView) layout.findViewById(R.id.pathInfoText);
		TextView pathGoOnText = (TextView) layout.findViewById(R.id.pathGoOnText);
		LinearLayout pathLayout = (LinearLayout) layout.findViewById(R.id.pathLayout);

		String path = (String) items.get(position).get("path");
		String distance = (String) items.get(position).get("disText");
		String pathGoOn = (String) items.get(position).get("pathGoOn");
		
		final String lat = (String) items.get(position).get("startLat");
		final String lng = (String) items.get(position).get("startLng");
		
		pathText.setText(path);
		pathInfoText.setText(distance);
		if (pathGoOn != null)
			pathGoOnText.setText(pathGoOn);
		
		pathLayout.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				((Map2Activity)context).exListMapMove(lat, lng);
			}
		});
		
		if (viewable)
		{
			String pathName = pois.get(0).getName();
			if (path.equals(pathName))
				pathText.setTextColor(context.getResources().getColor(R.drawable.DarkOrange));
			
		}
		
		return layout;
	}
	
	public void putPoi()
	{
		pois = new ArrayList<Poi>();
		for (int i = 0; i < items.size(); i++)
		{
			double lat = Double.parseDouble((String) items.get(i).get("startLat"));
			double lng = Double.parseDouble((String) items.get(i).get("startLng"));
			String path = items.get(i).get("path").toString();
			pois.add(new Poi(path, lat, lng));
		}
		countable = true;
	}
	
	private void distanceSort(ArrayList<Poi> poi)
	{
		Collections.sort(poi, new Comparator<Poi>()
		{
			@Override
			public int compare(Poi poi1, Poi poi2)
			{
				return poi1.getDistance() < poi2.getDistance() ? -1 : 1;
			}
		});
		viewable = true;
		
		String pathName = pois.get(0).getName();
		((Map2Activity) context).setNearestNum(pathName.substring(0, pathName.indexOf("-")));
	}
	
	public LocationListener LocationChange = new LocationListener()
	{
		@Override
		public void onLocationChanged(Location location) {
			// TODO Auto-generated method stub
			Log.d("AdapterLocation",""+location);
			if (countable && !(""+location).equals("null"))
			{
				for (Poi poi : pois)
				{
					poi.setDistance(((Map2Activity)context).distance(location.getLatitude(),
							location.getLongitude(),
							poi.getLatitude(),
							poi.getLongitude()));

				}
				distanceSort(pois);
				notifyDataSetChanged();
			}
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
	};
	
	public void removeUpdate()
	{
		locManager.removeUpdates(LocationChange);
	}
}