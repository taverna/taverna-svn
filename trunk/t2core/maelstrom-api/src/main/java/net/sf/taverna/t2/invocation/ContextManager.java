package net.sf.taverna.t2.invocation;

import net.sf.taverna.t2.cloudone.DataManager;

/**
 * Provides access to heirarchical context on a per-process name basis. Any
 * object within the workflow system may request or push contextual information
 * informing workflow invocation such as security credentials here.
 * <p>
 * The context manager also provides access to fundamental facilities, at the
 * moment this means the DataManager used to resolve references and store
 * collections and data objects but it will probably expand to other facilities
 * as and when we find them.
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
