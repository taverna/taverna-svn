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

import net.sf.taverna.t2.compatibility.activity.ActivityTranslator;

import org.embl.ebi.escience.scufl.Processor;

/**
 * <p>
 * An Exception indicating that a suitable {@link ActivityTranslator} cannot be found.
 * This will generally occur when no ActivityTranslator can be found for a given Taverna 1 Processor.
 * </p>
 * @author Stuart Owen
 *
 */
public class ActivityTranslatorNotFoundException extends Exception {

	private static final long serialVersionUID = 8779255468276952392L;
	
	private Processor taverna1Processor;

	/**
	 * @param msg a message describing the reason for the exception.
	 * @param tavena1Processor the Taverna 1 processor for which no translator could be found.
	 */
	public ActivityTranslatorNotFoundException(String msg, Processor taverna1Processor) {
		super(msg);
		this.taverna1Processor = taverna1Processor;
	}


	/**
	 * @return the Taverna 1 processor for which a translator could not be found
	 */
	public Processor getTaverna1Processor() {
		return taverna1Processor;
	}
	
	
}
