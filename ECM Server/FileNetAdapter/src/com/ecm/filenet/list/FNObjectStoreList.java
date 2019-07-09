package com.ecm.filenet.list;

import java.util.ArrayList;
import java.util.Iterator;

import com.ecm.db.list.ECMConfigurationList;
import com.ecm.filenet.model.FNConnection;
import com.ecm.filenet.model.FNObjectStore;
import com.ecm.filenet.transport.TFNObjectStore;
import com.filenet.api.collection.ObjectStoreSet;
import com.filenet.api.core.ObjectStore;

public class FNObjectStoreList {
	
	public static FNObjectStoreList getInstance()
	{
		return new FNObjectStoreList();
	}
	
	private FNObjectStoreList()
	{
	}
	
	public ArrayList<FNObjectStore> getObjectStores() throws Exception
	{
		ArrayList<FNObjectStore> osList = new ArrayList<FNObjectStore>();
		
		ObjectStoreSet osSet = FNConnection.getInstance().getDomain().get_ObjectStores();
		Iterator osIter = osSet.iterator();
		while (osIter.hasNext()){
			ObjectStore store;
			store = (ObjectStore) osIter.next();
			FNObjectStore os = new FNObjectStore();
			os.setId(store.get_Id().toString());
			os.setName(store.get_Name());
			os.setSymbolicName(store.get_SymbolicName());
			os.setObjectStore(store);
			osList.add(os);
		}
		return osList;
	}
	
	public ArrayList<TFNObjectStore> getObjectStoresTransport() throws Exception
	{
		ArrayList<FNObjectStore> osList = getObjectStores();
		ArrayList<TFNObjectStore> osTList = new ArrayList<TFNObjectStore>();
		for(int i=0; i < osList.size(); i++)
		{
			osTList.add(osList.get(i).getTransport());
		}
		return osTList;
	}
	
	public FNObjectStore getApplicationObjectStore(String appID, String keyName) throws Exception
	{
		String osName = ECMConfigurationList.getInstance(appID, "SYSTEM").getConfigValue(keyName);
		FNObjectStore fnOS = new FNObjectStore();
		fnOS.setSymbolicName(osName);
		fnOS.getObjectStore();
		return fnOS;
	}
}
