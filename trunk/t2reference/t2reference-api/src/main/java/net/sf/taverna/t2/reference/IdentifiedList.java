package net.sf.taverna.t2.reference;

import java.util.List;

/**
 * An identified list is a list which is identified by a T2Reference. Lists are
 * immutable once named - if getId() returns a non null value all list methods
 * modifying the underlying list data will throw IllegalStateException. In the
 * reference management API this list sub-interface is used to represent both
 * collections of identifiers (i.e. 'raw' stored lists) and more fully resolved
 * structures where the types in the list can be reference sets, error documents
 * and other lists of such. The ListDao interface uses only the 'raw' form
 * consisting of flat lists of identifiers.
 * 
 * @author Tom Oinn
 * 
 * @param <T>
 */
public interface IdentifiedList<T> extends List<T> {

	/**
	 * The IdentifiedList has a unique T2Reference associated with it. If this
	 * is null the contents of the list may be modified, otherwise all
	 * modification operations throw IllegalStateException. Lists in T2, once
	 * named, are immutable.
	 */
	public T2Reference getId();

}
