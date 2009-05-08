package net.sf.taverna.t2.invocation;

/**
 * Implemented by components within a workflow which may cause workflow level
 * failures. Such components allow an enclosing dataflow to attach a failure
 * handler and also support state purge operations
 * 
 * @author Tom Oinn
 * 
 */
public interface FailureAware {

	public void setFailureHandler(FailureHandler handler);

	public void purgeState(ProcessIdentifier parentProcess);

}
