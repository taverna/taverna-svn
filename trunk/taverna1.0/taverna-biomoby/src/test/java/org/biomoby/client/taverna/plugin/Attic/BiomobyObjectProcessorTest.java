package org.biomoby.client.taverna.plugin;

import junit.framework.TestCase;

import org.biomoby.client.CentralImpl;


public class BiomobyObjectProcessorTest extends TestCase {

	public BiomobyObjectProcessorTest() {
		super();
	}

	public void testCreation() {
		try {
			BiomobyObjectProcessor bop = new BiomobyObjectProcessor(null,"DNASequenceProcessor", "www.illuminae.com","DNASequence", CentralImpl.DEFAULT_ENDPOINT);
			assertTrue(bop.locatePort("String(SequenceString)", true) != null);
			assertTrue(bop.locatePort("Integer(Length)", true) != null);
			assertTrue(bop.locatePort("namespace", true) != null);
			assertTrue(bop.locatePort("id", true) != null);
			assertTrue(bop.locatePort("article name", true) != null);
			assertTrue(bop.getOutputPorts().length == 1);
			assertTrue(bop.locatePort("mobyData", false) != null);
			assertEquals(bop.getAuthorityName(), "www.illuminae.com");
			assertEquals(bop.getName(), "DNASequenceProcessor");
			assertEquals(bop.getServiceName(), "DNASequence");
			
		} catch (Exception e) {
			fail("There was a problem creating the BioMOBY Object processor:\n"+ e);
		}
	}

}
