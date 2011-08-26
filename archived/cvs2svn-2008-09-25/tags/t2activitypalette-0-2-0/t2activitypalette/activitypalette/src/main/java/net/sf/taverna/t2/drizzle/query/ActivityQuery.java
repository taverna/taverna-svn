/**
 * 
 */
package net.sf.taverna.t2.drizzle.query;

import net.sf.taverna.t2.drizzle.model.ActivitySubsetIdentification;
import net.sf.taverna.t2.drizzle.model.ProcessorFactoryAdapter;
import net.sf.taverna.t2.drizzle.util.PropertiedObjectSet;

/**
 * @author alanrw
 *
 */
/**
 * @author alanrw
 *
 * @param <ActivityQueryConfiguration>
 */
public interface ActivityQuery<ActivityQueryConfiguration> {
	
	/**
	 * @param configuration
	 */
	void configure (ActivityQueryConfiguration configuration);
	
	/**
	 * @param targetSet
	 * @return
	 */
	ActivitySubsetIdentification runQuery(PropertiedObjectSet<ProcessorFactoryAdapter> targetSet);
	
	/**
	 * @return
	 */
	ActivitySubsetIdentification lastRun();
}
