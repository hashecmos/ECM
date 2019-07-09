package com.ecm.db.model;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

import oracle.jdbc.OracleTypes;

import com.ecm.db.util.DBUtil;
import com.ecm.db.list.ECMAdministrationList;
import com.ecm.db.list.ECMUserList;
import com.ecm.db.transport.TAdminEmailSet;
import com.ecm.db.transport.TDelegate;
import com.ecm.db.transport.TOrgUnit;
import com.ecm.db.transport.TRole;
import com.ecm.db.transport.TUser;

public class ECMUser {

	private String userName;
	private String fullName;
	private String title;
	private String mail;
	private long EmpNo;
	private String KOCId;
	private String orgCode;
	private String teamName;
	private String groupName;
	private String location;
	private String phoneNo;
	private String nationality;
	private String type;
	private String grade;
	private String createdDate;
	private String modifiedDate;
	private String isAdmin = "N";
	private String isReport = "N";
	private String isReportAdmin = "N";
	private long iseSignAllowed = 0;
	private long isInitalAllowed = 0;
	private long id;
	ArrayList<ECMRole> roles;
	ArrayList<TDelegate> delegated;
	ArrayList<TOrgUnit> headof;
	private String createdBy;
	private String justification;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getMail() {
		return mail;
	}

	public void setMail(String mail) {
		this.mail = mail;
	}

	public long getEmpNo() {
		return EmpNo;
	}

	public void setEmpNo(long empno) {
		this.EmpNo = empno;
	}	

	public String getKOCId() {
		return KOCId;
	}

	public void setKOCId(String kocId) {
		KOCId = kocId;
	}

	public String getOrgCode() {
		return orgCode;
	}

	public void setOrgCode(String orgCode) {
		this.orgCode = orgCode;
	}

	public String getTeamName() {
		return teamName;
	}

	public void setTeamName(String teamName) {
		this.teamName = teamName;
	}

	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getPhoneNo() {
		return phoneNo;
	}

	public void setPhoneNo(String phoneNo) {
		this.phoneNo = phoneNo;
	}

	public String getNationality() {
		return nationality;
	}

	public void setNationality(String nationality) {
		this.nationality = nationality;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getGrade() {
		return grade;
	}

	public void setGrade(String grade) {
		this.grade = grade;
	}

	public String getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(String createdDate) {
		this.createdDate = createdDate;
	}

	public String getModifiedDate() {
		return modifiedDate;
	}

	public void setModifiedDate(String modifiedDate) {
		this.modifiedDate = modifiedDate;
	}

	public long getIseSignAllowed() {
		return iseSignAllowed;
	}

	public void setIseSignAllowed(long iseSignAllowed) {
		this.iseSignAllowed = iseSignAllowed;
	}

	public long getIsInitalAllowed() {
		return isInitalAllowed;
	}

	public void setIsInitalAllowed(long isInitalAllowed) {
		this.isInitalAllowed = isInitalAllowed;
	}
	
	public String getIsReport() {
		return isReport;
	}

	public void setIsReport(String isReport) {
		this.isReport = isReport;
	}
	
	public String getIsReportAdmin() {
		return isReportAdmin;
	}

	public void setIsReportAdmin(String isReportAdmin) {
		this.isReportAdmin = isReportAdmin;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}
	
	public String getJustification() {
		return justification;
	}

	public void setJustification(String justification) {
		this.justification = justification;
	}
	
	public boolean isLoaded() {
		return isLoaded;
	}

	public void setLoaded(boolean isLoaded) {
		this.isLoaded = isLoaded;
	}

	public ArrayList<ECMRole> getRoles() throws Exception
	{
		load();
		return this.roles;
	}
	
	private boolean isLoaded;

	public void setLoaded() {
		this.isLoaded = true;
	}
	
	public void load() throws Exception 
	{
		if (this.isLoaded)
			return;

		Connection conn = null;
		ResultSet rs = null;
		String sqlQuery = null;
		CallableStatement callableStatement = null;
		try {
			conn = DBUtil.getECMDBConnection();
			sqlQuery = "{call ECM_GET_USER(?,?)}";
			callableStatement = conn.prepareCall(sqlQuery);
			callableStatement.setString(1, this.userName);
			callableStatement.registerOutParameter(2, OracleTypes.CURSOR);
			//callableStatement.executeQuery();
			//rs = callableStatement.getResultSet();
			callableStatement.execute();
			rs = (ResultSet)callableStatement.getObject(2);
			if (rs != null && rs.next()) {

				this.setFullName(rs.getString("FullName"));
				this.setEmpNo(rs.getLong("EMPNo"));
				this.setMail(rs.getString("Mail"));
				this.setTitle(rs.getString("Title"));
				this.setOrgCode(rs.getString("OrgCode"));
				this.setId(rs.getLong("ID"));
				this.setIseSignAllowed(rs.getLong("IseSignAllowed"));
				this.setIsInitalAllowed(rs.getLong("IsInitialAllowed"));
				this.setKOCId(rs.getString("KOCID"));

				System.out.println(rs.getString("FullName"));
				isLoaded = true;
			} else {
				System.out.println("ID not present");
			}
			
			// Get the User Roles
			roles = getUserRoles(conn);
			delegated = getUserDelegatedBy(conn);
			isAdmin = isAdministrator(conn);
			isReport = isReportUser(conn);
			isReportAdmin = isReportAdmin(conn);
			headof = getHeadOf(conn, this.EmpNo, delegated, roles);
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			rs.close();
			callableStatement.close();
			conn.close();
		}
	}

	public ArrayList<TOrgUnit> getUserOrgUnits() throws Exception 
	{
		Connection conn = null;
		try {
			conn = DBUtil.getECMDBConnection();
			roles = getUserRoles(conn);
			delegated = getUserDelegatedBy(conn);
			isAdmin = isAdministrator(conn);
			isReport = isReportUser(conn);
			if(isAdmin.equalsIgnoreCase("Y")) {
				headof = new ArrayList<TOrgUnit>();
				headof.add(ECMAdministrationList.getInstance().getToplevelOrgUnit());
			} else
				headof = getHeadOf(conn, this.EmpNo, delegated, roles);
			return headof;
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			conn.close();
		}
	}
	
	private ArrayList<ECMRole> getUserRoles(Connection conn) throws Exception 
	{
		ResultSet rs = null;
		String sqlQuery = null;
		CallableStatement callableStatement = null;
		ArrayList<ECMRole> ecmRoles = new ArrayList<ECMRole>();
		try {
			sqlQuery = "{call ECM_GET_USER_ROLES(?,?)}";
			callableStatement = conn.prepareCall(sqlQuery);
			callableStatement.setInt(1, (int)this.EmpNo);
			callableStatement.registerOutParameter(2, OracleTypes.CURSOR);
			
			//callableStatement.executeQuery();
			//rs = callableStatement.getResultSet();
			callableStatement.execute();
			rs = (ResultSet)callableStatement.getObject(2);
			while (rs != null && rs.next()) {
				ECMRole userRole = new ECMRole();
				userRole.setId(rs.getLong("ID"));
				userRole.setName(rs.getString("Name"));
				userRole.setType(rs.getString("Type"));
				userRole.setLoaded();

				ecmRoles.add(userRole);
			}
			
			return ecmRoles;
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			rs.close();
			callableStatement.close();
		}
	}
	
	private ArrayList<TDelegate> getUserDelegatedBy(Connection conn) {
		ArrayList<TDelegate> dList = new ArrayList<TDelegate>();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = conn.prepareStatement("SELECT ID, USERID, FROMDATE, TODATE, " 
					+ "(SELECT FullName FROM ECM_USER WHERE EMPNo = A.USERID) AS DelegatedBy "
					+ "FROM ECM_DELEGATION A WHERE A.DELEGATEID = ? AND "
					+ "USERTYPE = ? "
					+ "AND trunc(FROMDATE) <= TO_DATE('" + DBUtil.escapeString(DBUtil.getTodayDate()) + "','dd/MM/yyyy') "
					+ "AND trunc(TODATE) >= TO_DATE('" + DBUtil.escapeString(DBUtil.getTodayDate()) + "','dd/MM/yyyy') "
					+ "AND STATUS = 'ACTIVE' ORDER BY FROMDATE ASC");
			ps.setInt(1,  (int)this.EmpNo);
			ps.setString(2, "USER");
			rs = ps.executeQuery();
			while (rs.next()) {
				TDelegate td = new TDelegate();
				td.id= rs.getInt("ID");
				td.userId = rs.getInt("USERID");
				td.delegateId = this.EmpNo;
				td.fromDate = rs.getTimestamp("FROMDATE");
				td.toDate = rs.getTimestamp("TODATE");
				td.delName = rs.getString("DelegatedBy");
				
				dList.add(td);
			}
			ps.close();
			rs.close();
			
		} catch (Exception e) {
			e.printStackTrace();
			//throw new Exception(e.getMessage());
		} 
		return dList;
	}
	
	private String isAdministrator(Connection conn) {
		String isAdmin = "N";
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = conn.prepareStatement("SELECT ID FROM ECM_ADMINISTRATORS "
					+ "WHERE EMPNO = ? AND STATUS = 'ACTIVE'");
			ps.setInt(1,  (int)this.EmpNo);
			rs = ps.executeQuery();
			if (rs.next()) {
				isAdmin = "Y";
			}
			ps.close();
			rs.close();
			
		} catch (Exception e) {
			e.printStackTrace();
		} 
		return isAdmin;
	}
	
	private String isReportUser(Connection conn) {
		String isReport = "N";
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = conn.prepareStatement("SELECT ID FROM ECM_REPORT_USERS "
					+ "WHERE EMPNO = ? AND STATUS = 'ACTIVE'");
			ps.setInt(1,  (int)this.EmpNo);
			rs = ps.executeQuery();
			if (rs.next()) {
				isReport = "Y";
			}
			ps.close();
			rs.close();
			
		} catch (Exception e) {
			e.printStackTrace();
		} 
		return isReport;
	}
	
	private String isReportAdmin(Connection conn) {
		String isReportAdmin = "N";
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = conn.prepareStatement("SELECT ID FROM ECM_REPORT_USERS "
					+ "WHERE EMPNO = ? AND STATUS = 'ACTIVE' AND ISADMIN = 'Y'");
			ps.setInt(1,  (int)this.EmpNo);
			rs = ps.executeQuery();
			if (rs.next()) {
				isReportAdmin = "Y";
			}
			ps.close();
			rs.close();
			
		} catch (Exception e) {
			e.printStackTrace();
		} 
		return isReportAdmin;
	}
	
	private ArrayList<TOrgUnit> getHeadOf(Connection conn, long empno, 
			ArrayList<TDelegate> delegated, ArrayList<ECMRole> roles) throws Exception 
	{
		ArrayList<TOrgUnit> oList = new ArrayList<TOrgUnit>();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			String roleString = "";
			for(ECMRole rId: roles) {
				if(roleString.length() > 0)
					roleString += ",";
				roleString += rId.getId();
			}
			String delString = "";
			for(TDelegate dId: delegated) {
				if(delString.length() > 0)
					delString += ",";
				delString += dId.id;
			}
			
			String strSQL = "SELECT * FROM ECM_ORGUNIT WHERE Head = ? ";
			if(roleString.length() > 0)
				strSQL += "OR HeadRoleId IN (?) ";
			
			if(delString.length() > 0)
				strSQL += "OR Head IN (?) ";
			
			ps = conn.prepareStatement(strSQL); 
			ps.setInt(1,  (int)empno);
			if(roleString.length() > 0) {
				ps.setString(2,  roleString);
				if(delString.length() > 0)
					ps.setString(3,  delString);
			} else {
				if(delString.length() > 0)
					ps.setString(2,  delString);
			}
			
			rs = ps.executeQuery();
			while (rs.next()) {
				TOrgUnit to = new TOrgUnit();
				to.desc = rs.getString("Description");
				to.orgCode = rs.getString("OrgCode");
				to.type = rs.getString("Type");
				to.id = rs.getInt("ID");
				oList.add(to);
			}
			ps.close();
			rs.close();
			
		} catch (Exception e) {
			e.printStackTrace();
			//throw new Exception(e.getMessage());
		} 
		return oList;
	}
	
	private void insert() throws Exception {
		Connection conn = null;
		CallableStatement cs = null;
		try {
			conn = DBUtil.getECMDBConnection();

			String sqlQuery = "{call ECM_CREATE_USER(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)}";
			cs = conn.prepareCall(sqlQuery);
			
			cs.setString(1, DBUtil.escapeString(this.userName));
			cs.setString(2, DBUtil.escapeString(this.fullName));
			cs.setString(3, DBUtil.escapeString(this.title));
			cs.setString(4, DBUtil.escapeString(this.mail));
			cs.setInt(5, (int)this.EmpNo);
			cs.setString(6, DBUtil.escapeString(this.orgCode));
			cs.setString(7, DBUtil.escapeString(this.teamName));
			cs.setString(8, DBUtil.escapeString(this.groupName));
			cs.setString(9, DBUtil.escapeString(this.location));
			cs.setString(10, DBUtil.escapeString(this.phoneNo));
			cs.setString(11, DBUtil.escapeString(this.nationality));
			cs.setString(12, DBUtil.escapeString(this.grade));
			cs.setInt(13, (int)this.iseSignAllowed);
			cs.setInt(14, (int)this.isInitalAllowed);

			cs.registerOutParameter(15, java.sql.Types.INTEGER);
			cs.executeUpdate();
			this.setId((int)cs.getInt(15));

		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			cs.close();
			conn.close();
		}
	}

	private void update() throws Exception {
		Connection conn = null;
		PreparedStatement ps = null;
		String sqlQuery = null;
		try {
			conn = DBUtil.getECMDBConnection();

			// The stored procedure does not exist. TODO: Convert to SQL call
			sqlQuery = "UPDATE ECM_USER SET ISINITIALALLOWED = ?, ISESIGNALLOWED = ? WHERE EMPNo = ?";
			ps = conn.prepareStatement(sqlQuery);
			ps.setInt(1, (int)this.isInitalAllowed);
			ps.setInt(2, (int)this.iseSignAllowed);
			ps.setInt(3, (int)this.EmpNo);
			//ps.setString(4, DBUtil.escapeString(this.fullName));
			//ps = conn.prepareStatement(sqlQuery);
			ps.executeUpdate();

			if(!(sendSaveUserEmail("Updated")))
			{
					//New method to send email later
			}
			

		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			ps.close();
			conn.close();;
		}
	}

	private Boolean sendSaveUserEmail(String action) {

		try {
			ECMMailManager em = new ECMMailManager();
			TAdminEmailSet taes = new TAdminEmailSet();
			if (this.iseSignAllowed == 0)
				taes.iseSignAllowed = "No";
			else
				taes.iseSignAllowed = "Yes";

			if (this.isInitalAllowed == 0)
				taes.isInitalAllowed = "No";
			else
				taes.isInitalAllowed = "Yes";

			taes.EmpNo = this.EmpNo;
			taes.fullName = ECMUserList.getInstance().getUserFullName(this.EmpNo);
			em.sendEmail("ECMUsers", taes, action);

			return true;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}

	}

	public void save() throws Exception {
		if (this.id <= 0)
			insert();
		else
			update();
	}
	
	public String saveReportUser(long reportUserId, String isadmin) throws Exception {
		if (reportUserId <= 0)
			return insertReportUser(isadmin);
		else
			return updateReportUser(isadmin);
	}
	
	
	public String saveExcludedUser(long reportUserId) throws Exception {
		if (reportUserId <= 0)
			return insertExcludedUser();
		else
			return updateExcludedUser();
	}
	
	public String saveAdminUser(long reportUserId) throws Exception {
		if (reportUserId <= 0)
			return insertAdminUser();
		else
			return updateAdminUser();
	}

	
	private String insertExcludedUser() throws Exception {
		Connection conn = null;
		PreparedStatement ps = null;
		String sqlQuery = null;
		String strMsg = "Failed";
		try {
			conn = DBUtil.getECMDBConnection();
			if(!isExcludedUserExists(conn))
			{
				sqlQuery = "INSERT INTO ECM_EXCLUDED_USERS(ID, EMPNO, STATUS, CREATEDBY, CREATEDDATE) "
						+ "VALUES(ECM_REPORT_USER_SEQ.NEXTVAL, ?, ?, ?, SYSTIMESTAMP)";
				ps = conn.prepareStatement(sqlQuery);
				ps.setInt(1,  (int)this.EmpNo);
				ps.setString(2,  "ACTIVE");
				ps.setString(3, DBUtil.escapeString(this.createdBy));
				ps.executeUpdate();
				if(!(sendExcludedUserEmail("Inserted")))
				{
						//New method to send email later
				}
				
				strMsg = "OK";
			}
			else
				strMsg = "User Exists";

			return strMsg;
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			if(strMsg == "OK")
				ps.close();
			conn.close();
		}
	}
	private Boolean sendExcludedUserEmail(String action){
		
		try {
			ECMMailManager em=new ECMMailManager();
			TAdminEmailSet taes=new TAdminEmailSet();
		    taes.EmpNo=this.EmpNo;
		    taes.fullName = ECMUserList.getInstance().getUserFullName(this.EmpNo);
			em.sendEmail("ECMExcludedOperator", taes, action);
			
			return true;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		
		
	}
	

	private String updateExcludedUser() throws Exception {
		Connection conn = null;
		PreparedStatement ps = null;
		String sqlQuery = null;
		try {
			conn = DBUtil.getECMDBConnection();

			sqlQuery = "UPDATE ECM_EXCLUDED_USERS SET STATUS = 'INACTIVE' WHERE EMPNo = ?";
			ps = conn.prepareStatement(sqlQuery);
			ps.setInt(1, (int)this.EmpNo);
			ps.executeUpdate();
			if(!(sendExcludedUserEmail("Removed")))
			{
					//New method to send email later
			}
			
			return "OK";

		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			ps.close();
			conn.close();;
		}
	}
		
	private boolean isExcludedUserExists(Connection conn) {
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = conn.prepareStatement("SELECT ID FROM ECM_EXCLUDED_USERS WHERE EMPNO = ? AND STATUS = 'ACTIVE'");
			ps.setInt(1,  (int)this.EmpNo);
			rs = ps.executeQuery();
			while (rs.next()) {
				return true;
			}
			ps.close();
			rs.close();			
		} catch (Exception e) {
			e.printStackTrace();
			//throw new Exception(e.getMessage());
		} 
		return false;
	}
	
	
	private String insertReportUser(String isadmin) throws Exception {
		Connection conn = null;
		PreparedStatement ps = null;
		String sqlQuery = null;
		String strMsg = "Failed";
		try {
			conn = DBUtil.getECMDBConnection();
			if(!isReportUserExists(conn))
			{
				sqlQuery = "INSERT INTO ECM_REPORT_USERS(ID, EMPNO, STATUS, ISADMIN) "
						+ "VALUES(ECM_REPORT_USER_SEQ.NEXTVAL, ?, ?, ?)";
				ps = conn.prepareStatement(sqlQuery);
				ps.setInt(1,  (int)this.EmpNo);
				ps.setString(2,  "ACTIVE");
				ps.setString(3, DBUtil.escapeString(isadmin));
				ps.executeUpdate();
				if(!(sendReportUserEmail(isadmin,"Inserted")))
				{
					//New method to send email later
				}
				
				
				strMsg = "OK";
			}
			else
				strMsg = "User Exists";

			return strMsg;
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			if(strMsg == "OK")
				ps.close();
			conn.close();
		}
	}
	 private Boolean sendReportUserEmail(String isadmin,String action){
			try {
				ECMMailManager em=new ECMMailManager();
				TAdminEmailSet taes=new TAdminEmailSet();
			    taes.EmpNo=this.EmpNo;
			    taes.fullName=ECMUserList.getInstance().getUserFullName(this.EmpNo);
			    taes.isadmin=isadmin;
				em.sendEmail("ECMReportUsers", taes, action);
				
				return true;
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return false;
			}	
		}
	private String updateReportUser(String isadmin) throws Exception {
		Connection conn = null;
		PreparedStatement ps = null;
		String sqlQuery = null;
		try {
			conn = DBUtil.getECMDBConnection();

			sqlQuery = "UPDATE ECM_REPORT_USERS SET STATUS = 'INACTIVE', ISADMIN = ? WHERE EMPNo = ?";
			ps = conn.prepareStatement(sqlQuery);
			ps.setString(1, DBUtil.escapeString(isadmin));
			ps.setInt(2, (int)this.EmpNo);
			ps.executeUpdate();
			if(!(sendReportUserEmail(isadmin,"Removed")))
			{
				//New method to send email later
			}
			
			return "OK";

		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			ps.close();
			conn.close();;
		}
	}
	
	private boolean isReportUserExists(Connection conn) {
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = conn.prepareStatement("SELECT ID FROM ECM_REPORT_USERS WHERE EMPNO = ? AND STATUS = 'ACTIVE'");
			ps.setInt(1,  (int)this.EmpNo);
			rs = ps.executeQuery();
			while (rs.next()) {
				return true;
			}
			ps.close();
			rs.close();			
		} catch (Exception e) {
			e.printStackTrace();
			//throw new Exception(e.getMessage());
		} 
		return false;
	}
	
	private String insertAdminUser() throws Exception {
		Connection conn = null;
		PreparedStatement ps = null;
		String sqlQuery = null;
		String strMsg = "Failed";
		try {
			conn = DBUtil.getECMDBConnection();
			if(!isAdminExists(conn))
			{
				sqlQuery = "INSERT INTO ECM_ADMINISTRATORS(ID, EMPNO, STATUS, CREATEDBY, CREATEDDATE, JUSTIFICATION) "
						+ "VALUES(ECM_REPORT_USER_SEQ.NEXTVAL, ?, ?, ?, SYSTIMESTAMP, ?)";
				ps = conn.prepareStatement(sqlQuery);
				ps.setInt(1,  (int)this.EmpNo);
				ps.setString(2,  "ACTIVE");
				ps.setString(3, DBUtil.escapeString(this.createdBy));
				ps.setString(4, DBUtil.escapeString(this.justification));
				ps.executeUpdate();
				if(!(sendAdminUserEmail("Inserted")))
				{
						//New method to send email later
				}
				strMsg = "OK";
			}
			else
				strMsg = "User Exists";
			
			return strMsg;
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			if(strMsg == "OK")
				ps.close();
			conn.close();
		}
	}

	private Boolean sendAdminUserEmail(String action) {

		try {
			ECMMailManager em = new ECMMailManager();
			TAdminEmailSet taes = new TAdminEmailSet();
			taes.EmpNo = this.EmpNo;
			taes.fullName = ECMUserList.getInstance().getUserFullName(this.EmpNo);
			taes.createdBy = this.createdBy;
			taes.justification = this.justification;
			em.sendEmail("ECMAdministrator", taes, action);

			return true;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}

	private String updateAdminUser() throws Exception {
		Connection conn = null;
		PreparedStatement ps = null;
		String sqlQuery = null;
		try {
			conn = DBUtil.getECMDBConnection();

			sqlQuery = "UPDATE ECM_ADMINISTRATORS SET STATUS = 'INACTIVE' WHERE EMPNo = ?";
			ps = conn.prepareStatement(sqlQuery);
			ps.setInt(1, (int)this.EmpNo);
			ps.executeUpdate();
			if(!(sendAdminUserEmail("Removed")))
			{
				//New method to send email later
			}
			
			return "OK";

		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			ps.close();
			conn.close();;
		}
	}
	
	private boolean isAdminExists(Connection conn) {
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = conn.prepareStatement("SELECT ID FROM ECM_ADMINISTRATORS WHERE EMPNO = ? AND STATUS = 'ACTIVE'");
			ps.setInt(1,  (int)this.EmpNo);
			rs = ps.executeQuery();
			while (rs.next()) {
				return true;
			}
			ps.close();
			rs.close();			
		} catch (Exception e) {
			e.printStackTrace();
			//throw new Exception(e.getMessage());
		} 
		return false;
	}
	
	public void delete() throws Exception {
		Connection conn = null;
		PreparedStatement ps = null;
		String sqlQuery = null;
		try {
			conn = DBUtil.getECMDBConnection();
			sqlQuery = "UPDATE ECM_USER SET Status = 'INACTIVE' WHERE EMPNo = ?";
			ps = conn.prepareStatement(sqlQuery);
			ps.setInt(1, (int)this.EmpNo);
			ps.executeUpdate();

		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			ps.close();
			conn.close();
		}
	}

	public TUser getTransport() throws Exception {
		load();
		if (!isLoaded)
			return null;

		TUser t_Usr = new TUser();

		t_Usr.fulName = this.fullName;
		t_Usr.id = this.id;
		t_Usr.userName = this.userName;
		t_Usr.EmpNo = this.EmpNo;
		t_Usr.mail = this.mail;
		t_Usr.orgCode = this.orgCode;
		t_Usr.title = this.title;
		t_Usr.delegated = this.delegated;
		t_Usr.isAdmin = this.isAdmin;
		t_Usr.isReport = this.isReport;
		t_Usr.isReportAdmin = this.isReportAdmin;
		t_Usr.headof = this.headof;
		t_Usr.iseSignAllowed = this.iseSignAllowed;
		t_Usr.isIntialAllowed = this.isInitalAllowed;
		t_Usr.KocId = this.KOCId;
		t_Usr.createdBy = this.createdBy;
		t_Usr.createdDate = this.createdDate;
		t_Usr.justification = this.justification;
		
		if(roles != null)
			for(ECMRole role: roles)
				t_Usr.roles.add(role.getTransport());
		
		return t_Usr;
	}

	public void getFromTransport(TUser user) throws Exception {
		
		this.setFullName(user.fulName);
		this.setId(user.id);
		this.setEmpNo(user.EmpNo);
		this.setMail(user.mail);
		this.setUserName(user.userName);
		this.setTitle(user.title);
		this.setOrgCode(user.orgCode);
		this.setIseSignAllowed(user.iseSignAllowed);
		this.setIsInitalAllowed(user.isIntialAllowed);
		this.setKOCId(user.KocId);
		this.setIsReport(user.isReport);
		this.setIsReportAdmin(user.isReportAdmin);
		
		
		for(TRole role:user.roles)
		{
			if(roles == null)
				roles = new ArrayList<ECMRole>();
			
			ECMRole userRole = new ECMRole();
			userRole.getFromTransport(role);
			roles.add(userRole);
		}
			
		//this.setGrade(user.grde);
		//this.setGroupName(user.group);
		//this.setType(user.type);
		//this.setCreatedDate(user.ctdDate);
		//this.setTeamName(user.team);
		//this.setPhoneNo(user.phone);	
		//this.setNationality(user.nationality);

	}

	public ArrayList<TOrgUnit> getUserSupervisorTree() throws Exception {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		ArrayList<TOrgUnit> ouRevList = new ArrayList<TOrgUnit>();
		
		try {
			ArrayList<TOrgUnit> ouList = new ArrayList<TOrgUnit>();
			conn = DBUtil.getECMDBConnection();
			long ouParent = 0;
			
			stmt = conn.prepareStatement("SELECT B.ID, B.ORGCODE, B.PARENTID, B.HEADROLEID, B.DESCRIPTION "
					+ "FROM ECM_USER A, ECM_ORGUNIT B "
					+ "WHERE A.EMPNO = ? AND A.ORGCODE = B.ORGCODE "
					+ "AND ROWNUM = 1");
			stmt.setInt(1,  (int)this.EmpNo);
			rs = stmt.executeQuery();
			if (rs.next()) {
				TOrgUnit ou = new TOrgUnit();
				ou.id = rs.getInt("ID");
				ou.headRoleId = rs.getInt("HEADROLEID");
				ou.parent = rs.getInt("PARENTID");
				ou.headRoleName = rs.getString("DESCRIPTION");
				ou.orgCode = rs.getString("ORGCODE");
				ouParent = ou.parent;
				
				ouList.add(ou);
			}
			
			while(ouParent > 0) {
				rs.close();
				stmt.close();
				stmt = conn.prepareStatement("SELECT A.ID, A.ORGCODE, A.PARENTID, A.HEADROLEID, A.DESCRIPTION "
						+ "FROM ECM_ORGUNIT A, ECM_ROLE B "
						+ "WHERE A.ID = ? AND A.HEADROLEID = B.ID AND ROWNUM = 1");
				
				stmt.setInt(1,  (int)ouParent);
				rs = stmt.executeQuery();
				if (rs.next()) {
					TOrgUnit ou = new TOrgUnit();
					ou.id = rs.getInt("ID");
					ou.headRoleId = rs.getInt("HEADROLEID");
					ou.parent = rs.getInt("PARENTID");
					ou.headRoleName = rs.getString("DESCRIPTION");
					ou.orgCode = rs.getString("ORGCODE");
					ouParent = ou.parent;
					
					ouList.add(ou);
				}
			}
			
			// Reverse the list
			for(int i=ouList.size()-1; i>=0; i--) {
				ouRevList.add(ouList.get(i));
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			rs.close();
			stmt.close();
			conn.close();
		}
		return ouRevList;
	}
}
