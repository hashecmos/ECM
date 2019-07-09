package com.ecm.filenet.list;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import com.ecm.filenet.model.FNDocumentClass;
import com.ecm.filenet.model.FNObjectStore;
import com.ecm.filenet.transport.TFNClass;
import com.filenet.api.admin.DocumentClassDefinition;
import com.filenet.api.collection.IndependentObjectSet;
import com.filenet.api.query.RepositoryRow;
import com.filenet.api.query.SearchSQL;
import com.filenet.api.query.SearchScope;

public class FNDocumentClassList 
{
	private static final Logger logger1 = Logger.getLogger(FNDocumentClassList.class);
	private FNObjectStore fnOS = null;
	
	public static FNDocumentClassList getInstance(FNObjectStore os)
	{
		return new FNDocumentClassList(os);
	}
	private FNDocumentClassList(FNObjectStore os)
	{	
		fnOS = os;
	}

	public ArrayList<FNDocumentClass> getDocumentClasses() throws Exception 
	{
		logger1.info("Started Method : getDocumentClasses Method parameter objectStore : " );
		ArrayList<FNDocumentClass> docClassList = new ArrayList<FNDocumentClass>();
		try {
			SearchScope searchScope = new SearchScope(fnOS.getObjectStore());
			SearchSQL sqlObject = new SearchSQL();
			String query = "SELECT ID, DisplayName,SymbolicName"
					+ " FROM DocumentClassDefinition WHERE "
					+ "IsHidden = FALSE AND IsSystemOwned = FALSE and AllowsInstances=TRUE";

			System.out.println(query);
			sqlObject.setQueryString(query);
			IndependentObjectSet rowSet = searchScope.fetchObjects(sqlObject, null, null, new Boolean(true));
			Iterator<RepositoryRow> iter = rowSet.iterator();
			
			while (iter.hasNext()) {
				DocumentClassDefinition row = (DocumentClassDefinition) iter.next();
				FNDocumentClass docClass = FNDocumentClass.getInstance(fnOS);
				docClass.setId(row.getProperties().getIdValue("ID").toString());
				docClass.setSymbolicName(row.getProperties().getStringValue("SymbolicName"));
				docClass.setName(row.getProperties().getStringValue("DisplayName"));
				docClassList.add(docClass);
			}

		} catch (Exception e) {
			logger1.error(e.getMessage(), e);
			throw new Exception(e.getMessage());
		}
		logger1.info("Exit Method : getDocumentClasses");
		return docClassList;
	}
	
	public ArrayList<TFNClass> getDocumentClassesTransport() throws Exception
	{
		ArrayList<FNDocumentClass> osList = getDocumentClasses();
		ArrayList<TFNClass> osTList = new ArrayList<TFNClass>();
		for(int i=0; i < osList.size(); i++)
		{
			osTList.add(osList.get(i).getTransport());
		}
		return osTList;
	}
	

}
