package com.ecm.db.model;

import java.sql.PreparedStatement;
import java.sql.Connection;
import java.sql.Timestamp;
import java.util.Date;

import com.ecm.db.util.DBUtil;
import com.ecm.db.list.ECMNewsList;
import com.ecm.db.transport.TAdminEmailSet;
import com.ecm.db.transport.TNews;

public class ECMNews {

	private String subject;
	private String message;
	private Date activeDate;
	private Date expiryDate;
	private String createdBy;
	private String createdDate;
	private String modifiedBy;
	private String modifiedDate;
	private long id;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getSubject() {
		return this.subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}
	
	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Date getActiveDate() {
		return activeDate;
	}

	public void setActiveDate(Date activeDate) {
		this.activeDate = activeDate;
	}

	public Date getExpiryDate() {
		return expiryDate;
	}

	public void setExpiryDate(Date expiryDate) {
		this.expiryDate = expiryDate;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public String getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(String createdDate) {
		this.createdDate = createdDate;
	}

	public String getModifiedBy() {
		return modifiedBy;
	}

	public void setModifiedBy(String modifiedBy) {
		this.modifiedBy = modifiedBy;
	}

	public String getModifiedDate() {
		return modifiedDate;
	}

	public void setModifiedDate(String modifiedDate) {
		this.modifiedDate = modifiedDate;
	}

	private void insert() throws Exception {
		Connection conn = null;
		PreparedStatement ps = null;
		try {
			conn = DBUtil.getECMDBConnection();
			String sqlQuery = "INSERT INTO ECM_NEWS "
					+ "(ID, Subject, Message, ActiveDate, ExpiryDate, CreatedBy, ModifiedBy,"
					+ "CreatedDate, ModifiedDate) "
					+ "VALUES (ECM_NEWS_SEQ.NEXTVAL, ?, ?, ?, ?, ?, ?, SYSDATE, SYSDATE)";
			ps = conn.prepareStatement(sqlQuery);

			ps.setString(1, DBUtil.escapeString(this.subject));
			ps.setString(2, DBUtil.escapeString(this.message));
			ps.setTimestamp(3, new Timestamp(this.activeDate.getTime()));
			ps.setTimestamp(4, new Timestamp(this.expiryDate.getTime()));
			ps.setString(5, this.createdBy);
			ps.setString(6, this.createdBy);
            ps.executeUpdate();
            if(!(sendEmailNewsUpdated()))
			{
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

    private Boolean sendEmailNewsUpdated(){
		
		try {
			ECMMailManager em=new ECMMailManager();
			TAdminEmailSet taes=new TAdminEmailSet();
			taes.subject=this.subject;
			taes.message=this.message;
			
			taes.activeDate=DBUtil.convertDateToString(activeDate);
			
			
			taes.expiryDate=DBUtil.convertDateToString(expiryDate);
			taes.createdBy=this.createdBy;
			taes.createdDate=DBUtil.getTodayDateTime();
			em.sendEmail("ECMNews", taes, "Inserted");
		
		return true;
		} catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
		return false;
		}
		
		
		}
	

	private void update() throws Exception {
		Connection conn = null;
		PreparedStatement ps = null;
		try {
			conn = DBUtil.getECMDBConnection();

			String sqlQuery = "UPDATE ECM_NEWS SET Subject = ?, Message = ?, "
					+ "ActiveDate = ?, ExpiryDate = ?, ModifiedBy = ?, ModifiedDate = SYSDATE "
					+ "WHERE ID = ?";
			ps = conn.prepareStatement(sqlQuery);

			ps.setString(1,  this.subject);
			ps.setString(2, DBUtil.escapeString(this.message));
			ps.setTimestamp(3, new Timestamp(this.activeDate.getTime()));
			ps.setTimestamp(4, new Timestamp(this.expiryDate.getTime()));
			ps.setString(5, this.modifiedBy);
			ps.setInt(6, (int)this.id);

			ps.executeUpdate();
			if(!(sendEmail(subject,message,activeDate, expiryDate,  modifiedBy)))
			{
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

	 private Boolean sendEmail(String subject, String message, Date activeDate, Date expiryDate, String modifiedBy){
			
		 try {
			    ECMMailManager em=new ECMMailManager();
				TAdminEmailSet taes=new TAdminEmailSet();
				taes.subject=this.subject;
				taes.message=this.message;
				taes.activeDate=DBUtil.convertDateToString(activeDate);
				taes.expiryDate=DBUtil.convertDateToString(expiryDate);
				taes.modifiedBy=this.modifiedBy;
				em.sendEmail("ECMNews", taes, "Updated");

		 return true;
		 } catch (Exception e) {
		 // TODO Auto-generated catch block
		 e.printStackTrace();
		 return false;
		 }
	 }
	
	public void remove() throws Exception {
		Connection conn = null;
		PreparedStatement ps = null;
		try {
			conn = DBUtil.getECMDBConnection();
			ECMNews news = ECMNewsList.getInstance().getNewsById(this.id);
			String sqlQuery = "DELETE ECM_NEWS WHERE ID = ?";
			ps = conn.prepareStatement(sqlQuery);
			ps.setInt(1, (int)this.id);

			ps.executeUpdate();
			if(!(sendEmail(news)))
			{
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

	private Boolean sendEmail(ECMNews news){
		try {
			ECMMailManager em=new ECMMailManager();
			TAdminEmailSet taes=new TAdminEmailSet();
			taes.id=news.id;
			taes.subject = news.subject;
			em.sendEmail("ECMNews", taes, "Removed");
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

	public void expire() throws Exception {
		Connection conn = null;
		PreparedStatement ps = null;
		String sqlQuery = null;
		try {
			conn = DBUtil.getECMDBConnection();
			sqlQuery = "UPDATE ECM_NEWS SET ExpiryDate = SYSDATE "
					+ "WHERE ID = ?";
			ps = conn.prepareCall(sqlQuery);
			ps.setInt(1, (int)this.id);
			ps.executeUpdate();

		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			ps.close();
			conn.close();
		}
	}

	public TNews getTransport() throws Exception {

		TNews t_News = new TNews();

		t_News.activeDate = this.activeDate;
		t_News.expiryDate = this.expiryDate;
		t_News.id = this.id;
		t_News.message = this.message;
		t_News.subject = this.subject;
		t_News.createdBy = this.createdBy;
		t_News.createdDate = this.createdDate;
		t_News.modifiedBy = this.modifiedBy;
		t_News.modifiedDate = this.modifiedDate;
		return t_News;
	}

	public void getFromTransport(TNews news) throws Exception {

		this.setActiveDate(news.activeDate);
		this.setExpiryDate(news.expiryDate);
		this.setId(news.id);
		this.setMessage(news.message);
		this.setSubject(news.subject);
		this.setCreatedBy(news.createdBy);
		this.setModifiedBy(news.modifiedBy);
	}

}