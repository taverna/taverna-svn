package net.sf.taverna.t2.workflowmodel.processor.iteration;

import javax.swing.tree.MutableTreeNode;

/**
 * The terminal node is the root of the iteration strategy tree, it is
 * responsible for forwarding all events up to the iteration strategy itself
 * which can then propogate them to the strategy stack.
 */
public abstract class TerminalNode extends AbstractIterationStrategyNode {
	@Override
	public synchronized void insert(MutableTreeNode child, int index) {
		if (getChildCount() > 0 && getChildAt(0) != child) {
			throw new IllegalStateException(
					"The terminal node can have maximum one child");
		}
		super.insert(child, index);
	}
}
