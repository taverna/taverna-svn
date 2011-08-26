/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package net.sourceforge.taverna.scuflworkers.bsf;

import org.embl.ebi.escience.scuflworkers.ProcessorFactory;

/**
 * Implementation of ProcessorFactory that creates Beanshell nodes
 * 
 * @author Tom Oinn
 */
public class BSFProcessorFactory extends ProcessorFactory {

	private BSFProcessor prototype = null;

	/**
	 * Create a new factory
	 */
	public BSFProcessorFactory() {
		setName("BSF scripting host");

	}

	public BSFProcessorFactory(BSFProcessor prot) {
		setName("BSF scripting host");
		this.prototype = prot;
	}

	public BSFProcessor getPrototype() {
		return this.prototype;
	}

	/**
	 * Return a textual description of the factory
	 */
	public String getProcessorDescription() {
		return "A processor that allows arbitrary scripts";
	}

	/**
	 * Return the Class object for processors that would be created by this
	 * factory
	 */
	public Class getProcessorClass() {
		return net.sourceforge.taverna.scuflworkers.bsf.BSFProcessor.class;
	}

}