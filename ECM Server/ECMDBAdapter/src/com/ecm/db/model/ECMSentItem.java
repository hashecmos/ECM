package com.ecm.db.model;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Date;

import oracle.jdbc.OracleTypes;

import com.ecm.db.list.ECMConfigurationList;
import com.ecm.db.list.ECMRoleList;
import com.ecm.db.list.ECMUserList;
import com.ecm.db.list.ECMWorkitemList;
import com.ecm.db.transport.TDelegate;
import com.ecm.db.transport.TDocPrincipal;
import com.ecm.db.transport.TRecipient;
import com.ecm.db.transport.TWorkflowHistory;
import com.ecm.db.transport.TWorkitemDetails;
import com.ecm.db.transport.TWorkitemInfo;
import com.ecm.db.transport.TWorkitemSet;
import com.ecm.db.util.DBUtil;
import com.ecm.db.util.ECMMail;

public class ECMSentItem {
	private long id = 0;
	private long workflowId = 0;
	private long parentItemId = 0;
	private long parentSentItemId = 0;
	private Date createdDate;
	private long senderRoleId = 0;
	private long senderEMPNo = 0;
	private long rootSentItemId = 0;
	private String status;
	private long priority = 0;
	private String actionTaken = "";
	private String remarks = "No Remarks";
	
	private ECMMail mailObject = new ECMMail();
	private Boolean mailConfigLoaded = false;

	private boolean isLoaded = false;
	public void setLoaded() { isLoaded = true; }
	
	public long getId() { return this.id; }
	public long getWorkflowId() { return this.workflowId; }
	public long getParentItemId() { return this.parentItemId; }
	public long getParentSentItemId() { return this.parentSentItemId; }
	public Date getCreatedDate() { return this.createdDate; }
	public long getSenderRoleId() { return this.senderRoleId; }
	public long getSenderEmpNo() { return this.senderEMPNo; }
	public long getRootSentItemId() { return this.rootSentItemId; }
	public String getStatus() { return this.status; }
	public long getPriority() { return priority; }
	public String getActionTaken() { return actionTaken; }
	public String getRemarks() { return remarks; }

	public void setId(long iid) { this.id = iid; }
	public void setWorkflowId(long iid) { this.workflowId = iid; }
	public void setParentItemId(long iid) { this.parentItemId = iid; }
	public void setParentSentItemId(long iid) { this.parentSentItemId = iid; }
	public void setCreatedDate(Date dt) { this.createdDate = dt; }
	public void setSenderRoleId(long iid) { this.senderRoleId = iid; }
	public void setSenderEmpNo(long ino) { this.senderEMPNo = ino; }
	public void setRootSentItemId(long iid) { this.rootSentItemId = iid; }
	public void setStatus(String stat) { this.status = stat; }
	public void setPriority(long priority) { this.priority = priority; }
	public void setActionTaken(String actionTaken) { this.actionTaken = actionTaken; }
	public void setRemarks(String remarks) { this.remarks = remarks; }
	
	public long getRootSentItem() throws Exception {
		load();
		return this.rootSentItemId;
	}
	
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
			sqlQuery = "SELECT * FROM ECM_WORKITEM_SENT WHERE ID = ?";
			ps = conn.prepareStatement(sqlQuery);
			ps.setInt(1, (int)this.id);
			
			rs = ps.executeQuery();
			if (rs.next()) {				
				setId(rs.getInt("ID"));
				setWorkflowId(rs.getInt("WorkflowID")); 
				setParentItemId(rs.getInt("ParentItemID")); 
				setParentSentItemId(rs.getInt("ParentSentItemID")); 
				setCreatedDate(rs.getTimestamp("CreatedDate")); 
				setSenderRoleId(rs.getInt("SenderRoleID")); 
				setSenderEmpNo(rs.getInt("SenderEMPNo")); 
				setRootSentItemId(rs.getInt("RootSentItemID")); 	
				setStatus(rs.getString("Status"));
				setPriority(rs.getInt("PRIORITY"));
				setActionTaken(rs.getString("ActionTaken"));
				setRemarks(rs.getString("Remarks"));
				isLoaded = true;
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
	
	public void insert() throws Exception{
		Connection conn = null;
		CallableStatement cs = null;
		try {
			conn = DBUtil.getECMDBConnection();

			String SQLQuery = "{call ECM_CREATE_SENT_WORKITEM(?,?,?,?,?,?,?,?,?)}";
			cs = conn.prepareCall(SQLQuery);
			
			cs.setInt(1, (int)this.parentSentItemId);
			cs.setInt(2, (int)this.parentItemId);
			cs.setInt(3, (int)this.senderEMPNo);
			cs.setInt(4, (int)this.senderRoleId);
			cs.setInt(5, (int)this.workflowId);
			cs.setInt(6, (int)this.priority);
			cs.setString(7, this.actionTaken);
			cs.setString(8, this.remarks);
			
			cs.registerOutParameter(9, java.sql.Types.INTEGER);
			cs.executeUpdate();

			this.setId(cs.getInt(9));
			System.out.println(this.getId());
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			cs.close();
			conn.close();
		}
	}
	
	public void archive(long empNo, long roleId) throws Exception{
		Connection conn = null;
		CallableStatement cs = null;
		try {
			conn = DBUtil.getECMDBConnection();

			String SQLQuery = "{call ECM_ARCHIVE_SENTITEM(?,?,?,?)}";
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
		long wiCount =0;
		try {
			conn = DBUtil.getECMDBConnection();
			wiCount = ECMWorkitemList.getInstance().getArchiveSentItemsCount(bDate, roleId, empNo);
			if(wiCount > 0)
			{
				String SQLQuery = "{call ECM_ARCHIVE_BEFORE_SENTITEM(?,?,?)}";
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
	
	public ArrayList<TDocPrincipal> recall() throws Exception {
		Connection conn = null;
		CallableStatement cs = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		ArrayList<TDocPrincipal> retList = null;
		try {
			conn = DBUtil.getECMDBConnection();
			
			String SQLQuery = "{call ECM_RECALL_SENTITEM(?,?,?)}";
			cs = conn.prepareCall(SQLQuery);
			
			cs.setInt(1, (int)this.id); 
			cs.setInt(2, (int)this.senderEMPNo);
			cs.setInt(3, (int)this.senderRoleId);
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
	
	public TWorkitemSet getWorkitems() throws Exception
	{
		if(this.id <= 0)
			return null;
		if((status == null) || (status.length() <= 0))
			status = "ACTIVE";
		
		TWorkitemSet ts = new TWorkitemSet();
		ArrayList<TWorkitemInfo> wiList = new ArrayList<TWorkitemInfo>();
		ts.workitems = wiList;
		
		Connection conn = null;
		ResultSet rs = null;
		String sqlQuery = null;
		CallableStatement callableStatement = null;
		try {
			conn = DBUtil.getECMDBConnection();
			sqlQuery = "{call ECM_GET_SENTITEM_WORKITEMS(?,?,?)}";
			callableStatement = conn.prepareCall(sqlQuery);
			callableStatement.setInt(1, (int)this.id);
			callableStatement.setString(2,  DBUtil.escapeString(status));
			callableStatement.registerOutParameter(3, OracleTypes.CURSOR);
			
			callableStatement.execute();
			rs = (ResultSet)callableStatement.getObject(3);
			
			while (rs.next()) {
				TWorkitemInfo wi = new TWorkitemInfo();
				wi.workitemId=rs.getInt("ID");
				wi.priority=rs.getInt("Priority");
				wi.subject=rs.getString("Subject");
				wi.status=rs.getString("Status");
				wi.remarks=rs.getString("Remarks");
				wi.instructions=rs.getString("Instructions");
				wi.type=rs.getString("Type");
				wi.deadline=DBUtil.convertDateTimeToString(rs.getTimestamp("Deadline"));
				wi.reminder=DBUtil.convertDateTimeToString(rs.getTimestamp("Reminder"));
				wi.receivedDate=DBUtil.convertDateTimeToString(rs.getTimestamp("CreatedDate"));
				wi.actions=rs.getString("Actions");
				wi.wfCreatorName=rs.getString("WFCreatorName");
				wi.recipientName=rs.getString("RecipientName");
				wi.senderName=rs.getString("SenderName");
				wi.sentitemId=rs.getInt("ParentSentItemID");
				if(wi.recipientName == null)
					wi.recipientName=rs.getString("RecipientRoleName");
					
				wiList.add(wi);
			}  
			
			ts.totalCount = wiList.size();
			ts.setCount = wiList.size();
			ts.curPage = 1;
			ts.pages = 1;
			ts.pageSize = ts.setCount;
			ts.workitems = wiList;
			
			return ts;
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			rs.close();
			callableStatement.close();
			conn.close();
		}
	}
	
	public String getAccessPolicy() throws Exception {
		Connection conn = null;
		ResultSet rs = null;
		String sqlQuery = null;
		PreparedStatement ps = null;
		String accessPolicy = null;
		try {
			conn = DBUtil.getECMDBConnection();
			sqlQuery = "SELECT A.AccessPolicyID FROM ECM_WORKFLOW A, ECM_WORKITEM_SENT B "
					+ " WHERE B.ID = ? AND A.ID = B.WorkflowID";
			ps = conn.prepareStatement(sqlQuery);
			ps.setLong(1, this.id);
			
			rs = ps.executeQuery();
			if (rs.next()) {		
				accessPolicy = rs.getString("AccessPolicyID");
			} 
			return accessPolicy;
			
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			rs.close();
			ps.close();
			conn.close();
		}
	}
	
	public long getFirstWorkitemId() throws Exception {
		Connection conn = null;
		ResultSet rs = null;
		String sqlQuery = null;
		PreparedStatement ps = null;
		long witemId = 0;
		try {
			conn = DBUtil.getECMDBConnection();
			sqlQuery = "SELECT ID FROM ECM_WORKITEM WHERE PARENTSENTITEMID = ? ORDER BY TYPE DESC, CREATEDDATE ASC";
			ps = conn.prepareStatement(sqlQuery);
			ps.setLong(1, this.id);
			
			rs = ps.executeQuery();
			if (rs.next()) {		
				witemId = rs.getInt("ID");
			} 
			return witemId;
			
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			rs.close();
			ps.close();
			conn.close();
		}
	}
	
	public ArrayList<TRecipient> getRecipients() throws Exception {
		Connection conn = null;
		ResultSet rs = null;
		String sqlQuery = null;
		PreparedStatement ps = null;
		ArrayList<TRecipient> recipientList = new ArrayList<TRecipient>();
		try {
			conn = DBUtil.getECMDBConnection();
			sqlQuery = "SELECT TYPE, RECIPIENTEMPNO, RECIPIENTROLEID FROM ECM_WORKITEM "
					+ "WHERE PARENTSENTITEMID = ? ORDER BY TYPE DESC, CREATEDDATE ASC";
			ps = conn.prepareStatement(sqlQuery);
			ps.setLong(1, this.id);
			
			rs = ps.executeQuery();
			while (rs.next()) {
				TRecipient tr = new TRecipient();
				tr.actionType = rs.getString("TYPE");
				tr.id = rs.getInt("RECIPIENTEMPNO");
				if(tr.id > 0) {
					tr.userType = "USER";
				} else {
					tr.id = rs.getInt("RECIPIENTROLEID");
					tr.userType = "ROLE";
				}
				recipientList.add(tr);
			} 
			return recipientList;
			
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			rs.close();
			ps.close();
			conn.close();
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
			
			TWorkitemSet wiList = ECMWorkitemList.getInstance().getSentItemWorkItems(witemId,0,"");
			
			ArrayList<TWorkitemInfo> witems = wiList.workitems;
			for (TWorkitemInfo twi : witems){
				
				if(twi.workitemId <= 0)
					continue;
				
				ECMWorkItem ewi = new ECMWorkItem();
				ewi.setId(twi.workitemId);
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
				//getChildItemsFromWorkItem()
			}
		
		} catch (Exception e) {
			// Log the error here
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
			sqlQuery = "{call ECM_GET_SENTITEM_HISTORY(?,?)}";
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
				
				long actionUserId =  rs.getLong("ActionUser");
				String actionDetails = hist.details;
				if(actionDetails.equalsIgnoreCase("RECALL") && (actionUserId != empNo))
					continue;
				else
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
}
