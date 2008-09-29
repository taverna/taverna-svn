package net.sf.taverna.t2.workflowmodel.processor.dispatch.events;

import net.sf.taverna.t2.invocation.InvocationContext;
import net.sf.taverna.t2.invocation.ProcessIdentifierException;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.description.DispatchMessageType;

/**
 * Dispatch event containing detailing a (potentially partial) completion of a
 * stream of streaming result events. Layers which do not support streaming by
 * definition can't cope with this event and the dispatch stack checker should
 * prevent them from ever seeing it.
 * 
 * @author Tom Oinn
 * 
 */
public class DispatchCompletionEvent extends
		AbstractDispatchEvent<DispatchCompletionEvent> {

	/**
	 * Construct a new dispatch result completion event
	 * 
	 * @param owner
	 * @param index
	 * @param context
	 */
	public DispatchCompletionEvent(String owner, int[] index,
			InvocationContext context) {
		super(owner, index, context);
	}

	@Override
	public DispatchCompletionEvent popOwningProcess()
			throws ProcessIdentifierException {
		return new DispatchCompletionEvent(popOwner(), index, context);
	}

	@Override
	public DispatchCompletionEvent pushOwningProcess(String localProcessName)
			throws ProcessIdentifierException {
		return new DispatchCompletionEvent(pushOwner(localProcessName), index,
				context);
	}

	/**
	 * DispatchMessageType.RESULT_COMPLETION
	 */
	@Override
	public DispatchMessageType getMessageType() {
		return DispatchMessageType.RESULT_COMPLETION;
	}

}
