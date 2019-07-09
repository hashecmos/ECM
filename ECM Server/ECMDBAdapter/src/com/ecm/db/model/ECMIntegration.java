package com.ecm.db.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import com.ecm.db.list.ECMIntegrationList;
import com.ecm.db.transport.TAdminEmailSet;
import com.ecm.db.transport.TIntegration;
import com.ecm.db.util.DBUtil;
import com.ecm.db.util.ECMAdminLogger;

public class ECMIntegration {
	private long id = 0;
	private String appId;
	private String className;
	private String template;
	private String param1;
	private String param2;
	private String param3;
	private String param4;
	private String param5;
	private String type;
	private String searchFilter;
	private String searchDate;
	private String description;
	private String creator;
	private String modifier;
	private String coordinator;
	
	public long getId() { return id; }
	public String getAppId() { return appId; }
	public String getClassName() { return className; }
	public String getTemplate() { return template; }
	public String getType() { return type; }
	public String getParam1() { return param1; }
	public String getParam2() { return param2; }
	public String getParam3() { return param3; }
	public String getParam4() { return param4; }
	public String getParam5() { return param5; }
	public String getSearchFilter() { return searchFilter; }
	public String getSearchDate() { return searchDate; }
	public String getDescription() { return description; }


	public String getCreator() {
		return creator;
	}
	public void setCreator(String creator) {
		this.creator = creator;
	}
	public String getModifier() {
		return modifier;
	}
	public void setModifier(String modifier) {
		this.modifier = modifier;
	}
	public String getCoordinator() {
		return coordinator;
	}
	public void setCoordinator(String coordinator) {
		this.coordinator = coordinator;
	}
	public void setId(long inId) { this.id = inId; }
	public void setAppId(String inAppId) { this.appId = inAppId; }
	public void setClassName(String inClass) { this.className = inClass; }
	public void setTemplate(String inTemp) { this.template = inTemp; }
	public void setType(String inType) { this.type = inType; }
	public void setParam1(String inParam) { this.param1 = inParam; }
	public void setParam2(String inParam) { this.param2 = inParam; }
	public void setParam3(String inParam) { this.param3 = inParam; }
	public void setParam4(String inParam) { this.param4 = inParam; }
	public void setParam5(String inParam) { this.param5 = inParam; }
	public void setSearchFilter(String searchFilter) { this.searchFilter = searchFilter; }
	public void setSearchDate(String searchDate) { this.searchDate = searchDate; }
	public void setDescription(String description) { this.description = description; }

	
	public void getFromTransport(TIntegration ti) {
		this.id = ti.id;
		this.appId = ti.appId;
		this.className = ti.className;
		this.template = ti.template;
		this.param1 = ti.param1;
		this.param2 = ti.param2;
		this.param3 = ti.param3;
		this.param4 = ti.param4;
		this.param5 = ti.param5;
		this.searchFilter = ti.searchFilter;
		this.searchDate = ti.searchDate;
		this.description = ti.description;
		this.type = ti.type;
		this.creator = ti.empName;
		this.modifier = ti.empName;
		this.coordinator = ti.coordinator;
	}
	
	public void load() throws Exception {
		Connection conn = null;
		ResultSet rs = null;
		String sqlQuery = null;
		PreparedStatement ps = null;
		try {
			conn = DBUtil.getECMDBConnection();
			sqlQuery = "SELECT * FROM ECM_INTEGRATION WHERE ID = ?";
			if(this.id <= 0) {
				if((this.appId == null) || (this.appId.trim().length() <= 0))
					throw new Exception("Invalid integration request!");
				sqlQuery = "SELECT * FROM ECM_INTEGRATION WHERE APPID = ? AND ROWNUM <= 1";
			}
			ps = conn.prepareStatement(sqlQuery);
			if(this.id <= 0)
				ps.setString(1, DBUtil.escapeString(this.appId));
			else
				ps.setInt(1, (int)this.id);
			
			rs = ps.executeQuery();
			if (rs.next()) {
				this.setId(rs.getInt("ID"));
				this.setAppId(rs.getString("APPID"));
				this.setClassName(rs.getString("CLASS"));
				this.setTemplate(rs.getString("TEMPLATEID"));
				this.setType(rs.getString("TYPE"));
				this.setParam1(rs.getString("PARAM1"));
				this.setParam2(rs.getString("PARAM2"));
				this.setParam3(rs.getString("PARAM3"));
				this.setParam4(rs.getString("PARAM4"));
				this.setParam5(rs.getString("PARAM5"));
				this.setSearchFilter(rs.getString("DOCNAME"));
				this.setSearchDate(rs.getString("DOCDATE"));
				this.setCoordinator(rs.getString("COORDINATOR"));
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
	
	public void save() throws Exception {
		if(this.id > 0)
		{
			update();
			ECMAdminLogger.getInstance("ECMIntegrations").info("ECM Integrations", this.modifier, "Integrations with Id - " 
					+ this.appId + " is updated." );
		}
		else
		{
			insert();
			ECMAdminLogger.getInstance("ECMIntegrations").info("ECM Integrations", this.modifier, "Integrations with Id - " 
					+ this.appId + " is added." );
		}
	}
	
	private void insert() throws Exception {	
		Connection conn = null;
		String sqlQuery = null;
		PreparedStatement ps = null;
		try {
			conn = DBUtil.getECMDBConnection();
			sqlQuery = "INSERT INTO ECM_INTEGRATION(ID, APPID, CLASS, TEMPLATEID, "
					+ "TYPE, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5, DOCNAME, DOCDATE, "
					+ "CREATEDBY, CREATEDDATE, MODIFIEDBY, MODIFIEDDATE, DESCRIPTION, COORDINATOR) VALUES ("
					+ "ECM_INTEGRATION_SEQ.NEXTVAL, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, "
					+ "SYSTIMESTAMP, ?, SYSTIMESTAMP, ?, ?)";
	
			ps = conn.prepareStatement(sqlQuery);
			ps.setString(1, DBUtil.escapeString(this.appId));
			ps.setString(2, DBUtil.escapeString(this.className));
			ps.setString(3, DBUtil.escapeString(this.template));
			ps.setString(4, DBUtil.escapeString(this.type));
			ps.setString(5, DBUtil.escapeString(this.param1));
			ps.setString(6, DBUtil.escapeString(this.param2));
			ps.setString(7, DBUtil.escapeString(this.param3));
			ps.setString(8, DBUtil.escapeString(this.param4));
			ps.setString(9, DBUtil.escapeString(this.param5));
			ps.setString(10, DBUtil.escapeString("DOCUMENTTITLE"));
			ps.setString(11, DBUtil.escapeString("CREATEDDATE"));
			ps.setString(12, DBUtil.escapeString(this.creator));
			ps.setString(13, DBUtil.escapeString(this.modifier));
			ps.setString(14, DBUtil.escapeString(this.description));
			ps.setString(15, DBUtil.escapeString(this.coordinator));
			
			ps.executeUpdate();
			if(!(sendEmail(appId,description,coordinator, creator, modifier)))
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

	private Boolean sendEmail(String appId, String description, String coordinator, String creator,String modifier){
		
		try {
			ECMMailManager em=new ECMMailManager();
			TAdminEmailSet taes=new TAdminEmailSet();
			taes.appId = appId;
			taes.description=description;
			taes.coordinator=coordinator;
			taes.createdBy=creator;
		    taes.modifiedBy=modifier;	
	
			em.sendEmail("ECMINTEGRATION", taes, "Inserted");
			return true;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		
		
	}
	
	private void update() throws Exception {
		Connection conn = null;
		String sqlQuery = null;
		PreparedStatement ps = null;
		try {
			conn = DBUtil.getECMDBConnection();
			sqlQuery = "UPDATE ECM_INTEGRATION SET PARAM1 = ?, PARAM2 = ?, "
					+ "PARAM3 = ?, PARAM4 = ?, PARAM5 = ?, Type = ?, MODIFIEDBY = ?, "
					+ "MODIFIEDDATE = SYSTIMESTAMP, DESCRIPTION = ?, COORDINATOR = ? WHERE ID = ?";
	
			ps = conn.prepareStatement(sqlQuery);
			ps.setString(1, DBUtil.escapeString(this.param1));
			ps.setString(2, DBUtil.escapeString(this.param2));
			ps.setString(3, DBUtil.escapeString(this.param3));
			ps.setString(4, DBUtil.escapeString(this.param4));
			ps.setString(5, DBUtil.escapeString(this.param5));
			ps.setString(6, DBUtil.escapeString(this.type));
			ps.setString(7, DBUtil.escapeString(this.modifier));
			ps.setString(8, DBUtil.escapeString(this.description));
			ps.setString(9, DBUtil.escapeString(this.coordinator));
			ps.setInt(10, (int)this.id);
			
			ps.executeUpdate();
			
			if(!(sendEmail(appId, description,coordinator, modifier)))
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

	private Boolean sendEmail(String appId, String description, String coordinator,String modifier){
		
		try {
			ECMMailManager em=new ECMMailManager();
			TAdminEmailSet taes=new TAdminEmailSet();
	        taes.modifiedBy=modifier;	
	        taes.description=description;
	        taes.coordinator=coordinator;
			em.sendEmail("ECMINTEGRATION", taes, "Updated");
			return true;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		
		
	}
	private Boolean sendEmail(TIntegration integration){
	
	try {
		ECMMailManager em=new ECMMailManager();
		TAdminEmailSet taes=new TAdminEmailSet();
		taes.id=this.id;
		taes.appId = integration.appId;
		taes.description=integration.description;
		taes.coordinator=integration.coordinator;
		em.sendEmail("ECMINTEGRATION", taes, "Removed");
		
		return true;
	} catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
		return false;
	}
	
	
}
	
	public void delete() throws Exception {
		Connection conn = null;
		String sqlQuery = null;
		PreparedStatement ps = null;
		try {
			conn = DBUtil.getECMDBConnection();
			TIntegration integration = ECMIntegrationList.getInstance().getIntegrationById(this.id);
			sqlQuery = "DELETE FROM ECM_INTEGRATION WHERE ID = ?";
	
			ps = conn.prepareStatement(sqlQuery);
			ps.setInt(1, (int)this.id);
			
			ps.executeUpdate();

			if(!(sendEmail(integration)))
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

}
