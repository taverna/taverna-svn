/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.baclava;

// Utility Imports
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;

import java.lang.Object;
import java.lang.UnsupportedOperationException;



/**
 * This provides an Iterator interface with a single
 * additional method to allow reset of the iterator
 * to its starting state. This is required to produce
 * orthogonal joins between iterators used in the implicit
 * iteration mechanism within the enactor.
 * @author Tom Oinn
 */
public class BaclavaIterator implements ResumableIterator {

    private Collection underlyingCollection = null;
    private Iterator internalIterator = null;

    public BaclavaIterator(Collection c) {
	this.underlyingCollection = c;
	this.internalIterator = c.iterator();
    }
    
    public synchronized boolean hasNext() {
	return this.internalIterator.hasNext();
    }

    public synchronized Object next() 
	throws NoSuchElementException {
	return this.internalIterator.next();
    }
    
    public void remove() 
	throws UnsupportedOperationException {
	throw new UnsupportedOperationException("Remove operation not allowed in a BaclavaIterator.");
    }
    
    public synchronized void reset() {
	this.internalIterator = underlyingCollection.iterator();
    }

    public int size() {
	return this.underlyingCollection.size();
    }
        
}
