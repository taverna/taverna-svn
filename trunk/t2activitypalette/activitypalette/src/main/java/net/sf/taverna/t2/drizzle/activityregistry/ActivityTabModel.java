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
public final class ActivityTabModel {
	PropertiedObjectFilter<ProcessorFactory> filter = null;
	String name;

	/**
	 * @return the name
	 */
	public synchronized final String getName() {
		return this.name;
	}

	/**
	 * @param name the name to set
	 */
	public synchronized final void setName(final String name) {
		this.name = name;
	}

	/**
	 * @return the filter
	 */
	public synchronized final PropertiedObjectFilter<ProcessorFactory> getFilter() {
		return this.filter;
	}

	/**
	 * @param filter the filter to set
	 */
	public synchronized final void setFilter(final PropertiedObjectFilter<ProcessorFactory> filter) {
		this.filter = filter;
	}


}
