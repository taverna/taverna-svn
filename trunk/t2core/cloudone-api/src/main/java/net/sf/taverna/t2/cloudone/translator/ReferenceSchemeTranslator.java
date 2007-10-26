package net.sf.taverna.t2.cloudone.translator;

import java.util.List;

import net.sf.taverna.t2.cloudone.TranslationPreference;
import net.sf.taverna.t2.cloudone.identifier.DataDocumentIdentifier;

public interface ReferenceSchemeTranslator {

	public AsynchRefScheme translateAsynch(DataDocumentIdentifier id,
			List<TranslationPreference> preferences);

}
