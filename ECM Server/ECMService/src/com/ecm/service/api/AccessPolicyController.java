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

import com.ecm.db.list.ECMAccessPolicyList;
import com.ecm.db.transport.TAccessPolicy;
import com.ecm.db.transport.TAcessPolicyMapping;
import com.ecm.db.transport.TDefaultAccessPolicy;
import com.ecm.db.util.DBUtil;
import com.ecm.db.util.ECMAdminLogger;
import com.ecm.db.util.ECMLogger;
import com.ecm.db.util.RequestHelper;
import com.ecm.filenet.list.FNAccessPolicyList;
import com.ecm.filenet.list.FNObjectStoreList;
import com.ecm.filenet.model.FNAccessPolicy;
import com.ecm.filenet.model.FNEntryTemplate;
import com.ecm.filenet.model.FNObjectStore;
import com.ecm.filenet.transport.TFNAccessPolicy;
import com.ecm.service.object.AccessPolicyHelper;
import com.ecm.service.object.JSONHelper;
import com.ecm.service.object.ResponseObject;

@Path("/AccessPolicyService")
@ApplicationPath("resources")
public class AccessPolicyController {
	private static ECMLogger logger = ECMLogger.getInstance(AccessPolicyController.class);
	
	// http://localhost:9080/ECMService/resources/AccessPolicyService/getAccessPolicies?empNo=1234
	@GET
	@Path("/getAccessPolicies")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAccessPolicies(@QueryParam("empNo") String empNo,
			@Context HttpServletRequest req, @Context HttpServletResponse resp)
			throws Exception {
		try {
			ArrayList<TAccessPolicy> apList = ECMAccessPolicyList.getInstance().getAccessPolicies(DBUtil.stringToLong(empNo));
			//FNObjectStore os = FNObjectStoreList.getInstance().getApplicationObjectStore("ECM", "ECMOS");
			//ArrayList<TFNAccessPolicy> apList = FNAccessPolicyList.getInstance(os).getPermAccessPolicies();
			return Response.ok().entity(apList).build();
		} catch (Exception e) {
			logger.logException(e);
			ResponseObject responseObject = ResponseObject.getResponseObject(e
					.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject)
					.build();
		}
	}
	
	// http://localhost:9080/ECMService/resources/AccessPolicyService/getAccessPoliciesByOrgId?orgid=1234
	@GET
	@Path("/getAccessPoliciesByOrgId")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAccessPoliciesByOrgId(@QueryParam("orgId") String orgId,
			@Context HttpServletRequest req, @Context HttpServletResponse resp)
			throws Exception {
		try {
			ArrayList<TAccessPolicy> apList = ECMAccessPolicyList.getInstance().getAllAccessPoliciesByOrgId(DBUtil.stringToLong(orgId));
			return Response.ok().entity(apList).build();
		} catch (Exception e) {
			logger.logException(e);
			ResponseObject responseObject = ResponseObject.getResponseObject(e
					.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject)
					.build();
		}
	}
		
	// http://localhost:9080/ECMService/resources/AccessPolicyService/getAccessPolicyMappings?orgId=1234
	@GET
	@Path("/getAccessPolicyMappings")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAccessPolicyMappings(@QueryParam("orgId") String orgId,
			@Context HttpServletRequest req, @Context HttpServletResponse resp)
			throws Exception {
		try {
			ArrayList<TAcessPolicyMapping> mapList = ECMAccessPolicyList.getInstance()
					.getMappedAccessPolicies(DBUtil.stringToLong(orgId));

			return Response.ok().entity(mapList).build();
		} catch (Exception e) {
			logger.logException(e);
			ResponseObject responseObject = ResponseObject.getResponseObject(e
					.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject)
					.build();
		}
	}
		
	// http://localhost:9080/ECMService/resources/AccessPolicyService/addAccessPolicyMapping?etVsId={}&apId=1
	@GET
	@Path("/addAccessPolicyMapping")
	@Produces(MediaType.APPLICATION_JSON)
	public Response addAccessPolicyMapping(@QueryParam("etVsId") String etVsId, String etName, @QueryParam("apId") String apId,
			@Context HttpServletRequest req, @Context HttpServletResponse resp)
			throws Exception {
		try {
			String strResult = "";
			strResult = ECMAccessPolicyList.getInstance().addAccessPolicyMapping(etVsId, DBUtil.stringToLong(apId));
			String empName = RequestHelper.getLoggedInEmpName(req, "");
			ECMAdminLogger.getInstance("AccessPolicyMapping").info("ECM AccessPolicy Mapping", empName, "AccessPolicy Mapping with Id - " + apId + " is mapped to entry template id - " + etVsId + " is added." );
			return Response.ok().entity(strResult).build();
		} catch (Exception e) {
			logger.logException(e);
			ResponseObject responseObject = ResponseObject.getResponseObject(e
					.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject)
					.build();
		}
	}
	
	// http://localhost:9080/ECMService/resources/AccessPolicyService/removeAccessPolicyMapping?mappingid=1
	@GET
	@Path("/removeAccessPolicyMapping")
	@Produces(MediaType.APPLICATION_JSON)
	public Response removeAccessPolicyMapping(@QueryParam("mappingid") String mapId,
		@Context HttpServletRequest req,
		@Context HttpServletResponse resp) throws Exception {
		try {
			ECMAccessPolicyList.getInstance().removeAccessPolicyMapping(
					DBUtil.stringToLong(mapId));
			String empName = RequestHelper.getLoggedInEmpName(req, "");
			ECMAdminLogger.getInstance("AccessPolicyPermissions").info("ECM AccessPolicy Permissions", empName, "AccessPolicy Mapping with Id - " + mapId + " is removed." );
			return Response.ok().entity("OK").build();
		} catch (Exception e) {
			logger.logException(e);
			ResponseObject responseObject = ResponseObject.getResponseObject(e
					.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject)
					.build();
		}
	}

	// http://localhost:9080/ECMService/resources/AccessPolicyService/removeAccessPolicy?apid=1
		@GET
		@Path("/removeAccessPolicy")
		@Produces(MediaType.APPLICATION_JSON)
		public Response removeAccessPolicy(@QueryParam("apid") String apId,
			@Context HttpServletRequest req,
			@Context HttpServletResponse resp) throws Exception {
			try {
				String resMsg = ECMAccessPolicyList.getInstance().removeAccessPolicy(DBUtil.stringToLong(apId));
				
				if(resMsg.equalsIgnoreCase(""))
				{
					String empName = RequestHelper.getLoggedInEmpName(req, "");
					ECMAdminLogger.getInstance("AccessPolicy").info("ECM AccessPolicy", empName, "AccessPolicy with Id - " + apId + " is removed." );
				}
				return Response.ok().entity(resMsg).build();
			} catch (Exception e) {
				logger.logException(e);
				ResponseObject responseObject = ResponseObject.getResponseObject(e
						.getMessage());
				return Response.status(Status.BAD_REQUEST).entity(responseObject)
						.build();
			}
		}
		
	// http://localhost:9080/ECMService/resources/AccessPolicyService/getPermissionAccessPolicies?empNo=1234
	@GET
	@Path("/getPermissionAccessPolicies")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getPermissionAccessPolicies(@Context HttpServletRequest req, 
			@Context HttpServletResponse resp)
			throws Exception {
		try {
			FNObjectStore os = FNObjectStoreList.getInstance().getApplicationObjectStore("ECM", "ECMOS");
			ArrayList<TFNAccessPolicy> apList = FNAccessPolicyList.getInstance(os).getPermAccessPolicies();
			return Response.ok().entity(apList).build();
		} catch (Exception e) {
			logger.logException(e);
			ResponseObject responseObject = ResponseObject.getResponseObject(e
					.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject)
					.build();
		}
	}
		
	// http://localhost:9080/ECMService/resources/AccessPolicyService/getAllAccessPolicies
	@GET
	@Path("/getAllAccessPolicies")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAllAccessPolicies(@Context HttpServletRequest req, 
			@Context HttpServletResponse resp) throws Exception {
		try {
			ArrayList<TAccessPolicy> apList = ECMAccessPolicyList.getInstance().getAllAccessPolicies();
			return Response.ok().entity(apList).build();
		} catch (Exception e) {
			logger.logException(e);
			ResponseObject responseObject = ResponseObject.getResponseObject(e.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject).build();
		}
	}
		
	//http://localhost:9080/ECMService/resources/AccessPolicyService/addAccessPolicy
	@POST
    @Path("/addAccessPolicy")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response addAccessPolicy(String jsonString, @Context HttpServletRequest req,@Context HttpServletResponse resp) 
			throws Exception
	{
	    try {
	    	TAccessPolicy ta = JSONHelper.getInstance().getAccessPolicy(jsonString);
	    	FNObjectStore os = FNObjectStoreList.getInstance().getApplicationObjectStore("ECM", "ECMOS");
	    	String apId = AccessPolicyHelper.getInstance().createAccessPolicy(ta, os);
	    	String empName = RequestHelper.getLoggedInEmpName(req, "");
			ECMAdminLogger.getInstance("AccessPolicy").info("ECM AccessPolicy", empName, "AccessPolicy with Id - " 
					+ ta.name + "(" + ta.id + ") for orgunit - " + ta.orgCode + " is added." );
	        return Response.ok().entity(apId).build();
		}
	    catch (Exception e) {
	    	logger.logException(e);
	         e.printStackTrace();
	         throw new Exception(e.getMessage());
	    }
	}
	
	// http://localhost:9080/ECMService/resources/AccessPolicyService/getAccessPolicyPermissions?objId={1234-23 .. }
	@GET
	@Path("/getAccessPolicyPermissions")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAccessPolicyPermissions(@QueryParam("objId") String objId, @QueryParam("objType") String objType,
			@Context HttpServletRequest req, @Context HttpServletResponse resp) throws Exception {
		try {
			FNObjectStore os = FNObjectStoreList.getInstance().getApplicationObjectStore("ECM", "ECMOS");
			FNAccessPolicy fna = FNAccessPolicy.getInstance(os, objType);
			fna.setId(objId);
			return Response.ok().entity(fna.getPermissions()).build();
		} catch (Exception e) {
			logger.logException(e);
			ResponseObject responseObject = ResponseObject.getResponseObject(e.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject).build();
		}
	}
	
	//http://localhost:9080/ECMService/resources/AccessPolicyService/setAccessPolicyPermissions
	@POST
    @Path("/setAccessPolicyPermissions")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response setAccessPolicyPermissions(String jsonString, @Context HttpServletRequest req,@Context HttpServletResponse resp) 
			throws Exception
	{
	    try {
	    	TAccessPolicy ta = JSONHelper.getInstance().getAccessPolicy(jsonString);
	    	FNObjectStore os = FNObjectStoreList.getInstance().getApplicationObjectStore("ECM", "ECMOS");
	    	AccessPolicyHelper.getInstance().setPermissions(ta, os);
	    	String empName = RequestHelper.getLoggedInEmpName(req, "");
			ECMAdminLogger.getInstance("AccessPolicyPermissions").info("ECM AccessPolicy Permissions", empName, "AccessPolicy Permissions for Id - " 
					+ ta.name + "(" + ta.id + ") for orgunit - " + ta.orgCode + " is updated." );
	        return Response.ok().entity("OK").build();
		}
	    catch (Exception e) {
	    	logger.logException(e);
	         e.printStackTrace();
	         throw new Exception(e.getMessage());
	    }
	}
	
	// http://localhost:9080/ECMService/resources/AccessPolicyService/getDefaultAccessPolicies?empNo=1234
	@GET
	@Path("/getDefaultAccessPolicies")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getDefaultAccessPolicies(@QueryParam("empNo") String empNo, 
			@QueryParam("etid") String etId, 
			@Context HttpServletRequest req, 
			@Context HttpServletResponse resp) throws Exception {
		try {
			ArrayList<TDefaultAccessPolicy> apList = ECMAccessPolicyList.getInstance().getDefaultAccessPolicies(
																DBUtil.stringToLong(empNo), etId);
			return Response.ok().entity(apList).build();
		} catch (Exception e) {
			logger.logException(e);
			ResponseObject responseObject = ResponseObject.getResponseObject(e.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject).build();
		}
	}
}
