package com.tracker;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.util.Log;

public class ChildListHandler extends DefaultHandler
{
	//tag
	private String TAG = "ChildListHandler";
	
	private final static int ID = 1;
	private final static int CHILDID = 2;
	private final static int NAME = 3;
	private final static int GPSDATA = 4;
	
	private ChildStruct jls;
	private ChildListContainer jlcs;
	
	private int type;

	public ChildListContainer getContainer() 
	{
		return jlcs;
	}

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		
		String s = new String(ch, start, length);
		
		switch (type) 
		{
		case ID:
			jls.id = s;
			type = 0;
			break;
		case CHILDID:
			jls.name = s;
			type = 0;
			break;
		case NAME:
      jls.name = s;
			type = 0;
			break;
		case GPSDATA:
      jls.gpsdata = s;
			type = 0;
			break;
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {

		if (localName.toLowerCase().equals("item")) 
		{
			jlcs.addRXMLItem(jls);	
		}
	}

	@Override
	public void startDocument() throws SAXException 
	{
		jlcs = new ChildListContainer();
	}

	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException 
	{
		if (localName.toLowerCase().equals("item")) 
		{
			jls = new ChildStruct();
			return;
		}
		else if (localName.toLowerCase().equals("id")) 
		{
			type = ID;
			return;
		}
		else if (localName.toLowerCase().equals("childid")) 
		{
			type = CHILDID;
			return;
		}
		else if (localName.toLowerCase().equals("name")) 
		{
			type = NAME;
			return;
		}
		else if (localName.toLowerCase().equals("gpsdata")) 
		{
			type = GPSDATA;
			return;
		}
		
		type = 0;
	}

}