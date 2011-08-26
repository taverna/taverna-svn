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
package net.sf.taverna.t2.cloudone.datamanager;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import net.sf.taverna.t2.cloudone.entity.Entity;
import net.sf.taverna.t2.cloudone.identifier.ContextualizedIdentifier;
import net.sf.taverna.t2.cloudone.identifier.DataDocumentIdentifier;
import net.sf.taverna.t2.cloudone.identifier.EntityIdentifier;
import net.sf.taverna.t2.cloudone.identifier.EntityListIdentifier;
import net.sf.taverna.t2.cloudone.identifier.ErrorDocumentIdentifier;
import net.sf.taverna.t2.cloudone.peer.LocationalContext;
import net.sf.taverna.t2.cloudone.peer.PeerContainer;
import net.sf.taverna.t2.cloudone.peer.PeerProxy;
import net.sf.taverna.t2.cloudone.refscheme.ReferenceScheme;

/**
 * A peer data manager, wrapping another "backend" {@link DataManager} to do
 * {@link #getEntity(EntityIdentifier)} resolution using a list of
 * {@link PeerContainer}s.
 * <p>
 * {@link PeerContainer}s are added using {@link #addPeer(PeerContainer)} and
 * removed using {@link #removePeer(PeerContainer)}.
 * {@link #getEntity(EntityIdentifier)} will first attempt lookup with the local
 * data manager before attempting any of the peers. All register*() methods
 * delegate directly to the local datamanager.
 * 
 * @author Ian Dunlop
 * @author Stian Soiland
 * 
 */
public class PeerDataManager implements DataManager {

	DataManager dataManager;
	/*
	 * The other peers that this one knows about
	 */
	List<PeerContainer> peers = new ArrayList<PeerContainer>();

	/**
	 * Construct a PeerDataManager delegating writes to the given
	 * {@link DataManager}.
	 * 
	 * @param dManager
	 *            Delegated {@link DataManager}.
	 */
	public PeerDataManager(DataManager dManager) {
		dataManager = dManager;
	}

	/**
	 * {@inheritDoc}
	 */
	public BlobStore getBlobStore() {
		return dataManager.getBlobStore();
	}

	/**
	 * {@inheritDoc}
	 */
	public String getCurrentNamespace() {
		return dataManager.getCurrentNamespace();
	}

	/**
	 * Get entity from delegated {@link DataManager}, or on failure, get entity
	 * from list of {@link PeerContainer}s registered using
	 * {@link #addPeer(PeerContainer)}
	 * 
	 * @param identifier
	 *            Identifier of entity to retrieve
	 * @return Retrieved {@link Entity}
	 * @throws NotFoundException
	 *             If all known {@link PeerContainer}s threw
	 *             {@link NotFoundException}
	 * @throws RetrievalException
	 *             If the local datamanager threw a {@link RetrievalException}.
	 * 
	 */
	@SuppressWarnings("unchecked")
	public <EI extends EntityIdentifier> Entity<EI, ?> getEntity(EI identifier)
			throws NotFoundException, RetrievalException {

		try {
			// might throw RetrievalException on internal errors, which we
			// should fail early on
			return dataManager.getEntity(identifier);
		} catch (NotFoundException e) {
		}

		for (PeerContainer peer : peers) {
			if (!peer.isConnected()) {
				continue;
			}

			PeerProxy proxy;
			try {
				proxy = peer.getProxyForNamespace(identifier.getNamespace());
			} catch (NotFoundException e) {
				continue;
			}

			try {
				Entity<EI, ?> entity = (Entity<EI, ?>) proxy.export(identifier);
				// if (replicate)
				// dataManager.replicate(entity);
				if (entity != null) {
					return entity;
				}
			} catch (NotFoundException e) {
				continue;
			}
		}
		throw new NotFoundException(identifier);
	}

	/**
	 * {@inheritDoc}
	 */
	public Set<LocationalContext> getLocationalContexts() {
		return dataManager.getLocationalContexts();
	}

	/**
	 * {@inheritDoc}
	 */
	public List<String> getManagedNamespaces() {
		return dataManager.getManagedNamespaces();
	}

	/**
	 * {@inheritDoc}
	 */
	public int getMaxIDLength() {
		return dataManager.getMaxIDLength();
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public DataDocumentIdentifier registerDocument(
			ReferenceScheme... references) throws StorageException {
		return dataManager.registerDocument(references);
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public DataDocumentIdentifier registerDocument(
			Set<ReferenceScheme> references) throws StorageException {
		return dataManager.registerDocument(references);
	}

	/**
	 * {@inheritDoc}
	 */
	public EntityListIdentifier registerEmptyList(int depth)
			throws StorageException {
		return dataManager.registerEmptyList(depth);
	}

	/**
	 * {@inheritDoc}
	 */
	public ErrorDocumentIdentifier registerError(int depth, int implicitDepth,
			String msg, Throwable throwable) throws StorageException {
		return dataManager.registerError(depth, implicitDepth, msg, throwable);
	}

	/**
	 * {@inheritDoc}
	 */
	public ErrorDocumentIdentifier registerError(int depth, int implicitDepth,
			String msg) throws StorageException {
		return dataManager.registerError(depth, implicitDepth, msg);
	}

	/**
	 * {@inheritDoc}
	 */
	public ErrorDocumentIdentifier registerError(int depth, int implicitDepth,
			Throwable throwable) throws StorageException {
		return dataManager.registerError(depth, implicitDepth, throwable);
	}

	/**
	 * {@inheritDoc}
	 */
	public EntityListIdentifier registerList(EntityIdentifier... identifiers)
			throws StorageException {
		return dataManager.registerList(identifiers);
	}

	/**
	 * {@inheritDoc}
	 */
	public EntityListIdentifier registerList(List<EntityIdentifier> identifiers)
			throws StorageException {
		return dataManager.registerList(identifiers);
	}

	/**
	 * {@inheritDoc}
	 */
	public Iterator<ContextualizedIdentifier> traverse(
			EntityIdentifier identifier, int desiredDepth)
			throws RetrievalException {
		return dataManager.traverse(identifier, desiredDepth);
	}

	/**
	 * Add a {@link PeerContainer} to be used in resolution from
	 * {@link #getEntity(EntityIdentifier)}.
	 * 
	 * @param peer
	 *            {@link PeerContainer} to add
	 */
	public void addPeer(PeerContainer peer) {
		if (peers.contains(peer)) {
			throw new IllegalArgumentException("Peer was already registered");
		}
		peers.add(peer);
	}

	/**
	 * Remove a {@link PeerContainer} previously added using
	 * {@link #addPeer(PeerContainer)}.
	 * 
	 * @param peer
	 *            {@link PeerContainer} to remove
	 */
	public void removePeer(PeerContainer peer) {
		if (!peers.contains(peer)) {
			throw new IllegalArgumentException(
					"Peer was not registered so cannot be removed");
		}
		peers.remove(peer);
	}

}
