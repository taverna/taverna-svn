package net.sf.taverna.t2.reference;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Data access object handling NamedLists of T2Reference instances.
 * 
 * @author Tom Oinn
 */
public interface ListDao {

	/**
	 * Store a named and populated IdentifiedList of T2Reference to the
	 * database.
	 * 
	 * @param theList
	 *            list to store
	 * @throws DaoException
	 *             if any exception is thrown when connecting to the underlying
	 *             store or when storing the list
	 */
	@Transactional(propagation = Propagation.REQUIRED, readOnly = false)
	public void store(IdentifiedList<T2Reference> theList) throws DaoException;

	/**
	 * Retrieves a named and populated IdentifiedList of T2Reference from the
	 * database by T2Reference
	 * 
	 * @param reference
	 *            id of the list to retrieve
	 * @return a previously stored list of T2References
	 * @throws DaoException
	 *             if any exception is thrown when connecting to the underlying
	 *             data store or when attempting retrieval of the list
	 */
	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	public IdentifiedList<T2Reference> get(T2Reference reference)
			throws DaoException;

}
