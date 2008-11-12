package net.sf.taverna.t2.platform.plugin;

/**
 * Root exception thrown by the plugin manager and related interfaces
 * 
 * @author Tom Oinn
 * 
 */
public class PluginException extends RuntimeException {

	private static final long serialVersionUID = -3009194301355368167L;

	public PluginException() {
		super();
		// TODO Auto-generated constructor stub
	}

	public PluginException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

	public PluginException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	public PluginException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

}
