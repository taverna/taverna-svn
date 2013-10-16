/**
 * 
 */
package net.sf.taverna.t2.component.profile;

import uk.org.taverna.ns._2012.component.profile.Replacement;

/**
 * @author alanrw
 * 
 */
public class ExceptionReplacement {
	private final String id, message;

	public ExceptionReplacement(Replacement replacement) {
		id = replacement.getReplacementId();
		message = replacement.getReplacementMessage();
	}

	public String getReplacementId() {
		return id;
	}

	public String getReplacementMessage() {
		return message;
	}
}
