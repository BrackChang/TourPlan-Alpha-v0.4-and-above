package com.brack.mapmobile;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.OverlayItem;

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
		protected boolean onTap(int index) {
			AlertDialog.Builder infoDialog = new AlertDialog.Builder(context);
			infoDialog.setIcon(R.drawable.info_icon);
			infoDialog.setTitle(Items.get(index).getTitle());
			infoDialog.setMessage(Items.get(index).getSnippet());
			infoDialog.setPositiveButton("OK!", 
					new DialogInterface.OnClickListener()
					{
						public void onClick(DialogInterface dialog, int which)
						{
						//Actions after you press OK!	
						}
					});
			infoDialog.show();
			//Toast.makeText(Map2Activity.this, Items.get(index).getSnippet(), Toast.LENGTH_SHORT).show();
			return true;
		}
	}