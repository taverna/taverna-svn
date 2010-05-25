package org.taverna.server.master;

import java.util.Date;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.taverna.server.master.exceptions.NoUpdateException;

/**
 * This represents how a Taverna Server workflow run looks to a RESTful API.
 * 
 * @author Donal Fellows.
 */
@Produces( { "application/xml", "application/json" })
@Consumes( { "application/xml", "application/json" })
public interface TavernaServerRunREST {
	/**
	 * Describes a workflow run.
	 * 
	 * @param ui
	 *            About how this method was called.
	 * @return The description.
	 */
	@GET
	public RunDescription getDescription(@Context UriInfo ui);

	/**
	 * Deletes a workflow run.
	 * 
	 * @param ui
	 *            About how this method was called.
	 * @return An HTTP response to the deletion.
	 * @throws NoUpdateException
	 *             If the user may see the handle but may not delete it.
	 */
	@DELETE
	public Response destroy(@Context UriInfo ui) throws NoUpdateException;

	/**
	 * Returns the workflow document used to create the workflow run.
	 * 
	 * @return The SCUFL document.
	 */
	@GET
	@Path("workflow")
	public SCUFL getWorkflow();

	/**
	 * Returns the time when the workflow run becomes eligible for automatic
	 * deletion.
	 * 
	 * @return When the run expires.
	 */
	@GET
	@Path("expiry")
	public Date getExpiry();

	/**
	 * Sets the time when the workflow run becomes eligible for automatic
	 * deletion.
	 * 
	 * @param expiry
	 *            When the run will expire.
	 * @param ui
	 *            About how this method was called.
	 * @return An HTTP response to the call.
	 * @throws NoUpdateException
	 *             If the current user is not permitted to manage the lifetime
	 *             of the run.
	 */
	@PUT
	@Path("expiry")
	public Response setExpiry(Date expiry, @Context UriInfo ui)
			throws NoUpdateException;

	/**
	 * Gets the current status of the workflow run.
	 * 
	 * @return The status code.
	 */
	@GET
	@Path("status")
	@Produces("text/plain")
	public Status getStatus();

	/**
	 * Sets the status of the workflow run. This does nothing if the status code
	 * is the same as the run's current state.
	 * 
	 * @param status
	 *            The new status code.
	 * @param ui
	 *            About how this method was called.
	 * @return An HTTP response to the setting.
	 * @throws NoUpdateException
	 *             If the current user is not permitted to update the run.
	 */
	@PUT
	@Path("status")
	@Consumes("text/plain")
	public Response setStatus(Status status, @Context UriInfo ui)
			throws NoUpdateException;

	/**
	 * Gets the identity of who owns the workflow run.
	 * 
	 * @return The name of the owner of the run.
	 */
	@GET
	@Path("owner")
	@Produces("text/plain")
	public String getOwner();

	/**
	 * Get the working directory of this workflow run.
	 * 
	 * @return A RESTful delegate for the working directory.
	 */
	@Path("wd")
	public TavernaServerDirectoryREST getWorkingDirectory();

	/**
	 * Get the event listeners attached to this workflow run.
	 * 
	 * @return A RESTful delegate for the list of listeners.
	 */
	@Path("listeners")
	public TavernaServerListenerREST getListeners();
}
