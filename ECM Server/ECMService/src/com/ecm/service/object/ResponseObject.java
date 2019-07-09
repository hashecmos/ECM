package com.ecm.service.object;

import javax.ws.rs.core.Response.Status;

public class ResponseObject {
	private String responseStatus;
	private String responseMessage;
	private String responseCode;
	public String getResponseStatus() {
		return responseStatus;
	}
	public void setResponseStatus(String responseStatus) {
		this.responseStatus = responseStatus;
	}
	public String getResponseMessage() {
		return responseMessage;
	}
	public void setResponseMessage(String responseMessage) {
		this.responseMessage = responseMessage;
	}
	public String getResponseCode() {
		return responseCode;
	}
	public void setResponseCode(String responseCode) {
		this.responseCode = responseCode;
	}
	
	public static ResponseObject getResponseObject(String errorMessage) {
		ResponseObject responseObject = new ResponseObject();
		responseObject.setResponseCode(Status.BAD_REQUEST.toString());
		responseObject.setResponseMessage(errorMessage);
		responseObject.setResponseStatus("Failure");
		return responseObject;
	}
}

