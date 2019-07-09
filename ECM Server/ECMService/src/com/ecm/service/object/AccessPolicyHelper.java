package com.ecm.service.object;

import java.util.ArrayList;

import javax.ws.rs.core.Response;

import com.ecm.db.list.ECMAccessPolicyList;
import com.ecm.db.list.ECMAdministrationList;
import com.ecm.db.list.ECMRoleList;
import com.ecm.db.list.ECMUserList;
import com.ecm.db.model.ECMAccessPolicy;
import com.ecm.db.model.ECMMailManager;
import com.ecm.db.transport.TAccessPolicy;
import com.ecm.db.transport.TAdminEmailSet;
import com.ecm.db.transport.TAttachment;
import com.ecm.db.transport.TDocPermissions;
import com.ecm.db.transport.TDocPrincipal;
import com.ecm.db.transport.TPermission;
import com.ecm.db.transport.TRecipient;
import com.ecm.db.transport.TWorkitemAction;
import com.ecm.db.util.DBUtil;
import com.ecm.db.util.RequestHelper;
import com.ecm.filenet.model.FNAccessPolicy;
import com.ecm.filenet.model.FNDocument;
import com.ecm.filenet.model.FNFolder;
import com.ecm.filenet.model.FNObjectStore;
import com.ecm.filenet.transport.TFNDocument;

public class AccessPolicyHelper {
	private AccessPolicyHelper() { }
	
	public static AccessPolicyHelper getInstance() { return new AccessPolicyHelper(); }
	
	public String createAccessPolicy(TAccessPolicy ta, FNObjectStore os) throws Exception {
		TAccessPolicy tap = ECMAccessPolicyList.getInstance().getAccessPolicy(ta);
		FNAccessPolicy fnap = FNAccessPolicy.getInstance(os,ta.type);		
		if(tap != null) {
			ta.objectId = tap.objectId;
			ta.id = tap.id;
			fnap.setId(tap.objectId);
		} else {	
			String orgCode = ECMAdministrationList.getInstance().getOrgCodeFromUnitID(ta.orgUnitId);
			if(orgCode == null || orgCode == "")
				orgCode = "1";
			ta.objectId = fnap.createAccessPolicy(ta.name, orgCode, ta.desc);
		}
		try {
			fnap.setPermissions(ta.permissions);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ECMAccessPolicy ap = new ECMAccessPolicy();
		//ap= null;
		ap.getFromTransport(ta);
		ap.save();
		tap = ECMAccessPolicyList.getInstance().getAccessPolicy(ta);
		if(tap != null) {
			ta.id = tap.id;
		} 

		return ta.objectId;
	}
	
	public String createPermAccessPolicy(TAccessPolicy ta, FNObjectStore os) throws Exception {
		TAccessPolicy tap = ECMAccessPolicyList.getInstance().getAccessPolicy(ta);
		if(tap != null) {
			ta.objectId = tap.objectId;
			ta.id = tap.id;
		} else {
			FNAccessPolicy fnap = FNAccessPolicy.getInstance(os,ta.type);
			ta.objectId = fnap.createAccessPolicy(ta.name, null, ta.desc); 
			try {
				fnap.setPermissions(ta.permissions);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return ta.objectId;
	}
	
	public String getWorkflowAccessPolicy(String docId, FNObjectStore os) throws Exception {
		try {
			FNDocument fnd = FNDocument.getInstance(os);
			fnd.setId(docId);
			String apId = fnd.getAccessPolicy(5); // Workflow is 5
			if((apId != null) && (apId.trim().length() > 0))
				return apId;
			
			FNAccessPolicy fnap = FNAccessPolicy.getInstance(os, null);
			apId = fnap.createAccessPolicy(docId, docId, "WORKFLOW");
			fnd.setAccessPolicy(5, apId); // Workflow is always 5
			return apId;
		} catch (Exception e) {
			throw e;
		}
	}
	
	public String setNewWorkflowAccessPolicy(String docId, FNObjectStore os) throws Exception {
		try {
			ArrayList<TPermission> tpList = null;
			FNDocument fnd = FNDocument.getInstance(os);
			fnd.setId(docId);
			String apId = fnd.getAccessPolicy(5); // Workflow is 5
			if((apId != null) && (apId.trim().length() > 0))
			{
				FNAccessPolicy fna = FNAccessPolicy.getInstance(os, null);
				fna.setId(apId);
				tpList =  fna.getPermissions();
			}
			
			FNAccessPolicy fnap = FNAccessPolicy.getInstance(os, null);
			apId = fnap.createAccessPolicy(docId, docId, "WORKFLOW");
		
			fnap.setId(apId);
			fnap.setAPPermissions(tpList);
			
			fnd.setAccessPolicy(5, apId); // Workflow is always 5
			return apId;
		} catch (Exception e) {
			throw e;
		}
	}
	
	public String getAdhocAccessPolicy(String docId, FNObjectStore os) throws Exception {
		try {
			FNDocument fnd = FNDocument.getInstance(os);
			fnd.setId(docId);
			String apId = fnd.getAccessPolicy(3); // Ad-hoc is 3
			if((apId != null) && (apId.trim().length() > 0))
				return apId;
			
			FNAccessPolicy fnap = FNAccessPolicy.getInstance(os, null);
			apId = fnap.createAccessPolicy(docId, docId, "ADHOC");
			fnd.setAccessPolicy(3, apId); // Ad-hoc is always 3
			return apId;
		} catch (Exception e) {
			throw e;
		}
	}
	
	public void setPermissions(TAccessPolicy ta, FNObjectStore os) throws Exception {
		FNAccessPolicy fnap = FNAccessPolicy.getInstance(os, null);
		fnap.setId(ta.objectId);
		fnap.setPermissions(ta.permissions);
		ECMMailManager em=new ECMMailManager();
		TAdminEmailSet taes=new TAdminEmailSet();
		taes.setPermissions(ta.permissions);
		em.sendEmail("AP", taes, "Updated");
	}
	
	public void setPermissions(String apID, ArrayList<TPermission> tps, FNObjectStore os) throws Exception {
		FNAccessPolicy fnap = FNAccessPolicy.getInstance(os, null);
		fnap.setId(apID);
		fnap.setPermissions(tps);
	}
	
	private void addUsersToAccessPolicy(ArrayList<TRecipient> recipients,
			String apId, FNObjectStore os, String actions, long roleId)
			throws Exception {
		ArrayList<TPermission> tpList = new ArrayList<TPermission>();
		FNAccessPolicy fna = FNAccessPolicy.getInstance(os, null);
		fna.setId(apId);
		String principal = null;
		String granteeType = "USER";
		for (TRecipient tr : recipients) {
			if (tr.id <= 0)
				continue;

			principal = null;
			granteeType = "USER";
			if (tr.userType.equalsIgnoreCase("ROLE")) {
				principal = ECMRoleList.getInstance().getADGroup(tr.id);
				granteeType = "GROUP";
			} else
				principal = ECMUserList.getInstance().getLoginName(tr.id);

			if (principal != null) {
				// if(actions.equalsIgnoreCase("signature") ||
				// actions.equalsIgnoreCase("initial")) {
				// Owner 393687
				// Author 131543
				// Author+Unlink 131575
				// Viewer 131201
				// Viewer+Link+Annotate 131217
				// Viewer+Link+Annotate+Unlink 131249
				// Full Control 983511
				TPermission tp = new TPermission();
				tp.accessType = "ALLOW";
				tp.accessMask = 131575;
				tp.granteeType = granteeType;
				tp.granteeName = principal;
				tp.inheritDepth = -3; // Immediate children not this object
				tp.action = "ADD";
				tpList.add(tp);
				/*
				 * }else{ TPermission tp = new TPermission(); tp.accessType =
				 * "ALLOW"; tp.accessMask = 131249; // Viewer tp.granteeType =
				 * granteeType; tp.granteeName = principal; tp.inheritDepth =
				 * -3; // Immediate children not this object tp.action = "ADD";
				 * tpList.add(tp); }
				 */
			}
		}
		
		if (roleId > 0) {
			principal = ECMRoleList.getInstance().getADGroup(roleId);
			granteeType = "GROUP";
			
			if (principal != null) {
				TPermission tp = new TPermission();
				tp.accessType = "ALLOW";
				tp.accessMask = 131575;
				tp.granteeType = granteeType;
				tp.granteeName = principal;
				tp.inheritDepth = -3; // Immediate children not this object
				tp.action = "ADD";
				tpList.add(tp);
			}
		} 
		
		if (tpList.size() > 0)
			fna.setPermissions(tpList);
	}
	
	public void setWorkflowAccessPolicy(TWorkitemAction twa, FNObjectStore os) throws Exception {
		if((twa == null) || (twa.attachments == null))
			throw new Exception("No attachments for workflow!");
		for(TAttachment ta:twa.attachments) {
			String apId = getWorkflowAccessPolicy(ta.docId, os);
			if(twa.recipients == null)
				throw new Exception("No recipients for workflow!");
			
			addUsersToAccessPolicy(twa.recipients, apId, os, twa.actions, twa.roleId);
		} 
	}
	
	public void recallWorkflowSecurity(ArrayList<TDocPrincipal> docList, FNObjectStore os) throws Exception {
		if((docList == null) || (docList.size() <= 0))
			return;
		
		for(TDocPrincipal tdp:docList) {
			if(tdp.isAnnotOnly != null && tdp.isAnnotOnly.equalsIgnoreCase("N"))
			{
				FNAccessPolicy fna = FNAccessPolicy.getInstance(os, null);
				fna.setId(getWorkflowAccessPolicy(tdp.docId, os));
				String principal = null;
				String granteeType = "USER";
				if(tdp.pType.equalsIgnoreCase("ROLE")) {
					principal = ECMRoleList.getInstance().getADGroup(tdp.principal);
					granteeType = "GROUP";
				} else
					principal = ECMUserList.getInstance().getLoginName(tdp.principal);
				
				ArrayList<TPermission> tpList = new ArrayList<TPermission>();
				if(principal != null)
				{		
					TPermission tp = new TPermission();
					tp.accessType = "ALLOW";
					tp.accessMask = 131575; // Viewer
					tp.granteeType = granteeType;
					tp.granteeName = principal;
					tp.inheritDepth = -3; // All children not this object
					tp.action = "REMOVE";
					tpList.add(tp);
				}
				
				if(tpList.size() > 0)
					fna.setPermissions(tpList);
			}
		} 
	}
	
	public void setPermissionFolderSecurity(FNDocument fnd, FNObjectStore os) throws Exception{		
		ArrayList<FNFolder> folders = fnd.getFoldersFiledIn();
		for(int i=0; i< folders.size(); i++) {
			if(!(folders.get(i).getPath().startsWith("/ECMCart")) && 
					folders.get(i).getClassName().equalsIgnoreCase("permissionfolder")) 
				setFolderAccessPolicy(fnd.getId(), folders.get(i).getId(), os);
		}
	}
	
	public String setFolderAccessPolicy(String docId, String folderId, FNObjectStore os) throws Exception {
		try {
			FNDocument fnd = FNDocument.getInstance(os);
			fnd.setId(docId);
			String apId = fnd.getAccessPolicy(6); // PermissionFolder is 6
			if((apId != null) && (apId.trim().length() > 0))
				return apId;
			
			FNFolder fnf = FNFolder.getInstance(os);
			fnf.setId(folderId);
			apId = fnf.getAccessPolicy(6);
			
			fnd.setAccessPolicy(6, apId); // PermissionFolder is always 6
			return apId;
		} catch (Exception e) {
			throw e;
		}
	}
}
