package uk.org.mygrid.sogsa.sbs;

public class SemanticBindingNotFoundException extends Exception {
	public SemanticBindingNotFoundException() {
		super();
	}

	public SemanticBindingNotFoundException(String message) {
		super(message);
	}

	public SemanticBindingNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}

	public SemanticBindingNotFoundException(Throwable cause) {
		super(cause);
	}

}
