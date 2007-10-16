package net.sf.taverna.t2.cloudone.translator;

import java.util.List;

import net.sf.taverna.t2.cloudone.BlobStore;
import net.sf.taverna.t2.cloudone.DataManager;
import net.sf.taverna.t2.cloudone.DereferenceException;
import net.sf.taverna.t2.cloudone.ReferenceScheme;
import net.sf.taverna.t2.cloudone.datamanager.DataFacade;
import net.sf.taverna.t2.cloudone.datamanager.NotFoundException;
import net.sf.taverna.t2.cloudone.datamanager.RetrievalException;
import net.sf.taverna.t2.cloudone.entity.DataDocument;
import net.sf.taverna.t2.cloudone.identifier.DataDocumentIdentifier;

public class ReferenceSchemeTranslatorImpl implements ReferenceSchemeTranslator {

	private final class TranslatorRunnableImpl implements 
			TranslatorRunnable {
		private final Class<? extends ReferenceScheme>[] preferredTypes;
		private final DataDocumentIdentifier id;
		private ReferenceScheme referenceScheme = null;
		private boolean finished = false;
		private Exception exception = null;

		private TranslatorRunnableImpl(
				Class<? extends ReferenceScheme>[] preferredTypes,
				DataDocumentIdentifier id) {
			this.preferredTypes = preferredTypes;
			this.id = id;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see net.sf.taverna.t2.cloudone.translator.TranslatorRunnableInterface#run()
		 */
		public void run() {
			if (finished) {
				throw new IllegalStateException("Can't run twice");
			}
			try {
				ReferenceScheme refScheme = translate(id, preferredTypes);
				setReferenceScheme(refScheme);
			} catch (RetrievalException e) {
				exception = e;
			} catch (NotFoundException e) {
				exception = e;
			} catch (RuntimeException e) {
				exception = e;
			} finally {
				finished = true;
			}
		}

		private void setReferenceScheme(ReferenceScheme refScheme) {
			this.referenceScheme = refScheme;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see net.sf.taverna.t2.cloudone.translator.TranslatorRunnableInterface#getReferenceScheme()
		 */
		public ReferenceScheme getReferenceScheme() {
			if (!finished) {
				throw new IllegalStateException("Not yet finished");
			}
			if (exception != null) {
				throw new IllegalStateException("Invocation failed", exception);
			}
			return referenceScheme;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see net.sf.taverna.t2.cloudone.translator.TranslatorRunnableInterface#isFinished()
		 */
		public boolean isFinished() {
			return finished;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see net.sf.taverna.t2.cloudone.translator.TranslatorRunnableInterface#getException()
		 */
		public Exception getException() {
			if (!finished) {
				throw new IllegalStateException("Not yet finished");
			}
			return exception;
		}
	}

	private DataManager dataManager;
	private DataFacade dataFacade;
	private BlobStore blobStore;
	private static TranslatorRegistry translatorReg = TranslatorRegistry
			.getInstance();

	public ReferenceSchemeTranslatorImpl(DataManager dataManager) {
		this.dataManager = dataManager;
		this.dataFacade = new DataFacade(dataManager);
		this.blobStore = dataManager.getBlobStore();
	}

	public TranslatorRunnable translateAsynch(
			final DataDocumentIdentifier id,
			final Class<? extends ReferenceScheme>... preferredTypes) {
		return new TranslatorRunnableImpl(preferredTypes, id);
	}

	protected ReferenceScheme translate(DataDocumentIdentifier id,
			Class<? extends ReferenceScheme>... preferredTypes)
			throws RetrievalException, NotFoundException {
		DataDocument dataDoc = (DataDocument) dataManager.getEntity(id);
		// mach ref scheme directly
		for (Class<? extends ReferenceScheme> type : preferredTypes) {
			for (ReferenceScheme ref : dataDoc.getReferenceSchemes()) {
				if (type.isInstance(ref)) {
					return ref;
				}
			}
		}
		// otherwise try to translate
		for (Class<? extends ReferenceScheme> type : preferredTypes) {
			for (ReferenceScheme ref : dataDoc.getReferenceSchemes()) {
				List<Translator> translators = translatorReg.getTranslators(
						ref, type);
				for (Translator translator : translators) {
					try {
						return translator.translate(dataManager, ref, type);
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
		return null;
	}

}
