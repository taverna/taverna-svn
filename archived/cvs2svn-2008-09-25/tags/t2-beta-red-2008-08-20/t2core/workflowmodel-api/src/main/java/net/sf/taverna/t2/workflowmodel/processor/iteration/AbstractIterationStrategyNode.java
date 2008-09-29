package net.sf.taverna.t2.workflowmodel.processor.iteration;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

import javax.swing.tree.MutableTreeNode;
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
 * @author Stian Soiland-Reyes
 * 
 */
public abstract class AbstractIterationStrategyNode implements
		IterationStrategyNode {

	private List<IterationStrategyNode> children = new ArrayList<IterationStrategyNode>();

	private IterationStrategyNode parent = null;

	/**
	 * Implement TreeNode
	 */
	public final synchronized Enumeration<IterationStrategyNode> children() {
		return new Vector<IterationStrategyNode>(children).elements();
	}

	/**
	 * Clear the child list and parent of this node
	 */
	public final synchronized void clear() {
		for (IterationStrategyNode child : children) {
			child.setParent(null);
		}
		children.clear();
		this.parent = null;
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
	public final synchronized IterationStrategyNode getChildAt(int position) {
		return children.get(position);
	}

	/**
	 * Implement TreeNode
	 */
	public final int getChildCount() {
		return children.size();
	}

	/**
	 * Implements IterationStrategyNode
	 */
	public final synchronized List<IterationStrategyNode> getChildren() {
		return new ArrayList<IterationStrategyNode>(this.children);
	}

	/**
	 * Implement TreeNode
	 */
	public final synchronized int getIndex(TreeNode node) {
		return children.indexOf(node);
	}

	/**
	 * Implement TreeNode
	 */
	public final IterationStrategyNode getParent() {
		return parent;
	}

	public synchronized void insert(MutableTreeNode child) {
		insert(child, getChildCount());
	}

	public synchronized void insert(MutableTreeNode child, int index) {
		if (!getAllowsChildren()) {
			throw new IllegalStateException("Node does not allow children");
		}
		if (!(child instanceof IterationStrategyNode)) {
			throw new IllegalArgumentException(
					"Child not an instance of IterationStrategyNode: " + child);
		}
		if (child == this) {
			throw new IllegalArgumentException("Can't be it's own parent");
		}
		// Check if it is already there (in case we'll just move it)
		int alreadyExistsIndex = children.indexOf(child);

		children.add(index, (IterationStrategyNode) child);

		if (alreadyExistsIndex > -1) {
			// Remove it from the old position
			if (index < alreadyExistsIndex
					&& alreadyExistsIndex + 1 < children.size()) {
				alreadyExistsIndex++;
			}
			children.remove(alreadyExistsIndex);
		}
		if (child.getParent() != this) {
			child.setParent(this);
		}
	}

	/**
	 * Implement TreeNode
	 */
	public boolean isLeaf() {
		return children.isEmpty();
	}

	public void remove(int index) {
		if (!getAllowsChildren()) {
			throw new IllegalStateException("Node does not allow children");
		}
		children.remove(index);

	}

	public synchronized void remove(MutableTreeNode node) {
		if (!getAllowsChildren()) {
			throw new IllegalStateException("Node does not allow children");
		}
		children.remove(node);
		if (node.getParent() == this) {
			node.setParent(null);
		}
	}

	public void removeFromParent() {
		if (parent != null) {
			IterationStrategyNode oldParent = parent;
			parent = null;
			oldParent.remove(this);
		}
	}

	/**
	 * Implements IterationStrategyNode
	 */
	public final synchronized void setParent(MutableTreeNode newParent) {
		if (newParent != null && !(newParent instanceof IterationStrategyNode)) {
			throw new IllegalArgumentException(
					"Parent not a IterationStrategyNode instance: " + newParent);
		}
		if (newParent != null && !newParent.getAllowsChildren()) {
			throw new IllegalStateException(
					"New parent does not allow children");
		}
		if (newParent == this) {
			throw new IllegalArgumentException("Can't be it's own parent");
		}
		removeFromParent();
		parent = (IterationStrategyNode) newParent;
		if (parent != null) {
			if (!parent.getChildren().contains(this)) {
				parent.insert(this);
			}
		}
	}

	public void setUserObject(Object object) {
		throw new UnsupportedOperationException("Can't set user object");
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

}
