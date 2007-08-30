package net.sf.taverna.t2.cloudone.entity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import net.sf.taverna.t2.cloudone.bean.EntityListBean;
import net.sf.taverna.t2.cloudone.identifier.EntityIdentifier;
import net.sf.taverna.t2.cloudone.identifier.EntityIdentifiers;
import net.sf.taverna.t2.cloudone.identifier.EntityListIdentifier;

/**
 * A named list of entity identifiers. Represents a single list within the data
 * manager system.
 * 
 * @author Tom Oinn
 * @author Matthew Pocock
 * 
 */
public class EntityList implements Entity<EntityListIdentifier, EntityListBean>, List<EntityIdentifier> {
	
	public EntityList() {
		identifier = null;
		list = new ArrayList<EntityIdentifier>();
	}
	
	private EntityListIdentifier identifier;

	private final List<EntityIdentifier> list;

	public EntityList(final EntityListIdentifier identifier,
			final List<EntityIdentifier> list) {
		super();
		if (identifier == null) {
			throw new NullPointerException("Identifier can't be null");
		}
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

	public EntityListBean getAsBean() {
		EntityListBean bean = new EntityListBean();
		bean.setIdentifier(identifier.getAsBean());
		
		List<String> content = new ArrayList<String>();
		for (EntityIdentifier child : list) {
			content.add(child.getAsBean());
		}
		bean.setContent(content);
		
		return bean;
	}

	public void setFromBean(EntityListBean bean) {
		if (identifier != null || ! list.isEmpty()) {
			throw new IllegalStateException("Can't run setFromBean() on initialised EntityList");
		}
		identifier = (EntityListIdentifier) EntityIdentifiers.parse(bean.getIdentifier());
		for (String id : bean.getContent()) {
			list.add(EntityIdentifiers.parse(id));
		}
	}

}
