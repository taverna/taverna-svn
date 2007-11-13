package net.sf.taverna.t2.cloudone.gui.entity.view;

import org.apache.log4j.Logger;

public class EditableException extends Exception {
	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(EditableException.class);

	/**
	 * 
	 */
	public EditableException() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 * @param cause
	 */
	public EditableException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 */
	public EditableException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param cause
	 */
	public EditableException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}
	
	
}
