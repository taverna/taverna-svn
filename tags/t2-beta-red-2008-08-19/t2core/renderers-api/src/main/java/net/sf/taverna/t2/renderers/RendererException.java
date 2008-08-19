package net.sf.taverna.t2.renderers;

/**
 * If a renderer fails for any reason then throw one of these with an appropriate
 * message
 * 
 * @author Ian Dunlop
 * 
 */
public class RendererException extends Exception {

	public RendererException() {
	}

	public RendererException(String message) {
		super(message);
	}

	public RendererException(Throwable cause) {
		super(cause);
	}

	public RendererException(String message, Throwable cause) {
		super(message, cause);
	}

}
