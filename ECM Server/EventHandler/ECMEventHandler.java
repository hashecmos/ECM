
import com.filenet.api.constants.RefreshMode;
import com.filenet.api.core.CustomObject;
import com.filenet.api.core.Document;
import com.filenet.api.core.Factory;
import com.filenet.api.core.ObjectStore;
import com.filenet.api.engine.EventActionHandler;
import com.filenet.api.events.ObjectChangeEvent;
import com.filenet.api.exception.EngineRuntimeException;
import com.filenet.api.util.Id;

public class ECMEventHandler implements EventActionHandler {

	private static final String CLASS_NAME = ECMEventHandler.class.getName();
	private static String configPath = "/ECMConfiguration/";
	
	public void onEvent(ObjectChangeEvent event, Id subscriptionId)
			throws EngineRuntimeException {
	    try {
	    	System.out.println("Event: " + event.getClassName());
	    	System.out.println("subscriptionId " + subscriptionId.toString() );
	    	
			ObjectStore os = event.getObjectStore();
			System.out.println("Object Store ");
	    	Id docId = event.get_SourceObjectId();
	    	System.out.println("Document ID: " + docId.toString());
	    	
	    	Document doc = Factory.Document.fetchInstance(os, docId, null);
	    	System.out.println("Document fetched ");
	    	setOwner(os, doc, "DefaultOwner","ecmceadmin");
	    	System.out.println("Set Owner");
	    	doc.save(RefreshMode.NO_REFRESH);
	    	System.out.println("Document Saved");
	    	System.out.println("End Event Handler: " + event.getClassName() );
		} catch (Exception e) {
			System.out.println(CLASS_NAME + " onEvent " + e.getMessage());
		}
	}
	
	private void setOwner(ObjectStore os, Document doc, String configKey, String defValue) {
		System.out.println("Begin Function: setOwner");
		String owner = getConfigurationValue(os, configKey, defValue);
		System.out.println("Configured Owner: " + owner);
		doc.set_Owner(owner);
		System.out.println("End Function: setOwner");
	}
	
	private String getConfigurationValue(ObjectStore os, String configKey, String defValue) {
		try {
			System.out.println("Begin Function: getConfigurationValue");
			System.out.println("ConfigKey: " + configKey + " defaultValue: " + defValue);
			String cfgPath = configPath + configKey;
			CustomObject co = Factory.CustomObject.fetchInstance(os, cfgPath, null);
			if(co != null) {
				System.out.println("Config Object not null");
				return co.getProperties().getStringValue("ConfigValue");
			}
			System.out.println("Config Object null");
		} catch (Exception ex) {
			System.out.println("getConfigurationValue: " + ex.getMessage());
		}
		System.out.println("Returning default Value");
		return defValue;
	}
}
