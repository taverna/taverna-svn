package net.sf.taverna.t2.workflowmodel.processor.dispatch;

import net.sf.taverna.t2.invocation.Completion;
import net.sf.taverna.t2.workflowmodel.processor.activity.Job;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.events.DispatchCompletionEvent;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.events.DispatchResultEvent;

/**
 * Debug dispatch stack layer, prints to stdout when it receives a result or
 * completion and when the cache purge message is sent from the parent dispatch
 * stack.
 * 
 * @author Tom
 * 
 */
public class DiagnosticLayer extends AbstractDispatchLayer<Object> {

	public DiagnosticLayer() {
		super();
	}

	@Override
	public void receiveResult(DispatchResultEvent resultEvent) {
		System.out.println("  "
				+ new Job(resultEvent.getOwningProcess(), resultEvent
						.getIndex(), resultEvent.getData(), resultEvent
						.getContext()));
		getAbove().receiveResult(resultEvent);
	}

	@Override
	public void receiveResultCompletion(DispatchCompletionEvent completionEvent) {
		System.out.println("  "
				+ new Completion(completionEvent.getOwningProcess(),
						completionEvent.getIndex(), completionEvent
								.getContext()));
		getAbove().receiveResultCompletion(completionEvent);
	}

	@Override
	public void finishedWith(String process) {
		System.out.println("  Purging caches for " + process);
	}

	public void configure(Object config) {
		// Do nothing
	}

	public Object getConfiguration() {
		return null;
	}

}
