package net.sf.taverna.t2.cloudone.translator;

import net.sf.taverna.t2.cloudone.peer.DataPeer;
import net.sf.taverna.t2.cloudone.peer.LocationalContext;
import net.sf.taverna.t2.cloudone.refscheme.DereferenceException;
import net.sf.taverna.t2.cloudone.refscheme.ReferenceScheme;

/**
 * Allows translation from one {@link ReferenceScheme} to another based on the
 * preferences provided by the user
 * 
 * @author Ian Dunlop
 * @author Stian Soiland
 * 
 * @param <TranslatedReferenceScheme>
 */
@SuppressWarnings("unchecked")
public interface Translator<TranslatedReferenceScheme extends ReferenceScheme> {
	/**
	 * 
	 * @param dataPeer -
	 *            the peer where the translation is taking place
	 * @param ref
	 *            the original {@link ReferenceScheme}
	 * @param preference
	 *            the {@link ReferenceScheme} required back, together with the
	 *            {@link LocationalContext} it should be valid in and the
	 *            maximum cost acceptable
	 * @return the translateds {@link ReferenceScheme}
	 * @throws DereferenceException
	 * @throws TranslatorException
	 */
	public TranslatedReferenceScheme translate(DataPeer dataPeer,
			ReferenceScheme ref, TranslationPreference preference)
			throws DereferenceException, TranslatorException;

	/**
	 * Check whether the given {@link ReferenceScheme} can be translated to the
	 * TranslatedReferenceScheme
	 * 
	 * @return true if <code>toType</code> is TranslatedReferenceScheme
	 */
	public boolean canTranslate(DataPeer dataPeer, ReferenceScheme fromScheme,
			TranslationPreference preference);

}
