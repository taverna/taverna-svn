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

import org.embl.ebi.escience.scuflworkers.stringconstant.StringConstantProcessor;
import java.lang.Class;
import java.lang.String;



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
    }
    
    private String name = "String Constant";

    public void setName(String newName) {
	this.name = newName;
    }

    public String getValue() {
	return this.value;
    }    

    /**
     * Return the constant value as the name
     */
    public String toString() {
	return this.name;
    }
    
    /**
     * Create a new StringConstantProcessor and add it to the model
     */
    /**
       public Processor createProcessor(String name, ScuflModel model)
       throws ProcessorCreationException,
       DuplicateProcessorNameException {
       Processor theProcessor = new StringConstantProcessor(model, name, this.value);
       if (model!=null) {
       model.addProcessor(theProcessor);
       }
       return theProcessor;
       }
    */
    
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
