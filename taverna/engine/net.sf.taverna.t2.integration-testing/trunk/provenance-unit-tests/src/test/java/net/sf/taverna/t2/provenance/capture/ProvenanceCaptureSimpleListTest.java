package net.sf.taverna.t2.provenance.capture;

import static org.junit.Assert.assertEquals;
import net.sf.taverna.t2.invocation.WorkflowDataToken;
import net.sf.taverna.t2.provenance.ProvenanceTestHelper;
import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.DataflowInputPort;

import org.junit.Before;
import org.junit.Test;

/**
 * test workflow: nested-2.t2flow
 * 
 * @author Paolo Missier
 * @author Stian Soiland-Reyes
 * 
 */
public class ProvenanceCaptureSimpleListTest extends ProvenanceTestHelper {

	private Dataflow dataflow;

	@Before
	public void runWorkflow() throws Exception {

		dataflow = prepareDataflowRun("nested-2");

		String i1 = "abcd";
		T2Reference entityId1 = context.getReferenceService().register(i1, 0,
				true, context);

		DataflowInputPort port = dataflow.getInputPorts().get(0);
		assertEquals("I", port.getName());

		WorkflowDataToken inputToken1 = new WorkflowDataToken("", new int[] {},
				entityId1, context);

		getFacade().pushData(inputToken1, port.getName());
		waitForCompletion();
	
		assertEquals("988", getListener().getResult("O"));		
	}
	
	@Test
	public void testOutputProvenance() {
		
		
	}
	

}
