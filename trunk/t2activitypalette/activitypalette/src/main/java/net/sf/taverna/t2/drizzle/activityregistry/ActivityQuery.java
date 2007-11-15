/**
 * 
 */
package net.sf.taverna.t2.drizzle.activityregistry;

import org.embl.ebi.escience.scuflworkers.ProcessorFactory;

import net.sf.taverna.t2.drizzle.util.PropertiedObjectSet;

/**
 * @author alanrw
 *
 */
public interface ActivityQuery<ActivityQueryConfiguration> {
	void configure (ActivityQueryConfiguration configuration);
	ActivityQueryRunIdentification runQuery(PropertiedObjectSet<ProcessorFactory> targetSet);
	ActivityQueryRunIdentification lastRun();
}
