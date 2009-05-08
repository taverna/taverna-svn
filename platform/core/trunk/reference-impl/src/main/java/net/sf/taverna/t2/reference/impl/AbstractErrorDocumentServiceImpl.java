/*******************************************************************************
 * Copyright (C) 2007 The University of Manchester   
 * 
 *  Modifications to the initial code base are copyright of their
 *  respective authors, or their employers as appropriate.
 * 
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2.1 of
 *  the License, or (at your option) any later version.
 *    
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *    
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 ******************************************************************************/
package net.sf.taverna.t2.reference.impl;

import net.sf.taverna.t2.reference.DaoException;
import net.sf.taverna.t2.reference.ErrorDocument;
import net.sf.taverna.t2.reference.ErrorDocumentDao;
import net.sf.taverna.t2.reference.ErrorDocumentService;
import net.sf.taverna.t2.reference.ErrorDocumentServiceCallback;
import net.sf.taverna.t2.reference.ErrorDocumentServiceException;
import net.sf.taverna.t2.reference.ListServiceException;
import net.sf.taverna.t2.reference.ReferenceContext;
import net.sf.taverna.t2.reference.T2Reference;

/**
 * Abstract implementation of ErrorDocumentService, inject with an appropriate
 * ErrorDocumentDao and T2ReferenceGenerator to enable. Contains injectors for
 * id generation and dao along with other bookkeeping, leaving the
 * implementation of the actual service logic to the subclass.
 * 
 * @author Tom Oinn
 */
public abstract class AbstractErrorDocumentServiceImpl extends
		AbstractServiceImpl implements ErrorDocumentService {

	protected ErrorDocumentDao errorDao = null;

	/**
	 * Inject the error document data access object.
	 */
	public final void setErrorDao(ErrorDocumentDao dao) {
		this.errorDao = dao;
	}

	/**
	 * Check that the list dao is configured
	 * 
	 * @throws ListServiceException
	 *             if the dao is still null
	 */
	protected final void checkDao() throws ErrorDocumentServiceException {
		if (errorDao == null) {
			throw new ErrorDocumentServiceException(
					"ErrorDocumentDao not initialized, error document "
							+ "service operations are not available");
		}
	}

	public final void getErrorAsynch(final T2Reference id,
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

	public final ErrorDocument registerError(String message, int depth,
			ReferenceContext context) throws ErrorDocumentServiceException {
		context = EmptyReferenceContext.checkContext(context);
		return registerError(message, (Throwable) null, depth, context);
	}

	public final ErrorDocument registerError(Throwable t, int depth,
			ReferenceContext context) throws ErrorDocumentServiceException {
		context = EmptyReferenceContext.checkContext(context);
		return registerError("", t, depth, context);
	}

}
