package org.embl.ebi.escience.scuflworkers.soaplab;

import static org.junit.Assert.*;
import junit.framework.TestCase;

import org.embl.ebi.escience.scufl.OutputPort;
import org.embl.ebi.escience.scufl.ProcessorCreationException;
import org.junit.Ignore;
import org.junit.Test;

public class SoaplabProcessorTest {

	@Test
	@Ignore("Integration test")
	public void testCreation() throws Exception {
		SoaplabProcessor proc = new SoaplabProcessor(null, "processor",
				"http://www.ebi.ac.uk/soaplab/services/utils_misc.embossversion");
		OutputPort[] ports = proc.getOutputPorts();
		assertEquals("incorrect number of outputs", 2, ports.length);
		assertEquals("incorrect output name", "report", ports[0].getName());
		assertEquals("incorrect output name", "outfile", ports[1].getName());
	}

	public void testCreationFailure() {
		try {
			SoaplabProcessor proc = new SoaplabProcessor(null, "invalid",
					"http://invalidaddress");
			fail("an exception should have been thrown");
		} catch (Exception e) {
			assertTrue("exception is of the wrong type",
					e instanceof ProcessorCreationException);
		}
	}
}
