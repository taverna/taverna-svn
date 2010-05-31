package org.taverna.server.master.localworker;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.security.Principal;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.taverna.server.localworker.remote.RemoteSingleRun;
import org.taverna.server.master.SCUFL;
import org.taverna.server.master.exceptions.NoCreateException;
import org.taverna.server.master.exceptions.NoDestroyException;
import org.taverna.server.master.exceptions.NoListenerException;
import org.taverna.server.master.exceptions.NoUpdateException;
import org.taverna.server.master.exceptions.UnknownRunException;
import org.taverna.server.master.factories.ListenerFactory;
import org.taverna.server.master.factories.RunFactory;
import org.taverna.server.master.interfaces.Listener;
import org.taverna.server.master.interfaces.Policy;
import org.taverna.server.master.interfaces.RunStore;
import org.taverna.server.master.interfaces.TavernaRun;

@ManagedResource(objectName = "Taverna:group=Server,name=Factory", description = "The factory for runs")
public abstract class AbstractRemoteRunFactory implements ListenerFactory, RunFactory, Policy, RunStore {
	static final Log log = LogFactory.getLog("Taverna.Server.WorkerFactory");

	String name;
	TavernaRun current;
	private static Timer timer = new Timer(
			"Taverna.Server.LocalWorker.Factory.Timer", true);
	TimerTask task;

	static Registry registry;
	static {
		try {
			registry = LocateRegistry.getRegistry();
		} catch (RemoteException e) {
			log.error("failed to get RMI registry handle", e);
		}
		try {
			registry.list();
		} catch (RemoteException ignored) {
			try {
				registry = LocateRegistry.createRegistry(1099);
			} catch (RemoteException e) {
				log.error("failed to create RMI registry", e);
			}
		}
	}

	@Override
	public List<String> getSupportedListenerTypes() {
		try {
			return ((RemoteRunDelegate) current).run.getListenerTypes();
		} catch (RemoteException e) {
			log.warn("failed to get list of listener types", e);
			return Collections.emptyList();
		}
	}

	@Override
	public Listener makeListener(TavernaRun run, String listenerType,
			String configuration) throws NoListenerException {
		try {
			return new RemoteRunDelegate.ListenerDelegate(
					((RemoteRunDelegate) run).run.makeListener(listenerType,
							configuration));
		} catch (RemoteException e) {
			NoListenerException nl = new NoListenerException(e.getMessage());
			nl.initCause(e);
			throw nl;
		}
	}

	@Override
	public TavernaRun create(Principal creator, SCUFL workflow) {
		try {
			return new RemoteRunDelegate(creator, workflow, getRealRun(creator, workflow));
		} catch (Exception e) {
			log.warn("failed to build run instance", e);
			throw new RuntimeException(e);
		}
	}

	protected abstract RemoteSingleRun getRealRun(Principal creator, SCUFL workflow) throws Exception;

	@ManagedAttribute(description = "The name of the current run.", currencyTimeLimit = 10)
	public String getCurrentRunName() {
		return name == null ? "" : name;
	}

	@ManagedAttribute(description = "The maximum number of simultaneous runs supported by the server.", currencyTimeLimit = 300)
	@Override
	public int getMaxRuns() {
		return 1;
	}

	@Override
	public Integer getMaxRuns(Principal user) {
		return null;
	}

	@Override
	public List<SCUFL> listPermittedWorkflows(Principal user) {
		return Collections.emptyList();
	}

	@Override
	public boolean permitAccess(Principal user, TavernaRun run) {
		return true;
	}

	@Override
	public synchronized void permitCreate(Principal user, SCUFL workflow)
			throws NoCreateException {
		if (current != null)
			throw new NoCreateException();
	}

	@Override
	public synchronized void permitDestroy(Principal user, TavernaRun run)
			throws NoDestroyException {
		if (run != current)
			throw new NoDestroyException();
	}

	@Override
	public void permitUpdate(Principal user, TavernaRun run)
			throws NoUpdateException {
		// Does nothing; all may update
	}

	@Override
	public synchronized TavernaRun getRun(Principal user, Policy p, String uuid)
			throws UnknownRunException {
		if (name != null && name.equals(uuid))
			return current;
		throw new UnknownRunException();
	}

	@Override
	public synchronized Map<String, TavernaRun> listRuns(Principal user,
			Policy p) {
		return Collections.singletonMap(name, current);
	}

	@Override
	public synchronized void registerRun(final String uuid, TavernaRun run) {
		name = uuid;
		current = run;
		task = new TimerTask() {
			@Override
			public void run() {
				synchronized (AbstractRemoteRunFactory.this) {
					if (current == null)
						return;
					Date now = new Date();
					if (current.getExpiry().after(now))
						return;
					current.destroy();
					unregisterRun(uuid);
				}
			}
		};
		timer.scheduleAtFixedRate(task, 30000, 30000);
	}

	@Override
	public synchronized void unregisterRun(String uuid) {
		name = null;
		current = null;
		if (task != null)
			task.cancel();
		task = null;
	}

	protected synchronized void finalize() {
		if (task != null)
			task.cancel();
	}
}
