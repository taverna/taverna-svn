/*
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Wei Tan, the University of Chicago
 */
package org.embl.ebi.escience.scuflworkers.gt4;

import org.embl.ebi.escience.scufl.DuplicateProcessorNameException;
import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scufl.ProcessorCreationException;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scufl.XScufl;
import org.embl.ebi.escience.scufl.parser.XScuflFormatException;
import org.embl.ebi.escience.scuflworkers.ProcessorFactory;
import org.embl.ebi.escience.scuflworkers.XMLHandler;
import org.jdom.Element;

/**
 * Handles XML store and load for the GT4 processor
 * 
 * @author Wei Tan
 */
public class GT4XMLHandler implements XMLHandler {

	public Element elementForProcessor(Processor p) {
		GT4Processor wsdlp = (GT4Processor) p;
		return getElement(wsdlp.getWSDLLocation(), wsdlp.getOperationName());
	}

	public Element elementForFactory(ProcessorFactory pf) {
		GT4ProcessorFactory factory = (GT4ProcessorFactory) pf;
		return getElement(factory.getWSDLLocation(), factory.getOperationName());
	}

	public Processor loadProcessorFromXML(Element processorNode,
			ScuflModel model, String name) throws ProcessorCreationException,
			DuplicateProcessorNameException, XScuflFormatException {
		Element gt4Processor = processorNode.getChild("arbitrarygt4",
				XScufl.XScuflNS);
		String wsdlLocation = gt4Processor.getChild("wsdl", XScufl.XScuflNS)
				.getTextTrim();
		String operationName = gt4Processor.getChild("operation",
				XScufl.XScuflNS).getTextTrim();
		return new GT4Processor(model, name, wsdlLocation, operationName);
	}

	public ProcessorFactory getFactory(Element specElement) {
		String wsdlLocation = specElement.getChild("wsdl", XScufl.XScuflNS)
				.getTextTrim();
		String operationName = specElement.getChild("operation",
				XScufl.XScuflNS).getTextTrim();
		return new GT4ProcessorFactory(wsdlLocation, operationName);
	}

	private Element getElement(String wsdlLocation, String operationName) {
		Element spec = new Element("arbitrarygt4", XScufl.XScuflNS);
		Element wsdl = new Element("wsdl", XScufl.XScuflNS);
		Element operation = new Element("operation", XScufl.XScuflNS);
		wsdl.setText(wsdlLocation);
		operation.setText(operationName);
		spec.addContent(wsdl);
		spec.addContent(operation);
		return spec;
	}

}
