package net.sf.taverna.t2.platform.plugin.adapters;

import java.net.URI;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * JAXB adapter class to map java.net.URI instances to and from strings
 * 
 * @author Tom Oinn
 * 
 */
public class URIAdapter extends XmlAdapter<String, URI> {

	@Override
	public String marshal(URI v) throws Exception {
		return v.toString();
	}

	@Override
	public URI unmarshal(String v) throws Exception {
		return URI.create(v);
	}

}
