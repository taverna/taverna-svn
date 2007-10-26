package net.sf.taverna.t2.cloudone.translator;

import java.util.List;

import net.sf.taverna.t2.cloudone.DataManager;
import net.sf.taverna.t2.cloudone.DataPeer;
import net.sf.taverna.t2.cloudone.DereferenceException;
import net.sf.taverna.t2.cloudone.ReferenceScheme;
import net.sf.taverna.t2.cloudone.TranslationPreference;
import net.sf.taverna.t2.cloudone.datamanager.NotFoundException;
import net.sf.taverna.t2.cloudone.datamanager.RetrievalException;
import net.sf.taverna.t2.cloudone.entity.DataDocument;
import net.sf.taverna.t2.cloudone.identifier.DataDocumentIdentifier;
import net.sf.taverna.t2.cloudone.util.AbstractAsynchRunnable;
import net.sf.taverna.t2.cloudone.util.AsynchRunnable;

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

	private static TranslatorRegistry translatorReg = TranslatorRegistry
			.getInstance();
	private DataManager dataManager;
	private DataPeer dataPeer;

	/**
	 * Construct using a {@link DataManager} to do any Entity resolution.
	 * 
	 * @param dataManager
	 *            {@link DataManager} for resolving entities
	 */
	public ReferenceSchemeTranslatorImpl(DataPeer dataPeer) {
		this.dataPeer = dataPeer;
		this.dataManager = dataPeer.getDataManager();
	}

	/**
	 * Translate {@link DataDocumentIdentifier} to one of the specified
	 * preferred {@link ReferenceScheme} types. If the referenced
	 * {@link DataDocument} already contains one of the desired reference
	 * schemes, that reference scheme will be returned. If not, translation will
	 * be attempted in the order of <code>preferredTypes</code>.
	 * <p>
	 * Note that this method is designed for asynchronous execution, and return
	 * an {@link AsynchRefScheme} instance which {@link Runnable#run()} method
	 * will do the actual execution. The result is available in
	 * {@link AsynchRunnable#getResult()}, or
	 * {@link AsynchRunnable#getException()} if the execution failed.
	 * 
	 * @see AsynchRefScheme
	 * @param id
	 *            {@link DataDocumentIdentifier} to be translated
	 * @param preferences
	 *            One or more desired {@link ReferenceScheme} classes in
	 *            preferred order (var args).
	 * @return A {@link AsynchRefScheme} that must be {@link Runnable#run()} to
	 *         do the translation.
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
	 * @param preferences
	 * @return
	 * @throws RetrievalException
	 * @throws NotFoundException
	 * @throws TranslatorException
	 */
	@SuppressWarnings("unchecked")
	protected ReferenceScheme translate(DataDocumentIdentifier id,
			List<TranslationPreference> preferences)
			throws RetrievalException, NotFoundException, TranslatorException {
		DataDocument dataDoc = (DataDocument) dataManager.getEntity(id);
		// match ref scheme directly
		for (TranslationPreference preference : preferences) {
			Class<? extends ReferenceScheme> type = preference.getReferenceSchemeClass();
			for (ReferenceScheme ref : dataDoc.getReferenceSchemes()) {
				if (! type.isInstance(ref)) {
					continue;
				}
				if (! ref.validInContext(preference.getContexts(), dataPeer)) {
					continue;
				}
				return ref;
			}
		}
		// otherwise try to translate directly
		for (TranslationPreference preference : preferences) {
			for (ReferenceScheme ref : dataDoc.getReferenceSchemes()) {
				List<Translator<ReferenceScheme>>  translators = translatorReg
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

		@SuppressWarnings("unchecked")
		protected ReferenceScheme execute() throws RetrievalException,
				NotFoundException, TranslatorException {
			ReferenceScheme refScheme = translate(id, preferences);
			return refScheme;
		}
	}

}
