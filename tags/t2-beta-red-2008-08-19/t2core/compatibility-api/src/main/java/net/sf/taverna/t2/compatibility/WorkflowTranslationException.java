package net.sf.taverna.t2.compatibility;

/**
 * <p>
 * An Exception indicating that a critical error has occurred whilst trying to translate
 * a Taverna 1 Scufl workflow model into an equivalent Taverna 2 Dataflow.
 * </p>
 * 
 * @author Stuart Owen
 *
 */
public class WorkflowTranslationException extends Exception {

	private static final long serialVersionUID = -6167115193252256144L;

	/**
	 * @param msg a message describing the reason for the exception.
	 */
	public WorkflowTranslationException(String msg) {
		super(msg);
	}

	/**
	 * @param cause a previous exception that caused this WorkflowTranslationException to be thrown.
	 */
	public WorkflowTranslationException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param msg a message describing the reason for the exception.
	 * @param cause a previous exception that caused this WorkflowTranslationException to be thrown.
	 */
	public WorkflowTranslationException(String msg, Throwable cause) {
		super(msg, cause);
	}

}
