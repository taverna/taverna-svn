package org.embl.ebi.escience.scuflworkers.beanshell;

import org.embl.ebi.escience.scufl.*;
import org.embl.ebi.escience.scufl.parser.XScuflFormatException;
import org.embl.ebi.escience.scuflworkers.*;

// Utility Imports
import java.util.Iterator;

// JDOM Imports
import org.jdom.Element;

import org.embl.ebi.escience.scuflworkers.beanshell.BeanshellProcessor;
import java.lang.String;



/**
 * Handles XML store and load for the beanshell processor
 * @author Tom Oinn
 */
public class BeanshellXMLHandler implements XMLHandler {

    public Element elementForProcessor(Processor p) {
	BeanshellProcessor bp = (BeanshellProcessor)p;
	Element spec = new Element("beanshell",XScufl.XScuflNS);
	// Script element
	Element script = new Element("scriptvalue",XScufl.XScuflNS);
	script.setText(bp.getScript());
	spec.addContent(script);
	// Input list
	Element inputList = new Element("beanshellinputlist",XScufl.XScuflNS);
	InputPort[] inputs = bp.getInputPorts();
	for (int i = 0; i < inputs.length; i++) {
	    Element inputElement = new Element("beanshellinput",XScufl.XScuflNS);
	    inputElement.setText(inputs[i].getName());
	    inputList.addContent(inputElement);
	}
	spec.addContent(inputList);
	// Output list
	Element outputList = new Element("beanshelloutputlist",XScufl.XScuflNS);
	OutputPort[] outputs = bp.getOutputPorts();
	for (int i = 0; i < outputs.length; i++) {
	    Element outputElement = new Element("beanshelloutput",XScufl.XScuflNS);
	    outputElement.setText(outputs[i].getName());
	    outputList.addContent(outputElement);
	}
	spec.addContent(outputList);
	return spec;
    }
    
    public Element elementForFactory(ProcessorFactory pf) {
	return new Element("beanshell",XScufl.XScuflNS);
    }

    public ProcessorFactory getFactory(Element specElement) {
	return new BeanshellProcessorFactory();
    }

    public Processor loadProcessorFromXML(Element processorNode, ScuflModel model, String name)
	throws ProcessorCreationException, 
	       DuplicateProcessorNameException, 
	       XScuflFormatException {
	BeanshellProcessor bp = new BeanshellProcessor(model, name, "", new String[0], new String[0]);
	Element beanshell = processorNode.getChild("beanshell",XScufl.XScuflNS);
	Element scriptElement = beanshell.getChild("script",XScufl.XScuflNS);
	if (scriptElement != null) {
	    String script = scriptElement.getTextTrim();
	    bp.setScript(script);
	}
	// Handle inputs
	Element inputList = beanshell.getChild("beanshellinputlist",XScufl.XScuflNS);
	for (Iterator i = inputList.getChildren().iterator(); i.hasNext(); ) {
	    Element inputElement = (Element)i.next();
	    String inputName = inputElement.getTextTrim();
	    try {
		InputPort p = new InputPort(bp, inputName);
		bp.addPort(p);
	    }
	    catch (PortCreationException pce) {
		throw new ProcessorCreationException("Unable to create port! "+pce.getMessage());
	    }
	    catch (DuplicatePortNameException dpne) {
		throw new ProcessorCreationException("Unable to create port! "+dpne.getMessage());
	    }
	}
	// Handle outputs
	Element outputList = beanshell.getChild("beanshelloutputlist",XScufl.XScuflNS);
	for (Iterator i = outputList.getChildren().iterator(); i.hasNext(); ) {
	    Element outputElement = (Element)i.next();
	    String outputName = outputElement.getTextTrim();
	    try {
		OutputPort p = new OutputPort(bp, outputName);
		bp.addPort(p);
	    }
	    catch (PortCreationException pce) {
		throw new ProcessorCreationException("Unable to create port! "+pce.getMessage());
	    }
	    catch (DuplicatePortNameException dpne) {
		throw new ProcessorCreationException("Unable to create port! "+dpne.getMessage());
	    }
	}
	//return new BeanshellProcessor(model, name, script, new String[0], new String[0]);
	return bp;
    }

}
