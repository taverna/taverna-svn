package org.embl.ebi.escience.scuflworkers.talisman;

import org.embl.ebi.escience.scufl.DuplicateProcessorNameException;
import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scufl.ProcessorCreationException;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scufl.XScufl;
import org.embl.ebi.escience.scufl.parser.XScuflFormatException;
import org.embl.ebi.escience.scuflworkers.XMLHandler;

// JDOM Imports
import org.jdom.Element;




/**
 * Handles XML store and load for the talisman processor
 * @author Tom Oinn
 */
public class TalismanXMLHandler implements XMLHandler {

    public Element elementForProcessor(Processor p) {
	TalismanProcessor tp = (TalismanProcessor)p;
	Element spec = new Element("talisman",XScufl.XScuflNS);
	Element tscript = new Element("tscript",XScufl.XScuflNS);
	tscript.setText(tp.getTScriptURL());
	spec.addContent(tscript);
	return spec;
    }
    
    public Processor loadProcessorFromXML(Element processorNode, ScuflModel model, String name)
	throws ProcessorCreationException, 
	       DuplicateProcessorNameException, 
	       XScuflFormatException {
	Element talismanProcessor = processorNode.getChild("talisman",XScufl.XScuflNS);
	String tscriptURL = talismanProcessor.getChild("tscript",XScufl.XScuflNS).getTextTrim();
	return new TalismanProcessor(model, name, tscriptURL);
    }

}
