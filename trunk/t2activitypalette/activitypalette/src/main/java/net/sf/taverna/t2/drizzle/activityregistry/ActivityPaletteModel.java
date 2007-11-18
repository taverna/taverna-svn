/**
 * 
 */
package net.sf.taverna.t2.drizzle.activityregistry;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import net.sf.taverna.raven.spi.RegistryListener;
import net.sf.taverna.raven.spi.SpiRegistry;

import org.apache.log4j.Logger;
import org.embl.ebi.escience.scuflui.workbench.Scavenger;
import org.embl.ebi.escience.scuflui.workbench.ScavengerHelperThreadPool;
import org.embl.ebi.escience.scuflui.workbench.URLBasedScavenger;
import org.embl.ebi.escience.scuflui.workbench.scavenger.spi.ScavengerRegistry;
import org.embl.ebi.escience.scuflworkers.ScavengerHelper;
import org.embl.ebi.escience.scuflworkers.ScavengerHelperRegistry;
import org.embl.ebi.escience.scuflworkers.web.WebScavengerHelper;

/**
 * @author alanrw
 * 
 */
// TODO consider if the ActivityRegistry really should be specific to the
// ActivityPaletteModel.
public final class ActivityPaletteModel {

	static Logger logger = Logger.getLogger(ActivityPaletteModel.class);

	private ActivityRegistry registry;

	private Set<ActivityTabModel> tabModels;
	
	private List<ActivityPaletteModelListener> listeners;

	/**
	 * A list of the names of all the scavengers contained within this tree
	 */
	ArrayList<String> scavengerList = null;

	public ActivityPaletteModel() {
		this.tabModels = new HashSet<ActivityTabModel>();
		this.scavengerList = new ArrayList<String> ();
		this.registry = new ActivityRegistry();
		this.listeners = new ArrayList<ActivityPaletteModelListener>();
	}

	public void initialize() {
		initializeRegistry();
	}
	
	public void addListener(final ActivityPaletteModelListener listener) {
		if (!this.listeners.contains(listener)) {
			this.listeners.add(listener);
		}
	}
	/**
	 * @param registry
	 *            the registry to set
	 */
	public synchronized void setRegistry(final ActivityRegistry registry) {
		if (registry == null) {
			throw new NullPointerException("registry cannot be null"); //$NON-NLS-1$
		}
		this.registry = registry;
	}
	
	private void addTabModel (final ActivityTabModel tabModel) {
		if (!this.tabModels.contains(tabModel)) {
			this.tabModels.add(tabModel);
			notifyListenersTabModelAdded(tabModel);
		}
	}

	private void notifyListenersTabModelAdded(final ActivityTabModel tabModel) {
		for (ActivityPaletteModelListener listener : this.listeners) {
			listener.tabModelAdded(this, tabModel);
		}
	}

	private void addImmediateQuery(ActivityQuery<?> query) {
		if (query == null) {
			throw new NullPointerException("query cannot be null"); //$NON-NLS-1$
		}
		ActivityQueryRunIdentification ident = this.registry
				.addImmediateQuery(query);
		ActivityTabModel tabModel = new ActivityTabModel();
		tabModel.setFilter(ident.getObjectFilter());
		tabModel.setName(ident.getName());
		addTabModel(tabModel);
	}

	public void removeTabModel(final ActivityTabModel tabModel) {
		if (tabModel == null) {
			throw new NullPointerException("tabModel cannot be null"); //$NON-NLS-1$
		}
		this.tabModels.remove(tabModel);
	}

	private void addScavenger(final Scavenger theScavenger) {
		synchronized (this.registry) {
			// Check to see we don't already have a scavenger with this name
			String newName = theScavenger.getUserObject().toString();
			if (!this.scavengerList.contains(newName)) {			
				this.scavengerList.add(theScavenger.getUserObject().toString());
				addImmediateQuery(new ActivityScavengerQuery(theScavenger));
			}
		}
	}
	
	/**
	 * Adapted from code in DefaultScavengerTree
	 * 
	 */
	private void initializeRegistry() {
		List<Scavenger> simpleScavengers = ScavengerRegistry.instance()
				.getScavengers();
		if (simpleScavengers.isEmpty() == false) {
			for (Scavenger scavenger : simpleScavengers) {
				if (!(scavenger instanceof URLBasedScavenger)) {
					addScavenger(scavenger);
				}
			}
		}
		new DefaultScavengerLoaderThread();
		ScavengerHelperRegistry.instance().addRegistryListener(
				new RegistryListener() {

					public void spiRegistryUpdated(SpiRegistry spiRegistry) {
						logger.info("Registry updated for class:" //$NON-NLS-1$
								+ spiRegistry.getClassName());
						new DefaultScavengerLoaderThread();
					}

				});
	}

	class DefaultScavengerLoaderThread extends Thread {

		public DefaultScavengerLoaderThread() {
			super("Default scavenger loader"); //$NON-NLS-1$
			start();
		}

		@Override
		public void run() {
			// scavengingStarting("Populating service list");

			addFromScavengerHelpers();

			// scavengingDone();
		}

	}
	
	protected void addFromScavengerHelpers() {
		DefaultTreeModel dummyTreeModel = new DefaultTreeModel(new DefaultMutableTreeNode());
		List<ScavengerHelper> helpers = ScavengerHelperRegistry.instance().getScavengerHelpers();
		ScavengerHelperThreadPool threadPool = new ScavengerHelperThreadPool();
		for (ScavengerHelper helper : helpers) {
//				web scavenger is a special case and requires ScavengerTree to construct the Scavengers
			if (helper instanceof WebScavengerHelper) 
			{
				for (Scavenger scavenger : ((WebScavengerHelper)helper).getDefaults(dummyTreeModel)) {
					addScavenger(scavenger);
				}
			}
			else {
				if (logger.isDebugEnabled()) logger.debug("Adding helper to thread pool...."+helper.getClass().getSimpleName()); //$NON-NLS-1$
				threadPool.addScavengerHelper(helper);
				
				//FIXME: this sleep sadly seems to be necessary to prevent linkage errors in Raven
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					logger.error(e);
				}
			}
		}
		
		while (!threadPool.isEmpty()) {
			if (logger.isDebugEnabled()) {
				logger.debug(threadPool.remaining() +" threads still waiting to complete."); //$NON-NLS-1$
				logger.debug(threadPool.waiting() +" threads still waiting in the queue."); //$NON-NLS-1$
			}
			Set<Scavenger> completed = threadPool.getCompleted();
			logger.debug(completed.size() +" completed scavenger threads found"); //$NON-NLS-1$
			for (Scavenger scavenger : completed) {
				addScavenger(scavenger);
			}
			try {
				if (!threadPool.isEmpty()) Thread.sleep(2500);
			} catch (InterruptedException e1) {
				logger.error("Interruption while waiting sleeping",e1); //$NON-NLS-1$
			}
		}
		logger.info("Scavenger thread pool completed"); //$NON-NLS-1$
	}


}
