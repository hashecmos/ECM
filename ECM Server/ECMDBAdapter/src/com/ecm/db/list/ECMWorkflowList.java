package com.ecm.db.list;

import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import oracle.jdbc.OracleTypes;

import com.ecm.db.model.ECMSentItem;
import com.ecm.db.model.ECMWorkItem;
import com.ecm.db.model.ECMWorkflow;
import com.ecm.db.transport.TDocPrincipal;
import com.ecm.db.transport.TDocSignItem;
import com.ecm.db.transport.TDocWorkflowDetails;
import com.ecm.db.transport.TOrgUnit;
import com.ecm.db.transport.TRecallAction;
import com.ecm.db.transport.TRecipient;
import com.ecm.db.transport.TWorkflowHistory;
import com.ecm.db.transport.TWorkitemAction;
import com.ecm.db.transport.TWorkitemDetails;
import com.ecm.db.transport.TWorkitemInfo;
import com.ecm.db.transport.TWorkitemQuery;
import com.ecm.db.transport.TWorkitemSet;
import com.ecm.db.transport.TWorkitemStat;
import com.ecm.db.util.DBUtil;
import com.ecm.db.util.ExcelHelper;
import com.ecm.db.util.PdfHelper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ECMWorkflowList {
	
	private ECMWorkflowList() { }

	public static ECMWorkflowList getInstance() {
		return new ECMWorkflowList();
	}

	public ArrayList<TDocWorkflowDetails> getDocumentWorkflowHistory(String docId) throws Exception {
		ArrayList<TDocWorkflowDetails> dwdList = new ArrayList<TDocWorkflowDetails>();
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			conn = DBUtil.getECMDBConnection();

			ps = conn.prepareStatement("select distinct A.WORKFLOWID, B.SUBJECT, "
					+ "(SELECT FullName from ECM_USER where empno = B.CREATEDBY AND ROWNUM =1) AS CREATOR, B.CREATEDDATE,"
					+ "(SELECT MAX(DEADLINE) FROM ECM_WORKITEM WHERE WORKFLOWID = A.WORKFLOWID) AS DEADLINE "
					+ "FROM ECM_WORKITEM_ATTACHMENT A, ECM_WORKFLOW B "
					+ "WHERE A.documentid = ? AND A.WORKFLOWID = B.ID");
			ps.setString(1, DBUtil.escapeString(docId));
			rs = ps.executeQuery();
			if((rs != null) && rs.next()) {
				TDocWorkflowDetails dwd = new TDocWorkflowDetails();
				dwd.subject = rs.getString("SUBJECT");
				dwd.creator = rs.getString("CREATOR");
				dwd.createdDate = rs.getTimestamp("CREATEDDATE");
				dwd.deadlineDate = rs.getTimestamp("DEADLINE");
				dwdList.add(dwd);
			}
			
			return dwdList;
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			rs.close();
			ps.close();
			conn.close();
		}
	}
	
	
	public long getFirstWorkItembyWorkflow(long wfId) throws Exception {
		long witemId = 0;
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			conn = DBUtil.getECMDBConnection();

			ps = conn.prepareStatement("SELECT ID FROM ECM_WORKITEM WHERE WORKFLOWID = ? AND ROWNUM = 1");
			ps.setInt(1, (int)wfId);
			rs = ps.executeQuery();
			if((rs != null) && rs.next()) {
				witemId = rs.getInt("ID");
			}
			
			return witemId;
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
