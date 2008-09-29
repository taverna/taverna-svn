/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.baclava.iterator;

import java.util.HashMap;
import java.util.Map;

import javax.swing.tree.DefaultMutableTreeNode;

/**
 * A DefaultMutableTreeNode implementing ResumableIterator by wrapping a
 * BaclavaIterator. Further, all output from the underlying ResumableIterator is
 * named and the output wrapped in a Map object
 * 
 * @author Tom Oinn
 */
public class BaclavaIteratorNode extends DefaultMutableTreeNode implements
		ResumableIterator {

	BaclavaIterator iterator;

	String name;

	/**
	 * A TreeNode that contains a BaclavaIterator. The iterator will produce Map
	 * objects with the single item having a value extracted from the underlying
	 * iterator and a key of the supplied name
	 */
	public BaclavaIteratorNode(BaclavaIterator i, String name) {
		this.iterator = i;
		this.name = name;
	}

	/**
	 * Return the name that will be used as the key for any maps that this
	 * iterator produces
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * Wraps the output of the underlying iterator's next() method into a Map
	 * with the key being the name of this BaclavaIteratorNode
	 */
	public Object next() {
		Map result = new HashMap();
		result.put(name, iterator.next());
		return result;
	}

	/**
	 * Delegate to the underlying iterator
	 */
	public boolean hasNext() {
		return iterator.hasNext();
	}

	/**
	 * Remove operation not supported
	 */
	public void remove() throws UnsupportedOperationException {
		iterator.remove();
	}

	/**
	 * Reset the underlying iterator to its initial state
	 */
	public void reset() {
		iterator.reset();
	}

	/**
	 * Return the size of the underlying iterator
	 */
	public int size() {
		return iterator.size();
	}

	/**
	 * These nodes are always leaf nodes, they act as the basic source of
	 * iterated objects
	 */
	public boolean isLeaf() {
		return true;
	}

	/**
	 * BaclavaIteratorNode objects are not allowed children
	 */
	public boolean getAllowsChildren() {
		return false;
	}

	/**
	 * Return the current position within this iterator
	 */
	public int[] getCurrentLocation() {
		return iterator.getCurrentLocation();
	}

}
