/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflworkers.java;

import org.embl.ebi.escience.scufl.DuplicateProcessorNameException;
import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scufl.ProcessorCreationException;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scuflworkers.ProcessorFactory;

import org.embl.ebi.escience.scuflworkers.java.LocalServiceProcessor;
import java.lang.Class;
import java.lang.String;



/**
 * Implementation of ProcessorFactory that can create LocalServiceProcessor instances
 * @author Tom Oinn
 */
public class LocalServiceProcessorFactory implements ProcessorFactory {
    
    private String className;
    private String descriptiveName;
    
    /**
     * Create a new factory configured with the specified
     * worker class.
     */
    public LocalServiceProcessorFactory(String workerClassName, String descriptiveName) {
	this.className = workerClassName;
	this.descriptiveName = descriptiveName;
    }
    
    /**
     * Return a name
     */
    public String toString() {
	return descriptiveName;
    }

    /**
     * Create a new processor and add to the model
     */
    public Processor createProcessor(String name, ScuflModel model)
	throws ProcessorCreationException,
	       DuplicateProcessorNameException {
	Processor theProcessor = new LocalServiceProcessor(model, name, this.className);
	if (model!=null) {
	    model.addProcessor(theProcessor);
	}
	return theProcessor;
    }

    /**
     * A description of the factory
     */
    public String getProcessorDescription() {
	return "A processor that uses the worker class "+className+" to run a process locally to the enactor.";
    }

    /**
     * Return the Class object for the processors that this factory creates
     */
    public Class getProcessorClass() {
	return org.embl.ebi.escience.scuflworkers.java.LocalServiceProcessor.class;
    }

}
