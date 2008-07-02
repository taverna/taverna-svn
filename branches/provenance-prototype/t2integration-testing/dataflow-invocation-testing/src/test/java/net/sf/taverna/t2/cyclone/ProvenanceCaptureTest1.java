package net.sf.taverna.t2.cyclone;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import net.sf.taverna.t2.cloudone.identifier.EntityIdentifier;
import net.sf.taverna.t2.facade.WorkflowInstanceFacade;
import net.sf.taverna.t2.invocation.WorkflowDataToken;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.DataflowInputPort;
import net.sf.taverna.t2.workflowmodel.DataflowOutputPort;
import net.sf.taverna.t2.workflowmodel.DataflowValidationReport;
import net.sf.taverna.t2.workflowmodel.impl.DataflowImpl;
import net.sf.taverna.t2.workflowmodel.impl.EditsImpl;

import org.junit.Ignore;
import org.junit.Test;


/**
 * @author David Withers
 * @author Stuart Owen
 * 
 */
public class ProvenanceCaptureTest1 extends TranslatorTestHelper {

	
	@Test
	public void testSimpleWorkflowWithInput() throws Exception {

		List<String> inputs = new ArrayList<String>();
		inputs.add("one");
		inputs.add("two");
//		inputs.add("three");
		
		for (String input : inputs) {
		
			Dataflow dataflow = translateScuflFile("simple_workflow_with_input.xml");
			
			DataflowValidationReport report = validateDataflow(dataflow);
			assertTrue("Unsatisfied processor found during validation",report.getUnsatisfiedEntities().size() == 0);
			assertTrue("Failed processors found during validation",report.getFailedEntities().size() == 0);
			assertTrue("Unresolved outputs found during validation",report.getUnresolvedOutputs().size() == 0);
			assertTrue("Validation failed",report.isValid());
			
			WorkflowInstanceFacade facade;
			facade = new EditsImpl().createWorkflowInstanceFacade(dataflow,context,"");
			CaptureResultsListener listener = new CaptureResultsListener(dataflow,dataFacade);
			facade.addResultListener(listener);
			
			facade.fire();
			
			EntityIdentifier entityId=dataFacade.register(input);
			for (DataflowInputPort port : dataflow.getInputPorts()) {
				WorkflowDataToken inputToken = new WorkflowDataToken("",new int[]{}, entityId, context);
				facade.pushData(inputToken, port.getName());
			}
			
			waitForCompletion(listener);
			
			assertEquals(input+"XXX", listener.getResult("output"));
		}
	}
	
}


