package net.sf.taverna.t2.cloudone.datamanager;

import java.util.List;
import java.util.Set;

import net.sf.taverna.t2.cloudone.DataManager;
import net.sf.taverna.t2.cloudone.DataPeer;
import net.sf.taverna.t2.cloudone.LocationalContext;
import net.sf.taverna.t2.cloudone.entity.DataDocument;
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
	
	public DataDocument exportDataDocument(
			Set<LocationalContext> remoteContext,
			DataDocumentIdentifier identifier) throws NotFoundException {
		// TODO Auto-generated method stub
		return null;
	}

	public String getCurrentNamespace() {
		return this.dataManager.getCurrentNamespace();
	}

	public DataManager getDataManager() {
		return this.dataManager;
	}

	public Set<LocationalContext> getLocationalContexts() {
		return this.dataManager.getLocationalContexts();
	}

	public List<String> getManagedNamespaces() {
		return this.dataManager.getManagedNamespaces();
	}

}
