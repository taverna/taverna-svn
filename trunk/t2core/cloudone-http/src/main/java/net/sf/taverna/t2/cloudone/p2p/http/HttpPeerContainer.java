package net.sf.taverna.t2.cloudone.p2p.http;

import net.sf.taverna.t2.cloudone.PeerContainer;
import net.sf.taverna.t2.cloudone.PeerProxy;
import net.sf.taverna.t2.cloudone.datamanager.NotFoundException;

public class HttpPeerContainer implements PeerContainer {

	public PeerProxy getProxyForNamespace(String namespace) throws NotFoundException {
		if (namespace.startsWith("http2p_")) {
			return new HttpPeerProxy(namespace);
		}
		throw new NotFoundException("No proxy found for " + namespace);
	}

	public boolean isConnected() {
		return true;
	}

}
