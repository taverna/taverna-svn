/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.baclava;

import javax.swing.tree.*;
import java.util.*;

/**
 * A DefaultMutableTreeNode implementing ResumableIterator which wraps a
 * LockStepIterator.
 * 
 * @author Tom Oinn
 */
public class LockStepIteratorNode extends DefaultMutableTreeNode implements
		ResumableIterator {

	LockStepIterator iterator;

	/**
	 * Create a new LockStepIteratorNode, calls to insert(...) will add
	 * iterators to the dot product.
	 */
	public LockStepIteratorNode() {
		iterator = new LockStepIterator() {
			// Gather a list of all children in the
			// enclosing class
			ResumableIterator[] iterators() {
				ResumableIterator[] result = new ResumableIterator[LockStepIteratorNode.this
						.getChildCount()];
				for (int i = 0; i < LockStepIteratorNode.this.getChildCount(); i++) {
					result[i] = (ResumableIterator) LockStepIteratorNode.this
							.getChildAt(i);
				}
				return result;
			}

			boolean emptyIterator() {
				return (LockStepIteratorNode.this.getChildCount() == 0);
			}
		};
	}

	/**
	 * Return a Map of named objects derived from any iterator nodes attached to
	 * this one as children, the iteration strategy is derived from the
	 * underlying LockStepIterator which performs a cross product of all
	 * available sub-iterators
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
	 * Delegates to contained LockStepIterator
	 */
	public boolean hasNext() {
		return iterator.hasNext();
	}

	/**
	 * Delegates to contained LockStepIterator
	 */
	public void reset() {
		iterator.reset();
	}

	/**
	 * Delegates to contained LockStepIterator
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
	 * A LockStepIteratorNode requires children to be functional and is
	 * therefore never a leaf.
	 */
	public boolean isLeaf() {
		return false;
	}

	/**
	 * LockStepIteratorNode objects pull iterators from their children,
	 * therefore always allow them.
	 */
	public boolean getAllowsChildren() {
		return true;
	}

	/**
	 * Get the current location
	 */
	public int[] getCurrentLocation() {
		return iterator.getCurrentLocation();
	}

}
