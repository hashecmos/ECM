package com.ecm.db.list;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Date;

import oracle.jdbc.OracleTypes;

import com.ecm.db.model.ECMOrgUnit;
import com.ecm.db.model.ECMUser;
import com.ecm.db.transport.TADPrincipal;
import com.ecm.db.transport.TAdminLog;
import com.ecm.db.transport.TEntryTemplate;
import com.ecm.db.transport.TLog;
import com.ecm.db.transport.TOrgUnit;
import com.ecm.db.util.ADManager;
import com.ecm.db.util.DBUtil;
import com.ecm.db.util.ECMAdminLogger;
import com.ecm.db.util.ECMEncryption;
import com.ecm.db.util.ECMLogger;

public class ECMAdministrationList {
	private static final String encKey = "8D14AC17AA4209231F357DB0EDF76DE4";
	
	private ECMAdministrationList() {	}

	public static ECMAdministrationList getInstance() {
		return new ECMAdministrationList();
	}
	
	public TOrgUnit getToplevelOrgUnit() throws Exception {
		ECMOrgUnit eou = new ECMOrgUnit();
		return eou.getTopLevelOrgUnit();
	}
	
	public ArrayList<TOrgUnit> getSubOrgUnits(long orgUnit) throws Exception {
		ECMOrgUnit eou = new ECMOrgUnit();
		return eou.getSubOrgUnits(orgUnit);
	}
	
	public ArrayList<TOrgUnit> getSubOrgUnitsFromOrgCode(String orgCode) throws Exception {
		ECMOrgUnit ou = new ECMOrgUnit();
		ou.setorgCode(orgCode);
		ou.load();
		return ou.getSubOrgUnits(ou.getId());
	}
	
	public ArrayList<TOrgUnit> searchOrgUnits(String searchText) throws Exception {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		String sqlQuery = null;
		ArrayList<TOrgUnit> oList = new ArrayList<TOrgUnit>();
		try {
			conn = DBUtil.getECMDBConnection();
	
			sqlQuery = "SELECT * FROM ECM_ORGUNIT WHERE ORGCODE LIKE ? OR DESCRIPTION LIKE ?";
			stmt = conn.prepareStatement(sqlQuery);
			stmt.setString(1, "%" + DBUtil.escapeString(searchText) + "%");
			stmt.setString(2, "%" + DBUtil.escapeString(searchText) + "%");

			rs = stmt.executeQuery();
			while (rs.next()) {
				TOrgUnit to = new TOrgUnit();
				to.id = rs.getInt("ID");
				to.orgCode = rs.getString("OrgCode");
				to.desc = rs.getString("Description");
				to.type = rs.getString("TYPE");
				to.headEmpNo = rs.getInt("HEAD");
				to.headRoleId = rs.getInt("HeadRoleID");
				to.parent = rs.getInt("ParentID");
				to.hid = rs.getString("HID");
				
				oList.add(to);
			}  
			return oList;
			
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			rs.close();
			stmt.close();
			conn.close();
		}	
	}
	
	public TOrgUnit getOrgFromCode(String orgCode) throws Exception {
		ECMOrgUnit ou = new ECMOrgUnit();
		ou.setorgCode(orgCode);
		ou.load();
		return ou.getTransport();
	}
	
	public String getOrgCodeFromUnitID(long orgUnit) throws Exception {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		String sqlQuery = null;
		String orgCode = "";
		try {
			conn = DBUtil.getECMDBConnection();
	
			sqlQuery = "SELECT OrgCode FROM ECM_ORGUNIT WHERE ID = ?";
			stmt = conn.prepareStatement(sqlQuery);
			stmt.setInt(1, (int)orgUnit);

			rs = stmt.executeQuery();
			while (rs.next()) {
				orgCode = rs.getString("OrgCode");
			}  
			return orgCode;
			
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			rs.close();
			stmt.close();
			conn.close();
		}	
	}
	
	public ArrayList<TOrgUnit> getUserOrgUnits(long empNo) throws Exception {
		ECMUser user = new ECMUser();
		user.setId(empNo);
		return user.getUserOrgUnits();
	}
	
	public String getNextECMNo() {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		String sqlQuery = null;
		String dateStr = DBUtil.formatDateForECMNo(new Date());
		try {
			conn = DBUtil.getECMDBConnection();
			int ecmNo = 0;
			sqlQuery = "SELECT ECM_DOCUMENT_SEQ.NEXTVAL AS EMCNO FROM DUAL";
			stmt = conn.prepareStatement(sqlQuery);

			rs = stmt.executeQuery();
			if (rs.next()) {
				ecmNo = rs.getInt("EMCNO");
			}  
			rs.close();
			stmt.close();
			conn.close();
			return dateStr + "-" + String.format("%d", ecmNo); //String.format("%06d", ecmNo);
		} catch (Exception e) {
		} 
		return DBUtil.formatDateForECMNo(new Date()) + "-" + String.format("%d", new Date().getSeconds());
	}
	
	public ArrayList<TEntryTemplate> getEntryTemplates(long empNo) throws Exception {
		if(empNo <= 0)
			return null;
		
		ArrayList<TEntryTemplate> etList = new ArrayList<TEntryTemplate>();
		
		Connection conn = null;
		ResultSet rs = null;
		String sqlQuery = null;
		CallableStatement callableStatement = null;
		try {
			conn = DBUtil.getECMDBConnection();
			sqlQuery = "{call ECM_GET_USER_ENTRY_TEMPLATES(?,?)}";
			callableStatement = conn.prepareCall(sqlQuery);
			callableStatement.setInt(1, (int)empNo);
			callableStatement.registerOutParameter(2, OracleTypes.CURSOR);
			callableStatement.execute();
			rs = (ResultSet)callableStatement.getObject(2);
			while (rs.next()) {
				TEntryTemplate et = new TEntryTemplate();
				et.id = rs.getInt("ETID");
				et.entryTemplateId = rs.getString("EntryTemplateID");
				et.name = rs.getString("Name");
				
				etList.add(et);
			}  
			
			return etList;
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			rs.close();
			callableStatement.close();
			conn.close();
		}
	}
	
	
	
	public ArrayList<TOrgUnit> getOrgUnitsByEntryTemplate(String etId) throws Exception {
		if((etId == null) || (etId.length() <= 0))
			return null;
		
		ArrayList<TOrgUnit> oList = new ArrayList<TOrgUnit>();
		
		Connection conn = null;
		ResultSet rs = null;
		String sqlQuery = null;
		PreparedStatement ps = null;
		try {
			conn = DBUtil.getECMDBConnection();
			sqlQuery = "SELECT B.ID, B.ORGCODE, B.DESCRIPTION "
					+ "FROM ECM_ENTRY_TEMPLATE A, ECM_ORGUNIT B "
					+ "WHERE (A.ENTRYTEMPLATEID = ? OR A.ETVSID = ?) AND "
					+ "A.ORGUNITID = B.ID";
			
			ps = conn.prepareCall(sqlQuery);
			ps.setString(1, DBUtil.escapeString(etId));
			ps.setString(2, DBUtil.escapeString(etId));
			rs = ps.executeQuery();
			while (rs.next()) {
				TOrgUnit to = new TOrgUnit();
				to.id = rs.getInt("ID");
				to.orgCode = rs.getString("OrgCode");
				to.desc = rs.getString("Description");
				
				oList.add(to);
			}  
			
			return oList;
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			rs.close();
			ps.close();
			conn.close();
		}
	}
	
	public ArrayList<TEntryTemplate> getEntryTemplatesByOrgId(long orgId) throws Exception {
		if(orgId <= 0)
			return null;
		
		ArrayList<TEntryTemplate> etList = new ArrayList<TEntryTemplate>();
		
		Connection conn = null;
		ResultSet rs = null;
		String sqlQuery = null;
		PreparedStatement ps = null;
		try {
			conn = DBUtil.getECMDBConnection();
			sqlQuery = "SELECT * FROM ECM_ENTRY_TEMPLATE WHERE ORGUNITID = ?";
			ps = conn.prepareCall(sqlQuery);
			ps.setInt(1, (int)orgId);
			rs = ps.executeQuery();
			while (rs.next()) {
				TEntryTemplate et = new TEntryTemplate();
				et.id = rs.getInt("ID");
				et.entryTemplateId = rs.getString("EntryTemplateID");
				et.name = rs.getString("Name");
				et.etVsId = rs.getString("ETVSID");
				etList.add(et);
			}  
			
			return etList;
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			rs.close();
			ps.close();
			conn.close();
		}
	}
	
	public ArrayList<TOrgUnit> getAllSubOrgUnits(String orgCode, long empNo) throws Exception {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		String sqlQuery = null;
		ArrayList<TOrgUnit> toList = new ArrayList<TOrgUnit>();
		
		try {
			conn = DBUtil.getECMDBConnection();
			sqlQuery = "select ID, OrgCode, description, LENGTH(TRIM(A.HID)) AS OFFSET, PARENTID " 
			+ "from ecm_orgunit A where isparentof(getorgid(?),id) = 'YES' order by hid asc";
			if(empNo>0)
				sqlQuery = "select ID, OrgCode, description, LENGTH(TRIM(A.HID)) AS OFFSET, PARENTID " 
						+ "from ecm_orgunit A where OrgCode = ? order by hid asc";
			
			stmt = conn.prepareStatement(sqlQuery);
			stmt.setString(1, DBUtil.escapeString(orgCode));
			rs = stmt.executeQuery();
			while (rs.next()) {
				TOrgUnit to = new TOrgUnit();
				to.desc = rs.getString("Description");
				if(empNo>0)
					to.desc = ECMUserList.getInstance().getUserFullName(empNo);
				
				to.id = rs.getInt("ID");
				to.orgCode = rs.getString("OrgCode");
				to.parent = rs.getInt("PARENTID");
				to.count = 0;				
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
	
	private String decryptString(String inString) throws Exception {
		ECMEncryption enc = new ECMEncryption();
		return enc.getDecryptedString(encKey, inString);
	}
	
	public ArrayList<TADPrincipal> getLDAPGroupMembers(String groupName) throws Exception {
		return getADManager().getGroupMembers(groupName);
	}
	
	public ArrayList<TADPrincipal> searchLDAPForUser(String userName) throws Exception {
		return getADManager().searchUsers(userName);
	}
	
	public ArrayList<TADPrincipal> searchLDAPForGroup(String groupName) throws Exception {
		return getADManager().searchGroups(groupName);
	}
	
	public void addEntryTemplateMapping(long orgUnitID, String etId, String name, String etVsId, String isVisible) throws Exception {
		ECMOrgUnit ou = new ECMOrgUnit();
		ou.setId(orgUnitID);
		ou.addEntryTemplateMapping(etId, name, etVsId, isVisible);
	}
	
	public void removeEntryTemplateMapping(long orgID, String etId) throws Exception {
		ECMOrgUnit ou = new ECMOrgUnit();
		ou.removeEntryTemplateMapping(orgID,etId);
	}
	
	public ArrayList<TLog> getLogs() throws Exception {
		return ECMLogger.getInstance("None").getLogs();
	}
	
	public ArrayList<TAdminLog> getAdminLogs(String instName) throws Exception {
		if(instName == null || instName.length() <= 0)
			instName = "None";
		return ECMAdminLogger.getInstance(instName).getLogs();
	}
	
	public String getLogDetails(long logId) throws Exception {
		return ECMLogger.getInstance("None").getLogDetails(logId);
	}
}
