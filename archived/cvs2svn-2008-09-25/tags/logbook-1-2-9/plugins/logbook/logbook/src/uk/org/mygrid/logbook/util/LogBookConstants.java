package uk.org.mygrid.logbook.util;

import org.apache.log4j.Logger;

public class LogBookConstants {

	public static final Logger PERFORMANCE_LOGGER = Logger
			.getLogger("uk.org.mygrid.logbook.util.PERFORMANCE");

	public static void logPerformance(String action, long startTime) {
		if (PERFORMANCE_LOGGER.isDebugEnabled())
			PERFORMANCE_LOGGER.debug(action + ": "
					+ (System.currentTimeMillis() - startTime) + "ms");
	}
}
