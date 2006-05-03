/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.biomoby.client.taverna.plugin;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.embl.ebi.escience.baclava.DataThing;
import org.embl.ebi.escience.scuflworkers.java.LocalWorker;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;

import uk.ac.soton.itinnovation.taverna.enactor.entities.ProcessorTask;
import uk.ac.soton.itinnovation.taverna.enactor.entities.TaskExecutionException;

/**
 * A local processor to extract simple data types from biomoby data packets.
 * 
 * @author Tom Oinn
 */
public class ExtractMobyData implements LocalWorker {

	private Logger logger = Logger.getLogger(ProcessorTask.class);
	
	Namespace mobyNS = CreateMobyData.mobyNS;

	public String[] inputNames() {
		return new String[] { "mobydata" };
	}

	public String[] inputTypes() {
		return new String[] { "'text/xml'" };
	}

	public String[] outputNames() {
		return new String[] { "namespace", "id", "value", "type" };
	}

	public String[] outputTypes() {
		return new String[] { LocalWorker.STRING, LocalWorker.STRING,
				LocalWorker.STRING, LocalWorker.STRING };
	}

	public Map execute(Map inputs) throws TaskExecutionException {
		logger.debug("Entering moby parse stage");
		// Must contain a single input called mobydata, if not
		// then complain
		if (inputs.containsKey("mobydata") == false) {
			throw new TaskExecutionException("Moby input data is mandatory");
		}
		String mobyDataString = (String) ((DataThing) inputs.get("mobydata"))
				.getDataObject();
		// Parse the XML
		Element mobyElement;
		try {
			SAXBuilder builder = new SAXBuilder(false);
			Document document = builder.build(new StringReader(mobyDataString));
			mobyElement = document.getRootElement();
		} catch (JDOMException jde) {
			throw new TaskExecutionException(
					"Unable to parse biomoby data, check the XML! "
							+ jde.getMessage());
		} catch (IOException ioe) {
			throw new TaskExecutionException(
					"Unable to parse biomoby data, check the XML! "
							+ ioe.getMessage());
		}
		// Now find the simple moby data
		Element mobyDataElement = null;
		try {
			Element mobyContent = mobyElement.getChild("mobyContent", mobyNS);
			if (mobyContent == null)
				mobyContent = mobyElement.getChild("mobyContent");
			if (mobyContent == null)
				throw new TaskExecutionException(
						"Unexpected structure within moby data: mobyContent tag is missing/misplaced.");

			Element mobyData = mobyContent.getChild("mobyData", mobyNS);
			if (mobyData == null)
				mobyData = mobyContent.getChild("mobyData");
			if (mobyData == null)
				throw new TaskExecutionException(
						"Unexpected structure within moby data: mobyData tag is missing /misplaced.");

			Element mobySimple = mobyData.getChild("Simple", mobyNS);
			if (mobySimple == null)
				mobySimple = mobyData.getChild("Simple");

			if (mobySimple != null)
				// Get the first immediate child of the <moby:Simple>
				mobyDataElement = (Element) mobySimple.getChildren().get(0);
		} catch (NullPointerException npe) {			
			throw new TaskExecutionException(
					"Unexpected structure within moby data, check the input XML.");
		}
		Map results = new HashMap();
		// Get the ID and namespace values, defaulting to the empty string if
		// not present
		if (mobyDataElement == null) {
			results.put("id", new DataThing(""));
			results.put("namespace", new DataThing(""));
			results.put("value", new DataThing(""));
			results.put("type", new DataThing(""));
			logger.debug("Returning an empty result");
		} else {
			String idValue, namespaceValue;
			idValue = mobyDataElement.getAttributeValue("id");
			if (idValue == null) {
				idValue = mobyDataElement.getAttributeValue("id", mobyNS, "");
			}
			namespaceValue = mobyDataElement.getAttributeValue("namespace");
			if (namespaceValue == null) {
				namespaceValue = mobyDataElement.getAttributeValue("namespace",
						mobyNS, "");
			}

			StringBuffer sbuffer = new StringBuffer();
			List childList = mobyDataElement.getChildren();
			for (Iterator it = childList.iterator(); it.hasNext();) {
				Object o = it.next();
				if (o instanceof Element) {
					Element oe = (Element) o;
					sbuffer.append(oe.getText()+System.getProperty("line.separator"));
				}
			}
			results.put("id", new DataThing(idValue));
			results.put("namespace", new DataThing(namespaceValue));
			if (sbuffer.toString().equals(""))
				results.put("value", new DataThing(mobyDataElement
						.getTextTrim()));
			else
				results.put("value", new DataThing(sbuffer.toString()));
			results.put("type", new DataThing(mobyDataElement.getName()));			
			logger.debug("Returning parsed results");
		}
		return results;
	}
}
