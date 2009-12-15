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

import net.sf.taverna.raven.repository.impl.LocalArtifactClassLoader;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * Implementation of LogInterface for a log4j backend. To use this
 * implementation, do:
 * 
 * <pre>
 * Log.setImplementation(new Log4jLog());
 * </pre>
 * 
 * Note that this depends on log4j being available through the system default
 * classloader (ie. normally on the CLASSPATH), as we can't load log4j through
 * Raven and at the same time use log4j for logging Raven internals, as that
 * would force recursive logging calls when log4j needs to load any of it's
 * classes.
 * <p>
 * (However, Raven can <b>download</b> log4j, this trick is used by Taverna's
 * Bootstrap class by including log4j in the profile with system="true", and
 * specifying BootstrapClassLoader as the system classloader).
 * 
 * @see net.sf.taverna.raven.appconfig.bootstrap.Bootstrap
 * @author Stian Soiland-Reyes
 */
public class Log4jLog implements LogInterface {
	// Mapping between LogInterface.Priority and Log4Js Levels, such as "Debug"
	// and "Warn"
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

	@SuppressWarnings("unchecked")
	public Log4jLog(Class c) {
		if (Logger.class.getClassLoader() instanceof LocalArtifactClassLoader) {
			throw new IllegalStateException(
					"log4j cannot be used as a Raven logger when loaded through Raven");
		}
		log4j = Logger.getLogger(c);
	}

	@SuppressWarnings("unchecked")
	public Log4jLog getLogger(Class c) {
		return new Log4jLog(c);
	}

	public void log(LogInterface.Priority p, Object msg, Throwable ex) {
		// FIXME: Will log wrong line number, as it will log the line below
		log4j.log(priorityMap.get(p), msg, ex);
	}
}
