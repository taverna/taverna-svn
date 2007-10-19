package net.sf.taverna.t2.cloudone.datamanager;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import net.sf.taverna.t2.cloudone.BlobStore;
import net.sf.taverna.t2.cloudone.DataManager;
import net.sf.taverna.t2.cloudone.LocationalContext;
import net.sf.taverna.t2.cloudone.PeerContainer;
import net.sf.taverna.t2.cloudone.PeerProxy;
import net.sf.taverna.t2.cloudone.ReferenceScheme;
import net.sf.taverna.t2.cloudone.entity.Entity;
import net.sf.taverna.t2.cloudone.identifier.ContextualizedIdentifier;
import net.sf.taverna.t2.cloudone.identifier.DataDocumentIdentifier;
import net.sf.taverna.t2.cloudone.identifier.EntityIdentifier;
import net.sf.taverna.t2.cloudone.identifier.EntityListIdentifier;
import net.sf.taverna.t2.cloudone.identifier.ErrorDocumentIdentifier;

public class MegaDataManager implements DataManager {

	DataManager dataManager;

	List<PeerContainer> peers = new ArrayList<PeerContainer>();

	public MegaDataManager(DataManager dManager) {
		dataManager = dManager;
	}

	public BlobStore getBlobStore() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getCurrentNamespace() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Get entity from list of datamanagers registered using
	 * {@link #addDataManager(DataManager)}.
	 * 
	 * @param identifier
	 *            Identifier of entity to retrieve
	 * @return Retrieved {@link Entity}
	 * @throws NotFoundException
	 *             If all known datamanagers threw {@link NotFoundException} or
	 *             didn't manage the identifier's namespace.
	 * @throws RetrievalException
	 * 
	 */
	@SuppressWarnings("unchecked")
	public <EI extends EntityIdentifier> Entity<EI, ?> getEntity(EI identifier)
			throws NotFoundException, RetrievalException {

		if (dataManager.getManagedNamespaces().contains(
				identifier.getNamespace())) {
			try {
				// might throw RetrievalException on internal errors, which we
				// should fail early on
				return dataManager.getEntity(identifier);
			} catch (NotFoundException e) {
			}
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

	public Set<LocationalContext> getLocationalContexts() {
		// TODO Auto-generated method stub
		return null;
	}

	public List<String> getManagedNamespaces() {
		// TODO Auto-generated method stub
		return null;
	}

	public int getMaxIDLength() {
		// TODO Auto-generated method stub
		return 0;
	}

	public DataDocumentIdentifier registerDocument(
			ReferenceScheme... references) throws StorageException {
		// TODO Auto-generated method stub
		return null;
	}

	public DataDocumentIdentifier registerDocument(
			Set<ReferenceScheme> references) throws StorageException {
		// TODO Auto-generated method stub
		return null;
	}

	public EntityListIdentifier registerEmptyList(int depth)
			throws StorageException {
		// TODO Auto-generated method stub
		return null;
	}

	public ErrorDocumentIdentifier registerError(int depth, int implicitDepth,
			String msg) throws StorageException {
		// TODO Auto-generated method stub
		return null;
	}

	public ErrorDocumentIdentifier registerError(int depth, int implicitDepth,
			String msg, Throwable throwable) throws StorageException {
		// TODO Auto-generated method stub
		return null;
	}

	public ErrorDocumentIdentifier registerError(int depth, int implicitDepth,
			Throwable throwable) throws StorageException {
		// TODO Auto-generated method stub
		return null;
	}

	public EntityListIdentifier registerList(EntityIdentifier[] identifiers)
			throws StorageException {
		// TODO Auto-generated method stub
		return null;
	}

	public Iterator<ContextualizedIdentifier> traverse(
			EntityIdentifier identifier, int desiredDepth)
			throws RetrievalException {
		// TODO Auto-generated method stub
		return null;
	}

	public void addPeer(PeerContainer peer) {
		if (peers.contains(peer)) {
			throw new IllegalArgumentException("Peer was already registered");
		}
		peers.add(peer);
	}

	public void removePeer(PeerContainer peer) {
		if (!peers.contains(peer)) {
			throw new IllegalArgumentException(
					"Peer was not registered so cannot be removed");
		}
		peers.remove(peer);
	}

}
