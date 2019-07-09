package com.ecm.service.object;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

import com.ecm.db.list.ECMAdministrationList;
import com.ecm.db.transport.TAccessPolicy;
import com.ecm.db.transport.TOrgUnit;
import com.ecm.db.transport.TPermission;
import com.ecm.db.util.DBUtil;
import com.ecm.filenet.list.FNEntryTemplateList;
import com.ecm.filenet.list.FNObjectStoreList;
import com.ecm.filenet.model.FNEntryTemplate;
import com.ecm.filenet.model.FNObjectStore;
import com.ecm.filenet.transport.TFNClass;

public class UtilityHelper {
	private class TAccessPolicyEx extends TAccessPolicy {
		public String entryTemplate; 
		public String orgCode;
	}
	
	public void addEntryTemplatesToDB() throws Exception {
		FNObjectStore os = FNObjectStoreList.getInstance().getApplicationObjectStore("ECM", "ECMOS");
		ArrayList<TFNClass> classList = FNEntryTemplateList.getInstance(os).getAllEntryTemplates();

		for(TFNClass et: classList) {
			FNEntryTemplate secet = FNEntryTemplate.getInstance(os);
			secet.setId(et.id);
			secet.setEmployeeNo(0);
			
			TFNClass dbet = secet.getTransport(true);
			addEntryTemplateToDB(dbet);
		}
	}
	
	private void addEntryTemplateToDB(TFNClass et) throws Exception {
		if((et == null) || (et.vsid == null))
			return;
		Connection conn = null;
		PreparedStatement stmt = null;
		String sqlQuery = null;
		ResultSet rs = null;
		try {
			conn = DBUtil.getECMDBConnection();
			sqlQuery = "SELECT COUNT(*) AS ETCOUNT FROM ECM_UTIL_ENTRYTEMPLATES WHERE ETVSID = ?";

			stmt = conn.prepareStatement(sqlQuery);
			stmt.setString(1, DBUtil.escapeString(et.vsid));
			
			rs = stmt.executeQuery();
			if(rs.next()) {
				int nCount = rs.getInt("ETCOUNT");
				if(nCount <= 0) {
					sqlQuery = "INSERT INTO ECM_UTIL_ENTRYTEMPLATES(ETNAME, ETID, ETVSID) "
							+ "VALUES(?,?,?)";
					stmt.close();
					stmt = conn.prepareStatement(sqlQuery);
					stmt.setString(1,  DBUtil.escapeString(et.name));
					stmt.setString(2,  DBUtil.escapeString(et.id));
					stmt.setString(3,  DBUtil.escapeString(et.vsid));
					stmt.executeUpdate();
				} else {
					sqlQuery = "UPDATE ECM_UTIL_ENTRYTEMPLATES SET ETNAME=?, ETID=? WHERE ETVSID=?";
					stmt.close();
					stmt = conn.prepareStatement(sqlQuery);
					stmt.setString(1,  DBUtil.escapeString(et.name));
					stmt.setString(2,  DBUtil.escapeString(et.id));
					stmt.setString(3,  DBUtil.escapeString(et.vsid));
					stmt.executeUpdate();
				}
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			stmt.close();
			conn.close();
		}
	}
	
	private String getEntryTemplateByName(String etName) throws Exception {
		if((etName == null) || (etName.length() <= 0))
			return null;
		Connection conn = null;
		PreparedStatement stmt = null;
		String sqlQuery = null;
		ResultSet rs = null;
		try {
			conn = DBUtil.getECMDBConnection();
			sqlQuery = "SELECT ETID FROM ECM_UTIL_ENTRYTEMPLATES WHERE ETNAME = ?";

			stmt = conn.prepareStatement(sqlQuery);
			stmt.setString(1, DBUtil.escapeString(etName));
			
			rs = stmt.executeQuery();
			if(rs.next()) {
				return rs.getString("ETID");
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
	
	public void createAccessPolicies(String accessType) throws Exception {
		FNObjectStore os = FNObjectStoreList.getInstance().getApplicationObjectStore("ECM", "ECMOS");
		ArrayList<TAccessPolicyEx> apList = getAccessPoliciesFromUtil(accessType);
		for(TAccessPolicyEx ap:apList) {
			TOrgUnit to = ECMAdministrationList.getInstance().getOrgFromCode(ap.orgCode);
			if(to != null)
			{
				ap.orgUnitId = to.id;
				AccessPolicyHelper.getInstance().createAccessPolicy(ap, os);
				if((ap.type != null) && (ap.type.equalsIgnoreCase("DEFAULT")))
					setDefaultAccessPolicy(ap);
			}
		}
	}
	
	public void createPermAccessPolicies() throws Exception {
		FNObjectStore os = FNObjectStoreList.getInstance().getApplicationObjectStore("ECM", "ECMOS");
		ArrayList<TAccessPolicyEx> apList = getAccessPoliciesFromUtil("PERMISSION");
		for(TAccessPolicyEx ap:apList) {
			ap.orgUnitId = 1;
			AccessPolicyHelper.getInstance().createPermAccessPolicy(ap, os);
		}
	}
	
	private ArrayList<TAccessPolicyEx> getAccessPoliciesFromUtil(String accessType) throws Exception {
		ArrayList<TAccessPolicyEx> apList = new ArrayList<TAccessPolicyEx>();
		Connection conn = null;
		try {
			conn = DBUtil.getECMDBConnection();	
			ArrayList<String> namesList = getAccessPolicyNames(conn, accessType);
			for(String apName: namesList) {
				TAccessPolicyEx ta = getAccessPolicyObject(conn, apName);	
				if(ta != null)
					apList.add(ta);
			}
		
		return apList;
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			conn.close();
		}
	}
	
	private ArrayList<String> getAccessPolicyNames(Connection conn, String accessType) throws Exception {
		ArrayList<String> namesList = new ArrayList<String>();
		PreparedStatement stmt = null;
		ResultSet rs = null;
		String sqlQuery = null;
		try {
			sqlQuery = "SELECT DISTINCT ACCESSPOLICYNAME from ECM_UTIL_ACCESSPOLICY_INFO"
					+ " WHERE ACCESSPOLICYNAME IS NOT NULL AND ACCESSPOLICYTYPE = ? ORDER BY ACCESSPOLICYNAME ASC";
			stmt = conn.prepareStatement(sqlQuery);
			stmt.setString(1, DBUtil.escapeString(accessType));
			rs = stmt.executeQuery();
			while (rs.next()) {
				String apName = rs.getString("ACCESSPOLICYNAME");
				if((apName != null) && (apName.trim().length() > 0))
					namesList.add(apName);
			}  
			return namesList;
			
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			rs.close();
			stmt.close();
		}
	}
	
	private TAccessPolicyEx getAccessPolicyObject(Connection conn, String apName) throws Exception {
		TAccessPolicyEx ta = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		String sqlQuery = null;
		try { 
			sqlQuery = "SELECT * FROM ECM_UTIL_ACCESSPOLICY_INFO "
					+ " WHERE ACCESSPOLICYNAME = ?";
			stmt = conn.prepareStatement(sqlQuery);
			stmt.setString(1,  DBUtil.escapeString(apName));
	
			rs = stmt.executeQuery();
			int nCount = 0;
			while (rs.next()) {
				if(nCount <= 0) {
					ta = new TAccessPolicyEx();
					ta.name = apName;
					ta.orgCode = rs.getString("ORGCODE");
					ta.type = rs.getString("ACCESSPOLICYTYPE").trim().toUpperCase();
					ta.entryTemplate = rs.getString("ENTRYTEMPLATE");
				}
				nCount++;
				TPermission tp = new TPermission();
				tp.granteeName = rs.getString("PRINCIPAL");
				tp.granteeType = rs.getString("USERTYPE");
				tp.accessMask = rs.getInt("MASK");
				tp.accessType = "ALLOW";
				tp.inheritDepth = -3;
				tp.action = "ADD";
				if(ta.permissions == null)
					ta.permissions = new ArrayList<TPermission>();
				ta.permissions.add(tp);		
			}  
			return ta;
			
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			rs.close();
			stmt.close();
		}
	}
	
	private void setDefaultAccessPolicy(TAccessPolicyEx ap) throws Exception {
		String etid = getEntryTemplateByName(ap.entryTemplate);
		if(etid == null)
			return;
		Connection conn = null;
		PreparedStatement stmt = null;
		String sqlQuery = null;
		try {
			conn = DBUtil.getECMDBConnection();
			long mappedId = getMappedID(conn, etid, ap.orgUnitId);
			if(mappedId <= 0){
				sqlQuery = "INSERT INTO ECM_ACCESS_POLICY_MAPPING(ID, ORGUNITID,"
						+ "ENTRYTEMPLATEID, POLICY4) VALUES(ECM_ACCESS_POLICY_MAPPING_SEQ.NEXTVAL,"
						+ "?,?,?)";
	
				stmt = conn.prepareStatement(sqlQuery);
				stmt.setInt(1,  (int)ap.orgUnitId);
				stmt.setString(2, DBUtil.escapeString(etid));
				stmt.setInt(3, (int)ap.id);
			} else {
				sqlQuery = "UPDATE ECM_ACCESS_POLICY_MAPPING SET POLICY4 = ? WHERE ID = ?";
	
				stmt = conn.prepareStatement(sqlQuery);
				stmt.setInt(2,  (int)mappedId);
				stmt.setInt(1, (int)ap.id);
			}
			stmt.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			stmt.close();
			conn.close();
		}
	}
	
	private long getMappedID(Connection conn, String etid, long orgUnitId) throws Exception {
		PreparedStatement stmt = null;
		String sqlQuery = null;
		ResultSet rs = null;
		try {
			sqlQuery = "SELECT ID FROM ECM_ACCESS_POLICY_MAPPING WHERE ORGUNITID = ? AND "
					+ "ENTRYTEMPLATEID = ?";

			stmt = conn.prepareStatement(sqlQuery);
			stmt.setInt(1,  (int)orgUnitId);
			stmt.setString(2, DBUtil.escapeString(etid));
			
			rs = stmt.executeQuery();
			if(rs.next()) {
				return rs.getInt(1);
			}
			return 0;
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			rs.close();
			stmt.close();
		}
	}
	
}
