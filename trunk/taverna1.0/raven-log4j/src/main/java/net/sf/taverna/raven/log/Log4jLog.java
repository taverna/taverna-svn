package net.sf.taverna.raven.log;

import java.util.HashMap;
import java.util.Map;

import net.sf.taverna.raven.repository.impl.LocalArtifactClassLoader;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * Implementation of LogInterface for a log4j backend.
 * To use this implementation, do:
 * <pre>
 * Log.setImplementation(new Log4jLog());
 * </pre>
 * Note that this depends on log4j being available through
 * the system default classloader (ie. normally on the CLASSPATH), 
 * as we can't load log4j through Raven and at the same time use
 * log4j for logging Raven internals, as that would force recursive 
 * logging calls when log4j needs to load any of it's classes.
 * <p>
 * (However, Raven can <b>download</b> log4j, this trick is used by 
 * Taverna's Bootstrap class by including log4j in the profile with 
 * system="true", and specifying BootstrapClassLoader as the system
 * classloader). 
 * 
 * @see net.sf.taverna.tools.Bootstrap
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
		if (Logger.class.getClassLoader() instanceof LocalArtifactClassLoader) {
			throw new IllegalStateException("log4j cannot be used as a Raven logger when loaded through Raven");
		}
		log4j = Logger.getLogger(c);
	}

	public Log4jLog getLogger(Class c) {
		return new Log4jLog(c);
	}

	public void log(LogInterface.Priority p, Object msg, Throwable ex) {
		// FIXME: Will log wrong line number, as it will log the line below
		log4j.log(priorityMap.get(p), msg, ex);
	}
}