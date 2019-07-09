package com.ecm.db.transport;

import java.util.ArrayList;
import java.util.Date;

public class TWorkitemDetails {
	public long priority;
	public String subject;
	public String remarks; 
	public String Keywords;
	public Date createdOn;
	public String docFrom;
	public String docTo;
	public Date docDate;
	public Date docRecdDate;
	public String refNo;
	public String projNo;
	public String contractNo;
	public String ECMNo;
	public long workitemId;
    public long workflowId;
    public long SentItemId;
    public String actions; 
	public String status;
	public String instructions;
	public String type;
	public Date deadline;
	public Date reminder;
	public Date receivedDate;
	public long senderRoleId; 
	public long senderEMPNo;
	public long recipientEMPNo;
	public long recipientRoleId; 
	public String systemStatus;
	public String wfCreatorName;
	public String recipientName;
	public String senderName; 
	public String recipientRoleName;
	public String senderRoleName;
	public String comments;
	public String actionTaken;
	public String wiRemarks;
	
	public ArrayList<TAttachment> attachments;
	public ArrayList<TRecipient> recipients;
}
