package net.sf.taverna.t2.reference.impl;

import java.util.HashSet;
import java.util.Set;

import net.sf.taverna.t2.reference.DaoException;
import net.sf.taverna.t2.reference.ExternalReferenceSPI;
import net.sf.taverna.t2.reference.ReferenceContext;
import net.sf.taverna.t2.reference.ReferenceSet;
import net.sf.taverna.t2.reference.ReferenceSetAugmentationException;
import net.sf.taverna.t2.reference.ReferenceSetService;
import net.sf.taverna.t2.reference.ReferenceSetServiceException;
import net.sf.taverna.t2.reference.T2Reference;

/**
 * Implementation of ReferenceSetService, inject with an appropriate
 * ReferenceSetDao to enable. Implements translation functionality as long as an
 * appropriate ReferenceSetAugmentor implementation is injected.
 * 
 * @author Tom Oinn
 * 
 */
public class ReferenceSetServiceImpl extends AbstractReferenceSetServiceImpl
		implements ReferenceSetService {

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
	public ReferenceSet getReferenceSetWithAugmentation(T2Reference id,
			Set<Class<ExternalReferenceSPI>> ensureTypes,
			ReferenceContext context) throws ReferenceSetServiceException {
		checkDao();
		checkAugmentor();
		// Obtain the reference set
		
		try {
			// Synchronize on the reference set, should ensure that we don't
			// have multiple concurrent translations assuming that Hibernate
			// retrieves the same entity each time. To work around this
			// potentially not being the case we can synchronize on the
			// stringified form of the identifier.
			synchronized (id.toString()) {
				ReferenceSet rs = getReferenceSet(id);
				Set<ExternalReferenceSPI> newReferences = referenceSetAugmentor
						.augmentReferenceSet(rs, ensureTypes, context);
				if (newReferences.isEmpty() == false) {
					// Write back changes to the store if we got here, this can
					// potentially throw an unsupported operation exception in
					// which
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
			}
			
		} catch (ReferenceSetAugmentationException rsae) {
			throw new ReferenceSetServiceException(rsae);
		}
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
