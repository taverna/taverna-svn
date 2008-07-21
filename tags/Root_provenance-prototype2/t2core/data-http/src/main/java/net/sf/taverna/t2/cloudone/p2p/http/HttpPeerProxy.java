package net.sf.taverna.t2.cloudone.p2p.http;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import net.sf.taverna.t2.cloudone.datamanager.NotFoundException;
import net.sf.taverna.t2.cloudone.datamanager.RetrievalException;
import net.sf.taverna.t2.cloudone.entity.Entity;
import net.sf.taverna.t2.cloudone.identifier.EntityIdentifier;
import net.sf.taverna.t2.cloudone.peer.PeerProxy;
import net.sf.taverna.t2.util.beanable.Beanable;
import net.sf.taverna.t2.util.beanable.jaxb.BeanSerialiser;

public class HttpPeerProxy implements PeerProxy {

	private String baseUrl;

	private BeanSerialiser beanSerialiser = BeanSerialiser.getInstance();

	public HttpPeerProxy(String namespace) {
		String[] splitted = namespace.split("http2p_", 2);
		if (!(splitted.length == 2)) {
			throw new IllegalArgumentException("Unsupported namespace "
					+ namespace);
		}
		String[] host_port = splitted[1].split("_", 2);
		if (!(host_port.length == 2)) {
			throw new IllegalArgumentException("Unsupported namespace "
					+ namespace);
		}
		baseUrl = "http://" + host_port[0] + ":" + host_port[1] + "/";
	}

	public Entity<?, ?> export(EntityIdentifier identifier)
			throws NotFoundException, RetrievalException {
		URL url;
		try {
			url = new URL(baseUrl + identifier.getAsURI());
		} catch (MalformedURLException e) {
			throw new NotFoundException("Invalid URL from identifier "
					+ identifier, e);
		}

		Beanable<?> beanable;
		try {
			beanable = beanSerialiser.beanableFromXMLStream(url.openStream());
		} catch (IOException e) {
			throw new RetrievalException("Can't read " + url, e);
		}
		return (Entity<?, ?>) beanable;
	}

}
