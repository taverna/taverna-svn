package net.sf.taverna.t2.activities.wsdl.soap;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import net.sf.taverna.t2.activities.wsdl.WSDLTestConstants;
import net.sf.taverna.t2.activities.wsdl.parser.WSDLParser;

import org.junit.Test;

public class SOAPResponseParserFactoryTest {

	//tests that the factory always returns a SOAPResponseLiteralParser regardless of the 
	//output mime type, if the use is set to 'literal' (unwrapped/literal)
	@Test
	public void testLiteralUnwrappedParserForNonXMLOutput() throws Exception {
		SOAPResponseParserFactory factory = SOAPResponseParserFactory.instance();
		List<String> response = new ArrayList<String>();
		WSDLParser wsdlParser = new WSDLParser(WSDLTestConstants.WSDL_TEST_BASE+"TestServices-unwrapped.wsdl");
		
		SOAPResponseParser parser = factory.create(response, "literal", "document", wsdlParser.getOperationOutputParameters("getString"));
		
		assertTrue("The parser is the wrong type, it was:"+parser.getClass().getSimpleName(),parser instanceof SOAPResponsePrimitiveLiteralParser);
	}
	
	//an additional test using another unwrapped/literal wsdl that returns a primative type
	@Test
	public void testLiteralUnwrappedAlternativeWSDL() throws Exception {
		SOAPResponseParserFactory factory = SOAPResponseParserFactory.instance();
		List<String> response = new ArrayList<String>();
		WSDLParser wsdlParser = new WSDLParser(WSDLTestConstants.WSDL_TEST_BASE+"prodoric.wsdl");
		
		SOAPResponseParser parser = factory.create(response, "literal", "document", wsdlParser.getOperationOutputParameters("hello"));
		
		assertTrue("The parser is the wrong type, it was:"+parser.getClass().getSimpleName(),parser instanceof SOAPResponsePrimitiveLiteralParser);
	}
}
