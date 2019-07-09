package com.ecm.db.list;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

import com.ecm.db.model.ECMProgress;
import com.ecm.db.transport.TWorkitemProgress;
import com.ecm.db.util.DBUtil;

public class ECMProgressList {

	private ECMProgressList() {	}

	public static ECMProgressList getInstance() {
		return new ECMProgressList();
	}

	public ArrayList<TWorkitemProgress> getWorkItemProgress(long wItemId, String status) throws Exception {

		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		String qCondition = "";
		
		if(status != null && status.length() > 0 && status.equalsIgnoreCase("UNREAD"))
			qCondition = " AND STATUS = 'UNREAD'";

		ArrayList<TWorkitemProgress> wiProgressList = new ArrayList<TWorkitemProgress>();
		try {
			conn = DBUtil.getECMDBConnection();
			stmt = conn.prepareStatement("SELECT * from ECM_WORKITEM_PROGRESS where WORKITEMID = " + wItemId 
					+ qCondition + " ORDER BY CreatedDate ASC");
			rs = stmt.executeQuery();

			while (rs.next()) {
				ECMProgress progress = new ECMProgress();
				progress.setId(rs.getInt("ID"));
				progress.setMessage(rs.getString("Message"));
				progress.setWorkitemId(rs.getInt("WorkitemId"));
				long empNo = rs.getInt("createdBy");
				progress.setEmpNo(empNo);
				progress.setCreatedBy(ECMUserList.getInstance().getLoginName(empNo));
				progress.setCreatedDate(rs.getTimestamp("CreatedDate"));
				wiProgressList.add(progress.getTransport());
				
				
			}
			return wiProgressList;

		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			rs.close();
			stmt.close();
			conn.close();
		}

	}
	

	public void saveProgress(TWorkitemProgress twp) throws Exception {
		ECMProgress progress = new ECMProgress();
		progress.getFromTransport(twp);
		progress.save();
	}
	
	public void updateProgress(long id) throws Exception {
		ECMProgress progress = new ECMProgress();
		progress.setId(id);
		progress.updateStatus();
	}
	
	public void removeProgress(long id) throws Exception {
		ECMProgress progress = new ECMProgress();
		progress.setId(id);
		progress.remove();
	}
	
	
}
