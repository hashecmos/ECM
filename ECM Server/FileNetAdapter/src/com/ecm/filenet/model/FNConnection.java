package com.ecm.filenet.model;

import org.apache.log4j.Logger;

import javax.security.auth.Subject;

import com.ecm.db.list.ECMConfigurationList;
import com.ecm.db.util.ECMEncryption;
import com.filenet.api.core.Connection;
import com.filenet.api.core.Domain;
import com.filenet.api.core.Factory;
import com.filenet.api.util.UserContext;

public class FNConnection {
	private static final Logger logger = Logger.getLogger(FNConnection.class);
	
	private static final String encKey = "8D14AC17AA4209231F357DB0EDF76DE4";
	
	private ECMConfigurationList configList = null;
	
	public static FNConnection getInstance()
	{
		return new FNConnection();
	}
	
	private FNConnection()
	{
	}
	
	private ECMConfigurationList getConfigurations()
	{
		if(configList != null)
			return configList;
		
		configList = ECMConfigurationList.getInstance("ECM", "SYSTEM");
		return configList;
	}
	
	public Connection getConnection() throws Exception {
		
		String uri = getConfigurations().getConfigValue("P8URL");
		Connection conn = Factory.Connection.getConnection(uri);
		String p8Auth = getConfigurations().getConfigValue("P8AUTH");
		if(p8Auth.equalsIgnoreCase("STANDALONE"))
		{
			String stanza = getConfigurations().getConfigValue("P8STANZA");
			String userid = decryptString(getConfigurations().getConfigValue("P8USERID"));
			String passwd = decryptString(getConfigurations().getConfigValue("P8PASSWORD"));
			Subject subject = UserContext.createSubject(conn, userid, passwd,stanza);
			UserContext uc = UserContext.get();
			uc.pushSubject(subject); 
		}
		return conn; 
	}
	
	public Domain getDomain() throws Exception
	{
		Connection conn = getConnection();
		String domainName = null; 
		Domain domain = Factory.Domain.fetchInstance(conn, domainName, null);
		return domain;
	}
	
//	public Connection getPowerConnection() throws Exception {
//		
//		String uri = getConfigurations().getConfigValue("P8MTOM");
//		Connection conn = Factory.Connection.getConnection(uri);
//		String p8Auth = "STANDALONE";
//		if(p8Auth.equalsIgnoreCase("STANDALONE"))
//		{
//			String stanza = getConfigurations().getConfigValue("P8STANZA");
//			String userid = decryptString(getConfigurations().getConfigValue("P8USERID"));
//			String passwd = decryptString(getConfigurations().getConfigValue("P8PASSWORD"));
//			Subject subject = UserContext.createSubject(conn, userid, passwd, stanza);
//			UserContext uc = UserContext.get();
//			uc.pushSubject(subject); 
//		}
//		return conn; 
//	}
//	
//	public Domain getPowerDomain() throws Exception
//	{
//		Connection conn = getPowerConnection();
//		String domainName = null; 
//		Domain domain = Factory.Domain.fetchInstance(conn, domainName, null);
//		return domain;
//	}
	
	private static String decryptString(String inString) throws Exception {
		ECMEncryption enc = new ECMEncryption();
		return enc.getDecryptedString(encKey, inString);
	}
}
