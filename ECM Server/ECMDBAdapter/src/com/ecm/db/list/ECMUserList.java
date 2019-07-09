package com.ecm.db.list;

import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

import com.ecm.db.model.ECMDelegation;
import com.ecm.db.model.ECMMailManager;
import com.ecm.db.model.ECMRole;
import com.ecm.db.model.ECMUser;
import com.ecm.db.model.ECMUserSearch;
import com.ecm.db.model.ECMUserSetting;
import com.ecm.db.model.ECMWorkItem;
import com.ecm.db.transport.TAdminEmailSet;
import com.ecm.db.transport.TAdminUser;
import com.ecm.db.transport.TDelegate;
import com.ecm.db.transport.TOrgUnit;
import com.ecm.db.transport.TReportUserSearch;
import com.ecm.db.transport.TRole;
import com.ecm.db.transport.TUser;
import com.ecm.db.transport.TUserList;
import com.ecm.db.transport.TUserSearch;
import com.ecm.db.transport.TUserSetting;
import com.ecm.db.transport.TWorkitemDetails;
import com.ecm.db.util.DBUtil;

public class ECMUserList {

	private ECMUserList() { }

	public static ECMUserList getInstance() {
		return new ECMUserList();
	}
	
	public void updateUserList(TUserList uList) throws Exception {
		Connection conn = null;
		try {
			
			if(uList.isGlobal == null)
				uList.isGlobal = "N";
			
			conn = DBUtil.getECMDBConnection();
			if((uList.id <= 0) && (!uList.name.trim().equalsIgnoreCase("Favourites"))) {
				uList.id = createUserList(conn, uList);	
				if(uList.id <= 0)
					throw new Exception("Could not create list " + uList.name);
			}
			if(uList.id > 0)
				updateUserList(conn, uList);
			
			if(uList.isGlobal.equalsIgnoreCase("Y"))
				updateGlobalListMembers(conn, uList);
			else
				updateUserListMembers(conn, uList);
				
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		} finally {
			conn.close();
		}
	}
	
	private long createUserList(Connection conn, TUserList uList) throws Exception {
		String sqlQuery = null;
		long listId = 0;
		CallableStatement callableStatement = null;
		try {
			String isGlobal = uList.isGlobal;
			if(isGlobal == null || isGlobal.length() <= 0)
				isGlobal = "N";
			
			sqlQuery = "{call ECM_ADD_USERLIST(?,?,?,?)}";
			callableStatement = conn.prepareCall(sqlQuery);
			callableStatement.setInt(1, (int)uList.empNo);
			callableStatement.setString(2, DBUtil.escapeString(uList.name));
			callableStatement.setString(3, DBUtil.escapeString(isGlobal));
			callableStatement.registerOutParameter(4, java.sql.Types.INTEGER);
			callableStatement.execute();
			
			listId = callableStatement.getInt(4);
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			callableStatement.close();
		}
		return listId;
	}
	
	private Boolean sendEmail( long empNo, String name ,TUserList uList){
			
			try {
				ECMMailManager em=new ECMMailManager();
				TAdminEmailSet taes=new TAdminEmailSet();
			    taes.EmpNo=uList.empNo;
			    taes.name=uList.name;
			    //taes.isGlobal=isGlobal;
			    //taes.Types=java.sql.Types.INTEGER;
				em.sendEmail("ECMGlobalList", taes, "Created");
				return true;
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return false;
			}
			
			
		}

	
	
	private long updateUserList(Connection conn, TUserList uList) throws Exception {
		String sqlQuery = null;
		long listId = 0;
		CallableStatement callableStatement = null;
		try {
			String isGlobal = uList.isGlobal;
			if(isGlobal == null || isGlobal.length() <= 0)
				isGlobal = "N";
			
			sqlQuery = "{call ECM_UPDATE_USERLIST(?,?,?,?)}";
			callableStatement = conn.prepareCall(sqlQuery);
			callableStatement.setInt(1, (int)uList.id);
			callableStatement.setString(2, DBUtil.escapeString(uList.name));
			callableStatement.setString(3, DBUtil.escapeString(isGlobal));
			callableStatement.registerOutParameter(4, java.sql.Types.INTEGER);
			callableStatement.execute();
			listId = callableStatement.getInt(4);
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			callableStatement.close();
		}
		return listId;
	}
	
	public void updateUserListMembers(Connection conn, TUserList uList) throws Exception {
		String sqlQuery = null;
		PreparedStatement ps = null;
		if(uList.users == null)
			return;		
		try {
			sqlQuery = "DELETE FROM ECM_USERLIST_MEMBER WHERE EMPNO = ? AND LISTID = ?";
			ps = conn.prepareStatement(sqlQuery);
			ps.setInt(1, (int)uList.empNo);
			ps.setInt(2, (int)uList.id);
			ps.executeUpdate();
			
			for(TUser user: uList.users) {
				ps.close();
				sqlQuery = "INSERT INTO ECM_USERLIST_MEMBER(ID, LISTEDEMPNO, LISTID, EMPNO, TYPE) "
						+ "VALUES(ECM_USERLIST_MEMBER_SEQ.NEXTVAL, ?, ?, ?, ?)";
				ps = conn.prepareStatement(sqlQuery);
				ps.setInt(1,  (int)user.EmpNo);
				ps.setInt(2, (int)uList.id);
				ps.setInt(3, (int)uList.empNo);
				ps.setString(4, DBUtil.escapeString(user.appRole.toUpperCase()));
				ps.executeUpdate();
			}
			if(!(sendEmail("ECMGlobalList", uList, "Updated"))){
				//New method to send email later
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			ps.close();
			conn.close();
		}
	}
	
	public void updateGlobalListMembers(Connection conn, TUserList uList) throws Exception {
		String sqlQuery = null;
		PreparedStatement ps = null;
		if(uList.users == null)
			return;		
		try {
			sqlQuery = "DELETE FROM ECM_USERLIST_MEMBER WHERE LISTID = ?";
			ps = conn.prepareStatement(sqlQuery);
			ps.setInt(1, (int)uList.id);
			ps.executeUpdate();
			for(TUser user: uList.users) {
				ps.close();
				sqlQuery = "INSERT INTO ECM_USERLIST_MEMBER(ID, LISTEDEMPNO, LISTID, EMPNO, TYPE) "
						+ "VALUES(ECM_USERLIST_MEMBER_SEQ.NEXTVAL, ?, ?, ?, ?)";
				ps = conn.prepareStatement(sqlQuery);
				ps.setInt(1,  (int)user.EmpNo);
				ps.setInt(2, (int)uList.id);
				ps.setInt(3, (int)uList.empNo);
				ps.setString(4, DBUtil.escapeString(user.appRole.toUpperCase()));
				ps.executeUpdate();
			}
			if(!(sendEmail("ECMGlobalList", uList, "Inserted"))){
				//New method to send email later
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			ps.close();
			conn.close();
		}
	}
	
	private Boolean sendEmail(String screen, TUserList uList, String action){
		try {
			ECMMailManager em = new ECMMailManager();
			TAdminEmailSet taes = new TAdminEmailSet();
			taes.fullName = ECMUserList.getInstance().getUserFullName(uList.empNo);
			taes.name=uList.name;
			taes.users = uList.users;
			if(uList.isGlobal.equalsIgnoreCase("Y"))
				taes.isGlobal = "Yes";
			else 
				taes.isGlobal = "No";
			em.sendEmail(screen, taes, action);
			
			return true;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}	
	}
	
	public void removeUserList(long empNo, long listId) throws Exception {
		Connection conn = null;
		String sqlQuery = null;
		PreparedStatement ps = null;
		try {
			conn = DBUtil.getECMDBConnection();
			sqlQuery = "DELETE FROM ECM_USERLIST_MEMBER WHERE EMPNO = ? AND ID = ?";
			ps = conn.prepareStatement(sqlQuery);
			ps.setInt(1, (int)empNo);
			ps.setInt(2, (int)listId);
			ps.executeUpdate();
			
			if(listId > 0) {
				ps.close();
				sqlQuery = "DELETE FROM ECM_USERLIST WHERE EMPNO = ? AND ID = ?";
				ps = conn.prepareStatement(sqlQuery);
				ps.setInt(1, (int)empNo);
				ps.setInt(2, (int)listId);
				ps.executeUpdate();
				
				if(!(sendEmail("ECMGlobalList", getUserListDetails(listId,empNo), "Removed"))){
					//New method to send email later
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			ps.close();
			conn.close();
		}
	}
	
	public TUserList getUserListDetails(long listId, long empNo) throws Exception {
		TUserList userList = getUserListByListId(listId);
		userList.empNo = empNo;
		return userList;
	}
	
	public TUserList getUserListByListId(long listId) throws Exception {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		TUserList ul = new TUserList();
		try {
			conn = DBUtil.getECMDBConnection();
			String strSQL = "SELECT * FROM ECM_USERLIST WHERE ID = ?";
			stmt = conn.prepareStatement(strSQL);
			stmt.setInt(1, (int)listId);
			rs = stmt.executeQuery();
			while (rs.next()) {
				ul.name = rs.getString("Name");
				ul.id = rs.getInt("ID");
				ul.isGlobal = rs.getString("ISGLOBAL");
			}

		} catch (Exception e) {
			e.printStackTrace();
			//throw new Exception(e.getMessage());
		} finally {
			rs.close();
			stmt.close();
			conn.close();
		}
		return ul;
	}

	public ArrayList<TUser> searchUsers(String searchKey, String searchText, String searchFilter) throws Exception {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		String filterCondition = null;
		ArrayList<TUser> userList = new ArrayList<TUser>();

		try {
			conn = DBUtil.getECMDBConnection();
			if(searchKey == null)
				searchKey = "ANY";
			
			if(searchFilter != null && searchFilter.length() > 0)
			{
				if(searchFilter.equalsIgnoreCase("esign"))
					filterCondition = "AND ISESIGNALLOWED = 1 AND (ISINITIALALLOWED = 1 OR ISINITIALALLOWED = 0) ";
				else if(searchFilter.equalsIgnoreCase("initial"))
					filterCondition = "AND (ISESIGNALLOWED = 0 OR ISESIGNALLOWED = 1) AND ISINITIALALLOWED = 1 ";
				else
					filterCondition = " ";
			}
			else
				filterCondition = " ";
					
			if(searchKey.trim().equalsIgnoreCase("LOGIN")) {
				stmt = conn.prepareStatement("SELECT * FROM ECM_USER WHERE UPPER(USERNAME) LIKE ? OR UPPER(FULLNAME) LIKE ? "
						+ filterCondition + "AND ROWNUM <= 250 ORDER BY FULLNAME ASC");
				stmt.setString(1, "%" + DBUtil.escapeString(searchText).toUpperCase() + "%");
				stmt.setString(2, "%" + DBUtil.escapeString(searchText).toUpperCase() + "%");
			} else if(searchKey.trim().equalsIgnoreCase("NAME")) {
				stmt = conn.prepareStatement("SELECT * FROM ECM_USER WHERE UPPER(FULLNAME) LIKE ? OR UPPER(USERNAME) LIKE ? "
						+ filterCondition + "AND ROWNUM <= 250 ORDER BY FULLNAME ASC");
				stmt.setString(1, "%" + DBUtil.escapeString(searchText).toUpperCase() + "%");
				stmt.setString(2, "%" + DBUtil.escapeString(searchText).toUpperCase() + "%");
			} else if(searchKey.trim().equalsIgnoreCase("EMAIL")) {
				stmt = conn.prepareStatement("SELECT * FROM ECM_USER WHERE UPPER(MAIL) LIKE ? "
						+ filterCondition + "AND ROWNUM <= 250 ORDER BY FULLNAME ASC");
				stmt.setString(1, "%" + DBUtil.escapeString(searchText).toUpperCase() + "%");
			}else if(searchKey.trim().equalsIgnoreCase("TITLE")) {
				stmt = conn.prepareStatement("SELECT * FROM ECM_USER WHERE UPPER(TITLE) LIKE ? "
						+ filterCondition + "AND ROWNUM <= 250 ORDER BY FULLNAME ASC");
				stmt.setString(1, "%" + DBUtil.escapeString(searchText).toUpperCase() + "%");
			} else if(searchKey.trim().equalsIgnoreCase("PHONE")) {
				stmt = conn.prepareStatement("SELECT * FROM ECM_USER WHERE PHONENO LIKE ? "
						+ filterCondition + "AND ROWNUM <= 250 ORDER BY FULLNAME ASC");
				stmt.setString(1, "%" + DBUtil.escapeString(searchText) + "%");
			} else if(searchKey.trim().equalsIgnoreCase("ORGCODE")) {
				stmt = conn.prepareStatement("SELECT * FROM ECM_USER WHERE UPPER(ORGCODE) = ? "
						+ filterCondition + "AND ROWNUM <= 250 ORDER BY FULLNAME ASC");
				stmt.setString(1, DBUtil.escapeString(searchText).toUpperCase());
			} else if(searchKey.trim().equalsIgnoreCase("KOCNO")) {
				stmt = conn.prepareStatement("SELECT * FROM ECM_USER WHERE UPPER(KOCID) = ? "
						+ filterCondition + "AND ROWNUM <= 250 ORDER BY FULLNAME ASC");
				stmt.setString(1, DBUtil.escapeString(searchText).toUpperCase());
			} else {
				String querySQL = "SELECT * FROM ECM_USER WHERE (UPPER(USERNAME) LIKE ? OR "
						+ "UPPER(FULLNAME) LIKE ? OR UPPER(MAIL) LIKE ? OR UPPER(TITLE) LIKE ? " 
						+ "OR PHONENO LIKE ? OR UPPER(ORGCODE) = ?) "
						+ filterCondition + "AND ROWNUM <= 250 ORDER BY FULLNAME ASC";
				long empNo = DBUtil.stringToLongDefault(searchText, 0);
				if(empNo > 0) 
					querySQL = "SELECT * FROM ECM_USER WHERE (UPPER(USERNAME) LIKE ? OR "
							+ "UPPER(FULLNAME) LIKE ? OR UPPER(MAIL) LIKE ? OR UPPER(TITLE) LIKE ? OR PHONENO LIKE ? OR UPPER(ORGCODE) = ? "
							+ "OR EMPNO LIKE ?) "
							+ filterCondition + "AND ROWNUM <= 250 ORDER BY FULLNAME ASC";
				stmt = conn.prepareStatement(querySQL);
				stmt.setString(1, "%" + DBUtil.escapeString(searchText).toUpperCase() + "%");
				stmt.setString(2, "%" + DBUtil.escapeString(searchText).toUpperCase() + "%");
				stmt.setString(3, "%" + DBUtil.escapeString(searchText) + "%");
				stmt.setString(4, "%" + DBUtil.escapeString(searchText).toUpperCase() + "%");
				stmt.setString(5, DBUtil.escapeString(searchText).toUpperCase());
				stmt.setString(6, "%" + DBUtil.escapeString(searchText).toUpperCase() + "%");
			}
			
			rs = stmt.executeQuery();
			
			while (rs.next()) {
				ECMUser user = new ECMUser();
				user.setUserName(rs.getString("UserName"));
				user.setEmpNo(rs.getInt("EMPNo"));
				user.setFullName(rs.getString("FullName"));
				user.setId(rs.getInt("ID"));
				user.setTitle(rs.getString("Title"));
				user.setMail(rs.getString("Mail"));
				user.setOrgCode(rs.getString("OrgCode"));
				
				userList.add(user.getTransport());
			}
			return userList;

		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			rs.close();
			stmt.close();
			conn.close();
		}
	}
	
	public ArrayList<TUser> searchECMUsers(TUserSearch sc, String searchFilter) throws Exception {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		String filterCondition = null;
		ArrayList<TUser> userList = new ArrayList<TUser>();

		try {
			conn = DBUtil.getECMDBConnection();
			
			if(searchFilter != null && searchFilter.length() > 0)
			{
				if(searchFilter.equalsIgnoreCase("esign"))
					filterCondition = "AND ISESIGNALLOWED = 1 AND (ISINITIALALLOWED = 1 OR ISINITIALALLOWED = 0) ";
				else if(searchFilter.equalsIgnoreCase("initial"))
					filterCondition = "AND (ISESIGNALLOWED = 0 OR ISESIGNALLOWED = 1) AND ISINITIALALLOWED = 1 ";
				else
					filterCondition = " ";
			}
			else
				filterCondition = " ";
			

			String querySQL = "SELECT * FROM ECM_USER WHERE Status = 'ACTIVE' " + getQueryString(sc) 
							  + filterCondition + "ORDER BY FULLNAME ASC";				
			stmt = conn.prepareStatement(querySQL);
			
			rs = stmt.executeQuery();
			
			while (rs.next()) {
				ECMUser user = new ECMUser();
				user.setUserName(rs.getString("UserName"));
				user.setEmpNo(rs.getInt("EMPNo"));
				user.setFullName(rs.getString("FullName"));
				user.setId(rs.getInt("ID"));
				user.setTitle(rs.getString("Title"));
				user.setMail(rs.getString("Mail"));
				user.setOrgCode(rs.getString("OrgCode"));
				user.setKOCId(rs.getString("KOCID"));
				
				userList.add(user.getTransport());
			}
			return userList;

		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			rs.close();
			stmt.close();
			conn.close();
		}
	}
	

	public ArrayList<TUser> searchECMUsersByOrgCode(TReportUserSearch sc, String searchFilter) throws Exception {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		ArrayList<TUser> userList = new ArrayList<TUser>();

		try {
			conn = DBUtil.getECMDBConnection();			
			String orgQuery = " ";
			if(sc.orgCode!=null)
				orgQuery = " AND ORGCODE IN(select ORGCODE from ecm_orgunit A where isparentof(getorgid(?),id) = 'YES')";
			
			String querySQL = "SELECT * FROM ECM_USER WHERE Status = 'ACTIVE' " + getOrgReportQueryString(sc) + orgQuery
							+ " ORDER BY FULLNAME ASC";	
			
			stmt = conn.prepareStatement(querySQL);
			if(sc.orgCode!=null)
				stmt.setString(1, DBUtil.escapeString(sc.orgCode));
			
			rs = stmt.executeQuery();
			
			while (rs.next()) {
				ECMUser user = new ECMUser();
				user.setUserName(rs.getString("UserName"));
				user.setEmpNo(rs.getInt("EMPNo"));
				user.setFullName(rs.getString("FullName"));
				user.setId(rs.getInt("ID"));
				user.setTitle(rs.getString("Title"));
				user.setMail(rs.getString("Mail"));
				user.setOrgCode(rs.getString("OrgCode"));
				user.setKOCId(rs.getString("KOCID"));
				
				userList.add(user.getTransport());
			}
			return userList;

		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			rs.close();
			stmt.close();
			conn.close();
		}
	}

	private String getQueryString(TUserSearch sc) {
		String whereQuery = "";
		if (sc == null)
			return whereQuery;
		
		String propWhere = getStringClauseForName(sc.userName, "USERNAME", "FULLNAME");
		if (propWhere.length() > 0)
			whereQuery += (" AND " + propWhere);
		
		propWhere = getStringClause(sc.mail, "MAIL");
		if (propWhere.length() > 0)
			whereQuery += (" AND " + propWhere);
		
		propWhere = getStringClause(sc.title, "TITLE");
		if (propWhere.length() > 0)
			whereQuery += (" AND " + propWhere);
		
		propWhere = getStringClause(sc.orgCode, "ORGCODE");
		if (propWhere.length() > 0)
			whereQuery += (" AND " + propWhere);
		
		propWhere = getStringClause(sc.phone, "PHONENO");
		if (propWhere.length() > 0)
			whereQuery += (" AND " + propWhere);
		
		if(sc.empNo > 0)
		{
			propWhere = getStringClause(DBUtil.intToString((int)sc.empNo), "KOCID");
			if (propWhere.length() > 0)
				whereQuery += (" AND " + propWhere);
		}
		
		return whereQuery;
	}
	
	private String getOrgReportQueryString(TReportUserSearch sc) {
		String whereQuery = "";
		if (sc == null)
			return whereQuery;
		
		String propWhere = getStringClauseForName(sc.userName, "USERNAME", "FULLNAME");
		if (propWhere.length() > 0)
			whereQuery += (" AND " + propWhere);

		propWhere = getStringClause(sc.empNo, "KOCID");
			if (propWhere.length() > 0)
				whereQuery += (" AND " + propWhere);
			
		return whereQuery;
	}
	
	private String getStringClause(String value, String name) {
		if ((value != null) && (value.length() > 0)){
			return "UPPER(" + name + ") LIKE '%" + DBUtil.escapeString(value).toUpperCase() + "%' ";
		}
		return "";
	}
	
	private String getStringClauseForName(String value, String name, String fullname) {
		if ((value != null) && (value.length() > 0)){
			return "(UPPER(" + name + ") LIKE '%" + DBUtil.escapeString(value).toUpperCase() + "%' OR UPPER(" 
					+ fullname + ") LIKE '%" + DBUtil.escapeString(value).toUpperCase() + "%') ";
		}
		return "";
	}

	@SuppressWarnings({ "unused" })
	private String getIntClause(long value, String name) {
		if (value > 0) {
			if (name != null)
				return name + " = " + value + " ";
		}
		return "";
	}
	
	public ArrayList<TUserList> getUserLists(long empNo, String isGlobal) throws Exception {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		ArrayList<TUserList> userList = new ArrayList<TUserList>();
		TUserList fav = new TUserList();
		fav.id = 0;
		fav.name = "Favourites";
		fav.empNo = empNo;
		userList.add(fav);
		
		try {
			conn = DBUtil.getECMDBConnection();
			String strSQL = "SELECT * FROM ECM_USERLIST WHERE EmpNo = ? AND ISGLOBAL = 'N' ORDER BY NAME ASC";
			if(isGlobal.equalsIgnoreCase("true"))
				strSQL = "SELECT * FROM ECM_USERLIST WHERE EmpNo = ? OR ISGLOBAL = 'Y' ORDER BY ISGLOBAL DESC, NAME ASC";
			stmt = conn.prepareStatement(strSQL);
			stmt.setInt(1, (int)empNo);
			rs = stmt.executeQuery();
			
			while (rs.next()) {
				TUserList ul = new TUserList();
				ul.name = rs.getString("Name");
				ul.id = rs.getInt("ID");
				ul.empNo = empNo;
				ul.isGlobal = rs.getString("ISGLOBAL");
				userList.add(ul);
			}

		} catch (Exception e) {
			e.printStackTrace();
			//throw new Exception(e.getMessage());
		} finally {
			rs.close();
			stmt.close();
			conn.close();
		}
		return userList;
	}
	
	public ArrayList<TUser> getUserList(long empNo, long listId) throws Exception {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		ArrayList<TUser> userList = new ArrayList<TUser>();

		try {
			conn = DBUtil.getECMDBConnection();
			String sqlQuery = "SELECT A.ListedEmpNo, A.Type, "
					+ "(SELECT FullName FROM ECM_USER WHERE EMPNO = A.ListedEmpNo AND ROWNUM=1) AS FullName, "
					+ "(SELECT Title FROM ECM_USER WHERE EMPNO = A.ListedEmpNo  AND ROWNUM=1) AS Title, "
					+ "(SELECT IseSignAllowed FROM ECM_USER WHERE EMPNO = A.ListedEmpNo  AND ROWNUM=1) AS eSignAllowed, "
					+ "(SELECT IsInitialAllowed FROM ECM_USER WHERE EMPNO = A.ListedEmpNo  AND ROWNUM=1) AS initialAllowed, "
					+ "(SELECT Name FROM ECM_ROLE WHERE ID = A.ListedEmpNo AND ROWNUM=1) AS RoleName "
					+ "FROM ECM_USERLIST_MEMBER A, ECM_USERLIST B WHERE "
					+ "A.ListID = ? AND A.ListID = B.ID AND (A.EmpNo = ? OR B.ISGLOBAL = 'Y') ORDER BY A.Type ASC, FullName ASC, RoleName ASC";
			stmt = conn.prepareStatement(sqlQuery);
			stmt.setInt(1, (int)listId);
			stmt.setInt(2, (int)empNo);
			rs = stmt.executeQuery();
			
			while (rs.next()) {
				TUser tu = new TUser();
				tu.EmpNo = rs.getInt("ListedEmpNo");
				tu.appRole = rs.getString("Type");
				tu.fulName = rs.getString("FullName");
				tu.title = rs.getString("Title");
				if(tu.appRole != null && tu.appRole.equalsIgnoreCase("ROLE")){
					tu.fulName = rs.getString("RoleName");
					tu.title = rs.getString("RoleName");
				}
				tu.iseSignAllowed = rs.getInt("eSignAllowed");
				tu.isIntialAllowed = rs.getInt("initialAllowed");
				
				if(tu.fulName != null && tu.fulName.length() > 0 )
					userList.add(tu);
			}
			return userList;

		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			rs.close();
			stmt.close();
			conn.close();
		}
	}
	
	public ArrayList<TUser> getUsers() throws Exception {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		ArrayList<TUser> userList = new ArrayList<TUser>();

		try {
			conn = DBUtil.getECMDBConnection();
			stmt = conn.prepareStatement("SELECT * FROM ECM_USER WHERE Status = 'ACTIVE' "
					+ "ORDER BY ID ASC");
			rs = stmt.executeQuery();
			int uCount = 0;
			while (rs.next()) {
				uCount++;
				ECMUser user = new ECMUser();
				user.setUserName(rs.getString("UserName"));
				user.setEmpNo(rs.getInt("EMPNo"));
				user.setFullName(rs.getString("FullName"));
				user.setId(uCount); //rs.getInt("ID")
				user.setTitle(rs.getString("Title"));
				user.setMail(rs.getString("Mail"));
				user.setOrgCode(rs.getString("OrgCode"));
				user.setKOCId(rs.getString("KOCID"));
				user.setIseSignAllowed(rs.getLong("IseSignAllowed"));
				user.setIsInitalAllowed(rs.getLong("IsInitialAllowed"));
				user.setLoaded();			
				userList.add(user.getTransport());

			}
			return userList;

		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			rs.close();
			stmt.close();
			conn.close();
		}
	}

	
	public ArrayList<TUser> getReportUsers() throws Exception {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		ArrayList<TUser> userList = new ArrayList<TUser>();

		try {
			conn = DBUtil.getECMDBConnection();
			stmt = conn.prepareStatement("SELECT A.EMPNO, B.USERNAME, B.FullName, A.ID, B.ORGCODE, B.MAIL, B.KOCID, A.ISADMIN "
					+ "FROM ECM_REPORT_USERS A, ECM_USER B  "
					+ "WHERE A.Status = 'ACTIVE' AND A.EMPNO = B.EMPNO"
					+ " ORDER BY FullName ASC");
			rs = stmt.executeQuery();
			
			int uCount = 0;
			while (rs.next()) {
				uCount++;
				ECMUser user = new ECMUser();
				user.setUserName(rs.getString("UserName"));
				user.setEmpNo(rs.getInt("EMPNo"));
				user.setFullName(rs.getString("FullName"));
				user.setId(uCount); //rs.getInt("ID")
				user.setOrgCode(rs.getString("OrgCode"));
				user.setKOCId(rs.getString("KOCID"));
				user.setIsReportAdmin(rs.getString("Isadmin"));
				user.setMail(rs.getString("MAIL"));
				user.setLoaded();			
				userList.add(user.getTransport());
			}
			return userList;

		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			rs.close();
			stmt.close();
			conn.close();
		}
	}
	
	public ArrayList<TUser> getExcludedUsers() throws Exception {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		ArrayList<TUser> userList = new ArrayList<TUser>();

		try {
			conn = DBUtil.getECMDBConnection();
			stmt = conn.prepareStatement("SELECT A.EMPNO, B.USERNAME, B.FullName, A.ID, B.ORGCODE, B.MAIL, B.KOCID "
					+ "FROM ECM_EXCLUDED_USERS A, ECM_USER B  "
					+ "WHERE A.Status = 'ACTIVE' AND A.EMPNO = B.EMPNO"
					+ " ORDER BY FullName ASC");
			rs = stmt.executeQuery();
			
			int uCount = 0;
			while (rs.next()) {
				uCount++;
				ECMUser user = new ECMUser();
				user.setUserName(rs.getString("UserName"));
				user.setEmpNo(rs.getInt("EMPNo"));
				user.setFullName(rs.getString("FullName"));
				user.setId(uCount); //rs.getInt("ID")
				user.setOrgCode(rs.getString("OrgCode"));
				user.setKOCId(rs.getString("KOCID"));
				user.setMail(rs.getString("MAIL"));
				user.setLoaded();			
				userList.add(user.getTransport());
			}
			return userList;

		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			rs.close();
			stmt.close();
			conn.close();
		}
	}
	
	public ArrayList<TUser> getAdminUsers() throws Exception {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		ArrayList<TUser> userList = new ArrayList<TUser>();

		try {
			conn = DBUtil.getECMDBConnection();
			stmt = conn.prepareStatement("SELECT A.EMPNO, B.USERNAME, B.FullName, A.ID, B.ORGCODE, B.MAIL, B.KOCID, A.CreatedBy, A.CreatedDate, A.Justification "
					+ "FROM ECM_ADMINISTRATORS A, ECM_USER B  "
					+ "WHERE A.Status = 'ACTIVE' AND A.EMPNO = B.EMPNO"
					+ " ORDER BY FullName ASC");
			rs = stmt.executeQuery();
			int uCount = 0;
			while (rs.next()) {
				uCount++;
				ECMUser user = new ECMUser();
				user.setUserName(rs.getString("UserName"));
				user.setEmpNo(rs.getInt("EMPNo"));
				user.setFullName(rs.getString("FullName"));
				user.setId(uCount); //rs.getInt("ID")
				user.setOrgCode(rs.getString("OrgCode"));
				user.setKOCId(rs.getString("KOCID"));
				user.setCreatedBy(rs.getString("CreatedBy"));
				user.setMail(rs.getString("MAIL"));
				user.setCreatedDate(DBUtil.convertDateToString(rs.getTimestamp("CreatedDate")));
				user.setJustification(rs.getString("Justification"));
				user.setLoaded();			
				userList.add(user.getTransport());

			}
			return userList;

		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			rs.close();
			stmt.close();
			conn.close();
		}
	}

	public TUser getUserDetails(String userid)
	{
		try {
			ECMUser cUser = new ECMUser();
			cUser.setUserName(userid);
			return cUser.getTransport();
		} catch (Exception e) {
			return null;
		}
	}
	
	public String refreshDelegation(long roleId) throws Exception {
		String strResult = "Failed";
		Connection conn = null;
		PreparedStatement ps = null;
		try {
			String roleString = "";
			if(roleId > 0)
				roleString = "AND USERID = " + roleId;
			
			conn = DBUtil.getECMDBConnection();
			String sqlQuery = "SELECT ID FROM ECM_DELEGATION WHERE STATUS = ? "
					+ "AND trunc(TODATE) < TO_DATE('" + DBUtil.escapeString(DBUtil.getTodayDate()) + "','dd/MM/yyyy') "
					+ roleString;
			ps = conn.prepareStatement(sqlQuery);
			ps.setString(1, DBUtil.escapeString("ACTIVE"));
			
			ResultSet rs = ps.executeQuery();
			if(rs.next()) {
				long delId = rs.getInt("ID");;			
				revokeDelegation(delId);
				strResult = "Success";
			}
			return strResult;

		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			ps.close();
			conn.close();
		}
	}
	
	public void saveUser(TUser user) throws Exception {
		ECMUser eUser = new ECMUser();
		eUser.getFromTransport(user);
		eUser.save();
	}
	
	public String saveReportUser(long empNo, long id, String isadmin) throws Exception {
		ECMUser eUser = new ECMUser();
		eUser.setEmpNo(empNo);
		return eUser.saveReportUser(id, isadmin);
	}
	
	public String saveExcludedUser(long empNo, long id) throws Exception {
		ECMUser eUser = new ECMUser();
		eUser.setEmpNo(empNo);
		return eUser.saveExcludedUser(id);
	}
	
	public String saveAdminUser(TAdminUser Tau) throws Exception {
		ECMUser eUser = new ECMUser();
		eUser.setEmpNo(Tau.empNo);
		eUser.setCreatedBy(Tau.createdBy);
		eUser.setJustification(Tau.justification);
		return eUser.saveAdminUser(Tau.id);
	}
	

	public String getLoginName(long empNo) throws Exception {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		String userName = null;
		
		try {
			conn = DBUtil.getECMDBConnection();
			stmt = conn.prepareStatement("SELECT UserName FROM ECM_USER WHERE EMPNO = ?");
			stmt.setInt(1,  (int)empNo);
			rs = stmt.executeQuery();
			if (rs.next()) {
				userName = rs.getString("UserName");
			}
			return userName;

		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			rs.close();
			stmt.close();
			conn.close();
		}
	}
	
	public String getKOCId(long empNo) throws Exception {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		String KOCId = null;
		
		try {
			conn = DBUtil.getECMDBConnection();
			stmt = conn.prepareStatement("SELECT KOCID FROM ECM_USER WHERE EMPNO = ?");
			stmt.setInt(1,  (int)empNo);
			rs = stmt.executeQuery();
			if (rs.next()) {
				KOCId = rs.getString("KOCID");
			}
			return KOCId;

		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			rs.close();
			stmt.close();
			conn.close();
		}
	}
	
	
	
	public String getUserFullName(long empNo) throws Exception {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		String userName = null;
		
		try {
			conn = DBUtil.getECMDBConnection();
			stmt = conn.prepareStatement("SELECT FullName FROM ECM_USER WHERE EMPNO = ?");
			stmt.setInt(1,  (int)empNo);
			rs = stmt.executeQuery();
			if (rs.next()) {
				userName = rs.getString("FullName");
			}
			return userName;

		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			rs.close();
			stmt.close();
			conn.close();
		}
	}
	
	public long getEmployee(String userName) throws Exception {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		long empNo = 0;
		
		try {
			conn = DBUtil.getECMDBConnection();
			stmt = conn.prepareStatement("SELECT EMPNO FROM ECM_USER WHERE UPPER(USERNAME) = UPPER(?)");
			stmt.setString(1,  DBUtil.escapeString(userName));
			rs = stmt.executeQuery();
			if (rs.next()) {
				empNo = rs.getInt("EMPNO");
			}
			return empNo;

		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			rs.close();
			stmt.close();
			conn.close();
		}
	}
	
	public String getOrgCode(long empNo) throws Exception {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		String orgCode = null;
		
		try {
			conn = DBUtil.getECMDBConnection();
			stmt = conn.prepareStatement("SELECT OrgCode FROM ECM_USER WHERE EMPNO = ?");
			stmt.setInt(1,  (int)empNo);
			rs = stmt.executeQuery();
			if (rs.next()) {
				orgCode = rs.getString("OrgCode");
			}
			return orgCode;

		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			rs.close();
			stmt.close();
			conn.close();
		}
	}
	
	public String getUserEmail(long empNo) throws Exception {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		String userMail = null;
		
		try {
			conn = DBUtil.getECMDBConnection();
			stmt = conn.prepareStatement("SELECT MAIL FROM ECM_USER WHERE EMPNo = ?");
			stmt.setInt(1,  (int)empNo);
			rs = stmt.executeQuery();
			if (rs.next()) {
				userMail = rs.getString("Mail");
			}
			return userMail;

		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			rs.close();
			stmt.close();
			conn.close();
		}
	}
	
	public String getUserEmailById(String kocId) throws Exception {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		String userMail = null;
		
		try {
			conn = DBUtil.getECMDBConnection();
			stmt = conn.prepareStatement("SELECT MAIL FROM ECM_USER WHERE KOCID = ?");
			stmt.setString(1,  kocId);
			rs = stmt.executeQuery();
			if (rs.next()) {
				userMail = rs.getString("Mail");
			}
			return userMail;

		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			rs.close();
			stmt.close();
			conn.close();
		}
	}
	
	private void addSetting(ArrayList<TUserSetting> sList, String key, long empNo, String appId) {
		for(TUserSetting set: sList) {
			if((set != null) && (set.key.equalsIgnoreCase(key)))
				return;
		}
		TUserSetting set = new TUserSetting();
		set.appId = appId;
		set.empNo = empNo;
		set.key = key;
		set.val = "";
		sList.add(set);
	}
	
	private void validateUserSettings(ArrayList<TUserSetting> sList, long empNo, String appId) {
		addSetting(sList, "Page Size", empNo, appId);
		addSetting(sList, "Default Template", empNo, appId);
		addSetting(sList, "Default Action", empNo, appId);
	}
	
	public ArrayList<TUserSetting> getUserSettings(long empNo, String appId) throws Exception {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		ArrayList<TUserSetting> sList = new ArrayList<TUserSetting>();
		try {
			conn = DBUtil.getECMDBConnection();
			stmt = conn.prepareStatement("SELECT * FROM ECM_USER_SETTING WHERE "
					+ "EMPNo = ? AND APPID = ?");
			stmt.setInt(1,  (int)empNo);
			stmt.setString(2,  DBUtil.escapeString(appId));
			rs = stmt.executeQuery();
			while (rs.next()) {
				TUserSetting set = new TUserSetting();
				set.id = rs.getInt("ID");
				set.empNo = empNo;
				set.appId = appId;
				set.key = rs.getString("KEYNAME");
				set.val = rs.getString("KEYVALUE");
				
				sList.add(set);
			}
			validateUserSettings(sList, empNo, appId);
			return sList;

		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			rs.close();
			stmt.close();
			conn.close();
		}
	}
	
	public String getUserSetting(long empNo, String appId, String keyName) throws Exception {
		ECMUserSetting userSetting = new ECMUserSetting();
		userSetting.setEmpNo(empNo);
		userSetting.setAppID(appId);
		userSetting.setSettingKey(keyName);
		userSetting.load();
		return userSetting.getSettingValue();
	}
	
	public void updateUserSettings(ArrayList<TUserSetting> sList) throws Exception {
		if(sList == null)
			return;
		for(TUserSetting set:sList) {
			ECMUserSetting eSet = new ECMUserSetting();
			eSet.getFromTransport(set);
			eSet.save();
		}
	}
	
	public ArrayList<TUserSetting> getUserSearches(long empNo, String appId) throws Exception {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		Clob itemClob = null;
		ArrayList<TUserSetting> sList = new ArrayList<TUserSetting>();
		try {
			conn = DBUtil.getECMDBConnection();
			stmt = conn.prepareStatement("SELECT * FROM ECM_USER_SEARCH WHERE "
					+ "EMPNo = ? AND APPID = ?");
			stmt.setInt(1,  (int)empNo);
			stmt.setString(2,  DBUtil.escapeString(appId));
			rs = stmt.executeQuery();
			while (rs.next()) {
				TUserSetting set = new TUserSetting();
				set.id = rs.getInt("ID");
				set.empNo = empNo;
				set.appId = appId;
				set.key = rs.getString("KEYNAME");
				
				itemClob = rs.getClob("KEYVALUE");
				set.val = DBUtil.clobToString(itemClob);
				itemClob.free();
				sList.add(set);
			}
			return sList;

		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			rs.close();
			stmt.close();
			conn.close();
		}
	}
	
	public void updateUserSearches(ArrayList<TUserSetting> sList) throws Exception {
		if(sList == null)
			return;
		for(TUserSetting set:sList) {
			ECMUserSearch eSer = new ECMUserSearch();
			eSer.getFromTransport(set);
			eSer.save();
		}
	}
	
	public String updateDelegation(TDelegate td) throws Exception {
		ECMDelegation ed = new ECMDelegation();
		ed.getFromTransport(td);
		String saveStatus = ed.save();
		boolean bAddToRole = false;
		if(ed.getUserType().equalsIgnoreCase("ROLE") && saveStatus.equalsIgnoreCase("Added"))
		{
			bAddToRole = ECMRoleList.getInstance().addUserToRole(ed.getDelegateId(), ed.getUserId(), "DELEGATE");
			if (!bAddToRole)
				saveStatus = "Failed";
		}
		return saveStatus;
	}
	
	public void revokeDelegation(long delId) throws Exception {
		ECMDelegation ed = new ECMDelegation();
		ed.setId(delId);
		ed.load();
		ed.revoke();
		if(ed.getUserType().equalsIgnoreCase("ROLE"))
			ECMRoleList.getInstance().removeUserFromRole(ed.getDelegateId(), ed.getUserId(), "DELEGATE");
	}
	
	public ArrayList<TDelegate> getDelegates(long userId, String uType) throws Exception {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		ArrayList<TDelegate> dList = new ArrayList<TDelegate>();
		
		try {
			String userType = "USER";
			String fnSQL = "(SELECT FullName FROM ECM_USER WHERE EMPNo = A.DELEGATEID) AS DelegateName ";
			String fnDelName = "(SELECT FullName FROM ECM_USER WHERE EMPNo = A.DELEGATEDBY) AS DelegatedByName, ";
			
			if((uType != null) && (uType.equalsIgnoreCase("ROLE"))) {
				userType = "ROLE";
				fnSQL += ",(SELECT Name FROM ECM_ROLE WHERE ID = A.USERID) AS RoleName ";
			}
			conn = DBUtil.getECMDBConnection();
			stmt = conn.prepareStatement("SELECT ID, USERID, DELEGATEID, FROMDATE, TODATE, DELEGATEDBY, "
					+ "DELEGATEDON, " + fnDelName + fnSQL
					+ " FROM ECM_DELEGATION A WHERE USERID = ? AND "
					+ "USERTYPE = ? AND trunc(TODATE) >= TO_DATE('" + DBUtil.escapeString(DBUtil.getTodayDate()) + "','dd/MM/yyyy')"
					+ " AND STATUS = 'ACTIVE' ORDER BY FROMDATE ASC");
			
			stmt.setInt(1,  (int)userId);
			stmt.setString(2, userType);
			rs = stmt.executeQuery();
			while (rs.next()) {
				TDelegate td = new TDelegate();
				td.id= rs.getInt("ID");
				td.userId = userId;
				td.userType = userType;
				td.status = "ACTIVE";
				td.delegateId = rs.getInt("DELEGATEID");
				if(rs.getTimestamp("FROMDATE").after(DBUtil.addYearstoDate(25)))
					td.fromDate = null;
				else
					td.fromDate = rs.getTimestamp("FROMDATE");
				if(rs.getTimestamp("TODATE").after(DBUtil.addYearstoDate(25)))
					td.toDate = null;
				else
					td.toDate = rs.getTimestamp("TODATE");
				
				td.delName = rs.getString("DelegateName");
				td.delegatedBy = rs.getInt("DELEGATEDBY");
				td.delegatedOn = rs.getTimestamp("DELEGATEDON");
				td.delegatedByName = rs.getString("DelegatedByName");
				if(userType.equalsIgnoreCase("ROLE"))
					td.roleName = rs.getString("RoleName");				
				dList.add(td);
			}
			return dList;

		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			rs.close();
			stmt.close();
			conn.close();
		}
	}
	
	public String validateDelegate(long delId) throws Exception {
		ECMDelegation eDel = new ECMDelegation();
		eDel.setId(delId);
		return eDel.validateDelegate();
	}
	
	public void sendEmailNotifications() throws Exception {
		ArrayList<Long> delList = getExpiringDelegation();
		sendEmailsFromList(delList);	
	}
	
	private void sendEmailsFromList(ArrayList<Long> delList) {
		for (Long dId : delList) {
			try {
				ECMDelegation eDel = new ECMDelegation();
				eDel.setId(dId);
				eDel.sendDelegationEmail("DELEGATE", dId, "Expired");
				eDel.load();
				eDel.revoke();
				if(eDel.getUserType().equalsIgnoreCase("ROLE"))
					ECMRoleList.getInstance().removeUserFromRole(eDel.getDelegateId(), eDel.getUserId(), "DELEGATE");
			} catch (Exception e) {
			}
		}
	}
	
	public ArrayList<TDelegate> getDelegatesBy(long empNo) throws Exception {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		ArrayList<TDelegate> dList = new ArrayList<TDelegate>();
		
		try {	
			conn = DBUtil.getECMDBConnection();
			stmt = conn.prepareStatement("SELECT ID, DELEGATEID, FROMDATE, TODATE, DELEGATEDBY, "
					+ "DELEGATEDON, USERID, USERTYPE, "
					+ "(SELECT FullName FROM ECM_USER WHERE EMPNo = A.DELEGATEID) AS DelegateEmpName, "
					+ "FROM ECM_DELEGATION A WHERE DELEGATEDBY = ? AND "
					+ "trunc(TODATE) >= TO_DATE('" + DBUtil.escapeString(DBUtil.getTodayDate()) + "','dd/MM/yyyy')"
					+ " AND STATUS = 'ACTIVE' ORDER BY FROMDATE ASC");
			stmt.setInt(1,  (int)empNo);
			rs = stmt.executeQuery();
			while (rs.next()) {
				TDelegate td = new TDelegate();
				td.id= rs.getInt("ID");
				td.userId = rs.getInt("USERID");
				td.userType = rs.getString("USERTYPE");;
				td.status = "ACTIVE";
				td.delegateId = rs.getInt("DELEGATEID");
				if(rs.getTimestamp("FROMDATE").after(DBUtil.addYearstoDate(25)))
					td.fromDate = null;
				else
					td.fromDate = rs.getTimestamp("FROMDATE");
				if(rs.getTimestamp("TODATE").after(DBUtil.addYearstoDate(25)))
					td.toDate = null;
				else
					td.toDate = rs.getTimestamp("TODATE");
				td.delName = rs.getString("DelegateName");
				td.delegatedBy = rs.getInt("DELEGATEDBY");
				td.delegatedOn = rs.getTimestamp("DELEGATEDON");
				dList.add(td);
			}
			return dList;

		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			rs.close();
			stmt.close();
			conn.close();
		}
	}
	
	public ArrayList<Long> getExpiringDelegation() throws Exception {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		String sqlQuery = null;
		try {
			ArrayList<Long> delList = new ArrayList<Long>();
			conn = DBUtil.getECMDBConnection();
			sqlQuery = "SELECT ID FROM ECM_DELEGATION STATUS = 'ACTIVE' AND NOTIFYSTATUS <> 'DONE' "
					+ "AND trunc(TODATE) < TO_DATE('" + DBUtil.escapeString(DBUtil.getTodayDate()) + "','dd/MM/yyyy')";
			stmt = conn.prepareStatement(sqlQuery);
			rs = stmt.executeQuery();
			while (rs.next()) {
				delList.add(new Long(rs.getInt("ID")));
			}
			return delList;
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			rs.close();
			stmt.close();
			conn.close();
		}
	}
	
	public ArrayList<TOrgUnit> getUserSupervisorTree(long empNo) throws Exception {
		ECMUser user = new ECMUser();
		user.setEmpNo(empNo);
		return user.getUserSupervisorTree();
	}
	
	public String getSupervisorEmailForUser(long empNo) throws Exception {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			conn = DBUtil.getECMDBConnection();
			stmt = conn.prepareStatement("SELECT A.ORGCODE, B.HEAD , "
					+ "(SELECT MAIL FROM ECM_USER WHERE EMPNO = B.HEAD) AS EMAIL "
					+ "FROM ECM_USER A, ECM_ORGUNIT B "
					+ "WHERE A.EMPNO = ? AND A.ORGCODE = B.ORGCODE AND ROWNUM=1");
			stmt.setInt(1,  (int)empNo);
			rs = stmt.executeQuery();
			if (rs.next()) {
				String email = rs.getString("EMAIL");
				
				return email;
			}
			return null;

		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			rs.close();
			stmt.close();
			conn.close();
		}
	}
}
