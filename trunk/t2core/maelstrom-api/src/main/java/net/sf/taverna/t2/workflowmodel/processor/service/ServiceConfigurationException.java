package net.sf.taverna.t2.workflowmodel.processor.service;

/**
 * Thrown when attempting to configure a Service instance with an invalid
 * configuration. Causes may include actual configuration errors, unavailable
 * services etc.
 * 
 * @author Tom Oinn
 * 
 */
public class ServiceConfigurationException extends Exception {

	private static final long serialVersionUID = -1000005633224821831L;

	public ServiceConfigurationException() {
		// TODO Auto-generated constructor stub
	}

	public ServiceConfigurationException(String arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}

	public ServiceConfigurationException(Throwable arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}

	public ServiceConfigurationException(String arg0, Throwable arg1) {
		super(arg0, arg1);
		// TODO Auto-generated constructor stub
	}

}
