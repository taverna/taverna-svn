package org.taverna.server.master.rest;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * A description of an event listener that is attached to a workflow run.
 * @author Donal Fellows
 */
@XmlRootElement
public class ListenerDescription {
	/**
	 * The (arbitrary) name of the event listener.
	 */
	@XmlAttribute
	public String name;
	/**
	 * The type of the event listener.
	 */
	@XmlAttribute
	public String type;
	/**
	 * The location of the configuration document for the event listener.
	 */
	public ConfigurationDescription configuration;
	/**
	 * The name and location of the properties supported by the event listener.
	 */
	public List<PropertyDescription> properties;

	/**
	 * The description of the configuration document.
	 * @author Donal Fellows
	 */
	public static class ConfigurationDescription {
		/**
		 * A reference to the configuration.
		 */
		@XmlAttribute(name="href",namespace="http://www.w3.org/1999/xlink")
		URI ref;
	}
	/**
	 * The description of a single property.
	 * @author Donal Fellows
	 */
	public static class PropertyDescription {
		/**
		 * The name of the property.
		 */
		@XmlAttribute
		String name;
		/**
		 * A reference to the property's value.
		 */
		@XmlAttribute(name = "href", namespace = "http://www.w3.org/1999/xlink")
		URI ref;
		public PropertyDescription(){}
		PropertyDescription(String listenerName, String propName, UriBuilder ub) {
			this.name = propName;
			this.ref = ub.build(listenerName, propName);
		}
	}

	public ListenerDescription() {}
	public ListenerDescription(String name, String type, String[] properties, UriInfo ui) {
		this.name = name;
		this.configuration = new ConfigurationDescription();
		this.configuration.ref = ui.getAbsolutePathBuilder().path("{name}/configuration").build(name);
		UriBuilder ub = ui.getAbsolutePathBuilder().path("{name}/properties/{prop}");
		this.properties = new ArrayList<PropertyDescription>(properties.length);
		for (String propName: properties)
			this.properties.add(new PropertyDescription(name, propName, ub));
	}
}
