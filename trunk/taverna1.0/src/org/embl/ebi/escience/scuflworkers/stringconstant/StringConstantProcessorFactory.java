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
import org.embl.ebi.escience.scuflworkers.ProcessorFactory;




/**
 * Implementation of ProcessorFactory that creates
 * StringConstantProcessor nodes
 * @author Tom Oinn
 */
public class StringConstantProcessorFactory implements ProcessorFactory {

    private String value;

    /**
     * Create a new factory configured with the specified
     * constant value
     */
    public StringConstantProcessorFactory(String value) {
	this.value = value;
    }
    
    /**
     * Return the constant value as the name
     */
    public String toString() {
	return "String constant";
    }
    
    /**
     * Create a new StringConstantProcessor and add it to the model
     */
    public void createProcessor(String name, ScuflModel model)
	throws ProcessorCreationException,
	       DuplicateProcessorNameException {
	Processor theProcessor = new StringConstantProcessor(model, name, this.value);
	model.addProcessor(theProcessor);
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
