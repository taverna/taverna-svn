package net.sf.taverna.raven.log;

import java.io.PrintStream;

/**
 * Implementation of LogInterface for simple System.err based printouts.
 * To use this implementation, do:
 * <pre>
 * Log.setImplementation(new ConsoleLog());
 * </pre>
 * <p>
 * This is a very simple implementation, that by default
 * only logs messages at WARN or higher. This
 * threshold can be changed by setting the 
 * <code>level</code> member. The destination output
 * can be changed by setting the <code>console</code> member.
 * Stack traces will be printed by default if provided, but
 * this can be disabled by setting <code>printStackTrace</code> to
 * <code>false</code>
 * 
 * @author Stian Soiland
 */
public class ConsoleLog implements LogInterface {
	/**
	 *  Minimum level to log, ie. messages with lover 
	 *  priority will be discarded. By default set to
	 *  Priority.WARN.
	 */
	public static Priority level = Priority.WARN;
	
	/**
	 * Where to print log messages. By default set to
	 * System.err.
	 */
	public static PrintStream console = System.err;

	/**
	 * Whether to print stacktraces if a Throwable was given with the
	 * log message. By default set to true.
	 */
	public boolean printStackTrace = true;
	
	// Class name to include in printout
	private String callingClass;
	
	public ConsoleLog() {
		this(ConsoleLog.class);
	}
	
	public ConsoleLog(Class c) {
		this.callingClass = c.toString();
	}
	
	public LogInterface getLogger(Class c) {
		return new ConsoleLog(c);
	}
	
	public void log(LogInterface.Priority p, Object msg, Throwable ex) {
		if (p.compareTo(level) < 0) {
			return;
		}
		console.println(p + " (" + callingClass + "): " + msg);
		if (printStackTrace && ex != null) {
			ex.printStackTrace(console);
		}
	}
}
