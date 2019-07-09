package com.ecm.filenet.list;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;

import com.ecm.db.list.ECMAdministrationList;
import com.ecm.db.transport.TEntryTemplate;
import com.ecm.filenet.model.FNEntryTemplate;
import com.ecm.filenet.model.FNObjectStore;
import com.ecm.filenet.transport.TFNClass;
import com.filenet.api.collection.IndependentObjectSet;
import com.filenet.api.core.Document;
import com.filenet.api.query.RepositoryRow;
import com.filenet.api.query.SearchSQL;
import com.filenet.api.query.SearchScope;

public class FNEntryTemplateList 
{
	private static final Logger logger1 = Logger.getLogger(FNEntryTemplateList.class);
	private FNObjectStore fnOS = null;
	
	public static FNEntryTemplateList getInstance(FNObjectStore os)
	{
		return new FNEntryTemplateList(os);
	}
	private FNEntryTemplateList(FNObjectStore os)
	{	
		fnOS = os;
	}

	public ArrayList<TFNClass> getAllEntryTemplates() throws Exception 
	{
		logger1.info("Started Method : getEntryTemplates Method parameter objectStore : " );
		ArrayList<TFNClass> etList = new ArrayList<TFNClass>();
		try {
			SearchScope searchScope = new SearchScope(fnOS.getObjectStore());
			SearchSQL sqlObject = new SearchSQL();
			String query = "SELECT ID, DocumentTitle, EntryTemplateDescription"
					+ " FROM EntryTemplate WHERE "
					+ " TargetObjectType = 'document' AND IsCurrentVersion = True "
					+ "ORDER BY DocumentTitle ASC";

			System.out.println(query);
			sqlObject.setQueryString(query);
			IndependentObjectSet rowSet = searchScope.fetchObjects(sqlObject, null, null, new Boolean(true));
			Iterator<RepositoryRow> iter = rowSet.iterator();
			
			while (iter.hasNext()) {
				Document row = (Document) iter.next();
				FNEntryTemplate et = FNEntryTemplate.getInstance(fnOS);
				et.setId(row.getProperties().getIdValue("ID").toString());
				et.setSymbolicName(row.getProperties().getStringValue("DocumentTitle"));
				et.setName(row.getProperties().getStringValue("EntryTemplateDescription"));
				etList.add(et.getTransport(false));
			}

		} catch (Exception e) {
			logger1.error(e.getMessage(), e);
			throw new Exception(e.getMessage());
		}
		logger1.info("Exit Method : getEntryTemplates");
		return etList;
	}

	public ArrayList<TFNClass> getEntryTemplates(long empNo) throws Exception  {
		ArrayList<TEntryTemplate> etList = ECMAdministrationList.getInstance().getEntryTemplates(empNo);
		HashMap<String, TEntryTemplate> etHash = new HashMap<String, TEntryTemplate>();
		ArrayList<TFNClass> classList = new ArrayList<TFNClass>();
		for(TEntryTemplate tet: etList) {
			if(!etHash.containsKey(tet.entryTemplateId))
				etHash.put(tet.entryTemplateId, tet);
		}
		
		Iterator it = etHash.entrySet().iterator();
	    while (it.hasNext()) {
	        Map.Entry<String, TEntryTemplate> pair = (Map.Entry<String, TEntryTemplate>)it.next();
	        TFNClass etClass = new TFNClass();
			etClass.id = pair.getKey();
			etClass.symName = pair.getValue().name;
			
			classList.add(etClass);
	    }
	    
		return classList;
	}
	
	public ArrayList<TFNClass> getEntryTemplatesByOrgId(long orgId) throws Exception  {
		ArrayList<TEntryTemplate> etList = ECMAdministrationList.getInstance().getEntryTemplatesByOrgId(orgId);
		HashMap<String, TEntryTemplate> etHash = new HashMap<String, TEntryTemplate>();
		ArrayList<TFNClass> classList = new ArrayList<TFNClass>();
		for(TEntryTemplate tet: etList) {
			if(!etHash.containsKey(tet.entryTemplateId))
				etHash.put(tet.entryTemplateId, tet);
		}
		
		Iterator it = etHash.entrySet().iterator();
	    while (it.hasNext()) {
	        Map.Entry<String, TEntryTemplate> pair = (Map.Entry<String, TEntryTemplate>)it.next();
	        TFNClass etClass = new TFNClass();
			etClass.id = pair.getKey();
			etClass.symName = pair.getValue().name;
			etClass.name = pair.getValue().name;
			etClass.vsid = pair.getValue().etVsId;
			classList.add(etClass);
	    }
	    
		return classList;
	}
}
