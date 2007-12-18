package net.sf.taverna.t2.cyclone;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;

import net.sf.taverna.t2.facade.WorkflowInstanceFacade;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.DataflowValidationReport;
import net.sf.taverna.t2.workflowmodel.impl.EditsImpl;

import org.junit.Test;

public class TranslateAndRunWithIterationsTest extends TranslatorTestHelper {

	@Test
	
	//Tests a dataflow that passes a list of lists to a processor that expects a list, so should iterate for each inner list. 
	public void testIterateListOfLists() throws Exception {
		Dataflow dataflow = translateScuflFile("test_iterate_list_of_lists.xml");
		DataflowValidationReport report = validateDataflow(dataflow);
		assertTrue("Unsatisfied processor found during validation",report.getUnsatisfiedProcessors().size() == 0);
		assertTrue("Failed processors found during validation",report.getFailedProcessors().size() == 0);
		assertTrue("Unresolved outputs found during validation",report.getUnresolvedOutputs().size() == 0);
		assertTrue("Validation failed",report.isValid());
		
		WorkflowInstanceFacade facade;
		facade = new EditsImpl().createWorkflowInstanceFacade(dataflow,context,"");
		CaptureResultsListener listener = new CaptureResultsListener(dataflow,dataFacade);
		facade.addResultListener(listener);
		
		facade.fire();
		
		waitForCompletion(listener);
		
		assertTrue("The result should be a list",listener.getResult("out") instanceof List);
	}
	
	@Test
	public void testIterateOverList() throws Exception {
		Dataflow dataflow = translateScuflFile("lists_iterate.xml");
		DataflowValidationReport report = validateDataflow(dataflow);
		assertTrue("Unsatisfied processor found during validation",report.getUnsatisfiedProcessors().size() == 0);
		assertTrue("Failed processors found during validation",report.getFailedProcessors().size() == 0);
		assertTrue("Unresolved outputs found during validation",report.getUnresolvedOutputs().size() == 0);
		assertTrue("Validation failed",report.isValid());
		
		WorkflowInstanceFacade facade;
		facade = new EditsImpl().createWorkflowInstanceFacade(dataflow,context,"");
		CaptureResultsListener listener = new CaptureResultsListener(dataflow,dataFacade);
		facade.addResultListener(listener);
		
		facade.fire();
		
		waitForCompletion(listener);
		
		assertTrue("The result should be a list",listener.getResult("out") instanceof List);	
	}
	
	@Test
	//a dataflow that has a beanshell with 2 inputs, 1 receives a list, the other a single string.
	public void testIterationStrategy() throws Exception {
		Dataflow dataflow = translateScuflFile("iteration-strategy.xml");
		DataflowValidationReport report = validateDataflow(dataflow);
		assertTrue("Unsatisfied processor found during validation",report.getUnsatisfiedProcessors().size() == 0);
		assertTrue("Failed processors found during validation",report.getFailedProcessors().size() == 0);
		assertTrue("Unresolved outputs found during validation",report.getUnresolvedOutputs().size() == 0);
		assertTrue("Validation failed",report.isValid());
		
		WorkflowInstanceFacade facade;
		facade = new EditsImpl().createWorkflowInstanceFacade(dataflow,context,"");
		CaptureResultsListener listener = new CaptureResultsListener(dataflow,dataFacade);
		facade.addResultListener(listener);
		
		facade.fire();
		
		try { 
			waitForCompletion(listener,3); //3 seconds should be plenty of time
		}
		catch(DataflowTimeoutException e) {
			fail("Dataflow didn't complete");
		}
		
		assertTrue("The result should be a list",listener.getResult("out") instanceof List);
		List list = (List)listener.getResult("out");
		assertEquals("There should be 3 items in the list",3,list.size());
		assertEquals("invalid output","oneXXX",list.get(0));
		assertEquals("invalid output","twoXXX",list.get(1));
		assertEquals("invalid output","threeXXX",list.get(2));
		
	}
	
}
