package com.brack.mapmobile;

import java.util.List;
import java.util.Map;

import android.content.Context;
import android.graphics.Typeface;
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
	
	public MenuListAdapter (Context context,  List<Map<String, Object>> items)
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
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.menu_list, null);
		
		TextView menuText = (TextView) layout.findViewById(R.id.menuItem);
		ImageView menuImage = (ImageView) layout.findViewById(R.id.menuIcon);
		
		String menuItem = (String) items.get(position).get("menuItems");
		int menuIcon = (Integer) items.get(position).get("menuIcons");
		
		if (position == 7)
			menuText.setTextColor(context.getResources().getColor(R.drawable.Brown));
		if (position == 1)
			menuText.setTextColor(context.getResources().getColor(R.drawable.DarkOrange));
		if (position == 4)
			menuText.setTextColor(context.getResources().getColor(R.color.DeepSkyBlue));
		if (position == 3)
			menuText.setTextColor(context.getResources().getColor(R.drawable.SeeGreen));
		
		menuText.setText(menuItem);
		menuText.setTypeface(null, Typeface.ITALIC);
		menuImage.setImageResource(menuIcon);
		
		return layout;
	}
}
