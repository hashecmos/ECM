package com.ecm.db.transport;

import java.util.ArrayList;

public class TAccessPolicy {
	public long id;
	public String name;
	public String desc;
	public String objectId;
	public long orgUnitId;
	public String type;
	public String orgCode;
	public String orgName;
	public String createdBy;
	public String createdDate;
	public String modifiedBy;
	public String modifiedDate;
	public ArrayList<TPermission> permissions;
}
