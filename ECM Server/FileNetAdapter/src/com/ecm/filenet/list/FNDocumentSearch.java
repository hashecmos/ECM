package com.ecm.filenet.list;

import java.util.Date;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.log4j.Logger;

import sun.misc.OSEnvironment;

import com.ecm.db.list.ECMConfigurationList;
import com.ecm.db.list.ECMUserList;
import com.ecm.db.transport.TFNContinueQuery;
import com.ecm.db.transport.TOrgUnit;
import com.ecm.db.transport.TReportFilter;
import com.ecm.db.transport.TUser;
import com.ecm.db.util.DBUtil;
import com.ecm.filenet.model.FNConnection;
import com.ecm.filenet.model.FNDocument;
import com.ecm.filenet.model.FNEntryTemplate;
import com.ecm.filenet.model.FNObjectStore;
import com.ecm.filenet.model.FNProperty;
import com.ecm.filenet.transport.TFNDocument;
import com.ecm.filenet.transport.TFNDocumentSearch;
import com.ecm.filenet.transport.TFNEvent;
import com.ecm.filenet.transport.TFNLink;
import com.ecm.filenet.transport.TFNPair;
import com.ecm.filenet.transport.TFNProperty;
import com.ecm.filenet.transport.TFNQuery;
import com.ecm.filenet.util.Utils;
import com.filenet.api.collection.IndependentObjectSet;
import com.filenet.api.collection.RepositoryRowSet;
import com.filenet.api.core.Factory.PageIterator;
import com.filenet.api.core.IndependentObject;
import com.filenet.api.meta.ClassDescription;
import com.filenet.api.property.FilterElement;
import com.filenet.api.property.Properties;
import com.filenet.api.property.PropertyFilter;
import com.filenet.api.query.SearchSQL;
import com.filenet.api.query.SearchScope;

public class FNDocumentSearch {
	private static final Logger logger1 = Logger.getLogger(FNDocumentSearch.class);
	private FNObjectStore fnOS = null;
	
	public static FNDocumentSearch getInstance(FNObjectStore os)
	{
		return new FNDocumentSearch(os);
	}
	private FNDocumentSearch(FNObjectStore os)
	{	
		fnOS = os;
	}
	
	private boolean hasContentSearch(TFNQuery queryObj) {
		if((queryObj != null) && (queryObj.contentSearch != null) && 
			(queryObj.contentSearch.mvalues != null) &&
			(queryObj.contentSearch.mvalues.size() > 0) && 
			(queryObj.contentSearch.mvalues.get(0) != null) &&
			(queryObj.contentSearch.mvalues.get(0).length() > 0))
				return true;
		return false;
	}
	
	private String getSelector(TFNQuery queryObj, boolean hasCS) {
		String selector = "";
		if(hasCS)
		{
			selector = "SELECT d.This, d.VersionSeries, d.MimeType, d.IsReserved, "
				+ "d.Reservation, d.DocType, d.DocumentStatus,"
				+ "d.Creator, d.DateCreated, d.DateLastModified, d.DocumentTitle, "
				+ "d.EmsSubject, d.ECMNo, d.ReferenceNo, d.DocumentDate,"
				+ "d.Id, d.LastModifier, d.Name, d.Owner, d.ClassDescription "
				+ "FROM [" + DBUtil.escapeString(queryObj.symName) + "] d "
				+ "INNER JOIN ContentSearch c ON d.This = c.QueriedObject "; 
			//
			/*selector = "SELECT d.This, d.VersionSeries, d.MimeType, d.IsReserved, "
					+ "d.Reservation, "
					+ "d.Creator, d.DateCreated, d.DateLastModified, d.DocumentTitle, "
					+ "d.Id, d.LastModifier, d.Name, d.Owner "
					+ "FROM [" + DBUtil.escapeString(queryObj.symName) + "] d "
					+ "INNER JOIN ContentSearch c ON d.This = c.QueriedObject "; */
		}
		else {
			selector = "SELECT [This], [VersionSeries],[MimeType], [IsReserved], "
					+ "[Reservation], [DocType], [DocumentStatus],"
					+ "[Creator], [DateCreated], [DateLastModified], [DocumentTitle], "
					+ "[EmsSubject],[ECMNo],[ReferenceNo],[DocumentDate],"
					+ "[Id], [LastModifier], [Name], [Owner], [ClassDescription] "
					+ "FROM [" + DBUtil.escapeString(queryObj.symName) + "] d "; 
			//
			/*selector = "SELECT d.This, d.VersionSeries,d.MimeType, d.IsReserved, "
					+ "d.Reservation, "
					+ "d.Creator, d.DateCreated, d.DateLastModified, d.DocumentTitle, "
					+ "d.Id, d.LastModifier, d.Name, d.Owner "
					+ "FROM [" + DBUtil.escapeString(queryObj.symName) + "] d "; */
		}
		return selector;
	}
	
	private String getContainsSearchText(TFNQuery queryObj) {
		String[] words = queryObj.contentSearch.mvalues.get(0).split(" ");
		String oper = queryObj.contentSearch.oper;
		if((oper == null) || (oper.trim().length() <= 0))
				oper = "EXACT";
		oper = oper.trim();
		if(oper.equalsIgnoreCase("EXACT"))
			return "\"" + queryObj.contentSearch.mvalues.get(0) + "\"";

		String qString = "";
		String qoper = "AND";
		if(oper.equalsIgnoreCase("ANY"))
			qoper = "OR";
		
		for(String word: words)
		{
			word = word.replaceAll("%", "").trim(); //Utils.getOnlyStrings(word);
			if(word.length() > 0) {
				if(qString.length() > 0)
					qString += (" " + qoper + " ");
				qString += word.trim();
			}
		}
		return qString;
	}
	
	private String getOrderBy(String orderBy, String ascdesc) {
		String ad = " DESC";
		String orderProp = "d.DateLastModified";
		if((ascdesc != null) && (ascdesc.trim().equalsIgnoreCase("ASC")))
			ad = " ASC";
		
		if((orderBy != null) && (orderBy.trim().length() > 0)) {
			String ob = orderBy.trim();
			if(ob.equalsIgnoreCase("MimeType"))
				orderProp = "d.MimeType";
			else if(ob.equalsIgnoreCase("DocType"))
				orderProp = "d.DocType";
			else if(ob.equalsIgnoreCase("DocumentStatus"))
				orderProp = "d.DocumentStatus";
			else if(ob.equalsIgnoreCase("Creator"))
				orderProp = "d.Creator";
			else if(ob.equalsIgnoreCase("DateCreated"))
				orderProp = "d.DateCreated";
			else if(ob.equalsIgnoreCase("DateLastModified"))
				orderProp = "d.DateLastModified";
			else if(ob.equalsIgnoreCase("DocumentTitle"))
				orderProp = "d.DocumentTitle";
			else if(ob.equalsIgnoreCase("EmsSubject"))
				orderProp = "d.EmsSubject";
			else if(ob.equalsIgnoreCase("ECMNo"))
				orderProp = "d.ECMNo";
			else if(ob.equalsIgnoreCase("ReferenceNo"))
				orderProp = "d.ReferenceNo";
			else if(ob.equalsIgnoreCase("DocumentDate"))
				orderProp = "d.DocumentDate";
		}
		return " ORDER BY " + orderProp + ad;
	}
	
	private String getIntegrationOrderBy(String orderBy, String ascdesc) {
		String ad = " DESC";
		String orderProp = " DateLastModified";
		if((ascdesc != null) && (ascdesc.trim().equalsIgnoreCase("ASC")))
			ad = " ASC";
		
		if((orderBy != null) && (orderBy.trim().length() > 0)) {
			String ob = orderBy.trim();
			if(ob.equalsIgnoreCase("Creator"))
				orderProp = "Creator";
			else if(ob.equalsIgnoreCase("DC"))
				orderProp = "DateCreated";
			else if(ob.equalsIgnoreCase("DLM"))
				orderProp = "DateLastModified";
			else if(ob.equalsIgnoreCase("DT"))
				orderProp = "DocumentTitle";
			else if(ob.equalsIgnoreCase("ECMNo"))
				orderProp = "ECMNo";
			else if(ob.equalsIgnoreCase("DD"))
				orderProp = "DocumentDate";
		}
		return " ORDER BY " + orderProp + ad;
	}
	
	private String getQuerySQL(TFNQuery queryObj) {
		
		String countLimit = ECMConfigurationList.getInstance("ECM", "SYSTEM").getConfigValue("COUNTLIMIT");
		String timeLimit = ECMConfigurationList.getInstance("ECM", "SYSTEM").getConfigValue("TIMELIMIT");
		
		if(countLimit == null || countLimit == "")
			countLimit = "2000";
		if(timeLimit == null || timeLimit == "")
			timeLimit = "180";
		
		String whereStart = "";
		Boolean hasCS = hasContentSearch(queryObj);
		if(hasCS) {
			whereStart = " WHERE CONTAINS(d.*,'" + 
							getContainsSearchText(queryObj) + "') " +
							" AND d.IsCurrentVersion = true"; 
		} else
			whereStart = " WHERE d.IsCurrentVersion = true ";
 
		String queryString = getQueryCriteria(queryObj);
		String whereClause = whereStart + queryString;
		String orderClause = getOrderBy(queryObj.orderBy, queryObj.ascdesc);
		if(hasCS)
			whereClause += (orderClause + " OPTIONS(COUNT_LIMIT " + countLimit + ")");
		else
			whereClause += (orderClause + " OPTIONS(TIMELIMIT " + timeLimit + ", COUNT_LIMIT " + countLimit + ")");
		
		return getSelector(queryObj, hasCS) + whereClause;
	}
	
	private String getQueryCriteria(TFNQuery queryObj) {
		String queryString = "";
		
		if((queryObj.props == null) || (queryObj.props.size() <= 0))
			return queryString;
		
		for(TFNProperty prop: queryObj.props) {
			String sc = getSearchCondition(prop);
			if(sc.length() > 0)
				queryString += (" AND " + sc + " ");
		}
		return queryString;
	}
	
	private String getSearchCondition(TFNProperty prop) {
		//"like","is equal to", "contains", "starts with", "ends with"
		//"is not equal to", ">", "<", "<=", ">=", "between"
		String sc = "";
		if((prop == null) || (prop.mvalues == null) || (prop.mvalues.size() <= 0) ||
			(prop.symName == null) || (prop.symName.length() <= 0))
			return sc;
		if((prop.mvalues.get(0) == null) || (prop.mvalues.get(0).trim().length() <= 0))
			return sc;
		String propValue = DBUtil.escapeString(prop.mvalues.get(0).trim());
		String condition = prop.oper;
		if(condition!=null && !condition.equalsIgnoreCase("")){
			if(condition.equalsIgnoreCase("Like")){
				condition = " LIKE '%"+propValue+"%'";
			}else if(condition.equalsIgnoreCase("is equal to")){
				condition = " = '"+propValue+"'";
			}else if(condition.equalsIgnoreCase("contains")){
				condition = " LIKE '%"+propValue+"%'";
			}else if(condition.equalsIgnoreCase("starts with")){
				condition = " LIKE '"+propValue+"%'";
			}else if(condition.equalsIgnoreCase("ends with")){
				condition = " LIKE '%"+propValue+"'";
			}else if (condition.equalsIgnoreCase("=")){
				condition = " = '"+propValue + "'";
			}else if(condition.equalsIgnoreCase(">=")){
					condition = " >= '"+propValue + "'";
			}else if(condition.equalsIgnoreCase(">")){
				condition = " > '"+propValue + "'";
			}else if(condition.equalsIgnoreCase("<=")){
				condition = " <= '"+propValue + "'";
			}else if(condition.equalsIgnoreCase("<")){
				condition = " < '"+propValue + "'";
			}else if(condition.equalsIgnoreCase("in")){
				String mValues = "";
				for(String val: prop.mvalues) {
					if((val == null) || (val.length() <= 0))
						continue;
					if(mValues.length() > 0)
						val = ("," + "'" + val + "'");
					mValues += val;
				}
				condition = " in (" + mValues + ")";
			}else if(condition.equalsIgnoreCase("between")){
				String secondValue = "";
				if(prop.mvalues.size() > 1)
					secondValue = DBUtil.escapeString(prop.mvalues.get(1).trim());;
				if(secondValue.trim().length() > 0)
					condition = " >= " + propValue + " AND d." + prop.symName + " < " + secondValue;
				else
					condition = " >= " + propValue;
			}else if(condition.equalsIgnoreCase("is not equal to")){
				condition = "  <> '" + propValue + "'";
			} else
				condition = " = '" + propValue + "'";
		} else
			condition = " LIKE '%"+propValue+"%'";
		
		sc = "d." + prop.symName + condition;
		return sc;
	}
	
	private PropertyFilter getPropertyFilter() {
		PropertyFilter pf = new PropertyFilter();
		FilterElement fe = new FilterElement(
				null,
				null,
				null,
				"SymbolicName Id DocumentTitle ClassDescription Name VersionSeries MimeType "
				+ "Creator DateCreated DateLastModified LastModifier IsReserved ContentSize "
				+ "MajorVersionNumber ContentElementsPresent Reservation FoldersFiledIn "
				+ "Versions PathName FolderName ContentElements ElementSequenceNumber "
				+ "RetrievalName ContentType DateCheckedIn DisplayName",
				null);
		pf.addIncludeProperty(fe);
		return pf;
	}
	
	@SuppressWarnings("unchecked")
	public ArrayList<TFNDocument> executeSearch(TFNQuery queryObj) throws Exception {
		ArrayList<TFNDocument> results = new ArrayList<TFNDocument>();
		try {

			SearchScope searchScope = new SearchScope(fnOS.getObjectStore());
			SearchSQL sqlObject = new SearchSQL();

			String query = getQuerySQL(queryObj);
			if (query != null && "".equals(query)) {
				return results;
			}

			sqlObject.setQueryString(query);
			PropertyFilter pf = getPropertyFilter();
		
			logger1.info("Start time ::" + System.currentTimeMillis());
			IndependentObjectSet rowSet = searchScope.fetchObjects(sqlObject,100, pf, new Boolean(true));
			logger1.info("End time ::" + System.currentTimeMillis());
			
			//Iterator<IndependentObject> iter = rowSet.iterator();
			com.filenet.api.collection.PageIterator p = rowSet.pageIterator();
			
			if (p == null)
				return results;

			/*while (iter.hasNext()) {
				TFNDocument tDoc = fetchDocument(iter.next());
				results.add(tDoc);
			}*/
			
			while(p.nextPage()){
				for(Object obj : p.getCurrentPage()){
					TFNDocument tDoc = fetchDocument((IndependentObject)obj);
					results.add(tDoc);
				}
			}
			return results;
		} catch (Exception e) {
			e.printStackTrace();
			String errorMessage = e.getMessage();
			if(errorMessage.contains("FullTextRowLimit has been exceeded"))
				throw new Exception("The search returned too many results! Please narrow the search");
			else
				throw new Exception(e.getMessage());
		}
	}
	
	@SuppressWarnings("unchecked")
	public TFNDocumentSearch executeSearchPaging(TFNQuery queryObj) throws Exception {
		TFNDocumentSearch results = new TFNDocumentSearch();
		try {

			SearchScope searchScope = new SearchScope(fnOS.getObjectStore());
			SearchSQL sqlObject = new SearchSQL();

			String query = getQuerySQL(queryObj);
			if (query != null && "".equals(query)) {
				return results;
			}

			sqlObject.setQueryString(query);
			PropertyFilter pf = getPropertyFilter();
		
			logger1.info("Start time ::" + System.currentTimeMillis());
			IndependentObjectSet rowSet = searchScope.fetchObjects(sqlObject, queryObj.pageSize, pf, new Boolean(true));
			logger1.info("End time ::" + System.currentTimeMillis());
			
			com.filenet.api.collection.PageIterator p = rowSet.pageIterator();
			results.continueData = p.getCurrentPageCheckpoint();
			
			Boolean countException = false;
			try{
				results.totalResults = p.getTotalCount();
			}catch(Exception e){
				e.printStackTrace();
				countException = true;
			}
			
			ArrayList<TFNDocument> tfnList = new ArrayList<TFNDocument>();
			int page = 1;
			int count = 0;
			while(p.nextPage() && page==1){
				page++;
				for(Object obj : p.getCurrentPage()){
					count++;
					TFNDocument tDoc = fetchDocument((IndependentObject)obj);
					tfnList.add(tDoc);
				}
			}
			if(rowSet!=null && countException)
				results.totalResults = count;

			results.row = tfnList;
			return results;
		} catch (Exception e) {
			e.printStackTrace();
			String errorMessage = e.getMessage();
			if(errorMessage.contains("FullTextRowLimit has been exceeded"))
				throw new Exception("The search returned too many results! Please narrow the search");
			else
				throw new Exception(e.getMessage());
		}
	}
	
	
	@SuppressWarnings("unchecked")
	public TFNDocumentSearch executeContinueSearch(TFNContinueQuery queryObj) throws Exception {
		TFNDocumentSearch results = new TFNDocumentSearch();
		try {			
			com.filenet.api.collection.PageIterator p = PageIterator.resumeInstance(FNConnection.getInstance().getConnection(), queryObj.continueData);
			results.continueData = p.getCurrentPageCheckpoint();
			
			ArrayList<TFNDocument> tfnList = new ArrayList<TFNDocument>();
			long items = 0;
			while(p.nextPage()){
				for(Object obj : p.getCurrentPage()){
					items++;
					if(items>queryObj.skip && tfnList.size()<queryObj.pageSize){
						TFNDocument tDoc = fetchDocument((IndependentObject)obj);
						tfnList.add(tDoc);
					}
				}
			}	
			results.row = tfnList;
			return results;
		} catch (Exception e) {
			e.printStackTrace();
			String errorMessage = e.getMessage();
			if(errorMessage.contains("FullTextRowLimit has been exceeded"))
				throw new Exception("The search returned too many results! Please narrow the search");
			else
				throw new Exception(e.getMessage());
		}
	}
	
	private TFNDocument fetchDocument(IndependentObject row) throws Exception {
		TFNDocument tDoc = null;
		try {
			tDoc = new TFNDocument();
			Properties properties = row.getProperties();
			tDoc.id = properties.getIdValue("Id").toString();
			tDoc.creator = getPropertyString(properties, "Creator");
			tDoc.addOn = getPropertyDate(properties, "DateCreated") ;
			tDoc.modOn = getPropertyDate(properties, "DateLastModified");
			tDoc.modifier = getPropertyString(properties, "LastModifier");
			tDoc.format = getPropertyString(properties, "MimeType");
			tDoc.fileName = getPropertyString(properties, "DocumentTitle");
			tDoc.docclass = getPropertyString(properties, "ClassDescription");
			
/*			String etId = getPropertyId(properties, "EntryTemplateID");
			FNEntryTemplate fnet = FNEntryTemplate.getInstance(fnOS);
			fnet.setId(etId);
			tDoc.entryTemplate = fnet.getEntryTemplateName();*/
			
			tDoc.props = new ArrayList<TFNProperty>();
			tDoc.props.add(getPropertyValue(properties, "DocumentTitle"));
			tDoc.props.add(getPropertyValue(properties, "ReferenceNo"));
			tDoc.props.add(getPropertyValue(properties, "ECMNo"));
			tDoc.props.add(getPropertyValue(properties, "DocumentDate"));
			//tDoc.props.add(getPropertyValue(properties, "DocumentStatus"));
			tDoc.props.add(getPropertyValue(properties, "DocType"));
			tDoc.props.add(getPropertyValue(properties, "EmsSubject"));
			
			return tDoc;
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
	
	private TFNDocument fetchIntegrationDocument(IndependentObject row) throws Exception {
		TFNDocument tDoc = null;
		try {
			tDoc = new TFNDocument();
			Properties properties = row.getProperties();
			tDoc.id = properties.getIdValue("Id").toString();
			tDoc.creator = getPropertyString(properties, "Creator");
			tDoc.addOn = getPropertyDate(properties, "DateCreated") ;
			tDoc.modOn = getPropertyDate(properties, "DateLastModified");
			tDoc.modifier = getPropertyString(properties, "LastModifier");
			tDoc.format = getPropertyString(properties, "MimeType");
			tDoc.fileName = getPropertyString(properties, "DocumentTitle");
			tDoc.entryTemplate = getPropertyId(properties, "EntryTemplateID");
			tDoc.docclass = getPropertyString(properties, "ClassDescription");
			
			tDoc.props = new ArrayList<TFNProperty>();
			tDoc.props.add(getPropertyValue(properties, "DocumentTitle"));
			tDoc.props.add(getPropertyValue(properties, "ReferenceNo"));
			tDoc.props.add(getPropertyValue(properties, "ECMNo"));
			tDoc.props.add(getPropertyValue(properties, "DocumentDate"));
			//tDoc.props.add(getPropertyValue(properties, "DocumentStatus"));
			tDoc.props.add(getPropertyValue(properties, "DocType"));
			tDoc.props.add(getPropertyValue(properties, "EmsSubject"));
			
			return tDoc;
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
	
	private String fetchDocumentOrgCode(IndependentObject row) throws Exception {
		String orgCode = "";
		try {
			Properties properties = row.getProperties();
			orgCode = properties.getStringValue("OrgCode");	
		} catch (Exception e) {
			e.printStackTrace();
		}
		return orgCode;
	}
	
	private String fetchDocumentID(IndependentObject row) throws Exception {
		String docId = "";
		try {
			Properties properties = row.getProperties();
			docId = properties.getIdValue("ID").toString();	
		} catch (Exception e) {
			e.printStackTrace();
		}
		return docId;
	}
	
	private String fetchDocumentCreator(IndependentObject row) throws Exception {
		String creator = "";
		try {
			Properties properties = row.getProperties();
			creator = properties.getStringValue("Creator").toString();	
		} catch (Exception e) {
			e.printStackTrace();
		}
		return creator;
	}
	
	private String getPropertyString(Properties properties, String propName) {
		try {
			if(properties == null)
				return "";
			return properties.getStringValue(propName);
		} catch (Exception e) {
			return "";
		}
	}
	
	//doc.getProperties().getIdValue("EntryTemplateId").toString();
	private String getPropertyId(Properties properties, String propName) {
		try {
			if(properties == null)
				return "";
			return properties.getIdValue(propName).toString();
		} catch (Exception e) {
			return "";
		}
	}
	
	private String getPropertyDate(Properties properties, String propName) {
		try {
			if(properties == null)
				return "";
			return Utils.formatDateForUI(properties.getDateTimeValue(propName));
		} catch (Exception e) {
			return "";
		}
	}
	
	private TFNProperty getPropertyValue(Properties properties, String propName) {
		FNProperty fnp = new FNProperty();
		fnp.setName(propName);
		fnp.setValue(getPropertyString(properties, propName));
		return fnp.getTransport();
	}
	
	public ArrayList<TFNDocument> getRecentDocuments(long empNo) throws Exception {
		ArrayList<TFNDocument> results = new ArrayList<TFNDocument>();
		try {

			SearchScope searchScope = new SearchScope(fnOS.getObjectStore());
			SearchSQL sqlObject = new SearchSQL();
			sqlObject.setMaxRecords(25);

			String userName = ECMUserList.getInstance().getLoginName(empNo);
			String query = "SELECT [This], [VersionSeries],[MimeType], [IsReserved], "
					+ "[Reservation], [DocType], "
					+ "[Creator], [DateCreated], [DateLastModified], [DocumentTitle], "
					+ "[EmsSubject],[ECMNo],[ReferenceNo],[DocumentDate],"
					+ "[Id], [LastModifier], [Name], [Owner], [ClassDescription] "
					+ "FROM [KOCDocument] WHERE IsCurrentVersion = true AND ([LastModifier] = '" + DBUtil.escapeString(userName) + "' "		
					+ "OR Creator = '" + DBUtil.escapeString(userName) + "') ORDER BY DateLastModified DESC"; 
			
			if (query != null && "".equals(query)) {
				return results;
			}

			sqlObject.setQueryString(query);
			PropertyFilter pf = getPropertyFilter();
		
			logger1.info("Start time ::" + System.currentTimeMillis());
			IndependentObjectSet rowSet = searchScope.fetchObjects(sqlObject,100, pf, new Boolean(false));
			logger1.info("End time ::" + System.currentTimeMillis());
			Iterator<IndependentObject> iter = rowSet.iterator();
			if (iter == null)
				return results;

			while (iter.hasNext()) {
				TFNDocument tDoc = fetchDocument(iter.next());
				results.add(tDoc);
			}
			return results;
		} catch (Exception e) {
			e.printStackTrace();
			String errorMessage = e.getMessage();
			if(errorMessage.contains("FullTextRowLimit has been exceeded"))
				throw new Exception("The search returned too many results! Please narrow the search");
			else
				throw new Exception(e.getMessage());
		}
	}
	
	public ArrayList<TFNDocument> getTeamDocuments(long empNo) throws Exception {
		ArrayList<TFNDocument> results = new ArrayList<TFNDocument>();
		try {

			SearchScope searchScope = new SearchScope(fnOS.getObjectStore());
			SearchSQL sqlObject = new SearchSQL();
			sqlObject.setMaxRecords(25);

			String orgCode = ECMUserList.getInstance().getOrgCode(empNo);
			if((orgCode == null) || (orgCode.trim().length() <= 0))
				return results;
			String query = "SELECT [This], [VersionSeries],[MimeType], [IsReserved], "
					+ "[Reservation], [DocType], "
					+ "[Creator], [DateCreated], [DateLastModified], [DocumentTitle], "
					+ "[EmsSubject],[ECMNo],[ReferenceNo],[DocumentDate],"
					+ "[Id], [LastModifier], [Name], [Owner], [ClassDescription] "
					+ "FROM [KOCDocument] WHERE [OrgCode] = '" + DBUtil.escapeString(orgCode) + "' "		
					+ "AND IsCurrentVersion = true ORDER BY DateLastModified DESC"; 
			
			if (query != null && "".equals(query)) {
				return results;
			}

			sqlObject.setQueryString(query);
			PropertyFilter pf = getPropertyFilter();
		
			logger1.info("Start time ::" + System.currentTimeMillis());
			IndependentObjectSet rowSet = searchScope.fetchObjects(sqlObject,100, pf, new Boolean(false));
			logger1.info("End time ::" + System.currentTimeMillis());
			Iterator<IndependentObject> iter = rowSet.iterator();
			if (iter == null)
				return results;

			while (iter.hasNext()) {
				TFNDocument tDoc = fetchDocument(iter.next());
				results.add(tDoc);
			}
			return results;
		} catch (Exception e) {
			e.printStackTrace();
			String errorMessage = e.getMessage();
			if(errorMessage.contains("FullTextRowLimit has been exceeded"))
				throw new Exception("The search returned too many results! Please narrow the search");
			else
				throw new Exception(e.getMessage());
		}
	}
	
	public ArrayList<TOrgUnit> getOrgDocumentCounts(ArrayList<TOrgUnit> ouList, TReportFilter trf) throws Exception {
		try {
			//Test
			String startDate = trf.fromDate ;
			String endDate = trf.toDate ;
			long empNo = trf.EmpNo ;
			String exOperator = trf.exOperator;
			boolean isExclude = false;
			
			String creator = "";
			String sDate = Utils.getStartFileNetQueryDate(DBUtil.convertStringToDateEx(startDate));
			String eDate = Utils.getEndFileNetQueryDate(DBUtil.convertStringToDateEx(endDate));
			String orgCodeSQL = "(";
			for(TOrgUnit org:ouList) {
				if(orgCodeSQL.length() > 1)
					orgCodeSQL += " OR ";
				orgCodeSQL += " [OrgCode] = '" + DBUtil.escapeString(org.orgCode) + "' ";
				org.count = 0;
			}
			orgCodeSQL += ") ";
			
			if(empNo>0){
				creator = ECMUserList.getInstance().getLoginName(empNo);
				if(creator != "" && creator.length()>0)
					orgCodeSQL += "AND [creator] LIKE '%"+creator+"%' ";
					
			}	
			SearchScope searchScope = new SearchScope(fnOS.getObjectStore());
			SearchSQL sqlObject = new SearchSQL();

			String query = "SELECT [This], [OrgCode], [Creator] "
					+ " FROM [KOCDocument] WHERE " + orgCodeSQL
					+ " AND [DateCreated] >= " + sDate + " "
					+ " AND [DateCreated] < " + eDate + " "
					+ " AND IsCurrentVersion = true ORDER BY [OrgCode] ASC"; 

			sqlObject.setQueryString(query);
			PropertyFilter pf = getPropertyFilter();
		
			logger1.info("Start time ::" + System.currentTimeMillis());
			IndependentObjectSet rowSet = searchScope.fetchObjects(sqlObject,100, pf, new Boolean(true));
			logger1.info("End time ::" + System.currentTimeMillis());
			Iterator<IndependentObject> iter = rowSet.iterator();
			if (iter == null)
				return ouList;
			
			if(exOperator != null && exOperator.equalsIgnoreCase("Y"))
				isExclude = true;
			
			ArrayList<TUser> uList = null;
			try {
				uList = ECMUserList.getInstance().getExcludedUsers();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			while (iter.hasNext()) {
				IndependentObject nextObj = iter.next();
				String orgCode = fetchDocumentOrgCode(nextObj);
				String docCreator = fetchDocumentCreator(nextObj);
				if(!(isExcludedUser(uList, docCreator, isExclude)))
				{
					for(TOrgUnit org:ouList) {
						if(org.orgCode.trim().equalsIgnoreCase(orgCode.trim())) {
							org.count++;
							break;
						}
					}
				}
			}
			return ouList;
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		}
	}
	
	private boolean isExcludedUser(ArrayList<TUser> uList, String username, boolean isExclude){
		boolean bExcludedUser = false;
		
		if(!(isExclude))
			return bExcludedUser;
		
		if(uList == null)
			return bExcludedUser;
		
		if(uList != null && uList.size() > 0)
		{
			for(TUser tu: uList) {
				if(tu.userName.toLowerCase().equalsIgnoreCase(username))
					return  true;
			}
		}

		return bExcludedUser;	
	}
	
	public ArrayList<TFNLink> getLinks(String docId) throws Exception {
		ArrayList<TFNLink> results = new ArrayList<TFNLink>();
		try {

			SearchScope searchScope = new SearchScope(fnOS.getObjectStore());
			SearchSQL sqlObject = new SearchSQL();
			sqlObject.setMaxRecords(500);

			String query = "SELECT [Id], [Name], [Description], [Head], [Tail] FROM [KOCLink] "
					+ "WHERE ([Head] = Object('" + DBUtil.escapeString(docId)
					+ "')) OPTIONS(TIMELIMIT 180)";

			sqlObject.setQueryString(query);
		
			logger1.info("Start time ::" + System.currentTimeMillis());
			IndependentObjectSet rowSet = searchScope.fetchObjects(sqlObject,100, null, new Boolean(false));
			logger1.info("End time ::" + System.currentTimeMillis());
			Iterator<IndependentObject> iter = rowSet.iterator();
			if (iter == null)
				return results;

			while (iter.hasNext()) {
				TFNLink tlink = fetchLink(iter.next());
				results.add(tlink);
			}
			return results;
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		}
	}
	
	private TFNLink fetchLink(IndependentObject row) throws Exception {
		TFNLink tlink = null;
		try {
			tlink = new TFNLink();
			Properties properties = row.getProperties();
			tlink.id = properties.getIdValue("Id").toString();
			tlink.desc = getPropertyString(properties, "Description");
			tlink.head = properties.getIdValue("Head").toString();
			tlink.tail = properties.getIdValue("Tail").toString();
		
			return tlink;
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
	
	public String getLinkId(String firstId, String secondId) throws Exception {
		try {

			SearchScope searchScope = new SearchScope(fnOS.getObjectStore());
			SearchSQL sqlObject = new SearchSQL();
			sqlObject.setMaxRecords(500);

			String query = "SELECT TOP 1 [Id] FROM [KOCLink] "
					+ "WHERE [Head] = Object('" + DBUtil.escapeString(firstId)
					+ "') AND [Tail] = Object('" + DBUtil.escapeString(secondId)
					+ "') OPTIONS(TIMELIMIT 180)";

			sqlObject.setQueryString(query);
		
			logger1.info("Start time ::" + System.currentTimeMillis());
			IndependentObjectSet rowSet = searchScope.fetchObjects(sqlObject,100, null, new Boolean(false));
			logger1.info("End time ::" + System.currentTimeMillis());
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
	
	public ArrayList<TFNDocument> getIntegrationDocuments(String className,
			ArrayList<TFNPair> props, String etVsId, String operator, String orderBy, String ascdesc)
			throws Exception {
		ArrayList<TFNDocument> results = new ArrayList<TFNDocument>();
		try {
			SearchScope searchScope = new SearchScope(fnOS.getObjectStore());
			SearchSQL sqlObject = new SearchSQL();
			sqlObject.setMaxRecords(500);

			if (operator == null || operator.length() <= 0)
				operator = "AND";
			String selector = "";
			String propWhere = "";
			for (TFNPair prop : props) {
				if ((prop.key == null) || (prop.key.trim().length() <= 0))
					continue;
				if ((prop.value == null) || (prop.value.trim().length() <= 0))
					continue;
				if (!(prop.key.equalsIgnoreCase("DocumentTitle")
						|| prop.key.equalsIgnoreCase("ID") || prop.key
							.equalsIgnoreCase("DateCreated")))
					selector += ("[" + prop.key + "], ");
				if (propWhere.length() > 0)
					propWhere += " " + operator.toUpperCase() + " ";
				propWhere += getQueryPropertyString(prop.key, prop.value);
				// propWhere += ("[" + prop.key +"] = '" +
				// DBUtil.escapeString(prop.value) + "' ");
			}
			if (propWhere.trim().length() <= 0)
				return results;
			
			String orderClause = getIntegrationOrderBy(orderBy, ascdesc);

			String query = "SELECT [This], [VersionSeries],[MimeType], [IsReserved], "
					+ "[Creator], [DateCreated], [DateLastModified], [DocumentTitle], "
					+ selector
					+ "[Id], [LastModifier], [Name], [Owner], [EntryTemplateId] "
					+ "FROM ["
					+ DBUtil.escapeString(className)
					+ "] "
					+ "WHERE IsCurrentVersion = true AND "
					+ propWhere
					+ orderClause + " OPTIONS(TIMELIMIT 180)"; //" ORDER BY DateLastModified DESC";

			sqlObject.setQueryString(query);
			PropertyFilter pf = getPropertyFilter();

			logger1.info("Start time ::" + System.currentTimeMillis());
			IndependentObjectSet rowSet = searchScope.fetchObjects(sqlObject,
					100, pf, new Boolean(false));
			logger1.info("End time ::" + System.currentTimeMillis());
			Iterator<IndependentObject> iter = rowSet.iterator();
			if (iter == null)
				return results;

			while (iter.hasNext()) {
				TFNDocument tDoc = fetchIntegrationDocument(iter.next());
				String etId = tDoc.entryTemplate;
				String tDocEtVsId = "";
				if (etId != null && etId.length() > 0)
					tDocEtVsId = FNEntryTemplate.getInstance(fnOS)
							.getVsIdByEntryTemplate(etId);
				if (tDocEtVsId.trim().equalsIgnoreCase(etVsId.trim()))
					results.add(tDoc);
			}
			return results;
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		}
	}
	
	private String getQueryPropertyString(String propKey, String propValue) throws Exception {
		String [] pValues = propValue.split(",");
		String propWhere = "";
		if((pValues == null) || (pValues.length <= 0))
			return propWhere;
		
		if(pValues[0] != null && pValues[0].trim().equalsIgnoreCase("DATE")) {
			String eDate = "";
			String sDate = "";
			String sOp = " = ";
			String eOp = " < ";
			
			if(pValues.length > 2) {
				Date jDate = DBUtil.safeConvertStringToDateEx(pValues[2]);
				eDate = Utils.getEndFileNetQueryDate(jDate);
			}
			
			if(pValues.length > 1) {
				Date jDate = DBUtil.safeConvertStringToDateEx(pValues[1]);
				sDate = Utils.getStartFileNetQueryDate(jDate);
			}
			
			if(eDate.length() > 0) {
				propWhere += " (";
				sOp = " >= ";
			}
			propWhere += ("[" + propKey +"]" + sOp + DBUtil.escapeString(sDate));
			if(eDate.length() > 0) {
				propWhere += (" AND [" + propKey +"]" + eOp + DBUtil.escapeString(eDate));
				propWhere += ") ";
			}
		} else {
			
			String opr = "=";
			String wild = "";
			if(propKey.equalsIgnoreCase("DocumentTitle"))
			{
				opr = "like";
				wild = "%";
				//propWhere += "[DocumentTitle] like '%" + DBUtil.escapeString(propValue) + "%'";
				//return propWhere;
			}
			
			if(pValues.length > 1) {
				if(pValues[0].trim().equalsIgnoreCase("STRING") || 
						pValues[0].trim().equalsIgnoreCase("INT"))
					if(pValues.length > 2)
						propWhere += " (";
			}
			String propSQL = "";
			int nCount = 0;
			String quote = "'";
			for(String prop:pValues) {
				if(nCount <= 0) {
					nCount++;
					if(pValues[0].trim().equalsIgnoreCase("STRING"))
						continue;
					if(pValues[0].trim().equalsIgnoreCase("INT")) {
						quote = "";
						wild = "";
						continue;
					}
				}	
				if(propSQL.length() > 0)
					propSQL += " OR ";
				propSQL += ("[" + propKey +"] " + opr + " " + quote + wild + DBUtil.escapeString(prop) + wild + quote + " ");

			}
			
			propWhere += propSQL;
			
			if(pValues.length > 1) {
				if(pValues[0].trim().equalsIgnoreCase("STRING") || 
						pValues[0].trim().equalsIgnoreCase("INT"))
					if(pValues.length > 2)
						propWhere += ") ";
			}
		}
		return propWhere;
	}
	
	public ArrayList<TFNEvent> getEvents(String docId) throws Exception {
		ArrayList<TFNEvent> eventList = new ArrayList<TFNEvent>();
		try {
			SearchScope searchScope = new SearchScope(fnOS.getObjectStore());
			SearchSQL sqlObject = new SearchSQL();
			sqlObject.setMaxRecords(500);

			String query = "SELECT TOP 500 [ClassDescription], "
					+ "[DateCreated], [Creator] FROM EVENT "
					+ "WHERE SourceObjectId = Object('" + DBUtil.escapeString(docId) + "') "
					+ "ORDER BY DateCreated ASC";

			sqlObject.setQueryString(query);
		
			logger1.info("Start time ::" + System.currentTimeMillis());
			IndependentObjectSet rowSet = searchScope.fetchObjects(sqlObject,100, null, new Boolean(false));
			logger1.info("End time ::" + System.currentTimeMillis());
			Iterator<IndependentObject> iter = rowSet.iterator();
			if (iter == null)
				return eventList;

			TFNEvent prevEvent = null;
			while (iter.hasNext()) {
				IndependentObject row = iter.next();
				TFNEvent docEvent = new TFNEvent();
				ClassDescription cd = (ClassDescription)row.getProperties().getObjectValue("ClassDescription");
				docEvent.desc = cd.get_DisplayName();
				docEvent.timestamp = DBUtil.convertDateTimeToString(row.getProperties().getDateTimeValue("DateCreated"));
				docEvent.user = row.getProperties().getStringValue("Creator");
				docEvent.desc = docEvent.desc.replace("Event", "");
				if(docEvent.desc.trim().equalsIgnoreCase("Get Object"))
					docEvent.desc = "Read";
				if(prevEvent != null) {
					if(prevEvent.user.equalsIgnoreCase(docEvent.user) && 
						prevEvent.timestamp.equalsIgnoreCase(docEvent.timestamp) &&
						prevEvent.desc.equalsIgnoreCase(docEvent.desc)) {
						continue;
					}
				}
				prevEvent = docEvent;
				eventList.add(docEvent);
			}
			return eventList;
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		}
	}

	public ArrayList<String> getOrgDocumentIDs(String orgCode) throws Exception {
		try {
			ArrayList<String> outList = new ArrayList<String>();
			
			String orgCodeSQL = " [OrgCode] = '" + DBUtil.escapeString(orgCode) + "' ";

			SearchScope searchScope = new SearchScope(fnOS.getObjectStore());
			SearchSQL sqlObject = new SearchSQL();

			String query = "SELECT TOP 2 [This], [ID], [DateCreated] "
					+ " FROM [KOCDocument] WHERE " + orgCodeSQL
					+ " ORDER BY [DateCreated] DESC"; 

			sqlObject.setQueryString(query);
			PropertyFilter pf = getPropertyFilter();
		
			logger1.info("Start time ::" + System.currentTimeMillis());
			IndependentObjectSet rowSet = searchScope.fetchObjects(sqlObject,100, pf, new Boolean(true));
			logger1.info("End time ::" + System.currentTimeMillis());
			Iterator<IndependentObject> iter = rowSet.iterator();
			if (iter == null)
				return outList;

			while (iter.hasNext()) {
				String docId = fetchDocumentID(iter.next());
				if((docId != null) && (docId.trim().length() > 0))
					outList.add(docId.trim());
			}
			return outList;
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		}
	}
}
