package uk.ac.manchester.cs.elico.rmservicetype.taverna;

import java.util.List;

public class RapidMinerParameterDescription implements Comparable<RapidMinerParameterDescription>{

	String parameterName;
	String description;
	String expert;
	String mandatory;
	String max;
	String min;
	String defaultValue;
	String type;
	String executionValue;
	List<String> choices;
	boolean useParameter;
	
	// Setters
	public void setUseParameter(boolean val) {
		useParameter = val;
	}
	
	public void setParameterName(String name) {
		parameterName = name;
	}
	
	public void setDescription(String desc) {
		description = desc;
	}
	
	public void setExpert(String val) { 
		expert = val;
	}
	
	public void setMandatory(String val) {
		mandatory = val;
	}
	
	public void setMax(String val) {
		max = val;
	}
	
	public void setMin(String val) {
		min = val;
	}
	
	//intially set the execution value as the default value if set
	public void setDefaultValue(String object) {
		defaultValue = object;
		executionValue = object;
	}
	
	public void setExecutionValue(String object) {
		executionValue = object;
	}
	
	public void setType(String val) {
		type = val;
	}
	
	public void setChoices(List<String> choiceList) {
		choices = choiceList;
	}
	
	public String getParameterName() {
		return parameterName;
	}
	
	public String getDescription() {
		return description;
	}
	
	public String getExpert() {
		return expert;
	}
	
	public String getMandatory() {
		return mandatory;
	}
	
	public String getMax() {
		return max;
	}
		
	public String getMin() {
		return min;
	}
	
	public String getDefaultValue() {
		return defaultValue;
	}
	
	public String getType() {
		return type;
	}
	
	public List<String> getChoices() {
		return choices;
	}
	
	public String getExecutionValue() {
		return executionValue;
	}
	
	public boolean getUseParameter() {
		return useParameter;
	}

	public int compareTo(RapidMinerParameterDescription compareObject) {
		
	   //int lastCmp = lastName.compareTo(n.lastName);
       // return (lastCmp != 0 ? lastCmp :
       //         firstName.compareTo(n.firstName));
	
		int lastCmp = parameterName.compareTo(compareObject.getParameterName());
			
		return lastCmp;
	}
	
}
