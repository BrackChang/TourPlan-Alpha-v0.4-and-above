package com.brack.mapmobile;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.OverlayItem;

@SuppressLint("ResourceAsColor")
public class MapOverlay extends ItemizedOverlay<OverlayItem> {
		
		private List<OverlayItem> Items = new ArrayList<OverlayItem>();
		private Context context;
		
		public MapOverlay (Drawable defaultMarker, Context context) {
			super (boundCenterBottom(defaultMarker));
			this.context = context;
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
		
		@Override
		protected boolean onTap(final int index) {
			DisplayMetrics DM = new DisplayMetrics();
			((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(DM);
			double diagonalPixels = Math.sqrt((Math.pow(DM.widthPixels, 2) + Math.pow(DM.heightPixels, 2)));
	        double size = diagonalPixels / (160 * DM.density);

			AlertDialog.Builder infoDialog = new AlertDialog.Builder(context);
			
			TextView title = new TextView(context);
			title.setText(Items.get(index).getTitle());
			title.setTextColor(context.getResources().getColor(R.color.DeepSkyBlue));
			title.setGravity(Gravity.CENTER);
			
			if (size >= 6.5)
			{
				title.setPadding(0, 15, 0, 15);
				title.setTextSize(30);
				//title.setTypeface(null,Typeface.BOLD);
				infoDialog.setCustomTitle(title);
			} else {
				title.setPadding(0, 15, 0, 15);
				title.setTextSize(22);
				infoDialog.setCustomTitle(title);
			}

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
			
			dialog.getWindow().getAttributes();
			
			TextView msgText = (TextView) dialog.findViewById(android.R.id.message);
			Button positive = (Button) dialog.getButton(DialogInterface.BUTTON_POSITIVE);
			Button negative = (Button) dialog.getButton(DialogInterface.BUTTON_NEGATIVE);
			positive.setTextColor(context.getResources().getColor(R.drawable.DarkOrange));
			negative.setTextColor(context.getResources().getColor(R.drawable.Brown));
			
			if (size >= 6.5)
			{
				msgText.setTextSize(28);
				msgText.setPadding(10, 15, 10, 15);
				positive.setTextSize(28);
				negative.setTextSize(28);
			} else {
				msgText.setTextSize(18);
				msgText.setPadding(10, 15, 10, 15);
				positive.setTextSize(18);
				negative.setTextSize(18);
			}

			return true;
		}
	}