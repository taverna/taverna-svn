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
public class EntityList implements
		Entity<EntityListIdentifier, EntityListBean>, List<EntityIdentifier> {

	private EntityListIdentifier identifier;

	private final List<EntityIdentifier> list;

	/**
	 * Construct an EntityList that must immediately be populated by
	 * {@link #setFromBean(EntityListBean)}.
	 * 
	 */
	public EntityList() {
		identifier = null;
		list = new ArrayList<EntityIdentifier>();
	}

	/**
	 * Construct an EntityList identified by an {@link EntityListIdentifier}
	 * containing a {@link List} of {@link EntityIdentifier}s.
	 * 
	 * @param identifier
	 *            The identifying {@link EntityListIdentifier}
	 * @param list
	 *            The contained {@link List} of {@link EntityIdentifier}s
	 */
	public EntityList(final EntityListIdentifier identifier,
			final List<EntityIdentifier> list) {
		super();
		if (identifier == null) {
			throw new NullPointerException("Identifier can't be null");
		}
		this.identifier = identifier;
		this.list = list;
	}

	/**
	 * Unsupported {@link List} operation.
	 */
	public boolean add(EntityIdentifier o) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Unsupported {@link List} operation.
	 */
	public void add(int index, EntityIdentifier element) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Unsupported {@link List} operation.
	 */
	public boolean addAll(Collection<? extends EntityIdentifier> c) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Unsupported {@link List} operation.
	 */
	public boolean addAll(int index, Collection<? extends EntityIdentifier> c) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Unsupported {@link List} operation.
	 */
	public void clear() {
		throw new UnsupportedOperationException();
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean contains(Object o) {
		return list.contains(o);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean containsAll(Collection<?> c) {
		return list.containsAll(c);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((identifier == null) ? 0 : identifier.hashCode());
		result = prime * result + ((list == null) ? 0 : list.hashCode());
		return result;
	}

	/**
	 * Check equality against <code>obj</code>.
	 * 
	 * @param obj
	 * @return true if and only if <code>obj</code> is an {@link EntityList}
	 *         and its {@link #getIdentifier()} equals this
	 *         {@link #getIdentifier()}.
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final EntityList other = (EntityList) obj;
		if (identifier == null) {
			if (other.identifier != null)
				return false;
		} else if (!identifier.equals(other.identifier))
			return false;
		if (list == null) {
			if (other.list != null)
				return false;
		} else if (!list.equals(other.list))
			return false;
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	public EntityIdentifier get(int index) {
		return list.get(index);
	}

	/**
	 * {@inheritDoc}
	 */
	public EntityListBean getAsBean() {
		EntityListBean bean = new EntityListBean();
		bean.setIdentifier(identifier.getAsURI());

		List<String> content = new ArrayList<String>();
		for (EntityIdentifier child : list) {
			content.add(child.getAsURI());
		}
		bean.setContent(content);

		return bean;
	}

	/**
	 * {@inheritDoc}
	 */
	public EntityListIdentifier getIdentifier() {
		return identifier;
	}

	/**
	 * {@inheritDoc}
	 */
	public int indexOf(Object o) {
		return list.indexOf(o);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isEmpty() {
		return list.isEmpty();
	}

	/**
	 * {@inheritDoc}
	 */
	public Iterator<EntityIdentifier> iterator() {
		return list.iterator();
	}

	/**
	 * {@inheritDoc}
	 */
	public int lastIndexOf(Object o) {
		return list.lastIndexOf(o);
	}

	/**
	 * {@inheritDoc}
	 */
	public ListIterator<EntityIdentifier> listIterator() {
		return list.listIterator();
	}

	/**
	 * {@inheritDoc}
	 */
	public ListIterator<EntityIdentifier> listIterator(int index) {
		return list.listIterator(index);
	}

	/**
	 * Unsupported {@link List} operation.
	 */
	public EntityIdentifier remove(int index) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Unsupported {@link List} operation.
	 */
	public boolean remove(Object o) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Unsupported {@link List} operation.
	 */
	public boolean removeAll(Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Unsupported {@link List} operation.
	 */
	public boolean retainAll(Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Unsupported {@link List} operation.
	 */
	public EntityIdentifier set(int index, EntityIdentifier element) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Populate from an {@link EntityListBean}. The EntityList must have been
	 * constructed using {@link #EntityList()}, and this method can only be
	 * called once.
	 * 
	 */
	public void setFromBean(EntityListBean bean) {
		if (identifier != null || !list.isEmpty()) {
			throw new IllegalStateException(
					"Can't run setFromBean() on initialised EntityList");
		}
		identifier = (EntityListIdentifier) EntityIdentifiers.parse(bean
				.getIdentifier());
		for (String id : bean.getContent()) {
			list.add(EntityIdentifiers.parse(id));
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public int size() {
		return list.size();
	}

	/**
	 * {@inheritDoc}
	 */
	public List<EntityIdentifier> subList(int fromIndex, int toIndex) {
		return list.subList(fromIndex, toIndex);
	}

	/**
	 * {@inheritDoc}
	 */
	public Object[] toArray() {
		return list.toArray();
	}

	/**
	 * {@inheritDoc}
	 */
	public <T> T[] toArray(T[] a) {
		return list.toArray(a);
	}

}
