package net.sf.taverna.t2.workflowmodel.processor.activity;

/**
 * Thrown when attempting to configure an Activity instance with an invalid
 * configuration. Causes may include actual configuration errors, unavailable
 * activities etc.
 * 
 * @author Tom Oinn
 * 
 */
public class ActivityConfigurationException extends Exception {

	private static final long serialVersionUID = -1000005633224821831L;

	/**
	 * @param msg a message describing the reason for the exception.
	 */
	public ActivityConfigurationException(String msg) {
		super(msg);
	}

	/**
	 * @param cause a previous exception that caused this ActivityConfigurationException to be thrown.
	 */
	public ActivityConfigurationException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param msg a message describing the reason for the exception.
	 * @param cause a previous exception that caused this ActivityConfigurationException to be thrown.
	 */
	public ActivityConfigurationException(String msg, Throwable cause) {
		super(msg, cause);
	}

}
