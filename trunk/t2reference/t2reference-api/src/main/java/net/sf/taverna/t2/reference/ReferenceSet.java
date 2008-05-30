package net.sf.taverna.t2.reference;

import java.util.Set;

/**
 * A set of ExternalReferenceSPI instances, all of which point to the same (byte
 * equivalent) data. The set is identified by a T2Reference. This interface is
 * read-only, as are most of the interfaces in this package. Rather than
 * modifying properties of the reference set directly the client code should use
 * the reference manager functionality.
 * <p>
 * It is technically okay, but rather unhelpful, to have a ReferenceSet with no
 * ExternalReferenceSPI implementations. In general this is a sign that
 * something has gone wrong somewhere as the reference set will not be
 * resolvable in any way, but it would still retain its unique identifier so
 * there may be occasions where this is the desired behaviour.
 * 
 * @author Tom Oinn
 * 
 */
public interface ReferenceSet {

	/**
	 * ReferenceSet instances are identified by a unique T2Reference. This
	 * T2Reference is used to refer to the reference set within the workflow and
	 * other platforms.
	 * 
	 * @return the unique ID of this reference set in the form of a T2Reference
	 *         instance
	 */
	public T2Reference getId();

	/**
	 * The reference set contains a set of ExternalReferenceSPI instances, all
	 * of which point to byte equivalent data.
	 * 
	 * @return the set of references to external data
	 */
	public Set<ExternalReferenceSPI> getExternalReferences();

}
