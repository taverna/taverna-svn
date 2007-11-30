package net.sf.taverna.wsdl.soap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import net.sf.taverna.wsdl.parser.WSDLParser;
import net.sf.taverna.wsdl.testutils.LocationConstants;

import org.apache.axis.message.SOAPBodyElement;
import org.junit.Test;
import org.w3c.dom.Node;

public class LiteralBodyBuilderTest implements LocationConstants{

	@Test
	public void testUnqualifiedNamespaces() throws Exception {
		BodyBuilder builder = createBuilder(WSDL_TEST_BASE+"whatizit.wsdl", "queryPmid");
		
		assertTrue("Is is the wrong type, it should be LiteralBodyBuilder",builder instanceof LiteralBodyBuilder);
		
		String parameters = "<parameters xmlns=\"http://www.ebi.ac.uk/webservices/whatizit/ws\"><pipelineName xmlns=\"\">swissProt</pipelineName><pmid xmlns=\"\">1234</pmid></parameters>";
		Map<String,Object> inputMap = new HashMap<String, Object>();
		inputMap.put("parameters", parameters);
		
		SOAPBodyElement body = builder.build(inputMap);
		
		String xml = body.getAsString();
		
		assertTrue("Content of body is incorrect in the definition of the pipelineName and pmid:"+xml,xml.contains("<pipelineName xmlns=\"\">swissProt</pipelineName><pmid xmlns=\"\">1234</pmid>"));
		assertTrue("Wrapping element should have its namespace declared",xml.contains("<ns1:queryPmid"));
	}
	
	@Test
	public void testQualifiedUnwrapped() throws Exception {
		BodyBuilder builder = createBuilder(WSDL_TEST_BASE+"TestServices-unwrapped.wsdl", "countString");
		
		assertTrue("Is is the wrong type, it should be LiteralBodyBuilder",builder instanceof LiteralBodyBuilder);
		Map<String,Object>inputMap = new HashMap<String, Object>();
		inputMap.put("str", "bob");
		
		String xml = builder.build(inputMap).getAsString();
		
		assertEquals("XML should containe qualifed namespace for str",xml,"<ns1:str xmlns:ns1=\"http://testing.org\">bob</ns1:str>");
	}
	
	@Test
	public void testUnwrappedSimple() throws Exception {
		BodyBuilder builder = createBuilder(WSDL_TEST_BASE+"TestServices-unwrapped.wsdl", "countString");
		
		assertTrue("Wrong type of builder, it should be Literal based",builder instanceof LiteralBodyBuilder);
		
		Map<String,Object> inputMap = new HashMap<String, Object>();
		inputMap.put("str", "12345");
		
		SOAPBodyElement body = builder.build(inputMap);
		
		assertEquals("Input element should be named str:","str",body.getNodeName());
		assertEquals("Value should be 12345:","12345",body.getFirstChild().getNextSibling().getNodeValue());
	}
	
	@Test
	public void testUnwrappedArray() throws Exception {
		BodyBuilder builder = createBuilder(WSDL_TEST_BASE+"TestServices-unwrapped.wsdl", "countStringArray");
		
		assertTrue("Wrong type of builder, it should be Literal based",builder instanceof LiteralBodyBuilder);
		
		Map<String,Object> inputMap = new HashMap<String, Object>();
		inputMap.put("array", "<array><item>1</item><item>2</item><item>3</item></array>");
		
		SOAPBodyElement body = builder.build(inputMap);
		
		String xml = body.getAsString();
		assertEquals("Outer element should be named array. xml = "+xml,"array",body.getNodeName());
		
		Node itemElement = body.getFirstChild().getNextSibling();
		assertEquals("Array element should be named item. xml = "+xml,"item",itemElement.getNodeName());
		assertEquals("First Array element should have the value '1'. xml = "+xml,"1",itemElement.getFirstChild().getNodeValue());
	}
	
	@Test
	public void testRPCLiteral() throws Exception {
		BodyBuilder builder = createBuilder(WSDL_TEST_BASE+"MyService-rpc-literal.wsdl", "countString");
		
		assertTrue("Wrong type of builder, it should be Literal based",builder instanceof LiteralBodyBuilder);
		
		Map<String,Object> inputMap = new HashMap<String, Object>();
		inputMap.put("str", "abcdef");
		
		SOAPBodyElement body = builder.build(inputMap);
		
		assertEquals("Outer element should be named countString","countString",body.getNodeName());
		Node strNode = body.getFirstChild();
		assertEquals("Inner element should be called 'str'","str",strNode.getNodeName());
		assertEquals("str content should be abcdef","abcdef",strNode.getFirstChild().getNextSibling().getNodeValue());
	}
	
	protected BodyBuilder createBuilder(String wsdl, String operation) throws Exception {
		WSDLParser parser = new WSDLParser(wsdl);
		return BodyBuilderFactory.instance().create(parser, operation, parser.getOperationInputParameters(operation));
	}
}
