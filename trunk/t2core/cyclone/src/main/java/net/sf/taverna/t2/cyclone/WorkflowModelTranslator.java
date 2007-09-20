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
import net.sf.taverna.t2.workflowmodel.Edit;
import net.sf.taverna.t2.workflowmodel.EditException;
import net.sf.taverna.t2.workflowmodel.Edits;
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

	private static Edits edits = new EditsImpl();

	public static Dataflow doTranslation(ScuflModel scuflModel) {

		Map<org.embl.ebi.escience.scufl.Processor, Processor> processorMap = new HashMap<org.embl.ebi.escience.scufl.Processor, Processor>();

		Dataflow dataflow = edits.createDataflow();

		createInputs(scuflModel, dataflow);

		createOutputs(scuflModel, dataflow);

		createProcessors(scuflModel, processorMap, dataflow);

		connectDataLinks(scuflModel, processorMap);

		connectConditions(scuflModel, processorMap);

		return dataflow;
	}

	private static void createInputs(ScuflModel scuflModel, Dataflow dataflow) {
		for (Port sourcePort : scuflModel.getWorkflowSourcePorts()) {
			try {
				// TODO check granular depth value
				edits.getCreateDataflowInputPortEdit(dataflow,
						sourcePort.getName(), getPortDepth(sourcePort), 0)
						.doEdit();
			} catch (EditException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private static void createOutputs(ScuflModel scuflModel, Dataflow dataflow) {
		for (Port sinkPort : scuflModel.getWorkflowSinkPorts()) {
			try {
				edits.getCreateDataflowOutputPortEdit(dataflow,
						sinkPort.getName()).doEdit();
			} catch (EditException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * @param scuflModel
	 * @param processorMap
	 * @param dataflow
	 */
	private static void createProcessors(ScuflModel scuflModel,
			Map<org.embl.ebi.escience.scufl.Processor, Processor> processorMap,
			Dataflow dataflow) {
		for (org.embl.ebi.escience.scufl.Processor t1Processor : scuflModel
				.getProcessors()) {
			try {
				ActivityTranslator<?> translator = ActivityTranslatorFactory
						.getTranslator(t1Processor);
				Activity<?> activity = translator.doTranslation(t1Processor);
				Edit<Processor> addProcessorEdit = edits
						.createProcessor(dataflow);
				try {
					Processor t2Processor = addProcessorEdit.doEdit();
					processorMap.put(t1Processor, t2Processor);

					createInputPorts(activity, t2Processor);

					createOutputPorts(activity, t2Processor);

					addDispatchLayers(t1Processor, t2Processor
							.getDispatchStack());

				} catch (EditException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} catch (ActivityTranslatorNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ActivityConfigurationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private static void setIterationStrategy(
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

	private static void addIterationNode(MutableTreeNode node,
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

	private static void addDispatchLayers(
			org.embl.ebi.escience.scufl.Processor t1Processor,
			DispatchStack dispatchStack) throws EditException {
		int maxJobs = t1Processor.getWorkers();
		int maxRetries = t1Processor.getRetries();
		long backoffFactor = (long) t1Processor.getBackoff();
		int initialDelay = t1Processor.getRetryDelay();
		int maxDelay = (int) (initialDelay * (backoffFactor ^ maxRetries));

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
	private static void createOutputPorts(Activity<?> activity,
			Processor t2Processor) throws EditException {
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
	private static void createInputPorts(Activity<?> activity,
			Processor t2Processor) throws EditException {
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
	 * @param processorMap
	 */
	private static void connectConditions(ScuflModel scuflModel,
			Map<org.embl.ebi.escience.scufl.Processor, Processor> processorMap) {
		for (ConcurrencyConstraint concurrencyConstraint : scuflModel
				.getConcurrencyConstraints()) {
			Processor controlProcessor = processorMap.get(concurrencyConstraint
					.getControllingProcessor());
			Processor targetProcessor = processorMap.get(concurrencyConstraint
					.getTargetProcessor());
			Edit<OrderedPair<Processor>> addConditionEdit = edits
					.getCreateConditionEdit(controlProcessor, targetProcessor);
			try {
				addConditionEdit.doEdit();
			} catch (EditException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * @param scuflModel
	 * @param processorMap
	 */
	private static void connectDataLinks(ScuflModel scuflModel,
			Map<org.embl.ebi.escience.scufl.Processor, Processor> processorMap) {
		for (DataConstraint dataConstraint : scuflModel.getDataConstraints()) {
			// TODO add dataflow inputs
			Processor sourceProcessor = processorMap.get(dataConstraint
					.getSource().getProcessor());
			String portName = dataConstraint.getSource().getName();
			Processor sinkProcessor = processorMap.get(dataConstraint.getSink()
					.getProcessor());
			for (EventHandlingInputPort inputPort : sinkProcessor
					.getInputPorts()) {
				if (inputPort.getName().equals(
						dataConstraint.getSink().getName())) {
					Edit<Processor> addLinkEdit = edits
							.getConnectProcessorOutputEdit(sourceProcessor,
									portName, inputPort);
					try {
						addLinkEdit.doEdit();
					} catch (EditException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					break;
				}
			}
		}
	}

	private static int getPortDepth(Port port) {
		String syntacticType = port.getSyntacticType();
		if (syntacticType == null) {
			return 0;
		} else {
			return syntacticType.split("l(").length - 1;
		}
	}

}
