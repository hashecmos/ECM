package com.ecm.service.api;

import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.ecm.db.list.ECMDocSignList;
import com.ecm.db.transport.TDocSign;
import com.ecm.db.transport.TSignToken;
import com.ecm.db.util.DBUtil;
import com.ecm.db.util.ECMLogger;
import com.ecm.filenet.list.FNObjectStoreList;
import com.ecm.filenet.model.FNObjectStore;
import com.ecm.service.object.ResponseObject;
import com.ecm.service.object.eSignHelper;

	
@Path("/ESignService")
@ApplicationPath("resources")
public class ESignController {
	private static ECMLogger logger = ECMLogger.getInstance(ESignController.class);
	
	// http://localhost:9080/ECMService/resources/ESignService/prepareESign?empno=1004&roleid=1&
							//docid={AFEB5ADF-773F-432F-83D5-76EBE9D4CDDE}&initial=N
	@GET
	@Path("/prepareESign")
	@Produces(MediaType.APPLICATION_JSON)
	public Response prepareESign(@QueryParam("empno") String empNo, @QueryParam("roleid") String roleId,
			@QueryParam("docid") String docId, @QueryParam("initial") String isInitial,@QueryParam("url") String url,
			@QueryParam("witemid") String witemId, @Context HttpServletRequest req, @Context HttpServletResponse resp)
			throws Exception {
		try {
			FNObjectStore os = FNObjectStoreList.getInstance().getApplicationObjectStore("ECM", "ECMOS");
			Boolean isInit = false;
			if((isInitial != null) && (isInitial.trim().equalsIgnoreCase("Y")))
				isInit = true;
			
			TSignToken ts = eSignHelper.getInstance().prepareForeSign(os, empNo, 
					DBUtil.stringToLong(roleId), docId, isInit, url, DBUtil.stringToLong(witemId));
			return Response.ok().entity(ts).build();
		} catch (Exception e) {
			logger.logException(e);
			ResponseObject responseObject = ResponseObject.getResponseObject(e
					.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject)
					.build();
		}
	}
	
	// http://localhost:9080/ECMService/resources/ESignService/completeSign?id=10&exitStatus=abcd
	@GET
	@Path("/completeSign")
	//@Produces(MediaType.APPLICATION_JSON)
	public Response completeSign(@QueryParam("id") String reqId, @QueryParam("exitStatus") String eStatus,
					@Context HttpServletRequest req, @Context HttpServletResponse resp) throws Exception {
		String eSignType = "";
		try {
			FNObjectStore os = FNObjectStoreList.getInstance().getApplicationObjectStore("ECM", "ECMOS");
			long rId = 0;
			if((reqId != null) && (reqId.length() > 0))
				rId = DBUtil.stringToLong(reqId);
			eSignType = eSignHelper.getInstance().completeSign(os, rId, eStatus);
			resp.sendRedirect(eSignHelper.getInstance().getResponseURL(true, eSignType));
			return Response.ok().build();
		} catch (Exception e) {
			logger.logException(e);
			ResponseObject responseObject = ResponseObject.getResponseObject(e.getMessage());
			resp.sendRedirect(eSignHelper.getInstance().getResponseURL(false, eSignType));
			return Response.status(Status.BAD_REQUEST).entity(responseObject).build();
		}
	}
	
	// http://localhost:9080/ECMService/resources/ESignService/getRequest?id=10
	@GET
	@Path("/getRequest")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getRequest(@QueryParam("id") String reqId,
					@Context HttpServletRequest req, @Context HttpServletResponse resp) throws Exception {
		try {

			TDocSign td = ECMDocSignList.getInstance().getRequest(DBUtil.stringToLong(reqId));
			return Response.ok().entity(td).build();
		} catch (Exception e) {
			logger.logException(e);
			ResponseObject responseObject = ResponseObject.getResponseObject(e.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject).build();
		}
	}
	
	// http://localhost:9080/ECMService/resources/ESignService/getUserRequests?userid=10&usertype=USER
	@GET
	@Path("/getUserRequests")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getUserRequests(@QueryParam("userid") String userId, @QueryParam("usertype") String userType,
					@Context HttpServletRequest req, @Context HttpServletResponse resp) throws Exception {
		try {

			ArrayList<TDocSign> tdList = ECMDocSignList.getInstance().getSignRequests(
											DBUtil.stringToLong(userId), userType);
			return Response.ok().entity(tdList).build();
		} catch (Exception e) {
			logger.logException(e);
			ResponseObject responseObject = ResponseObject.getResponseObject(e.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject).build();
		}
	}
	
	// http://localhost:9080/ECMService/resources/ESignService/getRequestsByDate?from=121123123&to=121321231
	@GET
	@Path("/getRequestsByDate")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getRequestsByDate(@QueryParam("from") String fromDate, @QueryParam("to") String toDate,
					@Context HttpServletRequest req, @Context HttpServletResponse resp) throws Exception {
		try {

			ArrayList<TDocSign> tdList = ECMDocSignList.getInstance().getSignRequestsbyDate(
					DBUtil.convertStringToDateEx(fromDate), DBUtil.convertStringToDateEx(toDate));
			return Response.ok().entity(tdList).build();
		} catch (Exception e) {
			logger.logException(e);
			ResponseObject responseObject = ResponseObject.getResponseObject(e.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject).build();
		}
	}
	

	// http://localhost:9080/ECMService/resources/ESignService/getSignedDocs?from=121123123&to=121321231
	@GET
	@Path("/getSignedDocs")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getSignedDocs(@QueryParam("from") String fromDate, @QueryParam("to") String toDate,
					@Context HttpServletRequest req, @Context HttpServletResponse resp) throws Exception {
		try {

			ArrayList<TDocSign> tdList = ECMDocSignList.getInstance().getSignedDocsbyDate(
					DBUtil.convertStringToDateEx(fromDate), DBUtil.convertStringToDateEx(toDate));
			return Response.ok().entity(tdList).build();
		} catch (Exception e) {
			logger.logException(e);
			ResponseObject responseObject = ResponseObject.getResponseObject(e.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject).build();
		}
	}
	
	// http://localhost:9080/ECMService/resources/ESignService/verifyESign?
	//docid={AFEB5ADF-773F-432F-83D5-76EBE9D4CDDE}&witemid=1
	@GET
	@Path("/verifyESign")
	@Produces(MediaType.APPLICATION_JSON)
	public Response verifyESign(@QueryParam("docid") String docId,
			@QueryParam("witemid") String witemId,
			@Context HttpServletRequest req, @Context HttpServletResponse resp)
			throws Exception {
		try {
			FNObjectStore os = FNObjectStoreList.getInstance().getApplicationObjectStore("ECM", "ECMOS");

			String response = eSignHelper.getInstance().getSignRequestsByWorkItem(os, docId, DBUtil.stringToLong(witemId));
			return Response.ok().entity(response).build();
		} catch (Exception e) {
			logger.logException(e);
			ResponseObject responseObject = ResponseObject.getResponseObject(e
					.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject)
					.build();
		}
	}
}
