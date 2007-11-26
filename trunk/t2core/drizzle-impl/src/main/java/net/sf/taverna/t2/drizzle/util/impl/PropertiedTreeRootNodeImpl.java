/**
 * 
 */
package net.sf.taverna.t2.drizzle.util.impl;

import net.sf.taverna.t2.drizzle.util.PropertiedTreeRootNode;

/**
 * @author alanrw
 * 
 * @param <O> The class of Object to which the leaf nodes correspond.
 *
 */
public final class PropertiedTreeRootNodeImpl<O> extends PropertiedTreeNodeImpl<O> implements
		PropertiedTreeRootNode<O> {

	/**
	 * 
	 */
	public PropertiedTreeRootNodeImpl() {
		// nothing to do
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return "root"; //$NON-NLS-1$
	}
}
