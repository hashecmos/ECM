package com.ecm.db.util;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.UUID;

import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.UnsupportedEncodingException;


public class DBUtil {
	private static final Logger logger = Logger.getLogger(DBUtil.class);
	
	public static Connection getECMDBConnection() throws Exception {
		Connection conn = null;
		try {
			conn = getConnection(DBConfiguration.getInstance().DB_URL,
				DBConfiguration.getInstance().USER_NAME, 
				DBConfiguration.getInstance().PASSWORD,
				DBConfiguration.getInstance().JDBC_DRIVER);
			Statement stmt = conn.createStatement();
			stmt.execute("ALTER SESSION SET NLS_COMP=LINGUISTIC");
			stmt.execute("ALTER SESSION SET NLS_SORT=BINARY_CI");
		} catch (Exception e) {
		}
		return conn;
	}
	
	public static Connection getConnection(String bduri, String userName,String password,String driverDetails) throws Exception{
		logger.info("Driver Details::"+driverDetails+" DB URL ::"+bduri+"  userName :"+userName);
		Connection conn=null;
		try {
			
			Class.forName(driverDetails);
			conn = DriverManager.getConnection(bduri,userName,password);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error(e.getMessage(), e);
		}	
		logger.info("getConnection method exit");
		return conn;
	}
	
	public static Statement getStatement(Connection conn) throws Exception{
		logger.info("getStatement method start");
		Statement st = null;
		try {
			st = conn.createStatement();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error(e.getMessage(), e);
		}	
		logger.info("getStatement method exit");
		return st;
	}
	
	public static String getGUID()
	{
		String guid = null;
		UUID uid = UUID.randomUUID();
		guid = uid.toString();
		return guid;
	}

	public static PreparedStatement getPreparedStatement(Connection conn, String sqlQuery) {
		PreparedStatement pstm = null;
		try {
			pstm = conn.prepareStatement(sqlQuery);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return pstm;
	}
	
	public static String formatDateForQuery(String date)
	{
		StringTokenizer st =new StringTokenizer(date,"/");
		StringBuilder newDate = new StringBuilder();
		//List<String> list = new ArrayList<String>();
		int i = 0;
		String selectedDate = null;
		String selectedMonth = null;
		String selectedYear = null;
		while(st.hasMoreTokens()){
		String value = st.nextToken();
		
		if(i ==0){
			selectedDate = value;
		}
		if(i ==1){
			 selectedMonth = value;
		}
		if(i ==2){
			 selectedYear = value;
		}
			i++;
		}
		newDate.append(selectedYear.trim()).append(selectedMonth.trim()).append(selectedDate.trim());
		return newDate.toString().trim().concat("T210000Z");
	}
	
	
	public static String convertDateToStringforSQL(Date date){
		DateFormat df = new SimpleDateFormat("yyyy-MM-ddThh:mm:ss.nnnZ");
		String reportDate = df.format(date);
		return reportDate;
	}
	public static String convertDateToString(Date date){
		if(date == null)
			return "";
		DateFormat df = new SimpleDateFormat("EEE d MMM yyyy hh:mm:ss a");
		String reportDate = df.format(date);
		return reportDate;
	}
	
	public static String convertDateToShortString(Date date, String defString){
		if(date == null)
			return defString;
		DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
		String reportDate = df.format(date);
		return reportDate;
	}
	
	public static String convertDateTimeToString(Date date){
		if(date == null)
			return "";
		DateFormat df = new SimpleDateFormat("dd/MM/yyyy hh:mm a");
		String reportDate = df.format(date);
		return reportDate;
	}
	
	public static java.util.Date safeConvertStringToDateEx(String stringDate) {
		try {
			return convertStringToDateEx(stringDate);
		} catch (Exception ex) {
		}
		return null;
	}
	
	public static java.util.Date convertStringToDateEx(String stringDate)throws Exception{
		if((stringDate == null) || (stringDate.trim().length() <= 0))
			return null;
		
		String [] formats = {"dd/MM/yyyy hh:mm", "dd/MM/yyyy", "dd/MM/yyyy hh:mm:ss",
							"dd-MM-yyyy hh:mm", "dd-MM-yyyy", "yyyy-MM-dd", "dd-MM-yyyy hh:mm:ss",
							"dd/MM/yyyy hh:mm:ss.SSS", "dd-MM-yyyy hh:mm:ss.SSS",
							"EEE d MMM yyyy hh:mm:ss a"};
		for(String format:formats) {
			try {
				DateFormat readFormat = new SimpleDateFormat(format);
				Date date = null;
				date = readFormat.parse( stringDate );
				int nYear = date.getYear();
				if(nYear < 0)
					continue;
				return date;
			} catch(Exception e) {
			}
		}
		throw new Exception("Invalid date format");
	}
	
	public static Timestamp convertStringtoDate(String stringDate) throws Exception{
		try {
			if((stringDate == null) || (stringDate.trim().length() <= 0))
				return null;
			
			DateFormat format = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH);
			Date date = format.parse(stringDate);
			
			Calendar c = Calendar.getInstance();
			c.setTime(date);
			java.sql.Timestamp timestamp = new java.sql.Timestamp(c.getTimeInMillis());
			return timestamp;
		} catch (Exception e) {
			throw new Exception(e.getMessage());
		}
		
	}
	
	public static String formatDateForLog(Date date)
	{
		if(date == null)
			return "";
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss.SSS");
		String formattedDate = sdf.format(date);
		return formattedDate;
	}
	
	public static String formatDateForECMLog(Date date)
	{
		if(date == null)
			return "";
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
		String formattedDate = sdf.format(date);
		return formattedDate;
	}
	
	
	public static String formatDateForUI(Date date)
	{
		if(date == null)
			return "";
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm a");
		String formattedDate = sdf.format(date);
		return formattedDate;
	}
	
	public static String getTodayDate()
	{
		SimpleDateFormat simpleDateFormat =  new SimpleDateFormat("dd/MM/yyyy");
		Date date = new Date();
		Date todayDate = null;
		try {
			todayDate = simpleDateFormat.parse(simpleDateFormat.format(date));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(todayDate == null)
			return "";
		else
			return convertDateToShortString(todayDate, "");
	}
	
	public static String getTodayDateTime()
	{
		SimpleDateFormat simpleDateFormat =  new SimpleDateFormat("dd/MM/yyyy hh:mm a");
		Date date = new Date();
		Date todayDate = null;
		try {
			todayDate = simpleDateFormat.parse(simpleDateFormat.format(date));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(todayDate == null)
			return "";
		else
			return convertDateToShortString(todayDate, "");
	}
	
	public static String formatDateForECMNo(Date date)
	{
		try {
		if(date == null)
			return "";
		SimpleDateFormat sdf = new SimpleDateFormat("yyMMdd");
		String formattedDate = sdf.format(date);
		return formattedDate;
		} catch (Exception e) {
			return "";
		}
	}
	
	public static String formatDateForFileName(Date date)
	{
		if(date == null)
			return "No-Date";
		SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy-hh-mm");
		String formattedDate = sdf.format(date);
		return formattedDate;
	}
	
	public static Timestamp getCurrentTimestamp() throws Exception{
		try {
			Calendar c1 = Calendar.getInstance();
			c1.setTime(new java.util.Date()); // Now use today date.
			c1.add(Calendar.DATE, 0); 
			java.sql.Timestamp timestamp = new java.sql.Timestamp(c1.getTimeInMillis());
			return timestamp;
		} catch (Exception e) {
			throw new Exception(e.getMessage());
		}
	}
	
	public static Timestamp addDaystoGivenDate(Date date, int noOfDays){
		Calendar c = Calendar.getInstance();
		c.setTime(date); // Now use today date.
		c.add(Calendar.DATE, noOfDays); // Adding noOfDays
		java.sql.Timestamp timestamp = new java.sql.Timestamp(c.getTimeInMillis());
		return timestamp;
	}
	
	public static Timestamp addDaystoDate(int noOfDays){
		Calendar c = Calendar.getInstance();
		c.setTime(new java.util.Date()); // Now use today date.
		c.add(Calendar.DATE, noOfDays); // Adding noOfDays
		java.sql.Timestamp timestamp = new java.sql.Timestamp(c.getTimeInMillis());
		return timestamp;
	}
	
	public static Timestamp addYearstoDate(int noOfYears){
		Calendar c = Calendar.getInstance();
		c.setTime(new java.util.Date()); // Now use today date.
		c.add(Calendar.YEAR, noOfYears); // Adding noOfDays
		java.sql.Timestamp timestamp = new java.sql.Timestamp(c.getTimeInMillis());
		return timestamp;
	}
	
	public static Timestamp minusOneDay(String dateString) throws Exception{
		DateFormat sdf1 = new SimpleDateFormat("dd/MM/yyyy");
		java.util.Date date = sdf1.parse(dateString);
		Calendar c1 = Calendar.getInstance();
		c1.setTime(date);
		c1.add(Calendar.DATE, -1);
		java.sql.Timestamp timestamp = new java.sql.Timestamp(c1.getTimeInMillis());
		return timestamp;
	}
	
	public static String getOverdue(Date workitemOverdueDate) {

		logger.debug("Entering getOverdue method");
		String isOverdue="false";
		SimpleDateFormat simpleDateFormat =  new SimpleDateFormat("dd/MM/yyyy");
		try {
			Date date = new Date();
			Date todayDate = simpleDateFormat.parse(simpleDateFormat.format(date));
			Date workItmDueDate = simpleDateFormat.parse(simpleDateFormat.format(workitemOverdueDate));
			
			if (todayDate.compareTo(workItmDueDate) > 0) {
				isOverdue="true";
	        }
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			e.printStackTrace();
		}
		logger.debug("Exit from getOverdue");
		return isOverdue;
	
	}

	
	public static String escapeString(String inString)
	{
		String outString = null;
		String originalString = inString;
		if(inString != null)
		{
			outString = inString.replaceAll("'", "''");
		
			byte[] byteText = outString.getBytes(Charset.forName("UTF-8"));
			//To get original string from byte.
			try {
				originalString = new String(byteText , "UTF-8");
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return originalString;
	}
	
	public static long stringToLong(String inString)
	{
		try {
			return Long.parseLong(inString);
		} catch (Exception e) {
			return 0;
		}
	}
	
	public static String intToString(int intString)
	{
		try {
			return String.valueOf(intString);
		} catch (Exception e) {
			return "";
		}
	}
	
	public static long stringToLongDefault(String inString, long def)
	{
		try {
			return Long.parseLong(inString);
		} catch (Exception e) {
			return def;
		}
	}
	
	public static String clobToString(Clob data) {
	    StringBuilder sb = new StringBuilder();
	    try {
	        java.io.Reader reader = data.getCharacterStream();
	        java.io.BufferedReader br = new BufferedReader(reader);

	        String line;
	        while(null != (line = br.readLine())) {
	            sb.append(line);
	        }
	        br.close();
	    } catch (Exception e) {
	        // handle this exception
	    }
	    return sb.toString();
	}
	
	public static String appendChar(String inString, char ch)
    {
        String outString = "" + ch;
        if (inString.length() > 0)
        {
            outString = inString;
            if (inString.charAt(inString.length() - 1) != ch)
                outString += ch;
        }
        return outString;
    }
	
	public static String removeAfter(String inString, char ch) {
		String removedString = inString;
		if((inString == null) || (inString.length() <= 0))
				return inString;
		int chIndex = inString.indexOf(ch);
		if(chIndex < 0)
			return inString;
		if(chIndex == 0)
			return "";
		if(chIndex < inString.length())
			removedString = inString.substring(0, inString.indexOf('@'));
		return removedString;
	}
	
	public static String getPriorityValue(long priority) {
        String strValue = "";
        int iPriority = (int) priority;
        if (iPriority == 3)
              strValue = "High";
        else if (iPriority == 2)
              strValue = "Normal";
        else if (iPriority == 1)
              strValue = "Low";
        return strValue;
  }

}
