package com.tracker;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.util.Log;

public class ChildListContainer 
{

	private ArrayList<ChildStruct> jlist_items;

	//item
	public ArrayList<ChildStruct> getListItems() 
	{
	  return jlist_items;
	}
	
	public ChildStruct getoneJL(int index)
	{
		return jlist_items.get(index);
	}
	
	public ChildListContainer() 
	{
		jlist_items = new ArrayList<ChildStruct>();
	}

	public void addRXMLItem(ChildStruct item) 
	{
		jlist_items.add(item);
	}
}
