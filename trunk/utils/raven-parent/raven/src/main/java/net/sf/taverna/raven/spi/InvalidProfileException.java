package net.sf.taverna.raven.spi;

import net.sf.taverna.raven.RavenException;

/**
 * Thrown when an attempt is made to construct a Profile from an invalid
 * XML document
 * @author Tom
 *
 */
public class InvalidProfileException extends RavenException {

	private static final long serialVersionUID = 1L;

	public InvalidProfileException(String message, Throwable cause) {
		super(message, cause);
	}
	public InvalidProfileException(String message) {
		super(message);
	}
	
}
