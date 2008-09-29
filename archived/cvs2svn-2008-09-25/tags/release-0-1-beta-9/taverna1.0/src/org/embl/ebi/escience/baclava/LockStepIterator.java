/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.baclava;

// Utility Imports
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.embl.ebi.escience.baclava.BaclavaIterator;
import java.lang.Object;
import java.lang.UnsupportedOperationException;

/**
 * Provides an Iterator that iterates over the contained
 * iterators in lock step, incrementing all at the same
 * time.
 * @author Tom Oinn
 */
public class LockStepIterator implements ResumableIterator {

    private ResumableIterator[] iteratorsArray;
    private boolean isEmptyIterator = false;

    public LockStepIterator() {
	iteratorsArray = new ResumableIterator[0];
	isEmptyIterator = true;
    }
    
    public LockStepIterator(ResumableIterator[] i) {
	iteratorsArray = i;
	if (i.length == 0) {
	    // Empty set supplied
	    System.out.println("Empty iterator created.");
	    isEmptyIterator = true;
	}
	else {
	    int iteratorSize = i[0].size();
	    for (int j = 0; j < i.length; j++) {
		if (i[j].size() != iteratorSize) {
		    // Iterator sizes don't match
		    System.out.println("Iterator sizes mismatch, creating an empty iterator.");
		    isEmptyIterator = true;
		}
	    }
	}
    }
    
    ResumableIterator[] iterators() {
	return this.iteratorsArray;
    }
    boolean emptyIterator() {
	return this.isEmptyIterator;
    }

    /**
     * Reset all the iterators
     */
    public synchronized void reset() {
	for (int i = 0; i < iterators().length; i++) {
	    iterators()[i].reset();
	}
    }
    
    /**
     * If the iterator isn't empty then all the iterators
     * contained within it are the same length, can therefore
     * just ask the first one
     */
    public synchronized boolean hasNext() {
	return (emptyIterator())?false:iterators()[0].hasNext();
    }
    
    /**
     * Calls to the remove operation are not allowed from this iterator
     */
    public void remove() 
	throws UnsupportedOperationException {
	throw new UnsupportedOperationException("Remove operation not allowed in a JoinIterator.");
    }
    
    /**
     * Returns an array of items created by calling the next()
     * method of each contained iterator
     */
    public synchronized Object next() throws NoSuchElementException {
	Object[] results = new Object[iterators().length];
	for (int i = 0; i < iterators().length; i++) {
	    results[i] = iterators()[i].next();
	}
	return results;
    }

    /**
     * All iterators are the same size
     */
    public int size() {
	return (emptyIterator())?0:iterators()[0].size();
    }

}
