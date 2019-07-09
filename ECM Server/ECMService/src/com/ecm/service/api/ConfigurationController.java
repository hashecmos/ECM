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

import com.ecm.db.list.ECMConfigurationList;
import com.ecm.db.model.ECMConfiguration;
import com.ecm.db.transport.TConfiguration;
import com.ecm.db.util.ECMLogger;
import com.ecm.service.object.JSONHelper;
import com.ecm.service.object.ResponseObject;

@Path("/ConfigurationService")
// @Path("/ConfigurationService")
@ApplicationPath("resources")
public class ConfigurationController {
	private static ECMLogger logger = ECMLogger.getInstance(ConfigurationController.class);
	// http://localhost:9080/ECMService/resources/ConfigurationService/getConfiguration?appId=ECM&scope=SYSTEM
	@GET
	@Path("/getConfiguration")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getConfiguration(@QueryParam("appId") String appId,
			@QueryParam("scope") String scope,@QueryParam("key") String keyName, @Context HttpServletRequest req,
			@Context HttpServletResponse resp) throws Exception {
		try {
			System.out.println("I am in");
			ECMConfiguration cnfg = new ECMConfiguration();
			cnfg.setAppId(appId);
			cnfg.setConfigScope(scope);
			cnfg.setKeyName(keyName);

			TConfiguration tCnfg = cnfg.getTransport();

			return Response.ok().entity(tCnfg).build();
		} catch (Exception e) {
			logger.logException(e);
			ResponseObject responseObject = ResponseObject.getResponseObject(e
					.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject)
					.build();
		}
	}
	
	// http://localhost:9080/ECMService/resources/ConfigurationService/getConfigurationsForUpdate?appId=ECM&scope=SYSTEM
	@GET
	@Path("/getConfigurationsForUpdate")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getConfigurationsForUpdate(@QueryParam("appId") String appId,
			@QueryParam("scope") String scope, @Context HttpServletRequest req,
			@Context HttpServletResponse resp) throws Exception {
		try {
			System.out.println("I am in");
			ArrayList<ECMConfiguration> cList = ECMConfigurationList.getInstance(appId, scope).getAllECMConfigurations();

			return Response.ok().entity(cList).build();
		} catch (Exception e) {
			logger.logException(e);
			ResponseObject responseObject = ResponseObject.getResponseObject(e
					.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject)
					.build();
		}
	}
		
	//http://localhost:9080/ECMService/resources/ConfigurationService/updateConfigurations
	@POST
    @Path("/updateConfigurations")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response updateConfigurations(String jsonString, @Context HttpServletRequest req,@Context HttpServletResponse resp) 
			throws Exception
	{
	    try {
	    	ArrayList<TConfiguration> sList = JSONHelper.getInstance().getConfigurations(jsonString);
	    	ECMConfigurationList.getInstanceForUpdate().updateConfigurations(sList);
	        return Response.ok().entity("OK").build();
		}
	    catch (Exception e) {
	    	logger.logException(e);
	         e.printStackTrace();
	         throw new Exception(e.getMessage());
	    }
	}

/*	//http://localhost:9080/ECMService/resources/ConfigurationService/updateConfigurations
	@POST
	@Path("/updateConfigurations")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response updateConfigurations(String jsonString, @Context HttpServletRequest req,@Context HttpServletResponse resp) 
			throws Exception
	{
	    try {
	    	ArrayList<TConfiguration> sList = JSONHelper.getInstance().getConfigurations(jsonString);
	    	ECMConfigurationList.getInstanceForUpdate().updateConfigurations(sList);
	        return Response.ok().entity("OK").build();
		}
	    catch (Exception e) {
	    	logger.logException(e);
	         e.printStackTrace();
	         throw new Exception(e.getMessage());
	    }
	}*/

}
	
	

		


