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

}
