/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflworkers.stringconstant;

import org.embl.ebi.escience.scufl.DuplicateProcessorNameException;
import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scufl.ProcessorCreationException;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scufl.*;

// Utility Imports
import java.util.Properties;




/**
 * A processor to contain a single string constant
 * @author Tom Oinn
 */
public class StringConstantProcessor extends Processor implements java.io.Serializable {

    private String theStringValue = "";
    
    /**
     * Construct a new processor with the given model and
     * name, delegates to the superclass.
     */
    public StringConstantProcessor(ScuflModel model, String name, String value)
	throws ProcessorCreationException,
	       DuplicateProcessorNameException {
	super(model, name);
	// Set the endpoint, this then populates the ports appropriately
	// from the returned parameters of the soap call.
	theStringValue = value;
	// Create a single output port
	try {
	    Port newPort = new OutputPort(this, "value");
	    newPort.setSyntacticType("string");
	    this.addPort(newPort);
	}
	catch (Exception ex) {
	    // should never happen
	}
    }

    /**
     * Override the toString method
     */
    public String toString() {
	return "Constant : "+theStringValue;
    }

    /**
     * Get the value for the string constant
     */
    public String getStringValue() {
	return theStringValue;
    }

    /**
     * Get the properties for this processor for display purposes
     */
    public Properties getProperties() {
	Properties props = new Properties();
	props.put("String Value",theStringValue);
	return props;
    }

}
