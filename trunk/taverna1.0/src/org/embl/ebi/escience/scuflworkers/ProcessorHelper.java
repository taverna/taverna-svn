/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflworkers;

import javax.swing.ImageIcon;
import org.embl.ebi.escience.scufl.DuplicateProcessorNameException;
import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scufl.ProcessorCreationException;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scufl.XScufl;
import org.embl.ebi.escience.scufl.parser.XScuflFormatException;
import org.embl.ebi.escience.scuflui.ScuflIcons;
import org.embl.ebi.escience.scuflworkers.soaplab.SoaplabProcessor;
import org.embl.ebi.escience.scuflworkers.talisman.TalismanProcessor;
import org.embl.ebi.escience.scuflworkers.workflow.WorkflowProcessor;
import org.embl.ebi.escience.scuflworkers.wsdl.WSDLBasedProcessor;

// JDOM Imports
import org.jdom.Element;
import org.jdom.Namespace;

// Network Imports
import java.net.MalformedURLException;
import java.net.URL;




/**
 * Provides rendering and other hints for different processor
 * implementations, including preferred colours and icons.
 * @author Tom Oinn
 */
public class ProcessorHelper {

    public static String getPreferredColour(Processor p) {
	if (p instanceof WSDLBasedProcessor) {
	    return "darkolivegreen3";
	}
	else if (p instanceof TalismanProcessor) {
	    return "plum2";
	}
	else if (p instanceof WorkflowProcessor) {
	    return "orange";
	}
	return "lightgoldenrodyellow";
    }

    public static ImageIcon getPreferredIcon(Processor p) {
	if (p instanceof WorkflowProcessor) {
	    return ScuflIcons.workflowIcon;
	}
	else if (p instanceof WSDLBasedProcessor) {
	    return ScuflIcons.wsdlIcon;
	}
	else if (p instanceof TalismanProcessor) {
	    return ScuflIcons.talismanIcon;
	}
	else if (p instanceof SoaplabProcessor) {
	    return ScuflIcons.soaplabIcon;
	}
	return null;
    }

    public static Element elementForProcessor(Processor p) {
	// Catch Soaplab processors - this should be more
	// extensible! Will do for now however...
	try {
	    SoaplabProcessor slp = (SoaplabProcessor)p;
	    // No exception therefore we have a soaplab processor
	    Element spec = new Element("soaplabwsdl",scuflNS());
	    spec.setText(slp.getEndpoint().toString());
	    return spec;
	}
	catch (ClassCastException cce) {
	    //
	}
	// Catch WorkflowProcessor
	try {
	    WorkflowProcessor wp = (WorkflowProcessor)p;
	    Element spec = new Element("workflow",scuflNS());
	    Element definition = new Element("xscufllocation",scuflNS());
	    spec.addContent(definition);
	    definition.setText(wp.getDefinitionURL());
	    return spec;
	}
	catch (ClassCastException cce) {
	    //
	}
	// Catch WSDLBasedProcessor
	try {
	    WSDLBasedProcessor wsdlp = (WSDLBasedProcessor)p;
	    Element spec = new Element("arbitrarywsdl",scuflNS());
	    Element wsdl = new Element("wsdl",scuflNS());
	    Element port = new Element("porttype",scuflNS());
	    Element operation = new Element("operation",scuflNS());
	    wsdl.setText(wsdlp.getWSDLLocation());
	    port.setText(wsdlp.getPortTypeName());
	    operation.setText(wsdlp.getOperationName());
	    spec.addContent(wsdl);
	    spec.addContent(port);
	    spec.addContent(operation);
	    return spec;
	}
	catch (ClassCastException cce) {
	    //
	}
	// Catch TalismanProcessor
	try {
	    TalismanProcessor tp = (TalismanProcessor)p;
	    Element spec = new Element("talisman",scuflNS());
	    Element tscript = new Element("tscript",scuflNS());
	    tscript.setText(tp.getTScriptURL());
	    spec.addContent(tscript);
	    return spec;
	}
	catch (ClassCastException cce) { 
	    //
	}
	return null;
    }

    /**
     * Spit back a processor given a chunk of xml, the element passed in being the 'processor' tag
     * return null if we can't handle it
     */
    public static Processor loadProcessorFromXML(Element processorNode, ScuflModel model, String name)
	throws ProcessorCreationException,
	       DuplicateProcessorNameException,
	       XScuflFormatException {
	// Handle soaplab
	Element soaplab = processorNode.getChild("soaplabwsdl",scuflNS());
	if (soaplab != null) {
	    // Get the textual endpoint
	    String endpoint = soaplab.getTextTrim();
	    // Check the URL for validity
	    try {
		URL endpointURL = new URL(endpoint);
	    }
	    catch (MalformedURLException mue) {
		throw new XScuflFormatException("The url specified for the soaplab endpoint for '"+name+"' was invalid : "+mue);
	    }
	    return new SoaplabProcessor(model, name, endpoint);
	}
	
	Element wsdlProcessor = processorNode.getChild("arbitrarywsdl",scuflNS());
	if (wsdlProcessor != null) {
	    String wsdlLocation = wsdlProcessor.getChild("wsdl",scuflNS()).getTextTrim();
	    String portTypeName = wsdlProcessor.getChild("porttype",scuflNS()).getTextTrim();
	    String operationName = wsdlProcessor.getChild("operation",scuflNS()).getTextTrim();
	    return new WSDLBasedProcessor(model, name, wsdlLocation, portTypeName, operationName);
	}
	
	Element talismanProcessor = processorNode.getChild("talisman",scuflNS());
	if (talismanProcessor != null) {
	    String tscriptURL = talismanProcessor.getChild("tscript",scuflNS()).getTextTrim();
	    return new TalismanProcessor(model, name, tscriptURL);
	}
	
	Element workflowProcessor = processorNode.getChild("workflow",scuflNS());
	if (workflowProcessor != null) {
	    String definitionURL = workflowProcessor.getChild("xscufllocation",scuflNS()).getTextTrim();
	    return new WorkflowProcessor(model, name, definitionURL);
	}
	

	return null;
    }
    

    /**
     * The namespace for the generated nodes,
     * references the scufl.XScufl class
     */
    private static Namespace scuflNS() {
	return XScufl.XScuflNS;
    }
}
