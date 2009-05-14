/*******************************************************************************
 * Copyright (C) 2007 The University of Manchester   
 * 
 *  Modifications to the initial code base are copyright of their
 *  respective authors, or their employers as appropriate.
 * 
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2.1 of
 *  the License, or (at your option) any later version.
 *    
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *    
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 ******************************************************************************/
package net.sf.taverna.t2.workflowmodel.serialization.xml;

import java.util.ArrayList;
import java.util.List;

import net.sf.taverna.t2.platform.plugin.PluginManager;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.Processor;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;
import net.sf.taverna.t2.workflowmodel.serialization.SerializationException;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.output.DOMOutputter;

/**
 * Implementation of the XML serialisation framework for serialising a dataflow
 * instance into a jdom XML element. <br>
 * 
 * @author Stuart Owen
 * @author Tom Oinn
 * 
 */
public class XMLSerializerImpl implements XMLSerializer,
		XMLSerializationConstants {

	private PluginManager manager;

	public XMLSerializerImpl(PluginManager manager) {
		this.manager = manager;
	}

	public static org.w3c.dom.Element toDom(Element e) {
		DOMOutputter parser = new DOMOutputter();
		Element cloned = (Element) (e.clone());
		try {
			return parser.output(new Document(cloned)).getDocumentElement();
		} catch (JDOMException e1) {
			throw new RuntimeException(
					"JDOM exception when converting to org.w3c.dom.Element", e1);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.taverna.t2.workflowmodel.serialization.xml.XMLSerializer#serializeDataflow(net.sf.taverna.t2.workflowmodel.Dataflow)
	 */
	public org.w3c.dom.Element serializeDataflow(Dataflow dataflow)
			throws SerializationException {
		List<Dataflow> innerDataflows = new ArrayList<Dataflow>();

		gatherDataflows(dataflow, innerDataflows);

		Element result = new Element(WORKFLOW, T2_WORKFLOW_NAMESPACE);
		// For future use
		result.setAttribute(WORKFLOW_VERSION, "1");
		Element dataflowElement = DataflowXMLSerializer.getInstance()
				.serializeDataflow(dataflow, manager);
		dataflowElement.setAttribute(DATAFLOW_ROLE, DATAFLOW_ROLE_TOP);
		result.addContent(dataflowElement);

		for (Dataflow innerDataflow : innerDataflows) {
			Element innerDataflowElement = DataflowXMLSerializer.getInstance()
					.serializeDataflow(innerDataflow, manager);
			innerDataflowElement.setAttribute(DATAFLOW_ROLE,
					DATAFLOW_ROLE_NESTED);
			result.addContent(innerDataflowElement);
		}

		return toDom(result);
	}

	private void gatherDataflows(Dataflow dataflow,
			List<Dataflow> innerDataflows) {
		for (Processor p : dataflow.getProcessors()) {
			for (Activity<?> a : p.getActivityList()) {
				if (a.getConfiguration() instanceof Dataflow) {
					Dataflow df = (Dataflow) a.getConfiguration();
					if (!innerDataflows.contains(df)) {
						innerDataflows.add(df);
						gatherDataflows(df, innerDataflows);
					}
				}
			}
		}

	}

}
