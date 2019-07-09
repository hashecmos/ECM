package com.ecm.filenet.model;

import java.util.ArrayList;
import java.util.Iterator;

import org.apache.log4j.Logger;

import com.ecm.db.transport.TPermission;
import com.ecm.db.util.DBUtil;
import com.ecm.filenet.transport.TFNProperty;
import com.ecm.filenet.util.Utils;
import com.filenet.api.collection.AccessPermissionList;
import com.filenet.api.constants.AccessType;
import com.filenet.api.constants.AutoUniqueName;
import com.filenet.api.constants.DefineSecurityParentage;
import com.filenet.api.constants.PermissionSource;
import com.filenet.api.constants.RefreshMode;
import com.filenet.api.constants.SecurityPrincipalType;
import com.filenet.api.core.CustomObject;
import com.filenet.api.core.Factory;
import com.filenet.api.core.ReferentialContainmentRelationship;
import com.filenet.api.core.UpdatingBatch;
import com.filenet.api.property.Properties;
import com.filenet.api.property.Property;
import com.filenet.api.security.AccessPermission;
import com.filenet.api.util.Id;

public class FNCustomObject extends FNObject {
	private static final Logger logger = Logger.getLogger(FNDocument.class);
	
	protected FNObjectStore fnOS = null;
	protected FNCustomObjectClass fnClass = null;
	protected CustomObject customObj = null;
	protected ArrayList<TPermission> permissions = new ArrayList<TPermission>();
	protected FNFolder folder = null;
	
	public static FNCustomObject getInstance(FNObjectStore os){
		return new FNCustomObject(os);
	}
	
	protected FNCustomObject(FNObjectStore os){
		fnOS = os;
	}
	
	public FNObjectStore getOs() {
		return fnOS;
	}

	public void setOs(FNObjectStore os) {
		this.fnOS = os;
	}
	
	public ArrayList<TFNProperty> getProperties() throws Exception {
		load();
		return fnClass.getPropertiesTransport();
	}
	
	public ArrayList<TPermission> getPermissions() throws Exception {
		load();
		if(permissions == null)
			permissions = new ArrayList<TPermission>();
		
		return permissions;
	}
	
	public void setFolder(FNFolder fileFolder) { folder = fileFolder; }
	
	public void load() throws Exception {
		try {
			if(customObj != null)
				return;		
			customObj = Factory.CustomObject.fetchInstance(getOs().getObjectStore(), new Id(id), null);
			setObjectProperties(customObj);
			getObjectPermissions(customObj);
		} catch (Exception e) {
			throw new Exception(e.getMessage());
		}
	}
	
	@SuppressWarnings("rawtypes")
	private void setObjectProperties(CustomObject obj)
	{
		if(fnClass == null)
			fnClass = FNCustomObjectClass.getInstance(fnOS);
		fnClass.setId(obj.get_ClassDescription().get_Id().toString());
		fnClass.setSymbolicName(obj.get_ClassDescription().get_SymbolicName());
		fnClass.setName(obj.get_ClassDescription().get_Name());
		
		Iterator iter = obj.getProperties().iterator();
		while(iter.hasNext())
			fnClass.setPropertyValue((Property)iter.next());
		
	}
	
	@SuppressWarnings("deprecation")
	private void getObjectPermissions(CustomObject obj)
	{
		if(permissions == null)
			permissions = new ArrayList<TPermission>();
		@SuppressWarnings("rawtypes")
		Iterator iter = obj.get_Permissions().iterator();
		while(iter.hasNext()) {
			AccessPermission ap = (AccessPermission)iter.next();
			
			if(ap.get_InheritableDepth() == -3 || ap.get_InheritableDepth() == -2)//-2 to be removed
			{
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
		
	}
	
	public void createObject() throws Exception {
		logger.info("Started Method : createObject");
		try 
		{
			ReferentialContainmentRelationship relationship = null;
			UpdatingBatch ub = UpdatingBatch.createUpdatingBatchInstance(
					fnOS.getObjectStore().get_Domain(), RefreshMode.REFRESH);

			//For add document we are passing the document class name. In case of an entry template, document class Id is being passed.
			// That is handled in the exception condition. 
			customObj = Factory.CustomObject.createInstance(fnOS.getObjectStore(), fnClass.getSymbolicName());
			
			//Set the properties for the document
			Properties properties = customObj.getProperties();
			setProperties(properties);
			ub.add(customObj, null);
			//If the document is being filed in a folder, get the folder details, and file the document to the folder.
			if (folder != null) {
				folder.load();
				if (folder.getFolder() != null ){
					relationship = folder.getFolder().file(customObj,AutoUniqueName.AUTO_UNIQUE,
							null,DefineSecurityParentage.DO_NOT_DEFINE_SECURITY_PARENTAGE);
						ub.add(relationship, null);
				}
			}
			//Commit the batch
			//ub.updateBatch();
			customObj.save(RefreshMode.REFRESH);
			
			id=customObj.get_Id().toString();

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new Exception(e.getMessage());
		}
		logger.info("Exit Method : addDocument");
	}
	
	private void setProperties(Properties dProps) 
	{
		ArrayList<FNProperty> pList = fnClass.getProperties();
		for(int i=0; i<pList.size(); i++) {
			pList.get(i).putPropertyValue(dProps);		
		}
	}
	
	public void deleteObject() throws Exception {
		logger.info("Started Method : deleteObject");
		try 
		{
			//For add document we are passing the document class name. In case of an entry template, document class Id is being passed.
			// That is handled in the exception condition. 
			customObj = Factory.CustomObject.fetchInstance(fnOS.getObjectStore(), new Id(this.id), null);
			
			customObj.delete();
			customObj.save(RefreshMode.REFRESH);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new Exception(e.getMessage());
		}
		logger.info("Exit Method : deleteDocument");
	}
	
	private AccessType getAccessType(String inAccess) {
		if(inAccess.equalsIgnoreCase("DENY"))
			return AccessType.DENY;
		else
			return AccessType.ALLOW;
	}
	
	public void setPermissions(ArrayList<TPermission> tpList) throws Exception {
		load();
		if(customObj == null)
			return;
		ArrayList<AccessPermission> apIList = new ArrayList<AccessPermission>();
		AccessPermissionList apList = customObj.get_Permissions();
		for(int i=0; i < apList.size(); i++) {
		//Iter apIter = (Iter)apList.iterator();
		//while(apIter.hasNext()) {
			//AccessPermission ap = (AccessPermission)apIter.next();
			AccessPermission ap = (AccessPermission)apList.get(i);
			for(TPermission tp: tpList) {
				if(tp.action.equalsIgnoreCase("REMOVE")) {
					String gName = Utils.maskCanonicalName(ap.get_GranteeName()).replaceAll(" ", "");//ap.get_GranteeName();
					String justGName = DBUtil.removeAfter(gName, '@');
					if((gName.equalsIgnoreCase(tp.granteeName.replaceAll(" ", "")) ||
							justGName.equalsIgnoreCase(tp.granteeName.replaceAll(" ", ""))) &&
							(ap.get_AccessMask() == tp.accessMask) && 
							(ap.get_AccessType() == getAccessType(tp.accessType))) {
						apIList.add(ap);
					}
				}
			}
		}
		for(AccessPermission ap:apIList) {
			try {
				apList.remove(ap);
			} catch (Exception ex) {
				continue;
			}
		}
		
		for(TPermission tp: tpList) {
			if(tp.action.equalsIgnoreCase("ADD"))
			{
				AccessPermission ap = Factory.AccessPermission.createInstance();
				ap.set_AccessMask((int)tp.accessMask);
				if(tp.accessType.equalsIgnoreCase("DENY"))
					ap.set_AccessType(AccessType.DENY);
				else
					ap.set_AccessType(AccessType.ALLOW);
				
				ap.set_GranteeName(tp.granteeName);
				ap.set_InheritableDepth(-3);
				apList.add(ap);
			}
		}
		
		customObj.save(RefreshMode.REFRESH);
		
	}
	
	public void setAPPermissions(ArrayList<TPermission> tpList) throws Exception {
		load();
		if(customObj == null)
			return;
		AccessPermissionList apList = customObj.get_Permissions();
				
		for(TPermission tp: tpList) {
			AccessPermission ap = Factory.AccessPermission.createInstance();
			ap.set_AccessMask((int)tp.accessMask);
			if(tp.accessType.equalsIgnoreCase("DENY"))
				ap.set_AccessType(AccessType.DENY);
			else
				ap.set_AccessType(AccessType.ALLOW);
			
			ap.set_GranteeName(tp.granteeName);
			ap.set_InheritableDepth(tp.inheritDepth);
			apList.add(ap);
		}
		
		customObj.save(RefreshMode.REFRESH);
		
	}
}
