package net.sf.taverna.t2.workflowmodel;

import java.util.List;

import net.sf.taverna.t2.annotation.AnnotationAssertion;
import net.sf.taverna.t2.annotation.AnnotationBeanSPI;
import net.sf.taverna.t2.annotation.AnnotationChain;
import net.sf.taverna.t2.annotation.AnnotationRole;
import net.sf.taverna.t2.annotation.AnnotationSourceSPI;
import net.sf.taverna.t2.annotation.CurationEvent;
import net.sf.taverna.t2.annotation.Person;
import net.sf.taverna.t2.cloudone.refscheme.ReferenceScheme;
import net.sf.taverna.t2.facade.WorkflowInstanceFacade;
import net.sf.taverna.t2.invocation.InvocationContext;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityInputPort;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.DispatchLayer;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.DispatchStack;

/**
 * Defines the set of all available edit actions over a workflow model. This is
 * the only point at which you can modify any of the entities in the workflow
 * object model, the rest of this API is purely read only.
 * <p>
 * In theory this would be some kind of static interface but Java doesn't have
 * this as a concept so the pattern here will be to discover an appropriate
 * implementation of this interface from whatever version of the implementation
 * package you want to use, instantiate it then use the methods defined here to
 * construct and manipulate the workflow model.
 * 
 * @author Tom Oinn
 * @author Stuart Owen
 * @author David Withers
 * 
 */
public interface Edits {

	/**
	 * Build a new Dataflow workflow
	 * 
	 * @return
	 */
	public Dataflow createDataflow();

	/**
	 * Build a new WorkflowInstanceFacade using the supplied Dataflow
	 * 
	 * @param dataflow
	 * @param context
	 * @return an instance of a WorkflowInstanceFacade
	 * 
	 * @see WorkflowInstanceFacade
	 */
	public WorkflowInstanceFacade createWorkflowInstanceFacade(
			Dataflow dataflow, InvocationContext context, String parentProcess);

	/**
	 * Builds a new instance of a Processor with the given name
	 * 
	 * @param the
	 *            local name for the processor.
	 */
	public Processor createProcessor(String name);

	/**
	 * Add an {@link AnnotationAssertion} to an {@link AnnotationChain}
	 * 
	 * @param annotationChain
	 * @param annotationAssertion
	 * @return an {@link Edit}able object with undo feature
	 */
	public Edit<AnnotationChain> getAddAnnotationAssertionEdit(
			AnnotationChain annotationChain,
			AnnotationAssertion annotationAssertion);

	/**
	 * Builds a new Datalink with the given source and sink ports
	 * 
	 * @param source
	 *            the source port
	 * @param sink
	 *            the sink port
	 * @return a new Datalink instance
	 */
	public Datalink createDatalink(EventForwardingOutputPort source,
			EventHandlingInputPort sink);

	/**
	 * Provides an edit object responsible for adding a Processor to a Dataflow
	 * 
	 * @param dataflow
	 *            the dataflow to add this processor to
	 * @param processor
	 *            the processor to be added to the dataflow
	 */
	public Edit<Dataflow> getAddProcessorEdit(Dataflow dataflow,
			Processor processor);

	public Edit<Dataflow> getAddMergeEdit(Dataflow dataflow, Merge processor);

	/**
	 * Create a new processor in the specified dataflow configured as a default
	 * Taverna 1 style activity with output and input ports matching those of
	 * the Activity instance supplied, a default cross product iteration
	 * strategy and a dispatch stack consisting of a parallelize, failover,
	 * retry and invoke layer set.
	 * 
	 * @param dataflow
	 *            the dataflow to add this processor to
	 * @param activity
	 *            a single activity to build the processor around
	 */
	public Edit<Processor> createProcessorFromActivity(Dataflow dataflow,
			Activity<?> activity);

	/**
	 * Connect the output port of the specified processor to a target input
	 * port. To connect multiple inputs use this method multiple times with
	 * different targetPort arguments.
	 * 
	 * @param processor
	 *            Processor to link from
	 * @param outputPortName
	 *            Name of the output port within the specified processor to link
	 *            from
	 * @param targetPort
	 *            Input port (specifically an EventHandlingInputPort) to forward
	 *            data events to.
	 */
	public Edit<Processor> getConnectProcessorOutputEdit(Processor processor,
			String outputPortName, EventHandlingInputPort targetPort);
	
	
	
	/**
	 * Add an {@link AnnotationBeanSPI} to an {@link AnnotationAssertion}
	 * @param annotationAssertion
	 * @param annotationBean
	 * @return the edit which has do/undo functionality
	 */
	public Edit<AnnotationAssertion> getAddAnnotationBean(AnnotationAssertion annotationAssertion, AnnotationBeanSPI annotationBean);
	
	public Edit<AnnotationAssertion> getAddCurationEvent(AnnotationAssertion annotationAssertion, CurationEvent curationEvent);

	public Edit<AnnotationAssertion> getAddCreator(AnnotationAssertion annotationAssertion, Person person);
	
	public Edit<AnnotationAssertion> getAddAnnotationRole(AnnotationAssertion annotationAssertion, AnnotationRole annotationRole);
	
	public Edit<AnnotationAssertion> getAddAnnotationSource(AnnotationAssertion annotationAssertion, AnnotationSourceSPI annotationSource);
	/**
	 * Connect a datalink to its source and sink.
	 * 
	 * @param datalink
	 *            the datalink to connect
	 * @return a datalink edit
	 */
	public Edit<Datalink> getConnectDatalinkEdit(Datalink datalink);

	/**
	 * Creates and returns an instance of an Edit<Merge> that is responsible
	 * for generating the links to an from the Merge instance to link together
	 * the source and sink port via the merge instance.
	 * 
	 * @return a new instance of Edit<Merge> constructed from the provided
	 *         parameters.
	 * 
	 * @param merge
	 *            a Merge instance
	 * @param sourcePort
	 *            the source port from which a link is to be created.
	 * @param sinkPort
	 *            the sink port to which the link is to be created.
	 * 
	 * @see Merge
	 */
	public Edit<Merge> getConnectMergedDatalinkEdit(Merge merge,
			EventForwardingOutputPort sourcePort,
			EventHandlingInputPort sinkPort);

	/**
	 * @param sinkPort
	 * @return an instance of Merge, using the sink port to generate the Merge
	 *         name.
	 * 
	 * @see Merge
	 */
	public Merge createMerge(EventHandlingInputPort sinkPort);

	/**
	 * Add a new layer to the specified dispatch stack
	 * 
	 * @param stack
	 *            Stack to add to
	 * @param layer
	 *            New dispatch layer to add
	 * @param position
	 *            Where to add the new layer? 0 is at the top of the stack.
	 */
	public Edit<DispatchStack> getAddDispatchLayerEdit(DispatchStack stack,
			DispatchLayer<?> layer, int position);

	/**
	 * Remove a dispatch layer from its dispatch stack
	 * 
	 * @param stack
	 *            The stack from which to remove the layer
	 * @param layer
	 *            The layer to remove
	 */
	public Edit<DispatchStack> getDeleteDispatchLayerEdit(DispatchStack stack,
			DispatchLayer<?> layer);

	/**
	 * Add an Activity implementation to the set of activities within a
	 * Processor
	 * 
	 * @param processor
	 *            Processor to add the activity to
	 * @param activity
	 *            Activity to add
	 */
	public Edit<Processor> getAddActivityEdit(Processor processor,
			Activity<?> activity);

	/**
	 * Build a new input port on a processor, creating matching ports in the
	 * iteration strategy or strategies as a side effect.
	 * 
	 * @param processor
	 *            processor to add the port to
	 * @param portName
	 *            name of the port, unique in the set of processor input ports
	 * @param portDepth
	 *            the conceptual depth of collections consumed by this input
	 *            port
	 */
	public Edit<Processor> getCreateProcessorInputPortEdit(Processor processor,
			String portName, int portDepth);

	/**
	 * Build a new output port on a processor
	 * 
	 * @param processor
	 *            processor to add the new output port to
	 * @param portName
	 *            name of the output port, unique within the set of processor
	 *            output ports
	 * @param portDepth
	 *            conceptual depth of collections emitted from this port
	 * @param granularDepth
	 *            granular depth, lowest collection depth that can be emitted
	 *            within a stream
	 */
	public Edit<Processor> getCreateProcessorOutputPortEdit(
			Processor processor, String portName, int portDepth,
			int granularDepth);

	/**
	 * Add an input port to a dataflow.
	 * 
	 * @param dataflow
	 *            dataflow to add the port to
	 * @param portName
	 *            name of the port, unique in the dataflow
	 * @param portDepth
	 *            the conceptual depth of collections consumed by this input
	 *            port
	 * @param granularDepth
	 *            granular depth to copy to the internal output port
	 */
	public Edit<Dataflow> getCreateDataflowInputPortEdit(Dataflow dataflow,
			String portName, int portDepth, int granularDepth);

	/**
	 * Add an output port to a dataflow.
	 * 
	 * @param dataflow
	 *            dataflow to add the port to
	 * @param portName
	 *            name of the port, unique in the dataflow
	 */
	public Edit<Dataflow> getCreateDataflowOutputPortEdit(Dataflow dataflow,
			String portName);

	/**
	 * Rename a processor
	 * 
	 * @param processor
	 *            the processor to rename
	 * @param newName
	 *            the new name, must be unique within the workflow enclosing the
	 *            processor instance
	 */
	public Edit<Processor> getRenameProcessorEdit(Processor processor,
			String newName);

	/**
	 * Create a condition governing execution of the target processor. The
	 * target will not consume jobs from any inputs until all control processors
	 * linked through this edit have completed.
	 * 
	 * @param control
	 *            Processor controlling execution - this must complete before
	 *            the target can start.
	 * @param target
	 *            Processor controlled by this condition.
	 */
	public Edit<OrderedPair<Processor>> getCreateConditionEdit(
			Processor control, Processor target);

	/**
	 * Remove a condition previously applied to the specified pair of Processor
	 * instances
	 * 
	 * @param control
	 *            Processor controlling execution - this must complete before
	 *            the target can start.
	 * @param target
	 *            Processor controlled by this condition.
	 * @return
	 */
	public Edit<OrderedPair<Processor>> getRemoveConditionEdit(
			Processor control, Processor target);

	/**
	 * Builds an instance of an {@link InputPort} for an Activity.
	 * 
	 * @param portName
	 * @param portDepth
	 * @param allowsLiteralValues
	 *            whether the input port can cope with literal values
	 * @param handledReferenceSchemes
	 *            a list of the reference scheme types that can be legitimately
	 *            pushed into this input port
	 * @param translatedElementClass
	 *            the class desired as result (or elements of collections of
	 *            results) when interpreted by the data facade
	 * @return an instance of InputPort
	 */
	ActivityInputPort buildActivityInputPort(String portName, int portDepth,
			boolean allowsLiteralValues,
			List<Class<? extends ReferenceScheme<?>>> handledReferenceSchemes,
			Class<?> translatedElementClass);

	/**
	 * Builds an instance of an {@link OutputPort} for an Activity.
	 * 
	 * @param portName
	 * @param portDepth
	 * @param portGranularDepth
	 * @return an instance of OutputPort
	 */
	OutputPort buildActivityOutputPort(String portName, int portDepth,
			int portGranularDepth);
	
	Edit<Dataflow> getUpdateDataflowNameEdit(Dataflow dataflow, String newName);
	
	Edit<Dataflow> getUpdateDataflowInternalIdentifierEdit(Dataflow dataflow, String newId);
}
