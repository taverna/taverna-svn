package net.sf.taverna.t2.workflowmodel.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;

import net.sf.taverna.t2.workflowmodel.CompoundEdit;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.Datalink;
import net.sf.taverna.t2.workflowmodel.Edit;
import net.sf.taverna.t2.workflowmodel.Edits;
import net.sf.taverna.t2.workflowmodel.EditsRegistry;
import net.sf.taverna.t2.workflowmodel.EventForwardingOutputPort;
import net.sf.taverna.t2.workflowmodel.EventHandlingInputPort;
import net.sf.taverna.t2.workflowmodel.InputPort;
import net.sf.taverna.t2.workflowmodel.Merge;
import net.sf.taverna.t2.workflowmodel.MergeInputPort;
import net.sf.taverna.t2.workflowmodel.MergeOutputPort;
import net.sf.taverna.t2.workflowmodel.OutputPort;
import net.sf.taverna.t2.workflowmodel.Processor;
import net.sf.taverna.t2.workflowmodel.ProcessorInputPort;
import net.sf.taverna.t2.workflowmodel.ProcessorOutputPort;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityInputPort;
import net.sf.taverna.t2.workflowmodel.processor.activity.NestedDataflow;

public class Tools {

	private static Edits edits = EditsRegistry.getEdits();

	/**
	 * Creates an Edit that creates a Datalink between a source and sink port
	 * and connects the Datalink.
	 * 
	 * If the sink port already has a Datalink connected this method checks if a
	 * new Merge is required and creates and connects the required Datalinks.
	 * 
	 * @param dataflow
	 *            the Dataflow to add the Datalink to
	 * @param source
	 *            the source of the Datalink
	 * @param sink
	 *            the source of the Datalink
	 * @return an Edit that creates a Datalink between a source and sink port
	 *         and connects the Datalink
	 */
	public static Edit<?> getCreateAndConnectDatalinkEdit(Dataflow dataflow,
			EventForwardingOutputPort source, EventHandlingInputPort sink) {
		Edit<?> edit = null;

		Datalink incomingLink = sink.getIncomingLink();
		if (incomingLink == null) {
			Datalink datalink = edits.createDatalink(source, sink);
			edit = edits.getConnectDatalinkEdit(datalink);
		} else {
			List<Edit<?>> editList = new ArrayList<Edit<?>>();

			Merge merge = null;
			if (incomingLink.getSource() instanceof MergeOutputPort) {
				merge = ((MergeOutputPort) incomingLink.getSource()).getMerge();
			} else {
				merge = edits.createMerge(sink);
				editList.add(edits.getAddMergeEdit(dataflow, merge));
				editList.add(edits.getDisconnectDatalinkEdit(incomingLink));
				MergeInputPort mergeInputPort = edits.createMergeInputPort(
						merge, getUniqueMergeInputPortName(merge, incomingLink
								.getSource().getName()
								+ "_toMerge", 0), incomingLink.getSink()
								.getDepth());
				editList.add(edits.getAddMergeInputPortEdit(merge,
						mergeInputPort));
				Datalink datalink = edits.createDatalink(incomingLink
						.getSource(), mergeInputPort);
				editList.add(edits.getConnectDatalinkEdit(datalink));
				datalink = edits.createDatalink(merge.getOutputPort(),
						incomingLink.getSink());
				editList.add(edits.getConnectDatalinkEdit(datalink));
			}
			MergeInputPort mergeInputPort = edits.createMergeInputPort(merge,
					getUniqueMergeInputPortName(merge, source.getName()
							+ "_toMerge", 0), sink.getDepth());
			editList.add(edits.getAddMergeInputPortEdit(merge, mergeInputPort));
			Datalink datalink = edits.createDatalink(source, mergeInputPort);
			editList.add(edits.getConnectDatalinkEdit(datalink));

			edit = new CompoundEdit(editList);
		}

		return edit;
	}

	public static Edit<?> getMoveDatalinkSinkEdit(Dataflow dataflow,
			Datalink datalink, EventHandlingInputPort sink) {
		List<Edit<?>> editList = new ArrayList<Edit<?>>();
		editList.add(edits.getDisconnectDatalinkEdit(datalink));
		if (datalink.getSink() instanceof ProcessorInputPort) {
			editList
					.add(getRemoveProcessorInputPortEdit((ProcessorInputPort) datalink
							.getSink()));
		}
		editList.add(getCreateAndConnectDatalinkEdit(dataflow, datalink
				.getSource(), sink));
		return new CompoundEdit(editList);
	}

	public static Edit<?> getDisconnectDatalinkAndRemovePortsEdit(
			Datalink datalink) {
		List<Edit<?>> editList = new ArrayList<Edit<?>>();
		editList.add(edits.getDisconnectDatalinkEdit(datalink));
		if (datalink.getSource() instanceof ProcessorOutputPort) {
			ProcessorOutputPort processorOutputPort = (ProcessorOutputPort) datalink
					.getSource();
			if (processorOutputPort.getOutgoingLinks().size() == 1) {
				editList
						.add(getRemoveProcessorOutputPortEdit(processorOutputPort));
			}
		}
		if (datalink.getSink() instanceof ProcessorInputPort) {
			editList
					.add(getRemoveProcessorInputPortEdit((ProcessorInputPort) datalink
							.getSink()));
		}
		return new CompoundEdit(editList);
	}

	public static Edit<?> getRemoveProcessorOutputPortEdit(
			ProcessorOutputPort port) {
		List<Edit<?>> editList = new ArrayList<Edit<?>>();
		Processor processor = port.getProcessor();
		editList.add(edits.getRemoveProcessorOutputPortEdit(
				port.getProcessor(), port));
		for (Activity<?> activity : processor.getActivityList()) {
			editList.add(edits.getRemoveActivityOutputPortMappingEdit(activity,
					port.getName()));
		}
		return new CompoundEdit(editList);
	}

	public static Edit<?> getRemoveProcessorInputPortEdit(
			ProcessorInputPort port) {
		List<Edit<?>> editList = new ArrayList<Edit<?>>();
		Processor processor = port.getProcessor();
		editList.add(edits.getRemoveProcessorInputPortEdit(port.getProcessor(),
				port));
		for (Activity<?> activity : processor.getActivityList()) {
			editList.add(edits.getRemoveActivityInputPortMappingEdit(activity,
					port.getName()));
		}
		return new CompoundEdit(editList);
	}

	public static ProcessorInputPort getProcessorInputPort(Processor processor,
			Activity<?> activity, InputPort activityInputPort) {
		ProcessorInputPort result = null;
		for (Entry<String, String> mapEntry : activity.getInputPortMapping()
				.entrySet()) {
			if (mapEntry.getValue().equals(activityInputPort.getName())) {
				for (ProcessorInputPort processorInputPort : processor
						.getInputPorts()) {
					if (processorInputPort.getName().equals(mapEntry.getKey())) {
						result = processorInputPort;
						break;
					}
				}
				break;
			}
		}
		return result;
	}

	public static ProcessorOutputPort getProcessorOutputPort(
			Processor processor, Activity<?> activity,
			OutputPort activityOutputPort) {
		ProcessorOutputPort result = null;
		for (Entry<String, String> mapEntry : activity.getOutputPortMapping()
				.entrySet()) {
			if (mapEntry.getValue().equals(activityOutputPort.getName())) {
				for (ProcessorOutputPort processorOutputPort : processor
						.getOutputPorts()) {
					if (processorOutputPort.getName().equals(mapEntry.getKey())) {
						result = processorOutputPort;
						break;
					}
				}
				break;
			}
		}
		return result;
	}

	public static ActivityInputPort getActivityInputPort(Activity<?> activity,
			String portName) {
		ActivityInputPort activityInputPort = null;
		for (ActivityInputPort inputPort : activity.getInputPorts()) {
			if (inputPort.getName().equals(portName)) {
				activityInputPort = inputPort;
				break;
			}
		}
		return activityInputPort;
	}

	public static OutputPort getActivityOutputPort(Activity<?> activity,
			String portName) {
		OutputPort activityOutputPort = null;
		for (OutputPort outputPort : activity.getOutputPorts()) {
			if (outputPort.getName().equals(portName)) {
				activityOutputPort = outputPort;
				break;
			}
		}
		return activityOutputPort;
	}

	private static String getUniqueMergeInputPortName(Merge merge, String name,
			int count) {
		String uniqueName = name + count;
		for (MergeInputPort mergeInputPort : merge.getInputPorts()) {
			if (mergeInputPort.getName().equals(uniqueName)) {
				return getUniqueMergeInputPortName(merge, name, ++count);
			}
		}
		return uniqueName;
	}

	public static Collection<Processor> getProcessorsWithActivity(
			Dataflow dataflow, Activity<?> activity) {
		Set<Processor> processors = new HashSet<Processor>();
		for (Processor processor : dataflow.getProcessors()) {
			if (processor.getActivityList().contains(activity)) {
				processors.add(processor);
			}
		}
		return processors;

	}

	public static Collection<Processor> getProcessorsWithActivityInputPort(
			Dataflow dataflow, ActivityInputPort inputPort) {
		
		Set<Processor> processors = new HashSet<Processor>();
		for (Processor processor : dataflow.getProcessors()) {	
			/*
			// Does it contain a nested workflow?
			if (processor.getActivityList().get(0) instanceof NestedDataflow){
				// Get the nested workflow
				Dataflow nestedWorkflow = ((NestedDataflow) processor.getActivityList().get(0))
						.getNestedDataflow();
				Collection<Processor> nested_processors = getProcessorsWithActivityInputPort(
						nestedWorkflow, inputPort);
				if (!nested_processors.isEmpty())
					processors.addAll(nested_processors);
			}
			else {*/
				for (Activity<?> activity : processor.getActivityList()) {
					if (activity.getInputPorts().contains(inputPort)) {
						processors.add(processor);
					}
				}
			//}
		}
		return processors;
	}
}
