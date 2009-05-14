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
package net.sf.taverna.t2.workflowmodel.processor.dispatch.layers;

import static net.sf.taverna.t2.workflowmodel.processor.dispatch.description.DispatchLayerStateEffect.CREATE_LOCAL_STATE;
import static net.sf.taverna.t2.workflowmodel.processor.dispatch.description.DispatchLayerStateEffect.REMOVE_LOCAL_STATE;
import static net.sf.taverna.t2.workflowmodel.processor.dispatch.description.DispatchLayerStateEffect.UPDATE_LOCAL_STATE;
import static net.sf.taverna.t2.workflowmodel.processor.dispatch.description.DispatchMessageType.JOB;

import java.util.Timer;
import java.util.TimerTask;

import net.sf.taverna.t2.workflowmodel.Processor;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.AbstractDispatchLayer;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.DispatchLayerCallback;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.DispatchLayerStateScoping;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.description.DispatchLayerErrorReaction;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.description.DispatchLayerJobReaction;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.description.DispatchLayerResultReaction;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.events.DispatchErrorEvent;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.events.DispatchJobEvent;

/**
 * Implements retry policy with delay between retries and exponential backoff
 * <p>
 * Default properties are as follows :
 * <ul>
 * <li>maxRetries = 0 (int)</li>
 * <li>initialDelay = 1000 (milliseconds)</li>
 * <li>maxDelay = 2000 (milliseconds)</li>
 * <li>backoffFactor = 1.0 (float)</li>
 * </ul>
 * 
 * @author Tom Oinn
 * 
 */
@DispatchLayerErrorReaction(emits = { JOB }, relaysUnmodified = true, stateEffects = {
		UPDATE_LOCAL_STATE, REMOVE_LOCAL_STATE })
@DispatchLayerJobReaction(emits = {}, relaysUnmodified = true, stateEffects = { CREATE_LOCAL_STATE })
@DispatchLayerResultReaction(emits = {}, relaysUnmodified = true, stateEffects = { REMOVE_LOCAL_STATE })
public class Retry extends AbstractDispatchLayer<RetryConfig, Retry.RetryState> {

	private RetryConfig config = new RetryConfig();

	private static Timer retryTimer = new Timer(true);

	public Retry() {
		super();
	}

	public Retry(int maxRetries, int initialDelay, int maxDelay,
			float backoffFactor) {
		super();
		this.config.setMaxRetries(maxRetries);
		this.config.setInitialDelay(initialDelay);
		this.config.setMaxDelay(maxDelay);
		this.config.setBackoffFactor(backoffFactor);
	}

	@Override
	public void receiveError(DispatchErrorEvent error,
			final DispatchLayerCallback callback, final RetryState state) {
		if (state.currentRetryCount == config.getMaxRetries()) {
			callback.clearLayerState();
			callback.sendError(error);
		} else {
			int delay = (int) (config.getInitialDelay() * (Math.pow(config
					.getBackoffFactor(), state.currentRetryCount)));
			if (delay > config.getMaxDelay()) {
				delay = config.getMaxDelay();
			}
			TimerTask task = new TimerTask() {
				@Override
				public void run() {
					state.currentRetryCount++;
					callback.sendJob(state.jobEvent);
				}
			};
			retryTimer.schedule(task, delay);
		}
	}

	@Override
	public void receiveJob(DispatchJobEvent job,
			DispatchLayerCallback callback, RetryState state) {
		state.jobEvent = job;
		callback.sendJob(job);
	}

	class RetryState {
		int currentRetryCount = 0;
		DispatchJobEvent jobEvent = null;
	}

	public void configure(RetryConfig config) {
		this.config = config;
	}

	public RetryConfig getConfiguration() {
		return this.config;
	}

	public RetryState createNewStateModel(Processor parent) {
		return new RetryState();
	}

	public DispatchLayerStateScoping getStateScope() {
		return DispatchLayerStateScoping.ITERATION;
	}
}
