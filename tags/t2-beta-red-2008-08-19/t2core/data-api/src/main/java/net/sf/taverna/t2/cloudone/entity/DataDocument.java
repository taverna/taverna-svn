package net.sf.taverna.t2.cloudone.entity;

import java.util.Set;

import net.sf.taverna.t2.cloudone.bean.DataDocumentBean;
import net.sf.taverna.t2.cloudone.identifier.DataDocumentIdentifier;
import net.sf.taverna.t2.cloudone.refscheme.ReferenceScheme;

/**
 * A bundle of zero or more reference scheme implementations pointing to byte
 * equivalent data held locally or remotely. References may not always be
 * resolvable subject to network and security constraints.
 * 
 * @author Tom Oinn
 * @author Matthew Pocock
 * 
 */
public interface DataDocument extends
		Entity<DataDocumentIdentifier, DataDocumentBean> {

	/**
	 * Return the set of reference schemes contained within this data document.
	 * 
	 * @return Set of {@link ReferenceScheme}s
	 */
	@SuppressWarnings("unchecked")
	public Set<ReferenceScheme> getReferenceSchemes();
}
