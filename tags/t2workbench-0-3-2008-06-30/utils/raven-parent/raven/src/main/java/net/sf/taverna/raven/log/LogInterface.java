/**
 * 
 */
package net.sf.taverna.raven.log;

/**
 * Interface for a logger to be used by {@link Log}, set by
 * {@link Log#setImplementation(LogInterface)}.
 * <p>
 * Implementations of this interface would typically interface 
 * a real logging system such as log4j.
 * 
 * @author Stian Soiland-Reyes
 * 
 */
public interface LogInterface {
	@SuppressWarnings("unchecked")
	/**
	 * Get a new instance of this logger to be used by class c.
	 * 
	 * @param c Class that is to use logger.
	 * 
	 */
	public LogInterface getLogger(Class c);

	/**
	 * Log a message.
	 * 
	 * @param p Priority
	 * @param msg Message
	 * @param ex Exception
	 */
	public void log(LogInterface.Priority p, Object msg, Throwable ex);

	/**
	 * Possible priorities. Priority levels would be mapped to the
	 * underlying priorities of the log system.
	 * 
	 */
	public static enum Priority {
		DEBUG, INFO, WARN, ERROR, FATAL,
	}
}
