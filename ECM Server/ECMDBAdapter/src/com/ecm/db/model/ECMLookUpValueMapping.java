package com.ecm.db.model;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.ecm.db.transport.TLookUpValueMapping;
import com.ecm.db.util.DBUtil;

public class ECMLookUpValueMapping {
	
	private long id;
	private long orgUnitId;
	private long lookUp;
	private String templateId;
	private String property;
	private boolean isLoaded=false;
	
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public long getOrgUnitId() {
		return orgUnitId;
	}
	public void setOrgUnitId(long orgUnitId) {
		this.orgUnitId = orgUnitId;
	}
	public long getLookUp() {
		return lookUp;
	}
	public void setLookUp(long lookUp) {
		this.lookUp = lookUp;
	}

	public String getTemplateId() {
		return templateId;
	}
	public void setTemplateId(String templateId) {
		this.templateId = templateId;
	}
	public String getProperty() {
		return property;
	}
	public void setProperty(String property) {
		this.property = property;
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
		CallableStatement callableStatement = null;

		try {
			if (this.id > 0) {
				conn = DBUtil.getECMDBConnection();
				sqlQuery = "SELECT * FROM ECM_LOOKUP_MAPPING WHERE ID = ?";
				callableStatement = conn.prepareCall(sqlQuery);
				callableStatement.setInt(1, (int)this.id);
				callableStatement.executeQuery();
				rs = callableStatement.getResultSet();
				if (rs.next()) {
					this.setOrgUnitId(rs.getInt("ORGUNITID"));
					this.setLookUp(rs.getInt("LOOKUP"));
					this.setTemplateId(rs.getString("TEMPLATEID"));
					this.setProperty(rs.getString("PROPERTY"));

					isLoaded = true;
				}

				else {
					System.out.println("ID not present");
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			rs.close();
			callableStatement.close();
			conn.close();
		}

	}

	private void update() throws Exception{
		Connection conn = null;
		String sqlQuery = null;
		PreparedStatement stmt = null;
		try {
			conn = DBUtil.getECMDBConnection();
			sqlQuery = "ECM_LOOKUP_MAPPING SET ORGUNITID = ?, LOOKUPID=?,"
					+ "TEMPLATEID=?,PROPERTY=? WHERE ID = ?";
			stmt = conn.prepareStatement(sqlQuery);
			
			stmt.setInt(1, (int)this.orgUnitId);
			stmt.setInt(2, (int)this.lookUp);
			stmt.setString(3, DBUtil.escapeString(this.templateId));
			stmt.setString(4, DBUtil.escapeString(this.property));
			stmt.setInt(5, (int)this.id);
			stmt.executeUpdate();

		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			stmt.close();
			conn.close();
		}
		
	}
	private void insert() throws Exception{
		Connection conn = null;
		CallableStatement callableStatement = null;
		String sqlQuery = null;
		try {
			conn = DBUtil.getECMDBConnection();

			sqlQuery = "INSERT INTO ECM_LOOKUP_MAPPING(ID, ORGUNITID, LOOKUPID,"
					+ "TEMPLATEID, PROPERTY) VALUES(ECM_LOOKUP_MAPPING_SEQ.NEXTVAL,"
					+ "?,?,?,?)";
			callableStatement = conn.prepareCall(sqlQuery);
			
			callableStatement.setInt(1, (int)this.orgUnitId);
			callableStatement.setInt(2, (int)this.lookUp);
			callableStatement.setString(3, DBUtil.escapeString(this.templateId));
			callableStatement.setString(4, DBUtil.escapeString(this.property));

			callableStatement.executeUpdate();

		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			callableStatement.close();
			conn.close();
		}
		
		
	}
	public void save() throws Exception{	
		if(this.id<=0)
			insert();
		update();
	}

	public void delete() throws SQLException{
		Connection conn = null;
		CallableStatement callableStatement = null;
		String sqlQuery = null;

		try {
			conn = DBUtil.getECMDBConnection();
			sqlQuery = "DELETE FROM ECM_LOOKUP_MAPPING WHERE ID = ?";
			callableStatement = conn.prepareCall(sqlQuery);
			callableStatement.setInt(1, (int)this.id);
			callableStatement.executeUpdate();

		} catch (Exception e) {
			e.printStackTrace();
		} finally {

			conn.close();
			callableStatement.close();
		}
		
	}
	public TLookUpValueMapping getTransport() throws Exception {

		load();
		if (!isLoaded)
			return null;

		TLookUpValueMapping tLukUpMap = new TLookUpValueMapping();
		tLukUpMap.id = this.id;
		tLukUpMap.lkUp=this.lookUp;
		tLukUpMap.orgUId=this.orgUnitId;
		tLukUpMap.prop=this.property;
		tLukUpMap.tmpId=this.templateId;

		return tLukUpMap;

	}

	public void getFromTransport(TLookUpValueMapping tLukUpMap) throws Exception {
		this.setId(tLukUpMap.id);
		this.setLookUp(tLukUpMap.lkUp);
		this.setOrgUnitId(tLukUpMap.orgUId);
		this.setProperty(tLukUpMap.prop);
		this.setTemplateId(tLukUpMap.tmpId);

	}

	

}
