package net.sf.taverna.wsdl.parser;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.net.URL;

import org.junit.Before;
import org.junit.Test;

/**
 * Check that WSDLParser can detect WSRF.
 * Tests {@link WSDLParser#checkWSRF()}
 * 
 * @author Stian Soiland-Reyes
 *
 */
public class WSRFParserTest {
	
	private URL counterServiceWSDL;
	private WSDLParser wsdlParser;

	@Before
	public void findWSDL() {
		String path = "wsrf/counterService/CounterService_.wsdl";
		counterServiceWSDL = getClass().getResource(path);	
		assertNotNull("Coult not find test WSDL " + path, counterServiceWSDL);
	}
	
	@Test
	public void isWSRF() throws Exception {
		wsdlParser = new WSDLParser(counterServiceWSDL.toExternalForm());
		assertTrue("Not recognized as WSRF service", wsdlParser.isWsrfService());
	}

	
}
