package net.sf.taverna.t2.cyclone.translators;

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
