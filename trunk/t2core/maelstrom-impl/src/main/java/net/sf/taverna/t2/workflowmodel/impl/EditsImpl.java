package net.sf.taverna.t2.workflowmodel.impl;

import net.sf.taverna.t2.annotation.Annotated;
import net.sf.taverna.t2.annotation.WorkflowAnnotation;
import net.sf.taverna.t2.annotation.impl.AddAnnotationEdit;
import net.sf.taverna.t2.annotation.impl.RemoveAnnotationEdit;
import net.sf.taverna.t2.annotation.impl.ReplaceAnnotationEdit;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.Edit;
import net.sf.taverna.t2.workflowmodel.Edits;
import net.sf.taverna.t2.workflowmodel.EventHandlingInputPort;
import net.sf.taverna.t2.workflowmodel.OrderedPair;
import net.sf.taverna.t2.workflowmodel.Processor;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.DispatchLayer;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.DispatchStack;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.impl.AddDispatchLayerEdit;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.impl.DeleteDispatchLayerEdit;

public class EditsImpl implements Edits {

	public Dataflow createDataflow() {
		return new DataflowImpl();
	}

	public Edit<Dataflow> getAddProcessorEdit(Dataflow dataflow, Processor processor) {
		return new AddProcessorEdit(dataflow,processor);
	}

	public Edit<Processor> createProcessorFromActivity(Dataflow dataflow,
			Activity<?> activity) {
		// TODO Auto-generated method stub
		return null;
	}

	public Edit<DispatchStack> getAddDispatchLayerEdit(DispatchStack stack,
			DispatchLayer<?> layer, int position) {
		return new AddDispatchLayerEdit(stack, layer, position);
	}

	public Edit<Processor> getAddActivityEdit(Processor processor,
			Activity<?> activity) {
		return new AddActivityEdit(processor, activity);
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

	public Edit<Dataflow> getCreateDataflowInputPortEdit(Dataflow dataflow,
			String portName, int portDepth, int granularDepth) {
		return new CreateDataflowInputPortEdit(dataflow, portName, portDepth, granularDepth);
	}

	public Edit<Dataflow> getCreateDataflowOutputPortEdit(Dataflow dataflow,
			String portName) {
		return new CreateDataflowOutputPortEdit(dataflow, portName);
	}

	public Edit<DispatchStack> getDeleteDispatchLayerEdit(DispatchStack stack,
			DispatchLayer<?> layer) {
		return new DeleteDispatchLayerEdit(stack, layer);
	}

	public Edit<Processor> getRenameProcessorEdit(Processor processor,
			String newName) {
		return new RenameProcessorEdit(processor, newName);
	}

	public Edit<Processor> getConnectProcessorOutputEdit(Processor processor, String outputPortName, EventHandlingInputPort targetPort) {
		return new ConnectProcessorOutputEdit(processor, outputPortName, targetPort);
	}

	public Edit<OrderedPair<Processor>> getCreateConditionEdit(Processor control, Processor target) {
		return new CreateConditionEdit(control, target);
	}

	public Edit<OrderedPair<Processor>> getRemoveConditionEdit(Processor control, Processor target) {
		return new RemoveConditionEdit(control, target);
	}

	public <TargetType extends Annotated> Edit<TargetType> getAddAnnotationEdit(WorkflowAnnotation newAnnotation, TargetType objectToAnnotate) {
		return new AddAnnotationEdit<TargetType>(objectToAnnotate, newAnnotation);
	}

	public <TargetType extends Annotated> Edit<TargetType> getRemoveAnnotationEdit(WorkflowAnnotation annotationToRemove, TargetType objectToAnnotate) {
		return new RemoveAnnotationEdit<TargetType>(objectToAnnotate, annotationToRemove);
	}

	public <TargetType extends Annotated> Edit<TargetType> getReplaceAnnotationEdit(WorkflowAnnotation oldAnnotation, WorkflowAnnotation newAnnotation, TargetType objectToAnnotate) {
		return new ReplaceAnnotationEdit<TargetType>(objectToAnnotate, oldAnnotation, newAnnotation);
	}

	public Processor createProcessor(String name) {
		ProcessorImpl processor = new ProcessorImpl();
		processor.setName(name);
		return processor;
	}

}
