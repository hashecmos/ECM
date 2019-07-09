package com.ecm.db.list;

import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import oracle.jdbc.OracleTypes;

import com.ecm.db.model.ECMInboxItems;
import com.ecm.db.model.ECMOrgReport;
import com.ecm.db.model.ECMSentItem;
import com.ecm.db.model.ECMSentitemTrack;
import com.ecm.db.model.ECMSentitems;
import com.ecm.db.model.ECMWorkItem;
import com.ecm.db.model.ECMWorkflow;
import com.ecm.db.transport.TDelegate;
import com.ecm.db.transport.TDocPrincipal;
import com.ecm.db.transport.TDocSignItem;
import com.ecm.db.transport.TMgmtReport;
import com.ecm.db.transport.TOrgUnit;
import com.ecm.db.transport.TRecallAction;
import com.ecm.db.transport.TRecipient;
import com.ecm.db.transport.TReportFilter;
import com.ecm.db.transport.TRole;
import com.ecm.db.transport.TUser;
import com.ecm.db.transport.TWorkflowHistory;
import com.ecm.db.transport.TWorkitemAction;
import com.ecm.db.transport.TWorkitemDetails;
import com.ecm.db.transport.TWorkitemInfo;
import com.ecm.db.transport.TWorkitemQuery;
import com.ecm.db.transport.TWorkitemSet;
import com.ecm.db.transport.TWorkitemStat;
import com.ecm.db.util.DBUtil;
import com.ecm.db.util.ExcelHelper;
import com.ecm.db.util.JasperHelper;
import com.ecm.db.util.PdfHelper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ECMWorkitemList {
	private long minRow = 0;
	private long maxRow = 10000;

	private ECMWorkitemList() {
	}

	public static ECMWorkitemList getInstance() {
		return new ECMWorkitemList();
	}

	public long launchWorkflow(TWorkitemAction twa) throws Exception {
		ECMWorkflow wf = new ECMWorkflow();
		wf.getFromTransport(twa);
		wf.launch();
		if (twa.draftId > 0)
			deleteDraftItem(twa.draftId);
		return wf.getId();
	}

	public long forwardWorkitem(TWorkitemAction twa) throws Exception {
		ECMWorkItem wi = new ECMWorkItem();
		wi.getFromActionTransport(twa);
		wi.forward();
		if (twa.draftId > 0)
			deleteDraftItem(twa.draftId);
		return wi.getId();
	}

	public long replyWorkitem(TWorkitemAction twa) throws Exception {
		ECMWorkItem wi = new ECMWorkItem();
		wi.getFromActionTransport(twa);
		wi.reply();
		if (twa.draftId > 0)
			deleteDraftItem(twa.draftId);
		return wi.getId();
	}

	public void addUserToWorkitem(TWorkitemAction twa) throws Exception {
		ECMWorkItem wi = new ECMWorkItem();
		wi.getFromActionTransport(twa);
		wi.addUser();
		if (twa.draftId > 0)
			deleteDraftItem(twa.draftId);
	}

	public ArrayList<TDocPrincipal> recallWorkitems(TRecallAction recall)
			throws Exception {
		ArrayList<TDocPrincipal> retList = new ArrayList<TDocPrincipal>();
		if ((recall != null) && (recall.items != null)) {
			for (long witmId : recall.items) {
				ArrayList<TDocPrincipal> wiRet = recallWorkitem(recall.empNo,
						recall.roleId, witmId);
				retList.addAll(wiRet);
			}
		}
		return retList;
	}

	private ArrayList<TDocPrincipal> recallWorkitem(long empNo, long roleId,
			long witemId) throws Exception {
		ECMWorkItem wi = new ECMWorkItem();
		wi.setId(witemId);
		wi.setEMPNo(empNo);
		wi.setRoleId(roleId);
		return wi.recall();
	}

	public ArrayList<TDocPrincipal> recallSentitem(long sentItemId, long empNo,
			long roleId) throws Exception {
		ECMSentItem si = new ECMSentItem();
		si.setId(sentItemId);
		si.setSenderEmpNo(empNo);
		si.setSenderRoleId(roleId);
		return si.recall();
	}

	public void archiveWorkitem(long witemId, long empNo, long roleId)
			throws Exception {
		ECMWorkItem wi = new ECMWorkItem();
		wi.setId(witemId);
		wi.archive(empNo, roleId);
	}

	public String archiveWorkitemBefore(String empNo, String roleId,
			String bDate) throws Exception {
		String strMessage = "";
		ECMWorkItem wi = new ECMWorkItem();
		strMessage = wi.archiveBefore(empNo, roleId, bDate);
		return strMessage;
	}

	public void finishWorkitem(long witemId, long empNo, long roleId)
			throws Exception {
		ECMWorkItem wi = new ECMWorkItem();
		wi.setId(witemId);
		wi.finish(empNo, roleId);
	}

	public String finishWorkitemBefore(String empNo, String roleId, String bDate)
			throws Exception {
		String strMessage = "";
		ECMWorkItem wi = new ECMWorkItem();
		strMessage = wi.finishBefore(empNo, roleId, bDate);
		return strMessage;
	}

	public void archiveSentitem(long sentItemId, long empNo, long roleId)
			throws Exception {
		ECMSentItem si = new ECMSentItem();
		si.setId(sentItemId);
		si.archive(empNo, roleId);
	}

	public String archiveSentitemBefore(String empNo, String roleId,
			String bDate) throws Exception {
		String strMessage = "";
		ECMSentItem si = new ECMSentItem();
		strMessage = si.archiveBefore(empNo, roleId, bDate);
		return strMessage;
	}

	public void readWorkitem(long witemId, long empNo) throws Exception {
		try {
			ECMWorkItem wi = new ECMWorkItem();
			wi.setId(witemId);
			wi.read(empNo);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public TWorkitemSet getUserInboxItems(long empNo, long pageNo, String sort,
			String order) throws Exception {
		return getUserWorkItems(empNo, "ACTIVE", pageNo, sort, order, "",
				"DEFAULT");
	}

	public TWorkitemSet getRoleInboxItems(long RoleId, long empNo, long pageNo,
			String sort, String order) throws Exception {
		return getRoleWorkItems(RoleId, empNo, "ACTIVE", pageNo, sort, order,
				"", "DEFAULT");
	}

	public TWorkitemSet getUserSentItems(long empNo, long pageNo, String sort,
			String order) throws Exception {
		return getUserSentItems(empNo, "ACTIVE", pageNo, sort, order);
	}

	public TWorkitemSet getRoleSentItems(long RoleId, long empNo, long pageNo,
			String sort, String order) throws Exception {
		return getRoleSentItems(RoleId, empNo, "ACTIVE", pageNo, sort, order);
	}

	public TWorkitemSet getUserArchiveInboxItems(long empNo, long pageNo,
			String sort, String order) throws Exception {
		return getUserWorkItems(empNo, "FINISH", pageNo, sort, order, "",
				"DEFAULT");
	}

	public TWorkitemSet getRoleArchiveInboxItems(long RoleId, long empNo,
			long pageNo, String sort, String order) throws Exception {
		return getRoleWorkItems(RoleId, empNo, "FINISH", pageNo, sort, order,
				"", "DEFAULT");
	}

	public TWorkitemSet getUserArchiveSentItems(long empNo, long pageNo,
			String sort, String order) throws Exception {
		return getUserSentItems(empNo, "ARCHIVE", pageNo, sort, order);
	}

	public TWorkitemSet getRoleArchiveSentItems(long RoleId, long empNo,
			long pageNo, String sort, String order) throws Exception {
		return getRoleSentItems(RoleId, empNo, "ARCHIVE", pageNo, sort, order);
	}

	public TWorkitemSet getSentItemWorkItems(long sentItemid, long pageNo,
			String status) throws Exception {
		ECMSentItem si = new ECMSentItem();
		si.setId(sentItemid);
		si.setStatus(status);
		return si.getWorkitems();
	}

	private TWorkitemSet getUserWorkItems(long empNo, String sysStatus,
			long pageNo, String sort, String order, String itemType,
			String dashBoard) throws Exception {
		if (empNo <= 0)
			return null;
		if ((sysStatus == null) || (sysStatus.length() <= 0))
			sysStatus = "ACTIVE";
		TWorkitemQuery tq = new TWorkitemQuery();
		tq.userId = empNo;
		tq.userType = "USER";
		tq.empNo = empNo;
		tq.pageNo = pageNo;
		tq.sort = sort;
		tq.order = order;
		if (dashBoard != null && dashBoard.equalsIgnoreCase("TODAY")) {
			tq.receivedDate = DBUtil.getTodayDate();
		}

		if (itemType != null
				&& (itemType.equalsIgnoreCase("TO") || itemType
						.equalsIgnoreCase("CC")))
			tq.type = itemType.toUpperCase();

		return searchUserInboxItemsCount(tq, sysStatus);
	}

	/*
	 * 
	 * private ArrayList<TWorkitemInfo> getUserWorkItems(long empNo, String
	 * 
	 * sysStatus, long pageSize, long pageNo) throws Exception { if(empNo <= 0)
	 * 
	 * return null; if((sysStatus == null) || (sysStatus.length() <= 0))
	 * 
	 * sysStatus = "ACTIVE";
	 * 
	 * 
	 * 
	 * ArrayList<TWorkitemInfo> wiList = null;
	 * 
	 * 
	 * 
	 * Connection conn = null; ResultSet rs = null; String sqlQuery = null;
	 * 
	 * CallableStatement callableStatement = null; try { conn =
	 * 
	 * DBUtil.getECMDBConnection(); sqlQuery =
	 * 
	 * "{call ECM_GET_USER_WORKITEMS(?,?,?)}"; callableStatement =
	 * 
	 * conn.prepareCall(sqlQuery); callableStatement.setInt(1, (int)empNo);
	 * 
	 * callableStatement.setString(2, DBUtil.escapeString(sysStatus));
	 * 
	 * callableStatement.registerOutParameter(3, OracleTypes.CURSOR);
	 * 
	 * callableStatement.execute(); rs =
	 * 
	 * (ResultSet)callableStatement.getObject(3); while (rs.next()) {
	 * 
	 * TWorkitemInfo wi = new TWorkitemInfo(); wi.workitemId=rs.getInt("ID");
	 * 
	 * wi.priority=rs.getInt("Priority"); wi.subject=rs.getString("Subject");
	 * 
	 * wi.status=rs.getString("Status");
	 * 
	 * wi.instructions=rs.getString("Instructions");
	 * 
	 * wi.type=rs.getString("Type");
	 * 
	 * wi.deadline=DBUtil.convertDateTimeToString(rs.getTimestamp("Deadline"));
	 * 
	 * wi.reminder=DBUtil.convertDateTimeToString(rs.getTimestamp("Reminder"));
	 * 
	 * wi.receivedDate=DBUtil.convertDateTimeToString(rs.getTimestamp(
	 * 
	 * "CreatedDate")); //wi.actionName=rs.getString("Name");
	 * 
	 * wi.wfCreatorName=rs.getString("WFCreatorName");
	 * 
	 * wi.recipientName=rs.getString("RecipientName");
	 * 
	 * wi.senderName=rs.getString("SenderName");
	 * 
	 * wi.sentitemId=rs.getInt("ParentSentItemID"); if(wi.senderName == null)
	 * 
	 * wi.senderName=rs.getString("SenderRoleName");
	 * 
	 * wi.comments=rs.getString("Comments");
	 * 
	 * 
	 * 
	 * if(wiList == null) wiList = new ArrayList<TWorkitemInfo>();
	 * 
	 * wiList.add(wi); }
	 * 
	 * 
	 * 
	 * return wiList; } catch (Exception e) { e.printStackTrace(); throw new
	 * 
	 * Exception(e.getMessage()); } finally { rs.close();
	 * 
	 * callableStatement.close(); conn.close(); } }
	 */
	private TWorkitemSet getRoleWorkItems(long roleId, long empNo,
			String sysStatus, long pageNo, String sort, String order,
			String itemType, String dashBoard) throws Exception {
		if (roleId <= 0)
			return null;
		if ((sysStatus == null) || (sysStatus.length() <= 0))
			sysStatus = "ACTIVE";
		TWorkitemQuery tq = new TWorkitemQuery();
		tq.userId = roleId;
		tq.userType = "ROLE";
		tq.empNo = empNo;
		tq.pageNo = pageNo;
		tq.sort = sort;
		tq.order = order;
		if (dashBoard != null && dashBoard.equalsIgnoreCase("TODAY")) {
			tq.receivedDate = DBUtil.getTodayDate();
		}

		if (itemType != null
				&& (itemType.equalsIgnoreCase("TO") || itemType
						.equalsIgnoreCase("CC")))
			tq.type = itemType.toUpperCase();

		return searchRoleInboxItemsCount(tq, sysStatus);
	}

	@SuppressWarnings("unused")
	private TWorkitemSet getUserSentItemsByWorkflow (long empNo, String sysStatus,
			long pageNo, String sort, String order) throws Exception {
		if (empNo <= 0)
			return null;
		if ((sysStatus == null) || (sysStatus.length() <= 0))
			sysStatus = "ACTIVE";
		TWorkitemQuery tq = new TWorkitemQuery();
		tq.userId = empNo;
		tq.userType = "USER";
		tq.empNo = empNo;
		tq.pageNo = pageNo;
		tq.sort = sort;
		tq.order = order;
		return searchUserSentItemsCountByWorkflow(tq, sysStatus);
	}
	
	private TWorkitemSet getUserSentItems(long empNo, String sysStatus,
			long pageNo, String sort, String order) throws Exception {
		if (empNo <= 0)
			return null;
		if ((sysStatus == null) || (sysStatus.length() <= 0))
			sysStatus = "ACTIVE";
		TWorkitemQuery tq = new TWorkitemQuery();
		tq.userId = empNo;
		tq.userType = "USER";
		tq.empNo = empNo;
		tq.pageNo = pageNo;
		tq.sort = sort;
		tq.order = order;
		return searchUserSentItemsCount(tq, sysStatus);
	}

	@SuppressWarnings("unused")
	private TWorkitemSet getRoleSentItemsByWorkflow(long roleId, long empNo,
			String sysStatus, long pageNo, String sort, String order)
			throws Exception {
		if (roleId <= 0)
			return null;
		if ((sysStatus == null) || (sysStatus.length() <= 0))
			sysStatus = "ACTIVE";
		TWorkitemQuery tq = new TWorkitemQuery();
		tq.userId = roleId;
		tq.userType = "ROLE";
		tq.empNo = empNo;
		tq.pageNo = pageNo;
		tq.sort = sort;
		tq.order = order;
		return searchRoleSentItemsCountByWorkflow(tq, sysStatus);
	}
	
	private TWorkitemSet getRoleSentItems(long roleId, long empNo,
			String sysStatus, long pageNo, String sort, String order)
			throws Exception {
		if (roleId <= 0)
			return null;
		if ((sysStatus == null) || (sysStatus.length() <= 0))
			sysStatus = "ACTIVE";
		TWorkitemQuery tq = new TWorkitemQuery();
		tq.userId = roleId;
		tq.userType = "ROLE";
		tq.empNo = empNo;
		tq.pageNo = pageNo;
		tq.sort = sort;
		tq.order = order;
		return searchRoleSentItemsCount(tq, sysStatus);
	}

	public TWorkitemDetails getWorkitemDetails(long witemId) throws Exception {
		ECMWorkItem wi = new ECMWorkItem();
		wi.setId(witemId);
		return wi.getDetails();
	}

	public TWorkitemDetails getFirstWorkitemDetails(long sitemId)
			throws Exception {
		return getWorkitemDetails(getFirstWorkitemId(sitemId));
	}

	private long getFirstWorkitemId(long sitemId) throws Exception {
		ECMSentItem si = new ECMSentItem();
		si.setId(sitemId);
		return si.getFirstWorkitemId();
	}

	public ArrayList<TRecipient> getSentitemRecipients(long sitemId)
			throws Exception {
		ECMSentItem si = new ECMSentItem();
		si.setId(sitemId);
		return si.getRecipients();
	}

	public ArrayList<TWorkflowHistory> getWorkitemHistory(long witemId,
			long empNo) throws Exception {
		ECMWorkItem wi = new ECMWorkItem();
		wi.setId(witemId);
		return wi.getHistory(empNo);
	}

	public ArrayList<TWorkflowHistory> getSentitemHistory(long sitemId,
			long empNo) throws Exception {
		ECMSentItem si = new ECMSentItem();
		si.setId(sitemId);
		return si.getHistory(empNo);
	}

	public TWorkitemStat getWorkItemStatistics(long userID, String userType,
			String reportType, String itemType) throws Exception {
		if (userID <= 0)
			return null;
		if (reportType.equalsIgnoreCase("TODAY")
				|| reportType.equalsIgnoreCase("TOTAL"))
			return getUserStatistics(userID, userType.trim().toUpperCase(),
					reportType.trim().toUpperCase(), itemType.trim()
							.toUpperCase());
		else if (reportType.equalsIgnoreCase("DEADLINE"))
			return getDeadlineStatistics(userID, userType.trim().toUpperCase(),
					itemType.trim().toUpperCase(), "INBOX");
		else
			return null;
	}

	// Abhishek
	public TWorkitemStat getDashboardStatistics(long userID, String userType,
			String reportType, String itemType, String dType) throws Exception {
		String sysStatus = "ACTIVE";

		if (userID <= 0)
			return null;

		TWorkitemStat userTS = null;

		if (userType.equalsIgnoreCase("USER")) {
			if (reportType.equalsIgnoreCase("TOTAL")) {
				userTS = new TWorkitemStat();
				// TWorkitemSet ts = getUserWorkItems(userID,sysStatus,0,"","",
				// itemType, "TOTAL");
				TWorkitemSet ta = searchUserActionedItemsCount(userID,
						sysStatus, "TOTAL");
				int unreadCount = searchUserInboxNewItemsCount(userID,
						sysStatus, itemType, "TOTAL");
				int readCount = searchUserInboxReadItemsCount(userID,
						sysStatus, itemType, "TOTAL");

				if (itemType.equalsIgnoreCase("ALL")) {
					userTS.total = readCount + unreadCount
							+ (int) ta.totalCount;
					userTS.reply = (int) ta.totalCount;
				} else {
					userTS.total = readCount + unreadCount;
					userTS.reply = 0;
				}
				userTS.unread = unreadCount;
				userTS.read = readCount;
			} else if (reportType.equalsIgnoreCase("TODAY")) {
				userTS = new TWorkitemStat();
				TWorkitemSet ta = searchUserActionedItemsCount(userID,
						sysStatus, "TODAY");
				int unreadCount = searchUserInboxNewItemsCount(userID,
						sysStatus, itemType, "TODAY");
				int readCount = searchUserInboxReadItemsCount(userID,
						sysStatus, itemType, "TODAY");

				if (itemType.equalsIgnoreCase("ALL")) {
					userTS.total = readCount + unreadCount
							+ (int) ta.totalCount;
					userTS.reply = (int) ta.totalCount;
				} else {
					userTS.total = readCount + unreadCount;
					userTS.reply = 0;
				}

				userTS.unread = unreadCount;
				userTS.read = readCount;
			} else if (reportType.equalsIgnoreCase("DEADLINE")) {
				if (dType.equalsIgnoreCase("INBOX"))
					return getDeadlineStatistics(userID, userType.trim()
							.toUpperCase(), itemType.trim().toUpperCase(),
							dType.trim().toUpperCase());
				else
					return getDeadlineStatistics(userID, userType.trim()
							.toUpperCase(), itemType.trim().toUpperCase(),
							dType.trim().toUpperCase());
			} else
				return null;
		} else if (userType.equalsIgnoreCase("ROLE")) {
			if (reportType.equalsIgnoreCase("TOTAL")) {
				userTS = new TWorkitemStat();
				// TWorkitemSet ts = getRoleWorkItems(userID, 0, sysStatus, 0,
				// "", "", itemType, "TOTAL");
				// userTS.total = (int)ts.totalCount;
				TWorkitemSet ta = searchRoleActionedItemsCount(userID,
						sysStatus, "TOTAL");
				int unreadCount = searchRoleInboxNewItemsCount(userID,
						sysStatus, itemType, "TOTAL");
				int readCount = searchRoleInboxReadItemsCount(userID,
						sysStatus, itemType, "TOTAL");

				if (itemType.equalsIgnoreCase("ALL")) {
					userTS.total = readCount + unreadCount
							+ (int) ta.totalCount;
					userTS.reply = (int) ta.totalCount;
				} else {
					userTS.total = readCount + unreadCount;
					userTS.reply = 0;
				}

				userTS.unread = unreadCount;
				userTS.read = readCount;
			} else if (reportType.equalsIgnoreCase("TODAY")) {
				userTS = new TWorkitemStat();
				// TWorkitemSet ts = getRoleWorkItems(userID, 0, sysStatus, 0,
				// "", "", itemType, "TOTAL");
				// userTS.total = (int)ts.totalCount;
				TWorkitemSet ta = searchRoleActionedItemsCount(userID,
						sysStatus, "TODAY");
				int unreadCount = searchRoleInboxNewItemsCount(userID,
						sysStatus, itemType, "TODAY");
				int readCount = searchRoleInboxReadItemsCount(userID,
						sysStatus, itemType, "TODAY");

				if (itemType.equalsIgnoreCase("ALL")) {
					userTS.total = readCount + unreadCount
							+ (int) ta.totalCount;
					userTS.reply = (int) ta.totalCount;
				} else {
					userTS.total = readCount + unreadCount;
					userTS.reply = 0;
				}

				userTS.unread = unreadCount;
				userTS.read = readCount;
			} else if (reportType.equalsIgnoreCase("DEADLINE")) {
				if (dType.equalsIgnoreCase("INBOX"))
					return getDeadlineStatistics(userID, userType.trim()
							.toUpperCase(), itemType.trim().toUpperCase(),
							dType.trim().toUpperCase());
				else
					return getDeadlineStatistics(userID, userType.trim()
							.toUpperCase(), itemType.trim().toUpperCase(),
							dType.trim().toUpperCase());
			} else
				return null;
		}

		return userTS;
	}

	private TWorkitemStat getUserStatistics(long userID, String userType,
			String reportType, String itemType) throws Exception {
		Connection conn = null;
		ResultSet rs = null;
		String sqlQuery = null;
		CallableStatement callableStatement = null;
		try {
			conn = DBUtil.getECMDBConnection();
			sqlQuery = "{call ECM_GET_USER_STATISTICS(?,?,?,?,?)}";
			callableStatement = conn.prepareCall(sqlQuery);
			callableStatement.setInt(1, (int) userID);
			callableStatement.setString(2, DBUtil.escapeString(userType));
			callableStatement.setString(3, DBUtil.escapeString(reportType));
			callableStatement.setString(4, DBUtil.escapeString(itemType));
			callableStatement.registerOutParameter(5, OracleTypes.CURSOR);
			callableStatement.execute();
			rs = (ResultSet) callableStatement.getObject(5);
			TWorkitemStat ws = new TWorkitemStat();
			if ((rs != null) && (rs.next())) {
				ws.total = rs.getInt("Total");
				ws.unread = rs.getInt("New");
				ws.forward = rs.getInt("Forward");
				ws.reply = rs.getInt("Reply");
			}
			return ws;
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			rs.close();
			callableStatement.close();
			conn.close();
		}
	}

	@SuppressWarnings("unused")
	private TWorkitemStat getDeadlineStatistics(long userID, String userType,
			String itemType) throws Exception {
		Connection conn = null;
		ResultSet rs = null;
		String sqlQuery = null;
		CallableStatement callableStatement = null;
		try {
			conn = DBUtil.getECMDBConnection();
			sqlQuery = "{call ECM_GET_DEADLINE_STATISTICS(?,?,?,?)}";
			callableStatement = conn.prepareCall(sqlQuery);
			callableStatement.setInt(1, (int) userID);
			callableStatement.setString(2, DBUtil.escapeString(userType));
			callableStatement.setString(3, DBUtil.escapeString(itemType));
			callableStatement.registerOutParameter(4, OracleTypes.CURSOR);
			callableStatement.execute();
			rs = (ResultSet) callableStatement.getObject(4);
			TWorkitemStat ws = null;
			if ((rs != null) && (rs.next())) {
				ws = new TWorkitemStat();
				ws.read = rs.getInt("ReadItems");
				ws.unread = rs.getInt("UnreadItems");
				ws.total = ws.read + ws.unread;
			}
			return ws;
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			rs.close();
			callableStatement.close();
			conn.close();
		}
	}

	private TWorkitemStat getDeadlineStatistics(long userID, String userType,
			String itemType, String dType) throws Exception {
		Connection conn = null;
		ResultSet rs = null;
		String sqlQuery = null;
		CallableStatement callableStatement = null;
		try {
			conn = DBUtil.getECMDBConnection();
			sqlQuery = "{call ECM_GET_OVERDUE_STATISTICS(?,?,?,?,?)}";
			callableStatement = conn.prepareCall(sqlQuery);
			callableStatement.setInt(1, (int) userID);
			callableStatement.setString(2, DBUtil.escapeString(userType));
			callableStatement.setString(3, DBUtil.escapeString(itemType));
			callableStatement.setString(4, DBUtil.escapeString(dType));
			callableStatement.registerOutParameter(5, OracleTypes.CURSOR);
			callableStatement.execute();
			rs = (ResultSet) callableStatement.getObject(5);
			TWorkitemStat ws = null;
			if ((rs != null) && (rs.next())) {
				ws = new TWorkitemStat();
				ws.read = rs.getInt("ReadItems");
				ws.unread = rs.getInt("UnreadItems");
				ws.total = ws.read + ws.unread;
			}
			return ws;
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			rs.close();
			callableStatement.close();
			conn.close();
		}
	}

	private String getDBColumnString(String propName) {
		String retString = null;
		if (propName == null)
			return retString;
		if (propName.equalsIgnoreCase("Id"))
			retString = "A.ID";
		else if (propName.equalsIgnoreCase("Priority"))
			retString = "A.Priority";
		else if (propName.equalsIgnoreCase("ActionID"))
			retString = "A.ActionID";
		else if (propName.equalsIgnoreCase("Subject"))
			retString = "UPPER(B.Subject)";
		else if (propName.equalsIgnoreCase("Status"))
			retString = "A.Status";
		else if (propName.equalsIgnoreCase("Instructions"))
			retString = "A.Instructions";
		else if (propName.equalsIgnoreCase("Type"))
			retString = "A.Type";
		else if (propName.equalsIgnoreCase("Actions"))
			retString = "UPPER(A.Actions)";
		else if (propName.equalsIgnoreCase("Deadline"))
			retString = "A.Deadline";
		else if (propName.equalsIgnoreCase("Reminder"))
			retString = "A.Reminder";
		else if (propName.equalsIgnoreCase("CreatedDate"))
			retString = "A.CreatedDate";
		else if (propName.equalsIgnoreCase("WFCreatorName"))
			retString = "WFCreatorName";
		else if (propName.equalsIgnoreCase("RecipientEMPNo"))
			retString = "A.RecipientEMPNo";
		else if (propName.equalsIgnoreCase("SenderEMPNo"))
			retString = "A.SenderEMPNo";
		else if (propName.equalsIgnoreCase("SenderRoleID"))
			retString = "A.SenderRoleID";
		else if (propName.equalsIgnoreCase("RecipientRoleID"))
			retString = "A.RecipientRoleID";
		else if (propName.equalsIgnoreCase("Comments"))
			retString = "A.Comments";
		else if (propName.equalsIgnoreCase("Keywords"))
			retString = "A.Keywords";
		else if (propName.equalsIgnoreCase("SenderName")) // Abhishek
			retString = "SenderName @, SenderRoleName";
		else if (propName.equalsIgnoreCase("Remarks")) // prvn
			retString = "A.Remarks";
		return retString;
	}

	private String getDBColumnString(String propName, boolean isSent) {
		String retString = null;
		if (propName == null)
			return retString;
		if (propName.equalsIgnoreCase("Id"))
			retString = "A.ID";
		else if (propName.equalsIgnoreCase("Priority"))
			if (isSent)
				retString = "A.Priority";
			else
				retString = "B.Priority";
		else if (propName.equalsIgnoreCase("Subject"))
			retString = "UPPER(B.Subject)";
		else if (propName.equalsIgnoreCase("Status"))
			retString = "A.Status";
		else if (propName.equalsIgnoreCase("Instructions"))
			retString = "A.Instructions";
		else if (propName.equalsIgnoreCase("ActionID"))
			retString = "A.ParentItemID";
		else if (propName.equalsIgnoreCase("Type"))
			retString = "A.Type";
		else if (propName.equalsIgnoreCase("Actions"))
			retString = "UPPER(A.Actions)";
		else if (propName.equalsIgnoreCase("Deadline"))
			retString = "A.Deadline";
		else if (propName.equalsIgnoreCase("Reminder"))
			retString = "A.Reminder";
		else if (propName.equalsIgnoreCase("CreatedDate"))
			retString = "A.CreatedDate";
		else if (propName.equalsIgnoreCase("WFCreatorName"))
			retString = "WfCreatorNameEmp @, WFCreatorName";
		else if (propName.equalsIgnoreCase("RecipientEMPNo"))
			retString = "A.RecipientEMPNo";
		else if (propName.equalsIgnoreCase("SenderEMPNo"))
			retString = "A.SenderEMPNo";
		else if (propName.equalsIgnoreCase("SenderRoleID"))
			retString = "A.SenderRoleID";
		else if (propName.equalsIgnoreCase("RecipientRoleID"))
			retString = "A.RecipientRoleID";
		else if (propName.equalsIgnoreCase("Comments"))
			retString = "A.Comments";
		else if (propName.equalsIgnoreCase("SenderName")) // Abhishek
			retString = "SenderName @, SenderRoleName";
		return retString;
	}

	private String getOrderByString(TWorkitemQuery twi, boolean isSent) {
		String orderBy = getDBColumnString(twi.sort, isSent);
		if (orderBy != null) {
			String so = " ASC";
			if ((twi.order != null)
					&& ((twi.order.equalsIgnoreCase("DESC")) || (twi.order
							.equalsIgnoreCase("D"))))
				so = " DESC";
			String strOrderBy = "";
			if (orderBy.contains("@"))
				strOrderBy = " ORDER BY " + orderBy.replace("@", so) + so;
			else
				strOrderBy = " ORDER BY " + orderBy + so;
			return strOrderBy;
		}
		return " ORDER By A.CREATEDDATE DESC";
	}

	private String getStringClause(String value, String name) {
		if ((value != null) && (value.length() > 0)
				&& !(value.equalsIgnoreCase("overdue"))
				&& !(value.equalsIgnoreCase("dueToday"))) {
			String dbCol = getDBColumnString(name);
			if (dbCol != null)
				return dbCol + " LIKE '%" + DBUtil.escapeString(value) + "%' ";
		}
		return "";
	}

	private String getStringClause(String value, String name, boolean bReciType) {
		if ((value != null) && (value.length() > 0) && bReciType) {
			String dbCol = "C.Type";
			if (dbCol != null)
				return dbCol + " LIKE '%" + DBUtil.escapeString(value) + "%' ";
		}
		return "";
	}

	private String getIntClause(long value, String name) {
		if (value > 0) {
			String dbCol = getDBColumnString(name);
			if (dbCol != null)
				return dbCol + " = " + value + " ";
		}
		return "";
	}

	private String getIntClause(long value, String name, boolean isSent) {
		if (value > 0) {
			String dbCol = getDBColumnString(name, isSent);
			if (dbCol != null)
				return dbCol + " = " + value + " ";
		}
		return "";
	}

	@SuppressWarnings("unused")
	private String getDateClause(String value, String name) {
		String retClause = "";
		try {
			if ((value != null) && (value.length() > 0)) {
				String[] vals = value.split(";");
				String fd = vals[0];
				String sd = "";
				if (vals.length > 1)
					sd = vals[1];
				String dbCol = getDBColumnString(name);
				if (dbCol != null) {
					// Change this for Oracle to
					retClause = dbCol + " >= TO_DATE('"
							+ DBUtil.escapeString(fd) + "','DD/MM/YYYY') ";
					// retClause = dbCol + " >= convert(date,'" +
					// DBUtil.escapeString(fd) + "',103) ";
					if ((sd != null) && (sd.length() > 0)) {
						// retClause += (" AND " + dbCol + " < convert(date,'" +
						// DBUtil.escapeString(sd) + "', 103) ");
						retClause += (" AND " + dbCol + " < TO_DATE('"
								+ DBUtil.escapeString(sd) + "','DD/MM/YYYY') ");
					}
				}
			}
		} catch (Exception e) {
			retClause = "";
		}
		return retClause;
	}

	private String getDateClause(String value, String name, String criteria,
			String type) {
		String retClause = "";
		try {
			if ((value != null) && (value.length() > 0)) {
				String[] vals = value.split(";");
				String fd = vals[0];
				String sd = "";
				if (vals.length > 1)
					sd = vals[1];
				String dbCol = getDBColumnString(name);
				if (dbCol != null) {
					// Change this for Oracle to
					if ((criteria != null) && (criteria.length() > 0)
							&& (criteria.equalsIgnoreCase("overdue"))) {
						if ((type != null)
								&& (type.equalsIgnoreCase("dueToday"))) {
							retClause = "trunc(" + dbCol + ") = trunc(SYSDATE)";
						} else {
							retClause = dbCol + " < SYSTIMESTAMP "; // " < TO_DATE('"
																	// +
																	// DBUtil.escapeString(fd)
																	// +
																	// "','DD/MM/YYYY') ";
						}
					} else {
						retClause = "trunc(" + dbCol + ") >= TO_DATE('"
								+ DBUtil.escapeString(fd) + "','DD/MM/YYYY') ";
						if (sd.equalsIgnoreCase("")) {
							String nd = DBUtil.convertDateToShortString(
									DBUtil.addDaystoGivenDate(
											DBUtil.convertStringtoDate(fd), 1),
									"");
							retClause += (" AND trunc(" + dbCol + ") < TO_DATE('"
									+ DBUtil.escapeString(nd) + "','DD/MM/YYYY') ");
						}
						// retClause = dbCol + " >= convert(date,'" +
						// DBUtil.escapeString(fd) + "',103) ";
						if ((sd != null) && (sd.length() > 0)) {
							// retClause += (" AND " + dbCol + " <
							// convert(date,'" + DBUtil.escapeString(sd) + "',
							// 103) ");
							retClause += (" AND trunc(" + dbCol + ") <= TO_DATE('"
									+ DBUtil.escapeString(sd) + "','DD/MM/YYYY') ");
						}
					}
				}
			}
		} catch (Exception e) {
			retClause = "";
		}
		return retClause;
	}

	private String splitandCheckTypes(String fullString, String optRole,
			String optUser) {
		String retString = "";
		if ((fullString != null) && (fullString.length() > 0)) {
			String[] types = fullString.trim().split("@");
			String roleTypes = types[0];
			retString = splitandCheck(roleTypes, optRole, optUser);
			if (types.length > 1) {
				String userTypes = types[1];
				retString = "(" + retString + " OR "
						+ splitandCheck(userTypes, optRole, optUser) + ")";
			}
		}
		return retString;
	}

	private String splitandCheck(String fullString, String optRole,
			String optUser) {
		String retString = "";
		if ((fullString != null) && (fullString.length() > 0)) {
			String[] props = fullString.trim().split(":");
			String type = props[0];
			String ids = "";
			if (props.length > 1)
				ids = props[1];
			String[] idlist = ids.split(";");
			String inClause = "";
			for (String id : idlist) {
				if (id.trim().length() > 0) {
					if (inClause.length() > 0)
						inClause += ",";
					inClause += id.trim();
				}
			}
			if (inClause.length() > 0) {
				inClause = ("(" + inClause + ")");
				String dbCol = "";
				if (type.equalsIgnoreCase("ROLE"))
					dbCol = getDBColumnString(optRole);
				else
					dbCol = getDBColumnString(optUser);
				retString = dbCol + " IN " + inClause + " ";
			}
		}
		return retString;
	}

	private String getQueryString(TWorkitemQuery twi, Boolean isSent) {
		String whereQuery = "";
		if (twi == null)
			return whereQuery;
		String propWhere = getStringClause(twi.comments, "Remarks");
		if (propWhere.length() > 0) {
			if (isSent) {
				whereQuery += ("AND " + propWhere);
			} else {
				String subQuery = "A.WORKFLOWID IN (SELECT DISTINCT X.ID FROM ECM_WORKFLOW X "
						+ "WHERE " + propWhere.replace("A.", "X.") + ") ";
				whereQuery += (" AND " + subQuery);
			}
		}

		propWhere = getStringClause(twi.keywords, "Keywords");
		if (propWhere.length() > 0) {
			String subQuery = "A.WORKFLOWID IN (SELECT DISTINCT X.ID FROM ECM_WORKFLOW X "
					+ "WHERE " + propWhere.replace("A.", "X.") + ") ";
			whereQuery += (" AND " + subQuery);
		}

		propWhere = getStringClause(twi.instructions, "Instructions");
		if (propWhere.length() > 0) {
			if (isSent) {
				String subQuery = "A.ID IN (SELECT DISTINCT X.PARENTSENTITEMID FROM ECM_WORKITEM X "
						+ "WHERE X.WORKFLOWID = B.ID AND "
						+ propWhere.replace("A.", "X.") + ") ";
				whereQuery += (" AND " + subQuery);
			} else {
				whereQuery += ("AND " + propWhere);
			}
		}
		propWhere = splitandCheckTypes(twi.recipientName, "RecipientRoleID",
				"RecipientEMPNo");
		if (propWhere.length() > 0) {
			if (isSent) {
				String subQuery = "A.ID IN (SELECT DISTINCT X.PARENTSENTITEMID FROM ECM_WORKITEM X "
						+ "WHERE X.WORKFLOWID = B.ID AND "
						+ propWhere.replace("A.", "X.") + ") ";
				whereQuery += (" AND " + subQuery);
			} else
				whereQuery += (" AND " + propWhere);
		}
		propWhere = splitandCheckTypes(twi.senderName, "SenderRoleID",
				"SenderEMPNo");
		if (propWhere.length() > 0)
			whereQuery += (" AND " + propWhere);
		if (twi.actionId == 0 || isSent) {
			propWhere = getStringClause(twi.status, "Status");
			if (propWhere.length() > 0) {
				if (isSent) {
					String subQuery = "A.ID IN (SELECT DISTINCT X.PARENTSENTITEMID FROM ECM_WORKITEM X "
							+ "WHERE X.WORKFLOWID = B.ID AND "
							+ propWhere.replace("A.", "X.") + ") ";
					whereQuery += (" AND " + subQuery);
				} else
					whereQuery += (" AND " + propWhere);
			}
		}
		propWhere = getStringClause(twi.subject, "Subject");
		if (propWhere.length() > 0)
			whereQuery += (" AND " + propWhere);

		propWhere = getStringClause(twi.type, "Type");
		if (propWhere.length() > 0) {
			if (isSent) {
				String subQuery = "A.ID IN (SELECT DISTINCT X.PARENTSENTITEMID FROM ECM_WORKITEM X "
						+ "WHERE X.WORKFLOWID = B.ID AND "
						+ propWhere.replace("A.", "X.") + ") ";
				whereQuery += (" AND " + subQuery);
			} else
				whereQuery += (" AND " + propWhere);
		}

		propWhere = getStringClause(twi.wfCreatorName, "WFCreatorName");
		if (propWhere.length() > 0)
			whereQuery += (" AND " + propWhere);
		propWhere = getIntClause(twi.priority, "Priority", isSent);
		if (propWhere.length() > 0)
			whereQuery += (" AND " + propWhere);
		if (!isSent && twi.actionId > 0) {
			propWhere = getIntClause(twi.actionId, "ActionID");
			if (propWhere.length() > 0)
				whereQuery += (" AND " + propWhere);
		}
		propWhere = getDateClause(twi.deadline, "Deadline", twi.status,
				twi.type);
		if (propWhere.length() > 0) {
			if (isSent) {
				String subQuery = "A.ID IN (SELECT DISTINCT X.PARENTSENTITEMID FROM ECM_WORKITEM X "
						+ "WHERE X.WORKFLOWID = B.ID AND "
						+ propWhere.replace("A.", "X.")
						+ getStatusQuery(twi.sysStatus) + ") ";
				whereQuery += (" AND " + subQuery);

			} else
				whereQuery += (" AND " + propWhere);
		}
		propWhere = getDateClause(twi.receivedDate, "CreatedDate", "search", "");
		if (propWhere.length() > 0)
			whereQuery += (" AND " + propWhere);

		propWhere = getStringClause(twi.actions, "Actions");
		if (propWhere.length() > 0) {
			if (isSent) {
				String subQuery = "A.ID IN (SELECT DISTINCT X.PARENTSENTITEMID FROM ECM_WORKITEM X "
						+ "WHERE X.WORKFLOWID = B.ID AND "
						+ propWhere.replace("A.", "X.") + ") ";
				whereQuery += (" AND " + subQuery);
			} else
				whereQuery += (" AND " + propWhere);
		}

		return whereQuery;
	}

	private String getStatusQuery(String wiStatus) {
		String statusCondition = "";
		if ((wiStatus != null) && (wiStatus.trim().length() > 0)) {
			if ((wiStatus.equalsIgnoreCase("New")))
				statusCondition = "AND X.Status = 'New' ";
			else if ((wiStatus.equalsIgnoreCase("Read")))
				statusCondition = "AND X.Status = 'Read' ";

			return statusCondition;
		}
		return "";
	}

	private String getActionedQueryString(TWorkitemQuery twi, Boolean isSent) {
		String whereQuery = "";
		if (twi == null)
			return whereQuery;
		String propWhere = getStringClause(twi.comments, "Remarks");
		if (propWhere.length() > 0) {
			/* whereQuery += (" AND " + propWhere); */
			if (isSent) {
				whereQuery += ("AND " + propWhere);
			} else {
				String subQuery = "A.WORKFLOWID IN (SELECT DISTINCT X.ID FROM ECM_WORKFLOW X "
						+ "WHERE " + propWhere.replace("A.", "X.") + ") ";
				whereQuery += (" AND " + subQuery);
			}
		}
		propWhere = getStringClause(twi.instructions, "Instructions");
		if (propWhere.length() > 0) {
			/* whereQuery += (" AND " + propWhere); */
			if (isSent) {
				String subQuery = "A.ID IN (SELECT DISTINCT X.PARENTSENTITEMID FROM ECM_WORKITEM X "
						+ "WHERE X.WORKFLOWID = B.ID AND "
						+ propWhere.replace("A.", "X.") + ") ";
				whereQuery += (" AND " + subQuery);
			} else {
				whereQuery += ("AND " + propWhere);
			}
		}
		propWhere = splitandCheckTypes(twi.recipientName, "RecipientRoleID",
				"RecipientEMPNo");
		if (propWhere.length() > 0) {
			if (isSent) {
				String subQuery = "A.ID IN (SELECT DISTINCT X.PARENTSENTITEMID FROM ECM_WORKITEM X "
						+ "WHERE X.WORKFLOWID = B.ID AND "
						+ propWhere.replace("A.", "X.") + ") ";
				whereQuery += (" AND " + subQuery);
			} else
				whereQuery += (" AND " + propWhere);
		}
		propWhere = splitandCheckTypes(twi.senderName, "SenderRoleID",
				"SenderEMPNo");
		if (propWhere.length() > 0)
			whereQuery += (" AND " + propWhere);
		if (twi.actionId == 0 || isSent) {
			propWhere = getStringClause(twi.status, "Status");
			if (propWhere.length() > 0) {
				if (isSent) {
					String subQuery = "A.ID IN (SELECT DISTINCT X.PARENTSENTITEMID FROM ECM_WORKITEM X "
							+ "WHERE X.WORKFLOWID = B.ID AND "
							+ propWhere.replace("A.", "X.") + ") ";
					whereQuery += (" AND " + subQuery);
				} else
					whereQuery += (" AND " + propWhere);
			}
		}
		propWhere = getStringClause(twi.subject, "Subject");
		if (propWhere.length() > 0)
			whereQuery += (" AND " + propWhere);
		propWhere = getStringClause(twi.type, "Type", true);
		if (propWhere.length() > 0) {
			if (isSent) {
				String subQuery = "A.ID IN (SELECT DISTINCT X.PARENTSENTITEMID FROM ECM_WORKITEM X "
						+ "WHERE X.WORKFLOWID = B.ID AND "
						+ propWhere.replace("A.", "X.") + ") ";
				whereQuery += (" AND " + subQuery);
			} else
				whereQuery += (" AND " + propWhere);
		}
		propWhere = getStringClause(twi.wfCreatorName, "WFCreatorName");
		if (propWhere.length() > 0)
			whereQuery += (" AND " + propWhere);
		propWhere = getIntClause(twi.priority, "Priority", isSent);
		if (propWhere.length() > 0)
			whereQuery += (" AND " + propWhere);
		if (!isSent && twi.actionId > 0) {
			propWhere = getIntClause(twi.actionId, "ActionID");
			if (propWhere.length() > 0)
				whereQuery += (" AND " + propWhere.replace("A.", "C."));
		}
		propWhere = getDateClause(twi.deadline, "Deadline", twi.status, "");
		if (propWhere.length() > 0) {
			if (isSent) {
				String subQuery = "A.ID IN (SELECT DISTINCT X.PARENTSENTITEMID FROM ECM_WORKITEM X "
						+ "WHERE X.WORKFLOWID = B.ID AND "
						+ propWhere.replace("A.", "X.") + ") ";
				whereQuery += (" AND " + subQuery);
			} else
				whereQuery += (" AND " + propWhere);
		}
		propWhere = getDateClause(twi.receivedDate, "CreatedDate", "search", "");
		if (propWhere.length() > 0)
			whereQuery += (" AND " + propWhere);
		return whereQuery;
	}

	private TWorkitemSet searchUserInboxItemsCount(TWorkitemQuery twi,
			String sysStatus) throws Exception {
		TWorkitemSet ts = null;
		if ((sysStatus == null) || (sysStatus.trim().length() <= 0))
			sysStatus = "ACTIVE";
		Connection conn = null;
		try {
			conn = DBUtil.getECMDBConnection();
			String countQuery = "SELECT count(A.ID) AS TotalCount FROM ECM_WORKITEM A, "
					+ "ECM_USER C "
					+ "WHERE  A.RecipientEMPNo = "
					+ twi.userId
					+ " AND A.Status in ('New', 'Read', 'Finished') "
					+ "AND C.EMPNo = A.RecipientEMPNo AND A.SystemStatus = '"
					+ sysStatus + "' " + getQueryString(twi, false);
			ts = getWorkitemSet(conn, twi.empNo, countQuery, twi.pageNo);
			return ts;
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			conn.close();
		}
	}

	private TWorkitemSet searchUserInboxItems(TWorkitemQuery twi,
			String sysStatus, String wiStatus) throws Exception {
		TWorkitemSet ts = null;
		String statusCondition = "AND A.Status in ('New', 'Read', 'Finished') ";
		ArrayList<TWorkitemInfo> wiList = new ArrayList<TWorkitemInfo>();
		if ((sysStatus == null) || (sysStatus.trim().length() <= 0))
			sysStatus = "ACTIVE";
		Connection conn = null;
		ResultSet rs = null;
		String sqlQuery = null;
		PreparedStatement ps = null;
		try {
			conn = DBUtil.getECMDBConnection();
			if ((wiStatus != null) && (wiStatus.trim().length() > 0)) {
				if ((wiStatus.equalsIgnoreCase("New")))
					statusCondition = "AND A.Status = 'New' ";
				else if ((wiStatus.equalsIgnoreCase("Read")))
					statusCondition = "AND A.Status = 'Read' ";
			}
			String countQuery = "SELECT count(A.ID) AS TotalCount FROM ECM_WORKITEM A, "
					+ "ECM_USER C, ECM_WORKFLOW B "
					+ "WHERE  A.RecipientEMPNo = "
					+ twi.userId
					+ "AND A.WorkflowID = B.ID "
					+ statusCondition
					+ "AND C.EMPNo = A.RecipientEMPNo AND A.SystemStatus = '"
					+ sysStatus + "' " + getQueryString(twi, false);

			ts = getWorkitemSet(conn, twi.empNo, countQuery, twi.pageNo);
			ts.workitems = wiList;
			setPaginationClause(ts);

			sqlQuery = "SELECT B.Priority, B.Subject, A.Status, A.Instructions, A.Type, A.ActionID, "
					+ "A.Deadline, A.ID, A.Reminder, A.CreatedDate, A.Actions, "
					+ "A.Comments, "
					+ "(SELECT FullName FROM ECM_USER WHERE EMPNo = B.CREATEDBY  AND ROWNUM = 1) AS WfCreatorNameEmp, "
					+ "(SELECT Name FROM ECM_ROLE WHERE ID = B.ROLEID  AND ROWNUM = 1) AS WfCreatorName, "
					+ "(SELECT FullName FROM ECM_USER WHERE EMPNo = A.SenderEMPNo  AND ROWNUM = 1) AS SenderName, "
					+ "(SELECT FullName FROM ECM_USER WHERE EMPNo = A.RecipientEMPNo  AND ROWNUM = 1) AS RecipientName, "
					+ "(SELECT Name FROM ECM_ROLE WHERE ID = A.SenderRoleID  AND ROWNUM = 1) AS SenderRoleName, "
					+ "(SELECT COUNT(*) FROM ECM_WORKITEM WHERE WorkflowID = A.WorkflowID AND RecipientEMPNo = A.RecipientEMPNo AND SystemStatus != 'RECALL') AS RecvCount "
					+ "FROM ECM_WORKITEM A, ECM_WORKFLOW B "
					+ "WHERE  A.RecipientEMPNo = "
					+ twi.userId
					+ " AND A.WorkflowID = B.ID "
					+ statusCondition
					+ "AND A.SystemStatus = '"
					+ sysStatus
					+ "' "
					+ getQueryString(twi, false) + getOrderByString(twi, false);

			String pageWhere = "SELECT * FROM "
					+ "(SELECT X.*, ROWNUM rnum FROM (" + sqlQuery + ") X "
					+ "WHERE ROWNUM <= " + maxRow + ") " + "WHERE rnum > "
					+ minRow;
			ps = conn.prepareStatement(pageWhere);
			rs = ps.executeQuery();
			while (rs.next()) {
				TWorkitemInfo wi = new TWorkitemInfo();
				wi.workitemId = rs.getInt("ID");
				wi.priority = rs.getInt("Priority");
				wi.subject = rs.getString("Subject");
				wi.status = rs.getString("Status");
				wi.instructions = rs.getString("Instructions");
				wi.type = rs.getString("Type");
				wi.actionId = rs.getInt("ActionID");
				wi.deadline = DBUtil.convertDateTimeToString(rs
						.getTimestamp("Deadline"));
				wi.reminder = DBUtil.convertDateTimeToString(rs
						.getTimestamp("Reminder"));
				wi.receivedDate = DBUtil.convertDateTimeToString(rs
						.getTimestamp("CreatedDate"));
				wi.wfCreatorName = rs.getString("WFCreatorName");
				if ((wi.wfCreatorName == null)
						|| (wi.wfCreatorName.length() <= 0))
					wi.wfCreatorName = rs.getString("WFCreatorNameEmp");
				wi.recipientName = rs.getString("RecipientName");
				wi.senderName = rs.getString("SenderName");
				wi.actions = rs.getString("Actions");
				if (wi.senderName == null)
					wi.senderName = rs.getString("SenderRoleName");
				wi.comments = rs.getString("Comments");
				wi.receiveCount = rs.getInt("RecvCount");
				wiList.add(wi);
			}
			if (ts != null) {
				ts.workitems = wiList;
				if (wiList != null)
					ts.setCount = wiList.size();
			}
			return ts;
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			rs.close();
			ps.close();
			conn.close();
		}
	}

	private ArrayList<ECMInboxItems> searchUserInboxItemsReport(
			TWorkitemQuery twi, String sysStatus, String wiStatus)
			throws Exception {
		String statusCondition = "AND A.Status in ('New', 'Read', 'Finished') ";
		ArrayList<ECMInboxItems> wiList = new ArrayList<ECMInboxItems>();
		if ((sysStatus == null) || (sysStatus.trim().length() <= 0))
			sysStatus = "ACTIVE";
		Connection conn = null;
		ResultSet rs = null;
		String sqlQuery = null;
		PreparedStatement ps = null;
		try {
			conn = DBUtil.getECMDBConnection();
			if ((wiStatus != null) && (wiStatus.trim().length() > 0)) {
				if ((wiStatus.equalsIgnoreCase("New")))
					statusCondition = "AND A.Status = 'New' ";
				else if ((wiStatus.equalsIgnoreCase("Read")))
					statusCondition = "AND A.Status = 'Read' ";
			}
			sqlQuery = "SELECT B.Priority, B.Subject, B.ECMNo, A.Status, A.Instructions, A.Type, A.ActionID, "
					+ "A.Deadline, A.ID, A.Reminder, A.CreatedDate, A.Actions, "
					+ "A.Comments, D.ActionTaken, D.Remarks, "
					+ "(SELECT FullName FROM ECM_USER WHERE EMPNo = B.CREATEDBY  AND ROWNUM = 1) AS WfCreatorNameEmp, "
					+ "(SELECT Name FROM ECM_ROLE WHERE ID = B.ROLEID  AND ROWNUM = 1) AS WfCreatorName, "
					+ "(SELECT FullName FROM ECM_USER WHERE EMPNo = A.SenderEMPNo  AND ROWNUM = 1) AS SenderName, "
					+ "(SELECT FullName FROM ECM_USER WHERE EMPNo = A.RecipientEMPNo  AND ROWNUM = 1) AS RecipientName, "
					+ "(SELECT Name FROM ECM_ROLE WHERE ID = A.SenderRoleID  AND ROWNUM = 1) AS SenderRoleName, "
					+ "(SELECT COUNT(*) FROM ECM_WORKITEM WHERE WorkflowID = A.WorkflowID AND RecipientEMPNo = A.RecipientEMPNo AND SystemStatus != 'RECALL') AS RecvCount "
					+ "FROM ECM_WORKITEM A, ECM_WORKFLOW B, ECM_WORKITEM_SENT D "
					+ "WHERE  A.RecipientEMPNo = "
					+ twi.userId
					+ " AND A.WorkflowID = B.ID AND A.WorkflowID = D.WorkflowID AND D.ID = A.PARENTSENTITEMID "
					+ statusCondition
					+ "AND A.SystemStatus = '"
					+ sysStatus
					+ "' "
					+ getQueryString(twi, false) + getOrderByString(twi, false);

			ps = conn.prepareStatement(sqlQuery);
			rs = ps.executeQuery();
			while (rs.next()) {
				ECMInboxItems wi = new ECMInboxItems();
				wi.setWorkitemId(rs.getInt("ID"));
				wi.setPriority(getPriorityValue(rs.getInt("Priority")));
				wi.setSubject(rs.getString("Subject"));
				wi.setStatus(rs.getString("Status"));
				wi.setInstructions(rs.getString("Instructions"));
				wi.setType(rs.getString("Type"));
				wi.setActionId(rs.getInt("ActionID"));
				wi.setDeadline(DBUtil.convertDateTimeToString(rs
						.getTimestamp("Deadline")));
				wi.setReminder(DBUtil.convertDateTimeToString(rs
						.getTimestamp("Reminder")));
				wi.setReceivedDate(DBUtil.convertDateTimeToString(rs
						.getTimestamp("CreatedDate")));
				wi.setWfCreatorName(rs.getString("WFCreatorName"));
				if ((wi.wfCreatorName == null)
						|| (wi.wfCreatorName.length() <= 0))
					wi.setWfCreatorName(rs.getString("WfCreatorNameEmp"));
				wi.setRecipientName(rs.getString("RecipientName"));
				if ((rs.getString("SenderName") == null)
						|| (rs.getString("SenderName").length() <= 0)) {
					wi.setSenderName(rs.getString("SenderRoleName"));
				} else {
					wi.setSenderName(rs.getString("SenderName"));
				}
				wi.setActions(rs.getString("Actions"));
				wi.setRemarks(rs.getString("Remarks"));
				wi.setComments(rs.getString("Comments"));
				wi.setActionTaken(rs.getString("ActionTaken"));
				wi.setEcmNo(rs.getString("ECMNo"));
				wi.setReceiveCount(rs.getInt("RecvCount"));
				wiList.add(wi);
			}
			return wiList;
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			rs.close();
			ps.close();
			conn.close();
		}
	}

	private long getItemsCount(Connection conn, String countQuery) {
		ResultSet rs = null;
		PreparedStatement ps = null;
		long totalCount = -1;
		try {
			ps = conn.prepareStatement(countQuery);
			rs = ps.executeQuery();
			if (rs.next())
				totalCount = rs.getInt("TotalCount");
			rs.close();
			ps.close();
		} catch (Exception e) {
		}
		return totalCount;
	}

	private long getPageSize(Connection conn, long empNo) {
		long pageSize = 25;
		ResultSet rs = null;
		PreparedStatement ps = null;
		try {
			ps = conn
					.prepareStatement("SELECT KEYVALUE FROM ECM_USER_SETTING WHERE EMPNO = ? "
							+ "AND KEYNAME = 'Page Size' AND APPID = 'ECM'");
			ps.setInt(1, (int) empNo);
			rs = ps.executeQuery();
			if (rs.next())
				pageSize = DBUtil.stringToLongDefault(rs.getString("KEYVALUE"),
						25);
			rs.close();
			ps.close();
		} catch (Exception e) {
			System.out.print(e.getMessage());
		}
		return pageSize;
	}

	private TWorkitemSet getWorkitemSet(Connection conn, long empNo,
			String countQuery, long pageNo) throws Exception {
		TWorkitemSet ts = new TWorkitemSet();
		ts.totalCount = getItemsCount(conn, countQuery);
		ts.pageSize = getPageSize(conn, empNo);
		if (ts.pageSize <= 0)
			ts.pageSize = 25;
		ts.pages = ts.totalCount / ts.pageSize;
		if ((ts.totalCount % ts.pageSize) > 0)
			ts.pages++;
		if (pageNo <= 0)
			pageNo = 1;
		ts.curPage = pageNo;
		return ts;
	}

	private void setPaginationClause(TWorkitemSet ts) {
		if ((ts.totalCount > 0) && (ts.curPage > 0) && (ts.curPage <= ts.pages)) {
			this.minRow = (ts.curPage - 1) * ts.pageSize;
			this.maxRow = ts.curPage * ts.pageSize;
		} else {
			this.minRow = 0;
			this.maxRow = 9999;
		}
	}

	private TWorkitemSet searchRoleInboxItemsCount(TWorkitemQuery twi,
			String sysStatus) throws Exception {
		TWorkitemSet ts = null;
		if ((sysStatus == null) || (sysStatus.trim().length() <= 0))
			sysStatus = "ACTIVE";
		Connection conn = null;
		try {
			conn = DBUtil.getECMDBConnection();
			String countQuery = "SELECT count(A.ID) AS TotalCount FROM ECM_WORKITEM A, "
					+ "ECM_ROLE C WHERE  A.RecipientRoleID = "
					+ twi.userId
					+ " AND "
					+ "A.Status in ('New', 'Read', 'Finished') AND A.RecipientRoleID = C.ID "
					+ "AND A.SystemStatus = '"
					+ sysStatus
					+ "' "
					+ getQueryString(twi, false);
			ts = getWorkitemSet(conn, twi.empNo, countQuery, twi.pageNo);
			return ts;
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			conn.close();
		}
	}

	private TWorkitemSet searchRoleInboxItems(TWorkitemQuery twi,
			String sysStatus, String wiStatus) throws Exception {
		TWorkitemSet ts = null;
		String statusCondition = "AND A.Status in ('New', 'Read', 'Finished') ";
		ArrayList<TWorkitemInfo> wiList = new ArrayList<TWorkitemInfo>();
		if ((sysStatus == null) || (sysStatus.trim().length() <= 0))
			sysStatus = "ACTIVE";
		Connection conn = null;
		ResultSet rs = null;
		String sqlQuery = null;
		PreparedStatement ps = null;
		try {
			conn = DBUtil.getECMDBConnection();
			if ((wiStatus != null) && (wiStatus.trim().length() > 0)) {
				if ((wiStatus.equalsIgnoreCase("New")))
					statusCondition = "AND A.Status = 'New' ";
				else if ((wiStatus.equalsIgnoreCase("Read")))
					statusCondition = "AND A.Status = 'Read' ";
			}
			String countQuery = "SELECT count(A.ID) AS TotalCount FROM ECM_WORKITEM A, "
					+ "ECM_ROLE C, ECM_WORKFLOW B WHERE  A.RecipientRoleID = "
					+ twi.userId
					+ "AND A.WorkflowID = B.ID "
					+ statusCondition
					+ "AND A.RecipientRoleID = C.ID "
					+ "AND A.SystemStatus = '"
					+ sysStatus
					+ "' "
					+ getQueryString(twi, false);
			ts = getWorkitemSet(conn, twi.empNo, countQuery, twi.pageNo);
			ts.workitems = wiList;
			setPaginationClause(ts);
			sqlQuery = "SELECT B.Priority, B.Subject, A.Status, A.Instructions, A.Type, A.ActionID, "
					+ "A.Deadline, A.ID, A.Reminder, A.CreatedDate, A.Actions, A.Comments, "
					+ "(SELECT Name FROM ECM_ROLE WHERE ID = B.RoleID AND ROWNUM = 1) AS WfCreatorName, "
					+ "(SELECT FullName FROM ECM_USER WHERE EMPNo = B.CREATEDBY  AND ROWNUM = 1) AS WfCreatorNameEmp, "
					+ "(SELECT FullName FROM ECM_USER WHERE EMPNo = A.SenderEMPNo  AND ROWNUM = 1) AS SenderName, "
					+ "(SELECT Name FROM ECM_ROLE WHERE ID = A.RecipientRoleID  AND ROWNUM = 1) AS RecipientName, "
					+ "(SELECT Name FROM ECM_ROLE WHERE ID = A.SenderRoleID  AND ROWNUM = 1) AS SenderRoleName, "
					+ "(SELECT COUNT(*) FROM ECM_WORKITEM WHERE WorkflowID = A.WorkflowID AND RecipientRoleID = A.RecipientRoleID AND SystemStatus != 'RECALL') AS RecvCount "
					+ "FROM ECM_WORKITEM A, ECM_WORKFLOW B "
					+ "WHERE  A.RecipientRoleID = "
					+ twi.userId
					+ " AND A.WorkflowID = B.ID "
					+ statusCondition
					+ " AND A.SystemStatus = '"
					+ sysStatus
					+ "' "
					+ getQueryString(twi, false) + getOrderByString(twi, false);
			String pageWhere = "SELECT * FROM "
					+ "(SELECT X.*, ROWNUM rnum FROM (" + sqlQuery + ") X "
					+ "WHERE ROWNUM <= " + maxRow + ") " + "WHERE rnum > "
					+ minRow;
			ps = conn.prepareStatement(pageWhere);
			rs = ps.executeQuery();
			while (rs.next()) {
				TWorkitemInfo wi = new TWorkitemInfo();
				wi.workitemId = rs.getInt("ID");
				wi.priority = rs.getInt("Priority");
				wi.subject = rs.getString("Subject");
				wi.status = rs.getString("Status");
				wi.instructions = rs.getString("Instructions");
				wi.type = rs.getString("Type");
				wi.actionId = rs.getInt("ActionID");
				wi.deadline = DBUtil.convertDateTimeToString(rs
						.getTimestamp("Deadline"));
				wi.reminder = DBUtil.convertDateTimeToString(rs
						.getTimestamp("Reminder"));
				wi.receivedDate = DBUtil.convertDateTimeToString(rs
						.getTimestamp("CreatedDate"));
				wi.wfCreatorName = rs.getString("WFCreatorName");
				if ((wi.wfCreatorName == null)
						|| (wi.wfCreatorName.length() <= 0))
					wi.wfCreatorName = rs.getString("WFCreatorNameEmp");
				wi.recipientName = rs.getString("RecipientName");
				wi.senderName = rs.getString("SenderName");
				if (wi.senderName == null)
					wi.senderName = rs.getString("SenderRoleName");
				wi.comments = rs.getString("Comments");
				wi.actions = rs.getString("Actions");
				wi.receiveCount = rs.getInt("RecvCount");
				wiList.add(wi);
			}
			if (ts != null) {
				ts.workitems = wiList;
				if (wiList != null)
					ts.setCount = wiList.size();
			}
			return ts;
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			rs.close();
			ps.close();
			conn.close();
		}
	}

	private ArrayList<ECMInboxItems> searchRoleInboxItemsReport(
			TWorkitemQuery twi, String sysStatus, String wiStatus)
			throws Exception {
		String statusCondition = "AND A.Status in ('New', 'Read', 'Finished') ";
		ArrayList<ECMInboxItems> wiList = new ArrayList<ECMInboxItems>();
		if ((sysStatus == null) || (sysStatus.trim().length() <= 0))
			sysStatus = "ACTIVE";
		Connection conn = null;
		ResultSet rs = null;
		String sqlQuery = null;
		PreparedStatement ps = null;
		try {
			conn = DBUtil.getECMDBConnection();
			if ((wiStatus != null) && (wiStatus.trim().length() > 0)) {
				if ((wiStatus.equalsIgnoreCase("New")))
					statusCondition = "AND A.Status = 'New' ";
				else if ((wiStatus.equalsIgnoreCase("Read")))
					statusCondition = "AND A.Status = 'Read' ";
			}
			sqlQuery = "SELECT B.Priority, B.Subject, B.EcmNo, A.Status, A.Instructions, A.Type, A.ActionID, "
					+ "A.Deadline, A.ID, A.Reminder, A.CreatedDate, A.Actions, A.Comments, D.ActionTaken, D.Remarks, "
					+ "(SELECT Name FROM ECM_ROLE WHERE ID = B.RoleID AND ROWNUM = 1) AS WfCreatorName, "
					+ "(SELECT FullName FROM ECM_USER WHERE EMPNo = B.CREATEDBY  AND ROWNUM = 1) AS WfCreatorNameEmp, "
					+ "(SELECT FullName FROM ECM_USER WHERE EMPNo = A.SenderEMPNo  AND ROWNUM = 1) AS SenderName, "
					+ "(SELECT Name FROM ECM_ROLE WHERE ID = A.RecipientRoleID  AND ROWNUM = 1) AS RecipientName, "
					+ "(SELECT Name FROM ECM_ROLE WHERE ID = A.SenderRoleID  AND ROWNUM = 1) AS SenderRoleName, "
					+ "(SELECT COUNT(*) FROM ECM_WORKITEM WHERE WorkflowID = A.WorkflowID AND RecipientRoleID = A.RecipientRoleID AND SystemStatus != 'RECALL') AS RecvCount "
					+ "FROM ECM_WORKITEM A, ECM_WORKFLOW B, ECM_WORKITEM_SENT D "
					+ "WHERE  A.RecipientRoleID = "
					+ twi.userId
					+ " AND A.WorkflowID = B.ID AND A.WorkflowID = D.WorkflowID AND D.ID = A.PARENTSENTITEMID "
					+ statusCondition
					+ " AND A.SystemStatus = '"
					+ sysStatus
					+ "' "
					+ getQueryString(twi, false) + getOrderByString(twi, false);

			ps = conn.prepareStatement(sqlQuery);
			rs = ps.executeQuery();
			while (rs.next()) {
				ECMInboxItems wi = new ECMInboxItems();
				wi.setWorkitemId(rs.getInt("ID"));
				wi.setPriority(getPriorityValue(rs.getInt("Priority")));
				wi.setSubject(rs.getString("Subject"));
				wi.setStatus(rs.getString("Status"));
				wi.setInstructions(rs.getString("Instructions"));
				wi.setType(rs.getString("Type"));
				wi.setActionId(rs.getInt("ActionID"));
				wi.setDeadline(DBUtil.convertDateTimeToString(rs
						.getTimestamp("Deadline")));
				wi.setReminder(DBUtil.convertDateTimeToString(rs
						.getTimestamp("Reminder")));
				wi.setReceivedDate(DBUtil.convertDateTimeToString(rs
						.getTimestamp("CreatedDate")));
				wi.setWfCreatorName(rs.getString("WFCreatorName"));
				if ((wi.wfCreatorName == null)
						|| (wi.wfCreatorName.length() <= 0))
					wi.setWfCreatorName(rs.getString("WfCreatorNameEmp"));
				wi.setRecipientName(rs.getString("RecipientName"));
				if ((rs.getString("SenderName") == null)
						|| (rs.getString("SenderName").length() <= 0)) {
					wi.setSenderName(rs.getString("SenderRoleName"));
				} else {
					wi.setSenderName(rs.getString("SenderName"));
				}
				wi.setActions(rs.getString("Actions"));
				wi.setRemarks(rs.getString("Remarks"));
				wi.setComments(rs.getString("Comments"));
				wi.setActionTaken(rs.getString("ActionTaken"));
				wi.setEcmNo(rs.getString("ECMNo"));
				wi.setReceiveCount(rs.getInt("RecvCount"));
				wiList.add(wi);
			}
			return wiList;
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			rs.close();
			ps.close();
			conn.close();
		}
	}

	private TWorkitemSet searchUserSentItemsCount(TWorkitemQuery twi,
			String sysStatus) throws Exception {
		TWorkitemSet ts = null;
		if ((sysStatus == null) || (sysStatus.trim().length() <= 0))
			sysStatus = "ACTIVE";
		Connection conn = null;
		try {
			conn = DBUtil.getECMDBConnection();
			String countQuery = "SELECT Count(A.ID) AS TotalCount FROM ECM_WORKITEM_SENT A, "
					+ "ECM_WORKFLOW B "
					+ "WHERE  A.SenderEMPNo = "
					+ twi.userId
					+ " AND A.WorkflowID = B.ID AND A.Status = '"
					+ sysStatus + "' " + getQueryString(twi, true);
			ts = getWorkitemSet(conn, twi.empNo, countQuery, twi.pageNo);
			return ts;
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			conn.close();
		}
	}
	
	private TWorkitemSet searchUserSentItemsCountByWorkflow(TWorkitemQuery twi,
			String sysStatus) throws Exception {
		TWorkitemSet ts = null;
		if ((sysStatus == null) || (sysStatus.trim().length() <= 0))
			sysStatus = "ACTIVE";
		Connection conn = null;
		try {
			conn = DBUtil.getECMDBConnection();
			String countQuery = "SELECT Count(DISTINCT(A.WORKFLOWID)) AS TotalCount FROM ECM_WORKITEM_SENT A, "
					+ "ECM_WORKFLOW B "
					+ "WHERE  A.SenderEMPNo = "
					+ twi.userId
					+ " AND A.WorkflowID = B.ID AND A.Status = '"
					+ sysStatus + "' " + getQueryString(twi, true);
			ts = getWorkitemSet(conn, twi.empNo, countQuery, twi.pageNo);
			return ts;
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			conn.close();
		}
	}

	private TWorkitemSet searchUserSentItems(TWorkitemQuery twi,
			String sysStatus) throws Exception {
		TWorkitemSet ts = null;
		String strQueryWhere = " ";
		ArrayList<TWorkitemInfo> wiList = new ArrayList<TWorkitemInfo>();
		if ((sysStatus == null) || (sysStatus.trim().length() <= 0))
			sysStatus = "ACTIVE";
		String criteria = twi.sysStatus;
		if ((criteria != null) && (criteria.length() > 0)
				&& (criteria.equalsIgnoreCase("actioned")))
			strQueryWhere = " AND A.ParentItemID <> 0 ";
		Connection conn = null;
		ResultSet rs = null;
		String sqlQuery = null;
		PreparedStatement ps = null;
		try {
			conn = DBUtil.getECMDBConnection();
			String countQuery = "SELECT Count(A.ID) AS TotalCount FROM ECM_WORKITEM_SENT A, "
					+ "ECM_WORKFLOW B "
					+ "WHERE  A.SenderEMPNo = "
					+ twi.userId
					+ " "
					+ "AND A.WorkflowID = B.ID"
					+ strQueryWhere
					+ "AND A.Status = '"
					+ sysStatus
					+ "' "
					+ getQueryString(twi, true);

			ts = getWorkitemSet(conn, twi.empNo, countQuery, twi.pageNo);
			ts.workitems = wiList;
			setPaginationClause(ts);
			sqlQuery = "SELECT A.Priority, B.Subject, A.Status, A.ID, A.CreatedDate, "
					+ "(SELECT Name FROM ECM_ROLE WHERE ID = B.RoleID  AND ROWNUM = 1) AS WfCreatorName, "
					+ "(SELECT FullName FROM ECM_USER WHERE EMPNo = B.CREATEDBY  AND ROWNUM = 1) AS WfCreatorNameEmp, "
					+ "(SELECT FullName FROM ECM_USER WHERE EMPNo = A.SenderEMPNo  AND ROWNUM = 1) AS SenderName "
					+ "FROM ECM_WORKITEM_SENT A, ECM_WORKFLOW B "
					+ "WHERE  A.SenderEMPNo = "
					+ twi.userId
					+ " AND A.WorkflowID = B.ID "
					+ strQueryWhere
					+ " AND A.Status = '"
					+ sysStatus
					+ "' "
					+ getQueryString(twi, true) + getOrderByString(twi, true);

			String pageWhere = "SELECT * FROM "
					+ "(SELECT X.*, ROWNUM rnum FROM (" + sqlQuery + ") X "
					+ "WHERE ROWNUM <= " + maxRow + ") " + "WHERE rnum > "
					+ minRow;
			ps = conn.prepareStatement(pageWhere);
			rs = ps.executeQuery();
			while (rs.next()) {
				TWorkitemInfo wi = new TWorkitemInfo();
				wi.workitemId = rs.getInt("ID");
				wi.priority = rs.getInt("Priority");
				wi.subject = rs.getString("Subject");
				wi.status = rs.getString("Status");
				wi.receivedDate = DBUtil.convertDateTimeToString(rs
						.getTimestamp("CreatedDate"));
				wi.wfCreatorName = rs.getString("WFCreatorName");
				if ((wi.wfCreatorName == null)
						|| (wi.wfCreatorName.length() <= 0))
					wi.wfCreatorName = rs.getString("WFCreatorNameEmp");
				wi.senderName = rs.getString("SenderName");
				wi.senderId = twi.userId;
				wiList.add(wi);
			}
			if (ts != null) {
				ts.workitems = wiList;
				if (wiList != null)
					ts.setCount = wiList.size();
			}
			return ts;
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			rs.close();
			ps.close();
			conn.close();
		}
	}

	private TWorkitemSet searchUserRelatedSentItems(TWorkitemQuery twi,
			String sysStatus) throws Exception {
		TWorkitemSet ts = null;
		String strQueryWhere = " ";
		ArrayList<TWorkitemInfo> wiList = new ArrayList<TWorkitemInfo>();
		if ((sysStatus == null) || (sysStatus.trim().length() <= 0))
			sysStatus = "ACTIVE";
		String criteria = twi.sysStatus;
		if ((criteria != null) && (criteria.length() > 0)
				&& (criteria.equalsIgnoreCase("actioned")))
			strQueryWhere = " AND A.ParentItemID <> 0 ";
		Connection conn = null;
		ResultSet rs = null;
		String sqlQuery = null;
		PreparedStatement ps = null;
		try {
			conn = DBUtil.getECMDBConnection();
			String countQuery = "SELECT Count(A.ID) AS TotalCount FROM ECM_WORKITEM_SENT A, "
					+ "ECM_WORKFLOW B "
					+ "WHERE  A.SenderEMPNo = " + twi.userId + " "
					+ "AND A.WorkflowID = B.ID"
					+ strQueryWhere
					+ "AND A.WorkflowID = " + twi.workflowId 
					+ "AND A.ID <> " + twi.workitemId
					+ "AND A.Status = '" + sysStatus + "' ";

			ts = getWorkitemSet(conn, twi.empNo, countQuery, twi.pageNo);
			ts.workitems = wiList;
			setPaginationClause(ts);
			sqlQuery = "SELECT A.Priority, B.Subject, A.Status, A.ID, A.CreatedDate, A.WorkflowID, "
					+ "(SELECT Name FROM ECM_ROLE WHERE ID = B.RoleID  AND ROWNUM = 1) AS WfCreatorName, "
					+ "(SELECT FullName FROM ECM_USER WHERE EMPNo = B.CREATEDBY  AND ROWNUM = 1) AS WfCreatorNameEmp, "
					+ "(SELECT FullName FROM ECM_USER WHERE EMPNo = A.SenderEMPNo  AND ROWNUM = 1) AS SenderName "
					+ "FROM ECM_WORKITEM_SENT A, ECM_WORKFLOW B "
					+ "WHERE  A.SenderEMPNo = " + twi.userId + " "
					+ "AND A.WorkflowID = B.ID"
					+ strQueryWhere
					+ "AND A.WorkflowID = '" + twi.workflowId + "' "
					+ "AND A.ID <> '" + twi.workitemId + "' "
					+ "AND A.Status = '" + sysStatus + "' " + getOrderByString(twi, true);

			String pageWhere = "SELECT * FROM "
					+ "(SELECT X.*, ROWNUM rnum FROM (" + sqlQuery + ") X "
					+ "WHERE ROWNUM <= " + maxRow + ") " + "WHERE rnum > "
					+ minRow;
			ps = conn.prepareStatement(pageWhere);
			rs = ps.executeQuery();
			while (rs.next()) {
				TWorkitemInfo wi = new TWorkitemInfo();
				wi.workitemId = rs.getInt("ID");
				wi.priority = rs.getInt("Priority");
				wi.subject = rs.getString("Subject");
				wi.status = rs.getString("Status");
				wi.receivedDate = DBUtil.convertDateTimeToString(rs
						.getTimestamp("CreatedDate"));
				wi.wfCreatorName = rs.getString("WFCreatorName");
				if ((wi.wfCreatorName == null)
						|| (wi.wfCreatorName.length() <= 0))
					wi.wfCreatorName = rs.getString("WFCreatorNameEmp");
				wi.senderName = rs.getString("SenderName");
				wi.senderId = twi.userId;
				wi.workflowId = rs.getInt("WorkflowID");
				wiList.add(wi);
			}
			if (ts != null) {
				ts.workitems = wiList;
				if (wiList != null)
					ts.setCount = wiList.size();
			}
			return ts;
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			rs.close();
			ps.close();
			conn.close();
		}
	}
	
	private TWorkitemSet searchUserSentItemsByWorkflow(TWorkitemQuery twi,
			String sysStatus) throws Exception {
		TWorkitemSet ts = null;
		String strQueryWhere = " ";
		ArrayList<TWorkitemInfo> wiList = new ArrayList<TWorkitemInfo>();
		if ((sysStatus == null) || (sysStatus.trim().length() <= 0))
			sysStatus = "ACTIVE";
		String criteria = twi.sysStatus;
		if ((criteria != null) && (criteria.length() > 0)
				&& (criteria.equalsIgnoreCase("actioned")))
			strQueryWhere = " AND A.ParentItemID <> 0 ";
		Connection conn = null;
		ResultSet rs = null;
		String sqlQuery = null;
		PreparedStatement ps = null;
		try {
			conn = DBUtil.getECMDBConnection();
			String countQuery = "SELECT Count(DISTINCT(A.WORKFLOWID)) AS TotalCount FROM ECM_WORKITEM_SENT A, "
					+ "ECM_WORKFLOW B "
					+ "WHERE  A.SenderEMPNo = "
					+ twi.userId
					+ " "
					+ "AND A.WorkflowID = B.ID"
					+ strQueryWhere
					+ "AND A.Status = '"
					+ sysStatus
					+ "' "
					+ getQueryString(twi, true);

			ts = getWorkitemSet(conn, twi.empNo, countQuery, twi.pageNo);
			ts.workitems = wiList;
			setPaginationClause(ts);
			sqlQuery = "SELECT A.Priority, B.Subject, A.Status, A.ID, A.CreatedDate, A.WorkflowID, "
					+ "(SELECT Name FROM ECM_ROLE WHERE ID = B.RoleID  AND ROWNUM = 1) AS WfCreatorName, "
					+ "(SELECT FullName FROM ECM_USER WHERE EMPNo = B.CREATEDBY  AND ROWNUM = 1) AS WfCreatorNameEmp, "
					+ "(SELECT FullName FROM ECM_USER WHERE EMPNo = A.SenderEMPNo  AND ROWNUM = 1) AS SenderName "
					+ "FROM ECM_WORKITEM_SENT A, ECM_WORKFLOW B "
					+ "WHERE  A.SenderEMPNo = "
					+ twi.userId
					+ " AND A.WorkflowID = B.ID "
					+ strQueryWhere
					+ " AND A.Status = '"
					+ sysStatus
					+ "' "
					+ getQueryString(twi, true) + getOrderByString(twi, true);

			String pageWhere = "SELECT * FROM "
					+ "(SELECT X.*, ROWNUM rnum FROM (" + sqlQuery + ") X "
					+ "WHERE ROWNUM <= " + maxRow + ") " + "WHERE rnum > "
					+ minRow;
			ps = conn.prepareStatement(pageWhere);
			rs = ps.executeQuery();
			int iterCount = 0;
			while (rs.next()) {
				iterCount++;
				TWorkitemInfo wi = new TWorkitemInfo();
				wi.workitemId = rs.getInt("ID");
				wi.priority = rs.getInt("Priority");
				wi.subject = rs.getString("Subject");
				wi.status = rs.getString("Status");
				wi.receivedDate = DBUtil.convertDateTimeToString(rs
						.getTimestamp("CreatedDate"));
				wi.wfCreatorName = rs.getString("WFCreatorName");
				if ((wi.wfCreatorName == null)
						|| (wi.wfCreatorName.length() <= 0))
					wi.wfCreatorName = rs.getString("WFCreatorNameEmp");
				wi.senderName = rs.getString("SenderName");
				wi.senderId = twi.userId;
				wi.workflowId = rs.getInt("WorkflowID");
				if(iterCount == 1)
					wiList.add(wi);
				else if(validateSentItemList(wiList, wi.workflowId, wi.workitemId))
					wiList.add(wi);
			}
			if (ts != null) {
				ts.workitems = wiList;
				if (wiList != null)
					ts.setCount = wiList.size();
			}
			return ts;
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			rs.close();
			ps.close();
			conn.close();
		}
	}
	
	private boolean validateSentItemList(ArrayList<TWorkitemInfo> wiList, long workflowId, long sentItemId) throws Exception{
		boolean isValidate = true;
		
		if(wiList == null)
			return isValidate;
		
		try {
			if(wiList != null && wiList.size() > 0)
			{
				for(TWorkitemInfo twi: wiList) {
					if(workflowId == twi.workflowId)
					{
						if(sentItemId > twi.workitemId)
						{
							wiList.remove(twi);
							return true;
						}
						else
							return false;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		}

		return isValidate;	
	}
	
	private ArrayList<ECMSentitems> searchUserSentItemsReport(
			TWorkitemQuery twi, String sysStatus) throws Exception {
		String strQueryWhere = " ";
		ArrayList<ECMSentitems> wiList = new ArrayList<ECMSentitems>();
		if ((sysStatus == null) || (sysStatus.trim().length() <= 0))
			sysStatus = "ACTIVE";
		String criteria = twi.sysStatus;
		if ((criteria != null) && (criteria.length() > 0)
				&& (criteria.equalsIgnoreCase("actioned")))
			strQueryWhere = " AND A.ParentItemID <> 0 ";
		Connection conn = null;
		ResultSet rs = null;
		String sqlQuery = null;
		PreparedStatement ps = null;
		try {
			conn = DBUtil.getECMDBConnection();
			sqlQuery = "SELECT A.Priority, B.Subject, B.ECMNo, A.Remarks, A.Status, A.ID, A.CreatedDate, A.WorkflowID, "
					+ "(SELECT Name FROM ECM_ROLE WHERE ID = B.RoleID  AND ROWNUM = 1) AS WfCreatorName, "
					+ "(SELECT FullName FROM ECM_USER WHERE EMPNo = B.CREATEDBY  AND ROWNUM = 1) AS WfCreatorNameEmp, "
					+ "(SELECT FullName FROM ECM_USER WHERE EMPNo = A.SenderEMPNo  AND ROWNUM = 1) AS SenderName "
					+ "FROM ECM_WORKITEM_SENT A, ECM_WORKFLOW B "
					+ "WHERE  A.SenderEMPNo = "
					+ twi.userId
					+ " AND A.WorkflowID = B.ID "
					+ strQueryWhere
					+ " AND A.Status = '"
					+ sysStatus
					+ "' "
					+ getQueryString(twi, true) + getOrderByString(twi, true);

			ps = conn.prepareStatement(sqlQuery);
			rs = ps.executeQuery();
			while (rs.next()) {
				ECMSentitems wi = new ECMSentitems();
				wi.setPriority(getPriorityValue(rs.getInt("Priority")));
				wi.setSubject(rs.getString("Subject"));
				wi.setStatus(rs.getString("Status"));
				wi.setReceivedDate(DBUtil.convertDateTimeToString(rs
						.getTimestamp("CreatedDate")));
				wi.setWfCreatorName(rs.getString("WFCreatorName"));
				if ((wi.getWfCreatorName() == null)
						|| (wi.getWfCreatorName().length() <= 0))
					wi.setWfCreatorName(rs.getString("WFCreatorNameEmp"));
				wi.setSenderName(rs.getString("SenderName"));
				wi.setSentitemId(rs.getInt("WorkflowID"));
				wi.setRemarks(rs.getString("Remarks"));
				wi.setEcmNo(rs.getString("EcmNo"));
				wi.setTrackSentitem(getSentItemTrackHistory(conn,
						wi.getSentitemId(), twi.userId));
				wiList.add(wi);
			}
			return wiList;
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			rs.close();
			ps.close();
			conn.close();
		}
	}

	public ArrayList<ECMSentitemTrack> getSentItemTrackHistory(Connection conn,
			long sentitemId, long empNo) throws Exception {
		if (sentitemId <= 0)
			return null;

		ArrayList<ECMSentitemTrack> hList = null;
		ResultSet rs = null;
		String sqlQuery = null;
		CallableStatement callableStatement = null;
		try {
			sqlQuery = "{call ECM_GET_TRACK_HISTORY(?,?)}";
			callableStatement = conn.prepareCall(sqlQuery);
			callableStatement.setInt(1, (int) sentitemId);
			callableStatement.registerOutParameter(2, OracleTypes.CURSOR);
			callableStatement.execute();
			rs = (ResultSet) callableStatement.getObject(2);
			while (rs.next()) {
				ECMSentitemTrack hist = new ECMSentitemTrack();
				hist.setId(rs.getInt("ID"));
				hist.setWorkitemId(rs.getInt("WorkItemID"));
				hist.setTimeStamp(DBUtil.formatDateForUI(rs
						.getTimestamp("ActionTimestamp")));
				hist.setDetails(rs.getString("Details"));
				hist.setActionBy(rs.getString("UserName"));
				if (hist.getActionBy() == null)
					hist.setActionBy(rs.getString("RoleName"));

				hist.setRecipientName(rs.getString("RecipientUser"));
				if (hist.getRecipientName() == null)
					hist.setRecipientName(rs.getString("RecipientRole"));
				if (hList == null)
					hList = new ArrayList<ECMSentitemTrack>();
				hist.setComments(rs.getString("Comments"));
				hist.setActionTaken(rs.getString("ActionTaken"));
				
				long actionUserId = rs.getLong("ActionUser");
				String actionDetails = hist.getDetails();
				if (actionDetails.equalsIgnoreCase("RECALL")
						&& (actionUserId != empNo))
					continue;
				else
					hList.add(hist);
			}

			return hList;
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			// rs.close();
			// callableStatement.close();
		}
	}

	private TWorkitemSet searchUserActionedItems(TWorkitemQuery twi,
			String sysStatus) throws Exception {
		TWorkitemSet ts = null;
		ArrayList<TWorkitemInfo> wiList = new ArrayList<TWorkitemInfo>();
		if ((sysStatus == null) || (sysStatus.trim().length() <= 0))
			sysStatus = "ACTIVE";
		Connection conn = null;
		ResultSet rs = null;
		String sqlQuery = null;
		PreparedStatement ps = null;
		try {
			conn = DBUtil.getECMDBConnection();
			String strCondition = "A.SenderEMPNo = " + twi.userId
					+ " AND c.RecipientEmpNo = " + twi.userId;
			/*
			 * String userName = ECMUserList.getInstance()
			 * .getLoginName(twi.userId); TUser user =
			 * ECMUserList.getInstance().getUserDetails(userName); if (user !=
			 * null) { ArrayList<TRole> tRoleList = user.roles; String tRoleIds
			 * = ""; for (TRole tr : tRoleList) { if (tRoleIds == "") tRoleIds
			 * += tr.id; else tRoleIds += ", " + tr.id; } if (tRoleIds != "")
			 * strCondition = "A.SenderRoleId IN (" + tRoleIds +
			 * ") AND c.RecipientEmpNo = " + twi.userId; }
			 */
			String countQuery = "SELECT Count(A.ID) AS TotalCount FROM ECM_WORKITEM_SENT A, "
					+ "ECM_WORKFLOW B, ECM_WORKITEM C "
					+ "WHERE "
					+ strCondition
					+ " AND A.WorkflowID = B.ID AND A.ParentItemID = C.ID "
					+ "AND C.STATUS IN ('Forward','Reply') "
					+ getActionedQueryString(twi, false);
			ts = getWorkitemSet(conn, twi.empNo, countQuery, twi.pageNo);
			ts.workitems = wiList;
			setPaginationClause(ts);
			sqlQuery = "SELECT A.Priority, B.Subject, A.Status, A.ID, A.CreatedDate, "
					+ "(SELECT Name FROM ECM_ROLE WHERE ID = B.RoleID  AND ROWNUM = 1) AS WfCreatorName, "
					+ "(SELECT FullName FROM ECM_USER WHERE EMPNo = B.CREATEDBY  AND ROWNUM = 1) AS WfCreatorNameEmp, "
					+ "(SELECT FullName FROM ECM_USER WHERE EMPNo = A.SenderEMPNo  AND ROWNUM = 1) AS SenderName "
					+ "FROM ECM_WORKITEM_SENT A, ECM_WORKFLOW B, ECM_WORKITEM C WHERE "
					+ strCondition
					+ " "
					+ "AND A.WorkflowID = B.ID AND A.ParentItemID = C.ID "
					+ "AND C.STATUS IN ('Forward','Reply') "
					+ getActionedQueryString(twi, false)
					+ getOrderByString(twi, true);
			String pageWhere = "SELECT * FROM "
					+ "(SELECT X.*, ROWNUM rnum FROM (" + sqlQuery + ") X "
					+ "WHERE ROWNUM <= " + maxRow + ") " + "WHERE rnum > "
					+ minRow;
			ps = conn.prepareStatement(pageWhere);
			rs = ps.executeQuery();
			while (rs.next()) {
				TWorkitemInfo wi = new TWorkitemInfo();
				wi.workitemId = rs.getInt("ID");
				wi.priority = rs.getInt("Priority");
				wi.subject = rs.getString("Subject");
				wi.status = rs.getString("Status");
				wi.receivedDate = DBUtil.convertDateTimeToString(rs
						.getTimestamp("CreatedDate"));
				wi.wfCreatorName = rs.getString("WFCreatorName");
				if ((wi.wfCreatorName == null)
						|| (wi.wfCreatorName.length() <= 0))
					wi.wfCreatorName = rs.getString("WFCreatorNameEmp");
				wi.senderName = rs.getString("SenderName");
				wi.senderId = twi.userId;
				wiList.add(wi);
			}
			if (ts != null) {
				ts.workitems = wiList;
				if (wiList != null)
					ts.setCount = wiList.size();
			}
			return ts;
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			rs.close();
			ps.close();
			conn.close();
		}
	}

	private ArrayList<ECMSentitems> searchUserActionedItemsReport(
			TWorkitemQuery twi, String sysStatus) throws Exception {
		ArrayList<ECMSentitems> wiList = new ArrayList<ECMSentitems>();
		if ((sysStatus == null) || (sysStatus.trim().length() <= 0))
			sysStatus = "ACTIVE";
		Connection conn = null;
		ResultSet rs = null;
		String sqlQuery = null;
		PreparedStatement ps = null;
		try {
			conn = DBUtil.getECMDBConnection();
			String strCondition = "A.SenderEMPNo = " + twi.userId
					+ " AND c.RecipientEmpNo = " + twi.userId;
			/*
			 * String userName = ECMUserList.getInstance()
			 * .getLoginName(twi.userId); TUser user =
			 * ECMUserList.getInstance().getUserDetails(userName); if (user !=
			 * null) { ArrayList<TRole> tRoleList = user.roles; String tRoleIds
			 * = ""; for (TRole tr : tRoleList) { if (tRoleIds == "") tRoleIds
			 * += tr.id; else tRoleIds += ", " + tr.id; } if (tRoleIds != "")
			 * strCondition = "A.SenderRoleId IN (" + tRoleIds +
			 * ") AND c.RecipientEmpNo = " + twi.userId; }
			 */
			sqlQuery = "SELECT A.Priority, B.Subject, A.Status, A.ID, A.CreatedDate, A.WorkflowID, "
					+ "(SELECT Name FROM ECM_ROLE WHERE ID = B.RoleID  AND ROWNUM = 1) AS WfCreatorName, "
					+ "(SELECT FullName FROM ECM_USER WHERE EMPNo = B.CREATEDBY  AND ROWNUM = 1) AS WfCreatorNameEmp, "
					+ "(SELECT FullName FROM ECM_USER WHERE EMPNo = A.SenderEMPNo  AND ROWNUM = 1) AS SenderName "
					+ "FROM ECM_WORKITEM_SENT A, ECM_WORKFLOW B, ECM_WORKITEM C WHERE "
					+ strCondition
					+ " "
					+ "AND A.WorkflowID = B.ID AND A.ParentItemID = C.ID "
					+ "AND C.STATUS IN ('Forward','Reply') "
					+ getActionedQueryString(twi, false)
					+ getOrderByString(twi, true);
			ps = conn.prepareStatement(sqlQuery);
			rs = ps.executeQuery();
			while (rs.next()) {
				ECMSentitems wi = new ECMSentitems();
				wi.setPriority(getPriorityValue(rs.getInt("Priority")));
				wi.setSubject(rs.getString("Subject"));
				wi.setStatus(rs.getString("Status"));
				wi.setReceivedDate(DBUtil.convertDateTimeToString(rs
						.getTimestamp("CreatedDate")));
				wi.setWfCreatorName(rs.getString("WFCreatorName"));
				if ((wi.getWfCreatorName() == null)
						|| (wi.getWfCreatorName().length() <= 0))
					wi.setWfCreatorName(rs.getString("WFCreatorNameEmp"));
				wi.setSenderName(rs.getString("SenderName"));
				wi.setSentitemId(rs.getInt("WorkflowID"));
				wi.setTrackSentitem(getSentItemTrackHistory(conn,
						wi.getSentitemId(), twi.userId));
				wiList.add(wi);
			}
			return wiList;
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			rs.close();
			ps.close();
			conn.close();
		}
	}

	private TWorkitemSet searchUserActionedItemsCount(long userId,
			String sysStatus, String dashType) throws Exception {
		TWorkitemSet ts = null;
		if ((sysStatus == null) || (sysStatus.trim().length() <= 0))
			sysStatus = "ACTIVE";
		Connection conn = null;

		try {
			conn = DBUtil.getECMDBConnection();
			String strCondition = "A.SenderEMPNo = " + userId
					+ " AND c.RecipientEmpNo = " + userId;
			/*
			 * String userName = ECMUserList.getInstance()
			 * .getLoginName(userId); TUser user =
			 * ECMUserList.getInstance().getUserDetails(userName); if (user !=
			 * null) { ArrayList<TRole> tRoleList = user.roles; String tRoleIds
			 * = ""; for (TRole tr : tRoleList) { if (tRoleIds == "") tRoleIds
			 * += tr.id; else tRoleIds += ", " + tr.id; } if (tRoleIds != "")
			 * strCondition = "A.SenderRoleId IN (" + tRoleIds +
			 * ") AND c.RecipientEmpNo = " + userId; }
			 */
			String countQuery = "SELECT Count(A.ID) AS TotalCount FROM ECM_WORKITEM_SENT A, "
					+ "ECM_WORKFLOW B, ECM_WORKITEM C "
					+ "WHERE "
					+ strCondition
					+ " AND A.WorkflowID = B.ID AND A.ParentItemID = C.ID "
					+ "AND C.STATUS IN ('Forward','Reply') ";

			if (dashType != null && dashType.equalsIgnoreCase("TODAY")) {
				countQuery += "AND "
						+ getDateClause(DBUtil.getTodayDate(), "CreatedDate",
								"search", "");
			}

			ts = getWorkitemSet(conn, userId, countQuery, 0);

			return ts;
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {

			conn.close();
		}
	}

	private TWorkitemSet searchRoleSentItemsCount(TWorkitemQuery twi,
			String sysStatus) throws Exception {
		TWorkitemSet ts = null;
		if ((sysStatus == null) || (sysStatus.trim().length() <= 0))
			sysStatus = "ACTIVE";
		Connection conn = null;
		try {
			conn = DBUtil.getECMDBConnection();
			String countQuery = "SELECT Count(A.ID) AS TotalCount FROM ECM_WORKITEM_SENT A, "
					+ "ECM_WORKFLOW B "
					+ "WHERE  A.SenderRoleID = "
					+ twi.userId
					+ " AND A.WORKFLOWID = B.ID "
					+ "AND A.Status = '"
					+ sysStatus
					+ "' "
					+ getQueryString(twi, true);
			ts = getWorkitemSet(conn, twi.empNo, countQuery, twi.pageNo);
			return ts;
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			conn.close();
		}
	}
	
	private TWorkitemSet searchRoleSentItemsCountByWorkflow(TWorkitemQuery twi,
			String sysStatus) throws Exception {
		TWorkitemSet ts = null;
		if ((sysStatus == null) || (sysStatus.trim().length() <= 0))
			sysStatus = "ACTIVE";
		Connection conn = null;
		try {
			conn = DBUtil.getECMDBConnection();
			String countQuery = "SELECT Count(DISTINCT(A.WORKFLOWID)) AS TotalCount FROM ECM_WORKITEM_SENT A, "
					+ "ECM_WORKFLOW B "
					+ "WHERE  A.SenderRoleID = "
					+ twi.userId
					+ " AND A.WORKFLOWID = B.ID "
					+ "AND A.Status = '"
					+ sysStatus
					+ "' "
					+ getQueryString(twi, true);
			ts = getWorkitemSet(conn, twi.empNo, countQuery, twi.pageNo);
			return ts;
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			conn.close();
		}
	}

	private TWorkitemSet searchRoleSentItems(TWorkitemQuery twi,
			String sysStatus) throws Exception {
		TWorkitemSet ts = null;
		String strQueryWhere = "";
		ArrayList<TWorkitemInfo> wiList = new ArrayList<TWorkitemInfo>();
		if ((sysStatus == null) || (sysStatus.trim().length() <= 0))
			sysStatus = "ACTIVE";
		String criteria = twi.sysStatus;
		if ((criteria != null) && (criteria.length() > 0)
				&& (criteria.equalsIgnoreCase("actioned")))
			strQueryWhere = " AND A.ParentItemID <> 0 ";
		Connection conn = null;
		ResultSet rs = null;
		String sqlQuery = null;
		PreparedStatement ps = null;
		try {
			conn = DBUtil.getECMDBConnection();
			String countQuery = "SELECT Count(A.ID) AS TotalCount FROM ECM_WORKITEM_SENT A, "
					+ "ECM_WORKFLOW B "
					+ "WHERE  A.SenderRoleID = "
					+ twi.userId
					+ " AND A.WORKFLOWID = B.ID "
					+ "AND A.Status = '"
					+ sysStatus
					+ "' "
					+ strQueryWhere
					+ " " + getQueryString(twi, true);
			ts = getWorkitemSet(conn, twi.empNo, countQuery, twi.pageNo);
			ts.workitems = wiList;
			setPaginationClause(ts);
			sqlQuery = "SELECT A.Priority, B.Subject, A.Status, A.ID, A.CreatedDate, "
					+ "(SELECT Name FROM ECM_ROLE WHERE ID = B.RoleID  AND ROWNUM = 1) AS WfCreatorName, "
					+ "(SELECT FullName FROM ECM_USER WHERE EMPNo = B.CREATEDBY  AND ROWNUM = 1) AS WfCreatorNameEmp, "
					+ "(SELECT Name FROM ECM_ROLE WHERE ID = A.SenderRoleID  AND ROWNUM = 1) AS SenderRoleName "
					+ "FROM ECM_WORKITEM_SENT A, ECM_WORKFLOW B "
					+ "WHERE  A.SenderRoleID = "
					+ twi.userId
					+ " AND A.WorkflowID = B.ID "
					+ "AND A.Status = '"
					+ sysStatus
					+ "' "
					+ strQueryWhere
					+ " "
					+ getQueryString(twi, true) + getOrderByString(twi, true);
			String pageWhere = "SELECT * FROM "
					+ "(SELECT X.*, ROWNUM rnum FROM (" + sqlQuery + ") X "
					+ "WHERE ROWNUM <= " + maxRow + ") " + "WHERE rnum > "
					+ minRow;
			ps = conn.prepareStatement(pageWhere);
			rs = ps.executeQuery();
			while (rs.next()) {
				TWorkitemInfo wi = new TWorkitemInfo();
				wi.workitemId = rs.getInt("ID");
				wi.priority = rs.getInt("Priority");
				wi.subject = rs.getString("Subject");
				wi.status = rs.getString("Status");
				wi.receivedDate = DBUtil.convertDateTimeToString(rs
						.getTimestamp("CreatedDate"));
				wi.wfCreatorName = rs.getString("WFCreatorName");
				if ((wi.wfCreatorName == null)
						|| (wi.wfCreatorName.length() <= 0))
					wi.wfCreatorName = rs.getString("WFCreatorNameEmp");
				wi.senderName = rs.getString("SenderRoleName");
				wi.senderId = twi.userId;
				wiList.add(wi);
			}
			if (ts != null) {
				ts.workitems = wiList;
				if (wiList != null)
					ts.setCount = wiList.size();
			}
			return ts;
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			rs.close();
			ps.close();
			conn.close();
		}
	}
	
	private TWorkitemSet searchRoleSentItemsByWorkflow(TWorkitemQuery twi,
			String sysStatus) throws Exception {
		TWorkitemSet ts = null;
		String strQueryWhere = "";
		ArrayList<TWorkitemInfo> wiList = new ArrayList<TWorkitemInfo>();
		if ((sysStatus == null) || (sysStatus.trim().length() <= 0))
			sysStatus = "ACTIVE";
		String criteria = twi.sysStatus;
		if ((criteria != null) && (criteria.length() > 0)
				&& (criteria.equalsIgnoreCase("actioned")))
			strQueryWhere = " AND A.ParentItemID <> 0 ";
		Connection conn = null;
		ResultSet rs = null;
		String sqlQuery = null;
		PreparedStatement ps = null;
		try {
			conn = DBUtil.getECMDBConnection();
			String countQuery = "SELECT Count(DISTINCT(A.WORKFLOWID)) AS TotalCount FROM ECM_WORKITEM_SENT A, "
					+ "ECM_WORKFLOW B "
					+ "WHERE  A.SenderRoleID = "
					+ twi.userId
					+ " AND A.WORKFLOWID = B.ID "
					+ "AND A.Status = '"
					+ sysStatus
					+ "' "
					+ strQueryWhere
					+ " " + getQueryString(twi, true);
			ts = getWorkitemSet(conn, twi.empNo, countQuery, twi.pageNo);
			ts.workitems = wiList;
			setPaginationClause(ts);
			sqlQuery = "SELECT A.Priority, B.Subject, A.Status, A.ID, A.CreatedDate, A.WorkflowID, "
					+ "(SELECT Name FROM ECM_ROLE WHERE ID = B.RoleID  AND ROWNUM = 1) AS WfCreatorName, "
					+ "(SELECT FullName FROM ECM_USER WHERE EMPNo = B.CREATEDBY  AND ROWNUM = 1) AS WfCreatorNameEmp, "
					+ "(SELECT Name FROM ECM_ROLE WHERE ID = A.SenderRoleID  AND ROWNUM = 1) AS SenderRoleName "
					+ "FROM ECM_WORKITEM_SENT A, ECM_WORKFLOW B "
					+ "WHERE  A.SenderRoleID = "
					+ twi.userId
					+ " AND A.WorkflowID = B.ID "
					+ "AND A.Status = '"
					+ sysStatus
					+ "' "
					+ strQueryWhere
					+ " "
					+ getQueryString(twi, true) + getOrderByString(twi, true);
			String pageWhere = "SELECT * FROM "
					+ "(SELECT X.*, ROWNUM rnum FROM (" + sqlQuery + ") X "
					+ "WHERE ROWNUM <= " + maxRow + ") " + "WHERE rnum > "
					+ minRow;
			ps = conn.prepareStatement(pageWhere);
			rs = ps.executeQuery();
			int iterCount = 0;
			while (rs.next()) {
				iterCount++;
				TWorkitemInfo wi = new TWorkitemInfo();
				wi.workitemId = rs.getInt("ID");
				wi.priority = rs.getInt("Priority");
				wi.subject = rs.getString("Subject");
				wi.status = rs.getString("Status");
				wi.receivedDate = DBUtil.convertDateTimeToString(rs
						.getTimestamp("CreatedDate"));
				wi.wfCreatorName = rs.getString("WFCreatorName");
				if ((wi.wfCreatorName == null)
						|| (wi.wfCreatorName.length() <= 0))
					wi.wfCreatorName = rs.getString("WFCreatorNameEmp");
				wi.senderName = rs.getString("SenderRoleName");
				wi.senderId = twi.userId;
				wi.workflowId = rs.getInt("WorkflowID");
				if(iterCount == 1)
					wiList.add(wi);
				else if(validateSentItemList(wiList, wi.workflowId, wi.workitemId))
					wiList.add(wi);
			}
			if (ts != null) {
				ts.workitems = wiList;
				if (wiList != null)
					ts.setCount = wiList.size();
			}
			return ts;
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			rs.close();
			ps.close();
			conn.close();
		}
	}
	
	private TWorkitemSet searchRoleRelatedSentItems(TWorkitemQuery twi,
			String sysStatus) throws Exception {
		TWorkitemSet ts = null;
		String strQueryWhere = "";
		ArrayList<TWorkitemInfo> wiList = new ArrayList<TWorkitemInfo>();
		if ((sysStatus == null) || (sysStatus.trim().length() <= 0))
			sysStatus = "ACTIVE";
		String criteria = twi.sysStatus;
		if ((criteria != null) && (criteria.length() > 0)
				&& (criteria.equalsIgnoreCase("actioned")))
			strQueryWhere = " AND A.ParentItemID <> 0 ";
		Connection conn = null;
		ResultSet rs = null;
		String sqlQuery = null;
		PreparedStatement ps = null;
		try {
			conn = DBUtil.getECMDBConnection();
			String countQuery = "SELECT Count(A.ID) AS TotalCount FROM ECM_WORKITEM_SENT A, "
					+ "ECM_WORKFLOW B "
					+ "WHERE  A.SenderRoleID = " + twi.userId
					+ " AND A.WORKFLOWID = B.ID "
					+ strQueryWhere
					+ "AND A.WorkflowID = " + twi.workflowId 
					+ "AND A.ID <> " + twi.workitemId
					+ "AND A.Status = '" + sysStatus + "' ";
			
			ts = getWorkitemSet(conn, twi.empNo, countQuery, twi.pageNo);
			ts.workitems = wiList;
			setPaginationClause(ts);
			sqlQuery = "SELECT A.Priority, B.Subject, A.Status, A.ID, A.CreatedDate, A.WorkflowID, "
					+ "(SELECT Name FROM ECM_ROLE WHERE ID = B.RoleID  AND ROWNUM = 1) AS WfCreatorName, "
					+ "(SELECT FullName FROM ECM_USER WHERE EMPNo = B.CREATEDBY  AND ROWNUM = 1) AS WfCreatorNameEmp, "
					+ "(SELECT Name FROM ECM_ROLE WHERE ID = A.SenderRoleID  AND ROWNUM = 1) AS SenderRoleName "
					+ "FROM ECM_WORKITEM_SENT A, ECM_WORKFLOW B "
					+ "WHERE  A.SenderRoleID = " + twi.userId
					+ " AND A.WORKFLOWID = B.ID "
					+ strQueryWhere
					+ "AND A.WorkflowID = " + twi.workflowId 
					+ "AND A.ID <> " + twi.workitemId
					+ "AND A.Status = '" + sysStatus + "' " + getOrderByString(twi, true);
			
			String pageWhere = "SELECT * FROM "
					+ "(SELECT X.*, ROWNUM rnum FROM (" + sqlQuery + ") X "
					+ "WHERE ROWNUM <= " + maxRow + ") " + "WHERE rnum > "
					+ minRow;
			
			ps = conn.prepareStatement(pageWhere);
			rs = ps.executeQuery();
			while (rs.next()) {
				TWorkitemInfo wi = new TWorkitemInfo();
				wi.workitemId = rs.getInt("ID");
				wi.priority = rs.getInt("Priority");
				wi.subject = rs.getString("Subject");
				wi.status = rs.getString("Status");
				wi.receivedDate = DBUtil.convertDateTimeToString(rs
						.getTimestamp("CreatedDate"));
				wi.wfCreatorName = rs.getString("WFCreatorName");
				if ((wi.wfCreatorName == null)
						|| (wi.wfCreatorName.length() <= 0))
					wi.wfCreatorName = rs.getString("WFCreatorNameEmp");
				wi.senderName = rs.getString("SenderRoleName");
				wi.senderId = twi.userId;
				wi.workflowId = rs.getInt("WorkflowID");
				wiList.add(wi);
			}
			if (ts != null) {
				ts.workitems = wiList;
				if (wiList != null)
					ts.setCount = wiList.size();
			}
			return ts;
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			rs.close();
			ps.close();
			conn.close();
		}
	}

	private ArrayList<ECMSentitems> searchRoleSentItemsReport(
			TWorkitemQuery twi, String sysStatus) throws Exception {
		String strQueryWhere = " ";
		ArrayList<ECMSentitems> wiList = new ArrayList<ECMSentitems>();
		if ((sysStatus == null) || (sysStatus.trim().length() <= 0))
			sysStatus = "ACTIVE";
		String criteria = twi.sysStatus;
		if ((criteria != null) && (criteria.length() > 0)
				&& (criteria.equalsIgnoreCase("actioned")))
			strQueryWhere = " AND A.ParentItemID <> 0 ";
		Connection conn = null;
		ResultSet rs = null;
		String sqlQuery = null;
		PreparedStatement ps = null;
		try {
			conn = DBUtil.getECMDBConnection();
			sqlQuery = "SELECT A.Priority, B.Subject, A.Status, B.ECMNo, A.Remarks, A.ID, A.CreatedDate, A.WorkflowID, "
					+ "(SELECT Name FROM ECM_ROLE WHERE ID = B.RoleID  AND ROWNUM = 1) AS WfCreatorName, "
					+ "(SELECT FullName FROM ECM_USER WHERE EMPNo = B.CREATEDBY  AND ROWNUM = 1) AS WfCreatorNameEmp, "
					+ "(SELECT Name FROM ECM_ROLE WHERE ID = A.SenderRoleID  AND ROWNUM = 1) AS SenderRoleName "
					+ "FROM ECM_WORKITEM_SENT A, ECM_WORKFLOW B "
					+ "WHERE  A.SenderRoleID = "
					+ twi.userId
					+ " AND A.WorkflowID = B.ID "
					+ strQueryWhere
					+ " AND A.Status = '"
					+ sysStatus
					+ "' "
					+ strQueryWhere
					+ " "
					+ getQueryString(twi, true)
					+ getOrderByString(twi, true);

			ps = conn.prepareStatement(sqlQuery);
			rs = ps.executeQuery();
			while (rs.next()) {
				ECMSentitems wi = new ECMSentitems();
				wi.setPriority(getPriorityValue(rs.getInt("Priority")));
				wi.setSubject(rs.getString("Subject"));
				wi.setStatus(rs.getString("Status"));
				wi.setReceivedDate(DBUtil.convertDateTimeToString(rs
						.getTimestamp("CreatedDate")));
				wi.setWfCreatorName(rs.getString("WFCreatorName"));
				if ((wi.getWfCreatorName() == null)
						|| (wi.getWfCreatorName().length() <= 0))
					wi.setWfCreatorName(rs.getString("WFCreatorNameEmp"));
				wi.setSenderName(rs.getString("SenderRoleName"));
				wi.setSentitemId(rs.getInt("WorkflowID"));
				wi.setRemarks(rs.getString("Remarks"));
				wi.setEcmNo(rs.getString("EcmNo"));
				wi.setTrackSentitem(getSentItemTrackHistory(conn,
						wi.getSentitemId(), twi.userId));
				wiList.add(wi);
			}
			return wiList;
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			rs.close();
			ps.close();
			conn.close();
		}
	}

	private TWorkitemSet searchRoleActionedItems(TWorkitemQuery twi,
			String sysStatus) throws Exception {
		TWorkitemSet ts = null;
		ArrayList<TWorkitemInfo> wiList = new ArrayList<TWorkitemInfo>();
		if ((sysStatus == null) || (sysStatus.trim().length() <= 0))
			sysStatus = "ACTIVE";
		Connection conn = null;
		ResultSet rs = null;
		String sqlQuery = null;
		PreparedStatement ps = null;
		try {
			conn = DBUtil.getECMDBConnection();
			String countQuery = "SELECT Count(A.ID) AS TotalCount FROM ECM_WORKITEM_SENT A, "
					+ "ECM_WORKFLOW B, ECM_WORKITEM C "
					+ "WHERE  A.SenderRoleID = "
					+ twi.userId
					+ " AND C.RecipientRoleID = "
					+ twi.userId
					+ " AND A.WorkflowID = B.ID AND A.ParentItemID = C.ID "
					+ "AND C.STATUS IN ('Forward','Reply') "
					+ getActionedQueryString(twi, true);
			// AND c.RecipientRoleID = 10000
			ts = getWorkitemSet(conn, twi.empNo, countQuery, twi.pageNo);
			ts.workitems = wiList;
			setPaginationClause(ts);
			sqlQuery = "SELECT A.Priority, B.Subject, A.Status, A.ID, A.CreatedDate, "
					+ "(SELECT Name FROM ECM_ROLE WHERE ID = B.RoleID  AND ROWNUM = 1) AS WfCreatorName, "
					+ "(SELECT FullName FROM ECM_USER WHERE EMPNo = B.CREATEDBY  AND ROWNUM = 1) AS WfCreatorNameEmp, "
					+ "(SELECT Name FROM ECM_ROLE WHERE ID = A.SenderRoleID  AND ROWNUM = 1) AS SenderRoleName "
					+ "FROM ECM_WORKITEM_SENT A, ECM_WORKFLOW B, ECM_WORKITEM C "
					+ "WHERE  A.SenderRoleID = "
					+ twi.userId
					+ " AND C.RecipientRoleID = "
					+ twi.userId
					+ " "
					+ "AND A.WorkflowID = B.ID AND A.ParentItemID = C.ID AND C.STATUS IN ('Forward','Reply') "
					+ getActionedQueryString(twi, true)
					+ getOrderByString(twi, true);
			String pageWhere = "SELECT * FROM "
					+ "(SELECT X.*, ROWNUM rnum FROM (" + sqlQuery + ") X "
					+ "WHERE ROWNUM <= " + maxRow + ") " + "WHERE rnum > "
					+ minRow;
			ps = conn.prepareStatement(pageWhere);
			rs = ps.executeQuery();
			while (rs.next()) {
				TWorkitemInfo wi = new TWorkitemInfo();
				wi.workitemId = rs.getInt("ID");
				wi.priority = rs.getInt("Priority");
				wi.subject = rs.getString("Subject");
				wi.status = rs.getString("Status");
				wi.receivedDate = DBUtil.convertDateTimeToString(rs
						.getTimestamp("CreatedDate"));
				wi.wfCreatorName = rs.getString("WFCreatorName");
				if ((wi.wfCreatorName == null)
						|| (wi.wfCreatorName.length() <= 0))
					wi.wfCreatorName = rs.getString("WFCreatorNameEmp");
				wi.senderName = rs.getString("SenderRoleName");
				wi.senderId = twi.userId;
				wiList.add(wi);
			}
			if (ts != null) {
				ts.workitems = wiList;
				if (wiList != null)
					ts.setCount = wiList.size();
			}
			return ts;
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			rs.close();
			ps.close();
			conn.close();
		}
	}

	private ArrayList<ECMSentitems> searchRoleActionedItemsReport(
			TWorkitemQuery twi, String sysStatus) throws Exception {
		ArrayList<ECMSentitems> wiList = new ArrayList<ECMSentitems>();
		if ((sysStatus == null) || (sysStatus.trim().length() <= 0))
			sysStatus = "ACTIVE";
		Connection conn = null;
		ResultSet rs = null;
		String sqlQuery = null;
		PreparedStatement ps = null;
		try {
			conn = DBUtil.getECMDBConnection();
			sqlQuery = "SELECT A.Priority, B.Subject, A.Status, A.ID, A.CreatedDate, A.WorkflowID, "
					+ "(SELECT Name FROM ECM_ROLE WHERE ID = B.RoleID  AND ROWNUM = 1) AS WfCreatorName, "
					+ "(SELECT FullName FROM ECM_USER WHERE EMPNo = B.CREATEDBY  AND ROWNUM = 1) AS WfCreatorNameEmp, "
					+ "(SELECT Name FROM ECM_ROLE WHERE ID = A.SenderRoleID  AND ROWNUM = 1) AS SenderRoleName "
					+ "FROM ECM_WORKITEM_SENT A, ECM_WORKFLOW B, ECM_WORKITEM C "
					+ "WHERE  A.SenderRoleID = "
					+ twi.userId
					+ " AND C.RecipientRoleID = "
					+ twi.userId
					+ " "
					+ "AND A.WorkflowID = B.ID AND A.ParentItemID = C.ID AND C.STATUS IN ('Forward','Reply') "
					+ getActionedQueryString(twi, true)
					+ getOrderByString(twi, true);
			ps = conn.prepareStatement(sqlQuery);
			rs = ps.executeQuery();
			while (rs.next()) {
				ECMSentitems wi = new ECMSentitems();
				wi.setPriority(getPriorityValue(rs.getInt("Priority")));
				wi.setSubject(rs.getString("Subject"));
				wi.setStatus(rs.getString("Status"));
				wi.setReceivedDate(DBUtil.convertDateTimeToString(rs
						.getTimestamp("CreatedDate")));
				wi.setWfCreatorName(rs.getString("WFCreatorName"));
				if ((wi.getWfCreatorName() == null)
						|| (wi.getWfCreatorName().length() <= 0))
					wi.setWfCreatorName(rs.getString("WFCreatorNameEmp"));
				wi.setSenderName(rs.getString("SenderRoleName"));
				wi.setSentitemId(rs.getInt("WorkflowID"));
				wi.setTrackSentitem(getSentItemTrackHistory(conn,
						wi.getSentitemId(), twi.userId));
				wiList.add(wi);
			}
			return wiList;
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			rs.close();
			ps.close();
			conn.close();
		}
	}

	private TWorkitemSet searchRoleActionedItemsCount(long userId,
			String sysStatus, String dashType) throws Exception {
		TWorkitemSet ts = null;
		if ((sysStatus == null) || (sysStatus.trim().length() <= 0))
			sysStatus = "ACTIVE";
		Connection conn = null;

		try {
			conn = DBUtil.getECMDBConnection();
			String countQuery = "SELECT Count(A.ID) AS TotalCount FROM ECM_WORKITEM_SENT A, "
					+ "ECM_WORKFLOW B, ECM_WORKITEM C "
					+ "WHERE  A.SenderRoleID = "
					+ userId
					+ " AND C.RecipientRoleID = "
					+ userId
					+ " AND A.WorkflowID = B.ID AND A.ParentItemID = C.ID "
					+ "AND C.STATUS " + "IN ('Forward','Reply') ";

			if (dashType != null && dashType.equalsIgnoreCase("TODAY")) {
				countQuery += "AND "
						+ getDateClause(DBUtil.getTodayDate(), "CreatedDate",
								"search", "");
			}

			ts = getWorkitemSet(conn, 0, countQuery, 0);

			return ts;
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			conn.close();
		}
	}

	public long getArchiveWorkitemsCount(String bDate, String roleId,
			String empNo) throws Exception {
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		long sentItem = 0;
		try {
			conn = DBUtil.getECMDBConnection();
			Date cDate = DBUtil.convertStringToDateEx(bDate);
			ps = conn
					.prepareStatement("SELECT COUNT(ID) AS TotalCount FROM ECM_WORKITEM WHERE CREATEDDATE < ? "
							+ "AND SYSTEMSTATUS = 'ACTIVE' AND ((RECIPIENTROLEID = ? AND RECIPIENTROLEID <> 0) "
							+ "OR (RECIPIENTEMPNO = ? AND RECIPIENTEMPNO <> 0))");
			ps.setTimestamp(1, new Timestamp(cDate.getTime()));
			ps.setInt(2, (int) DBUtil.stringToLong(roleId));
			ps.setInt(3, (int) DBUtil.stringToLong(empNo));
			rs = ps.executeQuery();
			if ((rs != null) && rs.next()) {
				sentItem = rs.getInt("TotalCount");
			}
			return sentItem;
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			rs.close();
			ps.close();
			conn.close();
		}
	}

	public long getArchiveSentItemsCount(String bDate, String roleId,
			String empId) throws Exception {
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		long sentItem = 0;
		try {
			conn = DBUtil.getECMDBConnection();
			Date cDate = DBUtil.convertStringToDateEx(bDate);
			ps = conn
					.prepareStatement("SELECT COUNT(ID) AS TotalCount FROM ECM_WORKITEM_SENT WHERE CREATEDDATE < ? "
							+ "AND STATUS = 'ACTIVE' AND ((SENDERROLEID = ? AND SENDERROLEID <> 0) "
							+ "OR (SENDEREMPNO = ? AND SENDEREMPNO <> 0))");
			ps.setTimestamp(1, new Timestamp(cDate.getTime()));
			ps.setString(2, DBUtil.escapeString(roleId));
			ps.setString(3, DBUtil.escapeString(empId));
			rs = ps.executeQuery();
			if ((rs != null) && rs.next()) {
				sentItem = rs.getInt("TotalCount");
			}
			return sentItem;
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			rs.close();
			ps.close();
			conn.close();
		}
	}

	public TWorkitemSet searchSentItemsByWorkflow(TWorkitemQuery twi) throws Exception {
		if (twi.userType.equalsIgnoreCase("ROLE"))
			return searchRoleSentItemsByWorkflow(twi, twi.repStatus.toUpperCase());
		else
			return searchUserSentItemsByWorkflow(twi, twi.repStatus.toUpperCase());
	}
	
	public TWorkitemSet searchSentItems(TWorkitemQuery twi) throws Exception {
		if (twi.userType.equalsIgnoreCase("ROLE"))
			return searchRoleSentItems(twi, twi.repStatus.toUpperCase());
		else
			return searchUserSentItems(twi, twi.repStatus.toUpperCase());
	}
	
	public TWorkitemSet searchRelatedSentItems(TWorkitemQuery twi) throws Exception {
		if (twi.userType.equalsIgnoreCase("ROLE"))
			return searchRoleRelatedSentItems(twi, twi.repStatus.toUpperCase());
		else
			return searchUserRelatedSentItems(twi, twi.repStatus.toUpperCase());
	}

	public TWorkitemSet searchActionedItems(TWorkitemQuery twi)
			throws Exception {
		if (twi.userType.equalsIgnoreCase("ROLE"))
			return searchRoleActionedItems(twi, twi.repStatus.toUpperCase());
		else
			return searchUserActionedItems(twi, twi.repStatus.toUpperCase());
	}

	public TWorkitemSet searchInboxItems(TWorkitemQuery twi) throws Exception {
		if (twi.userType.equalsIgnoreCase("ROLE"))
			return searchRoleInboxItems(twi, twi.repStatus.toUpperCase(),
					twi.sysStatus);
		else
			return searchUserInboxItems(twi, twi.repStatus.toUpperCase(),
					twi.sysStatus);
	}

	public TWorkitemSet searchNewWorkitemCount(String userId) throws Exception {
		TWorkitemSet ts = new TWorkitemSet();
		int wiCount = 0;
		try {
			String userName = ECMUserList.getInstance().getLoginName(
					DBUtil.stringToLong(userId));
			TUser user = ECMUserList.getInstance().getUserDetails(userName);
			wiCount += searchUserInboxNewItemsCount(
					DBUtil.stringToLong(userId), "ACTIVE", "", "");
			if (user != null) {
				ArrayList<TDelegate> tdList = user.delegated;
				for (TDelegate td : tdList) {
					wiCount += searchUserInboxNewItemsCount(td.delegateId,
							"ACTIVE", "", "");
				}
				ArrayList<TRole> tRoleList = user.roles;
				for (TRole tr : tRoleList) {
					wiCount += searchRoleInboxNewItemsCount(tr.id, "ACTIVE",
							"", "");
				}
			}
			ts = new TWorkitemSet();
			ts.totalCount = wiCount;
			return ts;
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		}
	}

	private int searchUserInboxNewItemsCount(long userId, String sysStatus,
			String itemType, String dashType) throws Exception {
		int iCount = 0;
		if ((sysStatus == null) || (sysStatus.trim().length() <= 0))
			sysStatus = "ACTIVE";
		Connection conn = null;
		try {
			conn = DBUtil.getECMDBConnection();
			String countQuery = "SELECT count(A.ID) AS TotalCount FROM ECM_WORKITEM A, "
					+ "ECM_USER C "
					+ "WHERE  A.RecipientEMPNo = "
					+ userId
					+ " AND A.Status = 'New' "
					+ "AND C.EMPNo = A.RecipientEMPNo AND A.SystemStatus = '"
					+ sysStatus + "' ";

			if (itemType != null && itemType.length() > 0) {
				if (itemType.equalsIgnoreCase("TO"))
					countQuery += " AND UPPER(A.TYPE) = 'TO' ";
				else if (itemType.equalsIgnoreCase("CC"))
					countQuery += " AND UPPER(A.TYPE) = 'CC' ";
			}

			if (dashType != null && dashType.equalsIgnoreCase("TODAY")) {
				countQuery += "AND "
						+ getDateClause(DBUtil.getTodayDate(), "CreatedDate",
								"search", "");
			}

			iCount = (int) getItemsCount(conn, countQuery);
			return iCount;
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			conn.close();
		}
	}

	private int searchRoleInboxNewItemsCount(long roleId, String sysStatus,
			String itemType, String dashType) throws Exception {
		int iCount = 0;
		if ((sysStatus == null) || (sysStatus.trim().length() <= 0))
			sysStatus = "ACTIVE";
		Connection conn = null;
		try {
			conn = DBUtil.getECMDBConnection();
			String countQuery = "SELECT count(A.ID) AS TotalCount FROM ECM_WORKITEM A, ECM_ROLE C "
					+ "WHERE  A.RecipientRoleID = "
					+ roleId
					+ " AND A.Status = 'New' "
					+ "AND A.RecipientRoleID = C.ID "
					+ "AND "
					+ "A.SystemStatus = '" + sysStatus + "' ";

			if (itemType != null && itemType.length() > 0) {
				if (itemType.equalsIgnoreCase("TO"))
					countQuery += " AND UPPER(A.TYPE) = 'TO' ";
				else if (itemType.equalsIgnoreCase("CC"))
					countQuery += " AND UPPER(A.TYPE) = 'CC' ";
			}

			if (dashType != null && dashType.equalsIgnoreCase("TODAY")) {
				countQuery += "AND "
						+ getDateClause(DBUtil.getTodayDate(), "CreatedDate",
								"search", "");
			}

			iCount = (int) getItemsCount(conn, countQuery);
			return iCount;
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			conn.close();
		}
	}

	private int searchUserInboxReadItemsCount(long userId, String sysStatus,
			String itemType, String dashType) throws Exception {
		int iCount = 0;
		if ((sysStatus == null) || (sysStatus.trim().length() <= 0))
			sysStatus = "ACTIVE";
		Connection conn = null;
		try {
			conn = DBUtil.getECMDBConnection();
			String countQuery = "SELECT count(A.ID) AS TotalCount FROM ECM_WORKITEM A, "
					+ "ECM_USER C "
					+ "WHERE  A.RecipientEMPNo = "
					+ userId
					+ " AND A.Status = 'Read' "
					+ "AND C.EMPNo = A.RecipientEMPNo AND A.SystemStatus = '"
					+ sysStatus + "' ";

			if (itemType != null && itemType.length() > 0) {
				if (itemType.equalsIgnoreCase("TO"))
					countQuery += " AND UPPER(A.TYPE) = 'TO' ";
				else if (itemType.equalsIgnoreCase("CC"))
					countQuery += " AND UPPER(A.TYPE) = 'CC' ";
			}

			if (dashType != null && dashType.equalsIgnoreCase("TODAY")) {
				countQuery += "AND "
						+ getDateClause(DBUtil.getTodayDate(), "CreatedDate",
								"search", "");
			}

			iCount = (int) getItemsCount(conn, countQuery);
			return iCount;
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			conn.close();
		}
	}

	private int searchRoleInboxReadItemsCount(long roleId, String sysStatus,
			String itemType, String dashType) throws Exception {
		int iCount = 0;
		if ((sysStatus == null) || (sysStatus.trim().length() <= 0))
			sysStatus = "ACTIVE";
		Connection conn = null;
		try {
			conn = DBUtil.getECMDBConnection();
			String countQuery = "SELECT count(A.ID) AS TotalCount FROM ECM_WORKITEM A, ECM_ROLE C "
					+ "WHERE  A.RecipientRoleID = "
					+ roleId
					+ " AND A.Status = 'Read' "
					+ "AND A.RecipientRoleID = C.ID "
					+ "AND "
					+ "A.SystemStatus = '" + sysStatus + "' ";

			if (itemType != null && itemType.length() > 0) {
				if (itemType.equalsIgnoreCase("TO"))
					countQuery += " AND UPPER(A.TYPE) = 'TO' ";
				else if (itemType.equalsIgnoreCase("CC"))
					countQuery += " AND UPPER(A.TYPE) = 'CC' ";
			}

			if (dashType != null && dashType.equalsIgnoreCase("TODAY")) {
				countQuery += "AND "
						+ getDateClause(DBUtil.getTodayDate(), "CreatedDate",
								"search", "");
			}

			iCount = (int) getItemsCount(conn, countQuery);
			return iCount;

		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			conn.close();
		}
	}

	private String getPriorityValue(long priority) {
		String strValue = "Normal";
		int iPriority = (int) priority;
		if (iPriority == 3)
			strValue = "High";
		else if (iPriority == 1)
			strValue = "Low";
		return strValue;
	}

	public void exportInboxItems(HttpServletRequest req,
			HttpServletResponse resp, TWorkitemQuery twi) throws Exception {
		ArrayList<ECMInboxItems> searchOutput = null;
		String reportTitle = "";
		if (twi.repStatus != null && twi.repStatus.equalsIgnoreCase("active"))
			reportTitle = "ECM Inbox Report";
		else
			reportTitle = "ECM Archive Inbox Report";

		if (twi.userType.equalsIgnoreCase("ROLE"))
			searchOutput = searchRoleInboxItemsReport(twi,
					twi.repStatus.toUpperCase(), "");
		else
			searchOutput = searchUserInboxItemsReport(twi,
					twi.repStatus.toUpperCase(), "");
		JasperHelper.generateInboxJasperReport(resp, req, reportTitle,
				searchOutput, twi);
	}

	public void exportActionedItems(HttpServletRequest req,
			HttpServletResponse resp, TWorkitemQuery twi) throws Exception {
		ArrayList<ECMSentitems> searchOutput = null;
		twi.pageNo = 0;
		if (twi.userType.equalsIgnoreCase("ROLE"))
			searchOutput = searchRoleActionedItemsReport(twi,
					twi.repStatus.toUpperCase());
		else
			searchOutput = searchUserActionedItemsReport(twi,
					twi.repStatus.toUpperCase());
		JasperHelper.generateSentitemJasperReport(resp, req,
				"ECM Actioned Track Report", searchOutput, twi, true);
	}

	public void exportSentItems(HttpServletRequest req,
			HttpServletResponse resp, TWorkitemQuery twi) throws Exception {
		ArrayList<ECMSentitems> searchOutput = null;
		String reportTitle = "";
		if (twi.repStatus != null && twi.repStatus.equalsIgnoreCase("active"))
			reportTitle = "ECM Track Report";
		else
			reportTitle = "ECM Archive Track Report";

		if (twi.userType.equalsIgnoreCase("ROLE"))
			searchOutput = searchRoleSentItemsReport(twi,
					twi.repStatus.toUpperCase());
		else
			searchOutput = searchUserSentItemsReport(twi,
					twi.repStatus.toUpperCase());
		JasperHelper.generateSentitemJasperReport(resp, req, reportTitle,
				searchOutput, twi, true);
	}

	public ArrayList<TWorkitemAction> getDraftItems(long userId, String userType)
			throws Exception {
		if ((userType == null) || (!userType.trim().equalsIgnoreCase("ROLE")))
			userType = "USER";
		return readDraftItems(userId, userType);
	}

	public void saveActionAsDraft(long empNo, long roleId, long draftId,
			String actionType, String jsonString) throws Exception {
		String userType = "USER";
		long userId = empNo;
		if (roleId > 0) {
			userType = "ROLE";
			userId = roleId;
		}
		if (draftId > 0)
			deleteDraftItem(draftId);
		saveDraft(userId, userType, actionType, jsonString);
	}

	private void saveDraft(long userId, String userType, String actionType,
			String action) throws Exception {
		Connection conn = null;
		PreparedStatement ps = null;
		Clob itemClob = null;
		try {
			conn = DBUtil.getECMDBConnection();
			String sqlQuery = "INSERT INTO ECM_DRAFTS "
					+ "(ID, USERID, USERTYPE, ACTIONTYPE, CREATEDDATE, JSONSTRING, STATUS) "
					+ "VALUES (ECM_DRAFTS_SEQ.NEXTVAL, ?, ?, ?, SYSDATE, ?, 'ACTIVE')";
			ps = conn.prepareStatement(sqlQuery);
			itemClob = oracle.sql.CLOB.createTemporary(conn, false,
					oracle.sql.CLOB.DURATION_SESSION);
			itemClob.setString(1, action);
			ps.setInt(1, (int) userId);
			ps.setString(2, DBUtil.escapeString(userType));
			ps.setString(3, DBUtil.escapeString(actionType));
			ps.setClob(4, itemClob);
			ps.executeUpdate();
			itemClob.free();
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			ps.close();
			conn.close();
		}
	}

	private ArrayList<TWorkitemAction> readDraftItems(long userId,
			String userType) throws Exception {
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		Clob itemClob = null;
		ArrayList<TWorkitemAction> actList = new ArrayList<TWorkitemAction>();
		try {
			conn = DBUtil.getECMDBConnection();
			String sqlQuery = "SELECT ID, ACTIONTYPE, JSONSTRING, CREATEDDATE FROM ECM_DRAFTS "
					+ "WHERE USERID = ? AND USERTYPE = ?";
			ps = conn.prepareStatement(sqlQuery);
			ps.setInt(1, (int) userId);
			ps.setString(2, DBUtil.escapeString(userType));
			rs = ps.executeQuery();
			while (rs.next()) {
				String actionType = rs.getString(2);
				long draftId = rs.getInt(1);
				itemClob = rs.getClob(3);
				String jsonString = DBUtil.clobToString(itemClob);
				itemClob.free();
				TWorkitemAction ta = getWorkitemAction(jsonString);
				ta.wiAction = actionType;
				ta.draftId = draftId;
				ta.draftDate = DBUtil.formatDateForUI(rs
						.getTimestamp("CREATEDDATE"));
				actList.add(ta);
			}
			return actList;
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			rs.close();
			ps.close();
			conn.close();
		}
	}

	private TWorkitemAction getWorkitemAction(String jsonString)
			throws Exception {
		try {
			ObjectMapper mapper = new ObjectMapper();
			mapper.setDateFormat(new SimpleDateFormat("dd-MM-yyyy hh:mm a"));
			return mapper.readValue(jsonString,
					new TypeReference<TWorkitemAction>() {
					});
		} catch (Exception e) {
			throw new Exception("Error while parsing the draft item. "
					+ e.getMessage());
		}
	}

	private void deleteDraftItem(long draftId) {
		try {
			Connection conn = null;
			PreparedStatement ps = null;
			try {
				conn = DBUtil.getECMDBConnection();
				String sqlQuery = "DELETE FROM ECM_DRAFTS WHERE ID = ?";
				ps = conn.prepareStatement(sqlQuery);
				ps.setInt(1, (int) draftId);
				ps.executeUpdate();
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				ps.close();
				conn.close();
			}
		} catch (Exception oe) {
		}
	}

	public String validateWorkitem(long witemId) throws Exception {
		ECMWorkItem wi = new ECMWorkItem();
		wi.setId(witemId);
		return wi.getWorkitemStatus();
	}

	public TWorkitemSet getRootItemsFromWorkItem(long witemId) throws Exception {
		ECMWorkItem wi = new ECMWorkItem();
		wi.setId(witemId);
		return getRootItemsFromSentItem(wi.getParentSentItem());
	}

	public TWorkitemSet getRootItemsFromSentItem(long sentItemId)
			throws Exception {
		ECMSentItem si = new ECMSentItem();
		si.setId(sentItemId);
		long rootSentItem = si.getRootSentItem();
		si.setId(rootSentItem);
		return si.getWorkitems();
	}

	public TWorkitemSet getChildItemsFromWorkItem(long witemId)
			throws Exception {
		ECMWorkItem wi = new ECMWorkItem();
		wi.setId(witemId);
		ECMSentItem si = new ECMSentItem();
		si.setId(wi.getChildSentItem());
		return si.getWorkitems();
	}

	public void sendEmailNotifications() throws Exception {
		ArrayList<Long> remList = getWorkitemsForNotification("REMINDER");
		sendEmailsFromList(remList, "REMINDER");
		ArrayList<Long> dlList = getWorkitemsForNotification("DEADLINE");
		sendEmailsFromList(dlList, "DEADLINE");
	}

	private void sendEmailsFromList(ArrayList<Long> wiList, String type) {
		for (Long wiId : wiList) {
			try {
				ECMWorkItem ewi = new ECMWorkItem();
				ewi.setId(wiId);
				TWorkitemDetails twd = ewi.getDetails();
				ewi.sendEmailFromDetails(type, twd);
				updateNotificationStatus(wiId, type);
			} catch (Exception e) {
			}
		}
	}

	private ArrayList<Long> getWorkitemsForNotification(String type)
			throws Exception {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		String sqlQuery = null;
		try {
			ArrayList<Long> wiList = new ArrayList<Long>();
			conn = DBUtil.getECMDBConnection();
			if (type.equalsIgnoreCase("REMINDER"))
				sqlQuery = "SELECT WORKITEMID from ECM_NOTIFICATIONS WHERE REMINDERSTATUS = 'PENDING' "
						+ "AND REMINDER <= SYSTIMESTAMP ORDER BY REMINDER ASC";
			else
				sqlQuery = "SELECT WORKITEMID from ECM_NOTIFICATIONS WHERE DEADLINESTATUS = 'PENDING' "
						+ "AND DEADLINE <= SYSTIMESTAMP ORDER BY DEADLINE ASC";
			stmt = conn.prepareStatement(sqlQuery);
			rs = stmt.executeQuery();
			while (rs.next()) {
				wiList.add(new Long(rs.getInt("WORKITEMID")));
			}
			return wiList;
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			rs.close();
			stmt.close();
			conn.close();
		}
	}

	private void updateNotificationStatus(long witemId, String type)
			throws Exception {
		Connection conn = null;
		PreparedStatement stmt = null;
		try {
			conn = DBUtil.getECMDBConnection();
			String sqlQuery = "";
			if (type.equalsIgnoreCase("REMINDER"))
				sqlQuery = "UPDATE ECM_NOTIFICATIONS SET REMINDERSTATUS = 'DONE' "
						+ "WHERE WORKITEMID = ? ";
			else
				sqlQuery = "UPDATE ECM_NOTIFICATIONS SET DEADLINESTATUS = 'DONE' "
						+ "WHERE WORKITEMID = ? ";
			stmt = conn.prepareStatement(sqlQuery);
			stmt.setInt(1, (int) witemId);
			stmt.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			stmt.close();
			conn.close();
		}
	}

	public ArrayList<TOrgUnit> getOrgWorkitemCount(String orgCode,
			String userType, String fDate, String tDate, long empNo) throws Exception {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		String sqlQuery = null;
		ArrayList<TOrgUnit> toList = new ArrayList<TOrgUnit>();
		try {
			conn = DBUtil.getECMDBConnection();
			sqlQuery = "select ID, OrgCode, description, LENGTH(TRIM(A.HID)) AS OFFSET, "
					+ "(select count(*) from ecm_workitem where systemstatus = 'ACTIVE' AND "
					+ "CREATEDDATE >= TO_DATE(?, 'DD/MM/YYYY') "
					+ "AND CREATEDDATE < TO_DATE(?, 'DD/MM/YYYY') "
					+ "AND ((isuserindirectorgunit(recipientempno, A.orgcode) = 'YES') OR "
					+ "(isroleinorgunit(recipientroleid, A.orgcode) = 'YES'))) as Workitemcount "
					+ " from ecm_orgunit A where isparentof(getorgid(?),id) = 'YES' order by hid asc";
			
			if(empNo>0){
				String recipientSql = "AND recipientempno = '"+empNo+"'";
				if(userType.equalsIgnoreCase("ROLE"))
					recipientSql = "AND RecipientRoleID = '"+empNo+"'";
					
				sqlQuery = "select ID, OrgCode, description, LENGTH(TRIM(A.HID)) AS OFFSET, "
						+ "(select count(*) from ecm_workitem where systemstatus = 'ACTIVE' AND "
						+ "CREATEDDATE >= TO_DATE(?, 'DD/MM/YYYY') "
						+ "AND CREATEDDATE < TO_DATE(?, 'DD/MM/YYYY') "
						+ recipientSql
						+ " ) as Workitemcount "
						+ "from ecm_orgunit A where orgcode = ? order by hid asc";
			}
			stmt = conn.prepareStatement(sqlQuery);			
			stmt.setString(1, DBUtil.escapeString(fDate));
			stmt.setString(2, DBUtil.escapeString(DBUtil.convertDateToShortString(DBUtil.addDaystoGivenDate(DBUtil.convertStringtoDate(tDate), 1),"")));
			stmt.setString(3, DBUtil.escapeString(orgCode));
			rs = stmt.executeQuery();
			while (rs.next()) {
				TOrgUnit to = new TOrgUnit();
				to.desc = rs.getString("Description");
				if(empNo>0){
					to.desc = ECMUserList.getInstance().getUserFullName(empNo);
					if(userType.equalsIgnoreCase("ROLE"))
						to.desc = ECMRoleList.getInstance().getRoleName(empNo);
					
				}
				to.id = rs.getInt("ID");
				to.orgCode = rs.getString("OrgCode");
				to.offset = rs.getInt("Offset");
				to.count = rs.getInt("WorkitemCount");
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

	public ArrayList<TOrgUnit> getOrgSentitemCount(String orgCode,
			String userType, String fDate, String tDate, long empNo) throws Exception {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		String sqlQuery = null;
		ArrayList<TOrgUnit> toList = new ArrayList<TOrgUnit>();
		try {
			conn = DBUtil.getECMDBConnection(); //DISTINCT(B.WORKFLOWID)
			sqlQuery = "select ID, OrgCode, description, LENGTH(TRIM(A.HID)) AS OFFSET, "
					+ "(select count(A.ID) from ECM_WORKITEM_SENT B, ECM_WORKFLOW C WHERE B.WorkflowID = C.ID AND "
					+ "B.CREATEDDATE >= TO_DATE(?,'DD/MM/YYYY')  AND B.CREATEDDATE < TO_DATE(?,'DD/MM/YYYY') AND "
					+ "B.ID IN (SELECT DISTINCT X.PARENTSENTITEMID FROM ECM_WORKITEM X WHERE X.WORKFLOWID = C.ID AND "
					+ " ((isuserindirectorgunit(X.Senderempno, A.orgcode) = 'YES') OR (isroleinorgunit(X.Senderroleid, A.orgcode) = 'YES')) "
					+ "AND B.Status = 'ACTIVE' )) as Workitemcount "
					+ "from ecm_orgunit A where isparentof(getorgid(?),id) = 'YES' order by hid asc";
			
			if(empNo>0){
				String recipientSql = "AND SenderEMPNo = '"+empNo+"'";
				if(userType.equalsIgnoreCase("ROLE"))
					recipientSql = "AND SenderRoleID = '"+empNo+"'";
					
				sqlQuery = "select ID, OrgCode, description, LENGTH(TRIM(A.HID)) AS OFFSET, "
						+ "(select count(A.ID) from ECM_WORKITEM_SENT B, ECM_WORKFLOW C WHERE B.WorkflowID = C.ID AND "
						+ "B.CREATEDDATE >= TO_DATE(?,'DD/MM/YYYY')  AND B.CREATEDDATE < TO_DATE(?,'DD/MM/YYYY') AND "
						+ "B.ID IN (SELECT DISTINCT X.PARENTSENTITEMID FROM ECM_WORKITEM X WHERE X.WORKFLOWID = C.ID "
						+ recipientSql
						+ " AND B.Status = 'ACTIVE' )) as Workitemcount "
						+ "from ecm_orgunit A where orgcode = ? order by hid asc";
			}
			stmt = conn.prepareStatement(sqlQuery);
			stmt.setString(1, DBUtil.escapeString(fDate));
			stmt.setString(2, DBUtil.escapeString(DBUtil.convertDateToShortString(DBUtil.addDaystoGivenDate(DBUtil.convertStringtoDate(tDate), 1),"")));
			stmt.setString(3, DBUtil.escapeString(orgCode));
			rs = stmt.executeQuery();
			while (rs.next()) {
				TOrgUnit to = new TOrgUnit();
				to.desc = rs.getString("Description");
				if(empNo>0){
					to.desc = ECMUserList.getInstance().getUserFullName(empNo);
					if(userType.equalsIgnoreCase("ROLE"))
						to.desc = ECMRoleList.getInstance().getRoleName(empNo);
					
				}
				to.id = rs.getInt("ID");
				to.orgCode = rs.getString("OrgCode");
				to.offset = rs.getInt("Offset");
				to.count = rs.getInt("WorkitemCount");
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
	
	public ArrayList<TOrgUnit> getOrgSentitemCountByWorkflow(String orgCode,
			String userType, String fDate, String tDate, long empNo) throws Exception {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		String sqlQuery = null;
		ArrayList<TOrgUnit> toList = new ArrayList<TOrgUnit>();
		try {
			conn = DBUtil.getECMDBConnection(); 
			sqlQuery = "select ID, OrgCode, description, LENGTH(TRIM(A.HID)) AS OFFSET, "
					+ "(select count(*) from ECM_WORKITEM_SENT B, ECM_WORKFLOW C WHERE B.WorkflowID = C.ID AND "
					+ "B.CREATEDDATE >= TO_DATE(?,'DD/MM/YYYY')  AND B.CREATEDDATE < TO_DATE(?,'DD/MM/YYYY') AND "
					+ "B.ID IN (SELECT DISTINCT X.PARENTSENTITEMID FROM ECM_WORKITEM X WHERE X.WORKFLOWID = C.ID AND "
					+ " ((isuserindirectorgunit(X.Senderempno, A.orgcode) = 'YES') OR (isroleinorgunit(X.Senderroleid, A.orgcode) = 'YES')) "
					+ "AND B.Status = 'ACTIVE' )) as Workitemcount "
					+ "from ecm_orgunit A where isparentof(getorgid(?),id) = 'YES' order by hid asc";
			
			if(empNo>0){
				String recipientSql = "AND SenderEMPNo = '"+empNo+"'";
				if(userType.equalsIgnoreCase("ROLE"))
					recipientSql = "AND SenderRoleID = '"+empNo+"'";
					
				//DISTINCT(B.WORKFLOWID)
				sqlQuery = "select ID, OrgCode, description, LENGTH(TRIM(A.HID)) AS OFFSET, "
						+ "(select count(*) from ECM_WORKITEM_SENT B, ECM_WORKFLOW C WHERE B.WorkflowID = C.ID AND "
						+ "B.CREATEDDATE >= TO_DATE(?,'DD/MM/YYYY')  AND B.CREATEDDATE < TO_DATE(?,'DD/MM/YYYY') AND "
						+ "B.ID IN (SELECT DISTINCT X.PARENTSENTITEMID FROM ECM_WORKITEM X WHERE X.WORKFLOWID = C.ID "
						+ recipientSql
						+ " AND B.Status = 'ACTIVE' )) as Workitemcount "
						+ "from ecm_orgunit A where orgcode = ? order by hid asc";
			}
			stmt = conn.prepareStatement(sqlQuery);
			stmt.setString(1, DBUtil.escapeString(fDate));
			stmt.setString(2, DBUtil.escapeString(DBUtil.convertDateToShortString(DBUtil.addDaystoGivenDate(DBUtil.convertStringtoDate(tDate), 1),"")));
			stmt.setString(3, DBUtil.escapeString(orgCode));
			rs = stmt.executeQuery();
			while (rs.next()) {
				TOrgUnit to = new TOrgUnit();
				to.desc = rs.getString("Description");
				if(empNo>0){
					to.desc = ECMUserList.getInstance().getUserFullName(empNo);
					if(userType.equalsIgnoreCase("ROLE"))
						to.desc = ECMRoleList.getInstance().getRoleName(empNo);
					
				}
				to.id = rs.getInt("ID");
				to.orgCode = rs.getString("OrgCode");
				to.offset = rs.getInt("Offset");
				to.count = rs.getInt("WorkitemCount");
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


	/*
	 * public TWorkitemSet searchReportInboxitems(TWorkitemQuery twi, Boolean
	 * sent) throws Exception { TWorkitemSet ts = null; ArrayList<TWorkitemInfo>
	 * wiList = new ArrayList<TWorkitemInfo>(); Connection conn = null;
	 * ResultSet rs = null; String sqlQuery = null; PreparedStatement ps = null;
	 * try { String status = twi.repStatus; Date fromDate =
	 * DBUtil.convertStringToDateEx(twi.fromDate); Date toDate =
	 * DBUtil.convertStringToDateEx(twi.toDate); if (status == null) status =
	 * "ACTIVE"; String wiSQL = ""; if (status.equalsIgnoreCase("PENDING")) {
	 * status = "ACTIVE"; wiSQL = "AND A.Status in ('New', 'Read') "; } else if
	 * (status.equalsIgnoreCase("COMPLETED")) { status = "ACTIVE"; wiSQL =
	 * "AND A.Status in ('Forward', 'Reply') "; } else if
	 * (status.equalsIgnoreCase("ARCHIVE")) { } else status = "ACTIVE"; String
	 * orgSQL = ""; if ((twi.orgCode == null) || (twi.orgCode.trim().length() <=
	 * 0)) throw new Exception("Please specify an organization code!"); String
	 * filterSQL = ""; if (sent) { orgSQL =
	 * " AND ((isuserinorgunit(A.senderempno, '" +
	 * DBUtil.escapeString(twi.orgCode) + "') = 'YES') " +
	 * "OR (isroleinorgunit(A.senderroleid, '" +
	 * DBUtil.escapeString(twi.orgCode) + "') = 'YES')) "; filterSQL =
	 * getQueryString(twi, true); } else { orgSQL =
	 * " AND ((isuserinorgunit(A.recipientempno, '" +
	 * DBUtil.escapeString(twi.orgCode) + "') = 'YES') " +
	 * "OR (isroleinorgunit(A.recipientroleid, '" +
	 * DBUtil.escapeString(twi.orgCode) + "') = 'YES')) "; filterSQL =
	 * getQueryString(twi, false); } conn = DBUtil.getECMDBConnection(); ts =
	 * new TWorkitemSet(); ts.workitems = wiList; sqlQuery =
	 * "SELECT B.Priority, B.Subject, A.Status, A.Instructions, A.Type, " +
	 * "A.Deadline, A.ID, A.Reminder, A.CreatedDate, A.Actions, " +
	 * "A.Comments, B.Remarks, " +
	 * "(SELECT FullName FROM ECM_USER WHERE EMPNo = B.CREATEDBY  AND ROWNUM = 1) AS WfCreatorNameEmp, "
	 * +
	 * "(SELECT Name FROM ECM_ROLE WHERE ID = B.ROLEID  AND ROWNUM = 1) AS WfCreatorName, "
	 * +
	 * "(SELECT FullName FROM ECM_USER WHERE EMPNo = A.SenderEMPNo  AND ROWNUM = 1) AS SenderName, "
	 * +
	 * "(SELECT FullName FROM ECM_USER WHERE EMPNo = A.RecipientEMPNo  AND ROWNUM = 1) AS RecipientName, "
	 * +
	 * "(SELECT Name FROM ECM_ROLE WHERE ID = A.SenderRoleID  AND ROWNUM = 1) AS SenderRoleName, "
	 * +
	 * "(SELECT Name FROM ECM_ROLE WHERE ID = A.RecipientRoleID AND ROWNUM = 1) AS RecipientRoleName "
	 * + "FROM ECM_WORKITEM A, ECM_WORKFLOW B " + "WHERE  A.WorkflowID = B.ID "
	 * + "AND A.CREATEDDATE >= ? AND A.CREATEDDATE <= ? " + wiSQL + orgSQL +
	 * "AND A.SystemStatus = '" + status + "' " + filterSQL +
	 * getOrderByString(twi, sent); String pageWhere = "SELECT * FROM " +
	 * "(SELECT X.*, ROWNUM rnum FROM (" + sqlQuery + ") X " +
	 * "WHERE ROWNUM <= " + maxRow + ") " + "WHERE rnum > " + minRow; ps =
	 * conn.prepareStatement(pageWhere); ps.setTimestamp(1, new
	 * Timestamp(fromDate.getTime())); ps.setTimestamp(2, new
	 * Timestamp(toDate.getTime())); rs = ps.executeQuery(); while (rs.next()) {
	 * TWorkitemInfo wi = new TWorkitemInfo(); wi.workitemId = rs.getInt("ID");
	 * wi.priority = rs.getInt("Priority"); wi.subject =
	 * rs.getString("Subject"); wi.status = rs.getString("Status");
	 * wi.instructions = rs.getString("Instructions"); wi.type =
	 * rs.getString("Type"); wi.deadline = DBUtil.convertDateTimeToString(rs
	 * .getTimestamp("Deadline")); wi.reminder =
	 * DBUtil.convertDateTimeToString(rs .getTimestamp("Reminder"));
	 * wi.receivedDate = DBUtil.convertDateTimeToString(rs
	 * .getTimestamp("CreatedDate")); wi.wfCreatorName =
	 * rs.getString("WFCreatorName"); if ((wi.wfCreatorName == null) ||
	 * (wi.wfCreatorName.length() <= 0)) wi.wfCreatorName =
	 * rs.getString("WFCreatorNameEmp"); wi.recipientName =
	 * rs.getString("RecipientName"); if (wi.recipientName == null)
	 * wi.recipientName = rs.getString("RecipientRoleName"); wi.senderName =
	 * rs.getString("SenderName"); wi.actions = rs.getString("Actions"); if
	 * (wi.senderName == null) wi.senderName = rs.getString("SenderRoleName");
	 * wi.comments = rs.getString("Comments"); wi.remarks =
	 * rs.getString("Remarks"); wiList.add(wi); } if (ts != null) {
	 * ts.totalCount = wiList.size(); ts.setCount = wiList.size(); ts.pages = 1;
	 * ts.pageSize = wiList.size(); ts.workitems = wiList; } return ts; } catch
	 * (Exception e) { e.printStackTrace(); throw new Exception(e.getMessage());
	 * } finally { rs.close(); ps.close(); conn.close(); } }
	 */

	public TWorkitemSet searchReportInboxitems(TWorkitemQuery twi, Boolean sent)
			throws Exception {
		TWorkitemSet ts = null;
		String statusCondition = "AND A.Status in ('New', 'Read', 'Finished') ";
		ArrayList<TWorkitemInfo> wiList = new ArrayList<TWorkitemInfo>();
		if ((twi.repStatus == null) || (twi.repStatus.trim().length() <= 0))
			twi.repStatus = "ACTIVE";
		Connection conn = null;
		ResultSet rs = null;
		String sqlQuery = null;
		PreparedStatement ps = null;
		try {
			conn = DBUtil.getECMDBConnection();
			String criteria = twi.repStatus;
			if ((twi.repStatus != null) && (criteria.trim().length() > 0)) {
				twi.repStatus = "ACTIVE";
				if ((criteria.equalsIgnoreCase("Pending")))
					statusCondition = "AND A.Status in ('New' , 'Read') ";
				else if ((criteria.equalsIgnoreCase("Actioned")))
					statusCondition = "AND A.Status in ('Reply' , 'Forward') ";
				else if ((criteria.equalsIgnoreCase("All")))
					statusCondition = "AND A.Status in ('New', 'Read', 'Reply', 'Forward', 'Finished') ";
				else if ((criteria.equalsIgnoreCase("Finished"))) {
					twi.repStatus = "FINISH";
					statusCondition = "AND A.Status = 'Finished' ";
				}
			}
			String orgSQL = " AND ((isuserinorgunit(A.recipientempno, '"
					+ DBUtil.escapeString(twi.orgCode) + "') = 'YES') "
					+ "OR (isroleinorgunit(A.recipientroleid, '"
					+ DBUtil.escapeString(twi.orgCode) + "') = 'YES')) ";

			String countQuery = "SELECT count(A.ID) AS TotalCount FROM ECM_WORKITEM A, "
					+ "ECM_WORKFLOW B "
					+ "WHERE A.WorkflowID = B.ID "
					+ statusCondition
					+ orgSQL
					+ " AND A.CREATEDDATE >= TO_DATE('"
					+ DBUtil.escapeString(twi.fromDate)
					+ "','DD/MM/YYYY') "
					+ " AND A.CREATEDDATE < TO_DATE('"
					+ DBUtil.escapeString(DBUtil.convertDateToShortString(DBUtil.addDaystoGivenDate(DBUtil.convertStringtoDate(twi.toDate), 1),""))
					+ "','DD/MM/YYYY') "
					+ "AND A.SystemStatus = '"
					+ twi.repStatus + "' ";

			ts = getWorkitemSet(conn, twi.empNo, countQuery, twi.pageNo);
			ts.workitems = wiList;
			setPaginationClause(ts);

			sqlQuery = "SELECT B.Priority, B.Subject, A.Status, A.Instructions, A.Type, A.ActionID, "
					+ "A.Deadline, A.ID, A.Reminder, A.CreatedDate, A.Actions, "
					+ "A.Comments, "
					+ "(SELECT FullName FROM ECM_USER WHERE EMPNo = B.CREATEDBY  AND ROWNUM = 1) AS WfCreatorNameEmp, "
					+ "(SELECT Name FROM ECM_ROLE WHERE ID = B.ROLEID  AND ROWNUM = 1) AS WfCreatorName, "
					+ "(SELECT FullName FROM ECM_USER WHERE EMPNo = A.SenderEMPNo  AND ROWNUM = 1) AS SenderName, "
					+ "(SELECT FullName FROM ECM_USER WHERE EMPNo = A.RecipientEMPNo  AND ROWNUM = 1) AS RecipientName, "
					+ "(SELECT Name FROM ECM_ROLE WHERE ID = A.SenderRoleID  AND ROWNUM = 1) AS SenderRoleName, "
					+ "(SELECT COUNT(*) FROM ECM_WORKITEM WHERE WorkflowID = A.WorkflowID AND RecipientEMPNo = A.RecipientEMPNo AND SystemStatus != 'RECALL') AS RecvCount "
					+ "FROM ECM_WORKITEM A, ECM_WORKFLOW B "
					+ "WHERE A.WorkflowID = B.ID "
					+ statusCondition
					+ orgSQL
					+ " AND A.CREATEDDATE >= TO_DATE('"
					+ DBUtil.escapeString(twi.fromDate)
					+ "','DD/MM/YYYY') "
					+ " AND A.CREATEDDATE < TO_DATE('"
					+ DBUtil.escapeString(DBUtil.convertDateToShortString(DBUtil.addDaystoGivenDate(DBUtil.convertStringtoDate(twi.toDate), 1),""))
					+ "','DD/MM/YYYY') "
					+ "AND A.SystemStatus = '"
					+ twi.repStatus
					+ "' "
					+ getOrderByString(twi, false);

			String pageWhere = "SELECT * FROM "
					+ "(SELECT X.*, ROWNUM rnum FROM (" + sqlQuery + ") X "
					+ "WHERE ROWNUM <= " + maxRow + ") " + "WHERE rnum > "
					+ minRow;
			ps = conn.prepareStatement(pageWhere);
			rs = ps.executeQuery();
			while (rs.next()) {
				TWorkitemInfo wi = new TWorkitemInfo();
				wi.workitemId = rs.getInt("ID");
				wi.priority = rs.getInt("Priority");
				wi.subject = rs.getString("Subject");
				wi.status = rs.getString("Status");
				wi.instructions = rs.getString("Instructions");
				wi.type = rs.getString("Type");
				wi.actionId = rs.getInt("ActionID");
				wi.deadline = DBUtil.convertDateTimeToString(rs
						.getTimestamp("Deadline"));
				wi.reminder = DBUtil.convertDateTimeToString(rs
						.getTimestamp("Reminder"));
				wi.receivedDate = DBUtil.convertDateTimeToString(rs
						.getTimestamp("CreatedDate"));
				wi.wfCreatorName = rs.getString("WFCreatorName");
				if ((wi.wfCreatorName == null)
						|| (wi.wfCreatorName.length() <= 0))
					wi.wfCreatorName = rs.getString("WFCreatorNameEmp");
				wi.recipientName = rs.getString("RecipientName");
				wi.senderName = rs.getString("SenderName");
				wi.actions = rs.getString("Actions");
				if (wi.senderName == null)
					wi.senderName = rs.getString("SenderRoleName");
				wi.comments = rs.getString("Comments");
				wi.receiveCount = rs.getInt("RecvCount");
				wiList.add(wi);
			}
			if (ts != null) {
				ts.workitems = wiList;
				if (wiList != null)
					ts.setCount = wiList.size();
			}
			return ts;
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			rs.close();
			ps.close();
			conn.close();
		}
	}

	public TWorkitemSet searchReportSentitems(TWorkitemQuery twi, Boolean sent)
			throws Exception {
		TWorkitemSet ts = null;
		String strQueryWhere = " ";
		ArrayList<TWorkitemInfo> wiList = new ArrayList<TWorkitemInfo>();
		if ((twi.repStatus == null) || (twi.repStatus.trim().length() <= 0))
			twi.repStatus = "ACTIVE";

		String criteria = twi.repStatus;
		if ((criteria != null) && (criteria.length() > 0)) {
			twi.repStatus = "ACTIVE";
			if (criteria.equalsIgnoreCase("Pending")) {
				strQueryWhere = "AND A.ID IN (SELECT DISTINCT X.PARENTSENTITEMID FROM ECM_WORKITEM X WHERE X.WORKFLOWID = B.ID AND X.Status in ('New', 'Read')) ";
			} else if (criteria.equalsIgnoreCase("Actioned")) {
				strQueryWhere = "AND A.ID IN (SELECT DISTINCT X.PARENTSENTITEMID FROM ECM_WORKITEM X WHERE X.WORKFLOWID = B.ID AND X.Status in ('Reply', 'Forward')) ";
			} else if (criteria.equalsIgnoreCase("All")) {
				strQueryWhere = "AND A.ID IN (SELECT DISTINCT X.PARENTSENTITEMID FROM ECM_WORKITEM X WHERE X.WORKFLOWID = B.ID AND X.Status in ('New', 'Read', 'Reply', 'Forward', 'Archived')) ";
			} else if (criteria.equalsIgnoreCase("Archived")) {
				twi.repStatus = "ARCHIVE";
			}
		}
		String orgSQL = " AND ((isuserinorgunit(A.senderempno, '"
				+ DBUtil.escapeString(twi.orgCode) + "') = 'YES') "
				+ "OR (isroleinorgunit(A.senderroleid, '"
				+ DBUtil.escapeString(twi.orgCode) + "') = 'YES')) ";

		Connection conn = null;
		ResultSet rs = null;
		String sqlQuery = null;
		PreparedStatement ps = null;
		try {
			conn = DBUtil.getECMDBConnection();
			String countQuery = "SELECT Count(A.ID) AS TotalCount FROM ECM_WORKITEM_SENT A, "
					+ "ECM_WORKFLOW B "
					+ "WHERE A.WorkflowID = B.ID "
					+ "AND A.CREATEDDATE >= TO_DATE('"
					+ DBUtil.escapeString(twi.fromDate)
					+ "','DD/MM/YYYY') "
					+ " AND A.CREATEDDATE < TO_DATE('"
					+ DBUtil.escapeString(DBUtil.convertDateToShortString(DBUtil.addDaystoGivenDate(DBUtil.convertStringtoDate(twi.toDate), 1),""))
					+ "','DD/MM/YYYY') "
					+ orgSQL
					+ strQueryWhere
					+ " AND A.Status = '"
					+ twi.repStatus + "' ";

			ts = getWorkitemSet(conn, twi.empNo, countQuery, twi.pageNo);
			ts.workitems = wiList;
			setPaginationClause(ts);
			sqlQuery = "SELECT A.Priority, B.Subject, A.Status, B.ECMNo, A.Remarks, A.ID, A.CreatedDate, A.WorkflowID, "
					+ "(SELECT Name FROM ECM_ROLE WHERE ID = B.RoleID  AND ROWNUM = 1) AS WfCreatorName, "
					+ "(SELECT FullName FROM ECM_USER WHERE EMPNo = B.CREATEDBY  AND ROWNUM = 1) AS WfCreatorNameEmp, "
					+ "(SELECT FullName FROM ECM_USER WHERE EMPNo = A.SenderEMPNo  AND ROWNUM = 1) AS SenderName, "
					+ "(SELECT Name FROM ECM_ROLE WHERE ID = A.SenderRoleID  AND ROWNUM = 1) AS SenderRoleName "
					+ "FROM ECM_WORKITEM_SENT A, ECM_WORKFLOW B "
					+ "WHERE A.WorkflowID = B.ID "
					+ "AND A.CREATEDDATE >= TO_DATE('"
					+ DBUtil.escapeString(twi.fromDate)
					+ "','DD/MM/YYYY') "
					+ " AND A.CREATEDDATE < TO_DATE('"
					+ DBUtil.escapeString(DBUtil.convertDateToShortString(DBUtil.addDaystoGivenDate(DBUtil.convertStringtoDate(twi.toDate), 1),""))
					+ "','DD/MM/YYYY') "
					+ orgSQL
					+ strQueryWhere
					+ " AND A.Status = '"
					+ twi.repStatus
					+ "' "
					+ getOrderByString(twi, true);

			String pageWhere = "SELECT * FROM "
					+ "(SELECT X.*, ROWNUM rnum FROM (" + sqlQuery + ") X "
					+ "WHERE ROWNUM <= " + maxRow + ") " + "WHERE rnum > "
					+ minRow;
			ps = conn.prepareStatement(pageWhere);
			rs = ps.executeQuery();
			while (rs.next()) {
				TWorkitemInfo wi = new TWorkitemInfo();
				wi.workitemId = rs.getInt("ID");
				wi.priority = rs.getInt("Priority");
				wi.subject = rs.getString("Subject");
				wi.status = rs.getString("Status");
				wi.receivedDate = DBUtil.convertDateTimeToString(rs
						.getTimestamp("CreatedDate"));
				wi.wfCreatorName = rs.getString("WFCreatorName");
				if ((wi.wfCreatorName == null)
						|| (wi.wfCreatorName.length() <= 0))
					wi.wfCreatorName = rs.getString("WFCreatorNameEmp");
				wi.senderName = rs.getString("SenderName");
				if ((wi.senderName == null) || (wi.senderName.length() <= 0))
					wi.senderName = rs.getString("SenderRoleName");
				wi.remarks = rs.getString("Remarks");
				wi.senderId = twi.userId;
				wiList.add(wi);
			}
			if (ts != null) {
				ts.workitems = wiList;
				if (wiList != null)
					ts.setCount = wiList.size();
			}
			return ts;
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			rs.close();
			ps.close();
			conn.close();
		}

	}

	public ArrayList<ECMInboxItems> searchExportReportInboxWorkitems(
			TWorkitemQuery twi) throws Exception {
		String statusCondition = "AND A.Status in ('New', 'Read', 'Finished') ";
		ArrayList<ECMInboxItems> wiList = new ArrayList<ECMInboxItems>();
		String criteria = "";
		Connection conn = null;
		ResultSet rs = null;
		String sqlQuery = null;
		PreparedStatement ps = null;
		try {
			conn = DBUtil.getECMDBConnection();
			if ((twi.repStatus == null) || (twi.repStatus.trim().length() <= 0)) {
				criteria = "ACTIVE";
			} else if ((twi.repStatus != null) && (twi.repStatus.trim().length() > 0)) {
				criteria = "ACTIVE";
				if ((twi.repStatus.equalsIgnoreCase("Pending")))
					statusCondition = "AND A.Status in ('New' , 'Read') ";
				else if ((twi.repStatus.equalsIgnoreCase("Actioned")))
					statusCondition = "AND A.Status in ('Reply' , 'Forward') ";
				else if ((twi.repStatus.equalsIgnoreCase("All")))
					statusCondition = "AND A.Status in ('New', 'Read', 'Reply', 'Forward', 'Finished') ";
				else if ((twi.repStatus.equalsIgnoreCase("Finished"))) {
					criteria = "FINISH";
					statusCondition = "AND A.Status = 'Finished' ";
				}
			}
			String orgSQL = " AND ((isuserinorgunit(A.recipientempno, '"
					+ DBUtil.escapeString(twi.orgCode) + "') = 'YES') "
					+ "OR (isroleinorgunit(A.recipientroleid, '"
					+ DBUtil.escapeString(twi.orgCode) + "') = 'YES')) ";

			sqlQuery = "SELECT B.Priority, B.Subject, A.Status, A.Instructions, A.Type, A.ActionID, "
					+ "A.Deadline, A.ID, A.Reminder, A.CreatedDate, A.Actions, A.Comments, B.Remarks, B.EcmNo, "
					+ "(SELECT FullName FROM ECM_USER WHERE EMPNo = B.CREATEDBY  AND ROWNUM = 1) AS WfCreatorNameEmp, "
					+ "(SELECT Name FROM ECM_ROLE WHERE ID = B.ROLEID  AND ROWNUM = 1) AS WfCreatorName, "
					+ "(SELECT FullName FROM ECM_USER WHERE EMPNo = A.SenderEMPNo  AND ROWNUM = 1) AS SenderName, "
					+ "(SELECT FullName FROM ECM_USER WHERE EMPNo = A.RecipientEMPNo  AND ROWNUM = 1) AS RecipientName, "
					+ "(SELECT Name FROM ECM_ROLE WHERE ID = A.SenderRoleID  AND ROWNUM = 1) AS SenderRoleName, "
					+ "(SELECT COUNT(*) FROM ECM_WORKITEM WHERE WorkflowID = A.WorkflowID AND RecipientEMPNo = A.RecipientEMPNo AND SystemStatus != 'RECALL') AS RecvCount "
					+ "FROM ECM_WORKITEM A, ECM_WORKFLOW B "
					+ "WHERE A.WorkflowID = B.ID "
					+ statusCondition
					+ orgSQL
					+ " AND A.CREATEDDATE >= TO_DATE('"
					+ DBUtil.escapeString(twi.fromDate)
					+ "','DD/MM/YYYY') "
					+ " AND A.CREATEDDATE < TO_DATE('"
					+ DBUtil.escapeString(DBUtil.convertDateToShortString(DBUtil.addDaystoGivenDate(DBUtil.convertStringtoDate(twi.toDate), 1),""))
					+ "','DD/MM/YYYY') "
					+ "AND A.SystemStatus = '"
					+ criteria
					+ "' ";

			ps = conn.prepareStatement(sqlQuery);
			rs = ps.executeQuery();
			while (rs.next()) {
				ECMInboxItems wi = new ECMInboxItems();
				wi.setWorkitemId(rs.getInt("ID"));
				wi.setPriority(DBUtil.getPriorityValue(rs.getInt("Priority")));
				wi.setSubject(rs.getString("Subject"));
				wi.setStatus(rs.getString("Status"));
				wi.setInstructions(rs.getString("Instructions"));
				wi.setType(rs.getString("Type"));
				wi.setActionId(rs.getInt("ActionID"));
				wi.setDeadline(DBUtil.convertDateTimeToString(rs
						.getTimestamp("Deadline")));
				wi.setReminder(DBUtil.convertDateTimeToString(rs
						.getTimestamp("Reminder")));
				wi.setReceivedDate(DBUtil.convertDateTimeToString(rs
						.getTimestamp("CreatedDate")));
				wi.setWfCreatorName(rs.getString("WFCreatorName"));
				if ((wi.getWfCreatorName() == null)
						|| (wi.getWfCreatorName().length() <= 0))
					wi.setWfCreatorName(rs.getString("WFCreatorNameEmp"));
				wi.setRecipientName(rs.getString("RecipientName"));
				wi.setSenderName(rs.getString("SenderName"));
				wi.setActions(rs.getString("Actions"));
				if (wi.getSenderName() == null)
					wi.setSenderName(rs.getString("SenderRoleName"));
				wi.setComments(rs.getString("Comments"));
				wi.setRemarks(rs.getString("Remarks"));
				wi.setEcmNo(rs.getString("EcmNo"));
				wi.setReceiveCount(rs.getInt("RecvCount"));
				wiList.add(wi);
			}
			return wiList;
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			rs.close();
			ps.close();
			conn.close();
		}
	}

	public ArrayList<ECMSentitems> searchExportReportSentWorkitems(
			TWorkitemQuery twi) throws Exception {
		String strQueryWhere = " ";
		ArrayList<ECMSentitems> wiList = new ArrayList<ECMSentitems>();
		String criteria = "";
		if ((twi.repStatus == null) || (twi.repStatus.trim().length() <= 0)) {
			criteria = "ACTIVE";
		} else if ((twi.repStatus != null) && (twi.repStatus.length() > 0)) {
			criteria = "ACTIVE";
			if (twi.repStatus.equalsIgnoreCase("Pending")) {
				strQueryWhere = "AND A.ID IN (SELECT DISTINCT X.PARENTSENTITEMID FROM ECM_WORKITEM X WHERE X.WORKFLOWID = B.ID AND X.Status in ('New', 'Read')) ";
			} else if (twi.repStatus.equalsIgnoreCase("Actioned")) {
				strQueryWhere = "AND A.ID IN (SELECT DISTINCT X.PARENTSENTITEMID FROM ECM_WORKITEM X WHERE X.WORKFLOWID = B.ID AND X.Status in ('Reply', 'Forward')) ";
			} else if (twi.repStatus.equalsIgnoreCase("All")) {
				strQueryWhere = "AND A.ID IN (SELECT DISTINCT X.PARENTSENTITEMID FROM ECM_WORKITEM X WHERE X.WORKFLOWID = B.ID AND X.Status in ('New', 'Read', 'Reply', 'Forward', 'Archived')) ";
			} else if (twi.repStatus.equalsIgnoreCase("Archived")) {
				criteria = "ARCHIVE";
			}
		}
		String orgSQL = " AND ((isuserinorgunit(A.senderempno, '"
				+ DBUtil.escapeString(twi.orgCode) + "') = 'YES') "
				+ "OR (isroleinorgunit(A.senderroleid, '"
				+ DBUtil.escapeString(twi.orgCode) + "') = 'YES')) ";

		Connection conn = null;
		ResultSet rs = null;
		String sqlQuery = null;
		PreparedStatement ps = null;
		try {
			conn = DBUtil.getECMDBConnection();
			sqlQuery = "SELECT A.Priority, B.Subject, A.Status, B.ECMNo, A.Remarks, A.ID, A.CreatedDate, A.WorkflowID, "
					+ "(SELECT Name FROM ECM_ROLE WHERE ID = B.RoleID  AND ROWNUM = 1) AS WfCreatorName, "
					+ "(SELECT FullName FROM ECM_USER WHERE EMPNo = B.CREATEDBY  AND ROWNUM = 1) AS WfCreatorNameEmp, "
					+ "(SELECT FullName FROM ECM_USER WHERE EMPNo = A.SenderEMPNo  AND ROWNUM = 1) AS SenderName, "
					+ "(SELECT Name FROM ECM_ROLE WHERE ID = A.SenderRoleID  AND ROWNUM = 1) AS SenderRoleName "
					+ "FROM ECM_WORKITEM_SENT A, ECM_WORKFLOW B "
					+ "WHERE A.WorkflowID = B.ID "
					+ "AND A.CREATEDDATE >= TO_DATE('"
					+ DBUtil.escapeString(twi.fromDate)
					+ "','DD/MM/YYYY') "
					+ " AND A.CREATEDDATE <TO_DATE('"
					+ DBUtil.escapeString(DBUtil.convertDateToShortString(DBUtil.addDaystoGivenDate(DBUtil.convertStringtoDate(twi.toDate), 1),""))
					+ "','DD/MM/YYYY') "
					+ orgSQL
					+ strQueryWhere
					+ " AND A.Status = '"
					+ criteria
					+ "' "
					+ getOrderByString(twi, true);

			ps = conn.prepareStatement(sqlQuery);
			rs = ps.executeQuery();
			while (rs.next()) {
				ECMSentitems wi = new ECMSentitems();
				wi.setWorkitemId(rs.getInt("ID"));
				wi.setPriority(DBUtil.getPriorityValue(rs.getInt("Priority")));
				wi.setSubject(rs.getString("Subject"));
				wi.setStatus(rs.getString("Status"));
				wi.setReceivedDate(DBUtil.convertDateTimeToString(rs
						.getTimestamp("CreatedDate")));
				wi.setWfCreatorName(rs.getString("WFCreatorName"));
				if ((wi.getWfCreatorName() == null)
						|| (wi.getWfCreatorName().length() <= 0))
					wi.setWfCreatorName(rs.getString("WFCreatorNameEmp"));
				wi.setSenderName(rs.getString("SenderName"));
				if ((wi.getSenderName() == null)
						|| (wi.getSenderName().length() <= 0))
					wi.setSenderName(rs.getString("SenderRoleName"));
				wi.setEcmNo(rs.getString("ECMNo"));
				wi.setRemarks(rs.getString("Remarks"));
				wi.setSenderId(twi.userId);
				wi.setSentitemId(rs.getInt("WorkflowID"));
				wi.setTrackSentitem(getSentItemTrackHistory(conn,
						wi.getSentitemId(), twi.userId));
				wiList.add(wi);
			}
			return wiList;
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			rs.close();
			ps.close();
			conn.close();
		}
	}

	public void exportOrgReportCounts(HttpServletRequest req,
			HttpServletResponse resp, TReportFilter trf, String ReportTitle,
			String exportType, ArrayList<TOrgUnit> toList) throws Exception {
		try {
			JasperHelper.generateOrgJasperReportCount(resp, req, ReportTitle,
					getOrgReportCountBookData(toList), trf);
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		}
	}

	private ArrayList<ECMOrgReport> getOrgReportCountBookData(
			ArrayList<TOrgUnit> toList) {
		ArrayList<ECMOrgReport> xlInput = new ArrayList<ECMOrgReport>();
		if (toList == null)
			return xlInput;
		for (TOrgUnit tOrgUnit : toList) {
			ECMOrgReport row = new ECMOrgReport();
			row.setOrgCode(tOrgUnit.orgCode);
			row.setDesc(tOrgUnit.desc);
			row.setCount(tOrgUnit.count);
			xlInput.add(row);
		}
		return xlInput;
	}

	public void exportReportInboxItems(HttpServletRequest req,
			HttpServletResponse resp, TWorkitemQuery twq) throws Exception {
		try {
			ArrayList<ECMInboxItems> iw = ECMWorkitemList.getInstance()
					.searchExportReportInboxWorkitems(twq);
			JasperHelper.generateOrgInboxJasperReport(resp, req,
					"Inbox Workitem", iw, twq);
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		}
	}

	public void exportReportSentItems(HttpServletRequest req,
			HttpServletResponse resp, TWorkitemQuery twq) throws Exception {
		try {
			ArrayList<ECMSentitems> si = ECMWorkitemList.getInstance()
					.searchExportReportSentWorkitems(twq);
			JasperHelper.generateOrgSentJasperReport(resp, req,
					"Sent Item", si, twq, true);
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		}
	}
	
	public void exportOrgAllReportCounts(HttpServletRequest req,
			HttpServletResponse resp, TReportFilter trf, String ReportTitle,
			String exportType, ArrayList<TMgmtReport> tmrList) throws Exception {
		try {
			JasperHelper.generateOrgAllJasperReportCount(resp, req, ReportTitle,
					getOrgAllReportCountBookData(tmrList), trf);
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		}
	}

	public ArrayList<ECMOrgReport> getOrgAllReportCountBookData(ArrayList<TMgmtReport> tmrList){
		ArrayList<ECMOrgReport> xlInput = new ArrayList<ECMOrgReport>();
		if (tmrList == null)
			return xlInput;
		for (TMgmtReport tMgmtRpt : tmrList) {
			int i = 0;
			for (TOrgUnit tOrgUnit : tMgmtRpt.details) {
				ECMOrgReport row = new ECMOrgReport();
				if(i>0)
					row.setCategory(" ");
				else
					row.setCategory(tMgmtRpt.category);
				
				row.setOrgCode(tOrgUnit.orgCode);
				row.setDesc(tOrgUnit.desc);
				row.setCount(tOrgUnit.count);
				xlInput.add(row);
				i++;
			}
		}
		return xlInput;
	}
	
	public void exportOrgDocumentCounts(HttpServletRequest req,
			HttpServletResponse resp, TReportFilter trf, String exportType,
			String exportOrgDocCounts, ArrayList<TOrgUnit> toList)
			throws Exception {
		try {
			if (exportType.equalsIgnoreCase("Excel")) {
				ExcelHelper.createExcelFileForReports(req, resp, "Inbox Items",
						exportOrgDocumentCountsReport(toList), trf,
						exportOrgDocCounts);
			} else if (exportType.equalsIgnoreCase("pdf")) {
				PdfHelper.createPdfFile(resp, req, "Inbox Items",
						exportOrgDocumentCountsReport(toList), trf, false);
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		}
	}

	private ArrayList<ArrayList<Object>> exportOrgDocumentCountsReport(
			ArrayList<TOrgUnit> toList) {
		ArrayList<ArrayList<Object>> xlInput = new ArrayList<ArrayList<Object>>();
		if (toList == null)
			return xlInput;
		xlInput.add(getOrgDocumentCountsHeaderRow());
		for (TOrgUnit tou : toList) {
			ArrayList<Object> row = new ArrayList<Object>();
			row.add(tou.orgCode);
			row.add(tou.count);
			xlInput.add(row);
		}
		return xlInput;
	}

	private ArrayList<Object> getOrgDocumentCountsHeaderRow() {
		ArrayList<Object> row = new ArrayList<Object>();
		row.add("Org Code");
		row.add("Document Count");
		return row;
	}

	public void exportOrgESignItems(HttpServletRequest req,
			HttpServletResponse resp, String exportType, TReportFilter trf,
			String ReportTitle, ArrayList<TDocSignItem> toList)
			throws Exception {
		try {
			JasperHelper.generateOrgJasperReportCount(resp, req, ReportTitle,
					exportOrgESignItemsReportBookData(toList), trf);
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		}
	}

	private ArrayList<ECMOrgReport> exportOrgESignItemsReportBookData(
			ArrayList<TDocSignItem> toList) {
		ArrayList<ECMOrgReport> xlInput = new ArrayList<ECMOrgReport>();
		if (toList == null)
			return xlInput;
		for (TDocSignItem tdsi : toList) {
			ECMOrgReport row = new ECMOrgReport();
			row.setEmpName(tdsi.empName);
			row.setDocTitle(tdsi.docTitle);
			row.setEmpNo(tdsi.empNo);
			row.setOrgCode(tdsi.orgCode);
			row.setSignDate(tdsi.signDate);
			row.setReqDate(tdsi.reqDate);
			row.setStatus(tdsi.status);
			row.setType(tdsi.type);
			xlInput.add(row);
		}
		return xlInput;
	}

	public ArrayList<TRecipient> getInboxFilterUsers(long userId,
			String userType, String status) throws Exception {
		if ((status == null)
				|| !(status.equalsIgnoreCase("ACTIVE") || status
						.equalsIgnoreCase("FINISH")))
			status = "ACTIVE";
		if (userType == null)
			userType = "USER";
		String whereSQL = "A.RECIPIENTEMPNO = ? ";
		if (userType.trim().equalsIgnoreCase("ROLE"))
			whereSQL = "A.RECIPIENTROLEID = ? ";
		Connection conn = null;
		ResultSet rs = null;
		String sqlQuery = null;
		PreparedStatement ps = null;
		ArrayList<TRecipient> recipientList = new ArrayList<TRecipient>();
		try {
			conn = DBUtil.getECMDBConnection();
			sqlQuery = "SELECT TYPE, SENDEREMPNO, SENDERROLEID, "
					+ "(SELECT FULLNAME FROM ECM_USER WHERE EMPNO = A.SENDEREMPNO AND ROWNUM=1) AS SENDEREMPNAME, "
					+ "(SELECT NAME FROM ECM_ROLE WHERE ID = A.SENDERROLEID AND ROWNUM=1) AS SENDERROLENAME "
					+ "FROM ECM_WORKITEM A WHERE "
					+ whereSQL
					+ "AND A.SYSTEMSTATUS = ? ORDER BY SENDEREMPNAME ASC, SENDERROLENAME ASC";
			ps = conn.prepareStatement(sqlQuery);
			ps.setLong(1, userId);
			ps.setString(2, DBUtil.escapeString(status));
			HashMap<String, TRecipient> hm = new HashMap<String, TRecipient>();
			rs = ps.executeQuery();
			while (rs.next()) {
				TRecipient tr = new TRecipient();
				tr.actionType = rs.getString("TYPE");
				tr.id = rs.getInt("SENDEREMPNO");
				if (tr.id > 0) {
					tr.userType = "USER";
					tr.name = rs.getString("SENDEREMPNAME");
				} else {
					tr.id = rs.getInt("SENDERROLEID");
					tr.name = rs.getString("SENDERROLENAME");
					tr.userType = "ROLE";
				}
				String hmKey = tr.userType + "#" + tr.id;
				if (!hm.containsKey(hmKey)) {
					recipientList.add(tr);
					hm.put(hmKey, tr);
				}
			}
			hm.clear();
			return recipientList;
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			rs.close();
			ps.close();
			conn.close();
		}
	}

	public ArrayList<TRecipient> getSentitemFilterUsers(long userId,
			String userType, String status) throws Exception {
		if ((status == null)
				|| !(status.equalsIgnoreCase("ACTIVE") || status
						.equalsIgnoreCase("ARCHIVE")))
			status = "ACTIVE";
		if (userType == null)
			userType = "USER";
		String whereSQL = "A.SENDEREMPNO = ? ";
		if (userType.trim().equalsIgnoreCase("ROLE"))
			whereSQL = "A.SENDERROLEID = ? ";
		Connection conn = null;
		ResultSet rs = null;
		String sqlQuery = null;
		PreparedStatement ps = null;
		ArrayList<TRecipient> recipientList = new ArrayList<TRecipient>();
		try {
			conn = DBUtil.getECMDBConnection();
			sqlQuery = "SELECT TYPE, RECIPIENTEMPNO, RECIPIENTROLEID, "
					+ "(SELECT FULLNAME FROM ECM_USER WHERE EMPNO = A.RECIPIENTEMPNO AND ROWNUM=1) AS RECIPIENTEMPNAME, "
					+ "(SELECT NAME FROM ECM_ROLE WHERE ID = A.RECIPIENTROLEID AND ROWNUM=1) AS RECIPIENTROLENAME "
					+ "FROM ECM_WORKITEM A WHERE "
					+ whereSQL
					+ "AND A.SYSTEMSTATUS = ? ORDER BY  RECIPIENTEMPNAME ASC, RECIPIENTROLENAME ASC";
			ps = conn.prepareStatement(sqlQuery);
			ps.setLong(1, userId);
			ps.setString(2, DBUtil.escapeString(status));
			HashMap<String, TRecipient> hm = new HashMap<String, TRecipient>();
			rs = ps.executeQuery();
			while (rs.next()) {
				TRecipient tr = new TRecipient();
				tr.actionType = rs.getString("TYPE");
				tr.id = rs.getInt("RECIPIENTEMPNO");
				if (tr.id > 0) {
					tr.userType = "USER";
					tr.name = rs.getString("RECIPIENTEMPNAME");
				} else {
					tr.id = rs.getInt("RECIPIENTROLEID");
					tr.name = rs.getString("RECIPIENTROLENAME");
					tr.userType = "ROLE";
				}
				String hmKey = tr.userType + "#" + tr.id;
				if (!hm.containsKey(hmKey)) {
					recipientList.add(tr);
					hm.put(hmKey, tr);
				}
			}
			hm.clear();
			return recipientList;
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			rs.close();
			ps.close();
			conn.close();
		}
	}

	public long getWorkItemWorkflowID(long witemId) throws Exception {
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		long wId = 0;

		try {

			conn = DBUtil.getECMDBConnection();

			ps = conn.prepareStatement("SELECT WORKFLOWID FROM ECM_WORKITEM "
					+ "WHERE ID = ?");
			ps.setInt(1, (int) witemId);
			rs = ps.executeQuery();
			if ((rs != null) && rs.next()) {
				wId = rs.getInt("WORKFLOWID");
			}

			return wId;
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			rs.close();
			ps.close();
			conn.close();
		}
	}
}
