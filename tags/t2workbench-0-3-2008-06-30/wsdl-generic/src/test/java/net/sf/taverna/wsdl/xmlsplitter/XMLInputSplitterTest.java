package net.sf.taverna.wsdl.xmlsplitter;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import net.sf.taverna.wsdl.parser.TypeDescriptor;
import net.sf.taverna.wsdl.parser.WSDLParser;
import net.sf.taverna.wsdl.testutils.LocationConstants;

import org.junit.Test;

public class XMLInputSplitterTest implements LocationConstants{

	@Test
	public void testExecute() throws Exception {
		WSDLParser parser = new WSDLParser(WSDL_TEST_BASE+"eutils/eutils_lite.wsdl");
		TypeDescriptor descriptor = parser.getOperationInputParameters("run_eInfo").get(0);
		XMLInputSplitter splitter = new XMLInputSplitter(descriptor,new String[]{"db","tool","email"},new String[]{"text/plain","text/plain","text/plain"},new String[]{"output"});
		Map<String,Object> inputMap = new HashMap<String, Object>();
		inputMap.put("db", "pubmed");
		inputMap.put("email", "bob.monkhouse@itv.com");
		Map<String,String> outputMap = splitter.execute(inputMap);
		assertNotNull("there should be an output named 'output'",outputMap.containsKey("output"));
		String xml = outputMap.get("output");
		assertTrue(xml.startsWith("<parameters xmlns=\"http://www.ncbi.nlm.nih.gov/soap/eutils/einfo\">"));
		assertTrue(xml.contains("<db>pubmed</db>"));
		assertTrue(xml.contains("<tool></tool>"));
		assertTrue(xml.contains("<email>bob.monkhouse@itv.com</email>"));
	} 
	
}
