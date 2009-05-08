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

import java.net.URL;
import java.util.ArrayList;

import net.sf.taverna.t2.platform.plugin.PluginException;
import net.sf.taverna.t2.platform.plugin.PluginIdentifier;
import net.sf.taverna.t2.platform.plugin.PluginManager;
import net.sf.taverna.t2.platform.plugin.generated.PluginDescription;
import net.sf.taverna.t2.workflowmodel.Edits;
import net.sf.taverna.t2.workflowmodel.impl.EditsImpl;
import net.sf.taverna.t2.workflowmodel.serialization.DeserializationException;

import org.jdom.Element;
import org.jdom.output.XMLOutputter;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import com.thoughtworks.xstream.mapper.CannotResolveClassException;

/**
 * An abstract base class that contains deserialisation methods common across
 * all dataflow elements.
 * 
 * @author Stuart Owen
 * 
 */
public abstract class AbstractXMLDeserializer implements
		XMLSerializationConstants {

	protected Edits edits = new EditsImpl(null);

	protected Object createBean(Element configElement, ClassLoader cl) {
		String encoding = configElement.getAttributeValue(BEAN_ENCODING);
		Object result = null;
		if (encoding.equals(XSTREAM_ENCODING)) {
			if (configElement.getChildren().isEmpty()) {
				throw new IllegalArgumentException(
						"XStream encoding expected in element");
			}
			Element beanElement = (Element) configElement.getChildren().get(0);
			XStream xstream = new XStream(new DomDriver());
			xstream.setClassLoader(cl);
			try {
				result = xstream.fromXML(new XMLOutputter()
						.outputString(beanElement));
			} catch (CannotResolveClassException crce) {
				// This happens because the classloader we gave it doesn't have
				// access to the xstream classes, and the element serializes to
				// null. In xstream this is returned as an internal static
				// member of one of the xstream classes which this classloader
				// can't see so it fails. We can work around this by simply
				// trapping this case and returning null.
				result = null;
			}
		} else if (encoding.equals(JDOMXML_ENCODING)) {
			if (configElement.getChildren().isEmpty()) {
				throw new IllegalArgumentException(
						"XML encoding expected in element");
			}
			result = (Element) configElement.getChildren().get(0);
			// } else if (encoding.equals(DATAFLOW_ENCODING)) {
			// // Oh noe
			// System.out.println(configElement);
		} else {
			throw new IllegalArgumentException("Unknown encoding " + encoding);
		}
		return result;
	}

	protected ClassLoader getPluginLoader(Element pluginElement,
			PluginManager manager) {
		String groupId = pluginElement.getChildTextTrim(PLUGIN_GROUP,
				T2_WORKFLOW_NAMESPACE);
		String pluginId = pluginElement.getChildTextTrim(PLUGIN_ID,
				T2_WORKFLOW_NAMESPACE);
		String version = pluginElement.getChildTextTrim(PLUGIN_VERSION,
				T2_WORKFLOW_NAMESPACE);
		return manager.getPluginClassLoader(new PluginIdentifier(groupId,
				pluginId, version), new ArrayList<URL>());
	}

	protected String elementToString(Element element) {
		return new XMLOutputter().outputString(element);
	}

	protected ClassLoader findClassLoader(ClassLoader classLoader,
			Element element, PluginManager manager)
			throws DeserializationException {
		ClassLoader cl = null;
		if (classLoader == null) {
			cl = Thread.currentThread().getContextClassLoader();
		}
		String className = element.getChild(CLASS, T2_WORKFLOW_NAMESPACE)
				.getTextTrim();

		Element pluginElement = element.getChild(PLUGIN, T2_WORKFLOW_NAMESPACE);
		if (pluginElement != null) {
			try {
				cl = getPluginLoader(pluginElement, manager);
			} catch (PluginException pe) {
				throw new DeserializationException("Plugin exception", pe);
			}
		} else {
			// Backwards compatability -check for old raven element
			// Element ravenElement = element.getChild(RAVEN,
			// T2_WORKFLOW_NAMESPACE);

			// (we actually don't need the content of this element)
			
			// Raven element specified, this means we have to enumerate over all
			// plug-ins currently installed and pick the first one that can find
			// this type
			boolean foundEquivalentPlugin = false;
			for (PluginDescription description : manager.getActivePluginList()) {
				// System.out.println("Checking " + description.getId() +
				// " for "
				// + className);
				ClassLoader pluginLoader = manager.getPluginClassLoader(
						description.getId(), new ArrayList<URL>());
				try {
					pluginLoader.loadClass(className);
					cl = pluginLoader;
					foundEquivalentPlugin = true;
					break;
				} catch (ClassNotFoundException cnfe) {
					// System.out.println(cnfe.getMessage());
				}
			}
			if (!foundEquivalentPlugin) {
				throw new DeserializationException(
						"Unable to find an equivalent plugin for class "
								+ className);
			}
		}
		return cl;
	}

}
