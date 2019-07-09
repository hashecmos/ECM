package com.ecm.filenet.model;

import java.util.ArrayList;
import java.util.Iterator;

import org.apache.log4j.Logger;

import com.ecm.db.transport.TPermission;
import com.ecm.db.util.DBUtil;
import com.ecm.filenet.util.Utils;
import com.filenet.api.collection.AccessPermissionList;
import com.filenet.api.constants.AccessType;
import com.filenet.api.constants.PermissionSource;
import com.filenet.api.constants.RefreshMode;
import com.filenet.api.constants.SecurityPrincipalType;
import com.filenet.api.core.Annotation;
import com.filenet.api.core.Document;
import com.filenet.api.core.Factory;
import com.filenet.api.security.AccessPermission;
import com.filenet.api.util.Id;

public class FNAnnotation extends FNObject {
	private static final Logger logger = Logger.getLogger(FNDocument.class);
	
	protected FNObjectStore fnOS = null;
	protected String docId = null;
	protected String annotId = null;
	protected ArrayList<TPermission> permissions = null;
	
	Annotation annotObj = null;
	Document document = null;
	
	public static FNAnnotation getInstance(FNObjectStore os){
		return new FNAnnotation(os);
	}
	
	public String getDocId() {
		return docId;
	}

	public void setDocId(String docId) {
		this.docId = docId;
	}

	protected FNAnnotation(FNObjectStore os){
		fnOS = os;
	}
	
	public FNObjectStore getOs() {
		return fnOS;
	}

	public void setOs(FNObjectStore os) {
		this.fnOS = os;
	}
	
	public Annotation getAnnotObj() {
		return annotObj;
	}

	public void setAnnotObj(Annotation annotObj) {
		this.annotObj = annotObj;
	}
	
	public String getAnnotId() {
		return annotId;
	}

	public void setAnnotId(String annotId) {
		this.annotId = annotId;
	}
	
	public ArrayList<TPermission> getPermissions() throws Exception {
		loadAnnotation();
		return permissions;
	}

	public void load() throws Exception {
		try {
			if(document != null)
				return;		
			document = Factory.Document.fetchInstance(getOs().getObjectStore(), new Id(docId), null);
			//setObjectProperties(annotObj);
			//getObjectPermissions(annotObj);
		} catch (Exception e) {
			throw new Exception(e.getMessage());
		}
	}
	
	public void loadAnnotation() throws Exception {
		try {
			if(annotObj != null)
				return;		
			annotObj = Factory.Annotation.fetchInstance(getOs().getObjectStore(), new Id(annotId), null);
			//setObjectProperties(customObj);
			getObjectPermissions(annotObj);
		} catch (Exception e) {
			throw new Exception(e.getMessage());
		}
	}
	
	private void getObjectPermissions(Annotation annot)
	{
		if(permissions == null)
			permissions = new ArrayList<TPermission>();
		Iterator iter = annot.get_Permissions().iterator();
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

	
	
	private AccessType getAccessType(String inAccess) {
		if(inAccess.equalsIgnoreCase("DENY"))
			return AccessType.DENY;
		else
			return AccessType.ALLOW;
	}
	
	@SuppressWarnings("unchecked")
	public void setPermissions(ArrayList<TPermission> tpList)  {		
		try {
			load();
			if (document == null)
				return;
			Iterator iter = document.get_Annotations().iterator();
			while (iter.hasNext()) {
				try {
					Annotation vAnnot = (Annotation)iter.next();
					this.annotId = vAnnot.get_Id().toString();
					//Loading Annotation with SSO
					loadAnnotation();
					
					if(annotObj == null)
						return;

					ArrayList<AccessPermission> apIList = new ArrayList<AccessPermission>();
					AccessPermissionList apList = annotObj.get_Permissions();
					for (int i = 0; i < apList.size(); i++) {
						AccessPermission ap = (AccessPermission) apList.get(i);
						for (TPermission tp : tpList) {
							if (tp.action.equalsIgnoreCase("REMOVE")) {
								String gName = Utils.maskCanonicalName(
										ap.get_GranteeName()).replaceAll(" ", "");// ap.get_GranteeName();
								String justGName = DBUtil.removeAfter(gName, '@');
								if ((gName.equalsIgnoreCase(tp.granteeName.replaceAll(
										" ", "")) || justGName
										.equalsIgnoreCase(tp.granteeName.replaceAll(
												" ", "")))
										&& (ap.get_AccessMask() == tp.accessMask)
										&& (ap.get_AccessType() == getAccessType(tp.accessType))) {
									apIList.add(ap);
								}
							}
						}
					}
					for (AccessPermission ap : apIList) {
						try {
							apList.remove(ap);
						} catch (Exception ex) {
							continue;
						}
					}

					for (TPermission tp : tpList) {
						if (tp.action.equalsIgnoreCase("ADD")) {
							AccessPermission ap = Factory.AccessPermission
									.createInstance();
							ap.set_AccessMask((int) tp.accessMask);
							if (tp.accessType.equalsIgnoreCase("DENY"))
								ap.set_AccessType(AccessType.DENY);
							else
								ap.set_AccessType(AccessType.ALLOW);

							ap.set_GranteeName(tp.granteeName);
							ap.set_InheritableDepth(tp.inheritDepth);
							apList.add(ap);
						}
					}

					annotObj.save(RefreshMode.REFRESH);
					annotObj = null;
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			document.save(RefreshMode.REFRESH);
			document = null;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
