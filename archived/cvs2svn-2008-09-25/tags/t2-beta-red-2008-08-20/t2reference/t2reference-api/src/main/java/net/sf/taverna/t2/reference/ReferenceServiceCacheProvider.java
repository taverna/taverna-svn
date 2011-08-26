package net.sf.taverna.t2.reference;

/**
 * A simple interface to be implemented by data access object cache providers,
 * intended to be used to inject cache implementations through AoP
 * 
 * @author Tom Oinn
 * 
 */
public interface ReferenceServiceCacheProvider {

	/**
	 * Called after an Identified has been written to the backing store, either
	 * for the first time or after modification. In our model ReferenceSet is
	 * the only Identified that is modifiable, specifically only by the addition
	 * of ExternalReferenceSPI instances to its reference set.
	 * 
	 * @param i
	 *            the Identified written to the backing store
	 */
	void put(Identified i);

	/**
	 * Called before an attempt is made to retrieve an item from the backing
	 * store
	 * 
	 * @param id
	 *            the T2Reference of the item to retrieve
	 * @return a cached item with matching T2Reference, or null if the cache
	 *         does not contain that item
	 */
	Identified get(T2Reference id);

}
