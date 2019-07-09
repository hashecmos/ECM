package com.ecm.filenet.model;

public class FNAccessPolicy extends FNCustomObject {

	protected FNAccessPolicy(FNObjectStore os, String objType) {
		super(os);
		// TODO Auto-generated constructor stub
		fnClass = FNCustomObjectClass.getInstance(os);
		if(objType != null && objType.equalsIgnoreCase("PERMISSION"))
			fnClass.setSymbolicName("ECMPermAccessPolicy");
		else
			fnClass.setSymbolicName("KOCAccessPolicy");
	}
	
	public static FNAccessPolicy getInstance(FNObjectStore os, String objType){
		return new FNAccessPolicy(os, objType);
	}
	
	public String createAccessPolicy(String name, String orgCode, String desc) throws Exception {
		fnClass.getProperties();
		fnClass.setProperty("OrgCode", orgCode);
		fnClass.setProperty("Name", name);
		fnClass.setProperty("Description",  desc);
		createObject();
		return this.id;
	}

}
