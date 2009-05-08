/*******************************************************************************
 * Copyright (C) 2007 The University of Manchester   
 * 
 *  Modifications to the initial code base are copyright of their
 *  respective authors, or their employers as appropriate.
 * 
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2.1 of
 *  the License, or (at your option) any later version.
 *    
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *    
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 ******************************************************************************/
package net.sf.taverna.t2.facade;

import net.sf.taverna.t2.invocation.InvocationContext;
import net.sf.taverna.t2.invocation.ProcessIdentifier;
import net.sf.taverna.t2.invocation.TokenOrderException;
import net.sf.taverna.t2.invocation.WorkflowDataToken;
import net.sf.taverna.t2.workflowmodel.ControlBoundary;
import net.sf.taverna.t2.workflowmodel.Dataflow;

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
@ControlBoundary
public interface WorkflowInstanceFacade {

	/**
	 * Push a data token into the specified port. If the token is part of a
	 * stream the index contains the index of this particular token. If not the
	 * index should be the empty integer array.
	 * 
	 * @param token
	 *            A WorkflowDataToken containing the data to be pushed to the
	 *            workflow along with its current owning process identifier and
	 *            index
	 * @param portName
	 *            Port name to use
	 * @throws TokenOrderException
	 *             if ordering constraints on the token stream to each input
	 *             port are violated
	 */
	public void pushData(WorkflowDataToken token, String portName)
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
	 * Cancel a running workflow, this will cause any listeners to be notified
	 * of a workflow failure
	 */
	public void cancel();

	/**
	 * Add a new listener to handle workflow lifecycle events such as data
	 * production, completion and failure
	 * 
	 * @param listener
	 */
	public void addWorkflowInstanceListener(WorkflowInstanceListener listener);

	/**
	 * Remove a previously registered workflow instance listener
	 * 
	 * @param listener
	 */
	public void removeWorkflowInstanceListener(WorkflowInstanceListener listener);

	/**
	 * Return the dataflow this facade facades
	 */
	public Dataflow getDataflow();

	/**
	 * Return the invocation context used by this facade
	 */
	public InvocationContext getContext();

	/**
	 * Get the status of this workflow instance
	 */
	public WorkflowInstanceStatus getStatus();

	/**
	 * Get the process identifier of this facade
	 */
	public ProcessIdentifier getFacadeProcessIdentifier();

	/**
	 * Get whether the underlying invocation context is in a paused state
	 */
	public boolean isPaused();

	/**
	 * Set the pause state on the underlying invocation context. Note that this
	 * does not pause this facade, but pauses <em>every</em> dataflow using the
	 * same invocation context as the facade. For this reason this method should
	 * only generally be called by a top level facade and not by one within an
	 * activity (for example)
	 */
	public void setPaused(boolean paused);

}
