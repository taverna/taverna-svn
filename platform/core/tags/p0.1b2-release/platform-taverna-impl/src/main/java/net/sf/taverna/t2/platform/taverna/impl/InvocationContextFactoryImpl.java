package net.sf.taverna.t2.platform.taverna.impl;

import net.sf.taverna.t2.invocation.InvocationContext;
import net.sf.taverna.t2.invocation.impl.InvocationContextImpl;
import net.sf.taverna.t2.platform.taverna.InvocationContextFactory;
import net.sf.taverna.t2.reference.ReferenceService;

/**
 * Implementation of InvocationContextFactory that returns a new
 * InvocationContext populated with the configured ReferenceService as its
 * reference service using the InvocationContextImpl from the workflowmodel-impl
 * module.
 * 
 * @author Tom Oinn
 */
public class InvocationContextFactoryImpl implements InvocationContextFactory {

	private ReferenceService referenceService;

	public void setReferenceService(ReferenceService referenceService) {
		this.referenceService = referenceService;
	}

	public InvocationContext createInvocationContext() {
		InvocationContextImpl ici = new InvocationContextImpl();
		ici.setReferenceService(referenceService);
		return ici;
	}

}
