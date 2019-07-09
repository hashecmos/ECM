package com.ecm.service.object;

import java.util.ArrayList;

import com.ecm.db.list.ECMConfigurationList;
import com.ecm.db.model.ECMIntegration;
import com.ecm.db.util.DBUtil;
import com.ecm.db.util.ECMLogger;
import com.ecm.filenet.list.FNDocumentSearch;
import com.ecm.filenet.model.FNEntryTemplate;
import com.ecm.filenet.model.FNObjectStore;
import com.ecm.filenet.transport.TFNClass;
import com.ecm.filenet.transport.TFNDocument;
import com.ecm.filenet.transport.TFNPair;

public class IntegrationHelper {
	private static ECMLogger logger = ECMLogger.getInstance(IntegrationHelper.class);
	
	public static IntegrationHelper getInstance() { return new IntegrationHelper(); }
	private IntegrationHelper() {}
	
	public Object getDocuments(FNObjectStore os, String appId, String param1, 
			String param2, String param3, String param4, String param5, 
			String searchFilter, String searchDate, String operator, String orderBy, String ascdesc)throws Exception {
		logger.info("Integration getDocuments App: " + appId + ", Param1: " + getString(param1)
				+", Param2: " + getString(param2) + ", Param3: " + getString(param3)
				+", Param4: " + getString(param4) + ", Param5: " + getString(param5));
		ECMIntegration intg = new ECMIntegration();
		intg.setAppId(appId);
		intg.load();
		String etVsId = "";
		String etId = intg.getTemplate();
		if(etId!= null && etId.length() > 0)
			etVsId = FNEntryTemplate.getInstance(os).getVsIdByEntryTemplate(etId);
		
		ArrayList<TFNPair> propList = new ArrayList<TFNPair>();
		
		addProperty(propList, intg.getParam1(), param1);
		addProperty(propList, intg.getParam2(), param2);
		addProperty(propList, intg.getParam3(), param3);
		addProperty(propList, intg.getParam4(), param4);
		addProperty(propList, intg.getParam5(), param5);
		addProperty(propList, intg.getSearchFilter(), searchFilter);
		addProperty(propList, intg.getSearchDate(), searchDate);
		
		ArrayList<TFNDocument> docs = FNDocumentSearch.getInstance(os).getIntegrationDocuments(
				intg.getClassName(), propList, etVsId, operator, orderBy, ascdesc);
		if(intg.getType().equalsIgnoreCase("SINGLE")) {
			if((docs!= null) && (docs.size() > 0)) {
				String viewerURL = ECMConfigurationList.getInstance("ECM", "APP").getConfigValue("WXTURL");
				
				String ecmURL = getDocumentViewURL(docs.get(0).id, viewerURL, docs.get(0).fileName, os.getSymbolicName());
				
/*				String ecmURL = DBUtil.appendChar(ECMConfigurationList.getInstance("ECM", "APP")
					.getConfigValue("ESIGNECMURL"), '/') + 
					"resources/DocumentService/downloadDocument?id=" + docs.get(0).id;*/
				
				logger.info(ecmURL);
				return (Object)ecmURL;
			}
			else
			{
				String appURL = ECMConfigurationList.getInstance("ECM", "APP").getConfigValue("ESIGNECMURL");
				String ecmURL = DBUtil.appendChar(appURL.trim(),'/') + "intgError.html";
				logger.info(ecmURL);
				return (Object)ecmURL;
			}
		}
		return docs;
	}
	
	private String getDocumentViewURL(String docId, String viewerURL, String docTitle, String objStoreName) throws Exception {
		
		String strUrl = DBUtil.appendChar(viewerURL.trim(),'/') + "getContent?id=" + docId + "&streamer=true&objectType=document&objectStoreName=" + objStoreName ;
        return strUrl;

	}
	
	private void addProperty(ArrayList<TFNPair> propList, String paramKey, String paramValue) {
		if((paramKey != null) && (paramKey.trim().length() > 0) && 
				(paramValue != null) && (paramValue.trim().length()  > 0)) {
			TFNPair prop = new TFNPair();
			prop.key = paramKey;
			prop.value = paramValue;
			if(!(propList.contains(paramKey)))
					propList.add(prop);
		}
	}
	
	private String getString(String inValue) {
		if(inValue == null)
			return "NULL";
		else if(inValue.trim().length() <= 0)
			return "<EMPTY>";
		
		return inValue;
	}
	
	public TFNClass getEntryTemplate(FNObjectStore os, String appId, long empNo)throws Exception {
		logger.info("Integration getDocuments App: " + appId);
		ECMIntegration intg = new ECMIntegration();
		intg.setAppId(appId);
		intg.load();
		
		FNEntryTemplate et = FNEntryTemplate.getInstance(os);
		et.setId(intg.getTemplate());
		et.setEmployeeNo(empNo);
		
		return et.getTransport(true);
	}
}
