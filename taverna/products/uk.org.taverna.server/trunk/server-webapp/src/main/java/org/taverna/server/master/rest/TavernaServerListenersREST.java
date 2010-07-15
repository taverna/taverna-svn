package org.taverna.server.master.rest;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.cxf.jaxrs.ext.Description;
import org.taverna.server.master.common.Uri;
import org.taverna.server.master.exceptions.NoListenerException;
import org.taverna.server.master.exceptions.NoUpdateException;
import org.taverna.server.master.interfaces.Listener;

/**
 * This represents <i>all</i> the event listeners attached to a workflow run.
 * 
 * @author Donal Fellows
 * @see TavernaServerListenerREST
 */
@Description("This represents all the event listeners attached to a workflow run.")
public interface TavernaServerListenersREST {
	/**
	 * Get the listeners installed in the workflow run.
	 * 
	 * @param ui
	 *            About how this method was called.
	 * @return A list of descriptions of listeners.
	 */
	@GET
	@Path("/")
	@Produces( { "application/xml", "application/json" })
	@Description("Get the listeners installed in the workflow run.")
	public Listeners getDescription(@Context UriInfo ui);

	/**
	 * Add a new event listener to the named workflow run.
	 * 
	 * @param typeAndConfiguration
	 *            What type of run should be created, and how should it be
	 *            configured.
	 * @param ui
	 *            About how this method was called.
	 * @return An HTTP response to the creation request.
	 * @throws NoUpdateException
	 *             If the user is not permitted to update the run.
	 * @throws NoListenerException
	 *             If no listener with the given type exists, or if the
	 *             configuration is unacceptable in some way.
	 */
	@POST
	@Path("/")
	@Consumes( { "application/xml", "application/json" })
	@Description("Add a new event listener to the named workflow run.")
	public Response addListener(ListenerDefinition typeAndConfiguration,
			@Context UriInfo ui) throws NoUpdateException, NoListenerException;

	/**
	 * Resolve a particular listener from its name.
	 * 
	 * @param name
	 *            The name of the listener to look up.
	 * @return The listener's delegate in the REST world.
	 * @throws NoListenerException
	 *             If no listener with the given name exists.
	 */
	@Path("{name}")
	@Description("Resolve a particular listener from its name.")
	public TavernaServerListenerREST getListener(@PathParam("name") String name)
			throws NoListenerException;

	/**
	 * This represents a single event listener attached to a workflow run.
	 * 
	 * @author Donal Fellows
	 * @see TavernaServerListenersREST
	 * @see Property
	 */
	@Description("This represents a single event listener attached to a workflow run.")
	public interface TavernaServerListenerREST {
		/**
		 * Get the description of this listener.
		 * 
		 * @param ui
		 *            Information about this request.
		 * @return A description document.
		 */
		@GET
		@Path("/")
		@Produces( { "application/xml", "application/json" })
		@Description("Get the description of this listener.")
		public ListenerDescription getDescription(@Context UriInfo ui);

		/**
		 * Get the configuration for the given event listener that is attached
		 * to a workflow run.
		 * 
		 * @return The configuration of the listener.
		 */
		@GET
		@Path("configuration")
		@Produces("text/plain")
		@Description("Get the configuration for the given event listener that is attached to a workflow run.")
		public String getConfiguration();

		/**
		 * Get the list of properties supported by a given event listener
		 * attached to a workflow run.
		 * 
		 * @param ui
		 *            Information about this request.
		 * @return The list of property names.
		 */
		@GET
		@Path("properties")
		@Produces( { "application/xml", "application/json" })
		@Description("Get the list of properties supported by a given event listener attached to a workflow run.")
		public Properties getProperties(@Context UriInfo ui);

		/**
		 * Get an object representing a particular property.
		 * 
		 * @param propertyName
		 * @return The property delegate.
		 * @throws NoListenerException
		 *             If there is no such property.
		 */
		@Path("properties/{propertyName}")
		@Description("Get an object representing a particular property.")
		public Property getProperty(
				@PathParam("propertyName") String propertyName)
				throws NoListenerException;
	}

	/**
	 * This represents a single property attached of an event listener.
	 * 
	 * @author Donal Fellows
	 */
	@Description("This represents a single property attached of an event listener.")
	public interface Property {
		/**
		 * Get the value of the particular property of an event listener
		 * attached to a workflow run.
		 * 
		 * @return The value of the property.
		 */
		@GET
		@Path("/")
		@Produces("text/plain")
		@Description("Get the value of the particular property of an event listener attached to a workflow run.")
		public String getValue();

		/**
		 * Set the value of the particular property of an event listener
		 * attached to a workflow run. Changing the value of the property may
		 * cause the listener to alter its behaviour significantly.
		 * 
		 * @param value
		 *            The value to set the property to.
		 * @return The value of the property after being set.
		 * @throws NoUpdateException
		 *             If the user is not permitted to update the run.
		 * @throws NoListenerException
		 *             If the property is in the wrong format.
		 */
		@PUT
		@Path("/")
		@Consumes("text/plain")
		@Produces("text/plain")
		@Description("Set the value of the particular property of an event listener attached to a workflow run.")
		public String setValue(String value) throws NoUpdateException,
				NoListenerException;
	}

	/**
	 * A description of an event listener that is attached to a workflow run.
	 * Done with JAXB.
	 * 
	 * @author Donal Fellows
	 */
	@XmlRootElement
	@XmlType(name = "ListenerDescription")
	public class ListenerDescription {
		/** The (arbitrary) name of the event listener. */
		@XmlAttribute
		public String name;
		/** The type of the event listener. */
		@XmlAttribute
		public String type;
		/**
		 * The location of the configuration document for the event listener.
		 */
		public Uri configuration;
		/**
		 * The name and location of the properties supported by the event
		 * listener.
		 */
		@XmlElementWrapper(name = "properties", nillable = false)
		@XmlElement(name = "property", nillable = false)
		public List<PropertyDescription> properties;

		/**
		 * Make a blank listener description.
		 */
		public ListenerDescription() {
		}

		/**
		 * Make a listener description that characterizes the given listener.
		 * 
		 * @param listener The listener to describe.
		 * @param ub The factor for URIs.
		 */
		public ListenerDescription(Listener listener, UriBuilder ub) {
			name = listener.getName();
			type = listener.getType();
			configuration = new Uri(ub.clone().path("configuration"));
			UriBuilder ub2 = ub.clone().path("properties/{prop}");
			properties = new ArrayList<PropertyDescription>(
					listener.listProperties().length);
			for (String propName : listener.listProperties())
				properties.add(new PropertyDescription(propName, ub2));
		}
	}

	/**
	 * The description of a single property, done with JAXB.
	 * 
	 * @author Donal Fellows
	 */
	@XmlType(name = "PropertyDescription")
	public static class PropertyDescription extends Uri {
		/**
		 * The name of the property.
		 */
		@XmlAttribute
		String name;

		/**
		 * Make an empty description of a property.
		 */
		public PropertyDescription() {
		}

		/**
		 * Make a description of a property.
		 * 
		 * @param listenerName
		 *            The name of the listener whose property this is.
		 * @param propName
		 *            The name of the property.
		 * @param ub
		 *            The factory for URIs.
		 */
		PropertyDescription(String propName, UriBuilder ub) {
			super(ub, propName);
			this.name = propName;
		}
	}

	/**
	 * The list of descriptions of listeners attached to a run. Done with JAXB.
	 * 
	 * @author Donal Fellows
	 */
	@XmlRootElement
	@XmlType(name = "")
	public static class Listeners {
		/**
		 * The listeners for a workflow run.
		 */
		@XmlElement
		public List<ListenerDescription> description;

		/**
		 * Make a blank description of listeners.
		 */
		public Listeners() {
			description = new ArrayList<ListenerDescription>();
		}

		/**
		 * Make a description of the whole group out of the given list of
		 * listener descriptions.
		 * 
		 * @param listeners
		 */
		public Listeners(List<ListenerDescription> listeners) {
			description = listeners;
		}
	}

	/**
	 * The list of properties of a listener. Done with JAXB.
	 * 
	 * @author Donal Fellows
	 */
	@XmlRootElement
	@XmlType(name = "")
	public static class Properties {
		/**
		 * The references to the properties of a listener.
		 */
		@XmlElement
		public List<PropertyDescription> property;

		/**
		 * Make an empty description of the properties of a listener.
		 */
		public Properties() {
		}

		/**
		 * Make the description of the properties of a listener.
		 * 
		 * @param ub
		 *            The factory for URIs, configured.
		 * @param properties
		 *            The names of the properties.
		 */
		public Properties(UriBuilder ub, String[] properties) {
			property = new ArrayList<PropertyDescription>(properties.length);
			for (String propName : properties)
				property.add(new PropertyDescription(propName, ub));
		}
	}
}
