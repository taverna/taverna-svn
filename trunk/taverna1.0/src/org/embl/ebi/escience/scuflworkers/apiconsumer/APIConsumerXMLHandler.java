/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflworkers.apiconsumer;

import org.embl.ebi.escience.scufl.*;
import org.embl.ebi.escience.scufl.parser.XScuflFormatException;
import org.embl.ebi.escience.scuflworkers.*;

// Utility Imports
import java.util.Iterator;

// JDOM Imports
import org.jdom.Element;

/**
 * Handles XML store and load for the APIConsumer processor
 * @author Tom Oinn
 */
public class APIConsumerXMLHandler implements XMLHandler {

    public Element elementForProcessor(Processor p) {
	APIConsumerProcessor ap = (APIConsumerProcessor)p;
	return ap.definition.asXML();
    }

    public Element elementForFactory(ProcessorFactory pf) {
	APIConsumerProcessorFactory apf = (APIConsumerProcessorFactory)pf;
	return apf.definition.asXML();
    }

    public ProcessorFactory getFactory(Element specElement) {
	return new APIConsumerProcessorFactory(new APIConsumerDefinition(specElement));
    }

    public Processor loadProcessorFromXML(Element processorNode, ScuflModel model, String name) 
	throws ProcessorCreationException,
	       DuplicateProcessorNameException,
	       XScuflFormatException {
	Element spec = processorNode.getChild("apiconsumer");
	return new APIConsumerProcessor(model, name, new APIConsumerDefinition(spec));
    }

}
