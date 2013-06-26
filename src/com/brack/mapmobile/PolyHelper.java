package com.brack.mapmobile;

import java.util.ArrayList;
import java.util.List;

import com.google.android.maps.GeoPoint;

public class PolyHelper {

	public static List<GeoPoint> decodePolyline(String poly) {
        int len = poly.length();
        int index = 0;
        int lat = 0;
        int lng = 0;
        List<GeoPoint> decoded = new ArrayList<GeoPoint>();

        while (index < len) {
            int b;
            int shift = 0;
            int result = 0;
            do {
                b = poly.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = poly.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;
            
            decoded.add(new GeoPoint(
                    Convert.asMicroDegrees(lat / 1E5), Convert.asMicroDegrees(lng / 1E5)));
        }
        return decoded;
    }
	
}
