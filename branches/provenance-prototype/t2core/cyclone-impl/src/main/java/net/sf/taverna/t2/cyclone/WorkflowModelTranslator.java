package net.sf.taverna.t2.cyclone;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

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
import net.sf.taverna.t2.workflowmodel.EditsRegistry;
import net.sf.taverna.t2.workflowmodel.EventForwardingOutputPort;
import net.sf.taverna.t2.workflowmodel.EventHandlingInputPort;
import net.sf.taverna.t2.workflowmodel.InputPort;
import net.sf.taverna.t2.workflowmodel.Merge;
import net.sf.taverna.t2.workflowmodel.MergeOutputPort;
import net.sf.taverna.t2.workflowmodel.OrderedPair;
import net.sf.taverna.t2.workflowmodel.OutputPort;
import net.sf.taverna.t2.workflowmodel.Processor;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityConfigurationException;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityInputPort;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.DispatchLayer;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.DispatchStack;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.layers.Failover;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.layers.Invoke;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.layers.Parallelize;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.layers.Provenance;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.layers.Retry;
import net.sf.taverna.t2.workflowmodel.processor.iteration.AbstractIterationStrategyNode;
import net.sf.taverna.t2.workflowmodel.processor.iteration.CrossProduct;
import net.sf.taverna.t2.workflowmodel.processor.iteration.DotProduct;
import net.sf.taverna.t2.workflowmodel.processor.iteration.NamedInputPortNode;
import net.sf.taverna.t2.workflowmodel.processor.iteration.impl.IterationStrategyImpl;
import net.sf.taverna.t2.workflowmodel.processor.iteration.impl.IterationStrategyStackImpl;

import org.apache.log4j.Logger;
import org.embl.ebi.escience.scufl.AlternateProcessor;
import org.embl.ebi.escience.scufl.ConcurrencyConstraint;
import org.embl.ebi.escience.scufl.DataConstraint;
import org.embl.ebi.escience.scufl.DataConstraintCreationException;
import org.embl.ebi.escience.scufl.DotNode;
import org.embl.ebi.escience.scufl.DuplicateProcessorNameException;
import org.embl.ebi.escience.scufl.IterationStrategy;
import org.embl.ebi.escience.scufl.LeafNode;
import org.embl.ebi.escience.scufl.Port;
import org.embl.ebi.escience.scufl.ProcessorCreationException;
import org.embl.ebi.escience.scufl.ScuflModel;
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
	
	private static Logger logger = Logger.getLogger(WorkflowModelTranslator.class);

	private Edits edits = EditsRegistry.getEdits();

	private Map<org.embl.ebi.escience.scufl.Processor, Processor> processorMap = new HashMap<org.embl.ebi.escience.scufl.Processor, Processor>();

	private Map<org.embl.ebi.escience.scufl.Port, DataflowInputPort> inputMap = new HashMap<org.embl.ebi.escience.scufl.Port, DataflowInputPort>();

	private Map<org.embl.ebi.escience.scufl.Port, DataflowOutputPort> outputMap = new HashMap<org.embl.ebi.escience.scufl.Port, DataflowOutputPort>();

	private ScuflModel scuflModel;

	private WorkflowModelTranslator(ScuflModel scuflModel) {
		this.scuflModel = scuflModel;
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
		WorkflowModelTranslator translator;
		try {
			translator = new WorkflowModelTranslator(scuflModel.clone());
		} catch (CloneNotSupportedException e) {
			throw new WorkflowTranslationException(
					"The scufl model could not be cloned", e);
		}

		Dataflow dataflow = translator.edits.createDataflow();

		try {

			translator.replaceDefaultsWithStringConstants();

			translator.createInputs(dataflow);

			translator.createOutputs(dataflow);

			translator.createProcessors(dataflow);

			translator.connectDataLinks(dataflow);

			translator.connectConditions();

		} catch (EditException e) {
			throw new WorkflowTranslationException(
					"An error occurred trying to edit the target Dataflow whilst doing a T1 workflow translation.",
					e);
		} catch (ActivityConfigurationException e) {
			throw new WorkflowTranslationException(
					"An error occurred whilst trying to configure an Activity whilst doing a T1 workflow translation.",
					e);
		} catch (ActivityTranslatorNotFoundException e) {
			org.embl.ebi.escience.scufl.Processor p = e.getTaverna1Processor();
			String msg = "An error occurred whilst trying to find a T2 ActivityTranslator for a T1 processor";
			if (p != null) {
				msg += " [" + p.getClass().getSimpleName() + ":" + p.getName()
						+ "]";
			}
			throw new WorkflowTranslationException(msg, e);
		} catch (ActivityTranslationException e) {
			throw new WorkflowTranslationException("An error occurred whilst translating T1 processor into a T2 activity", e);
		}

		return dataflow;
	}

	/**
	 * Crawls the scuflModel processors and checks their input ports for unbound
	 * default values. If one is found then a StringConstantProcessor is
	 * inserted upstream.
	 * 
	 * @param scuflModel
	 * @throws WorkflowTranslationException
	 */
	private void replaceDefaultsWithStringConstants()
			throws WorkflowTranslationException {
		for (org.embl.ebi.escience.scufl.Processor t1Processor : scuflModel
				.getProcessors()) {
			for (org.embl.ebi.escience.scufl.InputPort t1InputPort : t1Processor
					.getInputPorts()) {
				if (!t1InputPort.isBound()
						&& t1InputPort.getDefaultValue() != null) {
					String processorName = t1Processor.getName() + "_"
							+ sanitiseName(t1InputPort.getName()) + "_defaultValue";
					try {
						org.embl.ebi.escience.scufl.Processor stringConstantProcessor = new StringConstantProcessor(
								scuflModel, processorName, t1InputPort
										.getDefaultValue());
						scuflModel.addProcessor(stringConstantProcessor);
						DataConstraint constraint = new DataConstraint(
								scuflModel, stringConstantProcessor
										.getOutputPorts()[0], t1InputPort);
						scuflModel.addDataConstraint(constraint);
					} catch (ProcessorCreationException e) {
						throw new WorkflowTranslationException(e);
					} catch (DuplicateProcessorNameException e) {
						throw new WorkflowTranslationException(e);
					} catch (DataConstraintCreationException e) {
						throw new WorkflowTranslationException(e);
					}
				}
			}
		}

	}

	/**
	 * Checks that the name does not have any characters that are invalid for a processor
	 * name. 
	 * 
	 * The name must contain only the chars[A-Za-z_0-9].
	 * 
	 * @param name the original name
	 * @return the sanitised name
	 */
	private String sanitiseName(String name) {
		String result=name;
		if (Pattern.matches("\\w++", name) == false) {
			result="";
			for (char c : name.toCharArray()) {
				if (Character.isLetterOrDigit(c) || c=='_') {
					result+=c;
				}
			}
		}
		return result;
	}

	private void createInputs(Dataflow dataflow) throws EditException {
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

	private void createOutputs(Dataflow dataflow) throws EditException {
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
	private void createProcessors(Dataflow dataflow)
			throws ActivityTranslationException,
			ActivityConfigurationException, EditException,
			ActivityTranslatorNotFoundException, WorkflowTranslationException {
		for (org.embl.ebi.escience.scufl.Processor t1Processor : scuflModel
				.getProcessors()) {
			createProcessor(t1Processor, dataflow);
		}
	}

	private Processor createProcessor(
			org.embl.ebi.escience.scufl.Processor t1Processor, Dataflow dataflow)
			throws ActivityTranslatorNotFoundException,
			ActivityTranslationException, ActivityConfigurationException,
			EditException, WorkflowTranslationException {
		// create a T2 processor
		Processor t2Processor = edits.createProcessor(t1Processor.getName());
		// add the t2 processor to the dataflow
		edits.getAddProcessorEdit(dataflow, t2Processor).doEdit();
		processorMap.put(t1Processor, t2Processor);

		// create the t2 activity
		Activity<?> activity = createActivity(t1Processor);
		// add the t2 activity to the t2 processor
		edits.getAddActivityEdit(t2Processor, activity).doEdit();

		// add any alternate processors
		for (AlternateProcessor alternateProcesor : t1Processor
				.getAlternatesArray()) {
			org.embl.ebi.escience.scufl.Processor alternateT1Processor = alternateProcesor
					.getProcessor();
			// create the alternate t2 activity
			Activity<?> alternateActivity = createActivity(alternateT1Processor);
			// add the input mappings
			for (Map.Entry<String, String> entry : alternateProcesor
					.getInputMapping().entrySet()) {
				alternateActivity.getInputPortMapping().put(entry.getKey(),
						entry.getValue());
			}
			// add the output mappings
			for (Map.Entry<String, String> entry : alternateProcesor
					.getOutputMapping().entrySet()) {
				alternateActivity.getOutputPortMapping().put(entry.getValue(),
						entry.getKey());
			}
			// add the alternate t2 activity to the t2 processor
			edits.getAddActivityEdit(t2Processor, alternateActivity).doEdit();
		}

		addInputPorts(activity, t1Processor, t2Processor);

		addOutputPorts(activity, t2Processor);

		addDispatchLayers(t1Processor, t2Processor.getDispatchStack());

		setIterationStrategy(t1Processor, t2Processor);

		return t2Processor;
	}

	private Activity<?> createActivity(
			org.embl.ebi.escience.scufl.Processor t1Processor)
			throws ActivityTranslationException,
			ActivityConfigurationException, ActivityTranslatorNotFoundException {
		// find the translator
		ActivityTranslator<?> translator = ActivityTranslatorFactory
				.getTranslator(t1Processor);
		// translate the t1 processor to a t2 activity
		return translator.doTranslation(t1Processor);
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
		//new provenance layer
		DispatchLayer<?> provenance = new Provenance();
		DispatchLayer<?> parallelize = new Parallelize(maxJobs);
		DispatchLayer<?> failover = new Failover();
		DispatchLayer<?> retry = new Retry(maxRetries, initialDelay, maxDelay,
				backoffFactor);
		DispatchLayer<?> invoke = new Invoke();
		
		//new provenance layer
		edits.getAddDispatchLayerEdit(dispatchStack, provenance, 0).doEdit();
		edits.getAddDispatchLayerEdit(dispatchStack, parallelize, 1).doEdit();
		edits.getAddDispatchLayerEdit(dispatchStack, failover, 2).doEdit();
		edits.getAddDispatchLayerEdit(dispatchStack, retry, 3).doEdit();
		edits.getAddDispatchLayerEdit(dispatchStack, invoke, 4).doEdit();
	}

	/**
	 * @param activity
	 * @param t2Processor
	 * @throws EditException
	 */
	private void addOutputPorts(Activity<?> activity, Processor t2Processor)
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
	private void addInputPorts(Activity<?> activity,
			org.embl.ebi.escience.scufl.Processor t1Processor,
			Processor t2Processor) throws EditException,
			ActivityTranslatorNotFoundException, ActivityTranslationException,
			ActivityConfigurationException, WorkflowTranslationException {
		Set<ActivityInputPort> inputPorts = activity.getInputPorts();
		Map<String, org.embl.ebi.escience.scufl.InputPort> t1InputPorts = getInputPortMap(t1Processor);
		for (InputPort inputPort : inputPorts) {
			org.embl.ebi.escience.scufl.InputPort t1InputPort = t1InputPorts
					.get(inputPort.getName());
			if (t1InputPort.isBound()) {
				Edit<Processor> addInputPortEdit = edits
						.getCreateProcessorInputPortEdit(t2Processor, inputPort
								.getName(), inputPort.getDepth());
				addInputPortEdit.doEdit();
				activity.getInputPortMapping().put(inputPort.getName(),
						inputPort.getName());
			}
		}
	}

	private Map<String, org.embl.ebi.escience.scufl.InputPort> getInputPortMap(
			org.embl.ebi.escience.scufl.Processor processor) {
		Map<String, org.embl.ebi.escience.scufl.InputPort> inputPorts = new HashMap<String, org.embl.ebi.escience.scufl.InputPort>();
		for (org.embl.ebi.escience.scufl.InputPort inputPort : processor
				.getInputPorts()) {
			inputPorts.put(inputPort.getName(), inputPort);
		}
		return inputPorts;
	}

	/**
	 * @param scuflModel
	 * @throws EditException
	 */
	private void connectConditions() throws EditException {
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
	private void connectDataLinks(Dataflow targetFlow) throws EditException,
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
					addMergedDatalink(sourcePort, sinkPort, targetFlow);
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
			EventHandlingInputPort sinkPort, Dataflow targetFlow) throws EditException,
			WorkflowTranslationException {
		Merge merge = null;
		if (sinkPort.getIncomingLink() == null) {
			merge = edits.createMerge(sinkPort);
			// Add to the dataflow
			edits.getAddMergeEdit(targetFlow, merge).doEdit();
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
