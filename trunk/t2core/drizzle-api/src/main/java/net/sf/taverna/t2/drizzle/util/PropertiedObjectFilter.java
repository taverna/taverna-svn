/**
 * 
 */
package net.sf.taverna.t2.drizzle.util;

/**
 * @author alanrw
 *
 */
public interface PropertiedObjectFilter<O> {
	boolean acceptObject (final O object);
}
