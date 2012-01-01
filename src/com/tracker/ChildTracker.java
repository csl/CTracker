package com.tracker;

//import java.util.ArrayList;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.List; 
import java.util.Locale; 
import java.util.StringTokenizer;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

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
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
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
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle; 
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
//import android.util.Log;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
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

public class ChildTracker extends MapActivity 
{ 
   
  private static final int MSG_DIALOG_SAFE = 1;  
  private static final int MSG_DIALOG_OVERRANGE = 2;  
  private static final int MSG_CLOSE_PROGRESS = 3;
  private static final int MSG_TIMEOUT_CLOSE_PROGRESS = 4;
  private static final int MSG_DIALOG_SETS = 5;
  
  static public ChildTracker my;
  private Timer timer;
  private Timer auto_timer;
  
  private String TAG = "ChildTracker";

  private ChildTracker mMyGoogleMap = this;
  private String strLocationProvider = ""; 
  private LocationManager mLocationManager01; 
  private Location mLocation01; 
  private MapController mMapController01; 
  private MapView mMapView; 
  
  private MyOverLay overlay;
  
  private TextView label;
  public ProgressDialog pd;
  private int intZoomLevel=0;//geoLatitude,geoLongitude; 
  public GeoPoint nowGeoPoint;
  
  private String IPAddress;

  private int mchildid;
  
  public GeoPoint top_left;        
  public GeoPoint top_right;
  public GeoPoint bottom_left;
  public GeoPoint bottom_right;  
  
  public boolean Setting_Ready;
  
  public String oldGPSRangeData;

  private static final int MENU_ZOOMIN = Menu.FIRST;
  private static final int MENU_ZOOMOUT = Menu.FIRST +1;
  private static final int MENU_EXIT = Menu.FIRST +2;
  private static final int MENU_AT = Menu.FIRST +3;
  
  static public ArrayList<ChildStruct> childlist;
  private static volatile AtomicBoolean processing = new AtomicBoolean(false);
  private static volatile AtomicBoolean auto_processing = new AtomicBoolean(false);
  public CSXMLStruct csdata;
  
  
  @Override 
  protected void onCreate(Bundle icicle) 
  { 
    // TODO Auto-generated method stub 
    super.onCreate(icicle); 
    setContentView(R.layout.main2); 
    
    oldGPSRangeData = "";
    childlist = null;
    
    //Checking Status
    if (CheckInternet(3))
    {
        String provider = Settings.Secure.getString(getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
        if(provider == null)
        {
         openOptionsDialog("NO GPS");
        }
    }
    else
    {
      openOptionsDialog("NO Internet");
    } 
    
    my = this;

    timer = new Timer();

    mMapView = (MapView)findViewById(R.id.myMapView1); 
    mMapController01 = mMapView.getController(); 
     
    mMapView.setSatellite(false);
    mMapView.setStreetView(true);
    mMapView.setEnabled(true);
    mMapView.setClickable(true);
     
    nowGeoPoint = new GeoPoint((int) (24.070801 * 1000000),(int) (120.715486 * 1000000));
    
    intZoomLevel = 18; 
    mMapController01.setZoom(intZoomLevel); 
     
    mLocationManager01 = (LocationManager) getSystemService(Context.LOCATION_SERVICE); 
     
    getLocationProvider();     
    nowGeoPoint = getGeoByLocation(mLocation01); 
    
    if (nowGeoPoint != null)
    {
      refreshMapViewByGeoPoint(nowGeoPoint, 
          mMapView, intZoomLevel); 
    }
    mLocationManager01.requestLocationUpdates(strLocationProvider, 2000, 10, mLocationListener01); 
     
    Setting_Ready = false;
    
    overlay = new MyOverLay(this);
    mMapView.getOverlays().add(overlay);
    //mMapController01.setCenter(getMapLocations(true).get(0).getPoint());

    label = (TextView)findViewById(R.id.label);

    //get MTServer
    IPAddress = (String) this.getResources().getText(R.string.url);
    
    if (getChildList() != null)
    {
         //error
         if (childlist == null)
         {
           return;
         }          
         
         final CharSequence[] child_id = new String[childlist.size()];
         int checked = 0;
         
         for(int i = 0 ;i<childlist.size(); i++)
         {
           child_id[i] = childlist.get(i).name; 
         }
      
    
        AlertDialog.Builder builder = new AlertDialog.Builder(mMyGoogleMap);
        //openOptionsDialog(getLocalIpAddress());
        builder.setTitle("選擇您是那一個child");
        builder.setMessage("選出您的child");
        
        builder.setSingleChoiceItems(child_id, checked, new DialogInterface.OnClickListener() { 
          public void onClick(DialogInterface dialog, int which) 
          {
            mchildid = which;
          } 
       }); 
        
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() { 
          public void onClick(DialogInterface dialog, int which) 
          {
            timer.schedule(new DateTask(), 0, 5000);                        
          } 
       }); 
         
       AlertDialog alert = builder.create();  
       alert.show();
    }
  

  }

  public boolean onCreateOptionsMenu(Menu menu)
  {
    super.onCreateOptionsMenu(menu);
    
    menu.add(0 , MENU_ZOOMIN, 1 ,"放大")
    .setAlphabeticShortcut('E');
    menu.add(1 , MENU_ZOOMOUT, 1 ,"縮小")
    .setAlphabeticShortcut('E');
    menu.add(2 , MENU_EXIT, 1 ,R.string.menu_exit).setIcon(R.drawable.exit)
    .setAlphabeticShortcut('E');
    menu.add(2 , MENU_AT, 1 ,"自動測試程式").setIcon(R.drawable.exit)
    .setAlphabeticShortcut('E');   
  return true;  
  }
  
  @Override
  public boolean onOptionsItemSelected(MenuItem item)
  {
    switch (item.getItemId())
      { 
          case MENU_ZOOMIN:
              intZoomLevel++; 
              if(intZoomLevel>mMapView.getMaxZoomLevel()) 
              { 
                intZoomLevel = mMapView.getMaxZoomLevel(); 
              } 
              mMapController01.setZoom(intZoomLevel); 
              break;
          
          case MENU_ZOOMOUT:
              intZoomLevel--; 
              if(intZoomLevel<1) 
              { 
                intZoomLevel = 1; 
              } 
              mMapController01.setZoom(intZoomLevel); 
              break;
          case MENU_EXIT:
           
            timer.cancel();
            mLocationManager01.removeUpdates(mLocationListener01);            
            android.os.Process.killProcess(android.os.Process.myPid());

            openExitDialog();
    
             break ;
          case MENU_AT:
            
            if (auto_timer == null)
            {
              auto_timer = new Timer();
              auto_timer.schedule(new Auto_DateTask(), 2000, 2000);
            }
            else
            {
              auto_timer.cancel();
              auto_timer = null;
            }
            break;
      }
    
      return true ;
  }
  
  public boolean onKeyDown(int keyCode, KeyEvent event) 
  {
      MapController mc = mMapView.getController(); 
      switch (keyCode) 
      {
          case KeyEvent.KEYCODE_3:
              mc.zoomIn();
              break;
          case KeyEvent.KEYCODE_1:
              mc.zoomOut();
              break;
      }
      return super.onKeyDown(keyCode, event);
  }    
  
  
  
  public void setP(GeoPoint nw)
  {
    refreshMapViewByGeoPoint(nw, 
        mMapView, intZoomLevel);
  }

 
  public final LocationListener mLocationListener01 =  new LocationListener() 
  { 
    public void onLocationChanged(Location location) 
    { 
      // TODO Auto-generated method stub 
      mLocation01 = location; 
      nowGeoPoint = getGeoByLocation(location); 

      if (nowGeoPoint != null)
      {
        refreshMapViewByGeoPoint(nowGeoPoint, 
            mMapView, intZoomLevel); 

        if (Setting_Ready)
        {
          label.setText("Location IP: " + IPAddress + ", connection");

          //sendCurrentGPSData
          double Latitude = nowGeoPoint.getLatitudeE6()/ 1E6;
          double Longitude = nowGeoPoint.getLongitudeE6()/ 1E6;
   
          //over range
          if (CheckProximityAlert(Latitude, Longitude) == 0)
           {
            SendGPSData(Latitude + "," + Longitude + "," + "1");
            label.setText("warning: over range, please reback range!!");
           }
         else
          {
            SendGPSData(Latitude + "," + Longitude);
          }
          
        }
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
   

  public void SendGPSData(String GPSData)
  {
    String url_list = IPAddress + "updategps.php?gps=" + GPSData + "&ChildID=" + mchildid;
    toweb(url_list);
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
      strLocationProvider = mLocationManager01.getBestProvider(mCriteria01, true); 
       
      mLocation01 = mLocationManager01.getLastKnownLocation (strLocationProvider); //?
    } 
    catch(Exception e) 
    { 
      //mTextView01.setText(e.toString()); 
      e.printStackTrace(); 
    } 
  }
  
 
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
    Log.v("TAG", str);
    while (Tok.hasMoreElements())
    {
       GPSData[i] = Double.valueOf((String) Tok.nextElement());
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
    
    Message msg = new Message();
    msg.what = MSG_CLOSE_PROGRESS;
    myHandler.sendMessage(msg);       
    return 1;
  }
  
  public int timeouthandler()
  {
    Message msg = new Message();
    msg.what = MSG_TIMEOUT_CLOSE_PROGRESS;
    myHandler.sendMessage(msg);
    return 1;
  }

  
  public int CheckProximityAlert(double nowlat, double nowlon)
  {
    double Tlplat = top_left.getLatitudeE6()/ 1E6;
    double Trplat = top_right.getLatitudeE6()/ 1E6;
    
    double Trplon = top_right.getLongitudeE6()/ 1E6;
    double Brplon = bottom_right.getLongitudeE6()/ 1E6;
    
    label.setText("ok, " + nowlat + "," + Tlplat + "," + Trplat + "," + nowlon + "," + Trplon + "," + Brplon);
    if (nowlat >= Trplat && nowlat <= Tlplat)
    {
      if (nowlon >= Trplon && nowlon <= Brplon)
      {
          return 1;
      }
    }  
    
    return 0;
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
  
  private boolean CheckInternet(int retry)
  {
    boolean has = false;
    for (int i=0; i<=retry; i++)
    {
      has = HaveInternet();
      if (has == true) break;       
    }
    
  return has;
  }  
  
  void setStatus(int status)
  {    
    if (status == 1)
    {
      overlay.clearRange();      
    }
    
    Message msg = new Message();
    msg.what = MSG_DIALOG_SETS;
    myHandler.sendMessage(msg);    
    
  }

  public class DateTask extends TimerTask {
    public void run() 
    {
      if (!processing.compareAndSet(false, true)) return;
      
      int shour, sminute;
      final Calendar c = Calendar.getInstance();
      shour = c.get(Calendar.HOUR_OF_DAY);
      sminute = c.get(Calendar.MINUTE);      
      
      String uriAPI = IPAddress + "getchildstatus.php?nowtime=" + shour + ":" + sminute + "&childid=" + mchildid;
      
      URL url = null;
      try{
        url = new URL(uriAPI);
        
        SAXParserFactory spf = SAXParserFactory.newInstance();
        SAXParser sp = spf.newSAXParser();
        XMLReader xr = sp.getXMLReader();
        //Using login handler for xml
        CSXMLHandler myHandler = new CSXMLHandler();
        xr.setContentHandler(myHandler);
        //open connection
        xr.parse(new InputSource(url.openStream()));
        //verify OK
        csdata = myHandler.getParsedData();
      }
      catch(Exception e){
        e.printStackTrace();
        return;
      }
      
      //handle gps message

      if (!csdata.h_name.equals("nodata"))
      {
        String cname, cgps, cstime, cdtime;
        
        cname = csdata.h_name;
        cgps = csdata.h_rangegps;
        cstime = csdata.h_stime;
        cdtime = csdata.h_dtime;

        if (oldGPSRangeData.equals(""))
        {
          Log.i(TAG, "get: " +cgps);
          oldGPSRangeData = cgps;
          GPSRhander(cgps);

        }
        else if (!oldGPSRangeData.equals(cgps))
        {
          Log.i(TAG, "get: " + cgps);
          oldGPSRangeData = "";
          GPSRhander(cgps);
          //setStatus();
        }
        //else
          //setStatus();
      }

      processing.set(false);           
    }
  }
  
  public class Auto_DateTask extends TimerTask {
    public void run() 
    {
      if (!auto_processing.compareAndSet(false, true)) return;
      
      //get now gps data
      double nowlat = nowGeoPoint.getLatitudeE6()/ 1E6 + 0.1;
      double nowlon = nowGeoPoint.getLongitudeE6()/ 1E6 + 0.1;
      
      nowGeoPoint = new GeoPoint((int)(nowlat * 1e6), (int)(nowlon * 1e6));
      refreshMapViewByGeoPoint(nowGeoPoint, mMapView, intZoomLevel); 
      auto_processing.set(false);           
    }
  }
  
  void GPSRhander(String gpsdata)
  {
    if ( gpsdata != null )
    {
      StringTokenizer Tok = new StringTokenizer(gpsdata, ",");
      double GPSData[] = new double[8];
      int i=0;
      while (Tok.hasMoreElements())
      {
        GPSData[i] = Double.valueOf((String) Tok.nextElement());
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
      
      overlay.SetPoint(top_left, bottom_right, top_right, bottom_left);
      Log.i(TAG, "GPSData set OK");
    }
    else
    {
      overlay.clearRange();
    } 
  }

  
  private boolean HaveInternet()
  {
     boolean result = false;
     
     ConnectivityManager connManager = (ConnectivityManager) 
                                getSystemService(Context.CONNECTIVITY_SERVICE); 
      
     NetworkInfo info = connManager.getActiveNetworkInfo();
     
     if (info == null || !info.isConnected())
     {
       result = false;
     }
     else 
     {
       if (!info.isAvailable())
       {
         result =false;
       }
       else
       {
         result = true;
       }
   }
  
   return result;
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
  
  //處理HANDER: refreshDouble2Geo會傳送Message出來，決定要顯示什麼
  public Handler myHandler = new Handler(){
    public void handleMessage(Message msg) {
        switch(msg.what)
        {
          case MSG_DIALOG_SAFE:
            if (overlay.getGPSRangeSize() != 0)
              label.setText("安全/設置範圍");
            else
              label.setText("安全/未設置範圍");
            
            label.setText("安全");
            break;
          case MSG_DIALOG_OVERRANGE:
                label.setText("超出");
                break;
          case MSG_CLOSE_PROGRESS:
                //pd.dismiss();
                break;
          case MSG_TIMEOUT_CLOSE_PROGRESS:
              //pd.dismiss();
              label.setText("Connection Timeout");
              break;
          case MSG_DIALOG_SETS:
            if (overlay.getGPSRangeSize() != 0)
              label.setText("安全/設置範圍");
            else
              label.setText("安全/未設置範圍");
            break;               
          default:
                label.setText(Integer.toString(msg.what));
        }
        super.handleMessage(msg);
    }
};  

public ArrayList<ChildStruct> getChildList()
{
  String uriAPI = IPAddress + "getchildlist.php";
  
  URL url = null;
  try{
    url = new URL(uriAPI);
    
    SAXParserFactory spf = SAXParserFactory.newInstance();
    SAXParser sp = spf.newSAXParser();
    XMLReader xr = sp.getXMLReader();
    //Using login handler for xml
    ChildListHandler myHandler = new ChildListHandler();
    xr.setContentHandler(myHandler);
    //open connection
    xr.parse(new InputSource(url.openStream()));
    //verify OK
    childlist = myHandler.getContainer().getListItems();
  }
  catch(Exception e){
    e.printStackTrace();
    return null;
  }
  
  return childlist;
}

  
  private void openExitDialog() {
    
    new AlertDialog.Builder(this)
      .setTitle(R.string.msg_exit)
      .setMessage(R.string.str_exit_msg)
      .setNegativeButton(R.string.str_exit_no,
          new DialogInterface.OnClickListener() {
          
            public void onClick(DialogInterface dialoginterface, int i) {
              
            }
      }
      )
   
      .setPositiveButton(R.string.str_exit_ok,
          new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialoginterface, int i) {
            
            finish();
          }
          
      }
      )
      
      .show();
  }  
  
  public int toweb(String uriAPI)
  {
    int error = 0;
       HttpGet httpRequest = new HttpGet(uriAPI);
         
       try
        {
            HttpResponse httpResponse = new DefaultHttpClient().execute(httpRequest);
            if(httpResponse.getStatusLine().getStatusCode() == 200)
            {
              String strResult = EntityUtils.toString(httpResponse.getEntity());
            }
            else
            {
              //mTextView1.setText("Error Response: "+httpResponse.getStatusLine().toString());
            }
          }
          catch (ClientProtocolException e)
          {
            //mTextView1.setText(e.getMessage().toString());
            e.printStackTrace();
            error = 1;
          }
          catch (IOException e)
          {
            //mTextView1.setText(e.getMessage().toString());
            e.printStackTrace();
            error = 1;
          }
          catch (Exception e)
          {
            //mTextView1.setText(e.getMessage().toString());
            e.printStackTrace();
            error = 1;
          }

         return error;
  }    
  
  //show message
  public void openDialog(String info)
  {
    new AlertDialog.Builder(this)
    .setTitle("message")
    .setMessage(info)
    .setPositiveButton("OK",
        new DialogInterface.OnClickListener()
        {
         public void onClick(DialogInterface dialoginterface, int i)
         {
         }
         }
        )
    .show();
  }
}
