package com.ecm.service.object;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import com.ecm.db.transport.TAccessPolicy;
import com.ecm.db.transport.TAdminUser;
import com.ecm.db.transport.TConfiguration;
import com.ecm.db.transport.TDelegate;
import com.ecm.db.transport.TDocMoveList;
import com.ecm.db.transport.TDocPermissions;
import com.ecm.db.transport.TFNContinueQuery;
import com.ecm.db.transport.TIntegration;
import com.ecm.db.transport.TLookup;
import com.ecm.db.transport.TNews;
import com.ecm.db.transport.TReportUserSearch;
import com.ecm.db.transport.TUserSearch;
import com.ecm.db.transport.TRecallAction;
import com.ecm.db.transport.TReportFilter;
import com.ecm.db.transport.TRole;
import com.ecm.db.transport.TSecurity;
import com.ecm.db.transport.TUser;
import com.ecm.db.transport.TUserList;
import com.ecm.db.transport.TUserSetting;
import com.ecm.db.transport.TWorkflowDetails;
import com.ecm.db.transport.TWorkitemAction;
import com.ecm.db.transport.TWorkitemProgress;
import com.ecm.db.transport.TWorkitemQuery;
import com.ecm.filenet.transport.TFNDocument;
import com.ecm.filenet.transport.TFNDocumentSet;
import com.ecm.filenet.transport.TFNQuery;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JSONHelper {

	private JSONHelper() { }
	
	public static JSONHelper getInstance() { return new JSONHelper(); }
	
	public TWorkflowDetails getWorkflowDetails(String jsonString) throws Exception {
		try {
		ObjectMapper mapper = new ObjectMapper();
    	return mapper.readValue(jsonString,  new TypeReference<TWorkflowDetails>(){});
		} catch (Exception e) {
			throw new Exception("Error while parsing the input. " + e.getMessage());
		}
	}
	
	public TWorkitemAction getWorkitemAction(String jsonString) throws Exception {
		try {
		ObjectMapper mapper = new ObjectMapper();
		mapper.setDateFormat(new SimpleDateFormat("dd-MM-yyyy hh:mm a"));
    	return mapper.readValue(jsonString,  new TypeReference<TWorkitemAction>(){});
		} catch (Exception e) {
			throw new Exception("Error while parsing the input. " + e.getMessage());
		}
	}
	
	public TRecallAction getRecallAction(String jsonString) throws Exception {
		try {
		ObjectMapper mapper = new ObjectMapper();
    	return mapper.readValue(jsonString,  new TypeReference<TRecallAction>(){});
		} catch (Exception e) {
			throw new Exception("Error while parsing the input. " + e.getMessage());
		}
	}
	
	public TWorkitemQuery getWorkitemQuery(String jsonString) throws Exception {
		try {
		ObjectMapper mapper = new ObjectMapper();
    	return mapper.readValue(jsonString,  new TypeReference<TWorkitemQuery>(){});
		} catch (Exception e) {
			throw new Exception("Error while parsing the input. " + e.getMessage());
		}
	}
	
	public TDocMoveList getDocMoveList(String jsonString) throws Exception {
		try {
		ObjectMapper mapper = new ObjectMapper();
    	return mapper.readValue(jsonString,  new TypeReference<TDocMoveList>(){});
		} catch (Exception e) {
			throw new Exception("Error while parsing the input. " + e.getMessage());
		}
	}
	
	public TAccessPolicy getAccessPolicy(String jsonString) throws Exception {
		try {
		ObjectMapper mapper = new ObjectMapper();
    	return mapper.readValue(jsonString,  new TypeReference<TAccessPolicy>(){});
		} catch (Exception e) {
			throw new Exception("Error while parsing the input. " + e.getMessage());
		}
	}
	
	
	public TDocPermissions getDocPermissions(String jsonString) throws Exception {
		try {
		ObjectMapper mapper = new ObjectMapper();
    	return mapper.readValue(jsonString,  new TypeReference<TDocPermissions>(){});
		} catch (Exception e) {
			throw new Exception("Error while parsing the input. " + e.getMessage());
		}
	}
	
	public TLookup getLookup(String jsonString) throws Exception {
		try {
		ObjectMapper mapper = new ObjectMapper();
    	return mapper.readValue(jsonString,  new TypeReference<TLookup>(){});
		} catch (Exception e) {
			throw new Exception("Error while parsing the input. " + e.getMessage());
		}
	}
	
	public TDelegate getDelegate(String jsonString) throws Exception {
		try {
		ObjectMapper mapper = new ObjectMapper();
    	return mapper.readValue(jsonString,  new TypeReference<TDelegate>(){});
		} catch (Exception e) {
			throw new Exception("Error while parsing the input. " + e.getMessage());
		}
	}
	
	public TFNQuery getQuery(String jsonString) throws Exception {
		try {
		ObjectMapper mapper = new ObjectMapper();
    	return mapper.readValue(jsonString,  new TypeReference<TFNQuery>(){});
		} catch (Exception e) {
			throw new Exception("Error while parsing the input. " + e.getMessage());
		}
	}
	
	public TFNDocument getDocument(String jsonString) throws Exception {
		try {
		ObjectMapper mapper = new ObjectMapper();
    	return mapper.readValue(jsonString,  new TypeReference<TFNDocument>(){});
		} catch (Exception e) {
			throw new Exception("Error while parsing the input. " + e.getMessage());
		}
	}
	
	
	public TSecurity getSecurity(String jsonString) throws Exception {
		try {
		ObjectMapper mapper = new ObjectMapper();
    	return mapper.readValue(jsonString,  new TypeReference<TSecurity>(){});
		} catch (Exception e) {
			throw new Exception("Error while parsing the input. " + e.getMessage());
		}
	}
	
	public TFNDocumentSet getDocumentsSet(String jsonString) throws Exception {
		try {
		ObjectMapper mapper = new ObjectMapper();
    	return mapper.readValue(jsonString,  new TypeReference<TFNDocumentSet>(){});
		} catch (Exception e) {
			throw new Exception("Error while parsing the input. " + e.getMessage());
		}
	}
	
	public TRole getRole(String jsonString) throws Exception {
		try {
		ObjectMapper mapper = new ObjectMapper();
    	return mapper.readValue(jsonString,  new TypeReference<TRole>(){});
		} catch (Exception e) {
			throw new Exception("Error while parsing the input. " + e.getMessage());
		}
	}
	
	public TUser getUser(String jsonString) throws Exception {
		try {
		ObjectMapper mapper = new ObjectMapper();
    	return mapper.readValue(jsonString,  new TypeReference<TUser>(){});
		} catch (Exception e) {
			throw new Exception("Error while parsing the input. " + e.getMessage());
		}
	}
	
	public ArrayList<TUserSetting> getUserSettings(String jsonString) throws Exception {
		try {
		ObjectMapper mapper = new ObjectMapper();
    	return mapper.readValue(jsonString,  new TypeReference<ArrayList<TUserSetting>>(){});
		} catch (Exception e) {
			throw new Exception("Error while parsing the input. " + e.getMessage());
		}
	}
	
	public TUserList getUserList(String jsonString) throws Exception {
		try {
		ObjectMapper mapper = new ObjectMapper();
    	return mapper.readValue(jsonString,  new TypeReference<TUserList>(){});
		} catch (Exception e) {
			throw new Exception("Error while parsing the input. " + e.getMessage());
		}
	}
	
	public ArrayList<TConfiguration> getConfigurations(String jsonString) throws Exception {
		try {
		ObjectMapper mapper = new ObjectMapper();
    	return mapper.readValue(jsonString,  new TypeReference<ArrayList<TConfiguration>>(){});
		} catch (Exception e) {
			throw new Exception("Error while parsing the input. " + e.getMessage());
		}
	}
	
	public TReportFilter getReportFilter(String jsonString) throws Exception {
		try {
		ObjectMapper mapper = new ObjectMapper();
    	return mapper.readValue(jsonString,  new TypeReference<TReportFilter>(){});
		} catch (Exception e) {
			throw new Exception("Error while parsing the input. " + e.getMessage());
		}
	}
	
	public TNews getNews(String jsonString) throws Exception {
		try {
		ObjectMapper mapper = new ObjectMapper();
    	return mapper.readValue(jsonString,  new TypeReference<TNews>(){});
		} catch (Exception e) {
			throw new Exception("Error while parsing the input. " + e.getMessage());
		}
	}
	
	public TWorkitemProgress getWIProgress(String jsonString) throws Exception {
		try {
		ObjectMapper mapper = new ObjectMapper();
    	return mapper.readValue(jsonString,  new TypeReference<TWorkitemProgress>(){});
		} catch (Exception e) {
			throw new Exception("Error while parsing the input. " + e.getMessage());
		}
	}
	
	public ArrayList<String> getDocumentList(String jsonString) throws Exception {
		try {
		ObjectMapper mapper = new ObjectMapper();
    	return mapper.readValue(jsonString,  new TypeReference<ArrayList<String>>(){});
		} catch (Exception e) {
			throw new Exception("Error while parsing the input. " + e.getMessage());
		}
	}
	
	public TIntegration getIntegration(String jsonString) throws Exception {
		try {
		ObjectMapper mapper = new ObjectMapper();
    	return mapper.readValue(jsonString,  new TypeReference<TIntegration>(){});
		} catch (Exception e) {
			throw new Exception("Error while parsing the input. " + e.getMessage());
		}
	}

	public TUserSearch getUserSearchCriteria(String jsonString) throws Exception {
		try {
		ObjectMapper mapper = new ObjectMapper();
    	return mapper.readValue(jsonString,  new TypeReference<TUserSearch>(){});
		} catch (Exception e) {
			throw new Exception("Error while parsing the input. " + e.getMessage());
		}
	}
	
	public TReportUserSearch getReportUserSearchCriteria(String jsonString) throws Exception {
		try {
		ObjectMapper mapper = new ObjectMapper();
    	return mapper.readValue(jsonString,  new TypeReference<TReportUserSearch>(){});
		} catch (Exception e) {
			throw new Exception("Error while parsing the input. " + e.getMessage());
		}
	}
	
	public TAdminUser getAdminUser(String jsonString) throws Exception {
		try {
		ObjectMapper mapper = new ObjectMapper();
    	return mapper.readValue(jsonString,  new TypeReference<TAdminUser>(){});
		} catch (Exception e) {
			throw new Exception("Error while parsing the input. " + e.getMessage());
		}
	}
		
	public TFNContinueQuery getTFNContinueQuery(String jsonString) throws Exception {
		try {
		ObjectMapper mapper = new ObjectMapper();
    	return mapper.readValue(jsonString,  new TypeReference<TFNContinueQuery>(){});
		} catch (Exception e) {
			throw new Exception("Error while parsing the input. " + e.getMessage());
		}
	}
}
