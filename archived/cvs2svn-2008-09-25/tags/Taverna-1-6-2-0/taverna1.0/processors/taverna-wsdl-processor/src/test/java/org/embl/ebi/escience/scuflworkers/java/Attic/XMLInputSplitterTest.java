package org.embl.ebi.escience.scuflworkers.java;

import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import org.embl.ebi.escience.baclava.DataThing;
import org.embl.ebi.escience.baclava.factory.DataThingFactory;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scuflworkers.testhelpers.WSDLBasedTestCase;
import org.embl.ebi.escience.scuflworkers.wsdl.WSDLBasedProcessor;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;

/**
 * Tests the XMLInputSplitter local worker, which is used for splitting complex
 * types for processor inputs.
 * 
 * @author sowen
 * 
 */

public class XMLInputSplitterTest extends WSDLBasedTestCase {	

	/**
	 * a general all round test of the XMLInputSplitter class
	 * 
	 * @throws Exception
	 */

	public void testSplitter() throws Exception {
		XMLInputSplitter splitter = new XMLInputSplitter();
		ScuflModel model = new ScuflModel();
		WSDLBasedProcessor processor = new WSDLBasedProcessor(
				model,
				"testProc",
				TESTWSDL_BASE+"eutils/eutils_lite.wsdl",
				"run_eSpell");
		splitter.setUpInputs(processor.getInputPorts()[0]);

		assertEquals("wrong number of inputs", 4, splitter.inputNames().length);
		assertEquals("wrong number of types", 4, splitter.inputTypes().length);

		assertEquals("wrong name", "db", splitter.inputNames()[0]);
		assertEquals("wrong name", "term", splitter.inputNames()[1]);
		assertEquals("wrong name", "tool", splitter.inputNames()[2]);
		assertEquals("wrong name", "email", splitter.inputNames()[3]);

		assertEquals("wrong type", "'text/plain'", splitter.inputTypes()[0]);
		assertEquals("wrong type", "'text/plain'", splitter.inputTypes()[1]);
		assertEquals("wrong type", "'text/plain'", splitter.inputTypes()[2]);
		assertEquals("wrong type", "'text/plain'", splitter.inputTypes()[3]);

		assertEquals("wrong number of outputs", 1,
				splitter.outputNames().length);
		assertEquals("wrong name", "output", splitter.outputNames()[0]);
		assertEquals("wrong type", "'text/xml'", splitter.outputTypes()[0]);

		Map<String,DataThing>inputMap = new HashMap<String,DataThing>();
		inputMap.put("db", new DataThing("a database"));
		inputMap.put("tool", new DataThing("a tool"));

		Map outputMap = splitter.execute(inputMap);

		DataThing outputThing = (DataThing) outputMap.get("output");
		String outputString = outputThing.getDataObject().toString();

		assertEquals(
				"output is incorrect",
				"<parameters xmlns=\"http://www.ncbi.nlm.nih.gov/soap/eutils/espell\"><db>a database</db><tool>a tool</tool></parameters>",
				outputString);
	}

	public void testProvideXML() throws Exception {
		XMLInputSplitter splitter = new XMLInputSplitter();
		ScuflModel model = new ScuflModel();
		WSDLBasedProcessor processor = new WSDLBasedProcessor(
				model,
				"testProc",
				TESTWSDL_BASE+"eutils/eutils_lite.wsdl",
				"run_eSpell");
		splitter.setUpInputs(processor.getInputPorts()[0]);

		String xml = new XMLOutputter().outputString(splitter.provideXML());
		assertEquals("The xml generated is not as expected",eInfoXML(), xml);
	}

	public void testConsumeXML() throws Exception {
		XMLInputSplitter splitter = new XMLInputSplitter();
		splitter.consumeXML(new SAXBuilder()
				.build(new StringReader(eInfoXML())).getRootElement());

		assertEquals("wrong number of inputs", 4, splitter.inputNames().length);
		assertEquals("wrong number of types", 4, splitter.inputTypes().length);

		assertEquals("wrong name", "db", splitter.inputNames()[0]);
		assertEquals("wrong name", "term", splitter.inputNames()[1]);
		assertEquals("wrong name", "tool", splitter.inputNames()[2]);
		assertEquals("wrong name", "email", splitter.inputNames()[3]);

		assertEquals("wrong type", "'text/plain'", splitter.inputTypes()[0]);
		assertEquals("wrong type", "'text/plain'", splitter.inputTypes()[1]);
		assertEquals("wrong type", "'text/plain'", splitter.inputTypes()[2]);
		assertEquals("wrong type", "'text/plain'", splitter.inputTypes()[3]);

		assertEquals("wrong number of outputs", 1,
				splitter.outputNames().length);
		assertEquals("wrong name", "output", splitter.outputNames()[0]);
		assertEquals("wrong type", "'text/xml'", splitter.outputTypes()[0]);

		Map<String,DataThing>inputMap = new HashMap<String,DataThing>();
		inputMap.put("db", new DataThing("a database"));
		inputMap.put("tool", new DataThing("a tool"));

		Map outputMap = splitter.execute(inputMap);

		DataThing outputThing = (DataThing) outputMap.get("output");
		String outputString = outputThing.getDataObject().toString();

		assertEquals(
				"output is incorrect",
				"<parameters xmlns=\"http://www.ncbi.nlm.nih.gov/soap/eutils/espell\"><db>a database</db><tool>a tool</tool></parameters>",
				outputString);
	}

	private String eInfoXML() {
		return "<s:extensions xmlns:s=\"http://org.embl.ebi.escience/xscufl/0.1alpha\"><s:complextype optional=\"false\" unbounded=\"false\" typename=\"eSpellRequest\" name=\"parameters\" qname=\"{http://www.ncbi.nlm.nih.gov/soap/eutils/espell}eSpellRequest\"><s:elements><s:basetype optional=\"true\" unbounded=\"false\" typename=\"string\" name=\"db\" qname=\"{http://www.ncbi.nlm.nih.gov/soap/eutils/espell}&gt;eSpellRequest&gt;db\" /><s:basetype optional=\"true\" unbounded=\"false\" typename=\"string\" name=\"term\" qname=\"{http://www.ncbi.nlm.nih.gov/soap/eutils/espell}&gt;eSpellRequest&gt;term\" /><s:basetype optional=\"true\" unbounded=\"false\" typename=\"string\" name=\"tool\" qname=\"{http://www.ncbi.nlm.nih.gov/soap/eutils/espell}&gt;eSpellRequest&gt;tool\" /><s:basetype optional=\"true\" unbounded=\"false\" typename=\"string\" name=\"email\" qname=\"{http://www.ncbi.nlm.nih.gov/soap/eutils/espell}&gt;eSpellRequest&gt;email\" /></s:elements></s:complextype></s:extensions>";
	}



	public void testOrderPreserved() throws Exception {
		XMLInputSplitter splitter = new XMLInputSplitter();
		ScuflModel model = new ScuflModel();
		WSDLBasedProcessor processor = new WSDLBasedProcessor(
				model,
				"testProc",
				TESTWSDL_BASE+"eutils/eutils_lite.wsdl",
				"run_eSpell");
		splitter.setUpInputs(processor.getInputPorts()[0]);

		Map<String,DataThing>inputMap = new HashMap<String,DataThing>();
		inputMap.put("tool", new DataThing("a tool"));
		inputMap.put("email", new DataThing("an email"));
		inputMap.put("db", new DataThing("a database"));

		Map outputMap = splitter.execute(inputMap);
		DataThing output = (DataThing) outputMap.get("output");

		assertNotNull("'output' did not exist in output map", output);
		String xmlOutput = output.getDataObject().toString();
		assertTrue(
				"xml returned is unexpected, element order should be same as defined by the webservice",
				xmlOutput
						.indexOf("<db>a database</db><tool>a tool</tool><email>an email</email>") != -1);

	}

	public void testOrderPreserved2() throws Exception {
		XMLInputSplitter splitter = new XMLInputSplitter();
		ScuflModel model = new ScuflModel();
		WSDLBasedProcessor processor = new WSDLBasedProcessor(model,
				"testProc",
				TESTWSDL_BASE+"GMService.wsdl",
				"getReport");
		splitter.setUpInputs(processor.getInputPorts()[0]);

		Map<String,DataThing> inputMap = new HashMap<String,DataThing>();
		inputMap.put("in0", new DataThing(new String[] { "0" }));
		inputMap.put("in1", new DataThing(new String[] { "1" }));
		inputMap.put("in6", new DataThing("true"));
		inputMap.put("in2", new DataThing("2"));
		inputMap.put("in4", new DataThing("4"));
		inputMap.put("in5", new DataThing("true"));
		inputMap.put("in7", new DataThing("true"));
		inputMap.put("in3", new DataThing("3"));

		Map outputMap = splitter.execute(inputMap);
		DataThing output = (DataThing) outputMap.get("output");

		assertNotNull("'output' did not exist in output map", output);
		String xmlOutput = output.getDataObject().toString();

		xmlOutput=xmlOutput.replaceAll(" xmlns=\"\"", "");
		
		assertTrue(
				"xml returned is unexpected, element order should be same as defined by the webservice",
				xmlOutput
						.indexOf("<in0><string>0</string></in0><in1><string>1</string></in1><in2>2</in2><in3>3</in3><in4>4</in4><in5>true</in5><in6>true</in6><in7>true</in7>") != -1);

	}

	/**
	 * tests that a nil value gets converted to a nil="true" attribute
	 * 
	 * @throws Exception
	 */
	public void testForNil() throws Exception {
		XMLInputSplitter splitter = new XMLInputSplitter();
		ScuflModel model = new ScuflModel();
		WSDLBasedProcessor processor = new WSDLBasedProcessor(model,
				"testProc",
				TESTWSDL_BASE+"GMService.wsdl",
				"getReport");
		splitter.setUpInputs(processor.getInputPorts()[0]);
		Map<String,DataThing>inputMap = new HashMap<String,DataThing>();
		inputMap.put("in2", new DataThing("nil"));

		Map outputMap = splitter.execute(inputMap);
		DataThing output = (DataThing) outputMap.get("output");
		String xmlOutput = output.getDataObject().toString();
		xmlOutput=xmlOutput.replaceAll(" xmlns=\"\"", "");
		assertTrue(
				"xml incorrect",
				xmlOutput
						.indexOf("<in2 nil=\"true\" />") != -1);
	}

	public void testBase64EncodeInputData() throws Exception {
		XMLInputSplitter splitter = new XMLInputSplitter();
		Map<String,DataThing>inputMap = new HashMap<String,DataThing>();

		String xml = "<s:extensions xmlns:s=\"http://org.embl.ebi.escience/xscufl/0.1alpha\"><s:complextype optional=\"false\" unbounded=\"false\" typename=\"SomeData\" name=\"data\" qname=\"{http://testing.org}SomeData\"><s:elements><s:basetype optional=\"false\" unbounded=\"false\" typename=\"base64binary\" name=\"binaryData\" qname=\"{http://testing.org}base64Binary\" /><s:basetype optional=\"false\" unbounded=\"false\" typename=\"string\" name=\"value\" qname=\"{http://www.w3.org/2001/XMLSchema}string\" /></s:elements></s:complextype></s:extensions>";

		splitter.consumeXML(new SAXBuilder().build(new StringReader(xml))
				.getRootElement());

		assertEquals(splitter.inputTypes()[0], "'application/octet-stream'");
		assertEquals(splitter.inputTypes()[1], "'text/plain'");

		byte[] bytes = new byte[] { 1, 2, 3, 4, 5 };
		inputMap.put("binaryData", new DataThing(bytes));
		inputMap.put("value", new DataThing("a value"));

		Map outputMap = splitter.execute(inputMap);
		DataThing output = (DataThing) outputMap.get("output");
		String xmlOutput = output.getDataObject().toString();

		assertTrue(
				"XML should contain base64Binary encoded String for byte array",
				xmlOutput
						.contains("<binaryData xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:type=\"xsd:base64Binary\">AQIDBAU=</binaryData>"));
	}

	public void testForSimpleTypeEnumeration() throws Exception {
		XMLInputSplitter splitter = new XMLInputSplitter();
		WSDLBasedProcessor processor = createProcessor(TESTWSDL_BASE+"omii-graph.wsdl", "makePlot_x");
		
		splitter.setUpInputs(processor.getInputPorts()[1]);	
		
		String type=null;
		int i=0;
		for (String inName : splitter.inputNames()) {
			if (inName.equals("Smooth")) {
				type=splitter.inputTypes()[i];
				break;
			}
			i++;
		}
		
		assertNotNull("Input named Smooth not found",type);
		assertEquals("type should be type text/plain","'text/plain'",type);
		
		Map<String,DataThing>inputMap = new HashMap<String,DataThing>();
		inputMap.put("Smooth", DataThingFactory.bake("thing"));
		
		Map outputs = splitter.execute(inputMap);
		DataThing output = (DataThing)outputs.get("output");
		
		String xml = output.getDataObject().toString();
		
		assertFalse("There should be no <value> element in the result",xml.contains("value"));
		assertTrue("Error in the xml generated",xml.contains("<Smooth xmlns=\"\">thing</Smooth>"));
	}
	
	public void testForSimpleTypeRestricted() throws Exception {
		XMLInputSplitter splitter = new XMLInputSplitter();
		WSDLBasedProcessor processor = createProcessor(TESTWSDL_BASE+"whatizit.wsdl", "search");
		
		splitter.setUpInputs(processor.getInputPorts()[0]);	
		
		String type=null;
		int i=0;
		for (String inName : splitter.inputNames()) {
			if (inName.equals("limit")) {
				type=splitter.inputTypes()[i];
				break;
			}
			i++;
		}
		
		assertNotNull("Input named limit not found",type);
		assertEquals("type should be type text/plain","'text/plain'",type);
		
		Map<String,DataThing>inputMap = new HashMap<String,DataThing>();
		inputMap.put("limit", DataThingFactory.bake("5"));
		
		Map outputs = splitter.execute(inputMap);
		DataThing output = (DataThing)outputs.get("output");
		
		String xml = output.getDataObject().toString();
		
		assertFalse("There should be no <value> element in the result",xml.contains("value"));
		assertTrue("Error in the xml generated, xml:"+xml,xml.contains("<limit xmlns=\"\">5</limit>"));
	}
	
	public void testUnqualified() throws Exception {
		WSDLBasedProcessor processor = createProcessor(TESTWSDL_BASE+"whatizit.wsdl", "queryPmid");
		XMLInputSplitter splitter = new XMLInputSplitter();
		splitter.setUpInputs(processor.getInputPorts()[0]);
		
		Map<String,DataThing>inputMap = new HashMap<String, DataThing>();
		inputMap.put("pipelineName", DataThingFactory.bake("pipeline"));
		
		Map outputs = splitter.execute(inputMap);
		DataThing output = (DataThing)outputs.get("output");
		String xml = output.getDataObject().toString();
		
		assertTrue("Content of xml is not as expected:"+xml,xml.contains("<pipelineName xmlns=\"\">pipeline</pipelineName>"));
	}
	
	public void testQualified() throws Exception {
		WSDLBasedProcessor processor = createProcessor(TESTWSDL_BASE+"TestServices-wrapped.wsdl", "countString");
		XMLInputSplitter splitter = new XMLInputSplitter();
		splitter.setUpInputs(processor.getInputPorts()[0]);
		
		Map<String,DataThing>inputMap = new HashMap<String, DataThing>();
		inputMap.put("str", DataThingFactory.bake("a string"));
		Map outputs = splitter.execute(inputMap);
		DataThing output = (DataThing)outputs.get("output");
		String xml = output.getDataObject().toString();
		
		assertEquals("Content of xml is not as expected",xml,"<parameters xmlns=\"http://testing.org\"><str>a string</str></parameters>");
	}
	
	public void testQualifiedArray() throws Exception {
		WSDLBasedProcessor processor = createProcessor(TESTWSDL_BASE+"TestServices-wrapped.wsdl", "countStringArray");
		XMLInputSplitter splitter = new XMLInputSplitter();
		splitter.setUpInputs(processor.getInputPorts()[0]);
		
		Map<String,DataThing>inputMap = new HashMap<String, DataThing>();
		inputMap.put("array", DataThingFactory.bake(new String[] {"a","b","c"}));
		Map outputs = splitter.execute(inputMap);
		DataThing output = (DataThing)outputs.get("output");
		String xml = output.getDataObject().toString();
		
		assertEquals("Content of xml is not as expected",xml,"<parameters xmlns=\"http://testing.org\"><array>a</array><array>b</array><array>c</array></parameters>");
	}
	
	public void testQualified2() throws Exception {
		WSDLBasedProcessor processor = createProcessor(TESTWSDL_BASE+"eutils/eutils_lite.wsdl", "run_eInfo");
		XMLInputSplitter splitter = new XMLInputSplitter();
		splitter.setUpInputs(processor.getInputPorts()[0]);
		
		Map<String,DataThing>inputMap = new HashMap<String, DataThing>();
		inputMap.put("db", DataThingFactory.bake("pubmed"));
		Map outputs = splitter.execute(inputMap);
		DataThing output = (DataThing)outputs.get("output");
		String xml = output.getDataObject().toString();
		
		assertEquals("Content of xml is not as expected",xml,"<parameters xmlns=\"http://www.ncbi.nlm.nih.gov/soap/eutils/einfo\"><db>pubmed</db></parameters>");
	}
}
