package com.ecm.db.transport;

import java.util.ArrayList;

public class TUser {

	public long id;
	public String userName;
	public String fulName;
	public String title;
	public String mail;
	public long EmpNo;
	public String KocId;
	public String orgCode;
	public String appRole = "USER"; // ADMIN, BUSADMIN
	public String isAdmin = "N";
	public String isReport = "N";
	public String isReportAdmin = "N";
	public long iseSignAllowed = 0;
	public long isIntialAllowed = 0;
	
	public ArrayList<TRole> roles = new ArrayList<TRole>();
	public ArrayList<TDelegate> delegated = new ArrayList<TDelegate>();
	public ArrayList<TOrgUnit> headof = new ArrayList<TOrgUnit>();
	public String createdBy;
	public String createdDate;
	public String justification;
}
