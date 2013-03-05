/**
 * 
 */
package net.sf.taverna.t2.component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sf.taverna.t2.component.profile.ExceptionHandling;
import net.sf.taverna.t2.component.profile.HandleException;
import net.sf.taverna.t2.invocation.InvocationContext;
import net.sf.taverna.t2.reference.ErrorDocument;
import net.sf.taverna.t2.reference.ErrorDocumentService;
import net.sf.taverna.t2.reference.IdentifiedList;
import net.sf.taverna.t2.reference.ListService;
import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.reference.T2ReferenceType;
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
	private final ExceptionHandling exceptionHandling;
	private ListService listService;
	private ErrorDocumentService errorService;

	/**
	 * @param originalCallback
	 * @param exceptionHandling2 
	 */
	public ProxyCallback(AsynchronousActivityCallback originalCallback, ExceptionHandling exceptionHandling2) {
		super();
		this.originalCallback = originalCallback;
		this.exceptionHandling = exceptionHandling2;
		context = originalCallback.getContext();
		referenceService = context.getReferenceService();
		listService = referenceService.getListService();
		errorService = referenceService.getErrorDocumentService();
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
		List<T2Reference> exceptions = new ArrayList<T2Reference>();
		Map<String, T2Reference> replacement = new HashMap<String, T2Reference>();
		for (Entry<String, T2Reference> entry : data.entrySet()) {
			String key = entry.getKey();
			T2Reference value = entry.getValue();
				T2Reference replacementReference = considerReference(value, exceptions);
				replacement.put(key, replacementReference);
		}
		T2Reference exceptionsReference = referenceService.register(exceptions, 1, true, context);
		replacement.put("error_channel", exceptionsReference);
		return replacement;
	}
	


	private T2Reference considerReference(T2Reference value,
			List<T2Reference> exceptions) {
		if (!value.containsErrors()) {
			return value;
		}

		if (value.getReferenceType().equals(T2ReferenceType.IdentifiedList)) {
			if (exceptionHandling.failLists()) {
				T2Reference failure = findFirstFailure(value);
				T2Reference replacement = replaceErrors(failure, value.getDepth(), exceptions);
				return replacement;
			} else {
				IdentifiedList<T2Reference> originalList = listService.getList(value);
				List<T2Reference> replacementList = new ArrayList<T2Reference>();
				for (T2Reference subValue : originalList) {
					replacementList.add(considerReference(subValue, exceptions));
				}
				return referenceService.register(replacementList, value.getDepth(), true, context);
			}
		} else {
			return replaceErrors(value, exceptions);
		}
	}

	private T2Reference findFirstFailure(T2Reference value) {
		IdentifiedList<T2Reference> originalList = listService.getList(value);
		for (T2Reference subValue : originalList) {
			if (subValue.getReferenceType().equals(
					T2ReferenceType.ErrorDocument)) {
				return subValue;
			}
			if (subValue.getReferenceType().equals(
					T2ReferenceType.IdentifiedList)) {
				if (subValue.containsErrors()) {
					return findFirstFailure(subValue);
				}
			}
			// No need to consider value
		}
		return null;
	}

	private T2Reference replaceErrors(T2Reference value, List<T2Reference> exceptions) {
		return replaceErrors(value, value.getDepth(), exceptions);
	}

	private T2Reference replaceErrors(T2Reference value, int depth, List<T2Reference> exceptions) {
		ErrorDocument doc = errorService.getError(value);
		ComponentException newException = null;
		boolean found = false;
		  String exceptionMessage = doc.getExceptionMessage();
		for (HandleException he : exceptionHandling.getHandleExceptions()) {
			if (he.matches(exceptionMessage)) {
				String fred="no";
			}
		}
		if (!found && (newException == null)) {
		newException = ComponentExceptionFactory.createUnexpectedComponentException(exceptionMessage);
		}
		T2Reference replacement =
			referenceService.register(newException, depth, true, context);
		exceptions.add(referenceService.register(newException, 0, true, context));
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
