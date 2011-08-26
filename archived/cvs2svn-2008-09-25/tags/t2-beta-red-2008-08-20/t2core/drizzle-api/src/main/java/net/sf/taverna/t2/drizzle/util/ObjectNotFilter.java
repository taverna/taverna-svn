/**
 * 
 */
package net.sf.taverna.t2.drizzle.util;


/**
 * @author alanrw
 *
 */
public final class ObjectNotFilter<O> implements PropertiedObjectFilter<O> {
	
	private PropertiedObjectFilter<O> filter;
	
	public ObjectNotFilter(final PropertiedObjectFilter<O> filter) {
		this.filter = filter;
	}

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.drizzle.util.PropertiedObjectFilter#acceptObject(java.lang.Object)
	 */
	public boolean acceptObject(O object) {
		return !(this.filter.acceptObject(object));
	}

}
