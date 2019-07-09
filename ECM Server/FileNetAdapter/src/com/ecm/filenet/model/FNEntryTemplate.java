package com.ecm.filenet.model;

import java.io.InputStream;
import java.io.StringBufferInputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.ecm.db.list.ECMLookupList;
import com.ecm.db.transport.TLookup;
import com.ecm.db.transport.TLookupValue;
import com.ecm.filenet.list.FNAccessPolicyList;
import com.ecm.filenet.transport.TFNAccessPolicy;
import com.ecm.filenet.transport.TFNClass;
import com.ecm.filenet.transport.TFNProperty;
import com.ecm.filenet.util.Utils;
import com.filenet.api.admin.Choice;
import com.filenet.api.admin.ChoiceList;
import com.filenet.api.admin.ClassDefinition;
import com.filenet.api.admin.PropertyDefinition;
import com.filenet.api.admin.PropertyDefinitionString;
import com.filenet.api.collection.ContentElementList;
import com.filenet.api.collection.MarkingList;
import com.filenet.api.collection.PropertyDefinitionList;
import com.filenet.api.constants.ChoiceType;
import com.filenet.api.core.ContentElement;
import com.filenet.api.core.ContentTransfer;
import com.filenet.api.core.Factory;
import com.filenet.api.core.Document;
import com.filenet.api.security.Marking;
import com.filenet.api.security.MarkingSet;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.NodeList;
import org.w3c.dom.Node;

public class FNEntryTemplate extends FNObject 
{
	private static final Logger logger = Logger.getLogger(FNEntryTemplate.class);

	protected FNObjectStore fnOS = null;
	private Boolean selectFolder = false;
	private Boolean constrainFolder = false;
	private Boolean setProperties = false;
	private long empNo;
	private String vsId = null;
	
	ArrayList <FNProperty> properties = null;
	FNFolder templateFolder = null;
	
	public static FNEntryTemplate getInstance(FNObjectStore os){
		return new FNEntryTemplate(os);
	}
	
	private FNEntryTemplate(FNObjectStore os){
		fnOS = os;
	}
	
	public Boolean getCanSelectFolder() { return selectFolder; }
	public Boolean getCanConstrainFolder() { return constrainFolder; }
	public Boolean getCanSetProperties() { return setProperties; }
	public FNFolder getFolder() { return templateFolder; }
	public void setEmployeeNo(long empNo) { this.empNo = empNo; }
	
	public ArrayList<FNProperty> getProperties() {
		try {
		if(properties == null)
			getPropertyDefinitions();
		} catch(Exception e)
		{
			logger.error(e.getMessage());
		}
		return properties;
	}
	
	public void setProperties(ArrayList<FNProperty> props) {
		if(this.properties == null)
			this.properties = new ArrayList<FNProperty>();
		this.properties.clear();
		this.properties.addAll(props);
	}
	
	public void setProperty(String propSymName, String propValue) {
		for(int i=0; i<properties.size(); i++)
			if(properties.get(i).getSymbolicName().equalsIgnoreCase(propSymName))
				properties.get(i).setValue(propValue);
	}
	
	public List<FNProperty> getPropertyDefinitions() throws Exception 
	{
		try {
			Document etdoc = Factory.Document.fetchInstance(fnOS.getObjectStore(), id, null);
			Document etd = (Document)etdoc.get_CurrentVersion();
			this.vsId = etdoc.get_VersionSeries().get_Id().toString();
			InputStream inputStream = null;
			ContentElementList ceListOld = etd.get_ContentElements();
			Iterator<ContentElement> ceItr = ceListOld.iterator();
			if (ceItr.hasNext()) {
				ContentTransfer ce = (ContentTransfer) ceItr.next();
				inputStream = (InputStream)ce.accessContentStream();
			}
			Scanner s = new Scanner(inputStream).useDelimiter("\\A");
			String result = s.hasNext() ? s.next() : "";
			if(!parseEntryTemplate(result))
				parseJSONEntryTemplate(result);
			name = etd.get_Name();
			getChoiceLists();
			getLookups(this.empNo);
			//getPermissionAccessPolicies();
			
			logger.info("Exit Method : getPropertyDefinitions");
			return properties;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new Exception(e.getMessage());
		}

	}
	
	public FNObjectStore getOS() {
		return fnOS;
	}
	
	public void getVsId() throws Exception 
	{
		try {
			Document etdoc = Factory.Document.fetchInstance(fnOS.getObjectStore(), id, null);
			this.vsId = etdoc.get_VersionSeries().get_Id().toString();
			this.name = etdoc.getProperties().getStringValue("DocumentTitle");

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new Exception(e.getMessage());
		}
	}
	
	public String getVsIdByEntryTemplate(String etId) throws Exception 
	{
		try {
			this.id = etId;
			Document etdoc = Factory.Document.fetchInstance(fnOS.getObjectStore(), id, null);
			this.vsId = etdoc.get_VersionSeries().get_Id().toString();

			return this.vsId;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new Exception(e.getMessage());
		}

	}
	
	public String getEntryTemplateName() throws Exception 
	{
		try {
			Document etdoc = Factory.Document.fetchInstance(fnOS.getObjectStore(), id, null);
			this.name = etdoc.getProperties().getStringValue("DocumentTitle");
			return this.name;

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new Exception(e.getMessage());
		}

	}
	
	public TFNClass getTransport(boolean bProperties)
	{
		TFNClass tfnc = new TFNClass();
		try {
			getVsId();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(bProperties) {
			tfnc.props = new ArrayList<TFNProperty>();
			for(int i=0; i < getProperties().size(); i++)
				tfnc.props.add(properties.get(i).getTransport());
		}
		tfnc.name = name;
		tfnc.symName = symbolicName;
		tfnc.id = id;
		tfnc.type = "TEMPLATE";
		tfnc.vsid = this.vsId;
		return tfnc;
	}
	
	public TFNClass getTransportForSearch(boolean bProperties)
	{
		TFNClass tfnc = getTransport(true);
		TFNProperty tfnp = new TFNProperty();
		tfnp.desc = "Created By";
		tfnp.symName = "Creator";
		tfnp.name = "Created By";
		tfnp.dtype = "STRING";
		
		if(tfnc.props == null)
			tfnc.props = new ArrayList<TFNProperty>();
		tfnc.props.add(tfnp);
		
		tfnp = new TFNProperty();
		tfnp.desc = "Created Date";
		tfnp.symName = "DateCreated";
		tfnp.name = "Created Date";
		tfnp.dtype = "DATE";
		tfnc.props.add(tfnp);
		
		return tfnc;
	}
	
	private boolean parseEntryTemplate(String inputXML) throws Exception
	{
		try {
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			org.w3c.dom.Document doc = dBuilder.parse(new StringBufferInputStream(inputXML));
			doc.getDocumentElement().normalize();
			verifyEntryTemplateType(doc);
			getClassDetails(doc);
			setTemplateClassDetails(doc);
			getInstructions(doc);
			getFolder(doc);
		} catch (Exception ex) {
			return false;
		}
		return true;
	}
	
    private void verifyEntryTemplateType(org.w3c.dom.Document etDoc) throws Exception
    {
        NodeList typeList = etDoc.getElementsByTagName("type");
        for (int i=0; i < typeList.getLength(); i++) {
        	Node curNode = typeList.item(i);
        	if(curNode.getTextContent().equalsIgnoreCase("document"))
        		return;
        }
        
        throw new Exception("Not a document entry template");
    }

    private void getClassDetails(org.w3c.dom.Document etDoc) throws Exception
    {
        NodeList cdList = etDoc.getElementsByTagName("classdesc");
        Node classNode = null;
        for (int i=0; i < cdList.getLength();) {
        	classNode = cdList.item(i);
        	break;
        }
        if (classNode == null)
            throw new Exception("Invalid class description");

	    symbolicName = "";
        for (int i=0; i < classNode.getChildNodes().getLength(); i++) {
        	Node idNode = classNode.getChildNodes().item(i);
        	if(idNode.getNodeName().equalsIgnoreCase("id")) {
        		String etId = idNode.getTextContent();
        		FNDocumentClass fc = FNDocumentClass.getInstance(fnOS);
        		fc.setId(etId);
        		symbolicName = fc.getSymbolicNameFromID();
        		break;
        	}
        }
    }

    private void getInstructions(org.w3c.dom.Document etDoc)
    {
        NodeList iList = etDoc.getElementsByTagName("instructions");
        Node insNode = null;
        for (int i=0; i < iList.getLength();) {
        	insNode = iList.item(i);
        	break;
        }
        if (insNode == null)
            return;

        Node childNode = null;
        for (int i=0; i < insNode.getChildNodes().getLength(); i++) {
        	childNode = insNode.getChildNodes().item(i);
            if (!childNode.getNodeName().equalsIgnoreCase("instruction"))
                continue;
            Node nameNode = childNode.getFirstChild();
            Node valueNode = childNode.getLastChild();
            if (nameNode.getTextContent().equalsIgnoreCase("selectfolder"))
            {
                if (valueNode.getTextContent().trim().equalsIgnoreCase("1"))
                    selectFolder = true;
                else
                    selectFolder = false;
                continue;
            }
            if (nameNode.getTextContent().equalsIgnoreCase("constrainfolder"))
            {
                if (valueNode.getTextContent().trim().equalsIgnoreCase("1"))
                	constrainFolder = true;
                else
                	constrainFolder = false;
                continue;
            }
            if (nameNode.getTextContent().equalsIgnoreCase("setproperties"))
            {
                if (valueNode.getTextContent().trim().equalsIgnoreCase("1"))
                	setProperties = true;
                else
                	setProperties = false;
                continue;
            }
        }
    }

    private void setPropertyDescription(String propSymName, String label) {
    	if(properties == null)
    		return;
    	
    	for(FNProperty fnp:properties) {
    		if(fnp.symbolicName.equalsIgnoreCase(propSymName)) {
    			fnp.setDescription(label);
    			return;
    		}
    	}
    }
    
    private void setTemplateClassDetails(org.w3c.dom.Document etDoc) throws Exception
    {
        NodeList dList = etDoc.getElementsByTagName("propdescs");
        Node descsNode = null;
        for (int i=0; i < dList.getLength();) {
        	descsNode = dList.item(i);
        	break;
        }
        if (descsNode == null)
            return;

        Node curNode = null;
        for (int i=0; i < descsNode.getChildNodes().getLength(); i++) {
        	curNode = descsNode.getChildNodes().item(i);
        	if(!curNode.getNodeName().equalsIgnoreCase("propdesc"))
        		continue;
        	FNProperty curProp = setTemplatePropertyDetails(curNode);
        	if(curProp != null) {
        		if(properties == null)
        			properties = new ArrayList<FNProperty>();
        		properties.add(curProp);
        	}
        }
    }

    private FNProperty setTemplatePropertyDetails(Node propNode)
    {
        String dataType = "8";
        Node curNode = null;
        FNProperty curProp = new FNProperty();
        String nodeName = "";
    	String contentValue = "";
        for (int i=0; i < propNode.getChildNodes().getLength(); i++)
        {
        	curNode = propNode.getChildNodes().item(i);
        	nodeName = curNode.getNodeName();
        	contentValue = curNode.getTextContent();
        	logger.debug("Node : " + nodeName + ", Value: " + contentValue);
        	if(curNode.getNodeName().equalsIgnoreCase("symname"))
        		curProp.setSymbolicName(curNode.getTextContent().trim());
        	else if(curNode.getNodeName().equalsIgnoreCase("desctext"))
        		curProp.setDescription(curNode.getTextContent().trim());
        	else if(curNode.getNodeName().equalsIgnoreCase("name"))
        		curProp.setName(curNode.getTextContent().trim());
        	else if (curNode.getNodeName().equalsIgnoreCase("datatype"))
        		curProp.setDatatype(curNode.getTextContent().trim());
        	else if (curNode.getNodeName().equalsIgnoreCase("maxlen"))
        		curProp.setMaxLength(Utils.stringToInt(curNode.getTextContent().trim()));
        	else if (curNode.getNodeName().equalsIgnoreCase("cardinality"))
        		curProp.setCardinality(curNode.getTextContent().trim());
        	else if (curNode.getNodeName().equalsIgnoreCase("ishidden"))
        		curProp.setIsHidden(Utils.stringToBoolean(curNode.getTextContent().trim()));
        	else if (curNode.getNodeName().equalsIgnoreCase("isvalreq"))
        		curProp.setMandatory(Utils.stringToBoolean(curNode.getTextContent().trim()));
        	else if (curNode.getNodeName().equalsIgnoreCase("isreadonly"))
        		curProp.setIsReadOnly(Utils.stringToBoolean(curNode.getTextContent().trim()));
        	else if (curNode.getNodeName().equalsIgnoreCase("propdef")) {
        		if(dataType.equalsIgnoreCase("7")) {
        			Node objNode = null;
        	        for (int j=0; j < curNode.getChildNodes().getLength(); j++) {
        	        	objNode = curNode.getChildNodes().item(j);
        	        	if(objNode.getNodeName().equalsIgnoreCase("id"))
        	        		curProp.setValue(objNode.getTextContent());
        	        }
        		} else
        			curProp.setValue(curNode.getTextContent());
        	}
        }

        
        if (curProp.getSymbolicName().isEmpty())
            return null;
        
        return curProp;
    }

    private void getFolder(org.w3c.dom.Document etDoc)
    {
        NodeList fList = etDoc.getElementsByTagName("folder");
        Node fNode = null;
        for (int i=0; i < fList.getLength();)
        {
        	fNode = fList.item(i);
        	break;
        }
        if (fNode == null)
            return;
        FNFolder curFolder = FNFolder.getInstance(fnOS);
        Node curNode = null;
        for (int i=0; i < fNode.getChildNodes().getLength(); i++)
        {
        	curNode = fNode.getChildNodes().item(i);
            if (curNode.getNodeName().equalsIgnoreCase("id"))
            {
                curFolder.setId(curNode.getTextContent());
                break;
            }
        }

        if((curFolder.getId() != null) && (!curFolder.getId().isEmpty())) {
        	curFolder.load();
        	templateFolder = curFolder;
        }
        
    }
    
    private void getLookups(long empNo) throws Exception {
    	getProperties();
    	if(properties == null)
    		return;
    	if(empNo <= 0)
    		return;
    	
    	String letId = this.vsId;
    	if(letId == null)
    		letId = this.id;
    	ArrayList<TLookup> tlList = ECMLookupList.getInstance().getTemplateLookUps(empNo, letId);
    	if(tlList == null)
    		return;
    	for(FNProperty fnp: properties) {
	    	for(TLookup tl: tlList) {
	    		if(fnp.symbolicName.equalsIgnoreCase(tl.property))
	    		{
	    			fnp.setLookupType(2); // 2 for Entry Template, 1 for Choice List
	    			fnp.setLookupValues(tl.values);
	    			break;
	    		}
	    	}
    	}
    }
    
    private void getChoiceLists() throws Exception {
    	getProperties();
    	if(properties == null)
    		return;
    	ArrayList<FNProperty> classProps = getPropsWithChoiceLists();
    	if((classProps == null) || (classProps.size() <= 0))
    		return;
    	
    	for(FNProperty fnp: properties) {
	    	for(FNProperty cp: classProps) {
	    		if(fnp.symbolicName.equalsIgnoreCase(cp.symbolicName))
	    		{
	    			if((fnp.getLookupValues() != null) && (fnp.getLookupValues().size() > 0))
	    			{
		    			fnp.setLookupType(cp.getLookupType());
		    			fnp.setLookupValues(cp.getLookupValues());
	    			}
	    			break;
	    		}
	    	}
    	}	
    }
    
    private ArrayList<FNProperty> getPropsWithChoiceLists() throws Exception {
		try {
			ArrayList<FNProperty> fnPropList = new ArrayList<FNProperty>();
			ClassDefinition classDef = Factory.ClassDefinition.fetchInstance(fnOS.getObjectStore(), symbolicName, null);
			PropertyDefinitionList propList = classDef.get_PropertyDefinitions();
			if (propList != null) {
				for (int i = 0; i < propList.size(); i++) {
					boolean removeProp = true;
					PropertyDefinition propDef = (PropertyDefinition) propList.get(i);
					if (propDef.get_SymbolicName().equalsIgnoreCase("SourceDocument")
							|| propDef.get_SymbolicName().equalsIgnoreCase("RecordInformation")
							|| propDef.get_SymbolicName().equalsIgnoreCase("DestinationDocuments")) {
						removeProp = false;
					}
					FNProperty fnProp;
					if (!propDef.get_IsSystemOwned() && !propDef.get_IsHidden() && removeProp) {
						fnProp = new FNProperty();
						if (propDef.get_DisplayName() == null || propDef.get_DisplayName().equals("")) {
							if (propDef.get_DescriptiveText() == null || propDef.get_DescriptiveText().equals("")) {
								fnProp.setDescription(propDef.get_SymbolicName());
								fnProp.setName(propDef.get_SymbolicName());
							} else {
								fnProp.setDescription(propDef.get_DescriptiveText());
								fnProp.setName(propDef.get_DescriptiveText());
							}
						} else {
							fnProp.setName(propDef.get_DisplayName());
						}
						fnProp.setSymbolicName(propDef.get_SymbolicName());
						ArrayList<TLookupValue> lookups = getMarkingSet(propDef);
						if(lookups != null) {
							fnProp.setLookupType(3);
							fnProp.setLookupValues(lookups);
							continue;
						} else {
							lookups = getChoiceList(propDef);
							if(lookups != null) {
								fnProp.setLookupType(1);
								fnProp.setLookupValues(lookups);
								continue;
							} 
						}
						
						fnPropList.add(fnProp);
					}
				}
			}
			logger.info("Exit Method : getChoiceLists");
			return fnPropList;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new Exception(e.getMessage());
		}

	}
    
    private ArrayList<TLookupValue> getMarkingSet(PropertyDefinition propDef) {
    	ArrayList<TLookupValue> lookups = null;
    	try {
			if(propDef.get_DataType().toString().equalsIgnoreCase("String")) {
				PropertyDefinitionString ps = (PropertyDefinitionString)propDef;
				MarkingSet ms = ps.get_MarkingSet();
				if(ms != null) {
					MarkingList ml = ms.get_Markings();
					for(int j=0; j<ml.size(); j++) {
						TLookupValue tv = new TLookupValue();
						Marking mark = (Marking)ml.get(j);
						tv.label = mark.get_MarkingValue();
						tv.value = mark.get_MarkingValue();
						if(lookups == null)
							lookups = new ArrayList<TLookupValue>();
						lookups.add(tv);
					}
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
    	return lookups;
    }
    
    private ArrayList<TLookupValue> getChoiceList(PropertyDefinition propDef) {
    	ArrayList<TLookupValue> lookups = null;
    	try {
	    	ChoiceList cl = propDef.get_ChoiceList();
			if(cl != null) {
				Iterator iter = cl.get_ChoiceValues().iterator();
				while(iter.hasNext()) {
					Choice ch = (Choice)iter.next();
					TLookupValue tv = new TLookupValue();
					if(ch.get_ChoiceType() == ChoiceType.STRING)
						tv.value = ch.get_ChoiceStringValue();
					else if(ch.get_ChoiceType() == ChoiceType.INTEGER)
						tv.value = ch.get_ChoiceIntegerValue().toString();
					tv.label = ch.get_DisplayName();
					if(lookups == null)
						lookups = new ArrayList<TLookupValue>();
					lookups.add(tv);
				}
			}
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
		return lookups;
    }
    
    private void getPermissionAccessPolicies() throws Exception {
    	getProperties();
    	if(properties == null)
    		return;
    	
    	FNProperty apProperty = null;
    	for(FNProperty fnp: properties) {
	    	if((fnp.symbolicName != null) && (fnp.symbolicName.equalsIgnoreCase("Permission"))) {
	    		apProperty = fnp;
	    		break;
	    	}
    	}
    	if(apProperty == null)
    		return;
    	ArrayList<TFNAccessPolicy> apList = FNAccessPolicyList.getInstance(fnOS).getPermAccessPolicies();
    	ArrayList<TLookupValue> tlvalues = new ArrayList<TLookupValue>();
    	TLookupValue emptytv = new TLookupValue();
    	emptytv.value = emptytv.label = "";
    	tlvalues.add(emptytv);
    	for(TFNAccessPolicy ap: apList) {
    		TLookupValue tv = new TLookupValue();
    		tv.id = ap.id;
    		tv.value = ap.objectId;
    		tv.label = ap.desc;
    		tlvalues.add(tv);
    	}
		apProperty.setLookupValues(tlvalues);
    }
    
    private void parseJSONEntryTemplate(String inputStr) throws Exception {
    	
    	JSONObject jObj = new JSONObject(inputStr);
    	symbolicName =  getJSONString(jObj,"addClassName");
    	
    	

		JSONArray props = jObj.getJSONArray("propertiesOptions");
		for(int i=0; i < props.length(); i++) {
			JSONObject propObj = props.getJSONObject(i);
			FNProperty fnp = new FNProperty();
			fnp.setName(propObj.getString("name"));
			fnp.setDescription(fnp.name);
			fnp.setSymbolicName(propObj.getString("id"));
			String dt = getJSONString(propObj, "dataType");
			if(dt.equalsIgnoreCase("xs:string"))
				fnp.setDatatype("STRING");
			else if (dt.equalsIgnoreCase("xs:integer"))
				fnp.setDatatype("INTEGER");
			else if (dt.equalsIgnoreCase("xs:timestamp"))
				fnp.setDatatype("DATE");
			String dv = getJSONString(propObj, "defaultValue");
			if((dv != null) && (dv.length() > 0))
				fnp.setValue(dv);
			String ro = getJSONString(propObj,"readOnly");
			if((ro != null) && (ro.equalsIgnoreCase("true")))
				fnp.setIsReadOnly(true);
			else
				fnp.setIsReadOnly(false);
			String hd = getJSONString(propObj,"hidden");
			if((hd != null) && (hd.equalsIgnoreCase("true")))
				fnp.setIsHidden(true);
			else
				fnp.setIsHidden(false);
			String rq = getJSONString(propObj,"required");
			if((rq != null) && (rq.equalsIgnoreCase("true")))
				fnp.setMandatory(true);
			else
				fnp.setMandatory(false);
			
			if(properties == null)
				properties = new ArrayList<FNProperty>();
			
			properties.add(fnp);
		}
		
		String markup = getJSONString(jObj,"markup");
		if((markup == null) || (markup.length() <= 0))
			return;
		
		getPropertyLabels(markup);
		
		//Added by AKV for getting field length
		getPropertyLengths(symbolicName);
		
    }
    
    private void getPropertyLengths(String symClassName){
    	
    	try {
			FNDocumentClass fnc = FNDocumentClass.getInstance(fnOS);
			fnc.setSymbolicName(symbolicName);
			List<FNProperty> fnpList = fnc.getPropertyDefinitions();
			
			for(FNProperty fncp: fnpList) {
				String propSymName = fncp.symbolicName;
				for(FNProperty fnp:properties) {
		    		if(fnp.symbolicName.equalsIgnoreCase(propSymName)) {
		    			fnp.setMaxLength(fncp.getMaxLength());
		    			break;
		    		}
		    	}
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	
    }
    
    private void getPropertyLabels(String markup) {
    	try {
    		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
    		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
    		org.w3c.dom.Document doc = dBuilder.parse(new StringBufferInputStream(markup));
    		doc.getDocumentElement().normalize();
    		NodeList nl = doc.getChildNodes();
    		for (int i=0; i < nl.getLength(); i++) { // Layouts
    			Node lNode = nl.item(i);
    			NodeList inList = lNode.getChildNodes();
    			for(int j=0; j < inList.getLength(); j++) {
    	        	Node inNode = inList.item(j);
    	        	String attName = inNode.getAttributes().getNamedItem("data-dojo-props").getTextContent();
    	        	attName = "{" + attName.replace("\"", "") + "}";
    	        	JSONObject jObj = new JSONObject(attName);
    	        	String binding =  getJSONString(jObj,"binding");
    	        	String label = getJSONString(jObj,"label");
    	        	if((binding != null) && (label != null)) {
    	        		binding = binding.replace("'", "");
    	        		binding = binding.replace("Properties.", "");
    	        		label = label.replace("'", "");
    	        		setPropertyDescription(binding, label);
    	        	}
    			}
            }
    	} catch (Exception e) {
    		
    	}
    }
    
    private String getJSONString(JSONObject jObj, String key) {
    	String outString = null;
    	try {
    		outString = jObj.getString(key);
    	} catch (Exception e) {
    	}
    	return outString;
    }
}

