/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflworkers.beanshell;
import org.embl.ebi.escience.scufl.DuplicateProcessorNameException;
import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scufl.ProcessorCreationException;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scuflworkers.ProcessorFactory;

import org.embl.ebi.escience.scuflworkers.beanshell.BeanshellProcessor;
import java.lang.Class;
import java.lang.String;



/**
 * Implementation of ProcessorFactory that creates
 * Beanshell nodes
 * @author Tom Oinn
 */
public class BeanshellProcessorFactory implements ProcessorFactory {

    /**
     * Create a new factory
     */
    public BeanshellProcessorFactory() {
	//
    }
    
    /**
     * Return the constant value as the name
     */
    public String toString() {
	return "Beanshell scripting host";
    }
    
    /**
     * Create a new BeanshellProcessor and add it to the model
     */
    public void createProcessor(String name, ScuflModel model)
	throws ProcessorCreationException,
	       DuplicateProcessorNameException {
	Processor theProcessor = new BeanshellProcessor(model, name, "", new String[0], new String[0]);
	model.addProcessor(theProcessor);
    }
    
    /**
     * Return a textual description of the factory
     */
    public String getProcessorDescription() {
	return "A processor that allows arbitrary Java scripts";
    }

    /**
     * Return the Class object for processors that would
     * be created by this factory
     */
    public Class getProcessorClass() {
	return org.embl.ebi.escience.scuflworkers.beanshell.BeanshellProcessor.class;
    }
    
}
