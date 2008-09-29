/*
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 */
package net.sourceforge.taverna.scuflworkers.xml;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.sourceforge.taverna.baclava.DataThingAdapter;

import org.dom4j.Document;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.embl.ebi.escience.baclava.factory.DataThingFactory;
import org.embl.ebi.escience.scuflworkers.java.LocalWorker;

import uk.ac.soton.itinnovation.taverna.enactor.entities.TaskExecutionException;

/**
 * This worker applies an arbitrary XPath expression to an XML document, and
 * returns a nodelist containing the nodes that match the XPath expression.
 * 
 * @author mfortner
 * @version $Revision: 1.1.2.2 $
 * 
 * @tavinput "xpath", "xml-text"
 * 
 * @tavoutput "nodelist", "nodelistAsXML"
 */
public class XPathTextWorker implements LocalWorker {
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.embl.ebi.escience.scuflworkers.java.LocalWorker#execute(java.util.Map)
	 */
	public Map execute(Map inputMap) throws TaskExecutionException {
		Map outputMap = new HashMap();
		DataThingAdapter inAdapter = new DataThingAdapter(inputMap);
		String xpathStr = inAdapter.getString("xpath");
		String xmlText = inAdapter.getString("xml-text");

		Document doc;
		// Get the matching elements
		try {
			// doc = parseXml(xmlText, false);
			// XPath xpathSelector = DocumentHelper.createXPath(xpathStr);

			// List nodelist = xpathSelector.selectNodes(doc);

			SAXReader reader = new SAXReader(false);
			reader.setIncludeInternalDTDDeclarations(false);
			reader.setIncludeExternalDTDDeclarations(false);

			Document document = reader.read(new StringReader(xmlText));
			List nodelist = document.selectNodes(xpathStr);

			// Process the elements in the nodelist
			ArrayList outputList = new ArrayList();
			ArrayList outputXmlList = new ArrayList();

			String val = null;
			String xmlVal = null;
			for (Iterator iter = nodelist.iterator(); iter.hasNext();) {
				Node element = (Node) iter.next();
				xmlVal = element.asXML();
				val = element.getStringValue();
				if (val != null && !val.equals("")) {
					outputList.add(val);
					outputXmlList.add(xmlVal);
				}

			}
			String[] nodeArray = new String[outputList.size()];
			outputList.toArray(nodeArray);

			String[] valArray = new String[outputXmlList.size()];
			outputXmlList.toArray(valArray);

			outputMap.put("nodelist", DataThingFactory.bake(nodeArray));
			outputMap.put("nodelistAsXML", DataThingFactory.bake(valArray));

		} catch (Throwable th) {
			throw new TaskExecutionException(th);
		}

		return outputMap;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.embl.ebi.escience.scuflworkers.java.LocalWorker#inputNames()
	 */
	public String[] inputNames() {
		return new String[] { "xpath", "xml-text" };
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.embl.ebi.escience.scuflworkers.java.LocalWorker#inputTypes()
	 */
	public String[] inputTypes() {
		return new String[] { "'text/plain'", "'text/xml'" };
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.embl.ebi.escience.scuflworkers.java.LocalWorker#outputNames()
	 */
	public String[] outputNames() {
		return new String[] { "nodelist", "nodelistAsXML" };
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.embl.ebi.escience.scuflworkers.java.LocalWorker#outputTypes()
	 */
	public String[] outputTypes() {
		return new String[] { "l('text/plain')", "l('text/plain')" };
	}
}
