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
import org.embl.ebi.escience.scufl.XScufl;

import org.embl.ebi.escience.scuflworkers.workflow.WorkflowProcessor;
import java.lang.Class;
import java.lang.String;
import org.jdom.Element;



/**
 * Implementation of ProcessorFactory that creates
 * WorkflowProcessor nodes
 * @author Tom Oinn
 */
public class WorkflowProcessorFactory extends ProcessorFactory {
    
    private String definitionURL = null;
    private String name = "Inline Workflow";
    private Element definitionElement = null;

    /**
     * Set the name
     */
    public void setName(String newName) {
	this.name = newName;
    }

    /**
     * Return the definition URL
     */
    public String getDefinitionURL() {
	return this.definitionURL;
    }

    /**
     * Return the literak workflow definition
     */
    public Element getDefinition() {
	return this.definitionElement;
    }
    
    /**
     * Return the full spec element
     */
    public Element getDefinitionSpec() {
	Element def = (Element)this.definitionElement.clone();
	def.detach();
	Element spec = new Element("workflow",XScufl.XScuflNS);
	spec.addContent(def);
	return spec;
    }

    /**
     * Create a new factory configured with the specified
     * definition URL
     */
    public WorkflowProcessorFactory(String definitionURL) {
	this.definitionURL = definitionURL;
    }

    /**
     * Create a new factory configured with the specified
     * literal workflow definition
     */
    public WorkflowProcessorFactory(Element definition) {
	this.definitionElement = definition;
    }

    /**
     * Return the leaf of the path as the factory name
     */
    public String toString() {
	if (definitionURL != null) {
	    String[] parts = definitionURL.split("/");
	    return parts[parts.length - 1];
	}
	else {
	    return this.name;
	}
    }


    /**
     * Build a new WorkflowProcessor and add it to the model
     */
    /**
       public Processor createProcessor(String name, ScuflModel model) 
       throws ProcessorCreationException,
       DuplicateProcessorNameException {
       Processor theProcessor = new WorkflowProcessor(model, name, this.definitionURL);
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
	if (definitionURL != null) {
	    return "A processor encapsulating the xscufl workflow at "+this.definitionURL;
	}
	else {
	    return ("Anonymous workflow processor");
	}
    }

    /**
     * Return the Class object for processors that would
     * be created by this factory
     */
    public Class getProcessorClass() {
	return org.embl.ebi.escience.scuflworkers.workflow.WorkflowProcessor.class;
    }

}
