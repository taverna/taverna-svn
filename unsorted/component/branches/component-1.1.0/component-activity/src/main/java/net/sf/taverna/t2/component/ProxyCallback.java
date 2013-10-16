/**
 * 
 */
package net.sf.taverna.t2.component;

import static net.sf.taverna.t2.component.ComponentExceptionFactory.createComponentException;
import static net.sf.taverna.t2.component.ComponentExceptionFactory.createUnexpectedComponentException;
import static net.sf.taverna.t2.reference.T2ReferenceType.ErrorDocument;
import static net.sf.taverna.t2.reference.T2ReferenceType.IdentifiedList;
import static net.sf.taverna.t2.reference.T2ReferenceType.ReferenceSet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import net.sf.taverna.t2.component.profile.ExceptionHandling;
import net.sf.taverna.t2.component.profile.ExceptionReplacement;
import net.sf.taverna.t2.component.profile.HandleException;
import net.sf.taverna.t2.invocation.InvocationContext;
import net.sf.taverna.t2.reference.ErrorDocument;
import net.sf.taverna.t2.reference.ErrorDocumentService;
import net.sf.taverna.t2.reference.IdentifiedList;
import net.sf.taverna.t2.reference.ListService;
import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.workflowmodel.processor.activity.AsynchronousActivityCallback;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.events.DispatchErrorType;

import org.apache.log4j.Logger;

/**
 * @author alanrw
 * 
 */
public class ProxyCallback implements AsynchronousActivityCallback {
	private static final Logger logger = Logger.getLogger(ProxyCallback.class);

	private AsynchronousActivityCallback originalCallback;
	private final ReferenceService referenceService;
	private final InvocationContext context;
	private final ExceptionHandling exceptionHandling;
	private ListService listService;
	private ErrorDocumentService errorService;

	/**
	 * @param originalCallback
	 * @param invocationContext
	 * @param exceptionHandling2
	 */
	public ProxyCallback(AsynchronousActivityCallback originalCallback,
			InvocationContext invocationContext,
			ExceptionHandling exceptionHandling2) {
		super();
		this.originalCallback = originalCallback;
		this.exceptionHandling = exceptionHandling2;
		context = invocationContext;
		referenceService = context.getReferenceService();
		listService = referenceService.getListService();
		errorService = referenceService.getErrorDocumentService();
	}

	@Override
	public InvocationContext getContext() {
		return context;
	}

	@Override
	public void requestRun(Runnable runMe) {
		originalCallback.requestRun(runMe);
	}

	@Override
	public void receiveResult(Map<String, T2Reference> data, int[] index) {
		if (exceptionHandling == null) {
			originalCallback.receiveResult(data, index);
		} else {
			Map<String, T2Reference> errorReplacedData = replaceErrors(data);
			originalCallback.receiveResult(errorReplacedData, index);
		}
	}

	private Map<String, T2Reference> replaceErrors(Map<String, T2Reference> data) {
		List<T2Reference> exceptions = new ArrayList<T2Reference>();
		Map<String, T2Reference> replacement = new HashMap<String, T2Reference>();
		for (Entry<String, T2Reference> entry : data.entrySet()) {
			String key = entry.getKey();
			T2Reference value = entry.getValue();
			T2Reference replacementReference = considerReference(value,
					exceptions);
			replacement.put(key, replacementReference);
		}
		T2Reference exceptionsReference = referenceService.register(exceptions,
				1, true, context);
		replacement.put("error_channel", exceptionsReference);
		return replacement;
	}

	private T2Reference considerReference(T2Reference value,
			List<T2Reference> exceptions) {
		if (!value.containsErrors()) {
			return value;
		} else if (!value.getReferenceType().equals(IdentifiedList)) {
			return replaceErrors(value, exceptions);
		} else if (exceptionHandling.failLists()) {
			T2Reference failure = findFirstFailure(value);
			T2Reference replacement = replaceErrors(failure, value.getDepth(),
					exceptions);
			return replacement;
		} else {
			IdentifiedList<T2Reference> originalList = listService
					.getList(value);
			List<T2Reference> replacementList = new ArrayList<T2Reference>();
			for (T2Reference subValue : originalList)
				replacementList.add(considerReference(subValue, exceptions));
			return referenceService.register(replacementList, value.getDepth(),
					true, context);
		}
	}

	private T2Reference findFirstFailure(T2Reference value) {
		IdentifiedList<T2Reference> originalList = listService.getList(value);
		for (T2Reference subValue : originalList) {
			if (subValue.getReferenceType().equals(ErrorDocument))
				return subValue;
			if (subValue.getReferenceType().equals(IdentifiedList))
				if (subValue.containsErrors())
					return findFirstFailure(subValue);
			// No need to consider value
		}
		return null;
	}

	private T2Reference replaceErrors(T2Reference value,
			List<T2Reference> exceptions) {
		return replaceErrors(value, value.getDepth(), exceptions);
	}

	private T2Reference replaceErrors(T2Reference value, int depth,
			List<T2Reference> exceptions) {
		ErrorDocument doc = errorService.getError(value);
		HandleException matchingHandleException = null;

		ErrorDocument matchingDoc = doc;

		Set<ErrorDocument> toConsider = new HashSet<ErrorDocument>();
		Set<ErrorDocument> considered = new HashSet<ErrorDocument>();
		toConsider.add(doc);

		boolean found = false;
		while (!toConsider.isEmpty() && !found) {
			try {
				ErrorDocument errorDoc = toConsider.iterator().next();

				considered.add(errorDoc);
				toConsider.remove(errorDoc);
				String exceptionMessage = errorDoc.getExceptionMessage();
				for (HandleException he : exceptionHandling
						.getHandleExceptions())
					if (he.matches(exceptionMessage)) {
						found = true;
						matchingHandleException = he;
						matchingDoc = errorDoc;
					}
				if (!errorDoc.getErrorReferences().isEmpty())
					for (T2Reference subRef : errorDoc.getErrorReferences())
						for (T2Reference newErrorRef : getErrors(subRef)) {
							ErrorDocument subDoc = errorService
									.getError(newErrorRef);
							if (subDoc == null)
								logger.error("Error document contains references to non-existent sub-errors");
							else if (!considered.contains(subDoc))
								toConsider.add(subDoc);
						}
			} catch (Exception e) {
				logger.error(e);
			}

		}

		String exceptionMessage = matchingDoc.getExceptionMessage();
		// An exception that is not mentioned
		if (matchingHandleException == null) {
			ComponentException newException = createUnexpectedComponentException(exceptionMessage);
			T2Reference replacement = errorService.registerError(
					exceptionMessage, newException, depth, context).getId();
			exceptions.add(errorService.registerError(exceptionMessage,
					newException, 0, context).getId());
			return replacement;
		}

		if (matchingHandleException.pruneStack()) {
			matchingDoc.getStackTraceStrings().clear();
		}
		ExceptionReplacement exceptionReplacement = matchingHandleException
				.getReplacement();
		if (exceptionReplacement == null) {
			T2Reference replacement = referenceService.register(matchingDoc,
					depth, true, context);
			exceptions.add(referenceService.register(matchingDoc, 0, true,
					context));
			return replacement;
		} else {
			ComponentException newException = createComponentException(
					exceptionReplacement.getReplacementId(),
					exceptionReplacement.getReplacementMessage());
			T2Reference replacement = errorService.registerError(
					exceptionReplacement.getReplacementMessage(), newException,
					depth, context).getId();
			exceptions.add(errorService.registerError(
					exceptionReplacement.getReplacementMessage(), newException,
					0, context).getId());
			return replacement;
		}
	}

	private Set<T2Reference> getErrors(T2Reference ref) {
		Set<T2Reference> result = new HashSet<T2Reference>();
		if (ref.getReferenceType().equals(ReferenceSet)) {
			// nothing
		} else if (ref.getReferenceType().equals(IdentifiedList)) {
			IdentifiedList<T2Reference> originalList = listService.getList(ref);
			for (T2Reference subValue : originalList)
				if (subValue.containsErrors())
					result.addAll(getErrors(subValue));
		} else {
			result.add(ref);
		}
		return result;
	}

	@Override
	public void receiveCompletion(int[] completionIndex) {
		originalCallback.receiveCompletion(completionIndex);
	}

	@Override
	public void fail(String message, Throwable t, DispatchErrorType errorType) {
		originalCallback.fail(message, t, errorType);
	}

	@Override
	public void fail(String message, Throwable t) {
		originalCallback.fail(message, t);
	}

	@Override
	public void fail(String message) {
		originalCallback.fail(message);
	}

	@Override
	public String getParentProcessIdentifier() {
		// return "";
		return originalCallback.getParentProcessIdentifier();
	}

}
