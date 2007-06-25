package net.sf.taverna.t2.monitor;

import java.util.Set;

/**
 * A single node in the Monitor tree, containing an optional arbitrary workflow
 * object and a set of properties which may or may not be mutable. For tree
 * traversal operations the top level monitor tree must be used, instances of
 * this class are not aware of the surrounding tree structure.
 * 
 * @author Tom Oinn
 * 
 */
public interface MonitorNode {

	/**
	 * Each monitor node can reference zero or one workflow object. This is the
	 * object which is providing any properties the node exposes, so is likely
	 * to be a workflow or processor but could be anything.
	 * 
	 * @return the workflow object providing this node's properties, or null if
	 *         there is no directly corresponding workflow object
	 */
	public Object getWorkflowObject();

	/**
	 * Each monitor node has an identity corresponding to the identifier stack
	 * of the data flowing through the workflow object that created it. This
	 * string array also defines its position in the monitor tree.
	 */
	public String[] getOwningProcess();

	/**
	 * Each monitor node exposes a set of properties, which may or may not be
	 * mutable
	 */
	public Set<MonitorableProperty> getProperties();

}
