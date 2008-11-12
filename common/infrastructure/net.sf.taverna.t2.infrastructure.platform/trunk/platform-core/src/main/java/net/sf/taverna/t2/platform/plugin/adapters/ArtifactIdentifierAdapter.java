package net.sf.taverna.t2.platform.plugin.adapters;

import net.sf.taverna.t2.platform.pom.ArtifactIdentifier;
import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * JAXB adapter class to map ArtifactIdentifier instances in and out of string
 * types. The artifact specifier must be in the form 'group:artifact:version' in
 * the XML document
 * 
 * @author Tom Oinn
 * 
 */
public class ArtifactIdentifierAdapter extends
		XmlAdapter<String, ArtifactIdentifier> {

	@Override
	public ArtifactIdentifier unmarshal(String v) throws Exception {
		return new ArtifactIdentifier(v);
	}

	@Override
	public String marshal(ArtifactIdentifier v) throws Exception {
		return v.toString();
	}

}
