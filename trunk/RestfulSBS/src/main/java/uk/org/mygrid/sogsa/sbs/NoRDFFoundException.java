package uk.org.mygrid.sogsa.sbs;

public class NoRDFFoundException extends Exception {

	public NoRDFFoundException() {
		super();
	}
	
	public NoRDFFoundException(String message) {
		super(message);
	}

	public NoRDFFoundException(String message, Throwable cause) {
		super(message, cause);
	}

	public NoRDFFoundException(Throwable cause) {
		super(cause);
	}
}
