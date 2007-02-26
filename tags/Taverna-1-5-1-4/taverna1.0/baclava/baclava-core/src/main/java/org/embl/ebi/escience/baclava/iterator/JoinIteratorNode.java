/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.baclava.iterator;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;

/**
 * A DefaultMutableTreeNode implementing ResumableIterator which wraps a
 * JoinIterator.
 * 
 * @author Tom Oinn
 */
public class JoinIteratorNode extends DefaultMutableTreeNode implements
		ResumableIterator {

	JoinIterator iterator;

	/**
	 * Create a new JoinIteratorNode, calls to insert(...) will add iterators to
	 * the join.
	 */
	public JoinIteratorNode() {
		iterator = new JoinIterator() {
			// Gather a list of all children in the
			// enclosing class
			ResumableIterator[] iterators() {
				ResumableIterator[] result = new ResumableIterator[JoinIteratorNode.this
						.getChildCount()];
				for (int i = 0; i < JoinIteratorNode.this.getChildCount(); i++) {
					result[i] = (ResumableIterator) JoinIteratorNode.this
							.getChildAt(i);
				}
				return result;
			}

			// Check for validity of the iterator
			boolean emptyIterator() {
				for (int i = 0; i < JoinIteratorNode.this.getChildCount(); i++) {
					if (((ResumableIterator) JoinIteratorNode.this
							.getChildAt(i)).size() == 0) {
						return true;
					}
				}
				return false;
			}
		};
	}

	/**
	 * Return a Map of named objects derived from any iterator nodes attached to
	 * this one as children. The iteration strategy is derived from the
	 * JoinIterator which performs a cross product or orthoganol join across all
	 * the sub-iterators
	 */
	public Object next() {
		// Result will be an array of Map objects, convert to a Map i.e.
		// convert [{foo->bar}],[{urgle->wibble}] to {foo->bar,urgle->wibble}
		// and return
		Object[] o = (Object[]) iterator.next();
		Map[] map = new Map[o.length];
		for (int i = 0; i < o.length; i++) {
			map[i] = (Map) o[i];
		}

		Map result = new HashMap();
		for (int j = 0; j < map.length; j++) {
			for (Iterator i = map[j].keySet().iterator(); i.hasNext();) {
				String name = (String) i.next();
				Object value = map[j].get(name);
				result.put(name, value);
			}
		}
		return result;
	}

	/**
	 * Return true if calls to next() will succeed
	 */
	public boolean hasNext() {
		return iterator.hasNext();
	}

	/**
	 * Return the current location
	 */
	public int[] getCurrentLocation() {
		return iterator.getCurrentLocation();
	}

	/**
	 * Reset the underlying JoinIterator
	 */
	public void reset() {
		iterator.reset();
	}

	/**
	 * Return the size of the underlying JoinIterator
	 */
	public int size() {
		return iterator.size();
	}

	/**
	 * Calls to the remove operation are not allowed from this iterator
	 */
	public void remove() throws UnsupportedOperationException {
		throw new UnsupportedOperationException(
				"Remove operation not allowed in a JoinIterator.");
	}

	/**
	 * Trap any calls to add children and forcibly reset everything
	 */
	public void insert(MutableTreeNode node, int index) {
		super.insert(node, index);
		reset();
	}

	/**
	 * JoinIteratorNode objects are never leaves in the tree or at least
	 * shouldn't act as such.
	 */
	public boolean isLeaf() {
		return false;
	}

	/**
	 * JoinIteratorNode objects rely on their children to provide them with
	 * objects to iterate over, they do not contain any collections themselves
	 */
	public boolean getAllowsChildren() {
		return true;
	}
}
