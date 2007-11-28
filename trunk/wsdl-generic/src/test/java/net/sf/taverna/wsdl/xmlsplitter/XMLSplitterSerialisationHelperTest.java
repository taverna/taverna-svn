package net.sf.taverna.wsdl.xmlsplitter;

import static org.junit.Assert.assertEquals;

import org.embl.ebi.escience.scufl.XScufl;
import org.junit.Test;

public class XMLSplitterSerialisationHelperTest {

	@Test
	public void testScuflNS() throws Exception {
		assertEquals("namespace should be equal",XScufl.XScuflNS,XMLSplitterSerialisationHelper.XScuflNS);
	}
}
