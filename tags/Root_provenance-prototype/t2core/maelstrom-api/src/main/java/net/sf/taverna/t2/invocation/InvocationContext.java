package net.sf.taverna.t2.invocation;

import net.sf.taverna.t2.cloudone.datamanager.DataManager;

/**
 * Carries the context of a workflow invocation, the necessary data manager,
 * security agents and any other resource shared across the invocation such as
 * provenance injectors.
 * 
 * @author Tom Oinn
 * 
 */
public interface InvocationContext {

	public DataManager getDataManager();
	
}
