/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflworkers.stringconstant;

import java.util.Properties;

import org.embl.ebi.escience.scufl.DuplicateProcessorNameException;
import org.embl.ebi.escience.scufl.OutputPort;
import org.embl.ebi.escience.scufl.Port;
import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scufl.ProcessorCreationException;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scufl.ScuflModelEvent;



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
	setBoring(true);
	theStringValue = value;
	// Create a single output port
	try {
	    Port newPort = new OutputPort(this, "value");
	    newPort.setSyntacticType("'text/plain'");
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
	return getName() + " : "+theStringValue;
    }

    /**
     * Get the value for the string constant
     */
    public String getStringValue() {
	return theStringValue;
    }

    /**
     * Set the string value for the string constant
     */
    public void setStringValue(String theStringValue) {
	this.theStringValue = theStringValue;
	// Fire a model event
	fireModelEvent(new ScuflModelEvent(this, "String value changed to '"+theStringValue+"'"));
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
