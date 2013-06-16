package com.brack.mapmobile;

import java.util.List;
import java.util.Map;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class ExAdapter extends BaseExpandableListAdapter {

private Context context;
List<Map<String, Object>> spotGroup;
List<List<Map<String, String>>> spotChild;
	
	public ExAdapter (Context context, List<Map<String, Object>> groups, List<List<Map<String, String>>> childs)
	{
		this.spotGroup = groups;
		this.spotChild = childs;
		this.context = context;
	}

	@Override
	public Object getChild(int groupPosition, int childPosition) {
		// TODO Auto-generated method stub
		return spotChild.get(groupPosition).get(childPosition);
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		// TODO Auto-generated method stub
		return childPosition;
	}

	@Override
	public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent)
	{
		// TODO Auto-generated method stub
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		RelativeLayout layout = (RelativeLayout) inflater.inflate(R.layout.ex_layout2, null);
		
		@SuppressWarnings("unchecked")
		String itemText = ((Map<String, String>)getChild(groupPosition,childPosition)).get("info");
		
		TextView itemName = (TextView) layout.findViewById(R.id.spotInfo);
		itemName.setText(itemText);
		
		return layout;
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		// TODO Auto-generated method stub
		return spotChild.get(groupPosition).size();
	}

	@Override
	public Object getGroup(int groupPosition) {
		// TODO Auto-generated method stub
		return spotGroup.get(groupPosition);
	}

	@Override
	public int getGroupCount() {
		// TODO Auto-generated method stub
		return spotGroup.size();
	}

	@Override
	public long getGroupId(int groupPosition) {
		// TODO Auto-generated method stub
		return groupPosition;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent)
	{
		// TODO Auto-generated method stub
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		RelativeLayout layout = (RelativeLayout) inflater.inflate(R.layout.ex_layout1, null);
		
		String titleText = (String) spotGroup.get(groupPosition).get("title");
		String titleDescrText = (String) spotGroup.get(groupPosition).get("titleDescribe");
		
		String pic1 = (String) spotGroup.get(groupPosition).get("pic1");
		String pic2 = (String) spotGroup.get(groupPosition).get("pic2");
		String pic3 = (String) spotGroup.get(groupPosition).get("pic3");
		String pic4 = (String) spotGroup.get(groupPosition).get("pic4");
		String pic5 = (String) spotGroup.get(groupPosition).get("pic5");
		
		final String lat = (String) spotGroup.get(groupPosition).get("lat");
		final String lng = (String) spotGroup.get(groupPosition).get("lng");
		
		TextView titleName = (TextView) layout.findViewById(R.id.groupName);
		TextView titleDescrName = (TextView) layout.findViewById(R.id.groupDescr);
		ImageButton showMap = (ImageButton) layout.findViewById(R.id.showMap);
		ImageView foodFlag = (ImageView) layout.findViewById(R.id.typeImage1);
		ImageView hotelFlag = (ImageView) layout.findViewById(R.id.typeImage2);
		ImageView shopFlag = (ImageView) layout.findViewById(R.id.typeImage3);
		ImageView sceneFlag = (ImageView) layout.findViewById(R.id.typeImage4);
		ImageView transFlag = (ImageView) layout.findViewById(R.id.typeImage5);
		
		titleName.setText(titleText);
		titleDescrName.setText(titleDescrText);
		
		showMap.setFocusable(false);
		showMap.setFocusableInTouchMode(false);
		showMap.setClickable(true);
		
		if (pic1 != null) { foodFlag.setImageResource(R.drawable.food_icon); }
		if (pic2 != null) { hotelFlag.setImageResource(R.drawable.hotel_icon); }
		if (pic3 != null) { shopFlag.setImageResource(R.drawable.shopping_icon); }
		if (pic4 != null) { sceneFlag.setImageResource(R.drawable.scene_icon); }
		if (pic5 != null) { transFlag.setImageResource(R.drawable.transport_icon); }
		
		showMap.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				((Map2Activity) context).exListMapMove(lat, lng);
			}
		});
		
		return layout;
	}

	@Override
	public boolean hasStableIds() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		// TODO Auto-generated method stub
		return false;
	}
/*
	@Override
	public Filter getFilter() {
		// TODO Auto-generated method stub
		Filter filter = new Filter() {
			
			@SuppressWarnings("unchecked")
			@Override
			protected void publishResults(CharSequence constraint, FilterResults results)
			{
				// TODO Auto-generated method stub
				spotGroup = (List<Map<String, Object>>) results.values;
				notifyDataSetChanged();
			}
			
			@SuppressLint("DefaultLocale")
			@Override
			protected FilterResults performFiltering(CharSequence arg0)
			{
				// TODO Auto-generated method stub
				FilterResults results = new FilterResults();
				List<Map<String, String>> filterData = new ArrayList<Map<String, String>>();
				
				arg0 = arg0.toString().toLowerCase();
				
				for (int i = 0; i < spotGroup.size(); i++)
				{
					Map<String, String> data = spotGroup.get(i);
					if (data.toString().startsWith(arg0.toString()))
					{
						filterData.add(data);
					}
				}
				
				results.count = filterData.size();
				results.values = filterData;
				Log.i("Value", results.values.toString());
				
				return results;
			}
		};
		return filter;
	}
*/
}
