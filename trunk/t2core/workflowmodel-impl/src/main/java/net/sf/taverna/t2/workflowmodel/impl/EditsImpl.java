package net.sf.taverna.t2.workflowmodel.impl;

import java.util.List;

import net.sf.taverna.t2.annotation.AddAnnotationAssertionEdit;
import net.sf.taverna.t2.annotation.AnnotationAssertion;
import net.sf.taverna.t2.annotation.AnnotationBeanSPI;
import net.sf.taverna.t2.annotation.AnnotationChain;
import net.sf.taverna.t2.annotation.AnnotationRole;
import net.sf.taverna.t2.annotation.AnnotationSourceSPI;
import net.sf.taverna.t2.annotation.CurationEvent;
import net.sf.taverna.t2.annotation.Person;
import net.sf.taverna.t2.cloudone.refscheme.ReferenceScheme;
import net.sf.taverna.t2.facade.WorkflowInstanceFacade;
import net.sf.taverna.t2.facade.impl.WorkflowInstanceFacadeImpl;
import net.sf.taverna.t2.invocation.InvocationContext;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.DataflowInputPort;
import net.sf.taverna.t2.workflowmodel.DataflowOutputPort;
import net.sf.taverna.t2.workflowmodel.Datalink;
import net.sf.taverna.t2.workflowmodel.Edit;
import net.sf.taverna.t2.workflowmodel.Edits;
import net.sf.taverna.t2.workflowmodel.EventForwardingOutputPort;
import net.sf.taverna.t2.workflowmodel.EventHandlingInputPort;
import net.sf.taverna.t2.workflowmodel.Merge;
import net.sf.taverna.t2.workflowmodel.OrderedPair;
import net.sf.taverna.t2.workflowmodel.OutputPort;
import net.sf.taverna.t2.workflowmodel.Processor;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityInputPort;
import net.sf.taverna.t2.workflowmodel.processor.activity.impl.ActivityInputPortImpl;
import net.sf.taverna.t2.workflowmodel.processor.activity.impl.ActivityOutputPortImpl;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.DispatchLayer;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.DispatchStack;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.impl.AddDispatchLayerEdit;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.impl.DeleteDispatchLayerEdit;

public class EditsImpl implements Edits {

	public Dataflow createDataflow() {
		return new DataflowImpl();
	}

	public Datalink createDatalink(EventForwardingOutputPort source,
			EventHandlingInputPort sink) {
		return new DatalinkImpl(source, sink);
	}

	public Edit<Dataflow> getAddProcessorEdit(Dataflow dataflow,
			Processor processor) {
		return new AddProcessorEdit(dataflow, processor);
	}

	public Edit<Dataflow> getAddMergeEdit(Dataflow dataflow,
			Merge merge) {
		return new AddMergeEdit(dataflow, merge);
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
		return new CreateProcessorOutputPortEdit(processor, portName,
				portDepth, granularDepth);
	}

	public Edit<Dataflow> getCreateDataflowInputPortEdit(Dataflow dataflow,
			String portName, int portDepth, int granularDepth) {
		return new CreateDataflowInputPortEdit(dataflow, portName, portDepth,
				granularDepth);
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

	public Edit<DataflowInputPort> getRenameDataflowInputPortEdit(DataflowInputPort dataflowInputPort,
			String newName) {
		return new RenameDataflowInputPortEdit(dataflowInputPort, newName);
	}

	public Edit<DataflowOutputPort> getRenameDataflowOutputPortEdit(DataflowOutputPort dataflowOutputPort,
			String newName) {
		return new RenameDataflowOutputPortEdit(dataflowOutputPort, newName);
	}
	
	public Edit<Processor> getConnectProcessorOutputEdit(Processor processor,
			String outputPortName, EventHandlingInputPort targetPort) {
		return new ConnectProcessorOutputEdit(processor, outputPortName,
				targetPort);
	}

	public Edit<Datalink> getConnectDatalinkEdit(Datalink datalink) {
		return new ConnectDatalinkEdit(datalink);
	}
	
	
	public Edit<AnnotationChain> getAddAnnotationAssertionEdit(AnnotationChain annotationChain, AnnotationAssertion annotationAssertion) {
		return new AddAnnotationAssertionEdit(annotationChain, annotationAssertion);
	}

	/**
	 * Creates a MergeImpl instance, using the sinkPort to generate its name,
	 * which is the name of the port appended with 'Merge'.
	 */
	public Merge createMerge(EventHandlingInputPort sinkPort) {
		String mergeName = sinkPort.getName() + "Merge";
		return new MergeImpl(mergeName);
	}

	/**
	 * @return a new instance of ConnectMergedDatalinkEdit constructed from the
	 *         provided parameters.
	 * 
	 * @param merge
	 *            a Merge instance
	 * @param sourcePort
	 *            the source port from which a link is to be created.
	 * @param sinkPort
	 *            the sink port to which the link is to be created.
	 */
	public Edit<Merge> getConnectMergedDatalinkEdit(Merge merge,
			EventForwardingOutputPort sourcePort,
			EventHandlingInputPort sinkPort) {
		return new ConnectMergedDatalinkEdit(merge, sourcePort, sinkPort);
	}

	public Edit<OrderedPair<Processor>> getCreateConditionEdit(
			Processor control, Processor target) {
		return new CreateConditionEdit(control, target);
	}

	public Edit<OrderedPair<Processor>> getRemoveConditionEdit(
			Processor control, Processor target) {
		return new RemoveConditionEdit(control, target);
	}

	public Processor createProcessor(String name) {
		ProcessorImpl processor = new ProcessorImpl();
		processor.setName(name);
		return processor;
	}

	/**
	 * Builds an instance of {@link ActivityInputPortImpl}
	 */
	public ActivityInputPort buildActivityInputPort(String portName,
			int portDepth, boolean allowsLiteralValues,
			List<Class<? extends ReferenceScheme<?>>> handledReferenceSchemes,
			Class<?> translatedElementClass) {
		return new ActivityInputPortImpl(portName, portDepth,
				allowsLiteralValues, handledReferenceSchemes,
				translatedElementClass);
	}

	/**
	 * Builds an instance of {@link ActivityOutputPortImpl}
	 */
	public OutputPort buildActivityOutputPort(String portName, int portDepth,
			int portGranularDepth) {
		return new ActivityOutputPortImpl(portName, portDepth,
				portGranularDepth);
	}

	public WorkflowInstanceFacade createWorkflowInstanceFacade(
			Dataflow dataflow, InvocationContext context, String parentProcess) {
		return new WorkflowInstanceFacadeImpl(dataflow, context, parentProcess);
	}

	public Edit<AnnotationAssertion> getAddAnnotationBean(
			AnnotationAssertion annotationAssertion,
			AnnotationBeanSPI annotationBean) {
		return new AddAnnotationBeanEdit(annotationAssertion, annotationBean);
	}

	public Edit<AnnotationAssertion> getAddCurationEvent(
			AnnotationAssertion annotationAssertion, CurationEvent curationEvent) {
		return new AddCurationEventEdit(annotationAssertion, curationEvent);
	}

	public Edit<AnnotationAssertion> getAddAnnotationRole(
			AnnotationAssertion annotationAssertion,
			AnnotationRole annotationRole) {
		// TODO Auto-generated method stub
		return new AddAnnotationRoleEdit(annotationAssertion, annotationRole);
	}

	public Edit<AnnotationAssertion> getAddAnnotationSource(
			AnnotationAssertion annotationAssertion,
			AnnotationSourceSPI annotationSource) {
		return new AddAnnotationSourceEdit(annotationAssertion, annotationSource);
	}

	public Edit<AnnotationAssertion> getAddCreator(
			AnnotationAssertion annotationAssertion, Person person) {
		// TODO Auto-generated method stub
		return new AddCreatorEdit(annotationAssertion, person);
	}

	public Edit<Dataflow> getUpdateDataflowNameEdit(Dataflow dataflow,
			String newName) {
		return new UpdateDataflowNameEdit(dataflow,newName);
	}
	
	public Edit<Dataflow> getUpdateDataflowInternalIdentifierEdit(Dataflow dataflow,
			String newId) {
		return new UpdateDataflowInternalIdentifierEdit(dataflow,newId);
	}

}
