package net.sf.taverna.platform.spring;

import java.util.Iterator;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LoggingConfigurer {

	@SuppressWarnings("unchecked")
	public LoggingConfigurer(Properties props) {
		Iterator i = props.keySet().iterator();
		while (i.hasNext()) {
			String loggerName = (String) i.next();
			String levelName = props.getProperty(loggerName);
			try {
				Level level = Level.parse(levelName);
				Logger l = Logger.getLogger(loggerName);
				l.setLevel(level);
			} catch (IllegalArgumentException e) {
				System.err.println("WARNING: Unable to parse '" + levelName
						+ "' as a java.util.Level for logger " + loggerName
						+ "; ignoring...");
			}
		}
	}

}
