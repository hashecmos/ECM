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
import com.ecm.db.list.ECMLookupList;
import com.ecm.db.model.ECMLookup;
import com.ecm.db.transport.TADPrincipal;
import com.ecm.db.transport.TAdminLog;
import com.ecm.db.transport.TLog;
import com.ecm.db.transport.TLookUpValueMapping;
import com.ecm.db.transport.TLookupValue;
import com.ecm.db.transport.TOrgUnit;
import com.ecm.db.util.DBUtil;
import com.ecm.db.util.ECMAdminLogger;
import com.ecm.db.util.ECMEncryption;
import com.ecm.db.util.ECMLogger;
import com.ecm.db.util.RequestHelper;
import com.ecm.service.object.JSONHelper;
import com.ecm.service.object.ResponseObject;
import com.ecm.db.transport.TLookup;
import com.ecm.filenet.list.FNObjectStoreList;
import com.ecm.filenet.model.FNDocument;
import com.ecm.filenet.model.FNEntryTemplate;
import com.ecm.filenet.model.FNObjectStore;

@Path("/AdministrationService")
@ApplicationPath("resources")
public class AdministrationController {
	private static ECMLogger logger = ECMLogger.getInstance(AdministrationController.class);
	
	// http://localhost:9080/ECMService/resources/AdministrationService/getTopLevelOrgUnit
	@GET
	@Path("/getTopLevelOrgUnit")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getTopLevelOrgUnit(@Context HttpServletRequest req,
			@Context HttpServletResponse resp) throws Exception {
		try {
			logger.debug("Begin - getTopLevelOrgUnit()");
			TOrgUnit to = ECMAdministrationList.getInstance().getToplevelOrgUnit();
			logger.debug("End - getTopLevelOrgUnit()");
			return Response.ok().entity(to).build();
		} catch (Exception e) {
			logger.logException(e);
			ResponseObject responseObject = ResponseObject.getResponseObject(e
					.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject)
					.build();
		}
	}

	// http://localhost:9080/ECMService/resources/AdministrationService/removeLookup?id=2
	@GET
	@Path("/removeLookup")
	@Produces(MediaType.APPLICATION_JSON)
	public Response removeLookup(@QueryParam("id") String lkupId,
			@Context HttpServletRequest req, @Context HttpServletResponse resp)
			throws Exception {
		try {
			String strResult = ECMLookupList.getInstance().removeLookup(
					DBUtil.stringToLong(lkupId));
			String empName = RequestHelper.getLoggedInEmpName(req, "");
			ECMAdminLogger.getInstance("ECMLookup").info("ECM Lookups", empName, "Lookup Id -" + lkupId.toString() + "is removed" );
			return Response.ok().entity(strResult).build();
		} catch (Exception e) {
			logger.logException(e);
			ResponseObject responseObject = ResponseObject.getResponseObject(e
					.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject)
					.build();
		}
	}

	// http://localhost:9080/ECMService/resources/AdministrationService/getNextECMNo
	@GET
	@Path("/getNextECMNo")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getNextECMNo(@Context HttpServletRequest req,
			@Context HttpServletResponse resp) throws Exception {
		try {
			String ret = ECMAdministrationList.getInstance().getNextECMNo();

			return Response.ok().entity(ret).build();
		} catch (Exception e) {
			logger.logException(e);
			ResponseObject responseObject = ResponseObject.getResponseObject(e
					.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject)
					.build();
		}
	}
	
	// http://localhost:9080/ECMService/resources/AdministrationService/getNextECMNoForDC
	@POST
	@Path("/getNextECMNoForDC")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	public Response getNextECMNoForDC(@Context HttpServletRequest req,
			@Context HttpServletResponse resp) throws Exception {
		try {			
			String ret = ECMAdministrationList.getInstance().getNextECMNo();

			return Response.ok().entity(ret).build();
		} catch (Exception e) {
			logger.logException(e);
			ResponseObject responseObject = ResponseObject.getResponseObject(e
					.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject)
					.build();
		}
	}

		
	// http://localhost:9080/ECMService/resources/AdministrationService/getSubLevelOrgUnits?orgId=1
	@GET
	@Path("/getSubLevelOrgUnits")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getSubLevelOrgUnits(@QueryParam("orgId") String orgId, @Context HttpServletRequest req,
			@Context HttpServletResponse resp) throws Exception {
		try {
			logger.debug("Begin - getSubLevelOrgUnits()");
			ArrayList<TOrgUnit> toList = ECMAdministrationList.getInstance().getSubOrgUnits(DBUtil.stringToLong(orgId));
			logger.debug("End - getSubLevelOrgUnits() ");
			return Response.ok().entity(toList).build();
		} catch (Exception e) {
			logger.logException(e);
			ResponseObject responseObject = ResponseObject.getResponseObject(e
					.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject)
					.build();
		}
	}
	
    // http://localhost:9080/ECMService/resources/AdministrationService/getOrgUnitsByEntryTemplate?etId={sasd}
	@GET
	@Path("/getOrgUnitsByEntryTemplate")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getOrgUnitsByEntryTemplate(@QueryParam("etId") String etId, @Context HttpServletRequest req,
			@Context HttpServletResponse resp) throws Exception {
		try {
			logger.debug("Begin - getOrgUnitsByEntryTemplate()");
			ArrayList<TOrgUnit> toList = ECMAdministrationList.getInstance().getOrgUnitsByEntryTemplate(etId);

			return Response.ok().entity(toList).build();
		} catch (Exception e) {
			logger.logException(e);
			ResponseObject responseObject = ResponseObject.getResponseObject(e
					.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject)
					.build();
		}
	}
		
	// http://localhost:9080/ECMService/resources/AdministrationService/getEncryptionKey
	@GET
	@Path("/getEncryptionKey")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getEncryptionKey(@Context HttpServletRequest req,
			@Context HttpServletResponse resp) throws Exception {
		try {
			ECMEncryption enc = new ECMEncryption();
			String key = enc.getEncryptionKey();

			return Response.ok().entity(key).build();
		} catch (Exception e) {
			logger.logException(e);
			ResponseObject responseObject = ResponseObject.getResponseObject(e
					.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject)
					.build();
		}
	}
	
	// http://localhost:9080/ECMService/resources/AdministrationService/encryptString?key=keyvalue&string=stringvalue
	@GET
	@Path("/encryptString")
	@Produces(MediaType.APPLICATION_JSON)
	public Response encryptString(@QueryParam("key") String key, @QueryParam("string") String inputString,
			@Context HttpServletRequest req, @Context HttpServletResponse resp) throws Exception {
		try {
			ECMEncryption enc = new ECMEncryption();
			String encString = enc.getEncryptedString(key, inputString);
			return Response.ok().entity(encString).build();
		} catch (Exception e) {
			logger.logException(e);
			ResponseObject responseObject = ResponseObject.getResponseObject(e
					.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject)
					.build();
		}
	}
	
	// http://localhost:9080/ECMService/resources/AdministrationService/decryptString?key=keyvalue&string=stringvalue
	@GET
	@Path("/decryptString")
	@Produces(MediaType.APPLICATION_JSON)
	public Response decryptString(@QueryParam("key") String key, @QueryParam("string") String inputString,
			@Context HttpServletRequest req, @Context HttpServletResponse resp) throws Exception {
		try {
			ECMEncryption enc = new ECMEncryption();
			String decString = enc.getDecryptedString(key, inputString);

			return Response.ok().entity(decString).build();
		} catch (Exception e) {
			logger.logException(e);
			ResponseObject responseObject = ResponseObject.getResponseObject(e
					.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject)
					.build();
		}
	}
	
	// http://localhost:9080/ECMService/resources/AdministrationService/saveLookup?name=myLookup
	@GET
	@Path("/saveLookup")
	@Produces(MediaType.APPLICATION_JSON)
	public Response saveLookup(@QueryParam("name") String lName,@QueryParam("id") String lId,
			@Context HttpServletRequest req,
			@Context HttpServletResponse resp) throws Exception {
		try {
			String strResult = ECMLookupList.getInstance().saveLookup(
					DBUtil.stringToLongDefault(lId, 0), lName);
			String empName = RequestHelper.getLoggedInEmpName(req, "");
			ECMAdminLogger.getInstance("ECMLookup").info("ECM Lookup", empName, "Lookup - " + lName + " with Id -" + lId + " is saved" );
		
			return Response.ok().entity(strResult).build();
		} catch (Exception e) {
			logger.logException(e);
			ResponseObject responseObject = ResponseObject.getResponseObject(e
					.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject)
					.build();
		}
	}
	
	// http://localhost:9080/ECMService/resources/AdministrationService/getLookups
	@GET
	@Path("/getLookups")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getLookups(@Context HttpServletRequest req,
			@Context HttpServletResponse resp) throws Exception {
		try {
			ArrayList<TLookup> lList = ECMLookupList.getInstance().getAllLookUps(false);

			return Response.ok().entity(lList).build();
		} catch (Exception e) {
			logger.logException(e);
			ResponseObject responseObject = ResponseObject.getResponseObject(e
					.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject)
					.build();
		}
	}
	
	// http://localhost:9080/ECMService/resources/AdministrationService/getLookupsByOrgId?orgid=1234
	@GET
	@Path("/getLookupsByOrgId")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getLookupsByOrgId(@QueryParam("orgid") String orgId, @Context HttpServletRequest req,
			@Context HttpServletResponse resp) throws Exception {
		try {
			ArrayList<TLookup> lList = ECMLookupList.getInstance().getLookUpsByOrgId(DBUtil.stringToLong(orgId));

			return Response.ok().entity(lList).build();
		} catch (Exception e) {
			logger.logException(e);
			ResponseObject responseObject = ResponseObject.getResponseObject(e
					.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject)
					.build();
		}
	}
	
	//					
	
	// http://localhost:9080/ECMService/resources/AdministrationService/getLookupValues&id=
	@GET
	@Path("/getLookupValues")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getLookupValues(@QueryParam("id") String id, @Context HttpServletRequest req,
			@Context HttpServletResponse resp) throws Exception {
		try {
			
			ECMLookup ecml = new ECMLookup();
			ecml.setId(DBUtil.stringToLong(id));
			ArrayList<TLookupValue> valueList = ecml.getValues();

			return Response.ok().entity(valueList).build();
		} catch (Exception e) {
			logger.logException(e);
			ResponseObject responseObject = ResponseObject.getResponseObject(e
					.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject)
					.build();
		}
	}
	
	// http://localhost:9080/ECMService/resources/AdministrationService/getLookupsWithValues
	@GET
	@Path("/getLookupsWithValues")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getLookupsWithValues(@Context HttpServletRequest req,
			@Context HttpServletResponse resp) throws Exception {
		try {
			ArrayList<TLookup> lList = ECMLookupList.getInstance().getAllLookUps(true);

			return Response.ok().entity(lList).build();
		} catch (Exception e) {
			logger.logException(e);
			ResponseObject responseObject = ResponseObject.getResponseObject(e
					.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject)
					.build();
		}
	}
	
	// http://localhost:9080/ECMService/resources/AdministrationService/getLookupMappings
	@GET
	@Path("/getLookupMappings")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getLookupMappings(@Context HttpServletRequest req,
			@Context HttpServletResponse resp) throws Exception {
		try {
			ArrayList<TLookUpValueMapping> lList = ECMLookupList.getInstance().getAllLookUpMappings();

			return Response.ok().entity(lList).build();
		} catch (Exception e) {
			logger.logException(e);
			ResponseObject responseObject = ResponseObject.getResponseObject(e
					.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject)
					.build();
		}
	}
	
	// http://localhost:9080/ECMService/resources/AdministrationService/getLookupMappingsByOrg
	@GET
	@Path("/getLookupMappingsByOrg")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getLookupMappingsByOrg(@QueryParam("orgid") String orgId, @QueryParam("etvsid") String etVsId, 
			@Context HttpServletRequest req, @Context HttpServletResponse resp) throws Exception {
		try {
			ArrayList<TLookUpValueMapping> lList = ECMLookupList.getInstance().getLookUpMappingsByOrg(DBUtil.stringToLong(orgId), etVsId);

			return Response.ok().entity(lList).build();
		} catch (Exception e) {
			logger.logException(e);
			ResponseObject responseObject = ResponseObject.getResponseObject(e
					.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject)
					.build();
		}
	}
	
	// http://localhost:9080/ECMService/resources/AdministrationService/addLookupMapping?
		//id=1&orgUnit=1&templid={}&prop=propname
	@GET
	@Path("/addLookupMapping")
	@Produces(MediaType.APPLICATION_JSON)
	public Response addLookupMapping(@QueryParam("id") String lid,
			@QueryParam("orgUnit") String orgId,
			@QueryParam("templid") String templId,
			@QueryParam("prop") String propName,
			@Context HttpServletRequest req,
			@Context HttpServletResponse resp) throws Exception {
		try {
			/*FNObjectStore os = FNObjectStoreList.getInstance().getApplicationObjectStore("ECM", "ECMOS");
			FNEntryTemplate et = FNEntryTemplate.getInstance(os);		
			et.setId(templId);
			String etName = et.getEntryTemplateName();*/
			
			ECMLookupList.getInstance().addLookUpMapping(DBUtil.stringToLong(lid), DBUtil.stringToLong(orgId),templId, propName);//etName
			String empName = RequestHelper.getLoggedInEmpName(req, "");
			ECMAdminLogger.getInstance("ECMLookupMapping").info("ECM Lookup Mapping", empName, "Lookup Mapping for property -" + propName + " for orgunit - " + orgId + "is added" );
			return Response.ok().entity("OK").build();
		} catch (Exception e) {
			logger.logException(e);
			ResponseObject responseObject = ResponseObject.getResponseObject(e.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject).build();
		}
	}

	// http://localhost:9080/ECMService/resources/AdministrationService/removeLookupMapping?
			//orgUnit=1&templid={}&prop=propname
	@GET
	@Path("/removeLookupMapping")
	@Produces(MediaType.APPLICATION_JSON)
	public Response removeLookupMapping(@QueryParam("orgUnit") String orgId,
			@QueryParam("templid") String templId,
			@QueryParam("prop") String propName,
			@Context HttpServletRequest req,
			@Context HttpServletResponse resp) throws Exception {
		try {
			ECMLookupList.getInstance().removeLookUpMapping( DBUtil.stringToLong(orgId),templId, propName);
			String empName = RequestHelper.getLoggedInEmpName(req, "");
			ECMAdminLogger.getInstance("ECMLookupMapping").info("ECM Lookup Mapping", empName, "Lookup Mapping for property -" 
					+ propName + " for orgunit - " + orgId + "is removed" );
			return Response.ok().entity("OK").build();
		} catch (Exception e) {
			logger.logException(e);
			ResponseObject responseObject = ResponseObject.getResponseObject(e
					.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject)
					.build();
		}
	}
	
	//http://localhost:9080/ECMService/resources/AdministrationService/updateLookupValues
	@POST
    @Path("/updateLookupValues")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response updateLookupValues(String jsonString, @Context HttpServletRequest req,@Context HttpServletResponse resp) 
			throws Exception
	{
	    try {
	    	TLookup lookup = JSONHelper.getInstance().getLookup(jsonString);
	    	String strResult = ECMLookupList.getInstance().updateLookupValues(lookup);
	    	String empName = RequestHelper.getLoggedInEmpName(req, "");
			ECMAdminLogger.getInstance("ECMLookupValues").info("ECM Lookup Values", empName, "LookupValues for " 
					+ lookup.name + "(" + lookup.id + ") is updated." );
	    	return Response.ok().entity(strResult).build();
		}
	    catch (Exception e) {
	    	logger.logException(e);
	         e.printStackTrace();
	         throw new Exception(e.getMessage());
	    }
	}
	
	//http://localhost:9080/ECMService/resources/AdministrationService/removeLookupValue
	@GET
    @Path("/removeLookupValue")
	@Produces(MediaType.APPLICATION_JSON)
	public Response removeLookupValue(@QueryParam("id") String lvId, @QueryParam("lookupid") String lookUpId,
			@Context HttpServletRequest req,@Context HttpServletResponse resp) 
			throws Exception
	{
	    try {
	    	ECMLookupList.getInstance().removeLookupValue(DBUtil.stringToLong(lvId), DBUtil.stringToLong(lookUpId));
	    	String empName = RequestHelper.getLoggedInEmpName(req, "");
			ECMAdminLogger.getInstance("ECMLookupValue").info("ECM Lookup Value", empName, "Lookup Value for Lookup Id -" 
					+ lookUpId + " is removed" );
	    	return Response.ok().entity("OK").build();
		}
	    catch (Exception e) {
	    	logger.logException(e);
	         e.printStackTrace();
	         throw new Exception(e.getMessage());
	    }
	}
	
	// http://localhost:9080/ECMService/resources/AdministrationService/getLDAPGroupMembers?group=P8Admins
	@GET
	@Path("/getLDAPGroupMembers")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getLDAPGroupMembers(@QueryParam("group") String groupName, @Context HttpServletRequest req,
			@Context HttpServletResponse resp) throws Exception {
		try {
			ArrayList<TADPrincipal> mList = ECMAdministrationList.getInstance().getLDAPGroupMembers(groupName);
			return Response.ok().entity(mList).build();
		} catch (Exception e) {
			logger.logException(e);
			ResponseObject responseObject = ResponseObject.getResponseObject(e
					.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject)
					.build();
		}
	}
	
	// http://localhost:9080/ECMService/resources/AdministrationService/searchLDAPUsers?group=P8Admin
	@GET
	@Path("/searchLDAPUsers")
	@Produces(MediaType.APPLICATION_JSON)
	public Response searchLDAPUsers(@QueryParam("user") String userName, @Context HttpServletRequest req,
			@Context HttpServletResponse resp) throws Exception {
		try {
			ArrayList<TADPrincipal> uList = ECMAdministrationList.getInstance().searchLDAPForUser(userName);
			return Response.ok().entity(uList).build();
		} catch (Exception e) {
			logger.logException(e);
			ResponseObject responseObject = ResponseObject.getResponseObject(e
					.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject)
					.build();
		}
	}
	
	// http://localhost:9080/ECMService/resources/AdministrationService/searchOrgUnits?text=searchtext
	@GET
	@Path("/searchOrgUnits")
	@Produces(MediaType.APPLICATION_JSON)
	public Response searchOrgUnits(@QueryParam("text") String searchText, @Context HttpServletRequest req,
			@Context HttpServletResponse resp) throws Exception {
		try {
			ArrayList<TOrgUnit> toList = ECMAdministrationList.getInstance().searchOrgUnits(searchText);
			return Response.ok().entity(toList).build();
		} catch (Exception e) {
			logger.logException(e);
			ResponseObject responseObject = ResponseObject.getResponseObject(e
					.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject)
					.build();
		}
	}
	
	// http://localhost:9080/ECMService/resources/AdministrationService/searchLDAPGroups?group=P8Admins
	@GET
	@Path("/searchLDAPGroups")
	@Produces(MediaType.APPLICATION_JSON)
	public Response searchLDAPGroups(@QueryParam("group") String groupName, @Context HttpServletRequest req,
			@Context HttpServletResponse resp) throws Exception {
		try {
			ArrayList<TADPrincipal> gList = ECMAdministrationList.getInstance().searchLDAPForGroup(groupName);
			return Response.ok().entity(gList).build();
		} catch (Exception e) {
			logger.logException(e);
			ResponseObject responseObject = ResponseObject.getResponseObject(e
					.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject)
					.build();
		}
	}
	
	// http://localhost:9080/ECMService/resources/AdministrationService/addEntryTemplateMapping?
			//orgid=1&templid={}
	@GET
	@Path("/addEntryTemplateMapping")
	@Produces(MediaType.APPLICATION_JSON)
	public Response addEntryTemplateMapping(@QueryParam("orgid") String orgId,
			@QueryParam("etId") String etId, @QueryParam("isvisible") String isVisible,
			@QueryParam("etVsId") String etVsId, @Context HttpServletRequest req,
			@Context HttpServletResponse resp) throws Exception {
		try {
			FNObjectStore os = FNObjectStoreList.getInstance().getApplicationObjectStore("ECM", "ECMOS");
			FNDocument doc = FNDocument.getInstance(os);
			doc.setId(etId);
			doc.load();
			String name = doc.getDocumentTitle();
			ECMAdministrationList.getInstance().addEntryTemplateMapping(
					DBUtil.stringToLong(orgId), etId, name, etVsId, isVisible);
			
			String empName = RequestHelper.getLoggedInEmpName(req, "");
			ECMAdminLogger.getInstance("EntryTemplateMapping").info("ECM EntryTemplate Mapping", empName, "EntryTemplate Mapping for Id - " + etId + " for orgunit - " + orgId + " is added." );
			return Response.ok().entity("OK").build();
		} catch (Exception e) {
			logger.logException(e);
			ResponseObject responseObject = ResponseObject.getResponseObject(e
					.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject)
					.build();
		}
	}
	
	// http://localhost:9080/ECMService/resources/AdministrationService/removeEntryTemplateMapping?
				//mappingid=1
	@GET
	@Path("/removeEntryTemplateMapping")
	@Produces(MediaType.APPLICATION_JSON)
	public Response removeEntryTemplateMapping(@QueryParam("orgId") String orgId,
			@QueryParam("etId") String etId,
			@Context HttpServletRequest req,
			@Context HttpServletResponse resp) throws Exception {
		try {
			ECMAdministrationList.getInstance().removeEntryTemplateMapping(
					DBUtil.stringToLong(orgId),etId);

			return Response.ok().entity("OK").build();
		} catch (Exception e) {
			logger.logException(e);
			ResponseObject responseObject = ResponseObject.getResponseObject(e
					.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject)
					.build();
		}
	}
	
	// http://localhost:9080/ECMService/resources/AdministrationService/getLogs
	@GET
	@Path("/getLogs")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getLogs(@Context HttpServletRequest req, @Context HttpServletResponse resp) throws Exception {
		try {
			ArrayList<TLog> logList = ECMAdministrationList.getInstance().getLogs();
			return Response.ok().entity(logList).build();
		} catch (Exception e) {
			logger.logException(e);
			ResponseObject responseObject = ResponseObject.getResponseObject(e
					.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject)
					.build();
		}
	}
	
	// http://localhost:9080/ECMService/resources/AdministrationService/getAdminLogs
		@GET
		@Path("/getAdminLogs")
		@Produces(MediaType.APPLICATION_JSON)
		public Response getAdminLogs(@Context HttpServletRequest req, @Context HttpServletResponse resp) throws Exception {
			try {
				ArrayList<TAdminLog> logList = ECMAdministrationList.getInstance().getAdminLogs("");
				return Response.ok().entity(logList).build();
			} catch (Exception e) {
				logger.logException(e);
				ResponseObject responseObject = ResponseObject.getResponseObject(e
						.getMessage());
				return Response.status(Status.BAD_REQUEST).entity(responseObject)
						.build();
			}
		}
	
	// http://localhost:9080/ECMService/resources/AdministrationService/getLogDetails&logid=
	@GET
	@Path("/getLogDetails")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getLogDetails(@QueryParam("logid") String id, @Context HttpServletRequest req,
			@Context HttpServletResponse resp) throws Exception {
		try {
			String details = ECMAdministrationList.getInstance().getLogDetails(DBUtil.stringToLong(id));

			return Response.ok().entity(details).build();
		} catch (Exception e) {
			logger.logException(e);
			ResponseObject responseObject = ResponseObject.getResponseObject(e
					.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject)
					.build();
		}
	}
}