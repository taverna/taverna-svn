/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.baclava;

// Utility Imports
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Provides an Iterator that iterates over the contained iterators in lock step,
 * incrementing all at the same time.
 * 
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
		} else {
			int iteratorSize = i[0].size();
			for (int j = 0; j < i.length; j++) {
				if (i[j].size() != iteratorSize) {
					// Iterator sizes don't match
					System.out
							.println("Iterator sizes mismatch, creating an empty iterator.");
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
	 * Get the current location by finding the longest current position array
	 * from all the child iterators and returning it. This is because otherwise
	 * if we take the approach that the join iterator uses we end up with holes
	 * in the resultant collection structure caused by the redundant
	 * information.
	 */
	public int[] getCurrentLocation() {
		ResumableIterator[] iterators = iterators();
		int longestIteratorLength = -1;
		int longestIteratorIndex = 0;
		for (int i = 0; i < iterators.length; i++) {
			if (iterators[i].getCurrentLocation().length > longestIteratorLength) {
				longestIteratorLength = iterators[i].getCurrentLocation().length;
				longestIteratorIndex = i;
			}
		}
		return iterators[longestIteratorIndex].getCurrentLocation();
	}

	/**
	 * Consume a List of int[] and return an int[] formed from the concatenation
	 * of all contains arrays
	 */
	private int[] concatArrays(List listOfArrays) {
		// Find the array target sizes
		int totalSize = 0;
		for (Iterator i = listOfArrays.iterator(); i.hasNext();) {
			totalSize += ((int[]) i.next()).length;
		}
		int[] output = new int[totalSize];
		int currentIndex = 0;
		for (Iterator i = listOfArrays.iterator(); i.hasNext();) {
			int[] source = (int[]) i.next();
			for (int j = 0; j < source.length; j++) {
				output[currentIndex++] = source[j];
			}
		}
		return output;
	}

	/**
	 * If the iterator isn't empty then all the iterators contained within it
	 * are the same length, can therefore just ask the first one
	 */
	public synchronized boolean hasNext() {
		return (emptyIterator()) ? false : iterators()[0].hasNext();
	}

	/**
	 * Calls to the remove operation are not allowed from this iterator
	 */
	public void remove() throws UnsupportedOperationException {
		throw new UnsupportedOperationException(
				"Remove operation not allowed in a JoinIterator.");
	}

	/**
	 * Returns an array of items created by calling the next() method of each
	 * contained iterator
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
		return (emptyIterator()) ? 0 : iterators()[0].size();
	}

}
