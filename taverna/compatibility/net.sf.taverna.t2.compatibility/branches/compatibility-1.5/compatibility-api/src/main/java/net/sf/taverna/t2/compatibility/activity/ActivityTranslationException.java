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
package net.sf.taverna.t2.compatibility.activity;

/**
 * An Exception indicating problem translating a Taverna 1 Processor instance into an equivalent Taverna 2 Activity
 * 
 * @author Stuart Owen
 * @author David Withers
 *
 */
public class ActivityTranslationException extends Exception {

	private static final long serialVersionUID = 1L;

	/**
	 * Constructs the ActivityTranslationException instance with a message.
	 * 
	 * @param message the message explaining the cause of the problem.
	 */
	public ActivityTranslationException(String message) {
		super(message);
	}

	/**
	 * Constructs the ActivityTranslationException instance with the message and cause.
	 * 
	 * @param message the message explaining the cause of the problem.
	 * @param cause the root cause if this exception is being raised as the consequence of another Exception
	 */
	public ActivityTranslationException(String message, Throwable cause) {
		super(message, cause);
	}

}
