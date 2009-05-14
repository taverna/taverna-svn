/*******************************************************************************
 * Copyright (C) 2007 The University of Manchester   
 * 
 *  Modifications to the initial code base are copyright of their
 *  respective authors, or their employers as appropriate.
 * 
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2.1 of
 *  the License, or (at your option) any later version.
 *    
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *    
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 ******************************************************************************/
package net.sf.taverna.t2.workflowmodel;

import static net.sf.taverna.t2.annotation.HierarchyRole.CHILD;

import java.util.List;

import net.sf.taverna.t2.annotation.Annotated;
import net.sf.taverna.t2.annotation.HierarchyTraversal;
import net.sf.taverna.t2.facade.WorkflowInstanceListener;
import net.sf.taverna.t2.facade.WorkflowInstanceStatus;
import net.sf.taverna.t2.invocation.InvocationContext;
import net.sf.taverna.t2.invocation.ProcessIdentifier;

/**
 * Top level definition object for a dataflow workflow. Currently Taverna only
 * supports dataflow workflows, this is equivalent to the Taverna 1 ScuflModel
 * class in role.
 * 
 * @author Tom Oinn
 * 
 */
@ControlBoundary
public interface Dataflow extends Annotated<Dataflow>, TokenProcessingEntity {

	/**
	 * A Dataflow consists of a set of named Processor instances. This method
	 * returns an unmodifiable list of these processors. Equivalent to calling
	 * getEntities(Processor.class).
	 * 
	 * @return list of all processors in the dataflow
	 */
	@HierarchyTraversal(hierarchies = { "workflowStructure" }, role = { CHILD })
	public List<? extends Processor> getProcessors();

	/**
	 * Dataflows also contain a set of merge operations, this method returns an
	 * unmodifiable copy of the set. Equivalent to calling
	 * getEntities(Merge.class)
	 */
	@HierarchyTraversal(hierarchies = { "workflowStructure" }, role = { CHILD })
	public List<? extends Merge> getMerges();

	/**
	 * Dataflows have a list of input ports. These are the input ports the world
	 * outside the dataflow sees - each one contains an internal output port
	 * which is used to forward events on to entities (mostly processors) within
	 * the dataflow.
	 * 
	 * @return list of dataflow input port instances
	 */
	@HierarchyTraversal(hierarchies = { "workflowStructure" }, role = { CHILD })
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
	public <T extends Object> List<? extends T> getEntities(Class<T> entityType);

	/**
	 * Dataflows have a list of output ports. The output port in a dataflow is
	 * the port visible to the outside world and from which the dataflow emits
	 * events. Each dataflow output port also contains an instance of event
	 * receiving input port which is used by entities within the dataflow to
	 * push events to the corresponding external output port.
	 * 
	 * @return list of dataflow output port instances
	 */
	@HierarchyTraversal(hierarchies = { "workflowStructure" }, role = { CHILD })
	public List<? extends DataflowOutputPort> getOutputPorts();

	/**
	 * The dataflow is largely defined by the links between processors and other
	 * entities within its scope. This method returns them.
	 * 
	 * @return list of Datalink implementations
	 */
	@HierarchyTraversal(hierarchies = { "workflowStructure" }, role = { CHILD })
	public List<? extends Datalink> getLinks();

	/**
	 * Triggers a check for various basic potential problems with the workflow,
	 * in particular ones related to type checking. Returns a report object
	 * containing the state of the workflow and any problems found.
	 * 
	 * @return validation report
	 */
	public DataflowValidationReport checkValidity();

	/**
	 * A dataflow with no inputs cannot be driven by the supply of data tokens
	 * as it has nowhere to receive such tokens. This method allows a dataflow
	 * to fire on an empty input set, in this case the owning process identifier
	 * must be passed explicitly to the dataflow. This method then calls the
	 * fire methods of any Processor instances with no input ports.
	 */
	public void fire(ProcessIdentifier instanceOwningProcessId,
			InvocationContext context);

	/**
	 * An identifier that is unique to this dataflow and its current state. The
	 * identifier will change whenever the dataflow is modified.
	 * 
	 * @return a String representing a unique internal identifier.
	 */
	public String getInternalIdentier();

	/**
	 * Add a new workflow instance listener to this dataflow, using the
	 * specified process identifier to determine whether events are forwarded to
	 * the supplied listener. In general you should avoid using this directly
	 * and instead call the facade methods
	 * 
	 * @param dataflowProcessIdentifier
	 * @param listener
	 */
	public void addWorkflowInstanceListener(
			ProcessIdentifier dataflowProcessIdentifier,
			WorkflowInstanceListener listener);

	/**
	 * Remove a previously registered listener. In general you should avoid
	 * using this directly and instead call the facade methods
	 * 
	 * @param listener
	 */
	public void removeWorkflowInstanceListener(
			ProcessIdentifier dataflowProcessIdentifier,
			WorkflowInstanceListener listener);

	/**
	 * Get the status for an instance of this workflow, in general you should
	 * avoid calling this directly and instead use the facade methods.
	 * 
	 * @param dataflowProcessIdentifier
	 * @return
	 */
	public WorkflowInstanceStatus getInstanceStatus(
			ProcessIdentifier dataflowProcessIdentifier);

	/**
	 * Set the status for an instance of this workflow, this should never be
	 * called explicitly from your code, it is only here to support the suspend
	 * / resume notification system
	 */
	public void setWorkflowInstanceStatus(
			ProcessIdentifier dataflowProcessIdentifier,
			WorkflowInstanceStatus status);

	/**
	 * Cancel a specific instance of a dataflow. Do not call this from your
	 * code, use the method in the facade instead.
	 * 
	 * @param dataflowProcessIdentifier
	 * @param context
	 */
	public void cancelWorkflowInstance(
			ProcessIdentifier dataflowProcessIdentifier,
			InvocationContext context);

}
