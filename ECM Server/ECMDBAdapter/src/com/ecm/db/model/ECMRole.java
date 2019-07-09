package com.ecm.db.model;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

import oracle.jdbc.OracleTypes;

import com.ecm.db.util.DBUtil;
import com.ecm.db.util.ECMAdminLogger;
import com.ecm.db.util.RequestHelper;
import com.ecm.db.list.ECMRoleList;
import com.ecm.db.transport.TAdminEmailSet;
import com.ecm.db.transport.TUser;
import com.ecm.db.transport.TRole;


public class ECMRole {

	private String name;
	private String orgCode;
	private String type;
	private String createdDate;
	private String modifiedDate;
	private long id;
	private String adGroup;
	private long iseSignAllowed = 0;
	private long isInitalAllowed = 0;
	private long parentRole = 0;
	
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public String getADGroup() {
		return adGroup;
	}

	public void setADGroup(String name) {
		this.adGroup = name;
	}


	public String getOrgCode() {
		return this.orgCode;
	}

	public void setOrgCode(String orgnCode) {
		this.orgCode = orgnCode;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
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
	
	public long getIseSignAllowed() {
		return iseSignAllowed;
	}

	public void setIseSignAllowed(long iseSignAllowed) {
		this.iseSignAllowed = iseSignAllowed;
	}

	public long getIsInitalAllowed() {
		return isInitalAllowed;
	}

	public void setIsInitalAllowed(long isInitalAllowed) {
		this.isInitalAllowed = isInitalAllowed;
	}
	
	public long getParentRole() {
		return parentRole;
	}

	public void setParentRole(long parentRole) {
		this.parentRole = parentRole;
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

		if (this.isLoaded)
			return;

		Connection conn = null;
		ResultSet rs = null;
		String sqlQuery = null;
		PreparedStatement ps = null;
		try {
			conn = DBUtil.getECMDBConnection();
			sqlQuery = "SELECT * FROM ECM_ROLE WHERE ID = ?";
			ps = conn.prepareStatement(sqlQuery);
			ps.setInt(1, (int)this.id);
			
			rs = ps.executeQuery();
			if (rs.next()) {
						
				this.setCreatedDate(rs.getString("CreatedDate"));
				this.setModifiedDate(rs.getString("ModifiedDate"));
				this.setId(rs.getInt("ID"));
				this.setName(rs.getString("Name"));
				this.setOrgCode(rs.getString("OrgCode"));
				this.setType(rs.getString("Type"));
				this.setADGroup(rs.getString("ADGroup"));
				this.setIseSignAllowed(rs.getLong("IseSignAllowed"));
				this.setIsInitalAllowed(rs.getLong("IsInitialAllowed"));
				this.setParentRole(rs.getLong("ParentRole"));
				System.out.println(rs.getString("Name"));
				isLoaded = true;
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

	private void insert() throws Exception {
		Connection conn = null;
		CallableStatement stmt = null;
		try {
			conn = DBUtil.getECMDBConnection();

			String sqlQuery = "{call ECM_CREATE_ROLE(?,?,?,?,?,?,?,?)}";;
			stmt = conn.prepareCall(sqlQuery);
			stmt.setString(1, DBUtil.escapeString(this.name));
			stmt.setString(2, DBUtil.escapeString(this.orgCode));
			stmt.setString(3, DBUtil.escapeString(this.type));
			stmt.setString(4, DBUtil.escapeString(this.adGroup));
			stmt.setInt(5, (int)this.iseSignAllowed);
			stmt.setInt(6, (int)this.isInitalAllowed);
			stmt.setInt(7, (int)this.parentRole);
			stmt.registerOutParameter(8, OracleTypes.INTEGER);
			stmt.execute();
			this.id = (long)stmt.getInt(8);
			if(!(sendEmail(name,orgCode,adGroup,iseSignAllowed,parentRole,"Added")))
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

	private Boolean sendEmail( String name, String orgCode, String adGroup ,long iseSignAllowed, long parentRole, String action ){
		
		try {
			ECMMailManager em=new ECMMailManager();
			TAdminEmailSet taes=new TAdminEmailSet();
			taes.name=this.name;
			taes.orgUnitName=this.orgCode;
			if(DBUtil.stringToLong(this.type)==3)
				taes.type="Directorate";
			else if(DBUtil.stringToLong(this.type)==2)
				taes.type="Group";
			else if(DBUtil.stringToLong(this.type)==1)
				taes.type="Role";
			taes.adGroup=this.adGroup;
			if(this.iseSignAllowed==0)
				taes.iseSignAllowed = "No";
			else
				taes.iseSignAllowed = "Yes";
			
			if(this.isInitalAllowed==0)
				taes.isInitalAllowed = "No";
			else
				taes.isInitalAllowed = "Yes";
			
			if(this.parentRole>0){
				TRole rl = ECMRoleList.getInstance().getActiveRoleById(this.parentRole);
				taes.parentRole = rl.name;
			}
			
			taes.modifiedDate=DBUtil.getTodayDate();
			em.sendEmail("ECMRoleM", taes, action);
			
			return true;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		
		
	}

	private void update() throws Exception {
		Connection conn = null;
		PreparedStatement ps = null;
		try {
			conn = DBUtil.getECMDBConnection();

			String sqlQuery = "UPDATE ECM_ROLE SET Name = ?, orgCode = ?, Type = ?, "
					+ "ModifiedDate = SYSDATE, ADGroup = ?, IseSignAllowed = ?, IsInitialAllowed = ?, parentRole = ? WHERE ID = ?";
			ps = conn.prepareStatement(sqlQuery);
			
			ps.setInt(8, (int)this.id);
			ps.setString(1, DBUtil.escapeString(this.name));
			ps.setString(2, DBUtil.escapeString(this.orgCode));
			ps.setString(3, DBUtil.escapeString(this.type));
			ps.setString(4, DBUtil.escapeString(this.adGroup));
			ps.setInt(5, (int)this.iseSignAllowed);
			ps.setInt(6, (int)this.isInitalAllowed);
			ps.setInt(7, (int)this.parentRole);
			
			ps.executeUpdate();
			if(!(sendEmail(name,orgCode,adGroup,iseSignAllowed,parentRole,"Updated" )))
			{
					//New method to send email later
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			ps.close();
			conn.close();
		}
	}

	public void save(String empName) throws Exception {
		if (this.id <= 0)
		{
			insert();
			ECMAdminLogger.getInstance("ECMRoles").info("ECM Roles", empName, "ECM Role with Id - " 
					+ this.id + " is added." );
		}
		else
		{
			update();
			ECMAdminLogger.getInstance("ECMRoles").info("ECM Roles", empName, "ECM Role with Id - " 
					+ this.id + " is updated." );
		}
	}

	public void delete() throws Exception {
		Connection conn = null;
		PreparedStatement ps = null;
		String sqlQuery = null;
		try {
			conn = DBUtil.getECMDBConnection();
			TRole role = ECMRoleList.getInstance().getActiveRoleById(id);
			sqlQuery = "UPDATE ECM_ROLE SET Status='INACTIVE' WHERE ID=?";
			ps = conn.prepareStatement(sqlQuery);
			ps.setInt(1, (int)this.id);
			ps.executeUpdate();

			if(!(sendEmail(role)))
			{
				//New method to send email later
			}

		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			ps.close();
			conn.close();
		}
	}

	private Boolean sendEmail(TRole role){
		
		try {
			ECMMailManager em=new ECMMailManager();
			TAdminEmailSet taes=new TAdminEmailSet();
			taes.id=this.id;
			taes.name = role.name;
			em.sendEmail("ECMRoleM", taes, "Deleted");
			
			return true;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		
		
	}

	public ArrayList<TUser> getRoleMembers() throws Exception {
		
		Connection conn = null;
		CallableStatement stmt = null;
		ResultSet rs = null;
		ArrayList<TUser> userList = null;
		try {
			conn = DBUtil.getECMDBConnection();
			String sqlQuery = "{call ECM_GET_ROLE_MEMBERS(?,?)}";;
			stmt = conn.prepareCall(sqlQuery);
			stmt.setInt(1, (int)this.id);
			stmt.registerOutParameter(2, OracleTypes.CURSOR);
			stmt.execute();
			rs = (ResultSet)stmt.getObject(2);

			if (rs.next()) {

				TUser user = new TUser();
				user.id = rs.getInt("ID");
				user.fulName = rs.getString("Name");
				user.title = rs.getString("Title");
				user.EmpNo = rs.getInt("EMPNo");
			
				System.out.println(rs.getString("Name"));

				if(userList == null)
					userList = new ArrayList<TUser>();
				userList.add(user);

			} 
			return userList;

		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			rs.close();
			stmt.close();
			conn.close();
		}

	}


	public TRole getTransport() throws Exception {
		load();
		if (!isLoaded)
			return null;

		TRole t_Role = new TRole();

		t_Role.id=this.id;
		t_Role.name=this.name;
		t_Role.orgCode=this.orgCode;
		t_Role.type=this.type;
		t_Role.adGroup=this.adGroup;
		t_Role.iseSignAllowed = this.iseSignAllowed;
		t_Role.isInitalAllowed = this.isInitalAllowed;
		t_Role.parentRole = this.parentRole;
		return t_Role;
	}
	

	public void getFromTransport(TRole role) throws Exception {
		
		this.setId(role.id);
		this.setName(role.name);
		this.setOrgCode(role.orgCode);
		this.setType(role.type);
		this.setADGroup(role.adGroup);
		this.setIseSignAllowed(role.iseSignAllowed);
		this.setIsInitalAllowed(role.isInitalAllowed);
		this.setParentRole(role.parentRole);
	}

}
