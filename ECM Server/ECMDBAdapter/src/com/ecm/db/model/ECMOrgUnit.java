package com.ecm.db.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

import com.ecm.db.util.DBUtil;
import com.ecm.db.util.ECMLogger;
import com.ecm.db.transport.TAdminEmailSet;
import com.ecm.db.transport.TOrgUnit;

public class ECMOrgUnit {
	ECMLogger logger = ECMLogger.getInstance(ECMOrgUnit.class);
	
	private long id;

	private long parentId;
	private String HID;
	private String type;
	private String orgCode;
	private String description;
	private long head;
	private String createdDate;
	private String modifiedDate;
	private long headRoleId;
	private String headRoleName;
	
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getParentId() {
		return parentId;
	}

	public void setParentId(long parentId) {
		this.parentId = parentId;
	}

	public String getHID() {
		return HID;
	}

	public void setHID(String hID) {
		HID = hID;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getorgCode() {
		return orgCode;
	}

	public void setorgCode(String name) {
		this.orgCode = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public long getHead() {
		return head;
	}

	public void setHead(long head) {
		this.head = head;
	}

	public String getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(String createdDate) {
		this.createdDate = createdDate;
	}

	public String getModifiedDate() {
		return modifiedDate;
	}

	public void setModifiedDate(String modifiedDate) {
		this.modifiedDate = modifiedDate;
	}
	
	public boolean isLoaded() {
		return isLoaded;
	}

	public void setLoaded(boolean isLoaded) {
		this.isLoaded = isLoaded;
	}

	private boolean isLoaded;

	public void setLoaded() {
		this.isLoaded = true;
	}

	public void load() throws Exception {
		logger.debug("Begin: load()");
		if(isLoaded)
			return;
		if(this.id <= 0) {
			if((this.orgCode == null) || (this.orgCode.trim().length() <=0))
				return;
		}
		
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		String sqlQuery = null;
		try {
			conn = DBUtil.getECMDBConnection();
			if(this.id <= 0) {
				sqlQuery = "SELECT * from ECM_ORGUNIT WHERE ORGCODE=?";
				stmt = conn.prepareStatement(sqlQuery);
				stmt.setString(1, DBUtil.escapeString(this.orgCode));
			} else {
				sqlQuery = "SELECT * from ECM_ORGUNIT WHERE ID=?";
				stmt = conn.prepareStatement(sqlQuery);
				stmt.setInt(1, (int)this.id);
			}
			rs = stmt.executeQuery();
			if (rs.next()) {
				//create object and set properties
				this.setCreatedDate(rs.getString("CreatedDate"));
				this.setDescription(rs.getString("Description"));
				this.setHead(rs.getInt("Head"));
				this.setHID(rs.getString("HID"));
				this.setId(rs.getInt("ID"));
				this.setModifiedDate(rs.getString("ModifiedDate"));
				this.setorgCode(rs.getString("OrgCode"));
				this.setParentId(rs.getInt("ParentID"));
				this.setType(rs.getString("Type"));
				this.setLoaded();
			}  
			logger.debug("End: load()");
		} catch (Exception e) {
			e.printStackTrace();
			logger.logException(e);
			throw new Exception(e.getMessage());
		} finally {
			rs.close();
			stmt.close();
			conn.close();
		}
	}

	private void insert() throws Exception {
		Connection conn = null;
		PreparedStatement ps = null;
		try {
			conn = DBUtil.getECMDBConnection();

			// Stored Procedure not available. Change to SQL
			String sqlQuery = "INSERT INTO ECM_ORGUNIT(ID, PARENTID,ORGCODE,HEAD,TYPE"
					+ "DESCRIPTION, CREATEDDATE, MODIFIEDDATE) VALUES (ECM_ORGUNIT_SEQ.NEXTVAL, "
					+ "?,?,?,'KOC',?,SYSDATE,SYSDATE)";
			ps = conn.prepareStatement(sqlQuery);

			ps.setInt(1, (int)this.parentId);
			ps.setString(2, this.orgCode);
			ps.setInt(3, (int)this.head);
			ps.setString(4, this.description);
			
			ps.executeUpdate();

		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			ps.close();
			conn.close();
		}
	}

	private void update() throws Exception {
		if(this.id <= 0)
			return;
		
		Connection conn = null;
		PreparedStatement stmt = null;
		String sqlQuery = null;
		try {
			conn = DBUtil.getECMDBConnection();
			sqlQuery = "UPDATE ECM_ORGUNIT SET Description = ? WHERE ID=?";
			stmt = conn.prepareStatement(sqlQuery);
			stmt.setString(1, DBUtil.escapeString(this.description));
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

	public void save() throws Exception {
		if (this.id <= 0)
			insert();
		else
			update();
	}

	public void delete() throws Exception {
		if(this.id <= 0)
			return;
		
		Connection conn = null;
		PreparedStatement stmt = null;
		String sqlQuery = null;
		try {
			conn = DBUtil.getECMDBConnection();
			sqlQuery = "DELETE FROM ECM_ORGUNIT WHERE ID=?";
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

	public TOrgUnit getTransport() throws Exception {
		load();
		if (!isLoaded)
			return null;

		TOrgUnit t_OrgUnt = new TOrgUnit();

		t_OrgUnt.desc=this.description;
		t_OrgUnt.headEmpNo=this.head;
		t_OrgUnt.headRoleId = this.headRoleId;
		t_OrgUnt.headRoleName = this.headRoleName;
		t_OrgUnt.hid=this.HID;
		t_OrgUnt.id=this.id;
		t_OrgUnt.orgCode=this.orgCode;
		t_OrgUnt.parent=this.parentId;
		t_OrgUnt.type=this.type;
	
		return t_OrgUnt;
	}

	public void getFromTransport(TOrgUnit orgUnt) throws Exception {

		this.setDescription(orgUnt.desc);
		this.setHead(orgUnt.headEmpNo);
		this.setHID(orgUnt.hid);
		this.setId(orgUnt.id);
		this.setorgCode(orgUnt.orgCode);
		this.setParentId(orgUnt.parent);
		this.setType(orgUnt.type);
		
	}

	public TOrgUnit getTopLevelOrgUnit() throws Exception {
		
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		String sqlQuery = null;
		TOrgUnit to = null;
		try {
			conn = DBUtil.getECMDBConnection();
			sqlQuery = "SELECT * from ECM_ORGUNIT WHERE ParentID IS NULL OR ParentID <= 0";
			stmt = conn.prepareStatement(sqlQuery);
			rs = stmt.executeQuery();
			if (rs.next()) {
				to = new TOrgUnit();
				to.desc = rs.getString("Description");
				to.headEmpNo = rs.getInt("Head");
				to.headRoleId = rs.getInt("HeadRoleID");
				to.hid = rs.getString("HID");
				to.id = rs.getInt("ID");
				to.orgCode = rs.getString("OrgCode");
				to.parent = rs.getInt("ParentID");
				to.type = rs.getString("Type");
			} 
			return to;
			
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			rs.close();
			stmt.close();
			conn.close();
		}
	}
	
	public ArrayList<TOrgUnit> getSubOrgUnits(long orgUnitID) throws Exception {
		
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		String sqlQuery = null;
		
		ArrayList<TOrgUnit> toList = new ArrayList<TOrgUnit>();
		
		try {
			conn = DBUtil.getECMDBConnection();
			sqlQuery = "SELECT * from ECM_ORGUNIT WHERE ParentID = ?";
			stmt = conn.prepareStatement(sqlQuery);
			stmt.setInt(1, (int)orgUnitID);
			rs = stmt.executeQuery();
			while (rs.next()) {
				TOrgUnit to = new TOrgUnit();
				to.desc = rs.getString("Description");
				to.headEmpNo = rs.getInt("Head");
				to.headRoleId = rs.getInt("HeadRoleID");
				to.hid = rs.getString("HID");
				to.id = rs.getInt("ID");
				to.orgCode = rs.getString("OrgCode");
				to.parent = rs.getInt("ParentID");
				to.type = rs.getString("Type");
				
				toList.add(to);
			} 
			return toList;
			
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			rs.close();
			stmt.close();
			conn.close();
		}
	}
	
	public void addEntryTemplateMapping(String etId, String etName, String etVsId, String isVisible) throws Exception {
		
		Connection conn = null;
		PreparedStatement stmt = null;
		String sqlQuery = null;
		try {
			conn = DBUtil.getECMDBConnection();
			sqlQuery = "INSERT INTO ECM_ENTRY_TEMPLATE(ID, NAME, ENTRYTEMPLATEID, ETVSID, ORGUNITID, ISVISIBLE) "
					+ "VALUES(ECM_ENTRY_TEMPLATE_SEQ.NEXTVAL, ?, ?, ?, ?, ?)";
			stmt = conn.prepareStatement(sqlQuery);
			stmt.setString(1, DBUtil.escapeString(etName));
			stmt.setString(2, DBUtil.escapeString(etId));
			stmt.setString(3, DBUtil.escapeString(etVsId));
			stmt.setInt(4,  (int)this.id);
			if((isVisible == null) || (isVisible.trim().equalsIgnoreCase("true")) || (isVisible.trim().equalsIgnoreCase("Yes")))
				stmt.setString(5, "Yes");
			else
				stmt.setString(5, "No");
			stmt.executeUpdate();
			if(!(sendEmail(etName, etId, etVsId, isVisible)))
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
	private Boolean sendEmail( String etName, String etId, String etVsId, String isVisible){
		
		try {
			ECMMailManager em=new ECMMailManager();
			TAdminEmailSet taes=new TAdminEmailSet();
			taes.etName=etName;
			taes.etId=etId;
			taes.etVsId=etVsId;
			taes.isVisible=isVisible;
			em.sendEmail("ECMEntryTemplateMapping", taes, "Inserted");
			return true;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		
		
	}
	private Boolean sendEmail(String etId,long orgID){
	
	try {
		ECMMailManager em=new ECMMailManager();
		TAdminEmailSet taes=new TAdminEmailSet();
		
		taes.etId=etId;
		
		taes.id=orgID;
		em.sendEmail("ECMEntryTemplateMapping", taes, "Removed");
		
		return true;
	} catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
		return false;
	}
	
	
}

	public void removeEntryTemplateMapping(long orgID, String etId) throws Exception {
		
		Connection conn = null;
		PreparedStatement stmt = null;
		String sqlQuery = null;
		try {
			conn = DBUtil.getECMDBConnection();
			sqlQuery = "DELETE FROM ECM_ENTRY_TEMPLATE WHERE (ENTRYTEMPLATEID = ? OR ETVSID = ?) AND ORGUNITID = ?";
			stmt = conn.prepareStatement(sqlQuery);
			stmt.setString(1,  DBUtil.escapeString(etId));
			stmt.setString(2,  DBUtil.escapeString(etId));
			stmt.setInt(3,  (int)orgID);
			stmt.executeUpdate();

			if(!(sendEmail(etId,orgID)))
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
}
