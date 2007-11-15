/**
 * 
 */
package net.sf.taverna.t2.drizzle.activityregistry;

import org.embl.ebi.escience.scuflworkers.ProcessorFactory;

import net.sf.taverna.t2.drizzle.util.PropertiedObjectFilter;

/**
 * @author alanrw
 *
 */
public interface ActivityQueryRunIdentification {
	String getName();
	PropertiedObjectFilter<ProcessorFactory> getObjectFilter();
	long getTimeOfRun();
}
