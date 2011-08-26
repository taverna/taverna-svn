package org.embl.ebi.escience.baclava;

import java.util.ArrayList;
import java.util.List;

/**
 * Interface for things that can guess the mimetype associated with a Java
 * object. <p/> At some point, we may want to refactor this to use an SPI and
 * registry.
 * 
 * @author Matthew Pocock
 */
public interface MimeTypeGuesser {
	/**
	 * A default implementation.
	 */
	public static MimeTypeGuesser DEFAULT = new MimeTypeGuesser() {
		public List guessMimeType(Object obj) {
			List types = new ArrayList();

			if (obj instanceof String) {
				types.add("text/plain");
			} else if (obj instanceof byte[]) {
				types.add("application/octet-stream");
			} else {
				types.add("application/X-UNKNOWN-JAVA-TYPE-"
						+ obj.getClass().getName());
			}

			return types;
		}
	};

	/**
	 * Guess the mimetype of a Java object. Return an empty list if no guess
	 * could be made.
	 * 
	 * @param obj
	 * @return a List of mimetypes that pertain to the object
	 */
	public List guessMimeType(Object obj);
}
