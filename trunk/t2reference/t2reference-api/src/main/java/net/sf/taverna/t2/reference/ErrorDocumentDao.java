package net.sf.taverna.t2.reference;

/**
 * Data access object handling ErrorDocument instances.
 * 
 * @author Tom Oinn
 */
public interface ErrorDocumentDao {

	/**
	 * Store a named ErrorDocument to the database.
	 * 
	 * @param errorDoc
	 *            error document to store
	 * @throws DaoException
	 *             if any exception is thrown when connecting to the underlying
	 *             store or when storing the error document
	 */
	public void store(ErrorDocument errorDoc) throws DaoException;

	/**
	 * Retrieves a named and populated ErrorDocument
	 * 
	 * @param reference
	 *            id of the error document to retrieve
	 * @return a previously stored ErrorDocument instance
	 * @throws DaoException
	 *             if any exception is thrown when connecting to the underlying
	 *             data store or when attempting retrieval of the error document
	 */
	public ErrorDocument get(T2Reference reference) throws DaoException;

}
