package net.sf.taverna.t2.cloudone.translator;

/**
 * Thrown from {@link Translator} if translation fails
 * 
 * @author Ian Dunlop
 * @author Stian Soiland
 *
 */
public class TranslatorException extends Exception {

	private static final long serialVersionUID = -7733091027387460987L;

	public TranslatorException() {
	}

	public TranslatorException(String message) {
		super(message);
	}

	public TranslatorException(Throwable cause) {
		super(cause);
	}

	public TranslatorException(String message, Throwable cause) {
		super(message, cause);
	}

}
