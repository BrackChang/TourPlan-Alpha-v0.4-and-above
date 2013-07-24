package com.brack.mapmobile;

import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
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
		
		final DisplayMetrics DM = new DisplayMetrics();
		((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(DM);
		double diagonalPixels = Math.sqrt((Math.pow(DM.widthPixels, 2) + Math.pow(DM.heightPixels, 2)));
        final double size = diagonalPixels / (160 * DM.density);
		
		String titleText = (String) spotGroup.get(groupPosition).get("title");
		String titleDescrText = (String) spotGroup.get(groupPosition).get("titleDescribe");
		
		String pic1 = (String) spotGroup.get(groupPosition).get("pic1");
		String pic2 = (String) spotGroup.get(groupPosition).get("pic2");
		String pic3 = (String) spotGroup.get(groupPosition).get("pic3");
		String pic4 = (String) spotGroup.get(groupPosition).get("pic4");
		String pic5 = (String) spotGroup.get(groupPosition).get("pic5");
		
		final String lat = (String) spotGroup.get(groupPosition).get("lat");
		final String lng = (String) spotGroup.get(groupPosition).get("lng");
		
		ImageButton directRoute = (ImageButton) layout.findViewById(R.id.routeDirection);
		directRoute.setFocusable(false);
		directRoute.setFocusableInTouchMode(false);
		directRoute.setClickable(true);
		
		int next = spotGroup.size();
		
		if(groupPosition == next-1) {
			directRoute.setOnClickListener(new OnClickListener() {
				@SuppressWarnings("deprecation")
				public void onClick(View v) {
					LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
					View view = inflater.inflate(R.layout.route_selection, null);

					ImageButton route1 = (ImageButton) view.findViewById(R.id.route1);
					
					double width = DM.widthPixels / 4;
					double height = DM.heightPixels / 11.5;
					double btnWidth = DM.widthPixels / 4;
					Log.i("BtnWidth", ""+btnWidth);
					
					if (size >= 6.5)
					{
						btnWidth = DM.widthPixels / 4.9;
						width = DM.widthPixels / 4.9;
						height = DM.heightPixels / 15;
					}

					LinearLayout.LayoutParams params = new LinearLayout.LayoutParams
														((int)btnWidth, LayoutParams.WRAP_CONTENT);
					route1.setLayoutParams(params);

					final PopupWindow popUp = new PopupWindow (view, (int)width, (int)height);
					popUp.setFocusable(true);
					popUp.setOutsideTouchable(true);
					popUp.setBackgroundDrawable(new BitmapDrawable());

					double xpos = DM.widthPixels / 8 - popUp.getWidth();
					double ypos = DM.heightPixels / 50 - (popUp.getHeight() / 0.5);
					
					if (size >= 6.5)
					{
						ypos = DM.heightPixels / 50 - (popUp.getHeight() / 0.42);
					}

					popUp.showAsDropDown(v, (int)xpos, (int)ypos);

					route1.setOnClickListener(new OnClickListener() {
						public void onClick(View v) {
							((Map2Activity) context).routeFromMyPosition(lat, lng);
							((Map2Activity) context).mapHalf();
							InputMethodManager imm = (InputMethodManager)v.getContext().
													getSystemService(Context.INPUT_METHOD_SERVICE);
							imm.hideSoftInputFromWindow(v.getApplicationWindowToken(), 0);
							popUp.dismiss();
						}
					});		
				}
			});
		}
		else {
			final String nextLat = (String) spotGroup.get(groupPosition+1).get("lat");
			final String nextLng = (String) spotGroup.get(groupPosition+1).get("lng");
			
			directRoute.setOnClickListener(new OnClickListener() {
				@SuppressWarnings("deprecation")
				public void onClick(View v) {
					LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
					View view = inflater.inflate(R.layout.route_selection, null);
					
					ImageButton route1 = (ImageButton) view.findViewById(R.id.route1);
					ImageButton route2 = (ImageButton) view.findViewById(R.id.route2);
					
					double width = DM.widthPixels / 1.9;
					double height = DM.heightPixels / 11.5;
					double btnWidth = DM.widthPixels / 4;
					Log.i("BtnWidth", ""+btnWidth);
					
					if (size >= 6.5)
					{
						btnWidth = DM.widthPixels / 4.9;
						width = DM.widthPixels / 2.355;
						height = DM.heightPixels / 15;
					}
					
					LinearLayout.LayoutParams params = new LinearLayout.LayoutParams
														((int)btnWidth, LayoutParams.WRAP_CONTENT);
					route1.setLayoutParams(params);
					route2.setLayoutParams(params);
					
					final PopupWindow popUp = new PopupWindow (view, (int)width, (int)height);
					popUp.setFocusable(true);
					popUp.setOutsideTouchable(true);
					popUp.setBackgroundDrawable(new BitmapDrawable());
					
					double xpos = DM.widthPixels / 5 - popUp.getWidth();
					double ypos = DM.heightPixels / 50 - (popUp.getHeight() / 0.5);
					
					if (size >= 6.5)
					{
						ypos = DM.heightPixels / 50 - (popUp.getHeight() / 0.42);
					}
					
					popUp.showAsDropDown(v, (int)xpos, (int)ypos);
					
					final InputMethodManager imm = (InputMethodManager)v.getContext().
													getSystemService(Context.INPUT_METHOD_SERVICE);
					
					route1.setOnClickListener(new OnClickListener() {
						public void onClick(View v) {
							((Map2Activity) context).routeFromMyPosition(lat, lng);
							((Map2Activity) context).mapHalf();
							imm.hideSoftInputFromWindow(v.getApplicationWindowToken(), 0);
							popUp.dismiss();
						}
					});
					route2.setOnClickListener(new OnClickListener() {
						public void onClick(View v) {
							((Map2Activity) context).routeToNextSpot(lat, lng, nextLat, nextLng);
							((Map2Activity) context).mapHalf();
							imm.hideSoftInputFromWindow(v.getApplicationWindowToken(), 0);
							popUp.dismiss();
						}
					});
				}
			});
		}
		
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
		
		if (pic1 != null) foodFlag.setImageResource(R.drawable.food_icon);
		if (pic2 != null) hotelFlag.setImageResource(R.drawable.hotel_icon);
		if (pic3 != null) shopFlag.setImageResource(R.drawable.shopping_icon);
		if (pic4 != null) sceneFlag.setImageResource(R.drawable.scene_icon);
		if (pic5 != null) transFlag.setImageResource(R.drawable.transport_icon);
		
		showMap.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				((Map2Activity) context).exListMapMove(lat, lng);
				InputMethodManager imm = (InputMethodManager)arg0.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(arg0.getApplicationWindowToken(), 0);
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
