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

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementation of LogInterface for a java.util.logging backend. To use this
 * implementation, do:
 * 
 * <pre>
 * Log.setImplementation(new JavaLog());
 * </pre>
 * 
 * <p>
 * To modify java.util.logging settings and get debug messages from Raven, you
 * can specify a system property
 * <code>-Djava.util.logging.config.file=blah/bluh/logging.conf</code> and in
 * <code>logging.conf</code> specify something like:
 * 
 * <pre>
 * handlers=java.util.logging.ConsoleHandler
 * #.level= FINEST
 * net.sf.taverna.level=FINEST
 * java.util.logging.ConsoleHandler.level = FINEST
 * java.util.logging.ConsoleHandler.formatter = java.util.logging.SimpleFormatter
 * </pre>
 * 
 * <p>
 * The default log level from Java is INFO, which for Raven means anything
 * logged by Log.warn(), Log.error() or Log.fatal(). The mappings between
 * LogInterface.Priority and levels of java.util.logging are:
 * 
 * <pre>
 * DEBUG --&gt; FINER
 * INFO --&gt; FINE
 * WARN --&gt; WARNING
 * ERROR --&gt; SEVERE
 * FATAL --&gt; SEVERE
 * </pre>
 * 
 * @author Stian Soiland-Reyes
 * 
 */
public class JavaLog implements LogInterface {
	private static Map<Priority, Level> priorityMap = new HashMap<Priority, Level>();
	static {
		priorityMap.put(Priority.DEBUG, Level.FINER);
		// Not Level.INFO - as that is printed by default
		priorityMap.put(Priority.INFO, Level.FINE);
		priorityMap.put(Priority.WARN, Level.WARNING);
		// java.util.logging don't have anything between WARNING and SEVERE
		priorityMap.put(Priority.ERROR, Level.SEVERE);
		priorityMap.put(Priority.FATAL, Level.SEVERE);
		// Should now have all priorities
		assert priorityMap.size() == Priority.values().length;
	}
	private Logger logger;
	private String callingClass;

	public JavaLog() {
		this(JavaLog.class);
	}

	@SuppressWarnings("unchecked")
	public JavaLog(Class c) {
		// We'll use the full classname as our identifier to be
		// slightly log4j compatible
		callingClass = c.getName();
		logger = Logger.getLogger(callingClass);
	}
	
	@SuppressWarnings("unchecked")
	public JavaLog getLogger(Class c) {
		return new JavaLog(c);
	}

	public void log(Priority p, Object msg, Throwable ex) {
		// We can supply the callingClass, but don't want to start
		// guessing the calling method
		if (ex != null) {
			logger.logp(priorityMap.get(p), callingClass, "?", msg.toString(),
					ex);
		} else {
			logger.logp(priorityMap.get(p), callingClass, "?", msg.toString());
		}
	}

}
