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

import java.util.ArrayList;
import java.util.List;

import net.sf.taverna.t2.workflowmodel.Processor;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.AbstractDispatchLayer;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.DispatchLayerCallback;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.DispatchLayerStateScoping;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.description.DispatchLayerErrorReaction;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.description.DispatchLayerJobReaction;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.description.DispatchLayerResultReaction;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.events.DispatchErrorEvent;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.events.DispatchJobEvent;

/**
 * Failure handling dispatch layer, consumes job events with multiple activities
 * and emits the same job but with only the first activity. On failures the job
 * is resent to the layer below with a new activity list containing the second
 * in the original list and so on. If a failure is received and there are no
 * further activities to use the job fails and the failure is sent back up to
 * the layer above.
 * 
 * @author Tom Oinn
 * @author Stian Soiland-Reyes
 * 
 * 
 */
@DispatchLayerErrorReaction(emits = { JOB }, relaysUnmodified = true, stateEffects = {
		UPDATE_LOCAL_STATE, REMOVE_LOCAL_STATE })
@DispatchLayerJobReaction(emits = {}, relaysUnmodified = true, stateEffects = { CREATE_LOCAL_STATE })
@DispatchLayerResultReaction(emits = {}, relaysUnmodified = true, stateEffects = { REMOVE_LOCAL_STATE })
public class Failover extends
		AbstractDispatchLayer<Object, Failover.FailoverState> {

	/**
	 * Receive a job from the layer above, store it in the state map then relay
	 * it to the layer below with a modified activity list containing only the
	 * activity at index 0
	 */
	@Override
	public void receiveJob(DispatchJobEvent jobEvent,
			DispatchLayerCallback callback, FailoverState state) {
		state.job = jobEvent;
		List<Activity<?>> newActivityList = new ArrayList<Activity<?>>();
		newActivityList.add(state.getCurrentActivity());
		DispatchJobEvent newJob = callback.createJobEvent(state.job.getData(),
				newActivityList);
		callback.sendJob(newJob);
	}

	/**
	 * Respond to errors by attempting to retry with the next activity, or by
	 * forwarding the error if we're out of ideas, so to speak.
	 */
	@Override
	public void receiveError(DispatchErrorEvent error,
			DispatchLayerCallback callback, FailoverState state) {
		state.currentActivityIndex++;
		if (state.currentActivityIndex == state.job.getActivities().size()) {
			callback.sendError(error);
		} else {
			List<Activity<?>> newActivityList = new ArrayList<Activity<?>>();
			newActivityList.add(state.getCurrentActivity());
			DispatchJobEvent newJob = callback.createJobEvent(state.job
					.getData(), newActivityList);
			callback.sendJob(newJob);
		}
	}

	class FailoverState {
		int currentActivityIndex = 0;
		DispatchJobEvent job = null;

		Activity<?> getCurrentActivity() {
			return job.getActivities().get(currentActivityIndex);
		}
	}

	@Override
	public FailoverState createNewStateModel(Processor parent) {
		return new FailoverState();
	}

	@Override
	public DispatchLayerStateScoping getStateScope() {
		return DispatchLayerStateScoping.ITERATION;
	}

}
