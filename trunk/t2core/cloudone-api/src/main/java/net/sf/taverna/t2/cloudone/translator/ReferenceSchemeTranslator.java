package net.sf.taverna.t2.cloudone.translator;

import net.sf.taverna.t2.cloudone.ReferenceScheme;
import net.sf.taverna.t2.cloudone.identifier.DataDocumentIdentifier;

public interface ReferenceSchemeTranslator {

	public TranslatorRunnable translateAsynch(DataDocumentIdentifier id,
			Class<? extends ReferenceScheme>... preferredTypes);

}
