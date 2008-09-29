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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

import net.sf.taverna.interaction.workflow.InteractionPattern;
import net.sf.taverna.interaction.workflow.InteractionService;
import net.sf.taverna.interaction.workflow.impl.HTTPInteractionServiceProxy;

import org.embl.ebi.escience.scufl.DuplicatePortNameException;
import org.embl.ebi.escience.scufl.DuplicateProcessorNameException;
import org.embl.ebi.escience.scufl.InputPort;
import org.embl.ebi.escience.scufl.OutputPort;
import org.embl.ebi.escience.scufl.PortCreationException;
import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scufl.ProcessorCreationException;
import org.embl.ebi.escience.scufl.ScuflModel;

/**
 * Processor object representing an interaction request creator
 * 
 * @author Tom Oinn
 */
public class InteractionServiceProcessor extends Processor {

	private String baseURL, patternName;

	private InteractionPattern pattern = null;

	private InteractionService service = null;

	/**
	 * Create a new processor, connecting to the InteractionService to fetch the
	 * metadata for the specified pattern
	 * 
	 * @param baseURL
	 *            the base URL to the HTTP based interaction service
	 * @param patternName
	 *            full pattern name for the desired interaction pattern
	 * @param theModel
	 *            ScuflModel instance to bind this Processor to
	 * @param processorName
	 *            name for the Processor within the workflow
	 */
	public InteractionServiceProcessor(ScuflModel theModel,
			String processorName, String baseURL, String patternName)
			throws ProcessorCreationException, DuplicateProcessorNameException {
		super(theModel, processorName);
		this.baseURL = baseURL;
		this.patternName = patternName;
		URL baseURLObject = null;
		try {
			baseURLObject = new URL(baseURL);
		} catch (MalformedURLException mue) {
			throw new ProcessorCreationException("Can't connect, bad URL "
					+ mue.getMessage());
		}
		service = HTTPInteractionServiceProxy.connectTo(baseURLObject);
		InteractionPattern[] patterns = service.getInteractionPatterns();
		// Locate a pattern with the specified name
		for (int i = 0; i < patterns.length; i++) {
			if (patterns[i].getName().equals(patternName)) {
				pattern = patterns[i];
			}
		}
		if (pattern == null) {
			throw new ProcessorCreationException("Cannote locate an "
					+ "interaction pattern '" + patternName + "' in '"
					+ baseURL + "'");
		}
		setDescription(pattern.getDescription());
		try {
			InputPort emailPort = new InputPort(this, "emailAddress");
			emailPort.setSyntacticType("'text/plain'");
			addPort(emailPort);
			for (int i = 0; i < pattern.getInputNames().length; i++) {
				String inputName = pattern.getInputNames()[i];
				String inputType = pattern.getInputTypes()[i];
				InputPort p = new InputPort(this, inputName);
				p.setSyntacticType(inputType);
				addPort(p);
			}
			for (int i = 0; i < pattern.getOutputNames().length; i++) {
				String outputName = pattern.getOutputNames()[i];
				String outputType = pattern.getOutputTypes()[i];
				OutputPort p = new OutputPort(this, outputName);
				p.setSyntacticType(outputType);
				addPort(p);
			}
		} catch (DuplicatePortNameException dpne) {
			throw new ProcessorCreationException(
					"Duplicate port name in interaction processor");
		} catch (PortCreationException pce) {
			throw new ProcessorCreationException(
					"Unable to build port for some reason");
		}
	}

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

	/*
	 * Return the interaction pattern that will be used to generate
	 * InteractionRequest object when the corresponding task is invoked
	 */
	public InteractionPattern getInteractionPattern() {
		return this.pattern;
	}

	/**
	 * Return the interaction server proxy that this processor used to discover
	 * its interaction pattern
	 */
	public InteractionService getInteractionService() {
		return this.service;
	}

	/**
	 * Return the lifetime of this request in milliseconds, currently hardcoded
	 * to one day.
	 */
	public long getRequestLifetime() {
		return (long) (1000 * 60 * 60 * 24);
	}

	public Properties getProperties() {
		return new Properties();
	}

}
