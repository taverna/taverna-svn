package net.sf.taverna.t2.workflowmodel;

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

}
