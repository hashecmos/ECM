package com.ecm.service.object;

import java.io.InputStream;

public class FileObject {
	private InputStream inputStream;
	private String fileName;
	private String folderId;
	private String folderPath;
	private String docClass;
	public InputStream getInputStream() {
		return inputStream;
	}
	public void setInputStream(InputStream inputStream) {
		this.inputStream = inputStream;
	}
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String filenName) {
		this.fileName = filenName;
	}
	public String getFolderId() {
		return folderId;
	}
	public void setFolderId(String folderId) {
		this.folderId = folderId;
	}
	public String getFolderPath() {
		return folderPath;
	}
	public void setFolderPath(String folderPath) {
		this.folderPath = folderPath;
	}
	public String getDocClass() {
		return docClass;
	}
	public void setDocClass(String docClass) {
		this.docClass = docClass;
	}
	
}
