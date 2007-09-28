package net.sf.taverna.t2.cyclone.activity;

/**
 * <p>
 * An Exception indicating that a suitable {@link ActivityTranslator} cannot be found.
 * This will generally occur when no ActivityTranslator can be found for a given Taverna 1 Processor.
 * </p>
 * @author Stuart Owen
 *
 */
public class ActivityTranslatorNotFoundException extends Exception {

	private static final long serialVersionUID = 8779255468276952392L;

	/**
	 * @param msg a message describing the reason for the exception.
	 */
	public ActivityTranslatorNotFoundException(String msg) {
		super(msg);
	}

	
	/**
	 * @param msg a message describing the reason for the exception.
	 * @param cause a previous exception that caused this ActivityTranslatorNotFoundException to be thrown.
	 */
	public ActivityTranslatorNotFoundException(String msg, Throwable cause) {
		super(msg, cause);
	}

	/**
	 * @param cause a previous exception that caused this ActivityTranslatorNotFoundException to be thrown.
	 */
	public ActivityTranslatorNotFoundException(Throwable cause) {
		super(cause);
	}	
}
