package net.sf.taverna.t2.cyclone.translators;

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

	public ActivityTranslatorNotFoundException(String msg) {
		super(msg);
	}

	public ActivityTranslatorNotFoundException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	public ActivityTranslatorNotFoundException(Throwable arg0) {
		super(arg0);
	}	
}
