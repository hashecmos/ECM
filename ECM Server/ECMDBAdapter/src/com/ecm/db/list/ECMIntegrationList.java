package com.ecm.db.list;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

import com.ecm.db.model.ECMIntegration;
import com.ecm.db.model.ECMUser;
import com.ecm.db.transport.TIntegration;
import com.ecm.db.transport.TUser;
import com.ecm.db.util.DBUtil;

public class ECMIntegrationList {
	private ECMIntegrationList() { }

	public static ECMIntegrationList getInstance() {
		return new ECMIntegrationList();
	}
	
	public ArrayList<TIntegration> getIntegrations() throws Exception {
		ArrayList<TIntegration> iList = new ArrayList<TIntegration>();
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			conn = DBUtil.getECMDBConnection();
			stmt = conn.prepareStatement("SELECT * FROM ECM_INTEGRATION ORDER BY APPID ASC");
			rs = stmt.executeQuery();
			while (rs.next()) {
				TIntegration ti = new TIntegration();
				ti.id = rs.getInt("ID");
				ti.appId = rs.getString("APPID");
				ti.description = rs.getString("DESCRIPTION");
				ti.className = rs.getString("CLASS");
				ti.template = rs.getString("TEMPLATEID");
				ti.type = rs.getString("TYPE");
				ti.param1 = rs.getString("PARAM1");
				ti.param2 = rs.getString("PARAM2");
				ti.param3 = rs.getString("PARAM3");
				ti.param4 = rs.getString("PARAM4");
				ti.param5 = rs.getString("PARAM5");
				ti.empName = rs.getString("CreatedBy");
				ti.createdDate = DBUtil.formatDateForUI(rs.getTimestamp("CreatedDate"));
				ti.modifiedDate = DBUtil.formatDateForUI(rs.getTimestamp("ModifiedDate"));
				ti.modifiedBy = rs.getString("ModifiedBy");
				ti.coordinator = rs.getString("Coordinator");
				iList.add(ti);
			}
			return iList;

		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			rs.close();
			stmt.close();
			conn.close();
		}
	}
	
	public TIntegration getIntegrationById(long Id) throws Exception {
		TIntegration ti = new TIntegration();
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			conn = DBUtil.getECMDBConnection();
			stmt = conn.prepareStatement("SELECT * FROM ECM_INTEGRATION WHERE ID = ?");
			stmt.setInt(1, (int)Id);
			rs = stmt.executeQuery();
			while (rs.next()) {
				ti.id = rs.getInt("ID");
				ti.appId = rs.getString("APPID");
				ti.description = rs.getString("DESCRIPTION");
				ti.className = rs.getString("CLASS");
				ti.template = rs.getString("TEMPLATEID");
				ti.type = rs.getString("TYPE");
				ti.param1 = rs.getString("PARAM1");
				ti.param2 = rs.getString("PARAM2");
				ti.param3 = rs.getString("PARAM3");
				ti.param4 = rs.getString("PARAM4");
				ti.param5 = rs.getString("PARAM5");
				ti.empName = rs.getString("CreatedBy");
				ti.createdDate = DBUtil.formatDateForUI(rs.getTimestamp("CreatedDate"));
				ti.modifiedDate = DBUtil.formatDateForUI(rs.getTimestamp("ModifiedDate"));
				ti.modifiedBy = rs.getString("ModifiedBy");
				ti.coordinator = rs.getString("Coordinator");
			}
			return ti;

		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			rs.close();
			stmt.close();
			conn.close();
		}
	}
	
	public void saveIntegration(TIntegration ti) throws Exception {
		ECMIntegration ei = new ECMIntegration();
		ei.getFromTransport(ti);
		ei.save();
	}
	
	public void deleteIntegration(long intId) throws Exception {
		ECMIntegration ei = new ECMIntegration();
		ei.setId(intId);
		ei.delete();
	}
}
