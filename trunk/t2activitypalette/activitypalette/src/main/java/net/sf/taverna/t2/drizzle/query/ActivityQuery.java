/**
 * 
 */
package net.sf.taverna.t2.drizzle.query;

import org.embl.ebi.escience.scuflworkers.ProcessorFactory;

import net.sf.taverna.t2.drizzle.model.ActivityRegistrySubsetIdentification;
import net.sf.taverna.t2.drizzle.util.PropertiedObjectSet;

/**
 * @author alanrw
 *
 */
public interface ActivityQuery<ActivityQueryConfiguration> {
	void configure (ActivityQueryConfiguration configuration);
	ActivityRegistrySubsetIdentification runQuery(PropertiedObjectSet<ProcessorFactory> targetSet);
	ActivityRegistrySubsetIdentification lastRun();
}
