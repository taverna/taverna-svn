package net.sf.taverna.t2.reference.impl;

import net.sf.taverna.t2.reference.T2Reference;

/**
 * Abstract superclass of ReferenceSetImpl, IdentifiedArrayList and
 * ErrorDocumentImpl, manages the T2Reference field for these types and their
 * hibernate backing.
 * 
 * @author Tom Oinn
 * 
 */
public class AbstractEntityImpl {

	private T2ReferenceImpl id;

	private String compactId = null;

	public T2Reference getId() {
		return id;
	}

	/**
	 * This method is only ever called from within Hibernate, and is used to
	 * initialize the unique ID of this reference set.
	 */
	public void setTypedId(T2ReferenceImpl newId) {
		this.id = newId;
	}

	/**
	 * Used because technically you can't accept and return implementation types
	 * in the methods on a bean which implements an interface, but Hibernate
	 * needs to construct concrete input and output types!
	 */
	public T2ReferenceImpl getTypedId() {
		return this.id;
	}

	public void setInternalId(String newId) {
		this.compactId = newId;
	}

	public final String getInternalId() {
		if (this.compactId == null) {
			this.compactId = id.getCompactForm();
		}
		return this.compactId;
	}

}
