package net.sf.taverna.t2.reference.impl;

import net.sf.taverna.t2.reference.ErrorDocument;
import net.sf.taverna.t2.reference.ErrorDocumentService;
import net.sf.taverna.t2.reference.ErrorDocumentServiceException;
import net.sf.taverna.t2.reference.T2Reference;

/**
 * Implementation of ErrorDocumentService, inject with an appropriate
 * ErrorDocumentDao and T2ReferenceGenerator to enable.
 * 
 * @author Tom Oinn
 * 
 */
public class ErrorDocumentServiceImpl extends AbstractErrorDocumentServiceImpl
		implements ErrorDocumentService {

	public ErrorDocument getError(T2Reference id)
			throws ErrorDocumentServiceException {
		checkDao();
		try {
			return errorDao.get(id);
		} catch (Throwable t) {
			throw new ErrorDocumentServiceException(t);
		}
	}

	/**
	 * Register the specified error and any child errors (which have the same
	 * namespace and local part but a lower depth, down to depth of zero
	 */
	public ErrorDocument registerError(String message, Throwable t, int depth)
			throws ErrorDocumentServiceException {
		checkDao();
		checkGenerator();

		T2Reference ref = t2ReferenceGenerator
				.nextErrorDocumentReference(depth);
		T2ReferenceImpl typedId = T2ReferenceImpl.getAsImpl(ref);

		ErrorDocument docToReturn = null;
		while (depth >= 0) {
			ErrorDocumentImpl edi = new ErrorDocumentImpl();
			if (docToReturn == null) {
				docToReturn = edi;
			}
			edi.setTypedId(typedId);
			if (message != null) {
				edi.setMessage(message);
			} else {
				edi.setMessage("");
			}
			if (t != null) {
				edi.setExceptionMessage(t.getMessage());
				for (StackTraceElement ste : t.getStackTrace()) {
					StackTraceElementBeanImpl stebi = new StackTraceElementBeanImpl();
					stebi.setClassName(ste.getClassName());
					stebi.setFileName(ste.getFileName());
					stebi.setLineNumber(ste.getLineNumber());
					stebi.setMethodName(ste.getMethodName());
					edi.stackTrace.add(stebi);
				}
			} else {
				edi.setExceptionMessage("");
			}
			try {
				errorDao.store(edi);
			} catch (Throwable t2) {
				throw new ErrorDocumentServiceException(t2);
			}
			if (depth > 0) {
				typedId = typedId.getDeeperErrorReference();
			}
			depth--;
		}
		return docToReturn;

	}

	public T2Reference getChild(T2Reference errorId)
			throws ErrorDocumentServiceException {
		T2ReferenceImpl refImpl = T2ReferenceImpl.getAsImpl(errorId);
		try {
			return refImpl.getDeeperErrorReference();
		} catch (Throwable t) {
			throw new ErrorDocumentServiceException(t);
		}
	}

}
