package com.ecm.db.util;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Properties;
import java.util.UUID;
import java.util.*;
import javax.mail.*;
import javax.activation.*;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;

public class ECMMail {
	private class ECMMailAttachment {
		private String name;
		private String fileName;
	}
	
	private String smtpServer;
	private String mailFrom;
	private ArrayList<String> mailTo = new ArrayList<String>();
	private ArrayList<String> mailCC = new ArrayList<String>();
	private String mailSubject;
	private String mailMessage;
	private int mailPriority = 1;
	private String mtgLocation;
	private Date mtgStart;
	private Date mtgEnd;
	private String userName;
	private String userPassword;
	private ArrayList<ECMMailAttachment> attachments = new ArrayList<ECMMailAttachment>();
	private String emailType = "MAIL";
 	
	public void setSMTPServer(String host) { this.smtpServer = host; }
	public void setEmailFrom(String from) { this.mailFrom = from; }
	public void setSubject(String sub) { this.mailSubject = sub; }
	public void setMessage(String msg) { this.mailMessage = msg; }
	public void setPriority(int priority) { this.mailPriority = priority; }
	public void setLocation(String location) { this.mtgLocation = location; }
	public void setMeetingStart(Date startTime) { this.mtgStart = startTime; }
	public void setMeetingEnd(Date endTime) { this.mtgEnd = endTime; }
	public void setMailType(String mailType) { this.emailType = mailType; }
	public void setUserName(String userName) { this.userName = userName; }
	public void setPassword(String password) { this.userPassword = password; }
	public void addMailTo(String to) { addRecipient(this.mailTo, to); }
	public void addMailCC(String cc) { addRecipient(this.mailCC, cc); }
	
	private void addRecipient(ArrayList<String> rList, String address) {
		if(address==null)
			return;
		String toAdd = address.toLowerCase().trim();
		if(toAdd.length() <= 0)
			return;
		if(!rList.contains(toAdd))
			rList.add(toAdd); 
	}
	
	public void addAttachment(String name, String fileName) {
		for(ECMMailAttachment att: attachments)
			if(att.name.equalsIgnoreCase(name.trim()))
				return;
		ECMMailAttachment newAtt = new ECMMailAttachment();
		newAtt.name = name.trim();
		newAtt.fileName = fileName;
		attachments.add(newAtt);
	}
	
	public void send() throws Exception { 
		try {
			Properties prop = new Properties(); 
			prop.put("mail.host", this.smtpServer);   
			prop.put("mail.transport.protocol", "smtp");
	        prop.put("mail.smtp.ssl.trust", this.smtpServer);
	        prop.put("mail.smtp.auth", "true");
	        prop.put("mail.smtp.starttls.enable", "true");
	        //prop.put("mail.smtp.socketFactory.class","javax.net.ssl.SSLSocketFactory");
	        //prop.put("mail.smtp.port", "465");
	        
	        Session session = Session.getInstance(prop,
	      		  new javax.mail.Authenticator() {
	      			protected PasswordAuthentication getPasswordAuthentication() {
	      				return new PasswordAuthentication(userName, userPassword);
	      			}
	      		  });
			
			MimeMessage message;
			if(emailType.equalsIgnoreCase("CALENDAR"))
				message = getCalendarItem(session);
			else
				message = getEmailItem(session);
			
			Transport.send(message);
			
		} catch (MessagingException me) {
			me.printStackTrace();
			throw new Exception(me.getMessage());
		} catch (Exception ex) {
			ex.printStackTrace(); 
			throw new Exception(ex.getMessage());
		} 
	} 
	
	private MimeMessage getEmailItem(Session session) throws Exception {
		MimeMessage message = new MimeMessage(session);
		//message.addHeaderLine("charset=UTF-8");  
		message.setFrom(new InternetAddress(this.mailFrom)); 
		for(String to: this.mailTo)
		message.addRecipient(Message.RecipientType.TO, new InternetAddress(to)); 
		for(String cc: this.mailCC)
			message.addRecipient(Message.RecipientType.CC, new InternetAddress(cc));
		message.setSubject(this.mailSubject); 
		message.setSentDate(new Date());
		
		if(attachments.size() > 0) {
			Multipart multipart = new MimeMultipart();  
			
			BodyPart messageBodyPart = new MimeBodyPart(); 
			messageBodyPart.setContent(this.mailMessage, "text/html; charset=utf-8");
			multipart.addBodyPart(messageBodyPart);  
			
			addAttachments(multipart);

			message.setContent(multipart);
		} else {
			message.setContent(this.mailMessage, "text/html; charset=utf-8");
		}
		return message;
	}
	
	private MimeMessage getCalendarItem(Session session) throws Exception{
		MimeMessage message = new MimeMessage(session);
		message.addHeaderLine("method=REQUEST");
		message.addHeaderLine("charset=UTF-8"); 
		message.addHeaderLine("component=VEVENT"); 
		message.setFrom(new InternetAddress(this.mailFrom)); 
		for(String to: this.mailTo)
			message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
		for(String cc: this.mailCC)
			message.addRecipient(Message.RecipientType.CC, new InternetAddress(cc));
		message.setSubject(this.mailSubject); 
		message.setSentDate(new Date());
		String vc = getMessageVCalendar();
		
		BodyPart messageBodyPart = new MimeBodyPart(); 
		// Fill the message  
		messageBodyPart.setHeader("Content-Class", "urn:content-classes:calendarmessage");
		messageBodyPart.setHeader("Content-ID","calendar_message"); 
		messageBodyPart.setFileName("MeetingInvite.ics");
		messageBodyPart.setDataHandler(new DataHandler( new ByteArrayDataSource(vc, "text/calendar")));//very important
		// Create a Multipart  
		Multipart multipart = new MimeMultipart();  
		// Add part one 
		multipart.addBodyPart(messageBodyPart);  
		
		addAttachments(multipart);
		// Put parts in message
		message.setContent(multipart);  
		return message;
	}
	
	private void addAttachments(Multipart mp) throws MessagingException {
		
		for(ECMMailAttachment att: attachments) {
			MimeBodyPart attPart = new MimeBodyPart();  

		    DataSource source = new FileDataSource(att.fileName);  
		    attPart.setDataHandler(new DataHandler(source));  
		    attPart.setFileName(att.name);  
		    mp.addBodyPart(attPart);
		}
	}
	
	private String getMessageVCalendar() {
		StringBuffer sb = new StringBuffer();  
		String attStr = "";
		for(String to: this.mailTo)
			attStr += ("ATTENDEE;CN=" + to + ";MAILTO:" + to + "\n");
		String strTZ = java.util.TimeZone.getDefault().getID();
		sb.append("BEGIN:VCALENDAR\n" +
		"VERSION:2.0\n" +
		"PRODID:-//Microsoft Corporation//Outlook 9.0 MIMEDIR//EN\n"+
		"BEGIN:VEVENT\n" +
		"UID:" + UUID.randomUUID().toString() +"\n" +
		"DTSTAMP;TZID=" + strTZ + ":" + formatDateForMail(new Date()) + "\n" +
		"ORGANIZER;CN=" + this.mailFrom + ";MAILTO:" + this.mailFrom + "\n" + 
		attStr + 
		"LOCATION:" + this.mtgLocation + "\n" +
		"DTSTART;TZID=" + strTZ + ":" + formatDateForMail(this.mtgStart) + "\n" + 
		"DTEND;TZID=" + strTZ + ":" + formatDateForMail(this.mtgEnd) + "\n" +  
		"SUMMARY:" + this.mailSubject + "\n" +
		"DESCRIPTION:" + this.mailMessage + "\n" +
		"PRIORITY:" + this.mailPriority + "\n" +
		"CLASS:PUBLIC\n" + 
		"BEGIN:VALARM\n" + 
		"TRIGGER:-PT30M\n" +
		"ACTION:DISPLAY\n" + 
		"DESCRIPTION:" + this.mailSubject +"\n" + 
		"END:VALARM\n" +
		"END:VEVENT\n" +
		"END:VCALENDAR");
		return sb.toString();
	}
	
	public static String formatDateForMail(Date date)
	{
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd'T'hhmmss");
		sdf.setTimeZone(java.util.TimeZone.getDefault());
		String formattedDate = sdf.format(date);
		return formattedDate;
	}
}

