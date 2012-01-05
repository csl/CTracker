package com.tracker;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.Paint.Style;
import android.os.Environment;
import android.util.Log;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;


public class MyOverLay  extends Overlay 
{

    private Bitmap mNowIcon;
    
    private ChildTracker mLocationViewers;
    
    private List<GeoPoint> gp;
	
    private boolean ReadyShowRange;
    
    private String TAG = "MyOverLay";
    
	/**
	 * It is used to track the visibility of information window and clicked location is known location or not 
	 * of the currently selected Map Location
	 */
    
	public MyOverLay(ChildTracker mLocationViewers) {
		
		this.mLocationViewers = mLocationViewers;
		mNowIcon = BitmapFactory.decodeResource(mLocationViewers.getResources(),R.drawable.mappin_blue);
		
		gp = new ArrayList<GeoPoint>();
		ReadyShowRange = false;
	}
	
	@Override
	public boolean onTap(GeoPoint p, MapView mapView)  {
		
		/**
		 * Track the popup display
		 */
		/**
		 *   Return true if we handled this onTap()
		 */
		return true;
	}
	
    @Override
	public void draw(Canvas canvas, MapView	mapView, boolean shadow) 
  {
      drawNowGeoMap(canvas, mapView, shadow);
   		drawPointRange(canvas, mapView, shadow);
  }
    
  public void clearRange()
  {
    ReadyShowRange = false;
    gp.clear();
  }

    
    private void drawPointRange(Canvas canvas, MapView mapView, boolean shadow) 
    {
      if (ReadyShowRange == true)
      {
        Paint paint = new Paint();
        Point myScreenCoords1 = new Point();
        Point myScreenCoords2 = new Point();
        Point myScreenCoords3 = new Point();
        Point myScreenCoords4 = new Point();
        
        //calculate
        GeoPoint top_left = gp.get(0);        
        GeoPoint top_right =  gp.get(1);
        GeoPoint bottom_left =  gp.get(2);
        GeoPoint buttom_right = gp.get(3);        
        
        mapView.getProjection().toPixels(top_left, myScreenCoords1);
        mapView.getProjection().toPixels(top_right, myScreenCoords2);
        mapView.getProjection().toPixels(bottom_left, myScreenCoords3);
        mapView.getProjection().toPixels(buttom_right, myScreenCoords4);
        paint.setStrokeWidth(2);
        paint.setARGB(255, 255, 0, 0);

        canvas.drawLine((float) myScreenCoords1.x, (float) myScreenCoords1.y, (float) myScreenCoords2.x,
            (float) myScreenCoords2.y, paint);
        canvas.drawLine((float) myScreenCoords1.x, (float) myScreenCoords1.y, (float) myScreenCoords3.x,
            (float) myScreenCoords3.y, paint);
        canvas.drawLine((float) myScreenCoords2.x, (float) myScreenCoords2.y, (float) myScreenCoords4.x,
            (float) myScreenCoords4.y, paint);
        canvas.drawLine((float) myScreenCoords3.x, (float) myScreenCoords3.y, (float) myScreenCoords4.x,
            (float) myScreenCoords4.y, paint);
  
        Log.i(TAG, "ReadyShowRange");

      }
    }
    
    
    private void drawNowGeoMap(Canvas canvas, MapView mapView, boolean shadow) 
    {
      if (mLocationViewers.nowGeoPoint != null)
      {
        Paint paint = new Paint();
        Point myScreenCoords = new Point();
  
        mapView.getProjection().toPixels(mLocationViewers.nowGeoPoint, myScreenCoords);
        paint.setStrokeWidth(1);
        paint.setARGB(255, 255, 0, 0);
        paint.setStyle(Paint.Style.STROKE);
  
        canvas.drawBitmap(mNowIcon, myScreenCoords.x, myScreenCoords.y, paint);
        canvas.drawText("²{¦b¦ì¸m", myScreenCoords.x, myScreenCoords.y, paint);
      }
    }
    
	
  public void SetPoint(GeoPoint G1, GeoPoint G2, GeoPoint G3, GeoPoint G4)
  {
      gp.clear();
      
      gp.add(G1);
      gp.add(G2);
      gp.add(G3);
      gp.add(G4);
      
      ReadyShowRange = true;
  }	
  
  public int getGPSRangeSize()
  {
     return gp.size();
  }

}