/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflworkers.workflow;
import org.embl.ebi.escience.scufl.DuplicateProcessorNameException;
import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scufl.ProcessorCreationException;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scuflworkers.ProcessorFactory;

import org.embl.ebi.escience.scuflworkers.workflow.WorkflowProcessor;
import java.lang.Class;
import java.lang.String;



/**
 * Implementation of ProcessorFactory that creates
 * WorkflowProcessor nodes
 * @author Tom Oinn
 */
public class WorkflowProcessorFactory implements ProcessorFactory {
    
    private String definitionURL;

    /**
     * Return the definition URL
     */
    public String getDefinitionURL() {
	return this.definitionURL;
    }
    
    /**
     * Create a new factory configured with the specified
     * definition URL
     */
    public WorkflowProcessorFactory(String definitionURL) {
	this.definitionURL = definitionURL;
    }

    /**
     * Return the leaf of the path as the factory name
     */
    public String toString() {
	String[] parts = definitionURL.split("/");
	return parts[parts.length - 1];
    }

    /**
     * Build a new WorkflowProcessor and add it to the model
     */
    public void createProcessor(String name, ScuflModel model) 
	throws ProcessorCreationException,
	       DuplicateProcessorNameException {
	Processor theProcessor = new WorkflowProcessor(model, name, this.definitionURL);
	model.addProcessor(theProcessor);
    }

    /**
     * Return a textual description of the factory
     */
    public String getProcessorDescription() {
	return "A processor encapsulating the xscufl workflow at "+this.definitionURL;
    }

    /**
     * Return the Class object for processors that would
     * be created by this factory
     */
    public Class getProcessorClass() {
	return org.embl.ebi.escience.scuflworkers.workflow.WorkflowProcessor.class;
    }

}
