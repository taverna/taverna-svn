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
package net.sf.taverna.t2.compatibility;

/**
 * <p>
 * An Exception indicating that a critical error has occurred whilst trying to translate
 * a Taverna 1 Scufl workflow model into an equivalent Taverna 2 Dataflow.
 * </p>
 * 
 * @author Stuart Owen
 *
 */
public class WorkflowTranslationException extends Exception {

	private static final long serialVersionUID = -6167115193252256144L;

	/**
	 * @param msg a message describing the reason for the exception.
	 */
	public WorkflowTranslationException(String msg) {
		super(msg);
	}

	/**
	 * @param cause a previous exception that caused this WorkflowTranslationException to be thrown.
	 */
	public WorkflowTranslationException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param msg a message describing the reason for the exception.
	 * @param cause a previous exception that caused this WorkflowTranslationException to be thrown.
	 */
	public WorkflowTranslationException(String msg, Throwable cause) {
		super(msg, cause);
	}

}
