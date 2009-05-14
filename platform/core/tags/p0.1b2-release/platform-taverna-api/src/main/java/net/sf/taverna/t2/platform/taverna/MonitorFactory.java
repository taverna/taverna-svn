package net.sf.taverna.t2.platform.taverna;

import net.sf.taverna.t2.monitor.Monitor;

/**
 * Used to construct instances of Monitor, these can be added to an
 * InvocationContext and used to monitor a workflow enactment using that context
 * 
 * @author Tom Oinn
 */
public interface MonitorFactory {

	/**
	 * Create and return a new Monitor instance, this can be injected into
	 * multiple invocation contexts if federation of multiple workflow
	 * invocations is required in a single monitoring client
	 * 
	 * @return a newly created Monitor instance
	 */
	public Monitor createMonitor();

}
