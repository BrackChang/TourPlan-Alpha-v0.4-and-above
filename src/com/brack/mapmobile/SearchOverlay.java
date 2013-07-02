package com.brack.mapmobile;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.OverlayItem;

public class SearchOverlay extends ItemizedOverlay<OverlayItem> {
		
		private List<OverlayItem> Items = new ArrayList<OverlayItem>();
		private Context context;
		
		public SearchOverlay (Drawable defaultMarker, Context contex) {
			super (boundCenterBottom(defaultMarker));
			this.context = contex;
		}
		
		public void setPoint (GeoPoint points, String title, String snippet)
		{
			Items.add (new OverlayItem(points, title, snippet));
		}
		
		public void finish()
		{
			populate();
		}
		
		@Override
		protected OverlayItem createItem(int i) {
			return Items.get(i);
		}
		
		@Override
		public int size() {
			return Items.size();
		}
		
		@SuppressWarnings("deprecation")
		@Override
		protected boolean onTap(final int index) {
			WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
			int width = wm.getDefaultDisplay().getWidth();
			
			AlertDialog.Builder infoDialog = new AlertDialog.Builder(context);
			
			if (width >= 800)
			{
				TextView title = new TextView(context);
				title.setText(Items.get(index).getTitle());
				title.setTextColor(context.getResources().getColor(R.color.CadetBlue));
				title.setGravity(Gravity.CENTER);
				title.setPadding(0, 10, 0, 10);
				title.setTextSize(30);
				//title.setTypeface(null,Typeface.BOLD);

				infoDialog.setCustomTitle(title);
			} else {
				infoDialog.setTitle(Items.get(index).getTitle());
			}
			
			infoDialog.setIcon(R.drawable.info_icon);
			infoDialog.setMessage(Items.get(index).getSnippet());
			infoDialog.setPositiveButton("OK!", 
					new DialogInterface.OnClickListener()
					{
						public void onClick(DialogInterface dialog, int which)
						{
						//Actions after you press OK!	
						}
					});
			infoDialog.setNegativeButton("Navigate", new DialogInterface.OnClickListener()
			{
				public void onClick(DialogInterface dialog, int which)
				{
					String geoPoint = Items.get(index).getPoint().toString();
					String[] point = geoPoint.split(",");
					double lat = Double.parseDouble(point[0]) / 1E6;
					double lng =Double.parseDouble(point[1]) / 1E6;
					
					((Map2Activity) context).routeToSearch(""+lat, ""+lng);
				}
			});

			AlertDialog dialog = infoDialog.create();
			dialog.show();
			
			if (width >= 800)
			{
				dialog.getWindow().getAttributes();
				
				TextView msgText = (TextView) dialog.findViewById(android.R.id.message);
				Button positive = (Button) dialog.getButton(DialogInterface.BUTTON_POSITIVE);
				Button negative = (Button) dialog.getButton(DialogInterface.BUTTON_NEGATIVE);

				msgText.setTextSize(28);
				positive.setTextSize(28);
				negative.setTextSize(28);
			}
			
			return true;
		}
	}