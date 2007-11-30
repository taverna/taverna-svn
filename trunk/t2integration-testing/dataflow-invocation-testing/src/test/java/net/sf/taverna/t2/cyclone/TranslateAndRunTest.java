package net.sf.taverna.t2.cyclone;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import net.sf.taverna.t2.cloudone.identifier.EntityIdentifier;
import net.sf.taverna.t2.facade.WorkflowInstanceFacade;
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
public class TranslateAndRunTest extends TranslatorTestHelper {

	@Test
	public void translateAndValidateBiomartAndEMBOSSTest() throws Exception {
		DataflowImpl dataflow = (DataflowImpl) translateScuflFile("ModifiedBiomartAndEMBOSSAnalysis2.xml");
		DataflowValidationReport report = validateDataflow(dataflow);
		assertTrue("Unsatisfied processor found during validation",report.getUnsatisfiedProcessors().size() == 0);
		assertTrue("Failed processors found during validation",report.getFailedProcessors().size() == 0);
		assertTrue("Unresolved outputs found during validation",report.getUnresolvedOutputs().size() == 0);
		assertTrue("Validation failed",report.isValid());

		WorkflowInstanceFacade facade;
		facade = new EditsImpl().createWorkflowInstanceFacade(dataflow,context);
		CaptureResultsListener listener = new CaptureResultsListener(dataflow,dataFacade);
		facade.addResultListener(listener);
		
		facade.fire();
		
		waitForCompletion(listener,120);
		
		for (DataflowOutputPort outputPort : dataflow.getOutputPorts()) {
			System.out.println("Values for port " + outputPort.getName());
			Object result = listener.getResult(outputPort.getName());
			if (result instanceof List) {
				for (Object element : (List<?>) result) {
					System.out.println(element);
				}
			} else {
				System.out.println(result);
			}
		}
	}
	
	/**
	 * Tests a simple workflow that contains unbound ports and a port with a default value.
	 * During translation it should remove the unbound ports, and add a String Constant activity upstream
	 * of the port with the default value.
	 */
	@Test
	public void testUnboundPortsAndADefaultValue() throws Exception {
		Dataflow dataflow = translateScuflFile("unbound_ports_with_default.xml");
		DataflowValidationReport report = validateDataflow(dataflow);
		assertTrue("Unsatisfied processor found during validation",report.getUnsatisfiedProcessors().size() == 0);
		assertTrue("Failed processors found during validation",report.getFailedProcessors().size() == 0);
		assertTrue("Unresolved outputs found during validation",report.getUnresolvedOutputs().size() == 0);
		assertTrue("Validation failed",report.isValid());
		
		WorkflowInstanceFacade facade;
		facade = new EditsImpl().createWorkflowInstanceFacade(dataflow,context);
		CaptureResultsListener listener = new CaptureResultsListener(dataflow,dataFacade);
		facade.addResultListener(listener);
		
		facade.fire();
		
		waitForCompletion(listener);
		
		assertEquals("The output was incorrect","Some Data",listener.getResult("out"));
	
	}
	
	@Ignore
	@Test
	public void testErrorPropogation() throws Exception {
		Dataflow dataflow = translateScuflFile("test_error_propagation.xml");
		DataflowValidationReport report = validateDataflow(dataflow);
		assertTrue("Unsatisfied processor found during validation",report.getUnsatisfiedProcessors().size() == 0);
		assertTrue("Failed processors found during validation",report.getFailedProcessors().size() == 0);
		assertTrue("Unresolved outputs found during validation",report.getUnresolvedOutputs().size() == 0);
		assertTrue("Validation failed",report.isValid());
		
		WorkflowInstanceFacade facade;
		facade = new EditsImpl().createWorkflowInstanceFacade(dataflow,context);
		CaptureResultsListener listener = new CaptureResultsListener(dataflow,dataFacade);
		facade.addResultListener(listener);
		
		facade.fire();
		
		waitForCompletion(listener,5);
		
		assertTrue("The result should be a list",listener.getResult("out") instanceof List);
		
		//TODO: test that the error is passed through.
	}
	
	@Test
	public void testWorkflowContainingWSDL() throws Exception {
		Dataflow dataflow = translateScuflFile("wsdl_test.xml");
		
		DataflowValidationReport report = validateDataflow(dataflow);
		assertTrue("Unsatisfied processor found during validation",report.getUnsatisfiedProcessors().size() == 0);
		assertTrue("Failed processors found during validation",report.getFailedProcessors().size() == 0);
		assertTrue("Unresolved outputs found during validation",report.getUnresolvedOutputs().size() == 0);
		assertTrue("Validation failed",report.isValid());
		
		WorkflowInstanceFacade facade;
		facade = new EditsImpl().createWorkflowInstanceFacade(dataflow,context);
		CaptureResultsListener listener = new CaptureResultsListener(dataflow,dataFacade);
		facade.addResultListener(listener);
		
		facade.fire();
		
		waitForCompletion(listener);
		
		assertTrue("The result should be a list",listener.getResult("out") instanceof List);
	}
	
	
	
	@Test
	public void testSimpleWorkflowWithInput() throws Exception {
		Dataflow dataflow = translateScuflFile("simple_workflow_with_input.xml");
		
		DataflowValidationReport report = validateDataflow(dataflow);
		assertTrue("Unsatisfied processor found during validation",report.getUnsatisfiedProcessors().size() == 0);
		assertTrue("Failed processors found during validation",report.getFailedProcessors().size() == 0);
		assertTrue("Unresolved outputs found during validation",report.getUnresolvedOutputs().size() == 0);
		assertTrue("Validation failed",report.isValid());
		
		List<String> inputs = new ArrayList<String>();
		inputs.add("one");
		inputs.add("two");
		inputs.add("three");
		
		int i=0;
		for (String input : inputs) {
			
			WorkflowInstanceFacade facade;
			facade = new EditsImpl().createWorkflowInstanceFacade(dataflow,context);
			CaptureResultsListener listener = new CaptureResultsListener(dataflow,dataFacade);
			facade.addResultListener(listener);
			
			facade.fire();
			
			EntityIdentifier entityId=dataFacade.register(input);
			for (DataflowInputPort port : dataflow.getInputPorts()) {
				facade.pushData(entityId, new int[0], port.getName());
			}
			
			waitForCompletion(listener);
			
			assertEquals(input+"XXX", listener.getResult("output"));
		}
	}
}


