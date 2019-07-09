package com.ecm.service.api;

import java.util.ArrayList;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.ecm.db.list.ECMUserList;
import com.ecm.db.list.ECMWorkflowList;
import com.ecm.db.list.ECMWorkitemList;
import com.ecm.db.transport.TAccessPolicy;
import com.ecm.db.transport.TDocMoveList;
import com.ecm.db.transport.TDocPermissions;
import com.ecm.db.transport.TDocWorkflowDetails;
import com.ecm.db.transport.TFNContinueQuery;
import com.ecm.db.transport.TSecurity;
import com.ecm.db.transport.TWorkitemDetails;
import com.ecm.db.transport.TWorkitemQuery;
import com.ecm.db.util.DBUtil;
import com.ecm.db.util.ECMLogger;
import com.ecm.db.util.RequestHelper;
import com.ecm.filenet.list.FNDocumentSearch;
import com.ecm.filenet.list.FNFolderList;
import com.ecm.filenet.list.FNObjectStoreList;
import com.ecm.filenet.model.FNAccessPolicy;
import com.ecm.filenet.model.FNAnnotation;
import com.ecm.filenet.model.FNDocument;
import com.ecm.filenet.model.FNFolder;
import com.ecm.filenet.model.FNObjectStore;
import com.ecm.filenet.transport.TFNDocument;
import com.ecm.filenet.transport.TFNDocumentSearch;
import com.ecm.filenet.transport.TFNDocumentSet;
import com.ecm.filenet.transport.TFNEvent;
import com.ecm.filenet.transport.TFNLink;
import com.ecm.filenet.transport.TFNProperty;
import com.ecm.filenet.transport.TFNQuery;
import com.ecm.filenet.util.Utils;
import com.ecm.service.object.AccessPolicyHelper;
import com.ecm.service.object.JSONHelper;
import com.ecm.service.object.MigrationHelper;
import com.ecm.service.object.ReportHelper;
import com.ecm.service.object.ResponseObject;
import com.ecm.service.object.UploadHelper;

@Path("/DocumentService")
@ApplicationPath("resources")
public class DocumentController extends Application {
	private static ECMLogger logger = ECMLogger.getInstance(DocumentController.class);
	// http://localhost:9080/ECMService/resources/DocumentService/addDocument
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

	// http://localhost:9080/ECMService/resources/DocumentService/downloadDocument
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
	
	// http://localhost:9080/ECMService/resources/DocumentService/validateDocument
    @GET
    @Path("/validateDocument")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response validateDocument(@QueryParam("id") String docId, @Context HttpServletRequest req,
                @Context HttpServletResponse resp) throws Exception {
          try {
                FNObjectStore os = FNObjectStoreList.getInstance().getApplicationObjectStore("ECM", "ECMOS");
                FNDocument fnd = FNDocument.getInstance(os);
                fnd.setId(docId);
                fnd.downloadCurrentVersion(); 
                UploadHelper.getInstance().setValidateDocument(resp, fnd.getName(), 
                                                                fnd.getMimeType(), fnd.getInputStream());
                return Response.ok().entity("OK").build();
          } catch (Exception e) {
                logger.logException(e);
                ResponseObject responseObject = ResponseObject.getResponseObject(e.getMessage());
                return Response.status(Status.BAD_REQUEST).entity(responseObject).build();
          }
    }


	// http://localhost:9080/ECMService/resources/DocumentService/downloadThisDocument
	@GET
	@Path("/downloadThisDocument")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	public Response downloadThisDocument(@QueryParam("id") String docId, @Context HttpServletRequest req,
			@Context HttpServletResponse resp) throws Exception {
		try {
			FNObjectStore os = FNObjectStoreList.getInstance().getApplicationObjectStore("ECM", "ECMOS");
			FNDocument fnd = FNDocument.getInstance(os);
			fnd.setId(docId);
			fnd.download(); 
			UploadHelper.getInstance().setDownloadDocument(resp, fnd.getName(), 
											fnd.getMimeType(), fnd.getInputStream());
			return Response.ok().entity("OK").build();
		} catch (Exception e) {
			logger.logException(e);
			ResponseObject responseObject = ResponseObject.getResponseObject(e.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject).build();
		}
	}
	
	// http://localhost:9080/ECMService/resources/DocumentService/downloadMultipleDocuments
	@POST
	@Path("/downloadMultipleDocuments")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	public Response downloadMultipleDocuments(String jsonString, @Context HttpServletRequest req,
			@Context HttpServletResponse resp) throws Exception {
		try {
			FNObjectStore os = FNObjectStoreList.getInstance().getApplicationObjectStore("ECM", "ECMOS");
			ArrayList<String> docList = JSONHelper.getInstance().getDocumentList(jsonString);
			FNDocument fnd = FNDocument.getInstance(os);
			String fileName = fnd.downloadMultipleDocuments(docList);
			UploadHelper.getInstance().setDownloadDocument(resp, fnd.getName(), 
											fnd.getMimeType(), fnd.getInputStream());
			Utils.safeDelete(fileName);
			return Response.ok().entity("OK").build();
		} catch (Exception e) {
			logger.logException(e);
			ResponseObject responseObject = ResponseObject.getResponseObject(e.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject).build();
		}
	}
		
	// http://localhost:9080/ECMService/resources/DocumentService/linkDocuments?firstid={}&secondif={}
	@GET
	@Path("/linkDocuments")
	@Produces(MediaType.APPLICATION_JSON)
	public Response linkDocuments(@QueryParam("firstid") String firstId, 
			@QueryParam("secondid") String secondId,
			@Context HttpServletRequest req,
			@Context HttpServletResponse resp) throws Exception {
		try {
			FNObjectStore os = FNObjectStoreList.getInstance().getApplicationObjectStore("ECM", "ECMOS");
			FNDocument fnd = FNDocument.getInstance(os);
			fnd.setId(firstId);
			fnd.addLink(secondId);
			return Response.ok().entity("OK").build();
		} catch (Exception e) {
			logger.logException(e);
			ResponseObject responseObject = ResponseObject.getResponseObject(e.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject).build();
		}
	}
	
	// http://localhost:9080/ECMService/resources/DocumentService/removeLink?firstid={}&secondif={}
	@GET
	@Path("/removeLink")
	@Produces(MediaType.APPLICATION_JSON)
	public Response removeLink(@QueryParam("firstid") String firstId, 
			@QueryParam("secondid") String secondId,
			@Context HttpServletRequest req,
			@Context HttpServletResponse resp) throws Exception {
		try {
			FNObjectStore os = FNObjectStoreList.getInstance().getApplicationObjectStore("ECM", "ECMOS");
			FNDocument fnd = FNDocument.getInstance(os);
			fnd.setId(firstId);
			fnd.removeLink(secondId);
			return Response.ok().entity("OK").build();
		} catch (Exception e) {
			logger.logException(e);
			ResponseObject responseObject = ResponseObject.getResponseObject(e.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject).build();
		}
	}
		
	// http://localhost:9080/ECMService/resources/DocumentService/checkIn
	@POST
	@Path("/checkIn")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_JSON)
	public Response checkIn(@Context HttpServletRequest req, @Context HttpServletResponse resp)
			throws Exception {
		try {
			FNObjectStore os = FNObjectStoreList.getInstance().getApplicationObjectStore("ECM", "ECMOS");
			String docId = UploadHelper.getInstance().checkinDocument(req, os);

			return Response.ok().entity(docId).build();
		} catch (Exception e) {
			logger.logException(e);
			ResponseObject responseObject = ResponseObject.getResponseObject(e.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject).build();
		}
	}

	// http://localhost:9080/ECMService/resources/DocumentService/checkOut
	@GET
	@Path("/checkOut")
	@Produces(MediaType.APPLICATION_JSON)
	public Response checkOut(@QueryParam("id") String docId, @Context HttpServletRequest req,
			@Context HttpServletResponse resp) throws Exception {
		try {
			FNObjectStore os = FNObjectStoreList.getInstance().getApplicationObjectStore("ECM", "ECMOS");
			FNDocument fnd = FNDocument.getInstance(os);
			fnd.setId(docId);
			fnd.checkOut();
			return Response.ok().entity(fnd.getId()).build();
		} catch (Exception e) {
			logger.logException(e);
			ResponseObject responseObject = ResponseObject.getResponseObject(e.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject).build();
		}
	}

	// http://localhost:9080/ECMService/resources/DocumentService/cancelCheckOut
	@GET
	@Path("/cancelCheckOut")
	@Produces(MediaType.APPLICATION_JSON)
	public Response cancelCheckOut(@QueryParam("id") String docId, @Context HttpServletRequest req,
			@Context HttpServletResponse resp) throws Exception {
		try {
			FNObjectStore os = FNObjectStoreList.getInstance().getApplicationObjectStore("ECM", "ECMOS");
			FNDocument fnd = FNDocument.getInstance(os);
			fnd.setId(docId);
			fnd.cancelCheckOut();
			return Response.ok().entity(fnd.getId()).build();
		} catch (Exception e) {
			logger.logException(e);
			ResponseObject responseObject = ResponseObject.getResponseObject(e.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject).build();
		}
	}
	
	// http://mvcsecmdevicn:9080/ECMService/resources/DocumentService/getDocument?id={20B8715F-0000-C418-B758-B3EF150F8628}
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
	
	// http://mvcsecmtesticn:9080/ECMService/resources/DocumentService/getDocumentInfo?id={20B8715F-0000-C418-B758-B3EF150F8628}
	@GET
	@Path("/getDocumentInfo")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getDocumentInfo(@QueryParam("id") String id, @QueryParam("empno") String empNo,
			@Context HttpServletRequest req, @Context HttpServletResponse resp)
			throws Exception {
		try {
			FNObjectStore os = FNObjectStoreList.getInstance().getApplicationObjectStore("ECM", "ECMOS");
			FNDocument doc = FNDocument.getInstance(os);
			doc.setId(id);
			doc.setEmpNo(RequestHelper.getLoggedInEmployee(req, empNo));
			return Response.ok().entity(doc.getDocumentTransportObject()).build();
		} catch (Exception e) {
			logger.logException(e);
			ResponseObject responseObject = ResponseObject.getResponseObject(e.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject)
					.build();
		}
	}
	
	// http://localhost:9080/ECMService/resources/DocumentService/getThisDocument?id={2B0B8939-BCCF-4914-BC62-24A1A1635078}
	@GET
	@Path("/getThisDocument")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getThisDocument(@QueryParam("id") String id,
			@Context HttpServletRequest req, @Context HttpServletResponse resp)
			throws Exception {
		try {
			FNObjectStore os = FNObjectStoreList.getInstance().getApplicationObjectStore("ECM", "ECMOS");
			FNDocument doc = FNDocument.getInstance(os);
			doc.setId(id);
			doc.setEmpNo(RequestHelper.getLoggedInEmployee(req, ""));
			return Response.ok().entity(doc.getTransport()).build();
		} catch (Exception e) {
			logger.logException(e);
			ResponseObject responseObject = ResponseObject.getResponseObject(e.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject)
					.build();
		}
	}
	
	// http://localhost:9080/ECMService/resources/DocumentService/getDocumentVersions?id={AFEB5ADF-773F-432F-83D5-76EBE9D4CDDE}
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
	
	// http://localhost:9080/ECMService/resources/DocumentService/getDocumentPermissions?id={D0F07565-0000-C213-A697-5AF5050B98A6}
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
	
	// http://localhost:9080/ECMService/resources/DocumentService/getDocumentAdhocPermissions?id={30F8AD61-0000-C56F-B1AF-A002022FD6A0}
	@GET
	@Path("/getDocumentAdhocPermissions")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getDocumentAdhocPermissions(@QueryParam("id") String id,
			@Context HttpServletRequest req, @Context HttpServletResponse resp)
			throws Exception {
		try {
			String apId = "";
			FNObjectStore os = FNObjectStoreList.getInstance().getApplicationObjectStore("ECM", "ECMOS");
			FNDocument fnd = FNDocument.getInstance(os);
			
			fnd.setId(id);
			apId = AccessPolicyHelper.getInstance().getAdhocAccessPolicy(id, os);
			
			FNAccessPolicy fna = FNAccessPolicy.getInstance(os, "");
			fna.setId(apId);
			return Response.ok().entity(fna.getPermissions()).build();
			
		} catch (Exception e) {
			logger.logException(e);
			ResponseObject responseObject = ResponseObject.getResponseObject(e.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject)
					.build();
		}
	}
	
	// http://localhost:9080/ECMService/resources/DocumentService/getAnnotationPermissions?id={30F8AD61-0000-C56F-B1AF-A002022FD6A0}
	@GET
	@Path("/getAnnotationPermissions")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAnnotationPermissions(@QueryParam("id") String id,
			@Context HttpServletRequest req, @Context HttpServletResponse resp)
			throws Exception {
		try {
			FNObjectStore os = FNObjectStoreList.getInstance().getApplicationObjectStore("ECM", "ECMOS");
			FNAnnotation annot = FNAnnotation.getInstance(os);
			annot.setAnnotId(id);
			
			return Response.ok().entity(annot.getPermissions()).build();
		} catch (Exception e) {
			logger.logException(e);
			ResponseObject responseObject = ResponseObject.getResponseObject(e.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject)
					.build();
		}
	}
		
	// http://localhost:9080/ECMService/resources/DocumentService/getDocumentFolders?id={AFEB5ADF-773F-432F-83D5-76EBE9D4CDDE}
	@GET
	@Path("/getDocumentFolders")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getDocumentFolders(@QueryParam("id") String id,
			@Context HttpServletRequest req, @Context HttpServletResponse resp)
			throws Exception {
		try {
			FNObjectStore os = FNObjectStoreList.getInstance().getApplicationObjectStore("ECM", "ECMOS");
			FNDocument doc = FNDocument.getInstance(os);
			doc.setId(id);
			
			return Response.ok().entity(doc.getFoldersFiledInTransport()).build();
		} catch (Exception e) {
			logger.logException(e);
			ResponseObject responseObject = ResponseObject.getResponseObject(e.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject)
					.build();
		}
	}
	
	//http://localhost:9080/ECMService/resources/DocumentService/updateProperties
	@POST
    @Path("/updateProperties")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response updateProperties(String jsonString, @Context HttpServletRequest req,@Context HttpServletResponse resp) 
			throws Exception
	{
	    try {
	    	logger.info("Update props: Input = " + jsonString);
	    	TFNDocument doc = JSONHelper.getInstance().getDocument(jsonString);
	    	FNObjectStore os = FNObjectStoreList.getInstance().getApplicationObjectStore("ECM", "ECMOS");
	    	FNDocument fnd = FNDocument.getInstance(os);
	    	fnd.loadFromTransport(doc);
	    	fnd.updateProperties();
	    	return Response.ok().entity("OK").build();
		}
	    catch (Exception e) {
	    	logger.logException(e);
	         e.printStackTrace();
	         throw new Exception(e.getMessage());
	    }
	}
	
	// http://localhost:9080/ECMService/resources/DocumentService/setAccessPolicy
	@GET
	@Path("/setAccessPolicy")
	@Produces(MediaType.APPLICATION_JSON)
	public Response setAccessPolicy(@QueryParam("docid") String docId, @QueryParam("apid") String apId, 
			@QueryParam("apno") String apNo, @Context HttpServletRequest req,
			@Context HttpServletResponse resp) throws Exception {
		try {
			FNObjectStore os = FNObjectStoreList.getInstance().getApplicationObjectStore("ECM", "ECMOS");
			FNDocument fnd = FNDocument.getInstance(os);
			fnd.setId(docId);
			fnd.setAccessPolicy((int)DBUtil.stringToLong(apNo), apId);
			return Response.ok().entity(fnd.getId()).build();
		} catch (Exception e) {
			logger.logException(e);
			ResponseObject responseObject = ResponseObject.getResponseObject(e.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject).build();
		}
	}
	
	// http://localhost:9080/ECMService/resources/DocumentService/setSecurityAndPolicies
	@GET
	@Path("/setSecurityAndPolicies")
	@Produces(MediaType.APPLICATION_JSON)
	public Response setSecurityAndPolicies(@QueryParam("docid") String docId, @QueryParam("userid") String user_login, 
			@Context HttpServletRequest req,
			@Context HttpServletResponse resp) throws Exception {
		try {
			long empNo = RequestHelper.getLoggedInEmployee(req, user_login);
			logger.info("User Login: " + user_login);
			FNObjectStore os = FNObjectStoreList.getInstance().getApplicationObjectStore("ECM", "ECMOS");
			FNDocument fnd = FNDocument.getInstance(os);
			fnd.setId(docId);
			fnd.setEmpNo(empNo);
			String strReturnMsg = fnd.setSecurityAndTemplate();
			AccessPolicyHelper.getInstance().getWorkflowAccessPolicy(docId, os);
			return Response.ok().entity(fnd.getId()+ "_" + strReturnMsg).build();
		} catch (Exception e) {
			logger.logException(e);
			ResponseObject responseObject = ResponseObject.getResponseObject(e.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject).build();
		}
	}
	
	// http://localhost:9080/ECMService/resources/DocumentService/setSecurityAndPoliciesForDC
	@GET
	@Path("/setSecurityAndPoliciesForDC")
	@Produces(MediaType.APPLICATION_JSON)
	public Response setSecurityAndPoliciesForDC(@QueryParam("docid") String docId, @QueryParam("userid") String user_login, 
			@Context HttpServletRequest req,
			@Context HttpServletResponse resp) throws Exception {
		try {
			
			long empNo = ECMUserList.getInstance().getEmployee(user_login);
			logger.info("User Login: " + user_login);
			FNObjectStore os = FNObjectStoreList.getInstance().getApplicationObjectStore("ECM", "ECMOS");
			FNDocument fnd = FNDocument.getInstance(os);
			fnd.setId(docId);
			fnd.setEmpNo(empNo);
			String strReturnMsg = fnd.setSecurityAndTemplate();
			AccessPolicyHelper.getInstance().getWorkflowAccessPolicy(docId, os);
			return Response.ok().entity(fnd.getId()+ "_" + strReturnMsg).build();
		} catch (Exception e) {
			logger.logException(e);
			ResponseObject responseObject = ResponseObject.getResponseObject(e.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject).build();
		}
	}
	
	// http://localhost:9080/ECMService/resources/DocumentService/setSecurityAndPoliciesForDCP
	@POST
	@Path("/setSecurityAndPoliciesForDCP")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response setSecurityAndPoliciesForDCP(String jsonString, @Context HttpServletRequest req,
			@Context HttpServletResponse resp) throws Exception {
		try {
			
			TSecurity doc = JSONHelper.getInstance().getSecurity(jsonString);
			String user_login = doc.username;
			String docId = doc.docid;
			long empNo = ECMUserList.getInstance().getEmployee(user_login);
			logger.info("User Login: " + user_login);
			FNObjectStore os = FNObjectStoreList.getInstance().getApplicationObjectStore("ECM", "ECMOS");
			FNDocument fnd = FNDocument.getInstance(os);
			fnd.setId(docId);
			fnd.setEmpNo(empNo);
			String strReturnMsg = fnd.setSecurityAndTemplate();
			AccessPolicyHelper.getInstance().getWorkflowAccessPolicy(docId, os);
			return Response.ok().entity(fnd.getId()+ "_" + strReturnMsg).build();
		} catch (Exception e) {
			logger.logException(e);
			ResponseObject responseObject = ResponseObject.getResponseObject(e.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject).build();
		}
	}
	
	//http://localhost:9080/ECMService/resources/DocumentService/setDocumentAdhocPermissions
	@POST
    @Path("/setDocumentAdhocPermissions")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response setDocumentAdhocPermissions(String jsonString, @QueryParam("userid") String user_login,
			@Context HttpServletRequest req,@Context HttpServletResponse resp) 
			throws Exception
	{
	    try {
	    	String retMsg = "Failed";
	    	long empNo = RequestHelper.getLoggedInEmployee(req, user_login);
			logger.info("User Login: " + user_login);
			FNObjectStore os = FNObjectStoreList.getInstance().getApplicationObjectStore("ECM", "ECMOS");
	    	
			TDocPermissions tdp = JSONHelper.getInstance().getDocPermissions(jsonString);
			
			FNDocument fnd = FNDocument.getInstance(os);
			if(tdp.id != null && tdp.id.length() > 0)
			{
				fnd.setId(tdp.id);
				fnd.setEmpNo(empNo);
				String apId = AccessPolicyHelper.getInstance().getAdhocAccessPolicy(tdp.id, os);
				fnd.setAdhocAccessPolicyValue(apId);
				AccessPolicyHelper.getInstance().setPermissions(apId, tdp.permissions, os);
		    	retMsg = "OK";
			}
	        return Response.ok().entity(retMsg).build();
		}
	    catch (Exception e) {
	    	logger.logException(e);
	    	ResponseObject responseObject = ResponseObject.getResponseObject(e.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject).build();
	    }
	}
	
	//http://localhost:9080/ECMService/resources/DocumentService/setMigDocPermissions
	@POST
	@Path("/setMigDocPermissions")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response setMigDocPermissions(String jsonString, @QueryParam("userid") String user_login,
			@Context HttpServletRequest req,@Context HttpServletResponse resp) 
			throws Exception
	{
	    try {
	    	String retMsg = "Failed";
	    	long empNo = RequestHelper.getLoggedInEmployee(req, user_login);
			logger.info("User Login: " + user_login);
			FNObjectStore os = FNObjectStoreList.getInstance().getApplicationObjectStore("ECM", "ECMOS");
			TDocPermissions tdp = JSONHelper.getInstance().getDocPermissions(jsonString);
			
			if(tdp.id != null && tdp.id.length() > 0)
				retMsg = MigrationHelper.getInstance().setMigCategoryBasedPermissions(tdp, empNo, tdp.category, os);
			else
				retMsg = "Invalid Id";
	
	        return Response.ok().entity(retMsg).build();
		}
	    catch (Exception e) {
	    	logger.logException(e);
	    	ResponseObject responseObject = ResponseObject.getResponseObject(e.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject).build();
	    }
	}
	
		
	//http://localhost:9080/ECMService/resources/DocumentService/search
	@POST
    @Path("/search")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response search(String jsonString, @Context HttpServletRequest req,@Context HttpServletResponse resp) 
			throws Exception
	{
	    try {
	    	logger.info("Doc Search: Input = " + jsonString);
	    	TFNQuery tq = JSONHelper.getInstance().getQuery(jsonString);
	    	FNObjectStore os = FNObjectStoreList.getInstance().getApplicationObjectStore("ECM", "ECMOS");
	    	ArrayList<TFNDocument> results = FNDocumentSearch.getInstance(os).executeSearch(tq);
	        return Response.ok().entity(results).build();
		}
	    catch (Exception e) {
	    	logger.logException(e);
	         e.printStackTrace();
	         throw new Exception(e.getMessage());
	    }
	}
	
	//http://localhost:9080/ECMService/resources/DocumentService/pagingSearch
	@POST
    @Path("/pagingSearch")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response pagingSearch(String jsonString, @Context HttpServletRequest req,@Context HttpServletResponse resp) 
			throws Exception
	{
	    try {
	    	logger.info("Doc Search: Input = " + jsonString);
	    	TFNQuery tq = JSONHelper.getInstance().getQuery(jsonString);
	    	FNObjectStore os = FNObjectStoreList.getInstance().getApplicationObjectStore("ECM", "ECMOS");
	    	TFNDocumentSearch results = FNDocumentSearch.getInstance(os).executeSearchPaging(tq);
	        return Response.ok().entity(results).build();
		}
	    catch (Exception e) {
	    	logger.logException(e);
	         e.printStackTrace();
	         throw new Exception(e.getMessage());
	    }
	}
	
	//http://localhost:9080/ECMService/resources/DocumentService/continueSearch
	@POST
    @Path("/continueSearch")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response continueSearch(String jsonString, @Context HttpServletRequest req,@Context HttpServletResponse resp) 
			throws Exception
	{
	    try {
	    	logger.info("Doc Search: Input = " + jsonString);
	    	TFNContinueQuery tq = JSONHelper.getInstance().getTFNContinueQuery(jsonString);
	    	FNObjectStore os = FNObjectStoreList.getInstance().getApplicationObjectStore("ECM", "ECMOS");
	    	TFNDocumentSearch results = FNDocumentSearch.getInstance(os).executeContinueSearch(tq);
	        return Response.ok().entity(results).build();
		}
	    catch (Exception e) {
	    	logger.logException(e);
	         e.printStackTrace();
	         throw new Exception(e.getMessage());
	    }
	}
	
	//http://localhost:9080/ECMService/resources/DocumentService/exportSearchToExcel
	@POST
    @Path("/exportSearchToExcel")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response exportSearchToExcel(String jsonString, @Context HttpServletRequest req,@Context HttpServletResponse resp) 
			throws Exception
	{
	    try {
	    	logger.info("Doc Search Excel Export: Input = " + jsonString);
	    	TFNQuery tq = JSONHelper.getInstance().getQuery(jsonString);
	    	FNObjectStore os = FNObjectStoreList.getInstance().getApplicationObjectStore("ECM", "ECMOS");
	    	ArrayList<TFNDocument> results = FNDocumentSearch.getInstance(os).executeSearch(tq);
	        ReportHelper.getInstance().exportDocumentsToExcel(req, resp, results, "Search Results");
	    	return Response.ok().entity("OK").build();
		}
	    catch (Exception e) {
	    	logger.logException(e);
	         e.printStackTrace();
	         throw new Exception(e.getMessage());
	    }
	}
	
	// http://localhost:9080/ECMService/resources/DocumentService/exportFolderDocuments
    @POST
    @Path("/exportFolderDocuments")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response exportFolderDocuments(String jsonString,
                @Context HttpServletRequest req, @Context HttpServletResponse resp)
                throws Exception {
          try {
                logger.info("Doc Search Excel Export: Input = " + jsonString);
                TFNDocumentSet tdset = JSONHelper.getInstance().getDocumentsSet(
                            jsonString);
                ReportHelper.getInstance().exportDocumentsToExcel(req, resp,
                            tdset.documents, "Document details from folder (" + tdset.folderPath + ")");
                return Response.ok().entity("OK").build();
          } catch (Exception e) {
                logger.logException(e);
                e.printStackTrace();
                throw new Exception(e.getMessage());
          }
    }


	
	// http://localhost:9080/ECMService/resources/DocumentService/addToCart?empno=1234&id={2B0B8939-BCCF-4914-BC62-24A1A1635078}
	@GET
	@Path("/addToCart")
	@Produces(MediaType.APPLICATION_JSON)
	public Response addToCart(@QueryParam("empno") String empNo, @QueryParam("id") String id,
			@Context HttpServletRequest req, @Context HttpServletResponse resp)
			throws Exception {
		try {
			
			long empNum = RequestHelper.getLoggedInEmployee(req, empNo);
			FNObjectStore os = FNObjectStoreList.getInstance().getApplicationObjectStore("ECM", "ECMOS");
			String response = FNFolderList.getInstance(os).addDocumentToCart(empNum, id);
			
			return Response.ok().entity(response).build();
		} catch (Exception e) {
			logger.logException(e);
			ResponseObject responseObject = ResponseObject.getResponseObject(e.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject)
					.build();
		}
	}
	
	// http://localhost:9080/ECMService/resources/DocumentService/addToFavorites?empno=1234&id={2B0B8939-BCCF-4914-BC62-24A1A1635078}
	@GET
	@Path("/addToFavorites")
	@Produces(MediaType.APPLICATION_JSON)
	public Response addToFavorites(@QueryParam("empno") String empNo, @QueryParam("id") String id,
			@Context HttpServletRequest req, @Context HttpServletResponse resp)
			throws Exception {
		try {
			long empNum = RequestHelper.getLoggedInEmployee(req, empNo);
			FNObjectStore os = FNObjectStoreList.getInstance().getApplicationObjectStore("ECM", "ECMOS");
			String response = FNFolderList.getInstance(os).addDocumentToFavorites(empNum, id);
			
			return Response.ok().entity(response).build();
		} catch (Exception e) {
			logger.logException(e);
			ResponseObject responseObject = ResponseObject.getResponseObject(e.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject)
					.build();
		}
	}
		
	// http://localhost:9080/ECMService/resources/DocumentService/removeFromCart?empno=1234&id={2B0B8939-BCCF-4914-BC62-24A1A1635078}
	@GET
	@Path("/removeFromCart")
	@Produces(MediaType.APPLICATION_JSON)
	public Response removeFromCart(@QueryParam("empno") String empNo, @QueryParam("id") String id,
			@Context HttpServletRequest req, @Context HttpServletResponse resp)
			throws Exception {
		try {
			
			long empNum = RequestHelper.getLoggedInEmployee(req, empNo);
			FNObjectStore os = FNObjectStoreList.getInstance().getApplicationObjectStore("ECM", "ECMOS");
			String response = FNFolderList.getInstance(os).removeDocumentFromCart(empNum, id);
			
			return Response.ok().entity(response).build();
		} catch (Exception e) {
			logger.logException(e);
			ResponseObject responseObject = ResponseObject.getResponseObject(e.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject)
					.build();
		}
	}
	
	// http://localhost:9080/ECMService/resources/DocumentService/removeFromFavorites?empno=1234&id={2B0B8939-BCCF-4914-BC62-24A1A1635078}
	@GET
	@Path("/removeFromFavorites")
	@Produces(MediaType.APPLICATION_JSON)
	public Response removeFromFavorites(@QueryParam("empno") String empNo, @QueryParam("id") String id,
			@Context HttpServletRequest req, @Context HttpServletResponse resp)
			throws Exception {
		try {
			long empNum = RequestHelper.getLoggedInEmployee(req, empNo);
			FNObjectStore os = FNObjectStoreList.getInstance().getApplicationObjectStore("ECM", "ECMOS");
			String response = FNFolderList.getInstance(os).removeDocumentFromFavorites(empNum, id);
			
			return Response.ok().entity(response).build();
		} catch (Exception e) {
			logger.logException(e);
			ResponseObject responseObject = ResponseObject.getResponseObject(e.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject)
					.build();
		}
	}
		
	// http://localhost:9080/ECMService/resources/DocumentService/getCart?empno=1234
	@GET
	@Path("/getCart")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getCart(@QueryParam("empno") String empNo,
			@Context HttpServletRequest req, @Context HttpServletResponse resp)
			throws Exception {
		try {
			
			long empNum = RequestHelper.getLoggedInEmployee(req, empNo);
			FNObjectStore os = FNObjectStoreList.getInstance().getApplicationObjectStore("ECM", "ECMOS");
			ArrayList<TFNDocument> cart = FNFolderList.getInstance(os).getDocumentsInCart(empNum);
			
			return Response.ok().entity(cart).build();
		} catch (Exception e) {
			logger.logException(e);
			ResponseObject responseObject = ResponseObject.getResponseObject(e.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject)
					.build();
		}
	}
	
	// http://localhost:9080/ECMService/resources/DocumentService/getFavorites?empno=1234
	@GET
	@Path("/getFavorites")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getFavorites(@QueryParam("empno") String empNo,
			@Context HttpServletRequest req, @Context HttpServletResponse resp)
			throws Exception {
		try {
			long empNum = RequestHelper.getLoggedInEmployee(req, empNo);
			FNObjectStore os = FNObjectStoreList.getInstance().getApplicationObjectStore("ECM", "ECMOS");
			ArrayList<TFNDocument> cart = FNFolderList.getInstance(os).getFavoriteDocuments(empNum);
			
			return Response.ok().entity(cart).build();
		} catch (Exception e) {
			logger.logException(e);
			ResponseObject responseObject = ResponseObject.getResponseObject(e.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject)
					.build();
		}
	}
	
	// http://localhost:9080/ECMService/resources/DocumentService/getRecent?empno=1234
	@GET
	@Path("/getRecent")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getRecent(@QueryParam("empno") String empNo,
			@Context HttpServletRequest req, @Context HttpServletResponse resp)
			throws Exception {
		try {
			long empNum = RequestHelper.getLoggedInEmployee(req, empNo);
			FNObjectStore os = FNObjectStoreList.getInstance().getApplicationObjectStore("ECM", "ECMOS");
			ArrayList<TFNDocument> cart = FNDocumentSearch.getInstance(os).getRecentDocuments(empNum);	
			return Response.ok().entity(cart).build();
		} catch (Exception e) {
			logger.logException(e);
			ResponseObject responseObject = ResponseObject.getResponseObject(e.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject)
					.build();
		}
	}
	
	// http://localhost:9080/ECMService/resources/DocumentService/getTeamDocuments?empno=1234
	@GET
	@Path("/getTeamDocuments")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getTeamDocuments(@QueryParam("empno") String empNo,
			@Context HttpServletRequest req, @Context HttpServletResponse resp)
			throws Exception {
		try {
			long empNum = RequestHelper.getLoggedInEmployee(req, empNo);
			FNObjectStore os = FNObjectStoreList.getInstance().getApplicationObjectStore("ECM", "ECMOS");
			ArrayList<TFNDocument> cart = FNDocumentSearch.getInstance(os).getTeamDocuments(empNum);	
			return Response.ok().entity(cart).build();
		} catch (Exception e) {
			logger.logException(e);
			ResponseObject responseObject = ResponseObject.getResponseObject(e.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject)
					.build();
		}
	}
	
	// http://localhost:9080/ECMService/resources/DocumentService/getDocumentHistory?docid={}
	@GET
	@Path("/getDocumentHistory")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getDocumentHistory(@QueryParam("docid") String docId,
			@Context HttpServletRequest req, @Context HttpServletResponse resp)
			throws Exception {
		try {
			FNObjectStore os = FNObjectStoreList.getInstance().getApplicationObjectStore("ECM", "ECMOS");
			ArrayList<TFNEvent> hList = FNDocumentSearch.getInstance(os).getEvents(docId);	
			return Response.ok().entity(hList).build();
		} catch (Exception e) {
			logger.logException(e);
			ResponseObject responseObject = ResponseObject.getResponseObject(e.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject)
					.build();
		}
	}
	
	// http://localhost:9080/ECMService/resources/DocumentService/getDocumentWorkflowHistory?docid={}
	@GET
	@Path("/getDocumentWorkflowHistory")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getDocumentWorkflowHistory(@QueryParam("docid") String docId,
			@Context HttpServletRequest req, @Context HttpServletResponse resp)
			throws Exception {
		try {		
			ArrayList<TDocWorkflowDetails> dwDetails = ECMWorkflowList.getInstance().getDocumentWorkflowHistory(docId);
			return Response.ok().entity(dwDetails).build();
			
		} catch (Exception e) {
			logger.logException(e);
			ResponseObject responseObject = ResponseObject.getResponseObject(e.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject)
					.build();
		}
	}
		
	// http://localhost:9080/ECMService/resources/DocumentService/getLinks?docid={}
	@GET
	@Path("/getLinks")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getLinks(@QueryParam("docid") String docId,
			@Context HttpServletRequest req, @Context HttpServletResponse resp)
			throws Exception {
		try {
			FNObjectStore os = FNObjectStoreList.getInstance().getApplicationObjectStore("ECM", "ECMOS");
			ArrayList<TFNLink> links = FNDocumentSearch.getInstance(os).getLinks(docId);	
			return Response.ok().entity(links).build();
		} catch (Exception e) {
			logger.logException(e);
			ResponseObject responseObject = ResponseObject.getResponseObject(e.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject)
					.build();
		}
	}
		
	// http://localhost:9080/ECMService/resources/DocumentService/getSearchjson
	@GET
	@Path("/getSearchjson")
	@Produces(MediaType.APPLICATION_JSON)
	public  Response getSearchjson(@Context HttpServletRequest req, @Context HttpServletResponse resp)
			throws Exception {
		try {
			TFNQuery tq = new TFNQuery();
			tq.symName = "FieldService";
			tq.contentSearch = new TFNProperty();
			tq.contentSearch.oper = "ALL";
			tq.contentSearch.mvalues = new ArrayList<String>();
			tq.contentSearch.mvalues.add("Test");
			tq.props = new ArrayList<TFNProperty>();
			TFNProperty tp = new TFNProperty();
			tp.symName = "AccountNo";
			tp.oper = "=";
			tp.mvalues = new ArrayList<String>();
			tp.mvalues.add("100021");
			tq.props.add(tp);
			return Response.ok().entity(tq).build();
		} catch (Exception e) {
			logger.logException(e);
			ResponseObject responseObject = ResponseObject.getResponseObject(e.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject)
					.build();
		}
	}
	
	// http://localhost:9080/ECMService/resources/DocumentService/fileInFolder?id={2B0B8939-BCCF-4914-BC62-24A1A1635078}
	@GET
	@Path("/fileInFolder")
	@Produces(MediaType.APPLICATION_JSON)
	public Response fileInFolder(@QueryParam("folderid") String folderId, @QueryParam("id") String id,
			@Context HttpServletRequest req, @Context HttpServletResponse resp)
			throws Exception {
		try {
			FNObjectStore os = FNObjectStoreList.getInstance().getApplicationObjectStore("ECM", "ECMOS");
			FNFolder rf = FNFolder.getInstance(os);
			rf.setId(folderId);
			FNDocument fnd = FNDocument.getInstance(os);
			fnd.setId(id);
			fnd.setFolder(rf);
			String response = fnd.fileInFolder();
			
			return Response.ok().entity(response).build();
		} catch (Exception e) {
			logger.logException(e);
			ResponseObject responseObject = ResponseObject.getResponseObject(e.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject)
					.build();
		}
	}
	
	// http://localhost:9080/ECMService/resources/DocumentService/moveToFolder?id={2B0B8939-BCCF-4914-BC62-24A1A1635078}&sourceid=<>&targetid=<>
	@GET
	@Path("/moveToFolder")
	@Produces(MediaType.APPLICATION_JSON)
	public Response moveToFolder(@QueryParam("sourceid") String sourceId, @QueryParam("targetid") String targetId, @QueryParam("id") String id,
			@Context HttpServletRequest req, @Context HttpServletResponse resp)
			throws Exception {
		try {
			FNObjectStore os = FNObjectStoreList.getInstance().getApplicationObjectStore("ECM", "ECMOS");		
			String response = FNFolderList.getInstance(os).moveToFolder(sourceId, targetId, id);
			return Response.ok().entity(response).build();
		} catch (Exception e) {
			logger.logException(e);
			ResponseObject responseObject = ResponseObject.getResponseObject(e.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject)
					.build();
		}
	}
	
	// http://localhost:9080/ECMService/resources/DocumentService/moveMultipleDocuments
	@POST
	@Path("/moveMultipleDocuments")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response moveMultipleDocuments(String jsonString, @Context HttpServletRequest req,
			@Context HttpServletResponse resp) throws Exception {
		try {
			FNObjectStore os = FNObjectStoreList.getInstance().getApplicationObjectStore("ECM", "ECMOS");		
			TDocMoveList dml = JSONHelper.getInstance().getDocMoveList(jsonString);
			String response = FNFolderList.getInstance(os).moveMultipleDocuments(dml);

			return Response.ok().entity(response).build();
		} catch (Exception e) {
			logger.logException(e);
			ResponseObject responseObject = ResponseObject.getResponseObject(e.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject).build();
		}
	}
	
	// http://localhost:9080/ECMService/resources/DocumentService/unfileFromFolder?id={2B0B8939-BCCF-4914-BC62-24A1A1635078}
	@GET
	@Path("/unfileFromFolder")
	@Produces(MediaType.APPLICATION_JSON)
	public Response unfileFromFolder(@QueryParam("folderid") String folderId, @QueryParam("id") String id,
			@Context HttpServletRequest req, @Context HttpServletResponse resp)
			throws Exception {
		try {
			FNObjectStore os = FNObjectStoreList.getInstance().getApplicationObjectStore("ECM", "ECMOS");
			FNFolder rf = FNFolder.getInstance(os);
			rf.setId(folderId);
			FNDocument fnd = FNDocument.getInstance(os);
			fnd.setId(id);
			fnd.setFolder(rf);
			String response = fnd.unfileFromFolder();
			
			return Response.ok().entity(response).build();
		} catch (Exception e) {
			logger.logException(e);
			ResponseObject responseObject = ResponseObject.getResponseObject(e.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject)
					.build();
		}
	}

	@Override
	public Set<Class<?>> getClasses() {
		// TODO Auto-generated method stub
		return null;
	}
}
