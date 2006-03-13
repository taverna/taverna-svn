package org.embl.ebi.escience.scuflworkers.java;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.embl.ebi.escience.baclava.DataThing;
import org.embl.ebi.escience.baclava.factory.DataThingFactory;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scuflworkers.wsdl.WSDLBasedProcessor;

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

		assertEquals("output is incorrect", "<eSpellRequest><db>a database</db><tool>a tool</tool></eSpellRequest>",
				outputString);
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

		String xmlOutput = output.getDataObject().toString();

		assertEquals(
				"output xml is wrong",
				"<WSArrayofData><data><type>type1</type><content>content1</content></data><data><type>type2</type><content>content2</content></data></WSArrayofData>",
				xmlOutput);

	}
}
