/**
 * 
 */
package net.sf.taverna.t2.component.profile;

import java.util.regex.Pattern;

/**
 * @author alanrw
 *
 */
public class HandleException {

	private final uk.org.taverna.ns._2012.component.profile.HandleException proxied;
	private Pattern pattern;
	private ExceptionReplacement replacement;

	public HandleException(
			uk.org.taverna.ns._2012.component.profile.HandleException proxied) {
				this.proxied = proxied;
			pattern = Pattern.compile(proxied.getPattern(), Pattern.DOTALL);
	}
	
	public boolean matches(String s) {
		return pattern.matcher(s).matches();
	}
	
	public boolean pruneStack() {
		return (proxied.getPruneStack() != null);
	}
	
	public ExceptionReplacement getReplacement() {
		if ((replacement == null) && (proxied.getReplacement() != null)) {
			replacement = new ExceptionReplacement(proxied.getReplacement());
		}
		return replacement;
	}

}
