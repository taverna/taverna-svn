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
import org.embl.ebi.escience.scufl.XScufl;
import org.embl.ebi.escience.scufl.parser.XScuflFormatException;
import org.embl.ebi.escience.scuflworkers.XMLHandler;

// JDOM Imports
import org.jdom.Element;

import org.embl.ebi.escience.scuflworkers.java.LocalServiceProcessor;
import java.lang.String;



/**
 * Handles XML store and load for the local process processor
 * @author Tom Oinn
 */
public class LocalServiceXMLHandler implements XMLHandler {

    public Element elementForProcessor(Processor p) {
	LocalServiceProcessor lsp = (LocalServiceProcessor)p;
	Element spec = new Element("local",XScufl.XScuflNS);
	spec.setText(lsp.getWorkerClassName());
	return spec;
    }
    
    public Processor loadProcessorFromXML(Element processorNode, ScuflModel model, String name)
	throws ProcessorCreationException, 
	       DuplicateProcessorNameException, 
	       XScuflFormatException {
	Element local = processorNode.getChild("local",XScufl.XScuflNS);
	String workerClass = local.getTextTrim();
	return new LocalServiceProcessor(model, name, workerClass);
    }

}
