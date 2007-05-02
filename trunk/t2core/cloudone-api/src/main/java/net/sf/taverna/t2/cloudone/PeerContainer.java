package net.sf.taverna.t2.cloudone;

/**
 * Contains a single data peer and provides that peer with network awareness and
 * communication facilities. The container is responsible for discovery of other
 * containers (and their peers implicitly) and the creation of peer proxy
 * objects to be used for peer to peer communication in the form of data export
 * and namespace migration.
 * 
 * TODO - interface needs to be written
 * 
 * @author Tom Oinn
 * @author Matthew Pocock
 * 
 */
public interface PeerContainer {

	/**
	 * Discovery request, called by implementation of DataPeer on its enclosing
	 * container when trying to resolve an entity identifier in a namespace not
	 * managed by the data peer's data manager.
	 * 
	 * @param namespace
	 * @return
	 */
	public PeerProxy getProxyForNamespace(String namespace);

	/**
	 * Is the peer container connected to the network fabric? If not then
	 * requests for proxy by namespace will only ever return the proxy for the
	 * data peer hosted within the container (but will still return)
	 */
	public boolean isConnected();

}
