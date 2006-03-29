package org.embl.ebi.escience.scuflworkers.rdfgenerator;

import org.embl.ebi.escience.scufl.DuplicateProcessorNameException;
import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scufl.ProcessorCreationException;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scufl.XScufl;
import org.embl.ebi.escience.scufl.parser.XScuflFormatException;
import org.embl.ebi.escience.scuflworkers.*;

// JDOM Imports
import org.jdom.Element;

import org.embl.ebi.escience.scuflworkers.rdfgenerator.RDFGeneratorProcessor;
import java.lang.String;



/**
 * Handles XML store and load for the rdf generating processor
 * @author Tom Oinn
 */
public class RDFGeneratorXMLHandler implements XMLHandler {

    public Element elementForProcessor(Processor p) {
	Element spec = new Element("rdfgenerator",XScufl.XScuflNS);
	return spec;
    }

    public Element elementForFactory(ProcessorFactory pf) {
	Element spec = new Element("rdfgenerator",XScufl.XScuflNS);
	return spec;
    }
	
    public ProcessorFactory getFactory(Element specElement) {
	return new RDFGeneratorProcessorFactory();
    }
    
    public Processor loadProcessorFromXML(Element processorNode, ScuflModel model, String name)
	throws ProcessorCreationException, 
	       DuplicateProcessorNameException, 
	       XScuflFormatException {
	return new RDFGeneratorProcessor(model, name);
    }

}
