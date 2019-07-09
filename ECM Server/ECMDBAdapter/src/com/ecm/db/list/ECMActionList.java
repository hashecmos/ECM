package com.ecm.db.list;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

import com.ecm.db.model.ECMAction;
import com.ecm.db.transport.TAction;
import com.ecm.db.util.DBUtil;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ECMActionList {
	
	public static ECMActionList getInstance()
	{
		return new ECMActionList();
	}
	
	private ECMActionList()
	{
	}
	
	public ArrayList<TAction> getAllActions() throws Exception
	{
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		String sqlQuery = null;
		try {
			ArrayList<TAction> actList = new ArrayList<TAction>();
			conn = DBUtil.getECMDBConnection();
	
			sqlQuery = "SELECT * from ECM_ACTION ORDER BY Name ASC";
			stmt = conn.prepareStatement(sqlQuery);
	
			rs = stmt.executeQuery();
			while (rs.next()) {
				//create object and set properties
				ECMAction act = ECMAction.getInstance();
				act.setId(rs.getInt("ID"));
				act.setName(rs.getString("Name"));
				act.setType(rs.getString("Type"));
	            
				actList.add(act.getTransport());
			}  
			return actList;
			
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			rs.close();
			stmt.close();
			conn.close();
		}
	
	}
	
	public void addAction(String jsonString) throws Exception
	{	
		ECMAction ecmAct = ECMAction.getInstance();
		TAction tAct =new TAction();
	
		ObjectMapper mapper = new ObjectMapper();
		
		 tAct=mapper.readValue(jsonString, TAction.class);
		 ecmAct.getFromTransport(tAct);
		 ecmAct.save();
	}
	
	public void removeAction(String jsonString) throws Exception
	{
		ECMAction ecmAct = ECMAction.getInstance();
		TAction tAct =new TAction();
	
		ObjectMapper mapper = new ObjectMapper();
		tAct=mapper.readValue(jsonString, TAction.class);
		
		 ecmAct.getFromTransport(tAct);
		 ecmAct.delete();
			
	}
	
}
