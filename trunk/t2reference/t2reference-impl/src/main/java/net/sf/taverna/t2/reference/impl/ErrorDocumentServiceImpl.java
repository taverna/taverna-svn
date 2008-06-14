package net.sf.taverna.t2.reference.impl;

import net.sf.taverna.t2.reference.DaoException;
import net.sf.taverna.t2.reference.ErrorDocument;
import net.sf.taverna.t2.reference.ErrorDocumentDao;
import net.sf.taverna.t2.reference.ErrorDocumentService;
import net.sf.taverna.t2.reference.ErrorDocumentServiceCallback;
import net.sf.taverna.t2.reference.ErrorDocumentServiceException;
import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.reference.T2ReferenceGenerator;

/**
 * Implementation of ErrorDocumentService, inject with an appropriate
 * ErrorDocumentDao and T2ReferenceGenerator to enable.
 * 
 * @author Tom Oinn
 * 
 */
public class ErrorDocumentServiceImpl implements ErrorDocumentService {

	private ErrorDocumentDao errorDao = null;
	private T2ReferenceGenerator t2ReferenceGenerator = null;

	/**
	 * Inject the error document data access object.
	 */
	public void setErrorDao(ErrorDocumentDao dao) {
		this.errorDao = dao;
	}

	/**
	 * Inject the T2Reference generator used to allocate new IDs when
	 * registering ErrorDocuments
	 */
	public void setT2ReferenceGenerator(T2ReferenceGenerator t2rg) {
		this.t2ReferenceGenerator = t2rg;
	}

	/**
	 * Check that the list dao is configured
	 * 
	 * @throws ListServiceException
	 *             if the dao is still null
	 */
	private void checkDao() throws ErrorDocumentServiceException {
		if (errorDao == null) {
			throw new ErrorDocumentServiceException(
					"ErrorDocumentDao not initialized, error document "
							+ "service operations are not available");
		}
	}

	/**
	 * Check that the t2reference generator is configured
	 * 
	 * @throws ListServiceException
	 *             if the generator is still null
	 */
	private void checkGenerator() throws ErrorDocumentServiceException {
		if (t2ReferenceGenerator == null) {
			throw new ErrorDocumentServiceException(
					"T2ReferenceGenerator not initialized, error document "
							+ "service operations not available");
		}
	}

	/**
	 * Schedule a runnable for execution - current naive implementation uses a
	 * new thread and executes immediately, but this is where any thread pool
	 * logic would go if we wanted to add that.
	 * 
	 * @param r
	 */
	private void executeRunnable(Runnable r) {
		new Thread(r).start();
	}

	public ErrorDocument getError(T2Reference id)
			throws ErrorDocumentServiceException {
		checkDao();
		try {
			return errorDao.get(id);
		} catch (Throwable t) {
			throw new ErrorDocumentServiceException(t);
		}
	}

	public void getErrorAsynch(final T2Reference id,
			final ErrorDocumentServiceCallback callback)
			throws ErrorDocumentServiceException {
		checkDao();
		Runnable r = new Runnable() {
			public void run() {
				try {
					ErrorDocument e = errorDao.get(id);
					callback.errorRetrieved(e);
				} catch (DaoException de) {
					callback
							.errorRetrievalFailed(new ErrorDocumentServiceException(
									de));
				}
			}
		};
		executeRunnable(r);

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

	public ErrorDocument registerError(String message, int depth)
			throws ErrorDocumentServiceException {
		return registerError(message, null, depth);
	}

	public ErrorDocument registerError(Throwable t, int depth)
			throws ErrorDocumentServiceException {
		return registerError("", t, depth);
	}

}
