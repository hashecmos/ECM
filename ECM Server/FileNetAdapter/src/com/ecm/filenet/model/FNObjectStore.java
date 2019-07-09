package com.ecm.filenet.model;

import com.ecm.db.list.ECMConfigurationList;
import com.ecm.filenet.transport.TFNObjectStore;
import com.filenet.api.core.Factory;
import com.filenet.api.core.Folder;
import com.filenet.api.core.ObjectStore;

public class FNObjectStore extends FNObject {
	private ObjectStore os = null;
	
	public ObjectStore getObjectStore() throws Exception {
		if(os == null) {
			String filter = id;
			if(id == null)
				filter = symbolicName;
			os = Factory.ObjectStore.fetchInstance(FNConnection.getInstance().getDomain(),  
					filter, null);
			if(id == null)
				id = os.get_Id().toString();
			if(name == null)
				name = os.get_Name();
			if(symbolicName == null)
				symbolicName = os.get_SymbolicName();
		}
		return os;
	}
	
	public void setObjectStore(ObjectStore osObj) { os = osObj; }
	
	public TFNObjectStore getTransport()
	{
		TFNObjectStore tfnos = new TFNObjectStore();
		tfnos.id = id;
		tfnos.name = name;
		tfnos.symName = symbolicName;
		
		return tfnos;
	}
	
	public FNFolder getRootFolder() throws Exception
	{
		getObjectStore();
		Folder rf = os.get_RootFolder();
		FNFolder rootFnf = FNFolder.getInstance(this);
		rootFnf.setFolder(rf);
		rootFnf.setId(rf.get_Id().toString());
		rootFnf.setPath(rf.get_PathName());
		rootFnf.setName(rf.get_FolderName());
		rootFnf.setSymbolicName(rf.get_Name());
		return rootFnf;
	}
	
	public FNFolder getECMRootFolder() throws Exception
	{
		getObjectStore();
		
		FNFolder ecmFnf = FNFolder.getInstance(this);	
		ecmFnf.setPath("/" + getECMRootPath());
		if(!ecmFnf.exists())
			throw new Exception("ECM Root folder does not exist!");
		return ecmFnf;
	}
	
	private String getECMRootPath() {
		
		String folderPath = ECMConfigurationList.getInstance("ECM", "APP").getConfigValue("ECMROOTFolder");
		if((folderPath == null) || (folderPath.length() <= 0))
			folderPath = "ECMRootFolder/Public Folders";
		return folderPath;
	}
	
}
