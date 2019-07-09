package com.ecm.db.util;

import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

import com.ecm.db.list.ECMConfigurationList;
import com.ecm.db.model.ECMConfiguration;
import com.ecm.db.transport.TLog;

public class ECMLogger {
	private String lContext = "None";
	private long logLevel = 4;
	private long logRetention = 30;
	
	private ECMLogger(Object className) { 
		lContext = className.toString();
		if(lContext == null)
			lContext = "";
		lContext = safeTruncate(lContext, 200);
		getLogConfigurations();
		clearLogs();
	}
	
	public static ECMLogger getInstance(Object className) {
		return new ECMLogger(className);
	}
	
	public void debug(String message) {
		if(this.logLevel <= 1)
			saveLog(1, message);
	}
	 
	 public void info(String message) {
		 if(this.logLevel <= 2)
			 saveLog(2, message);
	 }
	 
	 public void error(String message) {
		 if(this.logLevel <= 4)
			 saveLog(4, message);
	 }
	 
	 public void warning(String message) {
		 if(this.logLevel <= 3)
			 saveLog(3, message);
	 }
	 
	 public void fatal(String message) {
		 if(this.logLevel <= 5)
			 saveLog(5, message);
	 }
	
	public void debug(String message, long empNo) {
		if(this.logLevel <= 1)
			saveLog(1, message, empNo);
	}
	 
	 public void info(String message, long empNo) {
		 if(this.logLevel <= 2)
			 saveLog(2, message, empNo);
	 }
	 
	 public void error(String message, long empNo) {
		 if(this.logLevel <= 4)
			 saveLog(4, message, empNo);
	 }
	 
	 public void warning(String message, long empNo) {
		 if(this.logLevel <= 3)
			 saveLog(3, message, empNo);
	 }
	 
	 public void fatal(String message, long empNo) {
		 if(this.logLevel <= 5)
			 saveLog(5, message, empNo);
	 }
	 
	 public void logException(Exception e) {
		 if(this.logLevel <= 4)
			 error(e.getMessage() + "\n" + e.getStackTrace(), 0);
	 }
	 
	 private void saveLog(long logType, String message) {
		 try {
			Connection conn = null;
			PreparedStatement ps = null;
			Clob itemClob = null;
			try {
				conn = DBUtil.getECMDBConnection();
				String sqlQuery = "INSERT INTO ECM_LOG "
						+ "(ID, TYPE, SUMMARY, LOGDATE, DETAILS, CONTEXT, SERVERNAME, APPNAME, USERNAME) "
						+ "VALUES (ECM_LOG_SEQ.NEXTVAL, ?, ?, SYSDATE, ?, ?, ?, ?, ?)";
				ps = conn.prepareStatement(sqlQuery);
				
				if(message == null)
					message = "";
				itemClob = oracle.sql.CLOB.createTemporary(
					      conn, false, oracle.sql.CLOB.DURATION_SESSION);
				itemClob.setString(1, message);
				String summary = safeTruncate(message, 200);
				ps.setInt(1, (int)(logType));
				ps.setString(2, DBUtil.escapeString(summary));
				ps.setClob(3, itemClob);
				ps.setString(4, DBUtil.escapeString(lContext));
				ps.setString(5, "MVCSECMTESTICN");
				ps.setString(6, "ECMAPP");
				ps.setString(7, "ECMAPP");

				ps.executeUpdate();
				itemClob.free();
			} catch (Exception e) {
				e.printStackTrace();
				throw new Exception(e.getMessage());
			} finally {
				ps.close();
				conn.close();
			}
		 } catch (Exception ex) {}
	 }
	 
	 private void saveLog(long logType, String message, long empNo) {
		 try {
			Connection conn = null;
			PreparedStatement ps = null;
			Clob itemClob = null;
			try {
				conn = DBUtil.getECMDBConnection();
				String sqlQuery = "INSERT INTO ECM_LOG "
						+ "(ID, TYPE, SUMMARY, LOGDATE, DETAILS, CONTEXT, SERVERNAME, APPNAME, USERNAME) "
						+ "VALUES (ECM_LOG_SEQ.NEXTVAL, ?, ?, SYSDATE, ?, ?, ?, ?, ?)";
				ps = conn.prepareStatement(sqlQuery);
				
				if(message == null)
					message = "";
				itemClob = oracle.sql.CLOB.createTemporary(
					      conn, false, oracle.sql.CLOB.DURATION_SESSION);
				itemClob.setString(1, message);
				String summary = safeTruncate(message, 200);
				ps.setInt(1, (int)(logType));
				ps.setString(2, DBUtil.escapeString(summary));
				ps.setClob(3, itemClob);
				ps.setString(4, DBUtil.escapeString(lContext));
				ps.setString(5, "MVCSECMTESTICN");
				ps.setString(6, "ECMAPP");
				ps.setString(7, DBUtil.intToString((int)empNo));

				ps.executeUpdate();
				itemClob.free();
			} catch (Exception e) {
				e.printStackTrace();
				throw new Exception(e.getMessage());
			} finally {
				ps.close();
				conn.close();
			}
		 } catch (Exception ex) {}
	 }
	 
	 private String getLogType(int lType) {
		 switch(lType) {
			 case 1: return "Debug";
			 case 2: return "Info";
			 case 3: return "Warning";
			 case 4: return "Error";
			 case 5: return "Fatal";
		 }
		 return "Unknown";
	 }
	 
	 public ArrayList<TLog> getLogs() throws Exception {
		 Connection conn = null;
		 PreparedStatement ps = null;
		 ResultSet rs = null;
		 ArrayList<TLog> logList = new ArrayList<TLog>();
		 try {
			 conn = DBUtil.getECMDBConnection();
			 String sqlQuery = "SELECT ID, TYPE, SUMMARY, LOGDATE, CONTEXT, SERVERNAME, APPNAME FROM ECM_LOG ORDER BY LOGDATE DESC";
			 ps = conn.prepareStatement(sqlQuery);
	
			 rs = ps.executeQuery();
			 while (rs.next()) {
				 TLog log = new TLog();
				 log.id = rs.getInt(1);
				 log.type = getLogType(rs.getInt(2));
				 log.summary = rs.getString(3);
				 log.timeStamp = DBUtil.formatDateForLog(rs.getTimestamp(4));
				 log.context = rs.getString(5);
				 log.servername = rs.getString(6);
				 log.appname = rs.getString(7);
				
				 logList.add(log);
			 }
			 return logList;
		 } catch (Exception e) {
			 e.printStackTrace();
			 throw new Exception(e.getMessage());
		 } finally {
			 rs.close();
			 ps.close();
			 conn.close();
		 }
	 }
	 
	 public String getLogDetails(long logid) throws Exception {
		 Connection conn = null;
		 PreparedStatement ps = null;
		 ResultSet rs = null;
		 Clob itemClob = null;
		 try {
			 conn = DBUtil.getECMDBConnection();
			 String sqlQuery = "SELECT DETAILS FROM ECM_LOG WHERE ID = ?";
			 ps = conn.prepareStatement(sqlQuery);
		
			 ps.setInt(1,  (int)logid);
	
			 rs = ps.executeQuery();
			 if (rs.next()) {
				 itemClob = rs.getClob(1);
				 String details = DBUtil.clobToString(itemClob);
				 itemClob.free();
				 return details;
			 }
			 return "";
		 } catch (Exception e) {
			 e.printStackTrace();
			 throw new Exception(e.getMessage());
		 } finally {
			 rs.close();
			 ps.close();
			 conn.close();
		 }
	 }
	 
	 private void getLogConfigurations() {
		 try {
			 ArrayList<ECMConfiguration> cList = ECMConfigurationList.getInstance("ECM", "LOG").getConfigurations();
			 for(ECMConfiguration cfg: cList) {
				 if(cfg.getKeyName().trim().equalsIgnoreCase("LOGRETENTION")) {
					 this.logRetention = DBUtil.stringToLongDefault(cfg.getValue(), 30);
				 }
				 if(cfg.getKeyName().trim().equalsIgnoreCase("LOGLEVEL")) {
					 this.logLevel = DBUtil.stringToLongDefault(cfg.getValue(), 4);
				 }
			 }
		 } catch (Exception e) {}
	 }
	 
	 private void clearLogs() {
		 try {
			 Connection conn = null;
			 PreparedStatement ps = null;
			 try {
				 conn = DBUtil.getECMDBConnection();
				 String sqlQuery = "DELETE FROM ECM_LOG WHERE LOGDATE < SYSDATE - ?";
				 ps = conn.prepareStatement(sqlQuery);
			
				 ps.setInt(1,  (int)this.logRetention);
		
				 ps.executeUpdate();
			 } catch (Exception e) {
				 e.printStackTrace();
				 throw new Exception(e.getMessage());
			 } finally {
				 ps.close();
				 conn.close();
			 }
		 } catch (Exception e){}
	 }
	 
	 private String safeTruncate(String inString, int length) {
		 try {
			 return inString.substring(0, length);
		 } catch(Exception e){}
		 return inString;
	 }
}
