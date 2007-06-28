package net.sf.taverna.t2.workflowmodel;

import net.sf.taverna.t2.annotation.Annotated;
import net.sf.taverna.t2.annotation.WorkflowAnnotation;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.DispatchLayer;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.DispatchStack;
import net.sf.taverna.t2.workflowmodel.processor.service.Service;

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
	 * Build a new Processor with no ports, iteration system, services or
	 * dispatch stack and add it to the specified Dataflow
	 * 
	 * @param dataflow
	 *            the dataflow to add this processor to
	 */
	public Edit<Processor> createProcessor(Dataflow dataflow);

	/**
	 * Create a new processor in the specified dataflow configured as a default
	 * Taverna 1 style service with output and input ports matching those of the
	 * service instance supplied, a default cross product iteration strategy and
	 * a dispatch stack consisting of a parallelize, failover, retry and invoke
	 * layer set.
	 * 
	 * @param dataflow
	 *            the dataflow to add this processor to
	 * @param service
	 *            a single service to build the processor around
	 */
	public Edit<Processor> createProcessorFromService(Dataflow dataflow,
			Service<?> service);

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
	 * Add a new layer to the specified dispath stack
	 * 
	 * @param stack
	 *            Stack to add to
	 * @param layer
	 *            New dispatch layer to add
	 * @param position
	 *            Where to add the new layer? 0 is at the top of the stack.
	 */
	public Edit<DispatchStack> getAddDispatchLayerEdit(DispatchStack stack,
			DispatchLayer layer, int position);

	/**
	 * Remove a dispatch layer from its dispatch stack
	 * 
	 * @param stack
	 *            The stack from which to remove the layer
	 * @param layer
	 *            The layer to remove
	 */
	public Edit<DispatchStack> getDeleteDispatchLayerEdit(DispatchStack stack,
			DispatchLayer layer);

	/**
	 * Add a service implementation to the set of services within a Processor
	 * 
	 * @param processor
	 *            processor to add the service to
	 * @param service
	 *            service to add
	 */
	public Edit<Processor> getAddServiceEdit(Processor processor,
			Service<?> service);

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
	 *            conceptual depth of collections emited from this port
	 * @param granularDepth
	 *            granular depth, lowest collection depth that can be emited
	 *            within a stream
	 */
	public Edit<Processor> getCreateProcessorOutputPortEdit(
			Processor processor, String portName, int portDepth,
			int granularDepth);

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
	 * Add new workflow object metadata to the specified workflow object
	 * 
	 * @param <TargetType>
	 *            the type of the object being annotated
	 * @param newAnnotation
	 *            metadata object to add to the annotated object
	 * @param objectToAnnotate
	 *            object to describe with the specified metadata
	 * @return edit object to perform and undo the metadata assignment
	 */
	public <TargetType extends Annotated> Edit<TargetType> getAddAnnotationEdit(
			WorkflowAnnotation newAnnotation, TargetType objectToAnnotate);

	/**
	 * Remove an annotation object from the specified Annotated workflow object
	 * 
	 * @param <TargetType>
	 *            type of the workflow object from which the annotation is
	 *            removed
	 * @param annotationToRemove
	 *            metadata object to remove
	 * @param objectToAnnotate
	 *            object from which the metadata is removed
	 * @return edit object to perform and undo the metadata removal
	 */
	public <TargetType extends Annotated> Edit<TargetType> getRemoveAnnotationEdit(
			WorkflowAnnotation annotationToRemove, TargetType objectToAnnotate);

	/**
	 * Replace an annotation on the specified object with a new annotation
	 * object. In all probability this is implemented as a compound edit using
	 * the remove and add annotation edits but from a user's point of view we
	 * want to have this as a single editing operation within the UI.
	 * 
	 * @param <TargetType>
	 *            type of the workflow object under modification
	 * @param oldAnnotation
	 *            annotation to replace
	 * @param newAnnotation
	 *            new version of the annotation
	 * @param objectToAnnotate
	 *            workflow object being annotated
	 * @return edit object to perform and undo the metadata modification
	 */
	public <TargetType extends Annotated> Edit<TargetType> getReplaceAnnotationEdit(
			WorkflowAnnotation oldAnnotation, WorkflowAnnotation newAnnotation,
			TargetType objectToAnnotate);

}
