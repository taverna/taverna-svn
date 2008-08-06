package net.sf.taverna.t2.compatibility;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.taverna.t2.compatibility.WorkflowModelTranslator;
import net.sf.taverna.t2.compatibility.WorkflowTranslationException;
import net.sf.taverna.t2.workflowmodel.Condition;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.DataflowInputPort;
import net.sf.taverna.t2.workflowmodel.DataflowOutputPort;
import net.sf.taverna.t2.workflowmodel.DataflowValidationReport;
import net.sf.taverna.t2.workflowmodel.Datalink;
import net.sf.taverna.t2.workflowmodel.TokenProcessingEntity;
import net.sf.taverna.t2.workflowmodel.EventHandlingInputPort;
import net.sf.taverna.t2.workflowmodel.InputPort;
import net.sf.taverna.t2.workflowmodel.Merge;
import net.sf.taverna.t2.workflowmodel.MergeOutputPort;
import net.sf.taverna.t2.workflowmodel.OutputPort;
import net.sf.taverna.t2.workflowmodel.Port;
import net.sf.taverna.t2.workflowmodel.Processor;
import net.sf.taverna.t2.workflowmodel.impl.DataflowImpl;
import net.sf.taverna.t2.workflowmodel.impl.MergeInputPortImpl;
import net.sf.taverna.t2.workflowmodel.impl.MergeOutputPortImpl;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.DispatchLayer;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.layers.ErrorBounce;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.layers.Failover;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.layers.Invoke;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.layers.Parallelize;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.layers.Retry;
import net.sf.taverna.t2.workflowmodel.processor.iteration.AbstractIterationStrategyNode;
import net.sf.taverna.t2.workflowmodel.processor.iteration.CrossProduct;
import net.sf.taverna.t2.workflowmodel.processor.iteration.NamedInputPortNode;
import net.sf.taverna.t2.workflowmodel.processor.iteration.impl.IterationStrategyStackImpl;

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
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author David Withers
 * @author Stuart Owen
 * 
 */
public class ModelTranslatorTest extends TranslatorTestHelper {

    @Ignore("Integration test")
	@Test
	public void translateAndValidateTest() throws Exception {
		DataflowImpl dataflow = (DataflowImpl) translateScuflFile("ModifiedBiomartAndEMBOSSAnalysis.xml");
		
		assertEquals("the name should have been transferred during the translation","BiomartAndEMBOSSAnalysis",dataflow.getLocalName());
		
		DataflowValidationReport report = dataflow.checkValidity();
		for (TokenProcessingEntity unsatisfiedProcessor : report.getUnsatisfiedEntities()) {
			System.out.println(unsatisfiedProcessor.getLocalName());
		}
		assertTrue(report.getUnsatisfiedEntities().size() == 0);
		for (TokenProcessingEntity failedProcessor : report.getFailedEntities()) {
			System.out.println(failedProcessor.getLocalName());
		}
		assertTrue(report.getFailedEntities().size() == 0);
		for (DataflowOutputPort unresolvedOutput : report
				.getUnresolvedOutputs()) {
			System.out.println(unresolvedOutput.getName());
		}
		assertTrue(report.getUnresolvedOutputs().size() == 0);
	}

	@Test
	public void testAlternateProcessorTranslation() throws Exception {
		Dataflow dataflow = translateScuflFile("alternate_test_workflow.xml");
		
		assertEquals(1, dataflow.getProcessors().size());
		Processor processor = dataflow.getProcessors().get(0);
		assertEquals(2, processor.getActivityList().size());
		
		assertEquals(1, processor.getActivityList().get(0).getInputPorts().size());
		assertEquals("input", processor.getActivityList().get(0).getInputPorts().iterator().next().getName());
		assertEquals(1, processor.getActivityList().get(0).getOutputPorts().size());
		assertEquals("output", processor.getActivityList().get(0).getOutputPorts().iterator().next().getName());

		assertEquals(1, processor.getActivityList().get(1).getInputPorts().size());
		assertEquals("in", processor.getActivityList().get(1).getInputPorts().iterator().next().getName());
		assertEquals(1, processor.getActivityList().get(1).getOutputPorts().size());
		assertEquals("out", processor.getActivityList().get(1).getOutputPorts().iterator().next().getName());
		
		assertEquals(1, processor.getActivityList().get(1).getInputPortMapping().keySet().size());
		assertEquals("input", processor.getActivityList().get(1).getInputPortMapping().keySet().iterator().next());
		assertEquals(1, processor.getActivityList().get(1).getInputPortMapping().values().size());
		assertEquals("in", processor.getActivityList().get(1).getInputPortMapping().values().iterator().next());
		assertEquals(1, processor.getActivityList().get(1).getOutputPortMapping().keySet().size());
		assertEquals("out", processor.getActivityList().get(1).getOutputPortMapping().keySet().iterator().next());
		assertEquals(1, processor.getActivityList().get(1).getOutputPortMapping().values().size());
		assertEquals("output", processor.getActivityList().get(1).getOutputPortMapping().values().iterator().next());
	}
	
	/**
	 * Test method for
	 * {@link net.sf.taverna.t2.compatibility.WorkflowModelTranslator#doTranslation(org.embl.ebi.escience.scufl.ScuflModel)}.
	 * 
	 * <p>
	 * A general all encompassing test of the translation processes.
	 * </p>
	 * 
	 * @throws Exception
	 */
	@Test
	public void testDoTranslation() throws Exception {

		Dataflow dataflow = translateScuflFile("translation-test.xml");

		List<String> processorNames = new ArrayList<String>();
		processorNames.addAll(Arrays.asList("processor_a", "processor_b"));

		List<String> inputNames = new ArrayList<String>();
		inputNames.addAll(Arrays.asList("input"));

		List<String> outputNames = new ArrayList<String>();
		outputNames.addAll(Arrays.asList("output"));

		List<String> portNameList = Arrays.asList("input_1", "input_2",
				"input_3", "output_1", "output_2", "output_3");
		List<String> portNames = new ArrayList<String>(portNameList);
		List<String> activityPortNames = new ArrayList<String>(portNameList);

		Map<String, String> datalinkMap = new HashMap<String, String>();
		datalinkMap.put("input", "input_1");
		datalinkMap.put("output_1", "input_2");
		datalinkMap.put("output_2", "input_3");
		datalinkMap.put("output_3", "output");

		for (DataflowInputPort input : dataflow.getInputPorts()) {
			assertTrue(inputNames.remove(input.getName()));
			assertEquals(input.getName(), input.getInternalOutputPort()
					.getName());
			assertTrue(input.getInternalOutputPort().getOutgoingLinks().size() > 0);
		}
		for (DataflowOutputPort output : dataflow.getOutputPorts()) {
			assertTrue(outputNames.remove(output.getName()));
			assertEquals(output.getName(), output.getInternalInputPort()
					.getName());
			assertNotNull(output.getInternalInputPort().getIncomingLink());
		}
		for (Processor processor : dataflow.getProcessors()) {
			assertTrue(processorNames.remove(processor.getLocalName()));
			for (Port inputPort : processor.getInputPorts()) {
				assertTrue(portNames.remove(inputPort.getName()));
			}
			for (Port outputPort : processor.getOutputPorts()) {
				assertTrue(portNames.remove(outputPort.getName()));
			}

			assertEquals(1, processor.getActivityList().size());
			Activity<?> activity = processor.getActivityList().get(0);
			assertNotNull(processor.getLocalName(), activity);
			Map<String, String> inputPortMap = activity.getInputPortMapping();
			Map<String, String> outputPortMap = activity.getOutputPortMapping();
			for (InputPort inputPort : activity.getInputPorts()) {
				assertTrue(activityPortNames.remove(inputPort.getName()));
				assertTrue(inputPortMap.containsKey(inputPort.getName()));
				assertEquals(inputPort.getName(), inputPortMap.get(inputPort
						.getName()));
			}
			for (OutputPort outputPort : activity.getOutputPorts()) {
				assertTrue(activityPortNames.remove(outputPort.getName()));
				assertTrue(outputPortMap.containsKey(outputPort.getName()));
				assertEquals(outputPort.getName(), outputPortMap.get(outputPort
						.getName()));
			}

			List<? extends Condition> conditions = processor
					.getPreconditionList();
			if (processor.getLocalName().equals("processor_b")) {
				assertEquals(1, conditions.size());
				assertEquals("processor_a", conditions.get(0).getControl()
						.getLocalName());
				assertEquals("processor_b", conditions.get(0).getTarget()
						.getLocalName());
			} else {
				assertEquals(0, conditions.size());
			}

			IterationStrategyStackImpl iterationStrategies = (IterationStrategyStackImpl) processor
					.getIterationStrategy();
			assertEquals(1, iterationStrategies.getStrategies().size());
			if (processor.getLocalName().equals("processor_a")) {
				AbstractIterationStrategyNode terminal = (AbstractIterationStrategyNode) iterationStrategies
						.getStrategies().get(0).getTerminal();
				assertEquals(1, terminal.getChildCount());
				assertTrue(terminal.getChildAt(0) instanceof NamedInputPortNode);
				assertEquals("input_1", ((NamedInputPortNode) terminal
						.getChildAt(0)).getPortName());
			} else if (processor.getLocalName().equals("processor_b")) {
				AbstractIterationStrategyNode terminal = (AbstractIterationStrategyNode) iterationStrategies
						.getStrategies().get(0).getTerminal();
				assertEquals(1, terminal.getChildCount());
				assertTrue(terminal.getChildAt(0) instanceof CrossProduct);
				assertEquals(2, ((CrossProduct) terminal.getChildAt(0))
						.getChildCount());
				assertTrue(terminal.getChildAt(0).getChildAt(0) instanceof NamedInputPortNode);
				assertTrue(terminal.getChildAt(0).getChildAt(1) instanceof NamedInputPortNode);
				assertEquals("input_2", ((NamedInputPortNode) terminal
						.getChildAt(0).getChildAt(0)).getPortName());
				assertEquals("input_3", ((NamedInputPortNode) terminal
						.getChildAt(0).getChildAt(1)).getPortName());
			}

			List<DispatchLayer<?>> dispatchLayers = processor
					.getDispatchStack().getLayers();
			assertEquals(5, dispatchLayers.size());
			assertTrue(dispatchLayers.get(0) instanceof Parallelize);
			assertTrue(dispatchLayers.get(1) instanceof ErrorBounce);
			assertTrue(dispatchLayers.get(2) instanceof Failover);
			assertTrue(dispatchLayers.get(3) instanceof Retry);
			assertTrue(dispatchLayers.get(4) instanceof Invoke);
			if (processor.getLocalName().equals("processor_a")) {
				assertEquals(1, ((Parallelize) dispatchLayers.get(0))
						.getConfiguration().getMaximumJobs());
				assertEquals(0, ((Retry) dispatchLayers.get(3))
						.getConfiguration().getMaxRetries());
				assertEquals(0, ((Retry) dispatchLayers.get(3))
						.getConfiguration().getInitialDelay());
				assertEquals(0, ((Retry) dispatchLayers.get(3))
						.getConfiguration().getMaxDelay());
				assertEquals(1, ((Retry) dispatchLayers.get(3))
						.getConfiguration().getBackoffFactor(), 0);
			} else if (processor.getLocalName().equals("processor_b")) {
				assertEquals(4, ((Parallelize) dispatchLayers.get(0))
						.getConfiguration().getMaximumJobs());
				assertEquals(2, ((Retry) dispatchLayers.get(3))
						.getConfiguration().getMaxRetries());
				assertEquals(1000, ((Retry) dispatchLayers.get(3))
						.getConfiguration().getInitialDelay());
				assertEquals(2250, ((Retry) dispatchLayers.get(3))
						.getConfiguration().getMaxDelay());
				assertEquals(1.5, ((Retry) dispatchLayers.get(3))
						.getConfiguration().getBackoffFactor(), 0);
			}
		}
		assertTrue(portNames.isEmpty());
		assertTrue(processorNames.isEmpty());
		assertTrue(inputNames.isEmpty());
		assertTrue(outputNames.isEmpty());
		assertTrue(activityPortNames.isEmpty());

		List<? extends Datalink> datalinks = dataflow.getLinks();
		assertEquals(4, datalinks.size());
		for (Datalink datalink : datalinks) {
			assertTrue(datalinkMap.containsKey(datalink.getSource().getName()));
			assertEquals(datalink.getSink().getName(), datalinkMap.get(datalink
					.getSource().getName()));
		}

	}

	private Dataflow translateScuflFile(String filename) throws IOException,
			UnknownProcessorException, UnknownPortException,
			ProcessorCreationException, DataConstraintCreationException,
			DuplicateProcessorNameException, MalformedNameException,
			ConcurrencyConstraintCreationException,
			DuplicateConcurrencyConstraintNameException, XScuflFormatException,
			WorkflowTranslationException {
		ScuflModel model = loadScufl(filename);
		Dataflow dataflow = WorkflowModelTranslator.doTranslation(model);
		return dataflow;
	}

	@SuppressWarnings("null")
	@Test
	public void testDataflowPortLinks() throws Exception {
		Dataflow dataflow = translateScuflFile("translation-test-workflow-ports.xml");
		DataflowInputPort workflowInput_1 = null;
		DataflowInputPort workflowInput_2 = null;
		DataflowOutputPort workflowOutput_1 = null;
		DataflowOutputPort workflowOutput_2 = null;

		for (DataflowInputPort port : dataflow.getInputPorts()) {
			if (port.getName().equals("workflow_input_1")) {
				workflowInput_1 = port;
			} else if (port.getName().equals("workflow_input_2")) {
				workflowInput_2 = port;
			}
		}
		assertNotNull("No input to dataflow called workflow_input_1 found",
				workflowInput_1);
		assertNotNull("No input to dataflow called workflow_input_2 found",
				workflowInput_2);

		for (DataflowOutputPort port : dataflow.getOutputPorts()) {
			if (port.getName().equals("workflow_output_1")) {
				workflowOutput_1 = port;
			} else if (port.getName().equals("workflow_output_2")) {
				workflowOutput_2 = port;
			}
		}
		assertNotNull("No output to dataflow called workflow_output_1 found",
				workflowOutput_1);
		assertNotNull("No output to dataflow called workflow_output_2 found",
				workflowOutput_2);

		assertEquals(1, workflowInput_1.getInternalOutputPort()
				.getOutgoingLinks().size());
		assertEquals(1, workflowInput_2.getInternalOutputPort()
				.getOutgoingLinks().size());

		Datalink link;
		link = (Datalink) workflowInput_1.getInternalOutputPort()
				.getOutgoingLinks().toArray()[0];
		assertEquals("input_1", link.getSink().getName());

		link = (Datalink) workflowInput_2.getInternalOutputPort()
				.getOutgoingLinks().toArray()[0];
		assertEquals("input_2", link.getSink().getName());

		assertEquals("output_1", workflowOutput_1.getInternalInputPort()
				.getIncomingLink().getSource().getName());
		assertEquals("output_2", workflowOutput_2.getInternalInputPort()
				.getIncomingLink().getSource().getName());
	}

	@SuppressWarnings("null")
	@Test
	public void testMerge() throws Exception {
		Dataflow dataflow = translateScuflFile("translation-test-merge.xml");

		// quick sanity check that it loaded correctly:
		assertEquals(3, dataflow.getInputPorts().size());
		assertEquals(1, dataflow.getOutputPorts().size());
		assertEquals(1, dataflow.getProcessors().size());
		Processor processor = dataflow.getProcessors().get(0);
		assertEquals("a_processor", processor.getLocalName());
		assertEquals(2, processor.getInputPorts().size());

		EventHandlingInputPort inputPort_1 = null;
		EventHandlingInputPort inputPort_2 = null;
		for (EventHandlingInputPort input : processor.getInputPorts()) {
			if (input.getName().equals("input_1")) {
				inputPort_1 = input;
			} else if (input.getName().equals("input_2")) {
				inputPort_2 = input;
			} else {
				fail("Input port found with unexpected name:" + input.getName());
			}
		}
		assertNotNull("input_1 was not found", inputPort_1);
		assertNotNull("input_2 was not found", inputPort_2);
		// test input_1 supports a merge.
		List<? extends Datalink> links = dataflow.getLinks();
		assertEquals(5, links.size());
		DataflowInputPort workflowIn1 = null;
		DataflowInputPort workflowIn2 = null;
		for (DataflowInputPort port : dataflow.getInputPorts()) {
			if (port.getName().equals("workflow_input_1")) {
				workflowIn1 = port;
			} else if (port.getName().equals("workflow_input_2")) {
				workflowIn2 = port;
			}
		}
		assertNotNull("No workflow input named workflow_input_1 found",
				workflowIn1);
		assertNotNull("No workflow input named workflow_input_2 found",
				workflowIn2);

		assertEquals(1, workflowIn1.getInternalOutputPort().getOutgoingLinks()
				.size());
		assertEquals(1, workflowIn2.getInternalOutputPort().getOutgoingLinks()
				.size());

		Datalink input1link = (Datalink) workflowIn1.getInternalOutputPort()
				.getOutgoingLinks().toArray()[0];
		Datalink input2link = (Datalink) workflowIn1.getInternalOutputPort()
				.getOutgoingLinks().toArray()[0];

		assertTrue(input1link.getSink() instanceof MergeInputPortImpl);
		assertTrue(input2link.getSink() instanceof MergeInputPortImpl);

		MergeInputPortImpl mergeInput1 = (MergeInputPortImpl) input1link
				.getSink();
		MergeInputPortImpl mergeInput2 = (MergeInputPortImpl) input2link
				.getSink();

		assertTrue(inputPort_1.getIncomingLink().getSource() instanceof MergeOutputPort);

		MergeOutputPort mergeOutput = (MergeOutputPort) inputPort_1
				.getIncomingLink().getSource();

		Merge merge = mergeInput1.getMergeInstance();

		assertSame(merge, mergeInput2.getMergeInstance());
		assertSame(merge, mergeOutput.getMerge());

		// test that input_2 doesn't.
		assertFalse(inputPort_2.getIncomingLink().getSource() instanceof MergeOutputPortImpl);
	}
}
