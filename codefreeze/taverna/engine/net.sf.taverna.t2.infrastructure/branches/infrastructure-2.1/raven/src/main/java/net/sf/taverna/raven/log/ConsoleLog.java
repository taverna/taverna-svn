/*******************************************************************************
 * Copyright (C) 2007 The University of Manchester   
 * 
 *  Modifications to the initial code base are copyright of their
 *  respective authors, or their employers as appropriate.
 * 
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2.1 of
 *  the License, or (at your option) any later version.
 *    
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *    
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 ******************************************************************************/
package net.sf.taverna.raven.log;

import java.io.PrintStream;

/**
 * Implementation of LogInterface for simple System.err based printouts. To use
 * this implementation, do:
 * 
 * <pre>
 * Log.setImplementation(new ConsoleLog());
 * </pre>
 * 
 * <p>
 * This is a very simple implementation, that by default only logs messages at
 * WARN or higher. This threshold can be changed by setting the
 * <code>level</code> member. The destination output can be changed by setting
 * the <code>console</code> member. Stack traces will be printed by default if
 * provided, but this can be disabled by setting <code>printStackTrace</code>
 * to <code>false</code>
 * 
 * @author Stian Soiland-Reyes
 */
public class ConsoleLog implements LogInterface {
	/**
	 * Minimum level to log, ie. messages with lover priority will be discarded.
	 * By default set to Priority.WARN.
	 */
	public static Priority level = Priority.WARN;

	/**
	 * Where to print log messages. By default set to System.err.
	 */
	public static PrintStream console = System.err;

	/**
	 * Whether to print stacktraces if a Throwable was given with the log
	 * message. By default set to true.
	 */
	public boolean printStackTrace = true;

	// Class name to include in printout
	private String callingClass;

	public ConsoleLog() {
		this(ConsoleLog.class);
	}

	@SuppressWarnings("unchecked")
	public ConsoleLog(Class c) {
		this.callingClass = c.toString();
	}

	@SuppressWarnings("unchecked")
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
