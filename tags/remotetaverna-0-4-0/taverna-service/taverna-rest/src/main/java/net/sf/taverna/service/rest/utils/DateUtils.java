package net.sf.taverna.service.rest.utils;

import java.math.BigDecimal;

import org.apache.log4j.Logger;
import org.apache.xmlbeans.GDuration;

public class DateUtils {
	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(DateUtils.class);
	

	/**
	 * Produce a human-readable version of an xsd:duration string. For example,
	 * "P1D5M" is represented as "1 day 5 minutes". Returns null if the string
	 * is empty or don't specify any period, this normally means "never".
	 * 
	 * @param duration
	 *            A XML Schema "duration" style (ISO 8601) duration
	 * @return An (English) human readable presentation of the duration
	 */
	public static String humanDuration(String xsdDuration) {
		if (xsdDuration == null || xsdDuration.equals("")) {
			return null;
		}
		GDuration duration = new GDuration(xsdDuration);
		StringBuffer sb = new StringBuffer();
		includeDuration(sb, duration.getYear(), "year");
		includeDuration(sb, duration.getMonth(), "month");
		includeDuration(sb, duration.getDay(), "day");
		includeDuration(sb, duration.getHour(), "hour");
		includeDuration(sb, duration.getMinute(), "minute");
		if (duration.getFraction().equals(BigDecimal.ZERO)) {
			includeDuration(sb, duration.getSecond(), "second");
		} else {
			BigDecimal seconds = BigDecimal.valueOf(duration.getSecond());
			seconds = seconds.add(duration.getFraction());
			sb.append(seconds.toPlainString());
			sb.append(" seconds ");
		}
		
		if (sb.length() == 0) {
			return null;
		}
		// Remove trailing space
		if (sb.charAt(sb.length()-1) == ' ') {
			sb.deleteCharAt(sb.length()-1);
		}
		return sb.toString();
	}

	/** 
	 * Used by {@link #humanDuration(GDuration)}.
	 * 
	 * @param sb
	 * @param num
	 * @param string
	 */
	private static void includeDuration(StringBuffer sb, int num, String string) {
		if (num == 0) {
			return;
		}
		sb.append(num);
		sb.append(' ');
		sb.append(string);
		if (Math.abs(num) != 1) {
			sb.append('s');
		}
		sb.append(' ');
	}
}
