/**
 * 
 */
package net.sf.taverna.t2.drizzle.model;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.DefaultListModel;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import net.sf.taverna.raven.spi.RegistryListener;
import net.sf.taverna.raven.spi.SpiRegistry;
import net.sf.taverna.t2.drizzle.bean.ActivityPaletteModelBean;
import net.sf.taverna.t2.drizzle.bean.SubsetKindConfigurationBean;
import net.sf.taverna.t2.drizzle.decoder.CommonKey;
import net.sf.taverna.t2.drizzle.query.ActivityQuery;
import net.sf.taverna.t2.drizzle.query.ActivityScavengerQuery;
import net.sf.taverna.t2.drizzle.util.FalseFilter;
import net.sf.taverna.t2.drizzle.util.ObjectMembershipFilter;
import net.sf.taverna.t2.drizzle.util.PropertyKey;
import net.sf.taverna.t2.drizzle.util.StringKey;
import net.sf.taverna.t2.drizzle.util.TrueFilter;
import net.sf.taverna.t2.drizzle.view.palette.ActivityPaletteModelToScavengerTreeAdapter;
import net.sf.taverna.t2.drizzle.view.palette.ActivityPalettePanel;
import net.sf.taverna.t2.drizzle.view.subset.ActivitySubsetPanel;
import net.sf.taverna.t2.util.beanable.Beanable;

import org.apache.log4j.Logger;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scuflui.workbench.Scavenger;
import org.embl.ebi.escience.scuflui.workbench.ScavengerCreationException;
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
// TODO consider if the ActivitySetModel really should be specific to the
// ActivityPaletteModel.
public final class ActivityPaletteModel implements Beanable<ActivityPaletteModelBean>{

	static Logger logger = Logger.getLogger(ActivityPaletteModel.class);

	ActivitySetModel activitySetModel;

	private Set<ActivitySubsetModel> subsetModels;
	
	private List<ActivityPaletteModelListener> listeners;
	
	/**
	 * A list of the names of all the scavengers contained within this tree
	 */
	ArrayList<String> scavengerList = null;

	private int scavengingInProgressCount = 0;
	
	private ActivityPaletteModelToScavengerTreeAdapter adapter = null;

	private static ActivitySubsetModel searchResultsSubsetModel = null;
	
	static ActivitySubsetModel allActivitiesSubsetModel = null;
	
	/**
	 * @return the adapter
	 */
	public synchronized final ActivityPaletteModelToScavengerTreeAdapter getAdapter() {
		return this.adapter;
	}
	
	/**
	 * @param representation
	 */
	public ActivityPaletteModel(final ActivityPalettePanel representation) {
		if (representation == null) {
			throw new NullPointerException("representation cannot be null"); //$NON-NLS-1$
		}
		this.subsetModels = new HashSet<ActivitySubsetModel>();
		this.scavengerList = new ArrayList<String>();
		this.activitySetModel = new ActivitySetModel();
		this.listeners = new ArrayList<ActivityPaletteModelListener>();
		this.adapter = new ActivityPaletteModelToScavengerTreeAdapter(this, representation);

	}

	/**
	 * 
	 */
	public void initialize() {
		initializeActivitySet();
		addAllSubsetModel();
		addSearchResultsSubsetModel();
	}
	
	/**
	 * @param listener
	 */
	public void addListener(final ActivityPaletteModelListener listener) {
		if (listener == null) {
			throw new NullPointerException("listener cannot be null"); //$NON-NLS-1$
		}
		if (!this.listeners.contains(listener)) {
			this.listeners.add(listener);
		}
	}

	/**
	 * @param activitySetModel
	 */
	public synchronized void setActivitySetModel(final ActivitySetModel activitySetModel) {
		if (activitySetModel == null) {
			throw new NullPointerException("activitySetModel cannot be null"); //$NON-NLS-1$
		}
		this.activitySetModel = activitySetModel;
	}
	
	void addSubsetModel(final ActivitySubsetModel subsetModel) {
		if (subsetModel == null) {
			throw new NullPointerException("subsetModel cannot be null"); //$NON-NLS-1$
		}
		if (!this.subsetModels.contains(subsetModel)) {
			this.subsetModels.add(subsetModel);
			notifyListenersSubsetModelAdded(subsetModel);
		}
	}

	private void notifyListenersSubsetModelAdded(final ActivitySubsetModel subsetModel) {
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

	/**
	 * @param query
	 */
	public void addImmediateQuery(final ActivityQuery<?> query) {
		if (query == null) {
			throw new NullPointerException("query cannot be null"); //$NON-NLS-1$
		}
		Runnable queryRunner = new Runnable() {

			public void run() {
				ActivitySubsetIdentification ident = ActivityPaletteModel.this.activitySetModel
				.addImmediateQuery(query);
		ActivitySubsetModel subsetModel = new ActivitySubsetModel();
		subsetModel.setIdent(ident);
		subsetModel.setParentActivitySetModel(ActivityPaletteModel.this.activitySetModel);
		subsetModel.setEditable(false);
		addSubsetModel(subsetModel);
		allActivitiesSubsetModel.setUpdated(true);
			}
			
		};
		SwingUtilities.invokeLater(queryRunner);
	}

	/**
	 * @param subsetModel
	 */
	public void removeSubsetModel(final ActivitySubsetModel subsetModel) {
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
			throw new NullPointerException("theScavenger cannot be null"); //$NON-NLS-1$
		}
		synchronized (this.activitySetModel) {
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
	private void initializeActivitySet() {
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
				logger.error("Interruption while waiting sleeping", e1); //$NON-NLS-1$
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
	
	/**
	 * @param theModel
	 */
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
				logger.error("Interruption while waiting sleeping", e1); //$NON-NLS-1$
			}
		}
		logger.info("Scavenger thread pool completed"); //$NON-NLS-1$
	}
	
	/**
	 * 
	 */
	public void scavengingDone() {
		this.scavengingInProgressCount--;
		if (this.scavengingInProgressCount==0) {
			notifyListenersScavengingDone();
		}
		
	}

	/**
	 * @param message
	 */
	public void scavengingStarting(String message) {
		if (this.scavengingInProgressCount==0) {
			notifyListenersScavengingStarted(message);
		}
		this.scavengingInProgressCount++;
	}

	public Set<ActivitySubsetModel> getSubsetModels() {
		return this.subsetModels;
	}
	
	/**
	 * @param model
	 * @throws ScavengerCreationException
	 */
	public void attachToModel(@SuppressWarnings("unused")
	final ScuflModel model) throws ScavengerCreationException {
		if (model == null) {
			throw new NullPointerException("model cannot be null"); //$NON-NLS-1$
		}
		this.adapter.addScavengersFromModel();
	}

	/**
	 * @param model
	 */
	public void detachFromModel(@SuppressWarnings("unused")
	final ScuflModel model) {
		if (model == null) {
			throw new NullPointerException("model cannot be null"); //$NON-NLS-1$
		}
		// nothing to do
	}
	
	/**
	 * @return
	 */
	public synchronized Set<String> getSubsetNames() {
		Set<String> names = new TreeSet<String>();
		for (ActivitySubsetModel subset : this.subsetModels) {
			names.add(subset.getName());
		}
		return (names);	
	}
	
	/**
	 * @return
	 */
	public synchronized Set<String> getSubsetKinds() {
		Set<String> kinds = new TreeSet<String>();
		for (ActivitySubsetModel subset : this.subsetModels) {
			kinds.add(subset.getIdent().getKind());
		}
		return (kinds);
	}
	
	private Set<PropertyKey> getAllKeys() {
		Set<PropertyKey> allKeys = new HashSet<PropertyKey>();
		allKeys.add(CommonKey.LocalServiceWorkerClassKey);
			allKeys.add(CommonKey.EndpointKey);
			allKeys.add(CommonKey.StringConstantValueKey);
			allKeys.add(CommonKey.WorkflowDefinitionURLKey);
			allKeys.add(CommonKey.ProcessorClassKey);
			allKeys.add(CommonKey.LocationKey);
			allKeys.add(CommonKey.WsdlOperationKey);
			allKeys.add(CommonKey.WsdlPortTypeKey);
			allKeys.add(CommonKey.NameKey);
			allKeys.add(CommonKey.MobyAuthorityKey);
			allKeys.add(CommonKey.CategoryKey);
			allKeys.add(CommonKey.BiomartMartKey);

			return allKeys;
	}

	/**
	 * @param subsetName
	 * @param subsetKind
	 * @param newKind
	 */
	public void addSubsetModelFromUser(String subsetName, String subsetKind, boolean newKind) {
		ActivitySubsetModel subsetModel = new ActivitySubsetModel();
		ActivitySubsetSelectionIdentification ident = new ActivitySubsetSelectionIdentification();
		ident.setObjectFilter(new ObjectMembershipFilter<ProcessorFactoryAdapter>(new HashSet<ProcessorFactoryAdapter>()));
		ident.setName(subsetName);
		if (newKind) {

				ident.setPropertyKeyProfile(getAllKeys());
		} else {
			//TODO something more sensible
			ident.setPropertyKeyProfile(getAllKeys());
		}
		ident.setKind(subsetKind);	
		subsetModel.setIdent(ident);
		subsetModel.setParentActivitySetModel(this.activitySetModel);
		subsetModel.setEditable(true);
		addSubsetModel(subsetModel);
	}
	
	private void addAllSubsetModel() {
		allActivitiesSubsetModel = new ActivitySubsetModel();
		ActivityQueryRunIdentification ident = new ActivityQueryRunIdentification();
		ident.setObjectFilter(new TrueFilter<ProcessorFactoryAdapter>());

		ident.setName("All activities"); //$NON-NLS-1$

		ident.setPropertyKeyProfile(getAllKeys());
		ident.setKind("allactivities");	 //$NON-NLS-1$
		allActivitiesSubsetModel.setIdent(ident);
		allActivitiesSubsetModel.setParentActivitySetModel(this.activitySetModel);
		allActivitiesSubsetModel.setEditable(false);
		addSubsetModel(allActivitiesSubsetModel);		
	}
	
	private void addSearchResultsSubsetModel() {
		searchResultsSubsetModel = new ActivitySubsetModel();
		ActivitySubsetSelectionIdentification ident = new ActivitySubsetSelectionIdentification();
		ident.setObjectFilter(new FalseFilter<ProcessorFactoryAdapter>());

		ident.setName("Search results"); //$NON-NLS-1$

		ident.setPropertyKeyProfile(getAllKeys());
		ident.setKind("allactivities");	 //$NON-NLS-1$
		searchResultsSubsetModel.setIdent(ident);
		searchResultsSubsetModel.setParentActivitySetModel(this.activitySetModel);
		searchResultsSubsetModel.setEditable(true);
		addSubsetModel(searchResultsSubsetModel);		
	}

	/**
	 * @return the searchResultsSubsetModel
	 */
	public synchronized static final ActivitySubsetModel getSearchResultsSubsetModel() {
		return searchResultsSubsetModel;
	}

	/**
	 * @return the activitySetModel
	 */
	public synchronized final ActivitySetModel getActivitySetModel() {
		return this.activitySetModel;
	}

	/**
	 * @see net.sf.taverna.t2.util.beanable.Beanable#getAsBean()
	 */
	public ActivityPaletteModelBean getAsBean() {
		ActivityPaletteModelBean result = new ActivityPaletteModelBean();
		List<SubsetKindConfigurationBean> configList = new ArrayList<SubsetKindConfigurationBean>();
		for (String kind : ActivitySubsetPanel.kindConfigurationMap.keySet()) {
			SubsetKindConfigurationBean configBean = new SubsetKindConfigurationBean();
			configBean.setKind(kind);
			SubsetKindConfiguration config = ActivitySubsetPanel.kindConfigurationMap.get(kind);
			List<String> tempList = new ArrayList<String>();
			for (PropertyKey pk : config.getKeyList()) {
				tempList.add(pk.toString());
			}
			configBean.setKeyList(tempList);
			
			DefaultListModel treeListModel = config.getTreeListModel();
			tempList = new ArrayList<String>();
			for (Enumeration<?> e = treeListModel.elements(); e.hasMoreElements();) {
				tempList.add(e.nextElement().toString());
			}
			configBean.setTreeKeyList(tempList);
			
			DefaultListModel treeTableListModel = config.getTreeTableListModel();
			tempList = new ArrayList<String>();
			for (Enumeration<?> e = treeTableListModel.elements(); e.hasMoreElements();) {
				tempList.add(e.nextElement().toString());
			}
			configBean.setTreeTableKeyList(tempList);

			DefaultListModel tableListModel = config.getTableListModel();
			tempList = new ArrayList<String>();
			for (Enumeration<?> e = tableListModel.elements(); e.hasMoreElements();) {
				tempList.add(e.nextElement().toString());
			}
			configBean.setTableKeyList(tempList);

			configList.add(configBean);
		}
		
		result.setSubsetKindConfigurationBeans(configList);
		return result;
	}

	/**
	 * @see net.sf.taverna.t2.util.beanable.Beanable#setFromBean(java.lang.Object)
	 */
	public void setFromBean(ActivityPaletteModelBean arg0) throws IllegalArgumentException {
		throw new UnsupportedOperationException();
	}
	
	/**
	 * @param arg0
	 */
	public void mergeWithBean(ActivityPaletteModelBean arg0){
		for (SubsetKindConfigurationBean kindConfigBean : arg0.getSubsetKindConfigurationBeans()) {
			SubsetKindConfiguration config = new SubsetKindConfiguration();
			ArrayList<PropertyKey> keyList = new ArrayList<PropertyKey>();
			if (kindConfigBean.getKeyList() != null) {
			for (String s : kindConfigBean.getKeyList()) {
				keyList.add(new StringKey(s));
			}
			}
			config.setKeyList(keyList);
			DefaultListModel treeListModel = new DefaultListModel();
			if (kindConfigBean.getTreeKeyList() != null) {
			for (String s : kindConfigBean.getTreeKeyList()) {
				treeListModel.addElement(new StringKey(s));
			}
			}
			config.setTreeListModel(treeListModel);
			
			DefaultListModel treeTableListModel = new DefaultListModel();
			if (kindConfigBean.getTreeTableKeyList() != null) {
			for (String s : kindConfigBean.getTreeTableKeyList()) {
				treeTableListModel.addElement(new StringKey(s));
			}
			}
			config.setTreeTableListModel(treeTableListModel);
			
			DefaultListModel tableListModel = new DefaultListModel();
			if (kindConfigBean.getTableKeyList() != null) {
			for (String s : kindConfigBean.getTableKeyList()) {
				tableListModel.addElement(new StringKey(s));
			}
			}
			config.setTableListModel(tableListModel);
			ActivitySubsetPanel.kindConfigurationMap.put(kindConfigBean.getKind(), config);
		}
	}
}
