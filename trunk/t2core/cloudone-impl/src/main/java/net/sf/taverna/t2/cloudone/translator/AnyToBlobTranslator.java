package net.sf.taverna.t2.cloudone.translator;

import java.io.InputStream;

import net.sf.taverna.t2.cloudone.BlobReferenceScheme;
import net.sf.taverna.t2.cloudone.BlobStore;
import net.sf.taverna.t2.cloudone.DataManager;
import net.sf.taverna.t2.cloudone.DereferenceException;
import net.sf.taverna.t2.cloudone.ReferenceScheme;
import net.sf.taverna.t2.cloudone.bean.ReferenceBean;

/**
 * Translator for any type of {@link ReferenceScheme} to a
 * {@link BlobReferenceScheme}.
 * 
 * @author Stian Soiland
 * @author Ian Dunlop
 * 
 */
//TODO: This does not build on Java 1.6 if parameterised type is BlobReferenceScheme<?>
@SuppressWarnings("unchecked")
public class AnyToBlobTranslator implements Translator<BlobReferenceScheme> { 

	public BlobReferenceScheme<? extends ReferenceBean> translate(DataManager dataManager,
			ReferenceScheme ref) throws DereferenceException,
			TranslatorException {
		BlobStore blobStore = dataManager.getBlobStore();
		InputStream stream = ref.dereference(dataManager);
		BlobReferenceScheme<?> blobRef = blobStore.storeFromStream(stream);
		return blobRef;
	}

	/**
	 * Check whether the given {@link ReferenceScheme} can be translated to a
	 * {@link ReferenceScheme}. 
	 * 
	 * @return true if <code>toType</code> is {@link BlobReferenceScheme}.
	 */
	public boolean canTranslate(ReferenceScheme fromScheme,
			Class<? extends ReferenceScheme> toType) {
		return toType.isAssignableFrom(BlobReferenceScheme.class);
	}

}
