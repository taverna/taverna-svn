package net.sf.taverna.t2.reference;

/**
 * Thrown when the reference set augmentor is unable to provide at least one of
 * the desired types for any reason.
 * 
 * @author Tom Oinn
 * 
 */
public class ReferenceSetAugmentationException extends RuntimeException {

	private static final long serialVersionUID = -6156508424485682266L;

	public ReferenceSetAugmentationException() {
		//
	}

	public ReferenceSetAugmentationException(String message) {
		super(message);
	}

	public ReferenceSetAugmentationException(Throwable cause) {
		super(cause);
	}

	public ReferenceSetAugmentationException(String message, Throwable cause) {
		super(message, cause);
	}

}
