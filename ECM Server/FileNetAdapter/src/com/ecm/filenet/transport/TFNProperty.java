package com.ecm.filenet.transport;

import java.util.ArrayList;

import com.ecm.db.transport.TLookupValue;

public class TFNProperty {
	public String name = null;
	public String symName = null;
	public String desc = null;
	public String id = null;
	public String dtype = "STRING";
	public ArrayList<TLookupValue> lookups = null; 
	public ArrayList<String> mvalues = new ArrayList<String>();
	public String mtype = "N";
	public int len = 64;
	public String oper = null;
	public String rOnly = "FALSE";
	public String hidden = "FALSE";
	public String req = "FALSE";
	public long ltype = 0;
}
