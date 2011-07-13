package uk.ac.manchester.cs.elico.rmservicetype.taverna;

import java.util.List;

/*
 * Copyright (C) 2007, University of Manchester
 *
 * Modifications to the initial code base are copyright of their
 * respective authors, or their employers as appropriate.  Authorship
 * of the modifications may be determined from the ChangeLog placed at
 * the end of this file.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.

 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.

 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

/**
 * Author: Rishi Ramgolam<br>
 * Date: Jul 13, 2011<br>
 * The University of Manchester<br>
 **/

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
