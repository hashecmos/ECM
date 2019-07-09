package com.ecm.filenet.model;

public abstract class FNObject {
	protected String id;
	protected String symbolicName;
	protected String name;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getSymbolicName() {
		return symbolicName;
	}
	public void setSymbolicName(String symbolicName) {
		this.symbolicName = symbolicName;
	}
	public String getName() {
		return name;
	}
	public void setName(String className) {
		this.name = className;
	}
}
