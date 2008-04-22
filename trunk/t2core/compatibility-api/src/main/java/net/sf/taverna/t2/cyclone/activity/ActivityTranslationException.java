package net.sf.taverna.t2.cyclone.activity;

/**
 * An Exception indicating problem translating a Taverna 1 Processor instance into an equivalent Taverna 2 Activity
 * 
 * @author Stuart Owen
 * @author David Withers
 *
 */
public class ActivityTranslationException extends Exception {

	private static final long serialVersionUID = 1L;

	/**
	 * Constructs the ActivityTranslationException instance with a message.
	 * 
	 * @param message the message explaining the cause of the problem.
	 */
	public ActivityTranslationException(String message) {
		super(message);
	}

	/**
	 * Constructs the ActivityTranslationException instance with the message and cause.
	 * 
	 * @param message the message explaining the cause of the problem.
	 * @param cause the root cause if this exception is being raised as the consequence of another Exception
	 */
	public ActivityTranslationException(String message, Throwable cause) {
		super(message, cause);
	}

}
