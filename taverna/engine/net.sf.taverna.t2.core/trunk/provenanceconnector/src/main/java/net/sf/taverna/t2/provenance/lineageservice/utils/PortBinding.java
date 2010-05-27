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

	String wfNameRef;
	String varNameRef;
	String wfInstanceRef;
	String value;
	String collIDRef;
	int positionInColl;
	String processorNameRef;
	String valueType;
	String ref;
	String    iterationVector;
	String resolvedValue;
	
	
	public PortBinding(){}
	
	public PortBinding(PortBinding vb) {
		wfNameRef = vb.wfNameRef;
		varNameRef = vb.varNameRef;
		wfInstanceRef = vb.wfInstanceRef;
		value = vb.value;
		collIDRef = vb.collIDRef;
		positionInColl = vb.positionInColl;
		processorNameRef = vb.processorNameRef;
		valueType = vb.valueType;
		ref = vb.ref;
		iterationVector = vb.iterationVector;
		resolvedValue = vb.resolvedValue;
	}


	public String toString() {
	
		StringBuffer sb = new StringBuffer();
		
		sb.append("**** PortBinding: \n").
			append("wfInstanceRef = "+wfInstanceRef+"\n").
			append("wfNameRef = "+wfNameRef+"\n").
			append("processorNameRef = "+processorNameRef+"\n").
			append("varNameRef = "+varNameRef+"\n").
			append("iteration = "+iterationVector+"\n").
			append("collIdef  " +collIDRef+"\n").
			append("positionInColl = "+positionInColl+"\n").
			append("value = "+value+"\n").
			append("ref = "+ref+"\n").
			append("resolvedValue = "+resolvedValue+"\n");
			

		return sb.toString();
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
	 * @return the varNameRef
	 */
	public String getVarNameRef() {
		return varNameRef;
	}
	/**
	 * @param varNameRef the varNameRef to set
	 */
	public void setVarNameRef(String varNameRef) {
		this.varNameRef = varNameRef;
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
	 * @return the processorNameRef
	 */
	public String getprocessorNameRef() {
		return processorNameRef;
	}
	/**
	 * @param nameRef the processorNameRef to set
	 */
	public void setprocessorNameRef(String nameRef) {
		processorNameRef = nameRef;
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
		return iterationVector;
	}
	/**
	 * @param iterationVector the iteration to set
	 */
	public void setIterationVector(String iterationVector) {
		this.iterationVector = iterationVector;
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
	public String getRef() {
		return ref;
	}
	/**
	 * @param ref the ref to set
	 */
	public void setRef(String ref) {
		this.ref = ref;
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
	 * @return the wfNameRef
	 */
	public String getWfNameRef() {
		return wfNameRef;
	}


	/**
	 * @param wfNameRef the wfNameRef to set
	 */
	public void setWfNameRef(String wfNameRef) {
		this.wfNameRef = wfNameRef;
	}
	
	
}
