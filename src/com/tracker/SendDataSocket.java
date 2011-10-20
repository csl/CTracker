package com.tracker;

import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Calendar;
import java.util.StringTokenizer;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;

import android.util.Log;
 
public class SendDataSocket extends Thread 
{
  private String TAG = "SendDataSocket";
	private String address;
	private int port;
	private int function;
	private String timestamp;
	private int PicCount;
	private int IsOK;
	private MyGoogleMap GoogleMap;
	public String error_string;
  public String send_Data;
	String line;
	
	public SendDataSocket(MyGoogleMap map) 
  {
		IsOK = 0;
		GoogleMap = map;
  }
	
	public void SetAddressPort(String addr, int p)
	{		
		this.address = addr;
		this.port = p;
	}
	
  public void SetSendData(String sdata)
  {   
    this.send_Data = sdata;
  }	
  
	public String getTimeStamp()
	{
		return timestamp;		
	}
	
	public void SetFunction(int func)
	{
		function = func;		
	}

	public void SetCount(int count)
	{
		PicCount = count;
		
	}
	public int getIsOK()
	{
		return IsOK;
	}
	
	@Override
	public void run() 
	{
	 int timeout = 0;

    do{
        Socket client = new Socket();
        InetSocketAddress isa = new InetSocketAddress(address, port);
        try {
            client.connect(isa, 10006);
            
            DataOutputStream out = new DataOutputStream(client.getOutputStream());

            if (function  == 1)
            {
              out.writeUTF("SetNowStatus");
           	  out.writeUTF(send_Data);
           	  
           	  /*
              final Calendar c = Calendar.getInstance();
              int shour = c.get(Calendar.HOUR_OF_DAY);
              int sminute = c.get(Calendar.MINUTE);
              
              //¶Ç°e¦r¦ê®y¼Ð
              out.writeUTF(shour + ":" + sminute);
           	  
            	 // As long as we receive data, server will data back to the client.
            	  
            	*/
              DataInputStream is = new DataInputStream(client.getInputStream());
              line = is.readUTF();              
              while (line.equals("OK")) 
              {
                /*
                if (line.equals("nowGPSRange"))
                {
                  String cname, cgps, cstime, cdtime;
                  
                  cname = is.readUTF();
                  cgps = is.readUTF();
                  cstime = is.readUTF();
                  cdtime = is.readUTF();
  
                  if (GoogleMap.oldGPSRangeData.equals(""))
                  {
                    
                    Log.i(TAG, "get: " +cgps);
                    GoogleMap.oldGPSRangeData = cgps;
                    GoogleMap.refreshSettingGPSMap(cgps);
                    GoogleMap.setStatus(0);
                    
                  }
                  else if (!GoogleMap.oldGPSRangeData.equals(cgps))
                  {
                    Log.i(TAG, "get: " + cgps);
                    GoogleMap.oldGPSRangeData = "";
                    GoogleMap.refreshSettingGPSMap(cgps);
                    GoogleMap.setStatus(0);
                  }
                  else
                    GoogleMap.setStatus(0);
                }
                else if (line.equals("NoGPSRange"))
                {
                  Log.i(TAG, "nogpsrange");
                  GoogleMap.setStatus(1);
                }*/            
                IsOK = 2;
              	break;
              }
              	
              is.close();
            }
            else if (function == 2)
             {
              out.writeUTF("OVERRANGE");
              // As long as we receive data, server will data back to the client.
              DataInputStream is = new DataInputStream(client.getInputStream());
                
              while (true)
              {
                line = is.readUTF();
                if (line.equals("OK")) 
                {
                  IsOK = 2;
                  break;
                }
              }
              is.close();              
            }
            else if (function  == 3)
            {
              final Calendar c = Calendar.getInstance();
              int shour = c.get(Calendar.HOUR_OF_DAY);
              int sminute = c.get(Calendar.MINUTE);
              out.writeUTF("GetGPSRange");
              //¶Ç°e¦r¦ê®y¼Ð
              out.writeUTF(shour + ":" + sminute);
              
               // As long as we receive data, server will data back to the client.
              DataInputStream is = new DataInputStream(client.getInputStream());
              line = is.readUTF();
              if (line.equals("nowGPSRange"))
              {
                  String cname, cgps, cstime, cdtime;
                  
                  cname = is.readUTF();
                  cgps = is.readUTF();
                  cstime = is.readUTF();
                  cdtime = is.readUTF();
  
                  Log.i(TAG, "get: " +cgps);
                  GoogleMap.oldGPSRangeData = cgps;
                  GoogleMap.refreshSettingGPSMap(cgps);
                  GoogleMap.setStatus(0);
                }
                else if (line.equals("NoGPSRange"))
                {
                  Log.i(TAG, "nogpsrange");
                  GoogleMap.setStatus(1);
                }
              
                IsOK = 2;
                is.close();              
          }
            
          out.close();
          client.close();
        } catch (java.io.IOException e) 
        {
          e.printStackTrace();
        }
        
        try {
          Thread.sleep(2000);
        } 
        catch (InterruptedException e) 
        {
          e.printStackTrace();
        }
        
        timeout++;
        if (timeout > 10)
        {
          timeout = 0;
          GoogleMap.timeouthandler();
          break;
        }
     } while (IsOK != 2);
	}
}