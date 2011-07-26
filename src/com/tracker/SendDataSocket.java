package com.tracker;

import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
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

	private String address;
	private int port;
	private int function;
	private String timestamp;
	private int PicCount;
	private int IsOK;
	private MyGoogleMap GoogleMap;
	private int timeout;
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
	
	public int getTimeout()
	{
	  return timeout;
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
    do{
        Socket client = new Socket();
        InetSocketAddress isa = new InetSocketAddress(address, port);
        try {
            client.connect(isa, 10000);
            
            DataOutputStream out = new DataOutputStream(client.getOutputStream());

            if (function  == 1)
             {
              out.writeUTF("SetNowStatus");
            	  out.writeUTF(send_Data);
            	 // As long as we receive data, server will data back to the client.
              DataInputStream is = new DataInputStream(client.getInputStream());
              line = is.readUTF();
              while (line.equals("OK")) 
                {
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
            else if (function == 3)
            {
              IsOK = 0;
              out.writeUTF("GetGPSRange");
              // As long as we receive data, server will data back to the client.
              DataInputStream is = new DataInputStream(client.getInputStream());
              line = is.readUTF();
              if (!line.equals("NoRangeData"))
               {
                Log.v("vDEBUG: ", "vClient " + line);
                //call back
                IsOK = 2;
                GoogleMap.refreshSettingGPSMap(line);
               }
              else
               {
                IsOK = 1;
               }
              is.close();
            }            
          out.close();
          client.close();
        } catch (java.io.IOException e) {
          e.printStackTrace();
        }
        timeout++;
        if (timeout > 10)
        {
          GoogleMap.timeouthandler();
          break;
        }
     } while (IsOK != 2);
	}
}