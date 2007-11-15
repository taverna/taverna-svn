/**
 * 
 */
package net.sf.taverna.t2.drizzle.activityregistry;

import net.sf.taverna.t2.drizzle.util.PropertiedObjectFilter;

/**
 * @author alanrw
 *
 */
public final class ActivityTabModel {
	PropertiedObjectFilter filter = null;
	String name;

	/**
	 * @return the name
	 */
	public synchronized final String getName() {
		return name;
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
	public synchronized final PropertiedObjectFilter getFilter() {
		return filter;
	}

	/**
	 * @param filter the filter to set
	 */
	public synchronized final void setFilter(final PropertiedObjectFilter filter) {
		this.filter = filter;
	}


}
