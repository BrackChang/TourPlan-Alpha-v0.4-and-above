package com.brack.mapmobile;

import java.util.List;
import java.util.Map;

import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MenuListAdapter extends BaseAdapter {
	
	private Context context;
	List<Map<String, Object>> items;
	private String feature;
	
	public MenuListAdapter (Context context,  List<Map<String, Object>> items, String feature)
	{
		this.context = context;
		this.items = items;
		this.feature = feature;
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
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.menu_list, null);
		
		TextView menuText = (TextView) layout.findViewById(R.id.menuText);
		ImageView menuImage = (ImageView) layout.findViewById(R.id.menuIcon);
		
		String menuItem = (String) items.get(position).get("menuItems");
		int menuIcon = (Integer) items.get(position).get("menuIcons");

		if (feature.equals("position"))
		{
			if (position == 0)
				menuText.setTextColor(context.getResources().getColor(R.drawable.anotherBlue));
		}
		if (feature.equals("save"))
		{
			menuText.setTextColor(context.getResources().getColor(R.drawable.AliceBlue));
		}
		if (feature.equals("general"))
		{
			if (position == 3)
				menuText.setTextColor(context.getResources().getColor(R.drawable.Brown));
		}
		if (feature.equals("traditional"))
		{
			menuText.setTextColor(context.getResources().getColor(R.drawable.Black));
			
			if (position == 0)
				menuText.setTextColor(context.getResources().getColor(R.drawable.DarkOrange));
			if (position == 1)
			{
				menuText.setTextColor(context.getResources().getColor(R.drawable.Gray));
				double size = menuText.getTextSize() * 0.9;
				Log.i("TextSize", ""+size);
				menuText.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) size);
			}
			if (position == 2)
				menuText.setTextColor(context.getResources().getColor(R.drawable.SteelBlue));
			if (position == 3)
				menuText.setTextColor(context.getResources().getColor(R.drawable.SeeGreen));
			if (position == 6)
				menuText.setTextColor(context.getResources().getColor(R.drawable.Brown));
		}
		
		menuText.setText(menuItem);
		menuText.setTypeface(null, Typeface.ITALIC);
		menuImage.setImageResource(menuIcon);
		
		return layout;
	}
}
