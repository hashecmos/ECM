package com.ecm.db.model;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import com.ecm.db.transport.TAttachment;
import com.ecm.db.util.DBUtil;

public class ECMAttachment {
	private long id = 0;
	private long witemId = 0;
	private long wfId = 0;
	private String docId;
	private String docTitle;
	private String format;
	
	private boolean isLoaded = false;
	public void setLoaded() { this.isLoaded = true; }
	
	public long getId() {  return this.id; }
	public long getWorkitemId() { return this.witemId; }
	public long getWorkflowId() { return this.wfId; }
	public String getDocumentId() { return this.docId; }
	public String getDocumentTitle() { return this.docTitle; }
	public String getFormat() { return this.format; }
	
	public void setId(long id) { this.id = id; }
	public void setWorkitemId(long wid) { this.witemId = wid; }
	public void setWorkflowId(long wid) { this.wfId = wid; }
	public void setDocumentId(String did) { this.docId = did; }
	public void setDocumentTitle(String dt) { this.docTitle = dt; }
	public void setFormat(String ft) { this.format = ft; }
	
	public void load() throws Exception {
		if(isLoaded)
			return;
		if(id <= 0)
			return;
		
		Connection conn = null;
		ResultSet rs = null;
		String sqlQuery = null;
		PreparedStatement ps = null;
		try {
			conn = DBUtil.getECMDBConnection();
			sqlQuery = "SELECT * FROM ECM_WORKITEM_ATTACHMENT WHERE ID = ?";
			ps = conn.prepareStatement(sqlQuery);
			ps.setInt(1, (int)this.id);
			
			rs = ps.executeQuery();
			if (rs.next()) {
				
				setId(rs.getInt("ID"));
				setWorkitemId(rs.getInt("WorkItemID"));
				setWorkflowId(rs.getInt("WorkflowID"));
				setDocumentId(rs.getString("DocumentID"));
				setDocumentTitle(rs.getString("DocumentTitle"));
				setFormat(rs.getString("Format"));
				
				System.out.println(rs.getString("DocumentID"));
				isLoaded = true;
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
	
	public void insert() throws Exception {
		Connection conn = null;
		CallableStatement cs = null;
		try {
			conn = DBUtil.getECMDBConnection();

			String SQLQuery = "{call ECM_ADD_WORKITEM_ATTACHMENT(?,?,?,?,?)}";
			cs = conn.prepareCall(SQLQuery);
			
			cs.setInt(1, (int)this.witemId);
			cs.setString(2, DBUtil.escapeString(this.docId));
			cs.setString(3, DBUtil.escapeString(this.docTitle));
			cs.setString(4, DBUtil.escapeString(this.format));
			
			cs.registerOutParameter(5, java.sql.Types.INTEGER);
			cs.executeUpdate();

			this.setId(cs.getInt(5));
			System.out.println(this.getId());
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			cs.close();
			conn.close();
		}
	}
	
	public void update() throws Exception {
		Connection conn = null;
		PreparedStatement ps = null;
		try {
			conn = DBUtil.getECMDBConnection();

			String sqlQuery = "UPDATE ECM_WORKITEM_ATTACHMENT SET DocumentTitle = ?, "
					+ "Format = ? WHERE ID = ?";
			ps = conn.prepareStatement(sqlQuery);
			
			ps.setString(1, DBUtil.escapeString(this.docTitle));
			ps.setString(2, DBUtil.escapeString(this.format));
			ps.setInt(3, (int)this.id);
		
			ps.executeUpdate();

		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			ps.close();
			conn.close();
		}
	}
	
	public void update(String docID, String newDocId, String docTitle, String mimeType) throws Exception {
		Connection conn = null;
		PreparedStatement ps = null;
		try {
			conn = DBUtil.getECMDBConnection();

			String sqlQuery = "UPDATE ECM_WORKITEM_ATTACHMENT SET DOCUMENTTITLE = ? "
					+ ", FORMAT= ? , DOCUMENTID = ? WHERE DOCUMENTID = ?";
			ps = conn.prepareStatement(sqlQuery);
			ps.setString(1, DBUtil.escapeString(docTitle));
			ps.setString(2, DBUtil.escapeString(mimeType));
			ps.setString(3, DBUtil.escapeString(newDocId));
			ps.setString(4, DBUtil.escapeString(docID));
		
			ps.executeUpdate();

		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			ps.close();
			conn.close();
		}
	}
	
	public void delete() throws Exception {
		Connection conn = null;
		PreparedStatement ps = null;
		try {
			conn = DBUtil.getECMDBConnection();

			String sqlQuery = "DELETE FROM ECM_WORKITEM_ATTACHMENT WHERE ID = ?";
			ps = conn.prepareStatement(sqlQuery);
			
			ps.setInt(1, (int)this.id);
		
			ps.executeUpdate();

		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			ps.close();
			conn.close();
		}
	}
	
	public void getFromTransport(TAttachment tatt) {
		id = tatt.id;
		witemId = tatt.witemId;
		wfId = tatt.wfId;
		docId = tatt.docId;
		docTitle = tatt.docTitle;
		format = tatt.format;
	}
	
	public TAttachment getTransport() {
		TAttachment tatt = new TAttachment();
		tatt.id = id;
		tatt.witemId = witemId;
		tatt.wfId = wfId;
		tatt.docId = docId;
		tatt.docTitle = docTitle;
		tatt.format = format;
		return tatt;
	}
}
