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
import org.jdom.*;
import org.embl.ebi.escience.scufl.XScufl;



/**
 * Implementation of ProcessorFactory that creates
 * WSDLBasedProcessor nodes
 * @author Tom Oinn
 */
public class WSDLBasedProcessorFactory extends ProcessorFactory {

    String wsdlLocation, operationName;

    /**
     * Create a new factory with the specified wsdl location,
     * port type name and operation name
     */
    public WSDLBasedProcessorFactory(String wsdlLocation, String operationName) {
	this.wsdlLocation = wsdlLocation;
	this.operationName = operationName;
    }

    public String getWSDLLocation() {
	return this.wsdlLocation;
    }

    public String getOperationName() {
	return this.operationName;
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
    /**
       public Processor createProcessor(String name, ScuflModel model) 
       throws ProcessorCreationException,
       DuplicateProcessorNameException {
       Processor theProcessor = new WSDLBasedProcessor(model, name, this.wsdlLocation, this.operationName);
       if (model!=null) {
       model.addProcessor(theProcessor);
       }
       return theProcessor;
       }
    */

    /**
     * Return the XML fragment for processors defined by this factory
     */
    /**
       public Element getXMLFragment() {
       Element spec = new Element("arbitrarywsdl",XScufl.XScuflNS);
       Element wsdl = new Element("wsdl",XScufl.XScuflNS);
       Element operation = new Element("operation",XScufl.XScuflNS);
       wsdl.setText(this.wsdlLocation);
       operation.setText(this.operationName);
       spec.addContent(wsdl);
       spec.addContent(operation);
       return spec;
       }
    */
    
    /**
     * Return a description of the factory
     */
    public String getProcessorDescription() {
	return "A WSDL based processor using the wsdl document at '"+wsdlLocation+"' with operation name '"+operationName+"'";
    }

    /**
     * Return the Class object for processors that would
     * be created by this factory
     */
    public Class getProcessorClass() {
	return org.embl.ebi.escience.scuflworkers.wsdl.WSDLBasedProcessor.class;
    }

}
