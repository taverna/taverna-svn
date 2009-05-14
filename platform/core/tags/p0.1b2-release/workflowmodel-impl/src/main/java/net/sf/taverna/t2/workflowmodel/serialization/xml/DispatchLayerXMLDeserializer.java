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

import net.sf.taverna.t2.platform.plugin.PluginManager;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.DispatchLayer;
import net.sf.taverna.t2.workflowmodel.serialization.DeserializationException;

import org.jdom.Element;

public class DispatchLayerXMLDeserializer extends AbstractXMLDeserializer {
	private static DispatchLayerXMLDeserializer instance = new DispatchLayerXMLDeserializer();

	private DispatchLayerXMLDeserializer() {

	}

	public static DispatchLayerXMLDeserializer getInstance() {
		return instance;
	}

	@SuppressWarnings("unchecked")
	public DispatchLayer<?,?> deserializeDispatchLayer(Element element,
			PluginManager manager) throws ClassNotFoundException,
			InstantiationException, IllegalAccessException,
			DeserializationException {

		String className = element.getChild(CLASS, T2_WORKFLOW_NAMESPACE)
				.getTextTrim();
		ClassLoader cl = findClassLoader(null, element, manager);

		Class<? extends DispatchLayer> c = (Class<? extends DispatchLayer>) cl
				.loadClass(className);
		DispatchLayer<Object,Object> layer = c.newInstance();

		// Handle the configuration of the dispatch layer
		Element configElement = element.getChild(CONFIG_BEAN,
				T2_WORKFLOW_NAMESPACE);
		Object configObject = createBean(configElement, cl);
		layer.configure(configObject);

		return layer;
	}
}
