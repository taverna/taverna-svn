package net.sf.taverna.t2.cloudone.p2p.http;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.io.IOUtils;

import net.sf.taverna.t2.cloudone.PeerProxy;
import net.sf.taverna.t2.cloudone.datamanager.NotFoundException;
import net.sf.taverna.t2.cloudone.entity.Entity;
import net.sf.taverna.t2.cloudone.identifier.EntityIdentifier;

public class HttpPeerProxy implements PeerProxy {

	private String namespace;
	private String baseUrl;

	public HttpPeerProxy(String namespace) {
		this.namespace = namespace;
		String[] splitted = namespace.split("http2p_", 2);
		if (! (splitted.length == 2)) {
			throw new IllegalArgumentException("Unsupported namespace " + namespace);
		}
		String[] host_port = splitted[1].split("_", 2);
		if (! (host_port.length == 2)) {
			throw new IllegalArgumentException("Unsupported namespace " + namespace);
		}
		baseUrl = "http://" + host_port[0] + ":" + host_port[1] + "/";
	}
	
	public Entity<?, ?> export(EntityIdentifier identifier)
			throws NotFoundException {
		URL url;
		try {
			url = new URL(baseUrl + identifier.getAsBean());
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		System.out.println(url);
		String result;
		try {
			result = IOUtils.toString(url.openStream());
			System.out.println(result);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		throw new NotFoundException(identifier);
		//TO-DO:
		//return url.openStream().beanable-stuff();
		
		
	}

}
