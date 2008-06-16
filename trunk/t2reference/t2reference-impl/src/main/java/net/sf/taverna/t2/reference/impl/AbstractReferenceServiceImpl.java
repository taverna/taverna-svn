package net.sf.taverna.t2.reference.impl;

import java.util.Set;

import net.sf.taverna.raven.spi.InstanceRegistry;
import net.sf.taverna.t2.reference.ErrorDocumentService;
import net.sf.taverna.t2.reference.ExternalReferenceSPI;
import net.sf.taverna.t2.reference.ListService;
import net.sf.taverna.t2.reference.ReferenceContext;
import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.reference.ReferenceServiceException;
import net.sf.taverna.t2.reference.ReferenceServiceResolutionCallback;
import net.sf.taverna.t2.reference.ReferenceSetService;
import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.reference.ValueToReferenceConverterSPI;

/**
 * Implementation of ReferenceService, inject with ReferenceSetService,
 * ErrorDocumentService and ListService to enable. Inject with an instance
 * registry of ValueToReferenceConvertorSPI to enable on the fly registration of
 * otherwise illegal object types. This class contains the basic injection
 * functionality and the getters for the sub-services, mostly to isolate these
 * mundane bits of code from the more interesting actual implementation of the
 * reference service logic.
 * 
 * @author Tom Oinn
 * 
 */
public abstract class AbstractReferenceServiceImpl extends AbstractServiceImpl
		implements ReferenceService {

	protected ErrorDocumentService errorDocumentService = null;
	protected ReferenceSetService referenceSetService = null;
	protected ListService listService = null;
	protected InstanceRegistry<ValueToReferenceConverterSPI> converterRegistry = null;

	/**
	 * Inject value to reference convertor SPI
	 */
	public final void setConverterRegistry(
			InstanceRegistry<ValueToReferenceConverterSPI> reg) {
		this.converterRegistry = reg;
	}

	/**
	 * Inject error document service
	 */
	public final void setErrorDocumentService(ErrorDocumentService eds) {
		this.errorDocumentService = eds;
	}

	/**
	 * Inject reference set service
	 */
	public final void setReferenceSetService(ReferenceSetService rss) {
		this.referenceSetService = rss;
	}

	/**
	 * Inject list service
	 */
	public final void setListService(ListService ls) {
		this.listService = ls;
	}

	/**
	 * Throw a ReferenceServiceException if methods in ReferenceService are
	 * called without the necessary sub-services configured.
	 */
	protected final void checkServices() throws ReferenceServiceException {
		if (errorDocumentService == null) {
			throw new ReferenceServiceException(
					"Reference service must be configued with an "
							+ "instance of ErrorDocumentService to function");
		}
		if (referenceSetService == null) {
			throw new ReferenceServiceException(
					"Reference service must be configued with an "
							+ "instance of ReferenceSetService to function");
		}
		if (listService == null) {
			throw new ReferenceServiceException(
					"Reference service must be configued with an "
							+ "instance of ListService to function");
		}
	}

	/**
	 * Check whether the converter registry has been defined, throw a
	 * ReferenceServiceException if not
	 */
	protected final void checkConverterRegistry()
			throws ReferenceServiceException {
		if (converterRegistry == null) {
			throw new ReferenceServiceException(
					"Reference service must be configued with an "
							+ "instance registry of ValueToReferenceConvertorSPI "
							+ "to enable on the fly mapping of arbitrary objects "
							+ "during compound registration");
		}
	}

	public final ErrorDocumentService getErrorDocumentService() {
		checkServices();
		return this.errorDocumentService;
	}

	public final ListService getListService() {
		checkServices();
		return this.listService;
	}

	public final ReferenceSetService getReferenceSetService() {
		checkServices();
		return this.referenceSetService;
	}

	/**
	 * Wraps the synchronous form, using the executeRunnable method to schedule
	 * it.
	 */
	public void resolveIdentifierAsynch(final T2Reference id,
			final Set<Class<ExternalReferenceSPI>> ensureTypes,
			final ReferenceContext context,
			final ReferenceServiceResolutionCallback callback)
			throws ReferenceServiceException {
		checkServices();
		Runnable r = new Runnable() {
			public void run() {
				try {
					callback.identifierResolved(resolveIdentifier(id,
							ensureTypes, context));
				} catch (ReferenceServiceException rse) {
					callback.resolutionFailed(rse);
				}
			}
		};
		executeRunnable(r);
	}

}
