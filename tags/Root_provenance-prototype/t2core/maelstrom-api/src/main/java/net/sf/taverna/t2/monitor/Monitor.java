package net.sf.taverna.t2.monitor;

import java.util.Set;

/**
 * The Monitor is used by work-flow objects to register themselves or
 * appropriate entities as data is passed into them and to de-register when data
 * streams are completed.
 * 
 * @author Tom Oinn
 * 
 */
public interface Monitor {

	/**
	 * Register a new node with this monitor. New nodes can only be registered
	 * when a new process identifier is allocated to data corresponding to entry
	 * to a boundary of control in the data-flow. For cases where extensions
	 * such as dispatch layers wish to augment existing state the plug-in point
	 * is responsible for the aggregation of appropriate properties.
	 * <p>
	 * The process identifier must not already exist within the state tree, if
	 * it does it will be ignored. Similarly the parent of the process
	 * identifier must exist, you cannot specify orphan nodes with no parent.
	 * 
	 * @param workflowObject
	 *            an object within the work-flow model which has received the
	 *            data stream and caused this node to be created. This may or
	 *            may not be a top level work-flow entity such as a processor or
	 *            data-flow. It may be empty in which case null can be used but
	 *            this is not recommended (there's almost always something you
	 *            can put here, in general the code to create a new node is
	 *            contained within something that's just received a data stream
	 *            so a default approach would be to use the 'this'
	 *            meta-variable)
	 * @param owningProcess
	 *            the process identifier which has been assigned to data moving
	 *            through the work-flow object. The list of IDs must contain the
	 *            new identifier as the last element.
	 * @param properties
	 *            the set of monitor-able, and potentially steer-able,
	 *            properties which can be monitored from this node. Properties
	 *            may cache, may become invalid and may make no guarantee about
	 *            timely updates.
	 */
	public void registerNode(Object workflowObject, String[] owningProcess,
			Set<MonitorableProperty<?>> properties);

	/**
	 * Push new property get / set methods into the specified node. This is used
	 * for monitor-able activities where we have to create the node in the state
	 * tree before we have the set of monitor-able properties for it.
	 * 
	 * @param owningProcess
	 *            the node path to add properties, this must already be
	 *            registered in the state model, if not then this method fails
	 *            silently and does nothing.
	 * @param newProperties
	 *            the set of properties to add to the specified node in the
	 *            state model
	 */
	public void addPropertiesToNode(String[] owningProcess,
			Set<MonitorableProperty<?>> newProperties);

	/**
	 * Remove the specified node from the monitor. This must be called when the
	 * final data or completion event leaves a boundary of control. The monitor
	 * is free to delay the actual removal of state, in which case the node may
	 * choose to throw exceptions when properties are accessed. The monitor
	 * should eventually release all references to work-flow objects and process
	 * identifiers - typically this is used to allow a UI a few seconds delay on
	 * de-registration so that very fast register / de-register cycles are
	 * visible to a user.
	 * <p>
	 * The specified process identifier must exist within the monitor.
	 * 
	 * @param owningProcess
	 *            the identifier of the node to remove.
	 */
	public void deregisterNode(String[] owningProcess);

}
