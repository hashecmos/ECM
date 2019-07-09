package com.ecm.db.list;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

import oracle.jdbc.OracleTypes;

import com.ecm.db.model.ECMLookUpValueMapping;
import com.ecm.db.model.ECMLookup;
import com.ecm.db.model.ECMMailManager;
import com.ecm.db.model.ECMOrgUnit;
import com.ecm.db.transport.TAccessPolicy;
import com.ecm.db.transport.TAdminEmailSet;
import com.ecm.db.transport.TLookUpValueMapping;
import com.ecm.db.transport.TLookup;
import com.ecm.db.transport.TLookupValue;
import com.ecm.db.util.DBUtil;

public class ECMLookupList {
	
	
	public static ECMLookupList getInstance()
	{
		return new ECMLookupList();
	}
	
	private ECMLookupList()
	{
	}
	
	public ArrayList<TLookup> getTemplateLookUps(long empNo, String templateID) throws Exception {
		
		ArrayList<TLookup> lList = new ArrayList<TLookup>();
		Connection conn = null;
		ResultSet rs = null;
		String sqlQuery = null;
		CallableStatement callableStatement = null;
		try {
			conn = DBUtil.getECMDBConnection();
			sqlQuery = "{call ECM_GET_USER_LOOKUPS(?,?,?)}";
			callableStatement = conn.prepareCall(sqlQuery);
			callableStatement.setInt(1, (int)empNo);
			callableStatement.setString(2,  DBUtil.escapeString(templateID));
			callableStatement.registerOutParameter(3, OracleTypes.CURSOR);
			callableStatement.execute();
			rs = (ResultSet)callableStatement.getObject(3);
			while (rs.next()) {
				TLookup tl = new TLookup();
				tl.id = rs.getInt("LookupID");
				tl.name = rs.getString("Name");
				tl.property = rs.getString("Property");
				ECMLookup lkup = new ECMLookup();
				lkup.setId(tl.id);
				tl.values = lkup.getValues();
				
				lList.add(tl);
			}  
			

			
			return lList;
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			rs.close();
			callableStatement.close();
			conn.close();
		}
	}
	
	public String saveLookup(long lid, String lName) throws Exception {
		ECMLookup lookup = new ECMLookup();
		lookup.setName(lName);
		lookup.setId(lid);
		return lookup.save();
	}
	
	public String removeLookup(long lid) throws Exception {
		ECMLookup lookup = new ECMLookup();
		lookup.setId(lid);
		return lookup.delete();
	}
	
	public String updateLookupValues(TLookup tl) throws Exception {
		ECMLookup lookup = new ECMLookup();
		lookup.getFromTransport(tl);
		String strMsg = "Failed";
		//lookup.deleteValues();
		if(tl.values != null) {
			String action = "Updated";
			for(TLookupValue tv:tl.values){
				if(tv.id <= 0){
					action = "Added";
					strMsg = lookup.addValue(tv.value, tv.label);
				}
				else{
					strMsg = lookup.updateValue(tv.id, tv.value, tv.label);
				}
			}
			if(strMsg == "OK"){
				if(!(sendEmail(action))){
					////New method to send email later
				}
			}
				
		}
		return strMsg;
	}
	
	private Boolean sendEmail(String action) {
		try {
			ECMMailManager em = new ECMMailManager();
			TAdminEmailSet taes = new TAdminEmailSet();
			em.sendEmail("Lookup", taes, action);

			return true;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}
	
	public void removeLookupValue(long lvId, long lookUpId) throws Exception {
		ECMLookup lookup = new ECMLookup();
		lookup.setId(lookUpId);
		lookup.deleteValues(lvId);
	}
	
	public void  addLookUpMapping(long lookupId,long orgUnit,String template, String prop) throws Exception{
	 
		Connection conn = null;
		CallableStatement callableStatement = null;
		String sqlQuery = null;
		try {
			conn = DBUtil.getECMDBConnection();

			sqlQuery = "{call ECM_ADD_LOOKUP_MAPPING(?,?,?,?)}";
			callableStatement = conn.prepareCall(sqlQuery);
			
			callableStatement.setInt(1, (int)orgUnit);
			callableStatement.setString(2,template);
			callableStatement.setString(3, prop);
			callableStatement.setInt(4, (int)lookupId);
			callableStatement.executeUpdate();
			
			if(!(sendEmail(orgUnit,template, prop, lookupId)))
			{
					//New method to send email later
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			callableStatement.close();
			conn.close();
		}
   
   }
	
	private Boolean sendEmail(long orgUnit, String template, String prop, long lookupId ){
		
		try {
			ECMMailManager em=new ECMMailManager();
			TAdminEmailSet taes=new TAdminEmailSet();
			
			ECMOrgUnit ou = new ECMOrgUnit();
			ou.setId(orgUnit);
			String orgUnitName = ou.getDescription() + "(" + ou.getorgCode() + ")";
			//taes.orgUnitName = ECMAdministrationList.getInstance().getOrgUnitsByEntryTemplate(etId);
			
			taes.template = template;
			taes.prop = prop;
			taes.lookupId=lookupId;
			//taes.etName=etName;
			em.sendEmail("LookupMapping", taes, "Added");
			
			return true;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		
		
	}
 
	public void removeLookUpMapping(long orgUnit,String template,String prop) throws Exception {
		
		Connection conn = null;
		PreparedStatement stmt = null;
		String sqlQuery = null;
		try {
			conn = DBUtil.getECMDBConnection();
	
			sqlQuery = "DELETE FROM ECM_LOOKUP_MAPPING WHERE OrgUnitID = ? "
					+ "AND TemplateID = ? AND Property = ?";
			stmt = conn.prepareStatement(sqlQuery);
			
			stmt.setInt(1,  (int)orgUnit);
			stmt.setString(2, DBUtil.escapeString(template));
			stmt.setString(3, DBUtil.escapeString(prop));

			stmt.executeUpdate();
			
			if(!sendEmail( orgUnit, template, prop))
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
private Boolean sendEmail( long orgUnit, String  template, String prop){
		
		try {
			ECMMailManager em=new ECMMailManager();
			TAdminEmailSet taes=new TAdminEmailSet();
			
			/*ECMOrgUnit ou = new ECMOrgUnit();
			ou.setId(orgUnit);
			String orgUnitName = ou.getDescription() + "(" + ou.getorgCode() + ")";*/
			
			
			
			taes.id =  (int)orgUnit;
			taes.template = template;
			taes.prop = prop;
			em.sendEmail("LookupMapping", taes, "Removed");
			
			return true;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		
		
	}


	public ArrayList<TLookup> getAllLookUps(boolean bWithvalues) throws Exception{
	   
	   	Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		String sqlQuery = null;
		try {
			ArrayList<TLookup> ecmLukUpList = new ArrayList<TLookup>();
			conn = DBUtil.getECMDBConnection();
	
			sqlQuery = "SELECT ID, Name from ECM_LOOKUP ORDER BY Name";
			stmt = conn.prepareStatement(sqlQuery);

			rs = stmt.executeQuery();
			while (rs.next()) {
				//create object and set properties
				TLookup tl = new TLookup();
				tl.id = rs.getInt("ID");
				tl.name = rs.getString("Name");	
				if(bWithvalues)
				{
					ECMLookup ecml = new ECMLookup();
					ecml.setId(tl.id);
					tl.values = ecml.getValues();
				}
				ecmLukUpList.add(tl);
			}  
			return ecmLukUpList;
			
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			rs.close();
			stmt.close();
			conn.close();
		}	
	}
	
	public ArrayList<TLookUpValueMapping> getLookUpMappingsByOrg(long orgUnit, String etVsId) throws Exception{
		   
	   	Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		String sqlQuery = null;
		try {
			ArrayList<TLookUpValueMapping> ecmLukUpMapList = new ArrayList<TLookUpValueMapping>();
			conn = DBUtil.getECMDBConnection();
	
			 sqlQuery = "SELECT ID, ORGUNITID, (SELECT DESCRIPTION FROM ECM_ORGUNIT WHERE ID = A.ORGUNITID AND ROWNUM = 1) AS ORGUNITNAME,"
			 		+ "LOOKUPID, TEMPLATEID, (SELECT NAME FROM ECM_ENTRY_TEMPLATE WHERE (ENTRYTEMPLATEID = A.TEMPLATEID OR ETVSID = A.TEMPLATEID) AND ROWNUM = 1 ) AS TMPNAME, "
			 		+ "(SELECT ENTRYTEMPLATEID FROM ECM_ENTRY_TEMPLATE WHERE (ENTRYTEMPLATEID = A.TEMPLATEID OR ETVSID = A.TEMPLATEID) AND ROWNUM = 1 ) AS TMPID, "
			 		+ "(SELECT NAME FROM ECM_LOOKUP WHERE ID= A.LOOKUPID) AS LOOKUPNAME, "
			 		+ "PROPERTY from ECM_LOOKUP_MAPPING A WHERE ORGUNITID = ? AND TEMPLATEID = ? ORDER BY ID";
			stmt = conn.prepareStatement(sqlQuery);
			stmt.setInt(1,  (int)orgUnit);
			stmt.setString(2, DBUtil.escapeString(etVsId));

			rs = stmt.executeQuery();
			while (rs.next()) {
				//create object and set properties
				TLookUpValueMapping tl = new TLookUpValueMapping();
				tl.id = rs.getInt("ID");
				tl.lkUp = rs.getInt("LOOKUPID");
				tl.orgUId = rs.getInt("ORGUNITID");	
				tl.tmpId = rs.getString("TEMPLATEID");
				tl.prop = rs.getString("PROPERTY");
				tl.orgUName = rs.getString("ORGUNITNAME");
				tl.tmpName = rs.getString("TMPNAME");
				tl.etId = rs.getString("TMPID");
				tl.lkupName = rs.getString("LOOKUPNAME");
				
				ECMLookUpValueMapping ecml = new ECMLookUpValueMapping();
				ecml.setId(tl.id);
				
				ecmLukUpMapList.add(tl);
			}  
			return ecmLukUpMapList;
			
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			rs.close();
			stmt.close();
			conn.close();
		}	
	}
	
	public ArrayList<TLookup>  getLookUpsByOrgId(long orgId) throws Exception {

		ArrayList<TLookup> ecmLukUpList = new ArrayList<TLookup>();
		
		Connection conn = null;
		ResultSet rs = null;
		String sqlQuery = null;
		PreparedStatement ps = null;
		try {
			conn = DBUtil.getECMDBConnection();
			sqlQuery = "SELECT DISTINCT(LOOKUPID) AS LOOKUPID  from ECM_LOOKUP_MAPPING WHERE ORGUNITID = ? ORDER BY LOOKUPID";
			ps = conn.prepareStatement(sqlQuery);
			ps.setInt(1, (int)orgId);
			rs = ps.executeQuery();
			while (rs.next()) {
				//create object and set properties
				TLookup tl = new TLookup();
				tl.id = rs.getInt("LOOKUPID");
				ECMLookup lkup = new ECMLookup();
				lkup.setId(tl.id);
				lkup.load();
				tl.name = lkup.getName();
				ecmLukUpList.add(tl);
			}  
			return ecmLukUpList;
			
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			rs.close();
			ps.close();
			conn.close();
		}	
	}
	
	public ArrayList<TLookUpValueMapping> getAllLookUpMappings() throws Exception{
		   
	   	Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		String sqlQuery = null;
		try {
			ArrayList<TLookUpValueMapping> ecmLukUpMapList = new ArrayList<TLookUpValueMapping>();
			conn = DBUtil.getECMDBConnection();
	
			 sqlQuery = "SELECT ID, ORGUNITID, (SELECT DESCRIPTION FROM ECM_ORGUNIT WHERE ID = A.ORGUNITID AND ROWNUM = 1) AS ORGUNITNAME, "
				     + "LOOKUPID, TEMPLATEID, (SELECT NAME FROM ECM_ENTRY_TEMPLATE WHERE (ENTRYTEMPLATEID = A.TEMPLATEID OR ETVSID = A.TEMPLATEID) AND ROWNUM = 1 ) AS TMPNAME, "
					 + "(SELECT ENTRYTEMPLATEID FROM ECM_ENTRY_TEMPLATE WHERE (ENTRYTEMPLATEID = A.TEMPLATEID OR ETVSID = A.TEMPLATEID) AND ROWNUM = 1 ) AS TMPID, "
					 + "(SELECT NAME FROM ECM_LOOKUP WHERE ID= A.LOOKUPID) AS LOOKUPNAME, "
					 + "PROPERTY from ECM_LOOKUP_MAPPING A ORDER BY ID";
			stmt = conn.prepareStatement(sqlQuery);

			rs = stmt.executeQuery();
			while (rs.next()) {
				//create object and set properties
				TLookUpValueMapping tl = new TLookUpValueMapping();
				tl.id = rs.getInt("ID");
				tl.lkUp = rs.getInt("LOOKUPID");
				tl.orgUId = rs.getInt("ORGUNITID");	
				tl.tmpId = rs.getString("TEMPLATEID");
				tl.prop = rs.getString("PROPERTY");
				tl.orgUName = rs.getString("ORGUNITNAME");
				tl.tmpName = rs.getString("TMPNAME");
				tl.etId = rs.getString("TMPID");
				tl.lkupName =  rs.getString("LOOKUPNAME");
				
				ECMLookUpValueMapping ecml = new ECMLookUpValueMapping();
				ecml.setId(tl.id);
				
				ecmLukUpMapList.add(tl);
			}  
			return ecmLukUpMapList;
			
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			rs.close();
			stmt.close();
			conn.close();
		}	
	}
	
	public TLookup getLookUpById(long lookUpId) throws Exception{
		TLookup tl = new TLookup();
	   	Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		String sqlQuery = null;
		try {
			conn = DBUtil.getECMDBConnection();
			sqlQuery = "SELECT ID, Name from ECM_LOOKUP WHERE ID = ? ORDER BY Name";
			stmt = conn.prepareStatement(sqlQuery);
			stmt.setInt(1, (int)lookUpId);
			rs = stmt.executeQuery();
			while (rs.next()) {
				tl.id = rs.getInt("ID");
				tl.name = rs.getString("Name");
			}  
			return tl;
			
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			rs.close();
			stmt.close();
			conn.close();
		}
	}

}
