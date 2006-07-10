/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflworkers.wsdl;

import javax.xml.namespace.QName;

import org.embl.ebi.escience.scuflworkers.ProcessorFactory;

/**
 * Implementation of ProcessorFactory that creates WSDLBasedProcessor nodes
 * 
 * @author Tom Oinn
 */
public class WSDLBasedProcessorFactory extends ProcessorFactory {

	String wsdlLocation, operationName, style;

	QName portTypeName;

	/**
	 * Create a new factory with the specified wsdl location, port type name and
	 * operation name
	 */
	public WSDLBasedProcessorFactory(String wsdlLocation, String operationName, QName portTypeName) {
		this.wsdlLocation = wsdlLocation;
		this.operationName = operationName;
		this.portTypeName = portTypeName;
		setName(operationName);
	}

	public String getWSDLLocation() {
		return this.wsdlLocation;
	}

	public String getOperationName() {
		return this.operationName;
	}

	public QName getPortTypeName() {
		return this.portTypeName;
	}

	/**
	 * Return a description of the factory
	 */
	public String getProcessorDescription() {
		return "A WSDL based processor using the wsdl document at '" + wsdlLocation + "' with operation name '"
				+ operationName + "'";
	}

	/**
	 * Return the Class object for processors that would be created by this
	 * factory
	 */
	public Class getProcessorClass() {
		return org.embl.ebi.escience.scuflworkers.wsdl.WSDLBasedProcessor.class;
	}

}
