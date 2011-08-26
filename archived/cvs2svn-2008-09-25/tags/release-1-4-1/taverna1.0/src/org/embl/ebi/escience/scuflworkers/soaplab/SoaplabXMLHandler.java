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
 * 
 * @author Tom Oinn
 */
public class SoaplabXMLHandler implements XMLHandler {

	public Element elementForProcessor(Processor p) {
		SoaplabProcessor slp = (SoaplabProcessor) p;
		Element spec = new Element("soaplabwsdl", XScufl.XScuflNS);
		String endpointString = slp.getEndpoint().toString();
		String[] split = endpointString.split("::");
		if (split.length == 2) {
			endpointString = split[0] + "." + split[1];
		}
		spec.setText(endpointString);
		if (slp.isPollingDefined()) {
			// Add attributes for polling...
			spec.setAttribute("interval", slp.getPollingInterval() + "");
			spec.setAttribute("backoff", slp.getPollingBackoff() + "");
			spec.setAttribute("maxinterval", slp.getPollingIntervalMax() + "");
		}
		return spec;
	}

	public Element elementForFactory(ProcessorFactory pf) {
		SoaplabProcessorFactory slpf = (SoaplabProcessorFactory) pf;
		Element spec = new Element("soaplabwsdl", XScufl.XScuflNS);
		spec.setText(slpf.getEndpoint());
		return spec;
	}

	public ProcessorFactory getFactory(Element specElement) {
		String endpoint = specElement.getTextTrim();
		return new SoaplabProcessorFactory(endpoint);
	}

	public Processor loadProcessorFromXML(Element processorNode,
			ScuflModel model, String name) throws ProcessorCreationException,
			DuplicateProcessorNameException, XScuflFormatException {
		Element soaplab = processorNode
				.getChild("soaplabwsdl", XScufl.XScuflNS);
		String endpoint = soaplab.getTextTrim();
		try {
			new URL(endpoint);
		} catch (MalformedURLException mue) {
			throw new XScuflFormatException(
					"The url specified for the soaplab endpoint for '" + name
							+ "' was invalid : " + mue);
		}
		SoaplabProcessor theProcessor = new SoaplabProcessor(model, name,
				endpoint);
		// Set the polling properties if they're defined, or just use the
		// defaults
		// if not.
		theProcessor.setPolling(Integer.parseInt(soaplab.getAttributeValue(
				"interval", "0")), Double.parseDouble(soaplab
				.getAttributeValue("backoff", "1.0")), Integer.parseInt(soaplab
				.getAttributeValue("intervalmax", "0")));

		return theProcessor;
	}

}
