package com.ecm.filenet.model;

import org.apache.log4j.Logger;

public class FNFolderClass extends FNClass {
	private static final Logger logger = Logger.getLogger(FNFolderClass.class);
	
	public static FNFolderClass getInstance(FNObjectStore fnos)
	{
		return new FNFolderClass(fnos);
	}

	private FNFolderClass(FNObjectStore fnos) {
		fnOS = fnos;
		classType = "FOLDER";
	}

	public FNFolderClass Copy() 
	{
		FNFolderClass newfc = new FNFolderClass(fnOS);
		newfc.copyFrom(this);
		return newfc;
	}

}
