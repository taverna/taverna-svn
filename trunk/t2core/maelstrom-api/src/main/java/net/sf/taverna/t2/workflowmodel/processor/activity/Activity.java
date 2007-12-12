package net.sf.taverna.t2.workflowmodel.processor.activity;

import static net.sf.taverna.t2.annotation.HierarchyRole.CHILD;

import java.util.Map;
import java.util.Set;

import net.sf.taverna.t2.annotation.Annotated;
import net.sf.taverna.t2.annotation.HierarchyTraversal;
import net.sf.taverna.t2.workflowmodel.OutputPort;

/**
 * Defines a single abstract or concrete invokable activity. Each Processor
 * contains at least one of these and may contain many, similarly the dispatch
 * stack may create new Activity instances from e.g. dynamic lookup or resolution
 * of an abstract activity to a concrete activity or set of activities.
 * 
 * @param <ConfigurationType> the ConfigurationType associated with the Activity. This is an arbitrary java class that provides details on how the Activity is configured..
 * @author Tom Oinn
 * 
 */
public interface Activity<ConfigurationType> extends Annotated<Activity<?>> {

	/**
	 * Each Activity implementation stores configuration within a bean of type
	 * ConfigurationType, this method returns the configuration. This is used by
	 * the automatic serialisation framework to store the activity definition in
	 * the workflow XML.
	 */
	public ConfigurationType getConfiguration();

	/**
	 * When the Activity implementation is built from the workflow definition XML
	 * the object is first constructed with a default constructor then this
	 * method is called, passing in the configuration bean returned by
	 * getConfiguration()
	 * 
	 * @throws ActivityConfigurationException
	 *             if a problem occurs when configuring the activity
	 */
	public void configure(ConfigurationType conf)
			throws ActivityConfigurationException;

	/**
	 * An Activity contains a set of named input ports. Names must be unique
	 * within this set.
	 * 
	 * @return the set of input ports for this activity
	 */
	@HierarchyTraversal(hierarchies = { "workflowStructure" }, role = { CHILD })
	public Set<ActivityInputPort> getInputPorts();

	/**
	 * A processor may have different input port names to the activity or
	 * activities it contains. This map is keyed on the processor input port names
	 * with the corresponding value being the activity port name.
	 * 
	 * @return mapping from processor input port names to activity input port
	 *         names
	 */
	public Map<String, String> getInputPortMapping();

	/**
	 * An Activity contains a set of named output ports. As with input ports names
	 * must be unique within the set.
	 * 
	 * @return
	 */
	@HierarchyTraversal(hierarchies = { "workflowStructure" }, role = { CHILD })
	public Set<OutputPort> getOutputPorts();

	/**
	 * Outputs of the activity may be named differently to those of the
	 * processor. This map is keyed on an activity output port name with each
	 * corresponding value being the processor output port name to which the
	 * activity output is bound.
	 * 
	 * @return mapping from activity output port name to processor output port
	 *         name
	 */
	public Map<String, String> getOutputPortMapping();

}
