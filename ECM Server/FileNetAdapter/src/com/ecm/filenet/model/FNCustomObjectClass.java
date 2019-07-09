package com.ecm.filenet.model;

import org.apache.log4j.Logger;

public class FNCustomObjectClass extends FNClass {
	private static final Logger logger = Logger.getLogger(FNCustomObjectClass.class);
	
	public static FNCustomObjectClass getInstance(FNObjectStore fnos)
	{
		return new FNCustomObjectClass(fnos);
	}

	private FNCustomObjectClass(FNObjectStore fnos) {
		fnOS = fnos;
		classType = "CUSTOM";
	}

	public FNCustomObjectClass Copy() 
	{
		FNCustomObjectClass newcc = new FNCustomObjectClass(fnOS);
		newcc.copyFrom(this);
		return newcc;
	}
}
