/**
 * 
 */
package net.sf.taverna.t2.drizzle.util;

/**
 * @author alanrw
 *
 * @param <O> The class of object to which the filter can be applied
 */
public interface PropertiedObjectFilter<O> {
	/**
	 * Accept or reject an object as passing the filter.
	 * 
	 * @param object The object to be considered
	 * @return True is the object has passed, otherwise false.
	 */
	boolean acceptObject (final O object);
}
