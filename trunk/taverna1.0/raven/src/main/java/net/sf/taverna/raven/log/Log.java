package net.sf.taverna.raven.log;

public class Log {
	
	private static LogInterface logImplementation = new JavaLog();
	private LogInterface logger;
	private Class callingClass;

	private Log(Class c) {
		this.callingClass = c;
		this.logger = null;
	}

	public static void setImplementation(LogInterface logger) {
		logImplementation = logger;
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
			logger.log(priority, msg, ex);
		} catch (Throwable t) {
			System.err.println("Could not log to " + logger);
			t.printStackTrace();
		}
	}
		
	private synchronized boolean checkImplementation() {
		if (logImplementation == null) {
			return false;
		}
		if (logger == null || ! logImplementation.getClass().isInstance(logger)) {
			// Not yet initialized or implementation has changed
			try {
				logger = logImplementation.getLogger(callingClass);
			} catch (Throwable t) {
				System.err.println("Could not get logger implementation " + logImplementation);
				t.printStackTrace();
			}
		}
		return (logger != null);
	}
}
