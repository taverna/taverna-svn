/**
 * 
 */
package net.sf.taverna.t2.drizzle.query;

import net.sf.taverna.t2.drizzle.model.ActivityRegistrySubsetIdentification;
import net.sf.taverna.t2.drizzle.model.ProcessorFactoryAdapter;
import net.sf.taverna.t2.drizzle.util.PropertiedObjectSet;

/**
 * @author alanrw
 *
 */
public interface ActivityQuery<ActivityQueryConfiguration> {
	void configure (ActivityQueryConfiguration configuration);
	ActivityRegistrySubsetIdentification runQuery(PropertiedObjectSet<ProcessorFactoryAdapter> targetSet);
	ActivityRegistrySubsetIdentification lastRun();
}
