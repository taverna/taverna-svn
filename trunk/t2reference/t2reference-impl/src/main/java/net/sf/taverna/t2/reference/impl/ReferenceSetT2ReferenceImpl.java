package net.sf.taverna.t2.reference.impl;

import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;

import net.sf.taverna.t2.reference.T2Reference;

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
public class ReferenceSetT2ReferenceImpl implements T2Reference, Serializable {

	private static final long serialVersionUID = 8363330461158750319L;
	private URI cachedUri = null;
	private String localPart;
	private String namespacePart;

	/**
	 * Reference sets by definition do not contain errors, so this method always
	 * returns false
	 * 
	 * @return false
	 */
	public boolean containsErrors() {
		return false;
	}

	/**
	 * Reference sets by definition have a depth of 0
	 * 
	 * @return 0
	 */
	public int getDepth() {
		return 0;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getLocalPart() {
		return this.localPart;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getNamespacePart() {
		return namespacePart;
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
	 * By default when printing an identifier we use {@link #toUri()}.{@link java.net.URI#toASCIIString() toASCIIString()}
	 */
	@Override
	public String toString() {
		return toUri().toASCIIString();
	}

	/**
	 * Returns the identifier expressed as a {@link java.net.URI URI},
	 * constructed from
	 * <code>new URI("t2:ref//" + namespacePart + "?" + localPart)</code>
	 * leading to URIs of the form <code>t2:ref//namespace?local</code>
	 */
	public synchronized URI toUri() {
		if (cachedUri != null) {
			return cachedUri;
		} else {
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
		if (other instanceof ReferenceSetT2ReferenceImpl) {
			ReferenceSetT2ReferenceImpl otherRef = (ReferenceSetT2ReferenceImpl) other;
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
