package net.sf.taverna.t2.platform.util.download.impl;

import java.net.URL;

import javax.xml.bind.JAXBException;

import static net.sf.taverna.t2.platform.plugin.impl.PluginDescriptionXMLHandler.*;
import net.sf.taverna.t2.platform.plugin.generated.PluginDescription;
import junit.framework.TestCase;

public class JAXBGeneratedPluginMappingsTest extends TestCase {

	public void testInit() throws JAXBException {
		try {
			getDescription((URL)null);
		} catch (IllegalArgumentException iae) {
			// Fine - this means we got past the init and on to the request to
			// unmarshall a null URL
		}
	}

	public void testMarshall() {
		PluginDescription p = createDescription();
		try {
			writeDocument(p, System.out);
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
