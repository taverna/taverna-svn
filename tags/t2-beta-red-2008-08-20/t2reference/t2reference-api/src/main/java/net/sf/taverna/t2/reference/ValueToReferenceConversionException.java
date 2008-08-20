package net.sf.taverna.t2.reference;

/**
 * Thrown by instances of ValueToReferenceConvertor when trying to convert an
 * object to an instance of ExternalReferenceSPI if the conversion process fails
 * for some reason.
 * 
 * @author Tom Oinn
 * 
 */
public class ValueToReferenceConversionException extends RuntimeException {

	private static final long serialVersionUID = 3259959719223191820L;

	public ValueToReferenceConversionException() {
		// 
	}

	public ValueToReferenceConversionException(String message) {
		super(message);
	}

	public ValueToReferenceConversionException(Throwable cause) {
		super(cause);
	}

	public ValueToReferenceConversionException(String message, Throwable cause) {
		super(message, cause);
	}

}
