package org.embl.ebi.escience.scuflworkers.wsdl;

import org.embl.ebi.escience.scufl.DuplicateProcessorNameException;
import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scuflworkers.ProcessorFactory;
import org.embl.ebi.escience.scufl.ProcessorCreationException;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scufl.XScufl;
import org.embl.ebi.escience.scufl.parser.XScuflFormatException;
import org.embl.ebi.escience.scuflworkers.XMLHandler;

// JDOM Imports
import org.jdom.Element;

/**
 * Handles XML store and load for the Soaplab processor
 * 
 * @author Tom Oinn
 */
public class WSDLXMLHandler implements XMLHandler {

	public Element elementForProcessor(Processor p) {
		WSDLBasedProcessor wsdlp = (WSDLBasedProcessor) p;
		return getElement(wsdlp.getWSDLLocation(), wsdlp.getOperationName());
	}

	public Element elementForFactory(ProcessorFactory pf) {
		WSDLBasedProcessorFactory factory = (WSDLBasedProcessorFactory) pf;
		return getElement(factory.getWSDLLocation(), factory.getOperationName());
	}

	public Processor loadProcessorFromXML(Element processorNode, ScuflModel model, String name)
			throws ProcessorCreationException, DuplicateProcessorNameException, XScuflFormatException {
		Element wsdlProcessor = processorNode.getChild("arbitrarywsdl", XScufl.XScuflNS);
		String wsdlLocation = wsdlProcessor.getChild("wsdl", XScufl.XScuflNS).getTextTrim();
		String operationName = wsdlProcessor.getChild("operation", XScufl.XScuflNS).getTextTrim();
		return new WSDLBasedProcessor(model, name, wsdlLocation, operationName);
	}

	public ProcessorFactory getFactory(Element specElement) {
		String wsdlLocation = specElement.getChild("wsdl", XScufl.XScuflNS).getTextTrim();
		String operationName = specElement.getChild("operation", XScufl.XScuflNS).getTextTrim();
		return new WSDLBasedProcessorFactory(wsdlLocation, operationName, null);
	}

	private Element getElement(String wsdlLocation, String operationName) {
		Element spec = new Element("arbitrarywsdl", XScufl.XScuflNS);
		Element wsdl = new Element("wsdl", XScufl.XScuflNS);
		Element operation = new Element("operation", XScufl.XScuflNS);
		wsdl.setText(wsdlLocation);
		operation.setText(operationName);
		spec.addContent(wsdl);
		spec.addContent(operation);
		return spec;
	}

}
