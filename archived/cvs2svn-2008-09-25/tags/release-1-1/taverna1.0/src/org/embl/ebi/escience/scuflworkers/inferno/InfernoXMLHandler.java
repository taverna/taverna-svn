/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflworkers.inferno;

import org.embl.ebi.escience.scufl.DuplicateProcessorNameException;
import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scuflworkers.ProcessorFactory;
import org.embl.ebi.escience.scufl.ProcessorCreationException;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scufl.XScufl;
import org.embl.ebi.escience.scufl.parser.XScuflFormatException;
import org.embl.ebi.escience.scuflworkers.XMLHandler;

import org.jdom.Element;

/**
 * Handle XML for the Inferno processor
 * @author Tom Oinn
 */
public class InfernoXMLHandler implements XMLHandler {

    public Element elementForProcessor(Processor p) {
	InfernoProcessor ip = (InfernoProcessor)p;
	return getElement(ip.getHost(), ip.getPort(), ip.getService());
    }
    
    public Element elementForFactory(ProcessorFactory pf) {
	InfernoProcessorFactory ipf = (InfernoProcessorFactory)pf;
	return getElement(ipf.getHost(), ipf.getPort(), ipf.getService());
    }
    
    public Processor loadProcessorFromXML(Element processorNode, ScuflModel model, String name)
	throws ProcessorCreationException, 
	       DuplicateProcessorNameException, 
	       XScuflFormatException {
	Element infernoProcessor = processorNode.getChild("inferno", XScufl.XScuflNS);
	String host = infernoProcessor.getChild("host", XScufl.XScuflNS).getTextTrim();
	String service = infernoProcessor.getChild("service", XScufl.XScuflNS).getTextTrim();
	int port = Integer.parseInt(infernoProcessor.getChild("port", XScufl.XScuflNS).getTextTrim());
	return new InfernoProcessor(model, name, host, port, service);
    }
	
    public ProcessorFactory getFactory(Element infernoProcessor) {
	String host = infernoProcessor.getChild("host", XScufl.XScuflNS).getTextTrim();
	String service = infernoProcessor.getChild("service", XScufl.XScuflNS).getTextTrim();
	int port = Integer.parseInt(infernoProcessor.getChild("port", XScufl.XScuflNS).getTextTrim());
	return new InfernoProcessorFactory(host, port, service);
    }
    
    private Element getElement(String host, int port, String service) {
	Element spec = new Element("inferno", XScufl.XScuflNS);
	
	Element hostElement = new Element("host", XScufl.XScuflNS);
	hostElement.setText(host);
	spec.addContent(hostElement);
	
	Element portElement = new Element("port", XScufl.XScuflNS);
	portElement.setText(port+"");
	spec.addContent(portElement);
	
	Element serviceElement = new Element("service", XScufl.XScuflNS);
	serviceElement.setText(service);
	spec.addContent(serviceElement);
	
	return spec;
    }

}
