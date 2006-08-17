package org.embl.ebi.escience.scuflworkers.java;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.embl.ebi.escience.baclava.DataThing;
import org.embl.ebi.escience.baclava.factory.DataThingFactory;
import org.embl.ebi.escience.scufl.ScuflModel;
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

public class XMLInputSplitterTest extends TestCase {

	/**
	 * a general all round test of the XMLInputSplitter class
	 * 
	 * @throws Exception
	 */

	public void testSplitter() throws Exception {
		XMLInputSplitter splitter = new XMLInputSplitter();
		ScuflModel model = new ScuflModel();
		WSDLBasedProcessor processor = new WSDLBasedProcessor(model, "testProc",
				"http://eutils.ncbi.nlm.nih.gov/entrez/eutils/soap/eutils_lite.wsdl", "run_eSpell");
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

		assertEquals("wrong number of outputs", 1, splitter.outputNames().length);
		assertEquals("wrong name", "output", splitter.outputNames()[0]);
		assertEquals("wrong type", "'text/xml'", splitter.outputTypes()[0]);

		Map inputMap = new HashMap();
		inputMap.put("db", new DataThing("a database"));
		inputMap.put("tool", new DataThing("a tool"));

		Map outputMap = splitter.execute(inputMap);

		DataThing outputThing = (DataThing) outputMap.get("output");
		String outputString = outputThing.getDataObject().toString();

		assertEquals("output is incorrect", "<parameters><db>a database</db><tool>a tool</tool></parameters>",
				outputString);
	}

	public void testProvideXML() throws Exception {
		XMLInputSplitter splitter = new XMLInputSplitter();
		ScuflModel model = new ScuflModel();
		WSDLBasedProcessor processor = new WSDLBasedProcessor(model, "testProc",
				"http://eutils.ncbi.nlm.nih.gov/entrez/eutils/soap/eutils_lite.wsdl", "run_eSpell");
		splitter.setUpInputs(processor.getInputPorts()[0]);

		String xml = new XMLOutputter().outputString(splitter.provideXML());
		assertEquals(eInfoXML(), xml);
	}

	public void testConsumeXML() throws Exception {
		XMLInputSplitter splitter = new XMLInputSplitter();
		splitter.consumeXML(new SAXBuilder().build(new StringReader(eInfoXML())).getRootElement());

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

		assertEquals("wrong number of outputs", 1, splitter.outputNames().length);
		assertEquals("wrong name", "output", splitter.outputNames()[0]);
		assertEquals("wrong type", "'text/xml'", splitter.outputTypes()[0]);

		Map inputMap = new HashMap();
		inputMap.put("db", new DataThing("a database"));
		inputMap.put("tool", new DataThing("a tool"));

		Map outputMap = splitter.execute(inputMap);

		DataThing outputThing = (DataThing) outputMap.get("output");
		String outputString = outputThing.getDataObject().toString();

		assertEquals("output is incorrect", "<parameters><db>a database</db><tool>a tool</tool></parameters>",
				outputString);
	}

	private String eInfoXML() {
		return "<s:extensions xmlns:s=\"http://org.embl.ebi.escience/xscufl/0.1alpha\"><s:complextype optional=\"false\" unbounded=\"false\" typename=\"eSpellRequest\" name=\"parameters\" qname=\"{http://www.ncbi.nlm.nih.gov/soap/eutils/espell}eSpellRequest\"><s:elements><s:basetype optional=\"true\" unbounded=\"false\" typename=\"string\" name=\"db\" qname=\"{http://www.w3.org/2001/XMLSchema}string\" /><s:basetype optional=\"true\" unbounded=\"false\" typename=\"string\" name=\"term\" qname=\"{http://www.w3.org/2001/XMLSchema}string\" /><s:basetype optional=\"true\" unbounded=\"false\" typename=\"string\" name=\"tool\" qname=\"{http://www.w3.org/2001/XMLSchema}string\" /><s:basetype optional=\"true\" unbounded=\"false\" typename=\"string\" name=\"email\" qname=\"{http://www.w3.org/2001/XMLSchema}string\" /></s:elements></s:complextype></s:extensions>";

	}

	public void testArrayTypes() throws Exception {
		XMLInputSplitter splitter = new XMLInputSplitter();
		ScuflModel model = new ScuflModel();
		WSDLBasedProcessor processor = new WSDLBasedProcessor(model, "testProc",
				"http://www.ebi.ac.uk/ws/WSFasta.wsdl", "runFasta");
		splitter.setUpInputs(processor.getInputPorts()[1]);

		assertEquals("wrong number of inputs", 1, splitter.inputNames().length);
		assertEquals("wrong number of types", 1, splitter.inputTypes().length);

		assertEquals("wrong name", "WSArrayofData", splitter.inputNames()[0]);
		assertEquals("wrong type", "l('text/xml')", splitter.inputTypes()[0]);

		assertEquals("wrong name", "output", splitter.outputNames()[0]);
		assertEquals("wrong type", "'text/xml'", splitter.outputTypes()[0]);

		ArrayList ins = new ArrayList();

		ins.add("<data><type>type1</type><content>content1</content></data>");
		ins.add("<data><type>type2</type><content>content2</content></data>");

		DataThing input = DataThingFactory.bake(ins);

		Map inputMap = new HashMap();
		inputMap.put("WSArrayofData", input);

		Map outputMap = splitter.execute(inputMap);

		DataThing output = (DataThing) outputMap.get("output");
		assertNotNull("'output' did not exist in output map", output);

		assertTrue("output should be a string", output.getDataObject() instanceof String);

		assertEquals(
				"<WSArrayofData><data><type>type1</type><content>content1</content></data><data><type>type2</type><content>content2</content></data></WSArrayofData>",
				output.getDataObject().toString());

	}

	public void testOrderPreserved() throws Exception {
		XMLInputSplitter splitter = new XMLInputSplitter();
		ScuflModel model = new ScuflModel();
		WSDLBasedProcessor processor = new WSDLBasedProcessor(model, "testProc",
				"http://eutils.ncbi.nlm.nih.gov/entrez/eutils/soap/eutils_lite.wsdl", "run_eSpell");
		splitter.setUpInputs(processor.getInputPorts()[0]);

		Map inputMap = new HashMap();
		inputMap.put("tool", new DataThing("a tool"));
		inputMap.put("email", new DataThing("an email"));
		inputMap.put("db", new DataThing("a database"));

		Map outputMap = splitter.execute(inputMap);
		DataThing output = (DataThing) outputMap.get("output");

		assertNotNull("'output' did not exist in output map", output);
		String xmlOutput = output.getDataObject().toString();
		assertTrue("xml returned is unexpected, element order should be same as defined by the webservice", xmlOutput
				.indexOf("<db>a database</db><tool>a tool</tool><email>an email</email>") != -1);

	}

	public void testOrderPreserved2() throws Exception {
		XMLInputSplitter splitter = new XMLInputSplitter();
		ScuflModel model = new ScuflModel();
		WSDLBasedProcessor processor = new WSDLBasedProcessor(model, "testProc",
				"http://discover.nci.nih.gov/gominer/xfire/GMService?wsdl", "getReport");
		splitter.setUpInputs(processor.getInputPorts()[0]);

		Map inputMap = new HashMap();
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

		assertTrue(
				"xml returned is unexpected, element order should be same as defined by the webservice",
				xmlOutput
						.indexOf("<in0><string>0</string></in0><in1><string>1</string></in1><in2>2</in2><in3>3</in3><in4>4</in4><in5>true</in5><in6>true</in6><in7>true</in7>") != -1);

	}

}
