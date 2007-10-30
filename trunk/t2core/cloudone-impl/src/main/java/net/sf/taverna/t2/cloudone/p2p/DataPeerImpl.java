package net.sf.taverna.t2.cloudone.p2p;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.sf.taverna.t2.cloudone.DataManager;
import net.sf.taverna.t2.cloudone.DataPeer;
import net.sf.taverna.t2.cloudone.LocationalContext;
import net.sf.taverna.t2.cloudone.ReferenceScheme;
import net.sf.taverna.t2.cloudone.datamanager.NotFoundException;
import net.sf.taverna.t2.cloudone.entity.DataDocument;
import net.sf.taverna.t2.cloudone.entity.impl.DataDocumentImpl;
import net.sf.taverna.t2.cloudone.identifier.DataDocumentIdentifier;

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

	public DataManager getDataManager() {
		return dataManager;
	}

	public Set<LocationalContext> getLocationalContexts() {
		return dataManager.getLocationalContexts();
	}

	public List<String> getManagedNamespaces() {
		return dataManager.getManagedNamespaces();
	}

}
