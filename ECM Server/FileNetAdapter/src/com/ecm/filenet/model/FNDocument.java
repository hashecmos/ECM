package com.ecm.filenet.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URLConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;




//import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.log4j.Logger;

import com.ecm.db.list.ECMAccessPolicyList;
import com.ecm.db.list.ECMAdministrationList;
import com.ecm.db.list.ECMConfigurationList;
import com.ecm.db.model.ECMAttachment;
import com.ecm.db.transport.TDefaultAccessPolicy;
import com.ecm.db.transport.TPermission;
import com.ecm.db.util.DBUtil;
import com.ecm.filenet.list.FNDocumentSearch;
import com.ecm.filenet.list.FNEntryTemplateList;
import com.ecm.filenet.transport.TFNClass;
import com.ecm.filenet.transport.TFNDocument;
import com.ecm.filenet.transport.TFNFolder;
import com.ecm.filenet.transport.TFNProperty;
import com.ecm.filenet.util.Utils;
import com.filenet.api.collection.AccessPermissionList;
import com.filenet.api.collection.ContentElementList;
import com.filenet.api.constants.AccessType;
import com.filenet.api.constants.AutoClassify;
import com.filenet.api.constants.AutoUniqueName;
import com.filenet.api.constants.CheckinType;
import com.filenet.api.constants.DefineSecurityParentage;
import com.filenet.api.constants.PermissionSource;
import com.filenet.api.constants.RefreshMode;
import com.filenet.api.constants.ReservationType;
import com.filenet.api.constants.SecurityPrincipalType;
import com.filenet.api.constants.VersionStatus;
import com.filenet.api.core.BatchItemHandle;
import com.filenet.api.core.ContentElement;
import com.filenet.api.core.ContentTransfer;
import com.filenet.api.core.CustomObject;
import com.filenet.api.core.Document;
import com.filenet.api.core.Factory;
import com.filenet.api.core.Link;
import com.filenet.api.core.Folder;
import com.filenet.api.core.ReferentialContainmentRelationship;
import com.filenet.api.core.UpdatingBatch;
import com.filenet.api.core.VersionSeries;
import com.filenet.api.property.FilterElement;
import com.filenet.api.property.Properties;
import com.filenet.api.property.Property;
import com.filenet.api.property.PropertyFilter;
import com.filenet.api.security.AccessPermission;
import com.filenet.api.util.Id;

public class FNDocument extends FNObject
{
	protected static final Logger logger = Logger.getLogger(FNDocument.class);
	
	protected FNObjectStore fnOS = null;
	protected FNDocumentClass fnClass = null;
	protected Document document = null;
	protected String vsid;
	protected String creator;
	protected String modifier;
	protected Date createdOn;
	protected Date modifiedOn;
	protected double size;
	protected String docClass;
	protected String mimeType;
	protected String versionNo;
	protected String checkinType = "MAJOR";
	protected InputStream inputStream = null;
	protected String fileName;
	protected FNFolder folder = null;
	protected ArrayList<TPermission> permissions = null;
	protected ArrayList<String> accessPolicies = null;
	protected String entryTemplateId = null;
	protected String entryTemplateName = null;
	protected long empNo = 0;
	protected TFNClass tfnClass = null;
	
	public static FNDocument getInstance(FNObjectStore os){
		return new FNDocument(os);
	}
	
	protected FNDocument(FNObjectStore os){
		fnOS = os;
	}
	
	public FNObjectStore getOs() {
		return fnOS;
	}

	public void setOs(FNObjectStore os) {
		this.fnOS = os;
	}
	
	public String getDocClass() {
		return docClass;
	}

	public void setDocClass(String docClass) {
		this.docClass = docClass;
	}
	
	public String getVsid() {
		return vsid;
	}
	
	public void setVsid(String vsid) {
		this.vsid = vsid;
	}
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public void setEmpNo(long empno) {
		this.empNo = empno;
	}
	public String getCreator() { return creator; }
	public String getModifier() { return modifier; }
	public String getMimeType() { return mimeType; }
	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}
	public Date getCreatedDate() { return createdOn; }
	public Date getModifiedDate() { return modifiedOn; }
	public double getContentSize() { return size; }
	public String getVersionNo() { return versionNo; }
	public void setInputStream(InputStream iStream) { inputStream = iStream; }
	public FNDocumentClass getDocumentClass() { return fnClass; }
	public void setDocumentClass(FNDocumentClass fnc) { fnClass = fnc; }
	public void setFileName(String fName) { fileName = fName; }
	public void setFolder(FNFolder fileFolder) { folder = fileFolder; }
	
	public InputStream getInputStream() {
		return inputStream;
	}
	
	public ArrayList<TFNProperty> getProperties() throws Exception {
		load();
		return fnClass.getPropertiesTransport();
	}
	
	public ArrayList<TPermission> getPermissions() throws Exception {
		load();
		return permissions;
	}
	
	public ArrayList<FNFolder> getFoldersFiledIn() throws Exception
	{
		ArrayList<FNFolder> fList = new ArrayList<FNFolder>();
		load();
		Iterator iter = document.get_FoldersFiledIn().iterator();
		while(iter.hasNext()) {
			Folder df = (Folder)iter.next();
			FNFolder fnf = FNFolder.getInstance(fnOS);
			fnf.setId(df.get_Id().toString());
			fnf.setPath(df.get_PathName());
			fnf.setName(df.get_Name());
			fnf.setClassName(df.get_ClassDescription().get_SymbolicName());
			fnf.setFolder(df);
			fList.add(fnf);
		}
		return fList;
	}
	
	public Boolean isFiledIn(String fId) throws Exception
	{
		try {
			load();
			Iterator iter = document.get_FoldersFiledIn().iterator();
			while(iter.hasNext()) {
				Folder df = (Folder)iter.next();
				
				if(fId.equalsIgnoreCase(df.get_Id().toString()))
					return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	
	private void getObjectPermissions(Document doc)
	{
		if(permissions == null)
			permissions = new ArrayList<TPermission>();
		Iterator iter = doc.get_Permissions().iterator();
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
	
	private void getDocumentProperties(Document doc)
	{
		if(fnClass == null)
			fnClass = FNDocumentClass.getInstance(fnOS);
		fnClass.setId(doc.get_ClassDescription().get_Id().toString());
		fnClass.setSymbolicName(doc.get_ClassDescription().get_SymbolicName());
		fnClass.setName(doc.get_ClassDescription().get_Name());
		
		Iterator iter = doc.getProperties().iterator();
		while(iter.hasNext()) {
			try {
				fnClass.setPropertyValue((Property)iter.next());
			} catch (Exception e) {	}
		}
		
		try {
			this.entryTemplateId = doc.getProperties().getIdValue("EntryTemplateId").toString();
			FNEntryTemplate fnet = FNEntryTemplate.getInstance(fnOS);
			fnet.id = this.entryTemplateId;
			this.entryTemplateName = fnet.getEntryTemplateName();
			fnet.name = this.entryTemplateName;
			this.tfnClass = fnet.getTransport(true);
			setTemplateValues(this.tfnClass, this.fnClass);
		} catch (Exception e) {}
		
		this.accessPolicies = new ArrayList<String>();
		for(int i=0; i <4; i++) {
			try {
				String apId = doc.getProperties().getIdValue("AccessPolicy" + i).toString();
				if((apId != null) && (apId.trim().length() > 0))
					this.accessPolicies.add(apId);
			} catch (Exception e) {}
		}
	}
	
	private void setTemplateValues(TFNClass tfnc, FNClass fnc) {
		for(FNProperty fnp: fnc.properties) {
			for(TFNProperty tfnp:tfnc.props) {
				if(tfnp.symName.equalsIgnoreCase(fnp.getSymbolicName())) {
					tfnp.mvalues = new ArrayList<String>();
					if(fnp.getCardinality().equalsIgnoreCase("MULTI")) {
						tfnp.mvalues.addAll(fnp.getPropertyMultiValues());
						tfnp.mtype = "Y";
					} else {
						tfnp.mvalues.add(fnp.getValue());
						tfnp.mtype = "N";
					}
					break;
				}
			}
			// Add System Properties Here
		}
	}
	
	private ArrayList<TFNProperty> convertPropertiesToEntryTemplate(ArrayList<TFNProperty>transport) throws Exception {
		try {
			if((this.entryTemplateId == null) || (this.entryTemplateId.trim().length() <= 0))
				return transport;
			
			FNEntryTemplate et = FNEntryTemplate.getInstance(this.fnOS);
			et.setId(this.entryTemplateId);
			et.setEmployeeNo(this.empNo);
			TFNClass classET = et.getTransport(true);
			
			for(TFNProperty etProp: classET.props) {
				for(TFNProperty prop:transport) {
					if(prop.name.equalsIgnoreCase(etProp.name)) {
						etProp.mvalues = prop.mvalues;
						break;
					}
				}
			}
			return classET.props;
		} catch(Exception e) {
		}
		return transport;
	}
	
	public String getDocumentTitle() {
		try {
			if(document == null)
				load();
			
			return document.get_Name();
			
		} catch(Exception e) {}
		return null;
	}
	
	private void setDocumentInfo(Document doc)
	{
		try {
			vsid = doc.get_VersionSeries().get_Id().toString();
			id = doc.get_Id().toString();
			creator = doc.get_Creator();
			modifier = doc.get_LastModifier();
			createdOn = doc.get_DateCreated();
			modifiedOn = doc.get_DateLastModified();
			mimeType = doc.get_MimeType();
			if((mimeType == null) || (mimeType.trim().length() <= 0))
				mimeType = ((Document)doc.get_CurrentVersion()).get_MimeType();
			
			versionNo = "0";
			if(doc.get_MajorVersionNumber() != null)
				versionNo = doc.get_MajorVersionNumber().toString();
			if(doc.get_MinorVersionNumber() != null)
				versionNo += ("." + doc.get_MinorVersionNumber().toString());
			name = doc.get_Name();
			size = doc.get_ContentSize();
			docClass = doc.get_ClassDescription().get_DisplayName();
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
	}
	
	public void loadFromVersionSeries(String vsID) throws Exception {
		try {
			VersionSeries vs = Factory.VersionSeries.fetchInstance(getOs().getObjectStore(), new Id(id), null);
			document = (Document)vs.get_CurrentVersion();
			id = document.get_Id().toString();
			setDocumentInfo(document);
			getDocumentProperties(document);
			getObjectPermissions(document);
		} catch (Exception e) {
			throw new Exception(e.getMessage());
		}
	}
	
	public void load() throws Exception {
		try {
			if(document != null)
				return;	
			if((this.id == null) || (this.id.length() <= 0))
				return;

			document = Factory.Document.fetchInstance(getOs().getObjectStore(), new Id(id), null);

			setDocumentInfo(document);
			getDocumentProperties(document);
			getObjectPermissions(document);
		} catch (Exception e) {
			throw new Exception(e.getMessage());
		}
	}
	
	public void loadDocOnly() throws Exception {
		try {
			if(document != null)
				return;	
			if((this.id == null) || (this.id.length() <= 0))
				return;

			document = Factory.Document.fetchInstance(getOs().getObjectStore(), new Id(id), null);

			setDocumentInfo(document);
			//getDocumentProperties(document);
			//getObjectPermissions(document);
		} catch (Exception e) {
			throw new Exception(e.getMessage());
		}
	}
	
	public void loadCurrentVersion() throws Exception {
		try {
			if((this.id == null) || (this.id.length() <= 0))
				return;

			Document doc = Factory.Document.fetchInstance(getOs().getObjectStore(), new Id(id), null);
			document = (Document)doc.get_CurrentVersion();
			setDocumentInfo(document);
			getDocumentProperties(document);
			getObjectPermissions(document);
		} catch (Exception e) {
			throw new Exception(e.getMessage());
		}
	}
	
	public void loadCurrentVersionOnly() throws Exception {
		try {
			if((this.id == null) || (this.id.length() <= 0))
				return;

			Document doc = Factory.Document.fetchInstance(getOs().getObjectStore(), new Id(id), null);
			document = (Document)doc.get_CurrentVersion();
			setDocumentInfo(document);
			//getDocumentProperties(document);
			//getObjectPermissions(document);
		} catch (Exception e) {
			throw new Exception(e.getMessage());
		}
	}
	
	public String getAccessPolicy(int seqNo) {
		try {
			if(document == null)
				document = Factory.Document.fetchInstance(getOs().getObjectStore(), new Id(id), null);
			if((seqNo < 1) || (seqNo > 5))
				return null;
			Property p = document.getProperties().find("AccessPolicy" + seqNo);
			return p.getIdValue().toString();
		} catch (Exception e) {
			return null;
		}
	}
	
	public void addLink(String docId) throws Exception {
		try {
			
			if(document == null)
				document = Factory.Document.fetchInstance(getOs().getObjectStore(), new Id(id), null);			
			String docLink1 = document.get_Name();
			
			Document secDoc = Factory.Document.fetchInstance(getOs().getObjectStore(), new Id(docId), null);
			String docLink2 = secDoc.get_Name();
			
			if(FNDocumentSearch.getInstance(getOs()).getLinkId(this.id, docId) == null) {
				Link link1 = Factory.Link.createInstance(getOs().getObjectStore(), "KOCLink");
				link1.set_Head(document);
				link1.set_Tail(secDoc);
				link1.getProperties().putValue("Description", docLink2);
				link1.save(RefreshMode.REFRESH);
			}
			
			if(FNDocumentSearch.getInstance(getOs()).getLinkId(docId, this.id) == null) {
				Link link2 = Factory.Link.createInstance(getOs().getObjectStore(), "KOCLink");
				link2.set_Head(secDoc);
				link2.set_Tail(document);
				link2.getProperties().putValue("Description", docLink1);
				link2.save(RefreshMode.REFRESH);
			}
		} catch (Exception e) {
			throw e;
		}
	}
	
	public void removeLink(String docId) throws Exception {
		try {
			String link1 = FNDocumentSearch.getInstance(getOs()).getLinkId(this.id, docId);
			if(link1 != null) {
				Link fLink = Factory.Link.getInstance(getOs().getObjectStore(), "KOCLink", new Id(link1));
				if(fLink != null) {
					fLink.delete();
					fLink.save(RefreshMode.REFRESH);
				}
			}
			String link2 = FNDocumentSearch.getInstance(getOs()).getLinkId(docId, this.id);
			if(link2 != null) {
				Link sLink = Factory.Link.getInstance(getOs().getObjectStore(), "KOCLink", new Id(link2));
				if(sLink != null) {
					sLink.delete();
					sLink.save(RefreshMode.REFRESH);
				}
			}	
		} catch (Exception e) {
			
		}
	}
	public FNDocument getCurrentVersion() throws Exception
	{
		load();
		FNDocument cv = getInstance(fnOS);
		cv.setId(((Document)document.get_CurrentVersion()).get_Id().toString());
		cv.load();
		return cv;
	}
	public ArrayList<FNDocument> getVersions() throws Exception
	{
		load();
		ArrayList<FNDocument> vList =  new ArrayList<FNDocument>();
		Iterator iter = document.get_Versions().iterator();
		while(iter.hasNext()) {
			Document vdoc = (Document)iter.next();
			FNDocument vfnDoc = FNDocument.getInstance(fnOS);
			vfnDoc.setDocumentInfo(vdoc);
			vfnDoc.document = vdoc;
			vList.add(vfnDoc);
		}
		return vList;
	}
	
	private void setProperties(Properties dProps) 
	{
		ArrayList<FNProperty> pList = fnClass.getProperties();
		for(int i=0; i<pList.size(); i++) {
			pList.get(i).putPropertyValue(dProps);		
		}
	}
	
	private void setEntryTemplateProperty(Properties dProps) {
		//String ecmNo = ECMAdministrationList.getInstance().getNextECMNo();
		if((this.entryTemplateId != null) && (this.entryTemplateId.length() > 0)) {
			dProps.putValue("EntryTemplateId", new Id(this.entryTemplateId));
			dProps.putValue("EntryTemplateObjectStoreName", fnOS.symbolicName);
		}
	}
	
	private void setECMNoProperty(Properties dProps) {
		try {
			String ecmNo = ECMAdministrationList.getInstance().getNextECMNo();
			if((this.entryTemplateId != null) && (this.entryTemplateId.length() > 0)) {
				dProps.putValue("ECMNo", ecmNo);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void setAccessPolicyProperties(Properties dProps) throws Exception {
		if(this.accessPolicies != null) {
			int i=1;
			for(String ap: this.accessPolicies) {
				if(ap == null)
					continue;
				String apName = "AccessPolicy" + i;
				CustomObject apObj = Factory.CustomObject.fetchInstance(fnOS.getObjectStore(), new Id(ap), null);
				if(apObj != null)
					dProps.putObjectValue(apName, apObj);
				i++;
				if(i > 3) // Maximum 3 Access policies. 5th one is reserved for Workflows
					break;
			}
		}
	}
	
	public String getEntryTemplateVSID() {
		try {
			if((this.entryTemplateId == null) || (this.entryTemplateId.length() <= 0))
			{
				if(document == null)
					document = Factory.Document.fetchInstance(fnOS.getObjectStore(), id, null);
				
				Id etId = document.getProperties().getIdValue("EntryTemplateId");
				if(etId != null)
					this.entryTemplateId = etId.toString();
			}
			Document doc = Factory.Document.fetchInstance(fnOS.getObjectStore(), this.entryTemplateId, null);
			return doc.get_VersionSeries().get_Id().toString();
		} catch (Exception e) {
			return null;
		}
	}
	
	private String setDefaultAccessPolicies(Properties dProps) throws Exception {
		String strReturnMsg = "Success";
		String etVSId = getEntryTemplateVSID();
		if(etVSId == null){
			strReturnMsg = "Invalid Entry Template";
			return strReturnMsg;
		}
		ArrayList<TDefaultAccessPolicy> defList = ECMAccessPolicyList.getInstance().getDefaultAccessPolicies(this.empNo, etVSId);
		if(defList == null){
			strReturnMsg = "No AccessPolicy Defined";
			return strReturnMsg;
		}
		
		try
		{
			for(TDefaultAccessPolicy dap: defList) {
				if((dap.propName != null) && (dap.propName.length() > 0)) {
					if(dProps.isPropertyPresent(dap.propName)) {
						String stringValue = dProps.getStringValue(dap.propName);
						if((stringValue != null) && (stringValue.equalsIgnoreCase(dap.propValue))) {
							if(setDefaultAccessPolicyValue(dap, dProps))
								break;
							else
								continue;
						}
					}
					continue;
				}
				setDefaultAccessPolicyValue(dap, dProps);
				break;
			}
		} catch (Exception e) {
			strReturnMsg = e.getMessage();
		}
		return strReturnMsg;
	}
	
	private String setOrgAccessPolicies(Properties dProps) throws Exception {
		String strReturnMsg = "Success";
		String etVSId = getEntryTemplateVSID();
		if(etVSId == null){
			strReturnMsg = "Invalid Entry Template";
			return strReturnMsg;
		}
		ArrayList<TDefaultAccessPolicy> defList = ECMAccessPolicyList.getInstance().getOrgAccessPolicies(dProps.getStringValue("OrgCode"), etVSId);
		if(defList == null){
			strReturnMsg = "No AccessPolicy Defined";
			return strReturnMsg;
		}
		
		try
		{
			for(TDefaultAccessPolicy dap: defList) {
				if((dap.propName != null) && (dap.propName.length() > 0)) {
					if(dProps.isPropertyPresent(dap.propName)) {
						String stringValue = dProps.getStringValue(dap.propName);
						if((stringValue != null) && (stringValue.equalsIgnoreCase(dap.propValue))) {
							if(setDefaultAccessPolicyValue(dap, dProps))
								break;
							else
								continue;
						}
					}
					continue;
				}
				if (setDefaultAccessPolicyValue(dap, dProps))
					break;
			}
		} catch (Exception e) {
			strReturnMsg = e.getMessage();
		}
		return strReturnMsg;
	}
	
	private String setMigDefaultAccessPolicies(Properties dProps) throws Exception {
		String strReturnMsg = "Success";
		String etVSId = getEntryTemplateVSID();
		if(etVSId == null){
			strReturnMsg = "Invalid Entry Template";
			return strReturnMsg;
		}
		
		String orgCode = "";
		if(dProps.isPropertyPresent("OrgCode")) {
			orgCode = dProps.getStringValue("OrgCode");
		}
		
		ArrayList<TDefaultAccessPolicy> defList = ECMAccessPolicyList.getInstance().getOrgDefaultAccessPolicies(orgCode, etVSId);
		if(defList == null){
			strReturnMsg = "No AccessPolicy Defined";
			return strReturnMsg;
		}
		
		try
		{
			for(TDefaultAccessPolicy dap: defList) {
				if((dap.propName != null) && (dap.propName.length() > 0)) {
					if(dProps.isPropertyPresent(dap.propName)) {
						String stringValue = dProps.getStringValue(dap.propName);
						if((stringValue != null) && (stringValue.equalsIgnoreCase(dap.propValue))) {
							if(setDefaultAccessPolicyValue(dap, dProps))
								break;
							else
								continue;
						}
					}
					continue;
				}
				setDefaultAccessPolicyValue(dap, dProps);
				break;
			}
		} catch (Exception e) {
			strReturnMsg = e.getMessage();
		}
		return strReturnMsg;
	}
	
	private boolean setDefaultAccessPolicyValue(TDefaultAccessPolicy dap, Properties dProps) throws Exception {
		String apID = dap.policy4;
		String policyProp = "AccessPolicy4";
		if((apID == null) || (apID.length() <= 0)) {
			apID = dap.policy2;
			policyProp = "AccessPolicy2";
		}
		if((apID == null) || (apID.length() <= 0))
			return false;
		CustomObject apObj = Factory.CustomObject.fetchInstance(fnOS.getObjectStore(), new Id(dap.policy4), null);
		if(apObj != null) {
			dProps.putObjectValue(policyProp, apObj);
			return true;
		}
		return false;
	}
	
	public boolean setAdhocAccessPolicyValue(String apID) throws Exception {
			
		if(document == null)
			document = Factory.Document.fetchInstance(fnOS.getObjectStore(), id, null);
		
		Properties dProps = this.document.getProperties();
		String policyProp = "AccessPolicy3";
		if((apID == null) || (apID.length() <= 0))
			return false;
		CustomObject apObj = Factory.CustomObject.fetchInstance(fnOS.getObjectStore(), new Id(apID), null);
		if(apObj != null) {
			dProps.putObjectValue(policyProp, apObj);
			return true;
		}
		return false;
	}
	
	private PropertyFilter getDocumentsPropertyFilter()
	{
		logger.info("Started Method : getDocumentsPropertyFilter");
		PropertyFilter pf = new PropertyFilter();
		FilterElement fe = new FilterElement(2, null, null, "SymbolicName Id DocumentTitle "
				+ "ClassDescription Name VersionSeries MimeType Creator DateCreated "
				+ "DateLastModified LastModifier IsReserved ContentSize MajorVersionNumber "
				+ "ContentElementsPresent Reservation FoldersFiledIn Versions PathName "
				+ "FolderName ContentElements ElementSequenceNumber RetrievalName "
				+ "ContentType DateCheckedIn DisplayName", null);
		pf.addIncludeProperty(fe);
		logger.info("Exit Method : getDocumentsPropertyFilter");
		return pf;
	}
	
	public void updateProperties() throws Exception {
		document = Factory.Document.fetchInstance(getOs().getObjectStore(), new Id(id), null);
		if(this.document == null)
			return;
		//Set the properties for the document
		Properties properties = document.getProperties();
		setProperties(properties);
		document.save(RefreshMode.REFRESH);
		
		ECMAttachment att = new ECMAttachment();
		att.update(id, id, this.getDocumentTitle(), this.getMimeType());
	}
	
	public void setAccessPolicy(int apNo, String apId) throws Exception {
		if((apNo <= 0) || (apNo > 6))
			throw new Exception("Access policy number is out of range");
		if((apId == null) || (apId.trim().length() <= 0))
			throw new Exception("Access policy provided in null or empty");
				
		String apString = "AccessPolicy" + apNo;
		document = Factory.Document.fetchInstance(fnOS.getObjectStore(), new Id(this.id), null);
		CustomObject apObj = null;
		if(!apId.trim().equalsIgnoreCase("$REMOVE"))
			apObj = Factory.CustomObject.fetchInstance(fnOS.getObjectStore(), new Id(apId), null);
		if((document != null) && (apObj != null)) {
			Properties props = document.getProperties();
			props.putObjectValue(apString, apObj);
			document.save(RefreshMode.REFRESH);
		}
	}
	
	public void unSetAccessPolicy(int apNo) throws Exception {
		if((apNo <= 0) || (apNo > 6))
			throw new Exception("Access policy number is out of range");
		
		String apString = "AccessPolicy" + apNo;
		document = Factory.Document.fetchInstance(fnOS.getObjectStore(), new Id(this.id), null);
		CustomObject apObj = null;	
		if(document != null) {
			Properties props = document.getProperties();
			props.putObjectValue(apString, apObj);
			document.save(RefreshMode.REFRESH);
		}
	}
	
	public String fileInFolder() throws Exception {
		load();
		try
		{
			ReferentialContainmentRelationship relationship = null;
			if (folder != null) {
				if(isFiledIn(folder.id))
					return "Exists";
				folder.load();
				if (folder.getFolder() != null ){
					relationship = folder.getFolder().file(this.document,AutoUniqueName.AUTO_UNIQUE,
							null,DefineSecurityParentage.DO_NOT_DEFINE_SECURITY_PARENTAGE);
					relationship.save(RefreshMode.REFRESH);
					if(folder.getClassName().equalsIgnoreCase("PermissionsFolder"))
					{
						//this.document.set_SecurityFolder(folder.getFolder());
						setFolderAccessPolicy(this.document.get_Id().toString(), folder.getId(), fnOS);
					}
					return "OK";
				} 
			}
			return "Failed";
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new Exception(e.getMessage());
		}
	}
	
	public String unfileFromFolder() throws Exception {
		load();
		ReferentialContainmentRelationship relationship = null;
		if (folder != null) {
			folder.load();
			if (folder.getFolder() != null ){
				relationship = folder.getFolder().unfile(this.document);
				relationship.save(RefreshMode.REFRESH);
				
				if(folder.getClassName().equalsIgnoreCase("PermissionsFolder"))
				{
					//this.document.set_SecurityFolder(null);
					unSetFolderAccessPolicy(this.document.get_Id().toString(), fnOS);
				}		
				return "OK";
			}
		}		
		return "Failed";
	}
	
	@SuppressWarnings("unchecked")
	public void addDocument() throws Exception 
	{
		logger.info("Started Method : addDocument");
		try 
		{
			ReferentialContainmentRelationship relationship = null;
			UpdatingBatch ub = UpdatingBatch.createUpdatingBatchInstance(
					fnOS.getObjectStore().get_Domain(), RefreshMode.REFRESH);
			Document doc = null;
			//For add document we are passing the document class name. In case of an entry template, document class Id is being passed.
			// That is handled in the exception condition. 
			doc = Factory.Document.createInstance(fnOS.getObjectStore(), fnClass.getSymbolicName());
			
			//Create the content element list and set the content, mimetype, attachment name
			ContentElementList ceList = Factory.ContentElement.createList();
			ContentTransfer content = Factory.ContentTransfer.createInstance();
			content.setCaptureSource(inputStream);
			content.set_RetrievalName(fileName);
			if((mimeType == null) || (mimeType.length() <= 0))
				mimeType = Utils.getMimeType(fileName);
			if((mimeType == null) || (mimeType.trim().length() <= 0))
				mimeType = URLConnection.guessContentTypeFromStream(inputStream);
			content.set_ContentType(mimeType);
			ceList.add(content);
			doc.set_ContentElements(ceList);
			
			//Set the properties for the document
			Properties properties = doc.getProperties();
			setProperties(properties);
			
			//setECMNoProperty(properties);
			setEntryTemplateProperty(properties);
			//setDefaultAccessPolicies(properties);
			setAccessPolicyProperties(properties);
			
			if(checkinType.equalsIgnoreCase("MAJOR"))
				doc.checkin(AutoClassify.AUTO_CLASSIFY,	CheckinType.MAJOR_VERSION);
			else
				doc.checkin(AutoClassify.AUTO_CLASSIFY,	CheckinType.MINOR_VERSION);
			
			//Create a batch operation and add the checkin operation 
			PropertyFilter pf=getDocumentsPropertyFilter();
			BatchItemHandle docHandle=ub.add(doc,pf );
			//String versionId=doc.get_VersionSeries().get_Id().toString();
			
			String folderClass = "";
			String folderId = "";
			//If the document is being filed in a folder, get the folder details, and file the document to the folder.
			if (folder != null) {
				folder.load();
				if (folder.getFolder() != null ){
					relationship = folder.getFolder().file(doc,AutoUniqueName.AUTO_UNIQUE,
							null,DefineSecurityParentage.DO_NOT_DEFINE_SECURITY_PARENTAGE);
					ub.add(relationship, null);
					
					folderClass = folder.getClassName();
					folderId = folder.getId();
				}
			}

			//Commit the batch
			ub.updateBatch();
			Document tempDoc=(Document) docHandle.getObject();
			id=tempDoc.get_Id().toString();
			
			
			if(folderClass.equalsIgnoreCase("PermissionsFolder") &&  folderId != "")
			{
				//this.document.set_SecurityFolder(folder.getFolder());
				setFolderAccessPolicy(id, folderId, fnOS);
				
			}
			
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new Exception(e.getMessage());
		}
		logger.info("Exit Method : addDocument");
	}
	
	@SuppressWarnings("unchecked")
	public void checkIn() throws Exception
	{
		String curDocId = this.id;
		Document curDoc = Factory.Document.fetchInstance(fnOS.getObjectStore(), new Id(this.id), null);
		if(curDoc.get_IsCurrentVersion().booleanValue()== false){
			curDoc = (Document)curDoc.get_CurrentVersion();
		}
		if (curDoc.get_IsReserved().booleanValue() == true )
		{
			if(curDoc.get_VersionStatus().getValue() != (VersionStatus.RESERVATION_AS_INT))
			{
				curDoc = (Document)curDoc.get_Reservation();
			}
			
			if(!curDoc.getClassName().equalsIgnoreCase(fnClass.getSymbolicName()))
				curDoc.changeClass(fnClass.symbolicName);
			
			setProperties(curDoc.getProperties());
			
			ContentElementList contentList = Factory.ContentElement.createList();
			com.filenet.api.core.ContentTransfer content1 = Factory.ContentTransfer.createInstance();
			content1.setCaptureSource(inputStream);
			content1.set_RetrievalName(fileName);
			if((mimeType == null) || (mimeType.length() <= 0))
				mimeType = Utils.getMimeType(fileName);
			content1.set_ContentType(mimeType);
			contentList.add(content1);
			curDoc.set_ContentElements(contentList);
			if(checkinType.equalsIgnoreCase("MAJOR"))
				curDoc.checkin(AutoClassify.DO_NOT_AUTO_CLASSIFY, CheckinType.MAJOR_VERSION);
			else
				curDoc.checkin(AutoClassify.DO_NOT_AUTO_CLASSIFY, CheckinType.MINOR_VERSION);
			curDoc.save(RefreshMode.REFRESH);
			System.out.println("The document is checked In");
			id = curDoc.get_Id().toString();
			
			ECMAttachment att = new ECMAttachment();
			this.getDocumentTitle();
			this.getMimeType();
			att.update(curDocId, id, this.name, this.mimeType);
		}
	}

	
	public void checkOut() throws Exception
	{
		try {
			load();
			Document doc = (Document) document.get_VersionSeries().get_CurrentVersion();
			// Check if the document is not already checked out
			if (!(doc.get_IsReserved().booleanValue())) {
				doc.checkout(ReservationType.EXCLUSIVE, null, null, null);
				doc.save(RefreshMode.REFRESH);
			}
		} catch (Exception e) {
			throw new Exception(e.getMessage());
		}
	}
	
	public void cancelCheckOut() throws Exception 
	{
		try {
			load();
			Document doc = (Document) document.get_VersionSeries().get_CurrentVersion();

			// Check if the document is currently checked out
			if (doc.get_IsReserved().booleanValue()) {
				Document versionableObj = (Document) doc.cancelCheckout();
				versionableObj.save(RefreshMode.REFRESH);
			}
		} catch (Exception e) {
			throw new Exception(e.getMessage());
		}
	} 
	
	public void download() throws Exception 
	{
		try {
			load();
			ContentElementList ceListOld = document.get_ContentElements();
			Iterator<ContentElement> ceItr = ceListOld.iterator();
			if (ceItr.hasNext()) {
				ContentTransfer ce = (ContentTransfer) ceItr.next();
				inputStream = ce.accessContentStream();
			}
		} catch (Exception e) {
			throw new Exception(e.getMessage());
		}
	}
	
	public void downloadForeSign() throws Exception 
	{
		try {
			loadDocOnly();
			ContentElementList ceListOld = document.get_ContentElements();
			Iterator<ContentElement> ceItr = ceListOld.iterator();
			if (ceItr.hasNext()) {
				ContentTransfer ce = (ContentTransfer) ceItr.next();
				inputStream = ce.accessContentStream();
			}
		} catch (Exception e) {
			throw new Exception(e.getMessage());
		}
	}
	
	public void downloadCurrentVersion() throws Exception 
	{
		try {
			load();
			Document curVer = (Document)document.get_CurrentVersion();
			ContentElementList ceListOld = curVer.get_ContentElements();
			Iterator<ContentElement> ceItr = ceListOld.iterator();
			if (ceItr.hasNext()) {
				ContentTransfer ce = (ContentTransfer) ceItr.next();
				inputStream = ce.accessContentStream();
			}
		} catch (Exception e) {
			throw new Exception(e.getMessage());
		}
	}
	
	public String downloadMultipleDocuments(ArrayList<String> docList) throws Exception 
	{
		try {
			String fileName = Utils.getUniqueDownloadFileName("ECM_") + ".zip";
			String zipFilePath = Utils.getTempFilePath() + fileName;
			FileOutputStream fos = new FileOutputStream(zipFilePath);
			ZipOutputStream zip = new ZipOutputStream(fos);
			
			int docCount = 0;
			for(String docID: docList) {
				docCount += 1;
				FNDocument fnd = FNDocument.getInstance(fnOS);
				fnd.setId(docID);
				fnd.downloadCurrentVersion();
				String dlFileName = Utils.getFileNameFromMimeType(fnd.getName(), fnd.getMimeType(), docCount);
				zip.putNextEntry(new ZipEntry(dlFileName)) ;
				byte[] bbuf = new byte[1024];
				int length = 0;
				while ( ( fnd.getInputStream() != null ) && ( ( length = fnd.getInputStream().read( bbuf ) ) != -1 ) ) {
					zip.write( bbuf, 0, length );  
				}  
				zip.closeEntry();
				fnd.getInputStream().close(); 
			}
			zip.flush();  
			zip.close(); 
			fos.flush(); 
			fos.close();
			
			this.inputStream = new FileInputStream(zipFilePath);
			this.name = fileName;
			this.mimeType = "application/zip";
			return zipFilePath;
			
		} catch (Exception e) {
			throw new Exception(e.getMessage());
		}
	}
	
	public TFNDocument getCurrentVersionTransport()
	{
		return getTransportObject(true);
	}
	
	public TFNDocument getTransport()
	{
		return getTransportObject(false);
	}
	
	private TFNDocument getTransportObject(Boolean isCurrent)
	{
		TFNDocument tfnv = null;
		try {
			if(isCurrent)
				loadCurrentVersion();
			else
				load();
			tfnv = new TFNDocument();
			tfnv.id = this.id;
			tfnv.creator = this.creator;
			tfnv.modifier = this.modifier;
			tfnv.addOn = Utils.formatDateForUI(this.createdOn);
			tfnv.modOn =  Utils.formatDateForUI(this.modifiedOn);
			tfnv.size = this.size + "";
			tfnv.format = this.mimeType;
			tfnv.verNo = this.versionNo;
			tfnv.vsid = this.vsid;
			tfnv.entryTemplate = this.entryTemplateId;
			tfnv.etName = this.entryTemplateName;
			
			if(this.tfnClass == null) {
				tfnv.docclass = this.fnClass.getSymbolicName();
				tfnv.props = new ArrayList<TFNProperty>();
				ArrayList<FNProperty> propList = fnClass.getProperties();
				for(int i=0; i < propList.size(); i++)
					tfnv.props.add(propList.get(i).getTransport());
			} else {
				tfnv.docclass = this.tfnClass.symName;
				tfnv.props = this.tfnClass.props;
			}
			tfnv.accessPolicies = new ArrayList<String>();
			if(this.accessPolicies != null) {
				for(String ap: this.accessPolicies)
					tfnv.accessPolicies.add(ap);
			}
			tfnv.props = convertPropertiesToEntryTemplate(tfnv.props);
			
		} catch (Exception e){
			logger.error(e.getMessage());
		}
		return tfnv;
	}
	
	public TFNDocument getDocumentTransportObject()
	{
		TFNDocument tfnv = null;
		try {
			loadCurrentVersionOnly();

			tfnv = new TFNDocument();
			tfnv.id = this.id;
			tfnv.creator = this.creator;
			tfnv.modifier = this.modifier;
			tfnv.addOn = Utils.formatDateForUI(this.createdOn);
			tfnv.modOn =  Utils.formatDateForUI(this.modifiedOn);
			tfnv.size = this.size + "";
			tfnv.format = this.mimeType;
			tfnv.verNo = this.versionNo;
			tfnv.vsid = this.vsid;
			tfnv.fileName = this.name;
			tfnv.entryTemplate = this.entryTemplateId;
			tfnv.etName = this.entryTemplateName;
			tfnv.docclass = this.docClass;
			
			tfnv.props = new ArrayList<TFNProperty>();
			TFNProperty tdprop = new TFNProperty();
			tdprop.name = "ECMNo";
			tdprop.mvalues.add(getECMNo());	
			tfnv.props.add(tdprop);
			
		} catch (Exception e){
			logger.error(e.getMessage());
		}
		return tfnv;
	}
	
	public TFNDocument getMigDocTransportObject()
	{
		TFNDocument tfnv = null;
		try {
			loadCurrentVersionOnly();

			tfnv = new TFNDocument();
			tfnv.id = this.id;
			tfnv.creator = this.creator;
			tfnv.modifier = this.modifier;
			tfnv.addOn = Utils.formatDateForUI(this.createdOn);
			tfnv.modOn =  Utils.formatDateForUI(this.modifiedOn);
			tfnv.size = this.size + "";
			tfnv.format = this.mimeType;
			tfnv.verNo = this.versionNo;
			tfnv.vsid = this.vsid;
			tfnv.fileName = this.name;
			tfnv.entryTemplate = this.entryTemplateId;
			tfnv.etName = this.entryTemplateName;
			tfnv.docclass = this.docClass;
			
			tfnv.props = new ArrayList<TFNProperty>();
			TFNProperty tdprop = new TFNProperty();
			tdprop.name = "ECMNo";
			tdprop.mvalues.add(getECMNo());	
			tfnv.props.add(tdprop);

			tdprop = new TFNProperty();
			tdprop.name = "OrgCode";
			tdprop.mvalues.add(getOrgCode());	
			tfnv.props.add(tdprop);
			
		} catch (Exception e){
			logger.error(e.getMessage());
		}
		return tfnv;
	}
	
	public void loadFromTransport(TFNDocument tfnv)
	{
		this.id = tfnv.id;
		this.mimeType = tfnv.format;
		if((tfnv.fileName != null) && (tfnv.fileName.length() > 0))
			this.fileName = tfnv.fileName;
		if((tfnv.folder != null) && (tfnv.folder.length() > 0)) {
			this.folder = FNFolder.getInstance(fnOS);
			this.folder.setId(tfnv.folder);
		}
		if((tfnv.docclass != null) && (tfnv.docclass.length() > 0)) {
			this.fnClass = FNDocumentClass.getInstance(fnOS);
			fnClass.setSymbolicName(tfnv.docclass);
			fnClass.getProperties();
			for(int i=0; i < tfnv.props.size(); i++)
				for(int j=0; j < tfnv.props.get(i).mvalues.size(); j++)
					fnClass.setProperty(tfnv.props.get(i).symName, tfnv.props.get(i).mvalues.get(j));
		}
		if((tfnv.accessPolicies != null) && (tfnv.accessPolicies.size() > 0)) {
			if(this.accessPolicies == null)
				this.accessPolicies = new ArrayList<String>();
			else
				this.accessPolicies.clear();
			for(String ap: tfnv.accessPolicies) {
				this.accessPolicies.add(ap);
			}
		}
		if((tfnv.entryTemplate != null) && (tfnv.entryTemplate.length() > 0))
			this.entryTemplateId = tfnv.entryTemplate;
	}
	

	public ArrayList<TFNDocument> getVersionsTransport() throws Exception
	{
		ArrayList<TFNDocument> tVersions = null;
		ArrayList<FNDocument> versions = getVersions();
		for(int i=0; i< versions.size(); i++) {
			if(tVersions == null)
				tVersions = new ArrayList<TFNDocument>();
			tVersions.add(versions.get(i).getTransport());
		}
		return tVersions;	
	}

	public ArrayList<TFNFolder> getFoldersFiledInTransport() throws Exception
	{
		ArrayList<TFNFolder> tFolders = new ArrayList<TFNFolder>();
		ArrayList<FNFolder> folders = getFoldersFiledIn();
		for(int i=0; i< folders.size(); i++) {
			if(!folders.get(i).getPath().startsWith("/ECMCart")) 
				tFolders.add(folders.get(i).getTransport());
		}
		return tFolders;	
	}
	
	public String getECMNo() {
		try {
			if(document == null)
				document = Factory.Document.fetchInstance(getOs().getObjectStore(), new Id(id), null);
			Property p = document.getProperties().find("ECMNo");
			return p.getStringValue();
		} catch (Exception e) {
			return null;
		}
	}
	
	public String getOrgCode() {
		try {
			if(document == null)
				document = Factory.Document.fetchInstance(getOs().getObjectStore(), new Id(id), null);
			Property p = document.getProperties().find("OrgCode");
			return p.getStringValue();
		} catch (Exception e) {
			return null;
		}
	}
	
	public String setSecurityAndTemplate() throws Exception {
		String strReturnMsg = "Success";
		try {
			if(this.document == null) {
				this.document = Factory.Document.fetchInstance(fnOS.getObjectStore(), new Id(this.id), null);
			}
			
			Properties dProps = this.document.getProperties();
			if(dProps.isPropertyPresent("Source")) {
				String source = dProps.getStringValue("Source");
				if(source != null) {
					if(source.trim().equalsIgnoreCase("Capture")) {
						String templId = dProps.getStringValue("FNCapEntryID");
						if((templId != null) && (templId.trim().length() > 0)) {
							dProps.putValue("EntryTemplateId", new Id(templId));
							dProps.putValue("EntryTemplateObjectStoreName", fnOS.symbolicName);
						}
					}
				}
			}
			
			if(document.getClassName().equalsIgnoreCase("Correspondences"))
				strReturnMsg = setOrgAccessPolicies(dProps);
			else
				strReturnMsg = setDefaultAccessPolicies(dProps);
			
			String defOwner = ECMConfigurationList.getInstance("ECM", "SYSTEM").getConfigValue("DEFAULTOWNER");
			if((defOwner != null) && (defOwner.length() > 0))
				document.set_Owner(defOwner);
			
			document.save(RefreshMode.REFRESH);
	
		} catch (Exception ex) {
			System.out.println("Error in setSecurityAndTemplate. Message: " + ex.getMessage());
			throw new Exception("Error in setSecurityAndTemplate. Message: " + ex.getMessage());
		}
		return strReturnMsg;
	}
	
	public String setMigratedDocSecurity() throws Exception {
		String strReturnMsg = "Success";
		try {
			if(this.document == null) {
				this.document = Factory.Document.fetchInstance(fnOS.getObjectStore(), new Id(this.id), null);
			}
			
			Properties dProps = this.document.getProperties();
			
			if(document.getClassName().equalsIgnoreCase("Correspondences"))
				strReturnMsg = setOrgAccessPolicies(dProps);
			else
				strReturnMsg = setMigDefaultAccessPolicies(dProps);	
			
			String defOwner = ECMConfigurationList.getInstance("ECM", "SYSTEM").getConfigValue("DEFAULTOWNER");
			if((defOwner != null) && (defOwner.length() > 0))
				document.set_Owner(defOwner);
			
			document.save(RefreshMode.REFRESH);
	
		} catch (Exception ex) {
			System.out.println("Error in setSecurityAndTemplate. Message: " + ex.getMessage());
			throw new Exception("Error in setSecurityAndTemplate. Message: " + ex.getMessage());
		}
		return strReturnMsg;
	}
	
	public void setPermissions(ArrayList<TPermission> tpList) throws Exception {
		load();
		if(document == null)
			return;
		ArrayList<AccessPermission> apIList = new ArrayList<AccessPermission>();
		AccessPermissionList apList = document.get_Permissions();
		for(int i=0; i < apList.size(); i++) {
			AccessPermission ap = (AccessPermission)apList.get(i);
			for(TPermission tp: tpList) {
				if(tp.action.equalsIgnoreCase("REMOVE")) {
					String gName = Utils.maskCanonicalName(ap.get_GranteeName()).replaceAll(" ", "");
					String justGName = DBUtil.removeAfter(gName, '@');
					if((gName.equalsIgnoreCase(tp.granteeName) ||
							justGName.equalsIgnoreCase(tp.granteeName)) &&
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
			} catch (Exception ex) {}
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
				ap.set_InheritableDepth(-2);
				apList.add(ap);
			}
		}
		
		document.save(RefreshMode.REFRESH);
	}
	
	private AccessType getAccessType(String inAccess) {
		if(inAccess.equalsIgnoreCase("DENY"))
			return AccessType.DENY;
		else
			return AccessType.ALLOW;
	}
	
	public void setSecurityParent(FNFolder secFolder) throws Exception {
		load();
		if(document == null)
			return;

		document.set_SecurityFolder(secFolder.getFolder());
		document.save(RefreshMode.REFRESH);
	}
	
	public void setPermissionFolderSecurity(FNDocument fnd, FNObjectStore os) throws Exception{		
		ArrayList<FNFolder> folders = fnd.getFoldersFiledIn();
		for(int i=0; i< folders.size(); i++) {
			if(!(folders.get(i).getPath().startsWith("/ECMCart")) && 
					folders.get(i).getClassName().equalsIgnoreCase("permissionsfolder")) 
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
			String fapId = fnf.getAccessPolicy(6);
			if((fapId != null) && (fapId.trim().length() > 0))
				fnd.setAccessPolicy(6, fapId); // PermissionFolder is always 6

			return fapId;
			
		} catch (Exception e) {
			throw e;
		}
	}
	
	public String unSetFolderAccessPolicy(String docId, FNObjectStore os) throws Exception {
		try {
			FNDocument fnd = FNDocument.getInstance(os);
			fnd.setId(docId);
			// PermissionFolder is 6
			unSetAccessPolicy(6);

			return "Done";
		} catch (Exception e) {
			throw e;
		}
	}
}