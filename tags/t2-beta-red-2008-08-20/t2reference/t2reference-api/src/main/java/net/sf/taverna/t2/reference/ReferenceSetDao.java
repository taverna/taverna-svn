package net.sf.taverna.t2.reference;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Data Access Object interface for {@link ReferenceSet}. Used by the
 * {@link ReferenceSetService} to store and retrieve implementations of
 * reference set to and from the database. Client code should use the reference
 * set service rather than using this Dao directly.
 * <p>
 * All methods throw DaoException, and nothing else. Where a deeper error is
 * propagated it is wrapped in a DaoException and passed on to the caller.
 * 
 * @author Tom Oinn
 * 
 */
public interface ReferenceSetDao {

	/**
	 * Store the specified new reference set
	 * 
	 * @param rs
	 *            a reference set, must not already exist in the database.
	 * @throws DaoException
	 *             if the entry already exists in the database or some other
	 *             database related problem occurs
	 */
	@Transactional(propagation = Propagation.REQUIRED, readOnly = false)
	public void store(ReferenceSet rs) throws DaoException;

	/**
	 * Update a pre-existing entry in the database
	 * 
	 * @param rs
	 *            the reference set to update. This must already exist in the
	 *            database
	 * @throws DaoException
	 */
	@Transactional(propagation = Propagation.REQUIRED, readOnly = false)
	public void update(ReferenceSet rs) throws DaoException;

	/**
	 * Fetch a reference set by id
	 * 
	 * @param ref
	 *            the T2Reference to fetch
	 * @return a retrieved ReferenceSet
	 * @throws DaoException
	 *             if the supplied reference is of the wrong type or if
	 *             something goes wrong fetching the data or connecting to the
	 *             database
	 */
	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	public ReferenceSet get(T2Reference ref) throws DaoException;
}
