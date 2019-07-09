package com.ecm.db.util;

import java.util.ArrayList;
import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;

import com.ecm.db.list.ECMUserList;
import com.sun.org.apache.xml.internal.security.utils.Base64;


public class RequestHelper {

	public static ArrayList<String> getHeaders(HttpServletRequest request) {
		ArrayList<String> headerList = new ArrayList<String>();
		
		Enumeration<String> headerNames = request.getHeaderNames();
		while (headerNames.hasMoreElements()) {
			String headerName = headerNames.nextElement();
			String headerValue = request.getHeader(headerName);
			
			headerList.add("Name: " + headerName + ", Value: " + headerValue);
		}
		
		return headerList;
	}
	
	public static String getAuthorization(HttpServletRequest request) {
		try {
			Enumeration<String> headerNames = request.getHeaderNames();
			while (headerNames.hasMoreElements()) {
				String headerName = headerNames.nextElement();
				if(headerName.trim().equalsIgnoreCase("Authorization"))
					return base64Decode(getBase64EncodedString(request.getHeader(headerName)));
			}
		} catch (Exception e) {}
		return "";
	}
	
	private static String getBase64EncodedString(String authString) {
		try {
			return authString.replace("Basic ", "");
		} catch (Exception e) {}
		return authString;
	}
	private static String base64Decode(String encString) {
		try {
			return new String(Base64.decode(encString));
		} catch (Exception e) { }
		return encString;
	}
	
	public static String getLoggedInUser(HttpServletRequest request, String user_login) {
		try {
			if(request == null)
				return user_login;
			String reqUser = request.getRemoteUser();
			if((reqUser == null) || (reqUser.length() <= 0))
				reqUser = request.getUserPrincipal().getName().trim();
			if((reqUser == null) || (reqUser.length() <= 0))
				return user_login;
			
			return reqUser;
		} catch (Exception e) {
			return user_login;
		}
	}
	
	public static String getLoggedInEmpName(HttpServletRequest request, String user_login) {
		try {
			if(request == null)
				return user_login;
			String reqUser = request.getRemoteUser();
			if((reqUser == null) || (reqUser.length() <= 0))
				reqUser = request.getUserPrincipal().getName().trim();
			if((reqUser == null) || (reqUser.length() <= 0))
				return user_login;
			
			long empNo = ECMUserList.getInstance().getEmployee(reqUser);
			String empName = "";
			if(empNo <= 0)
				empNo = DBUtil.stringToLongDefault(user_login, 0);
			
			empName = ECMUserList.getInstance().getUserFullName(empNo);
			
			return empName;
		} catch (Exception e) {
			return user_login;
		}
	}
	
	public static long getLoggedInEmployee(HttpServletRequest request, String user_login) {
		try {
			String userName = getLoggedInUser(request, user_login);
			long empNo = ECMUserList.getInstance().getEmployee(userName);
			if(empNo <= 0)
				empNo = DBUtil.stringToLongDefault(user_login, 0);
			return empNo;
		} catch (Exception e) {
			return 0;
		}
	}
}
