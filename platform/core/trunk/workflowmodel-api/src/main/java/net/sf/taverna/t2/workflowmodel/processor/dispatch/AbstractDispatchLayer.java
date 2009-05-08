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
package net.sf.taverna.t2.workflowmodel.processor.dispatch;

import net.sf.taverna.t2.invocation.ProcessIdentifier;
import net.sf.taverna.t2.workflowmodel.Processor;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.events.DispatchCompletionEvent;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.events.DispatchErrorEvent;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.events.DispatchJobEvent;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.events.DispatchJobQueueEvent;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.events.DispatchResultEvent;

/**
 * Convenience abstract implementation of DispatchLayer, relays all events
 * without any processing and does nothing with the 'finishedWith' method. This
 * also defines the layer to be stateless and to have no configuration.
 * 
 * @author Tom Oinn
 * 
 */
public abstract class AbstractDispatchLayer<ConfigurationType, StateModel>
		implements DispatchLayer<ConfigurationType, StateModel> {

	public void receiveError(DispatchErrorEvent errorEvent,
			DispatchLayerCallback callback, StateModel state) {
		callback.sendError(errorEvent);
	}

	public void receiveJob(DispatchJobEvent jobEvent,
			DispatchLayerCallback callback, StateModel state) {
		callback.sendJob(jobEvent);
	}

	public void receiveJobQueue(DispatchJobQueueEvent jobQueueEvent,
			DispatchLayerCallback callback, StateModel state) {
		callback.sendJobQueue(jobQueueEvent);
	}

	public void receiveResult(DispatchResultEvent resultEvent,
			DispatchLayerCallback callback, StateModel state) {
		callback.sendResult(resultEvent);
	}

	public void receiveResultCompletion(
			DispatchCompletionEvent completionEvent,
			DispatchLayerCallback callback, StateModel state) {
		callback.sendResultCompletion(completionEvent);
	}

	public void finishedWith(ProcessIdentifier owningProcess) {
		// Do nothing by default
	}

	public DispatchLayerStateScoping getStateScope() {
		return DispatchLayerStateScoping.NONE;
	}

	public StateModel createNewStateModel(Processor parent) {
		return null;
	}

	public ConfigurationType getConfiguration() {
		return null;
	}

	public void configure(ConfigurationType config) {
		//
	}

}
