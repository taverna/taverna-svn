package net.sf.taverna.t2.reference;

import java.util.Set;

/**
 * Provides facilities to register sets of ExternalReferenceSPI implementations
 * within the reference manager and to retrieve these sets by T2Reference either
 * as stored or with translation support. In general applications should be
 * using this interface (where only ReferenceSet functionality is required) or
 * the support classes which in turn use this and the collection and error
 * handling interfaces to present a unified view over the various components of
 * the reference management system.
 * 
 * @author Tom Oinn
 */
public interface ReferenceSetService {

	/**
	 * Register a set of {@link ExternalReferenceSPI} instances, all of which
	 * should point to byte equivalent data, and return the newly created
	 * {@link ReferenceSet}. This method blocks on the underlying store, but
	 * guarantees that the returned value has been persisted.
	 * 
	 * @param references
	 *            a set of {@link ExternalReferenceSPI} implementations to
	 *            register as a {@link ReferenceSet}
	 * @return the registered {@link ReferenceSet}
	 */
	public ReferenceSet registerReferenceSet(
			Set<ExternalReferenceSPI> references);

	/**
	 * Get a previously registered {@link ReferenceSet} by {@link T2Reference}.
	 * Note that this method blocks and may take some time to return in the case
	 * of distributed reference managers; if this is likely to be an issue then
	 * you should use the asynchronous form
	 * {@link #getReferenceSetAsynch(T2Reference, ReferenceSetServiceCallback) getReferenceSetAsynch}
	 * instead of this method.
	 * 
	 * @param id
	 *            a {@link T2Reference} identifying a {@link ReferenceSet} to
	 *            retrieve
	 * @return the requested {@link ReferenceSet}
	 */
	public ReferenceSet getReferenceSet(T2Reference id);

	/**
	 * Functionality the same as
	 * {@link #getReferenceSet(T2Reference) getReferenceSet} but in asynchronous
	 * mode, returning immediately and using the supplied callback to
	 * communicate its results.
	 * 
	 * @param id
	 *            a {@link T2Reference} identifying a {@link ReferenceSet} to
	 *            retrieve
	 * @param callback
	 *            a {@link ReferenceSetServiceCallback} used to convey the
	 *            results of the asynchronous call
	 */
	public void getReferenceSetAsynch(T2Reference id,
			ReferenceSetServiceCallback callback);

	/**
	 * Functionality the same as
	 * {@link #getReferenceSet(T2Reference) getReferenceSet} but with the
	 * additional option to specify a set of {@link ExternalReferenceSPI}
	 * classes. The reference set manager will attempt to ensure that the
	 * returned {@link ReferenceSet} contains an instance of at least one of the
	 * specified classes. This method blocks, and may potentially incur both the
	 * remote lookup overhead of the simpler version of this call and any
	 * translation logic. It is <em>strongly</em> recommended that you do not
	 * use this version of the call and instead use the asynchronous form
	 * {@link #getReferenceSetWithAugmentationAsynch(T2Reference, Set, ReferenceContext, ReferenceSetServiceCallback) getReferenceSetWithAugmentationAsynch}
	 * instead.
	 * <p>
	 * If the translation logic cannot provide at least one of the required
	 * types this call will fail, even if the {@link ReferenceSet} requested is
	 * otherwise available.
	 * 
	 * @param id
	 *            a {@link T2Reference} identifying a {@link ReferenceSet} to
	 *            retrieve
	 * @param ensureTypes
	 *            a set of {@link ExternalReferenceSPI} classes. The framework
	 *            will attempt to ensure there is an instance of at least one of
	 *            these classes in the returned {@link ReferenceSet}
	 * @param context
	 *            if translation of references is required the translation
	 *            infrastructure will need information in this
	 *            {@link ReferenceContext} parameter.
	 * @return the requested {@link ReferenceSet}
	 */
	public ReferenceSet getReferenceSetWithAugmentation(T2Reference id,
			Set<Class<? extends ExternalReferenceSPI>> ensureTypes,
			ReferenceContext context);

	/**
	 * Functionality as
	 * {@link #getReferenceSetWithAugmentation(T2Reference, Set, ReferenceContext) getReferenceSetWithAugmentation}
	 * but with the addition of a callback interface to report the result or
	 * failure of the method.
	 * 
	 * @param id
	 *            a {@link T2Reference} identifying a {@link ReferenceSet} to
	 *            retrieve
	 * @param ensureTypes
	 *            a set of {@link ExternalReferenceSPI} classes. The framework
	 *            will attempt to ensure there is an instance of at least one of
	 *            these classes in the returned {@link ReferenceSet}
	 * @param context
	 *            if translation of references is required the translation
	 *            infrastructure will need information in this
	 *            {@link ReferenceContext} parameter.
	 * @param callback
	 *            a {@link ReferenceSetServiceCallback} used to convey the
	 *            results of the asynchronous call
	 */
	public void getReferenceSetWithAugmentationAsynch(T2Reference id,
			Set<Class<? extends ExternalReferenceSPI>> ensureTypes,
			ReferenceContext context, ReferenceSetServiceCallback callback);

}
