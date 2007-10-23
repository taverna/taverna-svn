package net.sf.taverna.t2.cloudone.p2p.http;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import net.sf.taverna.t2.cloudone.PeerProxy;
import net.sf.taverna.t2.cloudone.bean.Beanable;
import net.sf.taverna.t2.cloudone.datamanager.NotFoundException;
import net.sf.taverna.t2.cloudone.entity.Entity;
import net.sf.taverna.t2.cloudone.identifier.EntityIdentifier;
import net.sf.taverna.t2.cloudone.util.BeanSerialiser;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

public class HttpPeerProxy implements PeerProxy {

	private static Logger logger = Logger.getLogger(HttpPeerProxy.class);

	private String namespace;
	private String baseUrl;

	public HttpPeerProxy(String namespace) {
		this.namespace = namespace;
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
			throws NotFoundException {
		URL url;
		try {
			url = new URL(baseUrl + identifier.getAsBean());
		} catch (MalformedURLException e) {
			throw new NotFoundException("Invalid URL from identifier "
					+ identifier, e);
		}
		SAXBuilder builder = new SAXBuilder();
		Document doc;
		try {
			doc = builder.build(url);
		} catch (JDOMException e) {
			logger.warn("Could not parse beanable from " + url, e);
			throw new NotFoundException(identifier);
		} catch (IOException e) {
			logger.warn("Could not read beanable from " + url, e);
			throw new NotFoundException(identifier);
		}
		Element beanableElem = doc.getRootElement();
		Beanable<?> beanable = BeanSerialiser.beanableFromXML(beanableElem);
		return (Entity<?, ?>) beanable;
	}

}
