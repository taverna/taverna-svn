/**
 * 
 */
package net.sf.taverna.t2.drizzle.util;

import java.util.Set;

/**
 * @author alanrw
 *
 */
public final class ObjectOrFilter<O> implements PropertiedObjectFilter<O> {
	
	private Set<PropertiedObjectFilter<O>> filters;
	
	public ObjectOrFilter(final Set<PropertiedObjectFilter<O>> filters) {
		this.filters = filters;
	}

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.drizzle.util.PropertiedObjectFilter#acceptObject(java.lang.Object)
	 */
	public boolean acceptObject(O object) {
		boolean result = false;
		for (PropertiedObjectFilter<O> filter : this.filters) {
			if (filter.acceptObject(object)) {
				result = true;
				break;
			}
		}
		return result;
	}

}
