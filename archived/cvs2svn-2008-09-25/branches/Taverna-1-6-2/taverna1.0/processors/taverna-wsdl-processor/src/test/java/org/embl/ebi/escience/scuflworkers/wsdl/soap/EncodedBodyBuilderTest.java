package org.embl.ebi.escience.scuflworkers.wsdl.soap;

import java.util.HashMap;
import java.util.Map;

import org.apache.axis.message.SOAPBodyElement;
import org.embl.ebi.escience.baclava.DataThing;
import org.embl.ebi.escience.baclava.factory.DataThingFactory;
import org.embl.ebi.escience.scuflworkers.testhelpers.WSDLBasedTestCase;
import org.embl.ebi.escience.scuflworkers.wsdl.WSDLBasedProcessor;

public class EncodedBodyBuilderTest extends WSDLBasedTestCase {

	
	
	public void testSimpleCase() throws Exception {
		Map<String,DataThing> inputMap = new HashMap<String, DataThing>();
		
		BodyBuilder builder = createBuilder(TESTWSDL_BASE+"TestServices-rpcencoded.wsdl", "countString");
		
		assertTrue("Wrong type of builder created",builder instanceof EncodedBodyBuilder);
		
		inputMap.put("str", DataThingFactory.bake("Roger Ramjet"));
		SOAPBodyElement body = builder.build(inputMap);
		
		String xml = body.getAsString();
		
		assertTrue("Contents of body are not as expected: actual body:"+xml,xml.contains("<str xsi:type=\"xsd:string\">Roger Ramjet</str>"));
	}
	
	public void testStringArray() throws Exception {
		Map<String,DataThing> inputMap = new HashMap<String, DataThing>();
		
		BodyBuilder builder = createBuilder(TESTWSDL_BASE+"TestServices-rpcencoded.wsdl", "countStringArray");
		
		assertTrue("Wrong type of builder created",builder instanceof EncodedBodyBuilder);
		
		inputMap.put("array", DataThingFactory.bake(new String[]{"one","two","three"}));
		SOAPBodyElement body = builder.build(inputMap);
		
		String xml = body.getAsString();
		
		assertTrue("Contents of body are not as expected: actual body:"+xml,xml.contains("<string>one</string><string>two</string><string>three</string>"));
	}
	
	public void testComplexType() throws Exception {
		BodyBuilder builder = createBuilder(TESTWSDL_BASE+"TestServices-rpcencoded.wsdl", "personToString");
		
		assertTrue("Wrong type of builder created",builder instanceof EncodedBodyBuilder);
		
		String p = "<Person xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\"><name xsi:type=\"xsd:string\">bob</name><age xsi:type=\"xsd:int\">12</age></Person>";
		
		Map<String,DataThing> inputMap = new HashMap<String, DataThing>();
		
		inputMap.put("p",DataThingFactory.bake(p));
		SOAPBodyElement body = builder.build(inputMap);
		
		String xml = body.getAsString();
		
		System.out.println(xml);
		
		assertTrue("Type definition of Person is missing",xml.contains("<p xsi:type=\"ns1:Person\">"));
		assertFalse("There shouldn't be ns2 declaration",xml.contains("xmlns:ns2"));
		assertTrue("Missing data content",xml.contains("<name xsi:type=\"xsd:string\">bob</name><age xsi:type=\"xsd:int\">12</age>"));
		
	}
	
	protected BodyBuilder createBuilder(String wsdl, String operation) throws Exception {
		WSDLBasedProcessor processor = new WSDLBasedProcessor(null, "test",
				wsdl,
				operation);
		return BodyBuilderFactory.instance().create(processor);
	}
}
