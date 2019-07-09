package com.ecm.db.transport;

import java.util.ArrayList;
import java.util.Date;

public class TWorkitemAction {
	public long id;
	public String actions;
	public String instructions;
	public Date deadline;
	public Date reminder;
	public long roleId;
	public long priority = 0;
	public long EMPNo;
	public String actionDetails;
	public String wiAction;
	public String saveType = "NORMAL";
	public long draftId = 0;
	public boolean draft = false;
	public String draftDate;
	public String actionTaken;
	public String wiRemarks;
	public long actionId = 0; // to be removed
	public ArrayList<TRecipient> recipients;
	public ArrayList<TAttachment> attachments;
	public TWorkflowDetails workflow;
}