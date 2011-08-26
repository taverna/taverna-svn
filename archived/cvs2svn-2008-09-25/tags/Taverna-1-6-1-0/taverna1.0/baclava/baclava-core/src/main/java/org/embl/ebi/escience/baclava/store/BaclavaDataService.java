/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.baclava.store;

import org.embl.ebi.escience.baclava.DataThing;

/**
 * Defines the API that a concrete backing store must implement. Note that
 * DataThing objects must be created on the fly in response to LSID based
 * requests, and that there is no way to retrieve any data other than by LSID.
 * This is entirely intentional, we rely on the attached metadata for each
 * DataThing in order to store it correctly. Implementations of this class must
 * therefore be used with an implementation of the LSIDProvider interface in
 * order to ensure that all values in the DataThing have been allocated
 * identifiers. As with most things, if you can't talk about it it might as well
 * not exist.
 * 
 * @author Tom Oinn
 */
public interface BaclavaDataService {

	/**
	 * Store a DataThing in the backing store, this must introspect on the
	 * DataThing object, determine any LSID values contained within and maintain
	 * appropriate references to such values.
	 * 
	 * @param theDataThing
	 *            DataThing object to store
	 * @param silent
	 *            whether to throw exceptions if duplicate LSID values are
	 *            found. If set to false then the storage operation will fail
	 *            under this condition, if set to true then the store will
	 *            always succeed, but duplicate LSID values will not be stored.
	 *            This is potentially useful when a DataThing is a composition
	 *            of some previously stored entities and some new ones,
	 *            effectively setting silent to true allows these new data to be
	 *            stored and the duplicates safely ignored.
	 * @throws DuplicateLSIDException
	 *             if an LSID in the DataThing is already held in the store and
	 *             the silent parameter is set to false
	 */
	public void storeDataThing(DataThing theDataThing, boolean silent)
			throws DuplicateLSIDException;

	/**
	 * Return a DataThing object from the store, extracting the child if this is
	 * not already a top level DataThing document. This means that the DataThing
	 * returned may or may not have a container LSID, the dataObject contained
	 * within it will always have the LSID supplied to the call.
	 * 
	 * @param LSID
	 *            a URI referencing an object within a previously stored
	 *            DataThing contained by the backing store.
	 * @return the DataThing object constructed from the data within the
	 *         specified LSID.
	 * @throws NoSuchLSIDException
	 *             if the LSID is not found within the store.
	 */
	public DataThing fetchDataThing(String LSID) throws NoSuchLSIDException;

	/**
	 * Whether the given LSID is resolvable to a concrete data object
	 */
	public boolean hasData(String LSID);

	/**
	 * Whether the given LSID is resolvable to a metadata reference
	 */
	public boolean hasMetadata(String LSID);

	/**
	 * Store a string containing RDF format metadata and one or more LSIDs as
	 * resources
	 */
	public void storeMetadata(String theMetadata);

	/**
	 * Returns a string of all the metadata statements that reference the given
	 * LSID
	 */
	public String getMetadata(String LSID);

}
