/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.baclava;

import javax.swing.tree.*;
import java.util.*;

/**
 * A DefaultMutableTreeNode implementing ResumableIterator
 * which wraps a LockStepIterator.
 * @author Tom Oinn
 */
public class LockStepIteratorNode extends DefaultMutableTreeNode implements ResumableIterator {
    
    LockStepIterator iterator;

    public LockStepIteratorNode() {
	iterator = new LockStepIterator() {
		// Gather a list of all children in the
		// enclosing class
		ResumableIterator[] iterators() {
		    ResumableIterator[] result = new ResumableIterator[LockStepIteratorNode.this.getChildCount()];
		    for (int i = 0; i < LockStepIteratorNode.this.getChildCount(); i++) {
			result[i] = (ResumableIterator)LockStepIteratorNode.this.getChildAt(i);
		    }
		    return result;
		}
		boolean emptyIterator() {
		    return false;
		}
	    };
    }
    
    public Object next() {
	// Result will be an array of Map objects, convert to a Map i.e.
	// convert [{foo->bar}],[{urgle->wibble}] to {foo->bar,urgle->wibble}
	// and return
	Object[] o = (Object[])iterator.next();
	Map[] map = new Map[o.length];
	for (int i = 0; i < o.length; i++) {
	    map[i] = (Map)o[i];
	}

	Map result = new HashMap();
	for (int j = 0; j < map.length; j++) {
	    for (Iterator i = map[j].keySet().iterator(); i.hasNext();) {
		String name = (String)i.next();
		Object value = map[j].get(name);
		result.put(name, value);
	    }
	}
	return result;
    }
    
    public boolean hasNext() {
	return iterator.hasNext();
    }
    
    public void reset() {
	iterator.reset();
    }

    public int size() {
	return iterator.size();
    }

    /**
     * Calls to the remove operation are not allowed from this iterator
     */
    public void remove() 
	throws UnsupportedOperationException {
	throw new UnsupportedOperationException("Remove operation not allowed in a JoinIterator.");
    }
    
    /**
     * Trap any calls to add children and forcibly reset everything
     */
    public void insert(MutableTreeNode node, int index) {
	super.insert(node, index);
	reset();
    }

}
