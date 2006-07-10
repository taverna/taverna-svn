/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflworkers.inferno;

import org.embl.ebi.escience.scuflworkers.ProcessorFactory;

/**
 * Holds the data required to create an SGS based processor
 * 
 * @author Tom Oinn
 */
public class InfernoProcessorFactory extends ProcessorFactory {

	private String host;

	private int port;

	private String service;

	public String getHost() {
		return this.host;
	}

	public int getPort() {
		return this.port;
	}

	public String getService() {
		return this.service;
	}

	public InfernoProcessorFactory(String host, int port, String service) {
		this.host = host;
		this.port = port;
		this.service = service;
		setName(service);
	}

	public String getProcessorDescription() {
		return "Processor based on an Inferno SGS streaming service";
	}

	public Class getProcessorClass() {
		return org.embl.ebi.escience.scuflworkers.inferno.InfernoProcessor.class;
	}

}
