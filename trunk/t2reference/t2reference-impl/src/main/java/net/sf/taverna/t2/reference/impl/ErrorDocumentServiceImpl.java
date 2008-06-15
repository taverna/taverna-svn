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

	public ErrorDocument registerError(String message, Throwable t, int depth)
			throws ErrorDocumentServiceException {
		checkDao();
		checkGenerator();
		ErrorDocumentImpl edi = new ErrorDocumentImpl();
		T2Reference ref = t2ReferenceGenerator
				.nextErrorDocumentReference(depth);
		if (ref instanceof T2ReferenceImpl) {
			edi.setTypedId((T2ReferenceImpl) ref);
		} else {
			throw new ErrorDocumentServiceException(
					"Can't use this ID generator, the generated IDs "
							+ "aren't instances of T2ReferenceImpl");
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
			return edi;
		} catch (Throwable t2) {
			throw new ErrorDocumentServiceException(t2);
		}
	}

}
