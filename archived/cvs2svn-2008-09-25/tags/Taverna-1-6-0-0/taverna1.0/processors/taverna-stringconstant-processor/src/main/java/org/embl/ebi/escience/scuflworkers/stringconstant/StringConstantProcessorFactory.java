/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflworkers.stringconstant;
import org.embl.ebi.escience.scuflworkers.ProcessorFactory;



/**
 * Implementation of ProcessorFactory that creates
 * StringConstantProcessor nodes
 * @author Tom Oinn
 */
public class StringConstantProcessorFactory extends ProcessorFactory {

    private String value = null;

    /**
     * Create a new factory configured with the specified
     * constant value
     */
    public StringConstantProcessorFactory(String value) {
	this.value = value;
	setName("String Constant");
    }
    
    public String getValue() {
	return this.value;
    }    
    
    /**
     * Return a textual description of the factory
     */
    public String getProcessorDescription() {
	return "A processor that supplies a constant string";
    }

    /**
     * Return the Class object for processors that would
     * be created by this factory
     */
    public Class getProcessorClass() {
	return org.embl.ebi.escience.scuflworkers.stringconstant.StringConstantProcessor.class;
    }
    
}
