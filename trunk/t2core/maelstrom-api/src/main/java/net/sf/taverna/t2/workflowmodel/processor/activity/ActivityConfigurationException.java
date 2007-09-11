package net.sf.taverna.t2.workflowmodel.processor.activity;

/**
 * Thrown when attempting to configure a Service instance with an invalid
 * configuration. Causes may include actual configuration errors, unavailable
 * services etc.
 * 
 * @author Tom Oinn
 * 
 */
public class ActivityConfigurationException extends Exception {

	private static final long serialVersionUID = -1000005633224821831L;

	public ActivityConfigurationException() {
		// TODO Auto-generated constructor stub
	}

	public ActivityConfigurationException(String arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}

	public ActivityConfigurationException(Throwable arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}

	public ActivityConfigurationException(String arg0, Throwable arg1) {
		super(arg0, arg1);
		// TODO Auto-generated constructor stub
	}

}
