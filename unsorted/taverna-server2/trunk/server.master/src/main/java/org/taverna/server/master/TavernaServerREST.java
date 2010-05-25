package org.taverna.server.master;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.taverna.server.master.exceptions.NoUpdateException;
import org.taverna.server.master.exceptions.UnknownRunException;

/**
 * The REST service interface to Taverna Server version 2.3.
 * 
 * @author Donal Fellows
 * @see TavernaServerSOAP
 */
@Produces( { "application/xml", "application/json" })
@Consumes( { "application/xml", "application/json" })
public interface TavernaServerREST {
	// MASTER API

	/**
	 * Produces the description of the service.
	 * 
	 * @param ui
	 *            About the URI being accessed.
	 * @return The description.
	 */
	@GET
	public ServerDescription describeService(@Context UriInfo ui);

	/**
	 * Accepts (or not) a request to create a new run executing the given
	 * workflow.
	 * 
	 * @param workflow
	 *            The workflow document to execute.
	 * @param ui
	 *            About the URI being accessed.
	 * @return A response to the POST describing what was created.
	 * @throws NoUpdateException
	 *             If the POST failed.
	 */
	@POST
	@Path("runs")
	@Consumes("application/xml")
	public Response submitWorkflow(SCUFL workflow, @Context UriInfo ui)
			throws NoUpdateException;

	/**
	 * Gets the maximum number of simultaneous runs that the user may create.
	 * The <i>actual</i> number they can create may be lower than this. If this
	 * number is lower than the number they currently have, they will be unable
	 * to create any runs at all.
	 * 
	 * @return The maximum number of runs.
	 */
	@GET
	@Path("policy/runLimit")
	@Produces("text/plain")
	public int getMaxSimultaneousRuns();

	/**
	 * Gets the list of permitted workflows. Any workflow may be submitted if
	 * the list is empty, otherwise it must be one of the workflows on this
	 * list.
	 * 
	 * @return The list of workflow documents.
	 */
	@GET
	@Path("policy/permittedWorkflows")
	public List<SCUFL> getPermittedWorkflows();

	/**
	 * Gets the list of permitted listeners. All event listeners must be of a
	 * type described on this list.
	 * 
	 * @return The types of event listeners allowed.
	 */
	@GET
	@Path("policy/permittedListenerTypes")
	public List<String> getPermittedListeners();

	/**
	 * Get a particular named run resource.
	 * 
	 * @param runName
	 *            The name of the run.
	 * @return A RESTful delegate for the run.
	 * @throws UnknownRunException
	 *             If the run handle is unknown to the current user.
	 */
	@Path("runs/{runName}")
	public TavernaServerRunREST getRunResource(
			@PathParam("runName") String runName) throws UnknownRunException;
}
