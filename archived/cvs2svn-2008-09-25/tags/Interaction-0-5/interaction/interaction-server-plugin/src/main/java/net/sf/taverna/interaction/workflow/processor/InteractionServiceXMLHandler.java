/*
 * Copyright 2005 Tom Oinn, EMBL-EBI
 *
 *  This file is part of Taverna.  Further information, and the
 *  latest version, can be found at http://taverna.sf.net
 * 
 *  Taverna is in turn part of the myGrid project, more details
 *  can be found at http://www.mygrid.org.uk
 *
 *  Taverna is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as
 *  published by the Free Software Foundation; either version 2.1
 *  of the License, or (at your option) any later version.
 *
 *  Taverna is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with Taverna; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package net.sf.taverna.interaction.workflow.processor;

import org.embl.ebi.escience.scufl.DuplicateProcessorNameException;
import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scufl.ProcessorCreationException;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scufl.XScufl;
import org.embl.ebi.escience.scufl.parser.XScuflFormatException;
import org.embl.ebi.escience.scuflworkers.ProcessorFactory;
import org.embl.ebi.escience.scuflworkers.XMLHandler;
import org.jdom.Element;

/**
 * Handle XML for the Interaction Service processor
 * 
 * @author Tom Oinn
 */
public class InteractionServiceXMLHandler implements XMLHandler {

	public Element elementForProcessor(Processor p) {
		InteractionServiceProcessor ip = (InteractionServiceProcessor) p;
		return getElement(ip.getBaseURL(), ip.getPatternName());
	}

	public Element elementForFactory(ProcessorFactory pf) {
		InteractionServiceProcessorFactory ipf = (InteractionServiceProcessorFactory) pf;
		return getElement(ipf.getBaseURL(), ipf.getPatternName());
	}

	public Processor loadProcessorFromXML(Element processorNode,
			ScuflModel model, String name) throws ProcessorCreationException,
			DuplicateProcessorNameException, XScuflFormatException {
		Element interactionProcessor = processorNode.getChild("interaction",
				XScufl.XScuflNS);
		String baseURL = interactionProcessor.getChild("baseurl",
				XScufl.XScuflNS).getTextTrim();
		String patternName = interactionProcessor.getChild("patternname",
				XScufl.XScuflNS).getTextTrim();
		return new InteractionServiceProcessor(model, name, baseURL,
				patternName);
	}

	public ProcessorFactory getFactory(Element interactionProcessor) {
		String baseURL = interactionProcessor.getChild("baseurl",
				XScufl.XScuflNS).getTextTrim();
		String patternName = interactionProcessor.getChild("patternname",
				XScufl.XScuflNS).getTextTrim();
		return new InteractionServiceProcessorFactory(baseURL, patternName);
	}

	private Element getElement(String baseURL, String patternName) {
		Element e = new Element("interaction", XScufl.XScuflNS);
		Element b = new Element("baseurl", XScufl.XScuflNS);
		b.setText(baseURL);
		e.addContent(b);
		Element p = new Element("patternname", XScufl.XScuflNS);
		p.setText(patternName);
		e.addContent(p);
		return e;
	}

}
