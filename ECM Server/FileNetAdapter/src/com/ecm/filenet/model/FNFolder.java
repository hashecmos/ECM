package com.ecm.filenet.model;
import java.util.ArrayList;
import java.util.Iterator;

import com.ecm.db.transport.TPermission;
import com.ecm.db.util.DBUtil;
import com.ecm.filenet.list.FNDocumentSearch;
import com.ecm.filenet.transport.TFNDocument;
import com.ecm.filenet.transport.TFNFolder;
import com.ecm.filenet.util.Utils;
import com.filenet.api.constants.AccessRight;
import com.filenet.api.constants.AccessType;
import com.filenet.api.constants.PermissionSource;
import com.filenet.api.constants.RefreshMode;
import com.filenet.api.constants.SecurityPrincipalType;
import com.filenet.api.core.Document;
import com.filenet.api.core.Factory;
import com.filenet.api.core.Folder;
import com.filenet.api.core.Link;
import com.filenet.api.property.Property;
import com.filenet.api.security.AccessPermission;
import com.filenet.api.util.Id;

public class FNFolder extends FNObject{
	private FNObjectStore fnOS = null;
	private String path = null;
	private String className = null;
	ArrayList<FNProperty> properties = null;
	protected ArrayList<TPermission> permissions = null;
	Folder folder = null;
	
	public static FNFolder getInstance(FNObjectStore os) {
		return new FNFolder(os);
	}

	private FNFolder(FNObjectStore os) {
		fnOS = os;
	}
	public String getPath() { return path; }
	public void setPath(String path) { this.path = path; }
	public Boolean exists() {
		load();
		if(folder == null)
			return false;
		return true;
	}
	public void setFolder(Folder folder) {
		this.folder = folder;
	}
	public Folder getFolder() {
		load();
		return this.folder;
	}
	public void setClassName(String className) {
		this.className = className;
	}
	public String getClassName() {
		load();
		return this.className;
	}
	public ArrayList<TPermission> getPermissions() throws Exception {
		load();
		if(permissions == null)
			getObjectPermissions(folder);
		return permissions;
	}
	
	public void load()
	{
		try {
			if(folder != null)
				return;
			if(id == null) {
				String fPath = Utils.appendSlash(path);
				folder = Factory.Folder.fetchInstance(fnOS.getObjectStore(), fPath, null);
			} else
				folder = Factory.Folder.fetchInstance(fnOS.getObjectStore(), new Id(id), null);
			if(folder != null) {
				id = folder.get_Id().toString();
				name = folder.get_FolderName();
				symbolicName = folder.get_Name();
				className = folder.get_ClassDescription().get_SymbolicName();
				path = folder.get_PathName();
			}
			getObjectPermissions(folder);
		} catch (Exception e) {
		}
	}
	
	public void loadFolderOnly()
	{
		try {
			if(folder != null)
				return;
			if(id == null) {
				String fPath = Utils.appendSlash(path);
				folder = Factory.Folder.fetchInstance(fnOS.getObjectStore(), fPath, null);
			} else
				folder = Factory.Folder.fetchInstance(fnOS.getObjectStore(), new Id(id), null);
			if(folder != null) {
				id = folder.get_Id().toString();
				name = folder.get_FolderName();
				symbolicName = folder.get_Name();
				className = folder.get_ClassDescription().get_SymbolicName();
				path = folder.get_PathName();
			}
			//getObjectPermissions(folder);
		} catch (Exception e) {
		}
	}
	
	public FNFolder createSubFolder(String folderName) throws Exception {
		String subPath = Utils.appendSlash(path) + folderName;
		load();
		if(folder == null)
			throw new Exception("Parent folder does not exist");
		FNFolder subFolder = new FNFolder(fnOS);
		subFolder.setPath(subPath);
		if(subFolder.exists())
			return subFolder;
		boolean isHidden = folder.getProperties().getBooleanValue("IsHiddenContainer");
		Folder sfolder = Factory.Folder.createInstance(fnOS.getObjectStore(), className);
		sfolder.set_FolderName(folderName);
		sfolder.set_Parent(folder);
		sfolder.getProperties().putValue("IsHiddenContainer", isHidden);
		sfolder.save(RefreshMode.REFRESH);
		System.out.println("Created");
		subFolder.setFolder(sfolder);
		subFolder.setId(sfolder.get_Id().toString());
		return subFolder;
	}

	public void delete() throws Exception {
		try {
			load();
			folder.delete();
			folder.save(RefreshMode.REFRESH);
			id = null;
			path = null;
			folder = null;
			className = null;
		} catch (Exception e) {
			throw new Exception(e.getMessage());
		}
	}

	public TFNFolder getTransport()
	{
		TFNFolder tnF=new TFNFolder();
		tnF.id=id;	
		tnF.name=name;
		tnF.path=path;
		tnF.type=className;
		return tnF;
	}
	
	public void loadFromTransport(TFNFolder tnF) 
	{
		if(tnF == null)
			return;
		id = tnF.id;
		name = tnF.name;
		path = tnF.path;
		className = tnF.type;
	}

	public ArrayList<TFNFolder> getTopSubFolders(String strFolderClass) throws Exception
	{
		load();
		if(folder == null)
			throw new Exception("Parent folder does not exist");
		
		ArrayList<TFNFolder> folderList = new ArrayList<TFNFolder>();
		Iterator iter = folder.get_SubFolders().iterator();
		while(iter.hasNext()) {
			Folder subFolder = (Folder) iter.next();
			if(subFolder != null) {
				if(subFolder.getProperties().getBooleanValue("IsHiddenContainer"))
					continue;
				if(subFolder.get_ClassDescription().get_SymbolicName().equalsIgnoreCase("ECMTOPFOLDER"))
				{
					TFNFolder sFolder = new TFNFolder();
					sFolder.id = subFolder.get_Id().toString();
					sFolder.name = subFolder.get_FolderName();
					sFolder.path = subFolder.get_PathName();
					sFolder.type = subFolder.get_ClassDescription().get_SymbolicName();
					
					folderList.add(sFolder);
				}
			}
		}
		return folderList;
	}
	
	private boolean belongsToClass(String objectClass, String className, String [] superClasses) {
		String symClassName = className.replace(" ", "");
		if((objectClass != null) && ((objectClass.trim().equalsIgnoreCase(symClassName)) ||
				(objectClass.trim().equalsIgnoreCase(className)))) {
			return true;
		}
		
		if(superClasses == null)
			return false;
		
		for(String sClass:superClasses) {
			if((sClass != null) && ((sClass.trim().equalsIgnoreCase(symClassName)) ||
					(sClass.trim().equalsIgnoreCase(className)))) {
				return true;
			}
		}
		return false;
	}
	
	public ArrayList<TFNFolder> getSubFolders() throws Exception
	{
		load();
		if(folder == null)
			throw new Exception("Parent folder does not exist");
		
		ArrayList<TFNFolder> folderList = new ArrayList<TFNFolder>();
		Iterator iter = folder.get_SubFolders().iterator();
		while(iter.hasNext()) {
			Folder subFolder = (Folder) iter.next();
			if(subFolder != null) {
				if(subFolder.getProperties().getBooleanValue("IsHiddenContainer"))
					continue;
				TFNFolder sFolder = new TFNFolder();
				sFolder.id = subFolder.get_Id().toString();
				sFolder.name = subFolder.get_FolderName();
				sFolder.path = subFolder.get_PathName();
				sFolder.type = subFolder.get_ClassDescription().get_SymbolicName();
				
				// TODO: Uncomment the below line once the ECM Folder class is created
				//if(belongsToClass(subFolder.getClassName(), "ECM Folder", subFolder.getSuperClasses()))
					folderList.add(sFolder);
				
			}
		}
		return folderList;
	}
	
	public TFNFolder getFolderDetails() throws Exception
	{
		load();
		if(folder == null)
			throw new Exception("Parent folder does not exist");

		TFNFolder sFolder = new TFNFolder();
		sFolder.id = this.id;
		sFolder.name = this.name;
		sFolder.path = this.path;
		sFolder.type = this.className;
				
		return sFolder;
	}
	
	public ArrayList<TFNFolder> getSubFoldersForAdd() throws Exception
    {
          load();
          if(folder == null)
                throw new Exception("Parent folder does not exist");
          ArrayList<TFNFolder> folderList = new ArrayList<TFNFolder>();
          Iterator iter = folder.get_SubFolders().iterator();
          while(iter.hasNext()) {
                Folder subFolder = (Folder)  iter.next();
                if(subFolder != null) {
                      if(subFolder.getProperties().getBooleanValue("IsHiddenContainer"))
                            continue;
                      int accessAllowed = subFolder.getAccessAllowed();
                      if (checkRight(accessAllowed, AccessRight.LINK)|| checkRight(accessAllowed, AccessRight.WRITE))
                      {
                            System.out.print("Access level " + AccessRight.LINK.toString() + " is present");   
                            TFNFolder sFolder = new TFNFolder();
                            sFolder.id = subFolder.get_Id().toString();
                            sFolder.name = subFolder.get_FolderName();
                            sFolder.path = subFolder.get_PathName();
                            sFolder.type = subFolder.get_ClassDescription().get_SymbolicName();
                            // TODO: Uncomment the below line once the ECM Folder class is created
                            //if(belongsToClass(subFolder.getClassName(), "ECM Folder", subFolder.getSuperClasses()))
                            folderList.add(sFolder);
                      }else
                            continue;
                }
          }
          return folderList;
    }
	
	private boolean checkRight(int rights, AccessRight ar)
	{
      return (rights & ar.getValue()) != 0;
	}


	
	public ArrayList<TFNDocument> getDocuments() throws Exception
	{
		load();
		if(folder == null)
			throw new Exception("Folder does not exist");
		
		ArrayList<TFNDocument> docList = new ArrayList<TFNDocument>();
		Iterator iter = folder.get_ContainedDocuments().iterator();
		while(iter.hasNext()) {
			Document doc = (Document) iter.next();
			if(doc != null) {
				FNDocument fnd = FNDocument.getInstance(fnOS);
				fnd.setId(doc.get_Id().toString());			 
				
				if(belongsToClass(doc.getClassName(), "KOC Document", doc.getSuperClasses()))
					docList.add(fnd.getDocumentTransportObject());//getTransport()
			}
		}
		return docList;
	}
	
	private void getObjectPermissions(Folder folder)
	{
		if(permissions == null)
			permissions = new ArrayList<TPermission>();
		Iterator iter = folder.get_Permissions().iterator();
		while(iter.hasNext()) {
			AccessPermission ap = (AccessPermission)iter.next();
			TPermission perm = new TPermission();
			if(ap.get_AccessType() == AccessType.ALLOW)
				perm.accessType = "ALLOW";
			else if(ap.get_AccessType() == AccessType.DENY)
				perm.accessType = "DENY";
			else
				perm.accessType = "UNKNOWN";
			perm.accessMask = ap.get_AccessMask();
			perm.granteeName = Utils.maskCanonicalName(ap.get_GranteeName());
			if(ap.get_GranteeType() == SecurityPrincipalType.GROUP)
				perm.granteeType = "GROUP";
			else if(ap.get_GranteeType() == SecurityPrincipalType.USER)
				perm.granteeType = "USER";
			else
				perm.granteeType = "UNKNOWN";
			
			if(ap.get_PermissionSource() == PermissionSource.MARKING)
				perm.permissionSource = "MARKING";
			else if(ap.get_PermissionSource() == PermissionSource.PROXY)
				perm.permissionSource = "PROXY";
			else if(ap.get_PermissionSource() == PermissionSource.SOURCE_DEFAULT)
				perm.permissionSource = "DEFAULT";
			else if(ap.get_PermissionSource() == PermissionSource.SOURCE_DIRECT)
				perm.permissionSource = "DIRECT";
			else if(ap.get_PermissionSource() == PermissionSource.SOURCE_PARENT)
				perm.permissionSource = "PARENT";
			else if(ap.get_PermissionSource() == PermissionSource.SOURCE_TEMPLATE)
				perm.permissionSource = "TEMPLATE";
			else
				perm.permissionSource = "UNKNOWN";
			
			perm.accessLevel = Utils.getAccessLevel(perm.accessMask);
			
			perm.inheritDepth = ap.get_InheritableDepth();
			perm.depthName = Utils.getInheritableDepthName(perm.inheritDepth);
			
			perm.action = "READ";
			
			permissions.add(perm);
		}
		
	}
	
	//FNFolder.java
	public String moveToFolder(String sourceID, String targetID) throws Exception {
			
		if(sourceID == null)
			throw new Exception("Source folder does not exist");
				
		if(targetID == null)
			throw new Exception("Target folder does not exist");
		
		try
		{
			FNFolder sourceFolder = FNFolder.getInstance(fnOS);
			sourceFolder.setId(sourceID);
			sourceFolder.load();
			
			FNFolder targetFolder = FNFolder.getInstance(fnOS);
			targetFolder.setId(targetID);
			targetFolder.load();
			
			Folder sFolder = sourceFolder.folder;
			Folder tFolder = targetFolder.folder;
			sFolder.move(tFolder);
			sFolder.set_Parent(tFolder);
			sFolder.save(RefreshMode.NO_REFRESH);
			System.out.println("Moved Folder");
			return "OK";
		}
		catch(Exception ex){
			System.out.println("Failed Moving");
			throw new Exception("Failed to move folder: " + ex.getMessage());
		}
	}
	
	public String getAccessPolicy(int seqNo) {
		try {
			if(folder == null)
				folder = Factory.Folder.fetchInstance(fnOS.getObjectStore(), new Id(id), null);
			if((seqNo < 1) || (seqNo > 6))
				return null;
			Property p = folder.getProperties().find("AccessPolicy" + seqNo);
			return p.getIdValue().toString();
		} catch (Exception e) {
			return null;
		}
	}
}
