package com.ecm.db.list;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Date;

import com.ecm.db.model.ECMRole;
import com.ecm.db.transport.TOrgUnit;
import com.ecm.db.transport.TRole;
import com.ecm.db.transport.TRoleMember;
import com.ecm.db.transport.TUserSearch;
import com.ecm.db.util.ADManager;
import com.ecm.db.util.DBUtil;
import com.ecm.db.util.ECMEncryption;

public class ECMRoleList {
	private static final String encKey = "8D14AC17AA4209231F357DB0EDF76DE4";
	
	private ECMRoleList() {	}

	public static ECMRoleList getInstance() {
		return new ECMRoleList();
	}

	private ArrayList<TRole> getRolesByStatus(String status) throws Exception {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		ArrayList<TRole> allRolesList = new ArrayList<TRole>();

		try {
			conn = DBUtil.getECMDBConnection();
			stmt = conn.prepareStatement("SELECT * FROM ECM_ROLE WHERE Status = ? ORDER BY Name ASC");
			stmt.setString(1, status);
			rs = stmt.executeQuery();
			while (rs.next()) {
				TRole role = new TRole();
				role.id = rs.getInt("ID");
				role.name = rs.getString("Name");
				role.orgCode = rs.getString("OrgCode");
				role.type = rs.getString("Type");
				role.adGroup = rs.getString("ADGroup");
				role.iseSignAllowed = rs.getInt("iseSignAllowed");
				role.isInitalAllowed = rs.getInt("isInitialAllowed");
				role.parentRole = rs.getInt("parentRole");
				System.out.println(rs.getString("Name"));
				
				allRolesList.add(role);

			}
			return allRolesList;

		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			rs.close();
			stmt.close();
			conn.close();
		}
	}
	
	private ArrayList<TRole> getRolesByOrgCode(String status, long empNo) throws Exception {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		ArrayList<TRole> allRolesList = new ArrayList<TRole>();

		try {
			String uOrgCode = ECMUserList.getInstance().getOrgCode(empNo);
			conn = DBUtil.getECMDBConnection();
			stmt = conn.prepareStatement("SELECT * FROM ECM_ROLE WHERE Status = ? "
					+ "AND ORGCODE IN(select ORGCODE from ecm_orgunit A where isparentof(getorgid(?),id) = 'YES') "
					+ "ORDER BY Name ASC");
			stmt.setString(1, DBUtil.escapeString(status));
			stmt.setString(2, DBUtil.escapeString(uOrgCode));
			rs = stmt.executeQuery();
			while (rs.next()) {
				TRole role = new TRole();
				role.id = rs.getInt("ID");
				role.name = rs.getString("Name");
				role.orgCode = rs.getString("OrgCode");
//				role.type = rs.getString("Type");
//				role.adGroup = rs.getString("ADGroup");
//				role.iseSignAllowed = rs.getInt("iseSignAllowed");
//				role.isInitalAllowed = rs.getInt("isInitialAllowed");
//				role.parentRole = rs.getInt("parentRole");
//				System.out.println(rs.getString("Name"));				
				allRolesList.add(role);

			}
			return allRolesList;

		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			rs.close();
			stmt.close();
			conn.close();
		}
	}
	
	public ArrayList<TRole> getActiveRolesByType(String type) throws Exception {
		return getRolesByStatusAndType("ACTIVE", type);
	}
	
	private ArrayList<TRole> getRolesByStatusAndType(String status, String type) throws Exception {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		ArrayList<TRole> allRolesList = new ArrayList<TRole>();

		try {
			conn = DBUtil.getECMDBConnection();
			stmt = conn.prepareStatement("SELECT * FROM ECM_ROLE WHERE Status = ? AND Type = ? ORDER BY Name ASC");
			stmt.setString(1, status);
			stmt.setString(2, DBUtil.escapeString(type));
			rs = stmt.executeQuery();
			while (rs.next()) {
				TRole role = new TRole();
				role.id = rs.getInt("ID");
				role.name = rs.getString("Name");
				role.orgCode = rs.getString("OrgCode");
				role.type = rs.getString("Type");
				role.adGroup = rs.getString("ADGroup");
				role.iseSignAllowed = rs.getInt("iseSignAllowed");
				role.isInitalAllowed = rs.getInt("isInitialAllowed");
				role.parentRole = rs.getInt("parentRole");
				System.out.println(rs.getString("Name"));
				
				allRolesList.add(role);

			}
			return allRolesList;

		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			rs.close();
			stmt.close();
			conn.close();
		}
	}

	public TRole getActiveRoleById(long Id) throws Exception {
		return getActiveRoleByRoleId("ACTIVE", Id);
	}
	
	private TRole getActiveRoleByRoleId(String status, long Id) throws Exception {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		TRole role = new TRole();
		try {
			conn = DBUtil.getECMDBConnection();
			stmt = conn.prepareStatement("SELECT * FROM ECM_ROLE WHERE Status = ? AND ID = ? ORDER BY Name ASC");
			stmt.setString(1, status);
			stmt.setInt(2, (int)Id);
			rs = stmt.executeQuery();
			while (rs.next()) {
				role.id = rs.getInt("ID");
				role.name = rs.getString("Name");
				role.orgCode = rs.getString("OrgCode");
				role.type = rs.getString("Type");
				role.adGroup = rs.getString("ADGroup");
				role.iseSignAllowed = rs.getInt("iseSignAllowed");
				role.isInitalAllowed = rs.getInt("isInitialAllowed");
				role.parentRole = rs.getInt("parentRole");
				System.out.println(rs.getString("Name"));
			}
			return role;

		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			rs.close();
			stmt.close();
			conn.close();
		}
	}
	
	public ArrayList<TRole> getActiveRoles() throws Exception {
		return getRolesByStatus("ACTIVE");
	}

	public ArrayList<TRole> getInactiveRoles() throws Exception {
		return getRolesByStatus("INACTIVE");
	}
	
	public ArrayList<TRole> getActiveRolesByOrgCode(long empNo) throws Exception {
		return getRolesByOrgCode("ACTIVE", empNo);
	}
	
	
	public ArrayList<TRole> searchRolesByName(String searchKey, String searchText, String searchFilter) throws Exception {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		String filterCondition = null;
		ArrayList<TRole> allRolesList = new ArrayList<TRole>();

		try {
			conn = DBUtil.getECMDBConnection();
			if(searchKey == null)
				searchKey = "ANY";
			
			if(searchFilter != null && searchFilter.length() > 0)
			{
				if(searchFilter.equalsIgnoreCase("esign"))
					filterCondition = "AND ISESIGNALLOWED = 1 AND (ISINITIALALLOWED = 0 OR ISINITIALALLOWED = 1) ";
				else if(searchFilter.equalsIgnoreCase("initial"))
					filterCondition = "AND (ISESIGNALLOWED = 0 OR ISESIGNALLOWED = 1) AND ISINITIALALLOWED = 1 ";
				else
					filterCondition = " ";
			}
			else
				filterCondition = " ";
			
			if(searchKey.trim().equalsIgnoreCase("ORGCODE")) {
				stmt = conn.prepareStatement("SELECT * FROM ECM_ROLE WHERE UPPER(ORGCODE) = ? AND Status = ?"
						+ filterCondition + "AND ROWNUM <= 50 ORDER BY ORGCODE ASC");
				stmt.setString(1, DBUtil.escapeString(searchText).toUpperCase());
				stmt.setString(2, DBUtil.escapeString("ACTIVE"));
			} else if(searchKey.trim().equalsIgnoreCase("NAME")) {
				stmt = conn.prepareStatement("SELECT * FROM ECM_ROLE WHERE UPPER(NAME) LIKE ? AND Status = ?"
						+ filterCondition + "AND ROWNUM <= 50 ORDER BY NAME ASC");
				stmt.setString(1, "%" + DBUtil.escapeString(searchText).toUpperCase() + "%");
				stmt.setString(2, DBUtil.escapeString("ACTIVE"));
			} else{
				stmt = conn.prepareStatement("SELECT * FROM ECM_ROLE WHERE  (UPPER(ORGCODE) = ? OR UPPER(NAME) LIKE ?)"
						+ filterCondition + "AND Status = ? ORDER BY NAME ASC");
				stmt.setString(1, DBUtil.escapeString(searchText).toUpperCase());
				stmt.setString(2, DBUtil.escapeString(searchText).toUpperCase());
				stmt.setString(3, DBUtil.escapeString("ACTIVE"));
			}
			rs = stmt.executeQuery();
			while (rs.next()) {
				TRole role = new TRole();
				role.id = rs.getInt("ID");
				role.name = rs.getString("Name");
				role.orgCode = rs.getString("OrgCode");
				role.type = rs.getString("Type");
				System.out.println(rs.getString("Name"));			
				allRolesList.add(role);
			}
			return allRolesList;

		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			rs.close();
			stmt.close();
			conn.close();
		}
	}
	
	public ArrayList<TRole> searchECMRolesByName(TUserSearch sCriteria, String searchFilter) throws Exception {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		String filterCondition = null;
		ArrayList<TRole> allRolesList = new ArrayList<TRole>();

		try {
			conn = DBUtil.getECMDBConnection();
			
			if(searchFilter != null && searchFilter.length() > 0)
			{
				if(searchFilter.equalsIgnoreCase("esign"))
					filterCondition = "AND ISESIGNALLOWED = 1 AND (ISINITIALALLOWED = 0 OR ISINITIALALLOWED = 1) ";
				else if(searchFilter.equalsIgnoreCase("initial"))
					filterCondition = "AND (ISESIGNALLOWED = 0 OR ISESIGNALLOWED = 1) AND ISINITIALALLOWED = 1 ";
				else
					filterCondition = " ";
			}
			else
				filterCondition = " ";
			
			if((sCriteria.userName != null && sCriteria.userName.length() > 0) 
					&&(sCriteria.orgCode != null && sCriteria.orgCode.length() > 0 )){
				stmt = conn.prepareStatement("SELECT * FROM ECM_ROLE WHERE  (UPPER(ORGCODE) = ? AND UPPER(NAME) LIKE ?)"
						+ filterCondition + "AND Status = ? ORDER BY NAME ASC");
				stmt.setString(1, DBUtil.escapeString(sCriteria.orgCode).toUpperCase());
				stmt.setString(2, "%" + DBUtil.escapeString(sCriteria.userName).toUpperCase() + "%");
				stmt.setString(3, DBUtil.escapeString("ACTIVE"));
			}	
			else if((sCriteria.userName == null) 
					&&(sCriteria.orgCode != null && sCriteria.orgCode.length() > 0 )) {
				stmt = conn.prepareStatement("SELECT * FROM ECM_ROLE WHERE UPPER(ORGCODE) = ? AND Status = ?"
						+ filterCondition + "AND ROWNUM <= 50 ORDER BY ORGCODE ASC");
				stmt.setString(1, DBUtil.escapeString(sCriteria.orgCode).toUpperCase());
				stmt.setString(2, DBUtil.escapeString("ACTIVE"));
			} 
			else if((sCriteria.userName != null && sCriteria.userName.length() > 0) 
					&&(sCriteria.orgCode == null )) {
				stmt = conn.prepareStatement("SELECT * FROM ECM_ROLE WHERE UPPER(NAME) LIKE ? AND Status = ?"
						+ filterCondition + "AND ROWNUM <= 50 ORDER BY NAME ASC");
				stmt.setString(1, "%" + DBUtil.escapeString(sCriteria.userName).toUpperCase() + "%");
				stmt.setString(2, DBUtil.escapeString("ACTIVE"));
			}
			else{
				stmt = conn.prepareStatement("SELECT * FROM ECM_ROLE WHERE AND Status = ? "
						+ filterCondition + "AND ROWNUM <= 50 ORDER BY NAME ASC");
				stmt.setString(1, DBUtil.escapeString("ACTIVE"));
			}
			
			rs = stmt.executeQuery();
			while (rs.next()) {
				TRole role = new TRole();
				role.id = rs.getInt("ID");
				role.name = rs.getString("Name");
				role.orgCode = rs.getString("OrgCode");
				role.type = rs.getString("Type");
				System.out.println(rs.getString("Name"));			
				if(role.orgCode != null && role.orgCode.length() > 0)
					allRolesList.add(role);
			} 
			return allRolesList;

		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			rs.close();
			stmt.close();
			conn.close();
		}
	}
	
	public ArrayList<TOrgUnit> getOrganizationRoles() throws Exception {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		ArrayList<TOrgUnit> allRolesList = new ArrayList<TOrgUnit>();

		try {
			conn = DBUtil.getECMDBConnection();
			stmt = conn.prepareStatement("SELECT A.ID, A.PARENTID, A.ORGCODE, B.ID AS ROLEID, "
					+ "B.NAME AS ROLENAME, LENGTH(TRIM(A.HID)) AS OFFSET "
					+ "FROM ECM_ORGUNIT A, ECM_ROLE B "
					+ "WHERE A.HEADROLEID = B.ID ORDER BY A.HID ASC");
			rs = stmt.executeQuery();
			while (rs.next()) {
				TOrgUnit ou = new TOrgUnit();
				ou.id = rs.getInt("ID");
				ou.parent = rs.getInt("PARENTID");
				ou.orgCode = rs.getString("ORGCODE");
				ou.headRoleId = rs.getInt("ROLEID");
				ou.headRoleName = rs.getString("ROLENAME");
				ou.offset = rs.getInt("OFFSET");
				
				allRolesList.add(ou);

			}
			return allRolesList;

		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			rs.close();
			stmt.close();
			conn.close();
		}
	}
	
	public ArrayList<TOrgUnit> getSubOrganizationRoles(long orgId) throws Exception {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		ArrayList<TOrgUnit> allRolesList = new ArrayList<TOrgUnit>();

		try {
			conn = DBUtil.getECMDBConnection();
			stmt = conn.prepareStatement("SELECT ID, PARENTROLE, ORGCODE, NAME , ISESIGNALLOWED, ISINITIALALLOWED"
					+ " FROM ECM_ROLE WHERE (TYPE <> 5) AND PARENTROLE = ? AND STATUS = ? ORDER BY NAME ASC");
			
			stmt.setInt(1,  (int)orgId);
			stmt.setString(2, DBUtil.escapeString("ACTIVE"));
			rs = stmt.executeQuery();
			
			while (rs.next()) {
				TOrgUnit ou = new TOrgUnit();
				ou = new TOrgUnit();
				ou.id = rs.getInt("ID");
				ou.parent = rs.getInt("PARENTROLE");
				ou.orgCode = rs.getString("ORGCODE");
				ou.headRoleId = rs.getInt("ID");
				ou.headRoleName = rs.getString("NAME");
				ou.iseSignAllowed = rs.getInt("ISESIGNALLOWED");
				ou.isInitialAllowed = rs.getInt("ISINITIALALLOWED");
				//ou.offset = rs.getInt("OFFSET");
				allRolesList.add(ou);
			}
			return allRolesList;

		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			rs.close();
			stmt.close();
			conn.close();
		}
	}
	
/*	public TOrgUnit getTopOrganizationRole() throws Exception {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		TOrgUnit ou = null;

		try {
			conn = DBUtil.getECMDBConnection();
			stmt = conn.prepareStatement("SELECT A.ID, A.PARENTID, A.ORGCODE, B.ID AS ROLEID,  "
					+ "B.NAME AS ROLENAME, LENGTH(TRIM(A.HID)) AS OFFSET "
					+ "FROM ECM_ORGUNIT A, ECM_ROLE B "
					+ "WHERE A.HEADROLEID = B.ID AND ((A.PARENTID <= 0) OR (A.PARENTID IS NULL))");
			rs = stmt.executeQuery();
			if (rs.next()) {
				ou = new TOrgUnit();
				ou.id = rs.getInt("ID");
				ou.parent = rs.getInt("PARENTID");
				ou.orgCode = rs.getString("ORGCODE");
				ou.headRoleId = rs.getInt("ROLEID");
				ou.headRoleName = rs.getString("ROLENAME");
				ou.offset = rs.getInt("OFFSET");
			}
			return ou;

		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			rs.close();
			stmt.close();
			conn.close();
		}
	}*/
	
	public TOrgUnit getTopOrganizationRole() throws Exception {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		TOrgUnit ou = null;

		try {
			conn = DBUtil.getECMDBConnection();
			stmt = conn.prepareStatement("SELECT ID, PARENTROLE, ORGCODE, NAME "
					+ "FROM ECM_ROLE WHERE TYPE = 2 AND ((PARENTROLE <= 0) OR (PARENTROLE IS NULL)) ORDER By NAME ASC");
			rs = stmt.executeQuery();
			if (rs.next()) {
				ou = new TOrgUnit();
				ou.id = rs.getInt("ID");
				ou.parent = rs.getInt("PARENTROLE");
				ou.orgCode = rs.getString("ORGCODE");
				ou.headRoleId = rs.getInt("ID");
				ou.headRoleName = rs.getString("NAME");
				//ou.offset = rs.getInt("OFFSET");
				
			}
			return ou;

		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			rs.close();
			stmt.close();
			conn.close();
		}
	}
	
	public ArrayList<TOrgUnit> getTopOrganizationRoles() throws Exception {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		ArrayList<TOrgUnit> allRolesList = new ArrayList<TOrgUnit>();

		try {
			conn = DBUtil.getECMDBConnection();
			stmt = conn.prepareStatement("SELECT ID, PARENTROLE, ORGCODE, NAME, ISESIGNALLOWED, ISINITIALALLOWED "
					+ "FROM ECM_ROLE WHERE TYPE = 3  AND STATUS = 'ACTIVE' AND ((PARENTROLE <= 0) OR (PARENTROLE IS NULL)) ORDER By NAME ASC");
			rs = stmt.executeQuery();
			
			while(rs.next()) {
				TOrgUnit ou = new TOrgUnit();
				ou = new TOrgUnit();
				ou.id = rs.getInt("ID");
				ou.parent = rs.getInt("PARENTROLE");
				ou.orgCode = rs.getString("ORGCODE");
				ou.headRoleId = rs.getInt("ID");
				ou.headRoleName = rs.getString("NAME");
				ou.iseSignAllowed = rs.getInt("ISESIGNALLOWED");
				ou.isInitialAllowed = rs.getInt("ISINITIALALLOWED");
				//ou.offset = rs.getInt("OFFSET");
				allRolesList.add(ou);
			}
			return allRolesList;
			
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			rs.close();
			stmt.close();
			conn.close();
		}
	}
	
	public ArrayList<String> getLoginNames(long roleId) throws Exception {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		ArrayList<String> userNames = new ArrayList<String>();
		
		try {
			conn = DBUtil.getECMDBConnection();
			stmt = conn.prepareStatement("SELECT B.UserName FROM ECM_ROLE_MEMBER A, dbo.ECM_USER B " +
										"WHERE A.EmpNo = B.EMPNo AND A.RoleID = ?");
			stmt.setInt(1,  (int)roleId);
			rs = stmt.executeQuery();
			while (rs.next()) {
				String userName = rs.getString("UserName");
				
				userNames.add(userName);
			}
			return userNames;

		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			rs.close();
			stmt.close();
			conn.close();
		}
	}
	
	public String getSupervisorEmailForRole(long roleId) throws Exception {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			conn = DBUtil.getECMDBConnection();
			stmt = conn.prepareStatement("SELECT C.MAIL AS EMAIL FROM ECM_ROLE A, ECM_ORGUNIT B, ECM_USER C "
					+ "WHERE A.ID = ? AND A.ORGCODE = B.ORGCODE AND ROWNUM = 1 AND B.HEAD = C.EMPNO");
			stmt.setInt(1,  (int)roleId);
			rs = stmt.executeQuery();
			if (rs.next()) {
				String email = rs.getString("EMAIL");
				
				return email;
			}
			return null;

		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			rs.close();
			stmt.close();
			conn.close();
		}
	}
	
	
	
	public ArrayList<String> getUserEmails(long roleId) throws Exception {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		ArrayList<String> emails = new ArrayList<String>();
		try {
			conn = DBUtil.getECMDBConnection();
			stmt = conn.prepareStatement("SELECT B.Mail FROM ECM_ROLE_MEMBER A, ECM_USER B " +
										"WHERE A.EmpNo = B.EMPNo AND A.RoleID = ?");
			stmt.setInt(1,  (int)roleId);
			rs = stmt.executeQuery();
			while (rs.next()) {
				String email = rs.getString("Mail");
				
				emails.add(email);
			}
			return emails;

		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			rs.close();
			stmt.close();
			conn.close();
		}
	}
	
	public String getADGroup(long roleId) throws Exception {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		try {
			conn = DBUtil.getECMDBConnection();
			stmt = conn.prepareStatement("SELECT ADGroup FROM ECM_ROLE WHERE ID=?");
			stmt.setInt(1,  (int)roleId);
			rs = stmt.executeQuery();
			if (rs.next()) {
				return rs.getString("ADGroup");
			}
			return null;

		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			rs.close();
			stmt.close();
			conn.close();
		}
	}
	
	public String getRoleName(long roleId) throws Exception {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		try {
			conn = DBUtil.getECMDBConnection();
			stmt = conn.prepareStatement("SELECT Name FROM ECM_ROLE WHERE ID=?");
			stmt.setInt(1,  (int)roleId);
			rs = stmt.executeQuery();
			if (rs.next()) {
				return rs.getString("Name");
			}
			return null;

		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			rs.close();
			stmt.close();
			conn.close();
		}
	}
	
	public boolean addUserToRole(long empNo, long roleId, String type) throws Exception {
		try {
			addUserToRoleDB(empNo, roleId, type);
			
			String userName = ECMUserList.getInstance().getLoginName(empNo);
			String ADGroup = getADGroup(roleId);
			ADManager am = getADManager();
			am.addUserToGroup(userName, ADGroup);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public void removeUserFromRole(long empNo, long roleId, String type) throws Exception {
		try {
			if(removeUserFromRoleDB(empNo, roleId, type)) {	
				String userName = ECMUserList.getInstance().getLoginName(empNo);
				String ADGroup = getADGroup(roleId);
				ADManager am = getADManager();
				am.removeUserFromGroup(userName, ADGroup);
			}
		} catch (Exception e) {
			throw e;
		}
	}
	
	private String decryptString(String inString) throws Exception {
		ECMEncryption enc = new ECMEncryption();
		return enc.getDecryptedString(encKey, inString);
	}
	
	private ADManager getADManager() throws Exception {
		try {
			ECMConfigurationList cfgList = ECMConfigurationList.getInstance("ECM", "APP");
			
			String userBaseDN = cfgList.getConfigValue("USERBASEDN");
			String groupBaseDN = cfgList.getConfigValue("GROUPBASEDN");
			String adUser = decryptString(cfgList.getConfigValue("ADUSER"));
			String adPassword = decryptString(cfgList.getConfigValue("ADPASSWORD"));
			String adHost = cfgList.getConfigValue("ADHOST");
			long adPort = DBUtil.stringToLongDefault(cfgList.getConfigValue("ADPORT"), 381);

			return ADManager.getInstance(userBaseDN, groupBaseDN, adUser, adPassword, adHost, (int)adPort);

		} catch (Exception e) {
			throw e;
		}
	}
	
	private void addUserToRoleDB(long empNo, long roleId, String type) throws Exception {
		
		if(isUserInRole(empNo, roleId, "ANY"))
			return;
		
		Connection conn = null;
		PreparedStatement stmt = null;

		try {
			conn = DBUtil.getECMDBConnection();
			stmt = conn.prepareStatement("INSERT INTO ECM_ROLE_MEMBER (ID, ROLEID, EMPNO, STATUS, CREATEDDATE, MODIFIEDDATE, TYPE) "
					+ "VALUES(ECM_ROLE_MEMBER_SEQ.NEXTVAL, ?, ?, 'ACTIVE', SYSDATE, SYSDATE, ?)");

			stmt.setInt(1, (int)roleId);
			stmt.setInt(2, (int)empNo);
			stmt.setString(3, DBUtil.escapeString(type));
			stmt.executeUpdate();
			
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		} finally {
			stmt.close();
			conn.close();
		}
	}
	
	private Boolean removeUserFromRoleDB(long empNo, long roleId, String type) throws Exception {
		
		if(!isUserInRole(empNo, roleId, type))
			return false;
		
		Connection conn = null;
		PreparedStatement stmt = null;

		try {
			conn = DBUtil.getECMDBConnection();
			stmt = conn.prepareStatement("DELETE FROM ECM_ROLE_MEMBER WHERE ROLEID = ? AND EMPNO = ? AND TYPE = ?");
			stmt.setInt(1, (int)roleId);
			stmt.setInt(2, (int)empNo);
			stmt.setString(3, DBUtil.escapeString(type));
			stmt.executeUpdate();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		} finally {
			stmt.close();
			conn.close();
		}
	}
	
	private Boolean isUserInRole(long empNo, long roleId, String type) {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			conn = DBUtil.getECMDBConnection();
			if((type != null) && ((type.trim().equalsIgnoreCase("ORG")) || 
					(type.trim().equalsIgnoreCase("DELEGATE")))) {
				stmt = conn.prepareStatement("SELECT COUNT(*) AS EMPCOUNT FROM ECM_ROLE_MEMBER "
						+ "WHERE RoleID = ? AND EMPNO = ? AND TYPE = ?");
				stmt.setInt(1, (int)roleId);
				stmt.setInt(2, (int)empNo);
				stmt.setString(3, type);
			} else {				
				stmt = conn.prepareStatement("SELECT COUNT(*) AS EMPCOUNT FROM ECM_ROLE_MEMBER WHERE RoleID = ? AND EMPNO = ?");
				stmt.setInt(1, (int)roleId);
				stmt.setInt(2, (int)empNo);
			}
			rs = stmt.executeQuery();
			if (rs.next()) {
				int nCount = rs.getInt("EMPCOUNT");
				if(nCount > 0)
					return true;
			}
			rs.close();
			stmt.close();
			conn.close();

		} catch (Exception e) {
			e.printStackTrace();
		} 
		return false;
	}
	
	public void saveRole(TRole role, String empName) throws Exception {
		ECMRole eRole = new ECMRole();
		eRole.getFromTransport(role);
		eRole.save(empName);
	}
	
	public void deleteRole(long roleId) throws Exception {
		ECMRole eRole = new ECMRole();
		eRole.setId(roleId);
		eRole.delete();
	}
	
	public ArrayList<TRoleMember> getRoleMembers(long roleId) throws Exception {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		ArrayList<TRoleMember> memberList = new ArrayList<TRoleMember>();
		try {
			conn = DBUtil.getECMDBConnection();
			String strSQL = "SELECT ID, ROLEID, EMPNO, TYPE, "
					+ "(SELECT ID FROM ECM_DELEGATION WHERE USERID = A.ROLEID AND "
					+ "TODATE > SYSDATE AND STATUS = 'ACTIVE' AND DELEGATEID = A.EMPNO AND ROWNUM=1) AS DELID, "
					+ "(SELECT FullName FROM ECM_USER WHERE EMPNO = A.EMPNO) AS USERNAME, "
					+ "(SELECT FROMDATE FROM ECM_DELEGATION WHERE USERID = A.ROLEID AND "
					+ "trunc(FROMDATE) <= TO_DATE('" + DBUtil.escapeString(DBUtil.getTodayDate()) + "','dd/MM/yyyy') "
							+ "AND STATUS = 'ACTIVE' AND DELEGATEID = A.EMPNO AND ROWNUM=1) AS FROMDATE, "
					+ "(SELECT TODATE FROM ECM_DELEGATION WHERE USERID = A.ROLEID AND "
					+ "trunc(TODATE) >= TO_DATE('" + DBUtil.escapeString(DBUtil.getTodayDate()) + "','dd/MM/yyyy') "
							+ "AND STATUS = 'ACTIVE' AND DELEGATEID = A.EMPNO AND ROWNUM=1) AS TODATE "
					+ "FROM ECM_ROLE_MEMBER A WHERE A.ROLEID = ?";
			
			stmt = conn.prepareStatement(strSQL);
			stmt.setInt(1, (int)roleId);
			rs = stmt.executeQuery();
			while (rs.next()) {
				TRoleMember tr = new TRoleMember();
				tr.id = rs.getInt("ID");
				tr.roleId = rs.getInt("ROLEID");
				tr.delId = rs.getInt("DELID");
				tr.empNo = rs.getInt("EMPNO");
				tr.type = rs.getString("TYPE");
				tr.name = rs.getString("USERNAME");
				tr.fromDate = DBUtil.convertDateToShortString(rs.getDate("FROMDATE"), "");
				Date toDate = rs.getDate("TODATE");
				if((toDate == null) || (toDate.getYear() > 140))
					tr.toDate = "Unlimited";
				else
					tr.toDate = DBUtil.convertDateToShortString(rs.getDate("TODATE"), "Unlimited");
				
				String rmName =  tr.name;
				if(rmName != null && rmName.length() > 0)
				{
					if(tr.type.equalsIgnoreCase("DELEGATE") && tr.fromDate != "" && (tr.toDate != "" || tr.toDate == "Unlimited"))
						memberList.add(tr);
					if(tr.type.equalsIgnoreCase("ORG"))
						memberList.add(tr);
				}
			}
			return memberList;
		} catch (Exception e) {
			e.printStackTrace();
			rs.close();
			stmt.close();
			conn.close();
			throw e;
		} 
	}
}
