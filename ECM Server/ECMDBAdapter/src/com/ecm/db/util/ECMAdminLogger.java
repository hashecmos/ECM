package com.ecm.db.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import com.ecm.db.transport.TAdminLog;

public class ECMAdminLogger {
	private String lContext = "None";

	private ECMAdminLogger(Object className) {
		lContext = className.toString();
		if (lContext == null)
			lContext = "";
	}

	public static ECMAdminLogger getInstance(Object className) {
		return new ECMAdminLogger(className);
	}

	public void info(String appModule, String empName, String actionDetails) {
		saveLog(appModule, empName, actionDetails);
	}

	private void saveLog(String appModule, String empName, String actionDetails) {
		try {
			Connection conn = null;
			PreparedStatement ps = null;
			try {
				conn = DBUtil.getECMDBConnection();
				String sqlQuery = "INSERT INTO ECM_ADMIN_LOG "
						+ "(ID, TYPE, LOGDATE, USERNAME, DETAILS) "
						+ "VALUES (ECM_ADMIN_LOG_SEQ.NEXTVAL, ?, SYSDATE, ?, ?)";
				ps = conn.prepareStatement(sqlQuery);

				if (actionDetails == null)
					actionDetails = "";
				ps.setString(1, DBUtil.escapeString(appModule));
				ps.setString(2, DBUtil.escapeString(empName));
				ps.setString(3, DBUtil.escapeString(actionDetails));

				ps.executeUpdate();

			} catch (Exception e) {
				e.printStackTrace();
				throw new Exception(e.getMessage());
			} finally {
				ps.close();
				conn.close();
			}
		} catch (Exception ex) {
		}
	}

	public ArrayList<TAdminLog> getLogs() throws Exception {
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		ArrayList<TAdminLog> logList = new ArrayList<TAdminLog>();
		try {
			conn = DBUtil.getECMDBConnection();
			String sqlQuery = "SELECT ID, TYPE, USERNAME, LOGDATE, DETAILS FROM ECM_ADMIN_LOG ORDER BY LOGDATE DESC";
			ps = conn.prepareStatement(sqlQuery);

			rs = ps.executeQuery();
			while (rs.next()) {
				TAdminLog log = new TAdminLog();
				log.id = rs.getInt(1);
				log.type = rs.getString(2);
				log.username = rs.getString(3);
				log.timeStamp = DBUtil.formatDateForLog(rs.getTimestamp(4));
				log.details = rs.getString(5);

				logList.add(log);
			}
			return logList;
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
