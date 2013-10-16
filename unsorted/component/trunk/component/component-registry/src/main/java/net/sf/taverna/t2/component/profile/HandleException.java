/**
 * 
 */
package net.sf.taverna.t2.component.profile;

import static java.util.regex.Pattern.DOTALL;
import static java.util.regex.Pattern.compile;

import java.util.regex.Pattern;

/**
 * @author alanrw
 * 
 */
public class HandleException {
	private final Pattern pattern;
	private ExceptionReplacement replacement;
	private final boolean pruneStack;

	public HandleException(
			uk.org.taverna.ns._2012.component.profile.HandleException proxied) {
		pruneStack = proxied.getPruneStack() != null;
		pattern = compile(proxied.getPattern(), DOTALL);
		if (proxied.getReplacement() != null)
			replacement = new ExceptionReplacement(proxied.getReplacement());
	}

	public boolean matches(String s) {
		return pattern.matcher(s).matches();
	}

	public boolean pruneStack() {
		return pruneStack;
	}

	public ExceptionReplacement getReplacement() {
		return replacement;
	}
}
