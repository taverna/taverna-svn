package net.sf.taverna.t2.platform.plugin.adapters;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * JAXB adapter class to map java.lang.Class instances to and from strings
 * 
 * @author Tom Oinn
 * 
 */
public class ClassAdapter extends XmlAdapter<String, Class<?>> {

	@Override
	public Class<?> unmarshal(String v) throws Exception {
		return Class.forName(v);
	}

	@Override
	public String marshal(Class<?> v) throws Exception {
		return v.getCanonicalName();
	}

}
