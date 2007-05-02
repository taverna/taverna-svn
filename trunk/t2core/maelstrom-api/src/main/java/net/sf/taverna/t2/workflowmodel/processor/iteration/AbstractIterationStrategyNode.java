package net.sf.taverna.t2.workflowmodel.processor.iteration;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.tree.TreeNode;

import net.sf.taverna.t2.invocation.Completion;
import net.sf.taverna.t2.workflowmodel.WorkflowStructureException;
import net.sf.taverna.t2.workflowmodel.processor.service.Job;

/**
 * Abstract superclass for implementations of IterationStrategyNode, adds logic
 * to connect nodes together and convenience methods to push jobs and completion
 * events up to the parent node.
 * 
 * @author Tom Oinn
 * 
 */
public abstract class AbstractIterationStrategyNode implements
		IterationStrategyNode {

	private IterationStrategyNode parent = null;

	private Map<String, boolean[]> ownerToCompletion = new HashMap<String, boolean[]>();

	private List<IterationStrategyNode> children;

	protected AbstractIterationStrategyNode() {
		children = new ArrayList<IterationStrategyNode>();
	}

	public final List<IterationStrategyNode> getChildren() {
		return this.children;
	}

	protected final synchronized void addChild(AbstractIterationStrategyNode newChild) {
		newChild.setParent(this);
	}

	public final synchronized void setParent(
			IterationStrategyNode newParent) {
		if (newParent != null) {
			this.parent = newParent;
			if (newParent.getChildren().contains(this) == false) {
				newParent.getChildren().add(newParent.getChildCount(), this);
			}
		} else {
			if (this.parent != null) {
				// Remove from the current parent
				if (this.parent.getChildren().contains(this)) {
					this.parent.getChildren().remove(this);
				}
			}
			this.parent = null;
		}
	}

	/**
	 * Push the specified job up to the parent node in the iteration strategy.
	 */
	protected final void pushJob(Job job) {
		if (parent != null) {
			int index = parent.getIndex(this);
			if (index < 0) {
				throw new WorkflowStructureException(
						"Parent doesn't have this node in its child list!");
			}
			parent.receiveJob(parent.getIndex(this), job);
		}
	}

	/**
	 * Push the specified completion event to the parent node
	 */
	protected final void pushCompletion(Completion completion) {
		if (parent != null) {
			parent.receiveCompletion(parent.getIndex(this), completion);
		}
	}

	/**
	 * Clear the child list and parent of this node
	 */
	public final void clear() {
		children.clear();
		this.parent = null;
	}

	/**
	 * Receive a total completion event, this is functionality that all
	 * subclasses must provide. The logic is that when all inputs for a given
	 * job receive a completion event with an empty index the node must emit a
	 * corresponding total completion event.
	 * 
	 * @return whether the final completion event was sent
	 */
	protected final boolean receiveFinalCompletion(String owningProcess,
			int inputIndex) {
		// Only interested in complete completion events, partials are
		// of no use in this system as the way events combine means we
		// can't draw any useful information from partial completion
		// when iterating in this manner
		if (!ownerToCompletion.containsKey(owningProcess)) {
			ownerToCompletion.put(owningProcess, new boolean[getChildCount()]);
			for (int i = 0; i < ownerToCompletion.get(owningProcess).length; i++) {
				ownerToCompletion.get(owningProcess)[i] = false;
			}
		}
		boolean[] completionStatus = ownerToCompletion.get(owningProcess);
		completionStatus[inputIndex] = true;
		boolean complete = true;
		for (int i = 0; i < completionStatus.length; i++) {
			if (completionStatus[i] == false) {
				complete = false;
			}
		}
		if (complete) {
			// Purge the caches and sent the 'everything done' message to
			// the parent
			ownerToCompletion.remove(owningProcess);
			pushCompletion(new Completion(owningProcess, new int[0]));
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Implement TreeNode
	 */
	public final Enumeration children() {
		return new Enumeration() {
			int currentPosition = 0;

			public boolean hasMoreElements() {
				return currentPosition < children.size();
			}

			public Object nextElement() {
				return getChildAt(currentPosition++);
			}
		};
	}

	/**
	 * Implement TreeNode
	 */
	public boolean getAllowsChildren() {
		return true;
	}

	/**
	 * Implement TreeNode
	 */
	public final TreeNode getChildAt(int arg0) {
		return children.get(arg0);
	}

	/**
	 * Implement TreeNode
	 */
	public final int getChildCount() {
		return children.size();
	}

	/**
	 * Implement TreeNode
	 */
	public final int getIndex(TreeNode arg0) {
		return children.indexOf(arg0);
	}

	/**
	 * Implement TreeNode
	 */
	public final TreeNode getParent() {
		return parent;
	}

	/**
	 * Implement TreeNode
	 */
	public boolean isLeaf() {
		return children.isEmpty();
	}

}
