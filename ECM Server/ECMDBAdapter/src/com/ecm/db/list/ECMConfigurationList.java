package com.ecm.db.list;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

import com.ecm.db.model.ECMConfiguration;
import com.ecm.db.transport.TConfiguration;
import com.ecm.db.util.DBUtil;

public class ECMConfigurationList {
	
	private ArrayList<ECMConfiguration> configList = null;
	private String appID;
	private String configScope;
	
	public static ECMConfigurationList getInstance(String appID, String configScope)
	{
		return new ECMConfigurationList(appID, configScope);
	}
	
	private ECMConfigurationList(String appID, String configScope)
	{
		this.appID = appID.toUpperCase();
		this.configScope = configScope.toUpperCase();
	}
	
	public static ECMConfigurationList getInstanceForUpdate()
	{
		return new ECMConfigurationList();
	}
	
	private ECMConfigurationList()
	{
	}
	
	public ArrayList<ECMConfiguration> getConfigurations() throws Exception
	{
		if(configList != null)
			return configList;
		
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		String sqlQuery = null;
		try {
			configList = new ArrayList<ECMConfiguration>();
			conn = DBUtil.getECMDBConnection();

			sqlQuery = "SELECT * from ECM_CONFIGURATION WHERE AppId=?"+
					" AND Scope=?";
			stmt = conn.prepareStatement(sqlQuery);
			stmt.setString(1, DBUtil.escapeString(this.appID));
			stmt.setString(2, DBUtil.escapeString(this.configScope));

			rs = stmt.executeQuery();
			while (rs.next()) {
				ECMConfiguration ec = new ECMConfiguration();
				ec.setId(rs.getInt("ID"));
				ec.setAppId(this.appID);
				ec.setConfigScope(this.configScope);
				ec.setKeyName(rs.getString("KeyName"));
				ec.setValue(rs.getString("KeyValue"));
				
				//System.out.println(rs.getString("KeyValue"));
				configList.add(ec);
			}  
			return configList;
			
		} catch (Exception e) {
			configList = null;
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			rs.close();
			stmt.close();
			conn.close();
		}
	}
	public ECMConfiguration getECMConfigurationsById(long id) throws Exception
	{
		ECMConfiguration ec = new ECMConfiguration();
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		String sqlQuery = null;
		try {
			conn = DBUtil.getECMDBConnection();
			sqlQuery = "SELECT * from ECM_CONFIGURATION WHERE ID =? ";
					
			stmt = conn.prepareStatement(sqlQuery);
			stmt.setInt(1, (int)id);

			rs = stmt.executeQuery();
			while (rs.next()) {
				ec.setId(rs.getInt("ID"));
				ec.setAppId(this.appID);
				ec.setConfigScope(this.configScope);
				ec.setKeyName(rs.getString("KeyName"));
				ec.setValue(rs.getString("KeyValue"));
				ec.setCreatedBy(rs.getString("CreatedBy"));
				ec.setModifiedBy(rs.getString("ModifiedBy"));
				ec.setModifiedDate(DBUtil.convertDateToString(rs.getTimestamp("ModifiedDate")));
				ec.setCreatedDate(DBUtil.convertDateToString(rs.getTimestamp("CreatedDate")));
				ec.setKeyDesc(rs.getString("KeyDesc"));
			}  
			return ec;
			
		} catch (Exception e) {
			configList = null;
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			rs.close();
			stmt.close();
			conn.close();
		}
	}

	
	public ArrayList<ECMConfiguration> getAllECMConfigurations() throws Exception
	{
		if(configList != null)
			return configList;
		
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		String sqlQuery = null;
		try {
			configList = new ArrayList<ECMConfiguration>();
			conn = DBUtil.getECMDBConnection();

			sqlQuery = "SELECT * from ECM_CONFIGURATION WHERE AppId=? "
					+ " AND Scope=?";
			stmt = conn.prepareStatement(sqlQuery);
			stmt.setString(1, DBUtil.escapeString(this.appID));
			stmt.setString(2, DBUtil.escapeString(this.configScope));

			rs = stmt.executeQuery();
			while (rs.next()) {
				ECMConfiguration ec = new ECMConfiguration();
				ec.setId(rs.getInt("ID"));
				ec.setAppId(this.appID);
				ec.setConfigScope(this.configScope);
				ec.setKeyName(rs.getString("KeyName"));
				ec.setValue(rs.getString("KeyValue"));
				ec.setCreatedBy(rs.getString("CreatedBy"));
				ec.setModifiedBy(rs.getString("ModifiedBy"));
				ec.setModifiedDate(DBUtil.convertDateToString(rs.getTimestamp("ModifiedDate")));
				ec.setCreatedDate(DBUtil.convertDateToString(rs.getTimestamp("CreatedDate")));
				ec.setKeyDesc(rs.getString("KeyDesc"));
				
				//System.out.println(rs.getString("KeyValue"));
				configList.add(ec);
			}  
			return configList;
			
		} catch (Exception e) {
			configList = null;
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			rs.close();
			stmt.close();
			conn.close();
		}
	}

	public String getConfigValue(String configKey)
	{
		try {
			getConfigurations();
			
			for(ECMConfiguration ec: configList)
			{
				if(ec.getKeyName().trim().toUpperCase().equals(configKey.trim().toUpperCase()))
					return ec.getValue();
			}
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}
	
	public void updateConfigurations(ArrayList<TConfiguration> cList) throws Exception {
		if(cList == null)
			return;
		for(TConfiguration set:cList) {
			ECMConfiguration eCfg = new ECMConfiguration();
			eCfg.getFromTransport(set);
			eCfg.save();
		}
	}
}
