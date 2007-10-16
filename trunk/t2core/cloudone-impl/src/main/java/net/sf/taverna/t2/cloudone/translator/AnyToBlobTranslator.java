package net.sf.taverna.t2.cloudone.translator;

import java.io.InputStream;

import net.sf.taverna.t2.cloudone.BlobReferenceScheme;
import net.sf.taverna.t2.cloudone.BlobStore;
import net.sf.taverna.t2.cloudone.DataManager;
import net.sf.taverna.t2.cloudone.DereferenceException;
import net.sf.taverna.t2.cloudone.ReferenceScheme;

public class AnyToBlobTranslator implements Translator {

	public BlobReferenceScheme<?> translate(DataManager dataManager, ReferenceScheme ref,
			Class<? extends ReferenceScheme> type) throws DereferenceException {
		BlobStore blobStore = dataManager.getBlobStore();
		InputStream stream = ref.dereference(dataManager);
		BlobReferenceScheme<?> blobRef = blobStore
				.storeFromStream(stream);
		return blobRef;
	}

	public boolean canTranslate(ReferenceScheme fromScheme,
			Class<? extends ReferenceScheme> toType) {
		return toType.isAssignableFrom(BlobReferenceScheme.class);
	}

}
