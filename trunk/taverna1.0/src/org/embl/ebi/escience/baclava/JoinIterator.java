/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.baclava;

// Utility Imports
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Provides an Iterator that iterates over the orthogonal join of a set of
 * BaclavaIterator instances.
 * 
 * @author Tom Oinn
 */
public class JoinIterator implements ResumableIterator {

	private boolean isEmptyIterator = false;

	private ResumableIterator[] iteratorsArray;	

	private Object[] currentState;

	private boolean initialized = false;

	public JoinIterator() {
		this.iteratorsArray = new ResumableIterator[0];
	}

	/**
	 * The iterator is constructed with an array of BaclavaIterator instances
	 * and creates an iterator that iterates over the orthogonal join of this
	 * set, where the iterator at position 0 in the array is regarded as the
	 * outermost one
	 */
	public JoinIterator(ResumableIterator[] b) {
		this.iteratorsArray = b;
		// Check that we haven't been passed a load
		// of empty iterators!
		
		if (b.length != 0) {
			// Check that all the iterators
			// have at least one element in
			for (int i = 0; i < b.length; i++) {
				if (b[i].hasNext() == false) {
					isEmptyIterator = true;
				}
			}
		}
		// If here and nextState == true then we have at least
		// one iterator in the array and every iterator present
		// has at least one item in. This is mandatory, if this
		// doesn't apply then we can't create the join.
	}

	/**
	 * Get the current location by appending all the current location arrays of
	 * all child iterators
	 */
	public int[] getCurrentLocation() {
		ResumableIterator[] iterators = iterators();
		ArrayList temp = new ArrayList();
		for (int i = 0; i < iterators.length; i++) {
			temp.add(iterators[i].getCurrentLocation());
		}
		return concatArrays(temp);
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

	ResumableIterator[] iterators() {
		return this.iteratorsArray;
	}

	boolean emptyIterator() {
		return this.isEmptyIterator;
	}

	/**
	 * The join has next if any iterator within it has a next value of true
	 */
	public synchronized boolean hasNext() {
		if (emptyIterator()) {
			return false;
		}
		for (int i = 0; i < iterators().length; i++) {
			if (iterators()[i].hasNext()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Calls to the remove operation are not allowed from this iterator
	 */
	public void remove() throws UnsupportedOperationException {
		throw new UnsupportedOperationException(
				"Remove operation not allowed in a JoinIterator.");
	}

	/**
	 * Get an array of objects corresponding to the current next state of the
	 * join of the member iterators
	 */
	public Object next() throws NoSuchElementException {
		if (initialized == false) {
			// Create the initial state of the iterator
			currentState = new Object[iterators().length];
			for (int i = 0; i < iterators().length; i++) {
				currentState[i] = iterators()[i].next();
			}
			initialized = true;
			return currentState;
		}
		// Try to roll over the nth element, where n starts at the rightmost
		// iterator...
		int iteratorNumber = iterators().length - 1;
		boolean doneRolling = false;
		// While we haven't finished rolling over the iterators and
		// there are still more to be rolled....
		while (!doneRolling && iteratorNumber >= 0) {
			// If the current iterator has elements left in it then extract the
			// next one
			// and put it into the result set.
			if (iterators()[iteratorNumber].hasNext()) {
				currentState[iteratorNumber] = iterators()[iteratorNumber]
						.next();
				doneRolling = true;
			} else {
				// Reset the current iterator and move onto the next most
				// significant one
				// If this was the last iterator then the iterator number will
				// now be -1
				// and the loop will exit.
				iterators()[iteratorNumber].reset();
				currentState[iteratorNumber] = iterators()[iteratorNumber]
						.next();
				iteratorNumber--;
			}
		}
		if (!doneRolling) {
			throw new NoSuchElementException("Reached end of join iterator");
		}
		return currentState;
	}

	/**
	 * Return the total number of iterations this joinIterator will produce,
	 * this is the product of the sizes of all component iterators.
	 */
	public int size() {
		int size = 1;
		for (int i = 0; i < iterators().length; i++) {
			size = size * iterators()[i].size();
		}
		return size;
	}

	/**
	 * Reset the join iterator to its starting state
	 */
	public void reset() {
		for (int i = 0; i < iterators().length; i++) {
			iterators()[i].reset();
		}
		initialized = false;
	}

}
