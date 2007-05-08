package net.sf.taverna.t2.workflowmodel.impl;

import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.Edit;
import net.sf.taverna.t2.workflowmodel.Edits;
import net.sf.taverna.t2.workflowmodel.Processor;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.DispatchLayer;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.DispatchStack;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.impl.AddDispatchLayerEdit;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.impl.DeleteDispatchLayerEdit;
import net.sf.taverna.t2.workflowmodel.processor.service.Service;

public class EditsImpl implements Edits {

	public Dataflow createDataflow() {
		// TODO Auto-generated method stub
		return null;
	}

	public Edit<Processor> createProcessor(Dataflow dataflow) {
		// TODO Auto-generated method stub
		return null;
	}

	public Edit<Processor> createProcessorFromService(Dataflow dataflow,
			Service<?> service) {
		// TODO Auto-generated method stub
		return null;
	}

	public Edit<DispatchStack> getAddDispatchLayerEdit(DispatchStack stack,
			DispatchLayer layer, int position) {
		return new AddDispatchLayerEdit(stack, layer, position);
	}

	public Edit<Processor> getAddServiceEdit(Processor processor,
			Service<?> service) {
		return new AddServiceEdit(processor, service);
	}

	public Edit<Processor> getCreateProcessorInputPortEdit(Processor processor,
			String portName, int portDepth) {
		return new CreateProcessorInputPortEdit(processor, portName, portDepth);
	}

	public Edit<Processor> getCreateProcessorOutputPortEdit(
			Processor processor, String portName, int portDepth,
			int granularDepth) {
		return new CreateProcessorOutputPortEdit(processor, portName, portDepth, granularDepth);
	}

	public Edit<DispatchStack> getDeleteDispatchLayerEdit(DispatchStack stack,
			DispatchLayer layer) {
		return new DeleteDispatchLayerEdit(stack, layer);
	}

	public Edit<Processor> getRenameProcessorEdit(Processor processor,
			String newName) {
		return new RenameProcessorEdit(processor, newName);
	}

}
