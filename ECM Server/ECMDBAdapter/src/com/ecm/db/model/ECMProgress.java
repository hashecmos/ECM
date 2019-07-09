package com.ecm.db.model;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.sql.PreparedStatement;
import java.sql.Connection;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;

import com.ecm.db.util.DBUtil;
import com.ecm.db.util.ECMMail;
import com.ecm.db.list.ECMConfigurationList;
import com.ecm.db.list.ECMRoleList;
import com.ecm.db.list.ECMUserList;
import com.ecm.db.transport.TAttachment;
import com.ecm.db.transport.TNews;
import com.ecm.db.transport.TRecipient;
import com.ecm.db.transport.TWorkitemDetails;
import com.ecm.db.transport.TWorkitemProgress;

public class ECMProgress {

	private long id;
	private long workitemId;
	private String message;
	private long empNo;
	private String createdBy;
	private Date createdDate;
	private Date modifiedDate;
	
	private ECMMail mailObject = new ECMMail();
	private Boolean mailConfigLoaded = false;

	private ArrayList<TAttachment> attachments = null;
	
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}
	
	public long getWorkitemId() {
		return workitemId;
	}

	public void setWorkitemId(long workitemId) {
		this.workitemId = workitemId;
	}
	
	public long getEmpNo() {
		return empNo;
	}

	public void setEmpNo(long empNo) {
		this.empNo = empNo;
	}

	
	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public Date getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}
	
	public Date getModifiedDate() {
		return modifiedDate;
	}

	public void setModifiedDate(Date modifiedDate) {
		this.modifiedDate = modifiedDate;
	}



	private void insert() throws Exception {
		Connection conn = null;
		PreparedStatement ps = null;
		try {
			String progressMsg = DBUtil.escapeString(this.message);
			
			conn = DBUtil.getECMDBConnection();
			String sqlQuery = "INSERT INTO ECM_WORKITEM_PROGRESS "
					+ "(ID, WORKITEMID, Message, CreatedBy, CreatedDate, ModifiedDate, Status) "
					+ "VALUES (ECM_WORKITEM_PROGRESS_SEQ.NEXTVAL, ?, ?, ?, SYSDATE, SYSDATE, ?)";
			ps = conn.prepareStatement(sqlQuery);

			ps.setInt(1, (int)this.workitemId);
			ps.setString(2, DBUtil.escapeString(progressMsg));
			ps.setInt(3, (int)this.empNo);
			ps.setString(4, DBUtil.escapeString("UNREAD"));
			
			ps.executeUpdate();
			
			sendEmail("PROGRESS", this.workitemId, progressMsg, "NEW");

		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			ps.close();
			conn.close();
		}
	}

	private void update() throws Exception {
		Connection conn = null;
		PreparedStatement ps = null;
		try {
			String progressMsg = DBUtil.escapeString(this.message);
					
			conn = DBUtil.getECMDBConnection();

			String sqlQuery = "UPDATE ECM_WORKITEM_PROGRESS SET Message = ?, "
					+ " ModifiedDate = SYSDATE WHERE ID = ?";
			ps = conn.prepareStatement(sqlQuery);

			ps.setString(1, progressMsg);
			ps.setInt(2, (int)this.id);

			ps.executeUpdate();
			
			sendEmail("PROGRESS", this.workitemId, progressMsg, "UPDATE");

		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			ps.close();
			conn.close();
		}
	}
	
	public void updateStatus() throws Exception {
		Connection conn = null;
		PreparedStatement ps = null;
		try {
			String progressMsg = DBUtil.escapeString(this.message);
					
			conn = DBUtil.getECMDBConnection();

			String sqlQuery = "UPDATE ECM_WORKITEM_PROGRESS SET Status = ?, "
					+ " ModifiedDate = SYSDATE WHERE ID = ?";
			ps = conn.prepareStatement(sqlQuery);

			ps.setString(1, DBUtil.escapeString("READ"));
			ps.setInt(2, (int)this.id);

			ps.executeUpdate();
			
			sendEmail("PROGRESS", this.workitemId, progressMsg, "UPDATE");

		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			ps.close();
			conn.close();
		}
	}
	
	public void remove() throws Exception {
		Connection conn = null;
		PreparedStatement ps = null;
		try {
			conn = DBUtil.getECMDBConnection();

			String sqlQuery = "DELETE ECM_WORKITEM_PROGRESS WHERE ID = ?";
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

	public void save() throws Exception {
		if (this.id <= 0)
			insert();
		else
			update();
	}

	public TWorkitemProgress getTransport() throws Exception {

		TWorkitemProgress twi_Progress = new TWorkitemProgress();
		
		twi_Progress.id = this.id;
		twi_Progress.message = this.message;
		twi_Progress.workitemId = this.workitemId;
		twi_Progress.empNo = this.empNo;
		twi_Progress.createdBy = this.createdBy;
		twi_Progress.createdDate = this.createdDate;
		twi_Progress.modifiedDate = this.modifiedDate;

		return twi_Progress;
	}

	public void getFromTransport(TWorkitemProgress progress) throws Exception {

		this.setId(progress.id);
		this.setMessage(progress.message);
		this.setWorkitemId(progress.workitemId);
		this.setCreatedBy(progress.createdBy);
		this.setEmpNo(progress.empNo);
		this.setCreatedDate(progress.createdDate);
		this.setModifiedDate(progress.modifiedDate);
	}


	private void sendEmail(String actionType, long witemId, String progressDetails, String progressType) {
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
				subject = "ECM Workflow item: " + progressType + " Progress for #SUBJECT#";
			
			if((message == null) || (message.length() <= 0))
				message = "You have a progress added/updated for Workflow item - #SUBJECT# in ECM.";
			
			ECMWorkItem ewi = new ECMWorkItem();
			ewi.setId(witemId);
			TWorkitemDetails twd = ewi.getDetails();
			
			subject = subject.replace("#SUBJECT#", twd.subject);
			subject = subject.replace("#TYPE#", progressType);
			
			message = message.replace("#SUBJECT#", twd.subject + "-" + progressType);
			
			StringBuilder msgBody = new StringBuilder();
			
			if(twd.senderName != null)
				msgBody.append(" <font style='color:gray'>Sender:</font> " + twd.senderName + "<br />");
			else
				msgBody.append(" <font style='color:gray'>Sender:</font> " + twd.senderRoleName + "<br />");
			msgBody.append(" <font style='color:gray'>Subject:</font> " + twd.subject + "<br />");
			msgBody.append("<font style='color:gray'>Progress Comments :</font> " + progressDetails + "<br />");
			
			message = message.replace("#MESSAGE#", msgBody.toString().replaceAll("null", "--") + "");
			message = message.replace("#WITMID#", twd.workitemId + "");
			
			attachments = twd.attachments;
			
			if(attachments != null)
			{
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
			}
			
			mailObject.setMessage(message);
			mailObject.setSubject(subject);
			
			long roleId = twd.senderRoleId;
			long empNo = twd.senderEMPNo;
			
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
	
	private String getDocumentViewURL(String docId, String viewerURL, String docTitle, String objStoreName) throws Exception {
		
		String strUrl = viewerURL.trim() + "/getContent?id=" + docId + "&streamer=true&objectType=document&objectStoreName=" + objStoreName ;
        return "<a href='"+strUrl+"'>"+docTitle.trim() + "</a>";

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

}
