package org.embl.ebi.escience.scuflworkers.workflow;

import org.embl.ebi.escience.scufl.DuplicateProcessorNameException;
import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scufl.ProcessorCreationException;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scufl.XScufl;
import org.embl.ebi.escience.scufl.parser.XScuflFormatException;
import org.embl.ebi.escience.scufl.view.XScuflView;
import org.embl.ebi.escience.scuflworkers.ProcessorFactory;
import org.embl.ebi.escience.scuflworkers.XMLHandler;
import org.jdom.Document;
import org.jdom.Element;



/**
 * Handles XML store and load for the workflow processor
 * @author Tom Oinn
 */
public class WorkflowXMLHandler implements XMLHandler {

    public Element elementForProcessor(Processor p) {
	WorkflowProcessor wp = (WorkflowProcessor)p;
	Element spec = new Element("workflow",XScufl.XScuflNS);
	// Does this processor have a reference to an external file?
	if (wp.getDefinitionURL() != null) {
	    Element definition = new Element("xscufllocation",XScufl.XScuflNS);
	    spec.addContent(definition);
	    definition.setText(wp.getDefinitionURL());
	}
	else {
	    // No definition URL so inline the workflow	    
	    Document doc = XScuflView.getDocument(wp.getInternalModel());
	    spec.addContent(doc.detachRootElement());	    
	}
	return spec;
    }
    
    public Element elementForFactory(ProcessorFactory pf) {
	WorkflowProcessorFactory wpf = (WorkflowProcessorFactory)pf;
	Element spec = new Element("workflow",XScufl.XScuflNS);
	if (wpf.getDefinitionURL() != null) {
	    Element definition = new Element("xscufllocation",XScufl.XScuflNS);
	    spec.addContent(definition);
	    definition.setText(wpf.getDefinitionURL());
	    return spec;
	}
	else {
	    return wpf.getDefinitionSpec();
	}
    }

    public ProcessorFactory getFactory(Element specElement) {
	if (specElement.getChild("xscufllocation",XScufl.XScuflNS) != null) {
	    String definitionURL = specElement.getChild("xscufllocation",XScufl.XScuflNS).getTextTrim();
	    return new WorkflowProcessorFactory(definitionURL);
	}
	else {
	    Element scuflElement = specElement.getChild("scufl",XScufl.XScuflNS);
	    return new WorkflowProcessorFactory(scuflElement);
	}
    }

    public Processor loadProcessorFromXML(Element processorNode, ScuflModel model, String name)
	throws ProcessorCreationException, 
	       DuplicateProcessorNameException, 
	       XScuflFormatException {
	Element workflowProcessor = processorNode.getChild("workflow",XScufl.XScuflNS);
	// See if we're being loaded from a URL, if so use the old mechanism to load.
	Element locationElement = workflowProcessor.getChild("xscufllocation",XScufl.XScuflNS);
	if (locationElement != null) {
	    String definitionURL = workflowProcessor.getChild("xscufllocation",XScufl.XScuflNS).getTextTrim();
	    return new WorkflowProcessor(model, name, definitionURL);
	}
	else {
	    // Loading from inlined workflow
	    Element scuflElement = workflowProcessor.getChild("scufl",XScufl.XScuflNS);
	    if (scuflElement == null) {
		// Neither location nor literal workflow, this is a fault
		throw new ProcessorCreationException("Neither XScufl location nor literal inlined workflow defined for nested workflow processor, failing!");
	    }
	    // Otherwise have a full XScufl definition to use to populate the model with
	    return new WorkflowProcessor(model, name, scuflElement);
	}
	
    }

}
