/*
 * Copyright (C) 2010-2011 The University of Manchester
 * 
 * See the file "LICENSE.txt" for license terms.
 */
package org.taverna.server.master.rest;

import java.net.URI;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;

import org.apache.cxf.jaxrs.ext.Description;
import org.taverna.server.master.common.Namespaces;
import org.taverna.server.master.common.Status;
import org.taverna.server.master.common.Uri;
import org.taverna.server.master.common.VersionedElement;
import org.taverna.server.master.common.Workflow;
import org.taverna.server.master.exceptions.BadStateChangeException;
import org.taverna.server.master.exceptions.FilesystemAccessException;
import org.taverna.server.master.exceptions.NoDirectoryEntryException;
import org.taverna.server.master.exceptions.NoUpdateException;
import org.taverna.server.master.exceptions.NotOwnerException;
import org.taverna.server.master.interfaces.Listener;
import org.taverna.server.master.interfaces.TavernaRun;
import org.taverna.server.output_description.RdfWrapper;

/**
 * This represents how a Taverna Server workflow run looks to a RESTful API.
 * 
 * @author Donal Fellows.
 */
@Description("This represents how a Taverna Server workflow run looks to a RESTful API.")
public interface TavernaServerRunREST {
	/**
	 * Describes a workflow run.
	 * 
	 * @param ui
	 *            About the URI used to access this resource.
	 * @return The description.
	 */
	@GET
	@Path("/")
	@Description("Describes a workflow run.")
	@Produces({ "application/xml", "application/json" })
	public RunDescription getDescription(@Context UriInfo ui);

	/**
	 * Deletes a workflow run.
	 * 
	 * @return An HTTP response to the deletion.
	 * @throws NoUpdateException
	 *             If the user may see the handle but may not delete it.
	 */
	@DELETE
	@Path("/")
	@Description("Deletes a workflow run.")
	public Response destroy() throws NoUpdateException;

	/**
	 * Returns the workflow document used to create the workflow run.
	 * 
	 * @return The workflow document.
	 */
	@GET
	@Path("workflow")
	@Produces({ "application/xml", "application/json" })
	@Description("Gives the workflow document used to create the workflow run.")
	public Workflow getWorkflow();

	/**
	 * Returns a resource that represents the workflow run's security
	 * properties. These may only be accessed by the owner.
	 * 
	 * @return The security resource.
	 * @throws NotOwnerException
	 *             If the accessing principal isn't the owning principal.
	 */
	@Path("security")
	@Description("Access the workflow run's security.")
	public TavernaServerSecurityREST getSecurity() throws NotOwnerException;

	/**
	 * Returns the time when the workflow run becomes eligible for automatic
	 * deletion.
	 * 
	 * @return When the run expires.
	 */
	@GET
	@Path("expiry")
	@Produces("text/plain")
	@Description("Gives the time when the workflow run becomes eligible for automatic deletion.")
	public String getExpiryTime();

	/**
	 * Sets the time when the workflow run becomes eligible for automatic
	 * deletion.
	 * 
	 * @param expiry
	 *            When the run will expire.
	 * @return When the run will actually expire.
	 * @throws NoUpdateException
	 *             If the current user is not permitted to manage the lifetime
	 *             of the run.
	 */
	@PUT
	@Path("expiry")
	@Consumes("text/plain")
	@Produces("text/plain")
	@Description("Sets the time when the workflow run becomes eligible for automatic deletion.")
	public String setExpiryTime(String expiry) throws NoUpdateException;

	/**
	 * Returns the time when the workflow run was created.
	 * 
	 * @return When the run was first submitted to the server.
	 */
	@GET
	@Path("createTime")
	@Produces("text/plain")
	@Description("Gives the time when the workflow run was first submitted to the server.")
	public String getCreateTime();

	/**
	 * Returns the time when the workflow run was started (through a user-driven
	 * state change).
	 * 
	 * @return When the run was started, or <tt>null</tt>.
	 */
	@GET
	@Path("startTime")
	@Produces("text/plain")
	@Description("Gives the time when the workflow run was started, or an empty string if the run has not yet started.")
	public String getStartTime();

	/**
	 * Returns the time when the workflow run was detected to have finished.
	 * 
	 * @return When the run finished, or <tt>null</tt>.
	 */
	@GET
	@Path("finishTime")
	@Produces("text/plain")
	@Description("Gives the time when the workflow run was first detected as finished, or an empty string if it has not yet finished (including if it has never started).")
	public String getFinishTime();

	/**
	 * Gets the current status of the workflow run.
	 * 
	 * @return The status code.
	 */
	@GET
	@Path("status")
	@Produces("text/plain")
	@Description("Gives the current status of the workflow run.")
	public String getStatus();

	/**
	 * Sets the status of the workflow run. This does nothing if the status code
	 * is the same as the run's current state.
	 * 
	 * @param status
	 *            The new status code.
	 * @return What status the run is actually in.
	 * @throws NoUpdateException
	 *             If the current user is not permitted to update the run.
	 * @throws BadStateChangeException
	 *             If the state cannot be modified in the manner requested.
	 */
	@PUT
	@Path("status")
	@Consumes("text/plain")
	@Produces("text/plain")
	@Description("Attempts to update the status of the workflow run.")
	public String setStatus(String status) throws NoUpdateException,
			BadStateChangeException;

	/**
	 * Get the working directory of this workflow run.
	 * 
	 * @return A RESTful delegate for the working directory.
	 */
	@Path("wd")
	@Description("Get the working directory of this workflow run.")
	public TavernaServerDirectoryREST getWorkingDirectory();

	/**
	 * Get the event listeners attached to this workflow run.
	 * 
	 * @return A RESTful delegate for the list of listeners.
	 */
	@Path("listeners")
	@Description("Get the event listeners attached to this workflow run.")
	public TavernaServerListenersREST getListeners();

	/**
	 * Get a delegate for working with the inputs to this workflow run.
	 * 
	 * @return A RESTful delegate for the inputs.
	 */
	@Path("input")
	@Description("Get the inputs to this workflow run.")
	public TavernaServerInputREST getInputs();

	/**
	 * Get the output Baclava file for this workflow run.
	 * 
	 * @return The filename, or empty string to indicate that the outputs will
	 *         be written to the <tt>out</tt> directory.
	 */
	@GET
	@Path("output")
	@Produces("text/plain")
	@Description("Gives the Baclava file where output will be written; empty means use multiple simple files in the out directory.")
	public String getOutputFile();

	/**
	 * Get a description of the outputs as RDF.
	 * 
	 * @param ui
	 *            About the URI used to access this operation.
	 * @return RDF as XML
	 * @throws BadStateChangeException
	 *             If the run is in the {@link Status#Initialized Initialized}
	 *             state.
	 * @throws FilesystemAccessException
	 *             If problems occur when accessing the filesystem.
	 * @throws NoDirectoryEntryException
	 *             If things are odd in the filesystem.
	 */
	@GET
	@Path("output")
	@Produces("application/xml")
	@Description("Gives an RDF description of the outputs, as currently understood")
	public RdfWrapper getOutputDescription(@Context UriInfo ui)
			throws BadStateChangeException, FilesystemAccessException,
			NoDirectoryEntryException;

	/**
	 * Set the output Baclava file for this workflow run.
	 * 
	 * @param filename
	 *            The Baclava file to use, or empty to make the outputs be
	 *            written to individual files in the <tt>out</tt> subdirectory
	 *            of the working directory.
	 * @return The Baclava file as actually set.
	 * @throws NoUpdateException
	 *             If the current user is not permitted to update the run.
	 * @throws FilesystemAccessException
	 *             If the filename is invalid (starts with <tt>/</tt> or
	 *             contains a <tt>..</tt> segment).
	 * @throws BadStateChangeException
	 *             If the workflow is not in the Initialized state.
	 */
	@PUT
	@Path("output")
	@Consumes("text/plain")
	@Produces("text/plain")
	@Description("Sets the Baclava file where output will be written; empty means use multiple simple files in the out directory.")
	public String setOutputFile(String filename) throws NoUpdateException,
			FilesystemAccessException, BadStateChangeException;

	/**
	 * The description of where everything is in a RESTful view of a workflow
	 * run. Done with JAXB.
	 * 
	 * @author Donal Fellows
	 */
	@XmlRootElement
	@XmlType(name = "")
	public static class RunDescription extends VersionedElement {
		@XmlAttribute(namespace = Namespaces.SERVER_REST)
		public String owner;
		/** The description of the expiry. */
		public Expiry expiry;
		/** The location of the creation workflow description. */
		public Uri creationWorkflow;
		/** The location of the creation time property. */
		public Uri createTime;
		/** The location of the start time property. */
		public Uri startTime;
		/** The location of the finish time property. */
		public Uri finishTime;
		/** The location of the status description. */
		public Uri status;
		/** The location of the working directory. */
		public Uri workingDirectory;
		/** The location of the inputs. */
		public Uri inputs;
		/** The location of the Baclava output. */
		public Uri output;
		/** The location of the security context. */
		public Uri securityContext;
		/** The list of listeners. */
		public ListenerList listeners;

		/**
		 * How to describe a run's expiry.
		 * 
		 * @author Donal Fellows
		 */
		@XmlType(name = "")
		public static class Expiry {
			/**
			 * Where to go to read the exiry
			 */
			@XmlAttribute(name = "href", namespace = Namespaces.XLINK)
			public URI ref;
			/**
			 * What the expiry currently is.
			 */
			@XmlValue
			public String timeOfDeath;

			/**
			 * Make a blank expiry description.
			 */
			public Expiry() {
			}

			private static DateFormat df;

			Expiry(TavernaRun r, UriBuilder ub) {
				ref = ub.build();
				if (df == null)
					df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
				this.timeOfDeath = df.format(r.getExpiry());
			}
		}

		/**
		 * The description of a list of listeners attached to a run.
		 * 
		 * @author Donal Fellows
		 */
		@XmlType(name = "")
		public static class ListenerList extends Uri {
			/**
			 * The references to the individual listeners.
			 */
			public List<Uri> listener;

			/**
			 * An empty description of listeners.
			 */
			public ListenerList() {
				listener = new ArrayList<Uri>();
			}

			ListenerList(TavernaRun r, UriBuilder ub) {
				super(ub);
				listener = new ArrayList<Uri>(r.getListeners().size());
				for (Listener l : r.getListeners()) {
					listener.add(new Uri(ub.path("{name}"), l.getName()));
				}
			}
		}

		/**
		 * An empty description of a run.
		 */
		public RunDescription() {
		}

		/**
		 * A description of a particular run.
		 * 
		 * @param run
		 *            The run to describe.
		 * @param ui
		 *            The factory for URIs.
		 */
		public RunDescription(TavernaRun run, UriInfo ui) {
			super(true);
			creationWorkflow = new Uri(ui, "workflow");
			expiry = new Expiry(run, ui.getAbsolutePathBuilder().path("expiry"));
			status = new Uri(ui, "status");
			workingDirectory = new Uri(ui, "wd");
			listeners = new ListenerList(run, ui.getAbsolutePathBuilder().path(
					"listeners"));
			securityContext = new Uri(ui, "security");
			inputs = new Uri(ui, "input");
			output = new Uri(ui, "output");
			createTime = new Uri(ui, "createTime");
			startTime = new Uri(ui, "startTime");
			finishTime = new Uri(ui, "finishTime");
			owner = run.getSecurityContext().getOwner().getName();
		}
	}
}
