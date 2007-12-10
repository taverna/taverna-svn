package net.sf.taverna.t2.monitor;

import java.util.Date;
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
	 *         there is no directly corresponding workflow object. Note that
	 *         this workflow object can be anything, and may not be a top level
	 *         workflow object at all.
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
	public Set<? extends MonitorableProperty<?>> getProperties();

	/**
	 * Each node has a creation date
	 */
	public Date getCreationDate();

	/**
	 * Properties can be added to the monitor node after creation if required,
	 * although this should be used only when necessary to avoid race conditions
	 */
	public void addMonitorableProperty(MonitorableProperty<?> newProperty);

	/**
	 * Nodes can persist in the tree after they have expired, in which case this
	 * will return true.
	 */
	public boolean hasExpired();

}
