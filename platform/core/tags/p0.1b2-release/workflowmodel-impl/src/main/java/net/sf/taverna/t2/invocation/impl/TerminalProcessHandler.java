package net.sf.taverna.t2.invocation.impl;

import net.sf.taverna.t2.invocation.InvocationContext;
import net.sf.taverna.t2.invocation.ProcessIdentifier;
import net.sf.taverna.t2.workflowmodel.TokenProcessingEntity;

/**
 * Injected into ProcessorImpl to receive notice that the processor has
 * completed handling of a given entity stream.
 * 
 * @author Tom Oinn
 * 
 */
public interface TerminalProcessHandler {

	void completeTerminal(ProcessIdentifier processIdentifier,
			TokenProcessingEntity terminal, InvocationContext context);

}
