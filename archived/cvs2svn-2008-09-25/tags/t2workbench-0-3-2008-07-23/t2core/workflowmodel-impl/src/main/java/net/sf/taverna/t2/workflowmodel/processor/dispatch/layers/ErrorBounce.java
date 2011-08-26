package net.sf.taverna.t2.workflowmodel.processor.dispatch.layers;

import static net.sf.taverna.t2.workflowmodel.processor.dispatch.description.DispatchLayerStateEffect.CREATE_PROCESS_STATE;
import static net.sf.taverna.t2.workflowmodel.processor.dispatch.description.DispatchLayerStateEffect.NO_EFFECT;
import static net.sf.taverna.t2.workflowmodel.processor.dispatch.description.DispatchLayerStateEffect.UPDATE_PROCESS_STATE;
import static net.sf.taverna.t2.workflowmodel.processor.dispatch.description.DispatchMessageType.RESULT;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import net.sf.taverna.t2.invocation.Event;
import net.sf.taverna.t2.monitor.MonitorableProperty;
import net.sf.taverna.t2.monitor.NoSuchPropertyException;
import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.workflowmodel.OutputPort;
import net.sf.taverna.t2.workflowmodel.Processor;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.AbstractDispatchLayer;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.PropertyContributingDispatchLayer;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.description.DispatchLayerErrorReaction;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.description.DispatchLayerJobReaction;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.description.DispatchLayerResultCompletionReaction;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.description.DispatchLayerResultReaction;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.description.SupportsStreamedResult;
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
public class ErrorBounce extends AbstractDispatchLayer<Object> implements
		PropertyContributingDispatchLayer<Object> {

	/**
	 * Track the number of reflected and translated errors handled by this error
	 * bounce instance
	 */
	private Map<String, ErrorBounceState> state = new HashMap<String, ErrorBounceState>();

	private synchronized ErrorBounceState getState(String owningProcess) {
		if (state.containsKey(owningProcess)) {
			return state.get(owningProcess);
		} else {
			ErrorBounceState ebs = new ErrorBounceState();
			state.put(owningProcess, ebs);
			return ebs;
		}
	}

	/**
	 * If the job contains errors, or collections which contain errors
	 * themselves then bounce a result message with error documents in back up
	 * to the layer above
	 */
	@Override
	public void receiveJob(DispatchJobEvent jobEvent) {
		for (T2Reference ei : jobEvent.getData().values()) {
			if (ei.containsErrors()) {
				getState(jobEvent.getOwningProcess())
						.incrementErrorsReflected();
				sendErrorOutput(jobEvent);
				return;
			}
		}

		// Got here so relay the message down...
		getBelow().receiveJob(jobEvent);
	}

	/**
	 * Always send the error document job result on receiving a failure, at
	 * least for now! This should be configurable, in effect this is the part
	 * that ensures the processor never sees a top level failure.
	 */
	@Override
	public void receiveError(DispatchErrorEvent errorEvent) {
		getState(errorEvent.getOwningProcess()).incrementErrorsTranslated();
		sendErrorOutput(errorEvent);
	}

	/**
	 * Construct and send a new result message with error documents in place of
	 * all outputs at the appropriate depth
	 * 
	 * @param e
	 */
	private void sendErrorOutput(Event<?> e) {
		ReferenceService rs = e.getContext().getReferenceService();

		// DataManager dm = e.getContext().getDataManager();
		Processor p = dispatchStack.getProcessor();
		Map<String, T2Reference> outputDataMap = new HashMap<String, T2Reference>();
		for (OutputPort op : p.getOutputPorts()) {
			outputDataMap.put(op.getName(), rs.getErrorDocumentService()
					.registerError("No message...", op.getDepth()).getId());
		}
		DispatchResultEvent dre = new DispatchResultEvent(e.getOwningProcess(),
				e.getIndex(), e.getContext(), outputDataMap, false);
		getAbove().receiveResult(dre);
	}

	public void configure(Object config) {
		// Do nothing - no configuration required
	}

	public Object getConfiguration() {
		// Layer has no configuration associated
		return null;
	}

	public void finishedWith(String owningProcess) {
		state.remove(owningProcess);
	}

	/**
	 * Two properties, dispatch.errorbounce.reflected(integer) is the number of
	 * incoming jobs which have been bounced back as results with errors,
	 * dispatch.errorbounce.translated(integer) is the number of failures from
	 * downstream in the stack that have been re-written as complete results
	 * containing error documents.
	 */
	public void injectPropertiesFor(final String owningProcess) {

		MonitorableProperty<Integer> errorsReflectedProperty = new MonitorableProperty<Integer>() {
			public Date getLastModified() {
				return new Date();
			}

			public String[] getName() {
				return new String[] { "dispatch", "errorbounce", "reflected" };
			}

			public Integer getValue() throws NoSuchPropertyException {
				ErrorBounceState ebs = state.get(owningProcess);
				if (ebs == null) {
					return 0;
				} else {
					return ebs.getErrorsReflected();
				}
			}
		};
		dispatchStack.receiveMonitorableProperty(errorsReflectedProperty,
				owningProcess);

		MonitorableProperty<Integer> errorsTranslatedProperty = new MonitorableProperty<Integer>() {
			public Date getLastModified() {
				return new Date();
			}

			public String[] getName() {
				return new String[] { "dispatch", "errorbounce", "translated" };
			}

			public Integer getValue() throws NoSuchPropertyException {
				ErrorBounceState ebs = state.get(owningProcess);
				if (ebs == null) {
					return 0;
				} else {
					return ebs.getErrorsTranslated();
				}
			}
		};
		dispatchStack.receiveMonitorableProperty(errorsTranslatedProperty,
				owningProcess);

	}

	class ErrorBounceState {
		private int errorsReflected = 0;
		private int errorsTranslated = 0;

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

}
