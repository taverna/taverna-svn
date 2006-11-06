package net.sf.taverna.raven.log;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementation of LogInterface for a java.util.logging backend.
 * 
 * @author Stian Soiland
 *
 */
public class JavaLog implements LogInterface {
	private Logger logger;
	private static Map<Priority, Level> priorityMap = new HashMap<Priority, Level>();
	static {
		priorityMap.put(Priority.DEBUG, Level.FINE);
		priorityMap.put(Priority.INFO, Level.INFO);
		priorityMap.put(Priority.WARN, Level.WARNING);
		// java.util.logging don't have anything between WARNING and SEVERE
		priorityMap.put(Priority.ERROR, Level.SEVERE);
		priorityMap.put(Priority.FATAL, Level.SEVERE);
		// Should now have all priorities
		assert priorityMap.size() == Priority.values().length;
	}
	
	public JavaLog() {
		this(JavaLog.class);
	}
	
	public JavaLog(Class c) {
		// We'll use the full classname as our identifier to be
		// slightly log4j compatible
		logger = Logger.getLogger(c.getName());
	}
	
	public JavaLog getLogger(Class c) {
		return new JavaLog(c);
	}

	public void log(Priority p, Object msg, Throwable ex) {
		if (ex != null) {
			logger.log(priorityMap.get(p), msg.toString(), ex);
		} else {
			logger.log(priorityMap.get(p), msg.toString());
		}
	}

}
