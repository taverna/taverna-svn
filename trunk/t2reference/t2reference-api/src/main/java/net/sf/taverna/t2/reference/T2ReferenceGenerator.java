package net.sf.taverna.t2.reference;

/**
 * Provides new unique T2Reference instances. Used by and injected into the
 * various service interface implementations when registering new reference
 * sets, error documents and lists.
 * 
 * @author Tom Oinn
 * 
 */
public interface T2ReferenceGenerator {

	/**
	 * All T2Reference objects will have this namespace
	 * 
	 * @return the namespace as a string
	 */
	public String getNamespace();

	/**
	 * Create a new and otherwise unused T2Reference to a ReferenceSet
	 * 
	 * @return new T2Reference for a ReferenceSet, namespace and local parts
	 *         will be initialized and the reference is ready to use when
	 *         returned.
	 */
	public T2Reference nextReferenceSetReference();

	/**
	 * Create a new and otherwise unused T2Reference to an IdentifiedList
	 * 
	 * @param containsErrors
	 *            whether the list this reference is generated for contains
	 *            t2references with their containsErrors property set to true.
	 *            Returns true if <em>any</em> reference in the list is or
	 *            contains an error.
	 * @param listDepth
	 *            depth of the list to which this identifier will be applied
	 * @return a new T2Reference for an IdentifiedList. Namespace, type and
	 *         local parts will be initialized but depth and error content will
	 *         still be at their default values of '0' and 'false' respectively,
	 *         these will need to be re-set before the reference is viable.
	 */
	public T2Reference nextListReference(boolean containsErrors, int listDepth);

}
