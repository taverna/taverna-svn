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

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import net.sf.taverna.t2.reference.DaoException;
import net.sf.taverna.t2.reference.ExternalReferenceSPI;
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
		return store.get(reference);		
	}

	public synchronized void store(ReferenceSet refSet) throws DaoException {
		store.put(refSet.getId(), refSet);
	}

	public synchronized void update(ReferenceSet refSet) throws DaoException {
		refSet.updateSummary();
		store.put(refSet.getId(), refSet);		
	}
	
	public synchronized boolean delete(ReferenceSet refSet) throws DaoException {
		return store.remove(refSet.getId())!=null;
	}

	public synchronized void deleteReferenceSetsForWFRun(String workflowRunId) throws DaoException {	
		for (T2Reference reference: store.keySet()){
			if (reference.getNamespacePart().equals(workflowRunId)){
				store.remove(reference);
			}
		}
	}

	public synchronized Set<T2Reference> getMutableIdentifiersForWorkflowRun(
			String workflowRunId) {
		Set<T2Reference> result = new HashSet<T2Reference>();
		for (T2Reference reference : store.keySet()) {
			if (reference.getNamespacePart().equals(workflowRunId)) {
				ReferenceSet rs = store.get(reference);
				if (rs.isAllMutable()) {
					result.add(reference);
				}
			}
		}
		return result;
	}

	public Set<T2Reference> getTidibleIdentifiersForWorkflowRun(
			String workflowRunId) {
		Set<T2Reference> result = new HashSet<T2Reference>();
		for (T2Reference reference : store.keySet()) {
			if (reference.getNamespacePart().equals(workflowRunId)) {
				ReferenceSet rs = store.get(reference);
				if (rs.isAnyDeletable() && !rs.isAllDeletable()) {
					result.add(reference);
				}
			}
		}
		return result;
	}
}
