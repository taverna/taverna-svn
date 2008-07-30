package net.sf.taverna.t2.workflowmodel.processor.iteration;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import javax.swing.tree.TreeNode;

import net.sf.taverna.t2.invocation.Completion;
import net.sf.taverna.t2.workflowmodel.WorkflowStructureException;
import net.sf.taverna.t2.workflowmodel.processor.activity.Job;

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

	private List<IterationStrategyNode> children;

	protected AbstractIterationStrategyNode() {
		children = new ArrayList<IterationStrategyNode>();
	}

	
	/**
	 * Implements IterationStrategyNode
	 */
	public final List<IterationStrategyNode> getChildren() {
		return this.children;
	}

	/**
	 * Implements IterationStrategyNode
	 */
	protected final synchronized void addChild(
			AbstractIterationStrategyNode newChild) {
		newChild.setParent(this);
	}

	/**
	 * Implements IterationStrategyNode
	 */
	public final synchronized void setParent(IterationStrategyNode newParent) {
		if (newParent != null) {
			parent = newParent;
			if (! newParent.getChildren().contains(this)) {
				newParent.getChildren().add(newParent.getChildCount(), this);
			}
		} else {
			if (parent != null) {
				// Remove from the current parent
				if (parent.getChildren().contains(this)) {
					parent.getChildren().remove(this);
				}
			}
			parent = null;
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
	 * Implement TreeNode
	 */
	public final Enumeration<?> children() {
		return new Enumeration<Object>() {
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
	public final TreeNode getChildAt(int position) {
		return children.get(position);
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
	public final int getIndex(TreeNode node) {
		return children.indexOf(node);
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
