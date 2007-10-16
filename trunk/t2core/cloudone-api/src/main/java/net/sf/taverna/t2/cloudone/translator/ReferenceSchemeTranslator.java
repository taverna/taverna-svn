package net.sf.taverna.t2.cloudone.translator;

import net.sf.taverna.t2.cloudone.ReferenceScheme;
import net.sf.taverna.t2.cloudone.datamanager.NotFoundException;
import net.sf.taverna.t2.cloudone.datamanager.RetrievalException;
import net.sf.taverna.t2.cloudone.identifier.DataDocumentIdentifier;

public interface ReferenceSchemeTranslator {

	public ReferenceScheme translate(DataDocumentIdentifier id,
			Class<? extends ReferenceScheme>... preferedTypes) throws RetrievalException, NotFoundException;

}
