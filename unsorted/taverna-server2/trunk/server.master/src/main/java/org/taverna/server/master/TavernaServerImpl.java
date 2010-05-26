package org.taverna.server.master;

import static java.lang.Math.min;
import static java.util.Arrays.asList;
import static java.util.UUID.randomUUID;
import static javax.ws.rs.core.Response.created;
import static javax.ws.rs.core.Response.seeOther;
import static javax.ws.rs.core.Response.temporaryRedirect;
import static org.taverna.server.master.DirEntryReference.makeDirEntryReference;

import java.security.Principal;
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
import javax.xml.ws.WebServiceContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.taverna.server.master.exceptions.FilesystemAccessException;
import org.taverna.server.master.exceptions.NoListenerException;
import org.taverna.server.master.exceptions.NoUpdateException;
import org.taverna.server.master.exceptions.UnknownRunException;
import org.taverna.server.master.factories.ListenerFactory;
import org.taverna.server.master.factories.RunFactory;
import org.taverna.server.master.interfaces.Directory;
import org.taverna.server.master.interfaces.DirectoryEntry;
import org.taverna.server.master.interfaces.File;
import org.taverna.server.master.interfaces.Listener;
import org.taverna.server.master.interfaces.Policy;
import org.taverna.server.master.interfaces.RunStore;
import org.taverna.server.master.interfaces.TavernaRun;

@Path("/rest")
@WebService(endpointInterface = "org.taverna.server.master.TavernaServerSOAP", serviceName = "TavernaServer")
public class TavernaServerImpl implements TavernaServerSOAP, TavernaServerREST {
	static Log log = LogFactory.getLog(TavernaServerImpl.class);

	@Resource
	private WebServiceContext jaxwsContext;
	@Resource
	private SecurityContext jaxrsContext;

	/**
	 * Encapsulates the policies applied by this server.
	 */
	Policy policy;
	/**
	 * A factory for workflow runs.
	 */
	RunFactory runFactory;
	/**
	 * A storage facility for workflow runs.
	 */
	RunStore runStore;
	/**
	 * A factory for event listeners to attach to workflow runs.
	 */
	ListenerFactory listenerFactory;

	public void setPolicy(Policy policy) {
		this.policy = policy;
	}

	public void setListenerFactory(ListenerFactory listenerFactory) {
		this.listenerFactory = listenerFactory;
	}

	public void setRunFactory(RunFactory runFactory) {
		this.runFactory = runFactory;
	}

	public void setRunStore(RunStore runStore) {
		this.runStore = runStore;
	}

	public ServerDescription describeService(UriInfo ui) {
		return new ServerDescription(runStore.listRuns(getPrincipal(), policy),
				ui);
	}

	public RunReference[] listRuns() {
		Principal p = getPrincipal();
		ArrayList<RunReference> ws = new ArrayList<RunReference>();
		UriBuilder ub = UriBuilder.fromUri("/tavernaserver/runs");
		for (String runName : runStore.listRuns(p, policy).keySet())
			ws.add(new RunReference(runName, ub));
		return ws.toArray(new RunReference[ws.size()]);
	}

	public Response submitWorkflow(SCUFL workflow, UriInfo ui)
			throws NoUpdateException {
		Principal p = getPrincipal();
		policy.permitCreate(p, workflow);
		TavernaRun w = runFactory.create(p, workflow);
		String uuid = randomUUID().toString();
		runStore.registerRun(uuid, w);
		return seeOther(ui.getRequestUriBuilder().path("{uuid}").build(uuid))
				.build();
	}

	public RunReference submitWorkflow(SCUFL workflow) throws NoUpdateException {
		Principal p = getPrincipal();
		policy.permitCreate(p, workflow);
		UriBuilder ub = UriBuilder.fromUri("/tavernaserver/runs");
		TavernaRun w = runFactory.create(p, workflow);
		String uuid = randomUUID().toString();
		runStore.registerRun(uuid, w);
		return new RunReference(uuid, ub);
	}

	public int getMaxSimultaneousRuns() {
		Integer limit = policy.getMaxRuns(getPrincipal());
		if (limit == null)
			return policy.getMaxRuns();
		return min(limit.intValue(), policy.getMaxRuns());
	}

	public List<SCUFL> getPermittedWorkflows() {
		return policy.listPermittedWorkflows(getPrincipal());
	}

	public SCUFL[] getAllowedWorkflows() {
		return policy.listPermittedWorkflows(getPrincipal()).toArray(
				new SCUFL[0]);
	}

	public List<String> getPermittedListeners() {
		return listenerFactory.getSupportedListenerTypes();
	}

	public String[] getAllowedListeners() {
		return listenerFactory.getSupportedListenerTypes().toArray(
				new String[0]);
	}

	@Override
	public TavernaServerRunREST getRunResource(final String runName)
			throws UnknownRunException {
		final TavernaRun w = getRun(runName);
		return new TavernaServerRunREST() {
			@Override
			public RunDescription getDescription(UriInfo ui) {
				return new RunDescription(w, ui);
			}

			@Override
			public Response destroy(UriInfo ui) throws NoUpdateException {
				policy.permitDestroy(getPrincipal(), w);
				runStore.unregisterRun(runName);
				w.destroy();
				return temporaryRedirect(
						ui.getBaseUriBuilder().path("runs").build()).build();
			}

			@Override
			public TavernaServerListenersREST getListeners() {
				return new TavernaServerListenersREST() {
					@Override
					public Response addListener(
							ListenerCreationDescription typeAndConfiguration,
							UriInfo ui) throws NoUpdateException,
							NoListenerException {
						policy.permitUpdate(getPrincipal(), w);
						String name = listenerFactory.makeListener(w,
								typeAndConfiguration.type,
								typeAndConfiguration.configuration).getName();
						return seeOther(
								ui.getAbsolutePathBuilder().path(
										"{listenerName}").build(name)).build();
					}

					@Override
					public TavernaServerListenerREST getListener(String name)
							throws NoListenerException {
						for (Listener listener : w.getListeners()) {
							final Listener l = listener;
							if (listener.getName().equals(name))
								return new TavernaServerListenerREST() {
									@Override
									public String getConfiguration() {
										return l.getConfiguration();
									}

									@Override
									public ListenerDescription getDescription(
											UriInfo ui) {
										return new ListenerDescription(l
												.getName(), l.getType(), l
												.listProperties(), ui);
									}

									@Override
									public List<String> getProperties() {
										return asList(l.listProperties());
									}

									@Override
									public Property getProperty(
											final String propertyName)
											throws NoListenerException {
										if (getProperties().contains(
												propertyName))
											return new Property() {
												@Override
												public String getValue() {
													try {
														return l
																.getProperty(propertyName);
													} catch (NoListenerException e) {
														log
																.error(
																		"unexpected exception; property \""
																				+ propertyName
																				+ "\" should exist",
																		e);
														return null;
													}
												}

												@Override
												public Response setValue(
														String value, UriInfo ui)
														throws NoUpdateException,
														NoListenerException {
													policy.permitUpdate(
															getPrincipal(), w);
													l.setProperty(propertyName,
															value);
													return seeOther(
															ui.getRequestUri())
															.build();
												}
											};

										throw new NoListenerException(
												"no such property");
									}
								};
						}
						throw new NoListenerException();
					}

					@Override
					public List<ListenerDescription> getDescription(UriInfo ui) {
						List<ListenerDescription> result = new ArrayList<ListenerDescription>();
						for (Listener l : w.getListeners()) {
							result.add(new ListenerDescription(l.getName(), l
									.getType(), l.listProperties(), ui));
						}
						return result;
					}
				};
			}

			@Override
			public String getOwner() {
				return w.getSecurityContext().getOwner().getName();
			}

			@Override
			public Date getExpiry() {
				return w.getExpiry();
			}

			@Override
			public Status getStatus() {
				return w.getStatus();
			}

			@Override
			public SCUFL getWorkflow() {
				return w.getWorkflow();
			}

			@Override
			public DirectoryREST getWorkingDirectory() {
				return new DirectoryREST(w);
			}

			@Override
			public Response setExpiry(Date expiry, UriInfo ui)
					throws NoUpdateException {
				policy.permitDestroy(getPrincipal(), w);
				w.setExpiry(expiry);
				return seeOther(ui.getRequestUri()).build();
			}

			@Override
			public Response setStatus(Status status, UriInfo ui)
					throws NoUpdateException {
				policy.permitUpdate(getPrincipal(), w);
				w.setStatus(status);
				return seeOther(ui.getRequestUri()).build();
			}
		};
	}

	class DirectoryREST implements TavernaServerDirectoryREST {
		private TavernaRun w;

		DirectoryREST(TavernaRun w) {
			this.w = w;
		}

		@Override
		public Response destroyDirectoryEntry(List<PathSegment> path, UriInfo ui)
				throws NoUpdateException, FilesystemAccessException {
			policy.permitUpdate(getPrincipal(), w);
			DirectoryEntry entry = getDirEntry(w, path);
			entry.destroy();
			return temporaryRedirect(
					ui.getAbsolutePathBuilder().path("..").build()).build();
		}

		@Override
		public DirEntryReference getDescription(UriInfo ui) {
			return makeDirEntryReference(ui.getAbsolutePathBuilder(), w
					.getWorkingDirectory());
		}

		@Override
		public Response getDirectoryOrFileContents(List<PathSegment> path,
				UriInfo ui) throws FilesystemAccessException {
			DirectoryEntry de = getDirEntry(w, path);
			if (de instanceof File) {
				return Response.ok(((File) de).getContents()).build();
			} else if (de instanceof Directory) {
				List<DirEntryReference> result = new ArrayList<DirEntryReference>();
				for (DirectoryEntry e : ((Directory) de).getContents())
					result.add(makeDirEntryReference(ui
							.getAbsolutePathBuilder(), e));
				return Response.ok(result).build();
			} else {
				throw new FilesystemAccessException("not a directory");
			}
		}

		@Override
		public Response makeDirectory(List<PathSegment> parent, String name,
				UriInfo ui) throws NoUpdateException, FilesystemAccessException {
			policy.permitUpdate(getPrincipal(), w);
			DirectoryEntry container = getDirEntry(w, parent);
			if (!(container instanceof Directory))
				throw new FilesystemAccessException(
						"You may not make a subdirectory of a file.");
			Directory dir = ((Directory) container).makeSubdirectory(
					getPrincipal(), name);
			return created(
					ui.getAbsolutePathBuilder().path("{name}").build(
							dir.getName())).build();
		}

		@Override
		public Response makeOrUpdateFile(List<PathSegment> parent, String name,
				byte[] contents, UriInfo ui) throws NoUpdateException,
				FilesystemAccessException {
			policy.permitUpdate(getPrincipal(), w);
			DirectoryEntry container = getDirEntry(w, parent);
			if (!(container instanceof Directory))
				throw new FilesystemAccessException(
						"You may not place a file in a file.");
			File f = null;
			for (DirectoryEntry e : ((Directory) container).getContents()) {
				if (e.getName().equals(name)) {
					if (e instanceof File) {
						f = (File) e;
						break;
					}
					throw new FilesystemAccessException(
							"You may not overwrite a directory with a file.");
				}
			}
			if (f == null) {
				f = ((Directory) container).makeEmptyFile(getPrincipal(), name);
				f.setContents(contents);
				return created(
						ui.getAbsolutePathBuilder().path("{name}").build(
								f.getName())).build();
			} else {
				f.setContents(contents);
				return Response.ok().build();
			}
		}
	}

	public void destroyRun(String uuid) throws UnknownRunException,
			NoUpdateException {
		TavernaRun w = getRun(uuid);
		policy.permitUpdate(getPrincipal(), w);
		runStore.unregisterRun(uuid);
		w.destroy();
	}

	public SCUFL getRunWorkflow(String uuid) throws UnknownRunException {
		return getRun(uuid).getWorkflow();
	}

	public Date getRunExpiry(String uuid) throws UnknownRunException {
		return getRun(uuid).getExpiry();
	}

	public void setRunExpiry(String uuid, Date d) throws UnknownRunException,
			NoUpdateException {
		TavernaRun w = getRun(uuid);
		policy.permitDestroy(getPrincipal(), w);
		w.setExpiry(d);
	}

	public Status getRunStatus(String uuid) throws UnknownRunException {
		return getRun(uuid).getStatus();
	}

	public void setRunStatus(String uuid, Status s) throws UnknownRunException,
			NoUpdateException {
		TavernaRun w = getRun(uuid);
		policy.permitUpdate(getPrincipal(), w);
		w.setStatus(s);
	}

	public String getRunOwner(String uuid) throws UnknownRunException {
		return getRun(uuid).getSecurityContext().getOwner().getName();
	}

	// -=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-
	// Operations on the filesystem

	public DirEntryReference[] getRunDirectoryContents(String uuid,
			DirEntryReference d) throws UnknownRunException,
			FilesystemAccessException {
		TavernaRun w = getRun(uuid);
		DirectoryEntry de = getDirEntry(w, d);
		if (!(de instanceof Directory))
			throw new FilesystemAccessException("not a directory");

		List<DirEntryReference> result = new ArrayList<DirEntryReference>();
		for (DirectoryEntry e : ((Directory) de).getContents())
			result.add(makeDirEntryReference(null, e));
		return result.toArray(new DirEntryReference[result.size()]);
	}

	public DirEntryReference makeRunDirectory(String uuid,
			DirEntryReference parent, String name) throws UnknownRunException,
			NoUpdateException, FilesystemAccessException {
		TavernaRun w = getRun(uuid);
		policy.permitUpdate(getPrincipal(), w);
		DirectoryEntry container = getDirEntry(w, parent);
		if (!(container instanceof Directory))
			throw new FilesystemAccessException("not inside a directory");
		Directory dir = ((Directory) container).makeSubdirectory(
				getPrincipal(), name);
		return makeDirEntryReference(null, dir);
	}

	public DirEntryReference makeRunFile(String uuid, DirEntryReference parent,
			String name) throws UnknownRunException, NoUpdateException,
			FilesystemAccessException {
		TavernaRun w = getRun(uuid);
		policy.permitUpdate(getPrincipal(), w);
		DirectoryEntry container = getDirEntry(w, parent);
		if (!(container instanceof Directory))
			throw new FilesystemAccessException("not inside a directory");
		File f = ((Directory) container).makeEmptyFile(getPrincipal(), name);
		return makeDirEntryReference(null, f);
	}

	public void destroyRunDirectoryEntry(String uuid, DirEntryReference d)
			throws UnknownRunException, NoUpdateException,
			FilesystemAccessException {
		TavernaRun w = getRun(uuid);
		policy.permitUpdate(getPrincipal(), w);
		getDirEntry(w, d).destroy();
	}

	public byte[] getRunFileContents(String uuid, DirEntryReference d)
			throws UnknownRunException, FilesystemAccessException {
		DirectoryEntry de = getDirEntry(getRun(uuid), d);
		if (!(de instanceof File))
			throw new FilesystemAccessException("not a file");
		return ((File) de).getContents();
	}

	public void setRunFileContents(String uuid, DirEntryReference d,
			byte[] newContents) throws UnknownRunException, NoUpdateException,
			FilesystemAccessException {
		TavernaRun w = getRun(uuid);
		policy.permitUpdate(getPrincipal(), w);
		DirectoryEntry de = getDirEntry(w, d);
		if (!(de instanceof File))
			throw new FilesystemAccessException("not a file");
		((File) de).setContents(newContents);
	}

	public long getRunFileLength(String uuid, DirEntryReference d)
			throws UnknownRunException, FilesystemAccessException {
		DirectoryEntry de = getDirEntry(getRun(uuid), d);
		if (!(de instanceof File))
			throw new FilesystemAccessException("not a file");
		return ((File) de).getSize();
	}

	// -=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-
	// Operations on listeners

	public String[] getRunListeners(String uuid) throws UnknownRunException {
		TavernaRun w = getRun(uuid);
		List<String> result = new ArrayList<String>();
		for (Listener l : w.getListeners()) {
			result.add(l.getName());
		}
		return result.toArray(new String[result.size()]);
	}

	public String addRunListener(String uuid, String listenerType,
			String configuration) throws UnknownRunException,
			NoUpdateException, NoListenerException {
		TavernaRun w = getRun(uuid);
		policy.permitUpdate(getPrincipal(), w);
		return listenerFactory.makeListener(w, listenerType, configuration)
				.getName();
	}

	public String getRunListenerConfiguration(String uuid, String listenerName)
			throws UnknownRunException, NoListenerException {
		for (Listener l : getRun(uuid).getListeners()) {
			if (l.getName().equals(listenerName)) {
				return l.getConfiguration();
			}
		}
		throw new NoListenerException();
	}

	public String[] getRunListenerProperties(String uuid, String listenerName)
			throws UnknownRunException, NoListenerException {
		for (Listener l : getRun(uuid).getListeners()) {
			if (l.getName().equals(listenerName)) {
				return l.listProperties().clone();
			}
		}
		throw new NoListenerException();
	}

	public String getRunListenerProperty(String uuid, String listenerName,
			String propName) throws UnknownRunException, NoListenerException {
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

	public void setRunListenerProperty(String uuid, String listenerName,
			String propName, String value) throws UnknownRunException,
			NoUpdateException, NoListenerException {
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
