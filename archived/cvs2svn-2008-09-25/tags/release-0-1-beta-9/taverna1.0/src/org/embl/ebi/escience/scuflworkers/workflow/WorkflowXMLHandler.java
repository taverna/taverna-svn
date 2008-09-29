package org.embl.ebi.escience.scuflworkers.workflow;

import org.embl.ebi.escience.scufl.DuplicateProcessorNameException;
import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scufl.ProcessorCreationException;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scufl.XScufl;
import org.embl.ebi.escience.scufl.parser.XScuflFormatException;
import org.embl.ebi.escience.scuflworkers.XMLHandler;

// JDOM Imports
import org.jdom.Element;

import org.embl.ebi.escience.scuflworkers.workflow.WorkflowProcessor;
import java.lang.String;



/**
 * Handles XML store and load for the workflow processor
 * @author Tom Oinn
 */
public class WorkflowXMLHandler implements XMLHandler {

    public Element elementForProcessor(Processor p) {
	WorkflowProcessor wp = (WorkflowProcessor)p;
	Element spec = new Element("workflow",XScufl.XScuflNS);
	Element definition = new Element("xscufllocation",XScufl.XScuflNS);
	spec.addContent(definition);
	definition.setText(wp.getDefinitionURL());
	return spec;
    }
    
    public Processor loadProcessorFromXML(Element processorNode, ScuflModel model, String name)
	throws ProcessorCreationException, 
	       DuplicateProcessorNameException, 
	       XScuflFormatException {
	Element workflowProcessor = processorNode.getChild("workflow",XScufl.XScuflNS);
	String definitionURL = workflowProcessor.getChild("xscufllocation",XScufl.XScuflNS).getTextTrim();
	return new WorkflowProcessor(model, name, definitionURL);
    }

}
