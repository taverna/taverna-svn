/*******************************************************************************
 * Copyright (C) 2008 The University of Manchester   
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
package net.sf.taverna.t2.service.webservice;

import java.io.PrintStream;

import org.apache.commons.logging.Log;

public class TestLog implements Log {
	
	private PrintStream out = System.out;

	public void debug(Object message) {
		out.println(message);
	}

	public void debug(Object message, Throwable t) {
		out.println(message);
	}

	public void error(Object message) {
		out.println(message);
	}

	public void error(Object message, Throwable t) {
		out.println(message);
	}

	public void fatal(Object message) {
		out.println(message);
	}

	public void fatal(Object message, Throwable t) {
		out.println(message);
	}

	public void info(Object message) {
		out.println(message);
	}

	public void info(Object message, Throwable t) {
		out.println(message);
	}

	public boolean isDebugEnabled() {
		return true;
	}

	public boolean isErrorEnabled() {
		return true;
	}

	public boolean isFatalEnabled() {
		return true;
	}

	public boolean isInfoEnabled() {
		return true;
	}

	public boolean isTraceEnabled() {
		return true;
	}

	public boolean isWarnEnabled() {
		return true;
	}

	public void trace(Object message) {
		out.println(message);
	}

	public void trace(Object message, Throwable t) {
		out.println(message);
	}

	public void warn(Object message) {
		out.println(message);
	}

	public void warn(Object message, Throwable t) {
		out.println(message);
	}

}
