package net.sf.taverna.t2.reference.impl;

import java.util.List;

import net.sf.taverna.t2.reference.IdentifiedList;
import net.sf.taverna.t2.reference.ListDao;
import net.sf.taverna.t2.reference.ListService;
import net.sf.taverna.t2.reference.ListServiceCallback;
import net.sf.taverna.t2.reference.ListServiceException;
import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.reference.T2ReferenceGenerator;

/**
 * Implementation of ListService, inject with an appropriate ListDao and
 * T2ReferenceGenerator to enable.
 * 
 * @author Tom Oinn
 * 
 */
public class ListServiceImpl implements ListService {

	private ListDao listDao = null;
	private T2ReferenceGenerator t2ReferenceGenerator = null;

	/**
	 * Inject the list data access object.
	 */
	public void setListDao(ListDao dao) {
		this.listDao = dao;
	}

	/**
	 * Inject the T2Reference generator used to allocate new IDs when
	 * registering lists of T2Reference
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
	private void checkDao() throws ListServiceException {
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
	private void checkGenerator() throws ListServiceException {
		if (t2ReferenceGenerator == null) {
			throw new ListServiceException(
					"T2ReferenceGenerator not initialized, list "
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

	public IdentifiedList<T2Reference> getList(T2Reference id)
			throws ListServiceException {
		checkDao();
		try {
			return listDao.get(id);
		} catch (Throwable t) {
			throw new ListServiceException(t);
		}
	}

	public void getListAsynch(final T2Reference id,
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

	public IdentifiedList<T2Reference> registerEmptyList(int depth)
			throws ListServiceException {
		checkDao();
		checkGenerator();
		try {
			T2ReferenceImpl newReference = (T2ReferenceImpl) t2ReferenceGenerator
					.nextListReference(false, depth);
			T2ReferenceListImpl newList = new T2ReferenceListImpl();
			newList.setTypedId(newReference);
			listDao.store(newList);
			return newList;
		} catch (Throwable t) {
			throw new ListServiceException(t);
		}
	}

	public IdentifiedList<T2Reference> registerList(List<T2Reference> items)
			throws ListServiceException {
		checkDao();
		checkGenerator();
		if (items.isEmpty()) {
			throw new ListServiceException(
					"Can't register an empty list with this method,"
							+ " use the registerEmptyList instead");
		}
		// Track whether there are any items in the collection which are or
		// contain error documents.
		boolean containsErrors = false;
		// Track depth, ensure that all items have the same depth, fail if not.
		int depth = items.get(0).getDepth();
		T2ReferenceListImpl newList = new T2ReferenceListImpl();
		int counter = 0;
		for (T2Reference ref : items) {
			if (ref.getDepth() != depth) {
				throw new ListServiceException(
						"Mismatched depths in list registration; reference at index '"
								+ counter + "' has depth " + ref.getDepth()
								+ " but all preceeding items have depth "
								+ depth);
			}
			if (ref.containsErrors()) {
				// The collection's reference contains errors if any child does
				containsErrors = true;
			}
			newList.add(ref);
			counter++;
		}
		try {
			T2ReferenceImpl newReference = (T2ReferenceImpl) t2ReferenceGenerator
					.nextListReference(containsErrors, depth);
			newList.setTypedId(newReference);
			listDao.store(newList);
			return newList;
		} catch (Throwable t) {
			throw new ListServiceException(t);
		}
	}

}
