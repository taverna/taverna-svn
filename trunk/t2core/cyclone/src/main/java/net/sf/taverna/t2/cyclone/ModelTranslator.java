package net.sf.taverna.t2.cyclone;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import net.sf.taverna.t2.cyclone.translators.ServiceTranslator;
import net.sf.taverna.t2.cyclone.translators.ServiceTranslatorFactory;
import net.sf.taverna.t2.cyclone.translators.ServiceTranslatorNotFoundException;
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

import org.embl.ebi.escience.scufl.ConcurrencyConstraint;
import org.embl.ebi.escience.scufl.DataConstraint;
import org.embl.ebi.escience.scufl.ScuflModel;

/**
 * @author David Withers
 * @author Stuart Owen
 *
 */
public class ModelTranslator {

	private static Edits edits = new EditsImpl();

	public static Dataflow doTranslation(ScuflModel scuflModel) {

		Map<org.embl.ebi.escience.scufl.Processor, Processor> processorMap = new HashMap<org.embl.ebi.escience.scufl.Processor, Processor>();

		Dataflow dataflow = edits.createDataflow();

		createProcessors(scuflModel, processorMap, dataflow);

		connectDataLinks(scuflModel, processorMap);

		connectConditions(scuflModel, processorMap);

		return dataflow;
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
				ServiceTranslator<?> translator = ServiceTranslatorFactory
						.getTranslator(t1Processor.getClass());
				Activity<?> service = translator.doTranslation(t1Processor);
				Edit<Processor> addProcessorEdit = edits
						.createProcessorFromService(dataflow, service);
				try {
					Processor t2Processor = addProcessorEdit.doEdit();
					processorMap.put(t1Processor, t2Processor);

					createInputPorts(service, t2Processor);

					createOutputPorts(service, t2Processor);

				} catch (EditException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} catch (ServiceTranslatorNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * @param service
	 * @param t2Processor
	 * @throws EditException
	 */
	private static void createOutputPorts(Activity<?> service,
			Processor t2Processor) throws EditException {
		Set<OutputPort> outputPorts = service.getOutputPorts();
		for (OutputPort outputPort : outputPorts) {
			Edit<Processor> addOutputPortEdit = edits
					.getCreateProcessorOutputPortEdit(t2Processor, outputPort
							.getName(), outputPort.getDepth(), outputPort
							.getGranularDepth());
			addOutputPortEdit.doEdit();
		}
	}

	/**
	 * @param service
	 * @param t2Processor
	 * @throws EditException
	 */
	private static void createInputPorts(Activity<?> service,
			Processor t2Processor) throws EditException {
		Set<InputPort> inputPorts = service.getInputPorts();
		for (InputPort inputPort : inputPorts) {
			Edit<Processor> addInputPortEdit = edits
					.getCreateProcessorInputPortEdit(t2Processor, inputPort
							.getName(), inputPort.getDepth());
			addInputPortEdit.doEdit();
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

	// private static int getPortDepth(Port port) {
	// String syntacticType = port.getSyntacticType();
	// if (syntacticType == null) {
	// return 0;
	// } else {
	// return syntacticType.split("l(").length - 1;
	// }
	// }
}
