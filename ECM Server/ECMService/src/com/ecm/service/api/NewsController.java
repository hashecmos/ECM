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
import com.ecm.db.list.ECMNewsList;
import com.ecm.db.transport.TNews;
import com.ecm.db.util.DBUtil;
import com.ecm.db.util.ECMLogger;
import com.ecm.filenet.model.FNAuthenticator;
import com.ecm.service.object.JSONHelper;
import com.ecm.service.object.ResponseObject;

@Path("/NewsService")
@ApplicationPath("resources")
public class NewsController {
	private static ECMLogger logger = ECMLogger.getInstance(NewsController.class);

	// http://localhost:9080/ECMService/resources/NewsService/getNews?userid=fatima
	@GET
	@Path("/getNews")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getNews(@QueryParam("userid") String user_login,
			@Context HttpServletRequest req, @Context HttpServletResponse resp)
			throws Exception {
		try {
			FNAuthenticator.Authenticate(user_login);
			
			ArrayList<TNews> news = ECMNewsList.getInstance().getActiveNews();
			return Response.ok().entity(news).build();
		} catch (Exception e) {
			logger.logException(e);
			ResponseObject responseObject = ResponseObject.getResponseObject(e
					.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject)
					.build();
		}
	}

	// http://localhost:9080/ECMService/resources/NewsService/getAllNews
	@GET
	@Path("/getAllNews")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAllNews(@QueryParam("userid") String user_login,
			@Context HttpServletRequest req, @Context HttpServletResponse resp)
			throws Exception {
		try {
			ArrayList<TNews> news = ECMNewsList.getInstance().getAllNews();
			return Response.ok().entity(news).build();
		} catch (Exception e) {
			logger.logException(e);
			ResponseObject responseObject = ResponseObject.getResponseObject(e
					.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject)
					.build();
		}
	}
	
	//http://localhost:9080/ECMService/resources/NewsService/saveNews
	@POST
    @Path("/saveNews")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response saveNews(String jsonString, @Context HttpServletRequest req,@Context HttpServletResponse resp) 
			throws Exception
	{
	    try {
	    	TNews news = JSONHelper.getInstance().getNews(jsonString);
	    	ECMNewsList.getInstance().saveNews(news);
	        return Response.ok().entity("OK").build();
		}
	    catch (Exception e) {
	    	logger.logException(e);
	         e.printStackTrace();
	         throw new Exception(e.getMessage());
	    }
	}
	
	// http://localhost:9080/ECMService/resources/NewsService/removeNews?id=1
	@GET
	@Path("/removeNews")
	@Produces(MediaType.APPLICATION_JSON)
	public Response removeNews(@QueryParam("id") String Id,
		@Context HttpServletRequest req,
		@Context HttpServletResponse resp) throws Exception {
		try {
			ECMNewsList.getInstance().removeNews(DBUtil.stringToLong(Id));
			
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
