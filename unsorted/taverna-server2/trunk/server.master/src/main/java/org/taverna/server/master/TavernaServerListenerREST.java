package org.taverna.server.master;

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
import javax.ws.rs.core.UriInfo;

import org.taverna.server.master.exceptions.NoListenerException;
import org.taverna.server.master.exceptions.NoUpdateException;

/**
 * This represents <i>all</i> the event listeners attached to a workflow run.
 * 
 * @author Donal Fellows
 * @see SingleListener
 */
@Produces( { "application/xml", "application/json" })
@Consumes( { "application/xml", "application/json" })
public interface TavernaServerListenerREST {
	/**
	 * Get the listeners installed in the workflow run.
	 * 
	 * @param ui
	 *            About how this method was called.
	 * @return A list of descriptions of listeners.
	 */
	@GET
	public List<ListenerDescription> getDescription(@Context UriInfo ui);

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
	public Response addListener(
			ListenerCreationDescription typeAndConfiguration,
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
	public SingleListener getListener(@PathParam("name") String name)
			throws NoListenerException;

	/**
	 * This represents a single event listener attached to a workflow run.
	 * 
	 * @author Donal Fellows
	 * @see TavernaServerListenerREST
	 * @see Property
	 */
	@Produces( { "application/xml", "application/json" })
	@Consumes( { "application/xml", "application/json" })
	public interface SingleListener {
		/**
		 * Get the description of this listener.
		 * 
		 * @param ui
		 *            Information about this request.
		 * @return A description document.
		 */
		@GET
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
		public String getConfiguration();

		/**
		 * Get the list of properties supported by a given event listener
		 * attached to a workflow run.
		 * 
		 * @return The list of property names.
		 */
		@GET
		@Path("properties")
		public List<String> getProperties();

		/**
		 * Get an object representing a particular property.
		 * 
		 * @param propertyName
		 * @return The property delegate.
		 * @throws NoListenerException
		 *             If there is no such property.
		 */
		@Path("properties/{propertyName}")
		public Property getProperty(
				@PathParam("propertyName") String propertyName)
				throws NoListenerException;
	}

	/**
	 * This represents a single property attached of an event listener.
	 * 
	 * @author Donal Fellows
	 */
	@Produces("text/plain")
	@Consumes("text/plain")
	public interface Property {
		/**
		 * Get the value of the particular property of an event listener
		 * attached to a workflow run.
		 * 
		 * @return The value of the property.
		 */
		@GET
		public String getValue();

		/**
		 * Set the value of the particular property of an event listener
		 * attached to a workflow run. Changing the value of the property may
		 * cause the listener to alter its behaviour significantly.
		 * 
		 * @param value
		 *            The value to set the property to.
		 * @param ui
		 *            About how this method was called.
		 * @return An HTTP response to the method.
		 * @throws NoUpdateException
		 *             If the user is not permitted to update the run.
		 * @throws NoListenerException
		 *             If the property is in the wrong format.
		 */
		@PUT
		public Response setValue(String value, @Context UriInfo ui)
				throws NoUpdateException, NoListenerException;

	}
}
