package net.sf.taverna.t2.cloudone.entity;

import java.util.Set;

import net.sf.taverna.t2.cloudone.ReferenceScheme;
import net.sf.taverna.t2.cloudone.identifier.DataDocumentIdentifier;

/**
 * A bundle of zero or more reference scheme implementations pointing to byte
 * equivalent data held locally or remotely. References may not always be
 * resolvable subject to network and security constraints.
 * 
 * @author Tom Oinn
 * @author Matthew Pocock
 * 
 */
public interface DataDocument extends Entity<DataDocumentIdentifier, String> {

	/**
	 * Return the set of reference schemes contained within this data document
	 * 
	 * @return
	 */
	public Set<ReferenceScheme> getReferenceSchemes();
}
