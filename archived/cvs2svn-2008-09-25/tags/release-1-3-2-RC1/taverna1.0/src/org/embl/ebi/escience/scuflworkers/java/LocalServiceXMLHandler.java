/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflworkers.java;

import org.embl.ebi.escience.scufl.DuplicateProcessorNameException;
import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scufl.ProcessorCreationException;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scufl.XScufl;
import org.embl.ebi.escience.scufl.parser.XScuflFormatException;
import org.embl.ebi.escience.scuflworkers.*;

// JDOM Imports
import org.jdom.Element;

import org.embl.ebi.escience.scuflworkers.java.LocalServiceProcessor;
import java.lang.String;

/**
 * Handles XML store and load for the local process processor
 * 
 * @author Tom Oinn
 */
public class LocalServiceXMLHandler implements XMLHandler {

	public Element elementForProcessor(Processor p) {
		LocalServiceProcessor lsp = (LocalServiceProcessor) p;
		Element spec = new Element("local", XScufl.XScuflNS);
		spec.setText(lsp.getWorkerClassName());
		if (lsp.getWorker() instanceof XMLExtensible)
		{
			spec.addContent(((XMLExtensible)lsp.getWorker()).provideXML());
		}
		return spec;
	}

	public Element elementForFactory(ProcessorFactory pf) {
		LocalServiceProcessorFactory lspf = (LocalServiceProcessorFactory) pf;
		Element spec = new Element("local", XScufl.XScuflNS);
		spec.setText(lspf.getWorkerClassName());
		return spec;
	}

	public ProcessorFactory getFactory(Element specElement) {
		String workerClass = specElement.getTextTrim();
		// Use the class leaf name as the descriptive name, as we don't
		// have anything better
		String[] parts = workerClass.split("\\.");
		String descriptiveName = parts[parts.length - 1];
		return new LocalServiceProcessorFactory(workerClass, descriptiveName);
	}

	public Processor loadProcessorFromXML(Element processorNode, ScuflModel model, String name)
			throws ProcessorCreationException, DuplicateProcessorNameException, XScuflFormatException {
		Element local = processorNode.getChild("local", XScufl.XScuflNS);
		Element additionalInfo = local.getChild("extensions", XScufl.XScuflNS);
		String workerClass = local.getTextTrim();
		if (additionalInfo == null) {
			return new LocalServiceProcessor(model, name, workerClass);
		} else {
			return new LocalServiceProcessor(model, name, workerClass, additionalInfo);
		}
	}
}
