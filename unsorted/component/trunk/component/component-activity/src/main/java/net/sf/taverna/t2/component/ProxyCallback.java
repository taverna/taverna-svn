/**
 * 
 */
package net.sf.taverna.t2.component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import net.sf.taverna.t2.activities.dataflow.DataflowActivity;
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
import net.sf.taverna.t2.reference.T2ReferenceType;
import net.sf.taverna.t2.reference.impl.ErrorDocumentImpl;
import net.sf.taverna.t2.workflowmodel.processor.activity.AsynchronousActivityCallback;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.events.DispatchErrorType;

/**
 * @author alanrw
 *
 */
public class ProxyCallback implements AsynchronousActivityCallback {
	
	private static final Logger logger = Logger
	.getLogger(ProxyCallback.class);

	
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
					for (HandleException he : exceptionHandling.getHandleExceptions()) {
						if (he.matches(exceptionMessage)) {
							found = true;
							matchingHandleException = he;
							matchingDoc = errorDoc;
						}
					}
					if (!errorDoc.getErrorReferences().isEmpty()) {
						for (T2Reference subRef : errorDoc.getErrorReferences()) {
							Set<T2Reference> newErrors = getErrors(subRef);
							for (T2Reference newErrorRef : newErrors) {
								ErrorDocument subDoc = errorService.getError(newErrorRef);
								if (subDoc == null) {
									logger.error("Error document contains references to non-existent sub-errors");
								} else {
									if (!considered.contains(subDoc)) {
										toConsider.add(subDoc);
									}
								}
							}
						}
					}
}
					catch (Exception e) {
						logger.error(e);
					}

			}
			
			String exceptionMessage = matchingDoc.getExceptionMessage();
		// An exception that is not mentioned
		if (matchingHandleException == null) {
			ComponentException newException = ComponentExceptionFactory.createUnexpectedComponentException(exceptionMessage);
			T2Reference replacement = errorService.registerError(exceptionMessage , newException, depth, context).getId();
			exceptions.add(errorService.registerError(exceptionMessage , newException, 0, context).getId());
			return replacement;
		}
		
		if (matchingHandleException.pruneStack()) {
			matchingDoc.getStackTraceStrings().clear();
		}
		ExceptionReplacement exceptionReplacement = matchingHandleException.getReplacement();
		if (exceptionReplacement == null) {
			T2Reference replacement = referenceService.register(matchingDoc, depth, true, context);
			exceptions.add(referenceService.register(matchingDoc, 0, true, context));
			return replacement;
		} else {
			ComponentException newException = ComponentExceptionFactory.createComponentException(exceptionReplacement.getReplacementId(), exceptionReplacement.getReplacementMessage());
			T2Reference replacement = errorService.registerError(exceptionReplacement.getReplacementMessage() , newException, depth, context).getId();
		exceptions.add(errorService.registerError(exceptionReplacement.getReplacementMessage() , newException, 0, context).getId());
			return replacement;
		}
	}

	private Set<T2Reference> getErrors(T2Reference ref) {
		Set<T2Reference> result = new HashSet<T2Reference> ();
		if (ref.getReferenceType().equals(T2ReferenceType.ReferenceSet)) {
			// nothing
		}
		else if (ref.getReferenceType().equals(T2ReferenceType.IdentifiedList)) {
			IdentifiedList<T2Reference> originalList = listService.getList(ref);
			for (T2Reference subValue : originalList) {
				if (subValue.containsErrors()) {
					result.addAll(getErrors(subValue));
				}
			}
			
		} else {
			result.add(ref);
		}
		return result;
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
