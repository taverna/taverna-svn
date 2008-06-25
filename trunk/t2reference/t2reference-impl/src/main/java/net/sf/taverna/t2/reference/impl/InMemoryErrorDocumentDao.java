package net.sf.taverna.t2.reference.impl;

import java.util.HashMap;
import java.util.Map;

import net.sf.taverna.t2.reference.DaoException;
import net.sf.taverna.t2.reference.ErrorDocument;
import net.sf.taverna.t2.reference.ErrorDocumentDao;
import net.sf.taverna.t2.reference.T2Reference;

/**
 * A trivial in-memory implementation of ErrorDocumentDao for either testing or
 * very lightweight embedded systems. Uses a java Map as the backing store.
 * 
 * @author Tom Oinn
 * 
 */
public class InMemoryErrorDocumentDao implements ErrorDocumentDao {

	private Map<T2Reference, ErrorDocument> store;

	public InMemoryErrorDocumentDao() {
		this.store = new HashMap<T2Reference, ErrorDocument>();
	}

	public synchronized ErrorDocument get(T2Reference reference)
			throws DaoException {
		if (store.containsKey(reference)) {
			return store.get(reference);
		} else {
			throw new DaoException("Key " + reference + " not found in store");
		}
	}

	public synchronized void store(ErrorDocument theDoc) throws DaoException {
		store.put(theDoc.getId(), theDoc);
	}

}
