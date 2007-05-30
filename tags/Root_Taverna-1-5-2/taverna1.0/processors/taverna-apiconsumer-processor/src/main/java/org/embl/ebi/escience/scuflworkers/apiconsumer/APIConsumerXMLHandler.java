/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflworkers.apiconsumer;

import org.embl.ebi.escience.scufl.DuplicateProcessorNameException;
import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scufl.ProcessorCreationException;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scufl.parser.XScuflFormatException;
import org.embl.ebi.escience.scuflworkers.ProcessorFactory;
import org.embl.ebi.escience.scuflworkers.XMLHandler;
import org.embl.ebi.escience.scuflworkers.dependency.DependencyXMLHandler;
import org.jdom.Element;

/**
 * Handles XML store and load for the APIConsumer processor
 * 
 * @author Tom Oinn
 * @author Stian Soiland
 */
public class APIConsumerXMLHandler implements XMLHandler {

	public Element elementForProcessor(Processor p) {
		APIConsumerProcessor ap = (APIConsumerProcessor) p;
		Element apiconsumer = ap.definition.asXML();
		apiconsumer.addContent(DependencyXMLHandler.saveDependencies(ap));
		return apiconsumer;
	}

	public Element elementForFactory(ProcessorFactory pf) {
		APIConsumerProcessorFactory apf = (APIConsumerProcessorFactory) pf;
		// FIXME: Include dependencies
		return apf.definition.asXML();
	}

	public ProcessorFactory getFactory(Element specElement) {
		return new APIConsumerProcessorFactory(new APIConsumerDefinition(
			specElement));
	}

	public Processor loadProcessorFromXML(Element processorNode,
		ScuflModel model, String name) throws ProcessorCreationException,
		DuplicateProcessorNameException, XScuflFormatException {
		Element spec = processorNode.getChild("apiconsumer");
		APIConsumerProcessor proc =
			new APIConsumerProcessor(model, name, new APIConsumerDefinition(
				spec));
		DependencyXMLHandler.loadDependencies(proc, spec);
		return proc;
	}

}
