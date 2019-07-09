package com.ecm.db.model;

public class ECMInboxItems {
public String priority;
public String subject;
public long workitemId;
public long sentitemId;
public String status;
public String ecmNo;
public String instructions;
public String type;
public String deadline;
public String reminder;
public String receivedDate;
public String wfCreatorName;
public String recipientName;
public String senderName;
public long senderId;
public String comments;
public String remarks;
public String actions;
public String actionTaken;
public long actionId = 0;
public long receiveCount=0;
public String keywords;

	public String getPriority() {
	return priority;
	}
	public void setPriority(String priority) {
		this.priority = priority;
	}
	public String getSubject() {
		return subject;
	}
	public void setSubject(String subject) {
		this.subject = subject;
	}
	public long getWorkitemId() {
		return workitemId;
	}
	public void setWorkitemId(long workitemId) {
		this.workitemId = workitemId;
	}
	public long getSentitemId() {
		return sentitemId;
	}
	public void setSentitemId(long sentitemId) {
		this.sentitemId = sentitemId;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getInstructions() {
		return instructions;
	}
	public void setInstructions(String instructions) {
		this.instructions = instructions;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getDeadline() {
		return deadline;
	}
	public void setDeadline(String deadline) {
		this.deadline = deadline;
	}
	public String getReminder() {
		return reminder;
	}
	public void setReminder(String reminder) {
		this.reminder = reminder;
	}
	public String getReceivedDate() {
		return receivedDate;
	}
	public void setReceivedDate(String receivedDate) {
		this.receivedDate = receivedDate;
	}
	public String getWfCreatorName() {
		return wfCreatorName;
	}
	public void setWfCreatorName(String wfCreatorName) {
		this.wfCreatorName = wfCreatorName;
	}
	public String getEcmNo() {
		return ecmNo;
	}
	public void setEcmNo(String ecmNo) {
		this.ecmNo = ecmNo;
	}
	public String getRecipientName() {
		return recipientName;
	}
	public void setRecipientName(String recipientName) {
		this.recipientName = recipientName;
	}
	public String getSenderName() {
		return senderName;
	}
	public void setSenderName(String senderName) {
		this.senderName = senderName;
	}
	public long getSenderId() {
		return senderId;
	}
	public void setSenderId(long senderId) {
		this.senderId = senderId;
	}
	public String getComments() {
		return comments;
	}
	public void setComments(String comments) {
		this.comments = comments;
	}
	public String getRemarks() {
		return remarks;
	}
	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}
	public String getActions() {
		return actions;
	}
	public void setActions(String actions) {
		this.actions = actions;
	}
	public long getActionId() {
		return actionId;
	}
	public void setActionId(long actionId) {
		this.actionId = actionId;
	}
	public long getReceiveCount() {
		return receiveCount;
	}
	public void setReceiveCount(long receiveCount) {
		this.receiveCount = receiveCount;
	}
	public String getKeywords() {
		return keywords;
	}
	public void setKeywords(String keywords) {
		this.keywords = keywords;
	}
	public String getActionTaken() {
		return actionTaken;
	}
	public void setActionTaken(String actionTaken) {
		this.actionTaken = actionTaken;
	}

}
