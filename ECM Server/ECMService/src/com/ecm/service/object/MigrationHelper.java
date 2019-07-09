package com.ecm.service.object;

import java.util.ArrayList;

import com.ecm.db.list.ECMAccessPolicyList;
import com.ecm.db.transport.TDefaultAccessPolicy;
import com.ecm.db.transport.TDocPermissions;
import com.ecm.db.transport.TPermission;
import com.ecm.db.util.ECMLogger;
import com.ecm.filenet.list.FNDocumentSearch;
import com.ecm.filenet.model.FNCustomObject;
import com.ecm.filenet.model.FNDocument;
import com.ecm.filenet.model.FNFolder;
import com.ecm.filenet.model.FNObjectStore;

public class MigrationHelper {
	private static ECMLogger logger = ECMLogger.getInstance(MigrationHelper.class);
	
	public static MigrationHelper getInstance() { return new MigrationHelper(); }
	private MigrationHelper() {}
	
	public void convertOrgDocuments(FNObjectStore os, String oldOrgCode, String newOrgCode) throws Exception {
		ArrayList<String> docList = FNDocumentSearch.getInstance(os).getOrgDocumentIDs(oldOrgCode);
		
		for(String docId:docList) {
			FNDocument fnd = FNDocument.getInstance(os);
			fnd.setId(docId);
			
			setPermissionFolderPermissions(fnd);
			setOrgcodePermissions(fnd, oldOrgCode, newOrgCode);
		}
	}
	
	private String getOrgAccessPolicy(String etVSId, String orgCode) {
		try {
			ArrayList<TDefaultAccessPolicy> defList = ECMAccessPolicyList.getInstance().getOrgAccessPolicies(orgCode, etVSId);
			if((defList != null) && (defList.size() > 0))
				return defList.get(0).policy4;

		} catch (Exception e) {
			logger.error("getOrgAccesspolicy : " + e.getMessage());
		}
		return null;
	}
	
	private void setOrgcodePermissions(FNDocument fnd, String oldOrgCode, String newOrgCode) throws Exception {
		String etVSId = fnd.getEntryTemplateVSID();
		if(etVSId == null)
			return;
		String ap4 = fnd.getAccessPolicy(4);
		String ap3 = fnd.getAccessPolicy(3);
		if((ap4 != null) || (ap3 != null))
			return;
		ArrayList<TPermission> dPermList = fnd.getPermissions();
		if(dPermList == null)
			return;
		
		String apId = getOrgAccessPolicy(etVSId, oldOrgCode);
		FNCustomObject fnap = FNCustomObject.getInstance(fnd.getOs());
		fnap.setId(apId);
		
		ArrayList<TPermission> removeList = new ArrayList<TPermission>();
		for(TPermission perm: dPermList) {
			TPermission rPerm = getRemovablePermission(perm, fnap.getPermissions());
			if(rPerm != null)
				removeList.add(rPerm);
		}
		
		fnd.setPermissions(removeList);
		String newApId = getOrgAccessPolicy(etVSId, newOrgCode);
		fnd.setAccessPolicy(4, newApId);
	}
	
	private void setPermissionFolderPermissions(FNDocument fnd) throws Exception {
		FNFolder pFolder = getPermissionFolder(fnd);
		if(pFolder == null)
			return;
	
		ArrayList<TPermission> dPermList = fnd.getPermissions();
		if(dPermList == null)
			return;
		
		ArrayList<TPermission> removeList = new ArrayList<TPermission>();
		for(TPermission perm: dPermList) {
			TPermission rPerm = getRemovablePermission(perm, pFolder.getPermissions());
			if(rPerm != null)
				removeList.add(rPerm);
		}
		
		fnd.setPermissions(removeList);
		fnd.setSecurityParent(pFolder);
	}
	
	private TPermission getRemovablePermission(TPermission dp, ArrayList<TPermission> pPermList) throws Exception {
		try {
			if(!dp.permissionSource.equalsIgnoreCase("DIRECT") && 
					!dp.permissionSource.equalsIgnoreCase("DEFAULT"))
				return null;
			for(TPermission pp: pPermList) {
				if((pp.accessMask == dp.accessMask) &&
						(pp.accessType.equalsIgnoreCase(dp.accessType)) &&
						(pp.granteeName.equalsIgnoreCase(dp.granteeName)) &&
						(pp.granteeType.equalsIgnoreCase(dp.granteeType)) &&
						(pp.inheritDepth != 0)) {
					pp.action = "REMOVE";
					return pp;
				}
			}
		} catch (Exception e) {
			logger.error("getRemovablePermission: " + e.getMessage());
		}
		return null;
	}
	
	private FNFolder getPermissionFolder(FNDocument fnd) throws Exception {
		try {
			ArrayList<FNFolder> fList = fnd.getFoldersFiledIn();
			if(fList == null)
				return null;
			
			for(FNFolder fnf: fList) {
				if(fnf.getClassName().trim().equalsIgnoreCase("PermissionsFolder"))
					return fnf;
			}
		} catch (Exception e) {
		}
		return null;
	}
	
	public String setMigCategoryBasedPermissions(TDocPermissions tdp, long empNo, String category, FNObjectStore os) throws Exception{
		String retMsg = "Failed";

		//TFNDocument tfnd = fnd.getDocumentTransportObject();
		FNDocument fnd = FNDocument.getInstance(os);
		String sDocId = tdp.id;
		fnd.setId(sDocId);
		fnd.setEmpNo(empNo);
		
		if(category.equalsIgnoreCase("cat-x"))
		{
			String apId = AccessPolicyHelper.getInstance().getAdhocAccessPolicy(sDocId, os);
			//fnd.setAdhocAccessPolicyValue(apId);
			AccessPolicyHelper.getInstance().setPermissions(apId, tdp.permissions, os);
	    	retMsg = "OK";
		}
		else if(category.equalsIgnoreCase("cat-a") || category.equalsIgnoreCase("cat-b"))
		{
			fnd.setMigratedDocSecurity();	
		}
		else if(category.equalsIgnoreCase("cat-c"))
		{
			setMigPermFolderSecurity(fnd, os);		
		}
		else if(category.equalsIgnoreCase("cat-d"))
		{
			setMigWorkflowAccessPolicy(sDocId, os, tdp.permissions);
		}
		else if(category.equalsIgnoreCase("cat-e") || category.equalsIgnoreCase("cat-f"))
		{
			fnd.setSecurityAndTemplate();
			setMigPermFolderSecurity(fnd, os);	
		}
		else if(category.equalsIgnoreCase("cat-g") || category.equalsIgnoreCase("cat-h"))
		{
			fnd.setSecurityAndTemplate();
			setMigWorkflowAccessPolicy(sDocId, os, tdp.permissions);	
		}
		else if(category.equalsIgnoreCase("cat-k") || category.equalsIgnoreCase("cat-l"))
		{
			fnd.setSecurityAndTemplate();
			setMigPermFolderSecurity(fnd, os);
			setMigWorkflowAccessPolicy(sDocId, os, tdp.permissions);
		}	
		return retMsg;
	}
	
	public void setMigPermFolderSecurity(FNDocument fnd, FNObjectStore os) throws Exception{		
		ArrayList<FNFolder> folders = fnd.getFoldersFiledIn();
		for(int i=0; i< folders.size(); i++) {
			if(!(folders.get(i).getPath().startsWith("/ECMCart")) && 
					folders.get(i).getClassName().equalsIgnoreCase("permissionsfolder")) 
				AccessPolicyHelper.getInstance().setFolderAccessPolicy(fnd.getId(), folders.get(i).getId(), os);
		}
	}
	
	public void setMigWorkflowAccessPolicy(String docId, FNObjectStore os, ArrayList<TPermission> tpList) throws Exception {
		String apId = AccessPolicyHelper.getInstance().getWorkflowAccessPolicy(docId, os);
		AccessPolicyHelper.getInstance().setPermissions(apId, tpList, os);
	}
	
}
