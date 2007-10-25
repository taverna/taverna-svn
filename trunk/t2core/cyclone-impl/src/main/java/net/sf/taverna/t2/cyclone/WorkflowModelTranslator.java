package net.sf.taverna.t2.cyclone;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.swing.tree.MutableTreeNode;

import net.sf.taverna.t2.cyclone.activity.ActivityTranslationException;
import net.sf.taverna.t2.cyclone.activity.ActivityTranslator;
import net.sf.taverna.t2.cyclone.activity.ActivityTranslatorFactory;
import net.sf.taverna.t2.cyclone.activity.ActivityTranslatorNotFoundException;
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
import net.sf.taverna.t2.workflowmodel.Merge;
import net.sf.taverna.t2.workflowmodel.MergeOutputPort;
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
import org.embl.ebi.escience.scufl.DuplicateProcessorNameException;
import org.embl.ebi.escience.scufl.IterationStrategy;
import org.embl.ebi.escience.scufl.LeafNode;
import org.embl.ebi.escience.scufl.Port;
import org.embl.ebi.escience.scufl.ProcessorCreationException;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scufl.UnknownPortException;
import org.embl.ebi.escience.scuflworkers.stringconstant.StringConstantProcessor;

/**
 * <p>
 * The WorkflowModelTranslator encapsulates all the steps involved in
 * translating a Taverna 1 ScuflModel instance to an equivalent Taverna 2
 * Dataflow instance.
 * </p>
 * <p>
 * The translation is carried out by a call to the method
 * {@link WorkflowModelTranslator#doTranslation(ScuflModel)}.
 * </p>
 * 
 * @author David Withers
 * @author Stuart Owen
 * 
 */
public class WorkflowModelTranslator {

	private Edits edits = new EditsImpl();

	private Map<org.embl.ebi.escience.scufl.Processor, Processor> processorMap = new HashMap<org.embl.ebi.escience.scufl.Processor, Processor>();

	private Map<org.embl.ebi.escience.scufl.Port, DataflowInputPort> inputMap = new HashMap<org.embl.ebi.escience.scufl.Port, DataflowInputPort>();

	private Map<org.embl.ebi.escience.scufl.Port, DataflowOutputPort> outputMap = new HashMap<org.embl.ebi.escience.scufl.Port, DataflowOutputPort>();

	private WorkflowModelTranslator() {
	}

	/**
	 * Main entry point for translating a Taverna 1 ScuflModel instance to an
	 * equivalent Taverna 2 Dataflow instance.
	 * 
	 * @param scuflModel
	 *            an instance of a Taverna 1 ScuflModel
	 * @return
	 * @throws WorkflowTranslationException
	 *             if a problem occurs translating the ScuflModel. The root
	 *             cause will provide more specific details.
	 * @throws CloneNotSupportedException 
	 */
	public static Dataflow doTranslation(ScuflModel scuflModel)
			throws WorkflowTranslationException {
		WorkflowModelTranslator translator = new WorkflowModelTranslator();

		Dataflow dataflow = translator.edits.createDataflow();

		try {
			translator.createInputs(scuflModel, dataflow);

			translator.createOutputs(scuflModel, dataflow);

			translator.createProcessors(scuflModel, dataflow);

			translator.connectDataLinks(scuflModel);

			translator.connectConditions(scuflModel);

		} catch (EditException e) {
			throw new WorkflowTranslationException(
					"An error occurred trying to edit the target Dataflow whilst doing a T1 workflow translation.",
					e);
		} catch (ActivityConfigurationException e) {
			throw new WorkflowTranslationException(
					"An error occurred whilst trying to configure an Activity whilst doing a T1 workflow translation.",
					e);
		} catch (ActivityTranslatorNotFoundException e) {
			throw new WorkflowTranslationException(
					"An error occurred whilst trying to find a T2 ActivityTranslator for a T1 processor",
					e);
		} catch (ActivityTranslationException e) {
			throw new WorkflowTranslationException(
					"An error occurred whilst translating T1 processor into a T2 activity",
					e);
		}

		return dataflow;
	}

	private void createInputs(ScuflModel scuflModel, Dataflow dataflow)
			throws EditException {
		for (Port sourcePort : scuflModel.getWorkflowSourcePorts()) {
			int portDepth = getPortDepth(sourcePort);
			edits.getCreateDataflowInputPortEdit(dataflow,
					sourcePort.getName(), portDepth, portDepth).doEdit();
			for (DataflowInputPort inputPort : dataflow.getInputPorts()) {
				if (inputPort.getName().equals(sourcePort.getName())) {
					inputMap.put(sourcePort, inputPort);
					break;
				}
			}
		}
	}

	private void createOutputs(ScuflModel scuflModel, Dataflow dataflow)
			throws EditException {
		for (Port sinkPort : scuflModel.getWorkflowSinkPorts()) {

			edits.getCreateDataflowOutputPortEdit(dataflow, sinkPort.getName())
					.doEdit();
			for (DataflowOutputPort outputPort : dataflow.getOutputPorts()) {
				if (outputPort.getName().equals(sinkPort.getName())) {
					outputMap.put(sinkPort, outputPort);
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
	 * @throws WorkflowTranslationException 
	 */
	private void createProcessors(ScuflModel scuflModel, Dataflow dataflow)
			throws ActivityTranslationException,
			ActivityConfigurationException, EditException,
			ActivityTranslatorNotFoundException, WorkflowTranslationException {
		for (org.embl.ebi.escience.scufl.Processor t1Processor : scuflModel
				.getProcessors()) {
			createProcessor(scuflModel, t1Processor, dataflow);
		}
	}

	/**
	 * @param t1Processor
	 * @param dataflow
	 * @throws ActivityTranslatorNotFoundException
	 * @throws ActivityTranslationException
	 * @throws ActivityConfigurationException
	 * @throws EditException
	 * @throws WorkflowTranslationException 
	 */
	private Processor createProcessor(ScuflModel scuflModel,
			org.embl.ebi.escience.scufl.Processor t1Processor, Dataflow dataflow)
			throws ActivityTranslatorNotFoundException,
			ActivityTranslationException, ActivityConfigurationException,
			EditException, WorkflowTranslationException {
		ActivityTranslator<?> translator = ActivityTranslatorFactory
				.getTranslator(t1Processor);
		Activity<?> activity = translator.doTranslation(t1Processor);

		Processor t2Processor = edits
				.createProcessor(t1Processor.getName());
		processorMap.put(t1Processor, t2Processor);

		Edit<Processor> addActivityEdit = edits.getAddActivityEdit(
				t2Processor, activity);
		Edit<Dataflow> addProcessorEdit = edits.getAddProcessorEdit(
				dataflow, t2Processor);

		addActivityEdit.doEdit();
		addProcessorEdit.doEdit();

		createInputPorts(scuflModel, activity, t1Processor, t2Processor, dataflow);

		createOutputPorts(activity, t2Processor);

		addDispatchLayers(t1Processor, t2Processor.getDispatchStack());

		setIterationStrategy(t1Processor, t2Processor);
		
		return t2Processor;
	}

	private void setIterationStrategy(
			org.embl.ebi.escience.scufl.Processor t1Processor,
			Processor t2Processor) {
		IterationStrategyStackImpl iterationStrategyStack = (IterationStrategyStackImpl) t2Processor
				.getIterationStrategy();
		IterationStrategy t1IterationStrategy = t1Processor
				.getIterationStrategy();
		IterationStrategyImpl t2IterationStrategy = new IterationStrategyImpl();
		if (t1IterationStrategy == null) {
			t1IterationStrategy = new IterationStrategy(t1Processor);
		}
		addIterationNode((MutableTreeNode) t1IterationStrategy.getTreeModel()
				.getRoot(), t2IterationStrategy, t2IterationStrategy
				.getTerminal(), t2Processor);

		iterationStrategyStack.clear();
		iterationStrategyStack.addStrategy(t2IterationStrategy);
	}

	private void addIterationNode(MutableTreeNode node,
			IterationStrategyImpl t2IterationStrategy,
			AbstractIterationStrategyNode parent, Processor t2Processor) {
		if (node instanceof LeafNode) {
			String nodeName = (String) ((LeafNode) node).getUserObject();
			for (InputPort ip : t2Processor.getInputPorts()) {
				if (ip.getName().equals(nodeName)) {
					NamedInputPortNode inputPortNode = new NamedInputPortNode(
							nodeName, ip.getDepth());
					inputPortNode.setParent(parent);
					t2IterationStrategy.addInput(inputPortNode);
					break;
				}
			}
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
						t2IterationStrategy, strategyNode, t2Processor);
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
		int maxDelay = (int) (initialDelay * (Math.pow(backoffFactor,
				maxRetries)));

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
	 * @throws ActivityConfigurationException 
	 * @throws ActivityTranslationException 
	 * @throws ActivityTranslatorNotFoundException 
	 * @throws WorkflowTranslationException 
	 * @throws DuplicateProcessorNameException 
	 * @throws  
	 */
	private void createInputPorts(ScuflModel scuflModel, Activity<?> activity,
			org.embl.ebi.escience.scufl.Processor t1Processor,
			Processor t2Processor, Dataflow dataflow) throws EditException, ActivityTranslatorNotFoundException, ActivityTranslationException, ActivityConfigurationException, WorkflowTranslationException {
		Set<InputPort> inputPorts = activity.getInputPorts();
		Map<String, org.embl.ebi.escience.scufl.InputPort> t1InputPorts = getInputPortMap(t1Processor);
		for (InputPort inputPort : inputPorts) {
			org.embl.ebi.escience.scufl.InputPort t1InputPort = t1InputPorts
					.get(inputPort.getName());
			if (t1InputPort.isBound() || t1InputPort.hasDefaultValue()) {
				Edit<Processor> addInputPortEdit = edits
						.getCreateProcessorInputPortEdit(t2Processor, inputPort
								.getName(), inputPort.getDepth());
				addInputPortEdit.doEdit();
				activity.getInputPortMapping().put(inputPort.getName(),
						inputPort.getName());
			}
			if (!t1InputPort.isBound() && t1InputPort.hasDefaultValue()) {
				String processorName = t2Processor.getLocalName() + "_" + inputPort.getName() + "_defaultValue";
				try {
					Processor stringConstantProcessor = createProcessor(scuflModel, new StringConstantProcessor(scuflModel, processorName, t1InputPort.getDefaultValue()), dataflow);
					Datalink datalink = edits.createDatalink(findOutputPort(stringConstantProcessor, "value"), findInputPort(t2Processor, inputPort.getName()));
					edits.getConnectDatalinkEdit(datalink).doEdit();
				} catch (ProcessorCreationException e) {
					throw new WorkflowTranslationException(e);
				} catch (DuplicateProcessorNameException e) {
					throw new WorkflowTranslationException(e);
				}
			}
		}
	}

	private Map<String, org.embl.ebi.escience.scufl.InputPort> getInputPortMap(
			org.embl.ebi.escience.scufl.Processor processor) {
		Map<String, org.embl.ebi.escience.scufl.InputPort> inputPorts = new HashMap<String, org.embl.ebi.escience.scufl.InputPort>();
		for (org.embl.ebi.escience.scufl.InputPort inputPort : processor.getInputPorts()) {
			inputPorts.put(inputPort.getName(), inputPort);
		}
		return inputPorts;
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
	private void connectDataLinks(ScuflModel scuflModel) throws EditException,
			WorkflowTranslationException {
		for (DataConstraint dataConstraint : scuflModel.getDataConstraints()) {
			org.embl.ebi.escience.scufl.InputPort scuflSinkPort = (org.embl.ebi.escience.scufl.InputPort) dataConstraint
					.getSink();
			org.embl.ebi.escience.scufl.OutputPort scuflSourcePort = (org.embl.ebi.escience.scufl.OutputPort) dataConstraint
					.getSource();
			boolean isMerge = false;
			if (scuflSinkPort.getMergeMode() == org.embl.ebi.escience.scufl.InputPort.MERGE) {
				isMerge = true;
			}

			org.embl.ebi.escience.scufl.Processor sourceProcessor = dataConstraint
					.getSource().getProcessor();
			org.embl.ebi.escience.scufl.Processor sinkProcessor = dataConstraint
					.getSink().getProcessor();
			String sourceName = dataConstraint.getSource().getName();
			String sinkName = dataConstraint.getSink().getName();

			EventForwardingOutputPort sourcePort = null;
			EventHandlingInputPort sinkPort = null;
			if (inputMap.containsKey(scuflSourcePort)) {
				sourcePort = inputMap.get(scuflSourcePort)
						.getInternalOutputPort();
			} else if (processorMap.containsKey(sourceProcessor)) {
				sourcePort = findOutputPort(processorMap.get(sourceProcessor),
						sourceName);
			}
			if (processorMap.containsKey(sinkProcessor)) {
				sinkPort = findInputPort(processorMap.get(sinkProcessor),
						sinkName);
			} else if (outputMap.containsKey(scuflSinkPort)) {
				sinkPort = outputMap.get(scuflSinkPort).getInternalInputPort();
			}
			if (sourcePort != null && sinkPort != null) {
				if (!isMerge) {
					Datalink datalink = edits.createDatalink(sourcePort,
							sinkPort);
					edits.getConnectDatalinkEdit(datalink).doEdit();
				} else {
					addMergedDatalink(sourcePort, sinkPort);
				}

			} else {
				if (sourcePort == null) {
					throw new WorkflowTranslationException(
							"The Taverna 1 sourcePort is NULL for the data constraint:"
									+ dataConstraint);
				} else {
					throw new WorkflowTranslationException(
							"The Taverna 1 sinkPort is NULL for the data constraint:"
									+ dataConstraint);
				}
			}
		}
	}

	private void addMergedDatalink(EventForwardingOutputPort sourcePort,
			EventHandlingInputPort sinkPort) throws EditException,
			WorkflowTranslationException {
		Merge merge = null;
		if (sinkPort.getIncomingLink() == null) {
			merge = edits.createMerge(sinkPort);
		} else {
			if (sinkPort.getIncomingLink().getSource() instanceof MergeOutputPort) {
				merge = ((MergeOutputPort) sinkPort.getIncomingLink()
						.getSource()).getMerge();
			} else {
				// FIXME: what to do when a Taverna 1 workflow has 2 inputs to a
				// single port that isn't a merge?? For now throw an exception
				throw new WorkflowTranslationException(
						"Unable to translate a workflow that has multiple un-merged inputs to a single port.");
			}
		}
		edits.getConnectMergedDatalinkEdit(merge, sourcePort, sinkPort)
				.doEdit();
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
