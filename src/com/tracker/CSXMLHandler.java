package com.tracker;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class CSXMLHandler extends DefaultHandler
{
	  private boolean h_id = false;
    private boolean h_name = false;
    private boolean h_rangegps = false;
    private boolean h_stime = false;
    private boolean h_dtime = false;
	  
	  private CSXMLStruct myParsedExampleDataSet = new CSXMLStruct();
	  
	  public CSXMLStruct getParsedData() 
	  {
	       return this.myParsedExampleDataSet;
	  }
	  
	  public void startDocument() throws SAXException 
	  {
	       this.myParsedExampleDataSet = new CSXMLStruct();
	  }

	  @Override
	  public void endDocument() throws SAXException {
	       // Nothing to do
	  }

	  @Override
	  public void startElement(String namespaceURI, String localName,
	            String qName, Attributes atts) throws SAXException 
	  {
	       if (localName.toLowerCase().equals("name")) {
           this.h_name = true;
         }
	       else if (localName.toLowerCase().equals("rangegps")) {
           this.h_rangegps = true;
         }
	       else if (localName.toLowerCase().equals("stime")) {
           this.h_stime = true;
         }
	       else if (localName.toLowerCase().equals("dtime")) {
           this.h_dtime = true;
         }
	  }
	  @Override
	  public void endElement(String namespaceURI, String localName, String qName)
	           throws SAXException {
     if (localName.toLowerCase().equals("name")) {
      this.h_name = false;
    }
    else if (localName.toLowerCase().equals("rangegps")) {
      this.h_rangegps = false;
    }
    else if (localName.toLowerCase().equals("stime")) {
      this.h_stime = false;
    }
    else if (localName.toLowerCase().equals("dtime")) {
      this.h_dtime = false;
    }
	  }
	  

	  @Override
	 public void characters(char ch[], int start, int length) 
	  {
	    if(this.h_name)
      {
         myParsedExampleDataSet.h_name = new String(ch,start,length);
      }
      else if(this.h_rangegps)
      {
         myParsedExampleDataSet.h_rangegps = new String(ch,start,length);
      }
      else if(this.h_stime)
      {
         myParsedExampleDataSet.h_stime = new String(ch,start,length);
      }
      else if(this.h_dtime)
      {
         myParsedExampleDataSet.h_dtime = new String(ch,start,length);
      }
	 }

}
