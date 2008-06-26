package net.sf.taverna.t2.reference.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.sf.taverna.t2.reference.DaoException;
import net.sf.taverna.t2.reference.ReferenceSet;
import net.sf.taverna.t2.reference.ReferenceSetDao;
import net.sf.taverna.t2.reference.T2Reference;

/**
 * A trivial in-memory implementation of ReferenceSetDao for either testing or
 * very lightweight embedded systems. Uses a java Map as the backing store.
 * 
 * @author Tom Oinn
 * 
 */
public class InMemoryReferenceSetDao implements ReferenceSetDao {

	private Map<T2Reference, ReferenceSet> store;

	public InMemoryReferenceSetDao() {
		this.store = new ConcurrentHashMap<T2Reference, ReferenceSet>();
	}

	public synchronized ReferenceSet get(T2Reference reference)
			throws DaoException {
		if (store.containsKey(reference)) {
			return store.get(reference);
		} else {
			throw new DaoException("Key " + reference + " not found in store");
		}
	}

	public synchronized void store(ReferenceSet refSet) throws DaoException {
		store.put(refSet.getId(), refSet);
	}

	public synchronized void update(ReferenceSet refSet) throws DaoException {
		store.put(refSet.getId(), refSet);		
	}

}
