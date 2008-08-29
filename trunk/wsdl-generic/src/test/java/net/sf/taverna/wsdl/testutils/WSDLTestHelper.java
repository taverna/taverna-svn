package net.sf.taverna.wsdl.testutils;

import net.sf.taverna.wsdl.parser.WSDLParserTest;

public class WSDLTestHelper implements LocationConstants {
	
	public static String wsdlResourcePath(String resourceName) throws Exception {
		return WSDLParserTest.class.getResource(WSDL_RESOURCE_BASE+resourceName).toExternalForm();
	}

}
