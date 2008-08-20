/**
 * 
 */
package net.sf.taverna.t2.drizzle.util;

/**
 * @author alanrw
 *
 */
public interface TwigConstructor<O> {
	
	PropertiedTreeNode<O> createTwig(final O object);

	boolean canHandle(Class<?> sourceClass);
}
