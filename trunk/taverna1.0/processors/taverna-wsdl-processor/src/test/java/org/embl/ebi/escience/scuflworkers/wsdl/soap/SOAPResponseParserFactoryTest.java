package org.embl.ebi.escience.scuflworkers.wsdl.soap;

import java.util.ArrayList;
import java.util.List;

import org.embl.ebi.escience.scuflworkers.testhelpers.WSDLBasedTestCase;
import org.embl.ebi.escience.scuflworkers.wsdl.WSDLBasedProcessor;

public class SOAPResponseParserFactoryTest extends WSDLBasedTestCase {

	//tests that the factory always returns a SOAPResponseLiteralParser regardless of the 
	//output mime type, if the use is set to 'literal' (unwrapped/literal)
	public void testLiteralUnwrappedParserForNonXMLOutput() throws Exception {
		SOAPResponseParserFactory factory = SOAPResponseParserFactory.instance();
		List<String> response = new ArrayList<String>();
		WSDLBasedProcessor processor = createProcessor(TESTWSDL_BASE+"TestServices-unwrapped.wsdl", "getString");
		
		assertEquals("There should only be 2 output ports",2,processor.getOutputPorts().length);
		assertEquals("Output port type should be 'text/plain'","'text/plain'",processor.getOutputPorts()[1].getSyntacticType());
		
		SOAPResponseParser parser = factory.create(response, "literal", "document", processor.getOutputPorts());
		
		assertTrue("The parser is the wrong type, it was:"+parser.getClass().getSimpleName(),parser instanceof SOAPResponsePrimitiveLiteralParser);
	}
	
	//an additional test using another unwrapped/literal wsdl that returns a primative type
	public void testLiteralUnwrappedAlternativeWSDL() throws Exception {
		SOAPResponseParserFactory factory = SOAPResponseParserFactory.instance();
		List<String> response = new ArrayList<String>();
		WSDLBasedProcessor processor = createProcessor(TESTWSDL_BASE+"prodoric.wsdl", "hello");
		
		assertEquals("There should only be 2 output ports",2,processor.getOutputPorts().length);
		assertEquals("Output port type should be 'text/plain'","'text/plain'",processor.getOutputPorts()[1].getSyntacticType());
		
		SOAPResponseParser parser = factory.create(response, "literal", "document", processor.getOutputPorts());
		
		assertTrue("The parser is the wrong type, it was:"+parser.getClass().getSimpleName(),parser instanceof SOAPResponsePrimitiveLiteralParser);
	}
}
