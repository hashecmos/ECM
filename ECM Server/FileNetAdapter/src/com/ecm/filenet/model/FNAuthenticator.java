package com.ecm.filenet.model;

import com.ecm.db.util.DBUtil;

public class FNAuthenticator {
	
	private FNAuthenticator() { }
	
	public static void Authenticate(String empNoString) throws Exception
	{
		long empNo = DBUtil.stringToLong(empNoString);
		FNAuthenticator fna = new FNAuthenticator();
		fna.ValidateUser(empNo);
	}
	
	private void ValidateUser(long empNo) throws Exception
	{
		// Add authentication code here
	}
}
