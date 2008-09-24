/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflworkers.workflow;
import org.embl.ebi.escience.scufl.ScuflWorkflowProcessorFactory;
import org.embl.ebi.escience.scufl.XScufl;
import org.embl.ebi.escience.scuflworkers.ProcessorFactory;
import org.jdom.Element;



/**
 * Implementation of ProcessorFactory that creates
 * WorkflowProcessor nodes
 * @author Tom Oinn
 */
public class WorkflowProcessorFactory extends ProcessorFactory implements ScuflWorkflowProcessorFactory {
    
    private String definitionURL = null;
    private Element definitionElement = null;

    /**
     * Return the definition URL
     */
    public String getDefinitionURL() {
	return this.definitionURL;
    }

    /**
     * Return the literal workflow definition
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
	String[] parts = definitionURL.split("/");
	setName(parts[parts.length - 1]);
	
    }

    /**
     * Create a new factory configured with the specified
     * literal workflow definition
     */
    public WorkflowProcessorFactory(Element definition) {
	this.definitionElement = definition;
	setName("Inline Workflow");
    }

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
