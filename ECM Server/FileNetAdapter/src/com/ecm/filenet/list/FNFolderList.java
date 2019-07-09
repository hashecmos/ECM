package com.ecm.filenet.list;

import java.util.ArrayList;
import java.util.Iterator;

import com.ecm.db.list.ECMConfigurationList;
import com.ecm.db.transport.TAttachment;
import com.ecm.db.transport.TDocMoveList;
import com.ecm.db.util.DBUtil;
import com.ecm.filenet.model.FNDocument;
import com.ecm.filenet.model.FNFolder;
import com.ecm.filenet.model.FNObjectStore;
import com.ecm.filenet.transport.TFNDocument;
import com.ecm.filenet.transport.TFNFolder;
import com.filenet.api.collection.IndependentObjectSet;
import com.filenet.api.constants.RefreshMode;
import com.filenet.api.core.Factory;
import com.filenet.api.core.IndependentObject;
import com.filenet.api.core.Link;
import com.filenet.api.property.Properties;
import com.filenet.api.query.SearchSQL;
import com.filenet.api.query.SearchScope;
import com.filenet.api.util.Id;

public class FNFolderList {
	private FNObjectStore fnOS = null;
	public static FNFolderList getInstance(FNObjectStore os)
	{
		return new FNFolderList(os);
	}
	
	private FNFolderList(FNObjectStore os)
	{
		fnOS = os;
	}
	
	private String getCartRootPath() {
		String folderPath = ECMConfigurationList.getInstance("ECM", "APP").getConfigValue("CARTROOT");
		if((folderPath == null) || (folderPath.length() <= 0))
			folderPath = "ECMUser";
		return folderPath;
	}
	
	private String getEmployeePath(long empNo) {
		long firstLevel = empNo / 1000;
		long secLevel = (empNo % 1000)/100;
		return "/" + getCartRootPath() + "/" + String.format("%03d", firstLevel) +
				"/" + String.format("%02d", secLevel) + "/" + empNo;
	}
	
	
	private FNFolder createEmployeeFolder(long empNo) throws Exception {
		String cartRoot = getCartRootPath();
		String firstLevel = String.format("%03d",  (empNo / 1000));
		String secLevel = String.format("%02d", ((empNo % 1000)/100));
		
		FNFolder rootFolder = fnOS.getRootFolder();
		FNFolder cRootF = rootFolder.createSubFolder(cartRoot);
		FNFolder firstF = cRootF.createSubFolder(firstLevel);
		FNFolder secF = firstF.createSubFolder(secLevel);
		return secF.createSubFolder(empNo + "");
	}
	
	private FNFolder getEmployeeFolder(long empNo) throws Exception {
		FNFolder f = FNFolder.getInstance(fnOS);
		f.setPath(getEmployeePath(empNo));
		if(!f.exists())
			f = createEmployeeFolder(empNo);
		if(!f.exists())
			throw new Exception("User folder does not exist!");
		return f;
	}
	
	private FNFolder getCartFolder(long empNo) throws Exception {
		return getECMFolder(empNo, "Cart");
	}
	
	private FNFolder getFavoriteFolder(long empNo) throws Exception {
		return getECMFolder(empNo, "Favorite");
	}
	
	private FNFolder getECMFolder(long empNo, String fType) throws Exception {
		FNFolder f = getEmployeeFolder(empNo);
		FNFolder secF = f.createSubFolder(fType);
		if(!secF.exists())
			throw new Exception(fType + " folder does not exist!");
		return secF;
	}
	
	public String addDocumentToCart(long empNo, String docID) throws Exception {
		FNFolder cartFolder = getCartFolder(empNo);
		FNDocument doc = FNDocument.getInstance(fnOS);
		doc.setId(docID);
		doc.setFolder(cartFolder);
		return doc.fileInFolder();
	}
	
	public String addDocumentToFavorites(long empNo, String docID) throws Exception {
		FNFolder favFolder = getFavoriteFolder(empNo);
		FNDocument doc = FNDocument.getInstance(fnOS);
		doc.setId(docID);
		doc.setFolder(favFolder);
		return doc.fileInFolder();
	}
	
	public String removeDocumentFromCart(long empNo, String docID) throws Exception {
		FNFolder cartFolder = getCartFolder(empNo);
		FNDocument doc = FNDocument.getInstance(fnOS);
		doc.setId(docID);
		doc.setFolder(cartFolder);
		return doc.unfileFromFolder();
	}
	
	public String removeDocumentFromFavorites(long empNo, String docID) throws Exception {
		FNFolder favFolder = getFavoriteFolder(empNo);
		FNDocument doc = FNDocument.getInstance(fnOS);
		doc.setId(docID);
		doc.setFolder(favFolder);
		return doc.unfileFromFolder();
	}
	
	public ArrayList<TFNDocument> getDocumentsInCart(long empNo) throws Exception {
		FNFolder cartFolder = getCartFolder(empNo);
		return cartFolder.getDocuments();
	}
	
	public ArrayList<TFNDocument> getFavoriteDocuments(long empNo) throws Exception {
		FNFolder favFolder = getFavoriteFolder(empNo);
		return favFolder.getDocuments();
	}
	
	public String moveMultipleDocuments(TDocMoveList dml) throws Exception {
		try {
			boolean bFail = false;
			if(dml.docIds.size() > 0)
			{
				for(String docId:dml.docIds) {			
					try {
						moveToFolder(dml.sourceFolder, dml.targetFolder, docId);
					} catch (Exception e) {
						// TODO: handle exception
						bFail = true;
						continue;
					}
				}
			}
			if(!bFail)
				return "OK";
			else
				return "Partial Fail";
		} catch (Exception e) {
			e.printStackTrace();
			//throw new Exception(e.getMessage());
			return "Failed";
		}
	}
	
	public String moveToFolder(String sourceId, String targetId, String docId) throws Exception {
		try {

			FNFolder rf = FNFolder.getInstance(fnOS);
			rf.setId(targetId);
			FNDocument fnd = FNDocument.getInstance(fnOS);
			fnd.setId(docId);
			fnd.setFolder(rf); 
			fnd.fileInFolder();
			
			FNFolder rf2 = FNFolder.getInstance(fnOS);
			rf2.setId(sourceId);
			FNDocument fnd2 = FNDocument.getInstance(fnOS);
			fnd2.setId(docId);
			fnd2.setFolder(rf2);
			fnd2.unfileFromFolder(); 
			return "OK";
		} catch (Exception e) {
			e.printStackTrace();
			//throw new Exception(e.getMessage());
			return "Failed";
		}
	}
	
	public String getLinkId(String firstId, String secondId) throws Exception {
		try {

			SearchScope searchScope = new SearchScope(fnOS.getObjectStore());
			SearchSQL sqlObject = new SearchSQL();
			sqlObject.setMaxRecords(500);

			String query = "SELECT TOP 1 [Id] FROM [KOCFavoriteLink] "
					+ "WHERE [Head] = Object('" + DBUtil.escapeString(firstId)
					+ "') AND [Tail] = Object('" + DBUtil.escapeString(secondId)
					+ "') OPTIONS(TIMELIMIT 180)";

			sqlObject.setQueryString(query);		
			IndependentObjectSet rowSet = searchScope.fetchObjects(sqlObject,100, null, new Boolean(false));
			Iterator<IndependentObject> iter = rowSet.iterator();
			if (iter == null)
				return null;

			if (iter.hasNext()) {
				String idVal = iter.next().getProperties().getIdValue("Id").toString();
				if((idVal != null) && (idVal.length() > 0))
					return idVal;
			}
			return null;
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		}
	}
	
	public void addFolderToFavorites(long empNo, String folderId) throws Exception {	
		try {	
			FNFolder favFolder = getFavoriteFolder(empNo);
			FNFolder userFolder = FNFolder.getInstance(fnOS);
			userFolder.setId(folderId);
			userFolder.load();
			if(getLinkId(favFolder.getId(), folderId) == null) {
				Link link1 = Factory.Link.createInstance(fnOS.getObjectStore(), "KOCFavoriteLink");
				link1.set_Head(favFolder.getFolder());
				link1.set_Tail(userFolder.getFolder());
				link1.save(RefreshMode.REFRESH);
			}
		} catch (Exception e) {
			throw e;
		}
	}
	
	public void removeFolderFromFavorites(long empNo, String folderId) throws Exception {
		try {
			FNFolder favFolder = getFavoriteFolder(empNo);
			String link = getLinkId(favFolder.getId(), folderId);
			if(link != null) {
				Link fLink = Factory.Link.getInstance(fnOS.getObjectStore(), "KOCFavoriteLink", new Id(link));
				if(fLink != null) {
					fLink.delete();
					fLink.save(RefreshMode.REFRESH);
				}
			}
		} catch (Exception e) {
			
		}
	}
	
	public ArrayList<TFNFolder> getFavoriteFolders(long empNo) throws Exception {
		ArrayList<TFNFolder> results = new ArrayList<TFNFolder>();
		try {
			FNFolder favFolder = getFavoriteFolder(empNo);
			
			SearchScope searchScope = new SearchScope(fnOS.getObjectStore());
			SearchSQL sqlObject = new SearchSQL();
			sqlObject.setMaxRecords(500);

			String query = "SELECT [Id], [Name], [Head], [Tail] FROM [KOCFavoriteLink] "
					+ "WHERE ([Head] = Object('" + DBUtil.escapeString(favFolder.getId())
					+ "')) OPTIONS(TIMELIMIT 180)";

			sqlObject.setQueryString(query);
		
			IndependentObjectSet rowSet = searchScope.fetchObjects(sqlObject,100, null, new Boolean(false));
			Iterator<IndependentObject> iter = rowSet.iterator();
			if (iter == null)
				return results;

			while (iter.hasNext()) {
				IndependentObject row = iter.next();
				Properties properties = row.getProperties();
				String folderId = properties.getIdValue("Tail").toString();
				if((folderId != null) && (folderId.trim().length() > 0)) {
					FNFolder fnf = FNFolder.getInstance(fnOS);
					fnf.setId(folderId);
					fnf.load();
					results.add(fnf.getTransport());
				}
			}
			return results;
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		}
	}
}
