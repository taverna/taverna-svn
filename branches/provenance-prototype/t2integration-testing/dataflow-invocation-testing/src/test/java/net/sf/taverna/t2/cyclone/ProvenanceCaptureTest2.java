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
 * @author Paolo Missier
 * 
 */
public class ProvenanceCaptureTest2 extends TranslatorTestHelper {
	
	String[] scuflFiles = 
			{ "provenance-testing/simple_workflow2_with_2_inputs.ml",  //0
			  "provenance-testing/test_iterate_list_of_lists.xml",  //1
			  "provenance-testing/test1.xml",  //2
			  "provenance-testing/test2.xml",  //3
			  "provenance-testing/test3.xml",  //4
			  "provenance-testing/test4.xml"};  //5
	
	
	@Test
	public void testNoInput() throws Exception {
		
		String scuflFile = scuflFiles[1];

		Dataflow dataflow = translateScuflFile(scuflFile);

		System.out.println("input workflow: ["+scuflFile+"]");
		
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

		waitForCompletion(listener);
		
		assertTrue("ok as long as we got this far", true);
	}
	
	
	@Test
	public void testSimpleWorkflowWithInput() throws Exception {
		
		String scuflFile = scuflFiles[1];
		
		String s1 = "foo";
		String s2 = "bar";

		Dataflow dataflow = translateScuflFile(scuflFile);

		System.out.println("input workflow: ["+scuflFile+"]");
		
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

		EntityIdentifier entityId1=dataFacade.register(s1);
		EntityIdentifier entityId2=dataFacade.register(s2);

		// provide inputs if necessary
		for (DataflowInputPort port : dataflow.getInputPorts()) {

			if (port.getName().equals("s1")) {
				WorkflowDataToken inputToken1 = new WorkflowDataToken("",new int[]{}, entityId1, context);
				facade.pushData(inputToken1, port.getName());
			}

			else if (port.getName().equals("s2")) {
				WorkflowDataToken inputToken2 = new WorkflowDataToken("",new int[]{}, entityId2, context);
				facade.pushData(inputToken2, port.getName());
			}
		}

		waitForCompletion(listener);

		assertEquals(s1+s2, listener.getResult("s3"));
	}

}


