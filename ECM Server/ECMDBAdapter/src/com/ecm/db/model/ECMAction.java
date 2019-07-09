package com.ecm.db.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import com.ecm.db.transport.TAction;
import com.ecm.db.util.DBUtil;

public class ECMAction {
	private long id;
	private String name;
	private String type;

	private ECMAction() {}
	
	public static ECMAction getInstance()
	{
		return new ECMAction();
	}
	
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

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	private void update() throws Exception 
	{
		Connection conn = null;
		String sqlQuery = null;
		PreparedStatement ps = null;
		try {
			conn = DBUtil.getECMDBConnection();
			
			sqlQuery = "UPDATE ECM_ACTION SET Name=?, Type=? WHERE ID=?";
		
			ps = conn.prepareStatement(sqlQuery);
			ps.setString(1,DBUtil.escapeString(this.name));
			ps.setString(2,DBUtil.escapeString(this.type));
			ps.setInt(3,(int)id);
			ps.executeUpdate();
			
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			conn.close();
			ps.close();
		}
	}

	private void insert() throws Exception 
	{
		Connection conn = null;
		String sqlQuery = null;
		PreparedStatement ps = null;
		try {
			conn = DBUtil.getECMDBConnection();
			
			sqlQuery = "INSERT INTO ECM_ACTION (Name, Type) VALUES(ECM_ACTION_SEQ.NEXTVAL, ?,?,?)";
		
			ps = conn.prepareStatement(sqlQuery);
			ps.setString(1,DBUtil.escapeString(this.name));
			ps.setString(2,DBUtil.escapeString(this.type));
			ps.executeUpdate();

		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			conn.close();
			ps.close();
		}
	}

	public void save() throws Exception {

		if (this.id <= 0)
			insert();
		else
			update();
	}

	public void delete() throws Exception 
	{
		Connection conn = null;
		String sqlQuery = null;
		PreparedStatement ps = null;
		try {
			conn = DBUtil.getECMDBConnection();
			
			sqlQuery = "DELETE FROM ECM_ACTION WHERE ID=?";
		
			ps = conn.prepareStatement(sqlQuery);
			ps.setInt(1, (int)this.id);
			ps.executeUpdate();

		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			conn.close();
			ps.close();
		}
	}

	public TAction getTransport() throws Exception 
	{
		TAction tAction = new TAction();
		tAction.id = this.id;
		tAction.name = this.name;
		tAction.type = this.type;

		return tAction;
	}

	public void getFromTransport(TAction tAction) throws Exception 
	{
		this.setId(tAction.id);
		this.setName(tAction.name);
		this.setType(tAction.type);
	}

}
