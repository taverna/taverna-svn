package net.sf.taverna.t2.reference.impl;

import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.reference.T2ReferenceGenerator;
import net.sf.taverna.t2.reference.T2ReferenceType;

/**
 * An abstract class for implementing simple {@link T2ReferenceGenerator}s.
 * 
 * @author Stian Soiland-Reyes
 * 
 */
public abstract class AbstractT2ReferenceGenerator implements
		T2ReferenceGenerator {

	public AbstractT2ReferenceGenerator() {
		super();
	}

	/**
	 * {@inheritDoc}
	 */
	public synchronized T2Reference nextReferenceSetReference() {
		T2ReferenceImpl r = new T2ReferenceImpl();
		r.setNamespacePart(getNamespace());
		r.setLocalPart(getNextLocalPart());
		r.setReferenceType(T2ReferenceType.ReferenceSet);
		r.setDepth(0);
		r.setContainsErrors(false);
		return r;
	}

	/**
	 * Generate a new local part for a new {@link T2Reference reference}. The
	 * local part should be unique within this
	 * {@link T2ReferenceGenerator#getNamespace() namespace}.
	 * 
	 * @return A new, unique local part to identify a new reference.
	 */
	protected abstract String getNextLocalPart();

	/**
	 * {@inheritDoc}
	 */
	public T2Reference nextListReference(boolean containsErrors, int listDepth) {
		T2ReferenceImpl r = new T2ReferenceImpl();
		r.setNamespacePart(getNamespace());
		r.setLocalPart(getNextLocalPart());
		r.setReferenceType(T2ReferenceType.IdentifiedList);
		r.setDepth(listDepth);
		r.setContainsErrors(containsErrors);
		return r;
	}

	/**
	 * {@inheritDoc}
	 */
	public T2Reference nextErrorDocumentReference(int depth) {
		T2ReferenceImpl r = new T2ReferenceImpl();
		r.setNamespacePart(getNamespace());
		r.setLocalPart(getNextLocalPart());
		r.setReferenceType(T2ReferenceType.ErrorDocument);
		r.setDepth(depth);
		// This is an error document, it contains errors by definition
		r.setContainsErrors(true);
		return r;
	}

}