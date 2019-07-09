package com.ecm.db.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.ecm.db.list.ECMConfigurationList;
import com.ecm.db.transport.TAdminEmailSet;
import com.ecm.db.transport.TConfiguration;
import com.ecm.db.util.DBUtil;
import com.ecm.db.util.ECMAdminLogger;


public class ECMConfiguration {
	
	private long id;
	private String keyName;
	private String keyValue;
	private String appId;
	private String configScope;
	private String keyDesc;
	private String createdBy;
	private String createdDate;
	private String modifiedBy;
	private String modifiedDate;
	
	public String getAppId() {
		return appId;
	}
	public void setAppId(String appId) {
		this.appId = appId;
	}

	private boolean isLoaded=false;

	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getKeyName() {
		return keyName;
	}
	public void setKeyName(String keyName) {
		this.keyName = keyName;
	}
	public String getValue() {
		return keyValue;
	}
	public String getKeyDesc() {
		return keyDesc;
	}
	public String getModifiedBy() {
		return modifiedBy;
	}
	public String getModifiedDate() {
		return modifiedDate;
	}
	public String getCreatedDate() {
		return createdDate;
	}
	public void setCreatedDate(String createdDate) {
		this.createdDate = createdDate;
	}
	public void setModifiedDate(String modifiedDate) {
		this.modifiedDate = modifiedDate;
	}
	public String getCreatedBy() {
		return createdBy;
	}
	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}
	public void setModifiedBy(String modifiedBy) {
		this.modifiedBy = modifiedBy;
	}
	public void setKeyDesc(String keyDesc) {
		this.keyDesc = keyDesc;
	}
	public void setValue(String valueName) {
		this.keyValue = valueName;
	}
	public String getConfigScope() {
		return this.configScope;
	}
	public void setConfigScope(String scopeName) {
		this.configScope = scopeName;
	}
	
	public void setLoaded(){
		this.isLoaded=true;
	}

	public void load() throws Exception{
		
		if (this.isLoaded)
			return;

		Connection conn = null;
		ResultSet rs = null;
		String sqlQuery = null;
		PreparedStatement ps = null;

		try {
			conn = DBUtil.getECMDBConnection();
			sqlQuery = "SELECT KeyValue FROM ECM_CONFIGURATION WHERE "
					+ "AppId = ? AND Scope = ? AND KeyName = ?";
			ps = conn.prepareStatement(sqlQuery);
			ps.setString(1, this.appId);
			ps.setString(2, this.configScope);
			ps.setString(3, this.keyName);
			rs = ps.executeQuery();
			if (rs.next()) {
				this.setValue(rs.getString("KeyValue"));
				isLoaded = true;
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

	private void update() throws Exception
	{
		Connection conn = null;
		String sqlQuery = null;
		PreparedStatement ps = null;
		try {
			conn = DBUtil.getECMDBConnection();
			ECMConfiguration config =ECMConfigurationList.getInstance(this.appId,this.configScope).getECMConfigurationsById(this.id);
			sqlQuery = "UPDATE ECM_CONFIGURATION SET KeyValue = ?, ModifiedDate = SYSTIMESTAMP, ModifiedBy = ?, KeyDesc = ? WHERE "
					+ "AppId = ? AND Scope = ? AND KeyName = ?";
			ps = conn.prepareStatement(sqlQuery);

			ps.setString(1, DBUtil.escapeString(this.keyValue));
			ps.setString(2, DBUtil.escapeString(this.modifiedBy));
			ps.setString(3, DBUtil.escapeString(this.keyDesc));
			ps.setString(4, DBUtil.escapeString(this.appId));
			ps.setString(5, DBUtil.escapeString(this.configScope));
			ps.setString(6, DBUtil.escapeString(this.keyName));
			
			ps.executeUpdate();
			
			if(!(sendEmail("Updated",config)))
			{
				//New method to send email later
			}
			
			ECMConfigurationList cfgList = ECMConfigurationList.getInstance(this.appId, this.configScope);
			String xValue = cfgList.getConfigValue(this.keyName);
			ECMAdminLogger.getInstance("Config").info(this.appId + "_" + this.configScope + "_" + this.keyName , 
                    this.modifiedBy , "The Configuration is updated with value '" + this.keyValue + "' from existing value - '" + xValue + "'" );

		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			conn.close();
			ps.close();
		}
			
	}
	private Boolean sendEmail(String action,ECMConfiguration config){
		try {
			ECMMailManager em=new ECMMailManager();
			TAdminEmailSet taes=new TAdminEmailSet();
            taes.value=config.keyValue;
            taes.description=config.keyDesc;
			taes.keyName=this.keyName;
			taes.keyValue=this.keyValue;
			taes.keyDesc=this.keyDesc;
			taes.modifiedBy=this.modifiedBy;
			taes.modifiedDate=DBUtil.getTodayDate();
			em.sendEmail("CONFIG", taes, action);
			
			return true;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}

	
	private void insert() throws Exception{
		
		Connection conn = null;
		PreparedStatement ps = null;
		String sqlQuery = null;
		try {
			conn = DBUtil.getECMDBConnection();

			sqlQuery = "INSERT TNTO ECM_CONFIGURATION (ID, AppId, Scope, KeyName, KeyValue,"
					+ "CreatedDate, ModifiedDate, CreatedBy, ModifiedBy, KeyDesc) "
					+ "VALUES (ECM_CONFIGURATION_SEQ.NEXTVAL, ?,?,?,?, SYSTIMESTAMP, SYSTIMESTAMP, ?, ?, ?)";
			ps = conn.prepareStatement(sqlQuery);
			ps.setString(1, DBUtil.escapeString(this.appId));
			ps.setString(2, DBUtil.escapeString(this.configScope));
			ps.setString(3, DBUtil.escapeString(this.keyName));
			ps.setString(4, DBUtil.escapeString(this.keyValue));
			ps.setString(5, DBUtil.escapeString(this.createdBy));
			ps.setString(6, DBUtil.escapeString(this.modifiedBy));
			ps.setString(7, DBUtil.escapeString(this.keyDesc));
			ps.executeUpdate();

			ECMAdminLogger.getInstance("Config").info(this.appId + "_" + this.configScope + "_" + this.keyName , 
					this.modifiedBy , "Added New Configuration with value " + this.keyValue );
			
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			ps.close();
			conn.close();
		}
	}
	
	public void save() throws Exception
	{
		if(this.id <= 0)
			insert();
		else
			update();
	}

	public void delete() throws SQLException {

		Connection conn = null;
		PreparedStatement ps = null;
		String sqlQuery = null;

		try {
			conn = DBUtil.getECMDBConnection();
			sqlQuery = "DELETE FROM ECM_CONFIGURATION WHERE ID = ?";
			ps = conn.prepareStatement(sqlQuery);
			ps.setInt(1, (int)this.id);
			ps.executeUpdate();

		} catch (Exception e) {
			e.printStackTrace();
		} finally {

			conn.close();
			ps.close();
		}
		
	}
	
	public TConfiguration getTransport() throws Exception {

		load();
		if (!isLoaded)
			return null;

		TConfiguration tconfig = new TConfiguration();
		tconfig.id=this.id;
		tconfig.name=this.keyName;
		tconfig.value=this.keyValue;
		tconfig.scope=this.configScope;
		tconfig.appId=this.appId;
		tconfig.desc=this.keyDesc;
		return tconfig;

	}

	public void getFromTransport(TConfiguration tconfig) throws Exception {
		this.setId(tconfig.id);
		this.setKeyName(tconfig.name);
		this.setValue(tconfig.value);
		this.setConfigScope(tconfig.scope);
		this.setAppId(tconfig.appId);
		this.setCreatedBy(tconfig.empName);
		this.setModifiedBy(tconfig.empName);
		this.setKeyDesc(tconfig.desc);
	}

		
		
}
	

