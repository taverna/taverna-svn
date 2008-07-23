/**
 * 
 */
package net.sf.taverna.t2.drizzle.util;


/**
 * @author alanrw
 *
 */
public final class TrueFilter<O> implements PropertiedObjectFilter<O> {
	
	public TrueFilter() {
	}

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.drizzle.util.PropertiedObjectFilter#acceptObject(java.lang.Object)
	 */
	public boolean acceptObject(O object) {
		return true;
	}

}
