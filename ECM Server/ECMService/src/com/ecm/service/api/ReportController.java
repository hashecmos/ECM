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

import com.ecm.db.list.ECMAdministrationList;
import com.ecm.db.list.ECMDocSignList;
import com.ecm.db.list.ECMWorkitemList;
import com.ecm.db.transport.TDocSignItem;
import com.ecm.db.transport.TMgmtReport;
import com.ecm.db.transport.TOrgUnit;
import com.ecm.db.transport.TReportFilter;
import com.ecm.db.transport.TWorkitemQuery;
import com.ecm.db.transport.TWorkitemSet;
import com.ecm.db.util.DBUtil;
import com.ecm.db.util.ECMLogger;
import com.ecm.filenet.list.FNDocumentSearch;
import com.ecm.filenet.list.FNObjectStoreList;
import com.ecm.filenet.model.FNObjectStore;
import com.ecm.service.object.JSONHelper;
import com.ecm.service.object.ReportHelper;
import com.ecm.service.object.ResponseObject;

@Path("/ReportService")
@ApplicationPath("resources")
public class ReportController {
	private static ECMLogger logger = ECMLogger.getInstance(ReportController.class);
	
	// http://localhost:9080/ECMService/resources/ReportService/getUserOrgUnits?empno=1234
	@GET
	@Path("/getUserOrgUnits")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getUserOrgUnits(@QueryParam("empno") String empNo, @Context HttpServletRequest req, 
			@Context HttpServletResponse resp) throws Exception {
		try {
			
			ArrayList<TOrgUnit> toList = ECMAdministrationList.getInstance().getUserOrgUnits(
					DBUtil.stringToLong(empNo));

			return Response.ok().entity(toList).build();
		} catch (Exception e) {
			logger.logException(e);
			ResponseObject responseObject = ResponseObject.getResponseObject(e.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject).build();
		}
	}
		
		
	// http://localhost:9080/ECMService/resources/ReportService/getSubOrgUnits?orgcode=TK310
	@GET
	@Path("/getSubOrgUnits")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getSubOrgUnits(@QueryParam("orgcode") String orgCode, @Context HttpServletRequest req, 
			@Context HttpServletResponse resp) throws Exception {
		try {
			ArrayList<TOrgUnit> toList = ECMAdministrationList.getInstance().getSubOrgUnitsFromOrgCode(orgCode);

			return Response.ok().entity(toList).build();
		} catch (Exception e) {
			logger.logException(e);
			ResponseObject responseObject = ResponseObject.getResponseObject(e.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject).build();
		}
	}
	
	// http://localhost:9080/ECMService/resources/ReportService/getOrgWorkitemCount	
	@POST
    @Path("/getOrgWorkitemCount")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getOrgWorkitemCount(String jsonString, @Context HttpServletRequest req, 
			@Context HttpServletResponse resp) throws Exception {
		try {
			TReportFilter trf = JSONHelper.getInstance().getReportFilter(jsonString);
			ArrayList<TOrgUnit> toList = ECMWorkitemList.getInstance().getOrgWorkitemCount(trf.orgCode, 
					trf.userType, trf.fromDate, trf.toDate, trf.EmpNo);

			return Response.ok().entity(toList).build();
		} catch (Exception e) {
			logger.logException(e);
			ResponseObject responseObject = ResponseObject.getResponseObject(e.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject).build();
		}
	}
	
	// http://localhost:9080/ECMService/resources/ReportService/getOrgAllReportCount
	@POST
	@Path("/getOrgAllReportCount")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getOrgAllReportCount(String jsonString,
			@Context HttpServletRequest req, @Context HttpServletResponse resp)
			throws Exception {
		try {
			TReportFilter trf = JSONHelper.getInstance().getReportFilter(jsonString);
			ArrayList<TMgmtReport> tmrList = ReportHelper.getInstance()
					.getOrgAllReportCount(trf);

			return Response.ok().entity(tmrList).build();
		} catch (Exception e) {
			logger.logException(e);
			ResponseObject responseObject = ResponseObject.getResponseObject(e
					.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject)
					.build();
		}
	}
		
	// http://localhost:9080/ECMService/resources/ReportService/getOrgSentitemCount
	@POST
    @Path("/getOrgSentitemCount")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getOrgSentitemCount(String jsonString, @Context HttpServletRequest req, 
			@Context HttpServletResponse resp) throws Exception {
		try {
			TReportFilter trf = JSONHelper.getInstance().getReportFilter(jsonString);
			ArrayList<TOrgUnit> toList = ECMWorkitemList.getInstance().getOrgSentitemCount(trf.orgCode, 
					trf.userType, trf.fromDate, trf.toDate, trf.EmpNo);
//			ArrayList<TOrgUnit> toList = ECMWorkitemList.getInstance().getOrgSentitemCountByWorkflow(trf.orgCode, 
//					trf.userType, trf.fromDate, trf.toDate, trf.EmpNo);

			return Response.ok().entity(toList).build();
		} catch (Exception e) {
			logger.logException(e);
			ResponseObject responseObject = ResponseObject.getResponseObject(e.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject).build();
		}
	}
		
/*	//http://localhost:9080/ECMService/resources/ReportService/getReportInboxItems 
	@POST
    @Path("/getReportInboxItems")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getReportInboxItems(String jsonString, @Context HttpServletRequest req,
			@Context HttpServletResponse resp) throws Exception
	{
	    try {
	    	logger.info("Search Inbox: Input = " + jsonString);
	    	TWorkitemQuery twq = JSONHelper.getInstance().getWorkitemQuery(jsonString);
	    	TWorkitemSet ts = ECMWorkitemList.getInstance().searchReportInboxitems(twq, false);
	
	        return Response.ok().entity(ts).build();
		}
	    catch (Exception e) {
	    	logger.logException(e);
			ResponseObject responseObject = ResponseObject.getResponseObject(e.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject).build();
	    }
	}*/
	
/*	//http://localhost:9080/ECMService/resources/ReportService/getReportSentItems 
	@POST
    @Path("/getReportSentItems")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getReportSentItems(String jsonString, @Context HttpServletRequest req,
			@Context HttpServletResponse resp) throws Exception
	{
	    try {
	    	logger.info("Search Sent Items: Input = " + jsonString);
	    	TWorkitemQuery twq = JSONHelper.getInstance().getWorkitemQuery(jsonString);
	    	TWorkitemSet ts = ECMWorkitemList.getInstance().searchReportSentitems(twq, false);
	
	        return Response.ok().entity(ts).build();
		}
	    catch (Exception e) {
	    	logger.logException(e);
			ResponseObject responseObject = ResponseObject.getResponseObject(e.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject).build();
	    }
	}*/
	
	// http://localhost:9080/ECMService/resources/ReportService/getOrgDocumentCount
	@POST
    @Path("/getOrgDocumentCount")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getOrgDocumentCount(String jsonString, @Context HttpServletRequest req, 
			@Context HttpServletResponse resp) throws Exception {
		try {
			TReportFilter trf = JSONHelper.getInstance().getReportFilter(jsonString);
			ArrayList<TOrgUnit> toList = ECMAdministrationList.getInstance().getAllSubOrgUnits(trf.orgCode, trf.EmpNo);
			FNObjectStore pos = FNObjectStoreList.getInstance().getApplicationObjectStore("ECM", "ECMOS");
			toList = FNDocumentSearch.getInstance(pos).getOrgDocumentCounts(toList, trf);
			return Response.ok().entity(toList).build();
		} catch (Exception e) {
			logger.logException(e);
			ResponseObject responseObject = ResponseObject.getResponseObject(e.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject).build();
		}
	}
	
	// http://localhost:9080/ECMService/resources/ReportService/getOrgESignItems
	@POST
    @Path("/getOrgESignItems")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getOrgESignItems(String jsonString, @Context HttpServletRequest req, 
			@Context HttpServletResponse resp) throws Exception {
		try {
			TReportFilter trf = JSONHelper.getInstance().getReportFilter(jsonString);
			ArrayList<TDocSignItem> toList = ECMDocSignList.getInstance().getOrgESignItems(trf.orgCode, 
					trf.fromDate, trf.toDate, trf.EmpNo);

			return Response.ok().entity(toList).build();
		} catch (Exception e) {
			logger.logException(e);
			ResponseObject responseObject = ResponseObject.getResponseObject(e.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject).build();
		}
	}
	
	// http://localhost:9080/ECMService/resources/ReportService/getOrgWorkitemCount
	@POST
	@Path("/exportOrgWorkitemCount")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	public Response exportOrgWorkitemCount(String jsonString, @Context HttpServletRequest req, 
			@Context HttpServletResponse resp) throws Exception {
		try {
			logger.info("Search Sent Items: Input = " + jsonString);
			TReportFilter trf = JSONHelper.getInstance().getReportFilter(jsonString);
			ArrayList<TOrgUnit> toList = ECMWorkitemList.getInstance().getOrgWorkitemCount(trf.orgCode, 
					trf.userType, trf.fromDate, trf.toDate, trf.EmpNo);
			ECMWorkitemList.getInstance().exportOrgReportCounts(req,resp,trf,"Received Workflow",trf.exportType,toList);
			return Response.ok().entity("OK").build();
		} catch (Exception e) {
			logger.logException(e);
			ResponseObject responseObject = ResponseObject.getResponseObject(e.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject).build();
		}
	}
	
	// http://localhost:9080/ECMService/resources/ReportService/exportOrgAllReportCount
	@POST
	@Path("/exportOrgAllReportCount")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	public Response exportOrgAllReportCount(String jsonString, @Context HttpServletRequest req, 
			@Context HttpServletResponse resp) throws Exception {
		try {
			logger.info("Search Sent Items: Input = " + jsonString);
			TReportFilter trf = JSONHelper.getInstance().getReportFilter(jsonString);
			ArrayList<TMgmtReport> tmrList = ReportHelper.getInstance().getOrgAllReportCount(trf);
			ECMWorkitemList.getInstance().exportOrgAllReportCounts(req,resp,trf,"All Report",trf.exportType,tmrList);
			return Response.ok().entity("OK").build();
		} catch (Exception e) {
			logger.logException(e);
			ResponseObject responseObject = ResponseObject.getResponseObject(e.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject).build();
		}
	}
	
	// http://localhost:9080/ECMService/resources/ReportService/exportOrgSentitemCount
	@POST
	@Path("/exportOrgSentitemCount")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	public Response exportOrgSentitemCount(String jsonString, @Context HttpServletRequest req, 
			@Context HttpServletResponse resp) throws Exception {
		try {
			logger.info("Search Sent Items: Input = " + jsonString);
			TReportFilter trf = JSONHelper.getInstance().getReportFilter(jsonString);
			ArrayList<TOrgUnit> toList = ECMWorkitemList.getInstance().getOrgSentitemCount(trf.orgCode, 
					trf.userType, trf.fromDate, trf.toDate, trf.EmpNo);
			ECMWorkitemList.getInstance().exportOrgReportCounts(req,resp,trf,"Sent Workflow",trf.exportType,toList);
			
			return Response.ok().entity("OK").build();
		} catch (Exception e) {
			logger.logException(e);
			ResponseObject responseObject = ResponseObject.getResponseObject(e.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject).build();
		}
	}
	
/*	//http://localhost:9080/ECMService/resources/ReportService/exportReportInboxItems 
	@POST
    @Path("/exportReportInboxItems")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	public Response exportReportInboxItems(String jsonString, @Context HttpServletRequest req,
			@Context HttpServletResponse resp) throws Exception
	{
	    try {
	    	logger.info("Search Inbox: Input = " + jsonString);
	    	TWorkitemQuery twq = JSONHelper.getInstance().getWorkitemQuery(jsonString);
	    	ECMWorkitemList.getInstance().exportReportInboxItems(req,resp,twq);
	    	
	        return Response.ok().entity("OK").build();
		}
	    catch (Exception e) {
	    	logger.logException(e);
			ResponseObject responseObject = ResponseObject.getResponseObject(e.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject).build();
	    }
	}*/
	
	
/*	//http://localhost:9080/ECMService/resources/ReportService/exportReportSentItems 
	@POST
    @Path("/exportReportSentItems")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	public Response exportReportSentItems(String jsonString, @Context HttpServletRequest req,
			@Context HttpServletResponse resp) throws Exception {
	    try {
	    	logger.info("Search Inbox: Input = " + jsonString);
	    	TWorkitemQuery twq = JSONHelper.getInstance().getWorkitemQuery(jsonString);
	    	ECMWorkitemList.getInstance().exportReportSentItems(req,resp,twq);
	    	
	        return Response.ok().entity("OK").build();
		}
	    catch (Exception e) {
	    	logger.logException(e);
			ResponseObject responseObject = ResponseObject.getResponseObject(e.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject).build();
	    }
	}*/
	
	// http://localhost:9080/ECMService/resources/ReportService/exportOrgDocumentCount
	@POST
	@Path("/exportOrgDocumentCount")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	public Response exportOrgDocumentCount(String jsonString ,
			@Context HttpServletRequest req, 
			@Context HttpServletResponse resp) throws Exception {
		try {
			TReportFilter trf = JSONHelper.getInstance().getReportFilter(jsonString);
			ArrayList<TOrgUnit> toList = ECMAdministrationList.getInstance().getAllSubOrgUnits(trf.orgCode, trf.EmpNo);
//			FNObjectStore os = FNObjectStoreList.getInstance().getPowerObjectStore("ECM", "ECMOS");
//			toList = FNDocumentSearch.getInstance(os).getOrgDocumentCounts(toList, trf.fromDate, trf.toDate);
			FNObjectStore pos = FNObjectStoreList.getInstance().getApplicationObjectStore("ECM", "ECMOS");
			toList = FNDocumentSearch.getInstance(pos).getOrgDocumentCounts(toList, trf);
			ECMWorkitemList.getInstance().exportOrgReportCounts(req, resp, trf, "Documents Created",trf.exportType,toList);
			return Response.ok().entity("OK").build();
		} catch (Exception e) {
			logger.logException(e);
			ResponseObject responseObject = ResponseObject.getResponseObject(e.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject).build();
		}
	}
	
	// http://localhost:9080/ECMService/resources/ReportService/exportOrgDocumentCounts
	@POST
	@Path("/exportOrgESignItems")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	public Response exportOrgESignItems(String jsonString ,
			@Context HttpServletRequest req, 
			@Context HttpServletResponse resp) throws Exception {
		try {
			TReportFilter trf = JSONHelper.getInstance().getReportFilter(jsonString);
			ArrayList<TDocSignItem> toList = ECMDocSignList.getInstance().getOrgESignItems(trf.orgCode, 
					 trf.fromDate, trf.toDate, trf.EmpNo);
			ECMWorkitemList.getInstance().exportOrgESignItems(req,resp,trf.exportType,trf,"eSign Documents",toList);
			return Response.ok().entity("OK").build();
		} catch (Exception e) {
			logger.logException(e);
			ResponseObject responseObject = ResponseObject.getResponseObject(e.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject).build();
		}
	}	
}
