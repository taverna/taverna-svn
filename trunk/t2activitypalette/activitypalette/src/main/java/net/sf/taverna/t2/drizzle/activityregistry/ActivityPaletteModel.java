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
import net.sf.taverna.t2.drizzle.util.ObjectMembershipFilter;
import net.sf.taverna.t2.drizzle.util.PropertyKey;

import org.apache.log4j.Logger;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scuflui.workbench.Scavenger;
import org.embl.ebi.escience.scuflui.workbench.ScavengerCreationException;
import org.embl.ebi.escience.scuflui.workbench.ScavengerHelperThreadPool;
import org.embl.ebi.escience.scuflui.workbench.URLBasedScavenger;
import org.embl.ebi.escience.scuflui.workbench.scavenger.spi.ScavengerRegistry;
import org.embl.ebi.escience.scuflworkers.ProcessorFactory;
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

	private Set<ActivityRegistrySubsetModel> subsetModels;
	
	private List<ActivityPaletteModelListener> listeners;
	
	/**
	 * A list of the names of all the scavengers contained within this tree
	 */
	ArrayList<String> scavengerList = null;

	private int scavengingInProgressCount = 0;
	
	private ActivityPaletteModelToScavengerTreeAdapter adapter = null;

	/**
	 * @return the adapter
	 */
	public synchronized final ActivityPaletteModelToScavengerTreeAdapter getAdapter() {
		return this.adapter;
	}
	
	/*
	 * representation should not be needed 
	 */
	public ActivityPaletteModel(final ActivityPalettePanel representation) {
		if (representation == null) {
			throw new NullPointerException ("representation cannot be null"); //$NON-NLS-1$
		}
		this.subsetModels = new HashSet<ActivityRegistrySubsetModel>();
		this.scavengerList = new ArrayList<String> ();
		this.registry = new ActivityRegistry();
		this.listeners = new ArrayList<ActivityPaletteModelListener>();
		this.adapter = new ActivityPaletteModelToScavengerTreeAdapter(this, representation);

	}

	public void initialize() {
		initializeRegistry();
	}
	
	public void addListener(final ActivityPaletteModelListener listener) {
		if (listener == null) {
			throw new NullPointerException ("listener cannot be null"); //$NON-NLS-1$
		}
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
	
	private void addSubsetModel (final ActivityRegistrySubsetModel subsetModel) {
		if (subsetModel == null) {
			throw new NullPointerException ("subsetModel cannot be null"); //$NON-NLS-1$
		}
		if (!this.subsetModels.contains(subsetModel)) {
			this.subsetModels.add(subsetModel);
			notifyListenersSubsetModelAdded(subsetModel);
		}
	}

	private void notifyListenersSubsetModelAdded(final ActivityRegistrySubsetModel subsetModel) {
		for (ActivityPaletteModelListener listener : this.listeners) {
			listener.subsetModelAdded(this, subsetModel);
		}
	}

	private void notifyListenersScavengingStarted(final String message) {
		for (ActivityPaletteModelListener listener : this.listeners) {
			listener.scavengingStarted(this, message);
		}
	}

	private void notifyListenersScavengingDone() {
		for (ActivityPaletteModelListener listener : this.listeners) {
			listener.scavengingDone(this);
		}
	}

	private void addImmediateQuery(ActivityQuery<?> query) {
		if (query == null) {
			throw new NullPointerException("query cannot be null"); //$NON-NLS-1$
		}
		ActivityQueryRunIdentification ident = this.registry
				.addImmediateQuery(query);
		ActivityRegistrySubsetModel subsetModel = new ActivityRegistrySubsetModel();
		subsetModel.setFilter(ident.getObjectFilter());
		subsetModel.setName(ident.getName());
		subsetModel.setPropertyKeyProfile(ident.getPropertyKeyProfile());
		subsetModel.setParentRegistry(this.registry);
		addSubsetModel(subsetModel);
	}

	public void removeSubsetModel(final ActivityRegistrySubsetModel subsetModel) {
		if (subsetModel == null) {
			throw new NullPointerException("subsetModel cannot be null"); //$NON-NLS-1$
		}
		this.subsetModels.remove(subsetModel);
	}

	/**
	 * In future this should not be needed.  It is only present to keep in concert with Taverna 1.
	 */
	public void addScavenger(final Scavenger theScavenger) {
		if (theScavenger == null) {
			throw new NullPointerException ("theScavenger cannot be null"); //$NON-NLS-1$
		}
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
			scavengingStarting("Populating service list"); //$NON-NLS-1$

			addFromScavengerHelpers();

			scavengingDone();
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

	class ScavengersFromModelThread extends Thread {
		
		private ScuflModel theModel;

		public ScavengersFromModelThread(final ScuflModel theModel) {
			super("Default scavenger loader"); //$NON-NLS-1$
			this.theModel = theModel;
			start();
		}

		@Override
		public void run() {
			scavengingStarting("Populating service list"); //$NON-NLS-1$

			addScavengersFromModel(this.theModel);

			scavengingDone();
		}

	}
	
	public void createScavengersFromModelThread(final ScuflModel theModel) {
		new ScavengersFromModelThread(theModel);
	}
	
	protected void addScavengersFromModel(final ScuflModel theModel) {
		if (theModel == null) {
			throw new NullPointerException ("theModel cannot be null"); //$NON-NLS-1$
		}
		List<ScavengerHelper> helpers = ScavengerHelperRegistry.instance().getScavengerHelpers();
		ScavengerHelperThreadPool threadPool = new ScavengerHelperThreadPool();
		for (ScavengerHelper helper : helpers) {

				if (logger.isDebugEnabled()) logger.debug("Adding helper to thread pool...."+helper.getClass().getSimpleName()); //$NON-NLS-1$
				threadPool.addScavengerHelperForModel(helper, theModel);
				
				//FIXME: this sleep sadly seems to be necessary to prevent linkage errors in Raven
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					logger.error(e);
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
	
	public void scavengingDone() {
		this.scavengingInProgressCount --;
		if (this.scavengingInProgressCount==0) {
			notifyListenersScavengingDone();
		}
		
	}

	public void scavengingStarting(String message) {
		if (this.scavengingInProgressCount==0) {
			notifyListenersScavengingStarted(message);
		}
		this.scavengingInProgressCount++;
	}

	public Set<ActivityRegistrySubsetModel> getSubsetModels() {
		return this.subsetModels;
	}
	
	public void addSubsetModelFromSelection (String name, Set<ProcessorFactory> selection, Set<PropertyKey> keyProfile) {
		if (name == null) {
			throw new NullPointerException("name cannot be null"); //$NON-NLS-1$
		}
		if (selection == null) {
			throw new NullPointerException("selection cannot be null"); //$NON-NLS-1$
		}
		if (keyProfile == null) {
			throw new NullPointerException("keyProfile cannot be null"); //$NON-NLS-1$
		}
		ActivityRegistrySubsetModel subsetModel = new ActivityRegistrySubsetModel();
		subsetModel.setFilter(new ObjectMembershipFilter<ProcessorFactory>(selection));
		subsetModel.setName(name);
		subsetModel.setPropertyKeyProfile(keyProfile);
		subsetModel.setParentRegistry(this.registry);
		addSubsetModel(subsetModel);
	}

	public void attachToModel(@SuppressWarnings("unused")
	final ScuflModel model) throws ScavengerCreationException {
		if (model == null) {
			throw new NullPointerException("model cannot be null"); //$NON-NLS-1$
		}
		this.adapter.addScavengersFromModel();
	}

	public void detachFromModel(@SuppressWarnings("unused")
	final ScuflModel model) {
		if (model == null) {
			throw new NullPointerException("model cannot be null"); //$NON-NLS-1$
		}
		// nothing to do
	}
}
