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
		
		paint.setColor(Color.rgb(65, 105, 225));
		paint.setAlpha(130);
		paint.setStrokeWidth(6.5f);
		paint.setStrokeJoin(Paint.Join.ROUND);
		paint.setStrokeCap(Paint.Cap.ROUND);
		paint.setStyle(Paint.Style.STROKE);
		paint.setDither(true);
		paint.setAntiAlias(true);
		
		Projection projection = mapView.getProjection();
		path.rewind();
		
		Iterator<GeoPoint> iterGeo = geoOverlays.iterator();
		if (iterGeo.hasNext())
		{
			projection.toPixels(iterGeo.next(), point);
			path.moveTo((float)point.x, (float)point.y);
		} else 
			return;
		
		while (iterGeo.hasNext())
		{
			projection.toPixels(iterGeo.next(), point);
			path.lineTo((float)point.x, (float)point.y);
		}
		path.setLastPoint(point.x, point.y);
		
		canvas.drawPath(path, paint);
	}
}
