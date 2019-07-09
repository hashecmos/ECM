package com.ecm.db.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Date;

import com.ecm.db.list.ECMWorkitemList;
import com.ecm.db.transport.TDocSign;
import com.ecm.db.util.DBUtil;

public class ECMDocSign {
	private long id;
	private String docId;
	private String inFile;
	private String outFile;
	private String status;
	private Date reqDate;
	private Date signDate;
	private String signDocId;
	private String empNo;
	private long roleId;
	private String message;
	private String docTitle;
	private long witemId;
	private long workflowId;
	private String docVsId;
	private String signType;
	
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getDocId() {
		return docId;
	}

	public void setDocId(String did) {
		this.docId = did;
	}
	
	public String getDocVsId() {
		return docVsId;
	}

	public void setDocVsId(String dvsid) {
		this.docVsId = dvsid;
	}
	
	public String getSignDocId() {
		return signDocId;
	}

	public void setSignDocId(String did) {
		this.signDocId = did;
	}
	
	public String getStatus() {
		return this.status;
	}

	public void setStatus(String stat) {
		this.status = stat;
	}
	
	public String getMessage() {
		return this.message;
	}

	public void setMessage(String msg) {
		this.message = msg;
	}
	
	public String getDocTitle() {
		return this.docTitle;
	}

	public void setDocTitle(String title) {
		this.docTitle = title;
	}
	
	public String getInFile() {
		return this.inFile;
	}

	public void setInFile(String filename) {
		this.inFile = filename;
	}

	public String getOutFile() {
		return this.outFile;
	}

	public void setOutFile(String filename) {
		this.outFile = filename;
	}
	
	public Date getReqDate() {
		return this.reqDate;
	}

	public void setReqDate(Date sdate) {
		this.reqDate = sdate;
	}
	
	public Date getSignDate() {
		return this.signDate;
	}

	public void setSignDate(Date sdate) {
		this.signDate = sdate;
	}

	public String getEmpNo() {
		return empNo;
	}

	public void setEmpNo(String empNo) {
		this.empNo = empNo;
	}
	
	public long getRoleId() {
		return roleId;
	}

	public void setRoleId(long rid) {
		this.roleId = rid;
	}
	
	public long getWitemId() {
		return witemId;
	}

	public void setWitemId(long witemId) {
		this.witemId = witemId;
	}
	
	public long getWorkflowId() {
		return workflowId;
	}

	public void setWorkflowId(long workflowId) {
		this.workflowId = workflowId;
	}
	
	public String getSignType() {
		return signType;
	}

	public void setSignType(String signType) {
		this.signType = signType;
	}

	private void insert() throws Exception {
		Connection conn = null;
		PreparedStatement ps = null;
		try {
			conn = DBUtil.getECMDBConnection();
			String sqlQuery = "INSERT INTO ECM_ESIGN_DOCS "
					+ "(ID, DOCID, STATUS, REQDATE, SIGNDATE, SIGNDOCID, EMPNO, "
					+ "ROLEID, MESSAGE, DOCTITLE, WORKFLOWID, WORKITEMID, DOCVSID, TYPE) "
					+ "VALUES (ECM_ESIGN_DOCS_SEQ.NEXTVAL, ?, 'PENDING', SYSTIMESTAMP, NULL, "
					+ "NULL, ?, ?, NULL, ?, ?, ?, ?, ?)";
			ps = conn.prepareStatement(sqlQuery);

			ps.setString(1, DBUtil.escapeString(this.docId));
			ps.setString(2,  DBUtil.escapeString(this.empNo));
			ps.setInt(3,  (int)this.roleId);
			ps.setString(4, DBUtil.escapeString(this.docTitle));
			ps.setInt(5, (int)this.workflowId);
			ps.setInt(6, (int)this.witemId);
			ps.setString(7, DBUtil.escapeString(this.docVsId));
			ps.setString(8, DBUtil.escapeString(this.signType));
			
			ps.executeUpdate();

		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			ps.close();
			conn.close();
		}
	}
	
	private void insertAll() throws Exception {
		Connection conn = null;
		PreparedStatement ps = null;
		try {
			conn = DBUtil.getECMDBConnection();
			String sqlQuery = "INSERT INTO ECM_ESIGN_DOCS "
					+ "(ID, DOCID, INFILE, OUTFILE, STATUS, REQDATE, SIGNDATE, SIGNDOCID, EMPNO, "
					+ "ROLEID, MESSAGE, DOCTITLE, WORKFLOWID, WORKITEMID, DOCVSID, TYPE) "
					+ "VALUES (ECM_ESIGN_DOCS_SEQ.NEXTVAL, ?, ?, ?, 'PENDING', SYSTIMESTAMP, NULL, "
					+ "NULL, ?, ?, NULL, ?, ?, ?, ?, ?)";
			ps = conn.prepareStatement(sqlQuery);

			ps.setString(1, DBUtil.escapeString(this.docId));
			ps.setString(2, DBUtil.escapeString(this.inFile));
			ps.setString(3, DBUtil.escapeString(this.outFile));
			ps.setString(4, DBUtil.escapeString(this.empNo));
			ps.setInt(5,  (int)this.roleId);
			ps.setString(6, DBUtil.escapeString(this.docTitle));
			ps.setInt(7, (int)this.workflowId);
			ps.setInt(8, (int)this.witemId);
			ps.setString(9, DBUtil.escapeString(this.docVsId));
			ps.setString(10, DBUtil.escapeString(this.signType));
			
			ps.executeUpdate();

		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			ps.close();
			conn.close();
		}
	}
	
	public void update() throws Exception {
		Connection conn = null;
		PreparedStatement ps = null;
		String sqlQuery = null;
		try {
			conn = DBUtil.getECMDBConnection();
			sqlQuery = "UPDATE ECM_ESIGN_DOCS SET INFILE = ?, OUTFILE = ?, WORKITEMID = ?, ROLEID = ? WHERE ID = ?";
			ps = conn.prepareCall(sqlQuery);
			
			ps.setString(1, DBUtil.escapeString(this.inFile));
			ps.setString(2, DBUtil.escapeString(this.outFile));
			ps.setInt(3, (int)this.witemId);
			ps.setInt(4, (int)this.roleId);
			ps.setInt(5, (int)this.id);
			ps.executeUpdate();

		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			ps.close();
			conn.close();
		}
	}

	public String save(boolean bInsert) throws Exception {
		
		if(bInsert)
		{
			insertAll();
			return "Success";
		}
		else
		{
			if (this.id <= 0)
			{
				insert();
				return "Success";
			}
			else
			{
				update();
				return "Success";
			}
		}
	}

	public long getRequestID() throws Exception {
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		long reqId = 0;
		try {
			conn = DBUtil.getECMDBConnection();
			String sqlQuery = "SELECT ID FROM ECM_ESIGN_DOCS WHERE DOCID = ? AND INFILE = ? AND "
					+ "OUTFILE = ? AND EMPNO = ?";
			ps = conn.prepareStatement(sqlQuery);

			ps.setString(1, DBUtil.escapeString(this.docId));
			ps.setString(2, DBUtil.escapeString(this.inFile));
			ps.setString(3, DBUtil.escapeString(this.outFile));
			ps.setString(4,  DBUtil.escapeString(this.empNo));
			
			rs = ps.executeQuery();
			if((rs!= null) && (rs.next())) {
				reqId = rs.getInt("ID");
			}
			
			return reqId;
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			rs.close();
			ps.close();
			conn.close();
		}
	}
	
	public long getRequestByWorkItem() throws Exception {
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		long reqId = 0;
		try {
			conn = DBUtil.getECMDBConnection();
			String sqlQuery = "SELECT ID FROM ECM_ESIGN_DOCS WHERE (DOCID = ? OR EMPNO = ?) "
					+ "AND (WORKFLOWID = ? OR WORKITEMID = ?)";
			ps = conn.prepareStatement(sqlQuery);

			ps.setString(1, DBUtil.escapeString(this.docId));
			ps.setString(2,  DBUtil.escapeString(this.empNo));
			ps.setInt(3, (int)this.workflowId);
			ps.setInt(4, (int)this.witemId);
			
			rs = ps.executeQuery();
			if((rs!= null) && (rs.next())) {
				reqId = rs.getInt("ID");
			}
			
			return reqId;
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			rs.close();
			ps.close();
			conn.close();
		}
	}
	
	public void setSignSuccess() throws Exception {
		Connection conn = null;
		PreparedStatement ps = null;
		String sqlQuery = null;
		try {
			conn = DBUtil.getECMDBConnection();
			sqlQuery = "UPDATE ECM_ESIGN_DOCS SET Status = 'SIGNED', SIGNDOCID = ?, MESSAGE = 'Success', "
					+ "SIGNDATE = SYSDATE WHERE ID = ?";
			ps = conn.prepareCall(sqlQuery);
			
			ps.setString(1, DBUtil.escapeString(this.signDocId));
			ps.setInt(2, (int)this.id);
			ps.executeUpdate();

		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			ps.close();
			conn.close();
		}
	}

	public void setSignFailure() throws Exception {
		Connection conn = null;
		PreparedStatement ps = null;
		String sqlQuery = null;
		try {
			conn = DBUtil.getECMDBConnection();
			sqlQuery = "UPDATE ECM_ESIGN_DOCS SET Status = 'FAILED', Message = ? WHERE ID = ?";
			ps = conn.prepareCall(sqlQuery);
			
			ps.setString(1,  this.message);
			ps.setInt(2, (int)this.id);
			ps.executeUpdate();

		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			ps.close();
			conn.close();
		}
	}
	
	public TDocSign getTransport() throws Exception {
		TDocSign td = new TDocSign();
		td.id = this.id;
		td.docId = this.docId;
		td.inFile = this.inFile;
		td.outFile = this.outFile;
		td.reqDate = this.reqDate;
		td.signDate = this.signDate;
		td.signDocId = this.signDocId;
		td.status = this.status;
		td.empNo = this.empNo;
		td.roleId = this.roleId;
		td.message = this.message;
		td.docTitle = this.docTitle;
		
		return td;
	}

	public void getFromTransport(TDocSign td) throws Exception {
		this.id = td.id;
		this.docId = td.docId;
		this.inFile = td.inFile;
		this.outFile = td.outFile;
		this.reqDate = td.reqDate;
		this.signDate = td.signDate;
		this.signDocId = td.signDocId;
		this.status = td.status;
		this.empNo = td.empNo;
		this.roleId = td.roleId;
		this.message = td.message;
		this.docTitle = td.docTitle;
	}	
	
	public void load() throws Exception {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;	
		try {
			conn = DBUtil.getECMDBConnection();
			if(this.id <= 0) {
				stmt = conn.prepareStatement("select * from ecm_esign_docs where ID = (select max(id) from ecm_esign_docs)");
			} else {
				stmt = conn.prepareStatement("SELECT * from ECM_ESIGN_DOCS WHERE ID = ?");
				stmt.setInt(1, (int) this.id);
			}
			rs = stmt.executeQuery();

			if (rs.next()) {
				this.id = rs.getInt("ID");
				this.docId = rs.getString("DOCID");
				this.inFile = rs.getString("INFILE");
				this.outFile = rs.getString("OUTFILE");
				this.status = rs.getString("STATUS");
				this.reqDate = rs.getTimestamp("REQDATE");
				this.signDate = rs.getTimestamp("SIGNDATE");
				this.signDocId = rs.getString("SIGNDOCID");
				this.empNo = rs.getString("EMPNO");
				this.roleId = rs.getInt("ROLEID");
				this.docTitle = rs.getString("DOCTITLE");
				this.signType = rs.getString("TYPE");
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
}
