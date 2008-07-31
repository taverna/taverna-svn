package net.sf.taverna.t2.activities.wsdl.xmlsplitter;

import java.util.ArrayList;
import java.util.List;

import net.sf.taverna.t2.activities.wsdl.WSDLActivity;
import net.sf.taverna.t2.workflowmodel.CompoundEdit;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.Datalink;
import net.sf.taverna.t2.workflowmodel.Edit;
import net.sf.taverna.t2.workflowmodel.EditException;
import net.sf.taverna.t2.workflowmodel.Edits;
import net.sf.taverna.t2.workflowmodel.EventForwardingOutputPort;
import net.sf.taverna.t2.workflowmodel.EventHandlingInputPort;
import net.sf.taverna.t2.workflowmodel.Processor;
import net.sf.taverna.t2.workflowmodel.impl.AbstractDataflowEdit;
import net.sf.taverna.t2.workflowmodel.impl.DataflowImpl;
import net.sf.taverna.t2.workflowmodel.impl.EditsImpl;
import net.sf.taverna.t2.workflowmodel.impl.Tools;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;
import net.sf.taverna.wsdl.parser.TypeDescriptor;

public class AddXMLSplitterEdit extends AbstractDataflowEdit {

	private final Activity<?> activity;
	private final String portName;
	private final boolean isInput;
	private final Object oldBean = null;
	private CompoundEdit compoundEdit1 = null;
	private Edit<?> linkUpEdit;

	public AddXMLSplitterEdit(Dataflow dataflow, Activity<?> activity,
			String portName, boolean isInput) {
		super(dataflow);
		this.activity = activity;
		this.portName = portName;
		this.isInput = isInput;

	}

	@Override
	protected void doEditAction(DataflowImpl dataflow) throws EditException {
		List<Edit<?>> editList = new ArrayList<Edit<?>>();
		Edits edits = new EditsImpl();

		Activity<?> splitter = null;
		String sourcePortName = "";
		Processor sourceProcessor = null;

		String sinkPortName = "";
		Processor sinkProcessor = null;

		String name = Tools.uniqueProcessorName(portName + "XML", dataflow);
		Processor splitterProcessor = edits.createProcessor(name);

		Processor activityProcessor = findProcessorForActivity(dataflow,
				activity);
		if (activityProcessor == null) {
			throw new EditException(
					"Cannot find the processor that the activity belongs to");
		}
		try {
			if (activity instanceof XMLInputSplitterActivity) {
				if (!isInput) {
					throw new EditException(
							"Can only add an input splitter to another input splitter");
				}
				TypeDescriptor descriptor = ((XMLInputSplitterActivity) activity)
						.getTypeDescriptorForInputPort(portName);
				XMLSplitterConfigurationBean bean = XMLSplitterConfigurationBeanBuilder
						.buildBeanForInput(descriptor);
				splitter = new XMLInputSplitterActivity();
				editList.add(edits.getConfigureActivityEdit(splitter, bean));
				
			} else if (activity instanceof XMLOutputSplitterActivity) {
				if (isInput) {
					throw new EditException(
							"Can only add an output splitter to another output splitter");
				}
				TypeDescriptor descriptor = ((XMLOutputSplitterActivity) activity)
						.getTypeDescriptorForOutputPort(portName);
				XMLSplitterConfigurationBean bean = XMLSplitterConfigurationBeanBuilder
						.buildBeanForInput(descriptor);
				splitter = new XMLInputSplitterActivity();
				editList.add(edits.getConfigureActivityEdit(splitter, bean));
				
			} else if (activity instanceof WSDLActivity) {

				if (isInput) {
					TypeDescriptor descriptor = ((WSDLActivity) activity)
							.getTypeDescriptorForInputPort(portName);
					XMLSplitterConfigurationBean bean = XMLSplitterConfigurationBeanBuilder
							.buildBeanForInput(descriptor);
					splitter = new XMLInputSplitterActivity();
					editList
							.add(edits.getConfigureActivityEdit(splitter, bean));
				} else {
					TypeDescriptor descriptor = ((WSDLActivity) activity)
							.getTypeDescriptorForOutputPort(portName);
					XMLSplitterConfigurationBean bean = XMLSplitterConfigurationBeanBuilder
							.buildBeanForOutput(descriptor);
					splitter = new XMLOutputSplitterActivity();
					editList
							.add(edits.getConfigureActivityEdit(splitter, bean));
				}
			} else {
				throw new EditException(
						"The activity type is not suitable for adding xml processing processors");
			}
		} catch (Exception e) {
			throw new EditException(
					"An error occured whilst tyring to add an XMLSplitter to the activity:"
							+ activity, e);
		}
		
		if (isInput) {
			sourcePortName = "output";
			sinkPortName = portName;
			sinkProcessor = activityProcessor;
			sourceProcessor = splitterProcessor;
		}
		else {
			sourcePortName = portName;
			sinkPortName = "input";
			sinkProcessor = splitterProcessor;
			sourceProcessor = activityProcessor;
		}

		editList.add(edits.getDefaultDispatchStackEdit(splitterProcessor));
		editList.add(edits.getAddActivityEdit(splitterProcessor, splitter));
		editList.add(edits
				.getMapProcessorPortsForActivityEdit(splitterProcessor));
		editList.add(edits.getAddProcessorEdit(dataflow, splitterProcessor));

		compoundEdit1 = new CompoundEdit(editList);
		compoundEdit1.doEdit();

		EventForwardingOutputPort source = getSourcePort(sourceProcessor,
				sourcePortName);
		EventHandlingInputPort sink = getSinkPort(sinkProcessor, sinkPortName);

		if (source == null)
			throw new EditException(
					"Unable to find the source port when linking up "
							+ sourcePortName + " to " + sinkPortName);
		if (sink == null)
			throw new EditException(
					"Unable to find the sink port when linking up "
							+ sourcePortName + " to " + sinkPortName);

		Datalink link = edits.createDatalink(source, sink);
		linkUpEdit = edits.getConnectDatalinkEdit(link);
		linkUpEdit.doEdit();

	}

	private EventHandlingInputPort getSinkPort(Processor sinkProcessor,
			String sinkPortName) {
		for (EventHandlingInputPort port : sinkProcessor.getInputPorts()) {
			if (port.getName().equals(sinkPortName)) {
				return port;
			}
		}
		return null;
	}

	private EventForwardingOutputPort getSourcePort(Processor sourceProcessor,
			String sourcePortName) {
		for (EventForwardingOutputPort port : sourceProcessor.getOutputPorts()) {
			if (port.getName().equals(sourcePortName)) {
				return port;
			}
		}
		return null;
	}

	@Override
	protected void undoEditAction(DataflowImpl dataflow) {
		if (linkUpEdit.isApplied())
			linkUpEdit.undo();
		if (compoundEdit1.isApplied())
			compoundEdit1.undo();
	}

	private Processor findProcessorForActivity(Dataflow dataflow,
			Activity<?> activity) {
		for (Processor p : dataflow.getProcessors()) {
			for (Activity<?> a : p.getActivityList()) {
				if (a == activity)
					return p;
			}
		}
		return null;
	}

}
