package com.ecm.db.model;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.sql.PreparedStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;

import com.ecm.db.util.DBUtil;
import com.ecm.db.util.ECMMail;
import com.ecm.db.list.ECMConfigurationList;
import com.ecm.db.list.ECMRoleList;
import com.ecm.db.list.ECMUserList;
import com.ecm.db.list.ECMWorkitemList;
import com.ecm.db.transport.TDelegate;
import com.ecm.db.transport.TWorkitemDetails;
import com.ecm.db.transport.TWorkitemInfo;
import com.ecm.db.transport.TWorkitemSet;

public class ECMDelegation {
	private long id;
	private long userId;
	private long delegateId;
	private String userType;
	private Date fromDate;
	private Date toDate;
	private String status;
	private long delegatedBy;
	private Date delegatedOn;
	
	private ECMMail mailObject = new ECMMail();
	private Boolean mailConfigLoaded = false;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getUserId() {
		return userId;
	}

	public void setUserId(long uid) {
		this.userId = uid;
	}
	
	public long getDelegateId() {
		return delegateId;
	}

	public void setDelegateId(long uid) {
		this.delegateId = uid;
	}
	
	public String getStatus() {
		return this.status;
	}

	public void setStatus(String stat) {
		this.status = stat;
	}
	
	public String getUserType() {
		return this.userType;
	}

	public void setUserType(String utype) {
		this.userType = utype;
	}

	public Date getFromDate() {
		return this.fromDate;
	}

	public void setFromDate(Date fdate) {
		this.fromDate = fdate;
	}
	
	public Date getToDate() {
		return this.toDate;
	}

	public void setToDate(Date tdate) {
		this.toDate = tdate;
	}

	public void load() throws Exception {
		Connection conn = null;
		PreparedStatement ps = null;
		try {
			conn = DBUtil.getECMDBConnection();
			String sqlQuery = "SELECT * FROM ECM_DELEGATION WHERE ID = ?";
			ps = conn.prepareStatement(sqlQuery);

			ps.setInt(1, (int)this.id);
			
			ResultSet rs = ps.executeQuery();
			if(rs.next()) {
				this.userType = rs.getString("USERTYPE");
				this.userId = rs.getInt("USERID");
				this.fromDate = rs.getTimestamp("FROMDATE");
				this.toDate = null;
				if(!(rs.getTimestamp("TODATE").after(DBUtil.addYearstoDate(25))))
					this.toDate = rs.getTimestamp("TODATE");
				this.delegateId = rs.getInt("DELEGATEID");
				this.delegatedBy = rs.getInt("DELEGATEDBY");
				this.delegatedOn = rs.getTimestamp("DELEGATEDON");
				this.status = rs.getString("STATUS");
			}

		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			ps.close();
			conn.close();
		}
	}
	
	public long getExistingDelegate() throws Exception {
		long dID = 0;
		Connection conn = null;
		PreparedStatement ps = null;
		try {
			conn = DBUtil.getECMDBConnection();
			String sqlQuery = "SELECT ID FROM ECM_DELEGATION WHERE USERID = ? AND "
					+ "USERTYPE = ? AND DelegateId = ? AND DelegatedBy = ? AND STATUS = ? "
					+ "AND trunc(FROMDATE) <= TO_DATE('" + DBUtil.escapeString(DBUtil.getTodayDate()) + "','dd/MM/yyyy')"
					+ "AND trunc(TODATE) >= TO_DATE('" + DBUtil.escapeString(DBUtil.getTodayDate()) + "','dd/MM/yyyy')";
			ps = conn.prepareStatement(sqlQuery);

			ps.setInt(1, (int)this.userId);
			ps.setString(2, DBUtil.escapeString(this.userType));
			ps.setInt(3, (int)this.delegateId);
			ps.setInt(4, (int)this.delegatedBy);
			ps.setString(5, DBUtil.escapeString("ACTIVE"));
			
			ResultSet rs = ps.executeQuery();
			if(rs.next()) {
				dID =  rs.getInt("ID");
			}
			return dID;

		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			ps.close();
			conn.close();
		}
	}
	
	public String validateDelegate() throws Exception {
		String strResult = "INACTIVE";
		Connection conn = null;
		PreparedStatement ps = null;
		try {
			conn = DBUtil.getECMDBConnection();
			String sqlQuery = "SELECT ID FROM ECM_DELEGATION WHERE ID = ? AND STATUS = ? "
					+ "AND trunc(FROMDATE) <= TO_DATE('" + DBUtil.escapeString(DBUtil.getTodayDate()) + "','dd/MM/yyyy')"
					+ "AND trunc(TODATE) >= TO_DATE('" + DBUtil.escapeString(DBUtil.getTodayDate()) + "','dd/MM/yyyy')";
			ps = conn.prepareStatement(sqlQuery);

			ps.setInt(1, (int)this.id);
			ps.setString(2, DBUtil.escapeString("ACTIVE"));
			
			ResultSet rs = ps.executeQuery();
			if(rs.next()) {
				strResult =  "ACTIVE";
			}
			return strResult;

		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			ps.close();
			conn.close();
		}
	}
	
	private void insert() throws Exception {
		Connection conn = null;
		PreparedStatement ps = null;
		try {
			conn = DBUtil.getECMDBConnection();
			String sqlQuery = "INSERT INTO ECM_DELEGATION "
					+ "(ID, UserId, UserType, DelegateId, FromDate, ToDate, Status, "
					+ "DelegatedBy, DelegatedOn)"
					+ "VALUES (ECM_DELEGATION_SEQ.NEXTVAL, ?, ?, ?, ?, ?, 'ACTIVE', "
					+ "?, SYSTIMESTAMP)";
			ps = conn.prepareStatement(sqlQuery);

			ps.setInt(1, (int)this.userId);
			ps.setString(2, DBUtil.escapeString(this.userType));
			ps.setInt(3, (int)this.delegateId);
			if(this.fromDate != null)
				ps.setTimestamp(4, new Timestamp(this.fromDate.getTime()));
			else
				ps.setTimestamp(4, DBUtil.addYearstoDate(29));
			if(this.toDate != null)
				ps.setTimestamp(5, new Timestamp(this.toDate.getTime()));
			else
				ps.setTimestamp(5, DBUtil.addYearstoDate(30));
			ps.setInt(6, (int)this.delegatedBy);
			ps.executeUpdate();
			
			sendEmail("DELEGATION", this.userId, this.userType, this.delegateId, this.fromDate, this.toDate, this.delegatedBy, this.delegatedOn, "Activated");

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
			conn = DBUtil.getECMDBConnection();

			String sqlQuery = "UPDATE ECM_DELEGATION SET FromDate = ?, ToDate = ?, "
					+ "DelegateId = ?, DelegatedOn = SYSTIMESTAMP  WHERE ID = ?";
			ps = conn.prepareStatement(sqlQuery);
			
			if(this.fromDate != null)
				ps.setTimestamp(1, new Timestamp(this.fromDate.getTime()));
			else
				ps.setTimestamp(1, DBUtil.addYearstoDate(29));
			
			if(this.toDate != null)
				ps.setTimestamp(2, new Timestamp(this.toDate.getTime()));
			else
				ps.setTimestamp(2, DBUtil.addYearstoDate(30));
			
			ps.setInt(3, (int)this.delegateId);
			ps.setInt(4, (int)this.id);

			ps.executeUpdate();
			
			sendDelegationEmail("DELEGATION",this.id, "Updated");

		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			ps.close();
			conn.close();
		}
	}

	public String save() throws Exception {
		String saveStatus = "Failed";
		if (this.id <= 0){
			long dID = getExistingDelegate();
			if(dID > 0)
				saveStatus = "Exists";
			else{
				insert();
				saveStatus = "Added";
			}
		}
		else{
			update();
			saveStatus = "Updated";
		}
		
		return saveStatus;
	}

	public void revoke() throws Exception {
		Connection conn = null;
		PreparedStatement ps = null;
		String sqlQuery = null;
		try {
			conn = DBUtil.getECMDBConnection();
			sqlQuery = "UPDATE ECM_DELEGATION SET Status = 'INACTIVE' WHERE ID = ?";
			ps = conn.prepareCall(sqlQuery);
			ps.setInt(1, (int)this.id);
			ps.executeUpdate();

			sendDelegationEmail("DELEGATION",this.id, "Revoked");
			
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			ps.close();
			conn.close();
		}
	}

	public TDelegate getTransport() throws Exception {
		TDelegate td = new TDelegate();
		td.id = this.id;
		td.userId = this.userId;
		td.userType = this.userType;
		td.delegateId = this.delegateId;
		td.fromDate = this.fromDate;
		td.toDate = this.toDate;
		td.status = this.status;
		td.delegatedOn = this.delegatedOn;
		td.delegatedBy = this.delegatedBy;
		
		return td;
	}

	public void getFromTransport(TDelegate del) throws Exception {
		this.id = del.id;
		this.userId = del.userId;
		this.userType = del.userType;
		this.delegateId = del.delegateId;
		this.fromDate = del.fromDate;
		this.toDate = del.toDate;
		this.status = del.status;
		this.delegatedBy = del.delegatedBy;
		this.delegatedOn = del.delegatedOn;
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
	
	public void sendDelegationEmail(String actionType, long delId, String emailType) {
		try {			
			this.id = delId;
			load();
			sendEmail(actionType, userId, userType, delegateId, fromDate, toDate, delegatedBy, delegatedOn, emailType);		
		} catch (Exception e) {
			// Log the error here
		}
	}
	
	private void sendEmail(String actionType, long usrId, String usrType, long deligateId,  
			Date fDate, Date tDate, long deligatedBy,  Date delegatedOn, String emailType ) {
		try {			
			String sDelBy = ECMUserList.getInstance().getUserFullName(deligatedBy);
			String sDelFromDate = DBUtil.convertDateToShortString(fDate,"");
			String sDelToDate = DBUtil.convertDateToShortString(tDate,"");
			String sDelAs = "";
			
			if(usrType != null && usrType.length() > 0)
			{
				if(usrType.equalsIgnoreCase("ROLE")){
					ECMRole er = new ECMRole();
					er.setId(usrId);
					er.load();
					sDelAs = er.getName();
				}
				else
					sDelAs = ECMUserList.getInstance().getUserFullName(deligatedBy);
			}
			
			loadEmailConfigurations();
			
			ECMConfigurationList cfgList = ECMConfigurationList.getInstance("ECM", "APP");
			String subject = cfgList.getConfigValue(actionType + "SUB");
			String messageFile = cfgList.getConfigValue(actionType + "MSG");
			
			if(sDelToDate.equalsIgnoreCase(""))
				sDelToDate = "Unlimited";
				
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
				subject = "ECM Delegation Account " + emailType;
			
			if((message == null) || (message.length() <= 0))
				message = "ECM Delegation Account " + emailType;
			
			StringBuilder msgBody = new StringBuilder();
			
			if(emailType.equalsIgnoreCase("revoked"))
			{
				msgBody.append("<font style='color:gray'>Revoked By:</font> " + sDelBy + "<br />");
				msgBody.append("<font style='color:gray'>Revoked Date:</font> " + DBUtil.getTodayDate() + "<br />");
				msgBody.append("<font style='color:gray'>Delegation Period :-</font><br />");
				msgBody.append("<font style='color:white'>-------</font><font style='color:gray'>From :</font> " + sDelFromDate + "<br />");
				msgBody.append("<font style='color:white'>-------</font><font style='color:gray'>To :</font> " + sDelToDate + "<br />");
			}
			else if(emailType.equalsIgnoreCase("expired"))
			{
				//msgBody.append("<font style='color:gray'>Revoked By:</font> " + sDelBy + "<br />");
				msgBody.append("<font style='color:gray'>Expired Date:</font> " + DBUtil.getTodayDate() + "<br />");
				msgBody.append("<font style='color:gray'>Delegation Period :-</font><br />");
				msgBody.append("<font style='color:white'>-------</font><font style='color:gray'>From :</font> " + sDelFromDate + "<br />");
				msgBody.append("<font style='color:white'>-------</font><font style='color:gray'>To :</font> " + sDelToDate + "<br />");
			}
			else
			{
				msgBody.append("<font style='color:gray'>Actioned By:</font> " + sDelBy + "<br />");
				msgBody.append("<font style='color:gray'>Actioned Date:</font> " + DBUtil.getTodayDate() + "<br />");
				msgBody.append("<font style='color:gray'>Delegation Period :-</font><br />");
				msgBody.append("<font style='color:white'>-------</font><font style='color:gray'>From :</font> " + sDelFromDate + "<br />");
				msgBody.append("<font style='color:white'>-------</font><font style='color:gray'>To :</font> " + sDelToDate + "<br />");
			}
			
			subject = subject.replace("#SUBJECT#", emailType + "");
			subject = subject.replace("#TYPE#", emailType + "");
			message = message.replace("#SUBJECT#", subject + "");
			
			message = message.replace("#DELTITLE#", "Your Account Acting as (" + sDelAs + ") is " + emailType);
			message = message.replace("#MESSAGE#", msgBody.toString() + "");
			
			mailObject.setMessage(message);
			mailObject.setSubject(subject);
					
			mailObject.addMailTo(ECMUserList.getInstance().getUserEmail(deligateId));
			mailObject.addMailCC(ECMUserList.getInstance().getUserEmail(deligatedBy));
			mailObject.send();
		
		} catch (Exception e) {
			// Log the error here
		}
	}

}