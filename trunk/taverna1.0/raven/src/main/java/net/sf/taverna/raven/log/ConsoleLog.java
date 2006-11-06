package net.sf.taverna.raven.log;

import java.io.PrintStream;

public class ConsoleLog implements LogInterface {
	// minimum level to log, ie. messages with lover priority will
	// be discarded
	public static Priority level = Priority.WARN;
	// Where to output log messages
	public static PrintStream console = System.err;

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
		ex.printStackTrace(console);
	}
}
