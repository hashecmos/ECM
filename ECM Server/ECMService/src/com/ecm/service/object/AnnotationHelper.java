package com.ecm.service.object;

import java.util.ArrayList;

import com.ecm.db.list.ECMRoleList;
import com.ecm.db.list.ECMUserList;
import com.ecm.db.transport.TAttachment;
import com.ecm.db.transport.TDocPrincipal;
import com.ecm.db.transport.TPermission;
import com.ecm.db.transport.TRecipient;
import com.ecm.db.transport.TWorkitemAction;
import com.ecm.db.util.ECMLogger;
import com.ecm.filenet.model.FNAnnotation;
import com.ecm.filenet.model.FNObjectStore;

public class AnnotationHelper {
	private static ECMLogger logger = ECMLogger.getInstance(AnnotationHelper.class);
	
	public static AnnotationHelper getInstance() { return new AnnotationHelper(); }
	private AnnotationHelper() {}
	
	public void setAnnotation(TWorkitemAction twa, FNObjectStore os) throws Exception {
		if((twa == null) || (twa.attachments == null))
			throw new Exception("No attachments for workflow!");
		
		if(twa.recipients == null)
			throw new Exception("No recipients for workflow!");
		
		ArrayList<TPermission> tpList = getUserPermissionList(twa.recipients, twa.roleId);		
		for(TAttachment ta:twa.attachments) {		
			logger.info("Adding users to document annotations");
			try {
				addUsersToAnnotations(tpList, ta.docId, os);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
//	public void setAnnotation(TWorkitemAction twa, FNObjectStore os, FNPowerObjectStore pos) throws Exception {
//		if((twa == null) || (twa.attachments == null))
//			throw new Exception("No attachments for workflow!");
//		
//		if(twa.recipients == null)
//			throw new Exception("No recipients for workflow!");
//		
//		ArrayList<TPermission> tpList = getUserPermissionList(twa.recipients, twa.roleId);		
//		for(TAttachment ta:twa.attachments) {		
//			logger.info("Adding users to document annotations");
//			try {
//				addUsersToAnnotations(tpList, ta.docId, os, pos);
//			} catch (Exception e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
//	}
	
	
	private void addUsersToAnnotations(ArrayList<TPermission> tpList,
			String docId, FNObjectStore os) throws Exception {
		
		FNAnnotation fna = FNAnnotation.getInstance(os);
		fna.setDocId(docId);
		
		if (tpList.size() > 0)
		{
			fna.setPermissions(tpList);
			logger.info("Annotation permissions set");
		}
	}
	
//	private void addUsersToAnnotations(ArrayList<TPermission> tpList,
//			String docId, FNObjectStore os, FNPowerObjectStore pos) throws Exception {
//		
//		FNPowerAnnotation fna = FNPowerAnnotation.getInstance(pos, os);
//		fna.setDocId(docId);
//		
//		if (tpList.size() > 0)
//		{
//			fna.setPermissions(tpList);
//			logger.info("Annotation permissions set");
//		}
//	}
	
	private ArrayList<TPermission> getUserPermissionList(ArrayList<TRecipient> recipients, long roleId) {
		ArrayList<TPermission> tpList = null;
		try {
			String principal = null;
			String granteeType = "USER";
			
			tpList = new ArrayList<TPermission>();
			for (TRecipient tr : recipients) {
				if (tr.id <= 0)
					continue;
				
				if (tr.userType.equalsIgnoreCase("ROLE")) {
					principal = ECMRoleList.getInstance().getADGroup(tr.id);
					granteeType = "GROUP";
				} else
					principal = ECMUserList.getInstance().getLoginName(tr.id);

				//Viewer+Modify Permissions 131201
				//Modify Properties 132563|131459
				//Modify All - 394707
				//Owner 998871
				//Full Control 983427
				if (principal != null) {
					TPermission tp = new TPermission();
					tp.accessType = "ALLOW";
					tp.accessMask = 394707;
					tp.granteeType = granteeType;
					tp.granteeName = principal;
					tp.inheritDepth = 0; // This object only
					tp.action = "ADD";
					tpList.add(tp);
				}
			}

			if (roleId > 0) {
				principal = ECMRoleList.getInstance().getADGroup(roleId);
				granteeType = "GROUP";
				
				if (principal != null) {
					TPermission tp = new TPermission();
					tp.accessType = "ALLOW";
					tp.accessMask = 983427;
					tp.granteeType = granteeType;
					tp.granteeName = principal;
					tp.inheritDepth = 0; // This object only
					tp.action = "ADD";
					tpList.add(tp);
				}
			} 
			return tpList;
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return tpList;
	}
	
	public void recallAnnotationSecurity(ArrayList<TDocPrincipal> docList, FNObjectStore os) throws Exception {
		if((docList == null) || (docList.size() <= 0))
			return;
		
		for(TDocPrincipal tdp:docList) {
			FNAnnotation fna = FNAnnotation.getInstance(os);
			fna.setDocId(tdp.docId);
			String principal = null;
			String granteeType = "USER";
			int accessMask = 394707;
			if(tdp.pType.equalsIgnoreCase("ROLE")) {
				principal = ECMRoleList.getInstance().getADGroup(tdp.principal);
				granteeType = "GROUP";
				accessMask = 983427;
			} else
				principal = ECMUserList.getInstance().getLoginName(tdp.principal);
			
			ArrayList<TPermission> tpList = new ArrayList<TPermission>();
			TPermission tp = new TPermission();
			tp.accessType = "ALLOW";
			tp.accessMask = accessMask;
			tp.granteeType = granteeType;
			tp.granteeName = principal;
			tp.inheritDepth = 0; // This object only
			tp.action = "REMOVE";
			tpList.add(tp);
			
			fna.setPermissions(tpList);
			
		} 
	}

	
}
