package net.sf.taverna.t2.cloudone;

import java.util.List;
import java.util.Set;

/**
 * Interface defining methods used by the DataPeerContainer. This interface
 * should only ever be accessed from an instance of PeerContainer to handle a
 * message received by that container.
 * 
 * @author Tom Oinn
 * @author Matthew Pocock
 * 
 */
public interface DataPeer {

	/**
	 * Each DataPeer is associated with exactly one DataManager
	 */
	public DataManager getDataManager();
	
	/**
	 * Get the current namespace for this DataManager. New data documents and
	 * lists registered with this manager will have this namespace in their
	 * identifiers.
	 * 
	 * @return
	 */
	public String getCurrentNamespace();

	/**
	 * Get all namespaces for which this data manager is the authority. This
	 * will always include at least the namespace returned by the
	 * getCurrentNamespace method
	 * 
	 * @return
	 */
	public List<String> getManagedNamespaces();

	/**
	 * Return a potentially modified copy of the specified data document after
	 * ensuring that all reference schemes within the specified document are
	 * valid in the remote context
	 * 
	 * @param remoteContext
	 * @return
	 */
	public DataDocument exportDataDocument(
			Set<LocationalContext> remoteContext,
			DataDocumentIdentifier identifier) throws EntityNotFoundException;

	/**
	 * Get the document which describes the locational context for this
	 * DataManager instance.
	 */
	public Set<LocationalContext> getLocationalContexts();

}
