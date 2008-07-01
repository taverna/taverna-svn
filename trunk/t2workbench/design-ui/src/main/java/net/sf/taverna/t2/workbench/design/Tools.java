package net.sf.taverna.t2.workbench.design;

import java.util.ArrayList;
import java.util.List;

import net.sf.taverna.t2.workflowmodel.CompoundEdit;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.Datalink;
import net.sf.taverna.t2.workflowmodel.Edit;
import net.sf.taverna.t2.workflowmodel.Edits;
import net.sf.taverna.t2.workflowmodel.EditsRegistry;
import net.sf.taverna.t2.workflowmodel.EventForwardingOutputPort;
import net.sf.taverna.t2.workflowmodel.EventHandlingInputPort;
import net.sf.taverna.t2.workflowmodel.Merge;
import net.sf.taverna.t2.workflowmodel.MergeInputPort;
import net.sf.taverna.t2.workflowmodel.MergeOutputPort;

public class Tools {

	private static Edits edits = EditsRegistry.getEdits();

	/**
	 * Creates an Edit that creates a Datalink between a source and sink port
	 * and connects the Datalink.
	 * 
	 * If the sink port already has a Datalink connected this method checks if
	 * a new Merge is required and creates and connects the required Datalinks.
	 * 
	 * @param dataflow the Dataflow to add the Datalink to
	 * @param source the source of the Datalink
	 * @param sink the source of the Datalink
	 * @return an Edit that creates a Datalink between a source and sink port
	 *         and connects the Datalink
	 */
	public static Edit<?> createAndConnectDatalinkEdit(Dataflow dataflow,
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
}
