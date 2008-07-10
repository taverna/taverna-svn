package net.sf.taverna.t2.workflowmodel.impl;

import java.util.ArrayList;
import java.util.List;

import net.sf.taverna.t2.annotation.AddAnnotationAssertionEdit;
import net.sf.taverna.t2.annotation.Annotated;
import net.sf.taverna.t2.annotation.AnnotationAssertion;
import net.sf.taverna.t2.annotation.AnnotationAssertionImpl;
import net.sf.taverna.t2.annotation.AnnotationBeanSPI;
import net.sf.taverna.t2.annotation.AnnotationChain;
import net.sf.taverna.t2.annotation.AnnotationChainImpl;
import net.sf.taverna.t2.annotation.AnnotationRole;
import net.sf.taverna.t2.annotation.AnnotationSourceSPI;
import net.sf.taverna.t2.annotation.CurationEvent;
import net.sf.taverna.t2.annotation.Person;
import net.sf.taverna.t2.facade.WorkflowInstanceFacade;
import net.sf.taverna.t2.facade.impl.WorkflowInstanceFacadeImpl;
import net.sf.taverna.t2.invocation.InvocationContext;
import net.sf.taverna.t2.reference.ExternalReferenceSPI;
import net.sf.taverna.t2.workflowmodel.CompoundEdit;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.DataflowInputPort;
import net.sf.taverna.t2.workflowmodel.DataflowOutputPort;
import net.sf.taverna.t2.workflowmodel.Datalink;
import net.sf.taverna.t2.workflowmodel.Edit;
import net.sf.taverna.t2.workflowmodel.Edits;
import net.sf.taverna.t2.workflowmodel.EventForwardingOutputPort;
import net.sf.taverna.t2.workflowmodel.EventHandlingInputPort;
import net.sf.taverna.t2.workflowmodel.Merge;
import net.sf.taverna.t2.workflowmodel.MergeInputPort;
import net.sf.taverna.t2.workflowmodel.OrderedPair;
import net.sf.taverna.t2.workflowmodel.OutputPort;
import net.sf.taverna.t2.workflowmodel.Processor;
import net.sf.taverna.t2.workflowmodel.ProcessorInputPort;
import net.sf.taverna.t2.workflowmodel.ProcessorOutputPort;
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

	public DataflowInputPort createDataflowInputPort(String name, int depth, int granularDepth, Dataflow dataflow) {
		return new DataflowInputPortImpl(name, depth, granularDepth, dataflow);
	}

	public DataflowOutputPort createDataflowOutputPort(String name, Dataflow dataflow) {
		return new DataflowOutputPortImpl(name, dataflow);
	}

	public MergeInputPort createMergeInputPort(Merge merge, String name,
			int depth) {
		if (merge instanceof MergeImpl) {
			return new MergeInputPortImpl((MergeImpl) merge, name, depth);
		} else {
			return null;
		}
	}
	
	public ProcessorOutputPort createProcessorOutputPort(Processor processor,String name,int depth, int granularDepth) {
		return new ProcessorOutputPortImpl((ProcessorImpl)processor,name,depth,granularDepth);
	}
	
	public ProcessorInputPort createProcessorInputPort(Processor processor, String name,int depth) {
		return new ProcessorInputPortImpl((ProcessorImpl)processor,name,depth);
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

	public Edit<Processor> getAddProcessorInputPortEdit(Processor processor,
			ProcessorInputPort port) {
		return new AddProcessorInputPortEdit(processor, port);
	}

	public Edit<Processor> getAddProcessorOutputPortEdit(
			Processor processor, ProcessorOutputPort port) {
		return new AddProcessorOutputPortEdit(processor, port);
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
		return new ConnectProcesorOutputEdit(processor, outputPortName,
				targetPort);
	}

	public Edit<Datalink> getConnectDatalinkEdit(Datalink datalink) {
		return new ConnectDatalinkEdit(datalink);
	}
	
	
	@SuppressWarnings("unchecked")
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
	public ActivityInputPort createActivityInputPort(String portName,
			int portDepth, boolean allowsLiteralValues,
			List<Class<? extends ExternalReferenceSPI>> handledReferenceSchemes,
			Class<?> translatedElementClass) {
		return new ActivityInputPortImpl(portName, portDepth,
				allowsLiteralValues, handledReferenceSchemes,
				translatedElementClass);
	}

	/**
	 * Builds an instance of {@link ActivityOutputPortImpl}
	 */
	public OutputPort createActivityOutputPort(String portName, int portDepth,
			int portGranularDepth) {
		return new ActivityOutputPortImpl(portName, portDepth,
				portGranularDepth);
	}

	public WorkflowInstanceFacade createWorkflowInstanceFacade(
			Dataflow dataflow, InvocationContext context, String parentProcess) {
		return new WorkflowInstanceFacadeImpl(dataflow, context, parentProcess);
	}

	@SuppressWarnings("unchecked")
	public Edit<AnnotationAssertion> getAddAnnotationBean(
			AnnotationAssertion annotationAssertion,
			AnnotationBeanSPI annotationBean) {
		return new AddAnnotationBeanEdit(annotationAssertion, annotationBean);
	}

	@SuppressWarnings("unchecked")
	public Edit<AnnotationAssertion> getAddCurationEvent(
			AnnotationAssertion annotationAssertion, CurationEvent curationEvent) {
		return new AddCurationEventEdit(annotationAssertion, curationEvent);
	}

	@SuppressWarnings("unchecked")
	public Edit<AnnotationAssertion> getAddAnnotationRole(
			AnnotationAssertion annotationAssertion,
			AnnotationRole annotationRole) {
		return new AddAnnotationRoleEdit(annotationAssertion, annotationRole);
	}

	@SuppressWarnings("unchecked")
	public Edit<AnnotationAssertion> getAddAnnotationSource(
			AnnotationAssertion annotationAssertion,
			AnnotationSourceSPI annotationSource) {
		return new AddAnnotationSourceEdit(annotationAssertion, annotationSource);
	}

	@SuppressWarnings("unchecked")
	public Edit<AnnotationAssertion> getAddCreator(
			AnnotationAssertion annotationAssertion, Person person) {
		return new AddCreatorEdit(annotationAssertion, person);
	}

	public Edit<?> getAddAnnotationChainEdit(Annotated<?> annotated, AnnotationBeanSPI annotation) {
		List<Edit<?>> editList = new ArrayList<Edit<?>>();

		AnnotationAssertion<?> annotationAssertion =  new AnnotationAssertionImpl();
		editList.add(getAddAnnotationBean(annotationAssertion, annotation));

		AnnotationChain annotationChain = new AnnotationChainImpl();
		editList.add(getAddAnnotationAssertionEdit(annotationChain, annotationAssertion));

		editList.add(annotated.getAddAnnotationEdit(annotationChain));

		return new CompoundEdit(editList);
	}
	
	public Edit<Dataflow> getUpdateDataflowNameEdit(Dataflow dataflow,
			String newName) {
		return new UpdateDataflowNameEdit(dataflow,newName);
	}
	
	public Edit<Dataflow> getUpdateDataflowInternalIdentifierEdit(Dataflow dataflow,
			String newId) {
		return new UpdateDataflowInternalIdentifierEdit(dataflow,newId);
	}

	public Edit<Datalink> getDisconnectDatalinkEdit(Datalink datalink) {
		return new DisconnectDatalinkEdit(datalink);
	}

	public Edit<Dataflow> getRemoveDataflowInputPortEdit(Dataflow dataflow,
			DataflowInputPort dataflowInputPort) {
		return new RemoveDataflowInputPortEdit(dataflow, dataflowInputPort);
	}

	public Edit<Dataflow> getRemoveDataflowOutputPortEdit(Dataflow dataflow,
			DataflowOutputPort dataflowOutputPort) {
		return new RemoveDataflowOutputPortEdit(dataflow, dataflowOutputPort);
	}

	public Edit<Dataflow> getRemoveProcessorEdit(Dataflow dataflow,
			Processor processor) {
		return new RemoveProcessorEdit(dataflow, processor);
	}

	public Edit<Dataflow> getAddDataflowInputPortEdit(Dataflow dataflow,
			DataflowInputPort dataflowInputPort) {
		return new AddDataflowInputPortEdit(dataflow, dataflowInputPort);
	}

	public Edit<Dataflow> getAddDataflowOutputPortEdit(Dataflow dataflow,
			DataflowOutputPort dataflowOutputPort) {
		return new AddDataflowOutputPortEdit(dataflow, dataflowOutputPort);
	}

	public Edit<Activity<?>> getAddActivityInputPortEdit(Activity<?> activity,
			ActivityInputPort activityInputPort) {
		return new AddActivityInputPortEdit(activity, activityInputPort);
	}

	public Edit<Activity<?>> getAddActivityOutputPortEdit(Activity<?> activity,
			OutputPort activityOutputPort) {
		return new AddActivityOutputPortEdit(activity, activityOutputPort);
	}

	public Edit<Activity<?>> getRemoveActivityInputPortEdit(Activity<?> activity,
			ActivityInputPort activityInputPort) {
		return new RemoveActivityInputPortEdit(activity, activityInputPort);
	}

	public Edit<Activity<?>> getRemoveActivityOutputPortEdit(Activity<?> activity,
			OutputPort activityOutputPort) {
		return new RemoveActivityOutputPortEdit(activity, activityOutputPort);
	}

	public Edit<Merge> getAddMergeInputPortEdit(Merge merge,
			MergeInputPort mergeInputPort) {
		return new AddMergeInputPortEdit(merge, mergeInputPort);
	}

	public Edit<Activity<?>> getConfigureActivityEdit(Activity<?> activity,
			Object configurationBean) {
		return new ConfigureActivityEdit(activity,configurationBean);
	}

	public Edit<Processor> getRemoveProcessorInputPortEdit(Processor processor,
			ProcessorInputPort port) {
		return new RemoveProcessorInputPortEdit(processor,port);
	}

	public Edit<Processor> getRemoveProcessorOutputPortEdit(
			Processor processor, ProcessorOutputPort port) {
		return new RemoveProcessorOutputPortEdit(processor,port);
	}

	public Edit<Processor> getMapProcessorPortsForActivityEdit(
			Processor processor) {
		return new MapProcessorPortsForActivityEdit(processor);
	}

}
