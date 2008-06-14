package net.sf.taverna.t2.reference.impl;

import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;

import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.reference.T2ReferenceType;
import net.sf.taverna.t2.reference.h3.HibernateComponentClass;

/**
 * An implementation of T2Reference specific to the ReferenceSetImpl. This is
 * needed because ReferenceSetImpl uses a component based primary key driven
 * from the namespace and local parts of T2Reference. This in turn means we can
 * query hibernate directly with a T2Reference instance in the data access
 * object. Because this is only used as a component (i.e. a value type) we don't
 * need to define a hibernate mapping file for it.
 * 
 * @author Tom Oinn
 * 
 */
public class T2ReferenceImpl implements T2Reference, Serializable,
		HibernateComponentClass {

	private static final long serialVersionUID = 8363330461158750319L;
	private URI cachedUri = null;
	private String localPart;
	private String namespacePart;
	private boolean containsErrors = false;
	private T2ReferenceType referenceType = T2ReferenceType.ReferenceSet;
	private int depth = 0;

	/**
	 * Return whether the identified entity either is or contains errors
	 */
	public boolean containsErrors() {
		return this.containsErrors;
	}

	/**
	 * Property accessor for Hibernate, complies with java bean spec
	 */
	public boolean getContainsErrors() {
		return this.containsErrors;
	}

	/**
	 * Get the depth of the entity referred to by this reference
	 */
	public int getDepth() {
		return this.depth;
	}

	/**
	 * Get the local part of the URI for this reference
	 */
	public String getLocalPart() {
		return this.localPart;
	}

	/**
	 * Get the namespace part of the URI for this reference
	 */
	public String getNamespacePart() {
		return namespacePart;
	}

	/**
	 * Get the type of the entity to which this reference refers
	 */
	public T2ReferenceType getReferenceType() {
		return this.referenceType;
	}

	/**
	 * This method is only ever called from within Hibernate when
	 * re-constructing the identifier component to set the namespace part of the
	 * identifier.
	 */
	public synchronized void setNamespacePart(String namespacePart) {
		this.namespacePart = namespacePart;
		cachedUri = null;
	}

	/**
	 * This method is only ever called from within Hibernate when
	 * re-constructing the identifier component to set the local part of the
	 * identifier.
	 */
	public synchronized void setLocalPart(String localPart) {
		this.localPart = localPart;
		cachedUri = null;
	}

	/**
	 * This method is only ever called from within Hibernate when
	 * re-constructing the identifier component to set the depth of the
	 * identifier.
	 */
	public synchronized void setDepth(int depth) {
		this.depth = depth;
		cachedUri = null;
	}

	/**
	 * This method is only ever called from within Hibernate when
	 * re-constructing the identifier component to set the error property of the
	 * identifier.
	 */
	public synchronized void setContainsErrors(boolean containsErrors) {
		this.containsErrors = containsErrors;
		cachedUri = null;
	}

	/**
	 * This method is only ever called from within Hibernate when
	 * re-constructing the identifier component to set the reference type
	 * property of the identifier.
	 */
	public synchronized void setReferenceType(T2ReferenceType type) {
		this.referenceType = type;
		cachedUri = null;
	}

	/**
	 * By default when printing an identifier we use {@link #toUri()}.{@link java.net.URI#toASCIIString() toASCIIString()}
	 */
	@Override
	public String toString() {
		return toUri().toASCIIString();
	}

	/**
	 * Returns the identifier expressed as a {@link java.net.URI URI},
	 * constructed based on the reference type. For references to ReferenceSet
	 * this is
	 * <code>new URI("t2:ref//" + namespacePart + "?" + localPart)</code>
	 * leading to URIs of the form <code>t2:ref//namespace?local</code>
	 */
	public synchronized URI toUri() {
		if (cachedUri != null) {
			return cachedUri;
		} else if (referenceType.equals(T2ReferenceType.ReferenceSet)) {
			try {
				URI result = new URI("t2:ref//" + namespacePart + "?"
						+ localPart);
				cachedUri = result;
				return result;
			} catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			}
		} else if (referenceType.equals(T2ReferenceType.IdentifiedList)) {
			try {
				URI result = new URI("t2:list//" + namespacePart + "?"
						+ localPart + "/" + containsErrors + "/" + depth);
				cachedUri = result;
				return result;
			} catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			}
		} else {
			return null;
		}
	}

	/**
	 * Use the equality operator over the URI representation of this bean.
	 */
	@Override
	public boolean equals(Object other) {
		if (other == this) {
			return true;
		}
		if (other instanceof T2ReferenceImpl) {
			T2ReferenceImpl otherRef = (T2ReferenceImpl) other;
			return (toUri().equals(otherRef.toUri()));
		} else {
			return false;
		}
	}

	/**
	 * Use hashcode method from the URI representation of this bean
	 */
	@Override
	public int hashCode() {
		return toUri().hashCode();
	}

}
