package com.ecm.db.model;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;

import oracle.jdbc.OracleTypes;

import com.ecm.db.list.ECMConfigurationList;
import com.ecm.db.list.ECMRoleList;
import com.ecm.db.list.ECMUserList;
import com.ecm.db.list.ECMWorkitemList;
import com.ecm.db.transport.TAttachment;
import com.ecm.db.transport.TDocPrincipal;
import com.ecm.db.transport.TRecipient;
import com.ecm.db.transport.TWorkflowHistory;
import com.ecm.db.transport.TWorkitemAction;
import com.ecm.db.transport.TWorkitemDetails;
import com.ecm.db.util.DBUtil;
import com.ecm.db.util.ECMMail;

public class ECMWorkItem {
	
	//private static final String encKey = "A1E88EC43D525843D9EAF11C8EEE0684";
	
	private long id;
	private long workflowId;
	private long parentSentItemId;
	private String actions;
	private String status;
	private String instructions;
	private String type;
	private Date readDate;
	private Date deadline;
	private Date reminder;
	private Date createdDate;
	private long roleId;
	private long EMPNo;
	private long recipientEMPNo;
	private long recipientRoleId;
	private String systemStatus;
	private String actionDetails;
	private long priority;
	private String actionTaken;
	private String remarks;
	
	private ECMMail mailObject = new ECMMail();
	private Boolean mailConfigLoaded = false;
	
	private ArrayList<TRecipient> recipients = null;
	private ArrayList<TAttachment> attachments = null;
	
	public long getId() { return this.id; }
	public long getWorkflowId() { return this.workflowId; }
	public long getParentSentItemId() { return this.parentSentItemId; }
	public String getActions() { return this.actions; }
	public String getStatus() { return this.status; }
	public String getInstructions() { return this.instructions; }
	public String getType() { return this.type; }
	public Date getReadDate() { return this.readDate; }
	public Date getDeadline() { return this.deadline; }
	public Date getReminder() { return this.reminder; }
	public Date getCreatedDate() { return this.createdDate; }
	public long getRoleId() { return this.roleId; }
	public long getEMPNo() { return this.EMPNo; }
	public long getRecipientEMPNo() { return this.recipientEMPNo; }
	public long getRecipientRoleId() { return this.recipientRoleId; }
	public String getSystemStatus() { return this.systemStatus; }
	public String getActionDetails() { return this.actionDetails; }
	public long getPriority() { return priority; }
	public String getActionTaken() { return actionTaken; }
	public String getRemarks() { return remarks; }

	public void setId(long id) { this.id = id; }
	public void setWorkflowId(long id) { this.workflowId = id; }
	public void setParentSentItemId(long id) { this.parentSentItemId = id; }
	public void setActions(String act) { this.actions = act; }
	public void setStatus(String stat) { this.status = stat; }
	public void setInstructions(String ins) { this.instructions = ins; }
	public void setType(String type) { this.type = type; }
	public void setReadDate(Date dt) { this.readDate = dt; }
	public void setDeadline(Date dt) { this.deadline = dt; }
	public void setReminder(Date dt) { this.reminder = dt; }
	public void setCreatedDate(Date dt) { this.createdDate = dt; }
	public void setRoleId(long id) { this.roleId = id; }
	public void setEMPNo(long no) { this.EMPNo = no; }
	public void setRecipientEMPNo(long no) { this.recipientEMPNo = no; }
	public void setRecipientRoleId(long id) { this.recipientRoleId = id; }
	public void setSystemStatus(String stat) { this.systemStatus = stat; }
	public void setActionDetails(String ad) { this.actionDetails = ad; }
	public void setPriority(long priority) { this.priority = priority; }
	public void setActionTaken(String actionTaken) { this.actionTaken = actionTaken; }
	public void setRemarks(String remarks) { this.remarks = remarks; }
	
	public void insert(ArrayList<TAttachment> attachments) throws Exception {
		Connection conn = null;
		CallableStatement cs = null;
		try {
			conn = DBUtil.getECMDBConnection();

			String SQLQuery = "{call ECM_CREATE_WORKITEM(?,?,?,?,?,?,?,?,?,?,?,?,?,?,1,0,?,?)}";
			cs = conn.prepareCall(SQLQuery);
			
			cs.setInt(1, (int)this.workflowId); 
			cs.setInt(2, (int)this.parentSentItemId);
			cs.setString(3, DBUtil.escapeString(this.actions));
			cs.setString(4, DBUtil.escapeString(this.instructions));
			cs.setString(5, DBUtil.escapeString(this.type));
			cs.setInt(6, (int)this.EMPNo);
			cs.setInt(7, (int)this.recipientEMPNo);
			cs.setInt(8, (int)this.roleId);
			cs.setInt(9, (int)this.recipientRoleId);
			if(this.deadline == null)
				cs.setString(10,  null);
			else
				cs.setTimestamp(10, new Timestamp(this.deadline.getTime()));
			if(this.reminder == null)
				cs.setString(11,  null);
			else
				cs.setTimestamp(11, new Timestamp(this.reminder.getTime()));
			
			cs.setString(12, this.status);
			cs.setString(13, this.actionDetails);
			cs.setString(14, this.systemStatus);
			cs.setString(15, this.remarks);
			
			cs.registerOutParameter(16, java.sql.Types.INTEGER);
			cs.executeUpdate();

			this.setId(cs.getInt(16));
			System.out.println(this.getId());
			
			sendEmail("LAUNCH", this.recipientEMPNo, this.recipientRoleId, this.id, attachments);
			
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			cs.close();
			conn.close();
		}
	}
	
	public void read(long empNo) throws Exception {
		Connection conn = null;
		CallableStatement cs = null;
		try {
			conn = DBUtil.getECMDBConnection();

			String SQLQuery = "{call ECM_READ_WORKITEM(?,?,?)}";
			cs = conn.prepareCall(SQLQuery);
			
			cs.setInt(1, (int)this.id); 
			cs.setInt(2, (int)empNo);
			cs.setInt(3, 0);
		
			cs.executeUpdate();

		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			cs.close();
			conn.close();
		}
	}
	
	public long getParentSentItem() throws Exception {
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		long sentItem = 0;
		try {
			conn = DBUtil.getECMDBConnection();

			ps = conn.prepareStatement("SELECT PARENTSENTITEMID FROM ECM_WORKITEM WHERE ID = ?");
			ps.setInt(1, (int)this.id);
			rs = ps.executeQuery();
			if((rs != null) && rs.next()) {
				sentItem = rs.getInt("PARENTSENTITEMID");
			}
			
			return sentItem;
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			rs.close();
			ps.close();
			conn.close();
		}
	}
	
	public String getWorkitemStatus() throws Exception {
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		String result = "ACTIVE";

		try {
			conn = DBUtil.getECMDBConnection();
			
			ps = conn.prepareStatement("SELECT SYSTEMSTATUS FROM ECM_WORKITEM WHERE ID = ?");
			ps.setInt(1, (int)this.id);
			rs = ps.executeQuery();
			String sysStatus = "";
			if((rs != null) && rs.next()) {
				sysStatus = rs.getString("SYSTEMSTATUS");
			}
			
			if(sysStatus != null && sysStatus.length() > 0)
			{
				if(sysStatus.equalsIgnoreCase("ACTIVE") || sysStatus.equalsIgnoreCase("FINISH") )
					result = "ACTIVE";
				else
					result = "INACTIVE";	
			}

			return result;
			
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			rs.close();
			ps.close();
			conn.close();
		}
	}
	
	public long getChildSentItem() throws Exception {
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		long sentItem = 0;
		try {
			conn = DBUtil.getECMDBConnection();

			ps = conn.prepareStatement("SELECT ID FROM ECM_WORKITEM_SENT WHERE PARENTITEMID = ?");
			ps.setInt(1, (int)this.id);
			rs = ps.executeQuery();
			if((rs != null) && rs.next()) {
				sentItem = rs.getInt("ID");
			}
			
			return sentItem;
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			rs.close();
			ps.close();
			conn.close();
		}
	}
	
	public int updateSentItemStatus(long sentItemId) throws Exception {
		Connection conn = null;
		CallableStatement cs = null;
		String SQLQuery;
		try {
			conn = DBUtil.getECMDBConnection();

			SQLQuery = "{call ECM_UPDATE_WORKITEM_SENT(?,?)}";
			cs = conn.prepareCall(SQLQuery);
			
			cs.setInt(1, (int)sentItemId); 
			cs.registerOutParameter(2, java.sql.Types.INTEGER);
			cs.executeUpdate();
			
			return cs.getInt(2);
			
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			cs.close();
			conn.close();
		}
	}
	
	public ArrayList<TDocPrincipal> recall() throws Exception {
		Connection conn = null;
		CallableStatement cs = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		ArrayList<TDocPrincipal> retList = null;
		try {
			conn = DBUtil.getECMDBConnection();
			
			String SQLQuery = "{call ECM_RECALL_WORKITEM(?,?,?,?)}";
			cs = conn.prepareCall(SQLQuery);
			
			cs.setInt(1, (int)this.id); 
			cs.setInt(2, (int)this.EMPNo);
			cs.setInt(3, (int)this.roleId);
			//cs.setBoolean(4,  true);
			cs.setInt(4, 1);
			cs.executeUpdate();

			ps = conn.prepareStatement("SELECT DOCID, USERID, USERTYPE, ISANNOT FROM TEMP_RECALLED_DOCPRINCIPAL");
			rs = ps.executeQuery();
			while((rs != null) && rs.next()) {
				TDocPrincipal tdp = new TDocPrincipal();
				tdp.docId = rs.getString("DocId");
				tdp.pType = rs.getString("UserType");
				tdp.principal = rs.getInt("UserId");
				tdp.isAnnotOnly = rs.getString("ISANNOT");
				if(retList == null)
					retList = new ArrayList<TDocPrincipal>();
				retList.add(tdp);
			}
			
			
			sendRecallEmail("RECALL", this.id);
			
			return retList;
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			rs.close();
			ps.close();
			cs.close();
			conn.close();
		}
	}
	
	public long reply(boolean replyAll) throws Exception {
		Connection conn = null;
		CallableStatement cs = null;
		String SQLQuery;
		try {
			conn = DBUtil.getECMDBConnection();

			if(replyAll)			
				SQLQuery = "{call ECM_REPLY_ALL_WORKITEMS(?,?,?,?,?,?)}";
			else
				SQLQuery = "{call ECM_REPLY_WORKITEM(?,?,?,?,?,?)}";
			cs = conn.prepareCall(SQLQuery);
			
			cs.setInt(1, (int)this.id); 
			cs.setString(2, DBUtil.escapeString(this.instructions));
			cs.setInt(3, (int)this.EMPNo);
			cs.setInt(4, (int)this.roleId);
			cs.setString(5, DBUtil.escapeString(this.systemStatus));
		
			cs.registerOutParameter(6, java.sql.Types.INTEGER);
			cs.executeUpdate();
			
			return cs.getInt(6);
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			cs.close();
			conn.close();
		}
	}
	
	public void getFromActionTransport(TWorkitemAction twi)throws Exception
	{
		try {
			id = twi.id;
			actions = twi.actions;
			instructions=twi.instructions;
			deadline=twi.deadline;
			reminder=twi.reminder;
			roleId=twi.roleId;
			EMPNo=twi.EMPNo;
			actionDetails=twi.actionDetails;
			priority = twi.priority;
			actionTaken = twi.actionTaken;
			remarks = twi.wiRemarks;
			
			recipients = twi.recipients;
			attachments = twi.attachments;
		} catch (Exception e) {
			throw new Exception("Data conversion error! " + e.getMessage());
		}
	}
	
	private ECMWorkItem cloneForAction()
	{
		ECMWorkItem clone = new ECMWorkItem();
		clone.setId(this.id);
		clone.setActions(this.actions);
		clone.setInstructions(this.instructions);
		clone.setDeadline(this.deadline);
		clone.setReminder(reminder);
		clone.setRoleId(this.roleId);
		clone.setEMPNo(this.EMPNo);
		clone.setActionDetails(this.actionDetails);
		clone.setPriority(this.priority);
		clone.setActionTaken(this.actionTaken);
		clone.setRemarks(this.remarks);
		return clone;
	}
	
	public void forward() throws Exception {
		if(recipients == null)
			throw new Exception("Recipients cannot be empty!");
		
		long sentId = createSentWorkitem();
		ArrayList<Long> wiList = createForwardWorkitems(sentId);
		addAttachments(wiList);
	}
	
	public void reply() throws Exception {
		if(recipients == null)
			throw new Exception("Recipients cannot be empty!");
		
		long sentId = createSentWorkitem();
		ArrayList<Long> wiList = createReplyWorkitems(sentId);
		addAttachments(wiList);
	}
	
	public void archive(long empNo, long roleId) throws Exception{
		Connection conn = null;
		CallableStatement cs = null;
		try {
			conn = DBUtil.getECMDBConnection();

			String SQLQuery = "{call ECM_ARCHIVE_WORKITEM(?,?,?,?)}";
			cs = conn.prepareCall(SQLQuery);
			
			cs.setInt(1, (int)this.id);
			cs.setInt(2, (int)empNo);
			cs.setInt(3, (int)roleId);
			cs.setInt(4, 1);
			cs.executeUpdate();

		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			cs.close();
			conn.close();
		}
	}
	
	public String archiveBefore(String empNo, String roleId, String bDate) throws Exception{
		Connection conn = null;
		CallableStatement cs = null;
		String strMessage = "";
		long wiCount = 0;
		try {
			conn = DBUtil.getECMDBConnection();
			
			wiCount = ECMWorkitemList.getInstance().getArchiveWorkitemsCount(bDate, roleId, empNo);
			
			if(wiCount > 0)
			{
				String SQLQuery = "{call ECM_ARCHIVE_BEFORE_WORKITEM(?,?,?)}";
				cs = conn.prepareCall(SQLQuery);
	
				cs.setInt(1, (int)DBUtil.stringToLong(empNo));
				cs.setInt(2, (int)DBUtil.stringToLong(roleId));
				cs.setTimestamp(3, DBUtil.convertStringtoDate(bDate));
				cs.executeUpdate();
				strMessage = "OK - " + wiCount;
			}
			else{
				strMessage = "Workitems not found";
			}
			return strMessage;

		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			if(wiCount > 0)
				cs.close();
			conn.close();
		}
	}
	
	public void finish(long empNo, long roleId) throws Exception{
		Connection conn = null;
		CallableStatement cs = null;
		try {
			conn = DBUtil.getECMDBConnection();

			String SQLQuery = "{call ECM_FINISH_WORKITEM(?,?,?,?)}";
			cs = conn.prepareCall(SQLQuery);
			
			cs.setInt(1, (int)this.id);
			cs.setInt(2, (int)empNo);
			cs.setInt(3, (int)roleId);
			cs.setInt(4, 1);
			cs.executeUpdate();

		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			cs.close();
			conn.close();
		}
	}
	
	public String finishBefore(String empNo, String roleId, String bDate) throws Exception{
		Connection conn = null;
		CallableStatement cs = null;
		String strMessage = "";
		long wiCount = 0;
		try {
			conn = DBUtil.getECMDBConnection();
			
			wiCount = ECMWorkitemList.getInstance().getArchiveWorkitemsCount(bDate, roleId, empNo);
			
			if(wiCount > 0)
			{
				String SQLQuery = "{call ECM_FINISH_BEFORE_WORKITEM(?,?,?)}";
				cs = conn.prepareCall(SQLQuery);
	
				cs.setInt(1, (int)DBUtil.stringToLong(empNo));
				cs.setInt(2, (int)DBUtil.stringToLong(roleId));
				cs.setTimestamp(3, DBUtil.convertStringtoDate(bDate));
				cs.executeUpdate();
				strMessage = "OK - " + wiCount;
			}
			else{
				strMessage = "Workitems not found";
			}
			return strMessage;

		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			if(wiCount > 0)
				cs.close();
			conn.close();
		}
	}
	
	private long createSentWorkitem() throws Exception {
		
		long sentitemID = getChildSentItem();
		if(sentitemID > 0){
			updateSentItemStatus(sentitemID);
			return sentitemID;
		}
		
		ECMSentItem sentItem = new ECMSentItem();

		sentItem.setSenderEmpNo(this.EMPNo);
		sentItem.setSenderRoleId(this.roleId);
		sentItem.setParentItemId(this.id);
		sentItem.setPriority(this.priority);
		sentItem.setActionTaken(this.actionTaken);
		sentItem.setRemarks(this.remarks);
		
		sentItem.insert();
		if(sentItem.getId() <= 0)
			throw new Exception("Error creating Sent Workitem");
		return sentItem.getId();
	}
	
	private ArrayList<Long> createForwardWorkitems(long sentId) throws Exception {
		ArrayList<Long> wiList = new ArrayList<Long>();
		for(TRecipient tr:recipients) {
			ECMWorkItem wi = cloneForAction();
			if(tr.userType.equalsIgnoreCase("ROLE"))
				wi.setRecipientRoleId(tr.id);
			else
				wi.setRecipientEMPNo(tr.id);
			wi.setType(tr.actionType);
			wi.setParentSentItemId(sentId);
			long retId = wi.createForwardWorkitem(attachments);
			if(retId > 0) {
				wi.setId(retId);
				wiList.add(retId);
			}
		}
		return wiList;
	}
	
	private ArrayList<Long> createReplyWorkitems(long sentId) throws Exception {
		ArrayList<Long> wiList = new ArrayList<Long>();
		for(TRecipient tr:recipients) {
			ECMWorkItem wi = cloneForAction();
			if(tr.userType.equalsIgnoreCase("ROLE"))
				wi.setRecipientRoleId(tr.id);
			else
				wi.setRecipientEMPNo(tr.id);
			wi.setParentSentItemId(sentId);
			wi.setType(tr.actionType);
			long retId = wi.createReplyWorkitem(attachments);
			if(retId > 0) {
				wi.setId(retId);
				wiList.add(retId);
			}
		}
		return wiList;
	}
	
	private void addAttachments(ArrayList<Long> wiList) throws Exception {
		for(Long wiId:wiList) {
			for(TAttachment ta:attachments) {
				ECMAttachment att = new ECMAttachment();
				att.getFromTransport(ta);
				att.setWorkitemId(wiId);
				
				att.insert();
			}
		}
	}
	
	private long createForwardWorkitem(ArrayList<TAttachment> attachment) throws Exception {
		Connection conn = null;
		CallableStatement cs = null;
		try {
			conn = DBUtil.getECMDBConnection();
			
			String SQLQuery = "{call ECM_FORWARD_WORKITEM(?,?,?,?,?,?,?,?,?,?,?,?,?,?)}";
			cs = conn.prepareCall(SQLQuery);
			
			cs.setInt(1, (int)this.id); 
			cs.setInt(2, (int)this.parentSentItemId); 
			cs.setString(3, DBUtil.escapeString(this.actions)); 
			cs.setString(4, DBUtil.escapeString(this.instructions)); 
			cs.setString(5, DBUtil.escapeString(this.type)); 
			if(this.deadline == null)
				cs.setString(6,  null);
			else
				cs.setTimestamp(6, new Timestamp(this.deadline.getTime()));
			if(this.reminder == null)
				cs.setString(7,  null);
			else
				cs.setTimestamp(7, new Timestamp(this.reminder.getTime()));
			cs.setInt(8, (int)this.EMPNo);
			cs.setInt(9, (int)this.roleId);
			cs.setInt(10, (int)this.recipientEMPNo);
			cs.setInt(11, (int)this.recipientRoleId);
			cs.setString(12, DBUtil.escapeString(this.systemStatus)); 
			cs.setString(13, DBUtil.escapeString(this.remarks));
			
			cs.registerOutParameter(14, java.sql.Types.INTEGER);
			cs.executeUpdate();
			
			long fwdId = cs.getInt(14);
			
			sendEmail("FORWARD", this.recipientEMPNo, this.recipientRoleId, fwdId, attachments);
			
			return fwdId;

		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			cs.close();
			conn.close();
		}
	}
	
	private long createReplyWorkitem(ArrayList<TAttachment> attachments) throws Exception {
		Connection conn = null;
		CallableStatement cs = null;
		try {
			conn = DBUtil.getECMDBConnection();
			
			String SQLQuery = "{call ECM_REPLY_WORKITEM(?,?,?,?,?,?,?,?,?,?,?,?)}";
			cs = conn.prepareCall(SQLQuery);
			
			cs.setInt(1, (int)this.id); 
			cs.setInt(2, (int)this.parentSentItemId); 
			cs.setString(3, DBUtil.escapeString(this.instructions)); 
			cs.setInt(4, (int)this.EMPNo);
			cs.setString(5, DBUtil.escapeString("Reply-" + this.type)); 
			cs.setInt(6, (int)this.roleId);
			cs.setInt(7, (int)this.recipientEMPNo);
			cs.setInt(8, (int)this.recipientRoleId);
			cs.setString(9, DBUtil.escapeString(this.systemStatus)); 
			cs.setString(10, DBUtil.escapeString(this.actions));
			cs.setString(11, DBUtil.escapeString(this.remarks));
			
			cs.registerOutParameter(12, java.sql.Types.INTEGER);
			cs.executeUpdate();
			
			long replyId = cs.getInt(12);
			
			sendEmail("REPLY", this.recipientEMPNo, this.recipientRoleId, replyId, attachments);
			
			return replyId;

		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			cs.close();
			conn.close();
		}
	}
	
	public void addUser() throws Exception {
		if(recipients == null)
			throw new Exception("Recipients cannot be empty!");
		
		ArrayList<Long> wiList = createAddUserWorkitems();
		addAttachments(wiList);
	}
	
	private ArrayList<Long> createAddUserWorkitems() throws Exception {
		ArrayList<Long> wiList = new ArrayList<Long>();
		for(TRecipient tr:recipients) {
			ECMWorkItem wi = cloneForAction();
			if(tr.userType.equalsIgnoreCase("ROLE"))
				wi.setRecipientRoleId(tr.id);
			else
				wi.setRecipientEMPNo(tr.id);
			wi.setType(tr.actionType);
			long retId = wi.createAddUserWorkitem(attachments);
			if(retId > 0) {
				wi.setId(retId);
				wiList.add(retId);
			}
		}
		return wiList;
	}
	
	private long createAddUserWorkitem(ArrayList<TAttachment> attachments) throws Exception {
		Connection conn = null;
		CallableStatement cs = null;
		try {
			conn = DBUtil.getECMDBConnection();
			
			String SQLQuery = "{call ECM_ADDUSER_WORKITEM(?,?,?,?,?,?,?,?,?,?,?,?,?)}";
			cs = conn.prepareCall(SQLQuery);
			
			cs.setInt(1, (int)this.id); 
			cs.setString(2, DBUtil.escapeString(this.actions)); 
			cs.setString(3, DBUtil.escapeString(this.instructions)); 
			cs.setString(4, DBUtil.escapeString(this.type)); 
			if(this.deadline == null)
				cs.setString(5,  null);
			else
				cs.setTimestamp(5, new Timestamp(this.deadline.getTime()));
			if(this.reminder == null)
				cs.setString(6,  null);
			else
				cs.setTimestamp(6, new Timestamp(this.reminder.getTime()));
			cs.setInt(7, (int)this.EMPNo);
			cs.setInt(8, (int)this.roleId);
			cs.setInt(9, (int)this.recipientEMPNo);
			cs.setInt(10, (int)this.recipientRoleId);
			cs.setString(11, DBUtil.escapeString(this.systemStatus));
			cs.setString(12, DBUtil.escapeString(this.remarks));
			
			cs.registerOutParameter(13, java.sql.Types.INTEGER);
			cs.executeUpdate();
			
			long fwdId = cs.getInt(13);
			
			sendEmail("ADDUSER", this.recipientEMPNo, this.recipientRoleId, fwdId, attachments);
			
			return fwdId;

		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			cs.close();
			conn.close();
		}
	}
	
	public TWorkitemDetails getDetails() throws Exception {
		if(this.id <= 0)
			return null;
		TWorkitemDetails wi = null;
		
		Connection conn = null;
		ResultSet rs = null;
		String sqlQuery = null;
		CallableStatement callableStatement = null;
		try {
			conn = DBUtil.getECMDBConnection();
			sqlQuery = "{call ECM_GET_WORKITEM_DETAILS(?,?)}";
			callableStatement = conn.prepareCall(sqlQuery);
			callableStatement.setInt(1, (int)this.id);
			callableStatement.registerOutParameter(2, OracleTypes.CURSOR);
			callableStatement.execute();
			rs = (ResultSet)callableStatement.getObject(2);

			if (rs.next()) {
				wi = new TWorkitemDetails();
				wi.workitemId=this.id;
				wi.priority=rs.getInt("Priority");
				wi.subject=rs.getString("Subject");
				wi.remarks=rs.getString("Remarks"); 
				wi.Keywords=rs.getString("Keywords");
				wi.createdOn=rs.getTimestamp("CreatedDate");
				wi.docFrom=rs.getString("DocumentFrom");
				wi.docTo=rs.getString("DocumentTo");
				wi.docDate=rs.getTimestamp("DocumentDate");
				wi.docRecdDate=rs.getTimestamp("DocumentReceivedDate");
				wi.refNo=rs.getString("ReferenceNo");
				wi.projNo=rs.getString("ProjectNo");
				wi.contractNo=rs.getString("ContractNo");
				wi.ECMNo=rs.getString("ECMNo");
				wi.workflowId=rs.getInt("WorkflowID");
				wi.SentItemId=rs.getInt("ParentSentItemID");
				wi.actions=rs.getString("Actions"); 
				wi.status=rs.getString("Status");
				wi.instructions=rs.getString("Instructions");
				wi.type=rs.getString("Type");
				wi.deadline=rs.getTimestamp("Deadline");
				wi.reminder=rs.getTimestamp("Reminder");
				wi.receivedDate=rs.getTimestamp("ReceivedDate");
				wi.senderRoleId=rs.getInt("SenderRoleID"); 
				wi.senderEMPNo=rs.getInt("SenderEMPNo");
				wi.recipientEMPNo=rs.getInt("RecipientEMPNo");
				wi.recipientRoleId=rs.getInt("RecipientRoleID"); 
				wi.systemStatus=rs.getString("SystemStatus");
				wi.wfCreatorName=rs.getString("WFCreatorName");
				wi.recipientName=rs.getString("RecipientName");
				wi.senderName=rs.getString("SenderName"); 
				wi.recipientRoleName=rs.getString("RecipientRoleName");
				wi.senderRoleName=rs.getString("SenderRoleName");
				wi.comments=rs.getString("Comments");
				wi.actionTaken=rs.getString("ActionTaken");
				wi.wiRemarks = rs.getString("WIRemarks"); 
			}  
			rs.close();
			callableStatement.close();
			conn.close();
			
			wi.attachments = getAttachments();
			wi.recipients = getRecipients();
			
			return wi;
		} catch (Exception e) {
			e.printStackTrace();
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			rs.close();
			callableStatement.close();
			conn.close();
		}
	}
	
	public ArrayList<TAttachment> getAttachments() throws Exception {
		if (this.id <= 0)
			return attachments;
		
		if(attachments != null)
			attachments.clear();
		if(attachments == null)
			attachments = new ArrayList<TAttachment>();
		
		Connection conn = null;
		ResultSet rs = null;
		String sqlQuery = null;
		PreparedStatement ps = null;
		try {
			conn = DBUtil.getECMDBConnection();
			sqlQuery = "SELECT * FROM ECM_WORKITEM_ATTACHMENT WHERE WorkItemID = ?";
			ps = conn.prepareStatement(sqlQuery);
			ps.setInt(1, (int)this.id);
			
			rs = ps.executeQuery();
			while (rs.next()) {
				TAttachment ta = new TAttachment();
				ta.id = rs.getInt("ID");
				ta.witemId = this.id;
				ta.wfId = rs.getInt("WorkflowID");
				ta.docId = rs.getString("DocumentID");
				ta.docTitle = rs.getString("DocumentTitle");
				ta.format = rs.getString("Format");
				
				attachments.add(ta);
			} 
			
			return attachments;

		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			rs.close();
			ps.close();
			conn.close();
		}
	}
	
	public ArrayList<TWorkflowHistory> getHistory(long empNo) throws Exception {
		if(this.id <= 0)
			return null;
		
		ArrayList<TWorkflowHistory> hList = null;
		
		Connection conn = null;
		ResultSet rs = null;
		String sqlQuery = null;
		CallableStatement callableStatement = null;
		try {
			conn = DBUtil.getECMDBConnection();
			sqlQuery = "{call ECM_GET_WORKITEM_HISTORY(?,?)}";
			callableStatement = conn.prepareCall(sqlQuery);
			callableStatement.setInt(1, (int)this.id);
			callableStatement.registerOutParameter(2, OracleTypes.CURSOR);
			callableStatement.execute();
			rs = (ResultSet)callableStatement.getObject(2);
			while (rs.next()) {
				TWorkflowHistory hist = new TWorkflowHistory();
				hist.id = rs.getInt("ID");
				hist.workitemId = rs.getInt("WorkItemID");
				hist.timeStamp = DBUtil.formatDateForUI(rs.getTimestamp("ActionTimestamp"));
				hist.details = rs.getString("Details");
				hist.actionBy = rs.getString("UserName");
				if(hist.actionBy == null)
					hist.actionBy = rs.getString("RoleName");
				
				hist.recipientName = rs.getString("RecipientUser");
				if(hist.recipientName == null)
					hist.recipientName = rs.getString("RecipientRole");
				
				if(hList == null)
					hList = new ArrayList<TWorkflowHistory>();
				
//				long actionUserId =  rs.getLong("ActionUser");
//				String actionDetails = hist.details;
//				if((actionDetails.equalsIgnoreCase("RECALL") || actionDetails.equalsIgnoreCase("RETURN")) 
//						&& (actionUserId != empNo))
//					continue;
//				else
				hList.add(hist);
			}  	
			return hList;
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			rs.close();
			callableStatement.close();
			conn.close();
		}
	}
	
	public ArrayList<TRecipient> getRecipients() throws Exception {
		if (this.id <= 0)
			return recipients;
		
		if(recipients != null)
			recipients.clear();
	
		Connection conn = null;
		ResultSet rs = null;
		String sqlQuery = null;
		CallableStatement callableStatement = null;
		try {
			conn = DBUtil.getECMDBConnection();
			sqlQuery = "{call ECM_GET_WORKITEM_RECIPIENTS(?,?)}";
			callableStatement = conn.prepareCall(sqlQuery);
			callableStatement.setInt(1, (int)this.id);
			callableStatement.registerOutParameter(2, OracleTypes.CURSOR);
			callableStatement.execute();
			rs = (ResultSet)callableStatement.getObject(2);
			while (rs.next()) {
				TRecipient tr = new TRecipient();
				tr.actionType = rs.getString("Type");
				tr.id = rs.getInt("RecipientEMPNo");
				tr.name = rs.getString("UserName");
				if(tr.name == null) {
					tr.id = rs.getInt("RecipientRoleID");
					tr.name = rs.getString("RoleName");
					tr.userType = "ROLE";
				} else
					tr.userType = "USER";
				
				if(recipients == null)
					recipients = new ArrayList<TRecipient>();
				recipients.add(tr);
			}  
			
			return recipients;
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			rs.close();
			callableStatement.close();
			conn.close();
		}
	}
	
	private void sendEmail(String actionType, long empNo, long roleId, long witemId, ArrayList<TAttachment> attachment) {
		try {
			loadEmailConfigurations();
			
			ECMConfigurationList cfgList = ECMConfigurationList.getInstance("ECM", "APP");
			String subject = cfgList.getConfigValue(actionType + "SUB");
			String messageFile = cfgList.getConfigValue(actionType + "MSG");
			String viewerURL = cfgList.getConfigValue("WXTURL");
			ECMConfigurationList cfgSysList = ECMConfigurationList.getInstance("ECM", "SYSTEM");
			String objStoreName = cfgSysList.getConfigValue("ECMOS");
			
			StringBuilder contentBuilder = new StringBuilder();
			try {
				ClassLoader loader = Thread.currentThread().getContextClassLoader();
				URL url = loader.getResource(messageFile);

			    BufferedReader in = new BufferedReader(new FileReader(URLDecoder.decode(url.getPath())));
			    String str;
			    while ((str = in.readLine()) != null) {
			        contentBuilder.append(str);
			    }
			    in.close();
			} catch (IOException e) {
				e.printStackTrace();
				throw new Exception(e.getMessage());
			}
			String message = contentBuilder.toString();
			
			if((subject == null) || (subject.length() <= 0))
				subject = "ECM Work item #WITMID#";
			
			if((message == null) || (message.length() <= 0))
				message = "You have a work item - (#WITMID#) in ECM.";
			
			ECMWorkItem ewi = new ECMWorkItem();
			ewi.setId(witemId);
			TWorkitemDetails twd = ewi.getDetails();
			
			subject = subject.replace("#SUBJECT#", twd.subject + "");
			
			message = message.replace("#SUBJECT#", twd.subject + "");
			
			StringBuilder msgBody = new StringBuilder();
			
			if(twd.senderName != null)
				msgBody.append(" <font style='color:gray'>Sender:</font> " + twd.senderName + "<br />");
			else
				msgBody.append(" <font style='color:gray'>Sender:</font> " + twd.senderRoleName + "<br />");
			msgBody.append(" <font style='color:gray'>Subject:</font> " + twd.subject + "<br />");
			msgBody.append("<font style='color:gray'>For :</font> " + twd.actions + "<br />");
			msgBody.append("<font style='color:gray'>Action Taken :</font> " + twd.actionTaken + "<br />");
			msgBody.append("<font style='color:gray'>Remarks :</font> " + twd.wiRemarks + "<br />");
			msgBody.append("<font style='color:gray'>Instructions :</font> " + twd.instructions + "<br />");
			msgBody.append("<font style='color:gray'>Keywords :</font> " + twd.Keywords + "<br />");
			msgBody.append("<font style='color:gray'>Sender Comments :</font> " + twd.comments + "<br />");
			
			message = message.replace("#MESSAGE#", msgBody.toString().replaceAll("null", "--") + "");
			message = message.replace("#WITMID#", twd.workitemId + "");
			
/*			message = message.replace("#WITMID#", twd.workitemId + "");
			message = message.replace("#PRIORITY#", twd.priority + "");
			message = message.replace("#SENDER#", twd.senderName + "");
			message = message.replace("#SENDROLE#", twd.senderRoleName + "");
			message = message.replace("#ACTIONS#", twd.actions + "");
			message = message.replace("#DEADLINE#", DBUtil.convertDateTimeToString(twd.deadline) + "");
			message = message.replace("#ORIGINATOR#", twd.wfCreatorName + "");
			message = message.replace("#WFLID#", twd.workflowId + "");*/
			
			String wfAttachments = "";
			for(TAttachment ta:attachments) {
				wfAttachments += "<li style=\"padding: 0px; cursor: pointer; "
						+ "list-style-type: square; list-style-position: inside; list-style-image: none\">" 
						+ getDocumentViewURL(ta.docId,viewerURL,ta.docTitle,objStoreName);
			}
			
			if(wfAttachments != "")
				message = message.replace("#ATTACHMENTS#", wfAttachments);
			else
				message = message.replace("#ATTACHMENTS#", "No attachments");
			
			mailObject.setMessage(message);
			mailObject.setSubject(subject);
			
			if(roleId > 0) { 
				ArrayList<String> mList = ECMRoleList.getInstance().getUserEmails(roleId);
				for(String to: mList)
					mailObject.addMailTo(to);
			}else  {		
				mailObject.addMailTo(ECMUserList.getInstance().getUserEmail(empNo));
			}
			mailObject.send();
		
		} catch (Exception e) {
			// Log the error here
			e.printStackTrace();
		}
	}
	
	private void loadEmailConfigurations() {
		if(mailConfigLoaded)
			return;
		try {
			ECMConfigurationList cfgList = ECMConfigurationList.getInstance("ECM", "APP");
			
			//mailObject.setUserName(decryptString(cfgList.getConfigValue("SMTPUSER")));
			//mailObject.setPassword(decryptString(cfgList.getConfigValue("SMTPPASSWORD")));
			mailObject.setUserName(cfgList.getConfigValue("SMTPUSER"));
			mailObject.setPassword(cfgList.getConfigValue("SMTPPASSWORD"));
			mailObject.setEmailFrom(cfgList.getConfigValue("SMTPSENTFROM"));
			mailObject.setSMTPServer(cfgList.getConfigValue("SMTPSERVER"));
			mailConfigLoaded = true;
			
		} catch (Exception e) {
		}
	}
	
	
	private String getDocumentViewURL(String docId, String viewerURL, String docTitle, String objStoreName) throws Exception {
		
		String strUrl = viewerURL.trim() + "/getContent?id=" + docId + "&streamer=true&objectType=document&objectStoreName=" + objStoreName ;
        return "<a href='"+strUrl+"'>"+docTitle.trim() + "</a>";

	}
	
	/*private String decryptString(String inString) throws Exception {
		ECMEncryption enc = new ECMEncryption();
		return enc.getDecryptedString(encKey, inString);
	} */
	
	public void sendEmailFromDetails(String actionType, TWorkitemDetails twd) {
		try {
			loadEmailConfigurations();
			
			ECMConfigurationList cfgList = ECMConfigurationList.getInstance("ECM", "APP");
			String subject = cfgList.getConfigValue(actionType + "SUB");
			String messageFile = cfgList.getConfigValue(actionType + "MSG");
			String viewerURL = cfgList.getConfigValue("WXTURL");
			ECMConfigurationList cfgSysList = ECMConfigurationList.getInstance("ECM", "SYSTEM");
			String objStoreName = cfgSysList.getConfigValue("ECMOS");
			
			StringBuilder contentBuilder = new StringBuilder();
			try {
				ClassLoader loader = Thread.currentThread().getContextClassLoader();
				URL url = loader.getResource(messageFile);

			    BufferedReader in = new BufferedReader(new FileReader(URLDecoder.decode(url.getPath())));
			    String str;
			    while ((str = in.readLine()) != null) {
			        contentBuilder.append(str);
			    }
			    in.close();
			} catch (IOException e) {
				e.printStackTrace();
				throw new Exception(e.getMessage());
			}
			String message = contentBuilder.toString();
			
			if((subject == null) || (subject.length() <= 0))
				subject = "ECM Workitem #WITMID# Reminder";
			
			if((message == null) || (message.length() <= 0))
				message = "You have reminder for a work item - (#WITMID#) in ECM.";
			
			subject = subject.replace("#SUBJECT#", twd.subject + "");
			message = message.replace("#SUBJECT#", twd.subject + "");
			
			StringBuilder msgBody = new StringBuilder();
			
			if(twd.senderName != null)
				msgBody.append(" <font style='color:gray'>Sender:</font> " + twd.senderName + "<br />");
			else
				msgBody.append(" <font style='color:gray'>Sender:</font> " + twd.senderRoleName + "<br />");
			msgBody.append(" <font style='color:gray'>Subject:</font> " + twd.subject + "<br />");
			msgBody.append("<font style='color:gray'>For :</font> " + twd.actions + "<br />");
			msgBody.append("<font style='color:gray'>Action Taken :</font> " + twd.actionTaken + "<br />");
			msgBody.append("<font style='color:gray'>Remarks :</font> " + twd.wiRemarks + "<br />");
			msgBody.append("<font style='color:gray'>Instructions :</font> " + twd.instructions + "<br />");
			msgBody.append("<font style='color:gray'>Keywords :</font> " + twd.Keywords + "<br />");
			msgBody.append("<font style='color:gray'>Sender Comments :</font> " + twd.comments + "<br />");
			
			message = message.replace("#MESSAGE#", msgBody.toString().replaceAll("null", "--") + "");
			message = message.replace("#WITMID#", twd.workitemId + "");
			
/*			message = message.replace("#WITMID#", twd.workitemId + "");
			message = message.replace("#PRIORITY#", twd.priority + "");
			message = message.replace("#SENDER#", twd.senderName + "");
			message = message.replace("#SENDROLE#", twd.senderRoleName + "");
			message = message.replace("#ACTIONS#", twd.actions + "");
			message = message.replace("#DEADLINE#", DBUtil.convertDateTimeToString(twd.deadline) + "");
			message = message.replace("#ORIGINATOR#", twd.wfCreatorName + "");
			message = message.replace("#WFLID#", twd.workflowId + "");*/
			
			String wfAttachments = "";
			for(TAttachment ta:attachments) {
				wfAttachments += "<li style=\"padding: 0px; cursor: pointer; "
						+ "list-style-type: square; list-style-position: inside; list-style-image: none\">" 
						+ getDocumentViewURL(ta.docId,viewerURL,ta.docTitle,objStoreName);
			}
			
			if(wfAttachments != "")
				message = message.replace("#ATTACHMENTS#", wfAttachments);
			else
				message = message.replace("#ATTACHMENTS#", "No attachments");
			
			mailObject.setMessage(message);
			mailObject.setSubject(subject);
			
			if(twd.recipientRoleId > 0) { 
				ArrayList<String> mList = ECMRoleList.getInstance().getUserEmails(twd.recipientRoleId);
				for(String to: mList)
					mailObject.addMailTo(to);
				if(actionType.equalsIgnoreCase("DEADLINE")) {
					String supEmail = ECMRoleList.getInstance().getSupervisorEmailForRole(twd.recipientRoleId);
					if(!mList.contains(supEmail))
						mailObject.addMailCC(supEmail);
				}
			}else  {		
				String uMail = ECMUserList.getInstance().getUserEmail(twd.recipientEMPNo);
				mailObject.addMailTo(uMail);
				if(actionType.equalsIgnoreCase("DEADLINE")) {
					String supEmail = ECMUserList.getInstance().getSupervisorEmailForUser(twd.recipientEMPNo);
					if(!uMail.equalsIgnoreCase(supEmail))
						mailObject.addMailCC(supEmail);
				}
			}
			mailObject.send();
		
		} catch (Exception e) {
			// Log the error here
		}
	}
	
	private void sendRecallEmail(String actionType, long witemId) {
		try {
			loadEmailConfigurations();
			
			ECMConfigurationList cfgList = ECMConfigurationList.getInstance("ECM", "APP");
			String subject = cfgList.getConfigValue(actionType + "SUB");
			String messageFile = cfgList.getConfigValue(actionType + "MSG");
			
			StringBuilder contentBuilder = new StringBuilder();
			try {
				ClassLoader loader = Thread.currentThread().getContextClassLoader();
				URL url = loader.getResource(messageFile);

			    BufferedReader in = new BufferedReader(new FileReader(URLDecoder.decode(url.getPath())));
			    String str;
			    while ((str = in.readLine()) != null) {
			        contentBuilder.append(str);
			    }
			    in.close();
			} catch (IOException e) {
				e.printStackTrace();
				throw new Exception(e.getMessage());
			}
			
			String message = contentBuilder.toString();
			
			if((subject == null) || (subject.length() <= 0))
				subject = "ECM Recall Workitem - #SUBJECT#";
			
			if((message == null) || (message.length() <= 0))
				message = "Recalled workitem in ECM - #SUBJECT#";
			
			ECMWorkItem ewi = new ECMWorkItem();
			ewi.setId(witemId);
			TWorkitemDetails twd = ewi.getDetails();
			
			subject = subject.replace("#SUBJECT#", twd.subject + "");
			message = message.replace("#SUBJECT#", twd.subject + "");
			
			StringBuilder msgBody = new StringBuilder();
			
			if(twd.senderName != null)
				msgBody.append(" <font style='color:gray'>Sender:</font> " + twd.senderName + "<br />");
			else
				msgBody.append(" <font style='color:gray'>Sender:</font> " + twd.senderRoleName + "<br />");
			msgBody.append(" <font style='color:gray'>Subject:</font> " + twd.subject + "<br />");
			msgBody.append("<font style='color:gray'>Sender Comments :</font> " + twd.comments + "<br />");
			
			message = message.replace("#MESSAGE#", msgBody.toString().replaceAll("null", "--") + "");
			//message = message.replace("#WITMID#", twd.workitemId + "");
			
			mailObject.setMessage(message);
			mailObject.setSubject(subject);
			
			long roleId = twd.recipientRoleId;
			long empNo = twd.recipientEMPNo;
			
			if(roleId > 0) { 
				ArrayList<String> mList = ECMRoleList.getInstance().getUserEmails(roleId);
				for(String to: mList)
					mailObject.addMailTo(to);
			}else  {		
				mailObject.addMailTo(ECMUserList.getInstance().getUserEmail(empNo));
			}
			mailObject.send();
			
			//checkForChildItems() - Abhishek to do
		
		} catch (Exception e) {
			// Log the error here
		}
	}
}
