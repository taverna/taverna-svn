/**
 * 
 */
package net.sf.taverna.raven.log;


public interface LogInterface {
	public static enum Priority {
		DEBUG,
		INFO,
		WARN,
		ERROR,
		FATAL,
	}
	public LogInterface getLogger(Class c);
	public void log(LogInterface.Priority p, Object msg, Throwable ex);
}

