package com.ecm.filenet.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import com.ecm.filenet.transport.TFNClass;
import com.ecm.filenet.transport.TFNProperty;
import com.filenet.api.admin.ClassDefinition;
import com.filenet.api.admin.PropertyDefinition;
import com.filenet.api.collection.PropertyDefinitionList;
import com.filenet.api.core.Factory;
import com.filenet.api.property.Property;

public abstract class FNClass extends FNObject {
	private static final Logger logger = Logger.getLogger(FNClass.class);

	private Boolean isQuery = false;
	protected FNObjectStore fnOS = null;
	protected String classType = "NONE";
	
	ArrayList <FNProperty> properties = null;
	
	public Boolean isQuery() {
		return isQuery;
	}
	public void setQuery(Boolean isSearch) {
		this.isQuery = isSearch;
	}
	
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
	
	public ArrayList<TFNProperty> getPropertiesTransport() {
		getProperties();
		if(properties == null)
			return null;
		ArrayList<TFNProperty> propList = null;
		for(FNProperty fnp: properties) {
			TFNProperty tfnp = fnp.getTransport();
			if(propList == null)
				propList = new ArrayList<TFNProperty>();
			propList.add(tfnp);
		}
		return propList;
	}
	
	public void setProperties(ArrayList<FNProperty> props) {
		this.properties.clear();
		this.properties.addAll(props);
	}
	public void setProperty(String propSymName, String propValue) {
		for(int i=0; i<properties.size(); i++)
			if(properties.get(i).getSymbolicName().equalsIgnoreCase(propSymName))
				properties.get(i).setValue(propValue);
	}
	
	public String getSymbolicNameFromID() {
		try {
			ClassDefinition classDef = Factory.ClassDefinition.fetchInstance(fnOS.getObjectStore(), id, null);
			return classDef.get_SymbolicName();
		} catch (Exception e) {
			return id;
		}
	}
	
	public List<FNProperty> getPropertyDefinitions() throws Exception 
	{
		try {
			if(properties == null)
				properties = new ArrayList<FNProperty>();
			
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
							fnProp.setDescription(propDef.get_DisplayName());
						}
						fnProp.setSymbolicName(propDef.get_SymbolicName());
						String defaultPropertyValue = getDefaultValuesForPropDef(propDef);
						if (propDef.get_DataType().toString().equalsIgnoreCase("String")) {
							int maxLength = propDef.getProperties().getInteger32Value("MaximumLengthString");
							fnProp.setMaxLength(maxLength);
						}

						fnProp.setValue(defaultPropertyValue);
						fnProp.setDatatype(propDef.get_DataType().toString());
						fnProp.setCardinality(propDef.get_Cardinality().toString());
						fnProp.setMandatory(propDef.get_IsValueRequired());
						
						fnProp.setLookupType(0);
						
						properties.add(fnProp);
					}
				}
			}
			logger.info("Exit Method : getPropertyDefinitions");
			return properties;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new Exception(e.getMessage());
		}

	}

	private String getDefaultValuesForPropDef(PropertyDefinition pd) throws Exception {
		logger.info("Started Method : getDefaultValuesForPropDef");
		String defaultPropertyValue = null;
		try {
			String dataType = pd.get_DataType().toString();
			if (dataType.equalsIgnoreCase("String")) {
				if (pd.getProperties().getObjectValue("PropertyDefaultString") != null)
					defaultPropertyValue = pd.getProperties().getObjectValue("PropertyDefaultString").toString();
			} else if (dataType.equalsIgnoreCase("Date")) {
				if (pd.getProperties().getObjectValue("PropertyDefaultDateTime") != null) {
					//Date date = pd.getProperties().getDateTimeValue("PropertyDefaultDateTime");
					// defaultPropertyValue
					// =com.ecm.filenet.model.util.Utils.formatDate(date);
				}
			} else if (dataType.equalsIgnoreCase("boolean")) {
				if (pd.getProperties().getObjectValue("PropertyDefaultBoolean") != null)
					defaultPropertyValue = pd.getProperties().getObjectValue("PropertyDefaultBoolean").toString();
			} else if (dataType.equalsIgnoreCase("Integer")) {
				if (pd.getProperties().getObjectValue("PropertyDefaultInteger32") != null)
					defaultPropertyValue = pd.getProperties().getObjectValue("PropertyDefaultInteger32").toString();
			} else if (dataType.equalsIgnoreCase("ID")) {
				if (pd.getProperties().getObjectValue("PropertyDefaultId") != null)
					defaultPropertyValue = pd.getProperties().getObjectValue("PropertyDefaultId").toString();
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new Exception(e);
		}
		logger.info("Exit Method : getDefaultValuesForPropDef");
		return defaultPropertyValue;
	}
	
	private FNProperty findProperty(String propName)
	{
		if(properties == null)
			return null;
		
		for(int i=0; i < properties.size(); i++) {
			FNProperty fnp = properties.get(i);
			if(fnp.getSymbolicName().equalsIgnoreCase(propName))
				return fnp;
		}
		return null;
	}
	
	public void setPropertyValue(Property prop)
	{
		getProperties();
		FNProperty curProp = findProperty(prop.getPropertyName());
		if(curProp != null)
			curProp.setPropertyValue(prop);
	}
	
	public FNObjectStore getOS() {
		return fnOS;
	}
	public String getClassType() {
		return classType;
	}
	protected void copyFrom(FNClass fnc)
	{
		this.classType = fnc.getClassType();
		this.fnOS = fnc.getOS();
		this.id = fnc.getId();
		this.name = fnc.getName();
		this.properties = fnc.getProperties();
		this.symbolicName = fnc.getSymbolicName();
		this.isQuery = fnc.isQuery();
	}
	
	public TFNClass getTransport()
	{
		TFNClass tfnc = new TFNClass();
		tfnc.name = name;
		tfnc.symName = symbolicName;
		tfnc.id = id;
		tfnc.type = classType;
		tfnc.props = new ArrayList<TFNProperty>();
		for(int i=0; i < getProperties().size(); i++)
			tfnc.props.add(properties.get(i).getTransport());
		
		return tfnc;
	}
	
	public void getFromTransport(TFNClass tfnc)
	{
		this.name = tfnc.name;
		this.symbolicName = tfnc.symName;
		this.id = tfnc.id;
		this.classType = tfnc.type;
		this.properties = new ArrayList<FNProperty>();
		for(TFNProperty tfnp: tfnc.props) {
			FNProperty fnp = new FNProperty();
			fnp.loadFromTransport(tfnp);
			this.properties.add(fnp);
		}
	}
}
