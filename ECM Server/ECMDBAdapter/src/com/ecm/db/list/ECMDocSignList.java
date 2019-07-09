package com.ecm.db.list;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;

import com.ecm.db.model.ECMDocSign;
import com.ecm.db.transport.TDocSign;
import com.ecm.db.transport.TDocSignItem;
import com.ecm.db.transport.TOrgUnit;
import com.ecm.db.util.DBUtil;

public class ECMDocSignList {
	private ECMDocSignList() {	}

	public static ECMDocSignList getInstance() {
		return new ECMDocSignList();
	}

	public ArrayList<TDocSign> getSignRequests(long userId, String userType) throws Exception {

		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		String filterString = "";
		if((userType != null) && (userType.trim().equalsIgnoreCase("ROLE")))
			filterString = "ROLEID = ? ";
		else
			filterString = "EMPNO = ? ";
		
		ArrayList<TDocSign> rList = new ArrayList<TDocSign>();
		try {
			conn = DBUtil.getECMDBConnection();
			stmt = conn.prepareStatement("SELECT * from ECM_ESIGN_DOCS WHERE " +
					filterString
					+ "ORDER BY REQDATE DESC");
			stmt.setInt(1, (int) userId);
			rs = stmt.executeQuery();

			while (rs.next()) {

				TDocSign td = new TDocSign();
				td.id = rs.getInt("ID");
				td.docId = rs.getString("DOCID");
				td.inFile = rs.getString("INFILE");
				td.outFile = rs.getString("OUTFILE");
				td.status = rs.getString("STATUS");
				td.reqDate = rs.getTimestamp("REQDATE");
				td.signDate = rs.getTimestamp("SIGNDATE");
				td.signDocId = rs.getString("SIGNDOCID");
				td.empNo = rs.getString("EMPNO");
				td.roleId = rs.getInt("ROLEID");
				td.docTitle = rs.getString("DOCTITLE");
				
				rList.add(td);
			}
			return rList;

		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			rs.close();
			stmt.close();
			conn.close();
		}
	}
	
	public ArrayList<TDocSign> getSignRequestsbyDate(Date fromDate, Date toDate) throws Exception {

		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		ArrayList<TDocSign> rList = new ArrayList<TDocSign>();
		try {
			conn = DBUtil.getECMDBConnection();
			stmt = conn.prepareStatement("SELECT * FROM ECM_ESIGN_DOCS WHERE "
					+ "REQDATE >= ? AND REQDATE < ? " 
					+ "ORDER BY REQDATE DESC");
			stmt.setTimestamp(1, new Timestamp(fromDate.getTime()));
			stmt.setTimestamp(2, new Timestamp(toDate.getTime()));
			rs = stmt.executeQuery();

			while (rs.next()) {

				TDocSign td = new TDocSign();
				td.id = rs.getInt("ID");
				td.docId = rs.getString("DOCID");
				td.inFile = rs.getString("INFILE");
				td.outFile = rs.getString("OUTFILE");
				td.status = rs.getString("STATUS");
				td.reqDate = rs.getTimestamp("REQDATE");
				td.signDate = rs.getTimestamp("SIGNDATE");
				td.signDocId = rs.getString("SIGNDOCID");
				td.empNo = rs.getString("EMPNO");
				td.roleId = rs.getInt("ROLEID");
				td.docTitle = rs.getString("DOCTITLE");
				
				rList.add(td);
			}
			return rList;

		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			rs.close();
			stmt.close();
			conn.close();
		}
	}
	
	public ArrayList<TDocSign> getSignedDocsbyDate(Date fromDate, Date toDate) throws Exception {

		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		ArrayList<TDocSign> rList = new ArrayList<TDocSign>();
		try {
			conn = DBUtil.getECMDBConnection();
			stmt = conn.prepareStatement("SELECT * FROM ECM_ESIGN_DOCS WHERE "
					+ "SIGNDATE >= ? AND SIGNDATE < ? " 
					+ "ORDER BY SIGNDATE DESC");
			stmt.setTimestamp(1, new Timestamp(fromDate.getTime()));
			stmt.setTimestamp(2, new Timestamp(toDate.getTime()));
			rs = stmt.executeQuery();

			while (rs.next()) {

				TDocSign td = new TDocSign();
				td.id = rs.getInt("ID");
				td.docId = rs.getString("DOCID");
				td.inFile = rs.getString("INFILE");
				td.outFile = rs.getString("OUTFILE");
				td.status = rs.getString("STATUS");
				td.reqDate = rs.getTimestamp("REQDATE");
				td.signDate = rs.getTimestamp("SIGNDATE");
				td.signDocId = rs.getString("SIGNDOCID");
				td.empNo = rs.getString("EMPNO");
				td.roleId = rs.getInt("ROLEID");
				td.docTitle = rs.getString("DOCTITLE");
				
				rList.add(td);
			}
			return rList;

		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			rs.close();
			stmt.close();
			conn.close();
		}
	}
	
	public TDocSign getRequest(long id) throws Exception {
		ECMDocSign sign = new ECMDocSign();
		sign.setId(id);
		sign.load();
		return sign.getTransport();
	}
	
	public ArrayList<TDocSign> getPendingSignRequests() throws Exception {

		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
	
		ArrayList<TDocSign> rList = new ArrayList<TDocSign>();
		try {
			conn = DBUtil.getECMDBConnection();
			stmt = conn.prepareStatement("SELECT * from ECM_ESIGN_DOCS WHERE STATUS = 'PENDING' " 
					+ "ORDER BY REQDATE ASC");
			rs = stmt.executeQuery();

			while (rs.next()) {

				TDocSign td = new TDocSign();
				td.id = rs.getInt("ID");
				td.docId = rs.getString("DOCID");
				td.inFile = rs.getString("INFILE");
				td.outFile = rs.getString("OUTFILE");
				td.status = rs.getString("STATUS");
				td.reqDate = rs.getTimestamp("REQDATE");
				td.signDate = rs.getTimestamp("SIGNDATE");
				td.signDocId = rs.getString("SIGNDOCID");
				td.empNo = rs.getString("EMPNO");
				td.roleId = rs.getInt("ROLEID");
				td.docTitle = rs.getString("DOCTITLE");
				
				rList.add(td);
			}
			return rList;

		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			rs.close();
			stmt.close();
			conn.close();
		}
	}
	
	public ArrayList<TDocSignItem> getOrgESignItems(String orgCode, String fDate, String tDate, long empNo) throws Exception {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		String sqlQuery = null;
		ArrayList<TDocSignItem> toList = new ArrayList<TDocSignItem>();
		
		try {
			String wiUserSQL = " ";

			String dateStr = "B.ReqDate";
			
			
			if(empNo > 0)
				wiUserSQL = "AND B.EMPNO = '"
							+ DBUtil.escapeString(ECMUserList.getInstance().getKOCId(empNo))
							+ "'";
			
			conn = DBUtil.getECMDBConnection();
			
			sqlQuery = "select A.OrgCode, A.description, "
					+ "B.status, "
					+ dateStr
					+ " AS ReqDate, B.SignDate As ESignDate, B.EmpNo, B.DocTitle, B.TYPE, "
					+ "C.FULLNAME AS EMPNAME "
					+ "FROM ECM_ORGUNIT A, ECM_ESIGN_DOCS B, ECM_USER C "
					+ "WHERE B.EMPNO = C.KOCID AND isparentof(getorgid(?),A.id) = 'YES' AND C.ORGCODE = A.ORGCODE "
					+ wiUserSQL + " AND " + dateStr
					+ " >= TO_DATE(?,'DD/MM/YYYY') AND " + dateStr
					+ " < TO_DATE(?,'DD/MM/YYYY') " + "ORDER BY A.hid ASC, "
					+ dateStr + " ASC";
			
			stmt = conn.prepareStatement(sqlQuery);
			stmt.setString(1, DBUtil.escapeString(orgCode));
			stmt.setString(2, DBUtil.escapeString(fDate));
			stmt.setString(3, DBUtil.escapeString(DBUtil.convertDateToShortString(DBUtil.addDaystoGivenDate(DBUtil.convertStringtoDate(tDate), 1),"")));
			
			rs = stmt.executeQuery();
			while (rs.next()) {
				TDocSignItem tds = new TDocSignItem();
				tds.status = rs.getString("Status");
				tds.reqDate = DBUtil.convertDateTimeToString(rs.getTimestamp("ReqDate"));
				tds.signDate = DBUtil.convertDateTimeToString(rs.getTimestamp("ESignDate"));
				tds.empNo = rs.getInt("EMPNo");
				tds.empName = rs.getString("EMPNAME");
				tds.docTitle = rs.getString("DocTitle");
				tds.orgCode = rs.getString("OrgCode");
				tds.orgName = rs.getString("Description");
				tds.type = rs.getString("TYPE");
				
				toList.add(tds);
			} 
			return toList;
			
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			rs.close();
			stmt.close();
			conn.close();
		}
	}
	
	public ArrayList<TOrgUnit> getOrgESignCount(String orgCode, String userType,
			String fDate, String tDate, long empNo) throws Exception {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		String sqlQuery = null;
		ArrayList<TOrgUnit> toList = new ArrayList<TOrgUnit>();

		try {

			conn = DBUtil.getECMDBConnection();
			sqlQuery = "select ID, OrgCode, description, LENGTH(TRIM(A.HID)) AS OFFSET, "
					+ "(select count(*) from ECM_ESIGN_DOCS B where B.ReqDate >= TO_DATE(?, 'DD/MM/YYYY') "
					+ "AND B.ReqDate < TO_DATE(?, 'DD/MM/YYYY') "
					+ "AND (isuserindirectorgunit((select C.empno from ecm_user C where C.kocid = B.EmpNo), A.orgcode) = 'YES')) as eSigncount "
					+ "from ecm_orgunit A where isparentof(getorgid(?),id) = 'YES' order by hid asc";
			
			if (empNo > 0)
				sqlQuery = "select ID, OrgCode, description, LENGTH(TRIM(A.HID)) AS OFFSET, "
						+ "(select count(*) from ECM_ESIGN_DOCS B where B.ReqDate >= TO_DATE(?, 'DD/MM/YYYY') "
						+ "AND B.ReqDate < TO_DATE(?, 'DD/MM/YYYY') AND B.EMPNO = '"
						+ DBUtil.escapeString(ECMUserList.getInstance().getKOCId(empNo))
						+ "' AND (isuserindirectorgunit((select C.empno from ecm_user C where C.kocid = B.EmpNo), A.orgcode) = 'YES')) as eSigncount "
						+ "from ecm_orgunit A where orgcode = ? order by hid asc";

			stmt = conn.prepareStatement(sqlQuery);
			stmt.setString(1, DBUtil.escapeString(fDate));
			stmt.setString(2, DBUtil.escapeString(DBUtil.convertDateToShortString(
												  DBUtil.addDaystoGivenDate(DBUtil.convertStringtoDate(tDate), 1), "")));
			stmt.setString(3, DBUtil.escapeString(orgCode));
			rs = stmt.executeQuery();
			while (rs.next()) {
				TOrgUnit to = new TOrgUnit();
				to.desc = rs.getString("description");
				if (empNo > 0)
					to.desc = ECMUserList.getInstance().getUserFullName(empNo);

				to.id = rs.getInt("ID");
				to.orgCode = rs.getString("OrgCode");
				to.offset = rs.getInt("Offset");
				to.count = rs.getInt("eSigncount");
				toList.add(to);
			}
			return toList;

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
