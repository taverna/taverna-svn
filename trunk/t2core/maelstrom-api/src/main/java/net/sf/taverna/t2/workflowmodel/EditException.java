package net.sf.taverna.t2.workflowmodel;

/**
 * Superclass of all exceptions thrown when altering the workflow model through
 * the edit manager.
 * 
 * @author Tom Oinn
 * 
 */
public class EditException extends Exception {

	public EditException(String string) {
		super(string);
	}

	public EditException(String string, EditException cause) {
		super(string, cause);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

}
