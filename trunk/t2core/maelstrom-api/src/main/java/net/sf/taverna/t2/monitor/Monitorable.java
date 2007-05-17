package net.sf.taverna.t2.monitor;

import java.util.List;

/**
 * Implemented by entities that can be included in the monitor state tree
 * 
 * @author Tom Oinn
 * 
 */
public interface Monitorable {

	public List<MonitorableProperty> getProperties(String owningProcess);
	
}
