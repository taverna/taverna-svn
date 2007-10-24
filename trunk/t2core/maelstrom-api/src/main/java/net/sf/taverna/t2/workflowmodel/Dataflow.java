package net.sf.taverna.t2.workflowmodel;

import java.util.List;
import net.sf.taverna.t2.annotation.Annotated;

/**
 * Top level definition object for a dataflow workflow. Currently Taverna only
 * supports dataflow workflows, this is equivalent to the Taverna 1 ScuflModel
 * class in role.
 * 
 * @author Tom Oinn
 * 
 */
public interface Dataflow extends Annotated, TokenProcessingEntity {

	/**
	 * A Dataflow consists of a set of named Processor instances. This method
	 * returns an unmodifiable list of these processors
	 * 
	 * @return list of all processors in the dataflow
	 */
	public List<? extends Processor> getProcessors();

	/**
	 * Dataflows have a list of input ports. These are the input ports the world
	 * outside the dataflow sees - each one contains an internal output port
	 * which is used to forward events on to entities (mostly processors) within
	 * the dataflow.
	 * 
	 * @return list of dataflow input port instances
	 */
	public List<? extends DataflowInputPort> getInputPorts();

	/**
	 * Get all workflow entities with the specified type restriction, this
	 * allows retrieval of Processor, Merge, a mix of the two or any other
	 * future entity to be added to the workflow model without a significant
	 * change in this part of the API.
	 * 
	 * @return an unmodifiable list of entities of the specified type
	 * @param entityType
	 *            a class of the type specified by the type variable T. All
	 *            entities returned in the list can be cast to this type
	 */
	public <T extends NamedWorkflowEntity> List<? extends T> getEntities(
			Class<T> entityType);

	/**
	 * Dataflows have a list of output ports. The output port in a dataflow is
	 * the port visible to the outside world and from which the dataflow emits
	 * events. Each dataflow output port also contains an instance of event
	 * receiving input port which is used by entities within the dataflow to
	 * push events to the corresponding external output port.
	 * 
	 * @return list of dataflow output port instances
	 */
	public List<? extends DataflowOutputPort> getOutputPorts();

	/**
	 * The dataflow is largely defined by the links between processors and other
	 * entities within its scope. This method returns them.
	 * 
	 * @return list of Datalink implementations
	 */
	public List<? extends Datalink> getLinks();

	/**
	 * Triggers a check for various basic potential problems with the workflow,
	 * in particular ones related to type checking. Returns a report object
	 * containing the state of the workflow and any problems found.
	 * 
	 * @return validation report
	 */
	public DataflowValidationReport checkValidity();

}
