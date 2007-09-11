package net.sf.taverna.t2.workflowmodel.processor.dispatch;

import net.sf.taverna.t2.invocation.Completion;
import net.sf.taverna.t2.workflowmodel.processor.activity.Job;

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
		messageActions.put(DispatchMessageType.JOBQUEUE, DispatchLayerAction.PASSTHROUGH);
	}
	
	public void receiveResult(Job j) {
		System.out.println("  "+j);
		getAbove().receiveResult(j);
	}

	public void receiveResultCompletion(Completion c) {
		System.out.println("  "+c);
		getAbove().receiveResultCompletion(c);
	}

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
