package org.taverna.server.master;

import static java.lang.Math.min;
import static java.util.Arrays.asList;
import static java.util.UUID.randomUUID;
import static javax.ws.rs.core.MediaType.APPLICATION_OCTET_STREAM_TYPE;
import static javax.ws.rs.core.Response.seeOther;
import static javax.ws.rs.core.Response.temporaryRedirect;
import static org.taverna.server.master.common.DirEntryReference.newInstance;

import java.io.StringWriter;
import java.security.Principal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.jws.WebService;
import javax.ws.rs.Path;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.ws.WebServiceContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.taverna.server.master.common.DirEntryReference;
import org.taverna.server.master.common.InputDescription;
import org.taverna.server.master.common.RunReference;
import org.taverna.server.master.common.SCUFL;
import org.taverna.server.master.common.Status;
import org.taverna.server.master.exceptions.BadPropertyValueException;
import org.taverna.server.master.exceptions.BadStateChangeException;
import org.taverna.server.master.exceptions.FilesystemAccessException;
import org.taverna.server.master.exceptions.NoCreateException;
import org.taverna.server.master.exceptions.NoListenerException;
import org.taverna.server.master.exceptions.NoUpdateException;
import org.taverna.server.master.exceptions.UnknownRunException;
import org.taverna.server.master.factories.ListenerFactory;
import org.taverna.server.master.factories.RunFactory;
import org.taverna.server.master.interfaces.Directory;
import org.taverna.server.master.interfaces.DirectoryEntry;
import org.taverna.server.master.interfaces.File;
import org.taverna.server.master.interfaces.Input;
import org.taverna.server.master.interfaces.Listener;
import org.taverna.server.master.interfaces.Policy;
import org.taverna.server.master.interfaces.RunStore;
import org.taverna.server.master.interfaces.TavernaRun;
import org.taverna.server.master.rest.DirectoryContents;
import org.taverna.server.master.rest.ListenerDefinition;
import org.taverna.server.master.rest.MakeOrUpdateDirEntry;
import org.taverna.server.master.rest.TavernaServerDirectoryREST;
import org.taverna.server.master.rest.TavernaServerInputREST;
import org.taverna.server.master.rest.TavernaServerListenersREST;
import org.taverna.server.master.rest.TavernaServerREST;
import org.taverna.server.master.rest.TavernaServerRunREST;
import org.taverna.server.master.rest.MakeOrUpdateDirEntry.MakeDirectory;
import org.taverna.server.master.rest.TavernaServerInputREST.InDesc.AbstractContents;
import org.taverna.server.master.rest.TavernaServerListenersREST.ListenerDescription;
import org.taverna.server.master.rest.TavernaServerListenersREST.TavernaServerListenerREST;
import org.taverna.server.master.soap.TavernaServerSOAP;

/**
 * The core implementation of the web application.
 * 
 * @author Donal Fellows
 */
@Path("/rest")
@WebService(endpointInterface = "org.taverna.server.master.TavernaServerSOAP", serviceName = "TavernaServer", targetNamespace = "http://www.taverna.org.uk/v2/")
@ManagedResource(objectName = "Taverna:group=Server,name=Webapp", description = "The main web-application interface to Taverna Server.")
public class TavernaServerImpl implements TavernaServerSOAP, TavernaServerREST {
	/** The logger for the server framework. */
	public static Log log = LogFactory.getLog(TavernaServerImpl.class);
	private static final String REST_BASE = "/taverna2/rest";
	static int invokes;
	private JAXBContext scuflSerializer;
	/**
	 * Whether outgoing exceptions should be logged before being converted to
	 * responses.
	 */
	public static boolean logOutgoingExceptions = false;

	/**
	 * @throws JAXBException
	 */
	public TavernaServerImpl() throws JAXBException {
		scuflSerializer = JAXBContext.newInstance(SCUFL.class);
	}

	/**
	 * @return Count of the number of external calls into this webapp.
	 */
	@ManagedAttribute(description = "Count of the number of external calls into this webapp.")
	public int getInvocationCount() {
		return invokes;
	}

	/**
	 * @return Current number of runs.
	 */
	@ManagedAttribute(description = "Current number of runs.")
	public int getCurrentRunCount() {
		return runStore.listRuns(null, null).size();
	}

	/**
	 * @return Whether to write submitted workflows to the log.
	 */
	@ManagedAttribute(description = "Whether to write submitted workflows to the log.")
	public boolean getLogIncomingWorkflows() {
		return logIncomingWorkflows;
	}

	/**
	 * @param logIncomingWorkflows
	 *            Whether to write submitted workflows to the log.
	 */
	@ManagedAttribute(description = "Whether to write submitted workflows to the log.")
	public void setLogIncomingWorkflows(boolean logIncomingWorkflows) {
		this.logIncomingWorkflows = logIncomingWorkflows;
	}

	/**
	 * @return Whether outgoing exceptions should be logged before being
	 *         converted to responses.
	 */
	@ManagedAttribute(description = "Whether outgoing exceptions should be logged before being converted to responses.")
	public boolean getLogOutgoingExceptions() {
		return logOutgoingExceptions;
	}

	/**
	 * @param logOutgoing
	 *            Whether outgoing exceptions should be logged before being
	 *            converted to responses.
	 */
	@ManagedAttribute(description = "Whether outgoing exceptions should be logged before being converted to responses.")
	public void setLogOutgoingExceptions(boolean logOutgoing) {
		logOutgoingExceptions = logOutgoing;
	}

	/** Whether we should log all workflows sent to us. */
	private boolean logIncomingWorkflows;

	/** Whether we allow the creation of new workflow runs. */
	private boolean allowNewWorkflowRuns = true;

	@Resource
	private WebServiceContext jaxwsContext;
	@Resource
	private SecurityContext jaxrsContext;

	/** Encapsulates the policies applied by this server. */
	Policy policy;
	/** A factory for workflow runs. */
	RunFactory runFactory;
	/** A storage facility for workflow runs. */
	RunStore runStore;
	/** A factory for event listeners to attach to workflow runs. */
	ListenerFactory listenerFactory;

	/**
	 * @param policy
	 */
	public void setPolicy(Policy policy) {
		this.policy = policy;
	}

	/**
	 * @param listenerFactory
	 */
	public void setListenerFactory(ListenerFactory listenerFactory) {
		this.listenerFactory = listenerFactory;
	}

	/**
	 * @param runFactory
	 */
	public void setRunFactory(RunFactory runFactory) {
		this.runFactory = runFactory;
	}

	/**
	 * @param runStore
	 */
	public void setRunStore(RunStore runStore) {
		this.runStore = runStore;
	}

	@Override
	public ServerDescription describeService(UriInfo ui) {
		invokes++;
		return new ServerDescription(runStore.listRuns(getPrincipal(), policy),
				ui);
	}

	private static DateFormat isoFormat;

	static DateFormat df() {
		if (isoFormat == null) {
			isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
		}
		return isoFormat;
	}

	@Override
	public RunReference[] listRuns() {
		invokes++;
		Principal p = getPrincipal();
		ArrayList<RunReference> ws = new ArrayList<RunReference>();
		UriBuilder ub = UriBuilder.fromUri(REST_BASE + "/runs");
		for (String runName : runStore.listRuns(p, policy).keySet())
			ws.add(new RunReference(runName, ub));
		return ws.toArray(new RunReference[ws.size()]);
	}

	private void logWorkflow(SCUFL workflow) {
		StringWriter sw = new StringWriter();
		try {
			scuflSerializer.createMarshaller().marshal(workflow, sw);
			log.info(sw);
		} catch (JAXBException e) {
			log.warn("problem when logging workflow", e);
		}
	}

	@Override
	public Response submitWorkflow(SCUFL workflow, UriInfo ui)
			throws NoUpdateException {
		invokes++;
		if (!allowNewWorkflowRuns) {
			throw new NoCreateException("run creation not currently enabled");
		}
		if (logIncomingWorkflows) {
			logWorkflow(workflow);
		}
		Principal p = getPrincipal();
		policy.permitCreate(p, workflow);
		TavernaRun w;
		try {
			w = runFactory.create(p, workflow);
		} catch (Exception e) {
			log.error("failed to build workflow run worker", e);
			throw new NoCreateException("failed to build workflow run worker");
		}
		String uuid = randomUUID().toString();
		runStore.registerRun(uuid, w);
		return seeOther(ui.getRequestUriBuilder().path("{uuid}").build(uuid))
				.build();
	}

	@Override
	public RunReference submitWorkflow(SCUFL workflow) throws NoUpdateException {
		invokes++;
		if (!allowNewWorkflowRuns) {
			throw new NoCreateException("run creation not currently enabled");
		}
		if (logIncomingWorkflows) {
			logWorkflow(workflow);
		}
		Principal p = getPrincipal();
		policy.permitCreate(p, workflow);
		UriBuilder ub = UriBuilder.fromUri(REST_BASE + "/runs");
		TavernaRun w;
		try {
			w = runFactory.create(p, workflow);
		} catch (Exception e) {
			log.error("failed to build workflow run worker", e);
			throw new NoCreateException("failed to build workflow run worker");
		}
		String uuid = randomUUID().toString();
		runStore.registerRun(uuid, w);
		return new RunReference(uuid, ub);
	}

	@Override
	public int getMaxSimultaneousRuns() {
		invokes++;
		Integer limit = policy.getMaxRuns(getPrincipal());
		if (limit == null)
			return policy.getMaxRuns();
		return min(limit.intValue(), policy.getMaxRuns());
	}

	@Override
	public PermittedWorkflows getPermittedWorkflows() {
		invokes++;
		return new PermittedWorkflows(policy
				.listPermittedWorkflows(getPrincipal()));
	}

	@Override
	public SCUFL[] getAllowedWorkflows() {
		invokes++;
		return policy.listPermittedWorkflows(getPrincipal()).toArray(
				new SCUFL[0]);
	}

	@Override
	public PermittedListeners getPermittedListeners() {
		invokes++;
		return new PermittedListeners(listenerFactory
				.getSupportedListenerTypes());
	}

	@Override
	public String[] getAllowedListeners() {
		invokes++;
		return listenerFactory.getSupportedListenerTypes().toArray(
				new String[0]);
	}

	@Override
	public TavernaServerRunREST getRunResource(final String runName)
			throws UnknownRunException {
		invokes++;
		final TavernaRun run = getRun(runName);
		return new TavernaServerRunREST() {
			@Override
			public RunDescription getDescription(UriInfo ui) {
				invokes++;
				return new RunDescription(run, ui);
			}

			@Override
			public Response destroy(UriInfo ui) throws NoUpdateException {
				invokes++;
				policy.permitDestroy(getPrincipal(), run);
				runStore.unregisterRun(runName);
				run.destroy();
				return temporaryRedirect(
						ui.getBaseUriBuilder().path("runs").build()).build();
			}

			@Override
			public TavernaServerListenersREST getListeners() {
				invokes++;
				return new TavernaServerListenersREST() {
					@Override
					public Response addListener(
							ListenerDefinition typeAndConfiguration, UriInfo ui)
							throws NoUpdateException, NoListenerException {
						invokes++;
						policy.permitUpdate(getPrincipal(), run);
						String name = listenerFactory.makeListener(run,
								typeAndConfiguration.type,
								typeAndConfiguration.configuration).getName();
						return seeOther(
								ui.getAbsolutePathBuilder().path(
										"{listenerName}").build(name)).build();
					}

					@Override
					public TavernaServerListenerREST getListener(String name)
							throws NoListenerException {
						invokes++;
						for (Listener listener : run.getListeners())
							if (listener.getName().equals(name))
								return new ListenerIfcImpl(listener);
						throw new NoListenerException();
					}

					@Override
					public Listeners getDescription(UriInfo ui) {
						List<ListenerDescription> result = new ArrayList<ListenerDescription>();
						invokes++;
						for (Listener l : run.getListeners())
							result.add(new ListenerDescription(l.getName(), l
									.getType(), l.listProperties(), ui));
						return new Listeners(result);
					}
				};
			}

			@Override
			public String getOwner() {
				invokes++;
				return run.getSecurityContext().getOwner().getName();
			}

			@Override
			public String getExpiry() {
				invokes++;
				return df().format(run.getExpiry());
			}

			@Override
			public String getStatus() {
				invokes++;
				return run.getStatus().toString();
			}

			@Override
			public SCUFL getWorkflow() {
				invokes++;
				return run.getWorkflow();
			}

			@Override
			public DirectoryREST getWorkingDirectory() {
				invokes++;
				return new DirectoryREST(run);
			}

			@Override
			public Response setExpiry(String expiry, UriInfo ui)
					throws NoUpdateException {
				invokes++;
				policy.permitDestroy(getPrincipal(), run);
				try {
					run.setExpiry(df().parse(expiry));
				} catch (ParseException e) {
					throw new NoUpdateException(e.getMessage(), e);
				}
				return seeOther(ui.getRequestUri()).build();
			}

			@Override
			public Response setStatus(String status, UriInfo ui)
					throws NoUpdateException {
				invokes++;
				policy.permitUpdate(getPrincipal(), run);
				run.setStatus(Status.valueOf(status));
				return seeOther(ui.getRequestUri()).build();
			}

			@Override
			public TavernaServerInputREST getInputs() {
				invokes++;
				return new TavernaServerInputREST() {
					@Override
					public InputsDescriptor get(UriInfo ui) {
						invokes++;
						return new InputsDescriptor(ui, run);
					}

					@Override
					public String getBaclavaFile() {
						invokes++;
						String i = run.getInputBaclavaFile();
						return i == null ? "" : i;
					}

					@Override
					public InDesc getInput(String name)
							throws BadPropertyValueException {
						invokes++;
						for (Input i : run.getInputs())
							if (i.getName().equals(name))
								return new InDesc(i);
						throw new BadPropertyValueException(
								"unknown input port name");
					}

					@Override
					public Response setBaclavaFile(String filename, UriInfo ui)
							throws NoUpdateException, BadStateChangeException,
							FilesystemAccessException {
						invokes++;
						policy.permitUpdate(getPrincipal(), run);
						run.setInputBaclavaFile(filename);
						return seeOther(ui.getRequestUri()).build();
					}

					@Override
					public Response setInput(String name,
							InDesc inputDescriptor, UriInfo ui)
							throws NoUpdateException, BadStateChangeException,
							FilesystemAccessException,
							BadPropertyValueException {
						invokes++;
						AbstractContents ac = inputDescriptor.assignment;
						if (name == null || name.isEmpty())
							throw new BadPropertyValueException(
									"bad property name");
						if (ac == null)
							throw new BadPropertyValueException("no content!");
						if (!(ac instanceof InDesc.File || ac instanceof InDesc.Value))
							throw new BadPropertyValueException(
									"unknown content type");
						policy.permitUpdate(getPrincipal(), run);
						for (Input i : run.getInputs()) {
							if (!i.getName().equals(name))
								continue;
							if (ac instanceof InDesc.File) {
								i.setFile(ac.contents);
							} else {
								i.setValue(ac.contents);
							}
							return seeOther(ui.getRequestUri()).build();
						}
						Input i = run.makeInput(name);
						if (ac instanceof InDesc.File) {
							i.setFile(ac.contents);
						} else {
							i.setValue(ac.contents);
						}
						return seeOther(ui.getRequestUri()).build();
					}
				};
			}

			@Override
			public String getOutputFile() {
				invokes++;
				String o = run.getOutputBaclavaFile();
				return o == null ? "" : o;
			}

			@Override
			public Response setOutputFile(String filename, UriInfo ui)
					throws NoUpdateException, FilesystemAccessException,
					BadStateChangeException {
				invokes++;
				policy.permitUpdate(getPrincipal(), run);
				if (filename != null && filename.length() == 0)
					filename = null;
				run.setOutputBaclavaFile(filename);
				return seeOther(ui.getRequestUri()).build();
			}

			class ListenerIfcImpl implements TavernaServerListenerREST {
				Listener listen;

				ListenerIfcImpl(Listener l) {
					this.listen = l;
				}

				@Override
				public String getConfiguration() {
					invokes++;
					return listen.getConfiguration();
				}

				@Override
				public ListenerDescription getDescription(UriInfo ui) {
					invokes++;
					return new ListenerDescription(listen.getName(), listen
							.getType(), listen.listProperties(), ui);
				}

				@Override
				public TavernaServerListenersREST.Properties getProperties(
						UriInfo ui) {
					invokes++;
					return new TavernaServerListenersREST.Properties(ui
							.getAbsolutePathBuilder().path(
									"../../{listener}/properties/{prop}"),
							listen.getName(), listen.listProperties());
				}

				@Override
				public TavernaServerListenersREST.Property getProperty(
						final String propertyName) throws NoListenerException {
					invokes++;
					List<String> p = asList(listen.listProperties());
					if (p.contains(propertyName))
						return new TavernaServerListenersREST.Property() {
							@Override
							public String getValue() {
								invokes++;
								try {
									return listen.getProperty(propertyName);
								} catch (NoListenerException e) {
									log.error(
											"unexpected exception; property \""
													+ propertyName
													+ "\" should exist", e);
									return null;
								}
							}

							@Override
							public Response setValue(String value, UriInfo ui)
									throws NoUpdateException,
									NoListenerException {
								invokes++;
								policy.permitUpdate(getPrincipal(), run);
								listen.setProperty(propertyName, value);
								return seeOther(ui.getRequestUri()).build();
							}
						};

					throw new NoListenerException("no such property");
				}
			}
		};
	}

	class DirectoryREST implements TavernaServerDirectoryREST {
		private TavernaRun run;

		DirectoryREST(TavernaRun w) {
			this.run = w;
		}

		@Override
		public Response destroyDirectoryEntry(List<PathSegment> path, UriInfo ui)
				throws NoUpdateException, FilesystemAccessException {
			invokes++;
			policy.permitUpdate(getPrincipal(), run);
			DirectoryEntry entry = getDirEntry(run, path);
			entry.destroy();
			return temporaryRedirect(
					ui.getAbsolutePathBuilder().path("..").build()).build();
		}

		@Override
		public DirectoryContents getDescription(UriInfo ui)
				throws FilesystemAccessException {
			invokes++;
			return new DirectoryContents(ui, run.getWorkingDirectory()
					.getContents());
		}

		// Nasty! This can have several different responses...
		@Override
		public Response getDirectoryOrFileContents(List<PathSegment> path,
				UriInfo ui) throws FilesystemAccessException {
			invokes++;
			DirectoryEntry de = getDirEntry(run, path);
			if (de instanceof File) {
				return Response.ok(((File) de).getContents()).type(
						APPLICATION_OCTET_STREAM_TYPE).build();
			} else if (de instanceof Directory) {
				return Response.ok(
						new DirectoryContents(ui, ((Directory) de)
								.getContents())).build();
			} else {
				throw new FilesystemAccessException("not a directory");
			}
		}

		@Override
		public Response makeDirectoryOrUpdateFile(List<PathSegment> parent,
				MakeOrUpdateDirEntry op, UriInfo ui) throws NoUpdateException,
				FilesystemAccessException {
			invokes++;
			policy.permitUpdate(getPrincipal(), run);
			DirectoryEntry container = getDirEntry(run, parent);
			if (!(container instanceof Directory)) {
				throw new FilesystemAccessException(
						"You may not "
								+ ((op instanceof MakeDirectory) ? "make a subdirectory of"
										: "place a file in") + " a file.");
			}
			if (op.name == null)
				throw new FilesystemAccessException("missing name attribute");
			Directory d = (Directory) container;
			UriBuilder ub = ui.getAbsolutePathBuilder().path("{name}");
			if (op instanceof MakeDirectory) {
				Directory dir = d.makeSubdirectory(getPrincipal(), op.name);
				return temporaryRedirect(ub.build(dir.getName())).build();
			} else {
				File f = null;
				for (DirectoryEntry e : d.getContents()) {
					if (e.getName().equals(op.name)) {
						if (e instanceof Directory) {
							throw new FilesystemAccessException(
									"You may not overwrite a directory with a file.");
						}
						f = (File) e;
						break;
					}
				}
				if (f == null)
					f = d.makeEmptyFile(getPrincipal(), op.name);
				f.setContents(op.contents);
				return temporaryRedirect(ub.build(f.getName())).build();
			}
		}
	}

	@Override
	public void destroyRun(String uuid) throws UnknownRunException,
			NoUpdateException {
		invokes++;
		TavernaRun w = getRun(uuid);
		policy.permitUpdate(getPrincipal(), w);
		runStore.unregisterRun(uuid);
		w.destroy();
	}

	@Override
	public SCUFL getRunWorkflow(String uuid) throws UnknownRunException {
		invokes++;
		return getRun(uuid).getWorkflow();
	}

	@Override
	public Date getRunExpiry(String uuid) throws UnknownRunException {
		invokes++;
		return getRun(uuid).getExpiry();
	}

	@Override
	public void setRunExpiry(String uuid, Date d) throws UnknownRunException,
			NoUpdateException {
		invokes++;
		TavernaRun w = getRun(uuid);
		policy.permitDestroy(getPrincipal(), w);
		w.setExpiry(d);
	}

	@Override
	public Status getRunStatus(String uuid) throws UnknownRunException {
		invokes++;
		return getRun(uuid).getStatus();
	}

	@Override
	public void setRunStatus(String uuid, Status s) throws UnknownRunException,
			NoUpdateException {
		invokes++;
		TavernaRun w = getRun(uuid);
		policy.permitUpdate(getPrincipal(), w);
		w.setStatus(s);
	}

	@Override
	public String getRunOwner(String uuid) throws UnknownRunException {
		invokes++;
		return getRun(uuid).getSecurityContext().getOwner().getName();
	}

	// -=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-
	// Operations on the filesystem

	@Override
	public DirEntryReference[] getRunDirectoryContents(String uuid,
			DirEntryReference d) throws UnknownRunException,
			FilesystemAccessException {
		invokes++;
		TavernaRun w = getRun(uuid);
		DirectoryEntry de = getDirEntry(w, d);
		if (!(de instanceof Directory))
			throw new FilesystemAccessException("not a directory");

		List<DirEntryReference> result = new ArrayList<DirEntryReference>();
		for (DirectoryEntry e : ((Directory) de).getContents())
			result.add(newInstance(null, e));
		return result.toArray(new DirEntryReference[result.size()]);
	}

	@Override
	public DirEntryReference makeRunDirectory(String uuid,
			DirEntryReference parent, String name) throws UnknownRunException,
			NoUpdateException, FilesystemAccessException {
		invokes++;
		TavernaRun w = getRun(uuid);
		policy.permitUpdate(getPrincipal(), w);
		DirectoryEntry container = getDirEntry(w, parent);
		if (!(container instanceof Directory))
			throw new FilesystemAccessException("not inside a directory");
		Directory dir = ((Directory) container).makeSubdirectory(
				getPrincipal(), name);
		return newInstance(null, dir);
	}

	@Override
	public DirEntryReference makeRunFile(String uuid, DirEntryReference parent,
			String name) throws UnknownRunException, NoUpdateException,
			FilesystemAccessException {
		invokes++;
		TavernaRun w = getRun(uuid);
		policy.permitUpdate(getPrincipal(), w);
		DirectoryEntry container = getDirEntry(w, parent);
		if (!(container instanceof Directory))
			throw new FilesystemAccessException("not inside a directory");
		File f = ((Directory) container).makeEmptyFile(getPrincipal(), name);
		return newInstance(null, f);
	}

	@Override
	public void destroyRunDirectoryEntry(String uuid, DirEntryReference d)
			throws UnknownRunException, NoUpdateException,
			FilesystemAccessException {
		invokes++;
		TavernaRun w = getRun(uuid);
		policy.permitUpdate(getPrincipal(), w);
		getDirEntry(w, d).destroy();
	}

	@Override
	public byte[] getRunFileContents(String uuid, DirEntryReference d)
			throws UnknownRunException, FilesystemAccessException {
		invokes++;
		DirectoryEntry de = getDirEntry(getRun(uuid), d);
		if (!(de instanceof File))
			throw new FilesystemAccessException("not a file");
		return ((File) de).getContents();
	}

	@Override
	public void setRunFileContents(String uuid, DirEntryReference d,
			byte[] newContents) throws UnknownRunException, NoUpdateException,
			FilesystemAccessException {
		invokes++;
		TavernaRun w = getRun(uuid);
		policy.permitUpdate(getPrincipal(), w);
		DirectoryEntry de = getDirEntry(w, d);
		if (!(de instanceof File))
			throw new FilesystemAccessException("not a file");
		((File) de).setContents(newContents);
	}

	@Override
	public long getRunFileLength(String uuid, DirEntryReference d)
			throws UnknownRunException, FilesystemAccessException {
		invokes++;
		DirectoryEntry de = getDirEntry(getRun(uuid), d);
		if (!(de instanceof File))
			throw new FilesystemAccessException("not a file");
		return ((File) de).getSize();
	}

	// -=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-
	// Operations on listeners

	@Override
	public String[] getRunListeners(String uuid) throws UnknownRunException {
		invokes++;
		TavernaRun w = getRun(uuid);
		List<String> result = new ArrayList<String>();
		for (Listener l : w.getListeners()) {
			result.add(l.getName());
		}
		return result.toArray(new String[result.size()]);
	}

	@Override
	public String addRunListener(String uuid, String listenerType,
			String configuration) throws UnknownRunException,
			NoUpdateException, NoListenerException {
		invokes++;
		TavernaRun w = getRun(uuid);
		policy.permitUpdate(getPrincipal(), w);
		return listenerFactory.makeListener(w, listenerType, configuration)
				.getName();
	}

	@Override
	public String getRunListenerConfiguration(String uuid, String listenerName)
			throws UnknownRunException, NoListenerException {
		invokes++;
		for (Listener l : getRun(uuid).getListeners()) {
			if (l.getName().equals(listenerName)) {
				return l.getConfiguration();
			}
		}
		throw new NoListenerException();
	}

	@Override
	public String[] getRunListenerProperties(String uuid, String listenerName)
			throws UnknownRunException, NoListenerException {
		invokes++;
		for (Listener l : getRun(uuid).getListeners()) {
			if (l.getName().equals(listenerName)) {
				return l.listProperties().clone();
			}
		}
		throw new NoListenerException();
	}

	@Override
	public String getRunListenerProperty(String uuid, String listenerName,
			String propName) throws UnknownRunException, NoListenerException {
		invokes++;
		for (Listener l : getRun(uuid).getListeners()) {
			if (l.getName().equals(listenerName)) {
				String propValue = l.getProperty(propName);
				if (propValue == null)
					throw new NoListenerException(
							"listener does not have property");
				return propValue;
			}
		}
		throw new NoListenerException();
	}

	@Override
	public void setRunListenerProperty(String uuid, String listenerName,
			String propName, String value) throws UnknownRunException,
			NoUpdateException, NoListenerException {
		invokes++;
		TavernaRun w = getRun(uuid);
		policy.permitUpdate(getPrincipal(), w);
		for (Listener l : w.getListeners()) {
			if (l.getName().equals(listenerName)) {
				if (l.getProperty(propName) == null)
					throw new NoListenerException(
							"listener does not have property");
				try {
					l.setProperty(propName, value);
				} catch (RuntimeException e) {
					throw new NoListenerException("problem setting property: "
							+ e.getMessage());
				}
				return;
			}
		}
		throw new NoListenerException();
	}

	@Override
	public InputDescription getRunInputs(String runName)
			throws UnknownRunException {
		invokes++;
		return new InputDescription(getRun(runName));
	}

	@Override
	public String getRunOutputBaclavaFile(String runName)
			throws UnknownRunException {
		invokes++;
		return getRun(runName).getOutputBaclavaFile();
	}

	@Override
	public void setRunInputBaclavaFile(String runName, String fileName)
			throws UnknownRunException, NoUpdateException,
			FilesystemAccessException, BadStateChangeException {
		invokes++;
		TavernaRun w = getRun(runName);
		policy.permitUpdate(getPrincipal(), w);
		w.setInputBaclavaFile(fileName);
	}

	@Override
	public void setRunInputPortFile(String runName, String portName,
			String portFilename) throws UnknownRunException, NoUpdateException,
			FilesystemAccessException, BadStateChangeException {
		invokes++;
		TavernaRun w = getRun(runName);
		policy.permitUpdate(getPrincipal(), w);
		for (Input i : w.getInputs())
			if (i.getName().equals(portName)) {
				i.setFile(portFilename);
				return;
			}
		w.makeInput(portName).setFile(portFilename);
	}

	@Override
	public void setRunInputPortValue(String runName, String portName,
			String portValue) throws UnknownRunException, NoUpdateException,
			BadStateChangeException {
		invokes++;
		TavernaRun w = getRun(runName);
		policy.permitUpdate(getPrincipal(), w);
		policy.permitUpdate(getPrincipal(), w);
		for (Input i : w.getInputs())
			if (i.getName().equals(portName)) {
				i.setValue(portValue);
				return;
			}
		w.makeInput(portName).setValue(portValue);
	}

	@Override
	public void setRunOutputBaclavaFile(String runName, String outputFile)
			throws UnknownRunException, NoUpdateException,
			FilesystemAccessException, BadStateChangeException {
		invokes++;
		TavernaRun w = getRun(runName);
		policy.permitUpdate(getPrincipal(), w);
		w.setOutputBaclavaFile(outputFile);
	}

	// -=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-

	TavernaRun getRun(String uuid) throws UnknownRunException {
		Principal p = getPrincipal();
		for (Map.Entry<String, TavernaRun> w : runStore.listRuns(p, policy)
				.entrySet()) {
			if (w.getKey().equals(uuid)) {
				return w.getValue();
			}
		}
		throw new UnknownRunException();
	}

	DirectoryEntry getDirEntry(TavernaRun run, DirEntryReference d)
			throws FilesystemAccessException {
		Directory dir = run.getWorkingDirectory();
		DirectoryEntry found = dir;
		for (String bit : d.path.split("/")) {
			found = null;
			if (dir == null)
				throw new FilesystemAccessException("no such directory entry");
			for (DirectoryEntry entry : dir.getContents()) {
				if (entry.getName().equals(bit)) {
					found = entry;
					break;
				}
			}
			if (found == null)
				throw new FilesystemAccessException("no such directory entry");
			if (found instanceof Directory) {
				dir = (Directory) found;
			} else {
				dir = null;
			}
		}
		return found;
	}

	DirectoryEntry getDirEntry(TavernaRun run, List<PathSegment> d)
			throws FilesystemAccessException {
		Directory dir = run.getWorkingDirectory();
		DirectoryEntry found = dir;
		for (PathSegment segment : d)
			for (String bit : segment.getPath().split("/")) {
				found = null;
				if (dir == null)
					throw new FilesystemAccessException(
							"no such directory entry");
				for (DirectoryEntry entry : dir.getContents()) {
					if (entry.getName().equals(bit)) {
						found = entry;
						break;
					}
				}
				if (found == null)
					throw new FilesystemAccessException(
							"no such directory entry");
				if (found instanceof Directory) {
					dir = (Directory) found;
				} else {
					dir = null;
				}
			}
		return found;
	}

	Principal getPrincipal() {
		try {
			if (jaxwsContext != null)
				return jaxwsContext.getUserPrincipal();
			if (jaxrsContext != null)
				return jaxrsContext.getUserPrincipal();
		} catch (NullPointerException e) {
		}
		return null;
	}
}
