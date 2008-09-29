/*
 * Copyright 2005 Tom Oinn, EMBL-EBI
 *
 *  This file is part of Taverna.  Further information, and the
 *  latest version, can be found at http://taverna.sf.net
 * 
 *  Taverna is in turn part of the myGrid project, more details
 *  can be found at http://www.mygrid.org.uk
 *
 *  Taverna is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as
 *  published by the Free Software Foundation; either version 2.1
 *  of the License, or (at your option) any later version.
 *
 *  Taverna is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with Taverna; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package net.sf.taverna.interaction.workflow.processor;

import org.embl.ebi.escience.scuflworkers.ProcessorFactory;

/**
 * Holds data required to create an Interaction Service processor
 * 
 * @author Tom Oinn
 */
public class InteractionServiceProcessorFactory extends ProcessorFactory {

	private String baseURL, patternName;

	/**
	 * Return the base URL of the HTTP based interaction service
	 */
	public String getBaseURL() {
		return this.baseURL;
	}

	/**
	 * Return the pattern name used to instantiate this factory
	 */
	public String getPatternName() {
		return this.patternName;
	}

	/**
	 * Create a new processor factory
	 * 
	 * @param baseURL
	 *            the base URL to the HTTP based interaction service
	 * @param patternName
	 *            full pattern name for the desired interaction pattern
	 */
	public InteractionServiceProcessorFactory(String baseURL, String patternName) {
		this.baseURL = baseURL;
		this.patternName = patternName;
		// Set the factory name to be the last component in the
		// full name (i.e. foo.bar.pattern is named 'pattern')
		String[] parts = patternName.split("\\.");
		setName(parts[parts.length - 1]);
	}

	public String getProcessorDescription() {
		return "Connection to an interaction service";
	}

	public Class getProcessorClass() {
		return InteractionServiceProcessor.class;
	}

}
