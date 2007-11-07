package net.sf.taverna.t2.cyclone;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import net.sf.taverna.t2.cloudone.datamanager.memory.InMemoryDataManager;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.DataflowOutputPort;
import net.sf.taverna.t2.workflowmodel.DataflowValidationReport;
import net.sf.taverna.t2.workflowmodel.Processor;
import net.sf.taverna.t2.workflowmodel.impl.ContextManager;
import net.sf.taverna.t2.workflowmodel.impl.DataflowImpl;

import org.embl.ebi.escience.scufl.ConcurrencyConstraintCreationException;
import org.embl.ebi.escience.scufl.DataConstraintCreationException;
import org.embl.ebi.escience.scufl.DuplicateConcurrencyConstraintNameException;
import org.embl.ebi.escience.scufl.DuplicateProcessorNameException;
import org.embl.ebi.escience.scufl.MalformedNameException;
import org.embl.ebi.escience.scufl.ProcessorCreationException;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scufl.UnknownPortException;
import org.embl.ebi.escience.scufl.UnknownProcessorException;
import org.embl.ebi.escience.scufl.parser.XScuflFormatException;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;


/**
 * @author David Withers
 * @author Stuart Owen
 * 
 */
public class TranslateAndRunTest extends TranslatorTestHelper {

	@SuppressWarnings("unchecked")
	@Before
	public void makeDataManager() {
		dataManager = new InMemoryDataManager("namespace",
				Collections.EMPTY_SET);
		ContextManager.baseManager = dataManager;
	}

	private Dataflow translateScuflFile(String filename) throws IOException,
			UnknownProcessorException, UnknownPortException,
			ProcessorCreationException, DataConstraintCreationException,
			DuplicateProcessorNameException, MalformedNameException,
			ConcurrencyConstraintCreationException,
			DuplicateConcurrencyConstraintNameException, XScuflFormatException,
			WorkflowTranslationException {
		System.setProperty("raven.eclipse", "true");
		setUpRavenRepository();
		ScuflModel model = loadScufl(filename);
		Dataflow dataflow = WorkflowModelTranslator.doTranslation(model);
		return dataflow;
	}

	@Ignore("Biomart error prevents this test from working correctly. Needs new workflow or Biomart fix")
	@Test
	public void translateAndValidateTest() throws Exception {
		DataflowImpl dataflow = (DataflowImpl) translateScuflFile("ModifiedBiomartAndEMBOSSAnalysis.xml");
		DataflowValidationReport report = dataflow.checkValidity();
		for (Processor unsatisfiedProcessor : report.getUnsatisfiedProcessors()) {
			System.out.println(unsatisfiedProcessor.getLocalName());
		}
		assertTrue(report.getUnsatisfiedProcessors().size() == 0);
		for (Processor failedProcessor : report.getFailedProcessors()) {
			System.out.println(failedProcessor.getLocalName());
		}
		assertTrue(report.getFailedProcessors().size() == 0);
		for (DataflowOutputPort unresolvedOutput : report
				.getUnresolvedOutputs()) {
			System.out.println(unresolvedOutput.getName());
		}
		assertTrue(report.getUnresolvedOutputs().size() == 0);

		Map<String, DummyEventHandler> eventHandlers = addDummyEventHandlersToOutputs(dataflow);

		for (Processor processor : dataflow.getProcessors()) {
			if (processor.getLocalName().equals("hsapiens_gene_ensembl")) {
				System.out.println("fire MakeList");
				processor.fire("test");
				break;
			}
		}

		waitForCompletion(eventHandlers);
		
		for (Map.Entry<String, DummyEventHandler> entry : eventHandlers
				.entrySet()) {
			System.out.println("Values for port " + entry.getKey());
			Object result = entry.getValue().getResult();
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
		DataflowValidationReport report = dataflow.checkValidity();
		for (Processor unsatisfiedProcessor : report.getUnsatisfiedProcessors()) {
			System.out.println(unsatisfiedProcessor.getLocalName());
		}
		assertTrue(report.getUnsatisfiedProcessors().size() == 0);
		for (Processor failedProcessor : report.getFailedProcessors()) {
			System.out.println(failedProcessor.getLocalName());
		}
		assertTrue(report.getFailedProcessors().size() == 0);
		for (DataflowOutputPort unresolvedOutput : report
				.getUnresolvedOutputs()) {
			System.out.println(unresolvedOutput.getName());
		}
		assertTrue(report.getUnresolvedOutputs().size() == 0);
		
		Map<String, DummyEventHandler> eventHandlers = addDummyEventHandlersToOutputs(dataflow);
		
		assertEquals("There should only be 1 eventHandler",1,eventHandlers.size());
		
		for (Processor processor : dataflow.getProcessors()) {
			//find the string constant processor
			if (!processor.getLocalName().equals("Processor_A"))  {
				processor.fire("test");
				break;
			}
		}
		
		waitForCompletion(eventHandlers);
		
		DummyEventHandler handler = eventHandlers.get("out");
		assertEquals("The output was incorrect","Some Data",handler.getResult());
	
	}
	
	@Ignore
	@Test
	public void testErrorPropogation() throws Exception {
		Dataflow dataflow = translateScuflFile("test_error_propagation.xml");
		DataflowValidationReport report = dataflow.checkValidity();
		for (Processor unsatisfiedProcessor : report.getUnsatisfiedProcessors()) {
			System.out.println(unsatisfiedProcessor.getLocalName());
		}
		assertTrue(report.getUnsatisfiedProcessors().size() == 0);
		for (Processor failedProcessor : report.getFailedProcessors()) {
			System.out.println(failedProcessor.getLocalName());
		}
		assertTrue(report.getFailedProcessors().size() == 0);
		for (DataflowOutputPort unresolvedOutput : report
				.getUnresolvedOutputs()) {
			System.out.println(unresolvedOutput.getName());
		}
		assertTrue(report.getUnresolvedOutputs().size() == 0);
		
		Map<String, DummyEventHandler> eventHandlers = addDummyEventHandlersToOutputs(dataflow);
		
		assertEquals("There should only be 1 eventHandler",1,eventHandlers.size());
		
		for (Processor processor : dataflow.getProcessors()) {
			//find the string constant processor
			if (!processor.getLocalName().equals("List_Emitter"))  {
				processor.fire("test");
				break;
			}
		}
		
		waitForCompletion(eventHandlers);
		
		DummyEventHandler handler = eventHandlers.get("out");
		assertTrue("The result should be a list",handler.getResult() instanceof List);
		
		//TODO: test that the error is passed through.
	}
	
	@Test
	public void testWorkflowContainingWSDL() throws Exception {
		Dataflow dataflow = translateScuflFile("wsdl_test.xml");
		
		DataflowValidationReport report = dataflow.checkValidity();
		for (Processor unsatisfiedProcessor : report.getUnsatisfiedProcessors()) {
			System.out.println(unsatisfiedProcessor.getLocalName());
		}
		assertTrue(report.getUnsatisfiedProcessors().size() == 0);
		for (Processor failedProcessor : report.getFailedProcessors()) {
			System.out.println(failedProcessor.getLocalName());
		}
		assertTrue(report.getFailedProcessors().size() == 0);
		for (DataflowOutputPort unresolvedOutput : report
				.getUnresolvedOutputs()) {
			System.out.println(unresolvedOutput.getName());
		}
		assertTrue(report.getUnresolvedOutputs().size() == 0);
		
		Map<String, DummyEventHandler> eventHandlers = addDummyEventHandlersToOutputs(dataflow);
		
		assertEquals("There should only be 1 eventHandler",1,eventHandlers.size());
		
		for (Processor processor : dataflow.getProcessors()) {
			//find the string constant processor
			if (processor.getLocalName().equals("Make_gene_list"))  {
				processor.fire("test");
				break;
			}
		}
		
		waitForCompletion(eventHandlers);
		
		DummyEventHandler handler = eventHandlers.get("out");
		assertTrue("The result should be a list",handler.getResult() instanceof List);
	}
}


