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

import net.sf.taverna.t2.compatibility.activity.ActivityTranslationException;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityConfigurationException;

import org.embl.ebi.escience.scufl.Processor;

/**
 * <p>
 * An interface defining an ActivityTranslator tied to translating a Taverna 1 Processor into a corresponding Taverna 2 Activity.<br>
 * </p>
 * 
 * @author Stuart Owen
 * @author David Withers
 *
 * @param <ConfigurationType>
 */
public interface ActivityTranslator<ConfigurationType> {
	
	/**
	 * <p>
	 * The entry point for carrying out a translation from a Taverna 1 Processor to a Taverna 2 Activity.<br>
	 * </p>
	 * 
	 * @param processor
	 * @return a translated Activity
	 * @throws ActivityTranslationException
	 * @throws ActivityConfigurationException
	 */
	Activity<ConfigurationType> doTranslation(Processor processor) throws ActivityTranslationException,ActivityConfigurationException;
	
	/**
	 * Returns true if this ActivityTranslator can translate the specified Taverna 1 Processor.
	 * 
	 * @param processor
	 * @return true if this ActivityTranslator can translate the specified Taverna 1 Processor
	 */
	public boolean canHandle(Processor processor);
	
}
