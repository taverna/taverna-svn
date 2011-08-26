package net.sf.taverna.t2.serialization;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import net.sf.taverna.t2.cloudone.identifier.EntityIdentifier;
import net.sf.taverna.t2.facade.WorkflowInstanceFacade;
import net.sf.taverna.t2.invocation.WorkflowDataToken;
import net.sf.taverna.t2.testing.CaptureResultsListener;
import net.sf.taverna.t2.testing.InvocationTestHelper;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.DataflowInputPort;
import net.sf.taverna.t2.workflowmodel.DataflowValidationReport;
import net.sf.taverna.t2.workflowmodel.impl.EditsImpl;
import net.sf.taverna.t2.workflowmodel.serialization.xml.XMLDeserializer;
import net.sf.taverna.t2.workflowmodel.serialization.xml.XMLDeserializerImpl;
import net.sf.taverna.t2.workflowmodel.serialization.xml.XMLSerializer;
import net.sf.taverna.t2.workflowmodel.serialization.xml.XMLSerializerImpl;

import org.jdom.Element;
import org.jdom.output.XMLOutputter;
import org.junit.Test;


public class DataflowSerializationTest extends InvocationTestHelper {
	
	private static XMLSerializer serializer = new XMLSerializerImpl();
	private static XMLDeserializer deserializer = new XMLDeserializerImpl();
	
	@Test
	public void testSimpleSerialization() throws Exception {
		
		Dataflow df = translateScuflFile("simple_workflow_with_input.xml");
		Element el = serializer.serializeDataflow(df);	
		
		System.out.println(new XMLOutputter().outputString(el));
		
		Dataflow df2=deserializer.deserializeDataflow(el);
		
		assertNotNull("deserialized dataflow must not be null",df2);
		
		assertTrue("The deserialized dataflow is not valid",df2.checkValidity().isValid());
		
		Element el2 = serializer.serializeDataflow(df2);
		XMLOutputter outputter = new XMLOutputter();
		assertEquals("XML of for round trip serialized dataflow should match",outputter.outputString(el),outputter.outputString(el2));
	}
	
	@Test
	public void testWithMerge() throws Exception {
		Dataflow df = translateScuflFile("merge_lists_workflow.xml");
		Element el = serializer.serializeDataflow(df);
		Dataflow df2 = deserializer.deserializeDataflow(el);
		
		DataflowValidationReport report = validateDataflow(df2);
		assertTrue("Unsatisfied processor found during validation",report.getUnsatisfiedEntities().size() == 0);
		assertTrue("Failed processors found during validation",report.getFailedEntities().size() == 0);
		assertTrue("Unresolved outputs found during validation",report.getUnresolvedOutputs().size() == 0);
		assertTrue("Validation failed",report.isValid());
		
		Element el2=serializer.serializeDataflow(df2);
		XMLOutputter outputter = new XMLOutputter();
		assertEquals("XML of for round trip serialized dataflow should match",outputter.outputString(el),outputter.outputString(el2));
		
	}
	
	@Test
	public void testSerializeRoundTripAndInvoke() throws Exception {
		Dataflow dataflow = translateScuflFile("simple_workflow_with_input.xml");
		Element el = serializer.serializeDataflow(dataflow);
		dataflow = deserializer.deserializeDataflow(el);
		
		DataflowValidationReport report = validateDataflow(dataflow);
		assertTrue("Unsatisfied processor found during validation",report.getUnsatisfiedEntities().size() == 0);
		assertTrue("Failed processors found during validation",report.getFailedEntities().size() == 0);
		assertTrue("Unresolved outputs found during validation",report.getUnresolvedOutputs().size() == 0);
		assertTrue("Validation failed",report.isValid());
		
		List<String> inputs = new ArrayList<String>();
		inputs.add("one");
		inputs.add("two");
		inputs.add("three");
		
		for (String input : inputs) {
			
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
	
	@SuppressWarnings("unchecked")
	@Test
	public void testSimpleSerializeNestedRoundTripAndInvoke() throws Exception {
		Dataflow dataflow_orig = translateScuflFile("simple-nested-test.xml");
		Element el = serializer.serializeDataflow(dataflow_orig);
		Dataflow dataflow = deserializer.deserializeDataflow(el);
		
		Element el2=serializer.serializeDataflow(dataflow);
		XMLOutputter outputter = new XMLOutputter();
		assertEquals("XML of for round trip serialized dataflow should match",outputter.outputString(el),outputter.outputString(el2));
		
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
		
		assertNotNull("There should have been an output event handler named 'out'",listener.getResult("out"));
		assertTrue("There result should be a list",listener.getResult("out") instanceof List);
		List<String> result = (List<String>)listener.getResult("out");
		assertEquals("one-x",result.get(0));
		assertEquals("two-x",result.get(1));
		assertEquals("three-x",result.get(2));
		assertEquals("four-x",result.get(3));
		assertEquals("five-x",result.get(4));
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testSerializeNestedRoundTripAndInvoke() throws Exception {
		Dataflow dataflow = translateScuflFile("less-simple-nested-test.xml");
		Element el = serializer.serializeDataflow(dataflow);
		System.out.println(new org.jdom.output.XMLOutputter().outputString(el));
		dataflow = deserializer.deserializeDataflow(el);
		
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
		
		assertNotNull("There should have been an output event handler named 'out'",listener.getResult("out"));
		assertTrue("There result should be a list",listener.getResult("out") instanceof List);
		List<List> result = (List<List>)listener.getResult("out");
		assertTrue("The result should be a list of lists",result.get(0) instanceof List);
		assertEquals("There should be 3 lists within the results",3,result.size());
		for (List innerList : result) {
			assertEquals("There should be 5 elements within each inner list",5,innerList.size());
			String [] expectedResults = new String[] {"one-xxx","two-xxx","three-xxx","four-xxx","five-xxx"};
			int i=0;
			for (Object innerListItem : innerList) {
				assertEquals(expectedResults[i++],innerListItem);
			}
		}
	}

	
}
