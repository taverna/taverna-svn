package org.taverna.server.master.rest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.cxf.jaxrs.ext.Description;
import org.taverna.server.master.SCUFL;
import org.taverna.server.master.TavernaServerSOAP;
import org.taverna.server.master.Uri;
import org.taverna.server.master.exceptions.NoUpdateException;
import org.taverna.server.master.exceptions.UnknownRunException;
import org.taverna.server.master.interfaces.TavernaRun;

/**
 * The REST service interface to Taverna Server version 2.3.
 * 
 * @author Donal Fellows
 * @see TavernaServerSOAP
 */
@Description("This is REST service interface to Taverna Server version 2.3")
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
	@Produces( { "application/xml", "application/json" })
	@Description("Produces the description of the service.")
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
	@Description("Accepts (or not) a request to create a new run executing the given workflow.")
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
	@Description("Gets the maximum number of simultaneous runs that the user may create.")
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
	@Produces( { "application/xml", "application/json" })
	@Description("Gets the list of permitted workflows.")
	public PermittedWorkflows getPermittedWorkflows();

	/**
	 * Gets the list of permitted event listener types. All event listeners must
	 * be of a type described on this list.
	 * 
	 * @return The types of event listeners allowed.
	 */
	@GET
	@Path("policy/permittedListenerTypes")
	@Produces( { "application/xml", "application/json" })
	@Description("Gets the list of permitted event listener types.")
	public PermittedListeners getPermittedListeners();

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
	@Description("Get a particular named run resource to dispatch to.")
	public TavernaServerRunREST getRunResource(
			@PathParam("runName") String runName) throws UnknownRunException;

	@XmlRootElement
	@XmlType(name = "")
	public static class ServerDescription {
		@XmlElementWrapper(name = "runs", required = true, nillable = false)
		@XmlElement(nillable = false)
		public List<Uri> run;
		public Uri runLimit, permittedWorkflows, permittedListeners;

		// Uri database;
		public ServerDescription() {
		}

		public ServerDescription(Map<String, TavernaRun> ws, UriInfo ui) {
			run = new ArrayList<Uri>(ws.size());
			for (Map.Entry<String, TavernaRun> w : ws.entrySet()) {
				run.add(new Uri(ui, "runs/{uuid}", w.getKey()));
			}
			runLimit = new Uri(ui, "policy/runLimit");
			permittedWorkflows = new Uri(ui, "policy/permittedWorkflows");
			permittedListeners = new Uri(ui, "policy/permittedListenerTypes");
			// database = new Uri(ui, "database");// TODO make this point to something real
		}
	}

	@XmlRootElement
	@XmlType(name="")
	public static class PermittedWorkflows {
		@XmlElement
		public List<SCUFL> workflow;

		public PermittedWorkflows() {
		}

		public PermittedWorkflows(List<SCUFL> permitted) {
			workflow = permitted;
		}
	}

	@XmlRootElement
	@XmlType(name="")
	public static class PermittedListeners {
		@XmlElement
		public List<String> type;

		public PermittedListeners() {
		}

		public PermittedListeners(List<String> listenerTypes) {
			type = listenerTypes;
		}
	}
}
