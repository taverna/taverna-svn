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
