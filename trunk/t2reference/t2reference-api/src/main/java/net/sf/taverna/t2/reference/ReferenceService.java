package net.sf.taverna.t2.reference;

import java.util.Iterator;
import java.util.Set;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Top level access point to the reference manager for client code which is
 * aware of references and error documents. Provides methods to store and
 * retrieve instances of ReferenceSet, IdentifiedList&lt;T2Reference&gt; and
 * ErrorDocument. Acts as an integration layer for the three sub-component
 * service, providing in addition collection traversal and retrieval of lists of
 * identified entities (ReferenceSet, IdentifiedList&lt;Identified&gt; and
 * ErrorDocument) from a T2Reference identifying a list.
 * <p>
 * Resolution of collections can happen at three different levels - the
 * ListService resolves the collection ID to a list of child IDs, and doesn't
 * traverse these children if they are themselves lists. This service instead
 * resolves to a fully realized collection of the entities which those IDs
 * reference, and does recursively apply this to child lists. The third level of
 * resolution would be to produce a POJO collection structure containing the
 * data values in place of ReferenceSet instances and failing appropriately on
 * ErrorDocuments - this third layer is part of the DataService. Client code
 * that has no need for explicit reference handling should use the DataService
 * for simplicity.
 * 
 * @author Tom Oinn
 * 
 */
public interface ReferenceService {

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
	@Transactional(propagation = Propagation.REQUIRED, readOnly = false)
	public Identified resolveIdentifier(T2Reference id,
			Set<Class<ExternalReferenceSPI>> ensureTypes,
			ReferenceContext context) throws ReferenceServiceException;

	/**
	 * As resolveIdentifier but using a callback object and returning
	 * immediately
	 * 
	 * @throws ReferenceServiceException
	 *             if anything goes wrong with the setup of the resolution job.
	 *             Any exceptions during the resolution process itself are
	 *             communicated through the callback object.
	 */
	@Transactional(propagation = Propagation.REQUIRED, readOnly = false)
	public void resolveIdentifierAsynch(T2Reference id,
			Set<Class<ExternalReferenceSPI>> ensureTypes,
			ReferenceContext context,
			ReferenceServiceResolutionCallback callback)
			throws ReferenceServiceException;

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
	@Transactional(propagation = Propagation.REQUIRED, readOnly = false)
	public T2Reference register(Object o, int targetDepth,
			boolean useConverterSPI, ReferenceContext context)
			throws ReferenceServiceException;

	/**
	 * Returns the {@link ErrorDocumentService} this ReferenceService uses, use
	 * this when you need functionality from that service explicitly.
	 */
	public ErrorDocumentService getErrorDocumentService();

	/**
	 * Returns the {@link ReferenceSetService} this ReferenceService uses, use
	 * this when you need functionality from that service explicitly.
	 */
	public ReferenceSetService getReferenceSetService();

	/**
	 * Returns the {@link ListService} this ReferenceService uses, use this when
	 * you need functionality from that service explicitly.
	 */
	public ListService getListService();

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
	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	public Iterator<ContextualizedT2Reference> traverseFrom(T2Reference source,
			int desiredDepth);

}
