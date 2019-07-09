package com.ecm.filenet.model;

import org.apache.log4j.Logger;

public class FNDocumentClass extends FNClass
{
	private static final Logger logger = Logger.getLogger(FNDocumentClass.class);
	
	public static FNDocumentClass getInstance(FNObjectStore fnos)
	{
		return new FNDocumentClass(fnos);
	}

	private FNDocumentClass(FNObjectStore fnos) {
		fnOS = fnos;
		classType = "DOCUMENT";
	}

	public FNDocumentClass Copy() 
	{
		FNDocumentClass newdc = new FNDocumentClass(fnOS);
		newdc.copyFrom(this);
		return newdc;
	}
}
