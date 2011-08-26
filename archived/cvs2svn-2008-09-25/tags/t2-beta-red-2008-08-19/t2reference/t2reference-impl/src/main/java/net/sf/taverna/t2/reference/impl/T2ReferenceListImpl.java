package net.sf.taverna.t2.reference.impl;

import java.util.List;

import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.reference.h3.HibernateMappedEntity;

/**
 * Simple extension of
 * <code>{@link IdentifiedArrayList IdentifiedArrayList&lt;T2Reference&gt;}</code>
 * exposing get and set methods for the list contents so we can map it in
 * hibernate.
 * 
 * @author Tom Oinn
 * 
 */
public class T2ReferenceListImpl extends IdentifiedArrayList<T2Reference>
		implements HibernateMappedEntity {

	public T2ReferenceListImpl() {
		super();
	}

	/**
	 * This is only called from Hibernate, outside of test code, so is
	 * relatively safe to leave unchecked.
	 */
	@SuppressWarnings("unchecked")
	public List getListContents() {
		return this.listDelegate;
	}

	/**
	 * This is only called from Hibernate, outside of test code, so is
	 * relatively safe to leave unchecked.
	 */
	@SuppressWarnings("unchecked")
	public void setListContents(List newList) {
		this.listDelegate = newList;
	}

	/**
	 * Print the contents of this list for vaguely human readable debug
	 * porpoises.
	 */
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(this.getId().toString() + "\n");
		int counter = 0;
		for (T2Reference ref : listDelegate) {
			sb.append("  " + (++counter) + ") " + ref.toString() + "\n");
		}
		return sb.toString();
	}

}
