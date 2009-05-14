package net.sf.taverna.t2.platform.taverna.impl;

import net.sf.taverna.t2.monitor.Monitor;
import net.sf.taverna.t2.monitor.impl.MonitorImpl;
import net.sf.taverna.t2.platform.taverna.MonitorFactory;

/**
 * Implementation of MonitorFactory that returns a new MonitorImpl from the
 * workflowmodel-impl module
 * 
 * @author Tom Oinn
 */
public class MonitorFactoryImpl implements MonitorFactory {

	public Monitor createMonitor() {
		return new MonitorImpl();
	}

}
