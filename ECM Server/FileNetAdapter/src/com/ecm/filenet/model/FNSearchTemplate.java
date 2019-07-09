package com.ecm.filenet.model;

import java.io.InputStream;
import java.io.StringBufferInputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.ecm.filenet.transport.TFNClass;
import com.ecm.filenet.transport.TFNProperty;
import com.ecm.filenet.util.Utils;
import com.filenet.api.collection.ContentElementList;
import com.filenet.api.core.ContentElement;
import com.filenet.api.core.ContentTransfer;
import com.filenet.api.core.Document;
import com.filenet.api.core.Factory;

public class FNSearchTemplate extends FNObject {

	private static final Logger logger = Logger.getLogger(FNEntryTemplate.class);

	protected FNObjectStore fnOS = null;
	String templateType = "DOCUMENT";
	Boolean isLoaded = false;
	
	ArrayList <FNProperty> selectProperties = null;
	ArrayList <FNProperty> filterProperties = null;
	FNFolder restrictFolder = null;
	String classSymName = "Document";
	
	public static FNSearchTemplate getInstance(FNObjectStore os){
		return new FNSearchTemplate(os);
	}
	
	private FNSearchTemplate(FNObjectStore os){
		fnOS = os;
	}
	
	public ArrayList<FNProperty> getSelectProperties() throws Exception
	{
		load();
		return selectProperties;
	}
	
	public ArrayList<FNProperty> getFilterProperties() throws Exception
	{
		load();
		return filterProperties;
	}
	
	public FNFolder getRestrictFolder() throws Exception
	{
		load();
		return restrictFolder;
	}
	
	public void load() throws Exception 
	{
		try {
			if(isLoaded)
				return;
			
			Document stdoc = Factory.Document.fetchInstance(fnOS.getObjectStore(), id, null);
			Document std = (Document)stdoc.get_CurrentVersion();
			InputStream inputStream = null;
			ContentElementList ceListOld = std.get_ContentElements();
			Iterator<ContentElement> ceItr = ceListOld.iterator();
			if (ceItr.hasNext()) {
				ContentTransfer ce = (ContentTransfer) ceItr.next();
				inputStream = (InputStream)ce.accessContentStream();
			}
			Scanner s = new Scanner(inputStream).useDelimiter("\\A");
			String result = s.hasNext() ? s.next() : "";
			parseSearchTemplate(result);
			isLoaded = true;
			logger.info("Exit Method : load");
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new Exception(e.getMessage());
		}
	}
	
	public FNObjectStore getOS() {
		return fnOS;
	}
	
	public TFNClass getTransport(boolean bProperties) throws Exception
	{
		load();
		TFNClass tfnc = new TFNClass();
		tfnc.name = name;
		tfnc.symName = classSymName;
		tfnc.id = id;
		tfnc.type = "SEARCH";
		if(bProperties) {
			tfnc.props = new ArrayList<TFNProperty>();
			if(filterProperties != null) {
				for(int i=0; i < filterProperties.size(); i++)
					tfnc.props.add(filterProperties.get(i).getTransport());
			}
		}
		return tfnc;
	}
	
	@SuppressWarnings("deprecation")
	private void parseSearchTemplate(String inputXML) throws Exception
	{
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		org.w3c.dom.Document doc = dBuilder.parse(new StringBufferInputStream(inputXML));
		doc.getDocumentElement().normalize();
		getClassDetails(doc);
		getSelectedProperties(doc);
		getFilterProperties(doc);
		getRestrictFolder(doc);
	}
	
	private void getClassDetails(org.w3c.dom.Document etDoc) throws Exception
    {
        NodeList cdList = etDoc.getElementsByTagName("subclass");
        Node classNode = null;
        for (int i=0; i < cdList.getLength(); i++) {
        	classNode = cdList.item(i);
    		if(!(classNode.getAttributes().getNamedItem("objecttype").getNodeValue().equalsIgnoreCase("document")))
    			continue;
    		this.classSymName = classNode.getAttributes().getNamedItem("symname").getNodeValue();
    		if((this.classSymName == null) || (this.classSymName.length() <= 0))
    			this.classSymName = "Document";
        	break;
        }
    }
	
	private void getSelectedProperties(org.w3c.dom.Document stDoc) throws Exception
    {
        NodeList propList = stDoc.getElementsByTagName("selectprop");
        Node propNode = null;
        for (int i=0; i < propList.getLength(); i++) {
        	propNode = propList.item(i);
    		if(!(propNode.getAttributes().getNamedItem("objecttype").getNodeValue().equalsIgnoreCase("document")))
    			continue;
    		FNProperty selProp = new FNProperty();
    		selProp.setName(propNode.getAttributes().getNamedItem("name").getNodeValue());
    		selProp.setSymbolicName(propNode.getAttributes().getNamedItem("symname").getNodeValue());
    		selProp.setSortOrder(propNode.getAttributes().getNamedItem("sortorder").getNodeValue());
    		selProp.setSortLevel(Utils.stringToInt(propNode.getAttributes().getNamedItem("sortlevel").getNodeValue()));
        	
    		if(selectProperties == null)
    			selectProperties = new ArrayList<FNProperty>();
    		selectProperties.add(selProp);
        }
    }
	
	private void getFilterProperties(org.w3c.dom.Document etDoc) throws Exception
    {
        NodeList propList = etDoc.getElementsByTagName("whereprop");
        Node propNode = null;
        for (int i=0; i < propList.getLength(); i++) {
        	propNode = propList.item(i);
    		if(!(propNode.getAttributes().getNamedItem("objecttype").getNodeValue().equalsIgnoreCase("document")))
    			continue;
    		FNProperty selProp = new FNProperty();
    		selProp.setName(propNode.getAttributes().getNamedItem("name").getNodeValue());
    		selProp.setSymbolicName(propNode.getAttributes().getNamedItem("symname").getNodeValue());
    		String editProp = propNode.getAttributes().getNamedItem("editproperty").getNodeValue();
    		if (editProp.equalsIgnoreCase("editable")) {
    			selProp.setIsHidden(false);
    			selProp.setIsReadOnly(false);
            } else if (editProp.equalsIgnoreCase("readonly")) {
            	selProp.setIsHidden(false);
    			selProp.setIsReadOnly(true);
            } else if (editProp.equalsIgnoreCase("hidden")) {
            	selProp.setIsHidden(true);
    			selProp.setIsReadOnly(false);
            }  else if (editProp.equalsIgnoreCase("required")) {
            	selProp.setMandatory(true);
            }
            
    		for(int j=0; j < propNode.getChildNodes().getLength(); j++) {
    			Node subNode = propNode.getChildNodes().item(j);
    			if(subNode.getNodeName().equalsIgnoreCase("propdesc")) {
    				//NamedNodeMap nnm = subNode.getAttributes();
    				//Node dtNode = nnm.getNamedItem("datatype");
    				selProp.setDatatype(getDataTypeString(subNode.getAttributes().getNamedItem("datatype").getNodeValue()));
    				break;
    			}
    		}
    		
    		selProp.setOperator(propNode.getParentNode().getTextContent());
    		
    		if(filterProperties == null)
    			filterProperties = new ArrayList<FNProperty>();
    		filterProperties.add(selProp);
        }
    }
	
	private void getRestrictFolder(org.w3c.dom.Document stDoc) throws Exception
    {
        NodeList folderList = stDoc.getElementsByTagName("folder");
        Node folderNode = null;
        for (int i=0; i < folderList.getLength(); i++) {
        	folderNode = folderList.item(i);
    		
    		restrictFolder = FNFolder.getInstance(fnOS);
    		restrictFolder.setId(folderNode.getAttributes().getNamedItem("id").getNodeValue());
    		restrictFolder.setPath(folderNode.getAttributes().getNamedItem("pathname").getNodeValue());
        }
    }
	
	private String getDataTypeString(String dtString)
	{
		if(dtString != null)
		{
			 if(dtString.equalsIgnoreCase("typestring"))
				 return "STRING";
			 else if (dtString.equalsIgnoreCase("typedate"))
				 return "DATE";
			 else if (dtString.equalsIgnoreCase("typelong"))
				 return "LONG";
			 else if (dtString.equalsIgnoreCase("typeboolean"))
				 return "BOOLEAN";
			 else if (dtString.equalsIgnoreCase("typedouble"))
				 return "DOUBLE";
		}
		return "STRING";
	}
}
