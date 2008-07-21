package net.sf.taverna.t2.cloudone.peer;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.sf.taverna.t2.cloudone.datamanager.DataManager;
import net.sf.taverna.t2.cloudone.datamanager.NotFoundException;
import net.sf.taverna.t2.cloudone.entity.DataDocument;
import net.sf.taverna.t2.cloudone.entity.Entity;
import net.sf.taverna.t2.cloudone.entity.impl.DataDocumentImpl;
import net.sf.taverna.t2.cloudone.identifier.DataDocumentIdentifier;
import net.sf.taverna.t2.cloudone.refscheme.ReferenceScheme;

/**
 * Simple implementation of a {@link DataPeer} designed to allow tests of
 * {@link LocationalContext}. No actual peering is carried out in this
 * implementation
 * 
 * @author Ian Dunlop
 * 
 */
public class DataPeerImpl implements DataPeer {

	private DataManager dataManager;

	public DataPeerImpl(DataManager dataManager) {
		this.dataManager = dataManager;
	}

	/**
	 * Export the {@link ReferenceScheme}s which are valid in the other
	 * {@link DataPeer}s {@link LocationalContext}s
	 */
	@SuppressWarnings("unchecked")
	public DataDocument exportDataDocument(
			Set<LocationalContext> remoteContext,
			DataDocumentIdentifier identifier) throws NotFoundException {
		Set<ReferenceScheme> exportedRefs = new HashSet<ReferenceScheme>();
		DataDocument entity = (DataDocument) dataManager.getEntity(identifier);
		for (ReferenceScheme<?> ref : entity.getReferenceSchemes()) {
			if (ref.validInContext(remoteContext, this)) {
				exportedRefs.add(ref);
			}
		}
		if (exportedRefs.isEmpty()) {
			// TODO: Translate reference
			/*
			 * DataDocument does permit 0 "or more" ReferenceScheme's, although
			 * this is probably not very useful..
			 */
		}
		DataDocumentImpl exportedDoc = new DataDocumentImpl(identifier,
				exportedRefs);
		return exportedDoc;
	}

	public String getCurrentNamespace() {
		return dataManager.getCurrentNamespace();
	}

	/**
	 * The {@link DataManager} handling storage of {@link Entity}s for this
	 * {@link DataPeer}
	 */
	public DataManager getDataManager() {
		return dataManager;
	}

	/**
	 * The contexts where this {@link DataPeer}s {@link DataManager} can handle
	 * {@link Entity}s from
	 */
	public Set<LocationalContext> getLocationalContexts() {
		return dataManager.getLocationalContexts();
	}

	public List<String> getManagedNamespaces() {
		return dataManager.getManagedNamespaces();
	}

}
