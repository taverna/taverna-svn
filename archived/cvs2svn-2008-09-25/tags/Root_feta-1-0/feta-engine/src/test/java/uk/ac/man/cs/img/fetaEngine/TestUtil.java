/*
 * TestUtil.java
 *
 * Created on January 21, 2005, 4:35 PM
 */

package uk.ac.man.cs.img.fetaEngine;

/**
 * 
 * @author penpecip
 */
public class TestUtil {

	/** Creates a new instance of TestUtil */
	public TestUtil() {
	}

	/* remove leading whitespace */
	public static String ltrim(String source) {
		return source.replaceAll("^\\s+", "");
	}

	/* remove trailing whitespace */
	public static String rtrim(String source) {
		return source.replaceAll("\\s+$", "");
	}

	/* replace multiple whitespaces between words with single blank */
	public static String itrim(String source) {
		return source.replaceAll("\\b\\s{2,}\\b", " ");
	}

	/* remove all superfluous whitespaces in source string */
	public static String trim(String source) {
		return itrim(ltrim(rtrim(source)));
	}

	public static String lrtrim(String source) {
		return ltrim(rtrim(source));
	}

}