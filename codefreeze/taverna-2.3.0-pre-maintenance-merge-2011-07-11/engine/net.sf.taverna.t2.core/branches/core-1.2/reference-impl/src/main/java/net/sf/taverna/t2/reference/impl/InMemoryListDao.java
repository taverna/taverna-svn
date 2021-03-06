/*******************************************************************************
 * Copyright (C) 2007 The University of Manchester   
 * 
 *  Modifications to the initial code base are copyright of their
 *  respective authors, or their employers as appropriate.
 * 
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2.1 of
 *  the License, or (at your option) any later version.
 *    
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *    
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 ******************************************************************************/
package net.sf.taverna.t2.reference.impl;

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
		return store.get(reference);		
	}

	public synchronized void store(IdentifiedList<T2Reference> theList) throws DaoException {
		store.put(theList.getId(), theList);
	}

	public boolean delete(
			IdentifiedList<T2Reference> theList) throws DaoException {
		return (store.remove(theList.getId())!=null);
	}

	public synchronized void deleteIdentifiedListsForWFRun(String workflowRunId) throws DaoException {
		for (T2Reference reference: store.keySet()){
			if (reference.getNamespacePart().equals(workflowRunId)){
				store.remove(reference);
			}
		}
	}
}
