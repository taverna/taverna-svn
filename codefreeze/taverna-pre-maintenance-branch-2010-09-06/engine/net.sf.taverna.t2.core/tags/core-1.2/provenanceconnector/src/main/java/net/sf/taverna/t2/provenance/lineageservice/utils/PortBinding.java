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
 * @author Paolo Missier
 *
 */
public class PortBinding {

	private String workflowId;
	private String portName;
	private String workflowRunId;
	private String value;
	private String collIDRef;
	private int positionInColl;
	private String processorName;
	private String valueType;
	private String reference;
	private String iteration;
	private String resolvedValue;
	
	
	public PortBinding(){}
	
	public PortBinding(PortBinding vb) {
		workflowId = vb.workflowId;
		portName = vb.portName;
		workflowRunId = vb.workflowRunId;
		value = vb.value;
		collIDRef = vb.collIDRef;
		positionInColl = vb.positionInColl;
		processorName = vb.processorName;
		valueType = vb.valueType;
		reference = vb.reference;
		iteration = vb.iteration;
		resolvedValue = vb.resolvedValue;
	}


	
	
	
	@Override
	public String toString() {
		return "PortBinding [collIDRef=" + collIDRef + ", iteration="
				+ iteration + ", portName=" + portName + ", positionInColl="
				+ positionInColl + ", processorName=" + processorName
				+ ", reference=" + reference + ", resolvedValue="
				+ resolvedValue + ", value=" + value + ", valueType="
				+ valueType + ", workflowId=" + workflowId + ", workflowRunId="
				+ workflowRunId + "]";
	}

	/**
	 * @return the positionInColl
	 */
	public int getPositionInColl() {
		return positionInColl;
	}
	/**
	 * @param positionInColl the positionInColl to set
	 */
	public void setPositionInColl(int positionInColl) {
		this.positionInColl = positionInColl;
	}
	/**
	 * @return the valueType
	 */
	public String getValueType() {
		return valueType;
	}
	/**
	 * @param valueType the valueType to set
	 */
	public void setValueType(String valueType) {
		this.valueType = valueType;
	}
	/**
	 * @return the portName
	 */
	public String getPortName() {
		return portName;
	}
	/**
	 * @param portName the portName to set
	 */
	public void setPortName(String portName) {
		this.portName = portName;
	}
	/**
	 * @return the workflowRunId
	 */
	public String getWorkflowRunId() {
		return workflowRunId;
	}
	/**
	 * @param workflowRunId the workflowRunId to set
	 */
	public void setWorkflowRunId(String workflowRunId) {
		this.workflowRunId = workflowRunId;
	}
	/**
	 * @return the processorName
	 */
	public String getProcessorName() {
		return processorName;
	}
	/**
	 * @param processorName the processorName to set
	 */
	public void setProcessorName(String processorName) {
		this.processorName = processorName;
	}
	/**
	 * @return the collIDRef
	 */
	public String getCollIDRef() {
		return collIDRef;
	}
	/**
	 * @param collIDRef the collIDRef to set
	 */
	public void setCollIDRef(String collIDRef) {
		this.collIDRef = collIDRef;
	}
	/**
	 * @return the iteration
	 */
	public String getIteration() {
		return iteration;
	}
	/**
	 * @param iterationVector the iteration to set
	 */
	public void setIteration(String iterationVector) {
		this.iteration = iterationVector;
	}
	/**
	 * @return the value
	 */
	public String getValue() {
		return value;
	}
	/**
	 * @param value the value to set
	 */
	public void setValue(String value) {
		this.value = value;
	}
	/**
	 * @return the ref
	 */
	public String getReference() {
		return reference;
	}
	/**
	 * @param ref the ref to set
	 */
	public void setReference(String ref) {
		this.reference = ref;
	}


	/**
	 * @return the resolvedValue
	 */
	public String getResolvedValue() {
		return resolvedValue;
	}


	/**
	 * @param resolvedValue the resolvedValue to set
	 */
	public void setResolvedValue(String resolvedValue) {
		this.resolvedValue = resolvedValue;
	}


	/**
	 * @return the workflowId
	 */
	public String getWorkflowId() {
		return workflowId;
	}


	/**
	 * @param workflowId the workflowId to set
	 */
	public void setWorkflowId(String workflowId) {
		this.workflowId = workflowId;
	}
	
	
}
