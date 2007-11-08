package net.sf.taverna.t2.workflowmodel.impl;

import net.sf.taverna.t2.cloudone.datamanager.DataManager;

/**
 * Provides access to hierarchical context on a per-process name basis. Any
 * object within the workflow system may request or push contextual information
 * informing workflow invocation such as security credentials here.
 * <p>
 * The context manager also provides access to fundamental facilities, at the
 * moment this means the DataManager used to resolve references and store
 * collections and data objects but it will probably expand to other facilities
 * as and when we find them.
 * 
 * TODO - implement this properly, at the moment it's just returning a fixed
 * DataManager which has to be set manually before anything will work! Should be
 * discovering the data manager somehow or using different managers per workflow
 * instance etc etc.
 * 
 * @author Tom Oinn
 * 
 */
public class ContextManager {

	public static DataManager baseManager;

	/**
	 * The DataManager provides access to and registration of data reference
	 * objects.
	 * 
	 * @param owningProcess
	 * @return
	 */
	public static DataManager getDataManager(String owningProcess) {
		return baseManager;
	}

}
