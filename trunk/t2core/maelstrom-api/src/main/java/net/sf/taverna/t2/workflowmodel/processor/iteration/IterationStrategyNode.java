package net.sf.taverna.t2.workflowmodel.processor.iteration;

import java.util.List;

import javax.swing.tree.TreeNode;

import net.sf.taverna.t2.invocation.Completion;
import net.sf.taverna.t2.workflowmodel.processor.service.Job;

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
	
}
