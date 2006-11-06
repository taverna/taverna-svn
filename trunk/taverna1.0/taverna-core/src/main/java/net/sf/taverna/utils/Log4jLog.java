package net.sf.taverna.utils;

import java.util.HashMap;
import java.util.Map;

import net.sf.taverna.raven.log.LogInterface;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * Implementation of LogInterface for a log4j backend.
 * 
 * @author Stian Soiland
 */
public class Log4jLog implements LogInterface {
	// Mapping between LogInterface.Priority and Log4Js Levels, such as "Debug" and "Warn"
	private static Map<Priority, Level> priorityMap = new HashMap<Priority, Level>();
	static {
		// We are lucky, the LogInterface.Priority has been designed to match
		// the log4j priorities
		for (Priority p : Priority.values()) {
			priorityMap.put(p, Level.toLevel(p.name()));
		}
	}
	private Logger log4j;
	
	public Log4jLog() {
		this(Log4jLog.class);
	}
	
	public Log4jLog(Class c) {
		log4j = Logger.getLogger(c);
	}

	public Log4jLog getLogger(Class c) {
		return new Log4jLog(c);
	}

	public void log(LogInterface.Priority p, Object msg, Throwable ex) {
		log4j.log(priorityMap.get(p), msg, ex);
	}
}