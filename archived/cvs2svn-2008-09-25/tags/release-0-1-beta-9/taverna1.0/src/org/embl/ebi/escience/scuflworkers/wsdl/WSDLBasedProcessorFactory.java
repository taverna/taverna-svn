/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflworkers.wsdl;
import org.embl.ebi.escience.scufl.DuplicateProcessorNameException;
import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scufl.ProcessorCreationException;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scuflworkers.ProcessorFactory;

import org.embl.ebi.escience.scuflworkers.wsdl.WSDLBasedProcessor;
import java.lang.Class;
import java.lang.String;



/**
 * Implementation of ProcessorFactory that creates
 * WSDLBasedProcessor nodes
 * @author Tom Oinn
 */
public class WSDLBasedProcessorFactory implements ProcessorFactory {

    String wsdlLocation, portTypeName, operationName, operationStyle, targetEndpoint;

    /**
     * Create a new factory with the specified wsdl location,
     * port type name and operation name
     */
    public WSDLBasedProcessorFactory(String wsdlLocation, String portTypeName, String operationName, String operationStyle) {
	this.wsdlLocation = wsdlLocation;
	this.portTypeName = portTypeName;
	this.operationName = operationName;
	this.operationStyle = operationStyle;
    }

    /**
     * Return the operation name as the toString result
     */
    public String toString() {
	return this.operationName;
    }

    /**
     * Create a new WSDLBasedProcessor and add it to the model
     */
    public Processor createProcessor(String name, ScuflModel model) 
	throws ProcessorCreationException,
	       DuplicateProcessorNameException {
	Processor theProcessor = new WSDLBasedProcessor(model, name, this.wsdlLocation, this.operationName);
	if (model!=null) {
	    model.addProcessor(theProcessor);
	}
	return theProcessor;
    }

    /**
     * Return a description of the factory
     */
    public String getProcessorDescription() {
	return "A WSDL based processor using the wsdl document at '"+wsdlLocation+"', port type '"+portTypeName+"' and operation name '"+operationName+"'";
    }

    /**
     * Return the Class object for processors that would
     * be created by this factory
     */
    public Class getProcessorClass() {
	return org.embl.ebi.escience.scuflworkers.wsdl.WSDLBasedProcessor.class;
    }

}
