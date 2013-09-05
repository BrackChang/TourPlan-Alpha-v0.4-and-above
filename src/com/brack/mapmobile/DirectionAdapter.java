package com.brack.mapmobile;

import java.util.List;
import java.util.Map;

import android.content.Context;
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
	
	public DirectionAdapter (Context context,  List<Map<String, Object>> items)
	{
		this.context = context;
		this.items = items;
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
		
		return layout;
	}
}