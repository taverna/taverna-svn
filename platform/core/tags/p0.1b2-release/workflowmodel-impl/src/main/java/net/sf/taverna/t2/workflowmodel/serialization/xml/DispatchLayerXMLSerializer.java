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

import java.io.IOException;

import net.sf.taverna.t2.platform.plugin.PluginIdentifier;
import net.sf.taverna.t2.platform.plugin.PluginManager;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.DispatchLayer;

import org.jdom.Element;
import org.jdom.JDOMException;

public class DispatchLayerXMLSerializer extends AbstractXMLSerializer {

	private static DispatchLayerXMLSerializer instance = new DispatchLayerXMLSerializer();

	public static DispatchLayerXMLSerializer getInstance() {
		return instance;
	}

	public Element dispatchLayerToXML(DispatchLayer<?,?> layer,
			PluginManager manager) throws IOException, JDOMException {
		Element result = new Element(DISPATCH_LAYER, T2_WORKFLOW_NAMESPACE);

		appendObjectDetails(layer, result, manager);

		// Get element for configuration
		Object o = layer.getConfiguration();
		Element configElement = beanAsElement(o);
		result.addContent(configElement);
		return result;
	}

	private void appendObjectDetails(DispatchLayer<?,?> layer, Element result,
			PluginManager manager) {
		PluginIdentifier pid = manager.definingPlugin(layer);
		if (pid != null) {
			// Loaded by plug-in manager
			result.addContent(pluginElement(pid));
		} else {
			// Loaded from root class loader
		}
		Element classNameElement = new Element(CLASS, T2_WORKFLOW_NAMESPACE);
		classNameElement.setText(layer.getClass().getName());
		result.addContent(classNameElement);
	}
}
