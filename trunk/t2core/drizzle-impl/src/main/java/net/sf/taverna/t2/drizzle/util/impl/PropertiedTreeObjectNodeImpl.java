/**
 * 
 */
package net.sf.taverna.t2.drizzle.util.impl;

import java.util.HashSet;
import java.util.Set;

import net.sf.taverna.t2.drizzle.util.PropertiedTreeObjectNode;

/**
 * @author alanrw
 *
 * @param <O> The class of Object represented by the tree node
 */
public final class PropertiedTreeObjectNodeImpl<O> extends PropertiedTreeNodeImpl<O>
	implements PropertiedTreeObjectNode<O> {
	
	private O object;

	/**
	 * 
	 */
	public PropertiedTreeObjectNodeImpl() {
		// Nothing to do
	}

	/**
	 * {@inheritDoc}
	 */
	public Set<O> getAllObjects() {
		Set<O> result = new HashSet<O> ();
		if (object != null) {
			result.add (object);
		}
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setObject(O object) {
		if (object == null) {
			throw new NullPointerException ("object cannot be null");
		}
		if (this.object != null) {
			throw new IllegalStateException("object cannot be initialized more than once");
		}
		this.object = object;
	}

	/**
	 * {@inheritDoc}
	 */
	public O getObject() {
		return this.object;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public String toString() {
		return this.object.toString();
	}
}
