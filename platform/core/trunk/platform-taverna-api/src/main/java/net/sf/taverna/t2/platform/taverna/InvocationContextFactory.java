package net.sf.taverna.t2.platform.taverna;

import net.sf.taverna.t2.invocation.InvocationContext;

/**
 * Used to obtain instances of the InvocationContext interface
 * 
 * @author Tom Oinn
 */
public interface InvocationContextFactory {

	/**
	 * Construct and return a new InvocationContext implementation which can be
	 * used to inject facilities such as provenance capture and workflow
	 * monitoring to an enactment
	 * 
	 * @return the newly created invocation context
	 */
	public InvocationContext createInvocationContext();

}
