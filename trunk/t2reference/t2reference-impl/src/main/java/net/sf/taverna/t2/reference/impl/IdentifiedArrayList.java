package net.sf.taverna.t2.reference.impl;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.ListIterator;

import net.sf.taverna.t2.reference.IdentifiedList;
import net.sf.taverna.t2.reference.T2Reference;

/**
 * Implementation of IdentifiedList which delegates to an ArrayList for its
 * storage functionality.
 * 
 * @author Tom Oinn
 * 
 * @param <T>
 */
public class IdentifiedArrayList<T> implements IdentifiedList<T> {

	private T2ReferenceImpl id = null;
	protected List<T> listDelegate = null;

	// Constructors copied from ArrayList for convenience
	public IdentifiedArrayList() {
		super();
		this.listDelegate = new ArrayList<T>();
	}
	public IdentifiedArrayList(Collection<T> c) {
		super();
		this.listDelegate = new ArrayList<T>(c);
	}
	public IdentifiedArrayList(int initialCapacity) {
		super();
		this.listDelegate = new ArrayList<T>(initialCapacity);
	}
	
	public T2Reference getId() {
		return id;
	}

	/**
	 * Used by Hibernate to get the implementation type of the internal ID
	 * correctly
	 */
	public T2ReferenceImpl getTypedId() {
		return id;
	}

	/**
	 * Used by Hibernate to restore the ID correctly when constructing instances
	 * of this object or its subclasses from the database
	 */
	public void setTypedId(T2ReferenceImpl newId) {
		this.id = newId;
	}

	private void checkUndefinedId() {
		if (this.id != null) {
			throw new IllegalStateException(
					"Attempt made to modify a list which has already been named");
		}
	}

	public boolean add(T e) {
		checkUndefinedId();
		return listDelegate.add(e);
	}

	public void add(int index, T element) {
		checkUndefinedId();
		listDelegate.add(index, element);
	}

	public boolean addAll(Collection<? extends T> c) {
		checkUndefinedId();
		return listDelegate.addAll(c);
	}

	public boolean addAll(int index, Collection<? extends T> c) {
		checkUndefinedId();
		return listDelegate.addAll(index, c);
	}

	public void clear() {
		checkUndefinedId();
		listDelegate.clear();
	}

	public boolean contains(Object o) {
		return listDelegate.contains(o);
	}

	public boolean containsAll(Collection<?> c) {
		return listDelegate.containsAll(c);
	}

	public T get(int index) {
		return listDelegate.get(index);
	}

	public int indexOf(Object o) {
		return listDelegate.indexOf(o);
	}

	public boolean isEmpty() {
		return listDelegate.isEmpty();
	}

	public Iterator<T> iterator() {
		return listDelegate.iterator();
	}

	public int lastIndexOf(Object o) {
		return listDelegate.lastIndexOf(o);
	}

	/**
	 * The ListIterator can modify the list contents, so wrap the delegate's
	 * list iterator and use as a delegate itself, checking for null ID on
	 * operations which set list properties.
	 * 
	 * @param iteratorDelegate
	 *            ListIterator to wrap.
	 * @return wrapped ListIterator which throws IllegalStateException on calls
	 *         which modify the list if the ID has been set to a non-null value
	 */
	private ListIterator<T> getCheckedListIterator(
			final ListIterator<T> iteratorDelegate) {
		return new ListIterator<T>() {
			public void add(T e) {
				checkUndefinedId();
				iteratorDelegate.add(e);
			}

			public boolean hasNext() {
				return iteratorDelegate.hasNext();
			}

			public boolean hasPrevious() {
				return iteratorDelegate.hasPrevious();
			}

			public T next() {
				return iteratorDelegate.next();
			}

			public int nextIndex() {
				return iteratorDelegate.nextIndex();
			}

			public T previous() {
				return iteratorDelegate.previous();
			}

			public int previousIndex() {
				return iteratorDelegate.previousIndex();
			}

			public void remove() {
				checkUndefinedId();
				iteratorDelegate.remove();
			}

			public void set(T e) {
				checkUndefinedId();
				iteratorDelegate.set(e);
			}
		};
	}

	public ListIterator<T> listIterator() {
		return getCheckedListIterator(listDelegate.listIterator());
	}

	public ListIterator<T> listIterator(int index) {
		return getCheckedListIterator(listDelegate.listIterator(index));
	}

	public boolean remove(Object o) {
		checkUndefinedId();
		return listDelegate.remove(o);
	}

	public T remove(int index) {
		checkUndefinedId();
		return listDelegate.remove(index);
	}

	public boolean removeAll(Collection<?> c) {
		checkUndefinedId();
		return listDelegate.removeAll(c);
	}

	public boolean retainAll(Collection<?> c) {
		checkUndefinedId();
		return listDelegate.retainAll(c);
	}

	public T set(int index, T element) {
		checkUndefinedId();
		return listDelegate.set(index, element);
	}

	public int size() {
		return listDelegate.size();
	}

	public List<T> subList(int fromIndex, int toIndex) {
		return listDelegate.subList(fromIndex, toIndex);
	}

	public Object[] toArray() {
		return listDelegate.toArray();
	}

	public <U> U[] toArray(U[] a) {
		return listDelegate.toArray(a);
	}

}