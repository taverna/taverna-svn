/**
 * 
 */
package net.sf.taverna.t2.component;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import net.sf.taverna.t2.invocation.InvocationContext;
import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.workflowmodel.processor.activity.AsynchronousActivityCallback;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.events.DispatchErrorType;

/**
 * @author alanrw
 *
 */
public class ProxyCallback implements AsynchronousActivityCallback {
	
	private AsynchronousActivityCallback originalCallback;
	private final ReferenceService referenceService;
	private final InvocationContext context;

	/**
	 * @param originalCallback
	 */
	public ProxyCallback(AsynchronousActivityCallback originalCallback) {
		super();
		this.originalCallback = originalCallback;
		context = originalCallback.getContext();
		referenceService = context.getReferenceService();
	}

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.workflowmodel.processor.activity.AsynchronousActivityCallback#getContext()
	 */
	@Override
	public InvocationContext getContext() {
		return originalCallback.getContext();
	}

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.workflowmodel.processor.activity.AsynchronousActivityCallback#requestRun(java.lang.Runnable)
	 */
	@Override
	public void requestRun(Runnable runMe) {
		originalCallback.requestRun(runMe);
	}

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.workflowmodel.processor.activity.AsynchronousActivityCallback#receiveResult(java.util.Map, int[])
	 */
	@Override
	public void receiveResult(Map<String, T2Reference> data, int[] index) {
		Map<String, T2Reference> errorReplacedData = replaceErrors(data);
		originalCallback.receiveResult(errorReplacedData, index);
	}

	private Map<String, T2Reference> replaceErrors(Map<String, T2Reference> data) {
		Map<String, T2Reference> replacement = new HashMap<String, T2Reference>();
		for (Entry<String, T2Reference> entry : data.entrySet()) {
			String key = entry.getKey();
			T2Reference value = entry.getValue();
			if (value.containsErrors()) {
				T2Reference replacementValue = replaceErrors(value);
				replacement.put(key, replacementValue);
			} else {
				replacement.put(key, value);
			}
		}
		return replacement;
	}

	private T2Reference replaceErrors(T2Reference value) {
		T2Reference replacement =
			referenceService.register(new UnexpectedComponentException("fred"), value.getDepth(), true, context);
		return replacement;
	}

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.workflowmodel.processor.activity.AsynchronousActivityCallback#receiveCompletion(int[])
	 */
	@Override
	public void receiveCompletion(int[] completionIndex) {
		originalCallback.receiveCompletion(completionIndex);
	}

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.workflowmodel.processor.activity.AsynchronousActivityCallback#fail(java.lang.String, java.lang.Throwable, net.sf.taverna.t2.workflowmodel.processor.dispatch.events.DispatchErrorType)
	 */
	@Override
	public void fail(String message, Throwable t, DispatchErrorType errorType) {
		originalCallback.fail(message, t, errorType);
	}

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.workflowmodel.processor.activity.AsynchronousActivityCallback#fail(java.lang.String, java.lang.Throwable)
	 */
	@Override
	public void fail(String message, Throwable t) {
		originalCallback.fail(message, t);
	}

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.workflowmodel.processor.activity.AsynchronousActivityCallback#fail(java.lang.String)
	 */
	@Override
	public void fail(String message) {
		originalCallback.fail(message);
	}

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.workflowmodel.processor.activity.AsynchronousActivityCallback#getParentProcessIdentifier()
	 */
	@Override
	public String getParentProcessIdentifier() {
		return originalCallback.getParentProcessIdentifier();
	}

}
