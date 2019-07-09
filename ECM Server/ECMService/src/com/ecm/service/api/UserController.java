package com.ecm.service.api;

import java.util.ArrayList;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.ecm.db.list.ECMRoleList;
import com.ecm.db.list.ECMUserList;
import com.ecm.db.transport.TAdminUser;
import com.ecm.db.transport.TDelegate;
import com.ecm.db.transport.TOrgUnit;
import com.ecm.db.transport.TReportUserSearch;
import com.ecm.db.transport.TRole;
import com.ecm.db.transport.TUser;
import com.ecm.db.transport.TUserList;
import com.ecm.db.transport.TRoleMember;
import com.ecm.db.transport.TUserSearch;
import com.ecm.db.transport.TUserSetting;
import com.ecm.db.util.DBUtil;
import com.ecm.db.util.ECMAdminLogger;
import com.ecm.db.util.ECMLogger;
import com.ecm.db.util.RequestHelper;
import com.ecm.service.object.JSONHelper;
import com.ecm.service.object.ResponseObject;
import com.ecm.filenet.model.FNAuthenticator;

@Path("/UserService")
@ApplicationPath("resources")
public class UserController extends Application {
	private static ECMLogger logger = ECMLogger
			.getInstance(UserController.class);

	// http://localhost:9080/ECMService/resources/UserService/getUserDetails?userid=fatima
	@GET
	@Path("/getUserDetails")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getUserDetails(@QueryParam("userid") String user_login,
			@Context HttpServletRequest req, @Context HttpServletResponse resp)
					throws Exception {
		try {
			String rUser = req.getRemoteUser();
			if (rUser == null)
				logger.info("R User: " + rUser);
			else
				logger.info("R User is NULL");
			user_login = RequestHelper.getLoggedInUser(req, user_login);
			logger.info("User Login: " + user_login);
			FNAuthenticator.Authenticate(user_login);
			ECMUserList.getInstance().refreshDelegation(0);
			TUser user = ECMUserList.getInstance().getUserDetails(user_login);
			if (user != null)
				logger.info(user.toString());
			else
				logger.info("User is NULL");
			return Response.ok().entity(user).build();
		} catch (Exception e) {
			logger.logException(e);
			ResponseObject responseObject = ResponseObject.getResponseObject(e
					.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject)
					.build();
		}
	}
	
	// http://localhost:9080/ECMService/resources/UserService/getUserRoles?userid=fatima
	@GET
	@Path("/getUserRoles")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getUserRoles(@QueryParam("userid") String user_login,
			@Context HttpServletRequest req, @Context HttpServletResponse resp)
					throws Exception {
		try {
			logger.info("User Login: " + user_login);

			TUser user = ECMUserList.getInstance().getUserDetails(user_login);
			if (user != null)
				logger.info(user.toString());
			else
				logger.info("User is NULL");
			return Response.ok().entity(user).build();
		} catch (Exception e) {
			logger.logException(e);
			ResponseObject responseObject = ResponseObject.getResponseObject(e
					.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject)
					.build();
		}
	}
	
	// http://localhost:9080/ECMService/resources/UserService/getUserOrgCode?empName=fatima
	@GET
	@Path("/getUserOrgCode")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getUserOrgCode(@QueryParam("empName") String user_login,
			@Context HttpServletRequest req, @Context HttpServletResponse resp)
					throws Exception {
		try {
			
			String rUser = "";
			if(user_login == null)
			{
				rUser = req.getRemoteUser();
				if (rUser == null)
					logger.info("R User: " + rUser);
				else
					logger.info("R User is NULL");
				
				user_login = RequestHelper.getLoggedInUser(req, user_login);
			}			
			logger.info("User Login: " + user_login);

			long empNo = ECMUserList.getInstance().getEmployee(user_login);
			String orgCode = ECMUserList.getInstance().getOrgCode(empNo);
			if (orgCode != null)
				logger.info("orgCode :: " + orgCode.toString());
			else
				logger.info("orgCode is NULL");
			return Response.ok().entity(orgCode).build();
		} catch (Exception e) {
			logger.logException(e);
			ResponseObject responseObject = ResponseObject.getResponseObject(e
					.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject)
					.build();
		}
	}
	

	// http://localhost:9080/ECMService/resources/UserService/getListUsers?empno=1003&list=12
	@GET
	@Path("/getListUsers")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getListUsers(@QueryParam("empno") String empNo,
			@QueryParam("list") String listId, @Context HttpServletRequest req,
			@Context HttpServletResponse resp) throws Exception {
		try {
			ArrayList<TUser> users = ECMUserList.getInstance().getUserList(
					DBUtil.stringToLong(empNo), DBUtil.stringToLong(listId));

			return Response.ok().entity(users).build();
		} catch (Exception e) {
			logger.logException(e);
			ResponseObject responseObject = ResponseObject.getResponseObject(e
					.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject)
					.build();
		}
	}

	// http://localhost:9080/ECMService/resources/UserService/removeUserList?userid=1003&removeUser=1002&type=fav
	@GET
	@Path("/removeUserList")
	@Produces(MediaType.APPLICATION_JSON)
	public Response removeUserList(@QueryParam("empno") Long empNo,
			@QueryParam("list") Long listId, @Context HttpServletRequest req,
			@Context HttpServletResponse resp) throws Exception {
		try {
			ECMUserList.getInstance().removeUserList(empNo, listId);

			return Response.ok().entity("OK").build();
		} catch (Exception e) {
			logger.logException(e);
			ResponseObject responseObject = ResponseObject.getResponseObject(e
					.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject)
					.build();
		}
	}

	// http://localhost:9080/ECMService/resources/UserService/getRoles?userid=fatima
	@GET
	@Path("/getRoles")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getRoles(@QueryParam("userid") String user_login,
			@Context HttpServletRequest req, @Context HttpServletResponse resp)
					throws Exception {
		try {
			FNAuthenticator.Authenticate(user_login);

			ArrayList<TRole> roles = ECMRoleList.getInstance().getActiveRoles();
			return Response.ok().entity(roles).build();
		} catch (Exception e) {
			logger.logException(e);
			ResponseObject responseObject = ResponseObject.getResponseObject(e
					.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject)
					.build();
		}
	}
	
	// http://localhost:9080/ECMService/resources/UserService/getRolesByType?userid=fatima&type=3
	@GET
	@Path("/getRolesByType")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getRolesByType(@QueryParam("userid") String user_login, @QueryParam("type") String type,
			@Context HttpServletRequest req, @Context HttpServletResponse resp)
					throws Exception {
		try {
			FNAuthenticator.Authenticate(user_login);

			ArrayList<TRole> roles = ECMRoleList.getInstance().getActiveRolesByType(type);
			return Response.ok().entity(roles).build();
		} catch (Exception e) {
			logger.logException(e);
			ResponseObject responseObject = ResponseObject.getResponseObject(e
					.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject)
					.build();
		}
	}
	
	// http://localhost:9080/ECMService/resources/UserService/getRolesByOrgCode?empno=179192
	@GET
	@Path("/getRolesByOrgCode")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getRolesByOrgCode(@QueryParam("empno") String empNo,
			@Context HttpServletRequest req, @Context HttpServletResponse resp)
					throws Exception {
		try {
			//FNAuthenticator.Authenticate(user_login);
			long empNum = RequestHelper.getLoggedInEmployee(req, empNo);
			ArrayList<TRole> roles = ECMRoleList.getInstance().getActiveRolesByOrgCode(empNum);
			return Response.ok().entity(roles).build();
		} catch (Exception e) {
			logger.logException(e);
			ResponseObject responseObject = ResponseObject.getResponseObject(e
					.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject)
					.build();
		}
	}

	// http://localhost:9080/ECMService/resources/UserService/getOrgRoles
	@GET
	@Path("/getOrgRoles")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getOrgRoles(@Context HttpServletRequest req,
			@Context HttpServletResponse resp) throws Exception {
		try {

			ArrayList<TOrgUnit> roles = ECMRoleList.getInstance()
					.getOrganizationRoles();
			return Response.ok().entity(roles).build();
		} catch (Exception e) {
			logger.logException(e);
			ResponseObject responseObject = ResponseObject.getResponseObject(e
					.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject)
					.build();
		}
	}

	// http://localhost:9080/ECMService/resources/UserService/getUsers
	@GET
	@Path("/getUsers")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getUsers(@Context HttpServletRequest req,
			@Context HttpServletResponse resp) throws Exception {
		try {
			ArrayList<TUser> users = ECMUserList.getInstance().getUsers();
			return Response.ok().entity(users).build();
		} catch (Exception e) {
			logger.logException(e);
			ResponseObject responseObject = ResponseObject.getResponseObject(e
					.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject)
					.build();
		}
	}
	
	// http://localhost:9080/ECMService/resources/UserService/getReportUsers
	@GET
	@Path("/getReportUsers")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getReportUsers(@Context HttpServletRequest req,
			@Context HttpServletResponse resp) throws Exception {
		try {
			ArrayList<TUser> users = ECMUserList.getInstance().getReportUsers();
			return Response.ok().entity(users).build();
		} catch (Exception e) {
			logger.logException(e);
			ResponseObject responseObject = ResponseObject.getResponseObject(e
					.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject)
					.build();
		}
	}
	
	// http://localhost:9080/ECMService/resources/UserService/getExcludedUsers
	@GET
	@Path("/getExcludedUsers")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getExcludedUsers(@Context HttpServletRequest req,
			@Context HttpServletResponse resp) throws Exception {
		try {
			ArrayList<TUser> users = ECMUserList.getInstance().getExcludedUsers();
			return Response.ok().entity(users).build();
		} catch (Exception e) {
			logger.logException(e);
			ResponseObject responseObject = ResponseObject.getResponseObject(e
					.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject)
					.build();
		}
	}
	
	// http://localhost:9080/ECMService/resources/UserService/getAdminUsers
	@GET
	@Path("/getAdminUsers")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAdminUsers(@Context HttpServletRequest req,
			@Context HttpServletResponse resp) throws Exception {
		try {
			ArrayList<TUser> users = ECMUserList.getInstance().getAdminUsers();
			return Response.ok().entity(users).build();
		} catch (Exception e) {
			logger.logException(e);
			ResponseObject responseObject = ResponseObject.getResponseObject(e
					.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject)
					.build();
		}
	}

	// http://localhost:9080/ECMService/resources/UserService/searchUsers?key=NAME&text=fatima&usertype=USER
	@GET
	@Path("/searchUsers")
	@Produces(MediaType.APPLICATION_JSON)
	public Response searchUsers(@QueryParam("key") String searchKey,
			@QueryParam("text") String searchText, @QueryParam("filter") String searchFilter,
			@QueryParam("usertype") String searchType,
			@Context HttpServletRequest req, @Context HttpServletResponse resp)
					throws Exception {
		try {

			if (searchType == null)
				searchType = "USER";
			if (searchType.equalsIgnoreCase("ROLE")) {
				ArrayList<TRole> roles = ECMRoleList.getInstance()
						.searchRolesByName(searchKey, searchText, searchFilter);
				return Response.ok().entity(roles).build();
			} else {
				ArrayList<TUser> users = ECMUserList.getInstance().searchUsers(
						searchKey, searchText, searchFilter);
				return Response.ok().entity(users).build();
			}
		} catch (Exception e) {
			logger.logException(e);
			ResponseObject responseObject = ResponseObject.getResponseObject(e
					.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject)
					.build();
		}
	}
	
	// http://localhost:9080/ECMService/resources/UserService/searchECMUsers
	@POST
	@Path("/searchECMUsers")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response searchECMUsers(String jsonString,
			@Context HttpServletRequest req, @Context HttpServletResponse resp)
					throws Exception {
		try {
			TUserSearch sc = JSONHelper.getInstance().getUserSearchCriteria(jsonString);
			String searchType = sc.userType;
			if (searchType == null)
				searchType = "USER";
			if (searchType.equalsIgnoreCase("ROLE")) {
				ArrayList<TRole> roles = ECMRoleList.getInstance().searchECMRolesByName(sc, sc.filter);
				return Response.ok().entity(roles).build();
			} else {
				ArrayList<TUser> users = ECMUserList.getInstance().searchECMUsers(sc, sc.filter);
				return Response.ok().entity(users).build();
			}
		} catch (Exception e) {
			logger.logException(e);
			ResponseObject responseObject = ResponseObject.getResponseObject(e
					.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject)
					.build();
		}
	}
	
	// http://localhost:9080/ECMService/resources/UserService/searchOrgECMUsers
	@POST
	@Path("/searchOrgECMUsers")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response searchOrgECMUsers(String jsonString,
			@Context HttpServletRequest req, @Context HttpServletResponse resp)
					throws Exception {
		try {
			TReportUserSearch sc = JSONHelper.getInstance().getReportUserSearchCriteria(jsonString);
			ArrayList<TUser> users = ECMUserList.getInstance().searchECMUsersByOrgCode(sc, sc.filter);
			return Response.ok().entity(users).build();

		} catch (Exception e) {
			logger.logException(e);
			ResponseObject responseObject = ResponseObject.getResponseObject(e
					.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject)
					.build();
		}
	}

	// http://localhost:9080/ECMService/resources/UserService/getUserSettings?empNo=1001&appid=ECM
	@GET
	@Path("/getUserSettings")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getUserSettings(@QueryParam("empNo") String empno,
			@QueryParam("appid") String appId, @Context HttpServletRequest req,
			@Context HttpServletResponse resp) throws Exception {
		try {

			FNAuthenticator.Authenticate(empno);
			ArrayList<TUserSetting> sList = ECMUserList.getInstance()
					.getUserSettings(DBUtil.stringToLong(empno), appId);

			return Response.ok().entity(sList).build();
		} catch (Exception e) {
			logger.logException(e);
			ResponseObject responseObject = ResponseObject.getResponseObject(e
					.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject)
					.build();
		}
	}
	
	// http://localhost:9080/ECMService/resources/UserService/getUserSearches?empNo=1001&appid=ECM
	@GET
	@Path("/getUserSearches")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getUserSearches(@QueryParam("empNo") String empno,
			@QueryParam("appid") String appId, @Context HttpServletRequest req,
			@Context HttpServletResponse resp) throws Exception {
		try {

			FNAuthenticator.Authenticate(empno);
			ArrayList<TUserSetting> sList = ECMUserList.getInstance()
					.getUserSearches(DBUtil.stringToLong(empno), appId);

			return Response.ok().entity(sList).build();
		} catch (Exception e) {
			logger.logException(e);
			ResponseObject responseObject = ResponseObject.getResponseObject(e
					.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject)
					.build();
		}
	}

	// http://localhost:9080/ECMService/resources/UserService/getUserSetting?empNo=1001&appid=ECM&key=pagesize
	@GET
	@Path("/getUserSetting")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getUserSetting(@QueryParam("empNo") String empno,
			@QueryParam("appid") String appId,
			@QueryParam("key") String keyName, @Context HttpServletRequest req,
			@Context HttpServletResponse resp) throws Exception {
		try {

			FNAuthenticator.Authenticate(empno);
			String sValue = ECMUserList.getInstance().getUserSetting(
					DBUtil.stringToLong(empno), appId, keyName);

			return Response.ok().entity(sValue).build();
		} catch (Exception e) {
			logger.logException(e);
			ResponseObject responseObject = ResponseObject.getResponseObject(e
					.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject)
					.build();
		}
	}

	// http://localhost:9080/ECMService/resources/UserService/saveDelegation
	@POST
	@Path("/saveDelegation")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response saveDelegation(String jsonString,
			@Context HttpServletRequest req, @Context HttpServletResponse resp)
					throws Exception {
		try {
			logger.debug("getDelegate: " + jsonString);
			TDelegate td = JSONHelper.getInstance().getDelegate(jsonString);
			String updateStatus = ECMUserList.getInstance().updateDelegation(td);
			return Response.ok().entity(updateStatus.toUpperCase()).build();
		} catch (Exception e) {
			logger.logException(e);
			e.printStackTrace();
			throw new Exception(e.getMessage());
		}
	}

	// http://localhost:9080/ECMService/resources/UserService/getUserDelegations?userid=1001&usertype=USER
	@GET
	@Path("/getUserDelegations")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getUserDelegations(@QueryParam("userid") String userid,
			@QueryParam("usertype") String userType,
			@Context HttpServletRequest req, @Context HttpServletResponse resp)
					throws Exception {
		try {

			ArrayList<TDelegate> dList = ECMUserList.getInstance()
					.getDelegates(DBUtil.stringToLong(userid), userType);
			return Response.ok().entity(dList).build();
		} catch (Exception e) {
			logger.logException(e);
			ResponseObject responseObject = ResponseObject.getResponseObject(e
					.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject)
					.build();
		}
	}
	
	// http://localhost:9080/ECMService/resources/UserService/validateDelegation?delid=1001
	@GET
	@Path("/validateDelegation")
	@Produces(MediaType.APPLICATION_JSON)
	public Response validateDelegation(@QueryParam("delid") String delId,
			@Context HttpServletRequest req, @Context HttpServletResponse resp)
					throws Exception {
		try {

			String validateMsg = ECMUserList.getInstance().validateDelegate(DBUtil.stringToLong(delId));
			return Response.ok().entity(validateMsg).build();
		} catch (Exception e) {
			logger.logException(e);
			ResponseObject responseObject = ResponseObject.getResponseObject(e
					.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject)
					.build();
		}
	}

	// http://localhost:9080/ECMService/resources/UserService/getDelegationsBy?empno=1001
	@GET
	@Path("/getDelegationsBy")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getDelegationsBy(@QueryParam("empno") String empNo,
			@Context HttpServletRequest req, @Context HttpServletResponse resp)
					throws Exception {
		try {

			ArrayList<TDelegate> dList = ECMUserList.getInstance()
					.getDelegatesBy(DBUtil.stringToLong(empNo));
			return Response.ok().entity(dList).build();
		} catch (Exception e) {
			logger.logException(e);
			ResponseObject responseObject = ResponseObject.getResponseObject(e
					.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject)
					.build();
		}
	}

	// http://localhost:9080/ECMService/resources/UserService/revokeDelegation?id=10
	@GET
	@Path("/revokeDelegation")
	@Produces(MediaType.APPLICATION_JSON)
	public Response revokeDelegation(@QueryParam("id") String did,
			@Context HttpServletRequest req, @Context HttpServletResponse resp)
					throws Exception {
		try {
			ECMUserList.getInstance()
			.revokeDelegation(DBUtil.stringToLong(did));
			return Response.ok().entity("OK").build();
		} catch (Exception e) {
			logger.logException(e);
			ResponseObject responseObject = ResponseObject.getResponseObject(e
					.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject)
					.build();
		}
	}
	
	// http://localhost:9080/ECMService/resources/UserService/sendNotifications
	@GET
	@Path("/sendNotifications")
	@Produces(MediaType.APPLICATION_JSON)
	public Response sendNotifications(@Context HttpServletRequest req, @Context HttpServletResponse resp)
			throws Exception {
		try {
			ECMUserList.getInstance().sendEmailNotifications();
			return Response.ok().entity("OK").build();
		} catch (Exception e) {
			ResponseObject responseObject = ResponseObject.getResponseObject(e.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject).build();
		}
	}

	// http://localhost:9080/ECMService/resources/UserService/addUserToRole?empNo=1003&roleId=1
	@GET
	@Path("/addUserToRole")
	@Produces(MediaType.APPLICATION_JSON)
	public Response addUserToRole(@QueryParam("empNo") String empNo,
			@QueryParam("roleId") String roleId,
			@Context HttpServletRequest req, @Context HttpServletResponse resp)
					throws Exception {
		try {
			ECMRoleList.getInstance().addUserToRole(DBUtil.stringToLong(empNo),
					DBUtil.stringToLong(roleId), "ORG");
			String empName = RequestHelper.getLoggedInEmpName(req, "");
			ECMAdminLogger.getInstance("ECMRoles").info("ECM Roles", empName, "User - " + 
			ECMUserList.getInstance().getUserFullName(DBUtil.stringToLong(empNo)) + 
			" is added to ECM Role with Id - " + roleId);
			return Response.ok().entity("OK").build();
		} catch (Exception e) {
			logger.logException(e);
			ResponseObject responseObject = ResponseObject.getResponseObject(e
					.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject)
					.build();
		}
	}
	
	// http://localhost:9080/ECMService/resources/UserService/deleteRole?roleId=1
	@GET
	@Path("/deleteRole")
	@Produces(MediaType.APPLICATION_JSON)
	public Response deleteRole(@QueryParam("roleId") String roleId,
			@Context HttpServletRequest req, @Context HttpServletResponse resp)
			throws Exception {
		try {
			ECMRoleList.getInstance().deleteRole(DBUtil.stringToLong(roleId));
			String empName = RequestHelper.getLoggedInEmpName(req, "");
			ECMAdminLogger.getInstance("ECMRoles").info("ECM Roles", empName, "ECM Role with Id - " 
					+ roleId + " is deleted." );
			return Response.ok().entity("OK").build();
		} catch (Exception e) {
			logger.logException(e);
			ResponseObject responseObject = ResponseObject.getResponseObject(e
					.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject)
					.build();
		}
	}

	// http://localhost:9080/ECMService/resources/UserService/removeUserFromRole?empNo=1003&roleId=1
	@GET
	@Path("/removeUserFromRole")
	@Produces(MediaType.APPLICATION_JSON)
	public Response removeUserFromRole(@QueryParam("empNo") String empNo,
			@QueryParam("roleId") String roleId,
			@Context HttpServletRequest req, @Context HttpServletResponse resp)
					throws Exception {
		try {
			
			ECMRoleList.getInstance().removeUserFromRole(
					DBUtil.stringToLong(empNo), DBUtil.stringToLong(roleId),
					"ORG");

			return Response.ok().entity("OK").build();
		} catch (Exception e) {
			logger.logException(e);
			ResponseObject responseObject = ResponseObject.getResponseObject(e
					.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject)
					.build();
		}
	}

	// http://localhost:9080/ECMService/resources/UserService/updateUserSettings
	@POST
	@Path("/updateUserSettings")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response updateUserSettings(String jsonString,
			@Context HttpServletRequest req, @Context HttpServletResponse resp)
					throws Exception {
		try {
			ArrayList<TUserSetting> sList = JSONHelper.getInstance()
					.getUserSettings(jsonString);
			ECMUserList.getInstance().updateUserSettings(sList);
			return Response.ok().entity("OK").build();
		} catch (Exception e) {
			logger.logException(e);
			e.printStackTrace();
			throw new Exception(e.getMessage());
		}
	}
	
	// http://localhost:9080/ECMService/resources/UserService/updateUserSearches
	@POST
	@Path("/updateUserSearches")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response updateUserSearches(String jsonString,
			@Context HttpServletRequest req, @Context HttpServletResponse resp)
					throws Exception {
		try {
			ArrayList<TUserSetting> sList = JSONHelper.getInstance()
					.getUserSettings(jsonString);
			ECMUserList.getInstance().updateUserSearches(sList);
			return Response.ok().entity("OK").build();
		} catch (Exception e) {
			logger.logException(e);
			e.printStackTrace();
			throw new Exception(e.getMessage());
		}
	}

	// http://localhost:9080/ECMService/resources/UserService/updateUserList
	@POST
	@Path("/updateUserList")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response updateUserList(String jsonString,
			@Context HttpServletRequest req, @Context HttpServletResponse resp)
					throws Exception {
		try {
			TUserList uList = JSONHelper.getInstance().getUserList(jsonString);
			ECMUserList.getInstance().updateUserList(uList);
			return Response.ok().entity("OK").build();
		} catch (Exception e) {
			logger.logException(e);
			e.printStackTrace();
			throw new Exception(e.getMessage());
		}
	}
	
	// http://localhost:9080/ECMService/resources/UserService/saveRole
	@POST
	@Path("/saveRole")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response saveRole(String jsonString,
			@Context HttpServletRequest req, @Context HttpServletResponse resp)
					throws Exception {
		try {
			TRole role = JSONHelper.getInstance().getRole(jsonString);
			String empName = RequestHelper.getLoggedInEmpName(req, "");
			ECMRoleList.getInstance().saveRole(role, empName);
			
			
			return Response.ok().entity("OK").build();
		} catch (Exception e) {
			logger.logException(e);
			e.printStackTrace();
			throw new Exception(e.getMessage());
		}
	}
	
	// http://localhost:9080/ECMService/resources/UserService/saveUser
	@POST
	@Path("/saveUser")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response saveUser(String jsonString,
			@Context HttpServletRequest req, @Context HttpServletResponse resp)
					throws Exception {
		try {
			TUser user = JSONHelper.getInstance().getUser(jsonString);
			ECMUserList.getInstance().saveUser(user);
			return Response.ok().entity("OK").build();
		} catch (Exception e) {
			logger.logException(e);
			e.printStackTrace();
			throw new Exception(e.getMessage());
		}
	}
	
	// http://localhost:9080/ECMService/resources/UserService/saveReportUser?empNo=1&id=1
	@GET
	@Path("/saveReportUser")
	@Produces(MediaType.APPLICATION_JSON)
	public Response saveReportUser(@QueryParam("empNo") String empNo,@QueryParam("id") String id,
			@QueryParam("isadmin") String isadmin,@Context HttpServletRequest req, @Context HttpServletResponse resp)
					throws Exception {
		try {
			if(isadmin == null || isadmin == "")
				isadmin = "N";
			String strResult = ECMUserList.getInstance().saveReportUser(DBUtil.stringToLong(empNo),DBUtil.stringToLong(id),isadmin);
			return Response.ok().entity(strResult).build();
		} catch (Exception e) {
			logger.logException(e);
			e.printStackTrace();
			throw new Exception(e.getMessage());
		}
	}
	
	// http://localhost:9080/ECMService/resources/UserService/saveExcludedUser?empNo=1&id=1
	@GET
	@Path("/saveExcludedUser")
	@Produces(MediaType.APPLICATION_JSON)
	public Response saveExcludedUser(@QueryParam("empNo") String empNo,@QueryParam("id") String id,
			@Context HttpServletRequest req, @Context HttpServletResponse resp)
					throws Exception {
		try {
			String strResult = ECMUserList.getInstance().saveExcludedUser(DBUtil.stringToLong(empNo),DBUtil.stringToLong(id));
			return Response.ok().entity(strResult).build();
		} catch (Exception e) {
			logger.logException(e);
			e.printStackTrace();
			throw new Exception(e.getMessage());
		}
	}
	
	// http://localhost:9080/ECMService/resources/UserService/saveAdminUser
	@POST
	@Path("/saveAdminUser")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response saveAdminUser(String jsonString, @Context HttpServletRequest req, @Context HttpServletResponse resp)
					throws Exception {
		try {
			logger.debug("saveAdminUser: " + jsonString);
			TAdminUser Tau = JSONHelper.getInstance().getAdminUser(jsonString);
			String strResult = ECMUserList.getInstance().saveAdminUser(Tau);
			return Response.ok().entity(strResult).build();
		} catch (Exception e) {
			logger.logException(e);
			e.printStackTrace();
			throw new Exception(e.getMessage());
		}
	}

	// http://localhost:9080/ECMService/resources/UserService/getRoleMembers?roleId=1
	@GET
	@Path("/getRoleMembers")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getRoleMembers(@QueryParam("roleId") String roleId,
			@Context HttpServletRequest req, @Context HttpServletResponse resp)
					throws Exception {
		try {
			ECMUserList.getInstance().refreshDelegation(DBUtil.stringToLong(roleId));
			ArrayList<TRoleMember> roles = ECMRoleList.getInstance()
					.getRoleMembers(DBUtil.stringToLong(roleId));
			return Response.ok().entity(roles).build();
		} catch (Exception e) {
			logger.logException(e);
			ResponseObject responseObject = ResponseObject.getResponseObject(e
					.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject)
					.build();
		}
	}

	// http://localhost:9080/ECMService/resources/UserService/getTopOrgRole
	@GET
	@Path("/getTopOrgRole")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getTopOrgRole(@Context HttpServletRequest req,
			@Context HttpServletResponse resp) throws Exception {
		try {
			System.out.println("I am in");
			ArrayList<TOrgUnit> toList = ECMRoleList.getInstance().getTopOrganizationRoles();
			return Response.ok().entity(toList).build();
		} catch (Exception e) {
			logger.logException(e);
			ResponseObject responseObject = ResponseObject.getResponseObject(e
					.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject)
					.build();
		}
	}
	
//	// http://localhost:9080/ECMService/resources/UserService/getTopOrgRole
//	@GET
//	@Path("/getTopOrgRole")
//	@Produces(MediaType.APPLICATION_JSON)
//	public Response getTopOrgRole(@Context HttpServletRequest req,
//			@Context HttpServletResponse resp) throws Exception {
//		try {
//			System.out.println("I am in");
//			TOrgUnit to = ECMRoleList.getInstance().getTopOrganizationRole();
//
//			return Response.ok().entity(to).build();
//		} catch (Exception e) {
//			logger.logException(e);
//			ResponseObject responseObject = ResponseObject.getResponseObject(e
//					.getMessage());
//			return Response.status(Status.BAD_REQUEST).entity(responseObject)
//					.build();
//		}
//	}

	// http://localhost:9080/ECMService/resources/UserService/getSubOrgRoles?orgId=1
	@GET
	@Path("/getSubOrgRoles")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getSubOrgRoles(@QueryParam("orgid") String orgId,
			@Context HttpServletRequest req, @Context HttpServletResponse resp)
					throws Exception {
		try {
			System.out.println("I am in");
			ArrayList<TOrgUnit> toList = ECMRoleList.getInstance()
					.getSubOrganizationRoles(DBUtil.stringToLong(orgId));

			return Response.ok().entity(toList).build();
		} catch (Exception e) {
			logger.logException(e);
			ResponseObject responseObject = ResponseObject.getResponseObject(e
					.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject)
					.build();
		}
	}
	
	// http://localhost:9080/ECMService/resources/UserService/getUserSupervisorTree?empNo=1001
	@GET
	@Path("/getUserSupervisorTree")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getUserSupervisorTree(@QueryParam("empNo") String empNo,
			@Context HttpServletRequest req, @Context HttpServletResponse resp)
					throws Exception {
		try {
			System.out.println("I am in");
			
			ArrayList<TOrgUnit> toList = ECMUserList.getInstance()
					.getUserSupervisorTree(DBUtil.stringToLong(empNo));

			return Response.ok().entity(toList).build();
		} catch (Exception e) {
			logger.logException(e);
			ResponseObject responseObject = ResponseObject.getResponseObject(e
					.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(responseObject)
					.build();
		}
	}
		
	// http://localhost:9080/ECMService/resources/UserService/getUserLists?empno=1003
	@GET
	@Path("/getUserLists")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getUserLists(@QueryParam("empno") String empNo, @QueryParam("global") String global,
			@Context HttpServletRequest req, @Context HttpServletResponse resp)
					throws Exception {
		try {
			ArrayList<TUserList> userList = new ArrayList<TUserList>();
			if(global == null || global.length() <= 0)
				global = "false";
			
			userList = ECMUserList.getInstance().getUserLists(DBUtil.stringToLong(empNo), global);		
			return Response.ok().entity(userList).build();
		} catch (Exception e) {
			logger.logException(e);
			ResponseObject responseObject = ResponseObject.getResponseObject(e
					.getMessage());
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
