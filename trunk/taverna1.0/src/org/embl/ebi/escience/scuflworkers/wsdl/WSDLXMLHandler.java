package org.embl.ebi.escience.scuflworkers.wsdl;

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
 * Handles XML store and load for the Soaplab processor
 * @author Tom Oinn
 */
public class WSDLXMLHandler implements XMLHandler {

    public Element elementForProcessor(Processor p) {
	WSDLBasedProcessor wsdlp = (WSDLBasedProcessor)p;
	Element spec = new Element("arbitrarywsdl",XScufl.XScuflNS);
	Element wsdl = new Element("wsdl",XScufl.XScuflNS);
	Element port = new Element("porttype",XScufl.XScuflNS);
	Element operation = new Element("operation",XScufl.XScuflNS);
	Element style = new Element("style",XScufl.XScuflNS);
	wsdl.setText(wsdlp.getWSDLLocation());
	port.setText(wsdlp.getPortTypeName());
	operation.setText(wsdlp.getOperationName());
	style.setText(wsdlp.getOperationStyle());
	spec.addContent(wsdl);
	spec.addContent(port);
	spec.addContent(operation);
	spec.addContent(style);
	return spec;
    }
    
    public Processor loadProcessorFromXML(Element processorNode, ScuflModel model, String name)
	throws ProcessorCreationException, 
	       DuplicateProcessorNameException, 
	       XScuflFormatException {
	Element wsdlProcessor = processorNode.getChild("arbitrarywsdl",XScufl.XScuflNS);
	String wsdlLocation = wsdlProcessor.getChild("wsdl",XScufl.XScuflNS).getTextTrim();
	String portTypeName = wsdlProcessor.getChild("porttype",XScufl.XScuflNS).getTextTrim();
	String operationName = wsdlProcessor.getChild("operation",XScufl.XScuflNS).getTextTrim();
	String operationStyle = "rpc";
	Element styleElement = wsdlProcessor.getChild("style",XScufl.XScuflNS);
	if (styleElement != null) {
	    operationStyle = styleElement.getTextTrim();
	}
	return new WSDLBasedProcessor(model, name, wsdlLocation, portTypeName, operationName, operationStyle);
    }

}
