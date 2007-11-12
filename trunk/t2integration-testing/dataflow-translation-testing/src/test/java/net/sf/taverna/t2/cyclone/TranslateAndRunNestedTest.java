package net.sf.taverna.t2.cyclone;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import net.sf.taverna.t2.cloudone.datamanager.memory.InMemoryDataManager;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.DataflowValidationReport;
import net.sf.taverna.t2.workflowmodel.Processor;
import net.sf.taverna.t2.workflowmodel.impl.ContextManager;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests specifically associated with Nested Workflows.
 * @author Stuart Owen
 *
 */
public class TranslateAndRunNestedTest extends TranslatorTestHelper {

	@SuppressWarnings("unchecked")
	@Before
	public void makeDataManager() {
		dataManager = new InMemoryDataManager("namespace",
				Collections.EMPTY_SET);
		ContextManager.baseManager = dataManager;
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void simpleNested() throws Exception {
		Dataflow dataflow = translateScuflFile("simple-nested-test.xml");
		DataflowValidationReport report = validateDataflow(dataflow);
		assertTrue("Unsatisfied processor found during validation",report.getUnsatisfiedProcessors().size() == 0);
		assertTrue("Failed processors found during validation",report.getFailedProcessors().size() == 0);
		assertTrue("Unresolved outputs found during validation",report.getUnresolvedOutputs().size() == 0);
		assertTrue("Validation failed",report.isValid());
		
		Map<String, DummyEventHandler> eventHandlers = addDummyEventHandlersToOutputs(dataflow);

		for (Processor processor : dataflow.getProcessors()) {
			if (processor.getLocalName().equals("Beanshell_scripting_host")) {
				processor.fire("test");
				break;
			}
		}

		waitForCompletion(eventHandlers);
		DummyEventHandler handler = eventHandlers.get("out");
		
		assertNotNull("There should have been an output event handler named 'out'",handler);
		assertTrue("There result should be a list",handler.getResult() instanceof List);
		List<String> result = (List<String>)handler.getResult();
		assertEquals("one-x",result.get(0));
		assertEquals("two-x",result.get(1));
		assertEquals("three-x",result.get(2));
		assertEquals("four-x",result.get(3));
		assertEquals("five-x",result.get(4));
	}
	
	/**
	 * A workflow that contains a Nested Workflow which recieves a List of Lists.
	 * Internally the first processor in the nested workflow requires a single item
	 * causing the nested workflow itself to produce a list of lists. 
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void lessSimpleNestedIteratesListOfLists() throws Exception {
		Dataflow dataflow = translateScuflFile("less-simple-nested-test.xml");
		DataflowValidationReport report = validateDataflow(dataflow);
		assertTrue("Unsatisfied processor found during validation",report.getUnsatisfiedProcessors().size() == 0);
		assertTrue("Failed processors found during validation",report.getFailedProcessors().size() == 0);
		assertTrue("Unresolved outputs found during validation",report.getUnresolvedOutputs().size() == 0);
		assertTrue("Validation failed",report.isValid());

		Map<String, DummyEventHandler> eventHandlers = addDummyEventHandlersToOutputs(dataflow);

		for (Processor processor : dataflow.getProcessors()) {
			if (processor.getLocalName().equals("Beanshell_scripting_host")) {
				processor.fire("test");
				break;
			}
		}

		waitForCompletion(eventHandlers);
		DummyEventHandler handler = eventHandlers.get("out");
		
		assertNotNull("There should have been an output event handler named 'out'",handler);
		assertTrue("There result should be a list",handler.getResult() instanceof List);
		List<List> result = (List<List>)handler.getResult();
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
	
	/**
	 * As in lessSimpleNestedIteratesListOfLists except the processor consumes a list and produces a single item.
	 * This causes the nested workflow to produce a list of 3 string items (which contains the a concatenated
	 * string referring to the 5 items in the orginal inner list).
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void lessSimpleNestedIteratesListOfLists2() throws Exception {
		Dataflow dataflow = translateScuflFile("less-simple-nested-test2.xml");
		DataflowValidationReport report = validateDataflow(dataflow);
		assertTrue("Unsatisfied processor found during validation",report.getUnsatisfiedProcessors().size() == 0);
		assertTrue("Failed processors found during validation",report.getFailedProcessors().size() == 0);
		assertTrue("Unresolved outputs found during validation",report.getUnresolvedOutputs().size() == 0);
		assertTrue("Validation failed",report.isValid());

		Map<String, DummyEventHandler> eventHandlers = addDummyEventHandlersToOutputs(dataflow);

		for (Processor processor : dataflow.getProcessors()) {
			if (processor.getLocalName().equals("Beanshell_scripting_host")) {
				processor.fire("test");
				break;
			}
		}

		waitForCompletion(eventHandlers);
		DummyEventHandler handler = eventHandlers.get("out");
		
		assertNotNull("There should have been an output event handler named 'out'",handler);
		assertTrue("There result should be a list",handler.getResult() instanceof List);
		List<String> result = (List<String>)handler.getResult();
		assertEquals("There should be 3 lists within the results",3,result.size());
		assertTrue("The result should be a list of lists",result.get(0) instanceof String);
		for (String resultItem : result) {
			assertEquals("Unexpected String in the result","[one, two, three, four, five]-xxx",resultItem);
		}
	}
}
