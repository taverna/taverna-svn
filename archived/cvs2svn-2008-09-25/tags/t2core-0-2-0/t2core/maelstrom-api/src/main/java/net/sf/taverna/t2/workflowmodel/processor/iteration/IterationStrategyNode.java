package net.sf.taverna.t2.workflowmodel.processor.iteration;

import java.util.List;
import java.util.Map;

import javax.swing.tree.TreeNode;

import net.sf.taverna.t2.invocation.Completion;
import net.sf.taverna.t2.workflowmodel.processor.activity.Job;

/**
 * Interface for nodes within an iteration strategy layer
 * 
 * @author Tom Oinn
 * 
 */
public interface IterationStrategyNode extends TreeNode {

	/**
	 * The nodes within the iteration strategy, a tree structure, are event
	 * based. When a new fragment of a job from upstream in the tree (towards
	 * leaves) arrives it is handled by this method. Implementations will
	 * probably have to handle state management, i.e. what jobs have we already
	 * seen, and emit appropriate jobs to downstream nodes.
	 * 
	 * @param inputIndex
	 * @param newJob
	 */
	public void receiveJob(int inputIndex, Job newJob);

	/**
	 * Nodes can also receive completion events, the simplest being one
	 * declaring that no further input is expected on the given input, or
	 * partial completion events which are interpreted as 'no event with an
	 * index array prefixed by the specified completion index array will be
	 * received on the specified index'
	 */
	public void receiveCompletion(int inputIndex, Completion completion);

	public void setParent(IterationStrategyNode newParent);

	public List<IterationStrategyNode> getChildren();

	public void clear();

	/**
	 * In the context of an enclosing iteration strategy each node should be
	 * able to return the iteration depth, i.e. the length of the index array,
	 * for items it will emit. In all cases other than leaf nodes this is
	 * defined in terms of the depth of child nodes. The input cardinalities for
	 * named ports are pushed through each node so that the terminal nodes
	 * corresponding to input port collators can evaluate this expression -
	 * pushing it through the entire evaluation means we don't have to keep
	 * state anywhere in the leaf nodes (standard dependency injection)
	 * <p>
	 * Nodes can choose to throw the IterationTypeMismatchException if their
	 * inputs aren't compatible with the operational semantics of the node such
	 * as in the case of a dot product node with inputs with different depths.
	 * 
	 * @param inputDepths
	 * @return
	 * @throws IterationTypeMismatchException
	 */
	public int getIterationDepth(Map<String, Integer> inputDepths)
			throws IterationTypeMismatchException;

}
