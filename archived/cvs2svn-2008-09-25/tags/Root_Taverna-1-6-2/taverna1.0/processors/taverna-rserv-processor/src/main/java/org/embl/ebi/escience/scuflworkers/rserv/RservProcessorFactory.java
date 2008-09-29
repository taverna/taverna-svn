/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Stian Soiland, myGrid
 */
package org.embl.ebi.escience.scuflworkers.rserv;

import org.embl.ebi.escience.scuflworkers.ProcessorFactory;

/**
 * Implementation of ProcessorFactory that creates Rserv nodes
 * 
 * @author Stian Soiland
 */
public class RservProcessorFactory extends ProcessorFactory {

	private RservProcessor prototype = null;

	public RservProcessorFactory() {
		setName("R script");
	}

	public RservProcessorFactory(RservProcessor prot) {
		this();
		this.prototype = prot;
	}

	public RservProcessor getPrototype() {
		return this.prototype;
	}

	public String getProcessorDescription() {
		return "Run R/S scripts through Rserve";
	}

	public Class getProcessorClass() {
		return org.embl.ebi.escience.scuflworkers.rserv.RservProcessor.class;
	}

}
