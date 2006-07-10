/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Stian Soiland, myGrid
 */
package org.embl.ebi.escience.scuflworkers.rserv;

import java.util.Iterator;

import org.embl.ebi.escience.scufl.DuplicatePortNameException;
import org.embl.ebi.escience.scufl.DuplicateProcessorNameException;
import org.embl.ebi.escience.scufl.InputPort;
import org.embl.ebi.escience.scufl.PortCreationException;
import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scufl.ProcessorCreationException;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scufl.XScufl;
import org.embl.ebi.escience.scufl.parser.XScuflFormatException;
import org.embl.ebi.escience.scuflworkers.ProcessorFactory;
import org.embl.ebi.escience.scuflworkers.XMLHandler;
import org.jdom.Element;

/**
 * Handle XML store and load for the Rserv processor
 * 
 * @author Stian Soiland
 */
public class RservXMLHandler implements XMLHandler {

	// TODO: Move addElement() and stringFromElement() to some Utils-package
	/*
	 * Create an XML element of the given tag name in XScuflNS, with the given
	 * textual content, and add it to the given parent element.
	 * 
	 * If the textual value given is null, don't create any element, return
	 * null.
	 */
	protected Element addElement(String tag, String value, Element parent) {
		// TODO Auto-generated method stub
		if (value == null) {
			return null;
		}
		Element element = new Element(tag, XScufl.XScuflNS);
		element.setText(value);
		parent.addContent(element);
		return element;
	}

	/*
	 * Retrieve an element from a parent element by tag name, and return the
	 * textual content of that element. If the element does not exist, null is
	 * returned.
	 */
	protected String stringFromElement(String tag, Element parent) {
		Element scriptElement = parent.getChild(tag, XScufl.XScuflNS);
		if (scriptElement != null) {
			return scriptElement.getTextTrim();
		}
		return null;
	}

	public Element elementForProcessor(Processor p) {
		RservProcessor rp = (RservProcessor) p;
		Element spec = new Element("rserv", XScufl.XScuflNS);
		// Connection info
		addElement("scriptvalue", rp.getScript(), spec);
		addElement("hostname", rp.getHostname(), spec);
		addElement("port", Integer.toString(rp.getPort()), spec);
		addElement("username", rp.getUsername(), spec);
		addElement("password", rp.getPassword(), spec);

		// Input list
		Element inputList = new Element("rservinputlist", XScufl.XScuflNS);
		InputPort[] inputs = rp.getInputPorts();
		for (int i = 0; i < inputs.length; i++) {
			RservInputPort inport = (RservInputPort) inputs[i];
			Element inputElement = addElement("rservinput", inport.getName(),
					inputList);
			inputElement.setAttribute("javatype", inport.getJavaType(),
					XScufl.XScuflNS);
		}
		spec.addContent(inputList);
		return spec;
	}

	public Element elementForFactory(ProcessorFactory pf) {
		RservProcessorFactory rpf = (RservProcessorFactory) pf;
		if (rpf.getPrototype() != null) {
			return elementForProcessor(rpf.getPrototype());
		} else {
			Element spec = new Element("rserv", XScufl.XScuflNS);
			return spec;
		}
	}

	public ProcessorFactory getFactory(Element specElement) {
		Element processorNode = new Element("processor");
		Element spec = (Element) specElement.clone();
		spec.detach();
		processorNode.addContent(spec);
		RservProcessor rp = null;
		try {
			rp = (RservProcessor) loadProcessorFromXML(processorNode, null,
					"foo");
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		if (rp != null) {
			return new RservProcessorFactory(rp);
		} else {
			return new RservProcessorFactory();
		}
	}

	public Processor loadProcessorFromXML(Element processorNode,
			ScuflModel model, String name) throws ProcessorCreationException,
			DuplicateProcessorNameException, XScuflFormatException {
		RservProcessor rp = new RservProcessor(model, name, "", new String[0]);
		Element rserv = processorNode.getChild("rserv", XScufl.XScuflNS);

		// All RP's set-methods accept null arguments, so it's OK if the element
		// does not exist.
		rp.setScript(stringFromElement("scriptvalue", rserv));
		rp.setHostname(stringFromElement("hostname", rserv));
		rp.setUsername(stringFromElement("username", rserv));
		rp.setPassword(stringFromElement("password", rserv));
		// And then boring stuff for the port number
		String portString = stringFromElement("port", rserv);
		if (portString != null) {
			int port = 0;
			try {
				port = Integer.parseInt(portString);
				rp.setPort(port);
				// Can be thrown both by parseInt and setPort
			} catch (IllegalArgumentException ex) {
				throw new ProcessorCreationException("Invalid port number! "
						+ ex.getMessage());
			}
		}

		// Handle inputs
		Element inputList = rserv.getChild("rservinputlist", XScufl.XScuflNS);
		if (inputList != null) {
			for (Iterator i = inputList.getChildren().iterator(); i.hasNext();) {
				Element inputElement = (Element) i.next();
				String inputName = inputElement.getTextTrim();
				String javaType = inputElement.getAttributeValue("javatype",
						XScufl.XScuflNS);
				try {
					RservInputPort p = new RservInputPort(rp, inputName);
					if (javaType != null) {
						try {
							p.setJavaType(javaType);
						} catch (IllegalArgumentException ex) {
							throw new ProcessorCreationException(
									"Unable to create port! " + ex.getMessage());
						}
					}
					rp.addPort(p);
				} catch (PortCreationException pce) {
					throw new ProcessorCreationException(
							"Unable to create port! " + pce.getMessage());
				} catch (DuplicatePortNameException dpne) {
					throw new ProcessorCreationException(
							"Unable to create port! " + dpne.getMessage());
				}
			}
		}
		// return new RservProcessor(model, name, script, new String[0]);
		return rp;
	}

}
