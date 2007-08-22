package net.sf.taverna.t2.cloudone;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * A named list of entity identifiers. Represents a single list within the data
 * manager system.
 * 
 * @author Tom Oinn
 * @author Matthew Pocock
 * 
 */
public class EntityList implements Entity<EntityListIdentifier>, List<EntityIdentifier> {
	private final EntityListIdentifier identifier;

	private final List<EntityIdentifier> list;

	public EntityList(final EntityListIdentifier identifier,
			final List<EntityIdentifier> list) {
		super();
		this.identifier = identifier;
		this.list = list;
	}

	public EntityListIdentifier getIdentifier() {
		return identifier;
	}

	public boolean add(EntityIdentifier o) {
		throw new UnsupportedOperationException();
	}

	public void add(int index, EntityIdentifier element) {
		throw new UnsupportedOperationException();
	}

	public boolean addAll(Collection<? extends EntityIdentifier> c) {
		throw new UnsupportedOperationException();
	}

	public boolean addAll(int index, Collection<? extends EntityIdentifier> c) {
		throw new UnsupportedOperationException();
	}

	public void clear() {
		throw new UnsupportedOperationException();
	}

	public boolean contains(Object o) {
		return list.contains(o);
	}

	public boolean containsAll(Collection<?> c) {
		return list.containsAll(c);
	}

	public boolean equals(Object o) {
		return list.equals(o);
	}

	public EntityIdentifier get(int index) {
		return list.get(index);
	}

	public int hashCode() {
		return list.hashCode();
	}

	public int indexOf(Object o) {
		return list.indexOf(o);
	}

	public boolean isEmpty() {
		return list.isEmpty();
	}

	public Iterator<EntityIdentifier> iterator() {
		return list.iterator();
	}

	public int lastIndexOf(Object o) {
		return list.lastIndexOf(o);
	}

	public ListIterator<EntityIdentifier> listIterator() {
		return list.listIterator();
	}

	public ListIterator<EntityIdentifier> listIterator(int index) {
		return list.listIterator(index);
	}

	public EntityIdentifier remove(int index) {
		throw new UnsupportedOperationException();
	}

	public boolean remove(Object o) {
		throw new UnsupportedOperationException();
	}

	public boolean removeAll(Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	public boolean retainAll(Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	public EntityIdentifier set(int index, EntityIdentifier element) {
		throw new UnsupportedOperationException();
	}

	public int size() {
		return list.size();
	}

	public List<EntityIdentifier> subList(int fromIndex, int toIndex) {
		return list.subList(fromIndex, toIndex);
	}

	public Object[] toArray() {
		return list.toArray();
	}

	public <T> T[] toArray(T[] a) {
		return list.toArray(a);
	}

}
