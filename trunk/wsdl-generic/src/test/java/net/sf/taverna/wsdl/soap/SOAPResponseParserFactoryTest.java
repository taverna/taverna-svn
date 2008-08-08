package net.sf.taverna.wsdl.soap;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import net.sf.taverna.wsdl.parser.WSDLParser;
import net.sf.taverna.wsdl.testutils.LocationConstants;

import org.junit.Ignore;
import org.junit.Test;

public class SOAPResponseParserFactoryTest  implements LocationConstants {

	//tests that the factory always returns a SOAPResponseLiteralParser regardless of the 
	//output mime type, if the use is set to 'literal' (unwrapped/literal)
	@Ignore("Integration test")
	@Test
	public void testLiteralUnwrappedParserForNonXMLOutput() throws Exception {
		SOAPResponseParserFactory factory = SOAPResponseParserFactory.instance();
		List<String> response = new ArrayList<String>();
		WSDLParser wsdlParser = new WSDLParser(WSDL_TEST_BASE+"TestServices-unwrapped.wsdl");
		
		SOAPResponseParser parser = factory.create(response, "literal", "document", wsdlParser.getOperationOutputParameters("getString"));
		
		assertTrue("The parser is the wrong type, it was:"+parser.getClass().getSimpleName(),parser instanceof SOAPResponsePrimitiveLiteralParser);
	}
	
	//an additional test using another unwrapped/literal wsdl that returns a primative type
	@Ignore("Integration test")
	@Test
	public void testLiteralUnwrappedAlternativeWSDL() throws Exception {
		SOAPResponseParserFactory factory = SOAPResponseParserFactory.instance();
		List<String> response = new ArrayList<String>();
		WSDLParser wsdlParser = new WSDLParser(WSDL_TEST_BASE+"prodoric.wsdl");
		
		SOAPResponseParser parser = factory.create(response, "literal", "document", wsdlParser.getOperationOutputParameters("hello"));
		
		assertTrue("The parser is the wrong type, it was:"+parser.getClass().getSimpleName(),parser instanceof SOAPResponsePrimitiveLiteralParser);
	}
}
