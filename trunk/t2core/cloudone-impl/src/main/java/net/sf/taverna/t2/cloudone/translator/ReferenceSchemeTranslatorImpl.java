package net.sf.taverna.t2.cloudone.translator;

import java.util.List;

import net.sf.taverna.t2.cloudone.DataManager;
import net.sf.taverna.t2.cloudone.DereferenceException;
import net.sf.taverna.t2.cloudone.ReferenceScheme;
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

	/**
	 * Construct using a {@link DataManager} to do any Entity resolution.
	 * 
	 * @param dataManager
	 *            {@link DataManager} for resolving entities
	 */
	public ReferenceSchemeTranslatorImpl(DataManager dataManager) {
		this.dataManager = dataManager;
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
	 * @param preferredTypes
	 *            One or more desired {@link ReferenceScheme} classes in
	 *            preferred order (var args).
	 * @return A {@link AsynchRefScheme} that must be {@link Runnable#run()} to
	 *         do the translation.
	 */
	public AsynchRefScheme translateAsynch(final DataDocumentIdentifier id,
			final Class<? extends ReferenceScheme>... preferredTypes) {
		return new AsynchTranslate(id, preferredTypes);
	}

	/**
	 * Perform the translation specified by
	 * {@link #translateAsynch(DataDocumentIdentifier, Class...)} and called
	 * from {@link AsynchTranslate#execute()}.
	 * 
	 * @param id
	 * @param preferredTypes
	 * @return
	 * @throws RetrievalException
	 * @throws NotFoundException
	 * @throws TranslatorException
	 */
	@SuppressWarnings("unchecked")
	protected ReferenceScheme translate(DataDocumentIdentifier id,
			Class<? extends ReferenceScheme>... preferredTypes)
			throws RetrievalException, NotFoundException, TranslatorException {
		DataDocument dataDoc = (DataDocument) dataManager.getEntity(id);
		// match ref scheme directly
		for (Class<? extends ReferenceScheme> type : preferredTypes) {
			for (ReferenceScheme ref : dataDoc.getReferenceSchemes()) {
				if (type.isInstance(ref)) {
					return ref;
				}
			}
		}
		// otherwise try to translate directly
		for (Class<? extends ReferenceScheme> type : preferredTypes) {
			for (ReferenceScheme ref : dataDoc.getReferenceSchemes()) {
				List<? extends Translator> translators = translatorReg
						.getTranslators(ref, type);
				for (Translator translator : translators) {
					try {
						return translator.translate(dataManager, ref);
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

	class AsynchTranslate extends AbstractAsynchRunnable<ReferenceScheme>
			implements AsynchRefScheme {

		protected final DataDocumentIdentifier id;
		protected final Class<? extends ReferenceScheme>[] preferredTypes;

		AsynchTranslate(DataDocumentIdentifier id,
				Class<? extends ReferenceScheme>[] preferredTypes) {
			this.id = id;
			this.preferredTypes = preferredTypes;
		}

		protected ReferenceScheme execute() throws RetrievalException,
				NotFoundException, TranslatorException {
			ReferenceScheme refScheme = translate(id, preferredTypes);
			return refScheme;
		}
	}

}
