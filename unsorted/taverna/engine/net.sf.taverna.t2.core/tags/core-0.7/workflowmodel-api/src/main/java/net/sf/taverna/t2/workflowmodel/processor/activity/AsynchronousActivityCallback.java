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
package net.sf.taverna.t2.workflowmodel.processor.activity;

import java.util.Map;

import net.sf.taverna.t2.invocation.InvocationContext;
import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.events.DispatchErrorType;

/**
 * The callback interface used by instances of AsynchronousActivity to push
 * results and failure messages back to the invocation layer.
 * 
 * @author Tom Oinn
 * 
 */
public interface AsynchronousActivityCallback {

	/**
	 * The invocation context contains resources such as data managers, security
	 * agents and provenance consumers to be used by the Activity as it runs.
	 * This replaces the getLocalDataManager and getLocalSecurityManager calls.
	 */
	public InvocationContext getContext();

	/**
	 * If an activity proxy wants to create a new thread of activity it should
	 * use this method unless there is a very good reason not to. This allows
	 * the workflow framework to control its own thread usage, possibly
	 * implementing per user, per workflow or per processor thread limit
	 * policies. Exceptions to this principle might include cases where the
	 * activity proxy is capable of managing thread usage across all instances
	 * of that activity type and therefore more efficiently (fewer threads) than
	 * if it let the workflow manager perform this function.
	 * 
	 * @param runMe
	 *            a Runnable to implement the activity proxy logic.
	 */
	public void requestRun(Runnable runMe);

	/**
	 * Push a map of named identifiers out to the invocation layer which is then
	 * responsible for wrapping them up into an appropriate Job object and
	 * sending it up the dispatch stack. The keys of the map are names local to
	 * the activity, the callback object is responsible for rewriting them
	 * according to the activity mapping rules (i.e. Activity.getXXXPortMapping)
	 * 
	 * @param data
	 *            a single result data packet
	 * @param index
	 *            the index of the result in the context of this single process
	 *            invocation. If there's no streaming involved this should be a
	 *            zero length int[].
	 */
	public void receiveResult(Map<String, T2Reference> data, int[] index);

	/**
	 * If (and only if) the activity is streaming data then this method can be
	 * called to signal a (possibly partial) completion of the stream. If this
	 * is a total completion event, i.e. one with a zero length index array and
	 * there have been no result data sent the callback object will create a
	 * single job containing empty lists and send that instead otherwise it will
	 * be passed straight through. The index array is relative to this
	 * particular activity invocation as the invocation has no contextual
	 * awareness.
	 * 
	 * @param completionIndex
	 */
	public void receiveCompletion(int[] completionIndex);

	/**
	 * If the job fails (as opposed to succeeding and sending an error for which
	 * the receiveResult method is used) this method will cause an error to be
	 * sent up the dispatch stack, triggering any appropriate handling methods
	 * such as retry, failover etc. This particular method accepts both a free
	 * text message and an instance of Throwable for additional information, in
	 * addition to which it sends an error type which allows upstream layers to
	 * determine whether they can handle the error or whether it should be
	 * passed directly upwards.
	 * 
	 * @param message
	 * @param t
	 */
	public void fail(String message, Throwable t, DispatchErrorType errorType);

	/**
	 * If the job fails (as opposed to succeeding and sending an error for which
	 * the receiveResult method is used) this method will cause an error to be
	 * sent up the dispatch stack, triggering any appropriate handling methods
	 * such as retry, failover etc. This particular method accepts both a free
	 * text message and an instance of Throwable for additional information.
	 * 
	 * @param message
	 * @param t
	 */
	public void fail(String message, Throwable t);

	/**
	 * If the job fails (as opposed to succeeding and sending an error for which
	 * the receiveResult method is used) this method will cause an error to be
	 * sent up the dispatch stack, triggering any appropriate handling methods
	 * such as retry, failover etc. This method just takes a free text message
	 * for cases where a failure is properly described by an instance of
	 * Throwable
	 * 
	 * @param message
	 */
	public void fail(String message);

	/**
	 * For activities which are going to establish state below the invoke node
	 * in the monitor tree this method returns the owning process identifier
	 * allocated to the invoke node. This is particularly necessary for nested
	 * workflow activities.
	 * <p>
	 * Any calls to Monitor.register... must establish a state tree rooted at
	 * this node, they may assume that this node already exists.
	 */
	public String getParentProcessIdentifier();

}
