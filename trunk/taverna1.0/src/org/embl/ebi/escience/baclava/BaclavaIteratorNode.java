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
 * by wrapping a BaclavaIterator. Further, all output from
 * the underlying ResumableIterator is named and the output
 * wrapped in a Map object
 * @author Tom Oinn
 */
public class BaclavaIteratorNode extends DefaultMutableTreeNode implements ResumableIterator {
    
    BaclavaIterator iterator;
    String name;

    public BaclavaIteratorNode(BaclavaIterator i, String name) {
	this.iterator = i;
	this.name = name;
    }
    
    public Object next() {
	Map result = new HashMap();
	result.put(name, iterator.next());
	return result;
    }

    public boolean hasNext() {
	return iterator.hasNext();
    }

    public void remove() throws UnsupportedOperationException {
	iterator.remove();
    }
    
    public void reset() {
	iterator.reset();
    }
    
    public int size() {
	return iterator.size();
    }
    
}
