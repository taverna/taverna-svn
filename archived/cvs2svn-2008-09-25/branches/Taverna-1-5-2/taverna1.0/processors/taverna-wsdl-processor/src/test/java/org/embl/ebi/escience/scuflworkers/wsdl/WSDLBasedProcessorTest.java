package org.embl.ebi.escience.scuflworkers.wsdl;

import junit.framework.TestCase;

import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scufl.ProcessorCreationException;
import org.embl.ebi.escience.scuflworkers.testhelpers.WSDLBasedTestCase;

public class WSDLBasedProcessorTest extends WSDLBasedTestCase {
	
	
	public void testCreation() throws Exception {

		Processor processor = new WSDLBasedProcessor(null, "test",
				TESTWSDL_BASE+"DBfetch.wsdl",
				"getSupportedDBs");

		assertEquals("invalid name", "test", processor.getName());
		assertEquals("incorrect number of outputs", 2, processor
				.getOutputPorts().length);
		assertEquals("incorrect output port name", "attachmentList", processor
				.getOutputPorts()[0].getName());
		assertEquals("incorrect output port name", "getSupportedDBsReturn",
				processor.getOutputPorts()[1].getName());
		assertEquals("incorrect output port syntactic type", "l('text/plain')",
				processor.getOutputPorts()[1].getSyntacticType());

	}

	public void testCreationFailureBadAddress() {
		try {
			new WSDLBasedProcessor(null, "fail", "http://invalidaddress",
					"fail");
			fail("an exception should have been thrown");
		} catch (Exception e) {
			assertTrue(
					"incorrect exception thrown, should have been ProcessorCreationException",
					e instanceof ProcessorCreationException);
		}
	}

	public void testCreationFailureBadService() {
		try {
			new WSDLBasedProcessor(null, "fail",
					TESTWSDL_BASE+"DBfetch.wsdl",
					"invalidservice");
			fail("an exception should have been thrown");
		} catch (Exception e) {
			assertTrue(
					"incorrect exception thrown, should have been ProcessorCreationException",
					e instanceof ProcessorCreationException);
		}
	}

	public void testDocumentPortType() throws Exception {
		Processor processor = new WSDLBasedProcessor(
				null,
				"test",
				TESTWSDL_BASE+"eutils/eutils_lite.wsdl",
				"run_eGquery");

		assertEquals("invalid name", "test", processor.getName());
		assertEquals("incorrect number of outputs", 2, processor
				.getOutputPorts().length);
		assertEquals("incorrect output port name", "attachmentList", processor
				.getOutputPorts()[0].getName());
		assertEquals("incorrect output port name", "parameters", processor
				.getOutputPorts()[1].getName());
		assertEquals("incorrect output port syntactic type", "'text/xml'",
				processor.getOutputPorts()[1].getSyntacticType());

	}

}
