package com.ecm.filenet.model;

import java.util.Date;
import java.util.ArrayList;
import java.util.List;

import com.ecm.db.list.ECMAdministrationList;
import com.ecm.db.transport.TLookup;
import com.ecm.db.transport.TLookupValue;
import com.ecm.db.util.DBUtil;
import com.ecm.filenet.transport.TFNProperty;
import com.ecm.filenet.util.Utils;
import com.filenet.api.collection.StringList;
import com.filenet.api.core.Factory;
import com.filenet.api.collection.DateTimeList;
import com.filenet.api.collection.Float64List;
import com.filenet.api.collection.Integer32List;
import com.filenet.api.property.Properties;
import com.filenet.api.property.Property;
import com.filenet.api.util.Id;

public class FNProperty extends FNObject {
	private String value;
	private String description;
	private String cardinality = "SINGLE";
	private Boolean isMandatory = false;
	private int maxLength = 64;
	private String datatype = "STRING";
	private int lookupType = 0;
	private String isSettable = "FALSE";
	private ArrayList<TLookupValue> lookupValues;
	private Boolean isHidden = false;
	private Boolean isReadOnly = false;
	private boolean isMultiple = false;
	private String DescriptiveText;
	private String docClassName;
	private ArrayList<String> propertyMultiValues;
	private String operator;
	private String sortOrder;
	private int sortLevel;
	
	public String getName() {
		return name;
	}
	public void setName(String propertyName) {
		this.name = propertyName;
	}
	public String getValue() {
		return value;
	}
	public String getID() {
		return id;
	}
	public void setID(String propertyID) {
		this.value = propertyID;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String descriptiveText) {
		this.description = descriptiveText;
	}
	public String getCardinality() {
		return cardinality;
	}
	public void setCardinality(String cardinality) {
		this.cardinality = cardinality;
		if(this.cardinality.equalsIgnoreCase("2"))
			this.cardinality = "MULTI";
		else
			this.cardinality = "SINGLE";
	}
	public Boolean isMandatory() {
		return isMandatory;
	}
	public void setMandatory(Boolean mandatory) {
		this.isMandatory = mandatory;
	}
	public Boolean isReadOnly() {
		return isReadOnly;
	}
	public void setIsReadOnly(Boolean readOnly) {
		this.isReadOnly = readOnly;
	}
	public int getMaxLength() {
		return maxLength;
	}
	public void setMaxLength(int maxLength) {
		this.maxLength = maxLength;
	}
	public String getDatatype() {
		return datatype;
	}
	public void setDatatype(String datatype) {
		this.datatype = datatype;
		if(this.datatype == null)
			return;
		if(this.datatype.trim().equals("8"))
			this.datatype = "STRING";
		else if(this.datatype.trim().equals("7"))
			this.datatype = "OBJECT";
		else if(this.datatype.trim().equals("6"))
			this.datatype = "LONG";
		else if(this.datatype.trim().equals("2"))
			this.datatype = "BOOLEAN";
		else if(this.datatype.trim().equals("5"))
			this.datatype = "GUID";
		else if(this.datatype.trim().equals("3"))
			this.datatype = "DATE";
	}
	public int getLookupType() {
		return lookupType;
	}
	public void setLookupType(int ltype) {
		this.lookupType = ltype;
	}
	public String getSymbolicName() {
		return symbolicName;
	}
	public void setSymbolicName(String symbolicName) {
		this.symbolicName = symbolicName;
	}
	public String getIsSettable() {
		return isSettable;
	}
	public void setIsSettable(String isSettable) {
		this.isSettable = isSettable;
	}
	public ArrayList<TLookupValue> getLookupValues() {
		return lookupValues;
	}
	public void setLookupValues(ArrayList<TLookupValue> lookupValues) {
		this.lookupValues = lookupValues;
	}
	public Boolean getIsHidden() {
		return isHidden;
	}
	public void setIsHidden(Boolean isHidden) {
		this.isHidden = isHidden;
	}
	public boolean isMultiple() {
		return isMultiple;
	}
	public void setMultiple(boolean isMultiple) {
		this.isMultiple = isMultiple;
	}
	public ArrayList<String> getPropertyMultiValues() {
		return propertyMultiValues;
	}
	public void setMultiValues(ArrayList<String> propertyMultiValues) {
		this.propertyMultiValues = propertyMultiValues;
	}
	public String getDescriptiveText() {
		return DescriptiveText;
	}
	public void setDescriptiveText(String descriptiveText) {
		DescriptiveText = descriptiveText;
	}
	public String getDocClassName() {
		return docClassName;
	}
	public void setDocClassName(String docClassName) {
		this.docClassName = docClassName;
	}
	public void addMultiValue(String propValue)
	{
		if(propertyMultiValues == null)
			propertyMultiValues = new ArrayList<String>();
		for(int i=0; i<propertyMultiValues.size(); i++)
		{
			if(propertyMultiValues.get(i).equalsIgnoreCase(propValue.trim()))
				return;
		}
		propertyMultiValues.add(propValue);
	}
	public void setQueryValue(String propertyValue) {
		this.value = propertyValue;
	}
	public void setValue(String propertyValue) {
		if(this.cardinality.equalsIgnoreCase("MULTI"))
			addMultiValue(propertyValue);
		else
			this.value = propertyValue;
	}
	public String getOperator() {
		return operator;
	}
	public void setOperator(String oper) {
		this.operator = oper;
	}
	public String getSortOrder() {
		return sortOrder;
	}
	public void setSortOrder(String sort) {
		this.sortOrder = sort;
	}
	public int getSortLevel() {
		return sortLevel;
	}
	public void setSortLevel(int sort) {
		this.sortLevel = sort;
	}
	
	
	
	public TFNProperty getTransport()
	{
		TFNProperty tfnp = new TFNProperty();
		tfnp.id = this.id;
		tfnp.name = this.name;
		tfnp.symName = this.symbolicName;
		tfnp.dtype = this.datatype;
		tfnp.desc = this.description;
		tfnp.rOnly = this.isReadOnly.toString();
		tfnp.hidden = this.isHidden.toString();
		tfnp.req = this.isMandatory.toString();
		tfnp.len = maxLength;
		tfnp.oper = operator;
		tfnp.ltype = this.lookupType;
		
		if(this.cardinality.equalsIgnoreCase("MULTI")) {
			tfnp.mtype = "Y";
			if(propertyMultiValues != null) {
				for(int i=0; i<propertyMultiValues.size(); i++)
					if(propertyMultiValues.get(i) != null)
						tfnp.mvalues.add(propertyMultiValues.get(i));
			}
		} else {
			tfnp.mtype = "N";
			if(this.value != null)
				tfnp.mvalues.add(this.value);
		}
	
		if(lookupValues != null)
		{
			for(int i=0; i<lookupValues.size(); i++) {
				if(tfnp.lookups == null)
					tfnp.lookups = new ArrayList<TLookupValue>();
				tfnp.lookups.add(lookupValues.get(i));
			}
		}
		return tfnp;
	}
	
	public void loadFromTransport(TFNProperty tfnp) 
	{
		if(tfnp == null)
			return;
		
		id = tfnp.id;
		name = tfnp.name;
		description = tfnp.desc;
		symbolicName = tfnp.symName;
		datatype = tfnp.dtype;
		maxLength = tfnp.len;
		operator = tfnp.oper;
		
		if(tfnp.mtype.equalsIgnoreCase("Y")) {
			this.cardinality = "MULTI";
			propertyMultiValues = new ArrayList<String>();
			if(tfnp.mvalues != null) {
				for(int i=0; i<tfnp.mvalues.size(); i++)
					propertyMultiValues.add(tfnp.mvalues.get(i));
			}
		} else {
			this.cardinality = "SINGLE";
			if((tfnp.mvalues != null) && tfnp.mvalues.size() > 0)
				this.value = tfnp.mvalues.get(0);
		}
	
		if(tfnp.lookups != null)
		{
			for(int i=0; i<tfnp.lookups.size(); i++)
				lookupValues.add(tfnp.lookups.get(i));
		}
	}
	
	private void putSingleValues(Properties props) 
	{

		try {
			if(this.datatype.equalsIgnoreCase("STRING")) {
				props.putValue(symbolicName, value);
			} else if(this.datatype.equalsIgnoreCase("INTEGER") ||
					this.datatype.equalsIgnoreCase("LONG")) {
				props.putValue(symbolicName,Integer.parseInt(value));
			} else if (this.datatype.equalsIgnoreCase("Double")){
				props.putValue(symbolicName,Double.parseDouble(value));
			}else if (this.datatype.equalsIgnoreCase("Float")) {
				props.putValue(symbolicName,Float.parseFloat(value));
			}else if (this.datatype.equalsIgnoreCase("Boolean")){
				props.putValue(symbolicName,Boolean.parseBoolean(value));
			}else if (this.datatype.equalsIgnoreCase("ID")){
				props.putValue(symbolicName, new Id(value));
			}else if (this.datatype.equalsIgnoreCase("Date")){
				Date dt = Utils.convertStringToDateEx(value);
				//Date dt = Utils.getDateFromString(value);
				if(dt != null)
					props.putValue(symbolicName, dt);
			}
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}
	}
	
	private void putMultipleValues(Properties props)
	{
		try {
			if (this.datatype.equalsIgnoreCase("String")){
				StringList sl = Factory.StringList.createList();

				for (int i = 0; i < propertyMultiValues.size(); i++){
					sl.add(propertyMultiValues.get(i));
				}
				props.putValue(symbolicName, sl);

			}else if ((datatype.equalsIgnoreCase("Integer") || datatype.equalsIgnoreCase("Long"))){
				Integer32List integer_list = Factory.Integer32List.createList();

				for (int i = 0; i < propertyMultiValues.size(); i++){
					integer_list.add(Integer.parseInt(propertyMultiValues.get(i)));
				}
				props.putValue(symbolicName, integer_list);
			}else if (datatype.equalsIgnoreCase("Double")){
				com.filenet.api.collection.Float64List float_list = Factory.Float64List.createList();

				for (int i = 0; i < propertyMultiValues.size(); i++){
					float_list.add(Double.parseDouble((propertyMultiValues.get(i))));
				}
				props.putValue(symbolicName, float_list);
			}else if (datatype.equalsIgnoreCase("Date")){
				DateTimeList dateTimeList = Factory.DateTimeList.createList();

				for (int i = 0; i < propertyMultiValues.size(); i++){
					Date dt = Utils.convertStringToDateEx(value);
					if(dt != null)
						dateTimeList.add(dt);
				}
				props.putValue(symbolicName, dateTimeList);
			}
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}
	}
	
	public void putPropertyValue(Properties props)
	{
		if(this.cardinality.equalsIgnoreCase("MULTI") || 
				this.cardinality.equalsIgnoreCase("LIST")) 
			putMultipleValues(props);
		else
			putSingleValues(props);
	}
	
	public void setPropertyValue(Property prop)
	{
		if(this.cardinality.equalsIgnoreCase("MULTI") || 
				this.cardinality.equalsIgnoreCase("LIST")) 
			setMultipleValue(prop);
		else
			setSingleValue(prop);
	}
	
	private void setSingleValue(Property prop) 
	{
		if(this.datatype.equalsIgnoreCase("STRING")) {
			setValue(prop.getStringValue());
		} else if(this.datatype.equalsIgnoreCase("INTEGER") ||
				this.datatype.equalsIgnoreCase("LONG")) {
			setValue(prop.getInteger32Value().toString());
		} else if (this.datatype.equalsIgnoreCase("Double")){
			setValue(prop.getFloat64Value().toString());
		}else if (this.datatype.equalsIgnoreCase("Float")) {
			setValue(prop.getFloat64Value().toString());
		}else if (this.datatype.equalsIgnoreCase("Boolean")){
			setValue(prop.getBooleanValue().toString());
		}else if (this.datatype.equalsIgnoreCase("ID")){
			setValue(prop.getIdValue().toString());
		}else if (this.datatype.equalsIgnoreCase("Date")){
			if(prop.getPropertyName().equalsIgnoreCase("documentdate") || prop.getPropertyName().equalsIgnoreCase("receivedon") )
				setValue(Utils.formatDateForDocumentDate(prop.getDateTimeValue()));
			else
				setValue(Utils.formatDateForUI(prop.getDateTimeValue()));
		}
	}
	
	private void setMultipleValue(Property prop)
	{
		if (this.datatype.equalsIgnoreCase("String")){
			for (int i = 0; i < prop.getStringListValue().size(); i++){
				setValue(prop.getStringListValue().get(i).toString());
			}
		}else if ((datatype.equalsIgnoreCase("Integer") || datatype.equalsIgnoreCase("Long"))){
			for (int i = 0; i < prop.getInteger32ListValue().size(); i++){
				setValue(prop.getInteger32ListValue().get(i).toString());
			}
		}else if (datatype.equalsIgnoreCase("Double")){
			for (int i = 0; i < prop.getFloat64ListValue().size(); i++){
				setValue(prop.getFloat64ListValue().get(i).toString());
			}
		}else if (datatype.equalsIgnoreCase("Date")){
			for (int i = 0; i < prop.getDateTimeListValue().size(); i++){
				setValue(Utils.formatDateForUI((Date)prop.getDateTimeListValue().get(i)));
			}
		}
	}
}
