package org.embl.ebi.escience.scuflworkers.soaplab;

import org.embl.ebi.escience.scufl.DuplicateProcessorNameException;
import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scufl.ProcessorCreationException;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scufl.XScufl;
import org.embl.ebi.escience.scufl.parser.XScuflFormatException;
import org.embl.ebi.escience.scuflworkers.*;

// JDOM Imports
import org.jdom.Element;

// Network Imports
import java.net.MalformedURLException;
import java.net.URL;

import org.embl.ebi.escience.scuflworkers.soaplab.SoaplabProcessor;
import java.lang.String;



/**
 * Handles XML store and load for the soaplab processor
 * @author Tom Oinn
 */
public class SoaplabXMLHandler implements XMLHandler {

    public Element elementForProcessor(Processor p) {
	SoaplabProcessor slp = (SoaplabProcessor)p;
	Element spec = new Element("soaplabwsdl",XScufl.XScuflNS);
	spec.setText(slp.getEndpoint().toString());
	return spec;
    }

    public Element elementForFactory(ProcessorFactory pf){
	SoaplabProcessorFactory slpf = (SoaplabProcessorFactory)pf;
	Element spec = new Element("soaplabwsdl",XScufl.XScuflNS);
	spec.setText(slpf.getEndpoint());
	return spec;
    }

    public ProcessorFactory getFactory(Element specElement) {
	String endpoint = specElement.getTextTrim();
	return new SoaplabProcessorFactory(endpoint);
    }
   
    public Processor loadProcessorFromXML(Element processorNode, ScuflModel model, String name)
	throws ProcessorCreationException, 
	       DuplicateProcessorNameException, 
	       XScuflFormatException {
	Element soaplab = processorNode.getChild("soaplabwsdl",XScufl.XScuflNS);
	String endpoint = soaplab.getTextTrim();
	try {
	    URL endpointURL = new URL(endpoint);
	}
	catch (MalformedURLException mue) {
	    throw new XScuflFormatException("The url specified for the soaplab endpoint for '"+name+"' was invalid : "+mue);
	}
	return new SoaplabProcessor(model, name, endpoint);
    }

}
