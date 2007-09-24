package net.sf.taverna.t2.cyclone;

/**
 * <p>
 * An Exception indicating that a critical error has occurred whilst trying to translate
 * a Taverna 1 scufl workflow model into a Taverna 2 Dataflow.
 * </p>
 * 
 * @author Stuart Owen
 *
 */
public class WorkflowTranslationException extends Exception {

	private static final long serialVersionUID = -6167115193252256144L;

	public WorkflowTranslationException(String msg) {
		super(msg);
	}

	public WorkflowTranslationException(Throwable cause) {
		super(cause);
	}

	public WorkflowTranslationException(String msg, Throwable cause) {
		super(msg, cause);
	}

}
