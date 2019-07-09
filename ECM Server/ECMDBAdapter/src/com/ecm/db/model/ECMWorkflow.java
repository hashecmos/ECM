package com.ecm.db.model;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;

import com.ecm.db.transport.TAttachment;
import com.ecm.db.transport.TRecipient;
import com.ecm.db.transport.TWorkflowDetails;
import com.ecm.db.transport.TWorkitemAction;
import com.ecm.db.util.DBUtil;

public class ECMWorkflow {
	private long id = 0;
	private long priority = 0;
	private String subject;
	private String remarks;
	private String keywords;
	private long role = 0;
	private long empNo = 0;
	private long delEmpNo = 0;
	private Date createdDate;
	private String documentFrom;
	private String documentTo;
	private Date documentDate;
	private Date documentReceivedDate;
	private String referenceNo;
	private String projectNo;
	private String contractNo;
	private String ECMNo;
	private String status;
	
	TWorkitemAction action = null;
	
	private ECMSentItem sentItem = null;
	
	private boolean isLoaded = false;
	public void setLoaded() { isLoaded = true; }
	
	public long getId() { return this.id; }
	public long getPriority() { return this.priority; }
	public String getSubject() { return this.subject; }
	public String getRemarks() { return this.remarks; }
	public String getKeywords() { return this.keywords; }
	public long getRole() { return this.role; }
	public long getEMPNo() { return this.empNo; }
	public long getDelEmpNo() { return this.delEmpNo; }
	public String getDocumentFrom() { return this.documentFrom; }
	public String getDocumentTo() { return this.documentTo; }
	public Date getCreatedDate() { return this.createdDate; }
	public Date getDocumentDate() { return this.documentDate; }
	public Date getDocumentReceivedDate() { return this.documentReceivedDate; }
	public String getReferenceNo() { return this.referenceNo; }
	public String getProjectNo() { return this.projectNo; }
	public String getContractNo() { return this.contractNo; }
	public String getECMNo() { return this.ECMNo; }
	public String getStatus() { return this.status; }
	public TWorkitemAction getAction() { return this.action; }
	
	public void setId(long id) { this.id = id; }
	public void setPriority(long priority) { this.priority = priority; }
	public void setSubject(String subject) { this.subject = subject; }
	public void setRemarks(String remarks) { this.remarks = remarks; }
	public void setKeywords(String keywords) { this.keywords = keywords; }
	public void setRole(long role) { this.role = role; }
	public void setEMPNo(long createdby) { this.empNo = createdby; }
	public void setDelEmpNo(long delCreatedBy) { this.delEmpNo = delCreatedBy; }
	public void setDocumentFrom(String docFrom) { this.documentFrom = docFrom; }
	public void setDocumentTo(String docTo) { this.documentTo = docTo; }
	public void setCreatedDate(Date createdDate) { this.createdDate = createdDate; }
	public void setDocumentDate(Date docDate) { this.documentDate = docDate; }
	public void setDocumentReceivedDate(Date receivedDate) { this.documentReceivedDate = receivedDate; }
	public void setReferenceNo(String refNo) { this.referenceNo = refNo; }
	public void setProjectNo(String projectNo) { this.projectNo = projectNo; }
	public void setContractNo(String contractNo) { this.contractNo = contractNo; }
	public void setECMNo(String ECMNo) { this.ECMNo = ECMNo; }
	public void setStatus(String status) { this.status = status; }
	public void setAction(TWorkitemAction wiaction) { this.action = wiaction; }
	
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
			sqlQuery = "SELECT * FROM ECM_WORKFLOW WHERE ID = ?";
			ps = conn.prepareStatement(sqlQuery);
			ps.setInt(1, (int)this.id);
			
			rs = ps.executeQuery();
			if (rs.next()) {
				
				setId(rs.getInt("ID"));
				setPriority(rs.getInt("Priority")); 
				setSubject(rs.getString("Subject"));
				setRemarks(rs.getString("Remarks"));
				setKeywords(rs.getString("Keywords"));
				setDocumentFrom(rs.getString("DocumentFrom"));
				setDocumentTo(rs.getString("DocumentTo"));
				setRole(rs.getInt("RoleID"));
				setEMPNo(rs.getInt("CreatedBy")); 
				setCreatedDate(rs.getTimestamp("CreatedDate"));
				setDocumentDate(rs.getDate("DocumentDate"));
				setDocumentReceivedDate(rs.getDate("DocumentReceivedDate"));
				setReferenceNo(rs.getString("ReferenceNo"));
				setProjectNo(rs.getString("ProjectNo"));
				setContractNo(rs.getString("ContractNo"));
				setECMNo(rs.getString("ECMNo"));
				setStatus(rs.getString("Status"));
				
				System.out.println(rs.getString("Subject"));
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
	
	private void validateInput() throws Exception {
		if(action == null)
			throw new Exception("No work actions provided!");
		if((action.recipients == null) || (action.recipients.size() <= 0))
			throw new Exception("No recipients provided!");
		if((action.attachments == null) || (action.attachments.size() <= 0))
			throw new Exception("No documents provided!");
	}
	
	public void launch() throws Exception {
		validateInput();
		createWorkflow();
		createSentWorkitem();
		ArrayList<Long> wiList = createWorkitems();
		addAttachments(wiList);
	}
	
	private void createWorkflow() throws Exception{
		Connection conn = null;
		CallableStatement cs = null;
		try {
			conn = DBUtil.getECMDBConnection();

			String SQLQuery = "{call ECM_CREATE_WORKFLOW(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)}";
			cs = conn.prepareCall(SQLQuery);
			
			cs.setInt(1, (int)this.priority);
			cs.setString(2, DBUtil.escapeString(this.subject));
			cs.setString(3, DBUtil.escapeString(this.remarks));
			cs.setString(4, DBUtil.escapeString(this.keywords));
			cs.setInt(5, (int)this.role);
			cs.setInt(6, (int)this.empNo);
			if(this.delEmpNo > 0)
				cs.setInt(6, (int)this.delEmpNo);
			cs.setString(7, DBUtil.escapeString(this.documentFrom));
			cs.setString(8, DBUtil.escapeString(this.documentTo));
			cs.setTimestamp(9, new Timestamp(this.documentDate.getTime()));
			cs.setTimestamp(10, new Timestamp(this.documentReceivedDate.getTime()));
			cs.setString(11, DBUtil.escapeString(this.referenceNo));
			cs.setString(12, DBUtil.escapeString(this.projectNo));
			cs.setString(13, DBUtil.escapeString(this.contractNo));
			cs.setString(14, DBUtil.escapeString(this.ECMNo));
			
			cs.registerOutParameter(15, java.sql.Types.INTEGER);
			cs.executeUpdate();

			this.setId(cs.getInt(15));
			System.out.println(this.getId());
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			cs.close();
			conn.close();
		}
	}
	
	private void createSentWorkitem() throws Exception {
		
		sentItem = new ECMSentItem();
		sentItem.setWorkflowId(this.id);
		sentItem.setSenderEmpNo(this.empNo);
		sentItem.setSenderRoleId(this.role);
		sentItem.setPriority(this.priority);
		sentItem.setRemarks(this.remarks);
		sentItem.insert();
		
		if(sentItem.getId() <= 0)
			throw new Exception("Error creating Sent Workitem");
	}
	
	private ArrayList<Long> createWorkitems() throws Exception {
		ArrayList<Long> wiList = new ArrayList<Long>();
		for(TRecipient tr:action.recipients) {
			ECMWorkItem wi = new ECMWorkItem();
			wi.setWorkflowId(this.id); 
			wi.getFromActionTransport(action);
			if(tr.userType.equalsIgnoreCase("ROLE"))
				wi.setRecipientRoleId(tr.id);
			else
				wi.setRecipientEMPNo(tr.id);
			wi.setType(tr.actionType);
			wi.setParentSentItemId(sentItem.getId());
			wi.setSystemStatus("ACTIVE");
			
			wi.insert(action.attachments);
			if(wi.getId() > 0)
				wiList.add(wi.getId());
		}
		return wiList;
	}
	
	private void addAttachments(ArrayList<Long> wiList) throws Exception {
		for(Long wiId:wiList) {
			for(TAttachment ta:action.attachments) {
				ECMAttachment att = new ECMAttachment();
				att.getFromTransport(ta);
				att.setWorkflowId(id);
				att.setWorkitemId(wiId);				
				att.insert();
			}
		}
	}
	
	public void finish() throws Exception {
		Connection conn = null;
		CallableStatement cs = null;
		try {
			conn = DBUtil.getECMDBConnection();

			String SQLQuery = "{call ECM_FINISH_WORKFLOW(?,?,?)}";
			cs = conn.prepareCall(SQLQuery);
			
			cs.setInt(1, (int)this.priority);
			cs.setInt(2, (int)this.empNo);
			cs.setInt(3, (int)this.role);

			cs.executeUpdate();

		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			cs.close();
			conn.close();
		}
	}
	
	public void getFromTransport(TWorkitemAction ta) {
		TWorkflowDetails tw = ta.workflow;
		this.priority=tw.priority;
		this.subject=tw.subject;
		this.remarks=tw.remarks;
		this.keywords=tw.keywords;
		this.role=tw.role;
		this.empNo=tw.empNo;
		this.delEmpNo = tw.delEmpNo;
		this.documentFrom=tw.docFrom;
		this.documentTo=tw.docTo;
		this.documentDate=tw.docDate;
		this.documentReceivedDate=tw.docRecDate;
		this.referenceNo=tw.refNo;
		this.projectNo=tw.projNo;
		this.contractNo=tw.contractNo;
		this.ECMNo=tw.ECMNo;
		this.action=ta;
	}
}
