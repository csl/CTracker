package com.tracker;

//import java.util.ArrayList;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List; 
import java.util.Locale; 
import java.util.StringTokenizer;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import android.app.AlertDialog;
import android.content.Context; 
import android.content.DialogInterface;
import android.content.Intent; 
//import android.graphics.drawable.Drawable;
import android.location.Address; 
import android.location.Criteria; 
import android.location.Geocoder; 
import android.location.Location; 
import android.location.LocationListener; 
import android.location.LocationManager; 
import android.os.Bundle; 
//import android.util.Log;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View; 
import android.widget.Button; 
import android.widget.EditText; 
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
//import android.widget.Toast;

import com.google.android.maps.GeoPoint; 
//import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapActivity; 
import com.google.android.maps.MapController; 
import com.google.android.maps.MapView; 
//import com.google.android.maps.Overlay;
//import com.google.android.maps.OverlayItem;

public class MyGoogleMap extends MapActivity 
{ 
  //private TextView mTextView01;
  static public MyGoogleMap my;
  private MyGoogleMap mMyGoogleMap = this;
  private String strLocationProvider = ""; 
  
  private LocationManager mLocationManager01; 
  private Location mLocation01; 
  private MapController mMapController01; 
  private MapView mMapView; 
  
  private MyOverLay overlay;
  private List<MapLocation> mapLocations;

  private TextView label;
  
  private int intZoomLevel=0;//geoLatitude,geoLongitude; 
  public GeoPoint nowGeoPoint;
  
  private String IPAddress;
  public String CIPAddress;
  private int showPoint;
  
  public static  MapLocation mSelectedMapLocation;  
  private SocketServer s_socket = null;
  private SendDataSocket sData;

  private int serve_port = 12122;
  private int port = 12122;
  
  private GeoPoint top_left;        
  private GeoPoint top_right;
  private GeoPoint bottom_left;
  private GeoPoint bottom_right;  
  
  private boolean Setting_Ready;

  @Override 
  protected void onCreate(Bundle icicle) 
  { 
    // TODO Auto-generated method stub 
    super.onCreate(icicle); 
    setContentView(R.layout.main2); 
    
    my = this;
    mMapView = (MapView)findViewById(R.id.myMapView1); 
    mMapController01 = mMapView.getController(); 
     
    mMapView.setSatellite(false);
    mMapView.setStreetView(true);
    mMapView.setEnabled(true);
    mMapView.setClickable(true);
     
    intZoomLevel = 15; 
    mMapController01.setZoom(intZoomLevel); 
     
    //mLocationManager01 =  
    //(LocationManager)getSystemService(Context.LOCATION_SERVICE); 
     
    getLocationProvider(); 
     
    nowGeoPoint = getGeoByLocation(mLocation01); 
    
    if (nowGeoPoint == null)
    {
      openOptionsDialog("no GPS rec");    
    }
    
    refreshMapViewByGeoPoint(nowGeoPoint, 
                       mMapView, intZoomLevel); 
     
    mLocationManager01.requestLocationUpdates 
    (strLocationProvider, 2000, 10, mLocationListener01); 
     
    getMapLocations(true);

    Setting_Ready = false;
    
    overlay = new MyOverLay(this);
    mMapView.getOverlays().add(overlay);
    //mMapController01.setCenter(getMapLocations(true).get(0).getPoint());

    label = (TextView)findViewById(R.id.label);

    IPAddress = getLocalIpAddress();
    label.setText("Location IP: " + IPAddress);
    
   /* GeoPoint gp = new GeoPoint((int)geoLatitude,(int)geoLongitude);
    Drawable dr = getResources().getDrawable
    (
      android.R.drawable.arrow 
     );
    dr.setBounds(-15,-15,15, 15);
    
    MyItemOverlay mOverlay01 = new MyItemOverlay(dr,gp);
    List<Overlay> overlays = mMapView.getOverlays();
    overlays.add(mOverlay01);*/
    
    //Open Server Socket
    try {
        s_socket = new SocketServer(serve_port, this);
        Thread socket_thread = new Thread(s_socket);
        socket_thread.start();
    } 
    catch (IOException e) {
        e.printStackTrace();
    }
    catch (Exception e) {
        e.printStackTrace();
    }
  }
  
  public List<MapLocation> getMapLocations(boolean doit) 
  {
    if (mapLocations == null || doit == true) 
    {
      mapLocations = new ArrayList<MapLocation>();
    }
    return mapLocations;
  }

 
  public final LocationListener mLocationListener01 =  
  new LocationListener() 
  { 
    public void onLocationChanged(Location location) 
    { 
      // TODO Auto-generated method stub 
       
      mLocation01 = location; 
      nowGeoPoint = getGeoByLocation(location); 
      refreshMapViewByGeoPoint(nowGeoPoint, 
            mMapView, intZoomLevel);
      
      if (Setting_Ready)
      {
        //sendCurrentGPSData
        double Longitude = nowGeoPoint.getLongitudeE6()/ 1E6;
        double Latitude = nowGeoPoint.getLatitudeE6()/ 1E6;
        
        SendGPSData(Latitude + "," + Longitude);
      }
      
    } 

    public void onProviderDisabled(String provider) 
    { 
      // TODO Auto-generated method stub 
      mLocation01 = null; 
    } 
     
    public void onProviderEnabled(String provider) 
    { 
      // TODO Auto-generated method stub 
       
    } 
     
    public void onStatusChanged(String provider, 
                int status, Bundle extras) 
    { 
      // TODO Auto-generated method stub 
       
    } 
  }; 
   
  private GeoPoint getGeoByLocation(Location location) 
  { 
    GeoPoint gp = null; 
    try 
    { 
      if (location != null) 
      { 
        double geoLatitude = location.getLatitude()*1E6; 
        double geoLongitude = location.getLongitude()*1E6; 
        gp = new GeoPoint((int) geoLatitude, (int) geoLongitude); 
      } 
    } 
    catch(Exception e) 
    { 
      e.printStackTrace(); 
    } 
    return gp; 
  } 
   
  private GeoPoint getGeoByAddress(String strSearchAddress) 
  { 
    GeoPoint gp = null; 
    try 
    { 
      if(strSearchAddress!="") 
      { 
        Geocoder mGeocoder01 = new Geocoder 
        (MyGoogleMap.this, Locale.getDefault()); 
         
        List<Address> lstAddress = mGeocoder01.getFromLocationName
                           (strSearchAddress, 10);
        if (!lstAddress.isEmpty()) 
        { 
          /*for (int i = 0; i < lstAddress.size(); ++i)
          {
            Address adsLocation = lstAddress.get(i);
            //Log.i(TAG, "Address found = " + adsLocation.toString()); 
            double geoLatitude = adsLocation.getLatitude();
            double geoLongitude = adsLocation.getLongitude();
          } */
          Address adsLocation = lstAddress.get(0); 
          double geoLatitude = adsLocation.getLatitude()*1E6; 
          double geoLongitude = adsLocation.getLongitude()*1E6; 
          gp = new GeoPoint((int) geoLatitude, (int) geoLongitude); 
        }
        
      } 
    } 
    catch (Exception e) 
    {  
      e.printStackTrace();  
    } 
    return gp; 
  } 
   
  public static void refreshMapViewByGeoPoint 
  (GeoPoint gp, MapView mapview, int zoomLevel) 
  { 
    try 
    { 
      mapview.displayZoomControls(true); 
      MapController myMC = mapview.getController(); 
      myMC.animateTo(gp); 
      myMC.setZoom(zoomLevel); 
      //mapview.setSatellite(false);
      
    } 
    catch(Exception e) 
    { 
      e.printStackTrace(); 
    } 
  } 
   
  public static void refreshMapViewByCode 
  (double latitude, double longitude, 
      MapView mapview, int zoomLevel) 
  { 
    try 
    { 
      GeoPoint p = new GeoPoint((int) latitude, (int) longitude); 
      mapview.displayZoomControls(true); 
      MapController myMC = mapview.getController(); 
      myMC.animateTo(p); 
      myMC.setZoom(zoomLevel); 
      mapview.setSatellite(false); 
    } 
    catch(Exception e) 
    { 
      e.printStackTrace(); 
    } 
  } 
   
  private String GeoPointToString(GeoPoint gp) 
  { 
    String strReturn=""; 
    try 
    { 
      if (gp != null) 
      { 
        double geoLatitude = (int)gp.getLatitudeE6()/1E6; 
        double geoLongitude = (int)gp.getLongitudeE6()/1E6; 
        strReturn = String.valueOf(geoLatitude)+","+
          String.valueOf(geoLongitude); 
      } 
    } 
    catch(Exception e) 
    { 
      e.printStackTrace(); 
    } 
    return strReturn; 
  }

  public String getIEMI()
  {
    return  ((TelephonyManager) getSystemService(TELEPHONY_SERVICE)).getDeviceId();
  }

  public void SendGPSData(String GPSData)
  {
    int port = 12122;

    sData = new SendDataSocket(this);
    sData.SetAddressPort(CIPAddress , port);
    sData.SetSendData(GPSData);
    sData.SetFunction(1); 
    sData.start();
  }  
  
  public void getLocationProvider() 
  { 
    try 
    { 
      Criteria mCriteria01 = new Criteria(); 
      mCriteria01.setAccuracy(Criteria.ACCURACY_FINE); 
      mCriteria01.setAltitudeRequired(false); 
      mCriteria01.setBearingRequired(false); 
      mCriteria01.setCostAllowed(true); 
      mCriteria01.setPowerRequirement(Criteria.POWER_LOW); 
      strLocationProvider =  
      mLocationManager01.getBestProvider(mCriteria01, true); 
       
      mLocation01 = mLocationManager01.getLastKnownLocation (strLocationProvider); //?
    } 
    catch(Exception e) 
    { 
      //mTextView01.setText(e.toString()); 
      e.printStackTrace(); 
    } 
  }
  
 /* private class MyItemOverlay extends ItemizedOverlay<OverlayItem>
  {
    private List<OverlayItem> items = new ArrayList<OverlayItem>();
    public MyItemOverlay(Drawable defaultMarker , GeoPoint gp)
    {
      super(defaultMarker);
      items.add(new OverlayItem(gp,"Title","Snippet"));
      populate();
    }
    
    @Override
    protected OverlayItem createItem(int i)
    {
      return items.get(i);
    }
    
    @Override
    public int size()
    {
      return items.size();
    }
    
    @Override
    protected boolean onTap(int pIndex)
    {
      Toast.makeText
      (
        Flora_Expo.this,items.get(pIndex).getSnippet(),
        Toast.LENGTH_LONG
      ).show();
      return true;
    }
  }*/
   
  @Override 
  protected boolean isRouteDisplayed() 
  { 
    // TODO Auto-generated method stub 
    return false; 
  } 
  
  public int refreshSettingGPSMap(String str)
  {
    //SetPoint
    StringTokenizer Tok = new StringTokenizer(str, ",");
    double GPSData[] = new double[8];
    int i=0;
    int r=0;
    while (Tok.hasMoreElements())
    {
      if (i==0)
      {
        CIPAddress = (String) Tok.nextElement();
      }
      else
      {
        GPSData[r] = Double.valueOf((String) Tok.nextElement());
        r++;
      }
      i++;
    }      
    
    top_left = new GeoPoint((int)(GPSData[0] * 1e6),
        (int)(GPSData[1] * 1e6));
    top_right = new GeoPoint((int)(GPSData[2] * 1e6),
        (int)(GPSData[3] * 1e6));
    bottom_left = new GeoPoint((int)(GPSData[4] * 1e6),
        (int)(GPSData[5] * 1e6));
    bottom_right = new GeoPoint((int)(GPSData[6] * 1e6),
        (int)(GPSData[7] * 1e6));
    
    overlay.SetPoint(top_left, top_right, bottom_left, bottom_right);
    Setting_Ready = true;
    
    return 1;
  }
  
  public String getLocalIpAddress() {
    try {
      for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); )
      {
          NetworkInterface intf = en.nextElement();
            for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) 
            {
                InetAddress inetAddress = enumIpAddr.nextElement();
                if (!inetAddress.isLoopbackAddress()) {
                    return inetAddress.getHostAddress().toString();
                }
            }
      }
    }
    catch (SocketException ex) {
        Log.e("", ex.toString());
    }

    return null;
}  
  
  //show message
  public void openOptionsDialog(String info)
  {
    new AlertDialog.Builder(this)
    .setTitle("message")
    .setMessage(info)
    .setPositiveButton("OK",
        new DialogInterface.OnClickListener()
        {
         public void onClick(DialogInterface dialoginterface, int i)
         {
           finish();
         }
         }
        )
    .show();
  }
}
