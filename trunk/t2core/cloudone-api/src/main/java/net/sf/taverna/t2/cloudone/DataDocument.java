package net.sf.taverna.t2.cloudone;

import java.util.Set;

/**
 * A bundle of zero or more reference scheme implementations pointing to byte
 * equivalent data held locally or remotely. References may not always be
 * resolvable subject to network and security constraints.
 * 
 * @author Tom Oinn
 * @author Matthew Pocock
 * 
 */
public interface DataDocument extends Entity<DataDocumentIdentifier> {

	/**
	 * Return the set of reference schemes contained within this data document
	 * 
	 * @return
	 */
	public Set<ReferenceScheme> getReferenceSchemes();
}
