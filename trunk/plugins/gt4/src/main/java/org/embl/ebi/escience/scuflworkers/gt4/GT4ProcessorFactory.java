/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Wei Tan, the University of Chicago
 */

package org.embl.ebi.escience.scuflworkers.gt4;

import javax.xml.namespace.QName;
import org.embl.ebi.escience.scuflworkers.ProcessorFactory;

/**
 * A processor factory used to create GT4 processor
 * 
 * @author Wei Tan
 */

public class GT4ProcessorFactory extends ProcessorFactory
{
	
	String wsdlLocation, operationName, style;

	//QName portTypeName;

	/**
	 * Create a new factory with the specified wsdl location, port type name and
	 * operation name
	 */
	public GT4ProcessorFactory(String wsdlLocation, String operationName) {
		this.wsdlLocation = wsdlLocation;
		this.operationName = operationName;
		//TODO: portType is no longer needed??
		//this.portTypeName = portTypeName;
		setName(operationName);
	}

	public String getWSDLLocation() {
		return this.wsdlLocation;
	}

	public String getOperationName() {
		return this.operationName;
	}
/*
	public QName getPortTypeName() {
		return this.portTypeName;
	}
*/
	/**
	 * Return a description of the factory
	 */
	public String getProcessorDescription() {
		return "A GT4 processor using the wsdl document at '"
				+ wsdlLocation + "' with operation name '" + operationName
				+ "'";
	}

	@Override
	public Class getProcessorClass() {
		
		//return null;
		// TODO add the processor class here
		return org.embl.ebi.escience.scuflworkers.gt4.GT4Processor.class;
	}

	

}
