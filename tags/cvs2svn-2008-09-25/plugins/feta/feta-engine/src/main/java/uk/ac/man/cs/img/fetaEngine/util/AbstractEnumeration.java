/*
 *This library is free software; you can redistribute it and/or
 *modify it under the terms of the GNU Lesser General Public
 *License as published by the Free Software Foundation; either
 *version 2.1 of the License, or (at your option) any later version.
 *
 *This library is distributed in the hope that it will be useful,
 *but WITHOUT ANY WARRANTY; without even the implied warranty of
 *MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *Lesser General Public License for more details.
 *
 *You should have received a copy of the GNU Lesser General Public
 *License along with this library; if not, write to the Free Software
 *Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package uk.ac.man.cs.img.fetaEngine.util; // Generated package name

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.ConcurrentModificationException;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * AbstractEnumeration.java
 * 
 * Provides support for Enumerated Types in Java. This class provides several
 * methods useful for all Enumerated Types including a sensible printable
 * toString method, the total number of instances of a given type, an Iterator
 * through all the types, and an ordinal number running from 0 upwards for each
 * type.
 * <p>
 * 
 * This class is used by extending it with a new class which
 * <ul>
 * <li>is declared final, which prevents subclasses from introducing new
 * instances</li>
 * <li>has a private constructor</li>
 * <li>declares a public static final data member for each instance that is
 * required</li>
 * </ul>
 * 
 * So for example
 * 
 * <code>
 *   <pre>
 *  public final class TrafficLight extends AbstractEnumeration
 *  {
 *    private TrafficLight( String toString ){
 *    {
 *      super( toString );
 *    }
 * 
 *    public static final TrafficLight RED 
 *       = new TrafficLight( &quot;TrafficLight Enumerated Type:- RED&quot; );
 *    public static final TrafficLight ORANGE 
 *       = new Traffic( &quot;TrafficLight Enumerated Type:- ORANGE&quot; );
 *    public static final TrafficLight GREEN 
 *       = new Traffic( TrafficLight Enumerated Type:- GREEN&quot; );
 *  }
 * 
 * </pre>
 * </code>
 * 
 * Currently this class can not be serialised. Having one of the subclasses
 * implement Serializable would be a mistake as it would provide an alternative
 * route for the instances of the class to be produced. This could be
 * circumvented using the replaceObject/writeObject methods introduced in the
 * 1.2 serialisation spec, but I haven't got around to implementing this yet!
 * 
 * It should be noted that there are problems in compiling this class with some
 * versions of javac. This is bug in javac (Bug ID:4157676), not my code which
 * is perfectly legal java. Jikes works fine. Alternatively you can comment out
 * the references to the ord variable and do without this functionality, or make
 * it non final, in which case attempts to alter it will no longer produce
 * compiler errors as they should.
 * 
 * Created: Mon Feb 21 14:11:41 2000
 * 
 * @author Phillip Lord
 * 
 */

public abstract class AbstractEnumeration implements Serializable {
	// A linked list of all these types.
	private String toString;

	private AbstractEnumeration next;

	private AbstractEnumeration prev;

	// private static hashtables, which support the enum
	// implementation. We are using hashtables because they are
	// synchronized and they need to be for this!
	private static Hashtable upperBoundHash = new Hashtable();

	private static Hashtable firstHash = new Hashtable();

	private static Hashtable lastHash = new Hashtable();

	private static Hashtable currentTopOrd = new Hashtable();

	// ordinal number. Very useful.
	public final int ord; // if this does not compile its a bug in javac.
							// Removing "final" should make it work!!!!

	protected AbstractEnumeration(String toString) {
		super();

		this.toString = toString;

		// sort the ordinal number for this type.
		// first retrieve it (if it exists) from the hash
		Object upBound = upperBoundHash.get(getClass());
		int upperBound;

		if (upBound == null) {
			upperBound = 0;
		} else {
			upperBound = ((Integer) upBound).intValue();
		}

		// make the upper bound recoverable. Sadly this causes javac to
		// croak, due to a bug in javac (see for instance Bug ID:
		// 4157676). As this utility is vital to the class I have decided
		// to incorporate it anyway, and simply compile with Jikes. The
		// bug should be fixed as of the 1.3 release.
		ord = upperBound;
		// then return the advanced upper bound to the hash
		upperBoundHash.put(getClass(), new Integer(++upperBound));

		// next step is to make add to (or create) a linked list
		// start and end elements in the appropriate elements
		Object firstObj = firstHash.get(getClass());
		Object lastObj = lastHash.get(getClass());

		AbstractEnumeration first = (firstObj == null) ? null
				: ((AbstractEnumeration) firstObj);
		AbstractEnumeration last = (lastObj == null) ? null
				: ((AbstractEnumeration) lastObj);

		// sort out the linked list that we use to store all of the
		// Elements
		if (first == null) {
			first = this;
			firstHash.put(getClass(), first);
		}

		if (last != null) {
			this.prev = last;
			last.next = this;
		}

		last = this;
		lastHash.put(getClass(), last);
	}

	// serialisation support
	public Object readResolve() throws ObjectStreamException {
		// this instance will share all the same attributes as the
		// original, but will
		// be an exact duplicate of an already existing class which is not
		// good. So we want to replace this instance with the existing
		// one. So first we get the class of the existing instance
		Class cls = getClass();

		// and then all of the other instances.
		AbstractEnumeration[] elements = getAllElements(cls);

		// the ordinal number of the instance is of course also the index
		// in the array so we can use it to do the retrieval.
		return elements[this.ord];
	}

	// these methods are enum support
	public static Iterator iterator(Class cla) {
		return new ElementIterator(cla);
	}

	public static class ElementIterator implements Iterator {
		// we need to store the size so that we can detect any concurrent
		// modifications. This should only happen if things go badly
		// wrong.
		int size;

		// store the current position
		private AbstractEnumeration curr;

		public ElementIterator(Class cla) {
			// store the size of this enum
			size = ((Integer) upperBoundHash.get(cla)).intValue();

			// store the current element
			curr = (AbstractEnumeration) firstHash.get(cla);
		}

		public boolean hasNext() {
			if (curr != null)
				return true;

			return false;
		}

		public Object next() {
			if (getSize(curr.getClass()) != size)
				throw new ConcurrentModificationException(
						"The total number of elements has changed, which means bad things");

			if (curr == null)
				throw new NoSuchElementException(
						"Attempt to iterate past last Element");
			AbstractEnumeration retn = curr;
			curr = curr.next;
			return retn;
		}

		public void remove() {
			throw new UnsupportedOperationException(
					"Removing elements not allowed");
		}
	}

	public static AbstractEnumeration[] getAllElements(Class cla) {
		// should I cache these. Maybe....
		AbstractEnumeration[] allElements = new AbstractEnumeration[getSize(cla)];

		Iterator iter = iterator(cla);
		int count = 0;
		while (iter.hasNext()) {
			allElements[count++] = (AbstractEnumeration) iter.next();
		}
		return allElements;
	}

	public static int getSize(Class cla) {
		return ((Integer) upperBoundHash.get(cla)).intValue();
	}

	public String toString() {
		return toString;
	}
} // AbstractEnumeration

