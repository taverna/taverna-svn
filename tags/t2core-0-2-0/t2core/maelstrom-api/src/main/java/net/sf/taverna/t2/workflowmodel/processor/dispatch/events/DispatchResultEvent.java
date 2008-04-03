package net.sf.taverna.t2.workflowmodel.processor.dispatch.events;

import java.util.Map;

import net.sf.taverna.t2.invocation.InvocationContext;
import net.sf.taverna.t2.invocation.ProcessIdentifierException;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.description.DispatchMessageType;
import net.sf.taverna.t2.cloudone.identifier.EntityIdentifier;

/**
 * Dispatch event containing the results from an invocation. If the event is
 * part of a stream of such events from a single job invocation the streaming
 * flag will be set to true - when set layers that do not support streaming
 * should either disable any related functionality or complain bitterly. They
 * should never see such an event as the type checker will in the future catch
 * such cases before they occur but for now it's something to watch for.
 * 
 * @author Tom Oinn
 * 
 */
public class DispatchResultEvent extends
		AbstractDispatchEvent<DispatchResultEvent> {

	private Map<String, EntityIdentifier> dataMap;
	private boolean streaming;

	/**
	 * Construct a new dispatch result event, specifying the data and whether
	 * the result is part of a stream of multiple results events from a single
	 * invocation
	 * 
	 * @param owner
	 * @param index
	 * @param context
	 * @param data
	 * @param streaming
	 */
	public DispatchResultEvent(String owner, int[] index,
			InvocationContext context, Map<String, EntityIdentifier> data,
			boolean streaming) {
		super(owner, index, context);
		this.dataMap = data;
		this.streaming = streaming;
	}

	/**
	 * If this result is part of a stream, that is to say multiple result events
	 * from a single job event, then return true otherwise return false.
	 * 
	 * @return whether this is part of a streamed result set
	 */
	public boolean isStreamingEvent() {
		return this.streaming;
	}

	/**
	 * The result contains a map of named EntityIdentifier instances
	 * corresponding to the result data.
	 * 
	 * @return the result data for this event
	 */
	public Map<String, EntityIdentifier> getData() {
		return this.dataMap;
	}

	@Override
	public DispatchResultEvent popOwningProcess()
			throws ProcessIdentifierException {
		return new DispatchResultEvent(popOwner(), index, context, dataMap,
				streaming);
	}

	@Override
	public DispatchResultEvent pushOwningProcess(String localProcessName)
			throws ProcessIdentifierException {
		return new DispatchResultEvent(pushOwner(localProcessName), index,
				context, dataMap, streaming);
	}

	/**
	 * DispatchMessageType.RESULT
	 */
	@Override
	public DispatchMessageType getMessageType() {
		return DispatchMessageType.RESULT;
	}

}
