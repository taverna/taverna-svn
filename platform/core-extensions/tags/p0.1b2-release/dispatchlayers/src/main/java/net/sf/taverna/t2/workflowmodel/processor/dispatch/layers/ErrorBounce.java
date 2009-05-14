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

import static net.sf.taverna.t2.workflowmodel.processor.dispatch.description.DispatchLayerStateEffect.CREATE_PROCESS_STATE;
import static net.sf.taverna.t2.workflowmodel.processor.dispatch.description.DispatchLayerStateEffect.NO_EFFECT;
import static net.sf.taverna.t2.workflowmodel.processor.dispatch.description.DispatchLayerStateEffect.UPDATE_PROCESS_STATE;
import static net.sf.taverna.t2.workflowmodel.processor.dispatch.description.DispatchMessageType.RESULT;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.sf.taverna.t2.monitor.MonitorableProperty;
import net.sf.taverna.t2.monitor.NoSuchPropertyException;
import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.workflowmodel.Processor;
import net.sf.taverna.t2.workflowmodel.ProcessorOutputPort;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.AbstractDispatchLayer;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.DispatchLayerCallback;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.DispatchLayerStateScoping;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.DispatchStack;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.PropertyContributingDispatchLayer;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.description.DispatchLayerErrorReaction;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.description.DispatchLayerJobReaction;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.description.DispatchLayerResultCompletionReaction;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.description.DispatchLayerResultReaction;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.description.SupportsStreamedResult;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.events.AbstractDispatchEvent;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.events.DispatchErrorEvent;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.events.DispatchJobEvent;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.events.DispatchResultEvent;

/**
 * Receives job events, checks to see whether any parameters in the job are
 * error tokens or collections which contain errors. If so then sends a
 * corresponding result message back where all outputs are error tokens having
 * registered such with the invocation context's data manager. It also re-writes
 * any failure messages as result messages containing error tokens at the
 * appropriate depth - this means that it must be placed above any error
 * handling layers in order for those to have an effect at all. In general this
 * layer should be placed immediately below the parallelize layer in most
 * default cases (this will guarantee the processor never sees a failure message
 * though, which may or may not be desirable)
 * 
 * @author Tom Oinn
 * 
 */
@DispatchLayerErrorReaction(emits = { RESULT }, relaysUnmodified = false, stateEffects = {
		CREATE_PROCESS_STATE, UPDATE_PROCESS_STATE })
@DispatchLayerJobReaction(emits = { RESULT }, relaysUnmodified = true, stateEffects = {
		CREATE_PROCESS_STATE, UPDATE_PROCESS_STATE, NO_EFFECT })
@DispatchLayerResultReaction(emits = {}, relaysUnmodified = true, stateEffects = {})
@DispatchLayerResultCompletionReaction(emits = {}, relaysUnmodified = true, stateEffects = {})
@SupportsStreamedResult
public class ErrorBounce extends
		AbstractDispatchLayer<Object, ErrorBounce.ErrorBounceState> implements
		PropertyContributingDispatchLayer<Object, ErrorBounce.ErrorBounceState> {

	/**
	 * If the job contains errors, or collections which contain errors
	 * themselves then bounce a result message with error documents in back up
	 * to the layer above
	 */
	@Override
	public void receiveJob(DispatchJobEvent jobEvent,
			DispatchLayerCallback callback, ErrorBounceState state) {
		Set<T2Reference> errorReferences = new HashSet<T2Reference>();
		for (T2Reference ei : jobEvent.getData().values()) {
			if (ei.containsErrors()) {
				errorReferences.add(ei);
			}
		}
		if (errorReferences.isEmpty()) {
			// relay the message down...
			callback.sendJob(jobEvent);
		} else {
			state.incrementErrorsReflected();
			sendErrorOutput(jobEvent, null, errorReferences, callback, state);
		}

	}

	/**
	 * Always send the error document job result on receiving a failure, at
	 * least for now! This should be configurable, in effect this is the part
	 * that ensures the processor never sees a top level failure.
	 */
	@Override
	public void receiveError(DispatchErrorEvent errorEvent,
			DispatchLayerCallback callback, ErrorBounceState state) {
		state.incrementErrorsTranslated();
		this.sendErrorOutput(errorEvent, errorEvent.getCause(), null, callback,
				state);
	}

	/**
	 * Construct and send a new result message with error documents in place of
	 * all outputs at the appropriate depth
	 * 
	 * @param event
	 * @param cause
	 * @param errorReferences
	 */
	@SuppressWarnings("unchecked")
	private void sendErrorOutput(AbstractDispatchEvent event, Throwable cause,
			Set<T2Reference> errorReferences, DispatchLayerCallback callback,
			ErrorBounceState state) {
		ReferenceService rs = event.getContext().getReferenceService();
		Map<String, T2Reference> outputDataMap = new HashMap<String, T2Reference>();
		String processor = state.processor.getLocalName();
		for (ProcessorOutputPort op : state.processor.getOutputPorts()) {
			String processorPortName = op.getName();
			String message = "Processor '" + processor + "' - Port '"
					+ processorPortName + "'";
			if (cause != null) {
				outputDataMap.put(op.getName(), rs.getErrorDocumentService()
						.registerError(message, cause, op.getDepth(),
								event.getContext()).getId());
			} else {
				outputDataMap.put(op.getName(), rs.getErrorDocumentService()
						.registerError(message, errorReferences, op.getDepth(),
								event.getContext()).getId());
			}
		}
		DispatchResultEvent dre = new DispatchResultEvent(event
				.getOwningProcess(), event.getIndex(), event.getContext(),
				outputDataMap, false);
		callback.sendResult(dre);
	}

	/**
	 * Two properties, dispatch.errorbounce.reflected(integer) is the number of
	 * incoming jobs which have been bounced back as results with errors,
	 * dispatch.errorbounce.translated(integer) is the number of failures from
	 * downstream in the stack that have been re-written as complete results
	 * containing error documents.
	 */
	public void injectPropertiesFor(DispatchStack stack,
			final String owningProcess, final ErrorBounceState state) {

		MonitorableProperty<Integer> errorsReflectedProperty = new MonitorableProperty<Integer>() {

			public Date getLastModified() {
				return new Date();
			}

			public String[] getName() {
				return new String[] { "dispatch", "errorbounce", "reflected" };
			}

			public Integer getValue() throws NoSuchPropertyException {
				if (state == null) {
					return 0;
				} else {
					return state.getErrorsReflected();
				}
			}

		};

		stack
				.receiveMonitorableProperty(errorsReflectedProperty,
						owningProcess);

		MonitorableProperty<Integer> errorsTranslatedProperty = new MonitorableProperty<Integer>() {
			public Date getLastModified() {
				return new Date();
			}

			public String[] getName() {
				return new String[] { "dispatch", "errorbounce", "translated" };
			}

			public Integer getValue() throws NoSuchPropertyException {
				if (state == null) {
					return 0;
				} else {
					return state.getErrorsTranslated();
				}
			}
		};
		stack.receiveMonitorableProperty(errorsTranslatedProperty,
				owningProcess);

	}

	public class ErrorBounceState {
		private int errorsReflected = 0;
		private int errorsTranslated = 0;
		Processor processor = null;

		public ErrorBounceState(Processor processor) {
			this.processor = processor;
		}

		/**
		 * Number of times the bounce layer has converted an incoming job event
		 * where the input data contained error tokens into a result event
		 * containing all errors.
		 */
		int getErrorsReflected() {
			return this.errorsReflected;
		}

		/**
		 * Number of times the bounce layer has converted an incoming failure
		 * event into a result containing error tokens
		 */
		int getErrorsTranslated() {
			return this.errorsTranslated;
		}

		synchronized void incrementErrorsReflected() {
			errorsReflected++;
		}

		synchronized void incrementErrorsTranslated() {
			errorsTranslated++;
		}

	}

	public ErrorBounceState createNewStateModel(Processor parent) {
		return new ErrorBounceState(parent);
	}

	public DispatchLayerStateScoping getStateScope() {
		return DispatchLayerStateScoping.PROCESS;
	}

}
