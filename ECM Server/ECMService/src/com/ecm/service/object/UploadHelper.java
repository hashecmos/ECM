package com.ecm.service.object;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLConnection;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import com.ecm.db.util.ECMLogger;
import com.ecm.db.util.RequestHelper;
import com.ecm.filenet.model.FNDocument;
import com.ecm.filenet.model.FNObjectStore;
import com.ecm.filenet.transport.TFNDocument;
import com.ecm.filenet.util.Utils;
import com.sun.org.apache.xml.internal.security.utils.Base64;

public class UploadHelper {
	private static ECMLogger logger = ECMLogger.getInstance(UploadHelper.class);
	
	private UploadHelper() { }
	
	public static UploadHelper getInstance() { return new UploadHelper(); }
	
	public String addDocument(HttpServletRequest req, FNObjectStore os) throws Exception {
		logger.info("Add Document");
		FNDocument fnd = getDocumentDetails(req, os);
		logger.info("Get Document Details");
		long empNo = RequestHelper.getLoggedInEmployee(req, "");
		fnd.setEmpNo(empNo);
		fnd.addDocument();
		fnd.setSecurityAndTemplate();
		logger.info("FND Add Document");
		AccessPolicyHelper.getInstance().getWorkflowAccessPolicy(fnd.getId(), os);	
		logger.info("Get Workflow Access Policy");
		return fnd.getId();
	}
	
	public String checkinDocument(HttpServletRequest req, FNObjectStore os) throws Exception {
		FNDocument fnd = getDocumentDetails(req, os);
		fnd.checkIn();
		String newDocId = fnd.getId();
		AccessPolicyHelper.getInstance().setNewWorkflowAccessPolicy(newDocId, os);	
		logger.info("Get Workflow Access Policy");
		return newDocId;
	}
	
	private FNDocument getDocumentDetails(HttpServletRequest req, FNObjectStore os) throws Exception {
		ServletFileUpload uploader = new ServletFileUpload(
				new DiskFileItemFactory());
		@SuppressWarnings("unchecked")
		List<FileItem> parseRequest = uploader.parseRequest(req);
		FNDocument fnd = FNDocument.getInstance(os);
		for (FileItem fileItem : parseRequest) {
			if (fileItem.isFormField()) {
				if (fileItem.getFieldName().equalsIgnoreCase("DocInfo")) {
					TFNDocument tfnd = JSONHelper.getInstance().getDocument(fileItem.getString("UTF-8"));
					//setDefaultAccessPolicies(req, tfnd);
					fnd.loadFromTransport(tfnd);
				}
				else if(fileItem.getFieldName().equalsIgnoreCase("document")) {
					fnd.setFileName("Scanned Document.pdf");
					byte inBytes [] = Base64.decode(fileItem.get());
					ByteArrayInputStream bis = new ByteArrayInputStream(inBytes);
					fnd.setInputStream(bis);
				}
			} else { // File
				fnd.setFileName(fileItem.getName());
				InputStream fileIS = fileItem.getInputStream();
				try
				{
					String fileMimeType = URLConnection.guessContentTypeFromStream(fileIS);
					if(fileMimeType != null && fileMimeType.length() > 0)
						fnd.setMimeType(fileMimeType);
				}
				catch(Exception ex){
				}
				fnd.setInputStream(fileItem.getInputStream());
			}
		}
		return fnd;
	}

	public void setDownloadDocument(HttpServletResponse resp, String docTitle, String mimeType, InputStream is) throws Exception {
		if((docTitle == null) || (docTitle.trim().length() <=0 ))
			docTitle = "DownloadDocument";
		if((mimeType == null) || (mimeType.trim().length() <= 0))
				mimeType = Utils.getMimeType(docTitle);
		if((mimeType == null) || (mimeType.trim().length() <= 0))
			mimeType = URLConnection.guessContentTypeFromStream(is);
		if((mimeType == null) || (mimeType.trim().length() <= 0))
			mimeType = "application/octet-stream";
		resp.setHeader("Content-Disposition", "attachment; filename=" + 
				Utils.getFileNameFromMimeType(docTitle, mimeType));
		resp.setContentType(mimeType + "; charset=UTF-8"); // + "; charset=UTF-8");
		resp.setCharacterEncoding("UTF-8");
		OutputStream outStream = resp.getOutputStream();
		byte[] buf = new byte[4096];
		int len = -1;
		while ((len = is.read(buf)) != -1) {
		    outStream.write(buf, 0, len);
		}
		outStream.flush();
		outStream.close();
	}
	
	public void setValidateDocument(HttpServletResponse resp, String docTitle, String mimeType, InputStream is) throws Exception {
        if((docTitle == null) || (docTitle.trim().length() <=0 ))
              docTitle = "DownloadDocument";

        if((mimeType == null) || (mimeType.trim().length() <= 0))
                    mimeType = Utils.getMimeType(docTitle);
        
        if((mimeType == null) || (mimeType.trim().length() <= 0))
			mimeType = URLConnection.guessContentTypeFromStream(is);

        if((mimeType == null) || (mimeType.trim().length() <= 0))
              mimeType = "application/octet-stream";

        resp.setHeader("Content-Disposition", "inline; filename=" + 
                    Utils.getFileNameFromMimeType(docTitle, mimeType));
        resp.setContentType(mimeType + "; charset=UTF-8"); // + "; charset=UTF-8");
        resp.setCharacterEncoding("UTF-8");

        OutputStream outStream = resp.getOutputStream();
        byte[] buf = new byte[4096];
        int len = -1;
        while ((len = is.read(buf)) != -1) {
            outStream.write(buf, 0, len);
        }
        outStream.flush();
        outStream.close();
  }

	
	/*private void setDefaultAccessPolicies(HttpServletRequest req, TFNDocument tfnd) {
		try {
			long empNo = RequestHelper.getLoggedInEmployee(req, "");
			ArrayList<String> apList = ECMAccessPolicyList.getInstance().getDefaultAccessPolicies(
					empNo, tfnd.entryTemplate);
			if(tfnd.accessPolicies == null)
				tfnd.accessPolicies = new ArrayList<String>();
			tfnd.accessPolicies.addAll(apList);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	} */
}
