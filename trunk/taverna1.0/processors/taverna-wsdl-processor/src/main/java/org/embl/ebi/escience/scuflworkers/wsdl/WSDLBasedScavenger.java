/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflworkers.wsdl;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;
import javax.wsdl.Operation;
import javax.wsdl.PortType;
import javax.wsdl.WSDLException;
import javax.xml.parsers.ParserConfigurationException;

import net.sf.taverna.wsdl.parser.WSDLParser;

import org.apache.log4j.Logger;
import org.embl.ebi.escience.scuflui.workbench.Scavenger;
import org.embl.ebi.escience.scuflui.workbench.ScavengerCreationException;
import org.embl.ebi.escience.scuflui.workbench.URLBasedScavenger;
import org.xml.sax.SAXException;

/**
 * A Scavenger that knows how to inspect a given wsdl document for all available
 * port types and operations within them.
 * 
 * @author Tom Oinn
 */
public class WSDLBasedScavenger extends URLBasedScavenger {

	private static final long serialVersionUID = 5281708181579790512L;
	private String wsdlLocation;

	private static Logger logger = Logger.getLogger(WSDLBasedScavenger.class);

	public Scavenger fromURL(URL theURL) throws ScavengerCreationException {
		if (theURL.getFile().toLowerCase().endsWith("wsdl")) {
			return new WSDLBasedScavenger(theURL.toExternalForm());
		}
		else {
			throw new ScavengerCreationException("Not a WSDL");
		}
	}
	
	@Override
	/**
	 * Overridden to catch when a scavenger is removed from the tree (newParent = null).
	 * This triggers the flushing of the WSDLParser cache.
	 */
	public void setParent(MutableTreeNode newParent) {		
		super.setParent(newParent);
		if (newParent==null) { 
			WSDLParser.flushCache(wsdlLocation);
		}
	}

	public WSDLBasedScavenger() {
		super("Blank");
	}
	
	/**
	 * Create a new WSDLBased scavenger, the single parameter should be
	 * resolvable to a location from which the wsdl document can be fetched.
	 */

	public WSDLBasedScavenger(String wsdlLocation)
			throws ScavengerCreationException {
		super("WSDL @ " + wsdlLocation);
		this.wsdlLocation=wsdlLocation;
		
		// Load the wsdl document
		try {
			URL url=new URL(wsdlLocation);
			
			//test for connection timeout
			URLConnection con = url.openConnection();
			con.setConnectTimeout(4000);
			con.getInputStream();
			
		} catch (MalformedURLException e) {
			throw new ScavengerCreationException(
					"Unable to parse the supplied URL '" + wsdlLocation
							+ "', error was " + e.getMessage());
		} catch(IOException e) {
			throw new ScavengerCreationException(
					"Unable to connect to the supplied URL '" + wsdlLocation
							+ "', error was " + e.getMessage());
		}
		

		try {			
			WSDLParser parser = new WSDLParser(wsdlLocation);
			List operations = parser.getOperations();

			Map<String, DefaultMutableTreeNode> portTypeNameTreeNodeMap = new HashMap<String, DefaultMutableTreeNode>();
			if (operations.size() > 0) {
				String style = parser.getStyle();

				DefaultMutableTreeNode portTypeNode;

				for (Iterator i = operations.iterator(); i.hasNext();) {
					Operation op = (Operation) i.next();
					PortType portType = parser.getPortType(op.getName());
					String portTypeName = portType.getQName().getLocalPart();

					portTypeNode = portTypeNameTreeNodeMap.get(portTypeName);

					if (portTypeNode == null) {
						String name;
						if (style.equals("document")) {
							name = "porttype: " + portTypeName
									+ " [<font color=\"blue\">DOCUMENT</font>]";
						} else {
							name = "porttype: " + portTypeName
									+ " [<font color=\"green\">"
									+ style.toUpperCase() + "</font>]";
						}
						portTypeNode = new DefaultMutableTreeNode(name);
						add(portTypeNode);
						portTypeNameTreeNodeMap.put(portTypeName, portTypeNode);
					}

					String operationName = op.getName();
					WSDLBasedProcessorFactory wpf = new WSDLBasedProcessorFactory(
							wsdlLocation, operationName, portType.getQName());
					DefaultMutableTreeNode operationNode = new DefaultMutableTreeNode(
							wpf);
					portTypeNode.add(operationNode);
				}
			}

		} catch (SAXException e) {
			logger.error("SAXException parsing wsdl:" + wsdlLocation);
			throw new ScavengerCreationException(
					"Unable to load the WSDL definition, underlying reason was "
							+ e.getMessage());
		} catch (ParserConfigurationException e) {
			logger.error("ParserConfigurationException parsing wsdl:"
					+ wsdlLocation);
			throw new ScavengerCreationException(
					"Unable to load the WSDL definition, underlying reason was "
							+ e.getMessage());
		} catch (WSDLException e) {
			logger.error("WSDLException parsing wsdl:" + wsdlLocation + ","
					+ e.getMessage());
			throw new ScavengerCreationException(
					"Unable to load the WSDL definition, underlying reason was "
							+ e.getMessage());
		} catch (IOException e) {
			logger.error("IOException parsing wsdl:" + wsdlLocation);
			throw new ScavengerCreationException(
					"Unable to load the WSDL definition, underlying reason was "
							+ e.getMessage());
		}
	}
}
