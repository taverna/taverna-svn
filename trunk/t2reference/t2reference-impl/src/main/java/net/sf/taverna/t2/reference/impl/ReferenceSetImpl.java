package net.sf.taverna.t2.reference.impl;

import java.util.Set;

import net.sf.taverna.t2.reference.ExternalReferenceSPI;
import net.sf.taverna.t2.reference.ReferenceSet;
import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.reference.h3.HibernateMappedEntity;

/**
 * An implementation of ReferenceSet with the additional methods and metadata
 * required by Hibernate3 to allow it to be persisted in a relational store. As
 * with everything else in this package you shouldn't be using this class
 * directly! Instead of this class you should use the registration methods on
 * {@link net.sf.taverna.t2.reference.ReferenceSetService}, implementations of
 * that interface will handle the construction of ReferenceSet implementations
 * (including this one).
 * 
 * @author Tom Oinn
 * 
 */
public class ReferenceSetImpl implements ReferenceSet, HibernateMappedEntity {

	private Set<ExternalReferenceSPI> externalReferences;
	private T2ReferenceImpl id;

	/**
	 * Construct a new ReferenceSetImpl with the given set of external
	 * references and identifier.
	 * 
	 * @param references
	 *            the set of ExternalReferenceSPI which this reference set
	 *            should contain initially
	 * @param id
	 *            the T2Reference to use, must be an instance of
	 *            ReferenceSetT2ReferenceImpl so hibernate can make use of it as
	 *            a compound primary key component
	 */
	public ReferenceSetImpl(Set<ExternalReferenceSPI> references,
			T2ReferenceImpl id) {
		this.id = id;
		this.externalReferences = references;
	}

	/**
	 * Default constructor, used by Hibernate when reconstructing this bean from
	 * the database. If you call this directly from your code you must then call
	 * both {@link #setExternalReferences(Set)} and
	 * {@link #setId(T2ReferenceImpl)} before any use of the
	 * reference set. If you're not writing the reference manager implementation
	 * you shouldn't be using this class anyway.
	 */
	public ReferenceSetImpl() {
		//
	}

	/**
	 * For debugging purposes, prints a summary of the contents and identifier
	 * of this reference set.
	 * 
	 * @return human readable string representation of this object. This is not
	 *         regarded as 'stable' and should not be parsed for any reason!
	 */
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(id + " [" + externalReferences.size() + "]\n");

		for (ExternalReferenceSPI ref : externalReferences) {
			sb.append("  " + ref.toString() + "\n");
		}
		return sb.toString();

	}

	/**
	 * {@inheritDoc}
	 */
	public Set<ExternalReferenceSPI> getExternalReferences() {
		return this.externalReferences;
	}

	/**
	 * {@inheritDoc}
	 */
	public T2Reference getId() {
		return this.id;
	}

	/**
	 * This method is only ever called from within Hibernate, and is used to
	 * initialize the set of external references.
	 */
	public void setExternalReferences(Set<ExternalReferenceSPI> newReferences) {
		this.externalReferences = newReferences;
	}

	/**
	 * This method is only ever called from within Hibernate, and is used to
	 * initialize the unique ID of this reference set.
	 * 
	 * @param newId
	 *            an instance of ReferenceSetT2ReferenceImpl created from the
	 *            database
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

}
