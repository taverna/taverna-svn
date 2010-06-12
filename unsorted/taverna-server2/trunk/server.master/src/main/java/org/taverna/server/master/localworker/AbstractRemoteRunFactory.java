package org.taverna.server.master.localworker;

import static java.lang.System.getSecurityManager;
import static java.lang.System.setProperty;
import static java.lang.System.setSecurityManager;
import static java.rmi.registry.LocateRegistry.createRegistry;
import static java.rmi.registry.LocateRegistry.getRegistry;
import static java.rmi.registry.Registry.REGISTRY_PORT;
import static java.util.Collections.emptyList;

import java.lang.ref.WeakReference;
import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.security.Principal;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
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
public abstract class AbstractRemoteRunFactory implements ListenerFactory,
		RunFactory, Policy, RunStore {
	static final Log log = LogFactory.getLog("Taverna.Server.LocalWorker");
	static Registry registry;
	public static final String SECURITY_POLICY_FILE = "security.policy";
	public static final int CLEANER_INTERVAL_MS = 30000;
	static Timer timer = new Timer("Taverna.Server.RemoteRunFactory.Timer",
			true);
	private int defaultLifetime = 20;
	private TimerTask cleaner;
	private Map<String, TavernaRun> runs = new HashMap<String, TavernaRun>();

	static {
		if (getSecurityManager() == null) {
			setProperty("java.security.policy", AbstractRemoteRunFactory.class
					.getClassLoader().getResource(SECURITY_POLICY_FILE)
					.toExternalForm());
			setSecurityManager(new RMISecurityManager());
		}
		try {
			registry = getRegistry();
		} catch (RemoteException e) {
			log.error("failed to get RMI registry handle", e);
		}
		try {
			registry.list();
		} catch (RemoteException ignored) {
			try {
				registry = createRegistry(REGISTRY_PORT);
			} catch (RemoteException e) {
				log.error("failed to create RMI registry", e);
			}
		}
	}

	public AbstractRemoteRunFactory() {
		cleaner = new AbstractRemoteRunFactoryCleaner(this);
		timer.scheduleAtFixedRate(cleaner, CLEANER_INTERVAL_MS,
				CLEANER_INTERVAL_MS);
	}

	@Override
	public List<String> getSupportedListenerTypes() {
		try {
			for (TavernaRun r : runs.values())
				return ((RemoteRunDelegate) r).run.getListenerTypes();
			log.warn("failed to get list of listener types; no runs");
		} catch (RemoteException e) {
			log.warn("failed to get list of listener types", e);
		}
		return emptyList();
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
			RemoteSingleRun rsr = getRealRun(creator, workflow);
			return new RemoteRunDelegate(creator, workflow, rsr,
					defaultLifetime);
		} catch (Exception e) {
			log.warn("failed to build run instance", e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * Gets the RMI connector for a new run.
	 * 
	 * @param creator
	 *            Who is creating the workflow run.
	 * @param workflow
	 *            What workflow are they instantiating.
	 * @return The remote interface to the run.
	 * @throws Exception
	 *             Just about anything can go wrong...
	 */
	protected abstract RemoteSingleRun getRealRun(Principal creator,
			SCUFL workflow) throws Exception;

	@ManagedAttribute(description = "The names of the current runs.", currencyTimeLimit = 5)
	public String[] getCurrentRunNames() {
		return runs.keySet().toArray(new String[0]);
	}

	@ManagedAttribute(description = "The maximum number of simultaneous runs supported by the server.", currencyTimeLimit = 300)
	@Override
	public int getMaxRuns() {
		return 5;
	}

	@ManagedAttribute(description = "How many minutes should a workflow live by default?", currencyTimeLimit = 300)
	public int getDefaultLifetime() {
		return defaultLifetime;
	}

	@ManagedAttribute
	public void setDefaultLifetime(int defaultLifetime) {
		this.defaultLifetime = defaultLifetime;
	}

	@Override
	public Integer getMaxRuns(Principal user) {
		return null;
	}

	@Override
	public List<SCUFL> listPermittedWorkflows(Principal user) {
		return emptyList();
	}

	@Override
	public boolean permitAccess(Principal user, TavernaRun run) {
		return true;
	}

	@Override
	public synchronized void permitCreate(Principal user, SCUFL workflow)
			throws NoCreateException {
		if (runs.size() >= getMaxRuns())
			throw new NoCreateException();
	}

	@Override
	public synchronized void permitDestroy(Principal user, TavernaRun run)
			throws NoDestroyException {
		// Simple model: if you can update, you can destroy
		try {
			permitUpdate(user, run);
		} catch (NoUpdateException e) {
			throw new NoDestroyException();
		}
	}

	@Override
	public void permitUpdate(Principal user, TavernaRun run)
			throws NoUpdateException {
		// Does nothing; all may update
	}

	@Override
	public synchronized TavernaRun getRun(Principal user, Policy p, String uuid)
			throws UnknownRunException {
		TavernaRun run = runs.get(uuid);
		if (run != null)
			return run;
		throw new UnknownRunException();
	}

	@Override
	public synchronized Map<String, TavernaRun> listRuns(Principal user,
			Policy p) {
		Map<String, TavernaRun> result = new HashMap<String, TavernaRun>();
		for (Map.Entry<String, TavernaRun> namedRun : runs.entrySet()) {
			result.put(namedRun.getKey(), namedRun.getValue());
		}
		return result;
	}

	@Override
	public synchronized void registerRun(final String uuid, TavernaRun run) {
		runs.put(uuid, run);
	}

	@Override
	public synchronized void unregisterRun(String uuid) {
		runs.remove(uuid);
	}

	protected synchronized void finalize() {
		cleaner.cancel();
		for (TavernaRun run : runs.values()) {
			run.destroy();
		}
	}

	Iterator<TavernaRun> iterator() {
		return runs.values().iterator();
	}
}

/**
 * Class that handles cleanup of tasks when their expiry is past.
 * 
 * @author Donal Fellows
 */
class AbstractRemoteRunFactoryCleaner extends TimerTask {
	private WeakReference<AbstractRemoteRunFactory> arrf;

	AbstractRemoteRunFactoryCleaner(AbstractRemoteRunFactory arrf) {
		this.arrf = new WeakReference<AbstractRemoteRunFactory>(arrf);
	}

	@Override
	public void run() {
		// Reconvert back to a strong reference for the length of this check
		AbstractRemoteRunFactory f = arrf.get();
		if (f == null) {
			cancel();
			return;
		}
		// Check to see if anything is needing cleaning; if not, we're done
		Date now = new Date();
		synchronized (f) {
			Iterator<TavernaRun> it = f.iterator();
			while (it.hasNext()) {
				TavernaRun run = it.next();
				if (run == null)
					continue;
				if (run.getExpiry().after(now))
					continue;
				it.remove();
				run.destroy();
			}
		}
	}
}
