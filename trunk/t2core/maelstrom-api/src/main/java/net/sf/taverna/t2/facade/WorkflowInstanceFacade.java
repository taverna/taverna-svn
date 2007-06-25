package net.sf.taverna.t2.facade;

import net.sf.taverna.t2.cloudone.EntityIdentifier;
import net.sf.taverna.t2.invocation.TokenOrderException;
import net.sf.taverna.t2.monitor.MonitorNode;
import net.sf.taverna.t2.utility.TypedTreeModel;

/**
 * The interaction point with a workflow instance. Technically there is no such
 * thing as a workflow instance in Taverna2, at least not in any real sense in
 * the code itself. The instance is more literally an identifier used as the
 * root of all data and error objects within this workflow and by which the top
 * level DataFlow or similar object is identified in the state tree. The
 * implementation of this interface should hide this though, automatically
 * prepending the internally stored (and hidden) identifier to all data push
 * messages and providing a subtree of the state model rooted at the internal
 * ID.
 * <p>
 * TODO - we should probably have callbacks for failure states here, but that
 * would need a decent definition (and maybe even ontology of) what failure
 * means. It's less obvious in a data streaming world what a failure is. At the
 * moment the dispatch stack can potentially treat unhandled error messages as
 * failing the processor, how do we get this exception information back up to
 * the workflow level?
 * 
 * @author Tom Oinn
 * 
 */
public interface WorkflowInstanceFacade {

	/**
	 * Push a data token into the specified port. If the token is part of a
	 * stream of such the index contains the index of this particular token. If
	 * not the index should be the empty integer array.
	 * 
	 * @param token
	 *            Identifier of the data token to be pushed to the workflow
	 * @param index
	 *            Index of the token
	 * @param portName
	 *            Port name to use
	 * @throws TokenOrderException
	 *             if ordering constraints on the token stream to each input
	 *             port are violated
	 */
	public void pushData(EntityIdentifier token, int[] index, String portName)
			throws TokenOrderException;

	/**
	 * Where a workflow has no inputs this method will cause it to start
	 * processing. Any processors within the workflow with no inputs are fired.
	 * 
	 * @throws IllegalStateException
	 *             if the workflow has already been fired or has had data pushed
	 *             to it.
	 */
	public void fire() throws IllegalStateException;

	/**
	 * The result listener is used to handle data tokens produced by the
	 * workflow.
	 * 
	 * @param listener
	 */
	public void addResultListener(ResultListener listener);

	/**
	 * Remove a previously registered result listener
	 * 
	 * @param listener
	 */
	public void removeResultListener(ResultListener listener);

	/**
	 * Workflow state is available through a sub-tree of the monitor tree. For
	 * security reasons the full monitor tree is never accessible through this
	 * interface but the sub-tree rooted at the node representing this workflow
	 * instance is and can be used for both monitoring and steering functions.
	 * <p>
	 * Uses the standard TreeModel-like mechanisms for registering change events
	 * and can be plugged into a JTree for display purposes through the
	 * TreeModelAdapter class.
	 * 
	 * @return Typed version of TreeModel representing the state of this
	 *         workflow. Nodes in the tree are instances of MonitorNode
	 */
	public TypedTreeModel<MonitorNode> getStateModel();

}
