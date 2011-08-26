package org.biomoby.client.taverna.plugin;

import junit.framework.TestCase;

import org.biomoby.client.CentralImpl;
import org.embl.ebi.escience.scufl.DataConstraint;
import org.embl.ebi.escience.scufl.ScuflModel;


public class BiomobyObjectProcessorTest extends TestCase {

	public BiomobyObjectProcessorTest() {
		super();
	}

	public void testCreation() {
		try {
			ScuflModel model = new ScuflModel();
			BiomobyObjectProcessor bop = new BiomobyObjectProcessor(model,"DNASequenceProcessor", "www.illuminae.com","DNASequence", CentralImpl.DEFAULT_ENDPOINT, false);
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
			
			// DNA sequence should have 2 inputs linked to it
			DataConstraint[] constraints = model.getDataConstraints();
			assertEquals(constraints.length, 2);
			
		} catch (Exception e) {
			fail("There was a problem creating the BioMOBY Object processor:\n"+ e);
		}
	}

}
