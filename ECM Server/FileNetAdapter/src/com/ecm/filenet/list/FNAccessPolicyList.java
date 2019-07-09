package com.ecm.filenet.list;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import com.ecm.db.list.ECMUserList;
import com.ecm.db.util.DBUtil;
import com.ecm.filenet.model.FNAccessPolicy;
import com.ecm.filenet.model.FNCustomObjectClass;
import com.ecm.filenet.model.FNDocumentClass;
import com.ecm.filenet.model.FNObjectStore;
import com.ecm.filenet.transport.TFNAccessPolicy;
import com.ecm.filenet.transport.TFNClass;
import com.ecm.filenet.transport.TFNDocument;
import com.ecm.filenet.transport.TFNProperty;
import com.filenet.api.admin.DocumentClassDefinition;
import com.filenet.api.collection.IndependentObjectSet;
import com.filenet.api.core.IndependentObject;
import com.filenet.api.property.Properties;
import com.filenet.api.property.PropertyFilter;
import com.filenet.api.query.RepositoryRow;
import com.filenet.api.query.SearchSQL;
import com.filenet.api.query.SearchScope;

public class FNAccessPolicyList 
{
	private static final Logger logger1 = Logger.getLogger(FNAccessPolicyList.class);
	private FNObjectStore fnOS = null;
	private FNCustomObjectClass fnClass = null;
	
	public static FNAccessPolicyList getInstance(FNObjectStore os)
	{
		return new FNAccessPolicyList(os);
	}
	private FNAccessPolicyList(FNObjectStore os)
	{	
		fnOS = os;
		fnClass = FNCustomObjectClass.getInstance(os);
		fnClass.setSymbolicName("ECMPermAccessPolicy");
		
	}
	
	@SuppressWarnings("unchecked")
	public ArrayList<TFNAccessPolicy> getPermAccessPolicies() throws Exception {
		ArrayList<TFNAccessPolicy> results = new ArrayList<TFNAccessPolicy>();
		try {

			SearchScope searchScope = new SearchScope(fnOS.getObjectStore());
			SearchSQL sqlObject = new SearchSQL();
			sqlObject.setMaxRecords(25);

			String query = "SELECT [This], [Id], [Description], [Name], [OrgCode]"
					+ " FROM [ECMPermAccessPolicy] ORDER BY Description ASC"; 
			
			if (query != null && "".equals(query)) {
				return results;
			}

			sqlObject.setQueryString(query);
			PropertyFilter pf = null;
		
			logger1.info("Start time ::" + System.currentTimeMillis());
			IndependentObjectSet rowSet = searchScope.fetchObjects(sqlObject,100, pf, new Boolean(false));
			logger1.info("End time ::" + System.currentTimeMillis());
			Iterator<IndependentObject> iter = rowSet.iterator();
			if (iter == null)
				return results;
			int iCounter = 0;
			while (iter.hasNext()) {
				TFNAccessPolicy tDoc = fetchAccessPolicy(iter.next(), iCounter);
				results.add(tDoc);
				iCounter++;
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

	private TFNAccessPolicy fetchAccessPolicy(IndependentObject row, int iCounter) throws Exception {
		TFNAccessPolicy tap = null;
		try {
			tap = new TFNAccessPolicy();
			Properties properties = row.getProperties();
			tap.id = iCounter + 1;
			tap.objectId = properties.getIdValue("Id").toString();
			tap.desc = properties.getStringValue("Description").toString();
			tap.name = properties.getStringValue("Description").toString();

			return tap;
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
}
