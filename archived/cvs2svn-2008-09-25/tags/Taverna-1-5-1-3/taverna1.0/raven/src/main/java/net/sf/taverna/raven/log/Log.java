package net.sf.taverna.raven.log;


/**
 * Log proxy for Raven
 * <p>
 * Allow Raven to log its internals without worrying about which 
 * implementation of the logging framework to use. 
 * Logging framework can be switched on the fly by 
 * calling Log.setImplementation(LogInterface).
 * <p>
 * The public interface of this class slightly resembles
 * that of Log4j.Logger. Example usage:
 * <pre>
 * public class LocalRepository implements Repository {
 *     private static Log logger = Log.getLogger(LocalRepository.class);
 *       ..
 *                      try { 
 * 							ac = new LocalArtifactClassLoader(dep);
 * 							logger.info("Found " + ac);
 *     					} catch (MalformedURLException e) {
 * 							logger.error("Malformed URL when loading " + dep, e);
 * 						}
 * </pre>
 * <p>
 * The default log implementation is JavaLog, which proxies to java.util.logging.
 * For details on how to modify these settings, see JavaLog.
 * Other available implementations are ConsoleLog, which just prints to System.err 
 * for everything on level WARN or above, or Log4jLog in the artifact raven-log4j, 
 * which depends on log4j being available through the system classloader. For details,
 * see Log4jLog.
 * 
 * @see java.util.logging.Logging
 * @see JavaLog
 * @see ConsoleLog
 * @see Log4jLog
 * @see Log.setImplementation(LogInterface)
 * @author Stian Soiland
 *
 */
public class Log {
	// For our internal logging (hihi)
	private static Log logger = Log.getLogger(Log.class);
	// FIXME: Default log implementation should be set by properties
	// Implementation as set with setImplementation() - possibly null (no logging)
	private static LogInterface logImplementation = new JavaLog();
	// The class that constructed this logger with getLogger(Class)
	private Class callingClass;
	// The instance of our implementation
	private LogInterface logInstance;

	/**
	 * Private constructor, use getLogger(Class) instead.
	 * 
	 * @param c Class that 
	 */
	private Log(Class c) {
		this.callingClass = c;
		this.logInstance = null;
	}

	/**
	 * Set the implementation 
	 * 
	 * @param implementation
	 */
	public synchronized static void setImplementation(LogInterface implementation) {
		LogInterface oldImplementation = logImplementation;
		logImplementation = implementation;
		logger.info("Changed log implementation from " + oldImplementation + " to " + implementation);
	}
	
	public static Log getLogger(Class c) {
		return new Log(c);
	}
	
	public void debug(Object msg) {
		log(LogInterface.Priority.DEBUG, msg, null);
	}
	public void info(Object msg) {
		log(LogInterface.Priority.INFO, msg, null);
	}
	public void warn(Object msg) {
		log(LogInterface.Priority.WARN, msg, null);
	}
	public void error(Object msg) {
		log(LogInterface.Priority.ERROR, msg, null);
	}
	public void fatal(Object msg) {
		log(LogInterface.Priority.FATAL, msg, null);
	}
	
	public void debug(Object msg, Throwable ex) {
		log(LogInterface.Priority.DEBUG, msg, ex);
	}
	public void info(Object msg, Throwable ex) {
		log(LogInterface.Priority.INFO, msg, ex);
	}
	public void warn(Object msg, Throwable ex) {
		log(LogInterface.Priority.WARN, msg, ex);
	}
	public void error(Object msg, Throwable ex) {
		log(LogInterface.Priority.ERROR, msg, ex);
	}
	public void fatal(Object msg, Throwable ex) {
		log(LogInterface.Priority.FATAL, msg, ex);
	}
	
	public synchronized void log(LogInterface.Priority priority, Object msg, Throwable ex) {
		if (! checkImplementation()) {
			return;
		}
		try {
			logInstance.log(priority, msg, ex);
		} catch (Throwable t) {
			System.err.println("Raven could not log to " + logInstance);
			t.printStackTrace();
			System.err.println("Disabling Raven logging");
			setImplementation(null);
		}
	}
		
	private synchronized boolean checkImplementation() {
		if (logImplementation == null) {
			return false;
		}
		if (logInstance == null || ! logImplementation.getClass().isInstance(logInstance)) {
			// Not yet initialized or implementation has changed
			try {
				logInstance = logImplementation.getLogger(callingClass);
			} catch (Throwable t) {
				System.err.println("Raven could not get logger implementation " + logImplementation);
				t.printStackTrace();
				System.err.println("Disabling Raven logging");
				setImplementation(null);
			}
		}
		return (logInstance != null);
	}
}
