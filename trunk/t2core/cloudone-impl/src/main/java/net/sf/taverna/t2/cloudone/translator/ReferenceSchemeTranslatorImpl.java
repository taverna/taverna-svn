package net.sf.taverna.t2.cloudone.translator;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import javax.management.RuntimeErrorException;

import net.sf.taverna.t2.cloudone.BlobReferenceScheme;
import net.sf.taverna.t2.cloudone.BlobStore;
import net.sf.taverna.t2.cloudone.DataManager;
import net.sf.taverna.t2.cloudone.DereferenceException;
import net.sf.taverna.t2.cloudone.ReferenceScheme;
import net.sf.taverna.t2.cloudone.datamanager.DataFacade;
import net.sf.taverna.t2.cloudone.datamanager.NotFoundException;
import net.sf.taverna.t2.cloudone.datamanager.RetrievalException;
import net.sf.taverna.t2.cloudone.entity.DataDocument;
import net.sf.taverna.t2.cloudone.identifier.DataDocumentIdentifier;
import net.sf.taverna.t2.cloudone.impl.url.URLReferenceScheme;

public class ReferenceSchemeTranslatorImpl implements ReferenceSchemeTranslator {

	private DataManager dataManager;
	private DataFacade dataFacade;
	private BlobStore blobStore;
	private static TranslatorRegistry translatorReg = TranslatorRegistry.getInstance();

	public ReferenceSchemeTranslatorImpl(DataManager dataManager) {
		this.dataManager = dataManager;
		this.dataFacade = new DataFacade(dataManager);
		this.blobStore = dataManager.getBlobStore();
	}

	public ReferenceScheme translate(DataDocumentIdentifier id,
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
				List<Translator> translators = translatorReg
						.getTranslators(ref, type);
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
