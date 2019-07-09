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

import com.ecm.db.list.ECMIntegrationList;
import com.ecm.db.transport.TIntegration;
import com.ecm.db.util.DBUtil;
import com.ecm.db.util.ECMAdminLogger;
import com.ecm.db.util.ECMLogger;
import com.ecm.db.util.RequestHelper;
import com.ecm.filenet.list.FNDocumentSearch;
import com.ecm.filenet.list.FNObjectStoreList;
import com.ecm.filenet.model.FNDocument;
import com.ecm.filenet.model.FNObjectStore;
import com.ecm.filenet.transport.TFNClass;
import com.ecm.filenet.transport.TFNEvent;
import com.ecm.service.object.IntegrationHelper;
import com.ecm.service.object.JSONHelper;
import com.ecm.service.object.ResponseObject;
import com.ecm.service.object.UploadHelper;

@Path("/IntegrationService")
@ApplicationPath("resources")
public class IntegrationController {
	private static ECMLogger logger = ECMLogger.getInstance(AccessPolicyController.class);
	
	// http://localhost:9080/ECMService/resources/IntegrationService/getDocuments?appid=MEDICAL&param1=123
	@GET
	@Path("/getDocuments")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getDocuments(@QueryParam("appid") String appId,
			@QueryParam("param1") String param1,
			@QueryParam("param2") String param2,
			@QueryParam("param3") String param3,
			@QueryParam("param4") String param4,
			@QueryParam("param5") String param5,
			@QueryParam("sfilter") String docName,
			@QueryParam("sdate") String addedOn,
			@QueryParam("operator") String operator,
			@QueryParam("orderby") String orderby,
			@QueryParam("ascdesc") String ascdesc,
			@Context HttpServletRequest req, @Context HttpServletResponse resp)
			throws Exception {
		try {
			FNObjectStore os = FNObjectStoreList.getInstance()
					.getApplicationObjectStore("ECM", "ECMOS");
			Object obj = IntegrationHelper.getInstance().getDocuments(os,
					appId, param1, param2, param3, param4, param5, docName,
					addedOn, operator, orderby, ascdesc);
			return Response.ok().entity(obj).build();
		} catch (Exception e) {
			logger.logException(e);
			ResponseObject responseObject = ResponseObject.getResponseObject(e
					.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject)
					.build();
		}
	}
	
	// http://localhost:9080/ECMService/resources/IntegrationService/addDocument
	@POST
	@Path("/addDocument")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_JSON)
	public Response addDocument(@Context HttpServletRequest req, @Context HttpServletResponse resp)
			throws Exception {
		try {
			FNObjectStore os = FNObjectStoreList.getInstance().getApplicationObjectStore("ECM", "ECMOS");
			String docId = UploadHelper.getInstance().addDocument(req, os);

			return Response.ok().entity(docId).build();
		} catch (Exception e) {
			logger.logException(e);
			ResponseObject responseObject = ResponseObject.getResponseObject(e.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject).build();
		}
	}
	
	// http://localhost:9080/ECMService/resources/IntegrationService/getDocument?id={20B8715F-0000-C418-B758-B3EF150F8628}
	@GET
	@Path("/getDocument")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getDocument(@QueryParam("id") String id, @QueryParam("empno") String empNo,
			@Context HttpServletRequest req, @Context HttpServletResponse resp)
			throws Exception {
		try {
			FNObjectStore os = FNObjectStoreList.getInstance().getApplicationObjectStore("ECM", "ECMOS");
			FNDocument doc = FNDocument.getInstance(os);
			doc.setId(id);
			doc.setEmpNo(RequestHelper.getLoggedInEmployee(req, empNo));
			return Response.ok().entity(doc.getCurrentVersionTransport()).build();
		} catch (Exception e) {
			logger.logException(e);
			ResponseObject responseObject = ResponseObject.getResponseObject(e.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject)
					.build();
		}
	}
	
	//http://localhost:9080/ECMService/resources/IntegrationService/downloadDocument/?id={39D29D28-31BA-4FF1-BFDE-A5FACAC8D4F7}
		@GET
		@Path("/downloadDocument")
		@Produces(MediaType.APPLICATION_OCTET_STREAM)
		public Response downloadDocument(@QueryParam("id") String docId, @Context HttpServletRequest req,
				@Context HttpServletResponse resp) throws Exception {
			try {
				FNObjectStore os = FNObjectStoreList.getInstance().getApplicationObjectStore("ECM", "ECMOS");
				FNDocument fnd = FNDocument.getInstance(os);
				fnd.setId(docId);
				fnd.downloadCurrentVersion(); 
				UploadHelper.getInstance().setDownloadDocument(resp, fnd.getName(), 
												fnd.getMimeType(), fnd.getInputStream());
				return Response.ok().entity("OK").build();
			} catch (Exception e) {
				logger.logException(e);
				ResponseObject responseObject = ResponseObject.getResponseObject(e.getMessage());
				return Response.status(Status.BAD_REQUEST).entity(responseObject).build();
			}
		}

		//http://localhost:9080/ECMService/resources/IntegrationService/getDocumentVersions?id={39D29D28-31BA-4FF1-BFDE-A5FACAC8D4F7}
	@GET
	@Path("/getDocumentVersions")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getDocumentVersions(@QueryParam("id") String id,
			@Context HttpServletRequest req, @Context HttpServletResponse resp)
			throws Exception {
		try {
			FNObjectStore os = FNObjectStoreList.getInstance().getApplicationObjectStore("ECM", "ECMOS");
			FNDocument doc = FNDocument.getInstance(os);
			doc.setId(id);
			
			return Response.ok().entity(doc.getVersionsTransport()).build();
		} catch (Exception e) {
			logger.logException(e);
			ResponseObject responseObject = ResponseObject.getResponseObject(e.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject)
					.build();
		}
	}
	//http://localhost:9080/ECMService/resources/IntegrationService/getDocumentPermissions?id={39D29D28-31BA-4FF1-BFDE-A5FACAC8D4F7}
		@GET
		@Path("/getDocumentPermissions")
		@Produces(MediaType.APPLICATION_JSON)
		public Response getDocumentPermissions(@QueryParam("id") String id,
				@Context HttpServletRequest req, @Context HttpServletResponse resp)
				throws Exception {
			try {
				FNObjectStore os = FNObjectStoreList.getInstance().getApplicationObjectStore("ECM", "ECMOS");
				FNDocument doc = FNDocument.getInstance(os);
				doc.setId(id);
				
				return Response.ok().entity(doc.getPermissions()).build();
			} catch (Exception e) {
				logger.logException(e);
				ResponseObject responseObject = ResponseObject.getResponseObject(e.getMessage());
				return Response.status(Status.BAD_REQUEST).entity(responseObject)
						.build();
			}
		}
	//http://localhost:9080/ECMService/resources/IntegrationService/getDocumentHistory?docid={39D29D28-31BA-4FF1-BFDE-A5FACAC8D4F7}
		@GET
		@Path("/getDocumentHistory")
		@Produces(MediaType.APPLICATION_JSON)
		public Response getDocumentHistory(@QueryParam("id") String Id,
				@Context HttpServletRequest req, @Context HttpServletResponse resp)
				throws Exception {
			try {
				FNObjectStore os = FNObjectStoreList.getInstance().getApplicationObjectStore("ECM", "ECMOS");
				ArrayList<TFNEvent> hList = FNDocumentSearch.getInstance(os).getEvents(Id);	
				return Response.ok().entity(hList).build();
			} catch (Exception e) {
				logger.logException(e);
				ResponseObject responseObject = ResponseObject.getResponseObject(e.getMessage());
				return Response.status(Status.BAD_REQUEST).entity(responseObject)
						.build();
			}
		}
	
	// http://localhost:9080/ECMService/resources/IntegrationService/getEntryTemplate?appid=MEDICAL
	@GET
	@Path("/getEntryTemplate")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getEntryTemplate(@QueryParam("appid") String appId, @QueryParam("empno") String empno,
			@Context HttpServletRequest req, @Context HttpServletResponse resp)
			throws Exception {
		try {
			FNObjectStore os = FNObjectStoreList.getInstance().getApplicationObjectStore("ECM", "ECMOS");
			long empNo = RequestHelper.getLoggedInEmployee(req, empno);
			TFNClass fnc = IntegrationHelper.getInstance().getEntryTemplate(os, appId, empNo);	
			return Response.ok().entity(fnc).build();
		} catch (Exception e) {
			logger.logException(e);
			ResponseObject responseObject = ResponseObject.getResponseObject(e.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject)
					.build();
		}
	}
	
	// http://localhost:9080/ECMService/resources/IntegrationService/getIntegrations
	@GET
	@Path("/getIntegrations")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getIntegrations(@Context HttpServletRequest req, @Context HttpServletResponse resp)
			throws Exception {
		try {
			ArrayList<TIntegration> iList = ECMIntegrationList.getInstance().getIntegrations();
			return Response.ok().entity(iList).build();
		} catch (Exception e) {
			logger.logException(e);
			ResponseObject responseObject = ResponseObject.getResponseObject(e.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject)
					.build();
		}
	}
	
	// http://localhost:9080/ECMService/resources/IntegrationService/saveIntegration
	@POST
	@Path("/saveIntegration")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response saveIntegration(String jsonString, @Context HttpServletRequest req, @Context HttpServletResponse resp)
			throws Exception {
		try {
			TIntegration ti = JSONHelper.getInstance().getIntegration(jsonString);
			ECMIntegrationList.getInstance().saveIntegration(ti);
			return Response.ok().entity("OK").build();
		} catch (Exception e) {
			logger.logException(e);
			ResponseObject responseObject = ResponseObject.getResponseObject(e.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject).build();
		}
	}
	
	// http://localhost:9080/ECMService/resources/IntegrationService/deleteIntegration?id=123L
	@GET
	@Path("/deleteIntegration")
	@Produces(MediaType.APPLICATION_JSON)
	public Response deleteIntegration(@QueryParam("id") String intId, 
			@Context HttpServletRequest req, @Context HttpServletResponse resp)
			throws Exception {
		try {
			ECMIntegrationList.getInstance().deleteIntegration(DBUtil.stringToLong(intId));
			String empName = RequestHelper.getLoggedInEmpName(req, "");
			ECMAdminLogger.getInstance("ECMIntegrations").info("ECM Integrations", empName, "Integrations with Id - " 
					+ intId + " is deleted." );
			return Response.ok().entity("OK").build();
		} catch (Exception e) {
			logger.logException(e);
			ResponseObject responseObject = ResponseObject.getResponseObject(e.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject)
					.build();
		}
	}
}