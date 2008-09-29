/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflworkers.wsdl;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.wsdl.Operation;
import javax.wsdl.PortType;
import javax.wsdl.WSDLException;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.embl.ebi.escience.scuflui.workbench.Scavenger;
import org.embl.ebi.escience.scuflui.workbench.ScavengerCreationException;
import org.embl.ebi.escience.scuflworkers.wsdl.parser.WSDLParser;
import org.xml.sax.SAXException;

/**
 * A Scavenger that knows how to inspect a given wsdl document for all available
 * port types and operations within them.
 * 
 * @author Tom Oinn
 */
public class WSDLBasedScavenger extends Scavenger {

	private static final long serialVersionUID = 5281708181579790512L;

	private static Logger logger = Logger.getLogger(WSDLBasedScavenger.class);

	/**
	 * Create a new WSDLBased scavenger, the single parameter should be
	 * resolvable to a location from which the wsdl document can be fetched.
	 */

	public WSDLBasedScavenger(String wsdlLocation) throws ScavengerCreationException {
		super("WSDL @ " + wsdlLocation);

		// Load the wsdl document
		try {
			new URL(wsdlLocation);
		} catch (MalformedURLException mue) {
			throw new ScavengerCreationException("Unable to parse the supplied URL '" + wsdlLocation + "', error was "
					+ mue.getMessage());
		}

		try {
			WSDLParser parser = new WSDLParser(wsdlLocation);
			List operations = parser.getOperations();
			String style = parser.getStyle();
			PortType portType = parser.getPortType();
			String portTypeName = portType.getQName().getLocalPart();
			String name = "";

			if (style.equals("document")) {
				name = "porttype: " + portTypeName + " [<font color=\"blue\">DOCUMENT</font>]";
			} else {
				name = "porttype: " + portTypeName + " [<font color=\"green\">" + style.toUpperCase() + "</font>]";
			}
			DefaultMutableTreeNode portTypeNode = new DefaultMutableTreeNode(name);
			add(portTypeNode);
			// Iterate over all the operation names
			for (Iterator i = operations.iterator(); i.hasNext();) {
				Operation op = (Operation) i.next();
				String operationName = op.getName();
				WSDLBasedProcessorFactory wpf = new WSDLBasedProcessorFactory(wsdlLocation, operationName, portType
						.getQName());
				DefaultMutableTreeNode operationNode = new DefaultMutableTreeNode(wpf);
				portTypeNode.add(operationNode);
			}

		} catch (SAXException e) {
			logger.error("SAXException parsing wsdl:" + wsdlLocation);
			throw new ScavengerCreationException("Unable to load the WSDL definition, underlying reason was "
					+ e.getMessage());
		} catch (ParserConfigurationException e) {
			logger.error("ParserConfigurationException parsing wsdl:" + wsdlLocation);
			throw new ScavengerCreationException("Unable to load the WSDL definition, underlying reason was "
					+ e.getMessage());
		} catch (WSDLException e) {
			logger.error("WSDLException parsing wsdl:" + wsdlLocation + "," + e.getMessage());
			throw new ScavengerCreationException("Unable to load the WSDL definition, underlying reason was "
					+ e.getMessage());
		} catch (IOException e) {
			logger.error("IOException parsing wsdl:" + wsdlLocation);
			throw new ScavengerCreationException("Unable to load the WSDL definition, underlying reason was "
					+ e.getMessage());
		}
	}
}
