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

	private Replacement proxied;

	public ExceptionReplacement(Replacement replacement) {
		this.proxied = replacement;
	}
	
	public String getReplacementId() {
		return proxied.getReplacementId();
	}
	
	public String getReplacementMessage() {
		return proxied.getReplacementMessage();
	}

}
