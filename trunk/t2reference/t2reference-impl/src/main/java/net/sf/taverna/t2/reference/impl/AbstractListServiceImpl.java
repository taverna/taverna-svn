package net.sf.taverna.t2.reference.impl;

import net.sf.taverna.t2.reference.ListDao;
import net.sf.taverna.t2.reference.ListService;
import net.sf.taverna.t2.reference.ListServiceCallback;
import net.sf.taverna.t2.reference.ListServiceException;
import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.reference.T2ReferenceGenerator;

/**
 * Abstract implementation of ListService, inject with an appropriate ListDao
 * and T2ReferenceGenerator to enable. Contains injectors for id generation and
 * dao along with other bookkeeping, leaving the implementation of the actual
 * service logic to the subclass.
 * 
 * @author Tom Oinn
 * 
 */
public abstract class AbstractListServiceImpl extends AbstractServiceImpl
		implements ListService {

	protected ListDao listDao = null;
	protected T2ReferenceGenerator t2ReferenceGenerator = null;

	/**
	 * Inject the list data access object.
	 */
	public final void setListDao(ListDao dao) {
		this.listDao = dao;
	}

	/**
	 * Inject the T2Reference generator used to allocate new IDs when
	 * registering lists of T2Reference
	 */
	public final void setT2ReferenceGenerator(T2ReferenceGenerator t2rg) {
		this.t2ReferenceGenerator = t2rg;
	}

	/**
	 * Check that the list dao is configured
	 * 
	 * @throws ListServiceException
	 *             if the dao is still null
	 */
	protected final void checkDao() throws ListServiceException {
		if (listDao == null) {
			throw new ListServiceException("ListDao not initialized, list "
					+ "service operations are not available");
		}
	}

	/**
	 * Check that the t2reference generator is configured
	 * 
	 * @throws ListServiceException
	 *             if the generator is still null
	 */
	protected final void checkGenerator() throws ListServiceException {
		if (t2ReferenceGenerator == null) {
			throw new ListServiceException(
					"T2ReferenceGenerator not initialized, list "
							+ "service operations not available");
		}
	}

	public final void getListAsynch(final T2Reference id,
			final ListServiceCallback callback) throws ListServiceException {
		checkDao();
		Runnable r = new Runnable() {
			public void run() {
				try {
					callback.listRetrieved(getList(id));
				} catch (ListServiceException lse) {
					callback.listRetrievalFailed(lse);
				}
			}
		};
		executeRunnable(r);
	}

}
