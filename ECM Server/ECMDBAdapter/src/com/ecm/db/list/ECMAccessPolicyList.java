package com.ecm.db.list;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

import oracle.jdbc.OracleTypes;

import com.ecm.db.model.ECMAccessPolicy;
import com.ecm.db.model.ECMMailManager;
import com.ecm.db.model.ECMOrgUnit;
import com.ecm.db.transport.TAccessPolicy;
import com.ecm.db.transport.TAcessPolicyMapping;
import com.ecm.db.transport.TAdminEmailSet;
import com.ecm.db.transport.TDefaultAccessPolicy;
import com.ecm.db.transport.TOrgUnit;
import com.ecm.db.util.DBUtil;

public class ECMAccessPolicyList {
	
	public static ECMAccessPolicyList getInstance()
	{
		return new ECMAccessPolicyList();
	}
	
	private ECMAccessPolicyList()
	{
	}
	
	public ArrayList<TAccessPolicy> getAccessPolicies(long empNo) throws Exception {
		if(empNo <= 0)
			return null;
		
		ArrayList<TAccessPolicy> apList = new ArrayList<TAccessPolicy>();
		
		Connection conn = null;
		ResultSet rs = null;
		String sqlQuery = null;
		CallableStatement callableStatement = null;
		try {
			conn = DBUtil.getECMDBConnection();
			sqlQuery = "{call ECM_GET_USER_ACCESS_POLICIES(?,?)}";
			callableStatement = conn.prepareCall(sqlQuery);
			callableStatement.setInt(1, (int)empNo);
			callableStatement.registerOutParameter(2, OracleTypes.CURSOR);
			callableStatement.execute();
			rs = (ResultSet)callableStatement.getObject(2);
			while (rs.next()) {
				TAccessPolicy ap = new TAccessPolicy();
				ap.id = rs.getInt("AccessPolicyID");
				ap.objectId = rs.getString("ObjectID");
				ap.name = rs.getString("Name");
				
				apList.add(ap);
			}  
			
			return apList;
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			rs.close();
			callableStatement.close();
			conn.close();
		}
	}
	
	public ArrayList<TAccessPolicy> getAllAccessPolicies() throws Exception {

		ArrayList<TAccessPolicy> apList = new ArrayList<TAccessPolicy>();
		
		Connection conn = null;
		ResultSet rs = null;
		String sqlQuery = null;
		PreparedStatement ps = null;
		try {
			conn = DBUtil.getECMDBConnection();
			sqlQuery = "SELECT A.ID, A.NAME, A.OBJECTID, A.ORGUNITID, A.TYPE, A.CREATEDBY, A.CREATEDDATE, A.MODIFIEDBY, A.MODIFIEDDATE, "
					+ "B.ORGCODE, B.DESCRIPTION "
					+ "FROM ECM_ACCESS_POLICY A, ECM_ORGUNIT B "
					+ "WHERE A.ORGUNITID = B.ID ORDER BY A.TYPE ASC, A.NAME ASC";
			ps = conn.prepareStatement(sqlQuery);
			rs = ps.executeQuery();
			while (rs.next()) {
				TAccessPolicy ap = new TAccessPolicy();
				ap.id = rs.getInt("ID");
				ap.objectId = rs.getString("ObjectID");
				ap.name = rs.getString("Name");
				ap.type = rs.getString("Type");
				ap.orgUnitId = rs.getInt("OrgUnitID");
				ap.orgCode = rs.getString("OrgCode");
				ap.orgName = rs.getString("Description");
				ap.createdBy = rs.getString("CreatedBy");
				ap.createdDate = DBUtil.convertDateToString(rs.getTimestamp("CreatedDate"));
				ap.modifiedBy = rs.getString("ModifiedBy");
				ap.modifiedDate = DBUtil.convertDateToString(rs.getTimestamp("ModifiedDate"));
				
				apList.add(ap);
			}  
			
			return apList;
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			rs.close();
			ps.close();
			conn.close();
		}
	}
	
	public ArrayList<TAccessPolicy> getAllAccessPoliciesByOrgId(long orgId) throws Exception {

		ArrayList<TAccessPolicy> apList = new ArrayList<TAccessPolicy>();
		
		Connection conn = null;
		ResultSet rs = null;
		String sqlQuery = null;
		PreparedStatement ps = null;
		try {
			conn = DBUtil.getECMDBConnection();
			sqlQuery = "SELECT A.ID, A.NAME, A.OBJECTID, A.ORGUNITID, A.TYPE, "
					+ "B.ORGCODE, B.DESCRIPTION "
					+ "FROM ECM_ACCESS_POLICY A, ECM_ORGUNIT B "
					+ "WHERE A.ORGUNITID = B.ID AND A.ORGUNITID = ? "
					+ "ORDER BY A.TYPE ASC, A.NAME ASC";
			ps = conn.prepareStatement(sqlQuery);
			ps.setInt(1, (int)orgId);
			rs = ps.executeQuery();
			while (rs.next()) {
				TAccessPolicy ap = new TAccessPolicy();
				ap.id = rs.getInt("ID");
				ap.objectId = rs.getString("ObjectID");
				ap.name = rs.getString("Name");
				ap.type = rs.getString("Type");
				ap.orgUnitId = rs.getInt("OrgUnitID");
				ap.orgCode = rs.getString("OrgCode");
				ap.orgName = rs.getString("Description");
				apList.add(ap);
			}  
			
			return apList;
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			rs.close();
			ps.close();
			conn.close();
		}
	}
	
	/*public ArrayList<String> getDefaultAccessPolicies(long empNo, String templateID) {
		if(empNo <= 0)
			return null;
		
		ArrayList<String> apList = new ArrayList<String>();
		try {
			Connection conn = null;
			ResultSet rs = null;
			String sqlQuery = null;
			CallableStatement callableStatement = null;
			try {
				conn = DBUtil.getECMDBConnection();
				sqlQuery = "{call ECM_GET_DEFAULT_ACCESSPOLICIES(?,?,?)}";
				callableStatement = conn.prepareCall(sqlQuery);
				callableStatement.setInt(1, (int)empNo);
				callableStatement.setString(2,  DBUtil.escapeString(templateID));
				callableStatement.registerOutParameter(3, OracleTypes.CURSOR);
				callableStatement.execute();
				rs = (ResultSet)callableStatement.getObject(3);
				if (rs.next()) {
					String policy1 = rs.getString("Policy1");
					String policy2 = rs.getString("Policy2");
					String policy3 = rs.getString("Policy3");
					String policy4 = rs.getString("Policy4");
					if((policy1 != null) && (policy1.length() > 0))
							apList.add(policy1);
					if((policy2 != null) && (policy2.length() > 0))
							apList.add(policy2);
					if((policy3 != null) && (policy3.length() > 0))
							apList.add(policy3);
					if((policy4 != null) && (policy4.length() > 0))
							apList.add(policy4);
				}  
			} catch (Exception e) {
				e.printStackTrace();
				throw new Exception(e.getMessage());
			} finally {
				rs.close();
				callableStatement.close();
				conn.close();
			}
	
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return apList;
	} */
	
	public ArrayList<TDefaultAccessPolicy> getDefaultAccessPolicies(long empNo, String templateID) {
		if(empNo <= 0)
			return null;
		
		ArrayList<TDefaultAccessPolicy> apList = new ArrayList<TDefaultAccessPolicy>();
		try {
			Connection conn = null;
			ResultSet rs = null;
			String sqlQuery = null;
			CallableStatement callableStatement = null;
			try {
				conn = DBUtil.getECMDBConnection();
				sqlQuery = "{call ECM_GET_DEFAULT_ACCESSPOLICIES(?,?,?)}";
				callableStatement = conn.prepareCall(sqlQuery);
				callableStatement.setInt(1, (int)empNo);
				callableStatement.setString(2,  DBUtil.escapeString(templateID));
				callableStatement.registerOutParameter(3, OracleTypes.CURSOR);
				callableStatement.execute();
				rs = (ResultSet)callableStatement.getObject(3);
				while (rs.next()) {
					TDefaultAccessPolicy dap = new TDefaultAccessPolicy();
					dap.policy1 = rs.getString("AccessPolicy1");
					dap.policy2 = rs.getString("AccessPolicy2");
					dap.policy3 = rs.getString("AccessPolicy3");
					dap.policy4 = rs.getString("AccessPolicy4");
					dap.propName = rs.getString("PropName");
					dap.propValue = rs.getString("PropValue");
					apList.add(dap);
				}  
			} catch (Exception e) {
				e.printStackTrace();
				throw new Exception(e.getMessage());
			} finally {
				rs.close();
				callableStatement.close();
				conn.close();
			}
	
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return apList;
	}
	
	public ArrayList<TDefaultAccessPolicy> getOrgDefaultAccessPolicies(String orgCode, String templateID) {
		if(orgCode.equalsIgnoreCase(null) || orgCode.length() <= 0)
			return null;
		
		ArrayList<TDefaultAccessPolicy> apList = new ArrayList<TDefaultAccessPolicy>();
		try {
			Connection conn = null;
			ResultSet rs = null;
			String sqlQuery = null;
			CallableStatement callableStatement = null;
			try {
				conn = DBUtil.getECMDBConnection();
				sqlQuery = "{call ECM_GET_DEFAULT_ORG_AP(?,?,?)}";
				callableStatement = conn.prepareCall(sqlQuery);
				callableStatement.setString(1, DBUtil.escapeString(orgCode));
				callableStatement.setString(2,  DBUtil.escapeString(templateID));
				callableStatement.registerOutParameter(3, OracleTypes.CURSOR);
				callableStatement.execute();
				rs = (ResultSet)callableStatement.getObject(3);
				while (rs.next()) {
					TDefaultAccessPolicy dap = new TDefaultAccessPolicy();
					dap.policy1 = rs.getString("AccessPolicy1");
					dap.policy2 = rs.getString("AccessPolicy2");
					dap.policy3 = rs.getString("AccessPolicy3");
					dap.policy4 = rs.getString("AccessPolicy4");
					dap.propName = rs.getString("PropName");
					dap.propValue = rs.getString("PropValue");
					apList.add(dap);
				}  
			} catch (Exception e) {
				e.printStackTrace();
				throw new Exception(e.getMessage());
			} finally {
				rs.close();
				callableStatement.close();
				conn.close();
			}
	
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return apList;
	}
	
	public ArrayList<TDefaultAccessPolicy> getOrgAccessPolicies(String orgCode, String templateID) {
		if((orgCode == null) || (orgCode.trim().length() <= 0))
			return null;
		
		ArrayList<TDefaultAccessPolicy> apList = new ArrayList<TDefaultAccessPolicy>();
		try {
			Connection conn = null;
			ResultSet rs = null;
			String sqlQuery = null;
			CallableStatement callableStatement = null;
			try {
				conn = DBUtil.getECMDBConnection();
				sqlQuery = "{call ECM_GET_ORG_ACCESSPOLICIES(?,?,?)}";
				callableStatement = conn.prepareCall(sqlQuery);
				callableStatement.setString(1, DBUtil.escapeString(orgCode));
				callableStatement.setString(2,  DBUtil.escapeString(templateID));
				callableStatement.registerOutParameter(3, OracleTypes.CURSOR);
				callableStatement.execute();
				rs = (ResultSet)callableStatement.getObject(3);
				while (rs.next()) {
					TDefaultAccessPolicy dap = new TDefaultAccessPolicy();
					dap.policy1 = rs.getString("AccessPolicy1");
					dap.policy2 = rs.getString("AccessPolicy2");
					dap.policy3 = rs.getString("AccessPolicy3");
					dap.policy4 = rs.getString("AccessPolicy4");
					dap.propName = rs.getString("PropName");
					dap.propValue = rs.getString("PropValue");
					
					apList.add(dap);
				}  
			} catch (Exception e) {
				e.printStackTrace();
				throw new Exception(e.getMessage());
			} finally {
				rs.close();
				callableStatement.close();
				conn.close();
			}
	
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return apList;
	}
	
	public TAccessPolicy getAccessPolicy(TAccessPolicy ta) throws Exception {
		
		Connection conn = null;
		ResultSet rs = null;
		String sqlQuery = null;
		PreparedStatement ps = null;
		TAccessPolicy tap = null;
		try {
			conn = DBUtil.getECMDBConnection();
			sqlQuery = "SELECT * FROM ECM_ACCESS_POLICY WHERE NAME=? AND ORGUNITID=?";
			ps = conn.prepareStatement(sqlQuery);
			ps.setString(1, DBUtil.escapeString(ta.name));
			ps.setInt(2, (int)ta.orgUnitId);
			rs = ps.executeQuery();
			if (rs.next()) {
				tap = new TAccessPolicy();
				tap.id = rs.getInt("ID");
				tap.objectId = rs.getString("OBJECTID");
				tap.name = ta.name;
				tap.orgUnitId = ta.orgUnitId;
				tap.type = ta.type;
				tap.createdBy = ta.createdBy;
				tap.createdDate = ta.createdDate;
				tap.modifiedBy = ta.modifiedBy;
				tap.modifiedDate = ta.modifiedDate;
				return tap;
			}  
			
			return tap;
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			rs.close();
			ps.close();
			conn.close();
		}
	}
	
	public String addAccessPolicyMapping(String etID, long apId) throws Exception {
		ECMAccessPolicy policy = new ECMAccessPolicy();
		policy.setId(apId);
		String strMsg = policy.addMapping(etID);
		return strMsg;
	}
	
	
































public TAcessPolicyMapping getMappedAccessPolicy(long Id) throws Exception {
		Connection conn = null;
		ResultSet rs = null;
		String sqlQuery = null;
		PreparedStatement ps = null;
		TAcessPolicyMapping map = new TAcessPolicyMapping();
		try {
			conn = DBUtil.getECMDBConnection();
			sqlQuery = "SELECT A.ID, A.OrgunitID, A.EntrytemplateID, A.Policy4 AS APID, A.ORGUNITID, "
					+"(SELECT Name FROM ECM_ACCESS_POLICY WHERE ID = A.Policy4) AS APName, "
					+ "(SELECT Name FROM ECM_ENTRY_TEMPLATE WHERE "
					+ "(ENTRYTEMPLATEID = A.ENTRYTEMPLATEID OR ETVSID = A.ENTRYTEMPLATEID) AND ROWNUM = 1) AS ETName "
					+ "FROM ECM_ACCESS_POLICY_MAPPING A WHERE A.ID = ?";

			ps = conn.prepareStatement(sqlQuery);
			ps.setInt(1, (int)Id);
			rs = ps.executeQuery();
			if (rs.next()){
				map.id = rs.getInt("ID");
				map.apid = rs.getInt("APID");
				map.apname = rs.getString("APName");
				map.etId = rs.getString("EntryTemplateID");
				map.etName = rs.getString("ETName");
				map.orgUnitId = rs.getInt("ORGUNITID");
			}
			return map;
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			rs.close();
			ps.close();
			conn.close();
		}	
	}

	public void removeAccessPolicyMapping(long mappingID) throws Exception {
		Connection conn = null;
		PreparedStatement stmt = null;
		String sqlQuery = null;
		try {
			conn = DBUtil.getECMDBConnection();
			TAcessPolicyMapping map = getMappedAccessPolicy(mappingID);
			sqlQuery = "DELETE FROM ECM_ACCESS_POLICY_MAPPING WHERE ID = ?";
			stmt = conn.prepareStatement(sqlQuery);
			stmt.setInt(1,  (int)mappingID);
			stmt.executeUpdate();
			

			
								
			if(!(sendEmail("APM", getAPMappingDetails(map))))
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
	
	
	private TAdminEmailSet getAPMappingDetails( TAcessPolicyMapping map){
		
		TAdminEmailSet taes = new TAdminEmailSet();
		taes.id=(int)map.id;
		taes.etName = map.etName;
		
		ECMOrgUnit ou = new ECMOrgUnit();
		ou.setId(map.orgUnitId);
		String orgUnitName = ou.getDescription() + "(" + ou.getorgCode() + ")";
		
		taes.orgUnitName = orgUnitName;
		
		return taes;
	}
	
	private TAdminEmailSet getAPDetails( long apId){
		try {
			TAdminEmailSet taes = null;
			if(apId > 0)
			{
				taes = new TAdminEmailSet();
				ECMAccessPolicy ap = new ECMAccessPolicy();
				ap.setId(apId);
				ap.load();
				
				taes.id = apId;
				taes.description = ap.getName();
				taes.orgUnitName =  ECMAdministrationList.getInstance().getOrgCodeFromUnitID(ap.getOrgUnitId());
			}
			return taes;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
	public String addAccessPolicyMapping(String etID, long apId) throws Exception {
		ECMAccessPolicy policy = new ECMAccessPolicy();
		policy.setId(apId);
		String strMsg = policy.addMapping(etID);
		return strMsg;
	}
	
	
private Boolean sendEmail(String screenType, TAdminEmailSet taes){
		try {
			ECMMailManager em=new ECMMailManager();
			em.sendEmail(screenType , taes, "Removed");
			
			return true;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}

	
	public ArrayList<TAcessPolicyMapping> getMappedAccessPolicies(long orgUnitId) throws Exception {
		Connection conn = null;
		ResultSet rs = null;
		String sqlQuery = null;
		PreparedStatement ps = null;
		ArrayList<TAcessPolicyMapping> mList = new ArrayList<TAcessPolicyMapping>();
		try {
			conn = DBUtil.getECMDBConnection();
			sqlQuery = "SELECT A.ID, A.OrgunitID, A.EntrytemplateID, A.Policy4 AS APID, "
					+"(SELECT Name FROM ECM_ACCESS_POLICY WHERE ID = A.Policy4) AS APName, "
					+ "(SELECT Name FROM ECM_ENTRY_TEMPLATE WHERE "
					+ "(ENTRYTEMPLATEID = A.ENTRYTEMPLATEID OR ETVSID = A.ENTRYTEMPLATEID) AND ROWNUM = 1) AS ETName "
					+ "FROM ECM_ACCESS_POLICY_MAPPING A WHERE A.ORGUNITID = ?";

			ps = conn.prepareStatement(sqlQuery);
			ps.setInt(1, (int)orgUnitId);
			rs = ps.executeQuery();
			while (rs.next()) {
				TAcessPolicyMapping map = new TAcessPolicyMapping();
				map.id = rs.getInt("ID");
				map.apid = rs.getInt("APID");
				map.apname = rs.getString("APName");
				map.etId = rs.getString("EntryTemplateID");
				map.etName = rs.getString("ETName");
				map.orgUnitId = orgUnitId;
				mList.add(map);
			}  
			
			return mList;
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			rs.close();
			ps.close();
			conn.close();
		}	
	}
	
	public TAcessPolicyMapping getMappedAccessPolicy(long Id) throws Exception {
		Connection conn = null;
		ResultSet rs = null;
		String sqlQuery = null;
		PreparedStatement ps = null;
		TAcessPolicyMapping map = null;
		try {
			conn = DBUtil.getECMDBConnection();
			sqlQuery = "SELECT A.ID, A.OrgunitID, A.EntrytemplateID, A.Policy4 AS APID, A.ORGUNITID, "
					+"(SELECT Name FROM ECM_ACCESS_POLICY WHERE ID = A.Policy4) AS APName, "
					+ "(SELECT Name FROM ECM_ENTRY_TEMPLATE WHERE "
					+ "(ENTRYTEMPLATEID = A.ENTRYTEMPLATEID OR ETVSID = A.ENTRYTEMPLATEID) AND ROWNUM = 1) AS ETName "
					+ "FROM ECM_ACCESS_POLICY_MAPPING A WHERE A.ID = ?";

			ps = conn.prepareStatement(sqlQuery);
			ps.setInt(1, (int)Id);
			rs = ps.executeQuery();
			while (rs.next()) {
				map = new TAcessPolicyMapping();
				map.id = rs.getInt("ID");
				map.apid = rs.getInt("APID");
				map.apname = rs.getString("APName");
				map.etId = rs.getString("EntryTemplateID");
				map.etName = rs.getString("ETName");
				map.orgUnitId = rs.getInt("ORGUNITID");
			}  
			
			return map;
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			rs.close();
			ps.close();
			conn.close();
		}	
	}
	
	public String removeAccessPolicy(long apId) throws Exception{
		Connection conn = null;
		PreparedStatement stmt = null;
		String sqlQuery = null;
		String strMsg = "Failed";
		try {
			if(!isAccessPolicyMapped(apId)){
				conn = DBUtil.getECMDBConnection();
				
				TAdminEmailSet taes = ECMAccessPolicyList.getInstance().getAPDetails(apId);
				
				sqlQuery = "DELETE FROM ECM_ACCESS_POLICY WHERE ID = ? ";
				stmt = conn.prepareStatement(sqlQuery);
				stmt.setInt(1, (int)apId);				
				stmt.executeUpdate();
				strMsg = "OK";
				
				if(!(sendEmail("AP", taes)))
				{
					//New method to send email later
				}
				
			}
			else
				strMsg = "Mapping Exists";
			
			return strMsg;
			
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			if(strMsg == "OK")
			{
				
				stmt.close();
				conn.close();
			}
		}
	}
	
	private Boolean isAccessPolicyMapped(long apId) {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			conn = DBUtil.getECMDBConnection();
			
			stmt = conn.prepareStatement("SELECT COUNT(*) AS APCOUNT FROM ECM_ACCESS_POLICY_MAPPING "
					+ "WHERE Policy4 = ? OR Policy1 = ? OR Policy2 = ? OR Policy3 = ?");
			stmt.setInt(1, (int)apId);
			stmt.setInt(2, (int)apId);
			stmt.setInt(3, (int)apId);
			stmt.setInt(4, (int)apId);
			
			rs = stmt.executeQuery();
			if (rs.next()) {
				int nCount = rs.getInt("APCOUNT");
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
}
