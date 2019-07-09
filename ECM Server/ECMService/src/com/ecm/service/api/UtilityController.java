package com.ecm.service.api;

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

import com.ecm.db.util.ECMLogger;
import com.ecm.service.object.ResponseObject;
import com.ecm.service.object.UtilityHelper;


@Path("/UtilityService")
@ApplicationPath("resources")
public class UtilityController {
	private static ECMLogger logger = ECMLogger.getInstance(NewsController.class);
	// http://mvcsecmdevicn:9080/ECMService/resources/UtilityService/addEntryTemplatesToDB
	@GET
	@Path("/addEntryTemplatesToDB")
	@Produces(MediaType.APPLICATION_JSON)
	public Response addEntryTemplatesToDB(@Context HttpServletRequest req, 
			@Context HttpServletResponse resp)
			throws Exception {
		try {
			UtilityHelper uh = new UtilityHelper();
			uh.addEntryTemplatesToDB();
			return Response.ok().entity("OK").build();
		} catch (Exception e) {
			logger.logException(e);
			ResponseObject responseObject = ResponseObject.getResponseObject(e
					.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject)
					.build();
		}
	}
	
	// http://mvcsecmdevicn:9080/ECMService/resources/UtilityService/addAccessPolicies?objType=DEFAULT ORGCODE
	@GET
	@Path("/addAccessPolicies")
	@Produces(MediaType.APPLICATION_JSON)
	public Response addAccessPolicies(@QueryParam("objType") String objType,@Context HttpServletRequest req, 
			@Context HttpServletResponse resp)
			throws Exception {
		try {
			UtilityHelper uh = new UtilityHelper();
			uh.createAccessPolicies(objType);
			return Response.ok().entity("OK").build();
		} catch (Exception e) {
			logger.logException(e);
			ResponseObject responseObject = ResponseObject.getResponseObject(e
					.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject)
					.build();
		}
	}
	
	// http://localhost:9080/ECMService/resources/UtilityService/addPermAccessPolicies
	@GET
	@Path("/addAccessPolicies")
	@Produces(MediaType.APPLICATION_JSON)
	public Response addPermAccessPolicies(@Context HttpServletRequest req, 
			@Context HttpServletResponse resp)
			throws Exception {
		try {
			UtilityHelper uh = new UtilityHelper();
			uh.createPermAccessPolicies();
			return Response.ok().entity("OK").build();
		} catch (Exception e) {
			logger.logException(e);
			ResponseObject responseObject = ResponseObject.getResponseObject(e
					.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject)
					.build();
		}
	}
}
