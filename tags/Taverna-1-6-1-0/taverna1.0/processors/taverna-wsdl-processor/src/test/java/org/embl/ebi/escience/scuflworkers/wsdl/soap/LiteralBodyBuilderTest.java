package org.embl.ebi.escience.scuflworkers.wsdl.soap;

import java.util.HashMap;
import java.util.Map;

import org.apache.axis.message.SOAPBodyElement;
import org.embl.ebi.escience.baclava.DataThing;
import org.embl.ebi.escience.baclava.factory.DataThingFactory;
import org.embl.ebi.escience.scufl.DuplicateProcessorNameException;
import org.embl.ebi.escience.scufl.ProcessorCreationException;
import org.embl.ebi.escience.scuflworkers.testhelpers.WSDLBasedTestCase;
import org.embl.ebi.escience.scuflworkers.wsdl.WSDLBasedProcessor;
import org.w3c.dom.Node;

public class LiteralBodyBuilderTest extends WSDLBasedTestCase {

	public void testUnqualifiedNamespaces() throws Exception {
		BodyBuilder builder = createBuilder(TESTWSDL_BASE+"whatizit.wsdl", "queryPmid");
		
		assertTrue("Is is the wrong type, it should be LiteralBodyBuilder",builder instanceof LiteralBodyBuilder);
		
		String parameters = "<parameters xmlns=\"http://www.ebi.ac.uk/webservices/whatizit/ws\"><pipelineName xmlns=\"\">swissProt</pipelineName><pmid xmlns=\"\">1234</pmid></parameters>";
		Map<String,DataThing> inputMap = new HashMap<String, DataThing>();
		inputMap.put("parameters", DataThingFactory.bake(parameters));
		
		SOAPBodyElement body = builder.build(inputMap);
		
		String xml = body.getAsString();
		
		assertTrue("Content of body is incorrect in the definition of the pipelineName and pmid:"+xml,xml.contains("<pipelineName xmlns=\"\">swissProt</pipelineName><pmid xmlns=\"\">1234</pmid>"));
		assertTrue("Wrapping element should have its namespace declared",xml.contains("<ns1:queryPmid"));
	}
	
	public void testQualifiedUnwrapped() throws Exception {
		BodyBuilder builder = createBuilder(TESTWSDL_BASE+"TestServices-unwrapped.wsdl", "countString");
		
		assertTrue("Is is the wrong type, it should be LiteralBodyBuilder",builder instanceof LiteralBodyBuilder);
		Map<String,DataThing>inputMap = new HashMap<String, DataThing>();
		inputMap.put("str", DataThingFactory.bake("bob"));
		
		String xml = builder.build(inputMap).getAsString();
		
		assertEquals("XML should containe qualifed namespace for str",xml,"<ns1:str xmlns:ns1=\"http://testing.org\">bob</ns1:str>");
	}
	
	public void testUnwrappedSimple() throws Exception {
		BodyBuilder builder = createBuilder(TESTWSDL_BASE+"TestServices-unwrapped.wsdl", "countString");
		
		assertTrue("Wrong type of builder, it should be Literal based",builder instanceof LiteralBodyBuilder);
		
		Map<String,DataThing> inputMap = new HashMap<String, DataThing>();
		inputMap.put("str", DataThingFactory.bake("12345"));
		
		SOAPBodyElement body = builder.build(inputMap);
		
		String xml = body.getAsString();
		assertEquals("Input element should be named str:","str",body.getNodeName());
		assertEquals("Value should be 12345:","12345",body.getFirstChild().getNextSibling().getNodeValue());
	}
	
	public void testUnwrappedArray() throws Exception {
		BodyBuilder builder = createBuilder(TESTWSDL_BASE+"TestServices-unwrapped.wsdl", "countStringArray");
		
		assertTrue("Wrong type of builder, it should be Literal based",builder instanceof LiteralBodyBuilder);
		
		Map<String,DataThing> inputMap = new HashMap<String, DataThing>();
		inputMap.put("array", DataThingFactory.bake("<array><item>1</item><item>2</item><item>3</item></array>"));
		
		SOAPBodyElement body = builder.build(inputMap);
		
		String xml = body.getAsString();
		assertEquals("Outer element should be named array. xml = "+xml,"array",body.getNodeName());
		
		Node itemElement = body.getFirstChild().getNextSibling();
		assertEquals("Array element should be named item. xml = "+xml,"item",itemElement.getNodeName());
		assertEquals("First Array element should have the value '1'. xml = "+xml,"1",itemElement.getFirstChild().getNodeValue());
	}
	
	public void testRPCLiteral() throws Exception {
		BodyBuilder builder = createBuilder(TESTWSDL_BASE+"MyService-rpc-literal.wsdl", "countString");
		
		assertTrue("Wrong type of builder, it should be Literal based",builder instanceof LiteralBodyBuilder);
		
		Map<String,DataThing> inputMap = new HashMap<String, DataThing>();
		inputMap.put("str", DataThingFactory.bake("abcdef"));
		
		SOAPBodyElement body = builder.build(inputMap);
		
		String xml = body.getAsString();
		
		assertEquals("Outer element should be named countString","countString",body.getNodeName());
		Node strNode = body.getFirstChild();
		assertEquals("Inner element should be called 'str'","str",strNode.getNodeName());
		assertEquals("str content should be abcdef","abcdef",strNode.getFirstChild().getNextSibling().getNodeValue());
	}
	
	protected BodyBuilder createBuilder(String wsdl, String operation) throws Exception {
		WSDLBasedProcessor processor = createProcessor(wsdl, operation);
		return BodyBuilderFactory.instance().create(processor);
	}
}
