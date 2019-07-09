package com.ecm.db.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

import com.ecm.db.list.ECMLookupList;
import com.ecm.db.transport.TAcessPolicyMapping;
import com.ecm.db.transport.TAdminEmailSet;
import com.ecm.db.transport.TLookUpValueMapping;
import com.ecm.db.transport.TLookup;
import com.ecm.db.transport.TLookupValue;
import com.ecm.db.util.DBUtil;

public class ECMLookup {
	private long id;
	private String name;
	private boolean isLoaded=false;

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
			sqlQuery = "SELECT * from ECM_LOOKUP WHERE ID=?";
			stmt = conn.prepareStatement(sqlQuery);
			stmt.setInt(1, (int)this.id);
			rs = stmt.executeQuery();
			if (rs.next()) {
				//create object and set properties
				this.name = rs.getString("Name");
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
	
	public ArrayList<TLookupValue> getValues() throws Exception{
		   
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		String sqlQuery = null;
		try {
			ArrayList<TLookupValue> ecmLukUpVlaueList = new ArrayList<TLookupValue>();
			 conn = DBUtil.getECMDBConnection();
			sqlQuery = "SELECT * from ECM_LOOKUP_VALUE WHERE LookupID=? ORDER BY LABEL ASC";
			stmt = conn.prepareStatement(sqlQuery);
			stmt.setInt(1, (int)this.id);
			rs = stmt.executeQuery();
			while (rs.next()) {
				//create object and set properties
				TLookupValue tlv = new TLookupValue();
				tlv.id = rs.getInt("ID");
				tlv.value = rs.getString("Value");
				tlv.label = rs.getString("Label");
						
				ecmLukUpVlaueList.add(tlv);
			}  
			return ecmLukUpVlaueList;
			
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			rs.close();
			stmt.close();
			conn.close();
		}
	}
	
	public String addValue(String value,String label) throws Exception{
		   
		Connection conn = null;
		PreparedStatement stmt = null;
		String sqlQuery = null;
		String strMsg = "Failed";
		try {
			conn = DBUtil.getECMDBConnection();
			
			if(!IsLookUpValueExist(value, label))
			{
				sqlQuery = "INSERT INTO ECM_LOOKUP_VALUE (ID, LookupID, Label, Value) "
						+ "VALUES (ECM_LOOKUP_VALUE_SEQ.NEXTVAL, ?, ?, ?)";
				stmt = conn.prepareStatement(sqlQuery);
				stmt.setInt(1, (int)this.id);
				stmt.setString(2, DBUtil.escapeString(label));
				stmt.setString(3, DBUtil.escapeString(value));
				stmt.executeUpdate();
				strMsg =  "OK";
			}
			else
				strMsg = "Already Exists";
			
			return strMsg;
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			if(strMsg == "OK")
				stmt.close();
			conn.close();
		}
   }
	
	public String updateValue(long lvId, String value,String label) throws Exception{
		   
		Connection conn = null;
		PreparedStatement stmt = null;
		String sqlQuery = null;
		String strMsg = "Failed";
		try {
			conn = DBUtil.getECMDBConnection();
			
			sqlQuery = "UPDATE ECM_LOOKUP_VALUE SET Label = ?, Value = ? WHERE ID = ? AND LOOKUPID = ? ";

			stmt = conn.prepareStatement(sqlQuery);
			stmt.setInt(4, (int)this.id);
			stmt.setInt(3, (int)lvId);
			stmt.setString(1, DBUtil.escapeString(label));
			stmt.setString(2, DBUtil.escapeString(value));
			stmt.executeUpdate();
			strMsg =  "OK";
			return strMsg;
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			stmt.close();
			conn.close();
		}
   }

	private String update() throws Exception{
		Connection conn = null;
		PreparedStatement stmt = null;
		String sqlQuery = null;
		try {
			conn = DBUtil.getECMDBConnection();
			sqlQuery = "UPDATE ECM_LOOKUP SET Name = ? WHERE ID = ? ";
			stmt = conn.prepareStatement(sqlQuery);
			stmt.setString(1, DBUtil.escapeString(this.name));
			stmt.setInt(2, (int)this.id);
			stmt.executeUpdate();
			if(!(sendEmailNameUpdated(name)))
			{
					//New method to send email later
			}
			return "OK";
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			stmt.close();
			conn.close();
		}
	}

	private Boolean sendEmailNameUpdated( String name){
		
		try {
			ECMMailManager em=new ECMMailManager();
			TAdminEmailSet taes=new TAdminEmailSet();
			taes.name=this.name;
			em.sendEmail("Lookup", taes, "Lookup Updated");
			
			return true;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		
		
	}

	
	private String insert() throws Exception{
		Connection conn = null;
		PreparedStatement stmt = null;
		String sqlQuery = null;
		String strMsg = "Failed";
		try {
			conn = DBUtil.getECMDBConnection();
			if(!isLookupNameExists(conn))
			{
				sqlQuery = "INSERT INTO ECM_LOOKUP (ID, Name) VALUES (ECM_LOOKUP_SEQ.NEXTVAL, ?)";
	
				stmt = conn.prepareStatement(sqlQuery);
				stmt.setString(1, DBUtil.escapeString(this.name));
				stmt.executeUpdate();
				strMsg = "OK";
				if(!(sendEmail(name)))
				{
						//New method to send email later
				}
				
			}
			else
				strMsg = "Lookup Exists";
		
			return strMsg;
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			if(strMsg == "OK")
				stmt.close();
			conn.close();
		}
	}

	private Boolean sendEmail(String name) {
		try {

			ECMMailManager em = new ECMMailManager();
			TAdminEmailSet taes = new TAdminEmailSet();
			taes.name = this.name;
			em.sendEmail("Lookup", taes, "Lookup Added");

			return true;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}

	}

	private boolean isLookupNameExists(Connection conn) {
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			//SELECT ID FROM ECM_LOOKUP WHERE regexp_replace ((UPPER(Name)), ' ', '') = regexp_replace (TRIM(UPPER('Document   Category ')), ' ', '')
			ps = conn.prepareStatement("SELECT ID FROM ECM_LOOKUP WHERE regexp_replace ((UPPER(Name)), ' ', '') = regexp_replace(TRIM(UPPER(?)), ' ', '')");
			//("SELECT ID FROM ECM_LOOKUP WHERE UPPER(Name) = ?");
			ps.setString(1,  this.name);
			rs = ps.executeQuery();
			while (rs.next()) {
				return true;
			}
			ps.close();
			rs.close();			
		} catch (Exception e) {
			e.printStackTrace();
			//throw new Exception(e.getMessage());
		} 
		return false;
	}
	
	public String save() throws Exception{
		
		if(this.id<=0)
			return insert();
		else
			return update();	
	}

	public String delete() throws Exception{
		Connection conn = null;
		PreparedStatement stmt = null;
		String sqlQuery = null;
		String strMsg = "Failed";
		try {
			if(!IsLookUpMapped()){
				conn = DBUtil.getECMDBConnection();
				TLookup tl = ECMLookupList.getInstance().getLookUpById(this.id);
				sqlQuery = "DELETE FROM ECM_LOOKUP WHERE ID = ? ";
				stmt = conn.prepareStatement(sqlQuery);
				stmt.setInt(1, (int)this.id);
				stmt.executeUpdate();
				if(!(sendEmailLVDeleted(getLookupDetails(tl))))
				{
						//New method to send email later
				}
				
				strMsg = "OK";
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
	
	private TAdminEmailSet getLookupDetails( TLookup tl){
		TAdminEmailSet taes = new TAdminEmailSet();
		taes.id = tl.id;
		taes.name = tl.name;
		return taes;
	}

	private Boolean sendEmailLVDeleted(TAdminEmailSet taes){
		try {
			ECMMailManager em=new ECMMailManager();
			em.sendEmail("Lookup", taes, "Deleted");
			
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}	
	}
	
	
	public void deleteValues(long lvId) throws Exception{
		Connection conn = null;
		PreparedStatement stmt = null;
		String sqlQuery = null;
		
		try {
			conn = DBUtil.getECMDBConnection();
			sqlQuery = "DELETE FROM ECM_LOOKUP_VALUE WHERE ID = ? AND LOOKUPID = ? ";
			stmt = conn.prepareStatement(sqlQuery);
			stmt.setInt(1, (int)lvId);
			stmt.setInt(2, (int)this.id);
			stmt.executeUpdate();
			if(!(sendEmail(lvId)))
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

private Boolean sendEmail(long lvId){
		
		try {
			ECMMailManager em=new ECMMailManager();
			TAdminEmailSet taes=new TAdminEmailSet();
			taes.id=lvId;
			em.sendEmail("Lookup", taes, "Removed");
			
			return true;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		
		
	}
	
	public boolean IsLookUpMapped() throws Exception{	   
	   	Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		String sqlQuery = null;
		
		try {
			conn = DBUtil.getECMDBConnection();
	
			sqlQuery = "SELECT ID from ECM_LOOKUP_MAPPING WHERE LOOKUPID = ?";
			stmt = conn.prepareStatement(sqlQuery);
			stmt.setInt(1, (int)this.id);
			rs = stmt.executeQuery();
			while (rs.next()) {
				return true;
			}  
			return false;
			
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			rs.close();
			stmt.close();
			conn.close();
		}	
	}
	
	public boolean IsLookUpValueExist(String value,String label) throws Exception{	   
		PreparedStatement stmt = null;
		ResultSet rs = null;
		String sqlQuery = null;
		Connection conn = null;
		
		try {	
			
			conn = DBUtil.getECMDBConnection();
			sqlQuery = "SELECT ID from ECM_LOOKUP_VALUE WHERE (( regexp_replace ((UPPER(LABEL)), ' ', '') = regexp_replace ((UPPER(?)), ' ', '') "
					+ "OR regexp_replace ((UPPER(LABEL)), ' ', '') = regexp_replace ((UPPER(?)), ' ', '')) "
					+ "OR ( regexp_replace ((UPPER(VALUE)), ' ', '') = regexp_replace ((UPPER(?)), ' ', '') "
					+ "OR regexp_replace ((UPPER(VALUE)), ' ', '') = regexp_replace ((UPPER(?)), ' ', ''))) AND LOOKUPID = ? "; 
					
			//"SELECT ID from ECM_LOOKUP_VALUE WHERE (LABEL = ? OR LABEL = ?) AND (VALUE = ? OR VALUE = ?)";
			stmt = conn.prepareStatement(sqlQuery);
			stmt.setString(1, DBUtil.escapeString(label));
			stmt.setString(2, DBUtil.escapeString(value));
			stmt.setString(3, DBUtil.escapeString(label));
			stmt.setString(4, DBUtil.escapeString(value));
			stmt.setInt(5,(int)this.id);
			rs = stmt.executeQuery();
			
			while (rs.next()) {
				return true;
			}
			return false;
			
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			rs.close();
			stmt.close();
			conn.close();
		}	
	}
	
	public TLookup getTransport(boolean withValues) throws Exception {

		load();
		if (!isLoaded)
			return null;

		TLookup tLukUp = new TLookup();
		tLukUp.id = this.id;
		tLukUp.name = this.name;
		if(withValues)
			tLukUp.values = getValues();
		return tLukUp;

	}

	public void getFromTransport(TLookup tLukUp) throws Exception {
		this.setId(tLukUp.id);
		this.setName(tLukUp.name);

	}

}
