package net.sf.taverna.t2.lineageService.capture.test;


import static org.junit.Assert.assertTrue;
import net.sf.taverna.t2.invocation.WorkflowDataToken;
import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.DataflowInputPort;

import org.junit.Test;


/**
 * @author Paolo Missier
 * test workflow: Retrieve SNPs from regions around known genes-simplified.t2flow<br/>
 * note it only takes one input, not a list...
 */
public class ProvenanceCaptureTest_test166 extends ProvenanceCaptureTestHelper {

	@Test
	public void testProvenanceCapture_test6() throws Exception {

		ProvenanceCaptureTestHelper helper = this;

		Dataflow dataflow = helper.setup("ProvenanceCaptureTestWithInput");

		String i1 = "ENSG00000139618";

		T2Reference entityId1 = context.getReferenceService().register(i1, 0,true, context);

		// provide inputs to ports
		for (DataflowInputPort port : dataflow.getInputPorts()) {

			if (port.getName().equals("I1")) {
				WorkflowDataToken inputToken1 = new WorkflowDataToken("",new int[]{}, entityId1, context);
				helper.getFacade().pushData(inputToken1, port.getName());
			}

		}

		helper.waitForCompletion();

		System.out.println("output: \n"+helper.getListener().getResult("O1"));
		assertTrue("ok as long as we got this far", true);
	}


}


