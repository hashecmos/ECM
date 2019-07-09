package com.ecm.filenet.list;

import java.util.ArrayList;
import java.util.Iterator;

import org.apache.log4j.Logger;

import com.ecm.filenet.model.FNObjectStore;
import com.ecm.filenet.transport.TFNClass;
import com.filenet.api.collection.IndependentObjectSet;
import com.filenet.api.core.Document;
import com.filenet.api.query.RepositoryRow;
import com.filenet.api.query.SearchSQL;
import com.filenet.api.query.SearchScope;

public class FNSearchTemplateList 
{
	private static final Logger logger1 = Logger.getLogger(FNSearchTemplateList.class);
	private FNObjectStore fnOS = null;
	
	public static FNSearchTemplateList getInstance(FNObjectStore os)
	{
		return new FNSearchTemplateList(os);
	}
	private FNSearchTemplateList(FNObjectStore os)
	{	
		fnOS = os;
	}

	public ArrayList<TFNClass> getSearchTemplates() throws Exception 
	{
		logger1.info("Started Method : getSearchTemplates Method parameter objectStore : " );
		ArrayList<TFNClass> stList = new ArrayList<TFNClass>();
		try {
			SearchScope searchScope = new SearchScope(fnOS.getObjectStore());
			SearchSQL sqlObject = new SearchSQL();
			String query = "SELECT ID, DocumentTitle, Description"
					+ " SearchingObjectType, SearchType"
					+ " FROM StoredSearch WHERE "
					+ " SearchingObjectType = 1 AND IsCurrentVersion = True";

			System.out.println(query);
			sqlObject.setQueryString(query);
			IndependentObjectSet rowSet = searchScope.fetchObjects(sqlObject, null, null, new Boolean(true));
			Iterator<RepositoryRow> iter = rowSet.iterator();
			
			while (iter.hasNext()) {
				Document row = (Document) iter.next();
				TFNClass st = new TFNClass();
				st.id = row.getProperties().getIdValue("ID").toString();
				st.symName = row.getProperties().getStringValue("DocumentTitle");
				st.name = row.getProperties().getStringValue("Description");
				st.type = "SEARCH";
				stList.add(st);
			}

		} catch (Exception e) {
			logger1.error(e.getMessage(), e);
			throw new Exception(e.getMessage());
		}
		logger1.info("Exit Method : getSearchTemplates");
		return stList;
	}
}
