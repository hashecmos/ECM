package com.ecm.db.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import com.ecm.db.list.ECMAdministrationList;
import com.ecm.db.transport.TAccessPolicy;
import com.ecm.db.transport.TAcessPolicyMapping;
import com.ecm.db.transport.TAdminEmailSet;
import com.ecm.db.util.DBUtil;


public class ECMAccessPolicy {
	private long id;
	private String objectId;
	private String name;
	private long orgUnitId;
	private boolean isLoaded=false;
	private String type;
	private String createdBy;
	private String createdDate;
	private String modifiedBy;
	private String modifiedDate;

	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public long getOrgUnitId() {
		return orgUnitId;
	}
	public void setOrgUnitId(long id) {
		this.orgUnitId = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getObjectId() {
		return objectId;
	}
	public void setObjectId(String obj) {
		this.objectId = obj;
	}
	public String getCreatedBy() {
		return createdBy;
	}
	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}
	public String getCreatedDate() {
		return createdDate;
	}
	public void setCreatedDate(String createdDate) {
		this.createdDate = createdDate;
	}
	public String getModifiedBy() {
		return modifiedBy;
	}
	public void setModifiedBy(String modifiedBy) {
		this.modifiedBy = modifiedBy;
	}
	public String getModifiedDate() {
		return modifiedDate;
	}
	public void setModifiedDate(String modifiedDate) {
		this.modifiedDate = modifiedDate;
	}
	public void setLoaded(){
		this.isLoaded=true;
		
	}
	
	public void load() throws Exception{
		if(isLoaded)
			return;
		
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		String sqlQuery = null;
		try {
			conn = DBUtil.getECMDBConnection();
			sqlQuery = "SELECT * from ECM_ACCESS_POLICY WHERE ID=?";
			stmt = conn.prepareStatement(sqlQuery);
			stmt.setInt(1, (int)this.id);
			rs = stmt.executeQuery();
			if (rs.next()) {
				//create object and set properties
				this.name = rs.getString("Name");
				this.objectId = rs.getString("ObjectID");
				this.orgUnitId = rs.getInt("OrgUnitID");
				this.type = rs.getString("Type");
				this.createdBy = rs.getString("CreatedBy");
				this.createdDate = rs.getString("CreatedDate");
				this.modifiedBy = rs.getString("ModifiedBy");
				this.modifiedDate = rs.getString("ModifiedDate");
				
				this.setLoaded();
			}  
			
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			rs.close();
			stmt.close();
			conn.close();
		}
	}
		
	private void update() throws Exception{
		Connection conn = null;
		PreparedStatement stmt = null;
		String sqlQuery = null;
		try {
			conn = DBUtil.getECMDBConnection();
			sqlQuery = "UPDATE ECM_ACCESS_POLICY SET Name = ?, OrgUnitID = ?, Type = ?, ModifiedBy = ?, ModifiedDate = SYSTIMESTAMP WHERE ID = ? ";
			stmt = conn.prepareStatement(sqlQuery);
			stmt.setString(1, DBUtil.escapeString(this.name));
			stmt.setInt(2, (int)this.orgUnitId);
			stmt.setString(3, DBUtil.escapeString(this.type));
			stmt.setString(4, DBUtil.escapeString(this.modifiedBy));
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
	
	
	public void update(String modifyUser) throws Exception{
		Connection conn = null;
		PreparedStatement stmt = null;
		String sqlQuery = null;
		try {
			conn = DBUtil.getECMDBConnection();
			sqlQuery = "UPDATE ECM_ACCESS_POLICY SET ModifiedBy = ?, ModifiedDate = SYSTIMESTAMP WHERE ID = ? ";
			stmt = conn.prepareStatement(sqlQuery);
			stmt.setString(1, DBUtil.escapeString(modifyUser));
			stmt.setInt(2, (int)this.id);
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
		PreparedStatement stmt = null;
		String sqlQuery = null;
		try {
			conn = DBUtil.getECMDBConnection();
			sqlQuery = "INSERT INTO ECM_ACCESS_POLICY (ID, Name, ObjectID, OrgUnitID, Type, CreatedBy, CreatedDate, ModifiedBy, ModifiedDate) "
					+ "VALUES (ECM_ACCESS_POLICY_SEQ.NEXTVAL, ?,?,?,?,?,SYSTIMESTAMP,?,SYSTIMESTAMP) ";

			stmt = conn.prepareStatement(sqlQuery);
			stmt.setString(1, DBUtil.escapeString(this.name));
			stmt.setString(2, DBUtil.escapeString(this.objectId));
			stmt.setInt(3, (int)this.orgUnitId);
			stmt.setString(4,DBUtil.escapeString(this.type));
			stmt.setString(5,DBUtil.escapeString(this.createdBy));
			stmt.setString(6,DBUtil.escapeString(this.createdBy));
			stmt.executeUpdate();
			
			if(!(sendEmail("Added")))
			{
				//New method to send email later
			}
			
			
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			stmt.close();
			conn.close();
		}
	}
	private Boolean sendEmail(String action){
		try {
			ECMMailManager em=new ECMMailManager();
			TAdminEmailSet taes=new TAdminEmailSet();
			
			taes.name=this.name;
			taes.orgUnitName = ECMAdministrationList.getInstance().getOrgCodeFromUnitID(this.orgUnitId);
			taes.type=this.type;
			taes.createdBy=this.createdBy;
			taes.createdDate=this.createdDate;
			em.sendEmail("AP", taes, action);
			
			return true;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}
	
	public void save() throws Exception{
		
		if(this.id<=0)
			insert();
		else
			update();	
	}

	public void delete() throws Exception{
		Connection conn = null;
		PreparedStatement stmt = null;
		String sqlQuery = null;
		try {
			conn = DBUtil.getECMDBConnection();
			sqlQuery = "DELETE FROM ECM_ACCESS_POLICY WHERE ID = ? ";
			stmt = conn.prepareStatement(sqlQuery);
			stmt.setInt(1, (int)this.id);
			stmt.executeUpdate();
			
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			stmt.close();
			conn.close();
		}
	}
	
	public TAccessPolicy getTransport(boolean withValues) throws Exception {

		load();
		if (!isLoaded)
			return null;

		TAccessPolicy tap = new TAccessPolicy();
		tap.id = this.id;
		tap.name = this.name;
		tap.orgUnitId = this.orgUnitId;
		tap.objectId = this.objectId;
		tap.type = this.type;
		tap.createdBy = this.createdBy;
		tap.createdDate = this.createdDate;
		tap.modifiedBy = this.modifiedBy;
		tap.modifiedDate = this.modifiedDate;
		return tap;

	}

	public void getFromTransport(TAccessPolicy tap) throws Exception {
		this.id = tap.id;
		this.name = tap.name;
		this.objectId = tap.objectId;
		this.orgUnitId = tap.orgUnitId;
		this.type = tap.type;
		this.createdBy = tap.createdBy;
		this.createdDate = tap.createdDate;
		this.modifiedBy = tap.modifiedBy;
		this.modifiedDate = tap.modifiedDate;
	}

	private boolean isMapped(Connection conn, String etID, long orgUnitID) throws Exception {
		PreparedStatement stmt = null;
		String sqlQuery = null;
		ResultSet rs = null;
		try {
			sqlQuery = "SELECT ID FROM ECM_ACCESS_POLICY_MAPPING WHERE "
					+ "ORGUNITID = ? AND ENTRYTEMPLATEID = ? AND PROPNAME IS NULL";
			stmt = conn.prepareStatement(sqlQuery);
			stmt.setInt(1, (int)orgUnitID);
			stmt.setString(2, DBUtil.escapeString(etID));
			
			rs = stmt.executeQuery();
			if((rs != null) && rs.next())
				return true;
			return false;
		} catch (Exception ex) {
			return false;
		} finally {
			rs.close();
			stmt.close();
		}
	}

	private Boolean sendEmail(long id, long orgUnitId, String etID) {
		try {
			ECMMailManager em = new ECMMailManager();
			TAdminEmailSet taes = new TAdminEmailSet();
			taes.id = orgUnitId;
			taes.etId = etID;
			taes.name = name;
			taes.orgUnitName = ECMAdministrationList.getInstance().getOrgCodeFromUnitID(orgUnitId);
			// taes.etName
			em.sendEmail("APM", taes, "Mapped");

			return true;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}
	
	public String addMapping(String etID) throws Exception{
		load();
		String strMsg = "";
		Connection conn = null;
		PreparedStatement stmt = null;
		String sqlQuery = null;
		try {
			
			conn = DBUtil.getECMDBConnection();
			if(isMapped(conn, etID, this.orgUnitId)) {
				sqlQuery = "UPDATE ECM_ACCESS_POLICY_MAPPING SET Policy4 = ? "
						+ "WHERE EntryTemplateID = ? AND OrgUnitID = ? AND "
						+ "PROPNAME IS NULL";
				strMsg = "Mapping exists";
			} else {
				sqlQuery = "INSERT INTO ECM_ACCESS_POLICY_MAPPING(ID, POLICY4, "
						+ "ENTRYTEMPLATEID, ORGUNITID) VALUES(ECM_ACCESS_POLICY_MAPPING_SEQ.NEXTVAL,"
						+ "?,?,?)";
				strMsg = "OK";
				if(!sendEmail( id,orgUnitId,etID))
				{
					//New method to send email later
				}
			}
			stmt = conn.prepareStatement(sqlQuery);
			stmt.setInt(1, (int)this.id);
			stmt.setString(2, DBUtil.escapeString(etID));
			stmt.setInt(3, (int)this.orgUnitId);
			stmt.executeUpdate();
			
			
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			stmt.close();
			conn.close();
		}
		return strMsg;
	}
}
