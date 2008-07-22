package net.sf.taverna.t2.cloudone.translator;

import java.util.List;

import net.sf.taverna.t2.cloudone.datamanager.DataManager;
import net.sf.taverna.t2.cloudone.datamanager.NotFoundException;
import net.sf.taverna.t2.cloudone.datamanager.RetrievalException;
import net.sf.taverna.t2.cloudone.entity.DataDocument;
import net.sf.taverna.t2.cloudone.identifier.DataDocumentIdentifier;
import net.sf.taverna.t2.cloudone.peer.DataPeer;
import net.sf.taverna.t2.cloudone.refscheme.DereferenceException;
import net.sf.taverna.t2.cloudone.refscheme.ReferenceScheme;
import net.sf.taverna.t2.cloudone.util.AbstractAsynchRunnable;

/**
 * Translate from one {@link ReferenceScheme} to another. Uses a
 * {@link AsynchRefScheme} which is {@link Runnable} to execute the translation
 * in an Asynchronous manner.
 * 
 * @author Stian Soiland
 * @author Ian Dunlop
 * 
 */
public class ReferenceSchemeTranslatorImpl implements ReferenceSchemeTranslator {

	private TranslatorRegistry translatorReg = TranslatorRegistry.getInstance();
	private DataManager dataManager;
	private DataPeer dataPeer;

	/**
	 * Construct using a {@link DataPeer} to do any Entity resolution.
	 * 
	 * @param dataManager
	 *            {@link DataPeer} for resolving entities
	 */
	public ReferenceSchemeTranslatorImpl(DataPeer dataPeer) {
		this.dataPeer = dataPeer;
		this.dataManager = dataPeer.getDataManager();
	}

	/**
	 * {@inheritDoc}
	 */
	public AsynchRefScheme translateAsynch(final DataDocumentIdentifier id,
			List<TranslationPreference> preferences) {
		return new AsynchTranslate(id, preferences);
	}

	/**
	 * Perform the translation specified by
	 * {@link #translateAsynch(DataDocumentIdentifier, Class...)} and called
	 * from {@link AsynchTranslate#execute()}.
	 * 
	 * @param id
	 *            {@link DataDocumentIdentifier} to be translated
	 * @param preferences
	 *            One or more {@link TranslationPreference}s describing the
	 *            preferred {@link ReferenceScheme} (and it's valid contexts).
	 * @return The translated ReferenceScheme
	 * @throws RetrievalException
	 *             If the referenced {@link DataDocument} or all of it's
	 *             references could not be retrieved
	 * @throws NotFoundException
	 *             If the {@link DataDocumentIdentifier} could not be found
	 * @throws TranslatorException
	 *             If no translations could be performed.
	 */
	@SuppressWarnings("unchecked")
	protected ReferenceScheme translate(DataDocumentIdentifier id,
			List<TranslationPreference> preferences) throws RetrievalException,
			NotFoundException, TranslatorException {
		DataDocument dataDoc = (DataDocument) dataManager.getEntity(id);
		// match ref scheme directly
		for (TranslationPreference preference : preferences) {
			Class<? extends ReferenceScheme> type = preference
					.getReferenceSchemeClass();
			for (ReferenceScheme ref : dataDoc.getReferenceSchemes()) {
				if (!type.isInstance(ref)) {
					continue;
				}
				if (!ref.validInContext(preference.getContexts(), dataPeer)) {
					continue;
				}
				return ref;
			}
		}
		// otherwise try to translate directly
		for (TranslationPreference preference : preferences) {
			for (ReferenceScheme ref : dataDoc.getReferenceSchemes()) {
				List<Translator<ReferenceScheme>> translators = translatorReg
						.getTranslators(dataPeer, ref, preference);
				for (Translator translator : translators) {
					try {
						return translator.translate(dataPeer, ref, preference);
					} catch (RuntimeException ex) {
						// Trying next translator
						continue;
					} catch (DereferenceException e) {
						// Trying next translator
						continue;
					} catch (TranslatorException e) {
						// Trying next translator
						continue;
					}
				}
			}
		}
		// TODO: Shortest path algorithm, etc.
		throw new TranslatorException("Could not translate " + id);
	}

	/**
	 * AbstractAsynchRunnable that calls
	 * {@link ReferenceSchemeTranslatorImpl#translate(DataDocumentIdentifier, List)}
	 * to perform the actual translation asynchronously. Returned by
	 * {@link ReferenceSchemeTranslatorImpl#translateAsynch(DataDocumentIdentifier, List)}.
	 */
	@SuppressWarnings("unchecked")
	class AsynchTranslate extends AbstractAsynchRunnable<ReferenceScheme>
			implements AsynchRefScheme {

		protected final DataDocumentIdentifier id;
		protected final List<TranslationPreference> preferences;

		AsynchTranslate(DataDocumentIdentifier id,
				List<TranslationPreference> preferences) {
			this.id = id;
			this.preferences = preferences;
		}

		@Override
		@SuppressWarnings("unchecked")
		protected ReferenceScheme execute() throws RetrievalException,
				NotFoundException, TranslatorException {
			ReferenceScheme refScheme = translate(id, preferences);
			return refScheme;
		}
	}

}
