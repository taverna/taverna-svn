package net.sf.taverna.t2.cyclone;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.swing.tree.MutableTreeNode;

import net.sf.taverna.t2.cyclone.translators.ActivityTranslator;
import net.sf.taverna.t2.cyclone.translators.ActivityTranslatorFactory;
import net.sf.taverna.t2.cyclone.translators.ActivityTranslatorNotFoundException;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.DataflowInputPort;
import net.sf.taverna.t2.workflowmodel.DataflowOutputPort;
import net.sf.taverna.t2.workflowmodel.Datalink;
import net.sf.taverna.t2.workflowmodel.Edit;
import net.sf.taverna.t2.workflowmodel.EditException;
import net.sf.taverna.t2.workflowmodel.Edits;
import net.sf.taverna.t2.workflowmodel.EventForwardingOutputPort;
import net.sf.taverna.t2.workflowmodel.EventHandlingInputPort;
import net.sf.taverna.t2.workflowmodel.InputPort;
import net.sf.taverna.t2.workflowmodel.OrderedPair;
import net.sf.taverna.t2.workflowmodel.OutputPort;
import net.sf.taverna.t2.workflowmodel.Processor;
import net.sf.taverna.t2.workflowmodel.impl.EditsImpl;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityConfigurationException;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.DispatchLayer;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.DispatchStack;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.layers.Failover;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.layers.Invoke;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.layers.Parallelize;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.layers.Retry;
import net.sf.taverna.t2.workflowmodel.processor.iteration.AbstractIterationStrategyNode;
import net.sf.taverna.t2.workflowmodel.processor.iteration.CrossProduct;
import net.sf.taverna.t2.workflowmodel.processor.iteration.DotProduct;
import net.sf.taverna.t2.workflowmodel.processor.iteration.NamedInputPortNode;
import net.sf.taverna.t2.workflowmodel.processor.iteration.impl.IterationStrategyImpl;
import net.sf.taverna.t2.workflowmodel.processor.iteration.impl.IterationStrategyStackImpl;

import org.embl.ebi.escience.scufl.ConcurrencyConstraint;
import org.embl.ebi.escience.scufl.DataConstraint;
import org.embl.ebi.escience.scufl.DotNode;
import org.embl.ebi.escience.scufl.IterationStrategy;
import org.embl.ebi.escience.scufl.LeafNode;
import org.embl.ebi.escience.scufl.Port;
import org.embl.ebi.escience.scufl.ScuflModel;

/**
 * @author David Withers
 * @author Stuart Owen
 * 
 */
public class WorkflowModelTranslator {

	private Edits edits = new EditsImpl();

	private Map<org.embl.ebi.escience.scufl.Processor, Processor> processorMap = new HashMap<org.embl.ebi.escience.scufl.Processor, Processor>();

	private Map<org.embl.ebi.escience.scufl.Processor, DataflowInputPort> inputMap = new HashMap<org.embl.ebi.escience.scufl.Processor, DataflowInputPort>();

	private Map<org.embl.ebi.escience.scufl.Processor, DataflowOutputPort> outputMap = new HashMap<org.embl.ebi.escience.scufl.Processor, DataflowOutputPort>();

	private WorkflowModelTranslator() {
	}

	public static Dataflow doTranslation(ScuflModel scuflModel) throws WorkflowTranslationException {
		WorkflowModelTranslator translator = new WorkflowModelTranslator();

		Dataflow dataflow = translator.edits.createDataflow();

		try {
			translator.createInputs(scuflModel, dataflow);
			
			translator.createOutputs(scuflModel, dataflow);

			translator.createProcessors(scuflModel, dataflow);

			translator.connectDataLinks(scuflModel);

			translator.connectConditions(scuflModel);
			
		} catch (EditException e) {
			throw new WorkflowTranslationException("An error occurred trying to edit the target Dataflow whilst doing a T1 workflow translation.",e);
		} catch (ActivityConfigurationException e) {
			throw new WorkflowTranslationException("An error occurred whilst trying to configure an Activity whilst doing a T1 workflow translation.",e);
		} catch (ActivityTranslatorNotFoundException e) {
			throw new WorkflowTranslationException("An error occurred whilst trying to find a T2 ActivityTranslator for a T1 processor",e);
		}

		return dataflow;
	}

	private void createInputs(ScuflModel scuflModel, Dataflow dataflow) throws EditException {
		for (Port sourcePort : scuflModel.getWorkflowSourcePorts()) {
			
		// TODO check granular depth value
		edits.getCreateDataflowInputPortEdit(dataflow,
				sourcePort.getName(), getPortDepth(sourcePort), 0)
				.doEdit();
		for (DataflowInputPort inputPort : dataflow.getInputPorts()) {
			if (inputPort.getName().equals(sourcePort.getName())) {
				inputMap.put(sourcePort.getProcessor(), inputPort);
				break;
			}
		}
			
		}
	}

	private void createOutputs(ScuflModel scuflModel, Dataflow dataflow) throws EditException {
		for (Port sinkPort : scuflModel.getWorkflowSinkPorts()) {
			
			edits.getCreateDataflowOutputPortEdit(dataflow,
					sinkPort.getName()).doEdit();
			for (DataflowOutputPort outputPort : dataflow.getOutputPorts()) {
				if (outputPort.getName().equals(sinkPort.getName())) {
					outputMap.put(sinkPort.getProcessor(), outputPort);
					break;
				}
			}
		}
	}

	/**
	 * @param scuflModel
	 * @param dataflow
	 * @throws ActivityConfigurationException 
	 * @throws EditException 
	 * @throws ActivityTranslatorNotFoundException 
	 */
	private void createProcessors(ScuflModel scuflModel, Dataflow dataflow) throws ActivityConfigurationException, EditException, ActivityTranslatorNotFoundException {
		for (org.embl.ebi.escience.scufl.Processor t1Processor : scuflModel
				.getProcessors()) {
			
				ActivityTranslator<?> translator = ActivityTranslatorFactory
						.getTranslator(t1Processor);
				Activity<?> activity = translator.doTranslation(t1Processor);

				Processor t2Processor = edits.createProcessor(t1Processor
						.getName());
				processorMap.put(t1Processor, t2Processor);

				Edit<Processor> addActivityEdit = edits.getAddActivityEdit(
						t2Processor, activity);
				Edit<Dataflow> addProcessorEdit = edits.getAddProcessorEdit(
						dataflow, t2Processor);
				
					addActivityEdit.doEdit();
					addProcessorEdit.doEdit();

					createInputPorts(activity, t2Processor);

					createOutputPorts(activity, t2Processor);

					addDispatchLayers(t1Processor, t2Processor
							.getDispatchStack());

					setIterationStrategy(t1Processor,
							(IterationStrategyStackImpl) t2Processor
									.getIterationStrategy());	
		}
	}

	private void setIterationStrategy(
			org.embl.ebi.escience.scufl.Processor t1Processor,
			IterationStrategyStackImpl iterationStrategyStack) {
		IterationStrategy t1IterationStrategy = t1Processor
				.getIterationStrategy();
		IterationStrategyImpl t2IterationStrategy = new IterationStrategyImpl();
		if (t1IterationStrategy == null) {
			t1IterationStrategy = new IterationStrategy(t1Processor);
		}
		addIterationNode((MutableTreeNode) t1IterationStrategy.getTreeModel()
				.getRoot(), t2IterationStrategy, t2IterationStrategy
				.getTerminal());

		iterationStrategyStack.clear();
		iterationStrategyStack.addStrategy(t2IterationStrategy);
	}

	private void addIterationNode(MutableTreeNode node,
			IterationStrategyImpl t2IterationStrategy,
			AbstractIterationStrategyNode parent) {
		if (node instanceof LeafNode) {
			String nodeName = (String) ((LeafNode) node).getUserObject();
			NamedInputPortNode inputPortNode = new NamedInputPortNode(nodeName,
					0);
			inputPortNode.setParent(parent);
			t2IterationStrategy.addInput(inputPortNode);
		} else {
			AbstractIterationStrategyNode strategyNode = null;
			if (node instanceof DotNode) {
				strategyNode = new DotProduct();
			} else {
				strategyNode = new CrossProduct();
			}
			strategyNode.setParent(parent);
			for (Enumeration<?> en = node.children(); en.hasMoreElements();) {
				addIterationNode((MutableTreeNode) en.nextElement(),
						t2IterationStrategy, strategyNode);
			}
		}
	}

	private void addDispatchLayers(
			org.embl.ebi.escience.scufl.Processor t1Processor,
			DispatchStack dispatchStack) throws EditException {
		int maxJobs = t1Processor.getWorkers();
		int maxRetries = t1Processor.getRetries();
		float backoffFactor = (float) t1Processor.getBackoff();
		int initialDelay = t1Processor.getRetryDelay();
		int maxDelay = (int) (initialDelay * (Math.pow(backoffFactor, maxRetries)));

		DispatchLayer<?> parallelize = new Parallelize(maxJobs);
		DispatchLayer<?> failover = new Failover();
		DispatchLayer<?> retry = new Retry(maxRetries, initialDelay, maxDelay,
				backoffFactor);
		DispatchLayer<?> invoke = new Invoke();

		edits.getAddDispatchLayerEdit(dispatchStack, parallelize, 0).doEdit();
		edits.getAddDispatchLayerEdit(dispatchStack, failover, 1).doEdit();
		edits.getAddDispatchLayerEdit(dispatchStack, retry, 2).doEdit();
		edits.getAddDispatchLayerEdit(dispatchStack, invoke, 3).doEdit();
	}

	/**
	 * @param activity
	 * @param t2Processor
	 * @throws EditException
	 */
	private void createOutputPorts(Activity<?> activity, Processor t2Processor)
			throws EditException {
		Set<OutputPort> outputPorts = activity.getOutputPorts();
		for (OutputPort outputPort : outputPorts) {
			Edit<Processor> addOutputPortEdit = edits
					.getCreateProcessorOutputPortEdit(t2Processor, outputPort
							.getName(), outputPort.getDepth(), outputPort
							.getGranularDepth());
			addOutputPortEdit.doEdit();
			activity.getOutputPortMapping().put(outputPort.getName(),
					outputPort.getName());
		}
	}

	/**
	 * @param activity
	 * @param t2Processor
	 * @throws EditException
	 */
	private void createInputPorts(Activity<?> activity, Processor t2Processor)
			throws EditException {
		Set<InputPort> inputPorts = activity.getInputPorts();
		for (InputPort inputPort : inputPorts) {
			Edit<Processor> addInputPortEdit = edits
					.getCreateProcessorInputPortEdit(t2Processor, inputPort
							.getName(), inputPort.getDepth());
			addInputPortEdit.doEdit();
			activity.getInputPortMapping().put(inputPort.getName(),
					inputPort.getName());
		}
	}

	/**
	 * @param scuflModel
	 * @throws EditException 
	 */
	private void connectConditions(ScuflModel scuflModel) throws EditException {
		for (ConcurrencyConstraint concurrencyConstraint : scuflModel
				.getConcurrencyConstraints()) {
			Processor controlProcessor = processorMap.get(concurrencyConstraint
					.getControllingProcessor());
			Processor targetProcessor = processorMap.get(concurrencyConstraint
					.getTargetProcessor());
			Edit<OrderedPair<Processor>> addConditionEdit = edits
					.getCreateConditionEdit(controlProcessor, targetProcessor);
			
			addConditionEdit.doEdit();
		}
	}

	/**
	 * @param scuflModel
	 * @throws EditException 
	 * @throws WorkflowTranslationException 
	 */
	private void connectDataLinks(ScuflModel scuflModel) throws EditException, WorkflowTranslationException {
		for (DataConstraint dataConstraint : scuflModel.getDataConstraints()) {
			org.embl.ebi.escience.scufl.Processor source = dataConstraint
					.getSource().getProcessor();
			org.embl.ebi.escience.scufl.Processor sink = dataConstraint
					.getSink().getProcessor();
			String sourceName = dataConstraint.getSource().getName();
			String sinkName = dataConstraint.getSink().getName();

			EventForwardingOutputPort sourcePort = null;
			EventHandlingInputPort sinkPort = null;
			if (inputMap.containsKey(source)) {
				sourcePort = inputMap.get(source).getInternalOutputPort();
			} else if (processorMap.containsKey(source)) {
				sourcePort = findOutputPort(processorMap.get(source),
						sourceName);
			}
			if (processorMap.containsKey(sink)) {
				sinkPort = findInputPort(processorMap.get(sink), sinkName);
			} else if (outputMap.containsKey(sink)) {
				sinkPort = outputMap.get(sink).getInternalInputPort();
			}
			if (sourcePort != null && sinkPort != null) {
				Datalink datalink = edits.createDatalink(sourcePort, sinkPort);
				
				edits.getConnectDatalinkEdit(datalink).doEdit();
				
			} else {
				if (sourcePort == null) {
					throw new WorkflowTranslationException("The Taverna 1 sourcePort is NULL for the data constraint:"+dataConstraint);
				}
				else {
					throw new WorkflowTranslationException("The Taverna 1 sinkPort is NULL for the data constraint:"+dataConstraint);
				}
			}
		}
	}

	private EventHandlingInputPort findInputPort(Processor processor,
			String name) {
		for (EventHandlingInputPort inputPort : processor.getInputPorts()) {
			if (inputPort.getName().equals(name)) {
				return inputPort;
			}
		}
		return null;
	}

	private EventForwardingOutputPort findOutputPort(Processor processor,
			String name) {
		for (EventForwardingOutputPort outputPort : processor.getOutputPorts()) {
			if (outputPort.getName().equals(name)) {
				return outputPort;
			}
		}
		return null;
	}

	private int getPortDepth(Port port) {
		String syntacticType = port.getSyntacticType();
		if (syntacticType == null) {
			return 0;
		} else {
			return syntacticType.split("l\\(").length - 1;
		}
	}

}
