package com.brack.mapmobile;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.Projection;

public class DrawOverlay extends Overlay 
{
	private List<GeoPoint> geoOverlays = new ArrayList<GeoPoint>();
	private static final int Alpha = 120;
	private static final float Stroke = 5.5f;
	private final Path path;
	private final Point point;
	private final Paint paint;
	
	public DrawOverlay (List<GeoPoint> geoOverlays)
	{
		this.geoOverlays = geoOverlays;
		path = new Path();
		point = new Point();
		paint = new Paint();
	}
	
	@Override
	public void draw (Canvas canvas, MapView mapView, boolean shadow)
	{
		super.draw(canvas, mapView, shadow);
		
		paint.setColor(Color.argb(120, 65, 105, 225));
		paint.setAlpha(Alpha);
		paint.setStrokeWidth(Stroke);
		paint.setStyle(Paint.Style.STROKE);
		
		final Projection proj = mapView.getProjection();
		path.rewind();
		
		final Iterator<GeoPoint> iterGeo = geoOverlays.iterator();
		proj.toPixels(iterGeo.next(), point);
		path.moveTo(point.x, point.y);
		
		while (iterGeo.hasNext())
		{
			proj.toPixels(iterGeo.next(), point);
			path.lineTo(point.x, point.y);
		}
		path.setLastPoint(point.x, point.y);
		
		canvas.drawPath(path, paint);
	}
}
