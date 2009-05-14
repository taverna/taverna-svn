package net.sf.taverna.t2.platform.plugin.adapters;

import java.net.URL;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * JAXB adapter class to map java.net.URL instances to and from strings
 * 
 * @author Tom Oinn
 * 
 */
public class URLAdapter extends XmlAdapter<String, URL> {

	@Override
	public String marshal(URL v) throws Exception {
		return v.toExternalForm();
	}

	@Override
	public URL unmarshal(String v) throws Exception {
		return new URL(v);
	}

}
