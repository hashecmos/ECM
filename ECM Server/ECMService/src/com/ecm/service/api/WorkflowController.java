package com.ecm.service.api;

import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.ecm.db.list.ECMActionList;
import com.ecm.db.list.ECMProgressList;
import com.ecm.db.list.ECMWorkflowList;
import com.ecm.db.list.ECMWorkitemList;
import com.ecm.db.transport.TAction;
import com.ecm.db.transport.TAttachment;
import com.ecm.db.transport.TDocPrincipal;
import com.ecm.db.transport.TRecallAction;
import com.ecm.db.transport.TRecipient;
import com.ecm.db.transport.TWorkflowDetails;
import com.ecm.db.transport.TWorkflowHistory;
import com.ecm.db.transport.TWorkitemAction;
import com.ecm.db.transport.TWorkitemDetails;
import com.ecm.db.transport.TWorkitemProgress;
import com.ecm.db.transport.TWorkitemQuery;
import com.ecm.db.transport.TWorkitemSet;
import com.ecm.db.transport.TWorkitemStat;
import com.ecm.db.util.DBUtil;
import com.ecm.db.util.ECMLogger;
import com.ecm.filenet.list.FNObjectStoreList;
import com.ecm.filenet.model.FNAuthenticator;
import com.ecm.filenet.model.FNObjectStore;
import com.ecm.service.object.AccessPolicyHelper;
import com.ecm.service.object.AnnotationHelper;
import com.ecm.service.object.JSONHelper;
import com.ecm.service.object.ResponseObject;
import com.ecm.service.object.eSignHelper;

@Path("/WorkflowService")
@ApplicationPath("resources")
public class WorkflowController {
	private static ECMLogger logger = ECMLogger.getInstance(WorkflowController.class);

	// http://localhost:9080/ECMService/resources/WorkflowService/getActions?empNo=112
	@GET
	@Path("/getActions")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getActions(@QueryParam("empNo") String empNo, @Context HttpServletRequest req,
			@Context HttpServletResponse resp) throws Exception {
		try {
			FNAuthenticator.Authenticate(empNo);

			ArrayList<TAction> actions = ECMActionList.getInstance().getAllActions();
			return Response.ok().entity(actions).build();
		} catch (Exception e) {
			logger.logException(e);
			ResponseObject responseObject = ResponseObject.getResponseObject(e.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject).build();
		}
	}

	// http://localhost:9080/ECMService/resources/WorkflowService/getUserInbox?empNo=112
	@GET
	@Path("/getUserInbox")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getUserInbox(@QueryParam("empNo") String empNo, @QueryParam("pageNo") String pageNo,
			@QueryParam("sort") String sort, @QueryParam("order") String order, @Context HttpServletRequest req,
			@Context HttpServletResponse resp) throws Exception {
		try {
			FNAuthenticator.Authenticate(empNo);

			TWorkitemSet inbox = ECMWorkitemList.getInstance().getUserInboxItems(DBUtil.stringToLong(empNo),
					DBUtil.stringToLong(pageNo), sort, order);
			return Response.ok().entity(inbox).build();
			// return Response.ok().entity(inbox.workitems).build();
		} catch (Exception e) {
			logger.logException(e);
			ResponseObject responseObject = ResponseObject.getResponseObject(e.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject).build();
		}
	}

	// http://localhost:9080/ECMService/resources/WorkflowService/getUserSentItems?empNo=112
	@GET
	@Path("/getUserSentItems")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getUserSentItems(@QueryParam("empNo") String empNo, @QueryParam("pageNo") String pageNo,
			@QueryParam("sort") String sort, @QueryParam("order") String order, @Context HttpServletRequest req,
			@Context HttpServletResponse resp) throws Exception {
		try {
			FNAuthenticator.Authenticate(empNo);

			TWorkitemSet sentItems = ECMWorkitemList.getInstance().getUserSentItems(DBUtil.stringToLong(empNo),
					DBUtil.stringToLong(pageNo), sort, order);
			return Response.ok().entity(sentItems).build();
			// return Response.ok().entity(sentItems.workitems).build();
		} catch (Exception e) {
			logger.logException(e);
			ResponseObject responseObject = ResponseObject.getResponseObject(e.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject).build();
		}
	}
	
	// http://localhost:9080/ECMService/resources/WorkflowService/getUserNewWorkitems?empNo=112
	@GET
	@Path("/getUserNewWorkitems")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getUserNewWorkitems(@QueryParam("empNo") String empNo, @Context HttpServletRequest req,
			@Context HttpServletResponse resp) throws Exception {
		try {
			FNAuthenticator.Authenticate(empNo);
			TWorkitemSet inbox = ECMWorkitemList.getInstance().searchNewWorkitemCount(empNo);
			return Response.ok().entity(inbox).build();
		} catch (Exception e) {
			logger.logException(e);
			ResponseObject responseObject = ResponseObject.getResponseObject(e.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject).build();
		}
	}

	// http://localhost:9080/ECMService/resources/WorkflowService/getRoleInbox?roleId=12&empNo=112
	@GET
	@Path("/getRoleInbox")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getRoleInbox(@QueryParam("roleId") String roleId, @QueryParam("empNo") String empNo,
			@QueryParam("pageNo") String pageNo, @QueryParam("sort") String sort, @QueryParam("order") String order,
			@Context HttpServletRequest req, @Context HttpServletResponse resp) throws Exception {
		try {
			FNAuthenticator.Authenticate(empNo);

			TWorkitemSet inbox = ECMWorkitemList.getInstance().getRoleInboxItems(DBUtil.stringToLong(roleId),
					DBUtil.stringToLong(empNo), DBUtil.stringToLong(pageNo), sort, order);
			return Response.ok().entity(inbox).build();
			// return Response.ok().entity(inbox.workitems).build();
		} catch (Exception e) {
			logger.logException(e);
			ResponseObject responseObject = ResponseObject.getResponseObject(e.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject).build();
		}
	}

	// http://localhost:9080/ECMService/resources/WorkflowService/getRoleSentItems?roleId=12
	@GET
	@Path("/getRoleSentItems")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getRoleSentItems(@QueryParam("roleId") String roleId, @QueryParam("empNo") String empNo,
			@QueryParam("pageNo") String pageNo, @QueryParam("sort") String sort, @QueryParam("order") String order,
			@Context HttpServletRequest req, @Context HttpServletResponse resp) throws Exception {
		try {
			FNAuthenticator.Authenticate(empNo);

			TWorkitemSet sentItems = ECMWorkitemList.getInstance().getRoleSentItems(DBUtil.stringToLong(roleId),
					DBUtil.stringToLong(empNo), DBUtil.stringToLong(pageNo), sort, order);
			return Response.ok().entity(sentItems).build();
			// return Response.ok().entity(sentItems.workitems).build();
		} catch (Exception e) {
			logger.logException(e);
			ResponseObject responseObject = ResponseObject.getResponseObject(e.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject).build();
		}
	}

	// http://localhost:9080/ECMService/resources/WorkflowService/getUserArchiveInbox?empNo=112
	@GET
	@Path("/getUserArchiveInbox")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getUserArchiveInbox(@QueryParam("empNo") String empNo, @QueryParam("pageNo") String pageNo,
			@QueryParam("sort") String sort, @QueryParam("order") String order, @Context HttpServletRequest req,
			@Context HttpServletResponse resp) throws Exception {
		try {
			FNAuthenticator.Authenticate(empNo);

			TWorkitemSet inbox = ECMWorkitemList.getInstance().getUserArchiveInboxItems(DBUtil.stringToLong(empNo),
					DBUtil.stringToLong(pageNo), sort, order);

			return Response.ok().entity(inbox).build();
			// return Response.ok().entity(inbox.workitems).build();
		} catch (Exception e) {
			logger.logException(e);
			ResponseObject responseObject = ResponseObject.getResponseObject(e.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject).build();
		}
	}

	// http://localhost:9080/ECMService/resources/WorkflowService/getUserArchiveSentItems?empNo=112
	@GET
	@Path("/getUserArchiveSentItems")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getUserArchiveSentItems(@QueryParam("empNo") String empNo, @QueryParam("pageNo") String pageNo,
			@QueryParam("sort") String sort, @QueryParam("order") String order, @Context HttpServletRequest req,
			@Context HttpServletResponse resp) throws Exception {
		try {
			FNAuthenticator.Authenticate(empNo);

			TWorkitemSet sentItems = ECMWorkitemList.getInstance().getUserArchiveSentItems(DBUtil.stringToLong(empNo),
					DBUtil.stringToLong(pageNo), sort, order);

			return Response.ok().entity(sentItems).build();
			// return Response.ok().entity(sentItems.workitems).build();
		} catch (Exception e) {
			logger.logException(e);
			ResponseObject responseObject = ResponseObject.getResponseObject(e.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject).build();
		}
	}

	// http://localhost:9080/ECMService/resources/WorkflowService/getRoleArchiveInbox?roleId=12&empNo=112
	@GET
	@Path("/getRoleArchiveInbox")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getRoleArchiveInbox(@QueryParam("roleId") String roleId, @QueryParam("empNo") String empNo,
			@QueryParam("pageNo") String pageNo, @QueryParam("sort") String sort, @QueryParam("order") String order,
			@Context HttpServletRequest req, @Context HttpServletResponse resp) throws Exception {
		try {
			FNAuthenticator.Authenticate(empNo);

			TWorkitemSet inbox = ECMWorkitemList.getInstance().getRoleArchiveInboxItems(DBUtil.stringToLong(roleId),
					DBUtil.stringToLong(roleId), DBUtil.stringToLong(pageNo), sort, order);
			return Response.ok().entity(inbox).build();
			// return Response.ok().entity(inbox.workitems).build();
		} catch (Exception e) {
			logger.logException(e);
			ResponseObject responseObject = ResponseObject.getResponseObject(e.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject).build();
		}
	}

	// http://localhost:9080/ECMService/resources/WorkflowService/getRoleArchiveSentItems?roleId=12
	@GET
	@Path("/getRoleArchiveSentItems")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getRoleArchiveSentItems(@QueryParam("roleId") String roleId, @QueryParam("empNo") String empNo,
			@QueryParam("pageNo") String pageNo, @QueryParam("sort") String sort, @QueryParam("order") String order,
			@Context HttpServletRequest req, @Context HttpServletResponse resp) throws Exception {
		try {
			FNAuthenticator.Authenticate(empNo);

			TWorkitemSet sentItems = ECMWorkitemList.getInstance().getRoleArchiveSentItems(DBUtil.stringToLong(roleId),
					DBUtil.stringToLong(empNo), DBUtil.stringToLong(pageNo), sort, order);

			return Response.ok().entity(sentItems).build();
			// return Response.ok().entity(sentItems.workitems).build();
		} catch (Exception e) {
			logger.logException(e);
			ResponseObject responseObject = ResponseObject.getResponseObject(e.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject).build();
		}
	}

	// http://localhost:9080/ECMService/resources/WorkflowService/getWorkitemDetails?witmid=12&empNo=112
	@GET
	@Path("/getWorkitemDetails")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getWorkitemDetails(@QueryParam("witmid") String witemId, @QueryParam("empNo") String empNo,
			@Context HttpServletRequest req, @Context HttpServletResponse resp) throws Exception {
		try {
			FNAuthenticator.Authenticate(empNo);

			TWorkitemDetails wi = ECMWorkitemList.getInstance().getWorkitemDetails(DBUtil.stringToLong(witemId));
			ECMWorkitemList.getInstance().readWorkitem(DBUtil.stringToLong(witemId), DBUtil.stringToLong(empNo));
			return Response.ok().entity(wi).build();
		} catch (Exception e) {
			logger.logException(e);
			ResponseObject responseObject = ResponseObject.getResponseObject(e.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject).build();
		}
	}

	// http://localhost:9080/ECMService/resources/WorkflowService/getWorkitemProgress?witmid=12
	@GET
	@Path("/getWorkitemProgress")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getWorkitemProgress(@QueryParam("witmid") String witemId, @QueryParam("status") String status,
			@Context HttpServletRequest req, @Context HttpServletResponse resp) throws Exception {
		try {
			ArrayList<TWorkitemProgress> wipList = ECMProgressList.getInstance()
					.getWorkItemProgress(DBUtil.stringToLong(witemId), status);
			return Response.ok().entity(wipList).build();
		} catch (Exception e) {
			logger.logException(e);
			ResponseObject responseObject = ResponseObject.getResponseObject(e.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject).build();
		}
	}

	// http://localhost:9080/ECMService/resources/WorkflowService/saveWorkitemProgress
	@POST
	@Path("/saveWorkitemProgress")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response saveWorkitemProgress(String jsonString, @Context HttpServletRequest req,
			@Context HttpServletResponse resp) throws Exception {
		try {
			TWorkitemProgress wiProgress = JSONHelper.getInstance().getWIProgress(jsonString);
			ECMProgressList.getInstance().saveProgress(wiProgress);
			return Response.ok().entity("OK").build();
		} catch (Exception e) {
			logger.logException(e);
			e.printStackTrace();
			throw new Exception(e.getMessage());
		}
	}

	// http://localhost:9080/ECMService/resources/WorkflowService/removeWorkitemProgress?id=1
	@GET
	@Path("/removeWorkitemProgress")
	@Produces(MediaType.APPLICATION_JSON)
	public Response removeWorkitemProgress(@QueryParam("id") String Id, @Context HttpServletRequest req,
			@Context HttpServletResponse resp) throws Exception {
		try {
			ECMProgressList.getInstance().removeProgress(DBUtil.stringToLong(Id));

			return Response.ok().entity("OK").build();
		} catch (Exception e) {
			logger.logException(e);
			ResponseObject responseObject = ResponseObject.getResponseObject(e.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject).build();
		}
	}

	// http://localhost:9080/ECMService/resources/WorkflowService/getFirstWorkitemDetails?sitmid=12&empNo=112
	@GET
	@Path("/getFirstWorkitemDetails")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getFirstWorkitemDetails(@QueryParam("sitmid") String sitemId, @QueryParam("empNo") String empNo,
			@Context HttpServletRequest req, @Context HttpServletResponse resp) throws Exception {
		try {
			FNAuthenticator.Authenticate(empNo);

			TWorkitemDetails wi = ECMWorkitemList.getInstance().getFirstWorkitemDetails(DBUtil.stringToLong(sitemId));
			return Response.ok().entity(wi).build();
		} catch (Exception e) {
			logger.logException(e);
			ResponseObject responseObject = ResponseObject.getResponseObject(e.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject).build();
		}
	}

	// http://localhost:9080/ECMService/resources/WorkflowService/getSentitemRecipients?sitmid=12
	@GET
	@Path("/getSentitemRecipients")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getSentitemRecipients(@QueryParam("sitmid") String sitemId, @Context HttpServletRequest req,
			@Context HttpServletResponse resp) throws Exception {
		try {
			ArrayList<TRecipient> rList = ECMWorkitemList.getInstance()
					.getSentitemRecipients(DBUtil.stringToLong(sitemId));
			return Response.ok().entity(rList).build();
		} catch (Exception e) {
			logger.logException(e);
			ResponseObject responseObject = ResponseObject.getResponseObject(e.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject).build();
		}
	}

	// http://localhost:9080/ECMService/resources/WorkflowService/getInboxFilterUsers?userid=12&usertype=USER
	@GET
	@Path("/getInboxFilterUsers")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getInboxFilterUsers(@QueryParam("userid") String userId, @QueryParam("usertype") String userType,
			@QueryParam("status") String sysStatus, @Context HttpServletRequest req, @Context HttpServletResponse resp)
					throws Exception {
		try {
			ArrayList<TRecipient> rList = ECMWorkitemList.getInstance().getInboxFilterUsers(DBUtil.stringToLong(userId),
					userType, sysStatus);
			return Response.ok().entity(rList).build();
		} catch (Exception e) {
			logger.logException(e);
			ResponseObject responseObject = ResponseObject.getResponseObject(e.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject).build();
		}
	}

	// http://localhost:9080/ECMService/resources/WorkflowService/getSentitemFilterUsers?userid=12&usertype=USER
	@GET
	@Path("/getSentitemFilterUsers")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getSentitemFilterUsers(@QueryParam("userid") String userId, @QueryParam("usertype") String userType,
			@QueryParam("status") String sysStatus, @Context HttpServletRequest req, @Context HttpServletResponse resp)
					throws Exception {
		try {
			ArrayList<TRecipient> rList = ECMWorkitemList.getInstance()
					.getSentitemFilterUsers(DBUtil.stringToLong(userId), userType, sysStatus);
			return Response.ok().entity(rList).build();
		} catch (Exception e) {
			logger.logException(e);
			ResponseObject responseObject = ResponseObject.getResponseObject(e.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject).build();
		}
	}

	// http://localhost:9080/ECMService/resources/WorkflowService/getWorkitemHistory?witmid=1&empNo=112
	@GET
	@Path("/getWorkitemHistory")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getWorkitemHistory(@QueryParam("witmid") String witmId, @QueryParam("empNo") String empNo,
			@Context HttpServletRequest req, @Context HttpServletResponse resp) throws Exception {
		try {
			FNAuthenticator.Authenticate(empNo);

			ArrayList<TWorkflowHistory> wiHistory = ECMWorkitemList.getInstance()
					.getWorkitemHistory(DBUtil.stringToLong(witmId), DBUtil.stringToLong(empNo));
			return Response.ok().entity(wiHistory).build();
		} catch (Exception e) {
			logger.logException(e);
			ResponseObject responseObject = ResponseObject.getResponseObject(e.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject).build();
		}
	}
	
	// http://localhost:9080/ECMService/resources/WorkflowService/getSentitemHistory?witmid=1&empNo=112
	@GET
	@Path("/getSentitemHistory")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getSentitemHistory(@QueryParam("sitmid") String sitmId, @QueryParam("empNo") String empNo,
			@Context HttpServletRequest req, @Context HttpServletResponse resp) throws Exception {
		try {
			FNAuthenticator.Authenticate(empNo);

			ArrayList<TWorkflowHistory> wiHistory = ECMWorkitemList.getInstance()
					.getSentitemHistory(DBUtil.stringToLong(sitmId), DBUtil.stringToLong(empNo));
			return Response.ok().entity(wiHistory).build();
		} catch (Exception e) {
			logger.logException(e);
			ResponseObject responseObject = ResponseObject.getResponseObject(e.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject).build();
		}
	}

	// http://localhost:9080/ECMService/resources/WorkflowService/getSentItemWorkItems?witmid=1&empNo=112
	@GET
	@Path("/getSentItemWorkItems")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getSentItemWorkItems(@QueryParam("witmid") String witmId, @QueryParam("empNo") String empNo,
			@QueryParam("pageNo") String pageNo, @QueryParam("status") String status, @Context HttpServletRequest req,
			@Context HttpServletResponse resp) throws Exception {
		try {
			FNAuthenticator.Authenticate(empNo);
			if ((status == null) || (status.length() <= 0))
				status = "ACTIVE";
			TWorkitemSet wiList = ECMWorkitemList.getInstance().getSentItemWorkItems(DBUtil.stringToLong(witmId),
					DBUtil.stringToLong(pageNo), status.toUpperCase());
			// return Response.ok().entity(wiList).build();
			return Response.ok().entity(wiList.workitems).build();
		} catch (Exception e) {
			logger.logException(e);
			ResponseObject responseObject = ResponseObject.getResponseObject(e.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject).build();
		}
	}

	// http://localhost:9080/ECMService/resources/WorkflowService/launchWorkflow
	@POST
	@Path("/launchWorkflow")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response launchWorkflow(String jsonString, @Context HttpServletRequest req,
			@Context HttpServletResponse resp) throws Exception {
		try {
			logger.info("Launch: Input = " + jsonString);
			TWorkitemAction twa = JSONHelper.getInstance().getWorkitemAction(jsonString);
			long wfID = 0;
			if (twa.draft)
				ECMWorkitemList.getInstance().saveActionAsDraft(twa.EMPNo, twa.roleId, twa.draftId,
						twa.wiAction.toUpperCase(), jsonString);
			else {
				FNObjectStore os = FNObjectStoreList.getInstance().getApplicationObjectStore("ECM", "ECMOS");
				//FNPowerObjectStore pos = FNPowerObjectStoreList.getInstance().getPowerObjectStore("ECM", "ECMOS");
				logger.info("Launch: OS = " + os.getName() + "(" + os.getId() + ")");
				AccessPolicyHelper.getInstance().setWorkflowAccessPolicy(twa, os);
				logger.info("Launch: Set Workflow Access Policy");
				AnnotationHelper.getInstance().setAnnotation(twa, os);
				logger.info("Launch: Set Annotation Security Permissions");
				wfID = ECMWorkitemList.getInstance().launchWorkflow(twa);
				logger.info("Launch: Workflow ID = " + wfID);
				eSignHelper.getInstance().registerForeSign(os, twa, wfID, 0);
				logger.info("Launch: Register for eSign history");
			}
			logger.info("Launch: Success");
			return Response.ok().entity(wfID).build();
		} catch (Exception e) {
			logger.logException(e);
			ResponseObject responseObject = ResponseObject.getResponseObject(e.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject).build();
		}
	}

	// http://localhost:9080/ECMService/resources/WorkflowService/forwardWorkitem
	@POST
	@Path("/forwardWorkitem")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response forwardWorkitem(String jsonString, @Context HttpServletRequest req,
			@Context HttpServletResponse resp) throws Exception {
		try {
			logger.info("Forward: Input = " + jsonString);
			TWorkitemAction twa = JSONHelper.getInstance().getWorkitemAction(jsonString);
			long wItemID = 0;
			if (twa.draft)
				ECMWorkitemList.getInstance().saveActionAsDraft(twa.EMPNo, twa.roleId, twa.draftId, "FORWARD",
						jsonString);
			else {
				FNObjectStore os = FNObjectStoreList.getInstance().getApplicationObjectStore("ECM", "ECMOS");
				logger.info("Forward: OS = " + os.getName() + "(" + os.getId() + ")");
				AccessPolicyHelper.getInstance().setWorkflowAccessPolicy(twa, os);
				logger.info("Forward: Set Workflow Access Policy");
				//FNPowerObjectStore pos = FNPowerObjectStoreList.getInstance().getPowerObjectStore("ECM", "ECMOS");
				AnnotationHelper.getInstance().setAnnotation(twa, os);
				logger.info("Forward: Set Annotation Security Permissions");
				wItemID = ECMWorkitemList.getInstance().forwardWorkitem(twa);
				logger.info("Forward: Success");
				eSignHelper.getInstance().registerForeSign(os, twa, ECMWorkitemList.getInstance().getWorkItemWorkflowID(wItemID), wItemID);
				logger.info("Launch: Register for eSign history");
			}
			return Response.ok().entity("OK").build();
		} catch (Exception e) {
			logger.logException(e);
			ResponseObject responseObject = ResponseObject.getResponseObject(e.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject).build();
		}
	}

	// http://localhost:9080/ECMService/resources/WorkflowService/recallWorkitems
	@POST
	@Path("/recallWorkitems")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response recallWorkitems(String jsonString, @Context HttpServletRequest req,
			@Context HttpServletResponse resp) throws Exception {
		try {
			logger.info("Recall: Input = " + jsonString);
			FNObjectStore os = FNObjectStoreList.getInstance().getApplicationObjectStore("ECM", "ECMOS");
			logger.info("Recall: OS = " + os.getName() + "(" + os.getId() + ")");

			TRecallAction tra = JSONHelper.getInstance().getRecallAction(jsonString);
			ArrayList<TDocPrincipal> docList = ECMWorkitemList.getInstance().recallWorkitems(tra);
			AccessPolicyHelper.getInstance().recallWorkflowSecurity(docList, os);
			logger.info("Recall: Revert/Set Workflow Access Policy");
			AnnotationHelper.getInstance().recallAnnotationSecurity(docList, os);
			logger.info("Recall: Revert/Set Annotation Security Permissions");

			return Response.ok().entity("OK").build();
		} catch (Exception e) {
			logger.logException(e);
			ResponseObject responseObject = ResponseObject.getResponseObject(e.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject).build();
		}
	}

	// http://localhost:9080/ECMService/resources/WorkflowService/replyWorkitem
	@POST
	@Path("/replyWorkitem")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response replyWorkitem(String jsonString, @Context HttpServletRequest req, @Context HttpServletResponse resp)
			throws Exception {
		try {
			logger.info("Reply: Input = " + jsonString);
			TWorkitemAction twa = JSONHelper.getInstance().getWorkitemAction(jsonString);
			long wItemID = 0;
			if (twa.draft)
				ECMWorkitemList.getInstance().saveActionAsDraft(twa.EMPNo, twa.roleId, twa.draftId, "REPLY",
						jsonString);
			else {
				FNObjectStore os = FNObjectStoreList.getInstance().getApplicationObjectStore("ECM", "ECMOS");
				logger.info("Reply: OS = " + os.getName() + "(" + os.getId() + ")");
				AccessPolicyHelper.getInstance().setWorkflowAccessPolicy(twa, os);
				logger.info("Reply: Set Workflow Access Policy");
				//FNPowerObjectStore pos = FNPowerObjectStoreList.getInstance().getPowerObjectStore("ECM", "ECMOS");
				AnnotationHelper.getInstance().setAnnotation(twa, os);
				logger.info("Reply: Set Annotation Security Permissions");
				wItemID = ECMWorkitemList.getInstance().replyWorkitem(twa);
				logger.info("Reply: Success");
				eSignHelper.getInstance().registerForeSign(os, twa, ECMWorkitemList.getInstance().getWorkItemWorkflowID(wItemID), wItemID);
				logger.info("Launch: Register for eSign history");
			}
			return Response.ok().entity("OK").build();
		} catch (Exception e) {
			logger.logException(e);
			ResponseObject responseObject = ResponseObject.getResponseObject(e.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject).build();
		}
	}

	// http://localhost:9080/ECMService/resources/WorkflowService/searchInbox
	@POST
	@Path("/searchInbox")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response searchInbox(String jsonString, @Context HttpServletRequest req, @Context HttpServletResponse resp)
			throws Exception {
		try {
			logger.info("Search Inbox: Input = " + jsonString);
			TWorkitemQuery twq = JSONHelper.getInstance().getWorkitemQuery(jsonString);
			TWorkitemSet ts = ECMWorkitemList.getInstance().searchInboxItems(twq);
			return Response.ok().entity(ts).build();
		} catch (Exception e) {
			logger.logException(e);
			ResponseObject responseObject = ResponseObject.getResponseObject(e.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject).build();
		}
	}
	
	// http://localhost:9080/ECMService/resources/WorkflowService/exportInbox
	@POST
	@Path("/exportInbox")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	public Response exportInbox(String jsonString,
			@Context HttpServletRequest req, @Context HttpServletResponse resp)
			throws Exception {
		try {
			logger.info("Export Inbox to Pdf: Input = " + jsonString);
			TWorkitemQuery twq = JSONHelper.getInstance().getWorkitemQuery(jsonString);
			ECMWorkitemList.getInstance().exportInboxItems(req, resp, twq);
			return Response.ok().entity("OK").build(); 
		} catch (Exception e) {
			logger.logException(e);
			ResponseObject responseObject = ResponseObject.getResponseObject(e.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject).build();
		}
	}

	// http://localhost:9080/ECMService/resources/WorkflowService/exportActioned
	@POST
	@Path("/exportActioned")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	public Response exportActioned(String jsonString, @Context HttpServletRequest req,
			@Context HttpServletResponse resp) throws Exception {
		try {
			logger.info("Export Inbox to Excel: Input = " + jsonString);
			TWorkitemQuery twq = JSONHelper.getInstance().getWorkitemQuery(jsonString);
			ECMWorkitemList.getInstance().exportActionedItems(req, resp, twq);
			return Response.ok().entity("OK").build();
		} catch (Exception e) {
			logger.logException(e);
			ResponseObject responseObject = ResponseObject.getResponseObject(e.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject).build();
		}
	}
	
	// http://localhost:9080/ECMService/resources/WorkflowService/searchSentItems
	@POST
	@Path("/searchSentItems")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response searchSentItems(String jsonString, @Context HttpServletRequest req,
			@Context HttpServletResponse resp) throws Exception {
		try {
			logger.info("Search Sent Items: Input = " + jsonString);
			TWorkitemQuery twq = JSONHelper.getInstance().getWorkitemQuery(jsonString);
			TWorkitemSet ts = ECMWorkitemList.getInstance().searchSentItems(twq);
			return Response.ok().entity(ts).build();
			// return Response.ok().entity(ts.workitems).build();
		} catch (Exception e) {
			logger.logException(e);
			ResponseObject responseObject = ResponseObject.getResponseObject(e.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject).build();
		}
	}
	
	// http://localhost:9080/ECMService/resources/WorkflowService/searchRelatedSentItems
	@POST
	@Path("/searchRelatedSentItems")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response searchRelatedSentItems(String jsonString, @Context HttpServletRequest req,
			@Context HttpServletResponse resp) throws Exception {
		try {
			logger.info("Search Related Sent Items: Input = " + jsonString);
			TWorkitemQuery twq = JSONHelper.getInstance().getWorkitemQuery(jsonString);
			TWorkitemSet ts = ECMWorkitemList.getInstance().searchRelatedSentItems(twq);
			return Response.ok().entity(ts).build();
			// return Response.ok().entity(ts.workitems).build();
		} catch (Exception e) {
			logger.logException(e);
			ResponseObject responseObject = ResponseObject.getResponseObject(e.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject).build();
		}
	}
	
	// http://localhost:9080/ECMService/resources/WorkflowService/searchActionedItems
	@POST
	@Path("/searchActionedItems")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response searchActionedItems(String jsonString, @Context HttpServletRequest req,
			@Context HttpServletResponse resp) throws Exception {
		try {
			logger.info("Search Actioned Items: Input = " + jsonString);
			TWorkitemQuery twq = JSONHelper.getInstance().getWorkitemQuery(jsonString);
			TWorkitemSet ts = ECMWorkitemList.getInstance().searchActionedItems(twq);
			return Response.ok().entity(ts).build();
			// return Response.ok().entity(ts.workitems).build();
		} catch (Exception e) {
			logger.logException(e);
			ResponseObject responseObject = ResponseObject.getResponseObject(e.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject).build();
		}
	}

	// http://localhost:9080/ECMService/resources/WorkflowService/exportSentItems
		@POST
		@Path("/exportSentItems")
		@Consumes(MediaType.APPLICATION_JSON)
		@Produces(MediaType.APPLICATION_OCTET_STREAM)
		public Response exportSentItems(String jsonString, @Context HttpServletRequest req,
				@Context HttpServletResponse resp) throws Exception {
			try {
				logger.info("Export Sent Items: Input = " + jsonString);
				TWorkitemQuery twq = JSONHelper.getInstance().getWorkitemQuery(jsonString);
				ECMWorkitemList.getInstance().exportSentItems(req, resp, twq);
				return Response.ok().entity("OK").build();
			} catch (Exception e) {
				logger.logException(e);
				ResponseObject responseObject = ResponseObject.getResponseObject(e.getMessage());
				return Response.status(Status.BAD_REQUEST).entity(responseObject).build();
			}
	}
	
	// http://localhost:9080/ECMService/resources/WorkflowService/addUserWorkitem
	@POST
	@Path("/addUserWorkitem")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response addUserWorkitem(String jsonString, @Context HttpServletRequest req,
			@Context HttpServletResponse resp) throws Exception {
		try {
			logger.info("Add User: Input = " + jsonString);
			TWorkitemAction twa = JSONHelper.getInstance().getWorkitemAction(jsonString);
			if (twa.draft)
				ECMWorkitemList.getInstance().saveActionAsDraft(twa.EMPNo, twa.roleId, twa.draftId, "ADDUSER",
						jsonString);
			else {
				FNObjectStore os = FNObjectStoreList.getInstance().getApplicationObjectStore("ECM", "ECMOS");
				logger.info("Add User: OS = " + os.getName() + "(" + os.getId() + ")");
				AccessPolicyHelper.getInstance().setWorkflowAccessPolicy(twa, os);
				logger.info("Add User: Set Workflow Access Policy");
				//FNPowerObjectStore pos = FNPowerObjectStoreList.getInstance().getPowerObjectStore("ECM", "ECMOS");
				AnnotationHelper.getInstance().setAnnotation(twa, os);
				logger.info("Add User: Set Annotation Security Permissions");
				ECMWorkitemList.getInstance().addUserToWorkitem(twa);
			}
			return Response.ok().entity("OK").build();
		} catch (Exception e) {
			logger.logException(e);
			ResponseObject responseObject = ResponseObject.getResponseObject(e.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject).build();
		}
	}

	// http://localhost:9080/ECMService/resources/WorkflowService/readWorkitem?witmid=1&empNo=112
	@GET
	@Path("/readWorkitem")
	@Produces(MediaType.APPLICATION_JSON)
	public Response readWorkitem(@QueryParam("witmid") String witmId, @QueryParam("empNo") String empNo,
			@Context HttpServletRequest req, @Context HttpServletResponse resp) throws Exception {
		try {
			logger.info("Read: Input = Witem (" + witmId + ") Emp No: (" + empNo + ")");
			FNAuthenticator.Authenticate(empNo);

			ECMWorkitemList.getInstance().readWorkitem(DBUtil.stringToLong(witmId), DBUtil.stringToLong(empNo));

			return Response.ok().entity("OK").build();
		} catch (Exception e) {
			logger.logException(e);
			ResponseObject responseObject = ResponseObject.getResponseObject(e.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject).build();
		}
	}

	// http://localhost:9080/ECMService/resources/WorkflowService/archiveWorkitem?witmid=1&empNo=112&roleId=0
	@GET
	@Path("/finishWorkitem")
	@Produces(MediaType.APPLICATION_JSON)
	public Response finishWorkitem(@QueryParam("witmid") String witmId, @QueryParam("empNo") String empNo,
			@QueryParam("roleId") String roleId, @Context HttpServletRequest req, @Context HttpServletResponse resp)
					throws Exception {
		try {
			logger.info("Finish Inbox Item : Input = Witem (" + witmId + ") Emp No: (" + empNo + ") Role: (" + roleId + ")");
			FNAuthenticator.Authenticate(empNo);

			ECMWorkitemList.getInstance().finishWorkitem(DBUtil.stringToLong(witmId), DBUtil.stringToLong(empNo),
					DBUtil.stringToLong(roleId));
			return Response.ok().entity("OK").build();
		} catch (Exception e) {
			logger.logException(e);
			ResponseObject responseObject = ResponseObject.getResponseObject(e.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject).build();
		}
	}

	// http://localhost:9080/ECMService/resources/WorkflowService/archiveWorkitemBefore?bDate=31/12/2017&empNo=112&roleId=0
	@GET
	@Path("/finishWorkitemBefore")
	@Produces(MediaType.APPLICATION_JSON)
	public Response finishWorkitemBefore(@QueryParam("empNo") String empNo, @QueryParam("roleId") String roleId,
			@QueryParam("bDate") String bDate, @Context HttpServletRequest req, @Context HttpServletResponse resp)
					throws Exception {
		try {
			logger.info("Finish Inbox Item: Input = bDate (" + bDate + ") Emp No: (" + empNo + ") Role: (" + roleId + ")");
			FNAuthenticator.Authenticate(empNo);

			String strResult = ECMWorkitemList.getInstance().finishWorkitemBefore(empNo, roleId, bDate);
			return Response.ok().entity(strResult).build();
		} catch (Exception e) {
			logger.logException(e);
			ResponseObject responseObject = ResponseObject.getResponseObject(e.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject).build();
		}
	}
	
	// http://localhost:9080/ECMService/resources/WorkflowService/archiveWorkitem?witmid=1&empNo=112&roleId=0
	@GET
	@Path("/archiveWorkitem")
	@Produces(MediaType.APPLICATION_JSON)
	public Response archiveWorkitem(@QueryParam("witmid") String witmId, @QueryParam("empNo") String empNo,
			@QueryParam("roleId") String roleId, @Context HttpServletRequest req, @Context HttpServletResponse resp)
					throws Exception {
		try {
			logger.info("Archive: Input = Witem (" + witmId + ") Emp No: (" + empNo + ") Role: (" + roleId + ")");
			FNAuthenticator.Authenticate(empNo);

			ECMWorkitemList.getInstance().archiveWorkitem(DBUtil.stringToLong(witmId), DBUtil.stringToLong(empNo),
					DBUtil.stringToLong(roleId));
			return Response.ok().entity("OK").build();
		} catch (Exception e) {
			logger.logException(e);
			ResponseObject responseObject = ResponseObject.getResponseObject(e.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject).build();
		}
	}

	// http://localhost:9080/ECMService/resources/WorkflowService/archiveWorkitemBefore?bDate=31/12/2017&empNo=112&roleId=0
	@GET
	@Path("/archiveWorkitemBefore")
	@Produces(MediaType.APPLICATION_JSON)
	public Response archiveWorkitemBefore(@QueryParam("empNo") String empNo, @QueryParam("roleId") String roleId,
			@QueryParam("bDate") String bDate, @Context HttpServletRequest req, @Context HttpServletResponse resp)
					throws Exception {
		try {
			logger.info("Archive: Input = bDate (" + bDate + ") Emp No: (" + empNo + ") Role: (" + roleId + ")");
			FNAuthenticator.Authenticate(empNo);

			String strResult = ECMWorkitemList.getInstance().archiveWorkitemBefore(empNo, roleId, bDate);
			return Response.ok().entity(strResult).build();
		} catch (Exception e) {
			logger.logException(e);
			ResponseObject responseObject = ResponseObject.getResponseObject(e.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject).build();
		}
	}

	// http://localhost:9080/ECMService/resources/WorkflowService/archiveSentitem?witmid=1&empNo=112&roleId=0
	@GET
	@Path("/archiveSentitem")
	@Produces(MediaType.APPLICATION_JSON)
	public Response archiveSentitem(@QueryParam("sitmid") String sitmId, @QueryParam("empNo") String empNo,
			@QueryParam("roleId") String roleId, @Context HttpServletRequest req, @Context HttpServletResponse resp)
					throws Exception {
		try {
			logger.info("Archive Sent: Input = Sitem (" + sitmId + ") Emp No: (" + empNo + ") Role: (" + roleId + ")");

			FNAuthenticator.Authenticate(empNo);

			ECMWorkitemList.getInstance().archiveSentitem(DBUtil.stringToLong(sitmId), DBUtil.stringToLong(empNo),
					DBUtil.stringToLong(roleId));
			return Response.ok().entity("OK").build();
		} catch (Exception e) {
			logger.logException(e);
			ResponseObject responseObject = ResponseObject.getResponseObject(e.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject).build();
		}
	}

	// http://localhost:9080/ECMService/resources/WorkflowService/archiveSentitemBefore?bDate=1&empNo=112&roleId=0
	@GET
	@Path("/archiveSentitemBefore")
	@Produces(MediaType.APPLICATION_JSON)
	public Response archiveSentitemBefore(@QueryParam("empNo") String empNo, @QueryParam("roleId") String roleId,
			@QueryParam("bDate") String bDate, @Context HttpServletRequest req, @Context HttpServletResponse resp)
					throws Exception {
		try {
			logger.info("Archive Sent: Input = bDate (" + bDate + ") Emp No: (" + empNo + ") Role: (" + roleId + ")");

			FNAuthenticator.Authenticate(empNo);
			String strResult = ECMWorkitemList.getInstance().archiveSentitemBefore(empNo, roleId, bDate);
			return Response.ok().entity(strResult).build();
		} catch (Exception e) {
			logger.logException(e);
			ResponseObject responseObject = ResponseObject.getResponseObject(e.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject).build();
		}
	}

	// http://localhost:9080/ECMService/resources/WorkflowService/recallSentItem?witmid=1&empNo=112
	@GET
	@Path("/recallSentItem")
	@Produces(MediaType.APPLICATION_JSON)
	public Response recallSentItem(@QueryParam("sitmid") String sitmId, @QueryParam("empNo") String empNo,
			@QueryParam("roleId") String roleId, @Context HttpServletRequest req, @Context HttpServletResponse resp)
					throws Exception {
		try {
			logger.info("Archive Sent: Input = Sitem (" + sitmId + ") Emp No: (" + empNo + ") Role: (" + roleId + ")");

			FNAuthenticator.Authenticate(empNo);
			FNObjectStore os = FNObjectStoreList.getInstance().getApplicationObjectStore("ECM", "ECMOS");
			logger.info("Recall Sent: OS = " + os.getName() + "(" + os.getId() + ")");
			ArrayList<TDocPrincipal> docList = ECMWorkitemList.getInstance().recallSentitem(DBUtil.stringToLong(sitmId),
					DBUtil.stringToLong(empNo), DBUtil.stringToLong(roleId));
			AccessPolicyHelper.getInstance().recallWorkflowSecurity(docList, os);
			logger.info("Recall: Revert/Set Workflow Access Policy");
			AnnotationHelper.getInstance().recallAnnotationSecurity(docList, os);
			logger.info("Recall: Revert/Set Annotation Security Permissions");

			return Response.ok().entity("OK").build();
		} catch (Exception e) {
			logger.logException(e);
			ResponseObject responseObject = ResponseObject.getResponseObject(e.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject).build();
		}
	}

	// http://localhost:9080/ECMService/resources/WorkflowService/getWorkitemStats?userId=12&userType=USER&reportType=TODAY&itemType=ALL
	@GET
	@Path("/getWorkitemStats")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getWorkitemStats(@QueryParam("userId") String userId, @QueryParam("userType") String userType,
			@QueryParam("reportType") String reportType, @QueryParam("itemType") String itemType,
			@QueryParam("dType") String dType,
			@Context HttpServletRequest req, @Context HttpServletResponse resp) throws Exception {
		try {
			// FNAuthenticator.Authenticate(userId);
			logger.info("Get WI Status: Input = User (" + userId + ") Type: (" + userType + ") Report Type: ("
					+ reportType + ") Item Type: (" + itemType + ") Deadline Type: (" + dType + ")");

			TWorkitemStat ws = ECMWorkitemList.getInstance().getDashboardStatistics(DBUtil.stringToLong(userId),
					userType, reportType, itemType, dType);
			return Response.ok().entity(ws).build();
		} catch (Exception e) {
			logger.logException(e);
			ResponseObject responseObject = ResponseObject.getResponseObject(e.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject).build();
		}
	}
	
	// http://localhost:9080/ECMService/resources/WorkflowService/validateWorkitem?id=1001
	@GET
	@Path("/validateWorkitem")
	@Produces(MediaType.APPLICATION_JSON)
	public Response validateWorkitem(@QueryParam("id") String witemId,
			@Context HttpServletRequest req, @Context HttpServletResponse resp)
					throws Exception {
		try {

			String validateMsg = ECMWorkitemList.getInstance().validateWorkitem(DBUtil.stringToLong(witemId));
			return Response.ok().entity(validateMsg).build();
		} catch (Exception e) {
			logger.logException(e);
			ResponseObject responseObject = ResponseObject.getResponseObject(e
					.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject)
					.build();
		}
	}

	// http://localhost:9080/ECMService/resources/WorkflowService/getDraftItems?userid=1001&usertype=USER
	@GET
	@Path("/getDraftItems")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getDraftItems(@QueryParam("userid") String userId, @QueryParam("usertype") String userType,
			@Context HttpServletRequest req, @Context HttpServletResponse resp) throws Exception {
		try {
			ArrayList<TWorkitemAction> actions = ECMWorkitemList.getInstance()
					.getDraftItems(DBUtil.stringToLong(userId), userType);
			return Response.ok().entity(actions).build();
		} catch (Exception e) {
			logger.logException(e);
			ResponseObject responseObject = ResponseObject.getResponseObject(e.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject).build();
		}
	}

	// http://localhost:9080/ECMService/resources/WorkflowService/getRootTrackItems?witem=112
	@GET
	@Path("/getRootTrackItems")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getRootTrackItems(@QueryParam("witem") String witem, @QueryParam("sitem") String sitem,
			@Context HttpServletRequest req, @Context HttpServletResponse resp) throws Exception {
		try {

			long wItem = DBUtil.stringToLong(witem);
			long sItem = DBUtil.stringToLong(sitem);
			TWorkitemSet tracker = null;
			if (wItem > 0)
				tracker = ECMWorkitemList.getInstance().getRootItemsFromWorkItem(wItem);
			else if (sItem > 0)
				tracker = ECMWorkitemList.getInstance().getRootItemsFromSentItem(sItem);

			return Response.ok().entity(tracker).build();
		} catch (Exception e) {
			logger.logException(e);
			ResponseObject responseObject = ResponseObject.getResponseObject(e.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject).build();
		}
	}

	// http://localhost:9080/ECMService/resources/WorkflowService/getChildTrackItems?witem=112
	@GET
	@Path("/getChildTrackItems")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getChildTrackItems(@QueryParam("witem") String witem, @Context HttpServletRequest req,
			@Context HttpServletResponse resp) throws Exception {
		try {
			long wItem = DBUtil.stringToLong(witem);
			TWorkitemSet tracker = ECMWorkitemList.getInstance().getChildItemsFromWorkItem(wItem);
			return Response.ok().entity(tracker).build();
		} catch (Exception e) {
			logger.logException(e);
			ResponseObject responseObject = ResponseObject.getResponseObject(e.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject).build();
		}
	}

	// http://localhost:9080/ECMService/resources/WorkflowService/sendNotifications
	@GET
	@Path("/sendNotifications")
	@Produces(MediaType.APPLICATION_JSON)
	public Response sendNotifications(@Context HttpServletRequest req, @Context HttpServletResponse resp)
			throws Exception {
		try {
			ECMWorkitemList.getInstance().sendEmailNotifications();
			return Response.ok().entity("OK").build();
		} catch (Exception e) {
			ResponseObject responseObject = ResponseObject.getResponseObject(e.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject).build();
		}
	}

	// http://localhost:9080/ECMService/resources/WorkflowService/getLaunchObject
	@GET
	@Path("/getLaunchObject")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getLaunchObject(@QueryParam("empNo") String empNo, @Context HttpServletRequest req,
			@Context HttpServletResponse resp) throws Exception {
		try {
			TWorkflowDetails twd = new TWorkflowDetails();

			twd.priority = 0;
			twd.subject = "This is my workflow Subject";
			twd.remarks = "This is workflow remarks";
			twd.keywords = "Keyword1, Keyword2";
			twd.role = 0;
			twd.empNo = 1001;
			twd.docFrom = "Corporate Department";
			twd.docTo = "Enterprise Solutions";
			twd.docDate = DBUtil.convertStringtoDate("10/01/2016");
			twd.docRecDate = DBUtil.convertStringtoDate("14/01/2016");
			twd.refNo = "REF1234";
			twd.projNo = "8020";
			twd.contractNo = "1572";
			twd.ECMNo = "9081";

			TWorkitemAction taction = new TWorkitemAction();
			taction.workflow = twd;
			taction.actions = "Approve;Info";
			taction.deadline = DBUtil.convertStringtoDate("15/03/2016 15:38:22");
			taction.reminder = DBUtil.convertStringtoDate("12/03/2016 15:38:22");
			taction.EMPNo = 1001;
			taction.instructions = "Please perform the requested task";
			taction.roleId = 0;
			taction.wiAction = "LAUNCH";
			taction.recipients = new ArrayList<TRecipient>();
			TRecipient tr = new TRecipient();
			tr.id = 1002;
			tr.userType = "USER";
			tr.actionType = "TO";
			taction.recipients.add(tr);

			TRecipient tr2 = new TRecipient();
			tr2.id = 1;
			tr2.userType = "ROLE";
			tr2.actionType = "CC";
			taction.recipients.add(tr2);

			taction.attachments = new ArrayList<TAttachment>();
			TAttachment ta = new TAttachment();
			ta.docId = "{2B0B8939-BCCF-4914-BC62-24A1A1635078}";
			ta.docTitle = "New Document";
			ta.format = "PDF";
			taction.attachments.add(ta);

			TAttachment ta2 = new TAttachment();
			ta2.docId = "{9A5E47B3-59A8-4449-8AA0-0D3F0BF4204C}";
			ta2.docTitle = "Clipboard.txt.pdf";
			ta2.format = "PDF";
			taction.attachments.add(ta2);

			return Response.ok().entity(taction).build();
		} catch (Exception e) {
			ResponseObject responseObject = ResponseObject.getResponseObject(e.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject).build();
		}
	}

	// http://localhost:9080/ECMService/resources/WorkflowService/getQueryObject
	@GET
	@Path("/getQueryObject")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getQueryObject(@Context HttpServletRequest req, @Context HttpServletResponse resp)
			throws Exception {
		try {
			TWorkitemQuery twq = new TWorkitemQuery();

			twq.priority = 0;
			twq.subject = "This is my workflow Subject";
			twq.comments = "This is my comments";
			twq.receivedDate = "10/01/2016|20/12/2017";
			twq.deadline = "14/01/2016|20/12/2017";
			twq.instructions = "instructions";
			twq.userId = 1001;
			twq.senderName = "Heba";
			twq.status = "New";
			twq.type = "TO";

			return Response.ok().entity(twq).build();
		} catch (Exception e) {
			ResponseObject responseObject = ResponseObject.getResponseObject(e.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject).build();
		}
	}
}
