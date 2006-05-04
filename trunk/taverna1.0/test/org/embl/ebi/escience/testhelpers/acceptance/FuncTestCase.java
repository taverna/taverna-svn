package org.embl.ebi.escience.testhelpers.acceptance;

import junit.extensions.jfunc.JFuncTestCase;

/**
 * Extensions to JFuncTestCase to  make it a bit more.. eh.. functional!
 * 
 * @author Stian Soiland
 *
 */
abstract public class FuncTestCase extends JFuncTestCase {
	
	public FuncTestCase() {
		super("testNothing");
		this.setFatal(false);
	}

	public void testNothing() {
		fail("Testcase is not supposed to be run.");
	}
	
	/**
	 * Assert that the string matches the regular expression pattern
	 * 
	 * @param pattern regular expression
	 * @param actual string to test
	 */
	public void assertMatches(String pattern, String actual) {
		assertMatches(null, pattern, actual);
	}
	
	/**
	 * Assert that the string matches the regular expression pattern
	 * 
	 * @param message message to append
	 * @param pattern regular expression
	 * @param actual string to test
	 */
	public void assertMatches(String message, String pattern, String actual) {
		if (actual.matches(pattern)) {
			return;
		}
		String formatted = "";
		if (message != null)
			formatted = message + " ";
		fail(formatted + "expected match with:<" + pattern + "> but was:<"
				+ actual + ">");
	}
}
