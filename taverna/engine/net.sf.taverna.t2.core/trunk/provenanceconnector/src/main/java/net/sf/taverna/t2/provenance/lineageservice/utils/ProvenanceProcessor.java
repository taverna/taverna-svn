/*******************************************************************************
 * Copyright (C) 2007 The University of Manchester   
 * 
 *  Modifications to the initial code base are copyright of their
 *  respective authors, or their employers as appropriate.
 * 
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2.1 of
 *  the License, or (at your option) any later version.
 *    
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *    
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 ******************************************************************************/
package net.sf.taverna.t2.provenance.lineageservice.utils;


/**
 * a Var that has no pName is either a WF input or output, depending on isInput
 * @author Paolo Missier
 */
public class ProvenanceProcessor {
	
	String identifier;
	String pname;
	String wfInstanceRef;
	String workflowExternalName;
	String type;
	private boolean isTopLevelProcessor;
	
	public ProvenanceProcessor() {
		
	}
	
	
	public String toString() {

		StringBuffer sb = new StringBuffer();
		sb.append("PROCESSOR: ****").
		append("\nworkflow: "+getWfInstanceRef()).
		append("\nworkflow name "+getWorkflowExternalName()).
		append("\nprocessor name: "+getPname()).
		append("\ntype: "+getType());

		return sb.toString();
	}

	
	/**
	 * @return the wfInstanceRef
	 */
	public String getWfInstanceRef() {
		return wfInstanceRef;
	}
	/**
	 * @param wfInstanceRef the wfInstanceRef to set
	 */
	public void setWfInstanceRef(String wfInstanceRef) {
		this.wfInstanceRef = wfInstanceRef;
	}
	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}
	/**
	 * @param type the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}
	/**
	 * @return the pname
	 */
	public String getPname() {
		return pname;
	}
	/**
	 * @param pname the pname to set
	 */
	public void setPname(String pname) {
		this.pname = pname;
	}
	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}
	public String getIdentifier() {
		return identifier;
	}


	/**
	 * @return the workflowExternalName
	 */
	public String getWorkflowExternalName() {
		return workflowExternalName;
	}


	/**
	 * @param workflowExternalName the workflowExternalName to set
	 */
	public void setWorkflowExternalName(String workflowExternalName) {
		this.workflowExternalName = workflowExternalName;
	}

	public void setTopLevelProcessor(boolean isTopLevelProcessor) {
		this.isTopLevelProcessor = isTopLevelProcessor;
	}


	public boolean isTopLevelProcessor() {
		return isTopLevelProcessor;
	}


}
