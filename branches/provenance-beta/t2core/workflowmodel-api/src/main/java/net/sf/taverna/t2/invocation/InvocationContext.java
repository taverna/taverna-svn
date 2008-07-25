package net.sf.taverna.t2.invocation;

import net.sf.taverna.t2.provenance.ProvenanceConnector;
import net.sf.taverna.t2.reference.ReferenceContext;
import net.sf.taverna.t2.reference.ReferenceService;

/**
 * Carries the context of a workflow invocation, the necessary data manager,
 * security agents and any other resource shared across the invocation such as
 * provenance injectors.
 * 
 * @author Tom Oinn
 * 
 */
public interface InvocationContext extends ReferenceContext {

	/**
	 * Return the reference service to be used within this invocation context
	 * 
	 * @return a configured instance of ReferenceService to be used to resolve
	 *         and register references to data in the workflow
	 */
	public ReferenceService getReferenceService();

	public ProvenanceConnector getProvenanceConnector();

}
