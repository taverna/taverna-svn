package net.sf.taverna.t2.reference.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import net.sf.taverna.t2.reference.ContextualizedT2Reference;
import net.sf.taverna.t2.reference.ErrorDocument;
import net.sf.taverna.t2.reference.ErrorDocumentServiceException;
import net.sf.taverna.t2.reference.ExternalReferenceSPI;
import net.sf.taverna.t2.reference.Identified;
import net.sf.taverna.t2.reference.IdentifiedList;
import net.sf.taverna.t2.reference.ListServiceException;
import net.sf.taverna.t2.reference.ReferenceContext;
import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.reference.ReferenceServiceException;
import net.sf.taverna.t2.reference.ReferenceSet;
import net.sf.taverna.t2.reference.ReferenceSetServiceException;
import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.reference.T2ReferenceType;
import net.sf.taverna.t2.reference.ValueToReferenceConversionException;
import net.sf.taverna.t2.reference.ValueToReferenceConverterSPI;

/**
 * Implementation of ReferenceService, inject with ReferenceSetService,
 * ErrorDocumentService and ListService to enable.
 * 
 * @author Tom Oinn
 */
public class ReferenceServiceImpl extends AbstractReferenceServiceImpl
		implements ReferenceService {

	/**
	 * The top level registration method is used to register either as yet
	 * unregistered ErrorDocuments and ReferenceSets (if these are passed in and
	 * already have an identifier this call does nothing) and arbitrarily nested
	 * Lists of the same. In addition any ExternalReferenceSPI instances found
	 * will be wrapped in a single item ReferenceSet and registered, and any
	 * Throwables will be wrapped in an ErrorDocument and registered. Lists will
	 * be converted to IdentifiedList&lt;T2Reference&gt; and registered if all
	 * children can be (or were already) appropriately named.
	 * <p>
	 * This method is only valid on parameters of the following type :
	 * <ol>
	 * <li>{@link ReferenceSet} - registered if not already registered,
	 * otherwise returns existing T2Reference</li>
	 * <li>{@link ErrorDocument} - same behaviour as ReferenceSet</li>
	 * <li>{@link ExternalReferenceSPI} - wrapped in ReferenceSet, registered
	 * and ID returned</li>
	 * <li>Throwable - wrapped in ErrorDocument with no message, registered and
	 * ID returned</li>
	 * <li>List - all children are first registered, if this succeeds the list
	 * is itself registered as an IdentifiedList of T2Reference and its
	 * reference returned.</li>
	 * </ol>
	 * The exception to this is if the useConvertorSPI parameter is set to true -
	 * in this case any objects which do not match the above allowed list will
	 * be run through any available ValueToReferenceConvertorSPI instances in
	 * turn until one succeeds or all fail, which may result in the creation of
	 * ExternalReferenceSPI instances. As these can be registered such objects
	 * will not cause an exception to be thrown.
	 * 
	 * @param o
	 *            the object to register with the reference system, must comply
	 *            with and will be interpreted as shown in the type list above.
	 * @param targetDepth
	 *            the depth of the top level object supplied. This is needed
	 *            when registering empty collections and error documents,
	 *            whether as top level types or as members of a collection
	 *            within the top level type. If registering a collection this is
	 *            the collection depth, so a List of ReferenceSchemeSPI would be
	 *            depth 1. Failing to specify this correctly will result in
	 *            serious problems downstream so be careful! We can't catch all
	 *            potential problems in this method (although some errors will
	 *            be trapped).
	 * @param useConverterSPI
	 *            whether to attempt to use the ValueToReferenceConvertorSPI
	 *            registry (if defined and available) to map arbitrary objects
	 *            to ExternalReferenceSPI instances on the fly. The registry of
	 *            converters is generally injected into the implementation of
	 *            this service.
	 * @param context
	 *            ReferenceContext to use if required by component services,
	 *            this is most likely to be used by the object to reference
	 *            converters if engaged.
	 * @return a T2Reference to the registered object
	 * @throws ReferenceServiceException
	 *             if the object type (or, for collections, the recursive type
	 *             of its contents) is not in the allowed list or if a problem
	 *             occurs during registration. Also thrown if attempting to use
	 *             the converter SPI without an attached registry.
	 */
	public T2Reference register(Object o, int targetDepth,
			boolean useConverterSPI, ReferenceContext context)
			throws ReferenceServiceException {
		checkServices();
		if (useConverterSPI) {
			checkConverterRegistry();
		}
		return getNameForObject(o, targetDepth, useConverterSPI, context);
	}

	private T2Reference getNameForObject(Object o, int currentDepth,
			boolean useConverterSPI, ReferenceContext context)
			throws ReferenceServiceException {
		// First check whether this is an Identified, and if so whether it
		// already has an ID. If this is the case then return it, we assume that
		// anything which has an identifier already allocated must have been
		// registered (this is implicit in the contract for the various
		// sub-services
		if (o instanceof Identified) {
			Identified i = (Identified) o;
			if (i.getId() != null) {
				return i.getId();
			}
		}
		// Next check lists.
		else if (o instanceof List) {
			List<?> l = (List<?>) o;
			// If the list is empty then register a new empty list of the
			// appropriate depth and return it
			if (l.isEmpty()) {
				try {
					IdentifiedList<T2Reference> newList = listService
							.registerEmptyList(currentDepth);
					return newList.getId();
				} catch (ListServiceException lse) {
					throw new ReferenceServiceException(lse);
				}
			}
			// Otherwise construct a new list of T2Reference and register it,
			// calling the getNameForObject method on all children of the list
			// to construct the list of references
			else {
				List<T2Reference> references = new ArrayList<T2Reference>();
				for (Object item : l) {
					// Recursively call this method with a depth one lower than
					// the current depth
					references.add(getNameForObject(item, currentDepth - 1,
							useConverterSPI, context));
				}
				try {
					IdentifiedList<T2Reference> newList = listService
							.registerList(references);
					return newList.getId();
				} catch (ListServiceException lse) {
					throw new ReferenceServiceException(lse);
				}
			}
		} else {
			// Neither a list nor an already identified object, first thing is
			// to engage the converters if enabled. Only engage if we don't
			// already have a Throwable or an ExternalReferenceSPI instance
			if (useConverterSPI && (o instanceof Throwable == false)
					&& (o instanceof ExternalReferenceSPI == false)) {
				for (ValueToReferenceConverterSPI converter : converterRegistry) {
					if (converter.canConvert(o, context)) {
						try {
							ExternalReferenceSPI ers = converter.convert(o,
									context);
							o = ers;
							break;
						} catch (ValueToReferenceConversionException vtrce) {
							// Fail, but that doesn't matter at the moment as
							// there may be more converters to try. TODO - log
							// this!
						}
					}
				}
			}
			// If the object is neither a Throwable nor an ExternalReferenceSPI
			// instance at this point we should fail the registration process,
			// this means either that the conversion process wasn't enabled or
			// that it failed to map the object type correctly.
			if ((o instanceof Throwable == false)
					&& (o instanceof ExternalReferenceSPI == false)) {
				throw new ReferenceServiceException(
						"Failed to register POJO, found a type '"
								+ o.getClass().getCanonicalName()
								+ "' which cannot be registered with the reference manager");
			}
			// Have either a Throwable or an ExternalReferenceSPI
			else {
				if (o instanceof Throwable) {
					// Wrap in an ErrorDocument and return the ID
					try {
						ErrorDocument doc = errorDocumentService.registerError(
								(Throwable) o, currentDepth);
						return doc.getId();
					} catch (ErrorDocumentServiceException edse) {
						throw new ReferenceServiceException(edse);
					}
				} else if (o instanceof ExternalReferenceSPI) {
					try {
						Set<ExternalReferenceSPI> references = new HashSet<ExternalReferenceSPI>();
						references.add((ExternalReferenceSPI) o);
						ReferenceSet rs = referenceSetService
								.registerReferenceSet(references);
						return rs.getId();
					} catch (ReferenceSetServiceException rsse) {
						throw new ReferenceServiceException(rsse);
					}
				}
			}
		}
		throw new ReferenceServiceException(
				"Should never see this, reference registration"
						+ " logic has fallen off the end of the"
						+ " world, check the code!");
	}

	/**
	 * Perform recursive identifier resolution, building a collection structure
	 * of Identified objects, any collection elements being IdentifiedLists of
	 * Identified subclasses. If the id has depth 0 this will just return the
	 * Identified to which that id refers.
	 * 
	 * @param id
	 *            the T2Reference to resolve
	 * @param ensureTypes
	 *            a set of ExternalReferenceSPI classes, this is used to augment
	 *            any resolved ReferenceSet instances to ensure that each one
	 *            has at least one of the specified types. If augmentation is
	 *            not required this can be set to null.
	 * @param context
	 *            the ReferenceContext to use to resolve this and any
	 *            recursively resolved identifiers
	 * @return fully resolved Identified subclass - this is either a (recursive)
	 *         IdentifiedList of Identified, a ReferenceSet or an ErrorDocument
	 * @throws ReferenceServiceException
	 *             if any problems occur during resolution
	 */
	public Identified resolveIdentifier(T2Reference id,
			Set<Class<ExternalReferenceSPI>> ensureTypes,
			ReferenceContext context) throws ReferenceServiceException {
		checkServices();

		if (id.getReferenceType().equals(T2ReferenceType.ReferenceSet)) {
			try {
				ReferenceSet rs;
				if (ensureTypes == null) {
					rs = referenceSetService.getReferenceSet(id);
				} else {
					rs = referenceSetService.getReferenceSetWithAugmentation(
							id, ensureTypes, context);
				}
				return rs;
			} catch (ReferenceSetServiceException rsse) {
				throw new ReferenceServiceException(rsse);
			}
		} else if (id.getReferenceType().equals(T2ReferenceType.ErrorDocument)) {
			try {
				ErrorDocument ed = errorDocumentService.getError(id);
				return ed;
			} catch (ErrorDocumentServiceException edse) {
				throw new ReferenceServiceException(edse);
			}
		} else if (id.getReferenceType().equals(T2ReferenceType.IdentifiedList)) {
			try {
				T2ReferenceImpl typedId;
				if (id instanceof T2ReferenceImpl) {
					typedId = (T2ReferenceImpl) id;
				} else {
					throw new ReferenceSetServiceException(
							"Supplied or nested T2Reference not an instance of T2ReferenceImpl");
				}
				IdentifiedList<T2Reference> idList = listService
						.getList(typedId);
				// Construct a new list, and populate with the result of
				// resolving each ID in turn
				IdentifiedArrayList<Identified> newList = new IdentifiedArrayList<Identified>();
				for (T2Reference item : idList) {
					newList.add(resolveIdentifier(item, ensureTypes, context));
				}
				newList.setTypedId(typedId);
				return newList;
			} catch (ListServiceException lse) {
				throw new ReferenceServiceException(lse);
			}
		} else {
			throw new ReferenceServiceException("Unsupported ID type : "
					+ id.getReferenceType());
		}
	}

	/**
	 * Initiates a traversal of the specified t2reference, traversing to
	 * whatever level of depth is required such that all identifiers returned
	 * within the iterator have the specified depth. The context (i.e. the index
	 * path from the originally specified reference to each reference within the
	 * iteration) is included through use of the ContextualizedT2Reference
	 * wrapper class
	 * 
	 * @param source
	 *            the T2Reference from which to traverse. In general this is the
	 *            root of a collection structure.
	 * @param desiredDepth
	 *            the desired depth of all returned T2References, must be less
	 *            than or equal to that of the source reference.
	 * @throws ReferenceServiceException
	 *             if unable to create the iterator for some reason. Note that
	 *             implementations are free to lazily perform the iteration so
	 *             this method may succeed but the iterator produced can fail
	 *             when used. If the iterator fails it will do so by throwing
	 *             one of the underlying sub-service exceptions.
	 */
	public Iterator<ContextualizedT2Reference> traverseFrom(T2Reference source,
			int desiredDepth) {
		checkServices();
		throw new ReferenceServiceException(new UnsupportedOperationException(
				"Not implemented yet!"));
	}

}
