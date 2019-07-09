package com.ecm.db.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class DBConfiguration {

	private static DBConfiguration sysConfig;
	private static final String encKey = "4568F72B8BE6E4132F90D14BB77943AF";
	
	public String JDBC_DRIVER;
	public String DB_URL;
	public String USER_NAME;
	public String PASSWORD;
	public String DATASOURCE_NAME;
	
	private DBConfiguration()
	{
		try {
			loadProperties();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static DBConfiguration getInstance()
	{
		//if (sysConfig == null)
		{
			sysConfig = new DBConfiguration();
		}
		
		return sysConfig;
	}
	
	private void loadProperties() throws Exception
	{
		InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("DBConn.properties");
		Properties props = new Properties();
		try {
			props.load(is);
			JDBC_DRIVER = props.getProperty("JDBC_DRIVER");
			DB_URL	= props.getProperty( "DB_URL" ) ;
			
			USER_NAME = decryptString(props.getProperty("USER_NAME"));
			PASSWORD = decryptString(props.getProperty("PASSWORD"));
			
			DATASOURCE_NAME = props.getProperty("DATASOURCE_NAME");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new Exception(e.getMessage());
		}
		finally
		{
			is.close();
		}
		
	}
	
	private static String decryptString(String inString) throws Exception {
		ECMEncryption enc = new ECMEncryption();
		return enc.getDecryptedString(encKey, inString);
	}

}
