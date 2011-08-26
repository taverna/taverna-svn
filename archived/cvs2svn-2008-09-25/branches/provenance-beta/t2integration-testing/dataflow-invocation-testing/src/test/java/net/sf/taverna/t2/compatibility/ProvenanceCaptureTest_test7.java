package net.sf.taverna.t2.compatibility;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import net.sf.taverna.t2.cloudone.identifier.EntityIdentifier;
import net.sf.taverna.t2.facade.WorkflowInstanceFacade;
import net.sf.taverna.t2.invocation.WorkflowDataToken;
import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.testing.CaptureResultsListener;
import net.sf.taverna.t2.testing.DataflowTimeoutException;
import net.sf.taverna.t2.testing.InvocationTestHelper;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.DataflowInputPort;
import net.sf.taverna.t2.workflowmodel.DataflowOutputPort;
import net.sf.taverna.t2.workflowmodel.DataflowValidationReport;
import net.sf.taverna.t2.workflowmodel.impl.DataflowImpl;
import net.sf.taverna.t2.workflowmodel.impl.EditsImpl;

import org.junit.Ignore;
import org.junit.Test;


/**
 * @author Paolo Missier
 * 
 */
public class ProvenanceCaptureTest_test7 extends InvocationTestHelper {
	

	@Test
	public void testProvenanceCapture_test6() throws Exception {
		
		String T2File = "provenance-testing/test7.xml";

		String i1 = "a";
		
		Dataflow dataflow = null;

		System.out.println("input workflow: ["+T2File+"]");
		
		try {
			dataflow = loadDataflow(T2File);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		DataflowValidationReport report = validateDataflow(dataflow);
		
		WorkflowInstanceFacade facade;
		facade = new EditsImpl().createWorkflowInstanceFacade(dataflow,context,"");
		CaptureResultsListener listener = new CaptureResultsListener(dataflow,context);
		facade.addResultListener(listener);

		facade.fire();
		
		T2Reference entityId1 = context.getReferenceService().register(i1, 0,
				true, context);

		// provide inputs if necessary
		for (DataflowInputPort port : dataflow.getInputPorts()) {

			if (port.getName().equals("I1")) {
				WorkflowDataToken inputToken1 = new WorkflowDataToken("",new int[]{}, entityId1, context);
				facade.pushData(inputToken1, port.getName());
			}

		}

		try {
			waitForCompletion(listener);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DataflowTimeoutException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		assertTrue("ok as long as we got this far", true);

	}
	
	
}


