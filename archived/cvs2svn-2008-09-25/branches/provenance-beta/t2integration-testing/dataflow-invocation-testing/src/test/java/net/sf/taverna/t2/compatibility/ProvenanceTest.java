package net.sf.taverna.t2.compatibility;

import net.sf.taverna.t2.facade.WorkflowInstanceFacade;
import net.sf.taverna.t2.facade.impl.WorkflowInstanceFacadeImpl;
import net.sf.taverna.t2.invocation.TokenOrderException;
import net.sf.taverna.t2.invocation.WorkflowDataToken;
import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.testing.CaptureResultsListener;
import net.sf.taverna.t2.testing.DataflowTimeoutException;
import net.sf.taverna.t2.testing.InvocationTestHelper;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.DataflowValidationReport;

import org.junit.Test;

public class ProvenanceTest extends InvocationTestHelper {

	@Test
	public void testT2Dataflow() {
		Dataflow dataflow = null;
		try {
			dataflow = loadDataflow("test15.t2flow");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		DataflowValidationReport report = validateDataflow(dataflow);

		WorkflowInstanceFacade facade;
		facade = new WorkflowInstanceFacadeImpl(dataflow, context, "");
		CaptureResultsListener listener = new CaptureResultsListener(dataflow,
				context);
		facade.addResultListener(listener);

		facade.fire();
		
		//register the input data
		
		T2Reference entityId1 = context.getReferenceService().register("a", 0,
				true, context);

		T2Reference entityId2 = context.getReferenceService().register("b", 0,
				true, context);

		WorkflowDataToken inputToken1 = new WorkflowDataToken("", new int[] {},
				entityId1, context);
		WorkflowDataToken inputToken2 = new WorkflowDataToken("", new int[] {},
				entityId2, context);

		//push the data to the correct input ports to kick off the 'enactment'
		try {
			facade.pushData(inputToken1, "P1Vi");
			facade.pushData(inputToken1, "P2Vi");
		} catch (TokenOrderException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
		System.out.println("Result is : " + listener.getResult("O"));
	}

}
