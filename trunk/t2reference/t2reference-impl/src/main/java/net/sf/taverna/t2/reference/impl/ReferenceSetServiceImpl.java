package net.sf.taverna.t2.reference.impl;

import java.util.HashSet;
import java.util.Set;

import net.sf.taverna.t2.reference.DaoException;
import net.sf.taverna.t2.reference.ExternalReferenceSPI;
import net.sf.taverna.t2.reference.ReferenceContext;
import net.sf.taverna.t2.reference.ReferenceSet;
import net.sf.taverna.t2.reference.ReferenceSetAugmentationException;
import net.sf.taverna.t2.reference.ReferenceSetAugmentor;
import net.sf.taverna.t2.reference.ReferenceSetDao;
import net.sf.taverna.t2.reference.ReferenceSetService;
import net.sf.taverna.t2.reference.ReferenceSetServiceCallback;
import net.sf.taverna.t2.reference.ReferenceSetServiceException;
import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.reference.T2ReferenceGenerator;

/**
 * Implementation of ReferenceSetService, inject with an appropriate
 * ReferenceSetDao to enable. Implements translation functionality as long as an
 * appropriate ReferenceSetAugmentor implementation is injected.
 * 
 * @author Tom Oinn
 * 
 */
public class ReferenceSetServiceImpl implements ReferenceSetService {

	private ReferenceSetDao referenceSetDao = null;
	private T2ReferenceGenerator t2ReferenceGenerator = null;
	private ReferenceSetAugmentor referenceSetAugmentor = null;

	/**
	 * Inject the reference set data access object.
	 */
	public void setReferenceSetDao(ReferenceSetDao dao) {
		this.referenceSetDao = dao;
	}

	/**
	 * Inject the T2Reference generator used to allocate new IDs when
	 * registering sets of ExternalReferenceSPI
	 */
	public void setT2ReferenceGenerator(T2ReferenceGenerator t2rg) {
		this.t2ReferenceGenerator = t2rg;
	}

	/**
	 * Inject the ReferenceSetAugmentor used to translate or construct new
	 * ExternalReferenceSPI instances within a ReferenceSet
	 */
	public void setReferenceSetAugmentor(ReferenceSetAugmentor rse) {
		this.referenceSetAugmentor = rse;
	}

	/**
	 * Check that the reference set dao is configured
	 * 
	 * @throws ReferenceSetServiceException
	 *             if the dao is still null
	 */
	private void checkDao() throws ReferenceSetServiceException {
		if (referenceSetDao == null) {
			throw new ReferenceSetServiceException(
					"ReferenceSetDao not initialized, reference set "
							+ "service operations are not available");
		}
	}

	/**
	 * Check that the t2reference generator is configured
	 * 
	 * @throws ReferenceSetServiceException
	 *             if the generator is still null
	 */
	private void checkGenerator() throws ReferenceSetServiceException {
		if (t2ReferenceGenerator == null) {
			throw new ReferenceSetServiceException(
					"T2ReferenceGenerator not initialized, reference "
							+ "set service operations not available");
		}
	}

	/**
	 * Check that the reference set augmentor is configured
	 * 
	 * @throws ReferenceSetServiceException
	 *             if the reference set augmentor is still null
	 */
	private void checkAugmentor() throws ReferenceSetServiceException {
		if (referenceSetAugmentor == null) {
			throw new ReferenceSetServiceException(
					"ReferenceSetAugmentor not initialized, reference "
							+ "set service operations not available");
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

	/**
	 * {@inheritDoc}
	 */
	public ReferenceSet getReferenceSet(T2Reference id)
			throws ReferenceSetServiceException {
		checkDao();
		try {
			return referenceSetDao.get(id);
		} catch (DaoException de) {
			throw new ReferenceSetServiceException(de);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void getReferenceSetAsynch(final T2Reference id,
			final ReferenceSetServiceCallback callback)
			throws ReferenceSetServiceException {
		checkDao();
		Runnable r = new Runnable() {
			public void run() {
				try {
					ReferenceSet rs = referenceSetDao.get(id);
					callback.referenceSetRetrieved(rs);
				} catch (DaoException de) {
					callback
							.referenceSetRetrievalFailed(new ReferenceSetServiceException(
									de));
				}
			}
		};
		executeRunnable(r);
	}

	/**
	 * {@inheritDoc}
	 */
	public ReferenceSet getReferenceSetWithAugmentation(T2Reference id,
			Set<Class<ExternalReferenceSPI>> ensureTypes,
			ReferenceContext context) throws ReferenceSetServiceException {
		checkDao();
		checkAugmentor();
		// Obtain the reference set
		ReferenceSet rs = getReferenceSet(id);
		try {
			Set<ExternalReferenceSPI> newReferences = referenceSetAugmentor
					.augmentReferenceSet(rs, ensureTypes, context);
			if (newReferences.isEmpty() == false) {
				// Write back changes to the store if we got here, this can
				// potentially throw an unsupported operation exception in which
				// case we have to fail the augmentation.
				try {
					rs.getExternalReferences().addAll(newReferences);
				} catch (RuntimeException re) {
					throw new ReferenceSetAugmentationException(
							"Can't add new references back into existing reference set instance");
				}
				referenceSetDao.update(rs);
			}
			return rs;
		} catch (ReferenceSetAugmentationException rsae) {
			throw new ReferenceSetServiceException(rsae);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void getReferenceSetWithAugmentationAsynch(final T2Reference id,
			final Set<Class<ExternalReferenceSPI>> ensureTypes,
			final ReferenceContext context,
			final ReferenceSetServiceCallback callback)
			throws ReferenceSetServiceException {
		checkDao();
		checkAugmentor();
		Runnable r = new Runnable() {
			public void run() {
				try {
					callback
							.referenceSetRetrieved(getReferenceSetWithAugmentation(
									id, ensureTypes, context));

				} catch (ReferenceSetServiceException rsse) {
					callback.referenceSetRetrievalFailed(rsse);
				}
			}
		};
		executeRunnable(r);
	}

	/**
	 * {@inheritDoc}
	 */
	public ReferenceSet registerReferenceSet(
			Set<ExternalReferenceSPI> references)
			throws ReferenceSetServiceException {
		checkDao();
		checkGenerator();
		ReferenceSetImpl rsi = new ReferenceSetImpl();
		rsi
				.setExternalReferences(new HashSet<ExternalReferenceSPI>(
						references));
		try {
			T2ReferenceImpl id = (T2ReferenceImpl) t2ReferenceGenerator
					.nextReferenceSetReference();
			rsi.setTypedId(id);
		} catch (ClassCastException cce) {
			throw new ReferenceSetServiceException(
					"ID supplied by generator must be an instance of "
							+ "ReferenceSetT2ReferenceImpl", cce);
		}
		try {
			referenceSetDao.store(rsi);
			return rsi;
		} catch (DaoException de) {
			throw new ReferenceSetServiceException(de);
		}
	}
}
