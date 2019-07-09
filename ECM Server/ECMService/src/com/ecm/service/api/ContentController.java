package com.ecm.service.api;
import java.util.ArrayList;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.ecm.db.list.ECMConfigurationList;
import com.ecm.db.list.ECMUserList;
import com.ecm.db.util.DBUtil;
import com.ecm.db.util.ECMLogger;
import com.ecm.db.util.RequestHelper;
import com.ecm.filenet.list.FNDocumentClassList;
import com.ecm.filenet.list.FNDocumentSearch;
import com.ecm.filenet.list.FNEntryTemplateList;
import com.ecm.filenet.list.FNFolderList;
import com.ecm.filenet.list.FNObjectStoreList;
import com.ecm.filenet.list.FNSearchTemplateList;
import com.ecm.filenet.model.FNEntryTemplate;
import com.ecm.filenet.model.FNFolder;
import com.ecm.filenet.model.FNObjectStore;
import com.ecm.filenet.model.FNSearchTemplate;
import com.ecm.filenet.transport.TFNClass;
import com.ecm.filenet.transport.TFNDocument;
import com.ecm.filenet.transport.TFNFolder;
import com.ecm.filenet.transport.TFNObjectStore;
import com.ecm.filenet.util.Utils;
import com.ecm.service.object.MigrationHelper;
import com.ecm.service.object.ReportHelper;
import com.ecm.service.object.ResponseObject;

@Path("/ContentService")
@ApplicationPath("resources")
public class ContentController extends Application {
	private static ECMLogger logger = ECMLogger.getInstance(ContentController.class);
	// http://localhost:9080/ECMService/resources/ContentService/getObjectStores
	@GET
	@Path("/getObjectStores")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getObjectStores(@Context HttpServletRequest req, @Context HttpServletResponse resp)
			throws Exception {
		try {
			ArrayList<TFNObjectStore> osList = FNObjectStoreList.getInstance().getObjectStoresTransport();

			return Response.ok().entity(osList).build();
		} catch (Exception e) {
			logger.logException(e);
			ResponseObject responseObject = ResponseObject.getResponseObject(e.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject).build();
		}
	}

	// http://localhost:9080/ECMService/resources/ContentService/getAppObjectStore
	@GET
	@Path("/getAppObjectStore")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAppObjectStore(@Context HttpServletRequest req, @Context HttpServletResponse resp)
			throws Exception {
		try {
			TFNObjectStore tOs = FNObjectStoreList.getInstance().getApplicationObjectStore("ECM", "ECMOS")
					.getTransport();

			return Response.ok().entity(tOs).build();
		} catch (Exception e) {
			logger.logException(e);
			ResponseObject responseObject = ResponseObject.getResponseObject(e.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject).build();
		}
	}

	// http://localhost:9080/ECMService/resources/ContentService/getDocumentClasses
	@GET
	@Path("/getDocumentClasses")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getDocumentClasses(@Context HttpServletRequest req, @Context HttpServletResponse resp)
			throws Exception {
		try {
			FNObjectStore os = FNObjectStoreList.getInstance().getApplicationObjectStore("ECM", "ECMOS");
			ArrayList<TFNClass> classList = FNDocumentClassList.getInstance(os).getDocumentClassesTransport();

			return Response.ok().entity(classList).build();
		} catch (Exception e) {
			logger.logException(e);
			ResponseObject responseObject = ResponseObject.getResponseObject(e.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject).build();
		}
	}

	// http://localhost:9080/ECMService/resources/ContentService/getCETopFolders
	@GET
	@Path("/getCETopFolders")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getCETopFolders(@Context HttpServletRequest req, @Context HttpServletResponse resp) throws Exception {
		try {
			FNObjectStore os = FNObjectStoreList.getInstance().getApplicationObjectStore("ECM", "ECMOS");
			FNFolder rf = os.getRootFolder();
			ArrayList<TFNFolder> fList = rf.getSubFolders();

			return Response.ok().entity(fList).build();
		} catch (Exception e) {
			logger.logException(e);
			ResponseObject responseObject = ResponseObject.getResponseObject(e.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject).build();
		}
	}
	
	// http://localhost:9080/ECMService/resources/ContentService/getTopFolders
	@GET
	@Path("/getTopFolders")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getTopFolders(@Context HttpServletRequest req, @Context HttpServletResponse resp) throws Exception {
		try {
			FNObjectStore os = FNObjectStoreList.getInstance().getApplicationObjectStore("ECM", "ECMOS");
			FNFolder rf = os.getECMRootFolder();
			ArrayList<TFNFolder> fList = rf.getSubFolders();

			return Response.ok().entity(fList).build();
		} catch (Exception e) {
			logger.logException(e);
			ResponseObject responseObject = ResponseObject.getResponseObject(e.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject).build();
		}
	}

	
	// http://localhost:9080/ECMService/resources/ContentService/getSubfolders
	@GET
	@Path("/getSubfolders")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getSubfolders(@QueryParam("id") String folderId, 
			@Context HttpServletRequest req,
			@Context HttpServletResponse resp) throws Exception {
		try {
			FNObjectStore os = FNObjectStoreList.getInstance().getApplicationObjectStore("ECM", "ECMOS");
			FNFolder rf =FNFolder.getInstance(os);	
			rf.setId(folderId);
			
			ArrayList<TFNFolder> fList = rf.getSubFolders();
			return Response.ok().entity(fList).build();

		} catch (Exception e) {
			logger.logException(e);
			ResponseObject responseObject = ResponseObject.getResponseObject(e.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject).build();
		}
	}
	
	// http://localhost:9080/ECMService/resources/ContentService/getFolderDetails
	@GET
	@Path("/getFolderDetails")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getFolderDetails(@QueryParam("id") String folderId, 
			@Context HttpServletRequest req,
			@Context HttpServletResponse resp) throws Exception {
		try {
			FNObjectStore os = FNObjectStoreList.getInstance().getApplicationObjectStore("ECM", "ECMOS");
			FNFolder rf =FNFolder.getInstance(os);	
			rf.setId(folderId);
			return Response.ok().entity(rf.getFolderDetails()).build();

		} catch (Exception e) {
			logger.logException(e);
			ResponseObject responseObject = ResponseObject.getResponseObject(e.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject).build();
		}
	}
	
	// http://localhost:9080/ECMService/resources/ContentService/getFolderDocuments
	@GET
	@Path("/getFolderDocuments")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getFolderDocuments(@QueryParam("id") String folderId, 
			@Context HttpServletRequest req,
			@Context HttpServletResponse resp) throws Exception {
		try {
			FNObjectStore os = FNObjectStoreList.getInstance().getApplicationObjectStore("ECM", "ECMOS");
			FNFolder rf =FNFolder.getInstance(os);	
			rf.setId(folderId);
			
			ArrayList<TFNDocument> dList = rf.getDocuments();
			return Response.ok().entity(dList).build();

		} catch (Exception e) {
			logger.logException(e);
			ResponseObject responseObject = ResponseObject.getResponseObject(e.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject).build();
		}
	}
	
	// http://localhost:9080/ECMService/resources/ContentService/exportFolderDocumentsToExcel
	@GET
	@Path("/exportFolderDocumentsToExcel")
	@Produces(MediaType.APPLICATION_JSON)
	public Response exportFolderDocumentsToExcel(@QueryParam("id") String folderId, 
			@Context HttpServletRequest req,
			@Context HttpServletResponse resp) throws Exception {
		try {
			FNObjectStore os = FNObjectStoreList.getInstance().getApplicationObjectStore("ECM", "ECMOS");
			FNFolder rf =FNFolder.getInstance(os);	
			rf.setId(folderId);
			
			ArrayList<TFNDocument> dList = rf.getDocuments();
			ReportHelper.getInstance().exportDocumentsToExcel(req, resp, dList, "Folder Documents");
			return Response.ok().entity("OK").build();

		} catch (Exception e) {
			logger.logException(e);
			ResponseObject responseObject = ResponseObject.getResponseObject(e.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject).build();
		}
	}
	
	// http://localhost:9080/ECMService/resources/ContentService/getFolderPermissions?id={78568800-1352-4F4D-A10D-03C0F0D4E671}
	@GET
	@Path("/getFolderPermissions")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getFolderPermissions(@QueryParam("id") String id,
			@Context HttpServletRequest req, @Context HttpServletResponse resp)
			throws Exception {
		try {
			FNObjectStore os = FNObjectStoreList.getInstance().getApplicationObjectStore("ECM", "ECMOS");
			FNFolder rf =FNFolder.getInstance(os);	
			rf.setId(id);
			
			return Response.ok().entity(rf.getPermissions()).build();
		} catch (Exception e) {
			logger.logException(e);
			ResponseObject responseObject = ResponseObject.getResponseObject(e.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject)
					.build();
		}
	}
	
	// http://localhost:9080/ECMService/resources/ContentService/getAccessPrivileges?mask=123123
	@GET
	@Path("/getAccessPrivileges")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAccessPrivileges(@QueryParam("mask") String mask,
			@Context HttpServletRequest req, @Context HttpServletResponse resp)
			throws Exception {
		try {
			return Response.ok().entity(Utils.getAccessPrivileges((int)DBUtil.stringToLong(mask))).build();
		} catch (Exception e) {
			logger.logException(e);
			ResponseObject responseObject = ResponseObject.getResponseObject(e.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject)
					.build();
		}
	}
	
	// http://localhost:9080/ECMService/resources/ContentService/createSubfolder
	@GET
	@Path("/createSubfolder")
	@Produces(MediaType.APPLICATION_JSON)
	public Response createSubfolder(@QueryParam("parentid") String folderId, @QueryParam("name") String folderName,
			@Context HttpServletRequest req,
			@Context HttpServletResponse resp) throws Exception {
		try {
			FNObjectStore os = FNObjectStoreList.getInstance().getApplicationObjectStore("ECM", "ECMOS");
			FNFolder rf =FNFolder.getInstance(os);	
			rf.setId(folderId);
			rf.setClassName("ECMFolder"); //Uncomment this once the class is created
			FNFolder sf = rf.createSubFolder(folderName);
			return Response.ok().entity(sf.getId()).build();

		} catch (Exception e) {
			logger.logException(e);
			ResponseObject responseObject = ResponseObject.getResponseObject(e.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject).build();
		}
	}
	
	// http://localhost:9080/ECMService/resources/ContentService/deleteFolder
	@GET
	@Path("/deleteFolder")
	@Produces(MediaType.APPLICATION_JSON)
	public Response deleteFolder(@QueryParam("id") String folderId, 
			@Context HttpServletRequest req,
			@Context HttpServletResponse resp) throws Exception {
		try {
			FNObjectStore os = FNObjectStoreList.getInstance().getApplicationObjectStore("ECM", "ECMOS");
			FNFolder rf =FNFolder.getInstance(os);	
			rf.setId(folderId);
			rf.delete();
			return Response.ok().entity("OK").build();

		} catch (Exception e) {
			logger.logException(e);
			ResponseObject responseObject = ResponseObject.getResponseObject(e.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject).build();
		}
	}
		
	// http://localhost:9080/ECMService/resources/ContentService/getEntryTemplates?empNo=1002
	@GET
	@Path("/getEntryTemplates")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getEntryTemplates(@QueryParam("empNo") String empno, @Context HttpServletRequest req,@Context HttpServletResponse resp) throws Exception{
		try{
			FNObjectStore os = FNObjectStoreList.getInstance().getApplicationObjectStore("ECM", "ECMOS");
			long empNo = RequestHelper.getLoggedInEmployee(req, empno);
			ArrayList<TFNClass> classList = FNEntryTemplateList.getInstance(os).getEntryTemplates(empNo);
			
			String defTemp = ECMUserList.getInstance().getUserSetting(empNo, "ECM", "Default Template");
			ArrayList<TFNClass> tempList = classList;
			
			if((defTemp == null) || (defTemp.trim().length() <= 0)) {
				ECMConfigurationList cList = ECMConfigurationList.getInstance("ECM", "APP");
				String sysDefTemp = cList.getConfigValue("DEFAULTTEMPLATE");
				if((sysDefTemp != null) && (sysDefTemp.trim().length() > 0))
					defTemp = sysDefTemp;			
			}
			if((defTemp != null) && (defTemp.trim().length() > 0)) {
				tempList = new ArrayList<TFNClass>();
				for(TFNClass cClass: classList) {
					if(cClass.symName.trim().equalsIgnoreCase(defTemp))
						tempList.add(0, cClass);
					else if(cClass.symName.trim().equalsIgnoreCase("KOC Document"))
						continue;
					else
						tempList.add(cClass);
				}
			}
			return Response.ok().entity(tempList).build();
		}catch (Exception e) {
			logger.logException(e);
			ResponseObject responseObject = ResponseObject.getResponseObject(e.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject).build();
		}
	}
	
	// http://localhost:9080/ECMService/resources/ContentService/getEntryTemplatesByOrgId?orgId=1
	@GET
	@Path("/getEntryTemplatesByOrgId")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getEntryTemplatesByOrgId(@QueryParam("orgId") String orgId, @Context HttpServletRequest req,@Context HttpServletResponse resp) throws Exception{
		try{
			FNObjectStore os = FNObjectStoreList.getInstance().getApplicationObjectStore("ECM", "ECMOS");
			ArrayList<TFNClass> classList = FNEntryTemplateList.getInstance(os).getEntryTemplatesByOrgId(
					DBUtil.stringToLong(orgId));
			
			return Response.ok().entity(classList).build();
		}catch (Exception e) {
			logger.logException(e);
			ResponseObject responseObject = ResponseObject.getResponseObject(e.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject).build();
		}
	}
		
	// http://localhost:9080/ECMService/resources/ContentService/getEntryTemplatesByName?empName=avannadil
	@GET
	@Path("/getEntryTemplatesByName")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getEntryTemplatesByName(@QueryParam("empName") String empName, @Context HttpServletRequest req,@Context HttpServletResponse resp) throws Exception{
		try{
			FNObjectStore os = FNObjectStoreList.getInstance().getApplicationObjectStore("ECM", "ECMOS");
			long empNo = RequestHelper.getLoggedInEmployee(req, empName);
			ArrayList<TFNClass> classList = FNEntryTemplateList.getInstance(os).getEntryTemplates(empNo);
			
			String defTemp = ECMUserList.getInstance().getUserSetting(empNo, "ECM", "Default Template");
			ArrayList<TFNClass> tempList = classList;
			
			if((defTemp == null) || (defTemp.trim().length() <= 0)) {
				ECMConfigurationList cList = ECMConfigurationList.getInstance("ECM", "APP");
				String sysDefTemp = cList.getConfigValue("DEFAULTTEMPLATE");
				if((sysDefTemp != null) && (sysDefTemp.trim().length() > 0))
					defTemp = sysDefTemp;			
			}
			if((defTemp != null) && (defTemp.trim().length() > 0)) {
				tempList = new ArrayList<TFNClass>();
				for(TFNClass cClass: classList) {
					if(cClass.symName.trim().equalsIgnoreCase(defTemp))
						tempList.add(0, cClass);
					else if(cClass.symName.trim().equalsIgnoreCase("KOC Document"))
						continue;
					else
						tempList.add(cClass);
				}
			}
			return Response.ok().entity(tempList).build();
		}catch (Exception e) {
			logger.logException(e);
			ResponseObject responseObject = ResponseObject.getResponseObject(e.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject).build();
		}
	}
	
	// http://localhost:9080/ECMService/resources/ContentService/getEntryTemplatesForSearch?empNo=1002
	@GET
	@Path("/getEntryTemplatesForSearch")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getEntryTemplatesForSearch(@QueryParam("empNo") String empno, @Context HttpServletRequest req,@Context HttpServletResponse resp) throws Exception{
		try{
			FNObjectStore os = FNObjectStoreList.getInstance().getApplicationObjectStore("ECM", "ECMOS");
			long empNo = RequestHelper.getLoggedInEmployee(req, empno);
			ArrayList<TFNClass> classList = FNEntryTemplateList.getInstance(os).getEntryTemplates(empNo);
			
			String defTemp = ECMUserList.getInstance().getUserSetting(empNo, "ECM", "Default Search");
			ArrayList<TFNClass> tempList = classList;
			
			if((defTemp == null) || (defTemp.trim().length() <= 0)) {
				ECMConfigurationList cList = ECMConfigurationList.getInstance("ECM", "APP");
				String sysDefTemp = cList.getConfigValue("DEFAULTSEARCH");
				if((sysDefTemp != null) && (sysDefTemp.trim().length() > 0))
					defTemp = sysDefTemp;
				else
					defTemp = "KOC Document";
			}
			if((defTemp != null) && (defTemp.trim().length() > 0)) {
				tempList = new ArrayList<TFNClass>();
				for(TFNClass cClass: classList) {
					if(cClass.symName.trim().equalsIgnoreCase(defTemp))
						tempList.add(0, cClass);
					else
						tempList.add(cClass);
				}
			}
			return Response.ok().entity(tempList).build();
		}catch (Exception e) {
			logger.logException(e);
			ResponseObject responseObject = ResponseObject.getResponseObject(e.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject).build();
		}
	}
	
	// http://localhost:9080/ECMService/resources/ContentService/getAllEntryTemplates
	@GET
	@Path("/getAllEntryTemplates")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAllEntryTemplates(@Context HttpServletRequest req,@Context HttpServletResponse resp) throws Exception{
		try{
			FNObjectStore os = FNObjectStoreList.getInstance().getApplicationObjectStore("ECM", "ECMOS");
			ArrayList<TFNClass> classList = FNEntryTemplateList.getInstance(os).getAllEntryTemplates();
			return Response.ok().entity(classList).build();
		}catch (Exception e) {
			logger.logException(e);
			ResponseObject responseObject = ResponseObject.getResponseObject(e.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject).build();
		}
	}
			
	// http://localhost:9080/ECMService/resources/ContentService/getSearchTemplates
	@GET
	@Path("/getSearchTemplates")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getSearchTemplates(@Context HttpServletRequest req,@Context HttpServletResponse resp) throws Exception{
		try{
			FNObjectStore os = FNObjectStoreList.getInstance().getApplicationObjectStore("ECM", "ECMOS");
			ArrayList<TFNClass> classList = FNSearchTemplateList.getInstance(os).getSearchTemplates();
				
			return Response.ok().entity(classList).build();
		}catch (Exception e) {
			logger.logException(e);
			ResponseObject responseObject = ResponseObject.getResponseObject(e.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject).build();
		}
	}
			
	// http://localhost:9080/ECMService/resources/ContentService/getEntryTemplate?id={E08DDEF8-A409-4997-8B61-41FAEDD421CE}
	@GET
	@Path("/getEntryTemplate")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getEntryTemplate(@QueryParam("id") String id, @QueryParam("empNo") String empNo,
			@Context HttpServletRequest req, @Context HttpServletResponse resp)
			throws Exception {
		try {
			FNObjectStore os = FNObjectStoreList.getInstance().getApplicationObjectStore("ECM", "ECMOS");
			FNEntryTemplate et = FNEntryTemplate.getInstance(os);
			et.setId(id);
			et.setEmployeeNo(RequestHelper.getLoggedInEmployee(req, empNo));
			
			return Response.ok().entity(et.getTransport(true)).build();
		} catch (Exception e) {
			logger.logException(e);
			ResponseObject responseObject = ResponseObject.getResponseObject(e.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject)
					.build();
		}
	}
	
	// http://localhost:9080/ECMService/resources/ContentService/getEntryTemplateForSearch?id={E08DDEF8-A409-4997-8B61-41FAEDD421CE}
	@GET
	@Path("/getEntryTemplateForSearch")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getEntryTemplateForSearch(@QueryParam("id") String id, @QueryParam("empNo") String empNo,
			@Context HttpServletRequest req, @Context HttpServletResponse resp)
			throws Exception {
		try {
			FNObjectStore os = FNObjectStoreList.getInstance().getApplicationObjectStore("ECM", "ECMOS");
			FNEntryTemplate et = FNEntryTemplate.getInstance(os);
			et.setId(id);
			et.setEmployeeNo(RequestHelper.getLoggedInEmployee(req, empNo));
			
			return Response.ok().entity(et.getTransportForSearch(true)).build();
		} catch (Exception e) {
			logger.logException(e);
			ResponseObject responseObject = ResponseObject.getResponseObject(e.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject)
					.build();
		}
	}
			
	// http://localhost:9080/ECMService/resources/ContentService/getEntryTemplateByName?id={E08DDEF8-A409-4997-8B61-41FAEDD421CE}
	@GET
	@Path("/getEntryTemplateByName")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getEntryTemplateByName(@QueryParam("id") String id, @QueryParam("empName") String empName,
			@Context HttpServletRequest req, @Context HttpServletResponse resp)
			throws Exception {
		try {
			FNObjectStore os = FNObjectStoreList.getInstance().getApplicationObjectStore("ECM", "ECMOS");
			
			long empNo = ECMUserList.getInstance().getEmployee(empName);
			FNEntryTemplate et = FNEntryTemplate.getInstance(os);
			et.setId(id);
			et.setEmployeeNo(empNo);
			
			return Response.ok().entity(et.getTransport(true)).build();
		} catch (Exception e) {
			logger.logException(e);
			ResponseObject responseObject = ResponseObject.getResponseObject(e.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject)
					.build();
		}
	}
	
	// http://localhost:9080/ECMService/resources/ContentService/getSearchTemplate?id={E08DDEF8-A409-4997-8B61-41FAEDD421CE}
	@GET
	@Path("/getSearchTemplate")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getSearchTemplate(@QueryParam("id") String id,
			@Context HttpServletRequest req, @Context HttpServletResponse resp)
			throws Exception {
		try {
			FNObjectStore os = FNObjectStoreList.getInstance().getApplicationObjectStore("ECM", "ECMOS");
			FNSearchTemplate st = FNSearchTemplate.getInstance(os);
			st.setId(id);
			
			return Response.ok().entity(st.getTransport(true)).build();
		} catch (Exception e) {
			logger.logException(e);
			ResponseObject responseObject = ResponseObject.getResponseObject(e.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject)
					.build();
		}
	}
	
	// http://localhost:9080/ECMService/resources/ContentService/addFolderToFavorites?empno=1234&id={2B0B8939-BCCF-4914-BC62-24A1A1635078}
	@GET
	@Path("/addFolderToFavorites")
	@Produces(MediaType.APPLICATION_JSON)
	public Response addFolderToFavorites(@QueryParam("empno") String empNo, @QueryParam("id") String id,
			@Context HttpServletRequest req, @Context HttpServletResponse resp)
			throws Exception {
		try {
			long empNum = RequestHelper.getLoggedInEmployee(req, empNo);
			FNObjectStore os = FNObjectStoreList.getInstance().getApplicationObjectStore("ECM", "ECMOS");
			FNFolderList.getInstance(os).addFolderToFavorites(empNum, id);			
			return Response.ok().entity("OK").build();
		} catch (Exception e) {
			logger.logException(e);
			ResponseObject responseObject = ResponseObject.getResponseObject(e.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject)
					.build();
		}
	}
		
	// http://localhost:9080/ECMService/resources/ContentService/removeFolderFromFavorites?empno=1234&id={2B0B8939-BCCF-4914-BC62-24A1A1635078}
	@GET
	@Path("/removeFolderFromFavorites")
	@Produces(MediaType.APPLICATION_JSON)
	public Response removeFolderFromFavorites(@QueryParam("empno") String empNo, @QueryParam("id") String id,
			@Context HttpServletRequest req, @Context HttpServletResponse resp)
			throws Exception {
		try {
			long empNum = RequestHelper.getLoggedInEmployee(req, empNo);
			FNObjectStore os = FNObjectStoreList.getInstance().getApplicationObjectStore("ECM", "ECMOS");
			FNFolderList.getInstance(os).removeFolderFromFavorites(empNum, id);			
			return Response.ok().entity("OK").build();
		} catch (Exception e) {
			logger.logException(e);
			ResponseObject responseObject = ResponseObject.getResponseObject(e.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject)
					.build();
		}
	}

	// http://localhost:9080/ECMService/resources/ContentService/getFavoriteFolders?empno=1234
	@GET
	@Path("/getFavoriteFolders")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getFavoriteFolders(@QueryParam("empno") String empNo,
			@Context HttpServletRequest req, @Context HttpServletResponse resp)
			throws Exception {
		try {
			long empNum = RequestHelper.getLoggedInEmployee(req, empNo);
			FNObjectStore os = FNObjectStoreList.getInstance().getApplicationObjectStore("ECM", "ECMOS");
			ArrayList<TFNFolder> favs = FNFolderList.getInstance(os).getFavoriteFolders(empNum);
			return Response.ok().entity(favs).build();
		} catch (Exception e) {
			logger.logException(e);
			ResponseObject responseObject = ResponseObject.getResponseObject(e.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject).build();		}
	}
		
	// http://localhost:9080/ECMService/resources/ContentService/MoveToFolder?sourceid={2B0B8939-BCCF-4914-BC62-24A1A1635078}&targetid={2B0B8939-BCCF-4914-BC62-24A1A1635078}
	@GET
	@Path("/moveToFolder")
	@Produces(MediaType.APPLICATION_JSON)
	public Response moveToFolder(@QueryParam("sourceid") String sourceId, @QueryParam("targetid") String targetId,
			@Context HttpServletRequest req, @Context HttpServletResponse resp)
			throws Exception {
		try {
			FNObjectStore os = FNObjectStoreList.getInstance().getApplicationObjectStore("ECM", "ECMOS");
			String result = FNFolder.getInstance(os).moveToFolder(sourceId, targetId);
			return Response.ok().entity(result.toUpperCase()).build();
				
		} catch (Exception e) {
			logger.logException(e);
			ResponseObject responseObject = ResponseObject.getResponseObject(e.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject)
					.build();
		}
	}
	
	// http://localhost:9080/ECMService/resources/ContentService/getOrgDocuments
	@GET
	@Path("/getOrgDocuments")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getOrgDocuments(@QueryParam("orgcode") String orgCode, 
			@Context HttpServletRequest req,
			@Context HttpServletResponse resp) throws Exception {
		try {
			FNObjectStore os = FNObjectStoreList.getInstance().getApplicationObjectStore("ECM", "ECMOS");
			ArrayList<String> docList = FNDocumentSearch.getInstance(os).getOrgDocumentIDs(orgCode);
			
			return Response.ok().entity(docList).build();

		} catch (Exception e) {
			logger.logException(e);
			ResponseObject responseObject = ResponseObject.getResponseObject(e.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject).build();
		}
	}
	
	// http://localhost:9080/ECMService/resources/ContentService/convertOrgDocuments?neworgcode=TK01&oldorgcode=EI71
	@GET
	@Path("/convertOrgDocuments")
	@Produces(MediaType.APPLICATION_JSON)
	public Response convertOrgDocuments(@QueryParam("oldorgcode") String oldOrgCode, 
			@QueryParam("neworgcode") String newOrgCode, 
			@Context HttpServletRequest req,
			@Context HttpServletResponse resp) throws Exception {
		try {
			FNObjectStore os = FNObjectStoreList.getInstance().getApplicationObjectStore("ECM", "ECMOS");
			MigrationHelper.getInstance().convertOrgDocuments(os, oldOrgCode, newOrgCode);
			
			return Response.ok().entity("OK").build();

		} catch (Exception e) {
			logger.logException(e);
			ResponseObject responseObject = ResponseObject.getResponseObject(e.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject).build();
		}
	}

	@Override
	public Set<Class<?>> getClasses() {
		// TODO Auto-generated method stub
		return null;
	}
		
}
