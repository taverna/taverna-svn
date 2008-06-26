package net.sf.taverna.t2.reference.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.sf.taverna.t2.reference.DaoException;
import net.sf.taverna.t2.reference.IdentifiedList;
import net.sf.taverna.t2.reference.ListDao;
import net.sf.taverna.t2.reference.T2Reference;

/**
 * A trivial in-memory implementation of ListDao for either testing or very
 * lightweight embedded systems. Uses a java Map as the backing store.
 * 
 * @author Tom Oinn
 * 
 */
public class InMemoryListDao implements ListDao {

	private Map<T2Reference, IdentifiedList<T2Reference>> store;

	public InMemoryListDao() {
		this.store = new ConcurrentHashMap<T2Reference, IdentifiedList<T2Reference>>();
	}

	public synchronized IdentifiedList<T2Reference> get(T2Reference reference)
			throws DaoException {
		if (store.containsKey(reference)) {
			return store.get(reference);
		} else {
			throw new DaoException("Key " + reference + " not found in store");
		}
	}

	public synchronized void store(IdentifiedList<T2Reference> theList) throws DaoException {
		store.put(theList.getId(), theList);
	}

}
