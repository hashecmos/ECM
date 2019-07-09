package com.ecm.filenet.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.Format;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.filenet.api.constants.AccessRight;


public class Utils {
	
	static final Logger logger = Logger.getLogger(Utils.class);

	public static String formatDate(Date date){
	
	Format formatter = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss");
	
	//Date dateCreated = (Date)date;
	String fomattedDate = formatter.format(date);
	return fomattedDate;
	}
	
	public static int stringToInt(String inString)
	{
		return Integer.parseInt(inString);
	}
	public static long stringToLong(String inString)
	{
		return Long.parseLong(inString);
	}
	public static Boolean stringToBoolean(String inString)
	{
		return Boolean.parseBoolean(inString);
	}
	
	public static String formatDateForUI(Date date)
	{
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm a");
		String formattedDate = sdf.format(date);
		return formattedDate;
	}
	
	public static String formatDateForDocumentDate(Date date)
	{
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
		String formattedDate = sdf.format(date);
		return formattedDate;
	}
	
	public static String getOnlyStrings(String s) {
	    Pattern pattern = Pattern.compile("[^a-z A-Z]");
	    Matcher matcher = pattern.matcher(s);
	    String number = matcher.replaceAll("");
	    return number;
	}
	
	public static String formatDateForQuery(String date)
	{
		StringTokenizer st =new StringTokenizer(date,"/");
		StringBuilder newDate = new StringBuilder();
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
		//newDate.toString().trim().concat("T040000Z");
	//T214614Z
		//T183000Z
		return newDate.toString().trim().concat("T210000Z");
	}
	
	public static String getStartFileNetQueryDate(Date date) {
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.add(Calendar.DATE,  -1); 
		return getEndFileNetQueryDate(c.getTime());
	}
	
	public static String getEndFileNetQueryDate(Date date) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		String formattedDate = sdf.format(date);
		return formattedDate + "T210000Z";
	}
	
	public static String convertStreamToString(InputStream is)	throws IOException {


		if (is != null) {

			Writer writer = new StringWriter();
			char[] buffer = new char[1024];
			try {
				Reader reader = new BufferedReader(
				new InputStreamReader(is, "UTF-8"));
				int n;
				while ((n = reader.read(buffer)) != -1) {
					writer.write(buffer, 0, n);
				}

			} finally {
				is.close();
			}

			return writer.toString();
		} else {
			return "";
		}
	}
	public static List<String> setMultipleValues(String input){
		
		StringTokenizer st =new StringTokenizer(input,";");
		List<String> list = new ArrayList<String>();
		while(st.hasMoreTokens()){
		String value = st.nextToken();
			list.add(value);
			
		}
		return list;
		
	}
	public static int getAccessRightsIntValue(List<String> accessRights){
		
		List<Integer> summation = new ArrayList<Integer>();
		for (int i = 0; i < accessRights.size(); i++) {
			
			if(accessRights.get(i).equalsIgnoreCase("DELETE")){
				
				int value = 65536;
				summation.add(new Integer(value));
				
			}
			else if(accessRights.get(i).equalsIgnoreCase("LINK")){
				int value = 16;
				summation.add(new Integer(value));
				
			}
			else if(accessRights.get(i).equalsIgnoreCase("UNLINK")){
				int value = 32;
				summation.add(new Integer(value));
				
			}
			else if(accessRights.get(i).equalsIgnoreCase("WRITE_ACL")){
				
				int value = AccessRight.WRITE_ACL_AS_INT;
				summation.add(new Integer(value));
			}
			else if(accessRights.get(i).equalsIgnoreCase("VIEW_CONTENT")){
				
				int value = 128;
				summation.add(new Integer(value));
			}
			else if(accessRights.get(i).equalsIgnoreCase("READ")){
	
				int value = 1;
					summation.add(new Integer(value));
			}
			else if(accessRights.get(i).equalsIgnoreCase("READ_ACL")){
	
				int value = 131072;
				summation.add(new Integer(value));
			}
			else if(accessRights.get(i).equalsIgnoreCase("MAJOR_VERSION")){
				
				int value = 4;
				summation.add(new Integer(value));
			}
			else if(accessRights.get(i).equalsIgnoreCase("CREATE_INSTANCE")){
				
				int value = 256;
				summation.add(new Integer(value));
			}
			else if(accessRights.get(i).equalsIgnoreCase("NONE")){
				
				int value = 0;
				summation.add(new Integer(value));
			}
			else if(accessRights.get(i).equalsIgnoreCase("WRITE_OWNER")){
				
				int value = 524288;
				summation.add(new Integer(value));
			}
			else if(accessRights.get(i).equalsIgnoreCase("WRITE")){
				
				int value = 2;
				summation.add(new Integer(value));
			}
			else if(accessRights.get(i).equalsIgnoreCase("CREATE_CHILD")){
				
				int value = 512;
				summation.add(new Integer(value));
			}
			
		}
		
		
		return add(summation);
		}
		
	private static int add(List<Integer> summation){
		int sum = 0;
		for (int i = 0; i < summation.size(); i++) {
			
			sum = summation.get(i)+sum;
			//logger.info("summmmmmm"+sum);
		}
		return sum;
	}
	
	public static String maskCanonicalName(String cnName) {
		try {
			if((cnName == null) || (cnName.trim().length() <= 0))
				return "";
			
			if((cnName.indexOf("CN=") ==0) || (cnName.indexOf("cn=") == 0)) {
				int nOu = cnName.indexOf(",OU=");
				if((nOu > 0) && (nOu < cnName.length())) {
					return cnName.substring(3, nOu);
				}
			}
		} catch (Exception e) {
		}
		return cnName;
	}
	
	public static List<String> getExcludeDocClassList(String docClassString)
	{
		List<String> docClassList = new ArrayList<String>();
		docClassList = setMultipleValues(docClassString);
		return docClassList;
	}
	
	public static String getFavoritesQuery(List<String> vsList)
	{
		StringBuilder favQueryBuilder = new StringBuilder();
		String favQuery = null;
		int counter = 0;
		for (int i = 0; i < vsList.size(); i++) {
			
			favQueryBuilder = favQueryBuilder.append("[VersionSeries] = Object('" +vsList.get(i) + "')" );
			
			if(counter < vsList.size()-1 )
				{
				favQueryBuilder.append(" OR ");
				}
			counter++;	
		}
		favQuery = " ( " + favQueryBuilder.toString() + " ) " ;
		return favQuery;
	}
	public static String convertToUTC(String presentDate) throws ParseException
	{
		 
		 String date = presentDate; 
		 SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
		 Calendar c = Calendar.getInstance();
		 c.setTime(df.parse(date));
		 c.add(Calendar.DATE, -1);  // how many days you want to add like here 1
		 String presentUTCDate = df.format(c.getTime()); 
		 
		 
		 return presentUTCDate;
	}
	public static String addingDate(String presentDate) throws ParseException
	{
		 
		 String date = presentDate; 
		 SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
		 Calendar c = Calendar.getInstance();
		 c.setTime(df.parse(date));
		 c.add(Calendar.DATE, 1);  // how many days you want to add like here 1
		 String addeddate = df.format(c.getTime()); 
		 
		 
		 return addeddate;
	}
	public static String getMimeType(String fileName){
		  
		  String mimeType =null;
		  StringTokenizer stringTokenizer = new  StringTokenizer(fileName,".");
		  System.out.println(fileName+"...................................................................");
		  while ( stringTokenizer.hasMoreElements() ) {
			  mimeType = stringTokenizer.nextToken();
			 
			  logger.info("mimetype :"+mimeType);	 
		  }
		  String orMime=mimeType;
		  System.out.println("BEgining "+mimeType);
		  InputStream is =Thread.currentThread().getContextClassLoader().getResourceAsStream("MimeType.properties");
		  Properties props=new Properties();
		  try {
			props.load(is);
			Enumeration e=props.propertyNames();
			while(e.hasMoreElements()){
				String key=(String)e.nextElement();
				System.out.println("Key Inside While "+key);
				if(mimeType.equals(key)){
					mimeType=props.getProperty(key);
					System.out.println("Inside while Mimetype "+mimeType);
					
					break;
				}		
			}
			if(mimeType.equals(orMime))
			{
				mimeType="application/octet-stream";
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		  
			if (mimeType.equalsIgnoreCase("pdf")) {
				mimeType = "application/pdf";
			} else if (mimeType.equalsIgnoreCase("ogg") ) {
				mimeType = "application/ogg";
			} else if (mimeType.equalsIgnoreCase("dtd") ) {
				mimeType = "application/xml-dtd";
			} else if (mimeType.equalsIgnoreCase("xhtml") ) {
				mimeType = "application/xhtml+xml";
			} else if (mimeType.equalsIgnoreCase("gzip") ) {
				mimeType = "application/x-gzip";
			} else if (mimeType.equalsIgnoreCase("zip") ) {
				mimeType = "application/zip";
			} else if (mimeType.equalsIgnoreCase("doc")	|| mimeType.equalsIgnoreCase("docx")) {
				mimeType = "application/msword";
			} else if (mimeType.equalsIgnoreCase("xls") || mimeType.equalsIgnoreCase("xlsx")) {
				mimeType = "application/vnd.ms-excel";
			} else if (mimeType.equalsIgnoreCase("bmp")) {
				mimeType = "image/bmp";
			} else if (mimeType.equalsIgnoreCase("ico")) {
				mimeType = "image/vnd.microsoft.icon";
			} else if (mimeType.equalsIgnoreCase("tiff")) {
				mimeType = "image/tiff";
			} else if (mimeType.equalsIgnoreCase("gif")) {
				mimeType = "image/gif";
			} else if (mimeType.equalsIgnoreCase("tif") || mimeType.equalsIgnoreCase("tiff")) {
				mimeType = "image/tiff";
			} else if (mimeType.equalsIgnoreCase("jpg") || mimeType.equalsIgnoreCase("jpe") || mimeType.equalsIgnoreCase("jpeg")) {
				mimeType = "image/jpeg";
			} else if (mimeType.equalsIgnoreCase("png")) {
				mimeType = "image/png";
			} else if (mimeType.equalsIgnoreCase("txt")) {
				mimeType = "text/plain";
			} else if (mimeType.equalsIgnoreCase("xml")) {
				mimeType = "text/xml";
			} else if (mimeType.equalsIgnoreCase("js")) {
				mimeType = "text/javascript";
			} else if (mimeType.equalsIgnoreCase("html")) {
				mimeType = "text/html";
			} else if (mimeType.equalsIgnoreCase("css")) {
				mimeType = "text/css";
			} else if (mimeType.equalsIgnoreCase("csv")) {
				mimeType = "text/csv";
			} else if (mimeType.equalsIgnoreCase("ai")) {
				mimeType = "application/ai";
			} else if (mimeType.equalsIgnoreCase("as")) {
				mimeType = "application/as";
			} else if (mimeType.equalsIgnoreCase("avi")) {
				mimeType = "application/avi";
			} else if (mimeType.equalsIgnoreCase("emf")) {
				mimeType = "application/emf";
			} else if (mimeType.equalsIgnoreCase("eps")) {
				mimeType = "application/eps";
			} else if (mimeType.equalsIgnoreCase("exe")) {
				mimeType = "application/exe";
			} else if (mimeType.equalsIgnoreCase("fla")) {
				mimeType = "application/fla";
			} else if (mimeType.equalsIgnoreCase("fon")) {
				mimeType = "application/fon";
			} else if (mimeType.equalsIgnoreCase("pot")) {
				mimeType = "application/pot";
			} else if (mimeType.equalsIgnoreCase("psd")) {
				mimeType = "application/psd";
			} else if (mimeType.equalsIgnoreCase("ttf")) {
				mimeType = "application/ttf";
			} else if (mimeType.equalsIgnoreCase("wav")) {
				mimeType = "application/wav";
			} else if (mimeType.equalsIgnoreCase("wmf")) {
				mimeType = "application/wmf";
			} else if (mimeType.equalsIgnoreCase("ppt") || mimeType.equalsIgnoreCase("pptx") ) {
				mimeType = "application/ppt";
			} else if (mimeType.equalsIgnoreCase("xlsx")) {
				mimeType = "application/xlsx";
			} else if (mimeType.equalsIgnoreCase("bin") || mimeType.equalsIgnoreCase("class") || mimeType.equalsIgnoreCase("dll")) {
				mimeType = "application/octet-stream";
			} else {
				mimeType = "application/octet-stream";
			}
		}
		System.out.println("Outside "+mimeType);
		return mimeType;
		  
	  }
	
	public static String getFileNameFromMimeType(String fileName, String mimeType){
		String justFile = fileName;
		String ext = "";
		if((fileName != null) && (fileName.trim().length() > 0)) {
			int lI = fileName.lastIndexOf('.');
			if((lI > 0) && (lI < fileName.length())) {
				justFile = fileName.substring(0, lI);
				try{
					ext = fileName.substring(lI + 1, fileName.length() - 1);
				}
				catch(Exception ex){}
			}
		}
		String retFile = justFile + "." + ext;
		if(ext.length() <= 0)
			retFile = justFile;
		if((mimeType == null) || (mimeType.trim().length() <= 0) ||
				mimeType.equals("application/octet-stream"))
				return encodeString(retFile) ;
		
		  System.out.println("Begining "+mimeType);
		  InputStream is =Thread.currentThread().getContextClassLoader().getResourceAsStream("MimeType.properties");
		  Properties props=new Properties();
		  try {
			props.load(is);
			Enumeration e=props.propertyNames();
			while(e.hasMoreElements()){
				String key=(String)e.nextElement();
				String mType = props.getProperty(key);
				System.out.println("Key Inside While "+key);
				if(mimeType.equals(mType)){
					ext=key;
					System.out.println("Inside while Mimetype "+mimeType + " Ext: " + ext);
					
					break;
				}		
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			ext = getExtensionDefault(mimeType, ext);
			
		}
		System.out.println("Outside "+ext);

		retFile = justFile + "." + ext;
		if(ext.length() <= 0)
			retFile = justFile;
		
        return encodeString(retFile);
	  }
	
	private static String encodeString(String retFile){
		   String outString = null;
           String finalRetFile = retFile;
           if(retFile != null)
           {
                 outString = retFile.replaceAll("'", "''");            
                 byte[] byteText = outString.getBytes(Charset.forName("UTF-8"));
                 try {
                       finalRetFile = new String(byteText , "UTF-8");
                 } catch (UnsupportedEncodingException e) {
                       e.printStackTrace();
                 }
           }          
           return finalRetFile;
	}
	
	
	public static String getFileNameFromMimeType(String fileName, String mimeType, int docCount){
		String justFile = fileName;
		String ext = "";
		if((fileName != null) && (fileName.trim().length() > 0)) {
			int lI = fileName.lastIndexOf('.');
			if((lI > 0) && (lI < fileName.length())) {
				justFile = fileName.substring(0, lI);
				ext = fileName.substring(lI + 1, fileName.length() - 1);
			}
		}
		String retFile = justFile + "." + ext;
		if(ext.length() <= 0)
			retFile = justFile + "." + docCount;
		if((mimeType == null) || (mimeType.trim().length() <= 0) ||
				mimeType.equals("application/octet-stream"))
				return encodeString(retFile)  + "." + docCount;
		
		  System.out.println("BEgining "+mimeType);
		  InputStream is =Thread.currentThread().getContextClassLoader().getResourceAsStream("MimeType.properties");
		  Properties props=new Properties();
		  try {
			props.load(is);
			Enumeration e=props.propertyNames();
			while(e.hasMoreElements()){
				String key=(String)e.nextElement();
				String mType = props.getProperty(key);
				System.out.println("Key Inside While "+key);
				if(mimeType.equals(mType)){
					ext=key;
					System.out.println("Inside while Mimetype "+mimeType + " Ext: " + ext);				
					break;
				}		
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			ext = getExtensionDefault(mimeType, ext);
			
		}
		System.out.println("Outside "+ext);

		retFile = justFile  + "." + docCount + "." + ext;
		if(ext.length() <= 0)
			retFile = justFile + "." + docCount;
		return encodeString(retFile);
	  }
	
	private static String getExtensionDefault(String mimeType, String ext) {
		try {
			if (mimeType.equalsIgnoreCase("application/pdf")) {
				ext = "pdf";
			} else if (mimeType.equalsIgnoreCase("application/ogg") ) {
				ext = "ogg";
			} else if (mimeType.equalsIgnoreCase("application/xml-dtd") ) {
				ext = "dtd";
			} else if (mimeType.equalsIgnoreCase("application/xhtml+xml") ) {
				ext = "xhtml";
			} else if (mimeType.equalsIgnoreCase("application/x-gzip") ) {
				ext = "gzip";
			} else if (mimeType.equalsIgnoreCase("application/zip") ) {
				ext = "zip";
			} else if (mimeType.equalsIgnoreCase("application/msword")) {
				ext = "docx";
			} else if (mimeType.equalsIgnoreCase("application/vnd.ms-excel")) {
				ext = "xlsx";
			} else if (mimeType.equalsIgnoreCase("image/bmp")) {
				ext = "bmp";
			} else if (mimeType.equalsIgnoreCase("image/vnd.microsoft.icon")) {
				ext = "ico";
			} else if (mimeType.equalsIgnoreCase("image/tiff")) {
				ext = "tif";
			} else if (mimeType.equalsIgnoreCase("image/gif")) {
				ext = "gif";
			} else if (mimeType.equalsIgnoreCase("image/jpeg")) {
				ext = "jpg";
			} else if (mimeType.equalsIgnoreCase("image/png")) {
				ext = "png";
			} else if (mimeType.equalsIgnoreCase("text/plain")) {
				ext = "txt";
			} else if (mimeType.equalsIgnoreCase("text/xml")) {
				ext = "xml";
			} else if (mimeType.equalsIgnoreCase("text/javascript")) {
				ext = "js";
			} else if (mimeType.equalsIgnoreCase("text/html")) {
				ext = "html";
			} else if (mimeType.equalsIgnoreCase("text/css")) {
				ext = "css";
			} else if (mimeType.equalsIgnoreCase("text/csv")) {
				ext = "csv";
			} else if (mimeType.equalsIgnoreCase("application/ai")) {
				ext = "ai";
			} else if (mimeType.equalsIgnoreCase("application/as")) {
				ext = "as";
			} else if (mimeType.equalsIgnoreCase("application/avi")) {
				ext = "avi";
			} else if (mimeType.equalsIgnoreCase("application/emf")) {
				ext = "emf";
			} else if (mimeType.equalsIgnoreCase("application/eps")) {
				ext = "eps";
			} else if (mimeType.equalsIgnoreCase("application/exe")) {
				ext = "exe";
			} else if (mimeType.equalsIgnoreCase("application/fla")) {
				ext = "fla";
			} else if (mimeType.equalsIgnoreCase("application/fon")) {
				ext = "fon";
			} else if (mimeType.equalsIgnoreCase("application/pot")) {
				ext = "pot";
			} else if (mimeType.equalsIgnoreCase("application/psd")) {
				ext = "psd";
			} else if (mimeType.equalsIgnoreCase("application/ttf")) {
				ext = "ttf";
			} else if (mimeType.equalsIgnoreCase("application/wav")) {
				ext = "wav";
			} else if (mimeType.equalsIgnoreCase("application/wmf")) {
				ext = "wmf";
			} else if (mimeType.equalsIgnoreCase("application/ppt") ) {
				ext = "pptx";
			} else if (mimeType.equalsIgnoreCase("application/pptx") ) {
				ext = "pptx";
			} else if (mimeType.equalsIgnoreCase("application/xlsx")) {
				ext = "xlsx";
			}
		} catch (Exception e) {}
		return ext;
	}
	
	public static String getFileNetSystemPropertyName(String prop) {
		String filenetName = prop;
		if (prop.equals("majorVersion"))
			return "MajorVersionNumber";
		else if (prop.equals("dateCreated"))
			return "DateCreated";
		else if (prop.equals("createdBy"))
			return "Creator";
		else
		return filenetName;
	}

	public static String appendSlash(String inString)
	{
		if((inString == null) || (inString.length() <= 0))
			return "/";
		if(inString.charAt(inString.length() - 1) == '/')
			return inString;
		else if(inString.charAt(inString.length() - 1) == '\\')
			return inString.replace((char) (inString.length()-1), '/');
		else
			return inString + "/";
	}
	
	public static String appendChar(String inString, char ch)
	{
		if((inString == null) || (inString.length() <= 0))
			return "" + ch;
		if(inString.charAt(inString.length() - 1) == ch)
			return inString;
		else
			return inString + ch;
	}
	
	public static Date getDateFromString(String dateStr)
	{
		Date dt = null;
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
			if(!(dateStr.trim().equalsIgnoreCase(""))){
				dt = sdf.parse(dateStr);
			} 
		} catch (Exception e) {
		}
		return dt;
	}

	public static java.util.Date convertStringToDateEx(String stringDate){
		if((stringDate == null) || (stringDate.trim().length() <= 0))
			return null;
		
		String [] formats = {"dd/MM/yyyy hh:mm", "dd/MM/yyyy", "dd/MM/yyyy hh:mm:ss", 
							"dd-MM-yyyy hh:mm", "dd-MM-yyyy", "dd-MM-yyyy hh:mm:ss", "yyyy-MM-dd",
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
		return null;
	}
	
	public static String getAccessLevel(int accessMask) {
		String levelNames [] = { "View All properties", "Modify all properties",
				"Major versioning", "Link a document/annotate", "Unlink documents", 
				"Minor versioning", "View content", "Create instance", "Create sub folder",
				"Change State", "Publish", "Reserved 12", "Reserved 13", "Delete",
				"Read permissions", "Modify permissions", "Modify owner" };
		int levelMasks [] = {1, 2, 4, 16, 32, 64, 128, 256, 512, 1024, 2048, 4096, 8192, 65536,
			131072, 262144, 524288 };
		
		for(int i = 0; i < levelMasks.length; i++)
			if(accessMask == levelMasks[i])
				return levelNames[i];
		
		if(((accessMask & 524288) != 0) && ((accessMask & 65536) == 0))
			return "Owner";
		if(((accessMask & 65536) != 0) && ((accessMask & 524288) != 0))
			return "Full Control";		
		if(((accessMask & 2) != 0) && ((accessMask & 4) != 0) || ((accessMask & 64) != 0))
			return "Author";
		if(((accessMask & 128) != 0) && ((accessMask & 1) != 0))
			return "Viewer";
		
		return "Custom";
	}
	
	public static ArrayList<String> getAccessPrivileges(int accessMask) {	
		ArrayList<String> pList = new ArrayList<String>();
		String levelNames [] = { "View All properties", "Modify all properties",
				"Major versioning", "Link a document/annotate", "Unlink documents", 
				"Minor versioning", "View content", "Create instance", "Create sub folder",
				"Change State", "Publish", "Reserved 12", "Reserved 13", "Delete",
				"Read permissions", "Modify permissions", "Modify owner" };
		int levelMasks [] = {1, 2, 4, 16, 32, 64, 128, 256, 512, 1024, 2048, 4096, 8192, 65536,
			131072, 262144, 524288 };
		
		for(int i = 0; i < levelMasks.length; i++)
			if((accessMask & levelMasks[i]) == 0)
				pList.add(levelNames[i]);
		
		return pList;
	}
	
	public static String getInheritableDepthName(int nDepth) {
		switch(nDepth) {
		case 0: return "This object only";
		case 1: return "This object and immediate children only";
		case -1: return "This object and all children";
		case -2: return "All children but not this object";
		case -3: return "Immediate children only, not this object";
		default: return "Unknown";
		}
	}
	
	public static String getTempFilePath() throws Exception{
		String javaTemp = System.getProperty("java.io.tmpdir");
		if(javaTemp == null)
			javaTemp = "C:\\Temp";
		File tempFolder = new File(javaTemp);
		String tempFilePath = "";
		if (!tempFolder.exists()) {
			tempFilePath = tempFolder.getAbsolutePath();
		    try{
		    	tempFolder.mkdir();
		    } 
		    catch(Exception se){
		    	throw new Exception("Error creating folder : " + javaTemp + " " + se.getMessage());
		    }
		}else{
			tempFilePath = tempFolder.getAbsolutePath();		
		}
		return appendChar(tempFilePath, '\\');
	}
	
	public static String getUniqueDownloadFileName(String prefix) throws Exception {
		String fileName = prefix + "_download";
		if(Calendar.getInstance().get(Calendar.AM_PM) == 1){
			fileName = prefix + (Calendar.getInstance().get(Calendar.MONTH)+1)+"_"+Calendar.getInstance().get(Calendar.DATE)+
					"_"+Calendar.getInstance().get(Calendar.YEAR)+"_"+
					Calendar.getInstance().get(Calendar.HOUR)+"_"+Calendar.getInstance().get(Calendar.MINUTE)+"_"+
					Calendar.getInstance().get(Calendar.SECOND)+"_PM";
		}else{
			fileName = prefix + (Calendar.getInstance().get(Calendar.MONTH)+1)+"_"+Calendar.getInstance().get(Calendar.DATE)+
					"_"+Calendar.getInstance().get(Calendar.YEAR)+"_"+
					Calendar.getInstance().get(Calendar.HOUR)+"_"+Calendar.getInstance().get(Calendar.MINUTE)+"_"+
					Calendar.getInstance().get(Calendar.SECOND)+"_AM";
		}
		return fileName;
	}
	
	public static void safeDelete(String fileName) {
		try {
			if(fileName == null)
				return;
			File file = new File(fileName);
			file.delete();
		} catch (Exception ex) {
			System.out.println("Delete file " + fileName + " failed. " + ex.getMessage());
		}
	}
}
