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
package net.sf.taverna.t2.service;

/**
 * Exception indicating that a job state change is invalid, e.g. attempting to
 * change a job state from CANCELLED to RUNNING.
 * 
 * 
 * @author David Withers
 */
public class JobStateException extends RuntimeException {

	private static final long serialVersionUID = 1354592503475036308L;

	/**
	 * Constructs a <code>JobStateException</code> with no detail message.
	 */
	public JobStateException() {
		super();
	}

	/**
	 * Constructs a <code>JobStateException</code> with the specified detail
	 * message and cause.
	 */
	public JobStateException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructs a <code>JobStateException</code> with the specified detail
	 * message.
	 */
	public JobStateException(String message) {
		super(message);
	}

	/**
	 * Constructs a <code>JobStateException</code> with the specified cause.
	 */
	public JobStateException(Throwable cause) {
		super(cause);
	}

}
