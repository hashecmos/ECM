package com.ecm.db.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import com.ecm.db.transport.TUserSetting;
import com.ecm.db.util.DBUtil;

public class ECMUserSetting {
	
	private long settingID ;
	private String appID;
	private long empNo;
	private String settingKey;
	private String settingValue;

	public long getSettingID() {
		return settingID;
	}
	public void setSettingID(long settingID) {
		this.settingID = settingID;
	}
	public String getAppID() {
		return appID;
	}
	public void setAppID(String appID) {
		this.appID = appID;
	}
	public long getEmpNo() {
		return empNo;
	}
	public void setEmpNo(long emp) {
		this.empNo = emp;
	}
	public String getSettingKey() {
		return settingKey;
	}
	public void setSettingKey(String settingKey) {
		this.settingKey = settingKey;
	}
	public String getSettingValue() {
		return settingValue;
	}
	public void setSettingValue(String settingValue) {
		this.settingValue = settingValue;
	}

	

	public void load() throws Exception {

//		if (this.isLoaded)
//			return;

		Connection conn = null;
		ResultSet rs = null;
		String sqlQuery = null;
		PreparedStatement ps = null;
		try {
				conn = DBUtil.getECMDBConnection();
				sqlQuery = "SELECT * FROM ECM_USER_SETTING WHERE APPID=? AND EMPNO=? AND "
						+ "UPPER(KEYNAME)=TRIM(UPPER(?))";
				ps = conn.prepareStatement(sqlQuery);
				ps.setString(1, DBUtil.escapeString(this.appID));
				ps.setInt(2, (int)this.empNo);
				ps.setString(3, DBUtil.escapeString(this.settingKey));
				rs = ps.executeQuery();
				if (rs.next()) {
					this.setSettingID(rs.getInt("ID"));
					this.setSettingValue(rs.getString("KEYVALUE"));
					if(this.settingValue == null)
						this.setSettingValue("");
//					isLoaded = true;
				} else {
					System.out.println("ID not present");
				}
			
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			rs.close();
			ps.close();
			conn.close();
		}

	}
	

	private void insert() throws Exception
	{
		Connection conn = null;
		PreparedStatement ps = null;
		try {
			conn = DBUtil.getECMDBConnection();

			String getDBUSERSql = "INSERT INTO ECM_USER_SETTING(ID, APPID, EMPNO, KEYNAME, KEYVALUE) "
					+ "VALUES(ECM_USER_SETTING_SEQ.nextval,?,?,?,?)";
			ps = conn.prepareCall(getDBUSERSql);
			
			ps.setString(1, DBUtil.escapeString(this.appID));
			ps.setInt(2, (int)this.empNo);
			ps.setString(3, DBUtil.escapeString(this.settingKey));
			ps.setString(4, DBUtil.escapeString(this.settingValue));

			ps.executeUpdate();

			System.out.println(this.getSettingID());
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			ps.close();
			conn.close();
		}
	}
	
	private void update() throws Exception
	{
		Connection conn = null;
		PreparedStatement stmt = null;
		String sqlQuery = null;
		try {
			conn = DBUtil.getECMDBConnection();

			sqlQuery = "UPDATE ECM_USER_SETTING SET APPID=?, EMPNO=?, "
					+ "KEYNAME=?,KEYVALUE=? WHERE ID=?";
			stmt = conn.prepareStatement(sqlQuery);
			stmt.setString(1, DBUtil.escapeString(this.appID));
			stmt.setInt(2, (int)this.empNo);
			stmt.setString(3, DBUtil.escapeString(this.settingKey));
			stmt.setString(4, DBUtil.escapeString(this.settingValue));
			stmt.setInt(5, (int)this.settingID);
		
			stmt.executeUpdate();

		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			conn.close();
			stmt.close();
		}
	}
	
	public void save() throws Exception 
	{
		if(this.settingID <= 0)
			insert();
		else
			update();
	}
	
	public void delete() throws Exception
	{
		Connection conn = null;
		PreparedStatement stmt = null;
		String sqlQuery= null;
		
		try {
			conn = DBUtil.getECMDBConnection();
		    sqlQuery="Delete from  ECM_USER_SETTING where ID=?";		
			stmt = conn.prepareStatement(sqlQuery);
			stmt.setInt(1, (int)this.settingID);
		    stmt.execute();	
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		}	finally {	
			conn.close();
			stmt.close();
		}
	}
	
	public TUserSetting getTransport() throws Exception
	{
		load();
		
		TUserSetting tUStng = new TUserSetting();
    
		tUStng.id=this.settingID;
		tUStng.appId=this.appID;
		tUStng.key=this.settingKey;
		tUStng.empNo=this.empNo;
		tUStng.val=this.settingValue;
		
		return tUStng;
	}
	
	public void getFromTransport(TUserSetting set) throws Exception
	{    
		this.settingID = set.id;;
		this.appID = set.appId;
		this.settingKey = set.key;
		this.empNo = set.empNo;
		this.settingValue = set.val;
	}
	
	
	
}