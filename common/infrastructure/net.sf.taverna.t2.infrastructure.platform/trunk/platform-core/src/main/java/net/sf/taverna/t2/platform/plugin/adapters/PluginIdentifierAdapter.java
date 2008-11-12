package net.sf.taverna.t2.platform.plugin.adapters;

import net.sf.taverna.t2.platform.plugin.PluginIdentifier;
import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * JAXB adapter class to map PluginIdentifier instances in and out of string
 * types. The artifact specifier must be in the form 'plugin:group:id:version'
 * in the XML document
 * 
 * @author Tom Oinn
 * 
 */
public class PluginIdentifierAdapter extends
		XmlAdapter<String, PluginIdentifier> {

	@Override
	public PluginIdentifier unmarshal(String v) throws Exception {
		return new PluginIdentifier(v);
	}

	@Override
	public String marshal(PluginIdentifier v) throws Exception {
		return v.toString();
	}

}
