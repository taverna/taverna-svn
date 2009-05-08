package net.sf.taverna.t2.platform.taverna.impl;

import net.sf.taverna.t2.platform.taverna.WorkflowEditKit;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.DataflowInputPort;
import net.sf.taverna.t2.workflowmodel.DataflowOutputPort;
import net.sf.taverna.t2.workflowmodel.Datalink;
import net.sf.taverna.t2.workflowmodel.EditException;
import net.sf.taverna.t2.workflowmodel.Edits;
import net.sf.taverna.t2.workflowmodel.EventForwardingOutputPort;
import net.sf.taverna.t2.workflowmodel.EventHandlingInputPort;
import net.sf.taverna.t2.workflowmodel.InputPort;
import net.sf.taverna.t2.workflowmodel.OutputPort;
import net.sf.taverna.t2.workflowmodel.Processor;
import net.sf.taverna.t2.workflowmodel.ProcessorInputPort;
import net.sf.taverna.t2.workflowmodel.ProcessorOutputPort;
import net.sf.taverna.t2.workflowmodel.impl.AddProcessorInputPortEdit;
import net.sf.taverna.t2.workflowmodel.impl.AddProcessorOutputPortEdit;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;

/**
 * Implementation of WorkflowEditKit
 * 
 * @author Tom Oinn
 * 
 */
public class WorkflowEditKitImpl implements WorkflowEditKit {

	private Edits edits;

	public WorkflowEditKitImpl() {
		//
	}

	public void setEdits(Edits e) {
		this.edits = e;
	}

	public Processor createDefaultProcessor(Activity<?> activity, String name)
			throws EditException {
		Processor processor = edits.createProcessor(name);
		edits.getDefaultDispatchStackEdit(processor).doEdit();

		// Add the Activity to the processor
		edits.getAddActivityEdit(processor, activity).doEdit();
		// Create processor inputs and outputs corresponding to activity inputs
		// and outputs and set the mappings in the Activity object.
		activity.getInputPortMapping().clear();
		activity.getOutputPortMapping().clear();
		for (InputPort ip : activity.getInputPorts()) {
			ProcessorInputPort pip = edits.createProcessorInputPort(processor,
					ip.getName(), ip.getDepth());
			new AddProcessorInputPortEdit(processor, pip).doEdit();
			activity.getInputPortMapping().put(ip.getName(), ip.getName());
		}
		for (OutputPort op : activity.getOutputPorts()) {
			ProcessorOutputPort pop = edits.createProcessorOutputPort(
					processor, op.getName(), op.getDepth(), op
							.getGranularDepth());
			new AddProcessorOutputPortEdit(processor, pop).doEdit();
			activity.getOutputPortMapping().put(op.getName(), op.getName());
		}
		return processor;
	}

	public Edits getEdits() {
		return this.edits;
	}

	public void connect(Dataflow workflow, String outputName, String inputName)
			throws EditException {
		// First locate the ports.

		EventForwardingOutputPort source = null;
		EventHandlingInputPort sink = null;

		String[] split = outputName.split("\\.");
		if (split.length == 1) {
			// Find dataflow input port and get internal port
			for (DataflowInputPort dip : workflow.getInputPorts()) {
				if (dip.getName().equals(split[0])) {
					source = dip.getInternalOutputPort();
					break;
				}
			}
		} else if (split.length == 2) {
			Processor sourceProcessor = null;
			for (Processor p : workflow.getEntities(Processor.class)) {
				if (p.getLocalName().equals(split[0])) {
					sourceProcessor = p;
					break;
				}
			}
			if (sourceProcessor != null) {
				for (EventForwardingOutputPort op : sourceProcessor.getOutputPorts()) {
					if (op.getName().equals(split[1])) {
						source = op;
						break;
					}
				}
			}
		}
		if (source == null) {
			throw new EditException("Unable to locate source port "
					+ outputName);
		}

		split = inputName.split("\\.");
		if (split.length == 1) {
			// Find dataflow input port and get internal port
			for (DataflowOutputPort dop : workflow.getOutputPorts()) {
				if (dop.getName().equals(split[0])) {
					sink = dop.getInternalInputPort();
					break;
				}
			}
		} else if (split.length == 2) {
			Processor sinkProcessor = null;
			for (Processor p : workflow.getEntities(Processor.class)) {
				if (p.getLocalName().equals(split[0])) {
					sinkProcessor = p;
					break;
				}
			}
			if (sinkProcessor != null) {
				for (EventHandlingInputPort ip : sinkProcessor.getInputPorts()) {
					if (ip.getName().equals(split[1])) {
						sink = ip;
						break;
					}
				}
			}
		}
		if (sink == null) {
			throw new EditException("Unable to locate sink port " + inputName);
		}
		if (sink.getIncomingLink() != null) {
			throw new EditException("Input port '" + inputName
					+ "' already has a link attached");
		}
		Datalink datalink = edits.createDatalink(source, sink);
		edits.getConnectDatalinkEdit(datalink).doEdit();
	}

}
