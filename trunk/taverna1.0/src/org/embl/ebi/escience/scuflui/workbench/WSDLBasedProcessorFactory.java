/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflui.workbench;
import org.embl.ebi.escience.scufl.DuplicateProcessorNameException;
import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scufl.ProcessorCreationException;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scuflworkers.wsdl.WSDLBasedProcessor;




/**
 * Implementation of ProcessorFactory that creates
 * WSDLBasedProcessor nodes
 * @author Tom Oinn
 */
public class WSDLBasedProcessorFactory implements ProcessorFactory {

    String wsdlLocation, portTypeName, operationName;

    /**
     * Create a new factory with the specified wsdl location,
     * port type name and operation name
     */
    public WSDLBasedProcessorFactory(String wsdlLocation, String portTypeName, String operationName) {
	this.wsdlLocation = wsdlLocation;
	this.portTypeName = portTypeName;
	this.operationName = operationName;
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
    public void createProcessor(String name, ScuflModel model) 
	throws ProcessorCreationException,
	       DuplicateProcessorNameException {
	Processor theProcessor = new WSDLBasedProcessor(model, name, this.wsdlLocation, this.portTypeName, this.operationName);
	model.addProcessor(theProcessor);
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
