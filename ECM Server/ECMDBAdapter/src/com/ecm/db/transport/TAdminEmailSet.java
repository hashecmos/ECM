package com.ecm.db.transport;

import java.util.ArrayList;
import java.util.Date;

public class TAdminEmailSet {
	public String parentRole;
	public long empNo;
	public String activeDate;
	public String expiryDate;
	public long EmpNo;
	public long lookupId;
	public long id;
	
	
	public String iseSignAllowed;
	public String isInitalAllowed;
	public String isGlobal;
	public String justification;
	public String message;
	public String ACTIVE;
	public String isadmin;
	
	
	public String coordinator;
	public String description;
	
	public String isVisible;
	public String etName;
	public String etId;
	public String etVsId;
	public String adGroup;
	
	public  String template;
	public String prop;
	

	
	public String keyValue;
	public String keyDesc;
	public String appId;
	public String configScope;
	public String keyName;
	public String modifiedBy;
	public String modifiedDate;
	public String orgUnitName;
	
	
	public String label;
	public String subject;
	public String name;
	public String value;
	public String objectId;
	public String type;
	public String createdBy;
	public String createdDate;
	
	public String fullName;
	
	public ArrayList<TPermission> permissions;
	public ArrayList<TUser> users;
	public ArrayList<TLookupValue> values;

	
	public ArrayList<TPermission> getPermissions() {
		return permissions;
	}
	public void setPermissions(ArrayList<TPermission> permissions) {
		this.permissions = permissions;
	}
}
