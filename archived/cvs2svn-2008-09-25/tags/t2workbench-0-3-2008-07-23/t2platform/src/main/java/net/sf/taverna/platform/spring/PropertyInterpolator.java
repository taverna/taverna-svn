package net.sf.taverna.platform.spring;

import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Static utility to insert string properties from a Properties object in place
 * of ${property.name} parts of the supplied string.
 * 
 * @author Tom Oinn
 * 
 */
public final class PropertyInterpolator {

	private static String regex = "\\$\\{([\\w\\.]+)\\}";
	private static Pattern pattern;
	private static Log log = LogFactory.getLog(PropertyInterpolator.class);

	static {
		pattern = Pattern.compile(regex);
	}

	/**
	 * Perform property interpolation using the system properties object
	 * 
	 * @param sourceString
	 *            a string containing zero or more ${...} elements corresponding
	 *            to properties to insert
	 * @return the sourceString with property values inserted in place of
	 *         variables *
	 * @throws RuntimeException
	 *             if a referenced property does not exist in the system
	 *             properties
	 */
	public static String interpolate(String sourceString) {
		return interpolate(sourceString, System.getProperties());
	}

	/**
	 * Perform property interpolation using the supplied properties object
	 * 
	 * @param sourceString
	 *            a string containing zero or more ${...} elements corresponding
	 *            to properties to insert
	 * @param props
	 *            the properties object from which property values should be
	 *            extracted
	 * @return the sourceString with property values inserted in place of
	 *         variables
	 * @throws RuntimeException
	 *             if a referenced property does not exist in the supplied
	 *             properties object
	 */
	public static String interpolate(String sourceString, Properties props) {
		Matcher matcher = pattern.matcher(sourceString);
		StringBuffer sb = new StringBuffer();
		int cursor = 0;
		while (matcher.find()) {
			String propertyValue = props.getProperty(matcher.group(1));
			if (propertyValue == null) {
				log.warn("Attempt to interpolate an undefined property '"
						+ matcher.group(1) + "'");
				throw new RuntimeException("Can't locate property '"
						+ matcher.group(1) + "'");
			}
			sb.append(sourceString.substring(cursor, matcher.start()));
			sb.append(propertyValue);
			cursor = matcher.end();
		}
		sb.append(sourceString.substring(cursor));
		return sb.toString();
	}

}
