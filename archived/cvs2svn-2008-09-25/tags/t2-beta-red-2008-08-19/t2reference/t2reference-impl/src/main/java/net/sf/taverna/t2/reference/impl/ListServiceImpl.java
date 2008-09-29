package net.sf.taverna.t2.reference.impl;

import java.util.List;

import net.sf.taverna.t2.reference.DaoException;
import net.sf.taverna.t2.reference.IdentifiedList;
import net.sf.taverna.t2.reference.ListService;
import net.sf.taverna.t2.reference.ListServiceException;
import net.sf.taverna.t2.reference.T2Reference;

/**
 * Implementation of ListService, inject with an appropriate ListDao and
 * T2ReferenceGenerator to enable.
 * 
 * @author Tom Oinn
 * 
 */
public class ListServiceImpl extends AbstractListServiceImpl implements
		ListService {

	public IdentifiedList<T2Reference> getList(T2Reference id)
			throws ListServiceException {
		checkDao();
		try {
			return listDao.get(id);
		} catch (DaoException de) {
			throw new ListServiceException(de);
		}
	}

	public IdentifiedList<T2Reference> registerEmptyList(int depth)
			throws ListServiceException {
		checkDao();
		checkGenerator();
		T2ReferenceImpl newReference = T2ReferenceImpl
				.getAsImpl(t2ReferenceGenerator.nextListReference(false, depth));
		T2ReferenceListImpl newList = new T2ReferenceListImpl();
		newList.setTypedId(newReference);
		try {
			listDao.store(newList);
			return newList;
		} catch (DaoException de) {
			throw new ListServiceException(de);
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
			T2ReferenceImpl newReference = T2ReferenceImpl
					.getAsImpl(t2ReferenceGenerator.nextListReference(
							containsErrors, depth + 1));
			newList.setTypedId(newReference);
			listDao.store(newList);
			return newList;
		} catch (Throwable t) {
			throw new ListServiceException(t);
		}
	}

}
