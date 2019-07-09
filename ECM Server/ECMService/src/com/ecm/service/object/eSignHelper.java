package com.ecm.service.object;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.UUID;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.nio.*;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileInputStream;
import jcifs.smb.SmbFileOutputStream;

import com.ecm.db.list.ECMConfigurationList;
import com.ecm.db.list.ECMUserList;
import com.ecm.db.list.ECMWorkflowList;
import com.ecm.db.list.ECMWorkitemList;
import com.ecm.db.model.ECMDocSign;
import com.ecm.db.transport.TAttachment;
import com.ecm.db.transport.TRecipient;
import com.ecm.db.transport.TSignToken;
import com.ecm.db.transport.TWorkitemAction;
import com.ecm.db.util.DBUtil;
import com.ecm.db.util.ECMEncryption;
import com.ecm.db.util.ECMLogger;
import com.ecm.filenet.model.FNDocument;
import com.ecm.filenet.model.FNObjectStore;

public class eSignHelper {

	private static ECMLogger logger = ECMLogger.getInstance(eSignHelper.class);

	public static eSignHelper getInstance() {
		return new eSignHelper();
	}

	private eSignHelper() {
	}

	private static final String encKey = "A1E88EC43D525843D9EAF11C8EEE0684";

	public TSignToken prepareForeSign(FNObjectStore os, String empNo,
			long roleId, String docid, Boolean isInitial, String url, long witemId)
			throws Exception {
		logger.info("EMPNo: " + empNo + ", RoleID: " + roleId + ", DocID: "
				+ docid + ", Initial: " + isInitial.toString());
		
		String signType = "ESIGN";
		if(isInitial)
			signType = "INITIAL";
		FNDocument fnd = FNDocument.getInstance(os);
		fnd.setId(docid);
		logger.info("Set Doc ID");

		fnd.downloadForeSign();
		logger.info("Downloaded");
		String docTitle = fnd.getDocumentTitle();
		logger.info("Doc Title: " + docTitle);
		String dvsid = fnd.getVsid();
		logger.info("Doc Version Series ID: " + dvsid);
		
		String dMimeType = fnd.getMimeType();
		logger.info("Doc MimeType is: " + dMimeType);
		String fileExt = ".pdf";
		//application/msword
		//application/vnd.openxmlformats-officedocument.wordprocessingml.document
		InputStream fileIS = null;
		String inFileName = UUID.randomUUID().toString();
		if(dMimeType.equalsIgnoreCase("application/msword") 
				|| dMimeType.equalsIgnoreCase("application/vnd.openxmlformats-officedocument.wordprocessingml.document"))
		{
			logger.info("MS Word to PDF Convert: Begin");
			/*ByteArrayOutputStream baos = PdfHelper.convertWordToPdf(fnd.getInputStream());
			logger.info("MS Word to PDF Convert: End");
			fileIS = new ByteArrayInputStream(baos.toByteArray());*/
			fileIS = fnd.getInputStream();
			//String fileMimeType = URLConnection.guessContentTypeFromStream(fileIS);
			//logger.info("BAOS to InputStream - OK");
			//baos.flush();
			//baos.close();
			//logger.info("BAOS - Flush:Close");
			inFileName = "Doc_" + inFileName;
			fileExt = ".doc";
		}
		else if(dMimeType.equalsIgnoreCase("application/pdf")) {
			fileIS = fnd.getInputStream();
		}

		ECMConfigurationList cList = ECMConfigurationList.getInstance("ECM", "APP");
		String signFolder = cList.getConfigValue("ESIGNFOLDER");
		logger.info("Sign Folder: " + signFolder);
		String inFile = inFileName + fileExt;
		logger.info("Input File: " + inFile);
		String outFile = UUID.randomUUID().toString() + ".pdf";
		logger.info("Output File: " + outFile);

		String authDomain = cList.getConfigValue("ESIGNDOMAIN");
		String authUser = decryptString(cList.getConfigValue("ESIGNUSER"));
		String authPass = decryptString(cList.getConfigValue("ESIGNPASSWORD"));
		logger.info("Config Domain: " + authDomain + ", User: " + authUser
				+ ", Password: " + authPass);

		NtlmPasswordAuthentication auth = new NtlmPasswordAuthentication(
				cList.getConfigValue("ESIGNDOMAIN"),
				decryptString(cList.getConfigValue("ESIGNUSER")),
				decryptString(cList.getConfigValue("ESIGNPASSWORD")));
		logger.info("Authenticated");
		SmbFile smbfile = new SmbFile("smb:"
				+ (DBUtil.appendChar(signFolder, '\\') + inFile).replace('\\',
						'/'), auth);
		logger.info("smbFile: " + smbfile);
		/*OutputStream outStream = new SmbFileOutputStream(smbfile);
		logger.info("Created outStream");
		
		byte[] buf = new byte[8192];
		int len = -1;
		while ((len = fileIS.read(buf)) != -1) {
			outStream.write(buf, 0, len);
		}
		outStream.flush();
		outStream.close();
		logger.info("Wrote to outStream");
		fileIS.close();
		logger.info("Closed to inStream");*/
		
		copyFile(smbfile, fileIS);
		
		long wID = ECMWorkitemList.getInstance().getWorkItemWorkflowID(witemId);
		
		ECMDocSign sign = new ECMDocSign();
		sign.setDocId(docid);
		sign.setEmpNo(empNo);
		sign.setRoleId(roleId);
		sign.setInFile(inFile);
		sign.setOutFile(outFile);
		sign.setWitemId(witemId);
		sign.setWorkflowId(wID);
		sign.setSignType(signType);
		sign.setDocTitle(docTitle);
		long eSignId =  sign.getRequestByWorkItem();
		sign.setId(eSignId);
		if(eSignId > 0)
			sign.save(false);
		else
			sign.save(true);
		
		logger.info("Created DB Record");

		String tokenUrl = null;
		long rId = sign.getRequestID();
		sign.setId(rId);
		logger.info("Request ID: " + rId);
		try {
			String requestURL = cList.getConfigValue("ESIGNREQUESTURL");
			logger.info("Request URL: " + requestURL);

			// Start esignature change
			String urlWithRequestId = url + rId;
			System.out.println(" complete button URL :: " + urlWithRequestId);
			tokenUrl = requestToken(empNo, requestURL, urlWithRequestId,
					inFile, outFile, isInitial);
			// END esignature change
			
			/*
			 * String ecmURL =
			 * DBUtil.appendChar(cList.getConfigValue("ESIGNECMURL"), '/') +
			 * "resources/ESignService/completeSign?id=" + rId;
			 * logger.info("ECM URL: " + ecmURL); tokenUrl = requestToken(empNo,
			 * requestURL, ecmURL, inFile, outFile, isInitial);
			 */

			logger.info("Token URL: " + tokenUrl);
		} catch (Exception ex) {
			sign.setMessage(ex.getMessage());
			logger.info("Exception in Prepare. Message: " + ex.getMessage());
			sign.setSignFailure();
			logger.info("Set Sign Failure");
			throw ex;
		}

		TSignToken ts = new TSignToken();
		ts.reqId = rId;
		ts.tokenUrl = tokenUrl;
		logger.info("Sign Token: " + ts.toString());
		return ts;
	}
	
	//os, twa.EMPNo, twa.roleId, twa, wfID
	public String registerForeSign(FNObjectStore os, TWorkitemAction twa, long workflowId, long wItemId)
			throws Exception {
		logger.info("Register Esign Request by EMPNo: " + twa.EMPNo );
		
		long empNo = 0;
		String registerStatus = "NA";
		if(twa.actions != null && twa.actions.length() > 0 
				&& (twa.actions.equalsIgnoreCase("signature") || twa.actions.equalsIgnoreCase("initial")))
		{
			registerStatus = "Failed";
			
			String signType = "ESIGN";
			if(twa.actions.equalsIgnoreCase("initial"))
				signType = "INITIAL";
			
			for(TRecipient tr: twa.recipients) {
				if(tr.id <= 0)
					continue;
				empNo = tr.id;
			}
			
			for(TAttachment ta:twa.attachments) {		
				logger.info("Adding eSign document history");
				try {
					String docid = ta.docId;
					FNDocument fnd = FNDocument.getInstance(os);
					fnd.setId(docid);
					logger.info("Set Doc ID");
	
					fnd.downloadForeSign();
					logger.info("Downloaded");
					String docTitle = fnd.getDocumentTitle();
					logger.info("Doc Title: " + docTitle);
					String dvsid = fnd.getVsid();
					logger.info("Doc Version Series ID: " + dvsid);
				
					if(wItemId <= 0)
						wItemId = ECMWorkflowList.getInstance().getFirstWorkItembyWorkflow(workflowId);
					
					ECMDocSign sign = new ECMDocSign();
					sign.setDocId(docid);
					sign.setEmpNo(ECMUserList.getInstance().getKOCId(empNo));
					sign.setRoleId(twa.roleId);
					sign.setDocTitle(docTitle);
					sign.setDocVsId(dvsid);
					sign.setWorkflowId(workflowId);
					sign.setWitemId(wItemId);
					sign.setSignType(signType);
					registerStatus = sign.save(false);
					logger.info("Created DB Record");
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return registerStatus;
	}
	
	private void copyFile(SmbFile smbfile, InputStream fileIS) throws Exception
	{
		OutputStream outStream = null;
		try {
			outStream = new SmbFileOutputStream(smbfile);
	
			logger.info("Created outStream");
			
			/*byte[] buf = new byte[8192];
			int len = -1;
			while ((len = fileIS.read(buf)) != -1) {
				outStream.write(buf, 0, len);
			}
			outStream.flush();
			outStream.close();
			logger.info("Wrote to outStream");
			fileIS.close();
			logger.info("Closed to inStream");*/
			
			//New Approach Begin
			final ReadableByteChannel inputChannel = Channels.newChannel(fileIS);
			final WritableByteChannel outputChannel = Channels.newChannel(outStream);
			// copy the channels
			logger.info("Start Write To outStream");
			fastChannelCopy(inputChannel, outputChannel);
			// closing the channels
			inputChannel.close();
			outputChannel.close();
			outStream.flush();
			outStream.close();
			fileIS.close();
			logger.info("End Write To  outStream");
			//New Approach End
			
		} catch (SmbException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void fastChannelCopy(final ReadableByteChannel src, final WritableByteChannel dest) throws IOException {
	    final ByteBuffer buffer = ByteBuffer.allocateDirect(16 * 1024);
	    while (src.read(buffer) != -1) {
	      // prepare the buffer to be drained
	      buffer.flip();
	      // write to the channel, may block
	      dest.write(buffer);
	      // If partial transfer, shift remainder down
	      // If buffer is empty, same as doing clear()
	      buffer.compact();
	    }
	    // EOF will leave buffer in fill state
	    buffer.flip();
	    // make sure the buffer is fully drained.
	    while (buffer.hasRemaining()) {
	      dest.write(buffer);
	    }
	  }


	private String requestToken(String empNo, String requestURL, String ecmURL,
			String inputFile, String outputFile, Boolean isInitial)
			throws Exception {
		try {
			logger.info("request Token. EMPNo: " + empNo + ", RequestURL: "
					+ requestURL + ", ECMURL: " + ecmURL + ", InputFile: "
					+ inputFile + ", OutputFile: " + outputFile + ", Initial: "
					+ isInitial.toString());

			ByteArrayOutputStream outArrStream = new ByteArrayOutputStream();

			String userEmail = ECMUserList.getInstance().getUserEmailById(empNo);
			logger.info("User Email: " + userEmail);

			URL url = new URL(requestURL);

			String sentContent = "";
			sentContent += "applicationId=myApp1";
			sentContent += "&redirectUrl=" + ecmURL;
			sentContent += "&signatureFilename=img.png";
			sentContent += "&signerNameConstraints="
					+ URLEncoder.encode("E = " + userEmail, "UTF-8");
			;
			sentContent += "&isInitialsSignature=" + isInitial;
			sentContent += "&empNo=" + empNo;
			sentContent += "&inputFilename=" + inputFile;
			sentContent += "&outputFilename=" + outputFile;

			logger.info("Sent Content: " + sentContent);

			URLConnection conn = url.openConnection();
			conn.setDoInput(true);
			conn.setDoOutput(true);
			conn.setUseCaches(false);
			conn.setRequestProperty("Content-Type",
					"application/x-www-form-urlencoded");
			logger.info("Set Connection");

			DataOutputStream outStream = new DataOutputStream(
					conn.getOutputStream());
			logger.info("Create OutStream");
			outStream.writeBytes(sentContent);
			logger.info("Write Bytes");
			outStream.flush();
			outStream.close();
			logger.info("Flush & Close");

			int off = 0;
			byte[] content = null;
			byte[] buffer = new byte[1024];
			int length = 0;
			DataInputStream inStream = new DataInputStream(
					conn.getInputStream());
			logger.info("Create inputStream");
			while ((length = inStream.read(buffer)) != -1) {
				logger.info("Read Buffer Start");
				outArrStream.write(buffer, off, length);
				logger.info("Read Buffer End");
				off = off + length;
			}
			inStream.close();
			logger.info("Read inputStream");

			content = outArrStream.toByteArray();
			logger.info("Convert to Byte Array");
			outArrStream.close();
			logger.info("Close outStream");

			if (content == null || content.length == 0)
				return null;

			logger.info("Content Not NULL");
			String receivedContent = new String(content, "UTF8");
			receivedContent = receivedContent.replace("\r", "");
			receivedContent = receivedContent.replace("\n", "");
			logger.info("Received Content: " + receivedContent);

			String[] returnURLs = receivedContent.split("doSignUrl=");
			logger.info("ReturnURLs: " + returnURLs.toString());
			if (returnURLs == null || returnURLs.length < 2)
				return null;
			logger.info("ReturnURL: " + returnURLs[1]);
			return returnURLs[1];
		} catch (Exception e) {
			logger.info("Exception in requestToken: " + e.getMessage());
			e.getStackTrace();
			throw e;
		}
	}

	public String completeSign(FNObjectStore os, long reqId, String eStatus)
			throws Exception {
		if (reqId > 0)
			return completeSignWithID(os, reqId, eStatus);
		else
			return "Error"; //completeSignWithoutID(os);
	}

	private String completeSignWithID(FNObjectStore os, long reqId, String eStatus)
			throws Exception {
		logger.info("Enter: CompleteSignwithID ReqID: " + reqId + ", eStatus: "
				+ eStatus);
		ECMDocSign sign = new ECMDocSign();
		sign.setId(reqId);
		sign.load();
		logger.info("Loaded SignRequest");

		String exitMessage = getExitStatusMessage(eStatus);
		if (exitMessage == null)
			logger.info("Exit Message: NULL");
		else
			logger.info("Exit Message: " + exitMessage);
		if ((exitMessage != null) && (exitMessage.trim().length() > 0)) {
			sign.setMessage(exitMessage);
			sign.setSignFailure();
			logger.info("Set Sign Failure ");
			throw new Exception(exitMessage);
		}

		if (!checkinSignedDocument(os, sign))
			throw new Exception("Error while retrieving signed document!");
		
		return sign.getSignType(); 
	}

	private Boolean checkinSignedDocument(FNObjectStore os, ECMDocSign sign) {
		try {
			logger.info("Enter: checkinSignedDocument ReqID: "
					+ sign.getRequestID());
			ECMConfigurationList cList = ECMConfigurationList.getInstance(
					"ECM", "APP");
			String signFolder = cList.getConfigValue("ESIGNFOLDER");
			logger.info("Sign Folder: " + signFolder);

			logger.info("Auth Domain: " + cList.getConfigValue("ESIGNDOMAIN")
					+ ", User: "
					+ decryptString(cList.getConfigValue("ESIGNUSER"))
					+ ", Password: "
					+ decryptString(cList.getConfigValue("ESIGNPASSWORD")));

			NtlmPasswordAuthentication auth = new NtlmPasswordAuthentication(
					cList.getConfigValue("ESIGNDOMAIN"),
					decryptString(cList.getConfigValue("ESIGNUSER")),
					decryptString(cList.getConfigValue("ESIGNPASSWORD")));

			logger.info("Authenticated ");
			SmbFile smbfile = new SmbFile(
					"smb:" + (DBUtil.appendChar(signFolder, '\\') + sign.getOutFile()).replace(
									'\\', '/'), auth);
			logger.info("SMBFile: " + smbfile);
			InputStream inStream = new SmbFileInputStream(smbfile);
			logger.info("Created InputStream ");

			FNDocument fnd = FNDocument.getInstance(os);
			fnd.setId(sign.getDocId());
			logger.info("SetID for FNDocument. ID = " + sign.getDocId());
			fnd.checkOut();
			logger.info("FNDocument Checkout");
			fnd.setInputStream(inStream);
			logger.info("FNDocument set input Stream");
			boolean checkInStatus = false;
			try {
				fnd.setMimeType("application/pdf");
				fnd.checkIn();
				logger.info("FNDocument Checkin");
				checkInStatus = true;
			} catch (Exception ex) {
				fnd.cancelCheckOut();
				logger.info("FNDocument Cancel Checkout");
				logger.info(ex.getMessage());
				return false;
			}
			if (checkInStatus) {
				sign.setSignDocId(fnd.getId());
				logger.info("FNDocument Checkin New ID: " + fnd.getId());
				sign.setSignSuccess();
				logger.info("SetSignSuccess");
				return true;
			} else
				return false;
		} catch (Exception e) {
			logger.info(e.getMessage());
			return false;
		}
	}

	private String getExitStatusMessage(String exitStatus) throws Exception {
		String exitMessage = null;
		try {
			if (exitStatus == null || exitStatus.equalsIgnoreCase(""))
				exitStatus = "-1";

			if (exitStatus.equalsIgnoreCase("-1"))
				exitMessage = "Unknown Error";
			else if (exitStatus.equalsIgnoreCase("1"))
				exitMessage = "Unknown Error";
			else if (exitStatus.equalsIgnoreCase("2"))
				exitMessage = "eSign has been Canceled";
			else if (exitStatus.equalsIgnoreCase("3"))
				exitMessage = "Signature has been Failed";
			else if (exitStatus.equalsIgnoreCase("4"))
				exitMessage = "pdf Error";
			else if (exitStatus.equalsIgnoreCase("5"))
				exitMessage = "Database Error";
			else if (exitStatus.equalsIgnoreCase("6"))
				exitMessage = "Connection Error";
			else if (exitStatus.equalsIgnoreCase("7"))
				exitMessage = "pdf Converter Error";
			else if (exitStatus.equalsIgnoreCase("100"))
				exitMessage = "Invalid Request Id";
			else if (exitStatus.equalsIgnoreCase("101"))
				exitMessage = "Invalid Application Id";
			else if (exitStatus.equalsIgnoreCase("102"))
				exitMessage = "Invalid Input Folder";
			else if (exitStatus.equalsIgnoreCase("103"))
				exitMessage = "Invalid Input Filename";
			else if (exitStatus.equalsIgnoreCase("104"))
				exitMessage = "Invalid Signature Image Filename";
			else if (exitStatus.equalsIgnoreCase("105"))
				exitMessage = "Invalid Output Folder";
			else if (exitStatus.equalsIgnoreCase("106"))
				exitMessage = "Invalid Output Filename";
			else if (exitStatus.equalsIgnoreCase("107"))
				exitMessage = "Invalid Redirect URL";
			else if (exitStatus.equalsIgnoreCase("108"))
				exitMessage = "Invalid Signer Name Constraint";
			else if (exitStatus.equalsIgnoreCase("200"))
				exitMessage = "Invalid Certificate";
			else if (exitStatus.equalsIgnoreCase("201"))
				exitMessage = "No Matching Certificate";
			else
				exitMessage = "";

			return exitMessage;
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		}
	}
	
	public String getSignRequestsByWorkItem(FNObjectStore os, String docId, long witemId) throws Exception {

		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		String vResult = "False";
		
		try {
			FNDocument fnd = FNDocument.getInstance(os);
			fnd.setId(docId);
			logger.info("Verify eSign Request: Start");

			fnd.loadDocOnly();
			String docVsId = fnd.getVsid();
			
			conn = DBUtil.getECMDBConnection();
			stmt = conn.prepareStatement("SELECT * from ECM_ESIGN_DOCS WHERE "
					+ "WORKITEMID = ? AND (DOCID = ? OR DOCVSID = ? OR SIGNDOCID = ?) AND STATUS = 'SIGNED'");
			stmt.setInt(1, (int) witemId);
			stmt.setString(2, DBUtil.escapeString(docId));
			stmt.setString(3, DBUtil.escapeString(docVsId));
			stmt.setString(4, DBUtil.escapeString(docId));
			
			rs = stmt.executeQuery();

			while (rs.next()) {
				vResult = "True";
			}
			logger.info("Verify eSign Request: Start");
			return vResult;

		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			rs.close();
			stmt.close();
			conn.close();
		}
	}

	public String getResponseURL(Boolean bSuccess, String eSignType) {
		logger.info("Inside getResponseURL: eSignType = " + eSignType + " + Result= " + bSuccess);
		String respURL = "failure.html";
		try {
			String ecmURL = ECMConfigurationList.getInstance("ECM", "APP").getConfigValue("ESIGNECMURL");
			if (bSuccess)
				respURL = DBUtil.appendChar(ecmURL, '/') + "EsignResult.jsp?result=success&type="+eSignType.toUpperCase();
			else
				respURL = DBUtil.appendChar(ecmURL, '/') + "EsignResult.jsp?result=failed&type="+eSignType.toUpperCase();
			
			logger.info("Completed getResponseURL: URL = " + respURL);
		} catch (Exception e) {
			logger.debug("Exception in getResponseURL: " + e.getMessage());
		}
		return respURL;
	}

	private String decryptString(String inString) throws Exception {
		ECMEncryption enc = new ECMEncryption();
		return enc.getDecryptedString(encKey, inString);
	}
}
