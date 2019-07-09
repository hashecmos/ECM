package com.ecm.db.transport;

import java.util.ArrayList;

public class TWorkitemSet {
	public long totalCount;
	public long setCount;
	public long pages;
	public long curPage;
	public long pageSize = 25;
	
	public ArrayList<TWorkitemInfo> workitems;
}
