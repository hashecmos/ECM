package com.ecm.db.model;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;

import com.ecm.db.list.ECMConfigurationList;
import com.ecm.db.list.ECMRoleList;
import com.ecm.db.list.ECMUserList;
import com.ecm.db.transport.TAdminEmailSet;
import com.ecm.db.transport.TAttachment;
import com.ecm.db.transport.TPermission;
import com.ecm.db.transport.TRecipient;
import com.ecm.db.transport.TUser;
import com.ecm.db.transport.TWorkitemDetails;
import com.ecm.db.util.DBUtil;
import com.ecm.db.util.ECMMail;

public class ECMMailManager {
	
	private ECMMail mailObject = new ECMMail();
	private Boolean mailConfigLoaded = false;
	
	private ArrayList<TRecipient> recipients = null;
	//private ArrayList<TAttachment> attachments = null;
	
	public void sendEmail(String screenType, TAdminEmailSet taes,
			String actionType) {
		try {
			loadEmailConfigurations();

			ECMConfigurationList cfgList = ECMConfigurationList.getInstance(
					"ECM", "APP");
			// String subject = cfgList.getConfigValue(actionType + "SUB");
			String subject = getEmailSubject(screenType, actionType);// cfgList.getConfigValue(actionType
																		// +
																		// "SUB");
			/*
			 * if (taes.subject != null) subject = taes.subject;
			 */
			String messageFile = cfgList.getConfigValue("ADMINMSG");

			StringBuilder contentBuilder = new StringBuilder();
			try {
				ClassLoader loader = Thread.currentThread()
						.getContextClassLoader();
				URL url = loader.getResource(messageFile);

				BufferedReader in = new BufferedReader(new FileReader(
						URLDecoder.decode(url.getPath())));
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

			if ((message == null) || (message.length() <= 0))
				message = "You have a keyvalue" + actionType
						+ " in ECM Configuration Admin.";

			StringBuilder msgBody = new StringBuilder();

			String newMessage = setEmailMessageBody(msgBody, screenType, taes, actionType);

			message = message.replace("#SUBJECT#", subject + "");
			message = message.replace("#MESSAGE#", newMessage + "");
			mailObject.setMessage(message);
			mailObject.setSubject(subject);
			mailObject.addMailTo(ECMUserList.getInstance().getUserEmail(179192));
			mailObject.send();

		} catch (Exception e) {
			// Log the error here
			e.printStackTrace();
		}
	}
	
	private String setEmailMessageBody( StringBuilder msgBody, String screenType, TAdminEmailSet taes, String actionType){
		
		if(screenType == "CONFIG")
		{
			return getConfigrationDetails(msgBody, screenType, taes, actionType);
		}
		else if(screenType == "AP")
		{
			return getAccessPolicyDetails(msgBody, screenType, taes, actionType);
		}
		else if(screenType == "APM")
		{
			return getAccessPolicyMappingDetails(msgBody, screenType, taes, actionType);
		}
		else if(screenType == "Lookup")
		{
			return getECMLookupDetails(msgBody, screenType, taes, actionType);
		}
		else if(screenType == "LookupMapping")
		{
			return getECMLookupMappingDetails(msgBody, screenType, taes, actionType);
		}
		else if(screenType == "ECMNews")
		{
			return getECMNewsDetails(msgBody, screenType, taes, actionType);
		}
		else if(screenType == "ECMRoleM")
		{
			return getECMRoleManagementDetails(msgBody, screenType, taes, actionType);
		}
		else if(screenType == "ECMEntryTemplateMapping")
		{
			return getECMEntryTemplateMappingDetails(msgBody, screenType, taes, actionType);
		}
		else if(screenType == "ECMINTEGRATION")
		{
			return getECMINTEGRATIONDetails(msgBody, screenType, taes, actionType);
		}
		else if(screenType == "ECMUsers")
		{
			return getECMUsersDetails(msgBody, screenType, taes, actionType);
		}
		else if(screenType == "ECMReportUsers")
		{
			return getECMReportUsersDetails(msgBody, screenType, taes, actionType);
		}
		else if(screenType == "ECMAdministrator")
		{
			return getECMAdministratorDetails(msgBody, screenType, taes, actionType);
		}
		else if(screenType == "ECMExcludedOperator")
		{
			return getECMExcludedOperatorDetails(msgBody, screenType, taes, actionType);
		}
		else if(screenType == "ECMGlobalList")
		{
			return getECMGlobalListDetails(msgBody, screenType, taes, actionType);
		}
		return "";
		 
	}
	
private String getEmailSubject(String screenType, String actionType){
		
	String subject ="ECM Admin Notifications for ";
	
		if(screenType == "CONFIG")
			return  subject + "Configuration - " + actionType;
		else if(screenType == "AP")
			return  subject + " AccessPolicy- " + actionType;
		else if(screenType == "APM")
		{
			return  subject + "AccessPolicy Mapping- " + actionType;
		}
		else if(screenType == "Lookup")
		{
			return  subject + "ECM Lookup- " + actionType;
		}
		else if(screenType == "LookupMapping")
		{
			return  subject + "Lookup Mapping- " + actionType;
		}
		else if(screenType == "ECMNews")
		{
			return  subject + "News- " + actionType;
		}
		else if(screenType == "ECMRoleM")
		{
			return  subject + "Role Management- " + actionType;
		}
		else if(screenType == "ECMEntryTemplateMapping")
		{
			return  subject + "EntryTemplate Mapping- " + actionType;
		}
		else if(screenType == "ECMINTEGRATION")
		{
			return  subject + "Intergration- " + actionType;
		}
		else if(screenType == "ECMUsers")
		{
			return  subject + "ECM Users- " + actionType;
		}
		else if(screenType == "ECMReportUsers")
		{
			return  subject + "ECM ReportUsers- " + actionType;
		}
		else if(screenType == "ECMAdministrator")
		{
			return  subject + "ECM Administrators- " + actionType;
		}
		else if(screenType == "ECMExcludedOperator")
		{
			return  subject + "ECM Exclude Operator - " + actionType;		}
		else if(screenType == "ECMGlobalList")
		{
			return  subject + "ECM GlobalList- " + actionType;
		}

		return "";

	}

	private String getConfigrationDetails(StringBuilder msgBody,
			String screenType, TAdminEmailSet taes, String actionType) {
		if (actionType.equalsIgnoreCase("Updated")) {
			msgBody.append("<font style='color:gray'>KeyName :</font> "+ taes.keyName + "<br />");
			msgBody.append("<font style='color:gray'>Value:</font> "+ taes.value + "&nbsp;<font style='color:gray'>update to:</font>"+taes.keyValue+"<br />");
			msgBody.append("<font style='color:gray'>Description:</font> "+ taes.description + "&nbsp;<font style='color:gray'>update to:</font>"+taes.keyDesc+"<br />");
			msgBody.append("<font style='color:gray'>Modified By:</font> "+ taes.modifiedBy + "<br />");
			msgBody.append("<font style='color:gray'>Modified Date:</font> "+ DBUtil.getTodayDate() + "<br />");
		}
		return msgBody.toString();
	}

	private String getAccessPolicyDetails(StringBuilder msgBody,
			String screenType, TAdminEmailSet taes, String actionType) {

		if (actionType.equalsIgnoreCase("Added")) {
			msgBody.append("<font style='color:gray'>Name :</font> "
					+ taes.name + "<br />");
			msgBody.append("<font style='color:gray'> Organization Code :</font> "
					+ taes.orgUnitName + "<br />");
			msgBody.append("<font style='color:gray'>Created BY :</font> "
					+ taes.createdBy + "<br />");
			msgBody.append("<font style='color:gray'> Created Date:</font> "
					+ DBUtil.getTodayDate() + "<br />");
		} else if (actionType.equalsIgnoreCase("Updated")) {
			msgBody.append("<table><tr><th>User/Group Name</th>&nbsp;&nbsp;<th>Type</th>&nbsp;&nbsp;<th>AccessType</th>&nbsp;&nbsp;<th>AccessLevel</th></tr>");

			for (TPermission permission : taes.permissions) {
				if (!(permission.action.equalsIgnoreCase("REMOVE"))) {
					msgBody.append("<tr><td><font style='color:gray'>"
							+ permission.granteeName
							+ "</font></td>&nbsp;&nbsp;");
					msgBody.append("<td><font style='color:gray'>"
							+ permission.granteeType
							+ "</font></td>&nbsp;&nbsp;");
					msgBody.append("<td><font style='color:gray'>"
							+ permission.accessType
							+ "</font></td>&nbsp;&nbsp;");
					msgBody.append("<td><font style='color:gray'>"
							+ permission.accessLevel
							+ "</font></td>&nbsp;&nbsp;</tr>");
				}
			}
			msgBody.append("</table>");

		} else if (actionType.equalsIgnoreCase("Removed")) {
			msgBody.append("<font style='color:gray'>AccessPolicy ID:</font> "
					+ taes.id + "<br />");
			msgBody.append("<font style='color:gray'>AccessPolicy Name:</font> "
					+ taes.description + "<br />");
			msgBody.append("<font style='color:gray'>OrgCode:</font> "
					+ taes.orgUnitName + "<br />");

		}

		return msgBody.toString();
	}

	private String getAccessPolicyMappingDetails(StringBuilder msgBody,
			String screenType, TAdminEmailSet taes, String actionType) {

		if (actionType.equalsIgnoreCase("Mapped")) {

			// msgBody.append("<font style='color:gray'>Entry Template:</font> "+
			// taes.etName + "<br />");
			msgBody.append("<font style='color:gray'> Access Policy:</font> "
					+ taes.name + "<br />");
			msgBody.append("<font style='color:gray'> OrgCode:</font> "
					+ taes.orgUnitName + "<br />");

		} else if (actionType.equalsIgnoreCase("Removed")) {
			msgBody.append("<font style='color:gray'>AccessPolicy Mapped ID :</font>"
					+ taes.id + " <br />");
			msgBody.append("<font style='color:gray'>Entry Template Name :</font>"
					+ taes.etName + " <br />");
		}

		return msgBody.toString();
	}

	private String getECMLookupDetails(StringBuilder msgBody,
			String screenType, TAdminEmailSet taes, String actionType) {
		if (actionType.equalsIgnoreCase("Lookup Added")) {
			msgBody.append("<font style='color:gray'>Name:</font> " + taes.name
					+ "<br />");
		} else if (actionType.equalsIgnoreCase("Lookup Updated")) {
			msgBody.append("<font style='color:gray'>Name:</font> " + taes.name
					+ "<br />");
		}

		else if (actionType.equalsIgnoreCase("Added")) {
			msgBody.append("<font style='color:gray'>LookUp Added:</font><br />");
			/*
			 * msgBody.append("<font style='color:gray'>Label:</font> "+
			 * taes.label + "<br />");
			 * msgBody.append("<font style='color:gray'> Value:</font> "+
			 * taes.value+ "<br />");
			 */

		} else if (actionType.equalsIgnoreCase("Updated")) {
			msgBody.append("<font style='color:gray'>LookUp Updated:</font><br />");
			/*
			 * msgBody.append("<font style='color:gray'>ID:</font> "+ taes.id +
			 * "<br />");
			 * msgBody.append("<font style='color:gray'>Label:</font> "+
			 * taes.label + "<br />");
			 * msgBody.append("<font style='color:gray'> Value:</font> "+
			 * taes.value+ "<br />");
			 */

		} else if (actionType.equalsIgnoreCase("Deleted")) {
			msgBody.append("<font style='color:gray'>LookUp ID:</font> "
					+ taes.id + "<br />");
			msgBody.append("<font style='color:gray'>LookUp Name:</font> "
					+ taes.name + "<br />");
		}

		else if (actionType.equalsIgnoreCase("Removed")) {
			msgBody.append("<font style='color:gray'>ID:</font> " + taes.id
					+ "<br />");
		}

		return msgBody.toString();
	}

	private String getECMLookupMappingDetails(StringBuilder msgBody,
			String screenType, TAdminEmailSet taes, String actionType) {
		if (actionType.equalsIgnoreCase("Added")) {

			msgBody.append("<font style='color:gray'>Property:</font> "
					+ taes.prop + "<br />");
			// msgBody.append("<font style='color:gray'>Organization Unit:</font> "+
			// taes.orgUnit + "<br />");
			msgBody.append("<font style='color:gray'> Entry Template:</font> "
					+ taes.template + "<br />");
			// msgBody.append("<font style='color:gray'> Entry Template:</font> "+
			// taes.etName+ "<br />");
		} else if (actionType.equalsIgnoreCase("Removed")) {

			msgBody.append("<font style='color:gray'>ECM Lookup Mapping Deleted:</font><br />");
			msgBody.append("<font style='color:gray'>Property:</font> "
					+ taes.prop + "<br />");
			// msgBody.append("<font style='color:gray'>Organization Unit:</font> "+
			// taes.orgUnit + "<br />");
			msgBody.append("<font style='color:gray'> Entry Template:</font> "
					+ taes.template + "<br />");

		}
		if (actionType.equalsIgnoreCase("addValue")) {
			msgBody.append("<font style='color:gray'>ID:</font> " + taes.id
					+ "<br />");
			msgBody.append("<font style='color:gray'>Label:</font> "
					+ taes.label + "<br />");
			msgBody.append("<font style='color:gray'> Value:</font> "
					+ taes.value + "<br />");

		} else if (actionType.equalsIgnoreCase("Update")) {
			msgBody.append("<font style='color:gray'>ID:</font> " + taes.id
					+ "<br />");
			msgBody.append("<font style='color:gray'>Label:</font> "
					+ taes.label + "<br />");
			msgBody.append("<font style='color:gray'> Value:</font> "
					+ taes.value + "<br />");
		} else if (actionType.equalsIgnoreCase("deleteID")) {
			msgBody.append("<font style='color:gray'>ID:</font> " + taes.id
					+ "<br />");
		} else if (actionType.equalsIgnoreCase("RemoveAPM")) {
			msgBody.append("<font style='color:gray'>Access Policy Mapping ID </font>&nbsp;&nbsp;&nbsp;"
					+ taes.id
					+ "&nbsp;&nbsp;&nbsp;<font style='color:gray'>Deleted </font>");
		} else if (actionType.equalsIgnoreCase("deleteValues")) {
			msgBody.append("<font style='color:gray'>ID:</font> " + taes.id
					+ "<br />");
		}

		return msgBody.toString();
	}

	private String getECMNewsDetails(StringBuilder msgBody, String screenType,
			TAdminEmailSet taes, String actionType) {
		if (actionType.equalsIgnoreCase("Inserted")) {
			msgBody.append("<font style='color:gray'>Subject:</font> "
					+ taes.subject + "<br />");
			msgBody.append("<font style='color:gray'>News:</font> "
					+ taes.message + "<br />");
			msgBody.append("<font style='color:gray'>Modified BY</font> "
					+ taes.createdBy + "<br />");
			msgBody.append("<font style='color:gray'> Active From:</font> "
					+ taes.activeDate + "<br />");
			msgBody.append("<font style='color:gray'> Expiry On:</font> "
					+ taes.expiryDate + "<br />");

		} else if (actionType.equalsIgnoreCase("Updated")) {
			msgBody.append("<font style='color:gray'>Subject:</font> "
					+ taes.subject + "<br />");
			msgBody.append("<font style='color:gray'>News:</font> "
					+ taes.message + "<br />");
			msgBody.append("<font style='color:gray'>Modified BY</font> "
					+ taes.modifiedBy + "<br />");
			msgBody.append("<font style='color:gray'> Active From:</font> "
					+ taes.activeDate + "<br />");
			msgBody.append("<font style='color:gray'> Active From:</font> "
					+ taes.expiryDate + "<br />");
		} else if (actionType.equalsIgnoreCase("Removed")) {
			msgBody.append("<font style='color:gray'>Subject:</font> "
					+ taes.subject + "<br />");
			msgBody.append("<font style='color:gray'>ID:</font> " + taes.id
					+ "<br />");

		}
		return msgBody.toString();
	}

	private String getECMRoleManagementDetails(StringBuilder msgBody,
			String screenType, TAdminEmailSet taes, String actionType) {
		if (actionType.equalsIgnoreCase("Added")) {
			msgBody.append("<font style='color:gray'>Type:</font> " + taes.type
					+ "<br />");
			msgBody.append("<font style='color:gray'>Name:</font> " + taes.name
					+ "<br />");
			if(taes.type.equalsIgnoreCase("Group")){
				msgBody.append("<font style='color:gray'>Parent Role:</font> "
						+ taes.parentRole + "<br />");
			}
			if(taes.type.equalsIgnoreCase("Role")){
				msgBody.append("<font style='color:gray'>Active Directory Group::</font> "
						+ taes.adGroup + "<br />");
				msgBody.append("<font style='color:gray'>Parent Role:</font> "
						+ taes.parentRole + "<br />");
				msgBody.append("<font style='color:gray'> eSign Allowed:</font> "
						+ taes.iseSignAllowed + "<br />");
				msgBody.append("<font style='color:gray'> Initial Allowed:</font> "
						+ taes.isInitalAllowed + "<br />");
			}
			
		} else if (actionType.equalsIgnoreCase("Updated")) {
			msgBody.append("<font style='color:gray'>Type:</font> " + taes.type
					+ "<br />");
			msgBody.append("<font style='color:gray'>Name:</font> " + taes.name
					+ "<br />");
			if(taes.type.equalsIgnoreCase("Group")){
				msgBody.append("<font style='color:gray'>Parent Role:</font> "
						+ taes.parentRole + "<br />");
			}
			if(taes.type.equalsIgnoreCase("Role")){
				msgBody.append("<font style='color:gray'>Active Directory Group::</font> "
						+ taes.adGroup + "<br />");
				msgBody.append("<font style='color:gray'>Parent Role:</font> "
						+ taes.parentRole + "<br />");
				msgBody.append("<font style='color:gray'> eSign Allowed:</font> "
						+ taes.iseSignAllowed + "<br />");
				msgBody.append("<font style='color:gray'> Initial Allowed:</font> "
						+ taes.isInitalAllowed + "<br />");
			}
		} else if (actionType.equalsIgnoreCase("Deleted")) {
				msgBody.append("<font style='color:gray'>Role Id:</font> " + taes.id
						+ "<br />");
				msgBody.append("<font style='color:gray'>Role Name:</font> " + taes.name
						+ "<br />");

		}
		return msgBody.toString();
	}

	private String getECMEntryTemplateMappingDetails(StringBuilder msgBody,
			String screenType, TAdminEmailSet taes, String actionType) {
		if (actionType.equalsIgnoreCase("Inserted")) {
			msgBody.append("<font style='color:gray'>EntryTemplates:</font> "
					+ taes.etName + "<br />");

			if ((taes.isVisible == null)
					|| (taes.isVisible.trim().equalsIgnoreCase("true"))
					|| (taes.isVisible.trim().equalsIgnoreCase("Yes"))) {
				msgBody.append("<font style='color:gray'>  Visible:</font>Yes<br />");
			} else {
				msgBody.append("<font style='color:gray'>  Visible:</font> NO<br />");
			}

		} else if (actionType.equalsIgnoreCase("Removed")) {
			msgBody.append("<font style='color:gray'>EntryTemplates ID:</font> "
					+ taes.etName + "<br />");

		}
		return msgBody.toString();
	}

	private String getECMINTEGRATIONDetails(StringBuilder msgBody,
			String screenType, TAdminEmailSet taes, String actionType) {
		if (actionType.equalsIgnoreCase("Inserted")) {
			msgBody.append("<font style='color:gray'>Integration Name:</font> "
					+ taes.appId + "<br />");
			msgBody.append("<font style='color:gray'>Integration Coordinator:</font> "
					+ taes.coordinator + "<br />");
			msgBody.append("<font style='color:gray'>Descripation:</font> "
					+ taes.description + "<br />");
			msgBody.append("<font style='color:gray'>Modifier Name:</font> "
					+ taes.modifiedBy + "<br />");
			msgBody.append("<font style='color:gray'>Creator Name:</font> "
					+ taes.createdBy + "<br />");

		} else if (actionType.equalsIgnoreCase("Updated")) {
			msgBody.append("<font style='color:gray'>Integration Name:</font> "
					+ taes.appId + "<br />");
			msgBody.append("<font style='color:gray'>Modifier Name:</font> "
					+ taes.modifiedBy + "<br />");
			msgBody.append("<font style='color:gray'>Integration Coordinator:</font> "
					+ taes.coordinator + "<br />");
			msgBody.append("<font style='color:gray'>Descripation:</font> "
					+ taes.description + "<br />");

		} else if (actionType.equalsIgnoreCase("Removed")) {
			msgBody.append("<font style='color:gray'>Integration ID </font> "
					+ taes.id + "<br />");
			msgBody.append("<font style='color:gray'>Integration Name:</font> "
					+ taes.appId + "<br />");
			msgBody.append("<font style='color:gray'>Integration Coordinator:</font> "
					+ taes.coordinator + "<br />");
			msgBody.append("<font style='color:gray'>Descripation:</font> "
					+ taes.description + "<br />");

		}
		return msgBody.toString();
	}

	private String getECMUsersDetails(StringBuilder msgBody, String screenType,
			TAdminEmailSet taes, String actionType) {
		if (actionType.equalsIgnoreCase("Updated")) {
			 msgBody.append("<font style='color:gray'>Employee Name:</font> "
					+ taes.fullName + "<br />");
			 msgBody.append("<font style='color:gray'>Employee No:</font> "
					+ taes.EmpNo + "<br />");
			 msgBody.append("<font style='color:gray'>eSign Allowed:</font> "+
			 taes.iseSignAllowed + "<br />");
			 msgBody.append("<font style='color:gray'>Initial Allowed:</font> "+
			 taes.isInitalAllowed+ "<br />");
		}

		return msgBody.toString();
	}

	private String getECMReportUsersDetails(StringBuilder msgBody,
			String screenType, TAdminEmailSet taes, String actionType) {
		if (actionType.equalsIgnoreCase("Inserted")) {
			msgBody.append("<font style='color:gray'>Employee Name:</font> "
					+ taes.fullName + "<br />");
			msgBody.append("<font style='color:gray'>Employee No:</font> "
					+ taes.EmpNo + "<br />");
			if(taes.isadmin.equalsIgnoreCase("Y")){
				msgBody.append("<font style='color:gray'>Report Admin:</font> "
						+ " Yes <br />");
			} else {
				msgBody.append("<font style='color:gray'>Report Admin:</font> "
						+ " No <br />");
			}
		} else if (actionType.equalsIgnoreCase("Removed")) {
			msgBody.append("<font style='color:gray'>Employee Name:</font> "
					+ taes.fullName + "<br />");
			msgBody.append("<font style='color:gray'> Employee No:</font> "
					+ taes.EmpNo + "<br />");
			if(taes.isadmin.equalsIgnoreCase("Y")){
				msgBody.append("<font style='color:gray'>Report Admin:</font> "
						+ " Yes <br />");
			} else {
				msgBody.append("<font style='color:gray'>Report Admin:</font> "
						+ " No <br />");
			}
		}

		return msgBody.toString();
	}

	private String getECMAdministratorDetails(StringBuilder msgBody,
			String screenType, TAdminEmailSet taes, String actionType) {
		if (actionType.equalsIgnoreCase("Inserted")) {
			msgBody.append("<font style='color:gray'>Employee Name:</font> "
					+ taes.fullName + "<br />");
			msgBody.append("<font style='color:gray'>EmpNo:</font> "
					+ taes.EmpNo + "<br />");
			msgBody.append("<font style='color:gray'>CreatedBy:</font> "
					+ taes.createdBy + "<br />");
			msgBody.append("<font style='color:gray'>Justification:</font> "
					+ taes.justification + "<br />");
		} else if (actionType.equalsIgnoreCase("Removed")) {
			msgBody.append("<font style='color:gray'>Employee Name:</font> "
					+ taes.fullName + "<br />");
			msgBody.append("<font style='color:gray'> Employee No:</font> "
					+ taes.EmpNo + "<br />");
		}
		return msgBody.toString();
	}

	private String getECMExcludedOperatorDetails(StringBuilder msgBody,
			String screenType, TAdminEmailSet taes, String actionType) {
		if (actionType.equalsIgnoreCase("Inserted")) {
			msgBody.append("<font style='color:gray'>Employee Name:</font> "
					+ taes.fullName + "<br />");
			msgBody.append("<font style='color:gray'>Employee No:</font> "
					+ taes.EmpNo + "<br />");
		} else if (actionType.equalsIgnoreCase("Removed")) {
			msgBody.append("<font style='color:gray'>Employee Name:</font> "
					+ taes.fullName + "<br />");
			msgBody.append("<font style='color:gray'> Employee No:</font> "
					+ taes.EmpNo + "<br />");
		}
		return msgBody.toString();
	}

	private String getECMGlobalListDetails(StringBuilder msgBody,
			String screenType, TAdminEmailSet taes, String actionType) {
		if (actionType.equalsIgnoreCase("Created")) {
			msgBody.append("<font style='color:gray'>EmpNo:</font> "
					+ taes.EmpNo + "<br />");
			msgBody.append("<font style='color:gray'>Name:</font> " + taes.name
					+ "<br />");
			msgBody.append("<font style='color:gray'>isGlobal:</font> "
					+ taes.isGlobal + "<br />");
			msgBody.append("<font style='color:gray'>Types:</font> "
					+ taes.type + "<br />");
		}
		if (actionType.equalsIgnoreCase("Updated")) {
			msgBody.append("<font style='color:gray'>User Name:</font> "
					+ taes.fullName + "<br />");
			msgBody.append("<font style='color:gray'>List Name:</font> " 
					+ taes.name + "<br />");
			msgBody.append("<font style='color:gray'>isGlobal:</font> "
					+ taes.isGlobal + "<br />");
			
			msgBody.append("<table><tr><th>Users</th></tr>");
			for(TUser user: taes.users){
				msgBody.append("<tr><td><font style='color:gray'>"
						+ user.fulName
						+ "</font></td>&nbsp;&nbsp;");
			}
			msgBody.append("</table>");
		}
		if (actionType.equalsIgnoreCase("Inserted")) {
			msgBody.append("<font style='color:gray'>User Name:</font> "
					+ taes.fullName + "<br />");
			msgBody.append("<font style='color:gray'>List Name:</font> " 
					+ taes.name + "<br />");
			msgBody.append("<font style='color:gray'>isGlobal:</font> "
					+ taes.isGlobal + "<br />");
			
			msgBody.append("<table><tr><th>Users</th></tr>");
			for(TUser user: taes.users){
				msgBody.append("<tr><td><font style='color:gray'>"
						+ user.fulName
						+ "</font></td>&nbsp;&nbsp;");
			}
			msgBody.append("</table>");
		}

		else if (actionType.equalsIgnoreCase("Removed")) {
			msgBody.append("<font style='color:gray'>User Name:</font> "
					+ taes.fullName + "<br />");
			msgBody.append("<font style='color:gray'>List Name:</font> " 
					+ taes.name + "<br />");
		}
		return msgBody.toString();
	}

	private Object TPermission(ArrayList<TPermission> permissions) {
		// TODO Auto-generated method stub
		return null;
	}

	private void loadEmailConfigurations() {
		if (mailConfigLoaded)
			return;
		try {
			ECMConfigurationList cfgList = ECMConfigurationList.getInstance(
					"ECM", "APP");
			mailObject.setUserName(cfgList.getConfigValue("SMTPUSER"));
			mailObject.setPassword(cfgList.getConfigValue("SMTPPASSWORD"));
			mailObject.setEmailFrom(cfgList.getConfigValue("SMTPSENTFROM"));
			mailObject.setSMTPServer(cfgList.getConfigValue("SMTPSERVER"));
			mailConfigLoaded = true;
		} catch (Exception e) {
		}
	}

private String getDocumentViewURL(String docId, String viewerURL, String docTitle, String objStoreName) throws Exception {

		String strUrl = viewerURL.trim() + "/getContent?id=" + docId + "&streamer=true&objectType=document&objectStoreName=" + objStoreName;
		return "<a href='" + strUrl + "'>" + docTitle.trim() + "</a>";

	}

}
