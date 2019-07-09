package com.ecm.db.list;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

import com.ecm.db.model.ECMNews;
import com.ecm.db.transport.TNews;
import com.ecm.db.util.DBUtil;

public class ECMNewsList {

	private ECMNewsList() {	}

	public static ECMNewsList getInstance() {
		return new ECMNewsList();
	}

	public ArrayList<TNews> getActiveNews() throws Exception {

		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;

		ArrayList<TNews> allNewsList = new ArrayList<TNews>();
		try {
			conn = DBUtil.getECMDBConnection();
			stmt = conn.prepareStatement("SELECT * from ECM_NEWS where "
					+ "trunc(ExpiryDate) >= TO_DATE('" + DBUtil.escapeString(DBUtil.getTodayDate()) + "','dd/MM/yyyy') "
					+ "ORDER BY ActiveDate DESC");
			
			
					
			rs = stmt.executeQuery();

			while (rs.next()) {

				ECMNews news = new ECMNews();
				news.setActiveDate(rs.getTimestamp("ActiveDate"));
				news.setExpiryDate(rs.getTimestamp("ExpiryDate"));
				news.setId(rs.getInt("ID"));
				news.setMessage(rs.getString("Message"));
				news.setSubject(rs.getString("Subject"));
				System.out.println(rs.getString("Message"));
				allNewsList.add(news.getTransport());

			}
			return allNewsList;

		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			rs.close();
			stmt.close();
			conn.close();
		}

	}
	
	public ArrayList<TNews> getAllNews() throws Exception {

		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;

		ArrayList<TNews> allNewsList = new ArrayList<TNews>();
		try {
			conn = DBUtil.getECMDBConnection();
			stmt = conn.prepareStatement("SELECT * from ECM_NEWS ORDER BY ExpiryDate DESC");
			rs = stmt.executeQuery();

			while (rs.next()) {
				ECMNews news = new ECMNews();
				news.setActiveDate(rs.getTimestamp("ActiveDate"));
				news.setExpiryDate(rs.getTimestamp("ExpiryDate"));
				news.setId(rs.getInt("ID"));
				news.setMessage(rs.getString("Message"));
				news.setSubject(rs.getString("Subject"));
				news.setCreatedDate(DBUtil.convertDateToString(rs.getTimestamp("CreatedDate")));
				news.setCreatedBy(rs.getString("CreatedBy"));
				news.setModifiedDate(DBUtil.convertDateToString(rs.getTimestamp("ModifiedDate")));
				news.setModifiedBy(rs.getString("ModifiedBy"));
				allNewsList.add(news.getTransport());

			}
			return allNewsList;

		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			rs.close();
			stmt.close();
			conn.close();
		}
	}
	
	public ECMNews getNewsById(long id) throws Exception {

		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;

		ECMNews news = new ECMNews();
		try {
			conn = DBUtil.getECMDBConnection();
			stmt = conn.prepareStatement("SELECT * from ECM_NEWS WHERE ID = ? ORDER BY ExpiryDate DESC");
			stmt.setInt(1, (int)id);
			rs = stmt.executeQuery();

			while (rs.next()) {
				news.setActiveDate(rs.getTimestamp("ActiveDate"));
				news.setExpiryDate(rs.getTimestamp("ExpiryDate"));
				news.setId(rs.getInt("ID"));
				news.setMessage(rs.getString("Message"));
				news.setSubject(rs.getString("Subject"));
				news.setCreatedDate(DBUtil.convertDateToString(rs.getTimestamp("CreatedDate")));
				news.setCreatedBy(rs.getString("CreatedBy"));
				news.setModifiedDate(DBUtil.convertDateToString(rs.getTimestamp("ModifiedDate")));
				news.setModifiedBy(rs.getString("ModifiedBy"));
			}
			return news;

		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			rs.close();
			stmt.close();
			conn.close();
		}
	}

	public void saveNews(TNews tn) throws Exception {
		ECMNews news = new ECMNews();
		news.getFromTransport(tn);
		news.save();
	}
	
	public void removeNews(long id) throws Exception {
		ECMNews news = new ECMNews();
		news.setId(id);
		news.remove();
	}
}