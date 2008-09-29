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
import org.embl.ebi.escience.scufl.OutputPort;
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

	public Element elementForProcessor(Processor p) {
		RservProcessor rp = (RservProcessor) p;
		Element spec = new Element("rserv", XScufl.XScuflNS);
		// Script element
		Element script = new Element("scriptvalue", XScufl.XScuflNS);
		script.setText(rp.getScript());
		spec.addContent(script);
		// Input list
		Element inputList = new Element("rservinputlist", XScufl.XScuflNS);
		InputPort[] inputs = rp.getInputPorts();
		for (int i = 0; i < inputs.length; i++) {
			RservInputPort inport = (RservInputPort) inputs[i];
			Element inputElement = new Element("rservinput", XScufl.XScuflNS);
			inputElement.setText(inport.getName());
			inputElement.setAttribute("javatype", inport.getJavaType(), XScufl.XScuflNS);
			inputList.addContent(inputElement);
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
		Element scriptElement = rserv.getChild("scriptvalue", XScufl.XScuflNS);
		if (scriptElement != null) {
			String script = scriptElement.getTextTrim();
			rp.setScript(script);
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
						} catch (IllegalArgumentException pce) {
							throw new ProcessorCreationException(
									"Unable to create port! "
											+ pce.getMessage());
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
