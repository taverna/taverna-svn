package net.sf.taverna.t2.cloudone.p2p.http;

import net.sf.taverna.t2.cloudone.datamanager.NotFoundException;
import net.sf.taverna.t2.cloudone.peer.PeerContainer;
import net.sf.taverna.t2.cloudone.peer.PeerProxy;

/**
 * Gateway for access to peers over HTTP
 * 
 * @author Ian Dunlop
 * @author Stian Soiland
 * 
 */
public class HttpPeerContainer implements PeerContainer {

	public PeerProxy getProxyForNamespace(String namespace)
			throws NotFoundException {
		if (namespace.startsWith("http2p_")) {
			return new HttpPeerProxy(namespace);
		}
		throw new NotFoundException("No proxy found for " + namespace);
	}

	public boolean isConnected() {
		return true;
	}

}
