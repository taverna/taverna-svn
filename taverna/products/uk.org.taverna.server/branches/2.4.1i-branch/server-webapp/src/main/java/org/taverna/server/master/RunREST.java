/*
 * Copyright (C) 2010-2011 The University of Manchester
 * 
 * See the file "LICENSE.txt" for license terms.
 */
package org.taverna.server.master;

import static javax.ws.rs.core.Response.noContent;
import static javax.ws.rs.core.Response.ok;
import static javax.ws.rs.core.Response.status;
import static org.joda.time.format.ISODateTimeFormat.dateTime;
import static org.joda.time.format.ISODateTimeFormat.dateTimeParser;
import static org.taverna.server.master.TavernaServerImpl.log;
import static org.taverna.server.master.common.Status.Initialized;
import static org.taverna.server.master.common.Status.Operating;

import java.net.MalformedURLException;
import java.net.URI;
import java.util.Date;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Required;
import org.taverna.server.master.TavernaServerImpl.SupportAware;
import org.taverna.server.master.common.Status;
import org.taverna.server.master.common.Workflow;
import org.taverna.server.master.exceptions.BadStateChangeException;
import org.taverna.server.master.exceptions.FilesystemAccessException;
import org.taverna.server.master.exceptions.NoDirectoryEntryException;
import org.taverna.server.master.exceptions.NoUpdateException;
import org.taverna.server.master.exceptions.NotOwnerException;
import org.taverna.server.master.exceptions.OverloadedException;
import org.taverna.server.master.exceptions.UnknownRunException;
import org.taverna.server.master.interaction.InteractionFeedSupport;
import org.taverna.server.master.interfaces.TavernaRun;
import org.taverna.server.master.interfaces.TavernaSecurityContext;
import org.taverna.server.master.rest.InteractionFeedREST;
import org.taverna.server.master.rest.TavernaServerInputREST;
import org.taverna.server.master.rest.TavernaServerListenersREST;
import org.taverna.server.master.rest.TavernaServerRunREST;
import org.taverna.server.master.rest.TavernaServerSecurityREST;
import org.taverna.server.master.utils.InvocationCounter.CallCounted;
import org.taverna.server.port_description.OutputDescription;

/**
 * RESTful interface to a single workflow run.
 * 
 * @author Donal Fellows
 */
abstract class RunREST implements TavernaServerRunREST, RunBean {
	private String runName;
	private TavernaRun run;
	private TavernaServerSupport support;
	private ContentsDescriptorBuilder cdBuilder;
	private InteractionFeedSupport interactionFeed;

	@Override
	@Required
	public void setSupport(TavernaServerSupport support) {
		this.support = support;
	}

	@Override
	@Required
	public void setCdBuilder(ContentsDescriptorBuilder cdBuilder) {
		this.cdBuilder = cdBuilder;
	}

	@Override
	@Required
	public void setInteractionFeed(InteractionFeedSupport interactionFeed) {
		this.interactionFeed = interactionFeed;
	}
	
	@Override
	public void setRunName(String runName) {
		this.runName = runName;
	}

	@Override
	public void setRun(TavernaRun run) {
		this.run = run;
	}

	@Override
	@CallCounted
	public RunDescription getDescription(UriInfo ui) {
		return new RunDescription(run, ui);
	}

	@Override
	@CallCounted
	public Response destroy() throws NoUpdateException {
		try {
			support.unregisterRun(runName, run);
		} catch (UnknownRunException e) {
			log.fatal("can't happen", e);
		}
		return noContent().build();
	}

	@Override
	@CallCounted
	public TavernaServerListenersREST getListeners() {
		return makeListenersInterface().connect(run);
	}

	@Override
	@CallCounted
	public TavernaServerSecurityREST getSecurity() throws NotOwnerException {
		TavernaSecurityContext secContext = run.getSecurityContext();
		if (!support.getPrincipal().equals(secContext.getOwner()))
			throw new NotOwnerException();

		// context.getBean("run.security", run, secContext);
		return makeSecurityInterface().connect(secContext, run);
	}

	@Override
	@CallCounted
	public String getExpiryTime() {
		return dateTime().print(new DateTime(run.getExpiry()));
	}

	@Override
	@CallCounted
	public String getCreateTime() {
		return dateTime().print(new DateTime(run.getCreationTimestamp()));
	}

	@Override
	@CallCounted
	public String getFinishTime() {
		Date f = run.getFinishTimestamp();
		return f == null ? "" : dateTime().print(new DateTime(f));
	}

	@Override
	@CallCounted
	public String getStartTime() {
		Date f = run.getStartTimestamp();
		return f == null ? "" : dateTime().print(new DateTime(f));
	}

	@Override
	@CallCounted
	public String getStatus() {
		return run.getStatus().toString();
	}

	@Override
	@CallCounted
	public Workflow getWorkflow() {
		return run.getWorkflow();
	}

	@Override
	@CallCounted
	public DirectoryREST getWorkingDirectory() {
		return makeDirectoryInterface().connect(run);
	}

	@Override
	@CallCounted
	public String setExpiryTime(String expiry) throws NoUpdateException,
			IllegalArgumentException {
		DateTime wanted = dateTimeParser().parseDateTime(expiry.trim());
		Date achieved = support.updateExpiry(run, wanted.toDate());
		return dateTime().print(new DateTime(achieved));
	}

	@Override
	@CallCounted
	public Response setStatus(String status) throws NoUpdateException {
		Status newStatus = Status.valueOf(status.trim());
		support.permitUpdate(run);
		if (newStatus == Operating && run.getStatus() == Initialized) {
			if (!support.getAllowStartWorkflowRuns())
				throw new OverloadedException();
			String issue = run.setStatus(newStatus);
			if (issue == null)
				issue = "starting run...";
			return status(202).entity(issue).type("text/plain").build();
		}
		run.setStatus(newStatus); // Ignore the result
		return ok(run.getStatus().toString()).type("text/plain").build();
	}

	@Override
	@CallCounted
	public TavernaServerInputREST getInputs(UriInfo ui) {
		return makeInputInterface().connect(run, ui);
	}

	@Override
	@CallCounted
	public String getOutputFile() {
		String o = run.getOutputBaclavaFile();
		return o == null ? "" : o;
	}

	@Override
	@CallCounted
	public String setOutputFile(String filename) throws NoUpdateException,
			FilesystemAccessException, BadStateChangeException {
		support.permitUpdate(run);
		if (filename != null && filename.length() == 0)
			filename = null;
		run.setOutputBaclavaFile(filename);
		String o = run.getOutputBaclavaFile();
		return o == null ? "" : o;
	}

	@Override
	@CallCounted
	public OutputDescription getOutputDescription(UriInfo ui)
			throws BadStateChangeException, FilesystemAccessException,
			NoDirectoryEntryException {
		if (run.getStatus() == Initialized)
			throw new BadStateChangeException(
					"may not get output description in initial state");
		return cdBuilder.makeOutputDescriptor(run, ui);
	}

	public InteractionFeedREST getInteractionFeed() {
		return new InteractionFeed(interactionFeed, run);
	}

	/**
	 * Construct a RESTful interface to a run's filestore.
	 * 
	 * @return The handle to the interface, as decorated by Spring.
	 */
	protected abstract DirectoryREST makeDirectoryInterface();

	/**
	 * Construct a RESTful interface to a run's input descriptors.
	 * 
	 * @return The handle to the interface, as decorated by Spring.
	 */
	protected abstract InputREST makeInputInterface();

	/**
	 * Construct a RESTful interface to a run's listeners.
	 * 
	 * @return The handle to the interface, as decorated by Spring.
	 */
	protected abstract ListenersREST makeListenersInterface();

	/**
	 * Construct a RESTful interface to a run's security.
	 * 
	 * @return The handle to the interface, as decorated by Spring.
	 */
	protected abstract RunSecurityREST makeSecurityInterface();
}

/**
 * Description of properties supported by {@link RunREST}.
 * 
 * @author Donal Fellows
 */
interface RunBean extends SupportAware {
	void setCdBuilder(ContentsDescriptorBuilder cdBuilder);

	void setInteractionFeed(InteractionFeedSupport interactionFeed);

	void setRun(TavernaRun run);

	void setRunName(String runName);
}

class InteractionFeed implements InteractionFeedREST {
	private InteractionFeedSupport interactionFeed;
	private TavernaRun run;

	InteractionFeed(InteractionFeedSupport interactionFeed, TavernaRun run) {
		this.interactionFeed = interactionFeed;
		this.run = run;
	}

	@Override
	public Feed getFeed() throws FilesystemAccessException,
			NoDirectoryEntryException {
		return interactionFeed.getRunFeed(run);
	}

	@Override
	public Response addEntry(Entry entry) throws MalformedURLException,
			FilesystemAccessException, NoDirectoryEntryException,
			NoUpdateException {
		String location = interactionFeed.addRunFeedEntry(run, entry);
		return Response.created(URI.create(location)).build();
	}

	@Override
	public Entry getEntry(String id) throws FilesystemAccessException,
			NoDirectoryEntryException {
		return interactionFeed.getRunFeedEntry(run, id);
	}

	@Override
	public String deleteEntry(String id) throws FilesystemAccessException,
			NoDirectoryEntryException, NoUpdateException {
		interactionFeed.removeRunFeedEntry(run, id);
		return "entry successfully deleted";
	}
}