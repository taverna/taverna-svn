package net.sf.taverna.t2.cyclone.translators;

public class ServiceTranslatorNotFoundException extends Exception {

	private static final long serialVersionUID = 8779255468276952392L;

	public ServiceTranslatorNotFoundException(String msg) {
		super(msg);
	}

	public ServiceTranslatorNotFoundException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	public ServiceTranslatorNotFoundException(Throwable arg0) {
		super(arg0);
	}	
}
